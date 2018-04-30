package edu.jhuapl.saavtk.state.gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import edu.jhuapl.saavtk.state.State;
import edu.jhuapl.saavtk.state.StateKey;
import edu.jhuapl.saavtk.state.StateManager;
import edu.jhuapl.saavtk.state.StateManagerCollection;
import edu.jhuapl.saavtk.state.StateSerializer;
import edu.jhuapl.saavtk.state.Version;
import edu.jhuapl.saavtk.state.gson.GsonElement.ElementIO;

public class GsonFileStateSerializer implements StateSerializer
{
	private static final Version GSON_VERSION = Version.of(1, 0);
	private static final IterableIO ITERABLE_IO = new IterableIO();
	private static final MapIO MAP_IO = new MapIO();
	private static final GsonKeyIO STATE_KEY_IO = new GsonKeyIO();
	private static final StateIO STATE_IO = new StateIO();
	private static final GsonVersionIO VERSION_IO = new GsonVersionIO();
	private static final ElementIO ELEMENT_IO = new ElementIO();
	private static final Gson GSON = configureGson();

	private final StateManagerCollection managerCollection;

	public static GsonFileStateSerializer of()
	{
		return new GsonFileStateSerializer();
	}

	protected GsonFileStateSerializer()
	{
		this.managerCollection = StateManagerCollection.of();
	}

	/**
	 * Return a key based on the supplied identification string.
	 * 
	 * @param keyId the identification string of the key to be returned.
	 * @return the key
	 * 
	 * @throws NullPointerException if argument is null
	 */
	@Override
	public <T> StateKey<T> getKey(String keyId)
	{
		return GsonKey.of(keyId);
	}

	@Override
	public void register(StateKey<State> key, StateManager manager)
	{
		managerCollection.add(key, manager);
	}

	@Override
	public void load(File file) throws IOException
	{
		Preconditions.checkNotNull(file);

		State source = State.of(Version.of(0, 0));
		try (JsonReader reader = GSON.newJsonReader(new FileReader(file)))
		{
			reader.beginArray();
			while (reader.hasNext())
			{
				GsonElement element = GSON.fromJson(reader, ValueTypeInfo.ELEMENT.getType());
				source.put(element.getKey(), element.getValue());
			}
			reader.endArray();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (SwingUtilities.isEventDispatchThread())
		{
			retrieveInSwingContext(source);
		}
		else
		{
			retrieveInSingleThreadContext(source);
		}
	}

	@Override
	public void save(File file) throws IOException
	{
		Preconditions.checkNotNull(file);
		try (FileWriter fileWriter = new FileWriter(file))
		{
			try (JsonWriter jsonWriter = GSON.newJsonWriter(fileWriter))
			{
				jsonWriter.beginArray();
				for (StateKey<State> key : managerCollection.getKeys())
				{
					StateManager manager = managerCollection.getManager(key);
					State state = manager.store();
					GsonElement element = GsonElement.of(key, state);
					GSON.toJson(element, ValueTypeInfo.ELEMENT.getType(), jsonWriter);
				}
				jsonWriter.endArray();
				jsonWriter.flush();
				fileWriter.write("\n");
			}
		}
	}

	private void retrieveInSwingContext(State source)
	{
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> {
			for (StateKey<State> key : managerCollection.getKeys())
			{
				try
				{
					State element = source.get(key);
					if (element != null)
					{
						SwingUtilities.invokeAndWait(() -> {
							managerCollection.getManager(key).retrieve(element);
						});
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		executor.shutdown();
	}

	private void retrieveInSingleThreadContext(State source)
	{
		try
		{
			for (StateKey<State> key : managerCollection.getKeys())
			{
				State element = source.get(key);
				if (element != null)
				{
					managerCollection.getManager(key).retrieve(element);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static Gson configureGson()
	{
		GsonBuilder builder = new GsonBuilder();
		builder.serializeNulls();
		builder.setPrettyPrinting();
		builder.serializeSpecialFloatingPointValues();
		builder.registerTypeAdapter(ValueTypeInfo.SORTED_SET.getType(), ITERABLE_IO);
		builder.registerTypeAdapter(ValueTypeInfo.SET.getType(), ITERABLE_IO);
		builder.registerTypeAdapter(ValueTypeInfo.LIST.getType(), ITERABLE_IO);
		builder.registerTypeAdapter(ValueTypeInfo.SORTED_MAP.getType(), MAP_IO);
		builder.registerTypeAdapter(ValueTypeInfo.MAP.getType(), MAP_IO);
		builder.registerTypeAdapter(ValueTypeInfo.STATE_KEY.getType(), STATE_KEY_IO);
		builder.registerTypeAdapter(ValueTypeInfo.STATE.getType(), STATE_IO);
		builder.registerTypeAdapter(ValueTypeInfo.VERSION.getType(), VERSION_IO);
		builder.registerTypeAdapter(ValueTypeInfo.ELEMENT.getType(), ELEMENT_IO);
		return builder.create();
	}

	private static class TestManager implements StateManager
	{
		private final State state;

		TestManager(State state)
		{
			this.state = state;
		}

		@Override
		public State store()
		{
			State destination = State.of(state.getVersion());
			for (StateKey<?> key : state.getKeys())
			{
				@SuppressWarnings("unchecked")
				StateKey<Object> newKey = (StateKey<Object>) key;
				destination.put(newKey, state.get(key));
			}

			return destination;
		}

		@Override
		public void retrieve(State source)
		{
			state.clear();
			for (StateKey<?> key : source.getKeys())
			{
				Object value = source.get(key);
				@SuppressWarnings("unchecked")
				StateKey<Object> newKey = (StateKey<Object>) key;
				state.put(newKey, value);
			}
		}

	}

	public static void main(String[] args) throws IOException
	{
		GsonFileStateSerializer serializer = new GsonFileStateSerializer();

		String v3 = "Bennu / V3";
		State v3State = State.of(Version.of(3, 1));

		v3State.put(serializer.getKey("tab"), "1");
		v3State.put(serializer.getKey("facets"), 2000000001L);
		v3State.put(serializer.getKey("showBaseMap"), true);
		v3State.put(serializer.getKey("resolution"), -5.e64);
		v3State.put(serializer.getKey("int"), 20);
		v3State.put(serializer.getKey("long"), (long) 20);
		v3State.put(serializer.getKey("short"), (short) 20);
		v3State.put(serializer.getKey("byte"), (byte) 20);
		v3State.put(serializer.getKey("double"), (double) 20);
		v3State.put(serializer.getKey("float"), (float) 20);
		v3State.put(serializer.getKey("char"), (char) 20);
		v3State.put(serializer.getKey("boolean"), false);
		v3State.put(serializer.getKey("string"), "a string");
		v3State.put(serializer.getKey("stringNull"), null);
		v3State.put(serializer.getKey("longNull"), null);

		List<String> stringList = new ArrayList<>();
		stringList.add("String0");
		stringList.add(null);
		stringList.add("String2");
		StateKey<List<String>> stringListKey = serializer.getKey("stringList");
		v3State.put(stringListKey, stringList);

		List<Integer> intList = new ArrayList<>();
		intList.add(0);
		intList.add(null);
		intList.add(2);
		StateKey<List<Integer>> intListKey = serializer.getKey("intList");
		v3State.put(intListKey, intList);

		List<List<String>> listListString = new ArrayList<>();
		listListString.add(null);
		listListString.add(ImmutableList.of("X", "y", "z"));
		listListString.add(stringList);

		final StateKey<State> testStateKey = serializer.getKey("testState");
		final State state = State.of(GSON_VERSION);
		StateManager manager = new TestManager(state);

		StateKey<List<List<String>>> listListStringKey = serializer.getKey("listListString");
		state.put(serializer.getKey("Bennu / V3"), v3State);
		state.put(serializer.getKey("Current View"), v3);
		state.put(serializer.getKey("Tab Number"), new Integer(3));
		state.put(serializer.getKey("Current View2"), v3);
		state.put(listListStringKey, listListString);
		state.put(serializer.getKey("stringSet"), ImmutableSortedSet.of("liver", "spleen", "aardvark"));

		Map<Byte, Short> byteShortMap = new HashMap<>();
		byteShortMap.put((byte) 1, null);
		byteShortMap.put(null, (short) 12);
		byteShortMap.put((byte) 11, (short) 23);
		byteShortMap.put((byte) 10, (short) 17);
		StateKey<Map<Byte, Short>> byteShortMapKey = serializer.getKey("byteShortMap");
		state.put(byteShortMapKey, byteShortMap);

		File file = new File("/Users/peachjm1/Downloads/MyState.sbmt");
		serializer.register(testStateKey, manager);
		serializer.save(file);
		System.out.println("Original state is: " + state);

		State state2 = State.of(GSON_VERSION);
		serializer = GsonFileStateSerializer.of();
		manager = new TestManager(state2);
		serializer.register(testStateKey, manager);
		serializer.load(file);
		System.out.println("Reloaded state is: " + state2);
		if (state.equals(state2))
		{
			System.out.println("States were found equal");
		}
		else
		{
			System.err.println("States were not found equal");
		}
		State v3State2 = state2.get(serializer.getKey("Bennu / V3"));
		Long longNull = v3State2.get(serializer.getKey("longNull"));
		System.out.println("longNull is " + longNull);

		System.out.println("stringSet is " + state2.get(serializer.getKey("stringSet")));

		// This fails at runtime. If this were a real key templated on Long it would fail at compile time.
		//		Float fVal = v3State2.get(serializer.getKey("long"));

		// This doesn't fail at runtime or compile time but I wish it would:
		//		List<List<Integer>> unpackedListList = state2.get(serializer.getKey("listListString"));

		// But the following does fail to compile, which is probably good enough.
		//		unpackedListList = state2.get(listListStringKey);
		// And this fails at runtime, as it should.
		//		System.out.println(unpackedListList.get(1).get(1) * 7);

		// It would be OK if this were to work but it doesn't. 
		//		float fVal = v3State2.get(serializer.getKey("resolution"));

		// This one is supposed to throw an exception. 
		//		Short floatAsDouble = v3State2.get(serializer.getKey("facets"));
		//		System.out.println("float as double is " + floatAsDouble);
	}

}

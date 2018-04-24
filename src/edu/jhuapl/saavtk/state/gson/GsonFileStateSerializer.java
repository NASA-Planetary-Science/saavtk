package edu.jhuapl.saavtk.state.gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class GsonFileStateSerializer implements StateSerializer
{
	private static final IterableIO ITERABLE_IO = new IterableIO();
	private static final MapIO MAP_IO = new MapIO();
	private static final GsonKeyIO STATE_KEY_IO = new GsonKeyIO();
	private static final StateIO STATE_IO = new StateIO();
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
		try (JsonReader reader = GSON.newJsonReader(new FileReader(file)))
		{
			State sessionState = GSON.fromJson(reader, STATE_IO.getTargetType());
			managerCollection.setState(sessionState);
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
				State state = managerCollection.getState();
				GSON.toJson(state, STATE_IO.getTargetType(), jsonWriter);
				fileWriter.write('\n');
			}
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
		public State getState()
		{
			return state;
		}

		@Override
		public void setState(State newState)
		{
			for (StateKey<?> key : newState.getKeys())
			{
				Object value = newState.get(key);
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
		State v3State = State.of();
		//		v3State.put(StateKey.ofString("tab"), "1");
		//		v3State.put(StateKey.ofLong("facets"), 2000000001L);
		//		v3State.put(StateKey.ofBoolean("showBaseMap"), true);
		//		v3State.put(StateKey.ofDouble("resolution"), -5.e64);
		//		v3State.put(StateKey.ofInteger("int"), 20);
		//		v3State.put(StateKey.ofLong("long"), (long) 20);
		//		v3State.put(StateKey.ofShort("short"), (short) 20);
		//		v3State.put(StateKey.ofByte("byte"), (byte) 20);
		//		v3State.put(StateKey.ofDouble("double"), (double) 20);
		//		v3State.put(StateKey.ofFloat("float"), (float) 20);
		//		v3State.put(StateKey.ofCharacter("char"), (char) 20);
		//		v3State.put(StateKey.ofBoolean("boolean"), false);
		//		v3State.put(StateKey.ofString("string"), "a string");
		//		v3State.put(StateKey.ofString("stringNull"), null);
		//		v3State.put(StateKey.ofLong("longNull"), null);

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
		//		StateKey<List<String>> stringListKey = serializer.getKeyList("stringList", String.class);
		StateKey<List<String>> stringListKey = serializer.getKey("stringList");
		v3State.put(stringListKey, stringList);

		List<Integer> intList = new ArrayList<>();
		intList.add(0);
		intList.add(null);
		intList.add(2);
		//		StateKey<List<Integer>> intListKey = serializer.getKeyList("intList", Integer.class);
		StateKey<List<Integer>> intListKey = serializer.getKey("intList");
		v3State.put(intListKey, intList);

		List<List<String>> listStringList = new ArrayList<>();
		listStringList.add(null);
		listStringList.add(ImmutableList.of("X", "y", "z"));
		listStringList.add(stringList);

		final State state = State.of();
		StateManager manager = new TestManager(state);

		StateKey<List<List<String>>> listListStringKey = serializer.getKey("listListString");
		state.put(serializer.getKey("Bennu / V3"), v3State);
		state.put(serializer.getKey("Current View"), v3);
		state.put(listListStringKey, listStringList);
		state.put(serializer.getKey("stringSet"), ImmutableSortedSet.of("liver", "spleen", "aardvark"));

		//		Map<Byte, Short> byteShortMap = new HashMap<>();
		//		byteShortMap.put((byte) 1, null);
		//		byteShortMap.put(null, (short) 12);
		//		byteShortMap.put((byte) 11, (short) 23);
		//		byteShortMap.put((byte) 10, (short) 17);
		//		StateKey<Map<Byte, Short>> byteShortMapKey = serializer.getKey("byteShortMap");
		//		state.put(byteShortMapKey, byteShortMap);

		System.out.println("Original state is: " + state);

		File file = new File("/Users/peachjm1/Downloads/MyState.sbmt");
		serializer.register(serializer.getKey("testState"), manager);
		serializer.save(file);

		State state2 = State.of();
		serializer = GsonFileStateSerializer.of();
		manager = new TestManager(state2);
		serializer.register(serializer.getKey("testState"), manager);
		serializer.load(file);
		System.out.println("Reloaded state is: " + state2);
		if (state.equals(state2))
		{
			System.out.println("States are considered equal");
		}
		State v3State2 = state2.get(serializer.getKey("Bennu / V3"));
		Long longNull = v3State2.get(serializer.getKey("longNull"));
		System.out.println("longNull is " + longNull);

		//		Float fVal = v3State2.get(serializer.getKey("long"));

		System.out.println("stringSet is " + state2.get(serializer.getKey("stringSet")));

		// This doesn't fail but I wish it would:
		List<List<Integer>> unpackedListList = state2.get(serializer.getKey("listListString"));

		// But the following does fail to compile, which is probably good enough.
		//		unpackedListList = state2.get(listListStringKey);
		// And this fails at runtime, as it should.
		//		System.out.println(unpackedListList.get(1).get(1) * 7);

		// This one is supposed to succeed. 
		//		float fVal = v3State2.get(serializer.getKey("resolution"));

		// This one is supposed to throw an exception. 
		Short floatAsDouble = v3State2.get(serializer.getKey("facets"));
		System.out.println("float as double is " + floatAsDouble);
	}

}

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
		builder.registerTypeAdapter(STATE_IO.getTargetType(), STATE_IO);
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

		v3State.put(GsonKey.of("tab"), "1");
		v3State.put(GsonKey.of("facets"), 2000000001L);
		v3State.put(GsonKey.of("showBaseMap"), true);
		v3State.put(GsonKey.of("resolution"), -5.e64);
		v3State.put(GsonKey.of("int"), 20);
		v3State.put(GsonKey.of("long"), (long) 20);
		v3State.put(GsonKey.of("short"), (short) 20);
		v3State.put(GsonKey.of("byte"), (byte) 20);
		v3State.put(GsonKey.of("double"), (double) 20);
		v3State.put(GsonKey.of("float"), (float) 20);
		v3State.put(GsonKey.of("char"), (char) 20);
		v3State.put(GsonKey.of("boolean"), false);
		v3State.put(GsonKey.of("string"), "a string");
		v3State.put(GsonKey.of("stringNull"), null);
		v3State.put(GsonKey.of("longNull"), null);

		List<String> stringList = new ArrayList<>();
		stringList.add("String0");
		stringList.add(null);
		stringList.add("String2");
		//		StateKey<List<String>> stringListKey = GsonKey.ofList("stringList", String.class);
		StateKey<List<String>> stringListKey = GsonKey.of("stringList");
		v3State.put(stringListKey, stringList);

		List<Integer> intList = new ArrayList<>();
		intList.add(0);
		intList.add(null);
		intList.add(2);
		//		StateKey<List<Integer>> intListKey = GsonKey.ofList("intList", Integer.class);
		StateKey<List<Integer>> intListKey = GsonKey.of("intList");
		v3State.put(intListKey, intList);

		List<List<String>> listStringList = new ArrayList<>();
		listStringList.add(null);
		listStringList.add(ImmutableList.of("X", "y", "z"));
		listStringList.add(stringList);

		final State state = State.of();
		StateManager manager = new TestManager(state);
		//		state.put(GsonKey.ofState("Bennu / V3"), v3State);
		//		state.put(GsonKey.ofString("Current View"), v3);
		//		state.put(GsonKey.ofList("listStringList", ArrayList.class), listStringList);
		//		state.put(GsonKey.ofSortedSet("stringSet", String.class), ImmutableSortedSet.of("liver", "spleen", "aardvark"));

		StateKey<List<List<String>>> listListStringKey = GsonKey.of("listListString");
		state.put(GsonKey.of("Bennu / V3"), v3State);
		state.put(GsonKey.of("Current View"), v3);
		//		state.put(GsonKey.of("listStringList"), listStringList);
		state.put(listListStringKey, listStringList);
		state.put(GsonKey.of("stringSet"), ImmutableSortedSet.of("liver", "spleen", "aardvark"));

		//		Map<Byte, Short> byteShortMap = new HashMap<>();
		//		byteShortMap.put((byte) 1, null);
		//		byteShortMap.put(null, (short) 12);
		//		byteShortMap.put((byte) 11, (short) 23);
		//		byteShortMap.put((byte) 10, (short) 17);
		//		StateKey<Map<Byte, Short>> byteShortMapKey = GsonKey.of("byteShortMap");
		//		state.put(byteShortMapKey, byteShortMap);

		System.out.println("Original state is: " + state);

		File file = new File("/Users/peachjm1/Downloads/MyState.sbmt");
		StateSerializer serializer = GsonFileStateSerializer.of();
		serializer.register(GsonKey.of("testState"), manager);
		serializer.save(file);

		State state2 = State.of();
		serializer = GsonFileStateSerializer.of();
		manager = new TestManager(state2);
		serializer.register(GsonKey.of("testState"), manager);
		serializer.load(file);
		System.out.println("Reloaded state is: " + state2);
		if (state.equals(state2))
		{
			System.out.println("States are considered equal");
		}
		State v3State2 = state2.get(GsonKey.of("Bennu / V3"));
		Long longNull = v3State2.get(GsonKey.of("longNull"));
		System.out.println("longNull is " + longNull);

		//		Float fVal = v3State2.get(GsonKey.of("long"));

		System.out.println("stringSet is " + state2.get(GsonKey.of("stringSet")));

		// This doesn't fail but I wish it would:
		List<List<Integer>> unpackedListList = state2.get(GsonKey.of("listListString"));

		// But the following does fail to compile, which is probably good enough.
		//		unpackedListList = state2.get(listListStringKey);
		// And this fails at runtime, as it should.
		//		System.out.println(unpackedListList.get(1).get(1) * 7);

		// This one is supposed to succeed. 
		//		float fVal = v3State2.get(GsonKey.of("resolution"));

		// This one is supposed to throw an exception. 
		Short floatAsDouble = v3State2.get(GsonKey.of("facets"));
		System.out.println("float as double is " + floatAsDouble);
	}

}

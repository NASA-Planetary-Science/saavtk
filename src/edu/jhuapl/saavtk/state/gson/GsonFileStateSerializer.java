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
import edu.jhuapl.saavtk.state.StateSerializer;

public class GsonFileStateSerializer implements StateSerializer
{
	private static final StateIO STATE_IO = new StateIO();
	private static final Gson GSON = configureGson();

	private final File file;

	public static GsonFileStateSerializer of(File file)
	{
		return new GsonFileStateSerializer(file);
	}

	protected GsonFileStateSerializer(File file)
	{
		Preconditions.checkNotNull(file);
		this.file = file;
	}

	@Override
	public State load() throws IOException
	{
		try (JsonReader reader = GSON.newJsonReader(new FileReader(file)))
		{
			return GSON.fromJson(reader, STATE_IO.getTargetType());
		}
	}

	@Override
	public void save(State state) throws IOException
	{
		Preconditions.checkNotNull(state);
		try (FileWriter fileWriter = new FileWriter(file))
		{
			try (JsonWriter jsonWriter = GSON.newJsonWriter(fileWriter))
			{
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

		v3State.put(StateKey.of("tab"), "1");
		v3State.put(StateKey.of("facets"), 2000000001L);
		v3State.put(StateKey.of("showBaseMap"), true);
		v3State.put(StateKey.of("resolution"), -5.e64);
		v3State.put(StateKey.of("int"), 20);
		v3State.put(StateKey.of("long"), (long) 20);
		v3State.put(StateKey.of("short"), (short) 20);
		v3State.put(StateKey.of("byte"), (byte) 20);
		v3State.put(StateKey.of("double"), (double) 20);
		v3State.put(StateKey.of("float"), (float) 20);
		v3State.put(StateKey.of("char"), (char) 20);
		v3State.put(StateKey.of("boolean"), false);
		v3State.put(StateKey.of("string"), "a string");
		v3State.put(StateKey.of("stringNull"), null);
		v3State.put(StateKey.of("longNull"), null);

		List<String> stringList = new ArrayList<>();
		stringList.add("String0");
		stringList.add(null);
		stringList.add("String2");
		//		StateKey<List<String>> stringListKey = StateKey.ofList("stringList", String.class);
		StateKey<List<String>> stringListKey = StateKey.of("stringList");
		v3State.put(stringListKey, stringList);

		List<Integer> intList = new ArrayList<>();
		intList.add(0);
		intList.add(null);
		intList.add(2);
		//		StateKey<List<Integer>> intListKey = StateKey.ofList("intList", Integer.class);
		StateKey<List<Integer>> intListKey = StateKey.of("intList");
		v3State.put(intListKey, intList);

		List<List<String>> listStringList = new ArrayList<>();
		listStringList.add(null);
		listStringList.add(ImmutableList.of("X", "y", "z"));
		listStringList.add(stringList);

		State state = State.of();
		//		state.put(StateKey.ofState("Bennu / V3"), v3State);
		//		state.put(StateKey.ofString("Current View"), v3);
		//		state.put(StateKey.ofList("listStringList", ArrayList.class), listStringList);
		//		state.put(StateKey.ofSortedSet("stringSet", String.class), ImmutableSortedSet.of("liver", "spleen", "aardvark"));

		StateKey<List<List<String>>> listListStringKey = StateKey.of("listListString");
		state.put(StateKey.of("Bennu / V3"), v3State);
		state.put(StateKey.of("Current View"), v3);
		//		state.put(StateKey.of("listStringList"), listStringList);
		state.put(listListStringKey, listStringList);
		state.put(StateKey.of("stringSet"), ImmutableSortedSet.of("liver", "spleen", "aardvark"));

		//		Map<Byte, Byte> byteByteMap = new HashMap<>();
		//		byteByteMap.put((byte) 1, null);
		//		byteByteMap.put(null, (byte) 12);
		//		byteByteMap.put((byte) 11, (byte) 23);
		//		byteByteMap.put((byte) 10, (byte) 17);
		//		StateKey<Map<Byte, Byte>> byteByteMapKey = StateKey.of("byteByteMap");
		//		state.put(byteByteMapKey, byteByteMap);

		System.out.println("Original state is: " + state);

		StateSerializer serializer = of(new File("/Users/peachjm1/Downloads/MyState.sbmt"));
		serializer.save(state);

		State state2 = serializer.load();
		System.out.println("Reloaded state is: " + state2);
		if (state.equals(state2))
		{
			System.out.println("States are considered equal");
		}
		State v3State2 = state2.get(StateKey.of("Bennu / V3"));
		Long longNull = v3State2.get(StateKey.of("longNull"));
		System.out.println("longNull is " + longNull);

		//		Float fVal = v3State2.get(StateKey.of("long"));

		System.out.println("stringSet is " + state2.get(StateKey.of("stringSet")));

		// This doesn't fail but I wish it would:
		List<List<Integer>> unpackedListList = state2.get(StateKey.of("listListString"));

		// But the following does fail to compile, which is probably good enough.
		//		unpackedListList = state2.get(listListStringKey);
		System.out.println(unpackedListList.get(1).get(1) * 7);

		// This one is supposed to succeed. 
		//		float fVal = v3State2.get(StateKey.of("resolution"));

		// This one is supposed to throw an exception. 
		Short floatAsDouble = v3State2.get(StateKey.of("facets"));
		System.out.println("float as double is " + floatAsDouble);
	}
}

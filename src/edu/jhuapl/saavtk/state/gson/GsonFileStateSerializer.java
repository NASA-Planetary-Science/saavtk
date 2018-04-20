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
		try (JsonWriter writer = GSON.newJsonWriter(new FileWriter(file)))
		{
			GSON.toJson(state, STATE_IO.getTargetType(), writer);
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
		v3State.put(StateKey.ofString("tab"), "1");
		v3State.put(StateKey.ofLong("facets"), 2000000001L);
		v3State.put(StateKey.ofBoolean("showBaseMap"), true);
		v3State.put(StateKey.ofDouble("resolution"), -5.e64);
		v3State.put(StateKey.ofInteger("int"), 20);
		v3State.put(StateKey.ofLong("long"), (long) 20);
		v3State.put(StateKey.ofShort("short"), (short) 20);
		v3State.put(StateKey.ofByte("byte"), (byte) 20);
		v3State.put(StateKey.ofDouble("double"), (double) 20);
		v3State.put(StateKey.ofFloat("float"), (float) 20);
		v3State.put(StateKey.ofCharacter("char"), (char) 20);
		v3State.put(StateKey.ofBoolean("boolean"), false);
		v3State.put(StateKey.ofString("string"), "a string");
		v3State.put(StateKey.ofString("stringNull"), null);
		v3State.put(StateKey.ofLong("longNull"), null);

		List<String> stringList = new ArrayList<>();
		stringList.add("String0");
		stringList.add(null);
		stringList.add("String2");
		StateKey<List<String>> stringListKey = StateKey.ofList("stringList", String.class);
		v3State.put(stringListKey, stringList);

		List<Integer> intList = new ArrayList<>();
		intList.add(0);
		intList.add(null);
		intList.add(2);
		StateKey<List<Integer>> intListKey = StateKey.ofList("intList", Integer.class);
		v3State.put(intListKey, intList);

		List<List<String>> listStringList = new ArrayList<>();
		listStringList.add(null);
		listStringList.add(ImmutableList.of("X", "y", "z"));
		listStringList.add(stringList);

		State state = State.of();
		state.put(StateKey.ofState("Bennu / V3"), v3State);
		state.put(StateKey.ofString("Current View"), v3);
		state.put(StateKey.ofList("listStringList", ArrayList.class), listStringList);
		state.put(StateKey.ofSortedSet("stringSet", String.class), ImmutableSortedSet.of("liver", "spleen", "aardvark"));
		System.out.println("Original state is: " + state);

		StateSerializer serializer = of(new File("/Users/peachjm1/Downloads/MyState.sbmt"));
		serializer.save(state);

		State state2 = serializer.load();
		System.out.println("Reloaded state is: " + state2);
		if (state.equals(state2))
		{
			System.out.println("States are considered equal");
		}
		State v3State2 = state2.get(StateKey.ofState("Bennu / V3"));
		Long longNull = v3State2.get(StateKey.ofLong("longNull"));
		System.out.println("longNull is " + longNull);
		Float dVal = v3State2.get(StateKey.ofFloat("long"));

		System.out.println("stringSet is " + state2.get(StateKey.ofSortedSet("stringSet", String.class)));

		// This doesn't fail but I wish it would:
		List<List<Integer>> unpackedListList = state2.get(StateKey.ofList("listStringList", List.class));
		System.out.println(unpackedListList);

		// This one is supposed to succeed. 
		dVal = v3State2.get(StateKey.ofFloat("resolution"));

		// This one is supposed to throw an exception. 
		Short floatAsDouble = v3State2.get(StateKey.ofShort("facets"));
		System.out.println("float as double is " + floatAsDouble);
	}
}

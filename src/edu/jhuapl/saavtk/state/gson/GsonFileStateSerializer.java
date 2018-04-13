package edu.jhuapl.saavtk.state.gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
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
		builder.registerTypeAdapter(STATE_IO.getTargetType(), STATE_IO);
		return builder.create();
	}

	public static void main(String[] args) throws IOException
	{
		String v3 = "Bennu / V3";
		State v3State = State.of(ImmutableMap.of(new StateKey<>("tab"), "1"));
		v3State.put(new StateKey<>("facets"), 2000000001L);
		v3State.put(new StateKey<>("showBaseMap"), true);
		v3State.put(new StateKey<>("resolution"), 20.);
		v3State.put(new StateKey<>("int"), 20);
		v3State.put(new StateKey<>("long"), (long) 20);
		v3State.put(new StateKey<>("short"), (short) 20);
		v3State.put(new StateKey<>("byte"), (byte) 20);
		v3State.put(new StateKey<>("double"), (double) 20);
		v3State.put(new StateKey<>("float"), (float) 20);
		v3State.put(new StateKey<>("char"), (char) 20);
		v3State.put(new StateKey<>("boolean"), false);
		v3State.put(new StateKey<>("string"), "a string");

		ImmutableMap<StateKey<?>, Object> map = ImmutableMap.of(new StateKey<>("Current View"), v3, new StateKey<>("Bennu / V3"), v3State);
		State state = State.of(map);
		System.out.println("Original state is: " + state);

		StateSerializer serializer = of(new File("/Users/peachjm1/Downloads/MyState.sbmt"));
		serializer.save(state);

		State state2 = serializer.load();
		System.out.println("Reloaded state is: " + state2);
		if (state.equals(state2))
		{
			System.out.println("States are considered equal");
		}
	}
}

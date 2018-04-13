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

import edu.jhuapl.saavtk.state.Attribute;
import edu.jhuapl.saavtk.state.State;
import edu.jhuapl.saavtk.state.StateKey;
import edu.jhuapl.saavtk.state.StateSerializer;
import edu.jhuapl.saavtk.state.StringAttribute;

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
		Attribute v3 = new StringAttribute("Bennu / V3");
		Attribute v3State = State.of(ImmutableMap.of(new StateKey<>("tab"), new StringAttribute("1")));
		ImmutableMap<StateKey<?>, Attribute> map = ImmutableMap.of(new StateKey<>("Current View"), v3, new StateKey<>("Bennu / V3"), v3State);
		State state = State.of(map);

		StateSerializer serializer = of(new File("/Users/peachjm1/Downloads/MyState.sbmt"));
		serializer.save(state);

		State state2 = serializer.load();
		System.out.println(state2);
	}
}

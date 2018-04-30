package edu.jhuapl.saavtk.state;

import edu.jhuapl.saavtk.state.gson.GsonFileStateSerializer;

public class Serializers
{
	private static final StateSerializer INSTANCE = GsonFileStateSerializer.of();

	public static StateSerializer getDefault()
	{
		return INSTANCE;
	}
}

package edu.jhuapl.saavtk.metadata;

import edu.jhuapl.saavtk.state.gson.GsonFileStateSerializer;

public class Serializers
{
	private static final Serializer INSTANCE = GsonFileStateSerializer.of();

	public static Serializer getDefault()
	{
		return INSTANCE;
	}
}

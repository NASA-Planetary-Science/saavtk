package edu.jhuapl.saavtk.metadata;

import edu.jhuapl.saavtk.state.gson.GsonSerializer;

public class Serializers
{
	private static final Serializer INSTANCE = GsonSerializer.of();

	public static Serializer getDefault()
	{
		return INSTANCE;
	}
}

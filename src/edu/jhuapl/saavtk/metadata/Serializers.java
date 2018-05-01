package edu.jhuapl.saavtk.metadata;

import edu.jhuapl.saavtk.metadata.gson.GsonSerializer;

public class Serializers
{
	private static final Serializer INSTANCE = GsonSerializer.of();

	public static Serializer getDefault()
	{
		return INSTANCE;
	}

	public static Serializer of()
	{
		return GsonSerializer.of();
	}

}

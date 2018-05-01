package edu.jhuapl.saavtk.metadata;

import java.io.File;
import java.io.IOException;

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

	public static void serialize(String metadataId, MetadataManager manager, File file) throws IOException
	{
		Serializer serializer = of();
		serializer.register(Key.of(metadataId), manager);
		serializer.save(file);
	}

}

package crucible.crust.metadata.impl.gson;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Preconditions;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Serializer;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.FixedMetadata;
import crucible.crust.metadata.impl.SettableMetadata;

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
		Preconditions.checkNotNull(metadataId);
		Preconditions.checkNotNull(manager);
		Preconditions.checkNotNull(file);

		Serializer serializer = of();
		serializer.register(Key.of(metadataId), manager);
		serializer.save(file);
	}

	public static void serialize(String metadataId, Metadata metadata, File file) throws IOException
	{
		Preconditions.checkNotNull(metadataId);
		Preconditions.checkNotNull(metadata);
		Preconditions.checkNotNull(file);

		serialize(metadataId, new MetadataManager() {

			@Override
			public Metadata store()
			{
				return metadata;
			}

			@Override
			public void retrieve(@SuppressWarnings("unused") Metadata source)
			{
				throw new UnsupportedOperationException();
			}

		}, file);
	}

	public static void deserialize(File file, String metadataId, MetadataManager manager) throws IOException
	{
		Preconditions.checkNotNull(file);
		Preconditions.checkNotNull(metadataId);
		Preconditions.checkNotNull(manager);

		Serializer serializer = of();
		Key<Metadata> key = Key.of(metadataId);
		serializer.register(key, manager);
		serializer.load(file);
		serializer.deregister(key);
	}

	public static FixedMetadata deserialize(File file, String metadataId) throws IOException
	{
		Preconditions.checkNotNull(file);
		Preconditions.checkNotNull(metadataId);

		class DirectMetadataManager implements MetadataManager
		{
			final SettableMetadata metadata = SettableMetadata.of(Version.of(0, 1));

			@Override
			public Metadata store()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public void retrieve(Metadata source)
			{
				for (Key<?> key : source.getKeys())
				{
					put(key, source.get(key), metadata);
				}
			}

		}

		DirectMetadataManager manager = new DirectMetadataManager();
		deserialize(file, metadataId, manager);
		return FixedMetadata.of(manager.metadata);
	}

	@SuppressWarnings("unchecked")
	private static <T> void put(Key<?> key, T object, SettableMetadata metadata)
	{
		metadata.put((Key<T>) key, object);
	}
}

package edu.jhuapl.saavtk.metadata;

import java.util.Map;

import com.google.common.collect.ImmutableList;

public class MetadataView extends BasicMetadata
{
	public static MetadataView of(BasicMetadata metadata)
	{
		if (metadata instanceof MetadataView)
		{
			return (MetadataView) metadata;
		}
		return new MetadataView(metadata);
	}

	private final BasicMetadata metadata;

	private MetadataView(BasicMetadata metadata)
	{
		super(metadata.getVersion());
		this.metadata = metadata;
	}

	@Override
	public ImmutableList<Key<?>> getKeys()
	{
		return metadata.getKeys();
	}

	@Override
	protected Map<Key<?>, Object> getMap()
	{
		return metadata.getMap();
	}

}

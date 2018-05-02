package edu.jhuapl.saavtk.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.MetadataManager;
import edu.jhuapl.saavtk.metadata.Version;

public final class CustomizableColoringDataManager implements ColoringDataManager
{
	private static final Version METADATA_VERSION = Version.of(1, 0);

	public static CustomizableColoringDataManager of(String dataId)
	{
		return new CustomizableColoringDataManager(dataId, ImmutableList.of());
	}

	public static CustomizableColoringDataManager of(String dataId, Iterable<? extends ColoringData> coloringData)
	{
		return new CustomizableColoringDataManager(dataId, coloringData);
	}

	private final BasicColoringDataManager builtIn;
	private final BasicColoringDataManager custom;
	private final BasicColoringDataManager all;

	private CustomizableColoringDataManager(String id, Iterable<? extends ColoringData> builtIn)
	{
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(builtIn);
		this.builtIn = BasicColoringDataManager.of(id + " (built-in)", builtIn);
		this.custom = BasicColoringDataManager.of(id + " (custom)");
		this.all = BasicColoringDataManager.of(id);
		update();
	}

	@Override
	public String getId()
	{
		return all.getId();
	}

	@Override
	public ImmutableList<String> getNames()
	{
		return all.getNames();
	}

	@Override
	public ImmutableList<Integer> getResolutions()
	{
		return all.getResolutions();
	}

	@Override
	public boolean has(String name, int numberElements)
	{
		return all.has(name, numberElements);
	}

	@Override
	public ColoringData get(String name, int numberElements)
	{
		return all.get(name, numberElements);
	}

	@Override
	public void clear()
	{
		custom.clear();
		update();
	}

	@Override
	public void add(ColoringData data)
	{
		custom.add(data);
		update();
	}

	MetadataManager getMetadataManager()
	{
		return new MetadataManager() {

			@Override
			public Metadata store()
			{
				Metadata result = Metadata.of(METADATA_VERSION);
				result.put(Key.of(builtIn.getId()), builtIn.getMetadataManager().store());
				result.put(Key.of(custom.getId()), custom.getMetadataManager().store());
				return result;
			}

			@Override
			public void retrieve(Metadata source)
			{
				clear();
			}

		};
	}

	private final void update()
	{
		all.clear();
		addAll(builtIn);
		addAll(custom);
	}

	private final void addAll(ColoringDataManager other)
	{
		for (String name : other.getNames())
		{
			for (int numberElements : other.getResolutions())
			{
				if (other.has(name, numberElements) && !all.has(name, numberElements))
				{
					ColoringData data = other.get(name, numberElements);
					all.add(data);
				}
			}
		}
	}
}

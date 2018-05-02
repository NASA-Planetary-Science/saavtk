package edu.jhuapl.saavtk.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.MetadataManager;
import edu.jhuapl.saavtk.metadata.Version;

public class BasicColoringDataManager implements ColoringDataManager
{
	private static final Version METADATA_VERSION = Version.of(1, 0);

	public static BasicColoringDataManager of(String dataId)
	{
		return new BasicColoringDataManager(dataId, ImmutableList.of());
	}

	public static BasicColoringDataManager of(String dataId, Iterable<? extends ColoringData> coloringData)
	{
		return new BasicColoringDataManager(dataId, coloringData);
	}

	private final String dataId;
	private final List<String> names;
	private final List<Integer> resolutions;
	private final Table<String, Integer, ColoringData> dataTable;

	private BasicColoringDataManager(String dataId, Iterable<? extends ColoringData> coloringData)
	{
		this.dataId = dataId;
		this.names = new ArrayList<>();
		this.resolutions = new ArrayList<>();
		this.dataTable = TreeBasedTable.create();
		for (ColoringData data : coloringData)
		{
			add(data);
		}
	}

	@Override
	public String getId()
	{
		return dataId;
	}

	@Override
	public ImmutableList<String> getNames()
	{
		return ImmutableList.copyOf(names);
	}

	@Override
	public ImmutableList<Integer> getResolutions()
	{
		return ImmutableList.copyOf(resolutions);
	}

	@Override
	public boolean has(String name, int numberElements)
	{
		Preconditions.checkNotNull(name);
		return dataTable.contains(name, numberElements);
	}

	@Override
	public ColoringData get(String name, int numberElements)
	{
		Preconditions.checkNotNull(name);
		ColoringData result = dataTable.get(name, numberElements);
		if (result == null)
		{
			throw new IllegalArgumentException("Cannot find coloring for " + name + " (" + numberElements + " elements)");
		}
		return result;
	}

	@Override
	public final void add(ColoringData data)
	{
		Metadata metadata = data.getMetadata();
		String name = metadata.get(ColoringData.NAME);
		Integer numberElements = metadata.get(ColoringData.NUMBER_ELEMENTS);
		if (dataTable.contains(name, numberElements))
		{
			throw new IllegalArgumentException("Duplicated coloring for " + name + " (" + numberElements + " elements)");
		}
		if (!dataTable.rowKeySet().contains(name))
		{
			names.add(name);
		}
		if (!dataTable.columnKeySet().contains(numberElements))
		{
			resolutions.add(numberElements);
		}
		dataTable.put(name, numberElements, data);
	}

	@Override
	public void clear()
	{
		names.clear();
		resolutions.clear();
		dataTable.clear();
	}

	MetadataManager getMetadataManager()
	{
		return new MetadataManager() {

			@Override
			public Metadata store()
			{
				Metadata result = Metadata.of(METADATA_VERSION);
				ImmutableList.Builder<Metadata> builder = ImmutableList.builder();
				for (String name : names)
				{
					for (int numberElements : resolutions)
					{
						if (has(name, numberElements))
						{
							builder.add(get(name, numberElements).getMetadata());
						}
					}
				}
				result.put(Key.of(dataId), builder.build());
				return result;
			}

			@Override
			public void retrieve(Metadata source)
			{
				clear();
			}

		};
	}

}

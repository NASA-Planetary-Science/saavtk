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

public class ColoringDataManager
{
	private static final Version METADATA_VERSION = Version.of(1, 0);

	public static ColoringDataManager of(String dataId)
	{
		return new ColoringDataManager(dataId, ImmutableList.of());
	}

	public static ColoringDataManager of(String dataId, Iterable<ColoringData> coloringData)
	{
		return new ColoringDataManager(dataId, coloringData);
	}

	private final String dataId;
	private final List<String> names;
	private final Table<String, Integer, ColoringData> dataTable;
	private ImmutableList<Integer> resolutions;

	private ColoringDataManager(String dataId, Iterable<ColoringData> coloringData)
	{
		this.dataId = dataId;
		this.names = new ArrayList<>();
		this.dataTable = TreeBasedTable.create();
		this.resolutions = ImmutableList.of();
		for (ColoringData data : coloringData)
		{
			add(data);
		}
	}

	public ImmutableList<String> getNames()
	{
		return ImmutableList.copyOf(names);
	}

	public ImmutableList<Integer> getNumberElements()
	{
		return resolutions;
	}

	public boolean has(String name, int resolutionLevel)
	{
		Preconditions.checkNotNull(name);
		return dataTable.contains(name, resolutions.get(resolutionLevel));
	}

	public ColoringData get(String name, int resolutionLevel)
	{
		Preconditions.checkNotNull(name);
		ColoringData result = dataTable.get(name, resolutions.get(resolutionLevel));
		if (result == null)
		{
			throw new IllegalArgumentException("Cannot find coloring for " + name + " (" + resolutions + " elements)");
		}
		return result;
	}

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
		dataTable.put(name, numberElements, data);
		this.resolutions = ImmutableList.copyOf(dataTable.columnKeySet());
	}

	void clear()
	{
		names.clear();
		dataTable.clear();
		resolutions = ImmutableList.of();
	}

	MetadataManager getMetadataManager()
	{
		return new MetadataManager() {

			@Override
			public Metadata store()
			{
				Metadata result = Metadata.of(METADATA_VERSION);
				ImmutableList.Builder<Metadata> builder = ImmutableList.builder();
				for (ColoringData data : dataTable.values())
				{
					builder.add(data.getMetadata());
				}
				result.put(Key.of(dataId), builder.build());
				return result;
			}

			@Override
			public void retrieve(Metadata source)
			{
				names.clear();
				resolutions.clear();
				dataTable.clear();

			}

		};
	}

}

package edu.jhuapl.saavtk.model;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.MetadataManager;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
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
	private final SortedSet<Integer> resolutions;
	private final Table<String, Integer, ColoringData> dataTable;

	private BasicColoringDataManager(String dataId, Iterable<? extends ColoringData> coloringData)
	{
		this.dataId = dataId;
		this.names = new ArrayList<>();
		this.resolutions = new TreeSet<>();
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
	public ImmutableList<ColoringData> get(int numberElements)
	{
		ImmutableList.Builder<ColoringData> builder = ImmutableList.builder();
		for (String name : getNames())
		{
			if (has(name, numberElements))
			{
				builder.add(get(name, numberElements));
			}
		}
		return builder.build();
	}

	@Override
	public BasicColoringDataManager copy()
	{
		ImmutableList.Builder<ColoringData> builder = ImmutableList.builder();
		for (String name : names)
		{
			for (Integer resolution : resolutions)
			{
				ColoringData coloringData = dataTable.get(name, resolution);
				if (coloringData != null)
				{
					builder.add(coloringData);
				}
			}
		}
		return new BasicColoringDataManager(dataId, builder.build());
	}

	public final boolean has(ColoringData data)
	{
		Preconditions.checkNotNull(data);
		return dataTable.get(data.getName(), data.getNumberElements()) == data;
	}

	public final void add(ColoringData data)
	{
		String name = data.getName();
		Integer numberElements = data.getNumberElements();
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

	public final void remove(ColoringData data)
	{
		String name = data.getName();
		Integer numberElements = data.getNumberElements();
		dataTable.remove(name, numberElements);

		if (!dataTable.rowKeySet().contains(name))
		{
			names.remove(name);
		}

		if (!dataTable.columnKeySet().contains(numberElements))
		{
			resolutions.remove(numberElements);
		}
	}

	public final void replace(ColoringData data)
	{
		String name = data.getName();
		Integer numberElements = data.getNumberElements();
		if (!dataTable.contains(name, numberElements))
		{
			throw new IllegalArgumentException("Cannot replace coloring " + name + " (" + numberElements + " elements)");
		}
		dataTable.put(name, numberElements, data);
	}

	public void clear()
	{
		names.clear();
		resolutions.clear();
		dataTable.clear();
	}

	public MetadataManager getMetadataManager()
	{
		return new MetadataManager() {

			@Override
			public Metadata store()
			{
				SettableMetadata result = SettableMetadata.of(METADATA_VERSION);
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
				List<Metadata> metadataList = source.get(Key.of(dataId));
				for (Metadata metadata : metadataList)
				{
					add(ColoringData.of(metadata));
				}
			}

		};
	}

}

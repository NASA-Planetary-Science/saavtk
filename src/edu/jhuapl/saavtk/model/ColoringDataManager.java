package edu.jhuapl.saavtk.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
	private final Set<Integer> numberElements;
	private final Table<String, Integer, ColoringData> data;

	private ColoringDataManager(String dataId, Iterable<ColoringData> coloringData)
	{
		this.dataId = dataId;
		this.names = new ArrayList<>();
		this.numberElements = new TreeSet<>();
		this.data = TreeBasedTable.create();
		for (ColoringData data : coloringData)
		{
			add(data);
		}
	}

	public ImmutableList<String> getNames()
	{
		return ImmutableList.copyOf(names);
	}

	public ImmutableSet<Integer> getNumberElements()
	{
		return ImmutableSet.copyOf(numberElements);
	}

	public boolean has(String name, int numberElements)
	{
		return data.contains(name, numberElements);
	}

	public ColoringData get(String name, int numberElements)
	{
		ColoringData result = data.get(name, numberElements);
		if (result == null)
		{
			throw new IllegalArgumentException("Cannot find coloring for " + name + " (" + numberElements + " elements)");
		}
		return result;
	}

	public final void add(ColoringData data)
	{
		Metadata metadata = data.getMetadata();
		String name = metadata.get(ColoringData.NAME);
		Integer numberElements = metadata.get(ColoringData.NUMBER_ELEMENTS);
		if (this.data.contains(name, numberElements))
		{
			throw new IllegalArgumentException("Duplicated coloring for " + name + " (" + numberElements + " elements)");
		}
		if (!this.data.rowKeySet().contains(name))
		{
			this.names.add(name);
		}
		this.numberElements.add(numberElements);
		this.data.put(name, numberElements, data);
	}

	public MetadataManager getMetadataManager()
	{
		return new MetadataManager() {

			@Override
			public Metadata store()
			{
				Metadata result = Metadata.of(METADATA_VERSION);
				ImmutableList.Builder<Metadata> builder = ImmutableList.builder();
				for (ColoringData data : data.values())
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
				numberElements.clear();
				data.clear();

			}

		};
	}

}

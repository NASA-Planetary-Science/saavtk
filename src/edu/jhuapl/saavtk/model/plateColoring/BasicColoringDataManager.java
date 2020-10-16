package edu.jhuapl.saavtk.model.plateColoring;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;

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

	/**
	 * Return the default coloring metadata file name for the provided version of
	 * the serializer.
	 * 
	 * @param serializerVersion the version of the serializer that will be used to
	 *            write and read the coloring metadata.
	 * @return the file name
	 */
	public static String getMetadataFileName(Version serializerVersion)
	{
		return "coloring-" + METADATA_VERSION + ".smd";
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
		remove(data.getName(), data.getNumberElements());
	}

	/**
	 * Replaces a coloring data object with another of the same resolution. The
	 * object to be replaced is explicitly identified only by its name (and
	 * implicitly by the resolution of the new data object supplied).
	 * <p>
	 * Other than the resolution, all other attributes of the new data object,
	 * including the name, may be different from the corresponding attributes of the
	 * object being replaced.
	 * 
	 * @param oldName the name of the old coloring to replace
	 * @param newData the new coloring data object
	 * @throws IllegalArgumentException if there is no coloring with the given name
	 *             and the same resolution as the supplied new coloring object
	 * @throws NullPointerException if either argument is null
	 */
	public final void replace(String oldName, ColoringData newData)
	{
		int index = names.indexOf(oldName);
		Preconditions.checkArgument(index >= 0);

		Integer resolution = newData.getNumberElements();
		Preconditions.checkArgument(dataTable.contains(oldName, resolution));

		// Remove the old data.
		remove(oldName, resolution);

		String newName = newData.getName();

		// If the new name is already present (i.e., if there are other colorings with
		// the same name but different resolutions), need not do anything to the
		// names list.
		if (!names.contains(newName))
		{
			// Add the new name at the same index where the old name
			// was removed.
			names.add(index, newName);
		}

		// Resolutions is a set, so just make sure this resolution is still present
		// in the set. Don't need to worry about the order.
		resolutions.add(resolution);

		dataTable.put(newName, resolution, newData);
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
							builder.add(FileBasedColoringData.of(get(name, numberElements)).getMetadata());
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
					add(FileBasedColoringData.of(metadata));
				}
			}

		};
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(getId());
		builder.append(" colorings: ");
		boolean startingLoop = true;
		for (String name : names)
		{
			if (!startingLoop)
				builder.append(", ");
			builder.append(name);

			builder.append(" [");
			startingLoop = true;
			ImmutableList<Integer> resolutions = ImmutableList.copyOf(this.resolutions);
			for (int index = 0; index < resolutions.size(); ++index)
			{
				if (!startingLoop)
					builder.append(", ");
				Integer numberElements = resolutions.get(index);
				if (has(name, numberElements))
				{
					builder.append(index);
				}
				startingLoop = false;
			}
			builder.append("]");
			startingLoop = false;
		}
		return builder.toString();
	}

	private void remove(String name, Integer resolution)
	{
		dataTable.remove(name, resolution);

		boolean moreWithSameName = false;
		boolean moreWithSameRes = false;
		for (String eachName : dataTable.rowKeySet())
		{
			for (Integer eachRes : dataTable.columnKeySet())
			{
				if (name.equals(eachName))
				{
					moreWithSameName = true;
				}
				if (resolution.equals(eachRes))
				{
					moreWithSameRes = true;
				}
			}
		}
		if (!moreWithSameName)
		{
			names.remove(name);
		}
		if (!moreWithSameRes)
		{
			resolutions.remove(resolution);
		}
	}

}

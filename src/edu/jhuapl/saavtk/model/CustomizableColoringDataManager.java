package edu.jhuapl.saavtk.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.MetadataManager;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;

public final class CustomizableColoringDataManager implements ColoringDataManager
{
	private static final Version METADATA_VERSION = Version.of(1, 0);

	public static CustomizableColoringDataManager of(String dataId)
	{
		return new CustomizableColoringDataManager(dataId, BasicColoringDataManager.of(dataId + " (built-in)"), BasicColoringDataManager.of(dataId + " (custom)"));
	}

	public static CustomizableColoringDataManager of(String dataId, Iterable<? extends ColoringData> builtIn)
	{
		return new CustomizableColoringDataManager(dataId, BasicColoringDataManager.of(dataId + " (built-in)", builtIn), BasicColoringDataManager.of(dataId + " (custom)"));
	}

	private final BasicColoringDataManager builtIn;
	private final BasicColoringDataManager custom;
	private final BasicColoringDataManager all;

	private CustomizableColoringDataManager(String dataId, BasicColoringDataManager builtIn, BasicColoringDataManager custom)
	{
		Preconditions.checkNotNull(dataId);
		Preconditions.checkNotNull(builtIn);
		this.builtIn = builtIn;
		this.custom = custom;
		this.all = BasicColoringDataManager.of(dataId);
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
	public ImmutableList<ColoringData> get(int numberElements)
	{
		return all.get(numberElements);
	}

	@Override
	public CustomizableColoringDataManager copy()
	{
		return new CustomizableColoringDataManager(all.getId(), builtIn.copy(), custom.copy());
	}

	public boolean isBuiltIn(ColoringData data)
	{
		return builtIn.has(data);
	}

	public boolean isCustom(ColoringData data)
	{
		return custom.has(data);
	}

	public void addBuiltIn(ColoringData data)
	{
		builtIn.add(data);
		update();
	}

	public void addCustom(ColoringData data)
	{
		custom.add(data);
		update();
	}

	public void removeCustom(ColoringData data)
	{
		custom.remove(data);
		update();
	}

	public void replaceCustom(String oldName, ColoringData newData)
	{
		custom.replace(oldName, newData);
		update();
	}

	public void clearCustom()
	{
		custom.clear();
		update();
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(getId());
		builder.append(" colorings: ");
		boolean startingLoop = true;
		boolean noBuiltInHasName = false;
		for (String name : all.getNames())
		{
			// Special flagging for custom colorings.
			if (!builtIn.getNames().contains(name))
				noBuiltInHasName = true;

			if (!startingLoop)
				builder.append(", ");

			builder.append(name);

			if (noBuiltInHasName)
				builder.append(" (custom)");

			builder.append(" [");
			startingLoop = true;
			ImmutableList<Integer> resolutions = ImmutableList.copyOf(all.getResolutions());
			for (int index = 0; index < resolutions.size(); ++index)
			{
				if (!startingLoop)
					builder.append(", ");
				Integer numberElements = resolutions.get(index);
				if (has(name, numberElements))
				{
					builder.append(index);
					if (!noBuiltInHasName && custom.has(name, numberElements))
					{
						builder.append(" (custom)");
					}
				}
				startingLoop = false;
			}
			builder.append("]");
			startingLoop = false;
		}
		return builder.toString();
	}

	MetadataManager getMetadataManager(boolean includeCustom)
	{
		return new MetadataManager() {

			@Override
			public Metadata store()
			{
				SettableMetadata result = SettableMetadata.of(METADATA_VERSION);
				result.put(Key.of(builtIn.getId()), builtIn.getMetadataManager().store());
				if (includeCustom)
				{
					result.put(Key.of(custom.getId()), custom.getMetadataManager().store());
				}
				return result;
			}

			@Override
			public void retrieve(Metadata source)
			{
				builtIn.clear();
				builtIn.getMetadataManager().retrieve(source.get(Key.of(builtIn.getId())));
				if (includeCustom)
				{
					custom.clear();
					custom.getMetadataManager().retrieve(source.get(Key.of(custom.getId())));
				}
				update();
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

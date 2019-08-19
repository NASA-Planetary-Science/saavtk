package edu.jhuapl.saavtk.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;

/**
 * A Config is a class for storing models should be instantiated together for a
 * specific tool. Should be subclassed for each tool application instance. This
 * class is also used when creating (to know which tabs to create).
 */
public abstract class ViewConfig implements Cloneable
{
	public String modelLabel;
	public boolean customTemporary = false;
	public ShapeModelType author; // e.g. Gaskell
	public String version; // e.g. 2.0
	public ShapeModelBody body; // e.g. EROS or ITOKAWA

	public boolean useMinimumReferencePotential = false; // uses average otherwise
	public boolean hasCustomBodyCubeSize = false;
	// if hasCustomBodyCubeSize is true, the following must be filled in and valid
	public double customBodyCubeSize; // km
	private ImmutableList<String> smallBodyLabelPerResolutionLevel;
	private ImmutableList<Integer> smallBodyNumberOfPlatesPerResolutionLevel;
	private boolean enabled = true;

	public abstract boolean isAccessible();

	protected ViewConfig(Iterable<String> resolutionLabels, Iterable<Integer> resolutionNumberElements)
	{
		setResolution(resolutionLabels, resolutionNumberElements);
	}

	public final void setResolution(Iterable<Integer> resolutionNumberElements)
	{
		Preconditions.checkNotNull(resolutionNumberElements);
		ImmutableList<Integer> numberElementsList = ImmutableList.copyOf(resolutionNumberElements);
		ImmutableList.Builder<String> builder = ImmutableList.builder();
		for (Integer numberElements : numberElementsList)
		{
			builder.add(numberElements + " plates");
		}
		setResolution(builder.build(), numberElementsList);
	}

	public final void setResolution(Iterable<String> resolutionLabels, Iterable<Integer> resolutionNumberElements)
	{
		Preconditions.checkNotNull(resolutionLabels);
		Preconditions.checkNotNull(resolutionNumberElements);
		ImmutableList<String> labelList = ImmutableList.copyOf(resolutionLabels);
		ImmutableList<Integer> numberElementsList = ImmutableList.copyOf(resolutionNumberElements);
		Preconditions.checkArgument(labelList.size() > 0);
		Preconditions.checkArgument(labelList.size() == numberElementsList.size());
		this.smallBodyLabelPerResolutionLevel = labelList;
		this.smallBodyNumberOfPlatesPerResolutionLevel = numberElementsList;
	}

	public ImmutableList<String> getResolutionLabels()
	{
		return smallBodyLabelPerResolutionLevel;
	}

	public ImmutableList<Integer> getResolutionNumberElements()
	{
		return smallBodyNumberOfPlatesPerResolutionLevel;
	}

	@Override
	public ViewConfig clone() // throws CloneNotSupportedException
	{
		ViewConfig c = null;
		try
		{
			c = (ViewConfig) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new AssertionError(e);
		}
		return c;
	}

	/**
	 * Return a unique name for this model. No other model may have this name. Note
	 * that only applies within built-in models or custom models but a custom model
	 * can share the name of a built-in one or vice versa. By default simply return
	 * the author concatenated with the name if the author is not null or just the
	 * name if the author is null.
	 * 
	 * @return
	 */
	public String getUniqueName()
	{
		if (ShapeModelType.CUSTOM == author)
			return author + "/" + modelLabel;
		else
			return "DefaultName";
	}

	public String getShapeModelName()
	{
		if (author == ShapeModelType.CUSTOM)
			return modelLabel;
		else
		{
			String ver = "";
			if (version != null)
				ver += " (" + version + ")";
			return "DefaultName" + ver;
		}
	}

    public abstract String[] getShapeModelFileNames();

	public boolean isEnabled()
	{
		return enabled;
	}

	public void enable(boolean enabled)
	{
		this.enabled = enabled;
	}

    private static ConfigArrayList builtInConfigs = new ConfigArrayList();
	private static String firstTimeDefaultModel = null;

    public static ConfigArrayList getBuiltInConfigs()
	{
		return builtInConfigs;
	}

	/**
	 * Get a Config of a specific name and author. Note a Config is uniquely
	 * described by its name, author, and version. No two small body configs can
	 * have all the same. This version of the function assumes the version is null
	 * (unlike the other version in which you can specify the version).
	 *
	 * @param name
	 * @param author
	 * @return
	 */
    public static ViewConfig getConfig(ShapeModelBody name, ShapeModelType author)
	{
		return getConfig(name, author, null);
	}

	/**
	 * Get a Config of a specific name, author, and version. Note a Config is
	 * uniquely described by its name, author, and version. No two small body
	 * configs can have all the same.
	 *
	 * @param name
	 * @param author
	 * @param version
	 * @return
	 */
    public static ViewConfig getConfig(ShapeModelBody name, ShapeModelType author, String version)
	{
		for (ViewConfig config : getBuiltInConfigs())
		{
			if (config.body == name && config.author == author && ((config.version == null && version == null) || (version != null && version.equals(config.version))))
				return config;
		}

		System.err.println("Error: Cannot find Config with name " + name + " and author " + author + " and version " + version);

		return null;
	}

	public static String getFirstTimeDefaultModelName()
	{
		Preconditions.checkState(firstTimeDefaultModel != null || !builtInConfigs.isEmpty());
		if (firstTimeDefaultModel == null)
		{
			firstTimeDefaultModel = builtInConfigs.get(0).getUniqueName();
		}
		return firstTimeDefaultModel;
	}

	public static void setFirstTimeDefaultModelName(String firstTimeDefaultModel)
	{
		Preconditions.checkState(ViewConfig.firstTimeDefaultModel == null);
		ViewConfig.firstTimeDefaultModel = firstTimeDefaultModel;
	}

	@Override
	public String toString()
	{
		return getUniqueName();
	}
}

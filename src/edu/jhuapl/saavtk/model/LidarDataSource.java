package edu.jhuapl.saavtk.model;

/**
 * Immutable class used to define a lidar data source.
 * 
 * @author lopeznr1
 */
public class LidarDataSource
{
	// Constants
	/** Invalid LidarDataSource */
	public static final LidarDataSource Invalid = new LidarDataSource("Invalid", "Invalid");

	// Attributes
	private final String name;
	private final String path;

	/**
	 * Standard Constructor
	 */
	public LidarDataSource(String aName, String aPath)
	{
		name = aName;
		path = aPath;
	}

	public String getName()
	{
		return name;
	}

	public String getPath()
	{
		return path;
	}

	@Override
	public String toString()
	{
		String str = name + " (" + path + ")";
		return str;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LidarDataSource other = (LidarDataSource) obj;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (path == null)
		{
			if (other.path != null)
				return false;
		}
		else if (!path.equals(other.path))
			return false;
		return true;
	}

}

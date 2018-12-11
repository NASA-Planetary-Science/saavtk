package edu.jhuapl.saavtk.metadata.api;

import com.google.common.base.Preconditions;

public final class Version implements Comparable<Version>
{
	public static Version of(int major, int minor)
	{
		return new Version(major, minor);
	}

	public static Version of(String versionString)
	{
		Preconditions.checkNotNull(versionString);
		Preconditions.checkArgument(versionString.matches("^\\d+\\.\\d+$"));
		String[] versionInfo = versionString.split("\\.", 2);
		if (versionInfo.length != 2)
		{
			throw new IllegalArgumentException();
		}
		return of(Integer.parseInt(versionInfo[0]), Integer.parseInt(versionInfo[1]));
	}

	private final int major;
	private final int minor;

	private Version(int major, int minor)
	{
		Preconditions.checkArgument(major >= 0 && minor >= 0);
		this.major = major;
		this.minor = minor;
	}

	public int getMajor()
	{
		return major;
	}

	public int getMinor()
	{
		return minor;
	}

	@Override
	public int compareTo(Version that)
	{
		int result = 1;
		if (that != null)
		{
			result = Integer.compare(this.major, that.major);
			if (result == 0)
			{
				result = Integer.compare(this.minor, that.minor);
			}
		}
		return result;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + major;
		result = prime * result + minor;
		return result;
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof Version))
		{
			return false;
		}
		Version that = (Version) other;
		return (this.major == that.major && this.minor == that.minor);
	}

	@Override
	public String toString()
	{
		return major + "." + minor;
	}

}
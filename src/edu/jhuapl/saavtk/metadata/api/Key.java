package edu.jhuapl.saavtk.metadata.api;

import com.google.common.base.Preconditions;

/**
 * @param <T> type of the object that may be associated with this key, used for
 *            compile-time safety only
 */
public class Key<T> implements Comparable<Key<?>>
{
	/**
	 * Return a key based on the supplied identification string.
	 * 
	 * @param keyId the identification string of the key to be returned.
	 * @return the key
	 * 
	 * @throws NullPointerException if argument is null
	 */
	public static <T> Key<T> of(String keyId)
	{
		return new Key<>(keyId);
	}

	private final String keyId;

	protected Key(String keyId)
	{
		Preconditions.checkNotNull(keyId);
		Preconditions.checkArgument(keyId.matches("^\\S.*"));
		Preconditions.checkArgument(keyId.matches(".*\\S$"));
		this.keyId = keyId;
	}

	public String getId()
	{
		return keyId;
	}

	@Override
	public final int compareTo(Key<?> that)
	{
		if (that == null)
			return 1;
		return this.getId().compareTo(that.getId());
	}

	@Override
	public final int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getId().hashCode();
		return result;
	}

	@Override
	public final boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (other instanceof Key)
		{
			Key<?> that = (Key<?>) other;
			return this.getId().equals(that.getId());
		}
		return false;
	}

	@Override
	public String toString()
	{
		return keyId;
	}

}

package edu.jhuapl.saavtk.metadata;

import java.util.Map;

import com.google.common.base.Preconditions;

/**
 * Base implementation that assumes/requires all metadata keys to be stored in a
 * standard {@link java.util.Map}. Implementations are provided for all
 * {@link Metadata} methods except copy. An additional protected abtract method,
 * getMap() is provided so that subclasses may provide the map used by this
 * impementation. To ensure invariants are preserved in sublasses, all methods
 * that rely on this implementation's contract are final.
 */
public abstract class BasicMetadata implements Metadata
{
	/**
	 * Object used to represent null. By proxying null with this object, it is
	 * possible to use any {@link java.util.Map} implementation for key-value pairs.
	 */
	private static final Object NULL_OBJECT = new Object() {
		@Override
		public String toString()
		{
			// Deliberately capitalizing this so that the astute debugger has a chance of
			// noticing that this object is not actually a null pointer.
			return "NULL";
		}
	};

	private final Version version;

	protected BasicMetadata(Version version)
	{
		Preconditions.checkNotNull(version);
		this.version = version;
	}

	/**
	 * Provide the map of keys to values. Subclasses are free to provide any
	 * {@link java.util.Map} implementation, regardless of that implementation's
	 * null policy and whether it is mutable or immutable.
	 * 
	 * @return the map of keys to values
	 */
	protected abstract Map<Key<?>, Object> getMap();

	@Override
	public final Version getVersion()
	{
		return version;
	}

	@Override
	public final boolean hasKey(Key<?> key)
	{
		Preconditions.checkNotNull(key);
		return getMap().containsKey(key);
	}

	@Override
	public final <V> V get(Key<V> key)
	{
		Preconditions.checkNotNull(key);
		Object object = getMap().get(key);
		if (object == null)
		{
			throw new IllegalArgumentException("FixedMetadata does not contain key " + key);
		}
		if (NULL_OBJECT == object)
		{
			return null;
		}
		@SuppressWarnings("unchecked")
		V result = (V) object;
		return result;
	}

	@Override
	public abstract BasicMetadata copy();

	@Override
	public final int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getMap().hashCode();
		return result;
	}

	@Override
	public final boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (other instanceof BasicMetadata)
		{
			BasicMetadata that = (BasicMetadata) other;
			return this.getMap().equals(that.getMap());
		}
		return false;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("(Metadata) version ");
		builder.append(getVersion());
		for (Key<?> key : getKeys())
		{
			builder.append("\n");
			builder.append(key + " = " + get(key));
		}
		return builder.toString();
	}

	protected static Object getNullObject()
	{
		return NULL_OBJECT;
	}

}

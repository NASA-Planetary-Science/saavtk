package edu.jhuapl.saavtk.config;

/**
 * @param <ValueType>
 *            Type of value associated with this key.
 */
public final class Key<ValueType>
{
	public static <ValueType> Key<ValueType> of(String identifier)
	{
		return new Key<>(identifier);
	}

	private final String identifier;

	private Key(String identifier)
	{
		if (identifier == null)
			throw new NullPointerException();
		this.identifier = identifier;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	/**
	 * Use Object's hashCode to ensure each key matches only itself.
	 */
	@Override
	public final int hashCode()
	{
		return super.hashCode();
	}

	/**
	 * Use Object's equals (==) to ensure each key matches only itself.
	 */
	@Override
	public final boolean equals(Object obj)
	{
		return super.equals(obj);
	}

	@Override
	public String toString()
	{
		return "Key: " + identifier;
	}
}
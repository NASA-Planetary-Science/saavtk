package edu.jhuapl.saavtk.config;

/**
 * A key for accessing objects of a specific type in a map or configuration.
 * Instances may be used as if they were enumerated constants, but without
 * the need to limit values to a fixed set at compile-time. Since hashCode
 * and equals just call the superclass (Object) implementation, and thus
 * equals behaves as ==, instances should be declared as static final.
 * 
 * @author peachjm1
 *
 * @param <ValueType> the type of value that may be retrieved with this Key.
 * Note that, while this parameter is not actually referenced in Key,
 * it is essential so that Keys identify the type of object they may access.
 */
public final class Key<ValueType>
{
	/**
	 * Create a new key from the given identifier.
	 * @param identifier cosmetic string identifier used for this key
	 * @return the new key.
	 * @throws NullPointerException if identifier is null
	 */
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

	/**
	 * Return this Key's identifier string.
	 * @return the identifier
	 */
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
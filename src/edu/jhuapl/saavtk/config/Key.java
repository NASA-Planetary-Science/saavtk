package edu.jhuapl.saavtk.config;

/**
 * @param <ValueType>
 *            Type of value associated with this key.
 */
public final class Key<ValueType>
{
	public static <ValueType> Key<ValueType> of(String label)
	{
		return new Key<>(label);
	}

	private final String label;

	private Key(String label)
	{
		if (label == null)
			throw new NullPointerException();
		this.label = label;
	}

	public String getLabel()
	{
		return label;
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
		return "Key: " + label;
	}
}
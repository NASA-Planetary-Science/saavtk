package edu.jhuapl.saavtk.config;

public final class Entry<T>
{
	private final Key<T> key;
	private final T value;

	public static <T> Entry<T> of(Key<T> key, T value)
	{
		return new Entry<>(key, value);
	}

	private Entry(Key<T> key, T value)
	{
		if (key == null || value == null)
			throw new NullPointerException();
		this.key = key;
		this.value = value;
	}

	public Key<T> getKey()
	{
		return key;
	}

	public T getValue()
	{
		return value;
	}

	@Override
	public String toString()
	{
		return "ConfigurationEntry [key=" + key + ", value=" + value + "]";
	}
}

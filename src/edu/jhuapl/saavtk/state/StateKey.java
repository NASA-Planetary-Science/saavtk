package edu.jhuapl.saavtk.state;

import java.util.List;

import com.google.common.base.Preconditions;

public final class StateKey<V> implements Comparable<StateKey<?>>
{
	public static StateKey<State> ofState(String keyId)
	{
		return new StateKey<>(keyId, State.class, null);
	}

	public static <T> StateKey<List<T>> ofList(String keyId, Class<T> secondaryClass)
	{
		// Need to tap dance around Java's type erasure.
		StateKey<?> genericKey = of(keyId, List.class, secondaryClass);
		@SuppressWarnings("unchecked")
		StateKey<List<T>> result = (StateKey<List<T>>) genericKey;
		return result;
	}

	public static StateKey<String> ofString(String keyId)
	{
		return new StateKey<>(keyId, String.class, null);
	}

	public static StateKey<Integer> ofInteger(String keyId)
	{
		return new StateKey<>(keyId, Integer.class, null);
	}

	public static StateKey<Long> ofLong(String keyId)
	{
		return new StateKey<>(keyId, Long.class, null);
	}

	public static StateKey<Short> ofShort(String keyId)
	{
		return new StateKey<>(keyId, Short.class, null);
	}

	public static StateKey<Byte> ofByte(String keyId)
	{
		return new StateKey<>(keyId, Byte.class, null);
	}

	public static StateKey<Double> ofDouble(String keyId)
	{
		return new StateKey<>(keyId, Double.class, null);
	}

	public static StateKey<Float> ofFloat(String keyId)
	{
		return new StateKey<>(keyId, Float.class, null);
	}

	public static StateKey<Character> ofCharacter(String keyId)
	{
		return new StateKey<>(keyId, Character.class, null);
	}

	public static StateKey<Boolean> ofBoolean(String keyId)
	{
		return new StateKey<>(keyId, Boolean.class, null);
	}

	public static <V> StateKey<V> of(String keyId, Class<V> valueClass)
	{
		return new StateKey<>(keyId, valueClass, null);
	}

	public static <V> StateKey<V> of(String keyId, Class<V> valueClass, Class<?> secondaryClass)
	{
		return new StateKey<>(keyId, valueClass, secondaryClass);
	}

	private final String keyId;
	private final Class<V> valueClass;
	private final Class<?> secondaryClass;

	private StateKey(String keyId, Class<V> valueClass, Class<?> secondaryClass)
	{
		this.secondaryClass = secondaryClass;
		Preconditions.checkNotNull(valueClass);
		Preconditions.checkNotNull(keyId);
		Preconditions.checkArgument(!keyId.isEmpty());
		Preconditions.checkArgument(!keyId.matches("^\\s"));
		Preconditions.checkArgument(!keyId.matches("\\s$"));
		checkClassValue(valueClass);
		this.valueClass = valueClass;
		this.keyId = keyId;
	}

	public Class<V> getValueClass()
	{
		return valueClass;
	}

	public Class<?> getSecondaryClass()
	{
		if (secondaryClass == null)
		{
			throw new UnsupportedOperationException("Key " + keyId + " does not have a secondary class");
		}
		return secondaryClass;
	}

	public String getId()
	{
		return keyId;
	}

	/**
	 * Compare only based on the keyId -- ignore type differences. This is so that
	 * conversion can occur among numeric types.
	 */
	@Override
	public int compareTo(StateKey<?> that)
	{
		int result = keyId.compareTo(that.getId());
		return result;
	}

	/**
	 * Compare only based on the keyId -- ignore type differences. This is so that
	 * conversion can occur among numeric types.
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + keyId.hashCode();
		return result;
	}

	/**
	 * Compare only based on the keyId -- ignore type differences. This is so that
	 * conversion can occur among numeric types.
	 */
	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (other instanceof StateKey)
		{
			StateKey<?> that = (StateKey<?>) other;
			if (!this.keyId.equals(that.keyId))
			{
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public String toString()
	{
		return "Key (" + valueClass.getSimpleName() + ") " + keyId;
	}

	private static final void checkClassValue(Class<?> valueClass)
	{
		if (State.class.isAssignableFrom(valueClass))
			return;
		if (List.class.isAssignableFrom(valueClass))
			return;
		if (String.class.isAssignableFrom(valueClass))
			return;
		if (Integer.class.isAssignableFrom(valueClass))
			return;
		if (Long.class.isAssignableFrom(valueClass))
			return;
		if (Short.class.isAssignableFrom(valueClass))
			return;
		if (Byte.class.isAssignableFrom(valueClass))
			return;
		if (Double.class.isAssignableFrom(valueClass))
			return;
		if (Float.class.isAssignableFrom(valueClass))
			return;
		if (Character.class.isAssignableFrom(valueClass))
			return;
		if (Boolean.class.isAssignableFrom(valueClass))
			return;

		throw new IllegalArgumentException("Cannot create a key for an object of class " + valueClass.getSimpleName());
	}
}

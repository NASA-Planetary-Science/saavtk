package edu.jhuapl.saavtk.state;

import com.google.common.base.Preconditions;

public final class StateKey<V> implements Comparable<StateKey<V>>
{
	public static StateKey<State> ofState(String keyId)
	{
		return new StateKey<>(State.class, keyId);
	}

	public static StateKey<Integer> ofInteger(String keyId)
	{
		return new StateKey<>(Integer.class, keyId);
	}

	public static StateKey<Long> ofLong(String keyId)
	{
		return new StateKey<>(Long.class, keyId);
	}

	public static StateKey<Short> ofShort(String keyId)
	{
		return new StateKey<>(Short.class, keyId);
	}

	public static StateKey<Byte> ofByte(String keyId)
	{
		return new StateKey<>(Byte.class, keyId);
	}

	public static StateKey<Double> ofDouble(String keyId)
	{
		return new StateKey<>(Double.class, keyId);
	}

	public static StateKey<Float> ofFloat(String keyId)
	{
		return new StateKey<>(Float.class, keyId);
	}

	public static StateKey<Character> ofCharacter(String keyId)
	{
		return new StateKey<>(Character.class, keyId);
	}

	public static StateKey<Boolean> ofBoolean(String keyId)
	{
		return new StateKey<>(Boolean.class, keyId);
	}

	public static StateKey<String> ofString(String keyId)
	{
		return new StateKey<>(String.class, keyId);
	}

	public static <V> StateKey<V> of(V object, String keyId)
	{
		Preconditions.checkNotNull(object);
		@SuppressWarnings("unchecked")
		StateKey<V> result = new StateKey<>((Class<V>) object.getClass(), keyId);
		return result;
	}

	private final Class<V> typeId;
	private final String keyId;

	private StateKey(Class<V> typeId, String keyId)
	{
		Preconditions.checkNotNull(typeId);
		Preconditions.checkNotNull(keyId);
		Preconditions.checkArgument(!keyId.isEmpty());
		Preconditions.checkArgument(!keyId.matches("^\\s"));
		Preconditions.checkArgument(!keyId.matches("\\s$"));
		this.typeId = typeId;
		this.keyId = keyId;
	}

	public Class<V> getTypeId()
	{
		return typeId;
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
	public int compareTo(StateKey<V> that)
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
		return "Key (" + typeId.getSimpleName() + ") " + keyId;
	}

}

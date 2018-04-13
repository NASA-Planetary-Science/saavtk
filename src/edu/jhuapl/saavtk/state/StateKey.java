package edu.jhuapl.saavtk.state;

import com.google.common.base.Preconditions;

public final class StateKey<A extends Attribute> implements Comparable<StateKey<A>>
{
	private final String keyId;

	public StateKey(String keyId)
	{
		Preconditions.checkNotNull(keyId);
		Preconditions.checkArgument(!keyId.isEmpty());
		Preconditions.checkArgument(!keyId.matches("^\\s"));
		Preconditions.checkArgument(!keyId.matches("\\s$"));
		this.keyId = keyId;
	}

	public String getId()
	{
		return keyId;
	}

	@Override
	public int compareTo(StateKey<A> that)
	{
		return keyId.compareTo(that.getId());
	}

	@Override
	public int hashCode()
	{
		return keyId.hashCode();
	}

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
			return this.keyId.equals(that.keyId);
		}
		return false;
	}

	@Override
	public String toString()
	{
		return keyId;
	}

}

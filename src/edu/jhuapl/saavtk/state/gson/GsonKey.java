package edu.jhuapl.saavtk.state.gson;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.state.StateKey;

public final class GsonKey<T> extends StateKey<T> implements Comparable<GsonKey<?>>
{
	static <T> GsonKey<T> of(String keyId)
	{
		return new GsonKey<>(keyId);
	}

	private final String keyId;

	private GsonKey(String keyId)
	{
		Preconditions.checkNotNull(keyId);
		Preconditions.checkArgument(!keyId.isEmpty());
		Preconditions.checkArgument(!keyId.matches("^\\s"));
		Preconditions.checkArgument(!keyId.matches("\\s$"));
		this.keyId = keyId;
	}

	@Override
	public String getId()
	{
		return keyId;
	}

	@Override
	public int compareTo(GsonKey<?> that)
	{
		if (that == null)
			return 1;
		return this.getId().compareTo(that.getId());
	}

	@Override
	public String toString()
	{
		return keyId;
	}

}

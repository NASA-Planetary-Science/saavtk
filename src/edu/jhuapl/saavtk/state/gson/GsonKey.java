package edu.jhuapl.saavtk.state.gson;

import java.lang.reflect.Type;

import com.google.common.base.Preconditions;
import com.google.gson.reflect.TypeToken;

import edu.jhuapl.saavtk.state.StateKey;

public final class GsonKey<T> extends StateKey<T>
{
	public static <T> GsonKey<T> of(String keyId)
	{
		return new GsonKey<>(keyId, new TypeToken<T>() {}.getType());
	}

	private final String keyId;
	private final Type type;

	private GsonKey(String keyId, Type type)
	{
		Preconditions.checkNotNull(keyId);
		Preconditions.checkArgument(!keyId.isEmpty());
		Preconditions.checkArgument(!keyId.matches("^\\s"));
		Preconditions.checkArgument(!keyId.matches("\\s$"));
		Preconditions.checkNotNull(type);
		this.keyId = keyId;
		this.type = type;
	}

	@Override
	public String getId()
	{
		return keyId;
	}

	public Type getType()
	{
		return type;
	}

	@Override
	public int compareTo(StateKey<?> that)
	{
		int result = keyId.compareTo(that.getId());
		if (result != 0)
			return result;
		return 0;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + keyId.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (other instanceof GsonKey)
		{
			GsonKey<?> that = (GsonKey<?>) other;
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
		return keyId;
	}

}

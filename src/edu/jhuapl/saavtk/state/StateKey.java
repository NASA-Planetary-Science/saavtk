package edu.jhuapl.saavtk.state;

/**
 * @param <T> type of the object that may be associated with this key, used for
 *            compile-time safety only
 */
public abstract class StateKey<T>
{
	public abstract String getId();

	@Override
	public abstract String toString();

	@Override
	public final int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getId().hashCode();
		return result;
	}

	@Override
	public final boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (other instanceof StateKey)
		{
			StateKey<?> that = (StateKey<?>) other;
			return this.getId().equals(that.getId());
		}
		return false;
	}

}

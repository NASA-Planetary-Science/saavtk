package edu.jhuapl.saavtk.state;

public abstract class StateKey<T> implements Comparable<StateKey<?>>
{
	public abstract String getId();

	@Override
	public abstract int compareTo(StateKey<?> that);

	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object other);

	@Override
	public abstract String toString();

}

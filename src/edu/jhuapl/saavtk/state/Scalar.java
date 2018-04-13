package edu.jhuapl.saavtk.state;

class Scalar<V extends Comparable<V>> implements Comparable<Scalar<V>>
{
	private final V value;

	protected Scalar(V value)
	{
		this.value = value;
	}

	public V get()
	{
		return value;
	}

	@Override
	public int compareTo(Scalar<V> that)
	{
		V thisValue = this.get();
		V thatValue = that.get();
		if (thisValue != null && thatValue != null)
		{
			return thisValue.compareTo(thatValue);
		}
		else if (thisValue == null && thatValue == null)
		{
			return 0;
		}
		else if (thisValue != null)
		{
			return 1;
		}
		else
		{
			return -1;
		}
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (!(other instanceof Scalar))
		{
			return false;
		}
		Scalar<?> that = (Scalar<?>) other;
		V thisValue = this.get();
		Object thatValue = that.get();
		return thisValue == null ? thatValue == null : thisValue.equals(thatValue);
	}

	@Override
	public String toString()
	{
		return value != null ? value.toString() : null;
	}
}

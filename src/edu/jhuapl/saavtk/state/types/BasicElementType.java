package edu.jhuapl.saavtk.state.types;

import com.google.common.base.Preconditions;

abstract class BasicElementType implements ElementType
{
	private final Class<?> primaryType;
	private final HasSubTypes subTypes;

	protected BasicElementType(Class<?> primaryType, HasSubTypes subTypes)
	{
		Preconditions.checkNotNull(primaryType);
		Preconditions.checkNotNull(subTypes);
		this.primaryType = primaryType;
		this.subTypes = subTypes;
	}

	@Override
	public abstract String toString();

	@Override
	public final Class<?> getPrimaryType()
	{
		return primaryType;
	}

	@Override
	public final HasSubTypes getSubTypes()
	{
		return subTypes;
	}

	@Override
	public final int compareTo(ElementType that)
	{
		int result = 1;
		if (that != null)
		{
			result = compareClass(this.getClass(), that.getClass());
			if (result == 0)
			{
				result = compareClass(this.getPrimaryType(), that.getPrimaryType());
			}
			if (result == 0)
			{
				result = this.getSubTypes().compareTo(that.getSubTypes());
			}
		}
		return result < 0 ? -1 : result > 0 ? +1 : 0;
	}

	@Override
	public final int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + getClass().hashCode();
		result = prime * result + getPrimaryType().hashCode();
		result = prime * result + getSubTypes().hashCode();
		return result;
	}

	@Override
	public final boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}

		if (other == null)
		{
			return false;
		}

		// Require exact type agreement.
		if (this.getClass() != other.getClass())
		{
			return false;
		}

		BasicElementType that = (BasicElementType) other;

		if (!this.getPrimaryType().equals(that.getPrimaryType()))
		{
			return false;
		}

		if (!this.getSubTypes().equals(that.getSubTypes()))
		{
			return false;
		}

		return true;
	}

	private static int compareClass(Class<?> class1, Class<?> class2)
	{
		int result = 0;
		if (class1 != class2)
		{
			result = class1.getSimpleName().compareTo(class2.getSimpleName());
			if (result == 0)
			{
				result = class1.getName().compareTo(class2.getName());
			}

		}
		return result;
	}
}

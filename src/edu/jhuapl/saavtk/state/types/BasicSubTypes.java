package edu.jhuapl.saavtk.state.types;

import java.util.Iterator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * 2018-04-24. This package was part of the initial "save state" effort (redmine
 * 1206). This package was a partly-successful attempt to keep track of the full
 * type of generics despite type erasure, similar to Guava/Gson's TypeToken
 * classes. It worked fine and was even better in some ways because it allowed
 * one to reconstruct a complicated object piece-by-piece, but was too clunky to
 * be practical.
 * 
 * Leaving it in the project for the time being in case we need it after all or
 * want to refer to it in the near future, but marking it deprecated, not so
 * much because of its flaws but to prevent its accidental use.
 * 
 * After a suitable time, if no use presents itself, it would be safe to remove
 * this whole package.
 */
@Deprecated
final class BasicSubTypes implements HasSubTypes
{
	private final ImmutableList<ElementType> secondaryTypes;

	BasicSubTypes(ImmutableList<ElementType> secondaryTypes)
	{
		Preconditions.checkNotNull(secondaryTypes);
		this.secondaryTypes = secondaryTypes;
	}

	@Override
	public ImmutableList<ElementType> getSubTypes()
	{
		return secondaryTypes;
	}

	@Override
	public final int compareTo(HasSubTypes that)
	{
		int result = 0;
		if (this.getClass() != that.getClass())
		{
			result = this.getClass().getSimpleName().compareTo(that.getClass().getSimpleName());
			if (result == 0)
			{
				result = this.getClass().getName().compareTo(that.getClass().getName());
			}
		}
		if (result == 0)
		{
			ImmutableList<ElementType> theseSubTypes = this.getSubTypes();
			ImmutableList<ElementType> thoseSubTypes = that.getSubTypes();
			result = theseSubTypes.size() - thoseSubTypes.size();
			if (result == 0)
			{
				for (int index = 0; index < theseSubTypes.size(); ++index)
				{
					result = theseSubTypes.get(index).compareTo(thoseSubTypes.get(index));
					if (result != 0)
					{
						break;
					}
				}
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

		BasicSubTypes that = (BasicSubTypes) other;

		if (!this.getSubTypes().equals(that.getSubTypes()))
		{
			return false;
		}

		return true;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		Iterator<ElementType> iterator = getSubTypes().iterator();
		if (iterator.hasNext())
		{
			builder.append(iterator.next());
			while (iterator.hasNext())
			{
				builder.append(", ");
				builder.append(iterator.next());
			}
		}
		return builder.toString();
	}

}

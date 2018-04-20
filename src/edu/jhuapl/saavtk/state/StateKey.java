package edu.jhuapl.saavtk.state;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public final class StateKey<T> implements Comparable<StateKey<?>>
{
	// Note: this is iterated on to find the first value that matches a type, so this means that super-types must
	// be listed *after* sub-types in this list, e.g., SortedSet before Set. Otherwise the first match may be the
	// wrong match. Even so there are issues down in the weeds: something serialized as SortedSet is not retrievable
	// as Set. Hard to sort that one out.
	private static final ImmutableList<Class<?>> SUPPORTED_VALUE_TYPES =
			ImmutableList.of(State.class, List.class, SortedSet.class, Set.class, String.class, Integer.class, Long.class, Short.class, Byte.class, Double.class, Float.class, Character.class, Boolean.class);

	public static StateKey<State> ofState(String keyId)
	{
		return new StateKey<>(keyId, State.class, null);
	}

	public static <T> StateKey<List<T>> ofList(String keyId, Class<?> secondaryClass)
	{
		// Need to tap dance around Java's type erasure.
		StateKey<?> genericKey = of(keyId, List.class, secondaryClass);
		@SuppressWarnings("unchecked")
		StateKey<List<T>> result = (StateKey<List<T>>) genericKey;
		return result;
	}

	public static <T> StateKey<SortedSet<T>> ofSortedSet(String keyId, Class<?> secondaryClass)
	{
		// Need to tap dance around Java's type erasure.
		StateKey<?> genericKey = of(keyId, SortedSet.class, secondaryClass);
		@SuppressWarnings("unchecked")
		StateKey<SortedSet<T>> result = (StateKey<SortedSet<T>>) genericKey;
		return result;
	}

	public static <T> StateKey<Set<T>> ofSet(String keyId, Class<?> secondaryClass)
	{
		// Need to tap dance around Java's type erasure.
		StateKey<?> genericKey = of(keyId, Set.class, secondaryClass);
		@SuppressWarnings("unchecked")
		StateKey<Set<T>> result = (StateKey<Set<T>>) genericKey;
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

	public static <T> StateKey<T> of(String keyId, Class<T> primaryClass)
	{
		return new StateKey<>(keyId, primaryClass, null);
	}

	public static <T> StateKey<T> of(String keyId, Class<T> primaryClass, Class<?> secondaryClass)
	{
		Preconditions.checkNotNull(secondaryClass);
		return new StateKey<>(keyId, primaryClass, secondaryClass);
	}

	public static StateKey<?> ofObject(String keyId, Object object)
	{
		Preconditions.checkNotNull(object);
		Class<?> primaryClass = getSupportedClass(object.getClass());
		Class<?> secondaryClass = needsSecondaryClass(primaryClass) ? getSecondaryClass(object) : null;
		return new StateKey<>(keyId, primaryClass, secondaryClass);
	}

	private final String keyId;
	private final Class<?> primaryClass;
	private final Class<?> secondaryClass;

	private StateKey(String keyId, Class<?> primaryClass, Class<?> secondaryClass)
	{
		Preconditions.checkNotNull(keyId);
		Preconditions.checkArgument(!keyId.isEmpty());
		Preconditions.checkArgument(!keyId.matches("^\\s"));
		Preconditions.checkArgument(!keyId.matches("\\s$"));
		checkValid(primaryClass, secondaryClass);
		this.keyId = keyId;
		this.primaryClass = getSupportedClass(primaryClass);
		this.secondaryClass = getSupportedClass(secondaryClass);
	}

	public Class<?> getPrimaryClass()
	{
		return primaryClass;
	}

	public boolean hasSecondaryClass()
	{
		return secondaryClass != null;
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

	@Override
	public int compareTo(StateKey<?> that)
	{
		int result = keyId.compareTo(that.getId());
		if (result != 0)
			return result;

		if (!areClassesConvertible(this.primaryClass, that.primaryClass))
		{
			return this.primaryClass.getSimpleName().compareTo(that.primaryClass.getSimpleName());
		}

		if (!areClassesConvertible(this.secondaryClass, that.secondaryClass))
		{
			return this.secondaryClass == null ? -1 : (that.secondaryClass == null ? 1 : this.secondaryClass.getSimpleName().compareTo(that.secondaryClass.getSimpleName()));
		}

		return 0;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + keyId.hashCode();
		if (areClassesConvertible(primaryClass, Number.class))
		{
			result = prime * result + Number.class.hashCode();
		}
		else
		{
			result = prime * result + primaryClass.hashCode();
		}
		if (areClassesConvertible(secondaryClass, Number.class))
		{
			result = prime * result + Number.class.hashCode();
		}
		else
		{
			result = prime * result + (secondaryClass == null ? 0 : secondaryClass.hashCode());
		}
		return result;
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
			if (!this.keyId.equals(that.keyId))
			{
				return false;
			}
			if (!areClassesConvertible(this.primaryClass, that.primaryClass))
			{
				return false;
			}
			if (!areClassesConvertible(this.secondaryClass, that.secondaryClass))
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
		return "Key (" + primaryClass.getSimpleName() + (secondaryClass != null ? "<" + secondaryClass.getSimpleName() + ">" : "") + ") " + keyId;
	}

	private static Class<?> getSupportedClass(Class<?> valueClass)
	{
		if (valueClass == null)
		{
			return null;
		}
		for (Class<?> supportedClass : SUPPORTED_VALUE_TYPES)
		{
			if (supportedClass.isAssignableFrom(valueClass))
			{
				return supportedClass;
			}
		}

		throw new IllegalArgumentException("Cannot create a key for an object of class " + valueClass.getSimpleName());
	}

	private final void checkValid(Class<?> primaryClass, Class<?> secondaryClass)
	{
		Preconditions.checkNotNull(primaryClass);
		if (needsSecondaryClass(primaryClass))
		{
			Preconditions.checkNotNull(secondaryClass);
		}
	}

	private static boolean needsSecondaryClass(Class<?> primaryClass)
	{
		return Iterable.class.isAssignableFrom(primaryClass);
	}

	private static Class<?> getSecondaryClass(Object object)
	{
		if (object instanceof Iterable)
		{
			Iterable<?> iterable = (Iterable<?>) object;
			Iterator<?> iterator = iterable.iterator();
			while (iterator.hasNext())
			{
				Object next = iterator.next();
				if (next != null)
				{
					return next.getClass();
				}
			}
			return null;
		}
		throw new AssertionError();
	}

	private static boolean areClassesConvertible(Class<?> class1, Class<?> class2)
	{
		// Both or neither argument must be null.
		if (class1 == null && class2 == null)
		{
			return true;
		}
		else if (class1 == null || class2 == null)
		{
			return false;
		}

		// Equal classes are convertible.
		if (class1.equals(class2))
		{
			return true;
		}

		// Numbers are also convertible.
		if (Number.class.isAssignableFrom(class1) && Number.class.isAssignableFrom(class2))
		{
			return true;
		}

		// Nothing else is convertible.
		return false;
	}
}

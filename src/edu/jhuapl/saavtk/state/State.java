package edu.jhuapl.saavtk.state;

import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedMap;

public final class State
{
	public static class InaccurateConversionException extends RuntimeException
	{
		private static final long serialVersionUID = 1332273620795657017L;

		public InaccurateConversionException(String msg)
		{
			super(msg);
		}
	}

	private static final Object NULL_OBJECT = new Object();

	public static State of()
	{
		return new State(new TreeMap<>());
	}

	private final SortedMap<StateKey<?>, Object> map;

	private State(TreeMap<StateKey<?>, Object> map)
	{
		Preconditions.checkNotNull(map);
		this.map = map;
	}

	public <V> V get(StateKey<V> key)
	{
		Preconditions.checkNotNull(key);
		Object object = map.get(key);
		if (object == NULL_OBJECT)
		{
			return null;
		}
		else if (object instanceof Number)
		{
			return convert((Number) object, key.getTypeId());
		}
		return key.getTypeId().cast(object);
	}

	public <V> void put(StateKey<V> key, V value)
	{
		Preconditions.checkNotNull(key);
		if (value == null)
		{
			map.put(key, NULL_OBJECT);
		}
		else
		{
			map.put(key, value);
		}
	}

	public ImmutableSortedMap<StateKey<?>, Object> getMap()
	{
		return ImmutableSortedMap.copyOf(map);
	}

	@Override
	public int hashCode()
	{
		return map.hashCode();
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (other instanceof State)
		{
			State that = (State) other;
			return this.map.equals(that.map);
		}
		return false;
	}

	@Override
	public String toString()
	{
		return "(State) " + map;
	}

	private <V> V convert(Number number, Class<V> typeId)
	{
		V result = tryConvert(number, typeId);
		if (result == null)
		{
			result = tryConvert(number.byteValue(), typeId);
		}
		if (result == null)
		{
			result = tryConvert(number.doubleValue(), typeId);
		}
		if (result == null)
		{
			result = tryConvert(number.floatValue(), typeId);
		}
		if (result == null)
		{
			result = tryConvert(number.intValue(), typeId);
		}
		if (result == null)
		{
			result = tryConvert(number.longValue(), typeId);
		}
		if (result == null)
		{
			result = tryConvert(number.shortValue(), typeId);
		}
		if (result == null)
		{
			// Nothing worked, so repeat the first cast that
			// failed. This will cause the appropriate
			// exception to be thrown.
			result = typeId.cast(number);
		}
		if (result != null)
		{
			checkForLossOfPrecision(number, result);
		}
		return result;
	}

	private <V> V tryConvert(Number number, Class<V> typeId)
	{
		V result = null;
		try
		{
			result = typeId.cast(number);
		}
		catch (@SuppressWarnings("unused") ClassCastException e)
		{}
		return result;
	}

	private void checkForLossOfPrecision(Number number, Object value)
	{
		Number valueAsNumber = (Number) value;
		String message = "Unable to convert " + number + " to a " + value.getClass().getSimpleName() + " accurately. Result was " + value;
		if (number instanceof Byte && Byte.compare(number.byteValue(), valueAsNumber.byteValue()) != 0)
		{
			throw new InaccurateConversionException(message);
		}
		if (number instanceof Double && Double.compare(number.doubleValue(), valueAsNumber.doubleValue()) != 0)
		{
			// Special case: only throw an exception if a floating point number overflows, not just because of precision.
			if (Double.compare(Math.abs(number.doubleValue()), Float.MAX_VALUE) > 0)
			{
				throw new InaccurateConversionException(message);
			}
		}
		if (number instanceof Float && Float.compare(number.floatValue(), valueAsNumber.floatValue()) != 0)
		{
			throw new InaccurateConversionException(message);
		}
		if (number instanceof Integer && Integer.compare(number.intValue(), valueAsNumber.intValue()) != 0)
		{
			throw new InaccurateConversionException(message);
		}
		if (number instanceof Long && Long.compare(number.longValue(), valueAsNumber.longValue()) != 0)
		{
			throw new InaccurateConversionException(message);
		}
		if (number instanceof Short && Short.compare(number.shortValue(), valueAsNumber.shortValue()) != 0)
		{
			throw new InaccurateConversionException(message);
		}
	}
}

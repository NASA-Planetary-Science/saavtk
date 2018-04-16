package edu.jhuapl.saavtk.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public abstract class StateBase
{
	public static final class InaccurateConversionException extends RuntimeException
	{
		private static final long serialVersionUID = 1332273620795657017L;

		public InaccurateConversionException(String msg)
		{
			super(msg);
		}
	}

	private static final Object NULL_OBJECT = new Object() {
		@Override
		public String toString()
		{
			// Deliberately capitalizing this so that the astute debugger has a chance of
			// noticing that this object is not actually a null pointer.
			return "Null";
		}
	};

	private final List<StateKey<?>> keys;
	private final Map<StateKey<?>, Object> map;

	protected StateBase()
	{
		this.keys = new ArrayList<>();
		this.map = new HashMap<>();
	}

	public ImmutableList<StateKey<?>> getKeys()
	{
		return ImmutableList.copyOf(keys);
	}

	@Override
	public final int hashCode()
	{
		return map.hashCode();
	}

	@Override
	public final boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		if (other instanceof StateBase)
		{
			StateBase that = (StateBase) other;
			return this.map.equals(that.map);
		}
		return false;
	}

	@Override
	public String toString()
	{
		return "(State) " + map;
	}

	protected Object getObject(StateKey<?> key)
	{
		Object object = map.get(key);
		if (object == null)
		{
			throw new IllegalArgumentException("State does not contain key " + key);
		}
		if (object == NULL_OBJECT)
		{
			return null;
		}
		return object;
	}

	protected void putObject(StateKey<?> key, Object value)
	{
		Preconditions.checkNotNull(key);
		keys.add(key);
		if (value == null)
		{
			map.put(key, NULL_OBJECT);
		}
		else
		{
			map.put(key, value);
		}
	}

	protected <V> V convert(Object object, Class<V> toClass)
	{
		V result = null;
		if (object != null)
		{
			Class<?> fromClass = object.getClass();
			if (toClass.isAssignableFrom(fromClass))
			{
				result = toClass.cast(object);
			}
			else if (Number.class.isAssignableFrom(fromClass) && Number.class.isAssignableFrom(toClass))
			{
				// Converting from one Number to another is supported with some error checking.
				Number number = (Number) object;
				{
					result = tryConvert(number.byteValue(), toClass);
				}
				if (result == null)
				{
					result = tryConvert(number.doubleValue(), toClass);
				}
				if (result == null)
				{
					result = tryConvert(number.floatValue(), toClass);
				}
				if (result == null)
				{
					result = tryConvert(number.intValue(), toClass);
				}
				if (result == null)
				{
					result = tryConvert(number.longValue(), toClass);
				}
				if (result == null)
				{
					result = tryConvert(number.shortValue(), toClass);
				}
				if (result != null)
				{
					checkForLossOfPrecision(number, (Number) result);
				}
			}
			else
			{
				// object is not null, but all attempts to convert it to
				// the type provided by toClass have failed.
				throw new ClassCastException("Unable to convert object " + object + " to a " + toClass.getSimpleName());
			}
		}
		return result;
	}

	protected <V> V tryConvert(Number number, Class<V> valueClass)
	{
		V result = null;
		try
		{
			result = valueClass.cast(number);
		}
		catch (@SuppressWarnings("unused") ClassCastException e)
		{}
		return result;
	}

	protected void checkForLossOfPrecision(Number number, Number value)
	{
		String message = "Unable to convert " + number + " to a " + value.getClass().getSimpleName() + " accurately. Result was " + value;
		if (number instanceof Byte && Byte.compare(number.byteValue(), value.byteValue()) != 0)
		{
			throw new InaccurateConversionException(message);
		}
		if (number instanceof Double && Double.compare(number.doubleValue(), value.doubleValue()) != 0)
		{
			// Special case: only throw an exception if a floating point number overflows, not just because of precision.
			if (Double.compare(Math.abs(number.doubleValue()), Float.MAX_VALUE) > 0)
			{
				throw new InaccurateConversionException(message);
			}
		}
		if (number instanceof Float && Float.compare(number.floatValue(), value.floatValue()) != 0)
		{
			throw new InaccurateConversionException(message);
		}
		if (number instanceof Integer && Integer.compare(number.intValue(), value.intValue()) != 0)
		{
			throw new InaccurateConversionException(message);
		}
		if (number instanceof Long && Long.compare(number.longValue(), value.longValue()) != 0)
		{
			throw new InaccurateConversionException(message);
		}
		if (number instanceof Short && Short.compare(number.shortValue(), value.shortValue()) != 0)
		{
			throw new InaccurateConversionException(message);
		}
	}

}

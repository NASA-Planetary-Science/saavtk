package edu.jhuapl.saavtk.state.gson;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import com.google.gson.reflect.TypeToken;

import edu.jhuapl.saavtk.state.State;
import edu.jhuapl.saavtk.state.StateKey;
import edu.jhuapl.saavtk.state.Version;

enum ValueTypeInfo
{
	STRING("String", new TypeToken<String>() {}.getType(), String.class),
	BOOLEAN("Boolean", new TypeToken<Boolean>() {}.getType(), Boolean.class),
	DOUBLE("Double", new TypeToken<Double>() {}.getType(), Double.class),
	VERSION("Version", new TypeToken<Version>() {}.getType(), Version.class),

	STATE_KEY("StateKey", new TypeToken<StateKey<?>>() {}.getType(), StateKey.class),
	STATE("State", new TypeToken<State>() {}.getType(), State.class),
	INTEGER("Integer", new TypeToken<Integer>() {}.getType(), Integer.class),
	LONG("Long", new TypeToken<Long>() {}.getType(), Long.class),
	SHORT("Short", new TypeToken<Short>() {}.getType(), Short.class),
	BYTE("Byte", new TypeToken<Byte>() {}.getType(), Byte.class),
	FLOAT("Float", new TypeToken<Float>() {}.getType(), Float.class),
	CHARACTER("Character", new TypeToken<Character>() {}.getType(), Character.class),
	DOUBLE_ARRAY("double[]", new TypeToken<double[]>() {}.getType(), double[].class),

	SORTED_MAP("SortedMap", new TypeToken<SortedMap<?, ?>>() {}.getType(), SortedMap.class),
	MAP("Map", new TypeToken<Map<?, ?>>() {}.getType(), Map.class),
	LIST("List", new TypeToken<List<?>>() {}.getType(), List.class),
	SORTED_SET("SortedSet", new TypeToken<SortedSet<?>>() {}.getType(), SortedSet.class),
	SET("Set", new TypeToken<Set<?>>() {}.getType(), Set.class),
	NULL("Null", new TypeToken<Object>() {}.getType(), Object.class),
	;

	public static ValueTypeInfo of(String typeId)
	{
		for (ValueTypeInfo info : values())
		{
			if (info.typeId.equals(typeId))
			{
				return info;
			}
		}
		throw new IllegalArgumentException();
	}

	public static ValueTypeInfo of(Type type)
	{
		for (ValueTypeInfo info : values())
		{
			if (info.type.equals(type))
			{
				return info;
			}
		}
		throw new IllegalArgumentException();
	}

	public static ValueTypeInfo of(Class<?> valueClass)
	{
		for (ValueTypeInfo info : values())
		{
			if (info.valueClass.isAssignableFrom(valueClass))
			{
				return info;
			}
		}
		throw new IllegalArgumentException();
	}

	public static ValueTypeInfo forObject(Object object)
	{
		if (object == null)
		{
			return NULL;
		}
		return of(object.getClass());
	}

	private final String typeId;
	private final Type type;
	private final Class<?> valueClass;

	private ValueTypeInfo(String typeId, Type type, Class<?> clazz)
	{
		this.typeId = typeId;
		this.type = type;
		this.valueClass = clazz;
	}

	public String getTypeId()
	{
		return typeId;
	}

	public Type getType()
	{
		return type;
	}

	public Class<?> getTypeClass()
	{
		return valueClass;
	}

	@Override
	public String toString()
	{
		return "TypeInfo: " + typeId + ", Type = " + type + ", class = " + valueClass.getSimpleName();
	}

}

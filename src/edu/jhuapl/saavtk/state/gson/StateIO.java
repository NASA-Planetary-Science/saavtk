package edu.jhuapl.saavtk.state.gson;

import java.lang.reflect.Type;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import edu.jhuapl.saavtk.state.State;
import edu.jhuapl.saavtk.state.StateKey;

final class StateIO implements JsonSerializer<State>, JsonDeserializer<State>
{
	private enum ValueTypeInfo
	{
		STATE("State", new TypeToken<State>() {}.getType(), State.class),
		STRING("String", new TypeToken<String>() {}.getType(), String.class),
		INTEGER("Integer", new TypeToken<Integer>() {}.getType(), Integer.class),
		LONG("Long", new TypeToken<Long>() {}.getType(), Long.class),
		SHORT("Short", new TypeToken<Short>() {}.getType(), Short.class),
		BYTE("Byte", new TypeToken<Byte>() {}.getType(), Byte.class),
		DOUBLE("Double", new TypeToken<Double>() {}.getType(), Double.class),
		FLOAT("Float", new TypeToken<Float>() {}.getType(), Float.class),
		CHARACTER("Character", new TypeToken<Character>() {}.getType(), Character.class),
		BOOLEAN("Boolean", new TypeToken<Boolean>() {}.getType(), Boolean.class),
		;

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

	private static final ImmutableMap<String, ValueTypeInfo> idMap = createIdMap();
	private static final ImmutableMap<Type, ValueTypeInfo> typeMap = createTypeMap();
	private static final ImmutableMap<Class<?>, ValueTypeInfo> classMap = createClassMap();

	private static ValueTypeInfo getValueTypeInfo(String typeId)
	{
		Preconditions.checkNotNull(typeId);
		if (!idMap.containsKey(typeId))
		{
			throw new IllegalArgumentException("No information about how to store/retrieve an object with typeId " + typeId);
		}
		return idMap.get(typeId);
	}

	private static ValueTypeInfo getValueTypeInfo(Type type)
	{
		Preconditions.checkNotNull(type);
		if (!typeMap.containsKey(type))
		{
			throw new IllegalArgumentException("No information about how to store/retrieve an object of type " + type.getTypeName());
		}
		return typeMap.get(type);
	}

	private static ValueTypeInfo getValueTypeInfo(Class<?> typeClass)
	{
		Preconditions.checkNotNull(typeClass);
		if (!classMap.containsKey(typeClass))
		{
			throw new IllegalArgumentException("No information about how to store/retrieve an object with class " + typeClass.getSimpleName());
		}
		return classMap.get(typeClass);
	}

	private static final String STORED_AS_TYPE_KEY = "type";
	private static final String STORED_AS_VALUE_KEY = "value";

	// @Override
	public Type getTargetType()
	{
		return ValueTypeInfo.STATE.getType();
	}

	@Override
	public JsonElement serialize(State src, Type typeOfSrc, JsonSerializationContext context)
	{
		Preconditions.checkNotNull(src);
		Preconditions.checkArgument(ValueTypeInfo.STATE.getType().equals(typeOfSrc));
		Preconditions.checkNotNull(context);

		JsonObject jsonObject = new JsonObject();
		for (StateKey<?> key : src.getKeys())
		{
			encode(key, src.get(key), jsonObject, context);
		}
		return jsonObject;
	}

	@Override
	public State deserialize(JsonElement jsonSrc, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		Preconditions.checkNotNull(jsonSrc);
		Preconditions.checkArgument(ValueTypeInfo.STATE.getType().equals(typeOfT));
		Preconditions.checkNotNull(context);

		State state = State.of();
		JsonObject object = (JsonObject) jsonSrc;
		for (Entry<String, JsonElement> entry : object.entrySet())
		{
			decode(entry, context, state);
		}
		return state;
	}

	private void encode(StateKey<?> key, Object attribute, JsonObject jsonDest, JsonSerializationContext context)
	{
		ValueTypeInfo info = getValueTypeInfo(key.getValueClass());
		Type type = info.getType();
		if (attribute instanceof State || attribute instanceof Number || attribute instanceof Character || attribute == null)
		{
			JsonObject jsonObject = new JsonObject();

			jsonObject.addProperty(STORED_AS_TYPE_KEY, info.getTypeId());
			jsonObject.add(STORED_AS_VALUE_KEY, context.serialize(attribute, type));

			jsonDest.add(key.getId(), jsonObject);
		}
		else if (attribute instanceof Boolean || attribute instanceof String)
		{
			jsonDest.add(key.getId(), context.serialize(attribute));
		}
		else
		{
			throw new IllegalArgumentException("Unable to serialize an object of type " + attribute.getClass().getSimpleName());
		}
	}

	private void decode(Entry<String, JsonElement> entry, JsonDeserializationContext context, State stateDest)
	{
		String keyId = entry.getKey();
		JsonElement element = entry.getValue();
		Type type = null;
		if (element.isJsonObject())
		{
			JsonObject jsonObject = (JsonObject) entry.getValue();

			String typeName = jsonObject.get(STORED_AS_TYPE_KEY).getAsString();
			type = getValueTypeInfo(typeName).getType();
			element = jsonObject.get(STORED_AS_VALUE_KEY);
		}
		else if (element.isJsonPrimitive())
		{
			// Only Boolean and Strings are stored this way.
			JsonPrimitive primitive = (JsonPrimitive) element;
			if (primitive.isBoolean())
			{
				type = ValueTypeInfo.BOOLEAN.getType();
			}
			else if (primitive.isString())
			{
				type = ValueTypeInfo.STRING.getType();
			}
		}
		else
		{
			throw new IllegalArgumentException("Unable to deserialize Json object " + element);
		}
		stateDest.put(getKeyForType(keyId, type), context.deserialize(element, type));
	}

	private StateKey<?> getKeyForType(String keyId, Type type)
	{
		return StateKey.of(keyId, getValueTypeInfo(type).getTypeClass());
	}

	private static ImmutableMap<String, ValueTypeInfo> createIdMap()
	{
		ImmutableMap.Builder<String, ValueTypeInfo> builder = ImmutableMap.builder();
		for (ValueTypeInfo info : ValueTypeInfo.values())
		{
			builder.put(info.getTypeId(), info);
		}
		return builder.build();
	}

	private static ImmutableMap<Type, ValueTypeInfo> createTypeMap()
	{
		ImmutableMap.Builder<Type, ValueTypeInfo> builder = ImmutableMap.builder();
		for (ValueTypeInfo info : ValueTypeInfo.values())
		{
			builder.put(info.getType(), info);
		}
		return builder.build();
	}

	private static ImmutableMap<Class<?>, ValueTypeInfo> createClassMap()
	{
		ImmutableMap.Builder<Class<?>, ValueTypeInfo> builder = ImmutableMap.builder();
		for (ValueTypeInfo info : ValueTypeInfo.values())
		{
			builder.put(info.getTypeClass(), info);
		}
		return builder.build();
	}

}

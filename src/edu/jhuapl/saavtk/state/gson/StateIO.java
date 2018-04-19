package edu.jhuapl.saavtk.state.gson;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
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
		// Group by how these are treated with respect to Gson.
		// JsonPrimitive (ends in false, false):
		STRING("String", new TypeToken<String>() {}.getType(), String.class, false, false),
		BOOLEAN("Boolean", new TypeToken<Boolean>() {}.getType(), Boolean.class, false, false),

		// JsonObject (ends in true, false):
		STATE("State", new TypeToken<State>() {}.getType(), State.class, true, false),
		INTEGER("Integer", new TypeToken<Integer>() {}.getType(), Integer.class, true, false),
		LONG("Long", new TypeToken<Long>() {}.getType(), Long.class, true, false),
		SHORT("Short", new TypeToken<Short>() {}.getType(), Short.class, true, false),
		BYTE("Byte", new TypeToken<Byte>() {}.getType(), Byte.class, true, false),
		DOUBLE("Double", new TypeToken<Double>() {}.getType(), Double.class, true, false),
		FLOAT("Float", new TypeToken<Float>() {}.getType(), Float.class, true, false),
		CHARACTER("Character", new TypeToken<Character>() {}.getType(), Character.class, true, false),

		// JsonArray (ends in false, true):
		LIST("List", new TypeToken<List<?>>() {}.getType(), List.class, false, true),
		;

		private final String typeId;
		private final Type type;
		private final Class<?> valueClass;
		private final boolean useJsonObject;
		private final boolean useJsonArray;

		private ValueTypeInfo(String typeId, Type type, Class<?> clazz, boolean useJsonObject, boolean useJsonArray)
		{
			this.typeId = typeId;
			this.type = type;
			this.valueClass = clazz;
			this.useJsonObject = useJsonObject;
			this.useJsonArray = useJsonArray;
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

		public boolean useJsonObject()
		{
			return useJsonObject;
		}

		public boolean useJsonArray()
		{
			return useJsonArray;
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
	private static final String STORED_AS_ELEMENT_TYPE_KEY = "elementType";
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
		Preconditions.checkArgument(jsonSrc instanceof JsonObject);
		Preconditions.checkArgument(ValueTypeInfo.STATE.getType().equals(typeOfT));
		Preconditions.checkNotNull(context);

		State state = State.of();
		JsonObject object = (JsonObject) jsonSrc;
		for (Entry<String, JsonElement> entry : object.entrySet())
		{
			decode(entry.getKey(), entry.getValue(), context, state);
		}
		return state;
	}

	private void encode(StateKey<?> key, Object attribute, JsonObject jsonDest, JsonSerializationContext context)
	{
		ValueTypeInfo info = getValueTypeInfo(key.getPrimaryClass());
		JsonElement jsonElement = null;
		if (attribute == null || info.useJsonObject())
		{
			JsonObject jsonObject = new JsonObject();
			Type valueType = info.getType();

			jsonObject.addProperty(STORED_AS_TYPE_KEY, info.getTypeId());

			// Serialize the attribute directly into the jsonObject. This may recursively
			// invoke the serialize method above.
			jsonObject.add(STORED_AS_VALUE_KEY, context.serialize(attribute, valueType));

			jsonElement = jsonObject;
		}
		else if (info.useJsonArray())
		{
			if (!(attribute instanceof Iterable) || key.getSecondaryClass() == null)
			{
				throw new AssertionError();
			}
			ValueTypeInfo elementInfo = getValueTypeInfo(key.getSecondaryClass());

			JsonObject jsonObject = new JsonObject();
			Type elementType = elementInfo.getType();

			jsonObject.addProperty(STORED_AS_TYPE_KEY, info.getTypeId());
			jsonObject.addProperty(STORED_AS_ELEMENT_TYPE_KEY, elementInfo.getTypeId());

			JsonArray jsonArray = new JsonArray();
			for (Object element : (Iterable<?>) attribute)
			{
				// Serialize the element directly into the jsonObject. This may recursively
				// invoke the serialize method above.
				jsonArray.add(context.serialize(element, elementType));
			}

			// The Json array gets stored as the value.
			jsonObject.add(STORED_AS_VALUE_KEY, jsonArray);

			jsonElement = jsonObject;
		}
		else
		{
			// Serialize using Gson built-in behavior (JsonPrimitive).
			jsonElement = context.serialize(attribute);
		}

		// Finally, just add the fully serialized element.
		jsonDest.add(key.getId(), jsonElement);
	}

	private void decode(String keyId, JsonElement element, JsonDeserializationContext context, State stateDest)
	{
		Type type = null;
		if (element.isJsonObject())
		{
			JsonObject jsonObject = (JsonObject) element;

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
		if (type == null)
		{
			throw new IllegalArgumentException("Unable to deserialize Json object " + element);
		}
		stateDest.put(getKeyForType(type, keyId), context.deserialize(element, type));
	}

	private StateKey<?> getKeyForType(Type type, String keyId)
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

package edu.jhuapl.saavtk.state.gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
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
		DOUBLE("Double", new TypeToken<Double>() {}.getType(), Double.class, false, false),
		NULL("Null", new TypeToken<Object>() {}.getType(), Object.class, false, false),

		// JsonObject (ends in true, false):
		STATE("State", new TypeToken<State>() {}.getType(), State.class, true, false),
		INTEGER("Integer", new TypeToken<Integer>() {}.getType(), Integer.class, true, false),
		LONG("Long", new TypeToken<Long>() {}.getType(), Long.class, true, false),
		SHORT("Short", new TypeToken<Short>() {}.getType(), Short.class, true, false),
		BYTE("Byte", new TypeToken<Byte>() {}.getType(), Byte.class, true, false),
		FLOAT("Float", new TypeToken<Float>() {}.getType(), Float.class, true, false),
		CHARACTER("Character", new TypeToken<Character>() {}.getType(), Character.class, true, false),
		MAP_ENTRY("Map.Entry", new TypeToken<Map.Entry<?, ?>>() {}.getType(), Map.Entry.class, false, true),

		// JsonArray (ends in false, true):
		MAP("Map", new TypeToken<Map<?, ?>>() {}.getType(), Map.class, false, true),
		LIST("List", new TypeToken<List<?>>() {}.getType(), List.class, false, true),
		SORTED_SET("SortedSet", new TypeToken<SortedSet<?>>() {}.getType(), SortedSet.class, false, true),
		SET("Set", new TypeToken<Set<?>>() {}.getType(), Set.class, false, true),;

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
	//	private static final ImmutableMap<Type, ValueTypeInfo> typeMap = createTypeMap();
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

	//	private static ValueTypeInfo getValueTypeInfo(Type type)
	//	{
	//		Preconditions.checkNotNull(type);
	//		if (!typeMap.containsKey(type))
	//		{
	//			throw new IllegalArgumentException("No information about how to store/retrieve an object of type " + type.getTypeName());
	//		}
	//		return typeMap.get(type);
	//	}

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
	private static final String STORED_AS_KEY_TYPE_KEY = "keyType";
	private static final String STORED_AS_VALUE_TYPE_KEY = "valueType";
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
		Preconditions.checkArgument(jsonSrc.isJsonObject());
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

	private ValueTypeInfo classify(Object object)
	{
		if (object == null)
			return ValueTypeInfo.NULL;

		// For interfaces and base classes, don't use the object's class..
		if (object instanceof List)
		{
			return ValueTypeInfo.LIST;
		}
		if (object instanceof SortedSet)
		{
			return ValueTypeInfo.SORTED_SET;
		}
		if (object instanceof Map.Entry)
		{
			return ValueTypeInfo.MAP_ENTRY;
		}
		if (object instanceof Map)
		{
			return ValueTypeInfo.MAP;
		}
		if (object instanceof Set)
		{
			return ValueTypeInfo.SET;
		}

		// Final classes: use the object's class.
		return getValueTypeInfo(object.getClass());
	}

	private void encode(StateKey<?> key, Object attribute, JsonObject jsonDest, JsonSerializationContext context)
	{
		ValueTypeInfo info = classify(attribute);
		JsonElement jsonElement = JsonNull.INSTANCE;
		if (attribute instanceof String || attribute instanceof Boolean || attribute instanceof Double)
		{
			// Serialize using Gson built-in behavior.
			jsonElement = context.serialize(attribute, info.getType());
		}
		else if (attribute instanceof Iterable)
		{
			jsonElement = encode((Iterable<?>) attribute, info.getTypeId(), context);
		}
		else if (attribute instanceof Map)
		{
			jsonElement = encode((Map<?, ?>) attribute, info.getTypeId(), context);
		}
		else if (ValueTypeInfo.NULL != info)
		{
			jsonElement = encode(attribute, info.getTypeId(), info.getType(), context);
		}

		// Finally, just add the fully serialized element.
		jsonDest.add(key.getId(), jsonElement);
	}

	private JsonElement encode(Iterable<?> iterable, String typeId, JsonSerializationContext context)
	{
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(STORED_AS_TYPE_KEY, typeId);

		// First pass: look for an element that could give us the type information about the
		// element(s) stored in the iterable.
		ValueTypeInfo elementInfo = null;
		for (Object element : iterable)
		{
			if (element != null)
			{
				elementInfo = classify(element);
				jsonObject.addProperty(STORED_AS_ELEMENT_TYPE_KEY, elementInfo.getTypeId());
				break;
			}
		}

		JsonArray jsonArray = new JsonArray();
		if (elementInfo != null)
		{
			Type elementType = elementInfo.getType();
			for (Object element : iterable)
			{
				// Serialize the element directly into the jsonObject. This may recursively
				// invoke the serialize method above.
				jsonArray.add(context.serialize(element, elementType));
			}
		}

		// The Json array gets stored as the value.
		jsonObject.add(STORED_AS_VALUE_KEY, jsonArray);

		return jsonObject;
	}

	private JsonElement encode(Map<?, ?> map, String typeId, JsonSerializationContext context)
	{
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(STORED_AS_TYPE_KEY, typeId);

		// First pass: look for an element that could give us the type information about the
		// element(s) stored in the iterable.
		ValueTypeInfo keyInfo = null;
		ValueTypeInfo valueInfo = null;

		for (Object element : map.entrySet())
		{
			if (element instanceof Map.Entry)
			{
				Map.Entry<?, ?> entry = (Entry<?, ?>) element;
				Object key = entry.getKey();
				if (keyInfo == null && key != null)
				{
					keyInfo = classify(key);
				}
				Object value = entry.getValue();
				if (valueInfo == null && value != null)
				{
					valueInfo = classify(value);
				}
				if (keyInfo != null && valueInfo != null)
				{
					jsonObject.addProperty(STORED_AS_KEY_TYPE_KEY, keyInfo.getTypeId());
					jsonObject.addProperty(STORED_AS_VALUE_TYPE_KEY, valueInfo.getTypeId());
					break;
				}
			}
			else
			{
				throw new AssertionError();
			}
		}

		JsonArray jsonArray = new JsonArray();
		if (keyInfo != null)
		{
			Type keyType = keyInfo.getType();
			Type valueType = valueInfo != null ? valueInfo.getType() : null;
			for (Object element : map.entrySet())
			{
				if (element instanceof Map.Entry)
				{
					Map.Entry<?, ?> entry = (Entry<?, ?>) element;

					JsonArray entryArray = new JsonArray();

					// Serialize the key value pair directly into the entry array. This may recursively
					// invoke the serialize method above.
					entryArray.add(context.serialize(entry.getKey(), keyType));
				}
			}
		}

		// The Json array gets stored as the value.
		jsonObject.add(STORED_AS_VALUE_KEY, jsonArray);

		return jsonObject;
	}

	private JsonElement encode(Object attribute, String typeId, Type valueType, JsonSerializationContext context)
	{
		JsonObject jsonObject = new JsonObject();

		jsonObject.addProperty(STORED_AS_TYPE_KEY, typeId);

		// Serialize the attribute directly into the jsonObject. This may recursively
		// invoke the serialize method above.
		jsonObject.add(STORED_AS_VALUE_KEY, context.serialize(attribute, valueType));

		return jsonObject;
	}

	/**
	 * Top level decode method, called to reconstruct one Json element pulled from a
	 * serialized state and add it to a destination State.
	 * 
	 * @param keyId
	 * @param element
	 * @param context
	 * @param stateDest
	 */
	private void decode(String keyId, JsonElement element, JsonDeserializationContext context, State stateDest)
	{
		// if (element.isJsonNull()) we don't know the type of the key to construct, so this can't be
		// handled here.
		//
		// Similarly, if (element.isJsonArray()), additional metadata is needed to decode it, so this can't
		// be handled here.
		//
		if (element.isJsonPrimitive())
		{
			decode(keyId, element.getAsJsonPrimitive(), context, stateDest);
		}
		else if (element.isJsonObject())
		{
			JsonObject jsonObject = element.getAsJsonObject();
			String typeId = getString(jsonObject, STORED_AS_TYPE_KEY);
			ValueTypeInfo primaryInfo = getValueTypeInfo(typeId);

			//			if (primaryInfo.useJsonArray())
			//			{
			decode(keyId, jsonObject, context, primaryInfo, stateDest);
			//			}
		}
		else if (element.isJsonNull())
		{
			stateDest.put(GsonKey.of(keyId), null);
		}
		else
		{
			// This one is the programmer's fault.
			throw new AssertionError("Cannot call decode method for elements like " + element);
		}
	}

	private void decode(String keyId, JsonPrimitive primitive, JsonDeserializationContext context, State stateDest)
	{
		Type type = null;
		if (primitive.isBoolean())
		{
			type = ValueTypeInfo.BOOLEAN.getType();
		}
		else if (primitive.isString())
		{
			type = ValueTypeInfo.STRING.getType();
		}
		else if (primitive.isNumber())
		{
			type = ValueTypeInfo.DOUBLE.getType();
		}
		else
		{
			throw new IllegalArgumentException("Unable to deserialize Json primitive " + primitive);
		}
		stateDest.put(getKeyForType(keyId, type), context.deserialize(primitive, type));
	}

	/**
	 * Decode the supplied JsonObject, which might contain another state (a
	 * sub-state), a primitive, a null or even an array.
	 * 
	 * @param objectId
	 * @param jsonObject
	 * @param context
	 * @param primaryInfo
	 * @param stateDest
	 */
	private void decode(String objectId, JsonObject jsonObject, JsonDeserializationContext context, ValueTypeInfo primaryInfo, State stateDest)
	{
		JsonElement element = jsonObject.get(STORED_AS_VALUE_KEY);
		if (element == null)
		{
			throw new IllegalArgumentException("Field " + STORED_AS_VALUE_KEY + " is missing from Json object " + jsonObject);
		}

		// Must handle Json array case first, because it requires a StateKey with a secondary type. 
		if (element.isJsonArray())
		{
			String secondaryType = getString(jsonObject, STORED_AS_ELEMENT_TYPE_KEY);
			ValueTypeInfo secondaryInfo = getValueTypeInfo(secondaryType);
			if (ValueTypeInfo.LIST.equals(primaryInfo))
			{
				decodeList(getKeyForType(objectId, primaryInfo.getType(), secondaryInfo.getType()), element.getAsJsonArray(), context, secondaryInfo, stateDest);
				return;
			}
			if (ValueTypeInfo.MAP.equals(primaryInfo))
			{
				decodeMap(getKeyForType(objectId, primaryInfo.getType(), secondaryInfo.getType()), element.getAsJsonArray(), context, secondaryInfo, stateDest);
				return;
			}
			if (ValueTypeInfo.SET.equals(primaryInfo))
			{
				decodeSet(getKeyForType(objectId, primaryInfo.getType(), secondaryInfo.getType()), element.getAsJsonArray(), context, secondaryInfo, stateDest);
				return;
			}
			if (ValueTypeInfo.SORTED_SET.equals(primaryInfo))
			{
				decodeSortedSet(getKeyForType(objectId, primaryInfo.getType(), secondaryInfo.getType()), element.getAsJsonArray(), context, secondaryInfo, stateDest);
				return;
			}
			throw new IllegalArgumentException("Cannot decode a Json array into type " + primaryInfo);
		}

		// Remaining cases are scalars of various kinds.
		StateKey<?> simpleKey = getKeyForType(objectId, primaryInfo.getType());
		if (element.isJsonNull())
		{
			stateDest.put(simpleKey, null);
		}
		else if (element.isJsonPrimitive())
		{
			stateDest.put(simpleKey, context.deserialize(element, primaryInfo.getType()));
		}
		else if (element.isJsonObject())
		{
			// Recurse (potentially) to handle this case.
			stateDest.put(simpleKey, context.deserialize(element, primaryInfo.getType()));
		}
	}

	private <V> void decodeList(StateKey<List<V>> key, JsonArray jsonArray, JsonDeserializationContext context, ValueTypeInfo elementTypeInfo, State stateDest)
	{
		List<V> collection = new ArrayList<>();
		for (JsonElement element : jsonArray)
		{
			collection.add(context.deserialize(element, elementTypeInfo.getType()));
		}
		stateDest.put(key, collection);
	}

	private <K, V> void decodeMap(StateKey<Map<K, V>> key, JsonArray jsonArray, JsonDeserializationContext context, ValueTypeInfo elementTypeInfo, State stateDest)
	{
		Map<K, V> collection = new HashMap<>();
		for (JsonElement element : jsonArray)
		{
			Map.Entry<K, V> entry = context.deserialize(element, elementTypeInfo.getType());
			collection.put(entry.getKey(), entry.getValue());
		}
		stateDest.put(key, collection);
	}

	private <V> void decodeSet(StateKey<Set<V>> key, JsonArray jsonArray, JsonDeserializationContext context, ValueTypeInfo elementTypeInfo, State stateDest)
	{
		Set<V> collection = new HashSet<>();
		for (JsonElement element : jsonArray)
		{
			collection.add(context.deserialize(element, elementTypeInfo.getType()));
		}
		stateDest.put(key, collection);
	}

	private <V> void decodeSortedSet(StateKey<SortedSet<V>> key, JsonArray jsonArray, JsonDeserializationContext context, ValueTypeInfo elementTypeInfo, State stateDest)
	{
		SortedSet<V> collection = new TreeSet<>();
		for (JsonElement element : jsonArray)
		{
			collection.add(context.deserialize(element, elementTypeInfo.getType()));
		}
		stateDest.put(key, collection);
	}

	private String getString(JsonObject object, String field)
	{
		JsonPrimitive primitive = getPrimitive(object, field);
		if (!primitive.isString())
		{
			throw new IllegalArgumentException("Field " + field + " is not a String in the Json primitive " + primitive);
		}
		return primitive.getAsString();
	}

	private JsonPrimitive getPrimitive(JsonObject object, String field)
	{
		JsonElement element = object.get(field);
		if (element == null)
		{
			throw new IllegalArgumentException("Cannot get field " + field + " in Json object " + object);
		}
		if (!element.isJsonPrimitive())
		{
			throw new ClassCastException("Field " + field + " is unexpectedly not a Json primitive in object " + element);
		}
		return element.getAsJsonPrimitive();
	}

	@SuppressWarnings("unchecked")
	private <T> StateKey<T> getKeyForType(String keyId, Type type)
	{
		//		return StateKey.of(keyId, (Class<T>) getValueTypeInfo(type).getTypeClass());
		return GsonKey.of(keyId);
	}

	@SuppressWarnings("unchecked")
	private <T> StateKey<T> getKeyForType(String keyId, Type primaryType, Type secondaryType)
	{
		//		return StateKey.of(keyId, (Class<T>) getValueTypeInfo(primaryType).getTypeClass(), getValueTypeInfo(secondaryType).getTypeClass());
		return GsonKey.of(keyId);
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

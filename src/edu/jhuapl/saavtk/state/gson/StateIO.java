package edu.jhuapl.saavtk.state.gson;

import java.lang.reflect.Type;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
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
	private static final Type STATE_TYPE = new TypeToken<State>() {}.getType();
	private static final Type INT_TYPE = new TypeToken<Integer>() {}.getType();
	private static final Type LONG_TYPE = new TypeToken<Long>() {}.getType();
	private static final Type SHORT_TYPE = new TypeToken<Short>() {}.getType();
	private static final Type BYTE_TYPE = new TypeToken<Byte>() {}.getType();
	private static final Type DOUBLE_TYPE = new TypeToken<Double>() {}.getType();
	private static final Type FLOAT_TYPE = new TypeToken<Float>() {}.getType();
	private static final Type CHAR_TYPE = new TypeToken<Character>() {}.getType();
	private static final Type BOOL_TYPE = new TypeToken<Boolean>() {}.getType();
	private static final Type STRING_TYPE = new TypeToken<String>() {}.getType();

	private static final Type OBJECT_TYPE = new TypeToken<Object>() {}.getType();

	private static final String STORED_AS_TYPE_KEY = "type";
	private static final String STORED_AS_VALUE_KEY = "value";

	private static final String STORED_AS_STATE = "State";
	private static final String STORED_AS_INT = Integer.class.getSimpleName();
	private static final String STORED_AS_LONG = Long.class.getSimpleName();
	private static final String STORED_AS_SHORT = Short.class.getSimpleName();
	private static final String STORED_AS_BYTE = Byte.class.getSimpleName();
	private static final String STORED_AS_DOUBLE = Double.class.getSimpleName();
	private static final String STORED_AS_FLOAT = Float.class.getSimpleName();
	private static final String STORED_AS_CHAR = Character.class.getSimpleName();
	private static final String STORED_AS_BOOL = Boolean.class.getSimpleName();
	private static final String STORED_AS_STRING = String.class.getSimpleName();

	// @Override
	public Type getTargetType()
	{
		return STATE_TYPE;
	}

	@Override
	public JsonElement serialize(State src, Type typeOfSrc, JsonSerializationContext context)
	{
		Preconditions.checkNotNull(src);
		Preconditions.checkArgument(STATE_TYPE.equals(typeOfSrc));
		Preconditions.checkNotNull(context);

		JsonObject object = new JsonObject();
		for (Entry<StateKey<?>, Object> entry : src.getMap().entrySet())
		{
			encode(object, entry, context);
		}
		return object;
	}

	@Override
	public State deserialize(JsonElement jsonSrc, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		Preconditions.checkNotNull(jsonSrc);
		Preconditions.checkArgument(STATE_TYPE.equals(typeOfT));
		Preconditions.checkNotNull(context);

		State state = State.of();
		JsonObject object = (JsonObject) jsonSrc;
		for (Entry<String, JsonElement> entry : object.entrySet())
		{
			decode(state, entry, context);
		}
		return state;
	}

	private void encode(JsonObject jsonDest, Entry<StateKey<?>, Object> entry, JsonSerializationContext context)
	{
		StateKey<?> key = entry.getKey();
		Object attribute = entry.getValue();
		Type type = getTypeToStore(attribute);
		if (attribute instanceof State || attribute instanceof Number || attribute instanceof Character)
		{
			JsonObject jsonObject = new JsonObject();

			String typeName = attribute instanceof State ? "State" : attribute.getClass().getSimpleName();
			jsonObject.addProperty(STORED_AS_TYPE_KEY, typeName);
			jsonObject.add(STORED_AS_VALUE_KEY, context.serialize(attribute, type));

			jsonDest.add(key.getId(), jsonObject);
		}
		else
		{
			jsonDest.add(key.getId(), context.serialize(attribute));
		}
	}

	private void decode(State stateDest, Entry<String, JsonElement> entry, JsonDeserializationContext context)
	{
		String keyString = entry.getKey();
		JsonElement element = entry.getValue();
		if (element instanceof JsonPrimitive)
		{
			Object object = context.deserialize(element, OBJECT_TYPE);
			stateDest.put(StateKey.of(object, keyString), object);
		}
		else if (element instanceof JsonObject)
		{
			JsonObject jsonObject = (JsonObject) entry.getValue();
			JsonElement value = jsonObject.get(STORED_AS_VALUE_KEY);

			String typeName = jsonObject.get(STORED_AS_TYPE_KEY).getAsString();
			Type type = getTypeToRetrieve(typeName);

			Object object = context.deserialize(value, type);
			stateDest.put(StateKey.of(object, keyString), object);
		}
	}

	private Type getTypeToStore(Object object)
	{
		Type result = null;
		Class<?> clazz = object.getClass();
		if (State.class.equals(clazz))
		{
			result = STATE_TYPE;
		}
		else if (Integer.class.equals(clazz))
		{
			result = INT_TYPE;
		}
		else if (Long.class.equals(clazz))
		{
			result = LONG_TYPE;
		}
		else if (Short.class.equals(clazz))
		{
			result = SHORT_TYPE;
		}
		else if (Byte.class.equals(clazz))
		{
			result = BYTE_TYPE;
		}
		else if (Double.class.equals(clazz))
		{
			result = DOUBLE_TYPE;
		}
		else if (Float.class.equals(clazz))
		{
			result = FLOAT_TYPE;
		}
		else if (Character.class.equals(clazz))
		{
			result = CHAR_TYPE;
		}
		else if (Boolean.class.equals(clazz))
		{
			result = BOOL_TYPE;
		}
		else if (String.class.equals(clazz))
		{
			result = STRING_TYPE;
		}
		else
		{
			throw new IllegalArgumentException("Cannot store an object of type " + clazz.getSimpleName());
		}
		return result;
	}

	private Type getTypeToRetrieve(String typeName)
	{
		Type result = null;
		if (STORED_AS_STATE.equals(typeName))
		{
			result = STATE_TYPE;
		}
		else if (STORED_AS_INT.equals(typeName))
		{
			result = INT_TYPE;
		}
		else if (STORED_AS_LONG.equals(typeName))
		{
			result = LONG_TYPE;
		}
		else if (STORED_AS_SHORT.equals(typeName))
		{
			result = SHORT_TYPE;
		}
		else if (STORED_AS_BYTE.equals(typeName))
		{
			result = BYTE_TYPE;
		}
		else if (STORED_AS_DOUBLE.equals(typeName))
		{
			result = DOUBLE_TYPE;
		}
		else if (STORED_AS_FLOAT.equals(typeName))
		{
			result = FLOAT_TYPE;
		}
		else if (STORED_AS_CHAR.equals(typeName))
		{
			result = CHAR_TYPE;
		}
		else if (STORED_AS_BOOL.equals(typeName))
		{
			result = BOOL_TYPE;
		}
		else if (STORED_AS_STRING.equals(typeName))
		{
			result = STRING_TYPE;
		}
		else
		{
			throw new IllegalArgumentException("Cannot retrieve an object of type " + typeName);
		}
		return result;
	}
}

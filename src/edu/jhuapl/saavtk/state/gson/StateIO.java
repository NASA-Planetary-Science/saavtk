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

import edu.jhuapl.saavtk.state.Attribute;
import edu.jhuapl.saavtk.state.Attribute.ValueType;
import edu.jhuapl.saavtk.state.IntegerAttribute;
import edu.jhuapl.saavtk.state.State;
import edu.jhuapl.saavtk.state.StateKey;
import edu.jhuapl.saavtk.state.StringAttribute;

final class StateIO implements JsonSerializer<State>, JsonDeserializer<State>
{
	private static final Type INTEGER_TYPE = new TypeToken<Integer>() {}.getType();
	private static final Type STRING_TYPE = new TypeToken<String>() {}.getType();
	private static final Type STATE_TYPE = new TypeToken<State>() {}.getType();

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
		for (Entry<StateKey<?>, Attribute> entry : src.getMap().entrySet())
		{
			encode(object, entry, context);
		}
		return object;
	}

	@Override
	public State deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		Preconditions.checkNotNull(json);
		Preconditions.checkArgument(STATE_TYPE.equals(typeOfT));
		Preconditions.checkNotNull(context);

		State state = State.of();
		JsonObject object = (JsonObject) json;
		for (Entry<String, JsonElement> entry : object.entrySet())
		{
			decode(state, entry, context);
		}
		return state;
	}

	private void encode(JsonObject object, Entry<StateKey<?>, Attribute> entry, JsonSerializationContext context)
	{
		StateKey<?> key = entry.getKey();
		Attribute attribute = entry.getValue();
		ValueType valueType;
		JsonElement element;
		if (attribute instanceof IntegerAttribute)
		{
			valueType = IntegerAttribute.getValueType();
			IntegerAttribute integerAttribute = (IntegerAttribute) attribute;
			element = context.serialize(integerAttribute.get(), INTEGER_TYPE);
		}
		else if (attribute instanceof StringAttribute)
		{
			valueType = StringAttribute.getValueType();
			StringAttribute stringAttribute = (StringAttribute) attribute;
			element = context.serialize(stringAttribute.get(), STRING_TYPE);
		}
		else if (attribute instanceof State)
		{
			valueType = State.getValueType();
			element = context.serialize(attribute, STATE_TYPE);
		}
		else
		{
			throw new IllegalArgumentException();
		}
		JsonObject jsonEntry = new JsonObject();
		jsonEntry.add("type", new JsonPrimitive(valueType.getId()));
		jsonEntry.add("value", element);
		object.add(key.getId(), jsonEntry);
	}

	private void decode(State state, Entry<String, JsonElement> entry, JsonDeserializationContext context)
	{
		String keyString = entry.getKey();

		JsonObject jsonObject = (JsonObject) entry.getValue();
		String typeString = jsonObject.get("type").getAsString();
		JsonElement value = jsonObject.get("value");

		Attribute attribute = null;
		if (IntegerAttribute.getValueType().getId().equals(typeString))
		{
			attribute = new IntegerAttribute(context.deserialize(value, INTEGER_TYPE));
		}
		else if (StringAttribute.getValueType().getId().equals(typeString))
		{
			attribute = new StringAttribute(context.deserialize(value, STRING_TYPE));
		}
		else if (State.getValueType().getId().equals(typeString))
		{
			attribute = context.deserialize(value, STATE_TYPE);
		}
		else
		{
			throw new IllegalArgumentException();
		}
		state.put(new StateKey<>(keyString), attribute);
	}

}

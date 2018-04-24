package edu.jhuapl.saavtk.state.gson;

import java.lang.reflect.Type;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import edu.jhuapl.saavtk.state.State;
import edu.jhuapl.saavtk.state.StateKey;

final class StateIO implements JsonSerializer<State>, JsonDeserializer<State>
{
	private static final String STORED_AS_KEY = "key";
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

		JsonArray jsonArray = new JsonArray();
		for (StateKey<?> key : src.getKeys())
		{
			Object value = src.get(key);
			ValueTypeInfo info = ValueTypeInfo.forObject(value);
			JsonObject jsonEntry = new JsonObject();

			jsonEntry.add(STORED_AS_KEY, context.serialize(key, ValueTypeInfo.STATE_KEY.getType()));
			jsonEntry.add(STORED_AS_VALUE_TYPE_KEY, context.serialize(info.getTypeId()));
			jsonEntry.add(STORED_AS_VALUE_KEY, context.serialize(value, info.getType()));

			jsonArray.add(jsonEntry);
		}
		return jsonArray;
	}

	@Override
	public State deserialize(JsonElement jsonSrc, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		Preconditions.checkNotNull(jsonSrc);
		Preconditions.checkArgument(jsonSrc.isJsonArray());
		Preconditions.checkArgument(ValueTypeInfo.STATE.getType().equals(typeOfT));
		Preconditions.checkNotNull(context);

		final State state = State.of();
		final JsonArray jsonArray = jsonSrc.getAsJsonArray();
		for (JsonElement jsonElement : jsonArray)
		{
			if (!jsonElement.isJsonObject())
			{
				throw new IllegalArgumentException();
			}
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			StateKey<?> key = context.deserialize(jsonObject.get(STORED_AS_KEY), ValueTypeInfo.STATE_KEY.getType());
			String typeId = context.deserialize(jsonObject.get(STORED_AS_VALUE_TYPE_KEY), ValueTypeInfo.STRING.getType());
			JsonElement valueElement = jsonObject.get(STORED_AS_VALUE_KEY);
			state.put(key, context.deserialize(valueElement, ValueTypeInfo.of(typeId).getType()));
		}
		return state;
	}

}

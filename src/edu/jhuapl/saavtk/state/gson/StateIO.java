package edu.jhuapl.saavtk.state.gson;

import java.lang.reflect.Type;
import java.util.Iterator;

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
import edu.jhuapl.saavtk.state.Version;

final class StateIO implements JsonSerializer<State>, JsonDeserializer<State>
{
	private static final String STORED_AS_ELEMENTS_KEY = "Elements";

	@Override
	public JsonElement serialize(State src, Type typeOfSrc, JsonSerializationContext context)
	{
		Preconditions.checkNotNull(src);
		Preconditions.checkArgument(ValueTypeInfo.STATE.getType().equals(typeOfSrc));
		Preconditions.checkNotNull(context);

		JsonArray array = new JsonArray();
		array.add(context.serialize(src.getVersion(), ValueTypeInfo.VERSION.getType()));

		JsonArray valueArray = new JsonArray();
		for (StateKey<?> key : src.getKeys())
		{
			Object value = src.get(key);
			valueArray.add(context.serialize(GsonElement.of(key, value), ValueTypeInfo.ELEMENT.getType()));
		}

		JsonObject jsonObject = null;

		jsonObject = new JsonObject();
		jsonObject.add(STORED_AS_ELEMENTS_KEY, valueArray);
		array.add(jsonObject);

		return array;
	}

	@Override
	public State deserialize(JsonElement jsonSrc, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		Preconditions.checkNotNull(jsonSrc);
		Preconditions.checkArgument(jsonSrc.isJsonArray());
		Preconditions.checkArgument(ValueTypeInfo.STATE.getType().equals(typeOfT));
		Preconditions.checkNotNull(context);

		//		JsonObject jsonObject = jsonSrc.getAsJsonObject();
		//		JsonElement arrayAsElement = jsonObject.get(ValueTypeInfo.STATE.getTypeId());
		//		if (arrayAsElement == null)
		//		{
		//			throw new IllegalArgumentException();
		//		}
		//
		//		JsonArray jsonArray = arrayAsElement.getAsJsonArray();
		JsonArray jsonArray = jsonSrc.getAsJsonArray();
		JsonElement jsonElement = null;
		Iterator<JsonElement> iterator = jsonArray.iterator();

		if (!iterator.hasNext())
		{
			throw new IllegalArgumentException();
		}
		jsonElement = iterator.next();

		Version version = context.deserialize(jsonElement, ValueTypeInfo.VERSION.getType());

		final State state = State.of(GsonKey.of("spud"), version);
		if (!iterator.hasNext())
		{
			throw new IllegalArgumentException();
		}
		jsonElement = iterator.next();
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		jsonArray = jsonObject.get(STORED_AS_ELEMENTS_KEY).getAsJsonArray();
		for (JsonElement arrayElement : jsonArray)
		{
			GsonElement element = context.deserialize(arrayElement, ValueTypeInfo.ELEMENT.getType());
			state.put(element.getKey(), element.getValue());
		}
		return state;
	}

}

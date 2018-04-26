package edu.jhuapl.saavtk.state.gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import edu.jhuapl.saavtk.state.StateKey;

final class GsonElement
{
	static final class ElementIO implements JsonSerializer<GsonElement>, JsonDeserializer<GsonElement>
	{
		@Override
		public GsonElement deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
		{
			Preconditions.checkArgument(ValueTypeInfo.ELEMENT.getType().equals(typeOfT));
			return GsonElement.of(jsonElement, context);
		}

		@Override
		public JsonElement serialize(GsonElement src, Type typeOfSrc, JsonSerializationContext context)
		{
			Preconditions.checkArgument(ValueTypeInfo.ELEMENT.getType().equals(typeOfSrc));
			return src.toElement(context);
		}

	}

	private static final String STORED_AS_VALUE_KEY = "Value";

	public static ElementIO elementIO()
	{
		return new ElementIO();
	}

	public static GsonElement of(StateKey<?> key, Object object)
	{
		return new GsonElement(key, object, ValueTypeInfo.forObject(object));
	}

	private static GsonElement of(JsonElement element, JsonDeserializationContext context)
	{
		Preconditions.checkNotNull(element);
		Preconditions.checkNotNull(context);
		Preconditions.checkArgument(element.isJsonObject());

		List<Entry<String, JsonElement>> entryList = new ArrayList<>(element.getAsJsonObject().entrySet());
		Preconditions.checkState(entryList.size() == 1);

		Entry<String, JsonElement> entry = entryList.get(0);
		ValueTypeInfo objectInfo = ValueTypeInfo.of(entry.getKey());
		if (objectInfo != ValueTypeInfo.ELEMENT)
		{
			throw new IllegalStateException();
		}

		JsonArray array = entry.getValue().getAsJsonArray();
		Preconditions.checkState(array.size() == 2);

		StateKey<?> key = context.deserialize(array.get(0), ValueTypeInfo.STATE_KEY.getType());
		JsonObject valueObject = array.get(1).getAsJsonObject();

		entryList = new ArrayList<>(valueObject.entrySet());
		Preconditions.checkState(entryList.size() == 1);

		entry = entryList.get(0);
		objectInfo = ValueTypeInfo.of(entry.getKey());

		Object object = context.deserialize(entry.getValue(), objectInfo.getType());

		return new GsonElement(key, object, objectInfo);
	}

	private final StateKey<?> key;
	private final Object object;
	private final ValueTypeInfo objectInfo;

	private GsonElement(StateKey<?> key, Object object, ValueTypeInfo objectInfo)
	{
		Preconditions.checkNotNull(key);
		Preconditions.checkNotNull(objectInfo);
		this.key = key;
		this.object = object;
		this.objectInfo = objectInfo;
	}

	public JsonElement toElement(JsonSerializationContext context)
	{
		JsonArray keyValuePair = new JsonArray();
		keyValuePair.add(context.serialize(key, ValueTypeInfo.STATE_KEY.getType()));

		JsonObject valueObject = new JsonObject();
		valueObject.add(objectInfo.getTypeId(), context.serialize(object, objectInfo.getType()));
		keyValuePair.add(valueObject);

		JsonObject result = new JsonObject();
		result.add(ValueTypeInfo.ELEMENT.getTypeId(), keyValuePair);
		return result;
	}

	public <T> StateKey<T> getKey()
	{
		@SuppressWarnings("unchecked")
		StateKey<T> result = (StateKey<T>) key;
		return result;
	}

	public Object getValue()
	{
		return object;
	}

	public ValueTypeInfo getInfo()
	{
		return objectInfo;
	}
}

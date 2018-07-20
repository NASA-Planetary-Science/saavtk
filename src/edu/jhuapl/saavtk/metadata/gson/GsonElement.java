package edu.jhuapl.saavtk.metadata.gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import edu.jhuapl.saavtk.metadata.Key;

final class GsonElement
{
	static final class ElementIO implements JsonSerializer<GsonElement>, JsonDeserializer<GsonElement>
	{
		@Override
		public GsonElement deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
		{
			Preconditions.checkArgument(DataTypeInfo.ELEMENT.getType().equals(typeOfT));
			return GsonElement.of(jsonElement, context);
		}

		@Override
		public JsonElement serialize(GsonElement src, Type typeOfSrc, JsonSerializationContext context)
		{
			Preconditions.checkArgument(DataTypeInfo.ELEMENT.getType().equals(typeOfSrc));
			return src.toElement(context);
		}

	}

	public static ElementIO elementIO()
	{
		return new ElementIO();
	}

	public static GsonElement of(Key<?> key, Object object)
	{
		return new GsonElement(key, object, DataTypeInfo.forObject(object));
	}

	private static GsonElement of(JsonElement element, JsonDeserializationContext context)
	{
		Preconditions.checkNotNull(element);
		Preconditions.checkNotNull(context);
		Preconditions.checkArgument(element.isJsonObject());

		List<Entry<String, JsonElement>> entryList = new ArrayList<>(element.getAsJsonObject().entrySet());
		Preconditions.checkState(entryList.size() == 1);

		Entry<String, JsonElement> entry = entryList.get(0);

		Key<?> key = Key.of(entry.getKey());
		JsonObject valueObject = entry.getValue().getAsJsonObject();

		entryList = new ArrayList<>(valueObject.entrySet());
		Preconditions.checkState(entryList.size() == 1);

		entry = entryList.get(0);
		DataTypeInfo objectInfo = DataTypeInfo.of(entry.getKey());

		Object object = context.deserialize(entry.getValue(), objectInfo.getType());

		return new GsonElement(key, object, objectInfo);
	}

	private final Key<?> key;
	private final Object object;
	private final DataTypeInfo objectInfo;

	private GsonElement(Key<?> key, Object object, DataTypeInfo objectInfo)
	{
		Preconditions.checkNotNull(key);
		Preconditions.checkNotNull(objectInfo);
		this.key = key;
		this.object = object;
		this.objectInfo = objectInfo;
	}

	public JsonElement toElement(JsonSerializationContext context)
	{
		JsonObject result = new JsonObject();

		JsonObject valueObject = new JsonObject();
		valueObject.add(objectInfo.getTypeId(), context.serialize(object, objectInfo.getType()));

		result.add(key.getId(), valueObject);
		return result;
	}

	public <T> Key<T> getKey()
	{
		@SuppressWarnings("unchecked")
		Key<T> result = (Key<T>) key;
		return result;
	}

	public Object getValue()
	{
		return object;
	}

	public DataTypeInfo getInfo()
	{
		return objectInfo;
	}
}

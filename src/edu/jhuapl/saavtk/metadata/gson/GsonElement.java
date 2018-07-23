package edu.jhuapl.saavtk.metadata.gson;

import java.lang.reflect.Type;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

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

	public static GsonElement of(Object object)
	{
		return new GsonElement(object, DataTypeInfo.forObject(object));
	}

	private static GsonElement of(JsonElement element, JsonDeserializationContext context)
	{
		Preconditions.checkNotNull(element);
		Preconditions.checkNotNull(context);
		Preconditions.checkArgument(element.isJsonObject());

		JsonObject valueObject = element.getAsJsonObject();

		Set<Entry<String, JsonElement>> entryList = valueObject.entrySet();
		Preconditions.checkState(entryList.size() == 1);

		Entry<String, JsonElement> entry = entryList.iterator().next();
		DataTypeInfo objectInfo = DataTypeInfo.of(entry.getKey());

		Object object = context.deserialize(entry.getValue(), objectInfo.getType());

		return new GsonElement(object, objectInfo);
	}

	private final Object object;
	private final DataTypeInfo objectInfo;

	private GsonElement(Object object, DataTypeInfo objectInfo)
	{
		Preconditions.checkNotNull(objectInfo);
		this.object = object;
		this.objectInfo = objectInfo;
	}

	public JsonElement toElement(JsonSerializationContext context)
	{
		JsonObject result = new JsonObject();
		result.add(objectInfo.getTypeId(), context.serialize(object, objectInfo.getType()));
		return result;
	}

	public DataTypeInfo getInfo()
	{
		return objectInfo;
	}

	public Object getValue()
	{
		return object;
	}
}

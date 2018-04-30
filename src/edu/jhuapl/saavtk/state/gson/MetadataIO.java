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

import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.Version;

final class MetadataIO implements JsonSerializer<Metadata>, JsonDeserializer<Metadata>
{
	private static final String STORED_AS_ELEMENTS_KEY = "Elements";

	@Override
	public JsonElement serialize(Metadata src, Type typeOfSrc, JsonSerializationContext context)
	{
		Preconditions.checkNotNull(src);
		Preconditions.checkArgument(ValueTypeInfo.METADATA.getType().equals(typeOfSrc));
		Preconditions.checkNotNull(context);

		JsonArray array = new JsonArray();
		array.add(context.serialize(src.getVersion(), ValueTypeInfo.VERSION.getType()));

		JsonArray valueArray = new JsonArray();
		for (Key<?> key : src.getKeys())
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
	public Metadata deserialize(JsonElement jsonSrc, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		Preconditions.checkNotNull(jsonSrc);
		Preconditions.checkArgument(jsonSrc.isJsonArray());
		Preconditions.checkArgument(ValueTypeInfo.METADATA.getType().equals(typeOfT));
		Preconditions.checkNotNull(context);

		JsonArray jsonArray = jsonSrc.getAsJsonArray();
		JsonElement jsonElement = null;
		Iterator<JsonElement> iterator = jsonArray.iterator();

		if (!iterator.hasNext())
		{
			throw new IllegalArgumentException();
		}
		jsonElement = iterator.next();

		Version version = context.deserialize(jsonElement, ValueTypeInfo.VERSION.getType());

		final Metadata metadata = Metadata.of(version);
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
			metadata.put(element.getKey(), element.getValue());
		}
		return metadata;
	}

}

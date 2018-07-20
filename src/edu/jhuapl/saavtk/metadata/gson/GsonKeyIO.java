package edu.jhuapl.saavtk.metadata.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import edu.jhuapl.saavtk.metadata.Key;

final class GsonKeyIO implements JsonSerializer<Key<?>>, JsonDeserializer<Key<?>>
{
	private static final String KEY_ID = DataTypeInfo.METADATA_KEY.getTypeId();

	@Override
	public JsonElement serialize(Key<?> src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonElement result = new JsonPrimitive(src.getId());
		return result;
	}

	@Override
	public Key<?> deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		if (!jsonElement.isJsonPrimitive())
		{
			throw new IllegalArgumentException();
		}

		JsonPrimitive object = jsonElement.getAsJsonPrimitive();

		return Key.of(object.getAsString());
	}

}

package edu.jhuapl.saavtk.state.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

final class GsonKeyIO implements JsonSerializer<GsonKey<?>>, JsonDeserializer<GsonKey<?>>
{
	private static final String KEY_ID = ValueTypeInfo.STATE_KEY.getTypeId();

	@Override
	public JsonElement serialize(GsonKey<?> src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject result = new JsonObject();
		result.addProperty(KEY_ID, src.getId());
		return result;
	}

	@Override
	public GsonKey<?> deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		if (!jsonElement.isJsonObject())
		{
			throw new IllegalArgumentException();
		}

		JsonObject object = jsonElement.getAsJsonObject();

		// Unpack metadata.
		JsonElement keyIdElement = object.get(KEY_ID);
		if (keyIdElement == null || !keyIdElement.isJsonPrimitive())
		{
			throw new IllegalArgumentException("Field \"" + KEY_ID + "\" is missing or has wrong type in Json object");
		}

		return GsonKey.of(keyIdElement.getAsString());
	}

}

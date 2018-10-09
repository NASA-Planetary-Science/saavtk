package edu.jhuapl.saavtk.metadata.serialization.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import edu.jhuapl.saavtk.metadata.Version;

final class GsonVersionIO implements JsonSerializer<Version>, JsonDeserializer<Version>
{
	private static final String KEY_ID = DataTypeInfo.VERSION.getTypeId();

	@Override
	public JsonElement serialize(Version src, @SuppressWarnings("unused") Type typeOfSrc, @SuppressWarnings("unused") JsonSerializationContext context)
	{
		JsonObject result = new JsonObject();
		result.addProperty(KEY_ID, src.toString());
		return result;
	}

	@Override
	public Version deserialize(JsonElement jsonElement, @SuppressWarnings("unused") Type typeOfT, @SuppressWarnings("unused") JsonDeserializationContext context) throws JsonParseException
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

		return Version.of(keyIdElement.getAsString());
	}

}

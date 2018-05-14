package edu.jhuapl.saavtk.state.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class NumberIO implements JsonDeserializer<Number>, JsonSerializer<Number>
{

	@Override
	public JsonElement serialize(Number src, Type typeOfSrc, JsonSerializationContext context)
	{

		return null;
	}

	@Override
	public Number deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		// TODO Auto-generated method stub
		return null;
	}

}

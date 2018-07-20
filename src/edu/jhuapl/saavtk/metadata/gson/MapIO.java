package edu.jhuapl.saavtk.metadata.gson;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

final class MapIO implements JsonSerializer<Map<?, ?>>, JsonDeserializer<Map<?, ?>>
{
	private static final String MAP_TYPE = "mapType";
	private static final String MAP_KEY_TYPE = "keyType";
	private static final String MAP_VALUE_TYPE = "valueType";
	private static final String MAP_VALUE = "value";

	@Override
	public JsonElement serialize(Map<?, ?> src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject result = new JsonObject();

		// First pass: if any entries are found, determine types of key and value.
		DataTypeInfo keyInfo = DataTypeInfo.NULL;
		DataTypeInfo valueInfo = DataTypeInfo.NULL;
		for (Entry<?, ?> entry : src.entrySet())
		{
			if (keyInfo == DataTypeInfo.NULL)
			{
				keyInfo = DataTypeInfo.forObject(entry.getKey());
			}
			if (valueInfo == DataTypeInfo.NULL)
			{
				valueInfo = DataTypeInfo.forObject(entry.getValue());
			}
			if (keyInfo != DataTypeInfo.NULL && valueInfo != DataTypeInfo.NULL)
			{
				break;
			}
		}

		// Second pass: write the map entries to a JsonObject.
		JsonObject dest = new JsonObject();
		Type keyType = keyInfo.getType();
		Type valueType = valueInfo.getType();

		for (Entry<?, ?> entry : src.entrySet())
		{
			Object key = entry.getKey();
			Object value = entry.getValue();
			dest.add(key != null ? key.toString() : JsonNull.INSTANCE.toString(), context.serialize(value, valueType));
		}

		// Put type information about key and value, along with the map entries
		// into the resultant object.
		result.add(MAP_TYPE, context.serialize(DataTypeInfo.forObject(src).getTypeId()));
		result.add(MAP_KEY_TYPE, context.serialize(keyInfo.getTypeId()));
		result.add(MAP_VALUE_TYPE, context.serialize(valueInfo.getTypeId()));
		result.add(MAP_VALUE, dest);

		return result;
	}

	@Override
	public Map<?, ?> deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		if (!jsonElement.isJsonObject())
		{
			throw new IllegalArgumentException();
		}

		JsonObject object = jsonElement.getAsJsonObject();

		// Unpack metadata.
		JsonElement mapTypeElement = object.get(MAP_TYPE);
		if (mapTypeElement == null || !mapTypeElement.isJsonPrimitive())
		{
			throw new IllegalArgumentException("Field \"" + MAP_TYPE + "\" is missing or has wrong type in Json object");
		}

		JsonElement keyTypeElement = object.get(MAP_KEY_TYPE);
		if (keyTypeElement == null || !keyTypeElement.isJsonPrimitive())
		{
			throw new IllegalArgumentException("Field \"" + MAP_KEY_TYPE + "\" is missing or has wrong type in Json object");
		}

		JsonElement valueTypeElement = object.get(MAP_VALUE_TYPE);
		if (valueTypeElement == null || !valueTypeElement.isJsonPrimitive())
		{
			throw new IllegalArgumentException("Field \"" + MAP_VALUE_TYPE + "\" is missing or has wrong type in Json object");
		}

		DataTypeInfo dataInfo = DataTypeInfo.of(mapTypeElement.getAsString());
		DataTypeInfo keyInfo = DataTypeInfo.of(keyTypeElement.getAsString());
		DataTypeInfo valueInfo = DataTypeInfo.of(valueTypeElement.getAsString());

		// Create output data object.
		Map<?, ?> map = null;
		if (dataInfo == DataTypeInfo.SORTED_MAP)
		{
			map = new TreeMap<>();
		}
		else if (dataInfo == DataTypeInfo.MAP)
		{
			map = new HashMap<>();
		}
		else
		{
			throw new IllegalArgumentException("Do not know how to deserialize an iterable of type " + dataInfo);
		}

		// Unpack data.
		JsonElement dataElement = object.get(MAP_VALUE);
		if (dataElement == null || !dataElement.isJsonObject())
		{
			throw new IllegalArgumentException("Field \"" + MAP_VALUE + "\" is missing or has wrong type in Json object");
		}

		Type keyType = keyInfo.getType();
		Type valueType = valueInfo.getType();
		for (Entry<String, JsonElement> entry : dataElement.getAsJsonObject().entrySet())
		{
			String keyString = entry.getKey();
			JsonElement keyElement = keyString.equals("null") ? JsonNull.INSTANCE : new JsonPrimitive(keyString);
			JsonElement valueElement = entry.getValue();

			map.put(context.deserialize(keyElement, keyType), context.deserialize(valueElement, valueType));
		}
		return map;
	}

	public static void main(String[] args)
	{
		Map<Integer, String> map1 = new HashMap<>();
		map1.put(1, "One");
		map1.put(null, "Null");

		Map<Integer, String> map2 = new HashMap<>();
		map2.put(0, "Zero");
		map2.put(2, null);

		SortedMap<String, Map<Integer, String>> createdMap = new TreeMap<>();
		createdMap.put("Map one", map1);
		createdMap.put("Map two", map2);

		Gson GSON = new GsonBuilder().serializeNulls().registerTypeAdapter(DataTypeInfo.of(SortedMap.class).getType(), new MapIO()).registerTypeAdapter(DataTypeInfo.of(Map.class).getType(), new MapIO()).setPrettyPrinting().create();

		String file = "/Users/peachjm1/Downloads/test-map.json";
		try (FileWriter fileWriter = new FileWriter(file))
		{
			try (JsonWriter jsonWriter = GSON.newJsonWriter(fileWriter))
			{
				GSON.toJson(createdMap, DataTypeInfo.forObject(createdMap).getType(), jsonWriter);
				fileWriter.write('\n');
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		try (JsonReader jsonReader = GSON.newJsonReader(new FileReader(file)))
		{
			Map<?, ?> readMap = GSON.fromJson(jsonReader, DataTypeInfo.of(Map.class).getType());
			if (!readMap.equals(createdMap))
			{
				System.err.println("OUTPUT IS NOT EQUAL TO INPUT!!");
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

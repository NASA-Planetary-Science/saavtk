package edu.jhuapl.saavtk.state.gson;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

final class IterableIO implements JsonSerializer<Iterable<?>>, JsonDeserializer<Iterable<?>>
{
	private static final String ITERABLE_TYPE = "collectionType";
	private static final String ITERABLE_VALUE_TYPE = "valueType";
	private static final String ITERABLE_VALUE = "value";

	@Override
	public JsonElement serialize(Iterable<?> src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject result = new JsonObject();

		// First pass: if any entries are found, determine types of key and value.
		ValueTypeInfo valueInfo = ValueTypeInfo.NULL;
		for (Object value : src)
		{
			if (valueInfo == ValueTypeInfo.NULL)
			{
				valueInfo = ValueTypeInfo.forObject(value);
			}
			if (valueInfo != ValueTypeInfo.NULL)
			{
				break;
			}
		}

		// Second pass: write the collection's entries to a JsonArray.
		JsonArray destArray = new JsonArray();
		Type valueType = valueInfo.getType();

		for (Object value : src)
		{
			destArray.add(context.serialize(value, valueType));
		}

		// Put iterable metadata and data into the resultant object.
		result.add(ITERABLE_TYPE, context.serialize(ValueTypeInfo.forObject(src).getTypeId()));
		result.add(ITERABLE_VALUE_TYPE, context.serialize(valueInfo.getTypeId()));
		result.add(ITERABLE_VALUE, destArray);

		return result;
	}

	@Override
	public Iterable<?> deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		if (!jsonElement.isJsonObject())
		{
			throw new IllegalArgumentException();
		}

		JsonObject object = jsonElement.getAsJsonObject();

		// Unpack metadata.
		JsonElement iterableTypeElement = object.get(ITERABLE_TYPE);
		if (iterableTypeElement == null || !iterableTypeElement.isJsonPrimitive())
		{
			throw new IllegalArgumentException("Field \"" + ITERABLE_TYPE + "\" is missing or has wrong type in Json object");
		}

		JsonElement valueTypeElement = object.get(ITERABLE_VALUE_TYPE);
		if (valueTypeElement == null || !valueTypeElement.isJsonPrimitive())
		{
			throw new IllegalArgumentException("Field \"" + ITERABLE_VALUE_TYPE + "\" is missing or has wrong type in Json object");
		}

		ValueTypeInfo dataInfo = ValueTypeInfo.of(iterableTypeElement.getAsString());
		ValueTypeInfo valueInfo = ValueTypeInfo.of(valueTypeElement.getAsString());

		// Create output data object.
		Set<?> set = null;
		List<?> list = null;
		if (dataInfo == ValueTypeInfo.SORTED_SET)
		{
			set = new TreeSet<>();
		}
		else if (dataInfo == ValueTypeInfo.SET)
		{
			set = new HashSet<>();
		}
		else if (dataInfo == ValueTypeInfo.LIST)
		{
			list = new ArrayList<>();
		}
		else
		{
			throw new IllegalArgumentException("Do not know how to deserialize an iterable of type " + dataInfo);
		}

		// Unpack data.
		JsonElement dataElement = object.get(ITERABLE_VALUE);
		if (dataElement == null || !dataElement.isJsonArray())
		{
			throw new IllegalArgumentException("Field \"" + ITERABLE_VALUE + "\" is missing or has wrong type in Json object");
		}

		Type valueType = valueInfo.getType();
		for (JsonElement entryElement : dataElement.getAsJsonArray())
		{
			if (set != null)
			{
				set.add(context.deserialize(entryElement, valueType));
			}
			else if (list != null)
			{
				list.add(context.deserialize(entryElement, valueType));
			}
			else
			{
				throw new AssertionError();
			}
		}
		return set != null ? set : list;
	}

	public static void main(String[] args)
	{
		Set<String> set1 = new HashSet<>();
		set1.add("One");
		set1.add(null);

		Set<String> set2 = new HashSet<>();
		set2.add("Zero");
		set2.add("Two");

		List<Set<String>> createdList = new ArrayList<>();
		createdList.add(set1);
		createdList.add(set2);

		Gson GSON =
				new GsonBuilder().serializeNulls().registerTypeAdapter(ValueTypeInfo.of(SortedSet.class).getType(), new IterableIO()).registerTypeAdapter(ValueTypeInfo.of(Set.class).getType(), new IterableIO()).registerTypeAdapter(ValueTypeInfo.of(List.class).getType(), new IterableIO()).setPrettyPrinting().create();

		String file = "/Users/peachjm1/Downloads/test-iterables.json";
		try (FileWriter fileWriter = new FileWriter(file))
		{
			try (JsonWriter jsonWriter = GSON.newJsonWriter(fileWriter))
			{
				GSON.toJson(createdList, ValueTypeInfo.forObject(createdList).getType(), jsonWriter);
				fileWriter.write('\n');
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		try (JsonReader jsonReader = GSON.newJsonReader(new FileReader(file)))
		{
			Iterable<?> readList = GSON.fromJson(jsonReader, ValueTypeInfo.of(List.class).getType());
			if (!readList.equals(createdList))
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

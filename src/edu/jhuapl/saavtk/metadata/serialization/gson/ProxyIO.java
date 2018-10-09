package edu.jhuapl.saavtk.metadata.serialization.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import edu.jhuapl.saavtk.metadata.InstanceGetter;
import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.ObjectToMetadata;

final class ProxyIO<T> implements JsonSerializer<ObjectToMetadata<?>>, JsonDeserializer<T>
{

	@Override
	public JsonElement serialize(ObjectToMetadata<?> src, @SuppressWarnings("unused") Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = new JsonObject();
		object.addProperty("proxiedType", src.getProxyKey().getId());
		object.add("proxyMetadata", context.serialize(src.to(), DataTypeInfo.METADATA.getType()));
		return object;
	}

	@Override
	public T deserialize(JsonElement json, @SuppressWarnings("unused") Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject object = json.getAsJsonObject();
		Key<T> proxyKey = Key.of(object.get("proxiedType").getAsString());
		Metadata objectMetadata = context.deserialize(object.get("proxyMetadata"), DataTypeInfo.METADATA.getType());
		return InstanceGetter.defaultInstanceGetter().of(proxyKey).from(objectMetadata);
	}

}

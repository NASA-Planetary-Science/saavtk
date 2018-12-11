package crucible.crust.metadata.impl.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.StorableAsMetadata;
import crucible.crust.metadata.impl.InstanceGetter;

final class ProxyIO<T> implements JsonSerializer<StorableAsMetadata<?>>, JsonDeserializer<T>
{

	@Override
	public JsonElement serialize(StorableAsMetadata<?> src, @SuppressWarnings("unused") Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject object = new JsonObject();
		object.addProperty("proxiedType", src.getKey().getId());
		object.add("proxyMetadata", context.serialize(src.store(), DataTypeInfo.METADATA.getType()));
		return object;
	}

	@Override
	public T deserialize(JsonElement json, @SuppressWarnings("unused") Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		JsonObject object = json.getAsJsonObject();
		Key<T> proxyKey = Key.of(object.get("proxiedType").getAsString());
		Metadata objectMetadata = context.deserialize(object.get("proxyMetadata"), DataTypeInfo.METADATA.getType());
		return InstanceGetter.defaultInstanceGetter().of(proxyKey).provide(objectMetadata);
	}

}

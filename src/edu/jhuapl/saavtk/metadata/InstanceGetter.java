package edu.jhuapl.saavtk.metadata;

import java.util.Map;
import java.util.TreeMap;

/**
 * A collection of {@link MetadataToObject}s that may be stored or retrieved
 * using an associated {@link Key}. Keys must be unique within one
 * InstanceGetter.
 * 
 * @param <T> the object type that can be gotten from the Metadata
 */
public final class InstanceGetter
{
	public static final Key<String> PROXY_TYPE = Key.of("proxiedType");
	public static final Key<Metadata> PROXIED_METADATA = Key.of("proxiedMetadata");

	/**
	 * Return a reference to the standard/global InstanceGetter. Most applications
	 * will use this so that any code may gain access to the various types of
	 * available {@link MetadataToObject} objects.
	 * 
	 * @return the instance
	 */
	public static InstanceGetter defaultInstanceGetter()
	{
		return DEFAULT_INSTANCE_GETTER;
	}

	private static final InstanceGetter DEFAULT_INSTANCE_GETTER = new InstanceGetter();

	private final Map<Key<?>, MetadataToObject<?>> proxyMap;

	/**
	 * Create a new InstanceGetter. In general, it's best to use the standard/global
	 * InstanceGetter supplied by the defaultInstanceGetter method. However, this
	 * constructor is public in case it ever becomes necessary to stand up an
	 * independent one (say to override how objects of a particular class are
	 * proxied by default).
	 */
	public InstanceGetter()
	{
		this.proxyMap = new TreeMap<>();
	}

	/**
	 * Get the {@link MetadataToObject} object that matches the supplied key,
	 * provided the InstanceGetter has access to one.
	 * 
	 * @param proxyTypeKey the key uniquely identifying the type of the object to be
	 *            handled by the returned {@link MetadataToObject}
	 * @return the helper object that may be used to create/get objects from
	 *         {@link Metadata}
	 * @throws IllegalArgumentException if this InstanceGetter does not have a
	 *             {@link MetadataToObject} object for the supplied key
	 * @throws ClassCastException if the supplied key matches a
	 *             {@link MetadataToObject} object managed by this InstanceGetter,
	 *             but the key has the wrong type.
	 */
	public <T> MetadataToObject<T> of(Key<T> proxyTypeKey)
	{
		if (!proxyMap.containsKey(proxyTypeKey))
		{
			throw new IllegalArgumentException();
		}
		@SuppressWarnings("unchecked")
		MetadataToObject<T> result = (MetadataToObject<T>) proxyMap.get(proxyTypeKey);
		return result;
	}

	/**
	 * Register a {@link MetadataToObject} object to the supplied key. The
	 * MetadataToObject will subsequently be accessible using this key from the of
	 * method.
	 * 
	 * @param proxyTypeKey the key identifying the type of object that may be
	 *            obtained by the MetadataToObject
	 * @param fromMetadata the MetadataToObject object to associate with this key
	 * @throws IllegalStateException if this InstanceGetter already has a
	 *             MetadataToObject associated with the key
	 */
	public <T> void register(Key<T> proxyTypeKey, MetadataToObject<? extends T> fromMetadata)
	{
		if (proxyMap.containsKey(proxyTypeKey))
		{
			throw new IllegalStateException();
		}
		proxyMap.put(proxyTypeKey, fromMetadata);
	}

	/**
	 * Deregister (remove/don't track or use) the {@link MetadataToObject}
	 * associated with this key, if any.
	 * 
	 * @param proxyTypeKey the key identifying the MetadataToObject to remove
	 */
	public void deRegister(Key<?> proxyForType)
	{
		proxyMap.remove(proxyForType);
	}

}

package edu.jhuapl.saavtk.metadata;

/**
 * Abstraction that may be converted to (proxied as) {@link Metadata}.
 * 
 * @param <T> the object type that can be converted to Metadata
 */
public interface ObjectToMetadata<T>
{
	/**
	 * Return a key that uniquely identifies the type of object being proxied. This
	 * is used in lieu of the object's Class to identify the type for purposes of
	 * saving/restoring the {@link Metadata}.
	 * 
	 * @return the key
	 * @see {@link MetadataToObject}
	 */
	Key<T> getProxyKey();

	/**
	 * Return a set of {@link Metadata} that encapsulates the complete state of the
	 * object, sufficient for subsequent restoration using a matched
	 * {@link MetadataToObject} instance.
	 * 
	 * @return the metadata
	 */
	Metadata to();

}

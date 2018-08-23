package edu.jhuapl.saavtk.metadata;

/**
 * Functional interface whose method uses supplied {@link Metadata} to create
 * (or get) an object instance of a particular type. This allows an object that
 * was previously proxied as {@link Metadata} to be restored, e.g., from a
 * serialized form or from a previous object state.
 * 
 * @param <T> the object type that can be gotten from the Metadata
 * @see {@link ObjectToMetadata}
 */
public interface MetadataToObject<T>
{
	/**
	 * Use the supplied {@link Metadata} to create (or get) an object of the
	 * appropriate instance type. Valid metadata typically must be created by an
	 * object of a matched instance of {@link ObjectToMetadata}.
	 * 
	 * @param metadata the metadata to unpack
	 * @return an instance of the object of the parametrized type T as specified by
	 *         the metadata
	 */
	T from(Metadata metadata);
}

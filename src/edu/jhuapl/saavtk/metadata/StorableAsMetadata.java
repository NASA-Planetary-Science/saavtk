package edu.jhuapl.saavtk.metadata;

/**
 * A {@link RepresentableAsMetadata} that includes a {@link Key} providing a
 * unique identifier of the type of object that may be converted to
 * {@link Metadata}.
 * 
 * The existence of the Key identifier makes implementations suitable for
 * serialization.
 * 
 * @param <T> the object type that can be converted to Metadata
 */
public interface StorableAsMetadata<T> extends RepresentableAsMetadata<T>
{
	/**
	 * Return a key that uniquely identifies the type of object being stored. This
	 * is used in lieu of the object's Class to identify the type for purposes of
	 * saving the {@link Metadata}.
	 * 
	 * @return the key
	 * @see {@link ProvidableFromMetadata}
	 */
	Key<T> getKey();

}

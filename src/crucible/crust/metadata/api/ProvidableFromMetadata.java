package edu.jhuapl.saavtk.metadata;

/**
 * Functional interface whose method uses supplied {@link Metadata} to provide
 * an object instance of a particular type. This allows an object that was
 * previously stored as {@link Metadata} to be recreated in the state in which
 * it was stored, i.e., from a serialized form or from a previous object state.
 * 
 * @param <T> the object type that can be provided from suitable Metadata
 * @see {@link StorableAsMetadata}
 */
public interface ProvidableFromMetadata<T>
{
	/**
	 * Use the supplied {@link Metadata} to create (or get) an object of the
	 * appropriate instance type. Valid metadata typically must be created by an
	 * object of a matched instance of {@link StorableAsMetadata}.
	 * 
	 * @param metadata the metadata to use to provide the instance
	 * @return an instance of the object of the parametrized type T obtained based
	 *         on the metadata
	 */
	T provide(Metadata metadata);
}

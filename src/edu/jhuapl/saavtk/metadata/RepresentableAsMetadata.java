package edu.jhuapl.saavtk.metadata;

/**
 * Abstraction that is capable of representing (storing) instances of the
 * supplied type parameter as {@link Metadata}.
 * 
 * Implementations may provide representations for themselves, or on behalf of
 * objects of another type.
 * 
 * @param <T> the object type that can be represented as Metadata
 */
public interface RepresentableAsMetadata<T>
{
	/**
	 * Return a set of {@link Metadata} that encapsulates the complete state of the
	 * object represented.
	 * 
	 * @return the metadata representing the object of the parameterized type.
	 */
	Metadata store();

}

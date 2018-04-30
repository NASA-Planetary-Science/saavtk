package edu.jhuapl.saavtk.metadata;

/**
 * Abstraction representing a manager of metadata, capable of storing/retrieving
 * a collection of elements to/from a Metadata object. An implementation
 * typically stores/retrieves all relevant metadata associated with the state of
 * a particular Java object or class, or some other resource (e.g., a specific
 * type of file or image, URI, URL, database query).
 */
public interface MetadataManager
{
	/**
	 * Store a (complete and self-consistent) set of metadata in the provided
	 * destination object.
	 * 
	 * @return destination object in which the metadata are stored
	 */
	Metadata store();

	/**
	 * Retrieve a (complete and self-consistent) set of metadata in the provided
	 * source object. The metadata will typically be used to create or to modify the
	 * state of some other type of object.
	 * 
	 * @param source the source of metadata
	 */
	void retrieve(Metadata source);

}

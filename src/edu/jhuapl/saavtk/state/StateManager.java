package edu.jhuapl.saavtk.state;

/**
 * Abstraction representing a manager of metadata, capable of storing/retrieving
 * a collection of elements to/from a Metadata object. An implementation
 * typically stores/retrieves all the information associated with the state of a
 * particular Java object or class, or some other resource (e.g., a specific
 * type of file or image, URI, URL, database query).
 */
public interface StateManager
{
	/**
	 * Store a (complete and self-consistent) set of metadata in the provided
	 * destination object.
	 * 
	 * @param destination object in which the metadata are stored
	 */
	void store(State destination);

	/**
	 * Retrieve a (complete and self-consistent) set of metadata in the provided
	 * source object. The metadata will typically be used to create or to modify the
	 * state of some other type of object.
	 * 
	 * @param source the source of metadata
	 */
	void retrieve(State source);

}

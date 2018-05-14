package edu.jhuapl.saavtk.state;

import java.io.File;
import java.io.IOException;

public interface StateSerializer
{
	<T> StateKey<T> getKey(String keyId);

	/**
	 * Register the provided state manager to manage State objects associated with
	 * the provided key.
	 * 
	 * StateManagers are called in the order in which they were originally added.
	 * Call this method the very first time a method is invoked that uses a State
	 * object to save and/or restore the state of an object. This is not in general
	 * within a constructor.
	 * 
	 * This is so that program execution will more-or-less preserve the natural
	 * order of operations that affect the state of the application as a whole.
	 * 
	 * @param key the key identifying the State objects this manager manages
	 * @param manager the manager for State objects associated with the key
	 * @throws IllegalStateException if method is called more than once with the
	 *             same key
	 */
	void register(StateKey<State> key, StateManager manager);

	void load(File file) throws IOException;

	void save(File file) throws IOException;

}

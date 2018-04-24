package edu.jhuapl.saavtk.state;

import java.io.File;
import java.io.IOException;

public interface StateSerializer
{
	<T> StateKey<T> getKey(String keyId);

	void register(StateKey<State> key, StateManager manager);

	void load(File file) throws IOException;

	void save(File file) throws IOException;

}

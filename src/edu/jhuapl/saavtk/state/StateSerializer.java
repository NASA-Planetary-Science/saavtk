package edu.jhuapl.saavtk.state;

import java.io.IOException;

public interface StateSerializer
{
	State load() throws IOException;

	void save(State state) throws IOException;

}

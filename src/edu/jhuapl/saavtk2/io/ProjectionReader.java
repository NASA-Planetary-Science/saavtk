package edu.jhuapl.saavtk2.io;

import java.io.File;

import edu.jhuapl.saavtk2.image.projection.MapCoordinates;
import edu.jhuapl.saavtk2.image.projection.Projection;

public interface ProjectionReader
{
	Projection read(File file);
}

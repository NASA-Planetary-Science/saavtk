package edu.jhuapl.saavtk2.io;

import java.nio.file.Path;

import edu.jhuapl.saavtk2.geom.Geometry;

public interface GeometryReader {
	public Path getPath();
	public Geometry readAndBuild();
}

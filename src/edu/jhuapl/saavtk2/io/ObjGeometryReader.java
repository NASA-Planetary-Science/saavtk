package edu.jhuapl.saavtk2.io;

import java.nio.file.Path;

import edu.jhuapl.saavtk2.geom.Geometry;
import edu.jhuapl.saavtk2.geom.OBJGeometry;

public class ObjGeometryReader implements GeometrySource {

	Path file;
	
	public ObjGeometryReader(Path file) {
		this.file=file;
	}
	
	@Override
	public Geometry get() {
		return new OBJGeometry(file);
	}

	@Override
	public Path getPath() {
		return file;
	}
}

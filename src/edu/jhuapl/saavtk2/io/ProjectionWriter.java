package edu.jhuapl.saavtk2.io;

import java.io.File;

import edu.jhuapl.saavtk2.image.projection.Projection;

public interface ProjectionWriter
{
	public void write(Projection projection, File file);
}

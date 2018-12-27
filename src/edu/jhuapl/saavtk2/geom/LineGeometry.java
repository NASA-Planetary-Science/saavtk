package edu.jhuapl.saavtk2.geom;

import java.util.Collection;

import edu.jhuapl.saavtk2.geom.euclidean.Line;

public class LineGeometry extends BasicGeometry
{
	public LineGeometry(Line line)
	{
		super(Line.createPolyDataRepresentation(line));
	}

	public LineGeometry(Collection<Line> lines)
	{
		super(Line.createPolyDataRepresentation(lines));
	}
	
}

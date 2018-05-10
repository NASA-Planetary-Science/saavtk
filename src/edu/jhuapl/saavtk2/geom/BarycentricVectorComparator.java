package edu.jhuapl.saavtk2.geom;

import java.util.Comparator;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk2.geom.euclidean.Line;

public class BarycentricVectorComparator implements Comparator<Vector3D> // compares two vectors to see which is closer to the first point of a given line (useful for finding closest intersection between ray and surface)
{

	Line line;

	public BarycentricVectorComparator(Line line)
	{
		this.line=line;
	}

	@Override
	public int compare(Vector3D o1, Vector3D o2)
	{
		Vector3D lhat = line.getUnit();
		Vector3D p0=line.getP1();
		double d1 = o1.subtract(p0).dotProduct(lhat);
		double d2 = o2.subtract(p0).dotProduct(lhat);
		return (int) Math.signum(d1 - d2);
	}

}

package edu.jhuapl.saavtk.illum;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface IlluminationField
{
	public Vector3D getUnobstructedFlux(Vector3D position);	// W/m^2
}

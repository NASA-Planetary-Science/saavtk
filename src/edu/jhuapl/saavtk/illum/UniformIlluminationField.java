package edu.jhuapl.saavtk.illum;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class UniformIlluminationField implements IlluminationField
{
	Vector3D flux;
	
	public UniformIlluminationField(Vector3D flux)
	{
		this.flux=flux;
	}
	
	@Override
	public Vector3D getUnobstructedFlux(Vector3D position)
	{
		return flux;
	}
	
}

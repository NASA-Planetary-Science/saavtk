package edu.jhuapl.saavtk.gui.renderer.axes;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public enum CartesianViewDirection
{
	PLUS_X(Vector3D.PLUS_I, Vector3D.PLUS_K), MINUS_X(Vector3D.MINUS_I, Vector3D.PLUS_K), PLUS_Y(Vector3D.PLUS_J, Vector3D.PLUS_K), MINUS_Y(Vector3D.MINUS_J, Vector3D.PLUS_K), PLUS_Z(Vector3D.PLUS_K, Vector3D.PLUS_J), MINUS_Z(Vector3D.MINUS_K, Vector3D.PLUS_J), NONE(null, null);

	Vector3D lookUnit;
	Vector3D upUnit;

	private CartesianViewDirection(Vector3D lookUnit, Vector3D upUnit)
	{
		this.lookUnit = lookUnit;
		this.upUnit=upUnit;
	}

	public Vector3D getLookUnit()
	{
		return lookUnit;
	}
	
	public Vector3D getUpUnit()
	{
		return upUnit;
	}
}

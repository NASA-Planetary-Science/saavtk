package edu.jhuapl.saavtk2.geom.euclidean;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class SemiSphere extends Sphere {

	double aziMin;
	double aziMax;
	double polMin;
	double polMax;

	public SemiSphere(double radius, double aziMin, double aziMax, double polMin, double polMax) {
		this(Vector3D.ZERO, radius, aziMin, aziMax, polMin, polMax);
	}

	public SemiSphere(Vector3D center, double radius, double aziMin, double aziMax, double polMin, double polMax) {
		super(center, radius);
		this.aziMax = aziMax;
		this.aziMin = aziMin;
		this.polMax = polMax;
		this.polMin = polMin;
	}

	public double getMinPolarAngle() {
		return polMin;
	}

	public double getMaxPolarAngle() {
		return polMax;
	}

	public double getMinAzimuthalAngle() {
		return aziMin;
	}

	public double getMaxAzumuthalAngle() {
		return aziMax;
	}

	@Override
	public Vector3D getPosition(double azi, double pol) {
		if (azi >= aziMin && azi <= aziMax && pol >= polMin && pol <= polMax)
			return super.getPosition(azi, pol);
		else
			return Vector3D.NaN;
	}
}

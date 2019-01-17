package edu.jhuapl.saavtk.gui.render.camera;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Immutable class that is used to define a 3D Cartesian coordinate system.
 * <P>
 * The vectors will be normalized and are guaranteed to not be equal to the Zero
 * vector.
 * <P>
 * TODO: Consider checking to make sure they are orthogonal to each other
 */
public class CoordinateSystem
{
	// Constants
	/**
	 * The standard unit coordinate system where:
	 * <UL>
	 * <LI>The X-Axis: [1, 0, 0]
	 * <LI>The Y-Axis: [0, 1, 0]
	 * <LI>The Z-Axis: [0, 0, 1]
	 * <LI>The origin is at: [0, 0, 0]
	 * </UL>
	 */
	public static final CoordinateSystem Standard = new CoordinateSystem(Vector3D.PLUS_I, Vector3D.PLUS_J,
			Vector3D.PLUS_K, Vector3D.ZERO);

	// Attributes
	private final Vector3D axisX;
	private final Vector3D axisY;
	private final Vector3D axisZ;
	private final Vector3D origin;

	public CoordinateSystem(Vector3D aAxisX, Vector3D aAxisY, Vector3D aAxisZ, Vector3D aOrigin)
	{
		if (aAxisX.equals(Vector3D.ZERO) == true)
			throw new RuntimeException("Invalid X-Axis: " + aAxisX);
		if (aAxisY.equals(Vector3D.ZERO) == true)
			throw new RuntimeException("Invalid Y-Axis: " + aAxisY);
		if (aAxisZ.equals(Vector3D.ZERO) == true)
			throw new RuntimeException("Invalid Z-Axis: " + aAxisZ);

		axisX = aAxisX.normalize();
		axisY = aAxisY.normalize();
		axisZ = aAxisZ.normalize();
		origin = aOrigin;
	}

	/**
	 * Returns the unit X-Axis
	 */
	public Vector3D getAxisX()
	{
		return axisX;
	}

	/**
	 * Returns the unit Y-Axis
	 */
	public Vector3D getAxisY()
	{
		return axisY;
	}

	/**
	 * Returns the unit Z-Axis
	 */
	public Vector3D getAxisZ()
	{
		return axisZ;
	}

	/**
	 * Returns the origin of the CoordinateSystem.
	 */
	public Vector3D getOrigin()
	{
		return origin;
	}

}

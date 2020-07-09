package edu.jhuapl.saavtk.model;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Interface which defines a 3D polygonal based surface. This interface provides
 * an abstraction of 3D based polygonal / polyhedral functionality.
 * <P>
 * A 3 dimensional surface that defines an enclosed volume will be refereed to
 * as a polyhedral while models that do not have an enclosed volume will be
 * referred to as polygonal.
 * <P>
 * This interface provides for the following:
 * <UL>
 * <LI>Retrieval of various geometric attributes.
 * <LI>Various geometric computations
 * <LI>Querying if this is a polyhedral or polygonal based surface.
 * </UL>
 *
 * @author lopeznr1
 */
public interface PolyModel
{
	/**
	 * Calculates and returns the surface intercept between two end points.
	 * <P>
	 * Returns null if there is no intersection between the {@link PolyModel} and
	 * the two end points.
	 */
	public Vector3D calcInterceptBetween(Vector3D aBegPos, Vector3D aEndPos);

	/**
	 * Returns the closest point that lies on the surface of the {@link PolyModel}.
	 */
	public Vector3D findClosestPoint(Vector3D aPoint);

	/**
	 * Method that returns the average surface normal over the the entire
	 * {@link PolyModel}.
	 * <P>
	 * Objects that are polyhedral models should return the {@link Vector3D#ZERO}
	 * vector (no normal) where as objects that are (open ended) polygonal models
	 * should return their average surface normal (normalized).
	 *
	 * @return Returns a normalized vector describing the average surface normal.
	 */
	public Vector3D getAverageSurfaceNormal();

	/**
	 * Returns the length of the diagonal corresponding to a bounding box that would
	 * enclose this {@link PolyModel}.
	 */
	public double getBoundingBoxDiagonalLength();

	/**
	 * Method that returns the geometric center of the polyhedral model.
	 * <P>
	 * The geometric center of the polyhedral model will typically lie at the origin
	 * but may differ if the model is offset or is a polygonal model instead.
	 */
	public Vector3D getGeometricCenterPoint();

	/**
	 * Method that return true if this is a (true) polyhedron (rather than just a
	 * polygonal surface).
	 */
	public boolean isPolyhedron();

}

package edu.jhuapl.saavtk.structure.vtk;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.util.MathUtil;

/**
 * Record that is a capture of the rendering state of an object. The following is supported:
 * <ul>
 * <li>centerPt: The center of the structure.
 * <li>pathLength: The cumulative length of the line as projected on the surface. Note this is not the distance between
 * all of the control points but rather the distance of the line as projected on the relevant surface.
 * <li>surfaceArea: The area of enclosed structure.
 * <li>controlPointL: The list of control points associated with the structure.
 * </ul>
 *
 * @author lopeznr1
 */
public record RenderState(Vector3D centerPt, double pathLength, double surfaceArea,
		ImmutableList<Vector3D> controlPointL)
{
	/** Invalid constant */
	public static RenderState Invalid = new RenderState(Vector3D.NaN, Double.NaN, Double.NaN, ImmutableList.of());

	/** Alternative Constructor */
	public RenderState(Vector3D aCenterPt, double aPathLength, double aSurfaceArea, Collection<Vector3D> aControlPointL)
	{
		this(aCenterPt, aPathLength, aSurfaceArea, ImmutableList.copyOf(aControlPointL));
	}

	/** Simplified Constructor */
	public RenderState(Vector3D aCenterPt, double aPathLength, double aSurfaceArea)
	{
		this(aCenterPt, aPathLength, aSurfaceArea, ImmutableList.of());
	}

	/**
	 * Returns a copy of this {@link RenderState} with the alternative center.
	 */
	public RenderState withCenterPt(Vector3D aCenterPt)
	{
		return new RenderState(aCenterPt, pathLength, surfaceArea, controlPointL);
	}

	/**
	 * Returns a copy of this {@link RenderState} with the alternative surfaceArea.
	 */
	public RenderState withSurfaceArea(double aSurfaceArea)
	{
		return new RenderState(centerPt, pathLength, aSurfaceArea, controlPointL);
	}

	/**
	 * Utility method that creates a render state from the specified vars
	 */
	public static RenderState fromSegments(Collection<Segment> aSegmentC, double aSurfaceArea)
	{
		var tmpX = 0.0;
		var tmpY = 0.0;
		var tmpZ = 0.0;
		var pathLength = 0.0;
		var controlPointS = new LinkedHashSet<Vector3D>();
		for (var aSegment : aSegmentC)
		{
			var begPt = aSegment.getPoints3D().get(0);
			var endPt = aSegment.getPoints3D().get(aSegment.getPoints3D().size() - 1);
			tmpX += begPt.getX() + endPt.getX();
			tmpY += begPt.getY() + endPt.getY();
			tmpZ += begPt.getZ() + endPt.getZ();

			pathLength += aSegment.getPathLength();

			var begLL = aSegment.getPointBegLL();
			var begXYZ = new Vector3D(MathUtil.latrec(begLL));
			var endLL = aSegment.getPointEndLL();
			var endXYZ = new Vector3D(MathUtil.latrec(endLL));

			// Add the (unique) end points of each Segment (as a control point)
			controlPointS.add(begXYZ);
			controlPointS.add(endXYZ);
		}
		var numPts = aSegmentC.size() * 2;
		var centerPt = new Vector3D(tmpX / numPts, tmpY / numPts, tmpZ / numPts);

		return new RenderState(centerPt, pathLength, aSurfaceArea, controlPointS);
	}

	/**
	 * Utility method that creates a render state from the specified vars
	 */
	public static RenderState fromSegments(Collection<Segment> aSegmentC)
	{
		return fromSegments(aSegmentC, Double.NaN);
	}

}

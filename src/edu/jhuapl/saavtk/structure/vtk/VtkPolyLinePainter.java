package edu.jhuapl.saavtk.structure.vtk;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.PolyLineMode;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.vtk.VtkResource;
import vtk.vtkPoints;
import vtk.vtkPolyData;

/**
 * Class which contains the logic to render a single {@link PolyLine} using the
 * VTK framework.
 * <P>
 * This class supports the following configurable state:
 * <UL>
 * <LI>Closed / Unclosed
 * <LI>Draw color
 * <LI>TODO: Outline width
 * </UL>
 * Note this class will update the reference {@link PolyLine}'s "path length"
 * during relevant VTK state updates.
 *
 * @author lopeznr1
 */
public class VtkPolyLinePainter<G1 extends PolyLine> implements VtkResource
{
	// Reference vars
	private final PolyhedralModel refSmallBody;
	private final PolyLine refItem;
	private final PolyLineMode refMode;

	// Note xyzPointList is what's displayed. There will usually be more of these
	// points than controlPointL in order to ensure the line is right above the
	// surface of the asteroid.
	protected ImmutableList<Vector3D> xyzPointL = ImmutableList.of();
	protected List<Integer> controlPointIdL = new ArrayList<>();

	// VTK vars
	private int vDrawId;

	/**
	 * Standard Constructor
	 */
	public VtkPolyLinePainter(PolyhedralModel aSmallBody, PolyLine aItem, PolyLineMode aMode)
	{
		refSmallBody = aSmallBody;
		refItem = aItem;
		refMode = aMode;

		vDrawId = -1;
	}

	public List<Integer> getControlPointIdList()
	{
		return controlPointIdL;
	}

	public ImmutableList<Vector3D> getXyzPointList()
	{
		return xyzPointL;
	}

	// TODO: Is this needed??? Seems a bit spurious.
	public int getNumControlPointIds()
	{
		return controlPointIdL.size();
	}

	// TODO: Is this needed??? Seems a bit spurious.
	public int getVtkDrawId()
	{
		return vDrawId;
	}

	public void setVtkDrawId(int aId)
	{
		vDrawId = aId;
	}

	public void addControlPoint(int aIdx, Vector3D aPoint)
	{
		List<Vector3D> tmpPointL = new ArrayList<>(xyzPointL);

		int evalIdx = aIdx - 1;

		// Remove points BETWEEN the 2 control points (If adding a point in the middle)
		if (evalIdx < controlPointIdL.size() - 1)
		{
			int id1 = controlPointIdL.get(evalIdx);
			int id2 = controlPointIdL.get(evalIdx + 1);
			int numberPointsRemoved = id2 - id1 - 1;
			for (int i = 0; i < id2 - id1 - 1; ++i)
			{
				tmpPointL.remove(id1 + 1);
			}

			tmpPointL.add(id1 + 1, aPoint);
			controlPointIdL.add(evalIdx + 1, id1 + 1);

			// Shift the control points ids from currentLineVertex+2 till the end by the
			// right amount.
			for (int i = evalIdx + 2; i < controlPointIdL.size(); ++i)
			{
				controlPointIdL.set(i, controlPointIdL.get(i) - (numberPointsRemoved - 1));
			}
		}
		else
		{
			tmpPointL.add(aPoint);
			controlPointIdL.add(tmpPointL.size() - 1);
		}

		if (controlPointIdL.size() >= 2)
		{
			if (evalIdx < 0)
			{
				// Do nothing
			}
			else if (evalIdx < controlPointIdL.size() - 2)
			{
				updateSegment(evalIdx, tmpPointL);
				updateSegment(evalIdx + 1, tmpPointL);
			}
			else
			{
				updateSegment(evalIdx, tmpPointL);
				if (refMode == PolyLineMode.CLOSED)
					updateSegment(evalIdx + 1, tmpPointL);
			}
		}

		xyzPointL = ImmutableList.copyOf(tmpPointL);
		updatePathLength();
	}

	public void delControlPoint(int aIdx)
	{
		List<Vector3D> tmpPointL = new ArrayList<>(xyzPointL);

		// If not in CLOSED mode:
		// If one of the end points is being removed, then we only need to remove the
		// line connecting the end point to the adjacent point. If we're removing a
		// non-end point, we need to remove the line segments connecting the 2 adjacent
		// control points and in addition, we need to draw a new line connecting the 2
		// adjacent control points.
		//
		// But if in CLOSED mode:
		// We always need to remove 2 adjacent segments to the control point that was
		// removed and draw a new line connecting the 2 adjacent control point.
		if (controlPointIdL.size() > 1)
		{
			// Remove initial point
			if (aIdx == 0)
			{
				int id2 = controlPointIdL.get(aIdx + 1);
				int numberPointsRemoved = id2;
				for (int i = 0; i < numberPointsRemoved; ++i)
				{
					tmpPointL.remove(0);
				}
				controlPointIdL.remove(aIdx);

				for (int i = 0; i < controlPointIdL.size(); ++i)
					controlPointIdL.set(i, controlPointIdL.get(i) - numberPointsRemoved);

				if (refMode == PolyLineMode.CLOSED)
				{
					int id = controlPointIdL.get(controlPointIdL.size() - 1);
					numberPointsRemoved = tmpPointL.size() - id - 1;
					;
					for (int i = 0; i < numberPointsRemoved; ++i)
					{
						tmpPointL.remove(id + 1);
					}

					// redraw segment connecting last point to first
					updateSegment(controlPointIdL.size() - 1, tmpPointL);
				}
			}
			// Remove final point
			else if (aIdx == controlPointIdL.size() - 1)
			{
				if (refMode == PolyLineMode.CLOSED)
				{
					int id = controlPointIdL.get(controlPointIdL.size() - 1);
					int numberPointsRemoved = tmpPointL.size() - id - 1;
					;
					for (int i = 0; i < numberPointsRemoved; ++i)
					{
						tmpPointL.remove(id + 1);
					}
				}

				int id1 = controlPointIdL.get(aIdx - 1);
				int id2 = controlPointIdL.get(aIdx);
				int numberPointsRemoved = id2 - id1;
				for (int i = 0; i < numberPointsRemoved; ++i)
				{
					tmpPointL.remove(id1 + 1);
				}
				controlPointIdL.remove(aIdx);

				if (refMode == PolyLineMode.CLOSED)
				{
					// redraw segment connecting last point to first
					updateSegment(controlPointIdL.size() - 1, tmpPointL);
				}
			}
			// Remove a middle point
			else
			{
				// Remove points BETWEEN the 2 adjacent control points
				int id1 = controlPointIdL.get(aIdx - 1);
				int id2 = controlPointIdL.get(aIdx + 1);
				int numberPointsRemoved = id2 - id1 - 1;
				for (int i = 0; i < numberPointsRemoved; ++i)
				{
					tmpPointL.remove(id1 + 1);
				}
				controlPointIdL.remove(aIdx);

				for (int i = aIdx; i < controlPointIdL.size(); ++i)
					controlPointIdL.set(i, controlPointIdL.get(i) - numberPointsRemoved);

				updateSegment(aIdx - 1, tmpPointL);
			}
		}
		else if (controlPointIdL.size() == 1)
		{
			controlPointIdL.remove(aIdx);
			tmpPointL.clear();
		}

		xyzPointL = ImmutableList.copyOf(tmpPointL);
		updatePathLength();
	}

	public void updateControlPoint(int aIdx, Vector3D aPoint)
	{
		int numVertices = refItem.getControlPoints().size();

		// If there are not enough points
		List<Vector3D> tmpPointL = new ArrayList<>(xyzPointL);
		if (numVertices == 1)
		{
			// Update the corresponding 3D point
			int id0 = controlPointIdL.get(0);
			tmpPointL.set(id0, aPoint);
		}
		// If we're modifying the last vertex
		else if (aIdx == numVertices - 1)
		{
			updateSegment(aIdx - 1, tmpPointL);
			if (refMode == PolyLineMode.CLOSED)
				updateSegment(aIdx, tmpPointL);
		}
		// If we're modifying the first vertex
		else if (aIdx == 0)
		{
			if (refMode == PolyLineMode.CLOSED)
				updateSegment(numVertices - 1, tmpPointL);
			updateSegment(aIdx, tmpPointL);
		}
		// If we're modifying a middle vertex
		else
		{
			updateSegment(aIdx - 1, tmpPointL);
			updateSegment(aIdx, tmpPointL);
		}

		xyzPointL = ImmutableList.copyOf(tmpPointL);
		updatePathLength();
	}

	public void updateAllSegments()
	{
		controlPointIdL.clear();
		List<Vector3D> tmpPointL = new ArrayList<>();

		int numSegments = refItem.getControlPoints().size();
		for (int aIdx = 0; aIdx < numSegments; ++aIdx)
		{
			controlPointIdL.add(tmpPointL.size());

			// Note, this point will be replaced with the correct values
			// when we call updateSegment
			tmpPointL.add(Vector3D.ZERO);

			if (aIdx > 0)
				updateSegment(aIdx - 1, tmpPointL);
		}

		if (refItem.isClosed() == true)
			updateSegment(controlPointIdL.size() - 1, tmpPointL);

		xyzPointL = ImmutableList.copyOf(tmpPointL);
		updatePathLength();
	}

	@Override
	public void vtkDispose()
	{
		; // Nothing to do
	}

	@Override
	public void vtkUpdateState()
	{
		if (refItem.getControlPoints().size() == controlPointIdL.size())
			return;

		updateAllSegments();
	}

	/**
	 * Helper method that will update the reference item's path length.
	 */
	private void updatePathLength()
	{
		double length = 0.0;

		int size = xyzPointL.size();
		for (int i = 1; i < size; ++i)
		{
			double dist = xyzPointL.get(i - 1).distance(xyzPointL.get(i));
			length += dist;
		}

		if (refItem.isClosed() && size > 1)
		{
			double dist = xyzPointL.get(size - 1).distance(xyzPointL.get(0));
			length += dist;
		}

		refItem.setPathLength(length);
	}

	/**
	 * Helper method to update the individual segment at the specified index.
	 * <P>
	 * The specified index provides one end point and the next incremental index
	 * will be utilized as the other end point.
	 */
	private void updateSegment(int aBegIdx, List<Vector3D> aPointL)
	{
		int nextIdx = aBegIdx + 1;
		if (nextIdx == refItem.getControlPoints().size())
			nextIdx = 0;

		LatLon ll1 = refItem.getControlPoints().get(aBegIdx);
		LatLon ll2 = refItem.getControlPoints().get(nextIdx);
		double pt1[] = MathUtil.latrec(ll1);
		double pt2[] = MathUtil.latrec(ll2);

		int id1 = controlPointIdL.get(aBegIdx);
		int id2 = controlPointIdL.get(nextIdx);

		// Set the 2 control points
		aPointL.set(id1, new Vector3D(pt1));
		aPointL.set(id2, new Vector3D(pt2));

		vtkPoints points = null;
		if (Math.abs(ll1.lat - ll2.lat) < 1e-8 && Math.abs(ll1.lon - ll2.lon) < 1e-8
				&& Math.abs(ll1.rad - ll2.rad) < 1e-8)
		{
			points = new vtkPoints();
			points.InsertNextPoint(pt1);
			points.InsertNextPoint(pt2);
		}
		else
		{
			vtkPolyData poly = refSmallBody.drawPath(pt1, pt2);
			if (poly == null)
				return;

			points = poly.GetPoints();
		}

		// Remove points BETWEEN the 2 control points
		int numberPointsToRemove = id2 - id1 - 1;
		if (nextIdx == 0)
			numberPointsToRemove = aPointL.size() - id1 - 1;
		for (int i = 0; i < numberPointsToRemove; ++i)
		{
			aPointL.remove(id1 + 1);
		}

		// Set the new points
		int numNewPoints = points.GetNumberOfPoints();
		for (int i = 1; i < numNewPoints - 1; ++i)
		{
			aPointL.add(id1 + i, new Vector3D(points.GetPoint(i)));
		}

		// Shift the control points ids from segment+1 till the end by the right amount.
		int shiftAmount = id1 + numNewPoints - 1 - id2;
		for (int i = aBegIdx + 1; i < controlPointIdL.size(); ++i)
		{
			controlPointIdL.set(i, controlPointIdL.get(i) + shiftAmount);
		}

	}

}

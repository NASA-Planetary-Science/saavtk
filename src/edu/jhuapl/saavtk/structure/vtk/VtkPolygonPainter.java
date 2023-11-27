package edu.jhuapl.saavtk.structure.vtk;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.vtk.VtkDrawUtil;
import vtk.vtkPolyData;
import vtk.vtkQuadricClustering;

/**
 * Class which contains the logic to render a single {@link Polygon} using the VTK framework.
 * <p>
 * This class supports the following:
 * <ul>
 * <li>Update / Refresh mechanism
 * <li>VTK state management
 * <li>Exterior rendering
 * <li>Interior rendering
 * </ul>
 * Note this class will update the reference structure's {@link RenderState} during a VTK state update.
 *
 * @author lopeznr1
 */
public class VtkPolygonPainter implements VtkStructurePainter
{
	// Ref vars
	private final PolyhedralModel refSmallBody;
	private final Polygon refItem;

	// VTK vars
	private final vtkPolyData vExteriorDecPD;
	private final vtkPolyData vExteriorRegPD;
	private final vtkPolyData vInteriorRegPD;
	private final vtkPolyData vInteriorDecPD;
	private final vtkPolyData vEmptyPD;
	private boolean vIsStale;

	// Cache vars
	private ImmutableMap<Segment, Segment> cSegmentM;
	private ImmutableList<Vector3D> cPointL;

	/** Standard Constructor */
	public VtkPolygonPainter(PolyhedralModel aSmallBody, Polygon aItem)
	{
		refSmallBody = aSmallBody;
		refItem = aItem;

		vExteriorDecPD = new vtkPolyData();
		vExteriorRegPD = new vtkPolyData();
		vInteriorRegPD = new vtkPolyData();
		vInteriorDecPD = new vtkPolyData();
		vEmptyPD = new vtkPolyData();
		vIsStale = true;

		cSegmentM = ImmutableMap.of();
		cPointL = ImmutableList.of();
	}

	/**
	 * Return the list of 3D points that are a function of the reference {@link PolyLine}'s control points.
	 */
	public ImmutableList<Vector3D> getXyzPointList()
	{
		return cPointL;
	}

	@Override
	public void vtkDispose()
	{
		PolyDataUtil.clearPolyData(vExteriorDecPD);
		PolyDataUtil.clearPolyData(vExteriorRegPD);
		PolyDataUtil.clearPolyData(vInteriorDecPD);
		PolyDataUtil.clearPolyData(vInteriorRegPD);

		// Release VTK resources
		vExteriorDecPD.Delete();
		vExteriorRegPD.Delete();
		vInteriorDecPD.Delete();
		vInteriorRegPD.Delete();
		vEmptyPD.Delete();
	}

	@Override
	public vtkPolyData vtkGetExteriorDecPD()
	{
		return vExteriorDecPD;
	}

	@Override
	public vtkPolyData vtkGetExteriorRegPD()
	{
		return vExteriorRegPD;
	}

	@Override
	public vtkPolyData vtkGetInteriorDecPD()
	{
		if (refItem.getShowInterior() == false)
			return vEmptyPD;

		return vInteriorDecPD;
	}

	@Override
	public vtkPolyData vtkGetInteriorRegPD()
	{
		if (refItem.getShowInterior() == false)
			return vEmptyPD;

		return vInteriorRegPD;
	}

	@Override
	public void vtkMarkStale()
	{
		vIsStale = true;
	}

	@Override
	public void vtkUpdateState()
	{
		// Bail if not stale
		if (vIsStale == false)
			return;
		vIsStale = false;

		// Refresh the Segment cache
		var oldSegmentM = ImmutableMap.copyOf(cSegmentM);

		var fullPointL = new ArrayList<Vector3D>();
		var controlPtL = refItem.getControlPoints();
		var prevLL = controlPtL.get(0);
		var tmpSegmentM = new LinkedHashMap<Segment, Segment>();
		var lastSegment = (Segment) null;

		// Iterate through each segment:
		// - Ensure the segment is up to date
		// - Extract all of the 3D points from each segment
		for (var aIdx = 1; aIdx <= controlPtL.size(); aIdx++)
		{
			// Wrap to the 1st ControlPoint when we reach the end
			var tmpIdx = aIdx;
			if (tmpIdx == controlPtL.size())
				tmpIdx = 0;
			var nextLL = controlPtL.get(tmpIdx);

			// Retrieve (or create) the segment, update it, and then cache it
			var tmpSegment = new Segment(prevLL, nextLL);
			var currSegment = oldSegmentM.get(tmpSegment);
			if (currSegment == null)
				currSegment = tmpSegment;

			currSegment.update(refSmallBody);
			tmpSegmentM.put(currSegment, currSegment);

			// Grab the first n-1 points from each segment
			var tmpPointL = currSegment.getPoints3D();
			fullPointL.addAll(tmpPointL.subList(0, tmpPointL.size() - 1));

			prevLL = nextLL;
			lastSegment = currSegment;
		}
		cSegmentM = ImmutableMap.copyOf(tmpSegmentM);

		// Note we need to add the very last connecting point (from the last segment)
		var lastPointIdx = lastSegment.getPoints3D().size() - 1;
		fullPointL.add(lastSegment.getPoints3D().get(lastPointIdx));

		// Draw the (high quality) polygon
		var vTmpInteriorRegPD = vInteriorRegPD;
		if (refItem.getShowInterior() == false)
			vTmpInteriorRegPD = null;

		VtkDrawUtil.drawPolygonOn(refSmallBody.getSmallBodyPolyData(), refSmallBody.getPointLocator(), fullPointL,
				vTmpInteriorRegPD, vExteriorRegPD);

		// Update the Polygon's RenderState
		var surfaceArea = Double.NaN;
		if (refItem.getShowInterior() == true)
			surfaceArea = PolyDataUtil.computeSurfaceArea(vInteriorRegPD);

		var renderState = RenderState.fromSegments(cSegmentM.values(), surfaceArea);
		refItem.setRenderState(renderState);

		// Setup decimator
		var decimator = new vtkQuadricClustering();

		// Decimate interior
		decimator.SetInputData(vInteriorRegPD);
		decimator.AutoAdjustNumberOfDivisionsOn();
		decimator.CopyCellDataOn();
		decimator.Update();
		vInteriorDecPD.DeepCopy(decimator.GetOutput());

		// Decimate exterior
		decimator.SetInputData(vExteriorRegPD);
		decimator.SetNumberOfXDivisions(2);
		decimator.SetNumberOfYDivisions(2);
		decimator.SetNumberOfZDivisions(2);
		decimator.CopyCellDataOn();
		decimator.Update();
		vExteriorDecPD.DeepCopy(decimator.GetOutput());

		// Destroy decimator
		decimator.Delete();

		// Update the list of Vtk3D points
		cPointL = ImmutableList.copyOf(fullPointL);
	}

}

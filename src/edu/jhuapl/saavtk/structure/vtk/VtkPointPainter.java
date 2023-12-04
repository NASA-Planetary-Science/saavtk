package edu.jhuapl.saavtk.structure.vtk;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.Point;
import edu.jhuapl.saavtk.structure.RenderAttr;
import edu.jhuapl.saavtk.structure.util.EllipseUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.vtk.VtkDrawUtil;
import vtk.vtkPolyData;
import vtk.vtkQuadricClustering;

/**
 * Class which contains the logic to render a single {@link Point} using the VTK framework.
 * <p>
 * This class supports the following:
 * <ul>
 * <li>Update / Refresh mechanism
 * <li>VTK state management
 * <li>Exterior rendering
 * <li>Interior rendering
 * </ul>
 *
 * @author lopeznr1
 */
public class VtkPointPainter implements VtkStructurePainter
{
	// Reference vars
	private final PolyhedralModel refSmallBody;
	private final Point refItem;

	// State vars
	private RenderAttr renderAttr;

	// VTK vars
	private final vtkPolyData vExteriorDecPD;
	private final vtkPolyData vExteriorRegPD;
	private final vtkPolyData vInteriorDecPD;
	private final vtkPolyData vInteriorRegPD;
	private boolean vIsStale;

	/** Standard Constructor */
	public VtkPointPainter(PolyhedralModel aSmallBody, Point aItem, RenderAttr aRenderAttr)
	{
		refSmallBody = aSmallBody;
		refItem = aItem;

		renderAttr = aRenderAttr;

		vExteriorDecPD = new vtkPolyData();
		vExteriorRegPD = new vtkPolyData();
		vInteriorDecPD = new vtkPolyData();
		vInteriorRegPD = new vtkPolyData();
		vIsStale = true;
	}

	/**
	 * Sets in the painter's {@link RenderAttr}.
	 */
	public void setRenderAttr(RenderAttr aRenderAttr)
	{
		var isChanged = false;
		isChanged |= renderAttr.numPointSides() != aRenderAttr.numPointSides();
		isChanged |= Double.compare(renderAttr.pointRadius(), aRenderAttr.pointRadius()) != 0;
		if (isChanged == true)
			vtkMarkStale();

		renderAttr = aRenderAttr;
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
		return vInteriorDecPD;
	}

	@Override
	public vtkPolyData vtkGetInteriorRegPD()
	{
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

		// Draw the (high quality) ellipse
		var vTmpPD = refSmallBody.getSmallBodyPolyData();
		var vTmpPL = refSmallBody.getPointLocator();
		var center = refItem.getCenter();
		var pointSizeMin = EllipseUtil.getPointSizeDef(refSmallBody) / 100.0;
		var pointSize = renderAttr.pointRadius();
		if (pointSize < pointSizeMin)
			pointSize = pointSizeMin;
		var numSides = renderAttr.numPointSides();
		VtkDrawUtil.drawEllipseOn(vTmpPD, vTmpPL, center, pointSize, 1.0, 0.0, numSides, vInteriorRegPD, vExteriorRegPD);

		// Update the Point's RenderState
		var pathLength = Double.NaN;
		var surfaceArea = Double.NaN;
		var renderState = new RenderState(refItem.getCenter(), pathLength, surfaceArea);
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
	}

}

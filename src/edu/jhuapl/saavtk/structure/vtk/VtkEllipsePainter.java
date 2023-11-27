package edu.jhuapl.saavtk.structure.vtk;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.RenderAttr;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.vtk.VtkDrawUtil;
import vtk.vtkPolyData;
import vtk.vtkQuadricClustering;

/**
 * Class which contains the logic to render a single {@link Ellipse} using the VTK framework.
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
public class VtkEllipsePainter implements VtkStructurePainter
{
	// Reference vars
	private final PolyhedralModel refSmallBody;
	private final Ellipse refItem;

	// State vars
	private RenderAttr renderAttr;

	// VTK vars
	private final vtkPolyData vExteriorDecPD;
	private final vtkPolyData vExteriorRegPD;
	private final vtkPolyData vInteriorDecPD;
	private final vtkPolyData vInteriorRegPD;
	private final vtkPolyData vEmptyPD;
	private boolean vIsStale;

	/** Standard Constructor */
	public VtkEllipsePainter(PolyhedralModel aSmallBody, Ellipse aItem, RenderAttr aRenderAttr)
	{
		refSmallBody = aSmallBody;
		refItem = aItem;

		renderAttr = aRenderAttr;

		vExteriorDecPD = new vtkPolyData();
		vExteriorRegPD = new vtkPolyData();
		vInteriorDecPD = new vtkPolyData();
		vInteriorRegPD = new vtkPolyData();
		vEmptyPD = new vtkPolyData();
		vIsStale = true;
	}

	/**
	 * Sets in the painter's {@link RenderAttr}.
	 */
	public void setRenderAttr(RenderAttr aRenderAttr)
	{
		var isChanged = false;
		isChanged |= renderAttr.numRoundSides() != aRenderAttr.numRoundSides();
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

		// Draw the (high quality) ellipse
		var vTmpInteriorRegPD = vInteriorRegPD;
		if (refItem.getShowInterior() == false)
			vTmpInteriorRegPD = null;

		var vTmpPD = refSmallBody.getSmallBodyPolyData();
		var vTmpPL = refSmallBody.getPointLocator();
		var center = refItem.getCenter();
		var radius = refItem.getRadius();
		var flattening = refItem.getFlattening();
		var angle = refItem.getAngle();
		var numSides = renderAttr.numRoundSides();
		VtkDrawUtil.drawEllipseOn(vTmpPD, vTmpPL, center, radius, flattening, angle, numSides, vTmpInteriorRegPD,
				vExteriorRegPD);

		// Update the Ellipse's RenderState
		var pathLength = Double.NaN;

		var surfaceArea = Double.NaN;
		if (refItem.getShowInterior() == true)
			surfaceArea = PolyDataUtil.computeSurfaceArea(vInteriorRegPD);

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

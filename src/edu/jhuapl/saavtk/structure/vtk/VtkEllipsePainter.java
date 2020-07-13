package edu.jhuapl.saavtk.structure.vtk;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.vtk.VtkDrawUtil;
import edu.jhuapl.saavtk.vtk.VtkResource;
import vtk.vtkPointLocator;
import vtk.vtkPolyData;
import vtk.vtkQuadricClustering;

/**
 * Class which contains the logic to render a single {@link Ellipse} using the
 * VTK framework.
 * <P>
 * This class supports the following:
 * <UL>
 * <LI>Update / Refresh mechanism
 * <LI>VTK state management
 * <LI>Exterior rendering
 * <LI>Interior rendering
 * </UL>
 *
 * @author lopeznr1
 */
public class VtkEllipsePainter implements VtkResource
{
	// Reference vars
	private final PolyhedralModel refSmallBody;
	private final Ellipse refItem;

	// Attributes
	private final int numSides;

	// VTK vars
	private final vtkPolyData vExteriorDecPD;
	private final vtkPolyData vExteriorRegPD;
	private final vtkPolyData vInteriorDecPD;
	private final vtkPolyData vInteriorRegPD;
	private boolean vIsStale;

	/**
	 * Standard Constructor
	 */
	public VtkEllipsePainter(PolyhedralModel aSmallBody, Ellipse aItem, int aNumSides)
	{
		refSmallBody = aSmallBody;
		refItem = aItem;

		numSides = aNumSides;

		vExteriorDecPD = new vtkPolyData();
		vExteriorRegPD = new vtkPolyData();
		vInteriorDecPD = new vtkPolyData();
		vInteriorRegPD = new vtkPolyData();
		vIsStale = true;
	}

	/**
	 * Returns the exterior (decimated) vtkPolyData.
	 */
	public vtkPolyData getVtkExteriorDecPolyData()
	{
		return vExteriorDecPD;
	}

	/**
	 * Returns the exterior (regular) vtkPolyData.
	 */
	public vtkPolyData getVtkExteriorPolyData()
	{
		return vExteriorRegPD;
	}

	/**
	 * Returns the interior (decimated) vtkPolyData.
	 */
	public vtkPolyData getVtkInteriorDecPolyData()
	{
		return vInteriorDecPD;
	}

	/**
	 * Returns the interior (regular) vtkPolyData.
	 */
	public vtkPolyData getVtkInteriorPolyData()
	{
		return vInteriorRegPD;
	}

	/**
	 * Notification that the VTK state is stale.
	 */
	public void markStale()
	{
		vIsStale = true;
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
	public void vtkUpdateState()
	{
		// Bail if not stale
		if (vIsStale == false)
			return;
		vIsStale = false;

		// Draw the (high quality) ellipse
		vtkPolyData vTmpPD = refSmallBody.getSmallBodyPolyData();
		vtkPointLocator vTmpPL = refSmallBody.getPointLocator();
		Vector3D center = refItem.getCenter();
		double radius = refItem.getRadius();
		double flattening = refItem.getFlattening();
		double angle = refItem.getAngle();
		VtkDrawUtil.drawEllipseOn(vTmpPD, vTmpPL, center, radius, flattening, angle, numSides, vInteriorRegPD,
				vExteriorRegPD);

		// Setup decimator
		vtkQuadricClustering decimator = new vtkQuadricClustering();

		// Decimate interior
		decimator.SetInputData(vInteriorRegPD);
		decimator.AutoAdjustNumberOfDivisionsOn();
		decimator.CopyCellDataOn();
		decimator.Update();
		vInteriorDecPD.DeepCopy(decimator.GetOutput());

		// Decimate boundary
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

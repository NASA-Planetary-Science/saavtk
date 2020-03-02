package edu.jhuapl.saavtk.structure.vtk;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.PolyLineMode;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import vtk.vtkCleanPolyData;
import vtk.vtkClipPolyData;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkQuadricClustering;
import vtk.vtkSelectPolyData;

/**
 * Class which contains the logic to render a single polygon using the VTK
 * framework.
 * <P>
 * This class supports the following configurable state:
 * <UL>
 * <LI>Filled / unfilled interior
 * <LI>TODO: Fill color
 * <LI>TODO: Outline width
 * </UL>
 * Note this class will update the reference {@link Polygon}'s "surface area"
 * during relevant VTK state updates.
 *
 * @author lopeznr1
 */
public class VtkPolygonPainter extends VtkPolyLinePainter<Polygon>
{
	// Ref vars
	private final Polygon refItem;
	private final PolyhedralModel refSmallBody;

	// VTK vars
	private vtkPolyData vInteriorRegPD;
	private final vtkPolyData vInteriorDecPD;

	/**
	 * Standard Constructor
	 */
	public VtkPolygonPainter(Polygon aItem, PolyhedralModel aSmallBody)
	{
		super(aSmallBody, aItem, PolyLineMode.CLOSED);

		refItem = aItem;
		refSmallBody = aSmallBody;

		vInteriorRegPD = new vtkPolyData();
		vInteriorDecPD = new vtkPolyData();
	}

	// TODO: Add comments
	public vtkPolyData getVtkInteriorDecPD()
	{
		return vInteriorDecPD;
	}

	// TODO: Add comments
	public vtkPolyData getVtkInteriorRegPD()
	{
		return vInteriorRegPD;
	}

	public void setShowInterior(boolean aShowInterior)
	{
		updateInteriorPolydata();

		if (aShowInterior == true)
		{
			// Decimate interiorPolyData for LODs
			vtkQuadricClustering decimator = new vtkQuadricClustering();
			decimator.SetInputData(vInteriorRegPD);
			decimator.AutoAdjustNumberOfDivisionsOn();
			decimator.CopyCellDataOn();
			decimator.Update();
			vInteriorDecPD.DeepCopy(decimator.GetOutput());
			decimator.Delete();
		}
		else
		{
			PolyDataUtil.clearPolyData(vInteriorRegPD);
			PolyDataUtil.clearPolyData(vInteriorDecPD);
		}
	}

	public void updateInteriorPolydata()
	{
		// Bail if no interior
		if (refItem.getShowInterior() == false)
			return;

		vtkPoints pts = new vtkPoints();
		for (Vector3D aPoint : xyzPointL)
			pts.InsertNextPoint(aPoint.toArray());

		// Clean the poly data here before selecting the interior facets.
		vtkCleanPolyData cleanPoly = new vtkCleanPolyData();
		cleanPoly.SetInputData(refSmallBody.getSmallBodyPolyData());
		cleanPoly.Update();
		vtkPolyData cleanPolyData = cleanPoly.GetOutput();

		vtkSelectPolyData loop = new vtkSelectPolyData();
		loop.SetInputData(cleanPolyData);
		loop.SetLoop(pts);
		loop.GenerateSelectionScalarsOn();
		loop.SetSelectionModeToSmallestRegion();
		loop.Update();
		vtkClipPolyData clipper = new vtkClipPolyData();
		clipper.SetInputData(loop.GetOutput());
		clipper.InsideOutOn();
		clipper.GenerateClipScalarsOff();
		clipper.Update();
		vInteriorRegPD = clipper.GetOutput();

		double tmpSurfaceArea = PolyDataUtil.computeSurfaceArea(vInteriorRegPD);
		refItem.setSurfaceArea(tmpSurfaceArea);
	}

}

package edu.jhuapl.saavtk.structure.vtk;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.render.VtkPropProvider;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.vtk.VtkResource;
import edu.jhuapl.saavtk.vtk.VtkUtil;
import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkCellData;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkUnsignedCharArray;

/**
 * Class which contains the logic to render a collection of control points using
 * the VTK framework.
 * <p>
 * This class supports the following configurable state:
 * <ul>
 * <li>Draw color
 * </ul>
 *
 * @author lopeznr1
 */
public class VtkControlPointPainter implements VtkPropProvider, VtkResource
{
	// State vars
	private ImmutableList<Vector3D> pointL;
	private Color drawColor;

	// VTK vars
	private final vtkPolyData vWorkPD;
	private final vtkActor vWorkActor;
	private final vtkPolyDataMapper vWorkPDM;
	private final vtkIdList vWorkIL;

	private vtkPolyData vEmptyPD;

	/**
	 * Standard Constructor
	 */
	public VtkControlPointPainter()
	{
		pointL = ImmutableList.of();
		drawColor = Color.RED;

		vEmptyPD = VtkUtil.formEmptyPolyData();

		vWorkActor = new vtkActor();
		var workProperty = vWorkActor.GetProperty();
		workProperty.SetColor(drawColor.getRed() / 255.0, drawColor.getGreen() / 255.0, drawColor.getBlue() / 255.0);
		workProperty.SetPointSize(7.0);

		vWorkPD = new vtkPolyData();
		vWorkPD.DeepCopy(vEmptyPD);

		vWorkPDM = new vtkPolyDataMapper();
		vWorkPDM.SetInputData(vWorkPD);
		vWorkPDM.Update();

		vWorkActor.SetMapper(vWorkPDM);
		vWorkActor.Modified();

		vWorkIL = new vtkIdList();
	}

	/**
	 * Adds a control point to the end of the list of current control points.
	 */
	public void addPoint(Vector3D aPoint)
	{
		List<Vector3D> tmpL = new ArrayList<>(pointL);
		tmpL.add(aPoint);

		setControlPoints(tmpL);
	}

	/**
	 * Removes the control point at the specified index.
	 */
	public void delPoint(int aIdx)
	{
		List<Vector3D> tmpL = new ArrayList<>(pointL);
		tmpL.remove(aIdx);

		setControlPoints(tmpL);
	}

	/**
	 * Returns the actor associated with this painter.
	 */
	public vtkActor getActor()
	{
		if (pointL.size() == 0)
			return null;

		return vWorkActor;
	}

	/**
	 * Returns the list of control points.
	 */
	public ImmutableList<Vector3D> getControlPoints()
	{
		return pointL;
	}

	/**
	 * Replaces the list of control points with the specified list.
	 * <P>
	 * VTK state will be updated at the same time.
	 */
	public void setControlPoints(List<Vector3D> aPointL)
	{
		pointL = ImmutableList.copyOf(aPointL);

		vWorkPD.DeepCopy(vEmptyPD);
		vtkPoints points = vWorkPD.GetPoints();
		vtkCellArray vert = vWorkPD.GetVerts();
		vtkCellData cellData = vWorkPD.GetCellData();
		vtkUnsignedCharArray colors = (vtkUnsignedCharArray) cellData.GetScalars();

		vWorkIL.SetNumberOfIds(1);

		int count = 0;
		for (Vector3D aPoint : pointL)
		{
			points.InsertNextPoint(aPoint.toArray());

			vWorkIL.SetId(0, count++);
			vert.InsertNextCell(vWorkIL);

			colors.InsertNextTuple4(drawColor.getRed(), drawColor.getGreen(), drawColor.getBlue(), drawColor.getAlpha());
		}
	}

	/**
	 * Adjusts the (rendered) points by the specified offset.
	 */
	public void shiftControlPoints(PolyhedralModel aSmallBody, double aOffset)
	{
		aSmallBody.shiftPolyLineInNormalDirection(vWorkPD, aOffset);
		vWorkPD.Modified();
	}

	/**
	 * Sets the color to draw the control points.
	 */
	public void setDrawColor(Color aColor)
	{
		drawColor = aColor;

		var workProperty = vWorkActor.GetProperty();
		workProperty.SetColor(drawColor.getRed() / 255.0, drawColor.getGreen() / 255.0, drawColor.getBlue() / 255.0);
	}

	@Override
	public Collection<vtkProp> getProps()
	{
		return ImmutableList.of(vWorkActor);
	}

	@Override
	public void vtkDispose()
	{
		; // Nothing to do
	}

	@Override
	public void vtkUpdateState()
	{
		; // Nothing to do
	}

}

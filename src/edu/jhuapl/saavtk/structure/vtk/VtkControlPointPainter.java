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
import vtk.vtkIdList;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkUnsignedCharArray;

/**
 * Class which contains the logic to render a collection of control points using the VTK framework.
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
	private Color hookColor;
	private int hookIdx;

	// VTK vars
	private final vtkPolyData vWorkPD;
	private final vtkActor vWorkActor;
	private final vtkPolyDataMapper vWorkPDM;
	private final vtkIdList vWorkIL;

	private vtkPolyData vEmptyPD;

	/** Standard Constructor */
	public VtkControlPointPainter()
	{
		pointL = ImmutableList.of();
		drawColor = Color.RED;
		hookColor = Color.RED;
		hookIdx = -1;

		vEmptyPD = VtkUtil.formEmptyPolyData();

		vWorkActor = new vtkActor();
		var workProperty = vWorkActor.GetProperty();
		workProperty.SetColor(drawColor.getRed() / 255.0, drawColor.getGreen() / 255.0, drawColor.getBlue() / 255.0);
		workProperty.SetPointSize(7.0f);

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
	 * Adds a control point at the position just past the specified index.
	 * <p>
	 * Note the hookIdx will be updated to reflect the position of the newly added point.
	 */
	public void addPoint(int aIdx, Vector3D aPoint)
	{
		var tmpPointL = new ArrayList<>(pointL);

		hookIdx = aIdx + 1;
		tmpPointL.add(hookIdx, aPoint);

		setPoints(tmpPointL);
	}

	/**
	 * Removes the control point at the specified index.
	 */
	public void delPoint(int aIdx)
	{
		var tmpPointL = new ArrayList<>(pointL);
		tmpPointL.remove(aIdx);

		if (hookIdx > 0)
			hookIdx--;

		setPoints(tmpPointL);
	}

	/**
	 * Removes all of the control points.
	 */
	public void delPointsAll()
	{
		setPoints(ImmutableList.of());
	}

	/**
	 * Returns the actor associated with this painter.
	 */
	public vtkActor getActor()
	{
		return vWorkActor;
	}

	/**
	 * Returns the index of the hooked point.
	 * <p>
	 * If no point is hooked then -1 will be returned.
	 */
	public int getHookIdx()
	{
		return hookIdx;
	}

	/**
	 * Returns the list of control points.
	 */
	public ImmutableList<Vector3D> getPoints()
	{
		return pointL;
	}

	/**
	 * Moves the control point at the specified index to the position.
	 */
	public void movePoint(int aIdx, Vector3D aPoint)
	{
		// Update the control point
		var tmpPointL = new ArrayList<>(pointL);
		tmpPointL.set(aIdx, aPoint);
		pointL = ImmutableList.copyOf(tmpPointL);

		setPoints(tmpPointL);
	}

	/**
	 * Sets the color used to render non-hooked points.
	 */
	public void setColorDraw(Color aColor)
	{
		drawColor = aColor;
	}

	/**
	 * Sets the color used to render hooked points.
	 */
	public void setColorHook(Color aColor)
	{
		hookColor = aColor;
	}

	/**
	 * Replaces the list of control points with the specified list.
	 * <P>
	 * VTK state will be updated at the same time.
	 */
	public void setPoints(List<Vector3D> aPointL)
	{
		pointL = ImmutableList.copyOf(aPointL);

		// Ensure hookIdx is not past the last point
		var maxIdx = pointL.size() - 1;
		if (hookIdx > maxIdx)
			hookIdx = maxIdx;

		// Update the VTK state
		vWorkPD.DeepCopy(vEmptyPD);
		var vPointP = vWorkPD.GetPoints();
		var vVertCA = vWorkPD.GetVerts();
		var vColorUCA = (vtkUnsignedCharArray) vWorkPD.GetCellData().GetScalars();

		vWorkIL.SetNumberOfIds(1);

		for (var aIdx = 0; aIdx < pointL.size(); aIdx++)
		{
			var tmpPoint = pointL.get(aIdx);
			vPointP.InsertNextPoint(tmpPoint.toArray());

			vWorkIL.SetId(0, aIdx);
			vVertCA.InsertNextCell(vWorkIL);

			var tmpColor = drawColor;
			if (aIdx == hookIdx)
				tmpColor = hookColor;

			vColorUCA.InsertNextTuple4(tmpColor.getRed(), tmpColor.getGreen(), tmpColor.getBlue(), tmpColor.getAlpha());
		}

		vWorkPD.Modified();
	}

	/**
	 * Returns the index of the hooked point.
	 * <p>
	 * The hookIdx will be set to -1 if an invalid value is given.
	 */
	public void setHookIdx(int aIdx)
	{
		var prevIdx = hookIdx;

		hookIdx = aIdx;
		if (hookIdx < 0 || hookIdx >= pointL.size())
			hookIdx = -1;

		// Update the VTK state
		var vColorUCA = (vtkUnsignedCharArray) vWorkPD.GetCellData().GetScalars();
		if (prevIdx >= 0 && prevIdx < pointL.size())
			VtkUtil.setColorOnUCA4(vColorUCA, prevIdx, drawColor);
		if (hookIdx >= 0 && hookIdx < pointL.size())
			VtkUtil.setColorOnUCA4(vColorUCA, hookIdx, hookColor);

		vWorkPD.Modified();
	}

	/**
	 * Adjusts the (rendered) points by the specified radial offset.
	 */
	public void shiftRadialOffset(PolyhedralModel aSmallBody, double aRadialOffset)
	{
		aSmallBody.shiftPolyLineInNormalDirection(vWorkPD, aRadialOffset);
		vWorkPD.Modified();
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

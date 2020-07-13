package edu.jhuapl.saavtk.pick;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.util.EllipseUtil;
import edu.jhuapl.saavtk.vtk.VtkUtil;
import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class CircleSelectionPicker extends Picker
{
	// Reference vars
	private final PolyhedralModel refSmallBody;
	private final AbstractEllipsePolygonModel refStructureManager;
	private final vtkJoglPanelComponent refRenWin;

	// VTK vars
	private final vtkCellPicker vSmallBodyCP;

	// State vars
	private int currVertexId;

	public CircleSelectionPicker(Renderer aRenderer, PolyhedralModel aSmallBody, StructureManager<?> aStructureManager)
	{
		refSmallBody = aSmallBody;
		refStructureManager = (AbstractEllipsePolygonModel) aStructureManager;
		refRenWin = aRenderer.getRenderWindowPanel();

		vSmallBodyCP = PickUtilEx.formSmallBodyPicker(refSmallBody);

		currVertexId = -1;
	}

	@Override
	public int getCursorType()
	{
		return Cursor.CROSSHAIR_CURSOR;
	}

	@Override
	public boolean isExclusiveMode()
	{
		if (currVertexId >= 0)
			return true;

		return false;
	}

	@Override
	public void mousePressed(MouseEvent aEvent)
	{
		// if (e.getButton() != MouseEvent.BUTTON1)
		// return;

		currVertexId = -1;

		refStructureManager.removeAllStructures();

		// Bail if we failed to pick something
		boolean isPicked = PickUtil.isPicked(vSmallBodyCP, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		// Bail if the picked actor is not associated with refSmallBody
		vtkActor pickedActor = vSmallBodyCP.GetActor();
		if (VtkUtil.getAssocModel(pickedActor) != refSmallBody)
			return;

		// Handle the action
		double[] pos = vSmallBodyCP.GetPickPosition();
		if (aEvent.getClickCount() == 1)
		{
			refStructureManager.addNewStructure(new Vector3D(pos));
			currVertexId = refStructureManager.getNumItems() - 1;
		}
	}

	@Override
	public void mouseReleased(MouseEvent aEvent)
	{
		currVertexId = -1;
	}

	@Override
	public void mouseDragged(MouseEvent aEvent)
	{
		// Bail if there is no vertex being edited
		if (currVertexId < 0)
			return;
		Ellipse tmpItem = refStructureManager.getItem(currVertexId);

//		if (aEvent.getButton() != MouseEvent.BUTTON1)
//			return;

		// Bail if we failed to pick something
		boolean isPicked = PickUtil.isPicked(vSmallBodyCP, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		// Bail if the picked actor is not associated with refSmallBody
		vtkActor pickedActor = vSmallBodyCP.GetActor();
		if (VtkUtil.getAssocModel(pickedActor) != refSmallBody)
			return;

		// Handle the action
		double[] lastDragPositionArr = vSmallBodyCP.GetPickPosition();
		Vector3D lastDragPosition = new Vector3D(lastDragPositionArr);

		EllipseUtil.changeRadius(refStructureManager, tmpItem, refSmallBody, lastDragPosition);
	}

}

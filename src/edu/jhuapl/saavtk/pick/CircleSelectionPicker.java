package edu.jhuapl.saavtk.pick;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.structure.Ellipse;
import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class CircleSelectionPicker extends Picker
{
	// Reference vars
	private ModelManager refModelManager;
	private PolyhedralModel refSmallBodyModel;
	private AbstractEllipsePolygonModel refStructureManager;
	private vtkJoglPanelComponent refRenWin;

	// VTK vars
	private vtkCellPicker smallBodyPicker;

	// State vars
	private int currVertexId;

	public CircleSelectionPicker(Renderer aRenderer, ModelManager aModelManager)
	{
		refModelManager = aModelManager;
		refSmallBodyModel = aModelManager.getPolyhedralModel();
		refStructureManager = (AbstractEllipsePolygonModel) aModelManager.getModel(ModelNames.CIRCLE_SELECTION);
		refRenWin = aRenderer.getRenderWindowPanel();

		smallBodyPicker = PickUtilEx.formSmallBodyPicker(refSmallBodyModel);

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
		boolean isPicked = PickUtil.isPicked(smallBodyPicker, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		vtkActor pickedActor = smallBodyPicker.GetActor();
		Model model = refModelManager.getModel(pickedActor);
		if (model == refSmallBodyModel)
		{
			double[] pos = smallBodyPicker.GetPickPosition();
			if (aEvent.getClickCount() == 1)
			{
				refStructureManager.addNewStructure(new Vector3D(pos));
				currVertexId = refStructureManager.getNumItems() - 1;
			}
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
		boolean isPicked = PickUtil.isPicked(smallBodyPicker, refRenWin, aEvent, getTolerance());
		if (isPicked == false)
			return;

		vtkActor pickedActor = smallBodyPicker.GetActor();
		Model model = refModelManager.getModel(pickedActor);

		if (model == refSmallBodyModel)
		{
			double[] lastDragPositionArr = smallBodyPicker.GetPickPosition();
			Vector3D lastDragPosition = new Vector3D(lastDragPositionArr);

			refStructureManager.changeRadiusOfPolygon(tmpItem, lastDragPosition);
		}
	}

}

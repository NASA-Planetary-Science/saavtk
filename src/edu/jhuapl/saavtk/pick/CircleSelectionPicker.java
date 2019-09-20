package edu.jhuapl.saavtk.pick;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.EllipsePolygon;
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

   public void mousePressed(MouseEvent e)
    {
        //if (e.getButton() != MouseEvent.BUTTON1)
        //    return;

        currVertexId = -1;

        refStructureManager.removeAllStructures();

        int pickSucceeded = doPick(e, smallBodyPicker, refRenWin);

        if (pickSucceeded == 1)
        {
            vtkActor pickedActor = smallBodyPicker.GetActor();
            Model model = refModelManager.getModel(pickedActor);

            if (model == refSmallBodyModel)
            {
                double[] pos = smallBodyPicker.GetPickPosition();
                if (e.getClickCount() == 1)
                {
                    refStructureManager.addNewStructure(pos);
                    currVertexId = refStructureManager.getNumItems()-1;
                }
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
		EllipsePolygon tmpItem = refStructureManager.getStructure(currVertexId);
		
//		if (aEvent.getButton() != MouseEvent.BUTTON1)
//			return;
 		
		// Bail if we failed to pick something
		int pickSucceeded = doPick(aEvent, smallBodyPicker, refRenWin);
		if (pickSucceeded != 1)
			return;

		vtkActor pickedActor = smallBodyPicker.GetActor();
		Model model = refModelManager.getModel(pickedActor);

		if (model == refSmallBodyModel)
		{
			double[] lastDragPosition = smallBodyPicker.GetPickPosition();

			refStructureManager.changeRadiusOfPolygon(tmpItem, lastDragPosition);
		}
	}

}

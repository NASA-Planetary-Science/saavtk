package edu.jhuapl.saavtk.pick;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.List;

import vtk.vtkActor;
import vtk.vtkCellPicker;
import vtk.vtkProp;
import vtk.vtkPropCollection;
import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.gui.joglrendering.vtksbmtJoglCanvas;
import edu.jhuapl.saavtk.model.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;

public class CircleSelectionPicker extends Picker
{
    private ModelManager modelManager;
    private vtksbmtJoglCanvas renWin;
    private PolyhedralModel smallBodyModel;
    private AbstractEllipsePolygonModel circleModel;

    private vtkCellPicker smallBodyPicker;

    private int vertexIdBeingEdited = -1;

    public CircleSelectionPicker(
            Renderer renderer,
            ModelManager modelManager
            )
    {
        this.renWin = renderer.getRenderWindowPanel();
        this.modelManager = modelManager;
        this.circleModel = (AbstractEllipsePolygonModel)modelManager.getModel(ModelNames.CIRCLE_SELECTION);

        smallBodyPicker = new vtkCellPicker();
        smallBodyPicker.PickFromListOn();
        smallBodyPicker.InitializePickList();
        smallBodyModel = (PolyhedralModel)modelManager.getPolyhedralModel();
        List<vtkProp> actors = smallBodyModel.getProps();
        vtkPropCollection smallBodyPickList = smallBodyPicker.GetPickList();
        smallBodyPickList.RemoveAllItems();
        for (vtkProp act : actors)
        {
            smallBodyPicker.AddPickList(act);
        }
        smallBodyPicker.AddLocator(smallBodyModel.getCellLocator());
    }

    public void mousePressed(MouseEvent e)
    {
        //if (e.getButton() != MouseEvent.BUTTON1)
        //    return;

        vertexIdBeingEdited = -1;

        circleModel.removeAllStructures();

        int pickSucceeded = doPick(e, smallBodyPicker, renWin);

        if (pickSucceeded == 1)
        {
            vtkActor pickedActor = smallBodyPicker.GetActor();
            Model model = modelManager.getModel(pickedActor);

            if (model == smallBodyModel)
            {
                double[] pos = smallBodyPicker.GetPickPosition();
                if (e.getClickCount() == 1)
                {
                    circleModel.addNewStructure(pos);
                    vertexIdBeingEdited = circleModel.getNumberOfStructures()-1;
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e)
    {
        vertexIdBeingEdited = -1;
    }

    public void mouseDragged(MouseEvent e)
    {
        //if (e.getButton() != MouseEvent.BUTTON1)
        //    return;


        if (vertexIdBeingEdited >= 0)
        {
            int pickSucceeded = doPick(e, smallBodyPicker, renWin);
            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = smallBodyPicker.GetActor();
                Model model = modelManager.getModel(pickedActor);

                if (model == smallBodyModel)
                {
                    double[] lastDragPosition = smallBodyPicker.GetPickPosition();

                    circleModel.changeRadiusOfPolygon(vertexIdBeingEdited, lastDragPosition);
                }
            }
        }
    }

    @Override
    public int getDefaultCursor()
    {
        return Cursor.CROSSHAIR_CURSOR;
    }
}

package edu.jhuapl.saavtk.pick;

import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import vtk.vtkActor;
import vtk.vtkCamera;
import vtk.vtkCellPicker;
import vtk.vtkProp;
import vtk.vtkPropCollection;
import vtk.vtkRenderer;
import edu.jhuapl.saavtk.gui.Renderer;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.Renderer.AxisType;
import edu.jhuapl.saavtk.gui.joglrendering.vtksbmtJoglCanvas;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.popupmenus.PopupManager;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.Properties;

/**
 * This is the picker normally in use by default.
 */
public class DefaultPicker extends Picker
{
    private Renderer renderer;
    private vtksbmtJoglCanvas renWin;
    private StatusBar statusBar;
    private ModelManager modelManager;
    private PopupManager popupManager;
    private vtkCellPicker mousePressNonSmallBodyCellPicker; // includes all props EXCEPT the small body
    private vtkCellPicker smallBodyCellPicker; // only includes small body prop
    private vtkCellPicker allPropsCellPicker; // includes all props including the small body
    private DecimalFormat decimalFormatter = new DecimalFormat("##0.000");
    private DecimalFormat decimalFormatter2 = new DecimalFormat("#0.000");
    private boolean suppressPopups = false;

    public DefaultPicker(
            Renderer renderer,
            StatusBar statusBar,
            ModelManager modelManager,
            PopupManager popupManager)
    {
        this.renderer = renderer;
        this.renWin = renderer.getRenderWindowPanel();
        this.statusBar = statusBar;
        this.modelManager = modelManager;
        this.popupManager = popupManager;

        modelManager.addPropertyChangeListener(this);

        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();

        // See comment in the propertyChange function below as to why
        // we use a custom pick list for these pickers.
        mousePressNonSmallBodyCellPicker = new vtkCellPicker();
        mousePressNonSmallBodyCellPicker.PickFromListOn();
        mousePressNonSmallBodyCellPicker.InitializePickList();

        smallBodyCellPicker = new vtkCellPicker();
        smallBodyCellPicker.PickFromListOn();
        smallBodyCellPicker.InitializePickList();
        List<vtkProp> actors = smallBodyModel.getProps();
        vtkPropCollection smallBodyPickList = smallBodyCellPicker.GetPickList();
        smallBodyPickList.RemoveAllItems();
        for (vtkProp act : actors)
        {
            smallBodyCellPicker.AddPickList(act);
        }
        smallBodyCellPicker.AddLocator(smallBodyModel.getCellLocator());

        allPropsCellPicker = new vtkCellPicker();
        allPropsCellPicker.AddLocator(smallBodyModel.getCellLocator());

        // We need to update the scale bar whenever there is a render or whenever
        // the window gets resized. Although resizing a window results in a render,
        // we still need to listen to a resize event since only listening to render
        // results in the scale bar not being positioned correctly when during the
        // resize for some reason. Thus we need to register a component
        // listener on the renderer panel as well to listen explicitly to resize events.
        // Note also that this functionality is in this class since picking is required
        // to compute the value of the scale bar.
        renWin.getRenderWindow().AddObserver("EndEvent", this, "updateScaleBarValue");
        renWin.getComponent().addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                updateScaleBarValue();
                updateScaleBarPosition();
            }
        });
    }

    public void setSuppressPopups(boolean b)
    {
        this.suppressPopups = b;
    }

    public void mousePressed(MouseEvent e)
    {
        if (renWin.getRenderWindow().GetNeverRendered() > 0)
            return;

        // need to shut off LODs to make sure pick is done on correct geometry
        boolean wasShowingLODs=renderer.showingLODs;
        renderer.hideLODs();

        // First try picking on the non-small-body picker. If that fails try the small body picker.
        int pickSucceeded = doPick(e, mousePressNonSmallBodyCellPicker, renWin);

        // show LODs again if they were shown before picking; view-menu enabling of LODs is handled by the renderer so we don't need to worry about it here
        if (wasShowingLODs)
            renderer.showLODs();

        if (pickSucceeded == 1)
        {
            vtkActor pickedActor = mousePressNonSmallBodyCellPicker.GetActor();
            Model model = modelManager.getModel(pickedActor);

            if (model != null)
            {
//                System.out.println("Picked non-small body: " + model.getClass().getSimpleName());
//                if (model instanceof ImageCollection)
//                {
//                    ImageCollection imageCollection = (ImageCollection)model;
//                    Image firstImage = imageCollection.getImages().iterator().next();
////                    System.out.println("Picked image: " + firstImage.getClass().getSimpleName());
//                    if (firstImage instanceof PerspectiveImage)
//                        setPositionInfoOnPerspectiveImage(e, (PerspectiveImage)firstImage);
//                }


                int cellId = mousePressNonSmallBodyCellPicker.GetCellId();
                double[] pickPosition = mousePressNonSmallBodyCellPicker.GetPickPosition();
                String text = model.getClickStatusBarText(pickedActor, cellId, pickPosition);
                statusBar.setLeftText(text);
                pcs.firePropertyChange(
                        Properties.MODEL_PICKED,
                        null,
                        new PickEvent(e, pickedActor, cellId, pickPosition));
            }
        }
        else
        {
            // If the non-small-body picker failed, see if the user clicked on the small body itself.
            pickSucceeded = doPick(e, smallBodyCellPicker, renWin);

            if (pickSucceeded == 1)
            {
                vtkActor pickedActor = smallBodyCellPicker.GetActor();
                Model model = modelManager.getModel(pickedActor);

                if (model != null)
                {
//                    System.out.println("Picked small body: " + model.getClass().getSimpleName());

                    int cellId = smallBodyCellPicker.GetCellId();
                    double[] pickPosition = smallBodyCellPicker.GetPickPosition();
                    String text = model.getClickStatusBarText(pickedActor, cellId, pickPosition);
                    statusBar.setLeftText(text);
                    pcs.firePropertyChange(
                            Properties.MODEL_PICKED,
                            null,
                            new PickEvent(e, pickedActor, cellId, pickPosition));
                }
            }
            else
            {
                statusBar.setLeftText(" ");
            }
        }
    }

    public void mouseClicked(MouseEvent e)
    {
        // Note that in general when a popup should appear is system dependent. On some systems
        // popups are triggered on mouse press and on others on mouse release.
        // However, in the renderer, we always want the popup to appear on mouse RELEASE
        // not mouse press regardless of platform, because otherwise the popup will interfere
        // with renderer's zoom in and out feature. Therefore, to avoid this whole
        // issue we only show the popup within the mouseClicked call since the mouseClicked
        // event is only thrown when the mouse is released.
        maybeShowPopup(e);

        if (e.getClickCount() == 1 &&
                (e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK)
        {
            // need to shut off LODs to make sure pick is done on correct geometry
            boolean wasShowingLODs=renderer.showingLODs;
            renderer.hideLODs();

            int pickSucceeded = doPick(e, smallBodyCellPicker, renWin);

            // show LODs again if they were shown before picking; view-menu enabling of LODs is handled by the renderer so we don't need to worry about it here
            if (wasShowingLODs)
                renderer.showLODs();

            if (pickSucceeded == 1)
            {
                double[] p = smallBodyCellPicker.GetPickPosition();
//                System.out.println(p[0] + " " + p[1] + " " + p[2]);
                System.out.println(p[0] + " " + p[1] + " " + p[2]);
            }
        }
    }

    public void mouseWheelMoved(MouseWheelEvent e)
    {
        showPositionInfoInStatusBar(e);
    }

    public void mouseDragged(MouseEvent e)
    {
        showPositionInfoInStatusBar(e);
    }

    public void mouseMoved(MouseEvent e)
    {
        if (renWin.getRenderWindow().GetNeverRendered() > 0)
            return;

        showPositionInfoInStatusBar(e);
    }

    private void maybeShowPopup(MouseEvent e)
    {
        if (e.getClickCount() != 1 || !isPopupTrigger(e))
        {
            return;
        }

        if (suppressPopups)
            return;

        if (renWin.getRenderWindow().GetNeverRendered() > 0)
            return;

        int pickSucceeded = doPick(e, mousePressNonSmallBodyCellPicker, renWin);
        if (pickSucceeded == 1)
        {
            vtkActor pickedActor = mousePressNonSmallBodyCellPicker.GetActor();
            popupManager.showPopup(
                    e,
                    pickedActor,
                    mousePressNonSmallBodyCellPicker.GetCellId(),
                    mousePressNonSmallBodyCellPicker.GetPickPosition());
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals(Properties.MODEL_CHANGED))
        {
            // Whenever the model actors change, we need to update the pickers
            // internal list of all actors to pick from. The small body actor is excluded
            // from this list since many other actors occupy the same position
            // as parts of the small body and we want the picker to pick these other
            // actors rather than the small body. Note that this exclusion only applies
            // to the following picker.
            ArrayList<vtkProp> actors = modelManager.getPropsExceptSmallBody();
            vtkPropCollection mousePressNonSmallBodyCellPickList = mousePressNonSmallBodyCellPicker.GetPickList();
            mousePressNonSmallBodyCellPickList.RemoveAllItems();
            for (vtkProp act : actors)
            {
                mousePressNonSmallBodyCellPicker.AddPickList(act);
            }
        }
    }

    private void showPositionInfoInStatusBar(MouseEvent e)
    {
        if (renWin.getRenderWindow().GetNeverRendered() > 0)
            return;

        vtkCamera activeCamera = renWin.getRenderer().GetActiveCamera();
        double[] cameraPos = activeCamera.GetPosition();
        double distance = Math.sqrt(
                cameraPos[0]*cameraPos[0] +
                cameraPos[1]*cameraPos[1] +
                cameraPos[2]*cameraPos[2]);
        String distanceStr = decimalFormatter.format(distance);
        if (distanceStr.length() == 5)
            distanceStr = "  " + distanceStr;
        else if (distanceStr.length() == 6)
            distanceStr = " " + distanceStr;
        distanceStr += " km";

        int pickSucceeded = doPick(e, smallBodyCellPicker, renWin);

        if (pickSucceeded == 1)
        {
            double[] pos = smallBodyCellPicker.GetPickPosition();
            LatLon llr = MathUtil.reclat(pos);

            // Note \u00B0 is the unicode degree symbol

            //double sign = 1.0;
            double lat = llr.lat*180/Math.PI;
            //if (lat < 0.0)
            //    sign = -1.0;
            String latStr = decimalFormatter.format(lat);
            if (latStr.length() == 5)
                latStr = "  " + latStr;
            else if (latStr.length() == 6)
                latStr = " " + latStr;
            //if (lat >= 0.0)
            //    latStr += "\u00B0N";
            //else
            //    latStr += "\u00B0S";
            latStr += "\u00B0";

            // Note that the convention seems to be that longitude
            // is never negative and is shown as E. longitude.
            double lon = llr.lon*180/Math.PI;
            if (lon < 0.0)
                lon += 360.0;
            String lonStr = decimalFormatter.format(lon);
            if (lonStr.length() == 5)
                lonStr = "  " + lonStr;
            else if (lonStr.length() == 6)
                lonStr = " " + lonStr;
            //if (lon >= 0.0)
            //    lonStr += "\u00B0E";
            //else
            //    lonStr += "\u00B0W";
            lonStr += "\u00B0";

            double rad = llr.rad;
            String radStr = decimalFormatter2.format(rad);
            if (radStr.length() == 5)
                radStr = " " + radStr;
            radStr += " km";

            statusBar.setRightText("Lat: " + latStr + "  Lon: " + lonStr + "  Radius: " + radStr + "  Distance: " + distanceStr + " ");
        }
        else
        {
            statusBar.setRightText("Distance: " + distanceStr + " ");
        }
    }

//    private void setPositionInfoOnPerspectiveImage(MouseEvent e, PerspectiveImage pi)
//    {
////        System.out.println("Setting position on image" + pi.getImageName());
//
//        if (renWin.getRenderWindow().GetNeverRendered() > 0)
//            return;
//
//        vtkCamera activeCamera = renWin.getRenderer().GetActiveCamera();
//        double[] cameraPos = activeCamera.GetPosition();
//
//        int pickSucceeded = doPick(e, smallBodyCellPicker, renWin);
//
//        if (pickSucceeded == 1)
//        {
//            double[] pos = smallBodyCellPicker.GetPickPosition();
//            pi.setPickedPosition(pos);
//        }
//    }

    /**
     * Computes the size of a pixel in body fixed coordinates. This is only meaningful
     * when the user is zoomed in a lot. To compute a result all 4 corners of the
     * view window must intersect the asteroid.
     *
     * @return
     */
    private double computeSizeOfPixel()
    {
        // Do a pick at each of the 4 corners of the renderer
        long currentTime = System.currentTimeMillis();
        int width = renWin.getComponent().getWidth();
        int height = renWin.getComponent().getHeight();

        int[][] corners = { {0, 0}, {width-1, 0}, {width-1, height-1}, {0, height-1} };
        double[][] points = new double[4][3];
        for (int i=0; i<4; ++i)
        {
            int pickSucceeded = doPick(currentTime, corners[i][0], corners[i][1], smallBodyCellPicker, renWin);

            if (pickSucceeded == 1)
            {
                points[i] = smallBodyCellPicker.GetPickPosition();
            }
            else
            {
                return -1.0;
            }
        }

        // Compute the scale if all 4 points intersect by averaging the distance of all 4 sides
        double bottom = MathUtil.distanceBetweenFast(points[0], points[1]);
        double right  = MathUtil.distanceBetweenFast(points[1], points[2]);
        double top    = MathUtil.distanceBetweenFast(points[2], points[3]);
        double left   = MathUtil.distanceBetweenFast(points[3], points[0]);

        double sizeOfPixel =
                ( bottom / (double)(width-1)  +
                  right  / (double)(height-1) +
                  top    / (double)(width-1)  +
                  left   / (double)(height-1) ) / 4.0;

        return sizeOfPixel;
    }

    private void updateScaleBarValue()
    {
        double sizeOfPixel = computeSizeOfPixel();
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
        smallBodyModel.updateScaleBarValue(sizeOfPixel);
    }

    public void updateScaleBarPosition()
    {
        PolyhedralModel smallBodyModel = modelManager.getPolyhedralModel();
        smallBodyModel.updateScaleBarPosition(renWin.getComponent().getWidth(), renWin.getComponent().getHeight());
    }

    public void keyPressed(KeyEvent e)
    {
        vtkRenderer ren = renWin.getRenderer();
        if (ren.VisibleActorCount() == 0) return;

        int keyCode = e.getKeyCode();

        // Only repond to c, x, y, or z press if in the default interactor (e.g.
        // not when drawing structures)
        if (keyCode == KeyEvent.VK_C && renWin.getRenderWindowInteractor().GetInteractorStyle() != null)
        {
            Point pt = renWin.getComponent().getMousePosition();
            if (pt != null)
            {
                // The call to doPick requires a MouseEvent, so create one here
                MouseEvent me = new MouseEvent(e.getComponent(), e.getID(), e.getWhen(),
                        e.getModifiers(),
                        pt.x, pt.y,
                        0, false);

                int pickSucceeded = doPick(me, allPropsCellPicker, renWin);
                if (pickSucceeded == 1)
                {
                    double[] pos = allPropsCellPicker.GetPickPosition();

                    renWin.getVTKLock().lock();
                    vtkCamera activeCamera = renWin.getRenderer().GetActiveCamera();
                    activeCamera.SetFocalPoint(pos);
                    renWin.getVTKLock().unlock();

                    renWin.resetCameraClippingRange();
                    renWin.Render();
                }
            }
        }
        else if ((keyCode == KeyEvent.VK_X ||
                  keyCode == KeyEvent.VK_Y ||
                  keyCode == KeyEvent.VK_Z) &&
                 renWin.getRenderWindowInteractor().GetInteractorStyle() != null)
        {
            char keyChar = e.getKeyChar();

            if ('X' == keyChar)
                renderer.setCameraOrientationInDirectionOfAxis(AxisType.NEGATIVE_X, true);
            else if ('x' == keyChar)
                renderer.setCameraOrientationInDirectionOfAxis(AxisType.POSITIVE_X, true);
            else if ('Y' == keyChar)
                renderer.setCameraOrientationInDirectionOfAxis(AxisType.NEGATIVE_Y, true);
            else if ('y' == keyChar)
                renderer.setCameraOrientationInDirectionOfAxis(AxisType.POSITIVE_Y, true);
            else if ('Z' == keyChar)
                renderer.setCameraOrientationInDirectionOfAxis(AxisType.NEGATIVE_Z, true);
            else if ('z' == keyChar)
                renderer.setCameraOrientationInDirectionOfAxis(AxisType.POSITIVE_Z, true);
        }
        else if (keyCode == KeyEvent.VK_N &&
                 renWin.getRenderWindowInteractor().GetInteractorStyle() != null)
      {
            // The following spins the view along the boresight of the current
            // camera so that the Z axis of the body is up.
            vtkCamera activeCamera = renWin.getRenderer().GetActiveCamera();
            double[] position = activeCamera.GetPosition();
            double[] focalPoint = activeCamera.GetFocalPoint();
            double viewAngle = renderer.getCameraViewAngle();

            double[] dir = {focalPoint[0]-position[0],
                    focalPoint[1]-position[1],
                    focalPoint[2]-position[2]
            };
            MathUtil.vhat(dir, dir);

            double[] zAxis = {0.0, 0.0, 1.0};
            double[] upVector = new double[3];
            MathUtil.vcrss(dir, zAxis, upVector);

            if (upVector[0] != 0.0 || upVector[1] != 0.0 || upVector[2] != 0.0)
            {
                MathUtil.vcrss(upVector, dir, upVector);
                renderer.setCameraOrientation(position, focalPoint, upVector, viewAngle);
            }
      }
    }

}

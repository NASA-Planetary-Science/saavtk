package edu.jhuapl.saavtk.gui.joglrendering;


import vtk.vtkAxesActor;
import vtk.vtkCamera;
import vtk.vtkCaptionActor2D;
import vtk.vtkOrientationMarkerWidget;
import vtk.vtkProperty;
import vtk.vtkRenderer;
import vtk.vtkTextProperty;
import edu.jhuapl.saavtk.util.Preferences;

public class MirrorCanvas extends vtksbmtJoglCanvas
{
    vtksbmtJoglCanvas parent;
    vtkRenderer defaultRenderer;
    double eyeSeparation;
//    vtkPolyData background;
//    vtkPolyDataMapper backgroundMapper;
//    vtkActor backgroundActor;
    private vtkOrientationMarkerWidget orientationWidget;
    private vtkAxesActor axes;
    private double axesSize;

    public MirrorCanvas(vtksbmtJoglCanvas parent)
    {
        this.parent=parent;
        parent.getActiveCamera().AddObserver("ModifiedEvent", this, "syncWindows");
        this.getActiveCamera().AddObserver("ModifiedEvent", this, "syncWindows");
        //
        defaultRenderer=getRenderer();
        eyeSeparation=getActiveCamera().GetEyeSeparation();
        //
        initOrientationWidget();
        //
        syncCameras(parent.getActiveCamera(), this.getActiveCamera());
        parent.uiComponent.repaint();   // this must be done here to "wake up" parent renderWindow; not sure why but otherwise the user has to refocus the mouse on the main window and go back to the mirror window

    }

    void initOrientationWidget()
    {
        axes = new vtkAxesActor();

        vtkCaptionActor2D caption = axes.GetXAxisCaptionActor2D();
        caption.GetTextActor().SetTextScaleModeToNone();
        vtkTextProperty textProperty = caption.GetCaptionTextProperty();
        textProperty.BoldOff();
        textProperty.ItalicOff();

        caption = axes.GetYAxisCaptionActor2D();
        caption.GetTextActor().SetTextScaleModeToNone();
        textProperty = caption.GetCaptionTextProperty();
        textProperty.BoldOff();
        textProperty.ItalicOff();

        caption = axes.GetZAxisCaptionActor2D();
        caption.GetTextActor().SetTextScaleModeToNone();
        textProperty = caption.GetCaptionTextProperty();
        textProperty.BoldOff();
        textProperty.ItalicOff();

        setXAxisColor(Preferences.getInstance().getAsIntArray(Preferences.AXES_XAXIS_COLOR, new int[]{255, 0, 0}));
        setYAxisColor(Preferences.getInstance().getAsIntArray(Preferences.AXES_YAXIS_COLOR, new int[]{0, 255, 0}));
        setZAxisColor(Preferences.getInstance().getAsIntArray(Preferences.AXES_ZAXIS_COLOR, new int[]{255, 255, 0}));
        setAxesLabelFontSize((int)Preferences.getInstance().getAsLong(Preferences.AXES_FONT_SIZE, 12L));
        setAxesLabelFontColor(Preferences.getInstance().getAsIntArray(Preferences.AXES_FONT_COLOR, new int[]{255, 255, 255}));
        setAxesLineWidth(Preferences.getInstance().getAsDouble(Preferences.AXES_LINE_WIDTH, 1.0));
        setAxesConeLength(Preferences.getInstance().getAsDouble(Preferences.AXES_CONE_LENGTH, 0.2));
        setAxesConeRadius(Preferences.getInstance().getAsDouble(Preferences.AXES_CONE_RADIUS, 0.4));

        orientationWidget = new vtkOrientationMarkerWidget();
        orientationWidget.SetOrientationMarker(axes);
        orientationWidget.SetInteractor(this.getRenderWindowInteractor());
        orientationWidget.SetTolerance(10);
        orientationWidget.SetEnabled(1);
        orientationWidget.SetInteractive(1);
    }

    public void setXAxisColor(int[] color)
    {
        vtkProperty property = axes.GetXAxisShaftProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        property = axes.GetXAxisTipProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        if (this.getRenderWindow().GetNeverRendered() == 0)
            this.Render();
    }

    public void setYAxisColor(int[] color)
    {
        vtkProperty property = axes.GetYAxisShaftProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        property = axes.GetYAxisTipProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        if (this.getRenderWindow().GetNeverRendered() == 0)
            this.Render();
    }

    public void setZAxisColor(int[] color)
    {
        vtkProperty property = axes.GetZAxisShaftProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        property = axes.GetZAxisTipProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        if (this.getRenderWindow().GetNeverRendered() == 0)
            this.Render();
    }

    public void setAxesConeLength(double size)
    {
        if (size > 1.0) size = 1.0;
        if (size < 0.0) size = 0.0;
        axes.SetNormalizedTipLength(size, size, size);
        // Change the shaft length also to fill in any space.
        axes.SetNormalizedShaftLength(1.0-size, 1.0-size, 1.0-size);
        if (this.getRenderWindow().GetNeverRendered() == 0)
            this.Render();
    }

    public void setAxesConeRadius(double size)
    {
        axes.SetConeRadius(size);
        if (this.getRenderWindow().GetNeverRendered() == 0)
            this.Render();
    }

    public void setAxesSize(double size)
    {
        this.axesSize = size;
        orientationWidget.SetViewport(0.0, 0.0, size, size);
        if (this.getRenderWindow().GetNeverRendered() == 0)
            this.Render();
    }

    public void setAxesLineWidth(double width)
    {
        vtkProperty property = axes.GetXAxisShaftProperty();
        property.SetLineWidth(width);
        property = axes.GetYAxisShaftProperty();
        property.SetLineWidth(width);
        property = axes.GetZAxisShaftProperty();
        property.SetLineWidth(width);
        if (this.getRenderWindow().GetNeverRendered() == 0)
            this.Render();
    }

    public void setAxesLabelFontSize(int size)
    {
        vtkCaptionActor2D caption = axes.GetXAxisCaptionActor2D();
        vtkTextProperty textProperty = caption.GetCaptionTextProperty();
        textProperty.SetFontSize(size);
        caption = axes.GetYAxisCaptionActor2D();
        textProperty = caption.GetCaptionTextProperty();
        textProperty.SetFontSize(size);
        caption = axes.GetZAxisCaptionActor2D();
        textProperty = caption.GetCaptionTextProperty();
        textProperty.SetFontSize(size);
        if (this.getRenderWindow().GetNeverRendered() == 0)
            this.Render();
    }

    public void setAxesLabelFontColor(int[] color)
    {
        vtkCaptionActor2D caption = axes.GetXAxisCaptionActor2D();
        vtkTextProperty textProperty = caption.GetCaptionTextProperty();
        textProperty.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        caption = axes.GetYAxisCaptionActor2D();
        textProperty = caption.GetCaptionTextProperty();
        textProperty.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        caption = axes.GetZAxisCaptionActor2D();
        textProperty = caption.GetCaptionTextProperty();
        textProperty.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        if (this.getRenderWindow().GetNeverRendered() == 0)
            this.Render();
    }

 /*   void generateBackground(int ndiv, double scale)
    {
        vtkRectilinearGrid grid=new vtkRectilinearGrid();
        vtkDoubleArray div=new vtkDoubleArray();
        for (int i=0; i<ndiv; i++)
            div.InsertNextValue(-scale+(double)i*2.*scale/(double)(ndiv-1));
        grid.SetDimensions(ndiv, ndiv, ndiv);
        grid.SetXCoordinates(div);
        grid.SetYCoordinates(div);
        grid.SetZCoordinates(div);
        vtkGeometryFilter filter=new vtkGeometryFilter();
        filter.SetInputData(grid);
        filter.Update();
        background=new vtkPolyData();
        background.DeepCopy(filter.GetOutput());
        //
        backgroundMapper=new vtkPolyDataMapper();
        backgroundMapper.SetInputData(background);
        backgroundActor=new vtkActor();
        backgroundActor.SetMapper(backgroundMapper);
        backgroundActor.GetProperty().SetColor(1,1,1);
        backgroundActor.GetProperty().SetRepresentationToWireframe();
        backgroundActor.GetProperty().BackfaceCullingOff();
        backgroundActor.GetProperty().SetLineWidth(5);
        //
        getRenderer().AddActor(backgroundActor);
    }*/

    void syncWindows()
    {
        if(parent.getComponent().isFocusOwner())
        {
            //System.out.println("parent "+System.currentTimeMillis());
            syncCameras(parent.getActiveCamera(), this.getActiveCamera());
            Render();
        }
        else
        {
            //System.out.println("mirror "+System.currentTimeMillis());
            syncCameras(this.getActiveCamera(), parent.getActiveCamera());
            parent.uiComponent.repaint();
        };
    }

    void syncCameras(vtkCamera sourceCam, vtkCamera targetCam)
    {
        if (sourceCam==null || targetCam==null)
            return;
        //Vector3D pos=new Vector3D(sourceCam.GetPosition());
        //Vector3D fp=new Vector3D(sourceCam.GetFocalPoint());
        //double len=pos.subtract(fp).getNorm();
        getVTKLock().lock();
        targetCam.SetPosition(sourceCam.GetPosition());
        targetCam.SetFocalPoint(sourceCam.GetFocalPoint());
        targetCam.SetViewUp(sourceCam.GetViewUp());
        targetCam.SetViewAngle(sourceCam.GetViewAngle());
        targetCam.SetClippingRange(sourceCam.GetClippingRange());
        targetCam.SetEyeSeparation(eyeSeparation);
        getVTKLock().unlock();
        Render();
    }

    public void decreaseEyeSeparation()
    {
        eyeSeparation*=0.95;
        Render();
    }

    public void increaseEyeSeparation()
    {
        eyeSeparation*=1.05;
        Render();
    }

}


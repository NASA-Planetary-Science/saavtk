package edu.jhuapl.saavtk.gui.render;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import vtk.vtkActor;
import vtk.vtkAxesActor;
import vtk.vtkBMPWriter;
import vtk.vtkCamera;
import vtk.vtkCaptionActor2D;
import vtk.vtkCellLocator;
import vtk.vtkIdList;
import vtk.vtkInteractorStyle;
import vtk.vtkInteractorStyleImage;
import vtk.vtkInteractorStyleJoystickCamera;
import vtk.vtkInteractorStyleTrackballCamera;
import vtk.vtkJPEGWriter;
import vtk.vtkLight;
import vtk.vtkLightKit;
import vtk.vtkOrientationMarkerWidget;
import vtk.vtkPNGWriter;
import vtk.vtkPNMWriter;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPostScriptWriter;
import vtk.vtkProp;
import vtk.vtkPropCollection;
import vtk.vtkProperty;
import vtk.vtkRenderer;
import vtk.vtkSTLWriter;
import vtk.vtkScalarBarActor;
import vtk.vtkTIFFWriter;
import vtk.vtkTextProperty;
import vtk.vtkTriangle;
import vtk.vtkWindowToImageFilter;
import vtk.vtksbTriangle;
import vtk.rendering.jogl.vtkJoglPanelComponent;
import edu.jhuapl.saavtk.colormap.Colorbar;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.jogl.StereoCapableMirrorCanvas;
import edu.jhuapl.saavtk.gui.jogl.StereoCapableMirrorCanvas.StereoMode;
import edu.jhuapl.saavtk.gui.render.camera.CameraFrame;
import edu.jhuapl.saavtk.gui.render.toolbar.RenderToolbar;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.OccludingCaptionActor;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.Preferences;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.util.SaavtkLODActor;
import edu.jhuapl.saavtk.util.SmallBodyCubes;

public class Renderer extends JPanel implements
            PropertyChangeListener, ActionListener
{
    public enum LightingType
    {
        NONE,
        HEADLIGHT,
        LIGHT_KIT,
        FIXEDLIGHT
    }

    public enum InteractorStyleType
    {
        TRACKBALL_CAMERA,
        JOYSTICK_CAMERA
    }

    public enum AxisType
    {
        NONE,
        POSITIVE_X,
        NEGATIVE_X,
        POSITIVE_Y,
        NEGATIVE_Y,
        POSITIVE_Z,
        NEGATIVE_Z
    }

    public enum ProjectionType
    {
        PERSPECTIVE {
            @Override
            public String toString() {
                return "Perspective";
            }
        },
        ORTHOGRAPHIC {
            @Override
            public String toString() {
                return "Orthographic";
            }
        }
    }

    protected RenderPanel mainCanvas;
    private ModelManager modelManager;
    private vtkInteractorStyleTrackballCamera trackballCameraInteractorStyle;
    private vtkInteractorStyleJoystickCamera joystickCameraInteractorStyle;
    private vtkInteractorStyle defaultInteractorStyle;
    private vtkAxesActor axes;
//    private vtkOrientationMarkerWidget orientationWidget;
    private vtkLightKit lightKit;
    private vtkLight headlight;
    private vtkLight fixedLight;
    private LightingType currentLighting = LightingType.NONE;
    // We need a separate flag for this since we should modify interaction if
    // axes are enabled
    private boolean interactiveAxes = true;
    private double axesSize; // needed because java wrappers do not expose vtkOrientationMarkerWidget.GetViewport() function.

    public static boolean enableLODs = true; // This is temporary to show off the LOD feature, very soon we will replace this with an actual menu
    public boolean showingLODs = false;
    
    private StatusBar statusBar=null;
    private Colorbar smallBodyColorbar;
    boolean inInteraction=false;

    void initOrientationAxes()
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

  //      orientationWidget = new vtkOrientationMarkerWidget();
  //      orientationWidget.SetOrientationMarker(axes);
  //      orientationWidget.SetInteractor(mainCanvas.getRenderWindowInteractor());
  //      orientationWidget.SetTolerance(10);
        setAxesSize(Preferences.getInstance().getAsDouble(Preferences.AXES_SIZE, 0.2));
    }

    void initLights()
    {
        headlight = mainCanvas.getRenderer().MakeLight();
        headlight.SetLightTypeToHeadlight();
        headlight.SetConeAngle(180.0);

        fixedLight = mainCanvas.getRenderer().MakeLight();
        fixedLight.SetLightTypeToSceneLight();
        fixedLight.PositionalOn();
        fixedLight.SetConeAngle(180.0);
        LatLon defaultPosition = new LatLon(
                Preferences.getInstance().getAsDouble(Preferences.FIXEDLIGHT_LATITUDE, 90.0),
                Preferences.getInstance().getAsDouble(Preferences.FIXEDLIGHT_LONGITUDE, 0.0),
                Preferences.getInstance().getAsDouble(Preferences.FIXEDLIGHT_DISTANCE, 1.0e8));
        setFixedLightPosition(defaultPosition);
        setLightIntensity(Preferences.getInstance().getAsDouble(Preferences.LIGHT_INTENSITY, 1.0));

        mainCanvas.getRenderer().AutomaticLightCreationOff();
        lightKit = new vtkLightKit();
        lightKit.SetKeyToFillRatio(1.0);
        lightKit.SetKeyToHeadRatio(20.0);

        LightingType lightingType = LightingType.valueOf(
                Preferences.getInstance().get(Preferences.LIGHTING_TYPE, LightingType.LIGHT_KIT.toString()));
        setLighting(lightingType);

    }

    List<KeyListener> listeners=Lists.newArrayList();

    @Override
    public synchronized void addKeyListener(KeyListener l)
    {
        listeners.add(l);
    }

    void localKeypressHandler() // this prioritizes key presses from the main canvas and if none exist then it handles any key presses from the mirror canvas
    // TODO: clean up logic here
    {
        for (KeyListener listener : listeners)
        {
            int shiftDown=mainCanvas.getRenderWindowInteractor().GetShiftKey();
            int altDown=mainCanvas.getRenderWindowInteractor().GetAltKey();
            int ctrlDown=mainCanvas.getRenderWindowInteractor().GetControlKey();
            int modifiers=(KeyEvent.SHIFT_DOWN_MASK*shiftDown) | (KeyEvent.ALT_DOWN_MASK*altDown) | (KeyEvent.CTRL_DOWN_MASK*ctrlDown);
            listener.keyPressed(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, mainCanvas.getRenderWindowInteractor().GetKeyCode(), mainCanvas.getRenderWindowInteractor().GetKeyCode()));
        }

    }

    public Renderer(final ModelManager modelManager, StatusBar statusBar)
    {
        this(modelManager);
        this.statusBar=statusBar;
    }
    
    RenderToolbar toolbar;
    
    public Renderer(final ModelManager modelManager)
    {

        setLayout(new BorderLayout());

        toolbar=new RenderToolbar();
        mainCanvas=new RenderPanel(toolbar);//, statusBar)
        mainCanvas.getRenderWindowInteractor().AddObserver("KeyPressEvent", this, "localKeypressHandler");

        this.modelManager = modelManager;

        
        modelManager.addPropertyChangeListener(this);

        trackballCameraInteractorStyle = new vtkInteractorStyleTrackballCamera();
        joystickCameraInteractorStyle = new vtkInteractorStyleJoystickCamera();

        defaultInteractorStyle = trackballCameraInteractorStyle;

 /*       InteractorStyleType interactorStyleType = InteractorStyleType.valueOf(
                Preferences.getInstance().get(Preferences.INTERACTOR_STYLE_TYPE, InteractorStyleType.TRACKBALL_CAMERA.toString()));
        setDefaultInteractorStyleType(interactorStyleType);

        setMouseWheelMotionFactor(Preferences.getInstance().getAsDouble(Preferences.MOUSE_WHEEL_MOTION_FACTOR, 1.0));
*/
        setBackgroundColor(new int[]{0,0,0});//Preferences.getInstance().getAsIntArray(Preferences.BACKGROUND_COLOR, new int[]{0, 0, 0}));

        initLights();

        add(toolbar, BorderLayout.NORTH);
        add(mainCanvas.getComponent(), BorderLayout.CENTER);
        toolbar.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

       // mainCanvas.getRenderWindow().StereoCapableWindowOn();
  //      mainCanvas.getRenderWindowInteractor().CreateRepeatingTimer(1000);  // once per second we make sure the stereo mode hasn't been changed via keyboard; this is admittedly a sloppy kludge to override the '3' key behavior (anaglyph toggle) in vtk
  //      mainCanvas.getRenderWindowInteractor().AddObserver("TimerEvent", this, "checkStereoModeSynchronization");

        // Setup observers for start/stop interaction events
        mainCanvas.getRenderWindowInteractor().AddObserver("StartInteractionEvent", this, "onStartInteraction");
        mainCanvas.getRenderWindowInteractor().AddObserver("InteractionEvent", this, "duringInteraction");
        mainCanvas.getRenderWindowInteractor().AddObserver("EndInteractionEvent", this, "onEndInteraction");
        
        smallBodyColorbar=new Colorbar(this);

        initOrientationAxes();

        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                setShowOrientationAxes(Preferences.getInstance().getAsBoolean(Preferences.SHOW_AXES, true));
                setOrientationAxesInteractive(Preferences.getInstance().getAsBoolean(Preferences.INTERACTIVE_AXES, true));
                setProps(modelManager.getProps());
                //mainCanvas.resetCamera();
                //mainCanvas.Render();
            }
        });
    }

    public void setProps(List<vtkProp> props)
    {
        setProps(props,mainCanvas,mainCanvas.getRenderer());
    }

    public void setProps(List<vtkProp> props, vtkJoglPanelComponent renderWindow, vtkRenderer whichRenderer)
    {
        // Go through the props and if an prop is already in the renderer,
        // do nothing. If not, add it. If an prop not listed is
        // in the renderer, remove it from the renderer.

    	
        // First remove the props not in the specified list that are currently rendered.
        vtkPropCollection propCollection = renderWindow.getRenderer().GetViewProps();
        int size = propCollection.GetNumberOfItems();
        HashSet<vtkProp> renderedProps = new HashSet<vtkProp>();
        for (int i=0; i<size; ++i)
            renderedProps.add((vtkProp)propCollection.GetItemAsObject(i));
        renderedProps.removeAll(props);
        if (!renderedProps.isEmpty())
        {
            renderWindow.getVTKLock().lock();
            for (vtkProp prop : renderedProps)
                whichRenderer.RemoveViewProp(prop);
            renderWindow.getVTKLock().unlock();
        }
        

        // Next add the new props.
        for (vtkProp prop : props)
        {
            if (whichRenderer.HasViewProp(prop) == 0)
                whichRenderer.AddViewProp(prop);
        }
        //whichRenderer.AddActor(mainCanvas.getAxesActor());

        // If we are in 2D mode, then remove all props of models that
        // do not support 2D mode.
        if (modelManager.is2DMode())
        {
            propCollection = whichRenderer.GetViewProps();
            size = propCollection.GetNumberOfItems();
            for (int i=size-1; i>=0; --i)
            {
                vtkProp prop = (vtkProp)propCollection.GetItemAsObject(i);
                Model model = modelManager.getModel(prop);
                if (model != null && !model.supports2DMode())
                {
                    //renderWindow.getVTKLock().lock();
                    whichRenderer.RemoveViewProp(prop);
                    //renderWindow.getVTKLock().unlock();
                }
            }
        }
        //
        occludeLabels();

        if (renderWindow.getRenderWindow().GetNeverRendered() > 0)
            return;
        renderWindow.Render();
    }

    public void onStartInteraction()
    {
        showLODs();
        occludeLabels();
    }

    public void duringInteraction()
    {
    	occludeLabels();
    }
    
    public void showLODs()
    {
        // LOD switching control for SaavtkLODActor
        if(enableLODs && modelManager != null && !showingLODs)
        {
            showingLODs=true;
            List<vtkProp> props = modelManager.getProps();
            for(vtkProp prop : props)
            {
                if(prop instanceof SaavtkLODActor)
                {
                    ((SaavtkLODActor)prop).showLOD();
                }
            }
        }

    }

    public void hideLODs()
    {
        if(enableLODs && modelManager != null && showingLODs)
        {
            showingLODs=false;
            List<vtkProp> props = modelManager.getProps();
            for(vtkProp prop : props)
            {
                if(prop instanceof SaavtkLODActor)
                {
                    ((SaavtkLODActor)prop).hideLOD();
                }
            }
        }

    }

    public void onEndInteraction()
    {
        hideLODs();
        occludeLabels();
    }
    
    public void occludeLabels()
    {
		Vector3D lookat=new Vector3D(getRenderWindowPanel().getActiveCamera().GetFocalPoint());
		Vector3D campos=new Vector3D(getRenderWindowPanel().getActiveCamera().GetPosition());
		Vector3D lookdir=lookat.subtract(campos);
		GenericPolyhedralModel model=(GenericPolyhedralModel)modelManager.getModel(ModelNames.SMALL_BODY);
		vtkCellLocator locator=model.getCellLocator();
        for (vtkProp prop : modelManager.getProps())
        	if (prop instanceof OccludingCaptionActor)
        	{
        		OccludingCaptionActor caption=(OccludingCaptionActor) prop;
        		Vector3D normal=new Vector3D(caption.getNormal());
        		if (!caption.isEnabled()|| normal.dotProduct(lookdir)>0)
        			prop.VisibilityOff();
        		else
        		{
        			double tolerance=1e-15;
        			vtkIdList ids=new vtkIdList();
        			double[] rayStartPoint=caption.getRayStartPoint();
        			locator.FindCellsAlongLine(rayStartPoint, campos.toArray(), tolerance, ids);
        			if (ids.GetNumberOfIds()>0)
        				prop.VisibilityOff();
        			else
        				prop.VisibilityOn();
        		}
        		        		
        	}

    }

    public void saveToFile()
    {
    	getRenderWindowPanel().Render();
        File file = CustomFileChooser.showSaveDialog(this, "Export to PNG Image", "image.png", "png");
        saveToFile(file, mainCanvas);
    }

    private BlockingQueue<CameraFrame> cameraFrameQueue;

    private File[] sixFiles = new File[6];

    AxisType[] sixAxes = {
            AxisType.POSITIVE_X, AxisType.NEGATIVE_X,
            AxisType.POSITIVE_Y, AxisType.NEGATIVE_Y,
            AxisType.POSITIVE_Z, AxisType.NEGATIVE_Z
    };


    public void save6ViewsToFile()
    {
        File file = CustomFileChooser.showSaveDialog(this, "Export to PNG Image", "", "png");
        String path = file.getAbsolutePath();
        String base = path.substring(0, path.lastIndexOf('.'));
        String ext = path.substring(path.lastIndexOf('.'));

        sixFiles[0] = new File(base + "+x" + ext);
        sixFiles[1] = new File(base + "-x" + ext);
        sixFiles[2] = new File(base + "+y" + ext);
        sixFiles[3] = new File(base + "-y" + ext);
        sixFiles[4] = new File(base + "+z" + ext);
        sixFiles[5] = new File(base + "-z" + ext);

        sixAxes[0] = AxisType.POSITIVE_X;
        sixAxes[1] = AxisType.NEGATIVE_X;
        sixAxes[2] = AxisType.POSITIVE_Y;
        sixAxes[3] = AxisType.NEGATIVE_Y;
        sixAxes[4] = AxisType.POSITIVE_Z;
        sixAxes[5] = AxisType.NEGATIVE_Z;

        // Check if one of the files already exist and if so, prompt user.
        for (File f : sixFiles)
        {
            if (f.exists())
            {
                int response = JOptionPane.showConfirmDialog (JOptionPane.getFrameForComponent(this),
                  "Overwrite file(s)?","Confirm Overwrite",
                   JOptionPane.OK_CANCEL_OPTION,
                   JOptionPane.QUESTION_MESSAGE);

                if (response == JOptionPane.CANCEL_OPTION)
                    return;
                else
                {
                    break;
                }
            }
        }

        cameraFrameQueue = new LinkedBlockingQueue<CameraFrame>();

        for (int i=0; i<6; i++)
        {
            File f = sixFiles[i];
            AxisType at = sixAxes[i];
            CameraFrame frame = createCameraFrameInDirectionOfAxis(at, true, f, 1);
            cameraFrameQueue.add(frame);
        }

        // start off the timer
        this.actionPerformed(null);

    }

    public void setCameraOrientationInDirectionOfAxis(AxisType axisType, boolean preserveCurrentDistance)
    {
        vtkRenderer ren = mainCanvas.getRenderer();
        if (ren.VisibleActorCount() == 0) return;

        mainCanvas.getVTKLock().lock();

        double[] bounds = modelManager.getPolyhedralModel().getBoundingBox().getBounds();
        double xSize = Math.abs(bounds[1] - bounds[0]);
        double ySize = Math.abs(bounds[3] - bounds[2]);
        double zSize = Math.abs(bounds[5] - bounds[4]);
        double maxSize = Math.max(Math.max(xSize, ySize), zSize);

        double cameraDistance = getCameraDistance();

        vtkCamera cam = ren.GetActiveCamera();
        cam.SetFocalPoint(0.0, 0.0, 0.0);

        if (axisType == AxisType.NEGATIVE_X)
        {
            double xpos = xSize / Math.tan(Math.PI/6.0) + 2.0*maxSize;
            cam.SetPosition(xpos, 0.0, 0.0);
            cam.SetViewUp(0.0, 0.0, 1.0);
        }
        else if (axisType == AxisType.POSITIVE_X)
        {
            double xpos = -xSize / Math.tan(Math.PI/6.0) - 2.0*maxSize;
            cam.SetPosition(xpos, 0.0, 0.0);
            cam.SetViewUp(0.0, 0.0, 1.0);
        }
        else if (axisType == AxisType.NEGATIVE_Y)
        {
            double ypos = ySize / Math.tan(Math.PI/6.0) + 2.0*maxSize;
            cam.SetPosition(0.0, ypos, 0.0);
            cam.SetViewUp(0.0, 0.0, 1.0);
        }
        else if (axisType == AxisType.POSITIVE_Y)
        {
            double ypos = -ySize / Math.tan(Math.PI/6.0) - 2.0*maxSize;
            cam.SetPosition(0.0, ypos, 0.0);
            cam.SetViewUp(0.0, 0.0, 1.0);
        }
        else if (axisType == AxisType.NEGATIVE_Z)
        {
            double zpos = zSize / Math.tan(Math.PI/6.0) + 2.0*maxSize;
            cam.SetPosition(0.0, 0.0, zpos);
            cam.SetViewUp(0.0, 1.0, 0.0);
        }
        else if (axisType == AxisType.POSITIVE_Z)
        {
            double zpos = -zSize / Math.tan(Math.PI/6.0) - 2.0*maxSize;
            cam.SetPosition(0.0, 0.0, zpos);
            cam.SetViewUp(0.0, 1.0, 0.0);
        }

        if (preserveCurrentDistance)
        {
            double[] pos = cam.GetPosition();

            MathUtil.unorm(pos, pos);

            pos[0] *= cameraDistance;
            pos[1] *= cameraDistance;
            pos[2] *= cameraDistance;

            cam.SetPosition(pos);
        }

        mainCanvas.getVTKLock().unlock();

        mainCanvas.resetCameraClippingRange();
        mainCanvas.Render();
    }

    public CameraFrame createCameraFrameInDirectionOfAxis(AxisType axisType, boolean preserveCurrentDistance, File file, int delayMilliseconds)
    {
        CameraFrame result = new CameraFrame();
        result.file = file;
        result.delay = delayMilliseconds;

        double[] bounds = modelManager.getPolyhedralModel().getBoundingBox().getBounds();
        double xSize = Math.abs(bounds[1] - bounds[0]);
        double ySize = Math.abs(bounds[3] - bounds[2]);
        double zSize = Math.abs(bounds[5] - bounds[4]);
        double maxSize = Math.max(Math.max(xSize, ySize), zSize);

        double cameraDistance = getCameraDistance();

        result.focalPoint = new double[] {0.0, 0.0, 0.0};

        if (axisType == AxisType.NEGATIVE_X)
        {
            double xpos = xSize / Math.tan(Math.PI/6.0) + 2.0*maxSize;
            result.position = new double[] {xpos, 0.0, 0.0};
            result.upDirection = new double[] {0.0, 0.0, 1.0};
        }
        else if (axisType == AxisType.POSITIVE_X)
        {
            double xpos = -xSize / Math.tan(Math.PI/6.0) - 2.0*maxSize;
            result.position = new double[] {xpos, 0.0, 0.0};
            result.upDirection = new double[] {0.0, 0.0, 1.0};
        }
        else if (axisType == AxisType.NEGATIVE_Y)
        {
            double ypos = ySize / Math.tan(Math.PI/6.0) + 2.0*maxSize;
            result.position = new double[] {0.0, ypos, 0.0};
            result.upDirection = new double[] {0.0, 0.0, 1.0};
        }
        else if (axisType == AxisType.POSITIVE_Y)
        {
            double ypos = -ySize / Math.tan(Math.PI/6.0) - 2.0*maxSize;
            result.position = new double[] {0.0, ypos, 0.0};
            result.upDirection = new double[] {0.0, 0.0, 1.0};
        }
        else if (axisType == AxisType.NEGATIVE_Z)
        {
            double zpos = zSize / Math.tan(Math.PI/6.0) + 2.0*maxSize;
            result.position = new double[] {0.0, 0.0, zpos};
            result.upDirection = new double[] {0.0, 1.0, 0.0};
        }
        else if (axisType == AxisType.POSITIVE_Z)
        {
            double zpos = -zSize / Math.tan(Math.PI/6.0) - 2.0*maxSize;
            result.position = new double[] {0.0, 0.0, zpos};
            result.upDirection = new double[] {0.0, 1.0, 0.0};
        }

        if (preserveCurrentDistance)
        {
            double[] poshat = new double[3];

            MathUtil.unorm(result.position, poshat);

            result.position[0] = poshat[0] * cameraDistance;
            result.position[1] = poshat[1] * cameraDistance;
            result.position[2] = poshat[2] * cameraDistance;
        }

        return result;
    }

    public void setCameraFrame(CameraFrame frame)
    {
        vtkRenderer ren = mainCanvas.getRenderer();
        if (ren.VisibleActorCount() == 0)
            return;

        mainCanvas.getVTKLock().lock();

        vtkCamera cam = ren.GetActiveCamera();
        cam.SetFocalPoint(frame.focalPoint[0], frame.focalPoint[1], frame.focalPoint[2]);
        cam.SetPosition(frame.position[0], frame.position[1], frame.position[2]);
        cam.SetViewUp(frame.upDirection[0], frame.upDirection[1], frame.upDirection[2]);

        mainCanvas.getVTKLock().unlock();

        mainCanvas.resetCameraClippingRange();
        mainCanvas.Render();
    }

    public void setCameraOrientation(
            double[] position,
            double[] focalPoint,
            double[] upVector,
            double viewAngle)
    {
//        orientationWidget.EnabledOff();
        mainCanvas.getVTKLock().lock();
        vtkCamera cam = mainCanvas.getRenderer().GetActiveCamera();
        cam.SetPosition(position);
        cam.SetFocalPoint(focalPoint);
        cam.SetViewUp(upVector);
        cam.SetViewAngle(viewAngle);
        mainCanvas.getVTKLock().unlock();
        mainCanvas.resetCameraClippingRange();
//        orientationWidget.EnabledOn();
        mainCanvas.Render();
    }

    public void setCameraViewAngle(
            double viewAngle)
    {
        mainCanvas.getVTKLock().lock();
        vtkCamera cam = mainCanvas.getRenderer().GetActiveCamera();
        cam.SetViewAngle(viewAngle);
        mainCanvas.getVTKLock().unlock();
        mainCanvas.resetCameraClippingRange();
        mainCanvas.Render();
    }

    public double getCameraViewAngle()
    {
        return mainCanvas.getRenderer().GetActiveCamera().GetViewAngle();
    }

    public void resetToDefaultCameraViewAngle()
    {
        setCameraViewAngle(30.0);
    }

    public void setProjectionType(ProjectionType projectionType)
    {
        mainCanvas.getVTKLock().lock();
        vtkCamera cam = mainCanvas.getRenderer().GetActiveCamera();
        if (projectionType == ProjectionType.ORTHOGRAPHIC)
            cam.ParallelProjectionOn();
        else
            cam.ParallelProjectionOff();
        mainCanvas.getVTKLock().unlock();
        mainCanvas.resetCameraClippingRange();
        mainCanvas.Render();
    }

    public ProjectionType getProjectionType()
    {
        vtkCamera cam = mainCanvas.getRenderer().GetActiveCamera();
        if (cam.GetParallelProjection() != 0)
            return ProjectionType.ORTHOGRAPHIC;
        else
            return ProjectionType.PERSPECTIVE;
    }

    /**
     * Change the distance to the asteroid by simply scaling the unit vector
     * the points from the center of the asteroid in the direction of the
     * asteroid.
     *
     * @param distance
     */
    public void setCameraDistance(double distance)
    {
        vtkCamera cam = mainCanvas.getRenderer().GetActiveCamera();

        double[] pos = cam.GetPosition();

        MathUtil.unorm(pos, pos);

        pos[0] *= distance;
        pos[1] *= distance;
        pos[2] *= distance;

        mainCanvas.getVTKLock().lock();
        cam.SetPosition(pos);
        mainCanvas.getVTKLock().unlock();
        mainCanvas.resetCameraClippingRange();
        mainCanvas.Render();
    }

    public double getCameraDistance()
    {
        vtkCamera cam = mainCanvas.getRenderer().GetActiveCamera();

        double[] pos = cam.GetPosition();

        return MathUtil.vnorm(pos);
    }

    /**
     * Note viewAngle is a 1-element array which is returned to caller
     * @param position
     * @param cx
     * @param cy
     * @param cz
     * @param viewAngle
     */
    public void getCameraOrientation(
            double[] position,
            double[] cx,
            double[] cy,
            double[] cz,
            double[] viewAngle)
    {
        vtkCamera cam = mainCanvas.getRenderer().GetActiveCamera();

        double[] pos = cam.GetPosition();
        position[0] = pos[0];
        position[1] = pos[1];
        position[2] = pos[2];

        double[] up = cam.GetViewUp();
        cx[0] = up[0];
        cx[1] = up[1];
        cx[2] = up[2];
        MathUtil.vhat(cx, cx);

        double[] fp = cam.GetFocalPoint();
        cz[0] = fp[0] - position[0];
        cz[1] = fp[1] - position[1];
        cz[2] = fp[2] - position[2];
        MathUtil.vhat(cz, cz);

        MathUtil.vcrss(cz, cx, cy);
        MathUtil.vhat(cy, cy);

        viewAngle[0] = cam.GetViewAngle();
    }

    // Gets the current lat/lon (degrees) position of the camera
    public LatLon getCameraLatLon()
    {
        vtkCamera cam = mainCanvas.getRenderer().GetActiveCamera();
        return MathUtil.reclat(cam.GetPosition()).toDegrees();
    }

    // Sets the lat/lon (degrees) position of the camera
    public void setCameraLatLon(LatLon latLon)
    {
        // Get active camera and current distance from origin
        vtkCamera cam = mainCanvas.getRenderer().GetActiveCamera();
        double distance = getCameraDistance();

        // Convert desired Lat/Lon to unit vector and scale to maintain same distance
        double[] pos = MathUtil.latrec(latLon.toRadians());
        MathUtil.unorm(pos, pos);
        pos[0] *= distance;
        pos[1] *= distance;
        pos[2] *= distance;

        // Set the new camera position
        mainCanvas.getVTKLock().lock();
        cam.SetPosition(pos);
        mainCanvas.getVTKLock().unlock();
        mainCanvas.resetCameraClippingRange();
        mainCanvas.Render();
    }

    // Set camera's focal point
    public void setCameraFocalPoint(double[] focalPoint)
    {
        // Obtain lock
        mainCanvas.getVTKLock().lock();
        vtkCamera cam = mainCanvas.getRenderer().GetActiveCamera();
        cam.SetFocalPoint(focalPoint);
        mainCanvas.getVTKLock().unlock();
        mainCanvas.resetCameraClippingRange();
        mainCanvas.Render();
    }
    
    // gets the camera focal point as defined by vtkCamera
    public double[] getCameraFocalPoint() {
      vtkCamera cam = mainCanvas.getRenderer().GetActiveCamera();
      return cam.GetFocalPoint();
    }

    // Sets the camera roll with roll as defined by vtkCamera
    public void setCameraRoll(double angle)
    {
        mainCanvas.getVTKLock().lock();
        vtkCamera cam = mainCanvas.getRenderer().GetActiveCamera();
        cam.SetRoll(angle);
        mainCanvas.getVTKLock().unlock();
        mainCanvas.resetCameraClippingRange();
        mainCanvas.Render();
    }

    // Gets the camera roll with roll as defined by vtkCamera
    public double getCameraRoll()
    {
        return mainCanvas.getRenderer().GetActiveCamera().GetRoll();
    }
    
    // gets the camera position as defined by vtk camera
    public double[] getCameraPosition()
    {
        vtkCamera cam = mainCanvas.getRenderer().GetActiveCamera();
        
        return cam.GetPosition();
    }

    public vtkJoglPanelComponent getRenderWindowPanel()
    {
        return mainCanvas;
    }

    public void propertyChange(PropertyChangeEvent e)
    {
        if (e.getPropertyName().equals(Properties.MODEL_CHANGED))
        {
            this.setProps(modelManager.getProps());
            
            PolyhedralModel sbModel=(PolyhedralModel)modelManager.getModel(ModelNames.SMALL_BODY);
            if (sbModel.isColoringDataAvailable() && sbModel.getColoringIndex()>=0)
            {
                if (!smallBodyColorbar.isVisible())
                    smallBodyColorbar.setVisible(true);
                smallBodyColorbar.setColormap(sbModel.getColormap());
                int index = sbModel.getColoringIndex();
                String title = sbModel.getColoringName(index).trim();
                String units = sbModel.getColoringUnits(index).trim();
                if (units != null && !units.isEmpty())
                {
                	title += " (" + units + ")";
                }
                if (title.length() > 16)
                {
                	title = title.replaceAll("\\s+", "\n");
                }
                smallBodyColorbar.setTitle(title);
                if (mainCanvas.getRenderer().HasViewProp(smallBodyColorbar.getActor())==0)
                    mainCanvas.getRenderer().AddActor(smallBodyColorbar.getActor());
                smallBodyColorbar.getActor().SetNumberOfLabels(sbModel.getColormap().getNumberOfLabels());
            }
            else
                smallBodyColorbar.setVisible(false);

        }
        else
        {
            mainCanvas.Render();
        }
    }

    public void setDefaultInteractorStyleType(InteractorStyleType interactorStyleType)
    {
        if (interactorStyleType == InteractorStyleType.JOYSTICK_CAMERA)
            defaultInteractorStyle = joystickCameraInteractorStyle;
        else
            defaultInteractorStyle = trackballCameraInteractorStyle;

        // Change the interactor now unless it is currently null.
        if (mainCanvas.getRenderWindowInteractor().GetInteractorStyle() != null)
            setInteractorStyleToDefault();
    }

    public InteractorStyleType getDefaultInteractorStyleType()
    {
        if (defaultInteractorStyle == joystickCameraInteractorStyle)
            return InteractorStyleType.JOYSTICK_CAMERA;
        else
            return InteractorStyleType.TRACKBALL_CAMERA;
    }

    public void setInteractorStyleToDefault()
    {
        mainCanvas.setInteractorStyle(defaultInteractorStyle);
  //      if (mirrorFrameOpen)
  //      {
//            mirrorCanvas.setInteractorStyle(defaultInteractorStyle);
  //      }
    }

    public void setInteractorStyleToNone()
    {
        mainCanvas.setInteractorStyle(null);
//        if (mirrorFrameOpen)
//            mirrorCanvas.setInteractorStyle(null);
    }

    public void setLighting(LightingType type)
    {
//        if (type != currentLighting)  // MZ commented out this if statement so mirror canvas lighting starts in sync with main canvas
//        {
            mainCanvas.getRenderer().RemoveAllLights();
//            if (mirrorFrameOpen)
//                mirrorCanvas.getRenderer().RemoveAllLights();
            if (type == LightingType.LIGHT_KIT)
            {
                lightKit.AddLightsToRenderer(mainCanvas.getRenderer());
//                if (mirrorFrameOpen)
//                    lightKit.AddLightsToRenderer(mirrorCanvas.getRenderer());
            }
            else if (type == LightingType.HEADLIGHT)
            {
                mainCanvas.getRenderer().AddLight(headlight);
//                if (mirrorFrameOpen)
//                    mirrorCanvas.getRenderer().AddLight(headlight);
            }
            else
            {
                mainCanvas.getRenderer().AddLight(fixedLight);
//                if (mirrorFrameOpen)
//                    mirrorCanvas.getRenderer().AddLight(fixedLight);
            }
            currentLighting = type;
            if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
                mainCanvas.Render();

//        }
    }

    public LightingType getLighting()
    {
        return currentLighting;
    }

    public void setLightIntensity(double percentage)
    {
        if (percentage != getLightIntensity())
        {
            headlight.SetIntensity(percentage);
            fixedLight.SetIntensity(percentage);
            if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
                mainCanvas.Render();
        }
    }

    /**
     * Get the absolute position of the light in lat/lon/rad where
     * lat and lon are in degress.
     * @return
     */
    public LatLon getFixedLightPosition()
    {
        double[] position = fixedLight.GetPosition();
        return MathUtil.reclat(position).toDegrees();
    }

    /**
     * Set the absolute position of the light in lat/lon/rad.
     * Lat and lon must be in degrees.
     * @param latLon
     */
    public void setFixedLightPosition(LatLon latLon)
    {
        fixedLight.SetPosition(MathUtil.latrec(latLon.toRadians()));
        if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
            mainCanvas.Render();
    }

    /**
     * Rather than setting the absolute position of the light as in the previous
     * function, set the direction of the light source in the body frame. We still need a
     * distance for the light, so simply use a large multiple of the
     * shape model bounding box diagonal.
     * @param dir
     */
    public void setFixedLightDirection(double[] dir)
    {
        dir = dir.clone();
        MathUtil.vhat(dir, dir);
        double bbd = modelManager.getPolyhedralModel().getBoundingBoxDiagonalLength();
        dir[0] *= (1.0e5 * bbd);
        dir[1] *= (1.0e5 * bbd);
        dir[2] *= (1.0e5 * bbd);
        fixedLight.SetPosition(dir);
        if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
            mainCanvas.Render();
    }

    public double getLightIntensity()
    {
        return headlight.GetIntensity();
    }

    public void setShowOrientationAxes(boolean show)
    {
        if (getShowOrientationAxes() != show)
        {
            mainCanvas.getVTKLock().lock();
    //        orientationWidget.SetEnabled(show ? 1 : 0);
    //        if (show)
    //            orientationWidget.SetInteractive(interactiveAxes ? 1 : 0);
            mainCanvas.getVTKLock().unlock();
            if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
                mainCanvas.Render();
        }
    }

    public boolean getShowOrientationAxes()
    {
      //  int value = orientationWidget.GetEnabled();
      //  return value == 1 ? true : false;
    	return false;
    }

    public void setOrientationAxesInteractive(boolean interactive)
    {
        if (getOrientationAxesInteractive() != interactive &&
            getShowOrientationAxes())
        {
            mainCanvas.getVTKLock().lock();
      //      orientationWidget.SetInteractive(interactive ? 1 : 0);
            mainCanvas.getVTKLock().unlock();
            if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
                mainCanvas.Render();
        }
        interactiveAxes = interactive;
    }

    public boolean getOrientationAxesInteractive()
    {
        return interactiveAxes;
    }

 /*   public void setMouseWheelMotionFactor(double factor)
    {
        trackballCameraInteractorStyle.SetMouseWheelMotionFactor(factor);
        joystickCameraInteractorStyle.SetMouseWheelMotionFactor(factor);
    }

    public double getMouseWheelMotionFactor()
    {
        return trackballCameraInteractorStyle.GetMouseWheelMotionFactor();
    }*/

    public int[] getBackgroundColor()
    {
        double[] bg = mainCanvas.getRenderer().GetBackground();
        return new int[]{(int)(255.0*bg[0]), (int)(255.0*bg[1]), (int)(255.0*bg[2])};
    }

    public void setBackgroundColor(int[] color)
    {
        mainCanvas.getRenderer().SetBackground(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
            mainCanvas.Render();
    }

    public void set2DMode(boolean enable)
    {
        modelManager.set2DMode(enable);

        if (enable)
        {
            vtkCamera cam = mainCanvas.getRenderer().GetActiveCamera();
            cam.ParallelProjectionOn();
            setCameraOrientationInDirectionOfAxis(AxisType.NEGATIVE_X, false);
            mainCanvas.getVTKLock().lock();
            cam.SetViewUp(0.0, 1.0, 0.0);
            mainCanvas.getVTKLock().unlock();
            vtkInteractorStyleImage style = new vtkInteractorStyleImage();
            mainCanvas.setInteractorStyle(style);
        }
        else
        {
            mainCanvas.getRenderer().GetActiveCamera().ParallelProjectionOff();
            mainCanvas.resetCamera();
       //     setInteractorStyleToDefault();
        }

        mainCanvas.Render();
    }

    public void setAxesSize(double size)
    {
        this.axesSize = size;
    //    orientationWidget.SetViewport(0.0, 0.0, size, size);
        if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
            mainCanvas.Render();
    }

    public double getAxesSize()
    {
        return axesSize;
    }

    public void setAxesLineWidth(double width)
    {
        vtkProperty property = axes.GetXAxisShaftProperty();
        property.SetLineWidth(width);
        property = axes.GetYAxisShaftProperty();
        property.SetLineWidth(width);
        property = axes.GetZAxisShaftProperty();
        property.SetLineWidth(width);
        if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
            mainCanvas.Render();
    }

    public double getAxesLineWidth()
    {
        vtkProperty property = axes.GetXAxisShaftProperty();
        return property.GetLineWidth();
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
        if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
            mainCanvas.Render();
    }

    public int getAxesLabelFontSize()
    {
        vtkCaptionActor2D caption = axes.GetXAxisCaptionActor2D();
        vtkTextProperty textProperty = caption.GetCaptionTextProperty();
        return textProperty.GetFontSize();
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
        if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
            mainCanvas.Render();
    }

    public int[] getAxesLabelFontColor()
    {
        vtkCaptionActor2D caption = axes.GetXAxisCaptionActor2D();
        vtkTextProperty textProperty = caption.GetCaptionTextProperty();
        double[] c = textProperty.GetColor();
        return new int[]{(int)(255.0*c[0]), (int)(255.0*c[1]), (int)(255.0*c[2])};
    }

    public void setXAxisColor(int[] color)
    {
        vtkProperty property = axes.GetXAxisShaftProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        property = axes.GetXAxisTipProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
            mainCanvas.Render();
    }

    public int[] getXAxisColor()
    {
        vtkProperty property = axes.GetXAxisShaftProperty();
        double[] c = property.GetColor();
        return new int[]{(int)(255.0*c[0]), (int)(255.0*c[1]), (int)(255.0*c[2])};
    }

    public void setYAxisColor(int[] color)
    {
        vtkProperty property = axes.GetYAxisShaftProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        property = axes.GetYAxisTipProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
            mainCanvas.Render();
    }

    public int[] getYAxisColor()
    {
        vtkProperty property = axes.GetYAxisShaftProperty();
        double[] c = property.GetColor();
        return new int[]{(int)(255.0*c[0]), (int)(255.0*c[1]), (int)(255.0*c[2])};
    }

    public void setZAxisColor(int[] color)
    {
        vtkProperty property = axes.GetZAxisShaftProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        property = axes.GetZAxisTipProperty();
        property.SetColor(color[0]/255.0, color[1]/255.0, color[2]/255.0);
        if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
            mainCanvas.Render();
    }

    public int[] getZAxisColor()
    {
        vtkProperty property = axes.GetZAxisShaftProperty();
        double[] c = property.GetColor();
        return new int[]{(int)(255.0*c[0]), (int)(255.0*c[1]), (int)(255.0*c[2])};
    }

    public void setAxesConeLength(double size)
    {
        if (size > 1.0) size = 1.0;
        if (size < 0.0) size = 0.0;
        axes.SetNormalizedTipLength(size, size, size);
        // Change the shaft length also to fill in any space.
        axes.SetNormalizedShaftLength(1.0-size, 1.0-size, 1.0-size);
        if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
            mainCanvas.Render();
    }

    public double getAxesConeLength()
    {
        return axes.GetNormalizedTipLength()[0];
    }

    public void setAxesConeRadius(double size)
    {
        axes.SetConeRadius(size);
        if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
            mainCanvas.Render();
    }

    public double getAxesConeRadius()
    {
        return axes.GetConeRadius();
    }
    
    public int getPanelWidth()
    {
    		return mainCanvas.getComponent().getWidth();
    }
    
    public int getPanelHeight()
    {
    		return mainCanvas.getComponent().getHeight();
    }

    public static void saveToFile(File file, vtkJoglPanelComponent renWin)
    {
        if (file != null)
        {
            try
            {
                // The following line is needed due to some weird threading
                // issue with JOGL when saving out the pixel buffer. Note release
                // needs to be called at the end.
                renWin.getComponent().getContext().makeCurrent();

                renWin.getVTKLock().lock();
                vtkWindowToImageFilter windowToImage = new vtkWindowToImageFilter();
                windowToImage.SetInput(renWin.getRenderWindow());

                String filename = file.getAbsolutePath();
                if (filename.toLowerCase().endsWith("bmp"))
                {
                    vtkBMPWriter writer = new vtkBMPWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.Write();
                }
                else if (filename.toLowerCase().endsWith("jpg") ||
                        filename.toLowerCase().endsWith("jpeg"))
                {
                    vtkJPEGWriter writer = new vtkJPEGWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.Write();
                }
                else if (filename.toLowerCase().endsWith("png"))
                {
                    vtkPNGWriter writer = new vtkPNGWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.Write();
                }
                else if (filename.toLowerCase().endsWith("pnm"))
                {
                    vtkPNMWriter writer = new vtkPNMWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.Write();
                }
                else if (filename.toLowerCase().endsWith("ps"))
                {
                    vtkPostScriptWriter writer = new vtkPostScriptWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.Write();
                }
                else if (filename.toLowerCase().endsWith("tif") ||
                        filename.toLowerCase().endsWith("tiff"))
                {
                    vtkTIFFWriter writer = new vtkTIFFWriter();
                    writer.SetFileName(filename);
                    writer.SetInputConnection(windowToImage.GetOutputPort());
                    writer.SetCompressionToNoCompression();
                    writer.Write();
                }
                renWin.getVTKLock().unlock();
            }
            finally
            {
                renWin.getComponent().getContext().release();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        CameraFrame frame = cameraFrameQueue.peek();
        if (frame != null)
        {
            if (frame.staged && frame.file != null)
            {
                saveToFile(frame.file, mainCanvas);
                cameraFrameQueue.remove();
            }
            else
            {
                setCameraFrame(frame);
                frame.staged = true;
            }

            Timer timer = new Timer(frame.delay, this);
            timer.setRepeats(false);
            timer.start();
        }

    }

    public GenericPolyhedralModel getGenericPolyhedralModel(){
      return (GenericPolyhedralModel) modelManager.getPolyhedralModel();
  }
    
    public void setMouseEnabled(boolean enabled)
    {
    		if (enabled) mainCanvas.mouseOn();
    		else mainCanvas.mouseOff();
    }


    public void setViewPointLatLong()//LatLon viewPoint)
    {
        //System.out.println( (SmallBodyModel) modelManager.getPolyhedralModel());
    }



}


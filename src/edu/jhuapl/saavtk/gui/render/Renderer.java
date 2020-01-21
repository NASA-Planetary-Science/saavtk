package edu.jhuapl.saavtk.gui.render;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.media.opengl.GLContext;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.axes.AxesPanel;
import edu.jhuapl.saavtk.gui.render.camera.Camera;
import edu.jhuapl.saavtk.gui.render.camera.CameraFrame;
import edu.jhuapl.saavtk.gui.render.camera.CameraUtil;
import edu.jhuapl.saavtk.gui.render.camera.CoordinateSystem;
import edu.jhuapl.saavtk.gui.render.camera.StandardCamera;
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
import edu.jhuapl.saavtk.util.SaavtkLODActor;
import vtk.vtkBMPWriter;
import vtk.vtkCamera;
import vtk.vtkCellLocator;
import vtk.vtkCubeAxesActor2D;
import vtk.vtkIdList;
import vtk.vtkInteractorStyleImage;
import vtk.vtkInteractorStyleTrackballCamera;
import vtk.vtkJPEGWriter;
import vtk.vtkLight;
import vtk.vtkLightKit;
import vtk.vtkPNGWriter;
import vtk.vtkPNMWriter;
import vtk.vtkPostScriptWriter;
import vtk.vtkProp;
import vtk.vtkPropCollection;
import vtk.vtkRenderWindow;
import vtk.vtkRenderer;
import vtk.vtkScalarBarActor;
import vtk.vtkTIFFWriter;
import vtk.vtkWindowToImageFilter;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class Renderer extends JPanel implements ActionListener
{
	public enum LightingType
	{
		NONE,
		HEADLIGHT,
		LIGHT_KIT,
		FIXEDLIGHT
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
		PERSPECTIVE
		{
			@Override
			public String toString()
			{
				return "Perspective";
			}
		},
		ORTHOGRAPHIC
		{
			@Override
			public String toString()
			{
				return "Orthographic";
			}
		}
	}
	
	// Constants
	private static final long serialVersionUID = 1L;

	// Ref vars
	private ModelManager refModelManager;

	// GUI vars
	private RenderPanel mainCanvas;
	private RenderToolbar toolbar;

	// State vars
	private Camera camera;

	// VTK vars
	private vtkInteractorStyleTrackballCamera trackballCameraInteractorStyle;
	private vtkLightKit lightKit;
	private vtkLight headlight;
	private vtkLight fixedLight;
	private LightingType currentLighting = LightingType.NONE;

	public static boolean enableLODs = true; // This is temporary to show off the LOD feature, very soon we will replace this with an actual menu
	public boolean showingLODs = false;

	boolean inInteraction = false;

	/**
	 * Constructor
	 */
	public Renderer(final ModelManager aModelManager)
	{
		refModelManager = aModelManager;

		mainCanvas = new RenderPanel();
		mainCanvas.getRenderWindowInteractor().AddObserver("KeyPressEvent", this, "localKeypressHandler");
		
		// Form a CoordinateSystem relative to tmpPolyModel
		PolyhedralModel tmpPolyModel = aModelManager.getPolyhedralModel();
		Vector3D centerVect = tmpPolyModel.getGeometricCenterPoint();
		Vector3D normalVect = tmpPolyModel.getAverageSurfaceNormal();
		CoordinateSystem tmpCoordinateSystem = CameraUtil.formCoordinateSystem(normalVect, centerVect);

		double tmpDistance = tmpPolyModel.getBoundingBoxDiagonalLength() * 2.0;

		camera = new StandardCamera(mainCanvas, tmpCoordinateSystem, tmpDistance);
		toolbar = new RenderToolbar(mainCanvas, camera);

		trackballCameraInteractorStyle = new vtkInteractorStyleTrackballCamera();

		setBackgroundColor(new int[] { 0, 0, 0 });// Preferences.getInstance().getAsIntArray(Preferences.BACKGROUND_COLOR,
																// new int[]{0, 0, 0}));
		initLights();
		setLayout(new BorderLayout());

		add(toolbar, BorderLayout.NORTH);
		add(mainCanvas.getComponent(), BorderLayout.CENTER);
		toolbar.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

		// Setup observers for start/stop interaction events
		mainCanvas.getRenderWindowInteractor().AddObserver("StartInteractionEvent", this, "onStartInteraction");
		mainCanvas.getRenderWindowInteractor().AddObserver("InteractionEvent", this, "duringInteraction");
		mainCanvas.getRenderWindowInteractor().AddObserver("EndInteractionEvent", this, "onEndInteraction");

		javax.swing.SwingUtilities.invokeLater(() -> {
			setProps(aModelManager.getProps());
		});
		
		// Cause the RenderPanel to be rendered whenever the camera changes
		camera.addListener((aEvent) -> { mainCanvas.Render(); }); 
		
		boolean useDepthPeeling = IsDepthPeelingSupported(mainCanvas.getRenderWindow(), mainCanvas.getRenderer(), true);
		System.out.println("Renderer: Renderer: depth peeling enabled = " + useDepthPeeling);
		
        ((GenericPolyhedralModel)tmpPolyModel).sortPolydata(mainCanvas.getActiveCamera());

	}

	/**
	 * Method that is called when this Renderer will no longer be used.
	 */
	public void dispose()
	{
		// Ensure the AxesFrame is hidden
		mainCanvas.setAxesFrameVisible(false);
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
		LatLon defaultPosition =
				new LatLon(Preferences.getInstance().getAsDouble(Preferences.FIXEDLIGHT_LATITUDE, 90.0), Preferences.getInstance().getAsDouble(Preferences.FIXEDLIGHT_LONGITUDE, 0.0), Preferences.getInstance().getAsDouble(Preferences.FIXEDLIGHT_DISTANCE, 1.0e8));
		setFixedLightPosition(defaultPosition);
		setLightIntensity(Preferences.getInstance().getAsDouble(Preferences.LIGHT_INTENSITY, 1.0));

		mainCanvas.getRenderer().AutomaticLightCreationOff();
		lightKit = new vtkLightKit();
		lightKit.SetKeyToFillRatio(1.0);
		lightKit.SetKeyToHeadRatio(20.0);

		LightingType lightingType = LightingType.valueOf(Preferences.getInstance().get(Preferences.LIGHTING_TYPE, LightingType.LIGHT_KIT.toString()));
		setLighting(lightingType);

	}

	List<KeyListener> listeners = Lists.newArrayList();

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
			int shiftDown = mainCanvas.getRenderWindowInteractor().GetShiftKey();
			int altDown = mainCanvas.getRenderWindowInteractor().GetAltKey();
			int ctrlDown = mainCanvas.getRenderWindowInteractor().GetControlKey();
			int modifiers = (KeyEvent.SHIFT_DOWN_MASK * shiftDown) | (KeyEvent.ALT_DOWN_MASK * altDown) | (KeyEvent.CTRL_DOWN_MASK * ctrlDown);
			listener.keyPressed(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers, mainCanvas.getRenderWindowInteractor().GetKeyCode(), mainCanvas.getRenderWindowInteractor().GetKeyCode()));
		}

	}

	/**
	 * Returns the camera associated with this Renderer.
	 */
	public Camera getCamera()
	{
		return camera;
	}

	public void setProps(List<vtkProp> props)
	{
		setProps(props, mainCanvas, mainCanvas.getRenderer());
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
		for (int i = 0; i < size; ++i)
			renderedProps.add((vtkProp) propCollection.GetItemAsObject(i));

		renderedProps.removeAll(props);

		if (!renderedProps.isEmpty())
		{
			renderWindow.getVTKLock().lock();
			for (vtkProp prop : renderedProps)
			{
				if (!(prop instanceof vtkCubeAxesActor2D) && !(prop instanceof vtkScalarBarActor))
					whichRenderer.RemoveViewProp(prop);
			}
			renderWindow.getVTKLock().unlock();
		}

		// Next add the new props.
		for (vtkProp prop : props)
		{
			if (whichRenderer.HasViewProp(prop) == 0)
				whichRenderer.AddViewProp(prop);
		}

		// If we are in 2D mode, then remove all props of models that
		// do not support 2D mode.
		if (refModelManager.is2DMode())
		{
			propCollection = whichRenderer.GetViewProps();
			size = propCollection.GetNumberOfItems();
			for (int i = size - 1; i >= 0; --i)
			{
				vtkProp prop = (vtkProp) propCollection.GetItemAsObject(i);
				Model model = refModelManager.getModel(prop);
				if (model != null && !model.supports2DMode())
				{
					whichRenderer.RemoveViewProp(prop);
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
		if (enableLODs && refModelManager != null && !showingLODs)
		{
			showingLODs = true;
			List<vtkProp> props = refModelManager.getProps();
			for (vtkProp prop : props)
			{
				if (prop instanceof SaavtkLODActor)
				{
					((SaavtkLODActor) prop).showLOD();
				}
			}
		}

	}

	public void hideLODs()
	{
		if (enableLODs && refModelManager != null && showingLODs)
		{
			showingLODs = false;
			List<vtkProp> props = refModelManager.getProps();
			for (vtkProp prop : props)
			{
				if (prop instanceof SaavtkLODActor)
				{
					((SaavtkLODActor) prop).hideLOD();
				}
			}
		}

	}

	public void onEndInteraction()
	{
		hideLODs();
		occludeLabels();
		// See Redmine #1135. This method was added in an attempt to address rendering problems that were caused
		// by clipping range limitations, but it interacted badly with other features, specifically center-in-window,
		// but who knows what else would have been affected. Leaving the code here,
		// but commented out, in case we need to revisit this capability.
		//        updateImageOffsets();
	}

	public void occludeLabels()
	{
		Vector3D lookat = new Vector3D(getRenderWindowPanel().getActiveCamera().GetFocalPoint());
		Vector3D campos = new Vector3D(getRenderWindowPanel().getActiveCamera().GetPosition());
		Vector3D lookdir = lookat.subtract(campos);
		GenericPolyhedralModel model = (GenericPolyhedralModel) refModelManager.getModel(ModelNames.SMALL_BODY);
		vtkCellLocator locator = model.getCellLocator();
		for (vtkProp prop : refModelManager.getProps())
			if (prop instanceof OccludingCaptionActor)
			{
				OccludingCaptionActor caption = (OccludingCaptionActor) prop;
				Vector3D normal = new Vector3D(caption.getNormal());
				if (!caption.isEnabled() || normal.dotProduct(lookdir) > 0)
					prop.VisibilityOff();
				else
				{
					double tolerance = 1e-15;
					vtkIdList ids = new vtkIdList();
					double[] rayStartPoint = caption.getRayStartPoint();
					locator.FindCellsAlongLine(rayStartPoint, campos.toArray(), tolerance, ids);
					if (ids.GetNumberOfIds() > 0)
						prop.VisibilityOff();
					else
						prop.VisibilityOn();
				}

			}

	}

	// See Redmine #1135. This method was added in an attempt to address rendering problems that were caused
	// by clipping range limitations, but it interacted badly with other features, specifically center-in-window,
	// but who knows what else would have been affected. Leaving the code here,
	// but commented out, in case we need to revisit this capability.
	//    public void updateImageOffsets() {
	//    	double oldDistance = this.cameraDistance;
	//    	double newDistance = getCameraDistance();
	//    	this.cameraDistance = newDistance;
	//    	if (newDistance != oldDistance)
	//    	{
	//    		firePropertyChange(CameraProperties.CAMERA_DISTANCE, oldDistance, newDistance);    		
	//    	}
	//    }

	public static File createAxesFile(File rawOutputFile)
	{
		String extension = FilenameUtils.getExtension(rawOutputFile.getName());
		return new File(rawOutputFile.getAbsolutePath().replaceAll("." + extension, ".axes." + extension));
	}

	public void saveToFile()
	{
		getRenderWindowPanel().Render();
		File file = CustomFileChooser.showSaveDialog(this, "Export to PNG Image", "image.png", "png");
		saveToFile(file, mainCanvas, mainCanvas.getAxesPanel());
	}

	private BlockingQueue<CameraFrame> cameraFrameQueue;

	private File[] sixFiles = new File[6];

	AxisType[] sixAxes = { AxisType.POSITIVE_X, AxisType.NEGATIVE_X, AxisType.POSITIVE_Y, AxisType.NEGATIVE_Y, AxisType.POSITIVE_Z, AxisType.NEGATIVE_Z
	};

	public void save6ViewsToFile()
	{
		File file = CustomFileChooser.showSaveDialog(this, "Export to PNG Image", "", "png");
		if (file == null)
			return;
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
				int response = JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(this), "Overwrite file(s)?", "Confirm Overwrite", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

				if (response == JOptionPane.CANCEL_OPTION)
					return;
				else
				{
					break;
				}
			}
		}

		cameraFrameQueue = new LinkedBlockingQueue<CameraFrame>();

		for (int i = 0; i < 6; i++)
		{
			File f = sixFiles[i];
			AxisType at = sixAxes[i];
			CameraFrame frame = createCameraFrameInDirectionOfAxis(at, true, f, 1);
			cameraFrameQueue.add(frame);
		}

		// start off the timer
		this.actionPerformed(null);

	}

	public CameraFrame createCameraFrameInDirectionOfAxis(AxisType axisType, boolean preserveCurrentDistance, File file, int delayMilliseconds)
	{
		CameraFrame result = new CameraFrame();
		result.file = file;
		result.delay = delayMilliseconds;

		double[] bounds = refModelManager.getPolyhedralModel().getBoundingBox().getBounds();
		double xSize = Math.abs(bounds[1] - bounds[0]);
		double ySize = Math.abs(bounds[3] - bounds[2]);
		double zSize = Math.abs(bounds[5] - bounds[4]);
		double maxSize = Math.max(Math.max(xSize, ySize), zSize);

		double cameraDistance = getCameraDistance();

		result.focalPoint = new double[] { 0.0, 0.0, 0.0 };

		if (axisType == AxisType.NEGATIVE_X)
		{
			double xpos = xSize / Math.tan(Math.PI / 6.0) + 2.0 * maxSize;
			result.position = new double[] { xpos, 0.0, 0.0 };
			result.upDirection = new double[] { 0.0, 0.0, 1.0 };
		}
		else if (axisType == AxisType.POSITIVE_X)
		{
			double xpos = -xSize / Math.tan(Math.PI / 6.0) - 2.0 * maxSize;
			result.position = new double[] { xpos, 0.0, 0.0 };
			result.upDirection = new double[] { 0.0, 0.0, 1.0 };
		}
		else if (axisType == AxisType.NEGATIVE_Y)
		{
			double ypos = ySize / Math.tan(Math.PI / 6.0) + 2.0 * maxSize;
			result.position = new double[] { 0.0, ypos, 0.0 };
			result.upDirection = new double[] { 0.0, 0.0, 1.0 };
		}
		else if (axisType == AxisType.POSITIVE_Y)
		{
			double ypos = -ySize / Math.tan(Math.PI / 6.0) - 2.0 * maxSize;
			result.position = new double[] { 0.0, ypos, 0.0 };
			result.upDirection = new double[] { 0.0, 0.0, 1.0 };
		}
		else if (axisType == AxisType.NEGATIVE_Z)
		{
			double zpos = zSize / Math.tan(Math.PI / 6.0) + 2.0 * maxSize;
			result.position = new double[] { 0.0, 0.0, zpos };
			result.upDirection = new double[] { 0.0, 1.0, 0.0 };
		}
		else if (axisType == AxisType.POSITIVE_Z)
		{
			double zpos = -zSize / Math.tan(Math.PI / 6.0) - 2.0 * maxSize;
			result.position = new double[] { 0.0, 0.0, zpos };
			result.upDirection = new double[] { 0.0, 1.0, 0.0 };
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

	public void setCameraOrientation(double[] position, double[] focalPoint, double[] upVector, double viewAngle)
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

	public void setCameraViewAngle(double viewAngle)
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
	 * Change the distance to the asteroid by simply scaling the unit vector the
	 * points from the center of the asteroid in the direction of the asteroid.
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
	 * 
	 * @param position
	 * @param cx
	 * @param cy
	 * @param cz
	 * @param viewAngle
	 */
	public void getCameraOrientation(double[] position, double[] cx, double[] cy, double[] cz, double[] viewAngle)
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
	public double[] getCameraFocalPoint()
	{
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

	public void viewDeactivating()
	{
		mainCanvas.getAxesFrame().setVisible(false);
	}

	public void viewActivating()
	{
		mainCanvas.getAxesFrame().setVisible(toolbar.getOrientationAxesToggleState());
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

	/**
	 * Sets the enable state of the associated Interactor.
	 * <P>
	 * If the Interactor is disabled then it will not respond to mouse / keyboard
	 * events.
	 */
	public void setInteractorEnableState(boolean aBool)
	{
		// Delegate
		mainCanvas.setInteractorEnableState(aBool);
	}

	public void setLighting(LightingType type)
	{
		mainCanvas.getRenderer().RemoveAllLights();
		if (type == LightingType.LIGHT_KIT)
		{
			lightKit.AddLightsToRenderer(mainCanvas.getRenderer());
		}
		else if (type == LightingType.HEADLIGHT)
		{
			mainCanvas.getRenderer().AddLight(headlight);
		}
		else
		{
			mainCanvas.getRenderer().AddLight(fixedLight);
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
	 * Get the absolute position of the light in lat/lon/rad where lat and lon are
	 * in degress.
	 * 
	 * @return
	 */
	public LatLon getFixedLightPosition()
	{
		double[] position = fixedLight.GetPosition();
		return MathUtil.reclat(position).toDegrees();
	}

	/**
	 * Set the absolute position of the light in lat/lon/rad. Lat and lon must be in
	 * degrees.
	 * 
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
	 * function, set the direction of the light source in the body frame. We still
	 * need a distance for the light, so simply use a large multiple of the shape
	 * model bounding box diagonal.
	 * 
	 * @param dir
	 */
	public void setFixedLightDirection(double[] dir)
	{
		dir = dir.clone();
		MathUtil.vhat(dir, dir);
		double bbd = refModelManager.getPolyhedralModel().getBoundingBoxDiagonalLength();
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

	public int[] getBackgroundColor()
	{
		double[] bg = mainCanvas.getRenderer().GetBackground();
		return new int[] { (int) (255.0 * bg[0]), (int) (255.0 * bg[1]), (int) (255.0 * bg[2]) };
	}

	public void setBackgroundColor(int[] color)
	{
		mainCanvas.getRenderer().SetBackground(color[0] / 255.0, color[1] / 255.0, color[2] / 255.0);
		if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
			mainCanvas.Render();
	}

	public void set2DMode(boolean enable)
	{
		refModelManager.set2DMode(enable);

		if (enable)
		{
			vtkCamera cam = mainCanvas.getRenderer().GetActiveCamera();
			cam.ParallelProjectionOn();

			double tmpDistance = refModelManager.getPolyhedralModel().getBoundingBoxDiagonalLength() * 2.0;
			CameraUtil.setOrientationInDirectionOfAxis(camera, AxisType.NEGATIVE_X, tmpDistance);
			
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

	public int getPanelWidth()
	{
		return mainCanvas.getComponent().getWidth();
	}

	public int getPanelHeight()
	{
		return mainCanvas.getComponent().getHeight();
	}

	public static void saveToFile(File file, vtkJoglPanelComponent renWin, AxesPanel axesWin)
	{
		saveToFile(file, renWin);
		if (axesWin != null && ((RenderPanel) renWin).isAxesPanelVisible())
		{
			//axesWin.printModeOn();
			//axesWin.setSize(200, 200);
			RenderPanel renderPanel = (RenderPanel) renWin;
			//boolean visible=renderPanel.axesFrame.isVisible();
			//if (!visible)
			//	renderPanel.axesFrame.setVisible(true);
			saveToFile(createAxesFile(file), axesWin);
			axesWin.Render();
			//if (!visible)
			//	renderPanel.axesFrame.setVisible(false);
			//axesWin.printModeOff();
		}
	}

	protected static void saveToFile(File file, vtkJoglPanelComponent renWin)
	{
		if (file != null)
		{
			GLContext glContext = null;
			try
			{
				glContext = renWin.getComponent().getContext();
				if (glContext != null)
				{
					// The following line is needed due to some weird threading
					// issue with JOGL when saving out the pixel buffer. Note release
					// needs to be called at the end.
					glContext.makeCurrent();
				}

				renWin.getVTKLock().lock();
				vtkWindowToImageFilter windowToImage = new vtkWindowToImageFilter();
				windowToImage.SetInput(renWin.getRenderWindow());
				windowToImage.ShouldRerenderOn();

				String filename = file.getAbsolutePath();
				if (filename.toLowerCase().endsWith("bmp"))
				{
					vtkBMPWriter writer = new vtkBMPWriter();
					writer.SetFileName(filename);
					writer.SetInputConnection(windowToImage.GetOutputPort());
					writer.Write();
				}
				else if (filename.toLowerCase().endsWith("jpg") || filename.toLowerCase().endsWith("jpeg"))
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
				else if (filename.toLowerCase().endsWith("tif") || filename.toLowerCase().endsWith("tiff"))
				{
					vtkTIFFWriter writer = new vtkTIFFWriter();
					writer.SetFileName(filename);
					writer.SetInputConnection(windowToImage.GetOutputPort());
					writer.SetCompressionToNoCompression();
					writer.Write();
				}
				renWin.getVTKLock().unlock();
			}
			catch (Exception e)
			{
				System.out.println(e);
			}
			finally
			{
				if (glContext != null)
				{
					glContext.release();
				}
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
				saveToFile(frame.file, mainCanvas, mainCanvas.getAxesPanel());
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

	public GenericPolyhedralModel getGenericPolyhedralModel()
	{
		return (GenericPolyhedralModel) refModelManager.getPolyhedralModel();
	}

	public void setMouseEnabled(boolean enabled)
	{
		if (enabled)
			mainCanvas.mouseOn();
		else
			mainCanvas.mouseOff();
	}
	
	
	
	
	
	/**
	 * Find out whether this box supports depth peeling. Depth peeling requires
	 * a variety of openGL extensions and appropriate drivers.
	 * @param renderWindow a valid openGL-supporting render window
	 * @param renderer a valid renderer instance
	 * @param doItOffscreen do the test off screen which means that nothing is
	 * rendered to screen (this requires the box to support off screen rendering)
	 * @return TRUE if depth peeling is supported, FALSE otherwise (which means
	 * that another strategy must be used for correct rendering of translucent
	 * geometry, e.g. CPU-based depth sorting)
	 */
	public boolean IsDepthPeelingSupported(vtkRenderWindow renderWindow,
	                             vtkRenderer renderer,
	                             boolean doItOffScreen)
	{
	  if (renderWindow == null|| renderer == null)
	  {
	    return false;
	  }

	  boolean success = true;

	  // Save original renderer / render window state
	  boolean origOffScreenRendering = renderWindow.GetOffScreenRendering() == 1;
	  boolean origAlphaBitPlanes = renderWindow.GetAlphaBitPlanes() == 1;
	  int origMultiSamples = renderWindow.GetMultiSamples();
	  boolean origUseDepthPeeling = renderer.GetUseDepthPeeling() == 1;
	  int origMaxPeels = renderer.GetMaximumNumberOfPeels();
	  double origOcclusionRatio = renderer.GetOcclusionRatio();

	  // Activate off screen rendering on demand
	  renderWindow.SetOffScreenRendering(doItOffScreen == true ? 1 : 0);

	  // Setup environment for depth peeling (with some default parametrization)
	  success = success && SetupEnvironmentForDepthPeeling(renderWindow, renderer,
	                                                       100, 0.1);

	  // Do a test render
	  renderWindow.Render();

	  // Check whether depth peeling was used
	  success = success && (renderer.GetLastRenderingUsedDepthPeeling() == 1 ? true : false);

	  // recover original state
	  renderWindow.SetOffScreenRendering(origOffScreenRendering == true ? 1 : 0);
	  renderWindow.SetAlphaBitPlanes(origAlphaBitPlanes == true ? 1 : 0);
	  renderWindow.SetMultiSamples(origMultiSamples);
	  renderer.SetUseDepthPeeling(origUseDepthPeeling == true ? 1 : 0);
	  renderer.SetMaximumNumberOfPeels(origMaxPeels);
	  renderer.SetOcclusionRatio(origOcclusionRatio);

	  return success;
	}
	
	/**
	 * Setup the rendering environment for depth peeling (general depth peeling
	 * support is requested).
	 * @see IsDepthPeelingSupported()
	 * @param renderWindow a valid openGL-supporting render window
	 * @param renderer a valid renderer instance
	 * @param maxNoOfPeels maximum number of depth peels (multi-pass rendering)
	 * @param occulusionRation the occlusion ration (0.0 means a perfect image,
	 * >0.0 means a non-perfect image which in general results in faster rendering)
	 * @return TRUE if depth peeling could be set up
	 */
	boolean SetupEnvironmentForDepthPeeling(
	  vtkRenderWindow renderWindow,
	  vtkRenderer renderer, int maxNoOfPeels,
	  double occlusionRatio)
	{
		if (renderWindow == null|| renderer == null)
	    return false;

	  // 1. Use a render window with alpha bits (as initial value is 0 (false)):
	  renderWindow.SetAlphaBitPlanes(1);

	  // 2. Force to not pick a framebuffer with a multisample buffer
	  // (as initial value is 8):
	  renderWindow.SetMultiSamples(0);

	  // 3. Choose to use depth peeling (if supported) (initial value is 0 (false)):
	  renderer.SetUseDepthPeeling(1);

	  // 4. Set depth peeling parameters
	  // - Set the maximum number of rendering passes (initial value is 4):
	  renderer.SetMaximumNumberOfPeels(maxNoOfPeels);
	  // - Set the occlusion ratio (initial value is 0.0, exact image):
	  renderer.SetOcclusionRatio(occlusionRatio);

	  return true;
	}


}

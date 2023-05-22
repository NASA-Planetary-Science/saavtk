package edu.jhuapl.saavtk.gui.render;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.camera.Camera;
import edu.jhuapl.saavtk.camera.CameraActionListener;
import edu.jhuapl.saavtk.camera.CameraFrame;
import edu.jhuapl.saavtk.camera.CameraUtil;
import edu.jhuapl.saavtk.camera.CoordinateSystem;
import edu.jhuapl.saavtk.camera.StandardCamera;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.toolbar.RenderToolbar;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.OccludingCaptionActor;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.pick.PickUtilEx;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.view.View;
import edu.jhuapl.saavtk.view.ViewActionListener;
import edu.jhuapl.saavtk.view.ViewChangeReason;
import edu.jhuapl.saavtk.view.light.LightCfg;
import edu.jhuapl.saavtk.view.light.LightingType;
import edu.jhuapl.saavtk.view.lod.LodActor;
import edu.jhuapl.saavtk.view.lod.LodMode;
import vtk.vtkActor;
import vtk.vtkCamera;
import vtk.vtkCaptionActor2D;
import vtk.vtkCellLocator;
import vtk.vtkCellPicker;
import vtk.vtkCubeAxesActor2D;
import vtk.vtkIdList;
import vtk.vtkInteractorStyleImage;
import vtk.vtkInteractorStyleTrackballCamera;
import vtk.vtkLight;
import vtk.vtkLightKit;
import vtk.vtkProp;
import vtk.vtkPropCollection;
import vtk.vtkRenderer;
import vtk.vtkTextActor;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class Renderer extends JPanel implements ActionListener, CameraActionListener, PickListener, SceneChangeNotifier, View
{
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

	// Constants
	private static final long serialVersionUID = 1L;

	// Ref vars
	private final PolyhedralModel refSmallBody;

	// State vars
	private final Set<VtkPropProvider> propProviderS;
	private final List<ViewActionListener> viewActionListenerL;
	private final Camera camera;

	private LodMode lodModeRegular;
	private LodMode lodModeTemporal;
	private double nominalPixelSpan;
	private boolean isMode2D;

	// Cache vars
	private LightCfg cLightCfg;
	private LodMode cLodModeInstant;

	// GUI vars
	private RenderPanel mainCanvas;
	private RenderToolbar toolbar;

	// VTK vars
	private final vtkInteractorStyleTrackballCamera trackballCameraInteractorStyle;
	private final vtkCellPicker vSmallBodyCP;
	private vtkLightKit lightKit;
	private vtkLight headlight;
	private vtkLight fixedLight;

	/**
	 * Standard Constructor
	 *
	 * @param aSmallBody The primary {@link PolyhedralModel} associated with this
	 *                   Renderer.
	 */
	public Renderer(PolyhedralModel aSmallBody)
	{
		refSmallBody = aSmallBody;

		propProviderS = new LinkedHashSet<>();
		viewActionListenerL = new ArrayList<>();

		lodModeRegular = LodMode.Auto;
		lodModeTemporal = LodMode.MaxQuality;
		nominalPixelSpan = Double.NaN;
		isMode2D = false;

		cLightCfg = LightCfg.Invalid;
		cLodModeInstant = null;

		mainCanvas = new RenderPanel();
		mainCanvas.getRenderWindowInteractor().AddObserver("KeyPressEvent", this, "localKeypressHandler");

		camera = formCamera(refSmallBody, mainCanvas);
		toolbar = new RenderToolbar(mainCanvas, camera);

		trackballCameraInteractorStyle = new vtkInteractorStyleTrackballCamera();
		trackballCameraInteractorStyle.AutoAdjustCameraClippingRangeOn();
		vSmallBodyCP = PickUtilEx.formSmallBodyPicker(refSmallBody);
		
		setBackgroundColor(new int[] { 0, 0, 0 });// Preferences.getInstance().getAsIntArray(Preferences.BACKGROUND_COLOR,
																// new int[]{0, 0, 0}));
		initVtkLights();
		setLayout(new BorderLayout());

		add(toolbar, BorderLayout.NORTH);
		add(mainCanvas.getComponent(), BorderLayout.CENTER);
		toolbar.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

		// Setup observers for start/stop interaction events
		mainCanvas.getRenderWindowInteractor().AddObserver("StartInteractionEvent", this, "onStartInteraction");
		mainCanvas.getRenderWindowInteractor().AddObserver("InteractionEvent", this, "duringInteraction");
		mainCanvas.getRenderWindowInteractor().AddObserver("EndInteractionEvent", this, "onEndInteraction");

		SwingUtilities.invokeLater(() -> notifySceneChange());
		
//		((GenericPolyhedralModel) refSmallBody).sortPolydata(mainCanvas.getActiveCamera());

		// Register for events of interest
		camera.addCameraChangeListener(this);
				
		mainCanvas.getComponent().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent aEvent)
			{
				// Delegate
				doViewChange(ViewChangeReason.Camera);
			}
		});
	}

	/**
	 * Registers a {@link VtkPropProvider} with this Renderer.
	 */
	public void addVtkPropProvider(VtkPropProvider aPropProvider)
	{
		boolean tmpBool = propProviderS.add(aPropProvider);
		if (tmpBool == false)
			return;

		notifySceneChange();
	}

	/**
	 * Returns true if the {@link VtkPropProvider} is registered with this Renderer.
	 */
	public boolean hasVtkPropProvider(VtkPropProvider aPropProvider)
	{
		return propProviderS.contains(aPropProvider);
	}

	/**
	 * Deregisters a {@link VtkPropProvider} with this Renderer.
	 */
	public void delVtkPropProvider(VtkPropProvider aPropProvider)
	{
		boolean tmpBool = propProviderS.remove(aPropProvider);
		if (tmpBool == false)
			return;

		notifySceneChange();
	}

	/**
	 * Method that is called when this Renderer will no longer be used.
	 */
	public void dispose()
	{
		// Ensure the AxesFrame is hidden
		mainCanvas.setAxesFrameVisible(false);
	}

	/**
	 * Returns the nominal pixel span. Pixel span defines the physical distance an
	 * individual pixel spans. The returned value is in kilometers.
	 * <P>
	 * The nominal pixel span is defined as the average pixel span for the 4 corners
	 * of the (current) view.
	 * <P>
	 * Note if all 4 corners are not in the view, then NaN will be returned.
	 */
	public double getNominalPixelSpan()
	{
		return nominalPixelSpan;
	}

	List<KeyListener> listeners = Lists.newArrayList();

	@Override
	public synchronized void addKeyListener(KeyListener l)
	{
		listeners.add(l);
	}

	// this prioritizes key presses from the main canvas and if none exist then it
	// handles any key presses from the mirror canvas
	void localKeypressHandler()
	// TODO: clean up logic here
	{
		for (KeyListener listener : listeners)
		{
			int shiftDown = mainCanvas.getRenderWindowInteractor().GetShiftKey();
			int altDown = mainCanvas.getRenderWindowInteractor().GetAltKey();
			int ctrlDown = mainCanvas.getRenderWindowInteractor().GetControlKey();
			int modifiers = (KeyEvent.SHIFT_DOWN_MASK * shiftDown) | (KeyEvent.ALT_DOWN_MASK * altDown)
					| (KeyEvent.CTRL_DOWN_MASK * ctrlDown);
			listener.keyPressed(new KeyEvent(this, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), modifiers,
					mainCanvas.getRenderWindowInteractor().GetKeyCode(),
					mainCanvas.getRenderWindowInteractor().GetKeyCode()));
		}

	}

	public void onStartInteraction()
	{
		occludeLabels();
	}

	public void duringInteraction()
	{
		setLodModeTemporal(LodMode.MaxSpeed);
		occludeLabels();
	}

	public void onEndInteraction()
	{
		setLodModeTemporal(null);
		occludeLabels();
	}
	
	public List<vtkProp> getAllActors()
	{
		List<vtkProp> actors = Lists.newArrayList();
		for (VtkPropProvider aPropProvider : propProviderS)
		{
			for (vtkProp aProp : aPropProvider.getProps())
			{
				if (!(aProp instanceof vtkCaptionActor2D) && !(aProp instanceof vtkTextActor))
					actors.add(aProp);
			}
		}
		return actors;
	}

	private BlockingQueue<CameraFrame> cameraFrameQueue;

	public void save6ViewsToFile()
	{
		File file = CustomFileChooser.showSaveDialog(this, "Export to PNG Image", "", "png");
		if (file == null)
			return;
		String path = file.getAbsolutePath();
		String base = path.substring(0, path.lastIndexOf('.'));
		String ext = path.substring(path.lastIndexOf('.'));

		File[] sixFiles = new File[6];
		sixFiles[0] = new File(base + "+x" + ext);
		sixFiles[1] = new File(base + "-x" + ext);
		sixFiles[2] = new File(base + "+y" + ext);
		sixFiles[3] = new File(base + "-y" + ext);
		sixFiles[4] = new File(base + "+z" + ext);
		sixFiles[5] = new File(base + "-z" + ext);

		AxisType[] sixAxes = { AxisType.POSITIVE_X, AxisType.NEGATIVE_X, AxisType.POSITIVE_Y, AxisType.NEGATIVE_Y,
				AxisType.POSITIVE_Z, AxisType.NEGATIVE_Z };
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
				int response = JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(this), "Overwrite file(s)?",
						"Confirm Overwrite", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

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
			CameraFrame frame = RenderIoUtil.createCameraFrameInDirectionOfAxis(refSmallBody, mainCanvas, at, true, f, 1);
			cameraFrameQueue.add(frame);
		}

		// start off the timer
		this.actionPerformed(null);
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
		cam.OrthogonalizeViewUp();
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
		cam.OrthogonalizeViewUp();
		mainCanvas.getVTKLock().unlock();
		mainCanvas.resetCameraClippingRange();
		//        orientationWidget.EnabledOn();
		mainCanvas.Render();
	}

	public double getCameraViewAngle()
	{
		// Delegate
		return camera.getViewAngle();
	}

	public void setCameraViewAngle(double viewAngle)
	{
		// Delegate
		camera.setViewAngle(viewAngle);
	}

	// gets the camera focal point as defined by vtkCamera
	public double[] getCameraFocalPoint()
	{
		// Delegate
		return camera.getFocalPoint().toArray();
	}

	// Set camera's focal point
	public void setCameraFocalPoint(double[] aFocalPointArr)
	{
		// Delegate
		camera.setFocalPoint(new Vector3D(aFocalPointArr));
	}
	
	public void viewDeactivating()
	{
		mainCanvas.getAxesFrame().setVisible(false);
	}

	public void viewActivating()
	{
		mainCanvas.getAxesFrame().setVisible(toolbar.getOrientationAxesToggleState());
	}

	public RenderPanel getRenderWindowPanel()
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

	/**
	 * Gets the Renderer's {@link LightCfg}.
	 */
	public LightCfg getLightCfg()
	{
		return cLightCfg;
		}

	/**
	 * Sets the Renderer's {@link LightCfg}.
	 */
	public void setLightCfg(LightCfg aLightCfg)
		{
		// Bail if configuration has not changed
		if (cLightCfg.equals(aLightCfg) == true)
			return;

		// Update the intensity
		double intensity = aLightCfg.getIntensity();
		boolean isValidIntensity = true;
		isValidIntensity &= Double.isNaN(intensity) == false;
		isValidIntensity &= intensity >= 0.0 && intensity <= 1.0;
		if (isValidIntensity == true)
		{
			headlight.SetIntensity(intensity);
			fixedLight.SetIntensity(intensity);
	}

		// Update (fixed) position
		LatLon positionLL = aLightCfg.getPositionLL();
		boolean isValidPosition = true;
		isValidPosition &= aLightCfg.getType() == LightingType.FIXEDLIGHT;
		isValidPosition &= Double.isNaN(positionLL.lat) == false && Double.isNaN(positionLL.lon) == false;
		isValidPosition &= Double.isNaN(positionLL.rad) == false;
		if (isValidPosition == true)
			fixedLight.SetPosition(MathUtil.latrec(positionLL));

		// Update the light source
		LightingType tmpType = aLightCfg.getType();
		mainCanvas.getRenderer().RemoveAllLights();
		if (tmpType == LightingType.FIXEDLIGHT && isValidIntensity == true && isValidPosition == true)
			mainCanvas.getRenderer().AddLight(fixedLight);
		else if (tmpType == LightingType.HEADLIGHT && isValidIntensity == true)
			mainCanvas.getRenderer().AddLight(headlight);
		else if (tmpType != LightingType.NONE)
			lightKit.AddLightsToRenderer(mainCanvas.getRenderer());

		// Update our cache copy
		cLightCfg = aLightCfg;

		// Render the scene
		if (mainCanvas.getRenderWindow().GetNeverRendered() == 0)
			mainCanvas.Render();

		// Sends out notification to the ViewChangeListeners
		for (ViewActionListener aListener : viewActionListenerL)
			aListener.handleViewAction(this, ViewChangeReason.Light);
	}

	/**
	 * Method to switch to the fixed light and set the direction of the light source
	 * in the body frame.
	 * <p>
	 * For the distance we utilize a large multiple of the shape model bounding box
	 * diagonal.
	 */
	public void setLightCfgToFixedLightAtDirection(Vector3D aDirection)
	{
		double[] dir = aDirection.toArray();
		MathUtil.vhat(dir, dir);
		double bbd = refSmallBody.getBoundingBoxDiagonalLength();
		dir[0] *= (1.0e5 * bbd);
		dir[1] *= (1.0e5 * bbd);
		dir[2] *= (1.0e5 * bbd);

		// Delegate updating the light configuration
		LatLon positionLL = MathUtil.reclat(dir);
		LightCfg tmpLightCfg = new LightCfg(LightingType.FIXEDLIGHT, positionLL, cLightCfg.getIntensity());
		setLightCfg(tmpLightCfg);
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

	public void set2DMode(boolean aBool)
	{
		isMode2D = aBool;

		if (isMode2D == true)
		{
			vtkCamera cam = mainCanvas.getRenderer().GetActiveCamera();
			cam.ParallelProjectionOn();

			double tmpDistance = refSmallBody.getBoundingBoxDiagonalLength() * 2.0;
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

	public void setMouseEnabled(boolean enabled)
	{
		if (enabled)
			mainCanvas.mouseOn();
		else
			mainCanvas.mouseOff();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		CameraFrame frame = cameraFrameQueue.peek();
		if (frame != null)
		{
			if (frame.staged && frame.file != null)
			{
				RenderIoUtil.saveToFile(frame.file, mainCanvas, mainCanvas.getAxesPanel());
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

	@Override
	public void addViewChangeListener(ViewActionListener aListener)
	{
		viewActionListenerL.add(aListener);
	}

	@Override
	public void delViewChangeListener(ViewActionListener aListener)
	{
		viewActionListenerL.remove(aListener);
	}

	@Override
	public Camera getCamera()
	{
		return camera;
	}

	@Override
	public LodMode getLodMode()
	{
		return lodModeRegular;
	}

	@Override
	public void handleCameraAction(Object aSource)
	{
		// Cause the RenderPanel to be rendered whenever the camera changes
		mainCanvas.Render();

		doViewChange(ViewChangeReason.Camera);
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Bail if this is just a mouse move event
		if (aEvent instanceof MouseEvent)
		{
			MouseEvent tmpME = (MouseEvent) aEvent;
			if (tmpME.getID() == MouseEvent.MOUSE_MOVED)
				return;
		}

		// Delegate the processing (after all other awt events)
		SwingUtilities.invokeLater(() -> doViewChange(ViewChangeReason.Other));
	}

	@Override
	public void notifySceneChange()
	{
		updateUtilizedVtkProps();

		if (mainCanvas.getRenderWindow().GetNeverRendered() > 0)
			return;
		mainCanvas.Render();
	}

	@Override
	public void setLodMode(LodMode aLodMode)
	{
		lodModeRegular = aLodMode;

		updateLodActors();
	}

	@Override
	public void setLodModeTemporal(LodMode aLodMode)
		{
		lodModeTemporal = aLodMode;

		updateLodActors();
		}

	/**
	 * Helper method that calculates the nominal pixel span associated with the
	 * current view.
	 * <P>
	 * Computes the size of a pixel in body fixed coordinates. This is only
	 * meaningful when the user is zoomed in a lot. To compute a result all 4
	 * corners of the view window must intersect the asteroid.
	 * <P>
	 * This basis of this method originated from the picker package (DefaultPicker
	 * class). This methods functionality is logically associated with the
	 * {@link Renderer} / {@link RenderPanel} rather than the picker package.
	 * <P>
	 * See: {@link #getNominalPixelSpan()}
	 */
	private double computeNominalPixelSpan(vtkCellPicker aSmallBodyCP)
	{
		// Do a pick at each of the 4 corners of the renderer
		int width = mainCanvas.getComponent().getWidth();
		int height = mainCanvas.getComponent().getHeight();

		int[][] corners = { { 0, 0 }, { width - 1, 0 }, { width - 1, height - 1 }, { 0, height - 1 } };
		double[][] points = new double[4][3];
		for (int i = 0; i < 4; ++i)
		{
			// Bail if any corner can not be picked
			boolean isPicked = PickUtil.isPicked(aSmallBodyCP, mainCanvas, corners[i][0], corners[i][1], 0.0);
			if (isPicked == false)
				return Double.NaN;

			points[i] = aSmallBodyCP.GetPickPosition();
		}

		// Computation is based on the average distance of all 4 sides
		double bottom = MathUtil.distanceBetweenFast(points[0], points[1]);
		double right = MathUtil.distanceBetweenFast(points[1], points[2]);
		double top = MathUtil.distanceBetweenFast(points[2], points[3]);
		double left = MathUtil.distanceBetweenFast(points[3], points[0]);

		double pixelSpan = (bottom / (width - 1) + right / (height - 1) + top / (width - 1) + left / (height - 1)) / 4.0;
		return pixelSpan;
	}

	/**
	 * Helper method that will process a view changed
	 */
	private void doViewChange(ViewChangeReason aReason)
	{
		// Update the nominalPixelSpan
		nominalPixelSpan = computeNominalPixelSpan(vSmallBodyCP);

		// Sends out notification to the ViewChangeListeners
		for (ViewActionListener aListener : viewActionListenerL)
			aListener.handleViewAction(this, aReason);
	}

	/**
	 * Helper method that forms the {@link Camera} to be associated with this
	 * Renderer. The camera is instantiated at construction time.
	 */
	private Camera formCamera(PolyhedralModel aPolyModel, vtkJoglPanelComponent aMainCanvas)
	{
		// Form a CoordinateSystem relative to tmpPolyModel
		Vector3D centerVect = aPolyModel.getGeometricCenterPoint();
		Vector3D normalVect = aPolyModel.getAverageSurfaceNormal();
		CoordinateSystem tmpCoordinateSystem = CameraUtil.formCoordinateSystem(normalVect, centerVect);

		double tmpDistance = aPolyModel.getBoundingBoxDiagonalLength() * 2.0;

		return new StandardCamera(aMainCanvas, tmpCoordinateSystem, tmpDistance);
	}

	/**
	 * Helper method to initialize the various light sources available to this
	 * {@link Renderer}.
	 */
	private void initVtkLights()
	{
		fixedLight = mainCanvas.getRenderer().MakeLight();
		fixedLight.SetLightTypeToSceneLight();
		fixedLight.PositionalOn();
		fixedLight.SetConeAngle(180.0);

		headlight = mainCanvas.getRenderer().MakeLight();
		headlight.SetLightTypeToHeadlight();
		headlight.SetConeAngle(180.0);

		mainCanvas.getRenderer().AutomaticLightCreationOff();
		lightKit = new vtkLightKit();
		lightKit.SetKeyToFillRatio(1.0);
		lightKit.SetKeyToHeadRatio(20.0);
	}

	/**
	 * Helper method that updates the visibility of {@link vtkProp}s of type
	 * {@link OccludingCaptionActor} to reflect their "line of sight" state.
	 */
	private void occludeLabels()
	{
		Vector3D lookat = new Vector3D(getRenderWindowPanel().getActiveCamera().GetFocalPoint());
		Vector3D campos = new Vector3D(getRenderWindowPanel().getActiveCamera().GetPosition());
		Vector3D lookdir = lookat.subtract(campos);

		vtkCellLocator tmpLocator = refSmallBody.getCellLocator();
		for (VtkPropProvider aPropProvider : propProviderS)
		{
			for (vtkProp aProp : aPropProvider.getProps())
			{
				if (aProp instanceof OccludingCaptionActor)
				{
					OccludingCaptionActor caption = (OccludingCaptionActor) aProp;
					Vector3D normal = new Vector3D(caption.getNormal());
					if (!caption.isEnabled() || normal.dotProduct(lookdir) > 0)
						aProp.VisibilityOff();
					else
					{
						double tolerance = 1e-15;
						vtkIdList ids = new vtkIdList();
						double[] rayStartPoint = caption.getRayStartPoint();
						tmpLocator.FindCellsAlongLine(rayStartPoint, campos.toArray(), tolerance, ids);
						if (ids.GetNumberOfIds() > 0)
							aProp.VisibilityOff();
						else
							aProp.VisibilityOn();
					}

				}
			}
		}
	}

	/**
	 * Helper method to update all {@link LodActor}s to reflect the proper
	 * {@link LodMode}.
	 */
	private void updateLodActors()
	{
		// Determine the LodMode to utilize. The temporal LodMode is utilized if:
		// - The regular LodMode is set to Auto
		// - If the temporal LodMode is null then default to highest quality
		LodMode tmpLodModeInstant = lodModeRegular;
		if (lodModeRegular == LodMode.Auto)
		{
			tmpLodModeInstant = lodModeTemporal;
			if (tmpLodModeInstant == null)
				tmpLodModeInstant = LodMode.MaxQuality;
		}

		// Bail if the instantaneous LodMode has not changed
		if (tmpLodModeInstant == cLodModeInstant)
			return;
		cLodModeInstant = tmpLodModeInstant;

		// Update the LodMode for all LodActors
		for (VtkPropProvider aPropProvider : propProviderS)
		{
			for (vtkProp aProp : aPropProvider.getProps())
			{
				if (aProp instanceof LodActor)
					((LodActor) aProp).setLodMode(tmpLodModeInstant);
			}
		}

		// Update the scene
		notifySceneChange();

		// Send out notification of the change
		doViewChange(ViewChangeReason.Lod);
	}

	/**
	 * Helper method that updates the VTK state to reflect the available vtkProps.
	 */
	private void updateUtilizedVtkProps()
	{
		// Generate the full list of vtkProps that should be installed
		List<vtkProp> fullPropL = new ArrayList<>();
		for (VtkPropProvider aPropProvider : propProviderS)
			fullPropL.addAll(aPropProvider.getProps());

		// Form the set of stale vtkProps that are installed but should be removed
		vtkRenderer tmpRenderer = mainCanvas.getRenderer();
		vtkPropCollection propCollection = tmpRenderer.GetViewProps();
		int size = propCollection.GetNumberOfItems();
		HashSet<vtkProp> stalePropS = new HashSet<>();
		for (int i = 0; i < size; ++i)
			stalePropS.add((vtkProp) propCollection.GetItemAsObject(i));

		stalePropS.removeAll(fullPropL);

		// Remove the stalePropS
		if (stalePropS.isEmpty() == false)
		{
			mainCanvas.getVTKLock().lock();
			for (vtkProp aProp : stalePropS)
		{
				// TODO: Eventually remove the legacy code below (2 lines)
				if (aProp instanceof vtkCubeAxesActor2D)
					continue;

				tmpRenderer.RemoveViewProp(aProp);
			}
			mainCanvas.getVTKLock().unlock();
		}

		// Install the available vtkProps (that are not already installed)
		for (vtkProp prop : fullPropL)
		{
			if (tmpRenderer.HasViewProp(prop) == 0)
				tmpRenderer.AddViewProp(prop);
		}

		// Delegate label occlude computations
		occludeLabels();
	}
	
//	@Override
//	public Object clone() throws CloneNotSupportedException
//	{
//		// TODO Auto-generated method stub
//		return super.clone();
//	}

}

package edu.jhuapl.saavtk.gui.render;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.MainWindow;
import edu.jhuapl.saavtk.gui.render.axes.AxesPanel;
import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkNativeLibrary;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkRenderer;
import vtk.rendering.vtkEventInterceptor;
import vtk.rendering.vtkInteractorForwarder;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class RenderPanel extends vtkJoglPanelComponent implements ComponentListener
{
	// GUI vars
	private JFrame axesFrame;
	private AxesPanel axesPanel;
	private CustomInteractorStyle interactorStyle;

	// State vars
	private boolean showAxesPanelOnRestore = false;
	private boolean axesPanelShownBefore = false;

	// VTK vars
	// vtkRenderer axesRenderer=new vtkRenderer();
	vtkRenderer propRenderer;

	// Axes axes=new Axes();

	/**
	 * Returns the JFrame associated with the Axes window.
	 */
	public JFrame getAxesFrame()
	{
		return axesFrame;
	}

	public AxesPanel getAxesPanel()
	{
		return axesPanel;
	}

	public boolean isAxesPanelVisible()
	{
		return axesFrame.isVisible();
	}

	public void setAxesFrameVisible(boolean aBool)
	{
		axesFrame.setVisible(aBool);
	}

	public RenderPanel()
	{
		getInteractorForwarder().setEventInterceptor(new Interceptor());

		interactorStyle = new CustomInteractorStyle(getRenderWindowInteractor());
		windowInteractor.SetInteractorStyle(interactorStyle);

		propRenderer = getRenderer();

		axesPanel = new AxesPanel(this);

		// Set up the axesFrame
		axesFrame = new JFrame() {
			@Override
			public void setVisible(boolean b)
			{
				super.setVisible(b);
				if (!axesPanelShownBefore && MainWindow.getMainWindow() != null && isWindowCreated)
				{
					setUpMainWindowListeners();
					Point point = RenderPanel.this.getComponent().getLocationOnScreen();
					Dimension dim = RenderPanel.this.getComponent().getSize();
					int size = (int) Math.max(dim.width / 5., dim.height / 5);
					axesPanel.setSize(size, size);
					axesFrame.setLocation(point.x, point.y + dim.height - size); // lower
					axesPanelShownBefore = true;
				}

			}
		};

		// axesFrame.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
		axesFrame.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		axesFrame.add(axesPanel.getComponent());
		axesFrame.setVisible(false);
		axesFrame.setAlwaysOnTop(true);

		// frame.setUndecorated(true);

		axesFrame.getRootPane().setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

		/*
		 * axesPanel.getComponent().addMouseListener(new MouseAdapter() {
		 * 
		 * @Override public void mouseEntered(MouseEvent e) {
		 * axesFrame.getRootPane().setBorder(BorderFactory.createLineBorder(
		 * Color.DARK_GRAY,2)); }
		 * 
		 * @Override public void mouseExited(MouseEvent e) {
		 * axesFrame.getRootPane().setBorder(null); } });
		 */

		axesFrame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent aEvent)
			{
				axesFrame.setVisible(false);
			}
		});

		axesFrame.addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				Point loc = e.getComponent().getLocation();
				int w = e.getComponent().getWidth();
				e.getComponent().setSize(w, w);
				e.getComponent().setLocation(loc);
				axesPanel.getComponent().setSize(e.getComponent().getSize());
				// axesPanel.getRenderer().ResetCamera();
			}

			@Override
			public void componentShown(ComponentEvent e)
			{
				axesPanel.Render();
			}
		});

		getComponent().addComponentListener(this);
	}

	public void setInteractorEnableState(boolean aBool)
	{
		if (aBool == true)
			windowInteractor.Enable();
		else
			windowInteractor.Disable();
	}

	public void mouseOff()
	{
		vtkInteractorForwarder forwarder = this.getInteractorForwarder();
		this.uiComponent.removeMouseListener(forwarder);
		this.uiComponent.removeMouseMotionListener(forwarder);
		this.uiComponent.removeMouseWheelListener(forwarder);
	}

	public void mouseOn()
	{
		vtkInteractorForwarder forwarder = this.getInteractorForwarder();
		this.uiComponent.addMouseListener(forwarder);
		this.uiComponent.addMouseMotionListener(forwarder);
		this.uiComponent.addMouseWheelListener(forwarder);
	}

	@Override
	public void componentHidden(ComponentEvent e)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void componentMoved(ComponentEvent e)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void componentResized(ComponentEvent e)
	{
		// redrawAxes();
		Render();
	}

	@Override
	public void componentShown(ComponentEvent e)
	{
	}

	/**
	 * Configures the RenderPanel so that rotation is constrained to the specified
	 * axis.
	 */
	public void constrainRotationAxis(Vector3D aAxis, Vector3D aOrigin)
	{
		// Delegate
		interactorStyle.setZoomOnly(false);
		interactorStyle.setRotationConstraint(aAxis, aOrigin);
	}
	
	public void setZoomOnly(boolean zoomOnly, Vector3D aAxis, Vector3D aOrigin)
	{
		// Delegate
		interactorStyle.setZoomOnly(zoomOnly, aAxis, aOrigin);
	}
	
	public void setZoomOnly(boolean zoomOnly)
	{
		// Delegate
		interactorStyle.setZoomOnly(zoomOnly);
	}

	@Override
	public void Render()
	{
		// redrawAxes();
		super.Render();
	}

	public static void main(String[] args) throws InterruptedException
	{
		vtkNativeLibrary.LoadAllNativeLibraries();
		RenderView renderView = new RenderView();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				int w = 600;
				int h = 600;
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				frame.getContentPane().add(renderView);
				frame.setVisible(true);

				frame.setSize(w, h);
				renderView.setSize(w, h);
			}
		});

		vtkConeSource source = new vtkConeSource();
		source.Update();
		vtkPolyData cone = source.GetOutput();
		vtkPolyDataMapper mapper = new vtkPolyDataMapper();
		mapper.SetInputData(cone);
		vtkActor actor = new vtkActor();
		actor.SetMapper(mapper);
		renderView.registerProp(actor);
		renderView.getRenderPanel().getRenderer().AddActor(actor);
	}

	protected void setUpMainWindowListeners()
	{

		WindowAdapter adapter = new WindowAdapter() {
			@Override
			public void windowDeiconified(@SuppressWarnings("unused") WindowEvent e)
			{
				axesFrame.setVisible(showAxesPanelOnRestore);
			}

			@Override
			public void windowIconified(@SuppressWarnings("unused") WindowEvent e)
			{
				showAxesPanelOnRestore = axesFrame.isVisible();
				axesFrame.setVisible(false);
			}

			@Override
			public void windowActivated(@SuppressWarnings("unused") WindowEvent e)
			{
				axesFrame.setAlwaysOnTop(true);
			}

			@Override
			public void windowDeactivated(@SuppressWarnings("unused") WindowEvent e)
			{
				axesFrame.setAlwaysOnTop(false);
			}

			@Override
			public void windowGainedFocus(@SuppressWarnings("unused") WindowEvent e)
			{
				axesFrame.setAlwaysOnTop(true);
			}

			@Override
			public void windowLostFocus(@SuppressWarnings("unused") WindowEvent e)
			{
				axesFrame.setAlwaysOnTop(false);
			}

			@Override
			public void windowStateChanged(WindowEvent e)
			{
				System.err.println(e);
			}
		};

		Window window = MainWindow.getMainWindow();
		window.addWindowListener(adapter);
		window.addWindowFocusListener(adapter);

		/*
		 * ComponentListener l=new ComponentListener() { Point location;
		 * 
		 * @Override public void componentShown(ComponentEvent e) {
		 * location=e.getComponent().getLocationOnScreen(); }
		 * 
		 * @Override public void componentResized(ComponentEvent e) { // TODO
		 * Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void componentMoved(ComponentEvent e) { if
		 * (location==null) location=e.getComponent().getLocationOnScreen(); int
		 * dx=(int)(e.getComponent().getLocationOnScreen().getX()-location.getX(
		 * )); int
		 * dy=(int)(e.getComponent().getLocationOnScreen().getY()-location.getY(
		 * )); axesFrame.setLocationRelativeTo(window);
		 * axesFrame.setLocation(dx, dy); }
		 * 
		 * @Override public void componentHidden(ComponentEvent e) { // TODO
		 * Auto-generated method stub
		 * 
		 * } }; window.addComponentListener(l);
		 */
	}
	/*
	 * private void redrawAxes() { vtkTransform transform=new vtkTransform();
	 * transform.DeepCopy(getActiveCamera().GetModelViewTransformObject());
	 * transform.Inverse(); transform.Update();
	 * 
	 * axes.getActor().SetOrientation(transform.GetOrientation()); double
	 * axesViewSize=0.25; double axesViewX=-0.75; double axesViewY=-0.75; double
	 * fovRad=Math.toRadians(getActiveCamera().GetViewAngle()); double
	 * axesWorldSize=1; double tanFovHf=Math.tan(fovRad/2.); double
	 * L=axesWorldSize/axesViewSize/tanFovHf;
	 * 
	 * Vector3D campos=new Vector3D(getActiveCamera().GetPosition()); Vector3D
	 * right,up,look; try { right=viewCamera.getRightUnit();
	 * up=viewCamera.getUpUnit(); look=viewCamera.getLookUnit(); } catch
	 * (MathArithmeticException e) { return; } double W=2*L*tanFovHf; Vector3D
	 * lambdaVec=right.scalarMultiply(W/2.*axesViewX).add(up.scalarMultiply(W/2.
	 * *axesViewY)).add(look.scalarMultiply(L)); Vector3D
	 * origin=campos.add(lambdaVec); axes.getActor().SetPosition(origin.toArray());
	 * 
	 * Vector3D xunit=new
	 * Vector3D(transform.TransformPoint(origin.add(Vector3D.PLUS_I).toArray())) ;
	 * Vector3D yunit=new
	 * Vector3D(transform.TransformPoint(origin.add(Vector3D.PLUS_J).toArray())) ;
	 * Vector3D zunit=new
	 * Vector3D(transform.TransformPoint(origin.add(Vector3D.PLUS_K).toArray())) ;
	 * vtkCoordinate coordTransform=new vtkCoordinate();
	 * coordTransform.SetCoordinateSystemToWorld();
	 * coordTransform.SetValue(xunit.toArray()); double[]
	 * xpos=coordTransform.GetComputedDoubleViewportValue(getRenderer());
	 * coordTransform.SetValue(yunit.toArray()); double[]
	 * ypos=coordTransform.GetComputedDoubleViewportValue(getRenderer());
	 * coordTransform.SetValue(zunit.toArray()); double[]
	 * zpos=coordTransform.GetComputedDoubleViewportValue(getRenderer()); double
	 * whf=getRenderWindow().GetSize()[0]/2; double
	 * hhf=getRenderWindow().GetSize()[1]/2;
	 * coordTransform.SetValue(Vector3D.ZERO.toArray()); double[]
	 * opos=coordTransform.GetComputedDoubleViewportValue(getRenderer()); //
	 * axes.getLabelActorX().SetPosition(xpos[0]-whf,xpos[1]-hhf); //
	 * axes.getLabelActorY().SetPosition(ypos[0]-whf,ypos[1]-hhf); //
	 * axes.getLabelActorZ().SetPosition(zpos[0]-whf,zpos[1]-hhf);
	 * axes.getLabelActorX().SetPosition(xpos[0]-opos[0],xpos[1]-opos[1]);
	 * axes.getLabelActorY().SetPosition(ypos[0]-opos[0],ypos[1]-opos[1]);
	 * axes.getLabelActorZ().SetPosition(zpos[0]-opos[0],zpos[1]-opos[1]);
	 * 
	 * 
	 * getActiveCamera().RemoveObserver(cameraObserver);
	 * propRenderer.ResetCameraClippingRange(); double[]
	 * propRange=propRenderer.GetActiveCamera().GetClippingRange();
	 * axesRenderer.ResetCameraClippingRange(); double[]
	 * axesRange=axesRenderer.GetActiveCamera().GetClippingRange();
	 * getActiveCamera().SetClippingRange(Math.min(propRange[0],
	 * axesRange[0]),Math.max(propRange[1], axesRange[1]));
	 * cameraObserver=getActiveCamera().AddObserver("ModifiedEvent", this,
	 * "redrawAxes"); }
	 */

	static class Interceptor implements vtkEventInterceptor
	{

		@Override
		public boolean keyPressed(KeyEvent e)
		{
			// Don't let VTK handle key events.
			return true;
		}

		@Override
		public boolean keyReleased(KeyEvent e)
		{
			// Don't let VTK handle key events.
			return true;
		}

		@Override
		public boolean keyTyped(KeyEvent e)
		{
			// Don't let VTK handle key events.
			return true;
		}

		@Override
		public boolean mouseDragged(MouseEvent e)
		{
			return false;
		}

		@Override
		public boolean mouseMoved(MouseEvent e)
		{
			return false;
		}

		@Override
		public boolean mouseClicked(MouseEvent e)
		{
			return false;
		}

		@Override
		public boolean mouseEntered(MouseEvent e)
		{
			return false;
		}

		@Override
		public boolean mouseExited(MouseEvent e)
		{
			return false;
		}

		@Override
		public boolean mousePressed(MouseEvent e)
		{
			return false;
		}

		@Override
		public boolean mouseReleased(MouseEvent e)
		{
			return false;
		}

		@Override
		public boolean mouseWheelMoved(MouseWheelEvent e)
		{
			return false;
		}
	}

}

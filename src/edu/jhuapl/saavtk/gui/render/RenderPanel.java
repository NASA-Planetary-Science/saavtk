package edu.jhuapl.saavtk.gui.render;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.util.Arrays;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.tools.Diagnostic;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.jidesoft.plaf.WindowsDesktopProperty;

import edu.jhuapl.saavtk.gui.MainWindow;
import edu.jhuapl.saavtk.gui.render.axes.Axes;
import edu.jhuapl.saavtk.gui.render.axes.AxesPanel;
import edu.jhuapl.saavtk.gui.render.axes.CartesianViewDirection;
import edu.jhuapl.saavtk.gui.render.camera.Camera;
import edu.jhuapl.saavtk.gui.render.camera.CameraEvent;
import edu.jhuapl.saavtk.gui.render.camera.CameraListener;
import edu.jhuapl.saavtk.gui.render.toolbar.RenderToolbar;
import edu.jhuapl.saavtk.gui.render.toolbar.RenderToolbarEvent;
import edu.jhuapl.saavtk.gui.render.toolbar.RenderToolbarListener;
import edu.jhuapl.saavtk.gui.render.toolbar.RenderToolbarEvent.ConstrainRotationAxisEvent;
import vtk.vtkActor;
import vtk.vtkAxes;
import vtk.vtkConeSource;
import vtk.vtkCoordinate;
import vtk.vtkNativeLibrary;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkRenderer;
import vtk.vtkTransform;
import vtk.rendering.vtkEventInterceptor;
import vtk.rendering.vtkInteractorForwarder;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class RenderPanel extends vtkJoglPanelComponent
		implements CameraListener, RenderToolbarListener, ComponentListener {
	Camera viewCamera;
	RenderToolbar toolbar = null;
	AxesPanel axesPanel = null;
	CustomInteractorStyle interactorStyle;
	CustomInteractorStyle defaultInteractorStyle;

	int cameraObserver;
	// vtkRenderer axesRenderer=new vtkRenderer();
	vtkRenderer propRenderer;

	// Axes axes=new Axes();

	public AxesPanel getAxesPanel() {
		return axesPanel;
	}


	boolean axesPanelShown = false;
	JFrame axesFrame=new JFrame();

	public RenderPanel(RenderToolbar toolbar)// , RenderStatusBar statusBar)
	{
		
		getInteractorForwarder().setEventInterceptor(new Interceptor());
		interactorStyle = new CustomInteractorStyle(getRenderWindowInteractor());
		defaultInteractorStyle = interactorStyle;
		viewCamera = new RenderPanelCamera(this);
		viewCamera.addCameraListener(this);
		this.toolbar = toolbar;
		toolbar.addToolbarListener(this);
		//
		propRenderer = getRenderer();

		axesPanel = new AxesPanel(this);
		
		//axesFrame.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
		axesFrame.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		axesFrame.add(axesPanel.getComponent());
		axesFrame.setVisible(false);
		axesFrame.setAlwaysOnTop(true);
		
		MainWindow.getMainWindow().addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowActivated(WindowEvent e) {
				//axesFrame.requestFocus();
				axesFrame.setAlwaysOnTop(true);
				//axesFrame.transferFocus();
			}
		
			@Override
			public void windowDeactivated(WindowEvent e) {
				//axesFrame.requestFocus();
				axesFrame.setAlwaysOnTop(false);
				//axesFrame.transferFocus();
			}
	});

		//frame.setUndecorated(true);
		toolbar.addToolbarListener(new RenderToolbarListener() {

			@Override
			public void handle(RenderToolbarEvent event) {
				if (event instanceof RenderToolbarEvent.ToggleAxesVisibilityEvent) {
					axesFrame.setVisible(((RenderToolbarEvent.ToggleAxesVisibilityEvent) event).show());
					if (!axesPanelShown) {
						Point point = RenderPanel.this.getComponent().getLocationOnScreen();
						Dimension dim = RenderPanel.this.getComponent().getSize();
						int size = (int) Math.max(dim.width / 5., dim.height / 5);
						axesPanel.setSize(size, size);
						axesFrame.setLocation(point.x, point.y + dim.height - size);	// lower left
						axesPanelShown = true;
					}
				}

			}
		});
		
		
		axesFrame.getRootPane().setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY,2));
		
/*		axesPanel.getComponent().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				axesFrame.getRootPane().setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY,2));
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				axesFrame.getRootPane().setBorder(null);
			}
		});*/

		axesFrame.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				toolbar.setOrientationAxesToggleState(false);
			}
			
			
		});
		
		axesFrame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Point loc=e.getComponent().getLocation();
				int w=e.getComponent().getWidth();
				e.getComponent().setSize(w,w);
				e.getComponent().setLocation(loc);
				axesPanel.getComponent().setSize(e.getComponent().getSize());
				//axesPanel.getRenderer().ResetCamera();
	}
		});
		
		
		getComponent().addComponentListener(this);
	}

	public void setInteractorStyleToDefault() {

		if (this.windowInteractor != null) {
			this.lock.lock();
			this.windowInteractor.SetInteractorStyle(defaultInteractorStyle);
			this.lock.unlock();
		}
	}

	public void mouseOff() {
		vtkInteractorForwarder forwarder = this.getInteractorForwarder();
		this.uiComponent.removeMouseListener(forwarder);
		this.uiComponent.removeMouseMotionListener(forwarder);
		this.uiComponent.removeMouseWheelListener(forwarder);
	}

	public void mouseOn() {
		vtkInteractorForwarder forwarder = this.getInteractorForwarder();
		this.uiComponent.addMouseListener(forwarder);
		this.uiComponent.addMouseMotionListener(forwarder);
		this.uiComponent.addMouseWheelListener(forwarder);
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentResized(ComponentEvent e) {
		// redrawAxes();
		Render();
	}

	
	@Override
	public void componentShown(ComponentEvent e) {

	}

	@Override
	public void handle(CameraEvent event) {
		// redrawAxes();
		Render();
	}

	@Override
	public void handle(RenderToolbarEvent event) {
		getActiveCamera().RemoveObserver(cameraObserver);
		viewCamera.removeCameraListener(this);
		if (event instanceof RenderToolbarEvent.ConstrainRotationAxisEvent) {
			interactorStyle.setRotationConstraint(((ConstrainRotationAxisEvent) event).getAxis());
		} else if (event instanceof RenderToolbarEvent.LookAlongAxisEvent) {
			Vector3D position = new Vector3D(getActiveCamera().GetPosition());
			CartesianViewDirection direction = ((RenderToolbarEvent.LookAlongAxisEvent) event).getDirection();
			viewCamera.setPosition(direction.getLookUnit().negate().scalarMultiply(position.getNorm()));
			viewCamera.setUpUnit(direction.getUpUnit());
		} else if (event instanceof RenderToolbarEvent.ViewAllEvent) {
			viewAll();
		} /*
			 * else if (event instanceof
			 * RenderToolbarEvent.ToggleAxesVisibilityEvent) {
			 * axes.setVisible(((RenderToolbarEvent.ToggleAxesVisibilityEvent)
			 * event).show()); super.Render(); }
			 */
		viewCamera.addCameraListener(this);
		// cameraObserver=getActiveCamera().AddObserver("ModifiedEvent", this,
		// "redrawAxes");
		// redrawAxes();
		Render();

	}

	public void viewAll() {
		// boolean showAxes=axes.isVisible();
		// axes.setVisible(false);
		getRenderer().ResetCamera();
		// axes.setVisible(showAxes);
		super.Render();
	}

	@Override
	public void Render() {
		// redrawAxes();
		super.Render();
	}

	public static void main(String[] args) throws InterruptedException {
		vtkNativeLibrary.LoadAllNativeLibraries();
		RenderView renderView = new RenderView();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
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
	 * origin=campos.add(lambdaVec);
	 * axes.getActor().SetPosition(origin.toArray());
	 * 
	 * Vector3D xunit=new
	 * Vector3D(transform.TransformPoint(origin.add(Vector3D.PLUS_I).toArray()))
	 * ; Vector3D yunit=new
	 * Vector3D(transform.TransformPoint(origin.add(Vector3D.PLUS_J).toArray()))
	 * ; Vector3D zunit=new
	 * Vector3D(transform.TransformPoint(origin.add(Vector3D.PLUS_K).toArray()))
	 * ; vtkCoordinate coordTransform=new vtkCoordinate();
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

	static class Interceptor implements vtkEventInterceptor {

		@Override
		public boolean keyPressed(KeyEvent e) {
			// Don't let VTK handle key events.
			return true;
		}

		@Override
		public boolean keyReleased(KeyEvent e) {
			// Don't let VTK handle key events.
			return true;
		}

		@Override
		public boolean keyTyped(KeyEvent e) {
			// Don't let VTK handle key events.
			return true;
		}

		@Override
		public boolean mouseDragged(MouseEvent e) {
			return false;
		}

		@Override
		public boolean mouseMoved(MouseEvent e) {
			return false;
		}

		@Override
		public boolean mouseClicked(MouseEvent e) {
			return false;
		}

		@Override
		public boolean mouseEntered(MouseEvent e) {
			return false;
		}

		@Override
		public boolean mouseExited(MouseEvent e) {
			return false;
		}

		@Override
		public boolean mousePressed(MouseEvent e) {
			return false;
		}

		@Override
		public boolean mouseReleased(MouseEvent e) {
			return false;
		}

		@Override
		public boolean mouseWheelMoved(MouseWheelEvent e) {
			return false;
		}
	}
}

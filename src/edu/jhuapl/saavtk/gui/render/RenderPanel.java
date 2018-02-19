package edu.jhuapl.saavtk.gui.render;

import java.awt.Color;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.render.axes.Axes;
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
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class RenderPanel extends vtkJoglPanelComponent implements CameraListener, RenderToolbarListener, ComponentListener
{
	Camera					viewCamera;
	RenderToolbar			toolbar	= null;
	CustomInteractorStyle	interactorStyle;

	int cameraObserver;
	vtkRenderer axesRenderer=new vtkRenderer();
	vtkRenderer propRenderer;

	Axes axes=new Axes();

	public RenderPanel(RenderToolbar toolbar)//, RenderStatusBar statusBar)
	{
		getInteractorForwarder().setEventInterceptor(new Interceptor());
		interactorStyle = new CustomInteractorStyle(getRenderWindowInteractor());
		viewCamera = new RenderPanelCamera(this);
		viewCamera.addCameraListener(this);
		toolbar.addToolbarListener(this);
		//
		propRenderer=getRenderer();

		getRenderWindow().SetNumberOfLayers(2);
		getRenderWindow().AddRenderer(axesRenderer);
		getRenderer().SetLayer(0);
		axesRenderer.SetLayer(1);
		axesRenderer.AddActor(axes.getActor());
		axesRenderer.AddActor(axes.getLabelActorX());
		axesRenderer.AddActor(axes.getLabelActorY());
		axesRenderer.AddActor(axes.getLabelActorZ());
		axesRenderer.SetActiveCamera(getActiveCamera());

		cameraObserver=getActiveCamera().AddObserver("ModifiedEvent", this, "redrawAxes");
		redrawAxes();
		
		getComponent().addComponentListener(this);
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
//		redrawAxes();
		Render();
	}

	@Override
	public void componentShown(ComponentEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void handle(CameraEvent event)
	{
//		redrawAxes();
		Render();
	}

	@Override
	public void handle(RenderToolbarEvent event)
	{
		getActiveCamera().RemoveObserver(cameraObserver);
		viewCamera.removeCameraListener(this);
		if (event instanceof RenderToolbarEvent.ConstrainRotationAxisEvent)
		{
			interactorStyle.setRotationConstraint(((ConstrainRotationAxisEvent) event).getAxis());
		} else if (event instanceof RenderToolbarEvent.LookAlongAxisEvent)
		{
			Vector3D position = new Vector3D(getActiveCamera().GetPosition());
			CartesianViewDirection direction = ((RenderToolbarEvent.LookAlongAxisEvent) event).getDirection();
			viewCamera.setPosition(direction.getLookUnit().negate().scalarMultiply(position.getNorm()));
			viewCamera.setUpUnit(direction.getUpUnit());
		} else if (event instanceof RenderToolbarEvent.ViewAllEvent)
		{
			viewAll();
		} else if (event instanceof RenderToolbarEvent.ToggleAxesVisibilityEvent)
		{
			axes.setVisible(((RenderToolbarEvent.ToggleAxesVisibilityEvent) event).show());
			super.Render();
		}
		viewCamera.addCameraListener(this);
		cameraObserver=getActiveCamera().AddObserver("ModifiedEvent", this, "redrawAxes");
//		redrawAxes();
		Render();
		
	}
	
	public void viewAll()
	{
		boolean showAxes=axes.isVisible();
		axes.setVisible(false);
		getRenderer().ResetCamera();
		axes.setVisible(showAxes);
		super.Render();
	}

	@Override
	public void Render() {
		redrawAxes();
		super.Render();
	}

	public static void main(String[] args) throws InterruptedException
	{
		vtkNativeLibrary.LoadAllNativeLibraries();
		RenderView renderView = new RenderView();
		SwingUtilities.invokeLater(new Runnable()
		{
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

	private void redrawAxes()
	{
		vtkTransform transform=new vtkTransform();
		transform.DeepCopy(getActiveCamera().GetModelViewTransformObject());
		transform.Inverse();
		transform.Update();
		
		axes.getActor().SetOrientation(transform.GetOrientation());
		double axesViewSize=0.25;
		double axesViewX=-0.75;
		double axesViewY=-0.75;
		double fovRad=Math.toRadians(getActiveCamera().GetViewAngle());
		double axesWorldSize=1;
		double tanFovHf=Math.tan(fovRad/2.);
		double L=axesWorldSize/axesViewSize/tanFovHf;

		Vector3D campos=new Vector3D(getActiveCamera().GetPosition());
		Vector3D right,up,look;
		try
		{
			right=viewCamera.getRightUnit();
			up=viewCamera.getUpUnit();
			look=viewCamera.getLookUnit();
		} catch (MathArithmeticException e)
		{
			return; 
		}
		double W=2*L*tanFovHf;
		Vector3D lambdaVec=right.scalarMultiply(W/2.*axesViewX).add(up.scalarMultiply(W/2.*axesViewY)).add(look.scalarMultiply(L));
		Vector3D origin=campos.add(lambdaVec);
		axes.getActor().SetPosition(origin.toArray());

		Vector3D xunit=new Vector3D(transform.TransformPoint(origin.add(Vector3D.PLUS_I).toArray()));
		Vector3D yunit=new Vector3D(transform.TransformPoint(origin.add(Vector3D.PLUS_J).toArray()));
		Vector3D zunit=new Vector3D(transform.TransformPoint(origin.add(Vector3D.PLUS_K).toArray()));
		vtkCoordinate coordTransform=new vtkCoordinate();
		coordTransform.SetCoordinateSystemToWorld();
		coordTransform.SetValue(xunit.toArray());
		double[] xpos=coordTransform.GetComputedDoubleViewportValue(getRenderer());
		coordTransform.SetValue(yunit.toArray());
		double[] ypos=coordTransform.GetComputedDoubleViewportValue(getRenderer());
		coordTransform.SetValue(zunit.toArray());
		double[] zpos=coordTransform.GetComputedDoubleViewportValue(getRenderer());
		double whf=getRenderWindow().GetSize()[0]/2;
		double hhf=getRenderWindow().GetSize()[1]/2;
		coordTransform.SetValue(Vector3D.ZERO.toArray());
		double[] opos=coordTransform.GetComputedDoubleViewportValue(getRenderer());
//		axes.getLabelActorX().SetPosition(xpos[0]-whf,xpos[1]-hhf);
//		axes.getLabelActorY().SetPosition(ypos[0]-whf,ypos[1]-hhf);
//		axes.getLabelActorZ().SetPosition(zpos[0]-whf,zpos[1]-hhf);
		axes.getLabelActorX().SetPosition(xpos[0]-opos[0],xpos[1]-opos[1]);
		axes.getLabelActorY().SetPosition(ypos[0]-opos[0],ypos[1]-opos[1]);
		axes.getLabelActorZ().SetPosition(zpos[0]-opos[0],zpos[1]-opos[1]);

		
		getActiveCamera().RemoveObserver(cameraObserver);
		propRenderer.ResetCameraClippingRange();
		double[] propRange=propRenderer.GetActiveCamera().GetClippingRange();
		axesRenderer.ResetCameraClippingRange();
		double[] axesRange=axesRenderer.GetActiveCamera().GetClippingRange();
		getActiveCamera().SetClippingRange(Math.min(propRange[0], axesRange[0]),Math.max(propRange[1], axesRange[1]));
		cameraObserver=getActiveCamera().AddObserver("ModifiedEvent", this, "redrawAxes");
	}

	private static class Interceptor implements vtkEventInterceptor {

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

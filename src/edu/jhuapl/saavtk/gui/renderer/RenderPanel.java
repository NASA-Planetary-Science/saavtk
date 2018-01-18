package edu.jhuapl.saavtk.gui.renderer;

import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.renderer.RenderToolbarEvent.ConstrainRotationAxisEvent;
import vtk.vtkActor;
import vtk.vtkActor2D;
import vtk.vtkApplyColors;
import vtk.vtkArrowSource;
import vtk.vtkAssembly;
import vtk.vtkAxesActor;
import vtk.vtkColorTransferFunction;
import vtk.vtkConeSource;
import vtk.vtkCoordinate;
import vtk.vtkFollower;
import vtk.vtkImageActor;
import vtk.vtkImageData;
import vtk.vtkImageMapToColors;
import vtk.vtkImageMapToRGBA;
import vtk.vtkImageMapper;
import vtk.vtkImageMask;
import vtk.vtkLookupTable;
import vtk.vtkMatrix4x4;
import vtk.vtkNativeLibrary;
import vtk.vtkOrientationMarkerWidget;
import vtk.vtkPNGReader;
import vtk.vtkPNGWriter;
import vtk.vtkPerspectiveTransform;
import vtk.vtkPlaneSource;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPropCollection;
import vtk.vtkRenderWindow;
import vtk.vtkRenderWindowInteractor;
import vtk.vtkRenderer;
import vtk.vtkSphereSource;
import vtk.vtkTextSource;
import vtk.vtkTexture;
import vtk.vtkTransform;
import vtk.vtkTransformFilter;
import vtk.vtkUnsignedCharArray;
import vtk.vtkWindow;
import vtk.vtkWindowToImageFilter;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class RenderPanel extends vtkJoglPanelComponent implements CameraListener, RenderToolbarListener
{
	Camera					viewCamera;
	RenderToolbar			toolbar	= null;
	CustomInteractorStyle	interactorStyle;

	//vtkRenderer renderer2=new vtkRenderer();
	
	//vtkRenderWindow axesRenderWindow = new vtkRenderWindow();
	//vtkRenderer axesRenderer=new vtkRenderer();
	
/*	vtkFollower actorx=new vtkFollower();
	vtkFollower actory=new vtkFollower();
	vtkFollower actorz=new vtkFollower();
	vtkFollower actorxlabel=new vtkFollower();
	vtkFollower actorylabel=new vtkFollower();
	vtkFollower actorzlabel=new vtkFollower();*/

	vtkTexture texture=new vtkTexture();
	OffScreenAxesRenderer axesSource;
	vtkFollower follower=new vtkFollower();
	vtkAxesActor axes=new vtkAxesActor();
	int cameraObserver;
	
	public vtkActor getAxesActor()
	{
		return follower;
	}

	public RenderPanel(RenderToolbar toolbar)//, RenderStatusBar statusBar)
	{
		interactorStyle = new CustomInteractorStyle(getRenderWindowInteractor());
		viewCamera = new RenderPanelCamera(this);
		viewCamera.addCameraListener(this);
		toolbar.addToolbarListener(this);
		//
		
		
		vtkPlaneSource source=new vtkPlaneSource();
		source.Update();
		
		vtkPolyDataMapper mapper=new vtkPolyDataMapper();
		mapper.SetInputData(source.GetOutput());
		mapper.Update();
		
		follower.SetMapper(mapper);
		follower.SetCamera(getActiveCamera());
		follower.GetProperty().SetEdgeColor(1, 1, 1);
		//follower.GetProperty().SetEdgeVisibility(1);
				
		getRenderer().AddActor(follower);
		axesSource=new OffScreenAxesRenderer(200);

		cameraObserver=getActiveCamera().AddObserver("ModifiedEvent", this, "test");
	}

	@Override
	public void handle(CameraEvent event)
	{
		Render();
	}

	@Override
	public void handle(RenderToolbarEvent event)
	{
		if (event instanceof RenderToolbarEvent.ConstrainRotationAxisEvent)
		{
			interactorStyle.setRotationConstraint(((ConstrainRotationAxisEvent) event).getAxis());
		} else if (event instanceof RenderToolbarEvent.LookAlongAxisEvent)
		{
			getActiveCamera().RemoveObserver(cameraObserver);
			Vector3D position = new Vector3D(getActiveCamera().GetPosition());
			CartesianViewDirection direction = ((RenderToolbarEvent.LookAlongAxisEvent) event).getDirection();
			viewCamera.setPosition(direction.getLookUnit().negate().scalarMultiply(position.getNorm()));
			viewCamera.setUpUnit(direction.getUpUnit());
			cameraObserver=getActiveCamera().AddObserver("ModifiedEvent", this, "test");
		} else if (event instanceof RenderToolbarEvent.ViewAllEvent)
		{
			follower.SetVisibility(0);
			getRenderer().ResetCamera();
			follower.SetVisibility(1);
			Render();
		} else if (event instanceof RenderToolbarEvent.ToggleAxesVisibilityEvent)
		{
			follower.SetVisibility(((RenderToolbarEvent.ToggleAxesVisibilityEvent) event).show()?1:0);
			Render();
		}
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
	}

	private void test()
	{

		vtkImageData imageData=axesSource.getImageData(getActiveCamera());
		texture.SetInputData(imageData);
		texture.Update();
		follower.SetTexture(texture);
		//follower.SetScale(2);

		Vector3D pos1=viewCamera.getPosition();
		Vector3D pos2=pos1.add(viewCamera.getLookUnit().scalarMultiply(10)).add(viewCamera.getRightUnit().scalarMultiply(-3)).add(viewCamera.getUpUnit().scalarMultiply(-2));
		//Vector3D pos=pos1.add(pos2.subtract(pos1)).normalize().scalarMultiply(2);
		follower.SetPosition(pos2.toArray());
		
	}
}

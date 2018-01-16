package edu.jhuapl.saavtk.gui.renderer;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkActor;
import vtk.vtkAxesActor;
import vtk.vtkCamera;
import vtk.vtkConeSource;
import vtk.vtkImageData;
import vtk.vtkPNGWriter;
import vtk.vtkPolyDataMapper;
import vtk.vtkRenderWindow;
import vtk.vtkRenderer;
import vtk.vtkWindowToImageFilter;

public class OffScreenAxesRenderer
{
	vtkRenderer renderer;
	vtkRenderWindow renderWindow;
	vtkAxesActor axes;
	
	public OffScreenAxesRenderer(int w)
	{
		renderer=new vtkRenderer();
		renderWindow=new vtkRenderWindow();
		renderWindow.AddRenderer(renderer);
		renderWindow.SetSize(w,w);
		renderWindow.OffScreenRenderingOn();
		axes=new vtkAxesActor();
		renderer.AddActor(axes);
		
		vtkConeSource source=new vtkConeSource();
		source.Update();
		vtkPolyDataMapper mapper=new vtkPolyDataMapper();
		mapper.SetInputData(source.GetOutput());
		mapper.Update();
		vtkActor actor=new vtkActor();
		actor.SetMapper(mapper);
		//renderer.AddActor(actor);
	}
	
	public void reshape(int w, int h)
	{
		renderWindow.SetSize(w,h);
	}
	
	public vtkImageData getImageData(vtkCamera cam)
	{
		Vector3D pos=new Vector3D(cam.GetPosition()).subtract(new Vector3D(cam.GetFocalPoint())).normalize().scalarMultiply(5);
		renderer.GetActiveCamera().SetPosition(pos.toArray());
		renderer.GetActiveCamera().SetFocalPoint(Vector3D.ZERO.toArray());
		renderer.GetActiveCamera().SetViewUp(cam.GetViewUp());
		renderer.ResetCamera();

		vtkWindowToImageFilter filter=new vtkWindowToImageFilter();
		filter.SetInput(renderWindow);
		filter.SetInputBufferTypeToRGBA();
		filter.ShouldRerenderOn();
		filter.Update();
		
		return filter.GetOutput();
	}
}

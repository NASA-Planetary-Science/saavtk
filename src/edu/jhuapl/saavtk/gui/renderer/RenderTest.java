package edu.jhuapl.saavtk.gui.renderer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkJPEGWriter;
import vtk.vtkNativeLibrary;
import vtk.vtkPNGWriter;
import vtk.vtkPolyDataMapper;
import vtk.vtkUnsignedCharArray;
import vtk.vtkWindowToImageFilter;
import vtk.rendering.jogl.vtkJoglCanvasComponent;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class RenderTest extends vtkJoglPanelComponent
{
	vtkJoglCanvasComponent offScreenCanvas=new vtkJoglCanvasComponent();
	
	public RenderTest(int w, int h)
	{
		offScreenCanvas.getRenderWindow().OffScreenRenderingOn();
		offScreenCanvas.setSize(w, h);
		setSize(w,h);
		
		vtkConeSource source=new vtkConeSource();
		source.Update();
		
		vtkPolyDataMapper mapper=new vtkPolyDataMapper();
		mapper.SetInputData(source.GetOutput());
		mapper.Update();
		
		vtkActor actor=new vtkActor();
		actor.SetMapper(mapper);
		offScreenCanvas.getRenderer().AddActor(actor);
		
		getActiveCamera().AddObserver("ModifiedEvent", this, "test0");
		//offScreenCanvas.getRenderer().AddObserver("RenderEvent", this, "test");
	}
	
	void test0()
	{
		offScreenCanvas.Render();
		while (offScreenCanvas.getRenderWindow().CheckInRenderStatus()==1)
			;
		vtkWindowToImageFilter filter=new vtkWindowToImageFilter();
		filter.SetInput(offScreenCanvas.getRenderWindow());
		filter.SetInputBufferTypeToRGB();
		filter.Update();
		
		
		//getRenderWindow().SetRGBACharPixelData(0, 0, offScreenCanvas.getRenderWindow().GetSize()[0]-1, offScreenCanvas.getRenderWindow().GetSize()[1]-1, (vtkUnsignedCharArray)filter.GetOutput().GetPointData().GetScalars(), 1, 0);
		//Render();
		
	}
	
	public static void main(String[] args)
	{
		vtkNativeLibrary.LoadAllNativeLibraries();
		
		SwingUtilities.invokeLater(new Runnable()
		{
			
			@Override
			public void run()
			{
				int w=600;
				int h=600;
				JFrame frame=new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(w,h);
				
				RenderTest test=new RenderTest(w, h);
				frame.getContentPane().add(test.getComponent());
				frame.setVisible(true);
			}
		});
	}
	
}

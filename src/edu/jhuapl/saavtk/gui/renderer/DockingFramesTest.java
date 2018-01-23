package edu.jhuapl.saavtk.gui.renderer;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGridArea;
import bibliothek.gui.dock.common.CMinimizeArea;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.action.predefined.CMinimizeAction;
import bibliothek.gui.dock.common.event.CDockableLocationEvent;
import bibliothek.gui.dock.common.event.CDockableLocationListener;
import edu.jhuapl.saavtk.gui.renderer.toolbar.RenderToolbar;
import vtk.vtkActor;
import vtk.vtkConeSource;
import vtk.vtkNativeLibrary;
import vtk.vtkPolyDataMapper;

public class DockingFramesTest
{
	static class vtkDockable extends DefaultSingleCDockable implements CDockableLocationListener
	{

		RenderPanel renderPanel=new RenderPanel(new RenderToolbar());
		
		public vtkDockable(String id)
		{
			super(id);
			add(renderPanel.getComponent());
			addCDockableLocationListener(this);
		}
		
		@Override
		public void changed(CDockableLocationEvent e)
		{
			remove(renderPanel.getComponent());
			
			add(renderPanel.getComponent());
		}
		
	}
	
	public static void main(String[] args)
	{
		vtkNativeLibrary.LoadAllNativeLibraries();

		JFrame frame=new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		CControl control=new CControl(frame);
		frame.add(control.getContentArea());
		
/*		RenderPanel renderPanel=new RenderPanel(new RenderToolbar());
		vtkConeSource source=new vtkConeSource();
		source.Update();
		vtkPolyDataMapper mapper=new vtkPolyDataMapper();
		mapper.SetInputData(source.GetOutput());
		mapper.Update();
		vtkActor actor=new vtkActor();
		actor.SetMapper(mapper);
		renderPanel.getRenderer().AddActor(actor);*/
		
		SingleCDockable dockable1=new vtkDockable("test");
		control.addDockable(dockable1);
		dockable1.setVisible(true);

		SingleCDockable dockable2=new DefaultSingleCDockable("dockable2", new JLabel("World!"));
		control.addDockable(dockable2);
		dockable2.setVisible(true);
		
		frame.setSize(600, 600);
		frame.setVisible(true);
	}
}

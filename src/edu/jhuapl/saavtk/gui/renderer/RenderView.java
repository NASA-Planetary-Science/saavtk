package edu.jhuapl.saavtk.gui.renderer;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.gui.renderer.toolbar.RenderToolbar;
import vtk.vtkProp;
import vtk.rendering.jogl.vtkJoglPanelComponent;

public class RenderView extends JPanel
{
	List<vtkProp> props=Lists.newArrayList();
	RenderStatusBar statusbar=new RenderStatusBar();
	RenderToolbar toolbar=new RenderToolbar();
	RenderPanel renderPanel=new RenderPanel(toolbar);//,statusbar);
	
	public RenderView()
	{
		this.setLayout(new BorderLayout());
		this.add(toolbar, BorderLayout.NORTH);
		this.add(renderPanel.getComponent(), BorderLayout.CENTER);
		this.add(statusbar, BorderLayout.SOUTH);
	}
	
	public vtkJoglPanelComponent getRenderPanel()
	{
		return renderPanel;
	}
	
	public void registerProp(vtkProp prop)
	{
		props.add(prop);
		renderPanel.getRenderer().AddActor(prop);
	}
	
	public void viewAll()
	{
		renderPanel.getRenderer().ResetCamera();
	}
}

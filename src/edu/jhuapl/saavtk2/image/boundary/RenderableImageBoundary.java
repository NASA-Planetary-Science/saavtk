package edu.jhuapl.saavtk2.image.boundary;
/*
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.Renderable;
import edu.jhuapl.saavtk.util.Properties;
import vtk.vtkActor;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataWriter;
import vtk.vtkProp;
import vtk.vtkUnsignedCharArray;

public class RenderableImageBoundary implements Renderable
{
	PropertyChangeSupport pcs=new PropertyChangeSupport(this);

	boolean showBoundary=true;
	double boundaryOpacity=1;
	vtkPolyDataMapper boundaryMapper=new vtkPolyDataMapper();

	static final double defaultOffset=0.001;
	static final double defaultPointSize=3;
	double offset;
	Vector3D offsetUnitVector;
	Vector3D initialCentroid;

	vtkActor boundaryActor=new vtkActor();
	ImageBoundary boundary;
	Color color;
	
	public RenderableImageBoundary(ImageBoundary boundary)
	{
		this.boundary=boundary;
		boundaryMapper.SetInputData(boundary.getPolyDataRepresentation());
		boundaryMapper.SetColorModeToDirectScalars();
		boundaryActor.SetMapper(boundaryMapper);
		boundaryActor.GetProperty().SetPointSize(defaultPointSize);
	}
	
	public boolean getVisibility()
	{
		return showBoundary;
	}
	
	public void setVisibility(boolean show)
	{
		showBoundary=show;
		boundaryActor.SetVisibility(getVisibility()?1:0);
		fireModelChangedEvent();
	}

	@Override
	public List<vtkProp> getProps()
	{
		return Lists.newArrayList(boundaryActor);
	}

	public double getOpacity()
	{
		return boundaryOpacity;
	}
	

	@Override
	public void setOpacity(double boundaryOpacity)
	{
		this.boundaryOpacity = boundaryOpacity;
		boundaryActor.GetProperty().SetOpacity(boundaryOpacity);
		fireModelChangedEvent();
	}
	
	
	protected void fireModelChangedEvent()
	{
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.removePropertyChangeListener(listener);
	}

	@Override
	public boolean isBuiltIn()
	{
		return false;
	}

	@Override
	public boolean isVisible()
	{
		return getVisibility();
	}

	@Override
	public void setVisible(boolean b)
	{
		setVisibility(b);
	}

	@Override
	public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
	{
		return "none";
	}

	@Override
	public void setOffset(double offset)
	{
		this.offset=offset;
	}

	@Override
	public double getOffset()
	{
		return offset;
	}

	@Override
	public double getDefaultOffset()
	{
		return defaultOffset;
	}

	@Override
	public void delete()
	{
		
	}

	@Override
	public void set2DMode(boolean enable)
	{
		
	}

	@Override
	public boolean supports2DMode()
	{
		return false;
	}

	public void setColor(Color color)
	{
		this.color=color;
		System.out.println(color);
		fireModelChangedEvent();
	}

}
*/
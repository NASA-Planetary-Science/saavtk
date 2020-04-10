package edu.jhuapl.saavtk.gui.render;

import java.util.Collection;

import vtk.vtkProp;

/**
 * Interface that allows an object to declare that it will provide
 * {@link vtkProp}s.
 *
 * @author lopeznr1
 */
public interface VtkPropProvider
{
	/**
	 * Returns the collection of {@link vtkProp}s provided by this object.
	 */
	public Collection<vtkProp> getProps();

}

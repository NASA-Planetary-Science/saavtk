package edu.jhuapl.saavtk.structure.vtk;

import edu.jhuapl.saavtk.vtk.VtkResource;
import vtk.vtkPolyData;

/**
 * Interface which defines standardized methods associated with painting of structures.
 *
 * @author lopeznr1
 */
public interface VtkStructurePainter extends VtkResource
{
	/**
	 * Returns the exterior (decimated) vtkPolyData.
	 */
	public vtkPolyData vtkGetExteriorDecPD();

	/**
	 * Returns the exterior (regular) vtkPolyData.
	 */
	public vtkPolyData vtkGetExteriorRegPD();

	/**
	 * Returns the interior (decimated) vtkPolyData.
	 */
	public vtkPolyData vtkGetInteriorDecPD();

	/**
	 * Returns the interior (regular) vtkPolyData.
	 */
	public vtkPolyData vtkGetInteriorRegPD();

}

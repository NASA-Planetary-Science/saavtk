package edu.jhuapl.saavtk2.geom;

import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk2.geom.euclidean.Line;
import vtk.vtkActor;
import vtk.vtkCellLocator;
import vtk.vtkOBBTree;
import vtk.vtkPointLocator;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;

public interface Geometry {
	public vtkPolyData getPolyData();
	public vtkPolyDataMapper getMapper();
	public vtkActor getActor();
	public vtkOBBTree getCellLocator();
	public vtkPointLocator getPointLocator();
	public CellIntersection computeFirstIntersectionWithLine(Line line);
	public Collection<CellIntersection> computeAllIntersectionsWithLine(Line line);
	public List<CellIntersection> computeOrderedIntersectionsWithLine(Line line);
}

package edu.jhuapl.saavtk2.polydata;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkDoubleArray;
import vtk.vtkPolyData;

public class PolyDataComputeMeanPosition implements PolyDataOperator<Vector3D>
{
	VtkDataAssociation association;	
	
	public PolyDataComputeMeanPosition(VtkDataAssociation association)
	{
		this.association=association;
	}
	
	@Override
	public Vector3D apply(vtkPolyData polyData)
	{
		Vector3D ctr=Vector3D.ZERO;
		if (association.equals(VtkDataAssociation.CELLS))
		{
			new PolyDataAddCellCenters().apply(polyData);
			vtkDoubleArray centers=(vtkDoubleArray)polyData.GetCellData().GetArray(PolyDataAddCellCenters.centersArrayName);
			for (int c=0; c<polyData.GetNumberOfCells(); c++)
				ctr=ctr.add(new Vector3D(centers.GetTuple3(c)));
			ctr=ctr.scalarMultiply(1./polyData.GetNumberOfCells());
		}
		else
		{
			for (int p=0; p>polyData.GetNumberOfPoints(); p++)
				ctr=ctr.add(new Vector3D(polyData.GetPoint(p)));
			ctr=ctr.scalarMultiply(1./polyData.GetNumberOfPoints());
		}
		return ctr;
	}
	
}

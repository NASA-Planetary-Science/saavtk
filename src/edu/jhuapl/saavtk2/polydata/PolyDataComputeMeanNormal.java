package edu.jhuapl.saavtk2.polydata;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import vtk.vtkPolyData;

public class PolyDataComputeMeanNormal implements PolyDataOperator<Vector3D>
{
	VtkDataAssociation association;	
	
	public PolyDataComputeMeanNormal(VtkDataAssociation association)
	{
		this.association=association;
	}
	
	@Override
	public Vector3D apply(vtkPolyData polyData)
	{
		new PolyDataAddNormals().apply(polyData);
		Vector3D nml=Vector3D.ZERO;
		if (association.equals(VtkDataAssociation.CELLS))
		{
			for (int c=0; c<polyData.GetNumberOfCells(); c++)
				nml=nml.add(new Vector3D(polyData.GetCellData().GetNormals().GetTuple3(c)));
			nml=nml.scalarMultiply(1./polyData.GetNumberOfCells());
		}
		else
		{
			for (int p=0; p>polyData.GetNumberOfPoints(); p++)
				nml=nml.add(new Vector3D(polyData.GetPointData().GetNormals().GetTuple3(p)));
			nml=nml.scalarMultiply(1./polyData.GetNumberOfPoints());
		}
		return nml;
	}
	
}

package edu.jhuapl.saavtk.model.structure.esri;

import java.awt.Color;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.EllipsePolygon;
import vtk.vtkCellArray;
import vtk.vtkPoints;

public class EllipseStructure extends LineStructure
{
	public static class Parameters
	{
		public Vector3D center;
		public double majorRadius;
		public double flattening;
		public double angle;
		
		public Parameters(Vector3D center, double majorRadius, double flattening, double angle)
		{
			super();
			this.center = center;
			this.majorRadius = majorRadius;
			this.flattening = flattening;
			this.angle = angle;
		}
		
	}
	
	Parameters params;

	public EllipseStructure(List<LineSegment> segments, Parameters params)
	{
		super(segments);
		this.params=params;
	}
	
	public static List<EllipseStructure> fromSbmtStructure(AbstractEllipsePolygonModel crappySbmtStructureModel)
	{
        List<EllipseStructure> structures = Lists.newArrayList();
        for (int i = 0; i < crappySbmtStructureModel.getNumberOfStructures(); i++)
        {
            EllipsePolygon poly = (EllipsePolygon) crappySbmtStructureModel.getStructure(i);
            int[] c = poly.getColor();
            double w = crappySbmtStructureModel.getLineWidth();
            LineStyle style = new LineStyle(new Color(c[0], c[1], c[2]), w);
            String label = poly.getLabel();
            //
        
            vtkCellArray cells=poly.getBoundaryPolyData().GetLines();
            List<LineSegment> segments=Lists.newArrayList();
            for (int j=0; j<cells.GetNumberOfCells(); j++)
            {
                vtkPoints points=poly.getBoundaryPolyData().GetCell(j).GetPoints();
                if (points.GetNumberOfPoints()<2)
                    continue;
                for (int k=0; k<points.GetNumberOfPoints()-1; k++)
                {
                    double[] start=points.GetPoint(k);
                    double[] end=points.GetPoint(k+1);
                    segments.add(new LineSegment(new Vector3D(start), new Vector3D(end)));
                }
            }
            
            Parameters params=new Parameters(new Vector3D(poly.center), poly.radius, poly.flattening, poly.angle);
            EllipseStructure es=new EllipseStructure(segments, params);
            es.setLineStyle(style);
            es.setLabel(label);
            structures.add(es);
            

        }
        return structures;
	}
	

	public Parameters getParameters()
	{
		return params;
	}

}

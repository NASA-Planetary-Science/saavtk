package edu.jhuapl.saavtk.model.structure.esri;

import java.awt.Color;
import java.util.List;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.EllipsePolygon;
import edu.jhuapl.saavtk.model.structure.esri.EllipseStructure.Parameters;
import vtk.vtkCellArray;
import vtk.vtkPoints;

public class EllipseStructure extends LineStructure
{
	public static class Parameters
	{
		public double[] center;
		public double majorRadius;
		public double flattening;
		public double angle;
		
		public Parameters(double[] center, double majorRadius, double flattening, double angle)
		{
			super();
			this.center = center;
			this.majorRadius = majorRadius;
			this.flattening = flattening;
			this.angle = angle;
		}
		
	}
	
	Parameters params;

	public EllipseStructure(LineSegment[] segments, LineStyle style, String label, Parameters params)
	{
		super(segments, style, label);
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
                    segments.add(new LineSegment(start, end));
                }
            }
            LineSegment[] segmentArr=new LineSegment[segments.size()];
            for (int j=0; j<segments.size(); j++)
                segmentArr[j]=segments.get(j);
            
            Parameters params=new Parameters(poly.center, poly.radius, poly.flattening, poly.angle);
            structures.add(new EllipseStructure(segmentArr, style, label, params));
            

        }
        return structures;
	}
	

	public EllipseStructure(LineSegment[] segments, String label, Parameters params)
	{
		super(segments, label);
		this.params=params;
	}

	public EllipseStructure(LineSegment[] segments, Parameters params)
	{
		super(segments);
		this.params=params;
	}
	
	public Parameters getParameters()
	{
		return params;
	}

}

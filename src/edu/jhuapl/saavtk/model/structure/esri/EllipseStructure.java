package edu.jhuapl.saavtk.model.structure.esri;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.EllipseManager;
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
		this.params = params;
	}

	public static List<EllipseStructure> fromSbmtStructure(EllipseManager aEllipseManager)
	{
		var structures = new ArrayList<EllipseStructure>();

		for (var aEllipse : aEllipseManager.getAllItems())
			structures.add(fromSbmtStructure(aEllipseManager, aEllipse));

		return structures;
	}

	/**
	 * Utility method that takes an {@link Ellipse} (and it's manager) and returns the corresponding
	 * {@link EllipseStructure}.
	 */
	public static EllipseStructure fromSbmtStructure(EllipseManager aEllipseManager, Ellipse aEllipse)
	{
		Color c = aEllipse.getColor();
		double w = aEllipseManager.getLineWidth();
		LineStyle style = new LineStyle(c, w);
		String label = aEllipse.getLabel();
		//

		var tmpPolyData = aEllipseManager.getVtkExteriorPolyDataFor(aEllipse);
		vtkCellArray cells = tmpPolyData.GetLines();
		List<LineSegment> segments = Lists.newArrayList();
		for (int j = 0; j < cells.GetNumberOfCells(); j++)
		{
			vtkPoints points = tmpPolyData.GetCell(j).GetPoints();
			if (points.GetNumberOfPoints() < 2)
				continue;
			for (int k = 0; k < points.GetNumberOfPoints() - 1; k++)
			{
				double[] start = points.GetPoint(k);
				double[] end = points.GetPoint(k + 1);
				segments.add(new LineSegment(new Vector3D(start), new Vector3D(end)));
			}
		}

		var params = new Parameters(aEllipse.getCenter(), aEllipse.getRadius(), aEllipse.getFlattening(),
				aEllipse.getAngle());
		var retItem = new EllipseStructure(segments, params);
		retItem.setLineStyle(style);
		retItem.setLabel(label);
		return retItem;
	}

	public Parameters getParameters()
	{
		return params;
	}

}

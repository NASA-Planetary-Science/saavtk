package edu.jhuapl.saavtk.model.structure.esri;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.util.MathUtil;

public class LineStructure implements Structure
{
	List<LineSegment> segments;
	Vector3D centroid;
	LineStyle lineStyle;
	PointStyle pointStyle;
	String label;
	List<Vector3D> controlPoints;

	public LineStructure(List<LineSegment> segments)
	{
		this(segments, Lists.newArrayList());
	}

	public LineStructure(List<LineSegment> segments, List<Vector3D> controlPoints)
	{
		this.segments = segments;
		this.controlPoints = controlPoints;
		this.centroid = StructureUtil.centroidOfSegments(segments);
		this.lineStyle = new LineStyle();
		this.pointStyle = new PointStyle();
	}

	public int getNumberOfSegments()
	{
		return segments.size();
	}

	public LineSegment getSegment(int i)
	{
		return segments.get(i);
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	@Override
	public Vector3D getCentroid()
	{
		return centroid;
	}

	public LineStyle getLineStyle()
	{
		return lineStyle;
	}

	public void setLineStyle(LineStyle lineStyle)
	{
		this.lineStyle = lineStyle;
	}

	public PointStyle getPointStyle()
	{
		return pointStyle;
	}

	public void setPointStyle(PointStyle pointStyle)
	{
		this.pointStyle = pointStyle;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public int getNumberOfControlPoints()
	{
		if (controlPoints == null)
			return 0;
		else
			return controlPoints.size();
	}

	public Vector3D getControlPoint(int i)
	{
		return controlPoints.get(i);
	}

	public static <G1 extends PolyLine> List<LineStructure> fromSbmtStructure(LineModel<G1> model)
	{
		var structures = new ArrayList<LineStructure>();
		for (var aPolyLine : model.getAllItems())
			structures.add(fromSbmtStructure(model, aPolyLine));

		return structures;
	}

	/**
	 * Utility method that takes a {@link PolyLine} (and it's manager) and returns the corresponding
	 * {@link LineStructure}.
	 */
	public static <G1 extends PolyLine> LineStructure fromSbmtStructure(LineModel<G1> model, G1 poly)
	{
		List<Vector3D> xyzPointL = model.getXyzPointsFor(poly);

		List<LineSegment> segments = Lists.newArrayList();
		for (int j = 0; j < xyzPointL.size() - 1; j++)
		{
			Vector3D begPt = xyzPointL.get(j);
			Vector3D endPt = xyzPointL.get(j + 1);
			segments.add(new LineSegment(begPt, endPt));
		}

		List<Vector3D> controlPoints = Lists.newArrayList();
		for (int m = 0; m < controlPoints.size(); m++)
			controlPoints.add(new Vector3D(MathUtil.latrec(poly.getControlPoints().get(m))));

		LineStructure ls = new LineStructure(segments, controlPoints);
		Color c = poly.getColor();
		double w = model.getLineWidth();
		LineStyle style = new LineStyle(c, w);
		String label = poly.getLabel();
		ls.setLineStyle(style);
		ls.setLabel(label);
		return ls;
	}

}

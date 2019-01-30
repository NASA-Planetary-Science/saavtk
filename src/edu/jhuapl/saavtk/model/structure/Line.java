package edu.jhuapl.saavtk.model.structure;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import crucible.crust.settings.api.Configuration;
import crucible.crust.settings.api.ContentKey;
import crucible.crust.settings.api.KeyValueCollection;
import crucible.crust.settings.api.SettableValue;
import crucible.crust.settings.api.Version;
import crucible.crust.settings.impl.Configurations;
import crucible.crust.settings.impl.KeyValueCollections;
import crucible.crust.settings.impl.SettableValues;
import crucible.crust.settings.impl.Utilities;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.StructureModel;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.Point3D;
import vtk.vtkCaptionActor2D;
import vtk.vtkPoints;
import vtk.vtkPolyData;

public class Line extends StructureModel.Structure
{
	private static final Version CONFIGURATION_VERSION = Version.of(1, 0);
	private static final SettableValues settableValues = SettableValues.instance();

	public static Configuration<KeyValueCollection<SettableValue<?>>> createConfiguration(int id, int[] color)
	{
		KeyValueCollections.Builder<SettableValue<?>> builder = KeyValueCollections.instance().builder(Utilities.getSpecificType(SettableValue.class));

		builder.put(ID, settableValues.of(id));
		builder.put(COLOR, settableValues.of(color));

		return Configurations.instance().of(CONFIGURATION_VERSION, builder.build());
	}

	public String name = "default";
	public String label = "";

	// Note that controlPoints is what gets stored in the saved file.
	public List<LatLon> controlPoints = new ArrayList<LatLon>();

	// Note xyzPointList is what's displayed. There will usually be more of these points than
	// controlPoints in order to ensure the line is right above the surface of the asteroid.
	public List<Point3D> xyzPointList = new ArrayList<Point3D>();
	public List<Integer> controlPointIds = new ArrayList<Integer>();
	public boolean hidden = false;
	public boolean labelHidden = false;

	private PolyhedralModel smallBodyModel;

	private static final int[] purpleColor = { 255, 0, 255, 255 }; // RGBA purple
	protected static final DecimalFormat decimalFormatter = new DecimalFormat("#.###");

	private boolean closed = false;
	public vtkCaptionActor2D caption;
	private static int maxId = 0;

	public static final String PATH = "path";
	public static final ContentKey<SettableValue<Integer>> ID = settableValues.key("id");
	public static final ContentKey<SettableValue<String>> NAME = settableValues.key("name");
	public static final ContentKey<SettableValue<String>> VERTICES = settableValues.key("vertices");
	public static final String LENGTH = "length";
	public static final ContentKey<SettableValue<int[]>> COLOR = settableValues.key("color");
	public static final ContentKey<SettableValue<String>> LABEL = settableValues.key("label");
	public static final String LABELCOLOR = "labelcolor";

	private final Configuration<KeyValueCollection<SettableValue<?>>> configuration;

	public Line(PolyhedralModel smallBodyModel, boolean closed, int id)
	{
		this.configuration = createConfiguration(id, purpleColor.clone());
		this.smallBodyModel = smallBodyModel;
		this.closed = closed;
	}

	@Override
	public int getId()
	{
		return configuration.getContent().getValue(ID).getValue();
	}

	private void setId(int id)
	{
		configuration.getContent().getValue(ID).setValue(id);
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	@Override
	public void setLabel(String label)
	{
		this.label = label;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String getType()
	{
		return PATH;
	}

	@Override
	public String getInfo()
	{
		return decimalFormatter.format(getPathLength()) + " km, " + controlPointIds.size() + " vertices";
	}

	@Override
	public int[] getColor()
	{
		return configuration.getContent().getValue(COLOR).getValue().clone();
	}

	@Override
	public void setColor(int[] color)
	{
		configuration.getContent().getValue(COLOR).setValue(color.clone());
	}

	public Element toXmlDomElement(Document dom)
	{
		Element linEle = dom.createElement(getType());
		linEle.setAttribute(ID.getId(), String.valueOf(getId()));
		linEle.setAttribute(NAME.getId(), name);
		linEle.setAttribute(LABEL.getId(), label);
		//        String labelcolorStr=labelcolor[0] + "," + labelcolor[1] + "," + labelcolor[2];
		//        linEle.setAttribute(LABELCOLOR, labelcolorStr);
		linEle.setAttribute(LENGTH, String.valueOf(getPathLength()));

		int[] color = getColor();
		String colorStr = color[0] + "," + color[1] + "," + color[2];
		linEle.setAttribute(COLOR.getId(), colorStr);

		String vertices = "";
		int size = controlPoints.size();

		for (int i = 0; i < size; ++i)
		{
			LatLon ll = controlPoints.get(i);
			double latitude = ll.lat * 180.0 / Math.PI;
			double longitude = ll.lon * 180.0 / Math.PI;
			if (longitude < 0.0)
				longitude += 360.0;

			vertices += latitude + " " + longitude + " " + ll.rad;

			if (i < size - 1)
				vertices += " ";
		}

		linEle.setAttribute(VERTICES.getId(), vertices);

		return linEle;
	}

	public void fromXmlDomElement(Element element, String shapeModelName, boolean append)
	{
		controlPoints.clear();
		controlPointIds.clear();
		xyzPointList.clear();

		int id = getId();
		if (!append)
		{ // If appending, simply use maxId
			id = Integer.parseInt(element.getAttribute(ID.getId()));
			setId(id);
		}
		if (id > maxId)
			maxId = id;

		name = element.getAttribute(NAME.getId());
		label = element.getAttribute(LABEL.getId());
		String tmp = element.getAttribute(VERTICES.getId());

		if (tmp.length() == 0)
			return;

		String[] tokens = tmp.split(" ");

		int count = 0;
		for (int i = 0; i < tokens.length;)
		{
			double lat = Double.parseDouble(tokens[i++]) * Math.PI / 180.0;
			double lon = Double.parseDouble(tokens[i++]) * Math.PI / 180.0;
			double rad = Double.parseDouble(tokens[i++]);
			controlPoints.add(new LatLon(lat, lon, rad));

			if (shapeModelName == null || !shapeModelName.equals(smallBodyModel.getModelName()))
				shiftPointOnPathToClosestPointOnAsteroid(count);

			controlPointIds.add(xyzPointList.size());

			// Note, this point will be replaced with the correct values
			// when we call updateSegment
			double[] dummy = { 0.0, 0.0, 0.0 };
			xyzPointList.add(new Point3D(dummy));

			if (count > 0)
				this.updateSegment(count - 1);

			++count;
		}

		if (closed)
		{
			// In CLOSED mode need to add segment connecting final point to initial point
			this.updateSegment(controlPointIds.size() - 1);
		}

		tmp = element.getAttribute(COLOR.getId());
		if (tmp.length() == 0)
			return;
		tokens = tmp.split(",");

		int[] color = { Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]) };
		setColor(color);

		//        String[] labelColors=element.getAttribute(LABELCOLOR).split(",");
		//        labelcolor[0] = Double.parseDouble(labelColors[0]);
		//        labelcolor[1] = Double.parseDouble(labelColors[1]);
		//        labelcolor[2] = Double.parseDouble(labelColors[2]);

	}

	@Override
	public String getClickStatusBarText()
	{
		return "Path, Id = " + getId() + ", Length = " + decimalFormatter.format(getPathLength()) + " km" + ", Number of Vertices = " + controlPoints.size();
	}

	public double getPathLength()
	{
		int size = xyzPointList.size();
		double length = 0.0;

		for (int i = 1; i < size; ++i)
		{
			double dist = xyzPointList.get(i - 1).distanceTo(xyzPointList.get(i));
			length += dist;
		}

		if (closed && size > 1)
		{
			double dist = xyzPointList.get(size - 1).distanceTo(xyzPointList.get(0));
			length += dist;
		}

		return length;
	}

	public void updateSegment(int segment)
	{
		int nextSegment = segment + 1;
		if (nextSegment == controlPoints.size())
			nextSegment = 0;

		LatLon ll1 = controlPoints.get(segment);
		LatLon ll2 = controlPoints.get(nextSegment);
		double pt1[] = MathUtil.latrec(ll1);
		double pt2[] = MathUtil.latrec(ll2);

		int id1 = controlPointIds.get(segment);
		int id2 = controlPointIds.get(nextSegment);

		// Set the 2 control points
		xyzPointList.set(id1, new Point3D(pt1));
		xyzPointList.set(id2, new Point3D(pt2));

		vtkPoints points = null;
		if (Math.abs(ll1.lat - ll2.lat) < 1e-8 && Math.abs(ll1.lon - ll2.lon) < 1e-8 && Math.abs(ll1.rad - ll2.rad) < 1e-8)
		{
			points = new vtkPoints();
			points.InsertNextPoint(pt1);
			points.InsertNextPoint(pt2);
		}
		else
		{
			vtkPolyData poly = smallBodyModel.drawPath(pt1, pt2);
			if (poly == null)
				return;

			points = poly.GetPoints();
		}

		// Remove points BETWEEN the 2 control points
		int numberPointsToRemove = id2 - id1 - 1;
		if (nextSegment == 0)
			numberPointsToRemove = xyzPointList.size() - id1 - 1;
		for (int i = 0; i < numberPointsToRemove; ++i)
		{
			xyzPointList.remove(id1 + 1);
		}

		// Set the new points
		int numNewPoints = points.GetNumberOfPoints();
		for (int i = 1; i < numNewPoints - 1; ++i)
		{
			xyzPointList.add(id1 + i, new Point3D(points.GetPoint(i)));
		}

		// Shift the control points ids from segment+1 till the end by the right amount.
		int shiftAmount = id1 + numNewPoints - 1 - id2;
		for (int i = segment + 1; i < controlPointIds.size(); ++i)
		{
			controlPointIds.set(i, controlPointIds.get(i) + shiftAmount);
		}

	}

	public void shiftPointOnPathToClosestPointOnAsteroid(int idx)
	{
		// When the resolution changes, the control points, might no longer
		// be touching the asteroid. Therefore shift each control to the closest
		// point on the asteroid.
		LatLon llr = controlPoints.get(idx);
		double pt[] = MathUtil.latrec(llr);
		double[] closestPoint = smallBodyModel.findClosestPoint(pt);
		LatLon ll = MathUtil.reclat(closestPoint);
		controlPoints.set(idx, ll);
	}

	public double[] getCentroid()
	{
		int size = controlPoints.size();

		double[] centroid = { 0.0, 0.0, 0.0 };
		for (int i = 0; i < size; ++i)
		{
			LatLon ll = controlPoints.get(i);
			double[] p = MathUtil.latrec(ll);
			centroid[0] += p[0];
			centroid[1] += p[1];
			centroid[2] += p[2];
		}

		centroid[0] /= size;
		centroid[1] /= size;
		centroid[2] /= size;

		double[] closestPoint = smallBodyModel.findClosestPoint(centroid);

		return closestPoint;
	}

	public double getSize()
	{
		int size = controlPoints.size();

		double[] centroid = getCentroid();
		double maxDistFromCentroid = 0.0;
		for (int i = 0; i < size; ++i)
		{
			LatLon ll = controlPoints.get(i);
			double[] p = MathUtil.latrec(ll);
			double dist = MathUtil.distanceBetween(centroid, p);
			if (dist > maxDistFromCentroid)
				maxDistFromCentroid = dist;
		}
		return maxDistFromCentroid;
	}

	@Override
	public boolean getHidden()
	{
		return hidden;
	}

	@Override
	public boolean getLabelHidden()
	{
		return labelHidden;
	}

	@Override
	public void setHidden(boolean b)
	{
		hidden = b;
	}

	@Override
	public void setLabelHidden(boolean b)
	{
		labelHidden = b;
	}

	public int getNumberOfPoints()
	{
		return xyzPointList.size();
	}

	public Vector3D getPoint(int i)
	{
		return new Vector3D(xyzPointList.get(i).xyz);
	}
}

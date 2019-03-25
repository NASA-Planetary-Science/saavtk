package edu.jhuapl.saavtk.model.structure;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.settings.api.Configuration;
import crucible.crust.settings.api.Content;
import crucible.crust.settings.api.ContentKey;
import crucible.crust.settings.api.KeyValueCollection;
import crucible.crust.settings.api.SettableValue;
import crucible.crust.settings.api.Value;
import crucible.crust.settings.api.Version;
import crucible.crust.settings.impl.Configurations;
import crucible.crust.settings.impl.KeyValueCollections;
import crucible.crust.settings.impl.SettableValues;
import crucible.crust.settings.impl.Values;
import crucible.crust.settings.impl.metadata.KeyValueCollectionMetadataManager;
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
	// Note that controlPoints is what gets stored in the saved file.
	private final List<LatLon> controlPoints = new ArrayList<>();

	// Note xyzPointList is what's displayed. There will usually be more of these points than
	// controlPoints in order to ensure the line is right above the surface of the asteroid.
	public List<Point3D> xyzPointList = new ArrayList<>();
	public List<Integer> controlPointIds = new ArrayList<>();

	private static final int[] purpleColor = { 255, 0, 255, 255 }; // RGBA purple
	protected static final DecimalFormat decimalFormatter = new DecimalFormat("#.###");

	private vtkCaptionActor2D caption;
	private static int maxId = 0;

	public static final String PATH = "path";
	public static final String LENGTH = "length";

	public static final ContentKey<SettableValue<Integer>> ID = SettableValues.key("id");
	public static final ContentKey<SettableValue<String>> NAME = SettableValues.key("name");
	public static final ContentKey<Value<List<LatLon>>> VERTICES = Values.fixedKey("vertices");
	public static final ContentKey<SettableValue<int[]>> COLOR = SettableValues.key("color");
	public static final ContentKey<SettableValue<String>> LABEL = SettableValues.key("label");
	public static final ContentKey<SettableValue<int[]>> LABEL_COLOR = SettableValues.key("labelColor");
	public static final ContentKey<SettableValue<Boolean>> HIDDEN = SettableValues.key("hidden");
	public static final ContentKey<SettableValue<Boolean>> LABEL_HIDDEN = SettableValues.key("labelHidden");

	private final Configuration configuration;

	public Line(int id)
	{
		this.configuration = createConfiguration(id, controlPoints, purpleColor.clone());
	}

	protected Configuration getConfiguration()
	{
		return configuration;
	}

	@Override
	public int getId()
	{
		return configuration.getCollection().getValue(ID).getValue();
	}

	private void setId(int id)
	{
		configuration.getCollection().getValue(ID).setValue(id);
	}

	@Override
	public String getLabel()
	{
		return configuration.getCollection().getValue(LABEL).getValue();
	}

	@Override
	public void setLabel(String label)
	{
		configuration.getCollection().getValue(LABEL).setValue(label);
	}

	@Override
	public int[] getLabelColor()
	{
		return configuration.getCollection().getValue(LABEL_COLOR).getValue();
	}

	@Override
	public void setLabelColor(int[] labelColor)
	{
		configuration.getCollection().getValue(LABEL_COLOR).setValue(labelColor);
	}

	@Override
	public String getName()
	{
		return configuration.getCollection().getValue(NAME).getValue();
	}

	@Override
	public void setName(String name)
	{
		configuration.getCollection().getValue(NAME).setValue(name);
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
		return configuration.getCollection().getValue(COLOR).getValue().clone();
	}

	@Override
	public void setColor(int[] color)
	{
		configuration.getCollection().getValue(COLOR).setValue(color.clone());
	}

	public ImmutableList<LatLon> getControlPoints()
	{
		return ImmutableList.copyOf(controlPoints);
	}

	public void setControlPoint(int index, LatLon controlPoint)
	{
		Preconditions.checkArgument(index >= 0 && index < controlPoints.size());

		controlPoints.set(index, controlPoint);
	}

	public void addControlPoint(int index, LatLon controlPoint)
	{
		// One past last is OK for this one.
		Preconditions.checkArgument(index >= 0 && index <= controlPoints.size());

		controlPoints.add(index, controlPoint);
	}

	public void removeControlPoint(int index)
	{
		Preconditions.checkArgument(index >= 0 && index < controlPoints.size());

		controlPoints.remove(index);
	}

	public Element toXmlDomElement(Document dom)
	{
		Element linEle = dom.createElement(getType());
		linEle.setAttribute(ID.getId(), String.valueOf(getId()));
		linEle.setAttribute(NAME.getId(), getName());
		linEle.setAttribute(LABEL.getId(), getLabel());
		//        String labelcolorStr=labelcolor[0] + "," + labelcolor[1] + "," + labelcolor[2];
		//        linEle.setAttribute(LABELCOLOR, labelcolorStr);
		linEle.setAttribute(LENGTH, String.valueOf(getPathLength()));

		int[] color = getColor();
		String colorStr = color[0] + "," + color[1] + "," + color[2];
		linEle.setAttribute(COLOR.getId(), colorStr);

		String vertices = "";
		int size = getControlPoints().size();

		for (int i = 0; i < size; ++i)
		{
			LatLon ll = getControlPoints().get(i);
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

	public void fromXmlDomElement(PolyhedralModel smallBodyModel, Element element, String shapeModelName, boolean append)
	{
		controlPoints.clear();
		controlPointIds.clear();
		xyzPointList.clear();

		int id = getId();
		if (!append)
		{ // If appending, use id as-is
			id = Integer.parseInt(element.getAttribute(ID.getId()));
			setId(id);
		}
		if (id > maxId)
			maxId = id;

		setName(element.getAttribute(NAME.getId()));
		setLabel(element.getAttribute(LABEL.getId()));
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
			addControlPoint(count, new LatLon(lat, lon, rad));

			if (shapeModelName == null || !shapeModelName.equals(smallBodyModel.getModelName()))
				shiftPointOnPathToClosestPointOnAsteroid(smallBodyModel, count);

			controlPointIds.add(xyzPointList.size());

			// Note, this point will be replaced with the correct values
			// when we call updateSegment
			double[] dummy = { 0.0, 0.0, 0.0 };
			xyzPointList.add(new Point3D(dummy));

			if (count > 0)
				this.updateSegment(smallBodyModel, count - 1);

			++count;
		}

		if (isClosed())
		{
			// In CLOSED mode need to add segment connecting final point to initial point
			this.updateSegment(smallBodyModel, controlPointIds.size() - 1);
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
		return "Path, Id = " + getId() + ", Length = " + decimalFormatter.format(getPathLength()) + " km" + ", Number of Vertices = " + getControlPoints().size();
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

		if (isClosed() && size > 1)
		{
			double dist = xyzPointList.get(size - 1).distanceTo(xyzPointList.get(0));
			length += dist;
		}

		return length;
	}

	public void updateAllSegments(PolyhedralModel smallBodyModel)
	{
		controlPointIds.clear();
		xyzPointList.clear();

		int numberSegments = isClosed() ? controlPoints.size() : controlPoints.size();

		for (int index = 0; index < numberSegments; ++index)
		{
			shiftPointOnPathToClosestPointOnAsteroid(smallBodyModel, index);

			controlPointIds.add(xyzPointList.size());

			// Note, this point will be replaced with the correct values
			// when we call updateSegment
			double[] dummy = { 0.0, 0.0, 0.0 };
			xyzPointList.add(new Point3D(dummy));

			if (index > 0)
			{
				updateSegment(smallBodyModel, index - 1);
			}
		}

		if (isClosed())
		{
			updateSegment(smallBodyModel, controlPointIds.size() - 1);
		}
	}

	public void updateSegment(PolyhedralModel smallBodyModel, int segment)
	{
		int nextSegment = segment + 1;
		if (nextSegment == getControlPoints().size())
			nextSegment = 0;

		LatLon ll1 = getControlPoints().get(segment);
		LatLon ll2 = getControlPoints().get(nextSegment);
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

	public void shiftPointOnPathToClosestPointOnAsteroid(PolyhedralModel smallBodyModel, int idx)
	{
		// When the resolution changes, the control points, might no longer
		// be touching the asteroid. Therefore shift each control to the closest
		// point on the asteroid.
		LatLon llr = getControlPoints().get(idx);
		double pt[] = MathUtil.latrec(llr);
		double[] closestPoint = smallBodyModel.findClosestPoint(pt);
		LatLon ll = MathUtil.reclat(closestPoint);
		setControlPoint(idx, ll);
	}

	@Override
	public double[] getCentroid(PolyhedralModel smallBodyModel)
	{
		int size = getControlPoints().size();

		double[] centroid = { 0.0, 0.0, 0.0 };
		for (int i = 0; i < size; ++i)
		{
			LatLon ll = getControlPoints().get(i);
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

	public double getSize(PolyhedralModel smallBodyModel)
	{
		int size = getControlPoints().size();

		double[] centroid = getCentroid(smallBodyModel);
		double maxDistFromCentroid = 0.0;
		for (int i = 0; i < size; ++i)
		{
			LatLon ll = getControlPoints().get(i);
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
		return configuration.getCollection().getValue(HIDDEN).getValue();
	}

	@Override
	public boolean getLabelHidden()
	{
		return configuration.getCollection().getValue(LABEL_HIDDEN).getValue();
	}

	@Override
	public void setHidden(boolean b)
	{
		configuration.getCollection().getValue(HIDDEN).setValue(b);
	}

	@Override
	public void setLabelHidden(boolean b)
	{
		configuration.getCollection().getValue(LABEL_HIDDEN).setValue(b);
	}

	public int getNumberOfPoints()
	{
		return xyzPointList.size();
	}

	public Vector3D getPoint(int i)
	{
		return new Vector3D(xyzPointList.get(i).xyz);
	}

	protected boolean isClosed()
	{
		return false;
	}

	@Override
	public vtkCaptionActor2D getCaption()
	{
		return caption;
	}

	@Override
	public void setCaption(vtkCaptionActor2D caption)
	{
		this.caption = caption;
	}

	private static final Version CONFIGURATION_VERSION = Version.of(1, 0);
	private static final SettableValues settableValues = SettableValues.instance();

	public static Configuration createConfiguration(int id, List<LatLon> controlPoints, int[] color)
	{
		KeyValueCollections.Builder<Content> builder = KeyValueCollections.instance().builder();

		builder.put(ID, settableValues.of(id));
		builder.put(NAME, settableValues.of("default"));
		// Note: it is correct to use settableValues to instantiate the setting for VERTICES. This is because
		// the list of controlPoints is final but mutable. If one used just "Values", the set of vertices would
		// not be saved in the file because it would not be considered "stateful".
		builder.put(VERTICES, settableValues.of(controlPoints));
		builder.put(COLOR, settableValues.of(color));
		builder.put(LABEL, settableValues.of(""));
		builder.put(LABEL_COLOR, settableValues.of(BLACK_INT_ARRAY.clone()));
		builder.put(HIDDEN, settableValues.of(false));
		builder.put(LABEL_HIDDEN, settableValues.of(false));

		return Configurations.instance().of(CONFIGURATION_VERSION, builder.build());
	}

	private static final Key<Line> LINE_STRUCTURE_PROXY_KEY = Key.of("Line (structure)");
	private static boolean proxyInitialized = false;

	public static void initializeSerializationProxy()
	{
		if (!proxyInitialized)
		{
			LatLon.initializeSerializationProxy();

			InstanceGetter.defaultInstanceGetter().register(LINE_STRUCTURE_PROXY_KEY, source -> {
				int id = source.get(Key.of(ID.getId()));

				Line result = new Line(id);

				unpackMetadata(source, result);

				return result;
			}, Line.class, line -> {
				Configuration configuration = line.configuration;
				return KeyValueCollectionMetadataManager.of(configuration.getVersion(), configuration.getCollection()).store();
			});

			proxyInitialized = true;
		}
	}

	protected static void unpackMetadata(Metadata source, Line line)
	{
		KeyValueCollection<Content> collection = line.configuration.getCollection();

		collection.getValue(NAME).setValue(source.get(Key.of(NAME.getId())));
		collection.getValue(COLOR).setValue(source.get(Key.of(COLOR.getId())));

		List<LatLon> sourceControlPoints = source.get(Key.of(VERTICES.getId()));
		List<LatLon> controlPoints = collection.getValue(VERTICES).getValue();

		controlPoints.addAll(sourceControlPoints);

		collection.getValue(LABEL).setValue(source.get(Key.of(LABEL.getId())));
		collection.getValue(LABEL_COLOR).setValue(source.get(Key.of(LABEL_COLOR.getId())));
		collection.getValue(HIDDEN).setValue(source.get(Key.of(HIDDEN.getId())));
		collection.getValue(LABEL_HIDDEN).setValue(source.get(Key.of(LABEL_HIDDEN.getId())));

	}
}

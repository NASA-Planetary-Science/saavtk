package edu.jhuapl.saavtk.model.structure;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.settings.api.Configuration;
import crucible.crust.settings.api.Content;
import crucible.crust.settings.api.ContentKey;
import crucible.crust.settings.api.KeyValueCollection;
import crucible.crust.settings.api.SettableValue;
import crucible.crust.settings.impl.Configurations;
import crucible.crust.settings.impl.KeyValueCollections;
import crucible.crust.settings.impl.SettableValues;
import crucible.crust.settings.impl.metadata.KeyValueCollectionMetadataManager;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import vtk.vtkClipPolyData;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkQuadricClustering;
import vtk.vtkSelectPolyData;

public abstract class Polygon extends Line
{
	public static Polygon of(int id)
	{
		final ArrayList<LatLon> controlPoints = new ArrayList<>();
		final Configuration configuration = createConfiguration(id, controlPoints);

		return new Polygon(controlPoints) {

			@Override
			public Configuration getConfiguration()
			{
				return configuration;
			}

		};
	}

	public vtkPolyData interiorPolyData;
	public vtkPolyData decimatedInteriorPolyData;

	private static final String POLYGON = "polygon";
	private static final String AREA = "area";

	protected Polygon(List<LatLon> controlPoints)
	{
		super(controlPoints);

		interiorPolyData = new vtkPolyData();
		decimatedInteriorPolyData = new vtkPolyData();
	}

	@Override
	public String getType()
	{
		return POLYGON;
	}

	@Override
	public String getInfo()
	{
		double surfaceArea = getContent(AREA_KEY).getValue();
		return "Area: " + decimalFormatter.format(surfaceArea) + " km^2, Length: " + decimalFormatter.format(getPathLength()) + " km, " + getControlPoints().size() + " vertices";
	}

	@Override
	public String getClickStatusBarText()
	{
		double surfaceArea = getContent(AREA_KEY).getValue();
		return "Polygon, Id = " + getId() + ", Length = " + decimalFormatter.format(getPathLength()) + " km" + ", Surface Area = " + decimalFormatter.format(surfaceArea) + " km^2" + ", Number of Vertices = " + getControlPoints().size();
	}

	public void setShowInterior(PolyhedralModel smallBodyModel, boolean showInterior)
	{
		getContent(SHOW_INTERIOR_KEY).setValue(showInterior);

		updateInteriorPolydata(smallBodyModel);

		if (showInterior)
		{
			// Decimate interiorPolyData for LODs
			vtkQuadricClustering decimator = new vtkQuadricClustering();
			decimator.SetInputData(interiorPolyData);
			decimator.AutoAdjustNumberOfDivisionsOn();
			decimator.CopyCellDataOn();
			decimator.Update();
			decimatedInteriorPolyData.DeepCopy(decimator.GetOutput());
			decimator.Delete();
		}
		else
		{
			PolyDataUtil.clearPolyData(interiorPolyData);
			PolyDataUtil.clearPolyData(decimatedInteriorPolyData);
		}
	}

	protected void updateInteriorPolydata(PolyhedralModel smallBodyModel)
	{
		SettableValue<Double> areaValue = getContent(AREA_KEY);
		double surfaceArea = areaValue.getValue();

		if (isShowInterior())
		{
			vtkPoints pts = new vtkPoints();
			for (int i = 0; i < xyzPointList.size(); i++)
			{
				pts.InsertNextPoint(xyzPointList.get(i).xyz);
			}

			vtkSelectPolyData loop = new vtkSelectPolyData();
			loop.SetInputData(smallBodyModel.getSmallBodyPolyData());
			loop.SetLoop(pts);
			loop.GenerateSelectionScalarsOn();
			loop.SetSelectionModeToSmallestRegion();
			loop.Update();
			vtkClipPolyData clipper = new vtkClipPolyData();
			clipper.SetInputData(loop.GetOutput());
			clipper.InsideOutOn();
			clipper.GenerateClipScalarsOff();
			clipper.Update();
			interiorPolyData = clipper.GetOutput();
			surfaceArea = PolyDataUtil.computeSurfaceArea(interiorPolyData);
		}

		areaValue.setValue(surfaceArea);
	}

	public boolean isShowInterior()
	{
		return getContent(SHOW_INTERIOR_KEY).getValue();
	}

	@Override
	public Element toXmlDomElement(Document dom)
	{
		Element element = super.toXmlDomElement(dom);
		double surfaceArea = getContent(AREA_KEY).getValue();
		element.setAttribute(AREA, String.valueOf(surfaceArea));

		return element;
	}

	@Override
	public void fromXmlDomElement(PolyhedralModel smallBodyModel, Element element, String shapeModelName, boolean append)
	{
		super.fromXmlDomElement(smallBodyModel, element, shapeModelName, append);

		double surfaceArea = element.hasAttribute(AREA) ? surfaceArea = Double.parseDouble(element.getAttribute(AREA)) : null;
		getContent(AREA_KEY).setValue(surfaceArea);
	}

	@Override
	protected boolean isClosed()
	{
		return true;
	}

	protected static Configuration createConfiguration(int id, List<LatLon> controlPoints)
	{
		Configuration lineConfiguration = Line.createConfiguration(id, controlPoints);
		KeyValueCollection<Content> collection = lineConfiguration.getCollection();

		KeyValueCollections.Builder<Content> builder = KeyValueCollections.instance().builder();
		for (ContentKey<? extends Content> key : collection.getKeys())
		{
			@SuppressWarnings("unchecked")
			ContentKey<Content> contentKey = (ContentKey<Content>) key;
			builder.put(contentKey, collection.getValue(contentKey));
		}
		builder.put(AREA_KEY, SettableValues.instance().of(0.));
		builder.put(SHOW_INTERIOR_KEY, SettableValues.instance().of(Boolean.FALSE));

		return Configurations.instance().of(lineConfiguration.getVersion(), builder.build());
	}

	private static final Key<Polygon> POLYGON_STRUCTURE_PROXY_KEY = Key.of("Polygon");
	public static final ContentKey<SettableValue<Double>> AREA_KEY = SettableValues.key("area");
	public static final ContentKey<SettableValue<Boolean>> SHOW_INTERIOR_KEY = SettableValues.key("showInterior");
	private static boolean proxyInitialized = false;

	public static void initializeSerializationProxy()
	{
		if (!proxyInitialized)
		{
			LatLon.initializeSerializationProxy();

			InstanceGetter.defaultInstanceGetter().register(POLYGON_STRUCTURE_PROXY_KEY, source -> {
				int id = source.get(Key.of(ID.getId()));
				Polygon result = of(id);
				unpackMetadata(source, result);

				return result;
			}, Polygon.class, polygon -> {
				Configuration configuration = polygon.getConfiguration();
				return KeyValueCollectionMetadataManager.of(configuration.getVersion(), configuration.getCollection()).store();
			});

			proxyInitialized = true;
		}
	}

	protected static void unpackMetadata(Metadata source, Polygon polygon)
	{
		Line.unpackMetadata(source, polygon);

		KeyValueCollection<Content> collection = polygon.getConfiguration().getCollection();

		Key<Double> areaKey = Key.of(AREA_KEY.getId());
		if (source.hasKey(areaKey))
		{
			collection.getValue(AREA_KEY).setValue(source.get(areaKey));
		}

		Key<Boolean> showInteriorKey = Key.of(SHOW_INTERIOR_KEY.getId());
		if (source.hasKey(showInteriorKey))
		{
			collection.getValue(SHOW_INTERIOR_KEY).setValue(source.get(showInteriorKey));
		}
	}
}

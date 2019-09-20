package edu.jhuapl.saavtk.model.structure;

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
import vtk.vtkCleanPolyData;
import vtk.vtkClipPolyData;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkQuadricClustering;
import vtk.vtkSelectPolyData;

public class Polygon extends Line
{
	// State vars
	private boolean showInterior;
	private double surfaceArea;

	// VTK vars
	protected vtkPolyData vInteriorRegPD;
	protected final vtkPolyData vInteriorDecPD;

	private static final String POLYGON = "polygon";
	private static final String AREA = "area";

	/**
	 * Standard Constructor
	 */
	public Polygon(int aId)
	{
		super(aId);

		showInterior = false;
		surfaceArea = 0.0;

		vInteriorRegPD = new vtkPolyData();
		vInteriorDecPD = new vtkPolyData();
	}

	public boolean isShowInterior()
	{
		return showInterior;
	}

	@Override
	public String getType()
	{
		return POLYGON;
	}

	@Override
	public String getInfo()
	{
		return "Area: " + decimalFormatter.format(surfaceArea) + " km^2, Length: "
				+ decimalFormatter.format(getPathLength()) + " km, " + getControlPoints().size() + " vertices";
	}

	@Override
	public String getClickStatusBarText()
	{
		return "Polygon, Id = " + getId() + ", Length = " + decimalFormatter.format(getPathLength()) + " km"
				+ ", Surface Area = " + decimalFormatter.format(surfaceArea) + " km^2" + ", Number of Vertices = "
				+ getControlPoints().size();
	}

	public void setShowInterior(PolyhedralModel smallBodyModel, boolean aShowInterior)
	{
		showInterior = aShowInterior;

		updateInteriorPolydata(smallBodyModel);

		if (showInterior)
		{
			// Decimate interiorPolyData for LODs
			vtkQuadricClustering decimator = new vtkQuadricClustering();
			decimator.SetInputData(vInteriorRegPD);
			decimator.AutoAdjustNumberOfDivisionsOn();
			decimator.CopyCellDataOn();
			decimator.Update();
			vInteriorDecPD.DeepCopy(decimator.GetOutput());
			decimator.Delete();
		}
		else
		{
			PolyDataUtil.clearPolyData(vInteriorRegPD);
			PolyDataUtil.clearPolyData(vInteriorDecPD);
		}
	}

	protected void updateInteriorPolydata(PolyhedralModel smallBodyModel)
	{
		// Bail if no interior
		if (isShowInterior() == false)
			return;

		vtkPoints pts = new vtkPoints();
		for (int i = 0; i < xyzPointList.size(); i++)
		{
			pts.InsertNextPoint(xyzPointList.get(i).xyz);
		}

		// Clean the poly data here before selecting the interior facets.
		vtkCleanPolyData cleanPoly = new vtkCleanPolyData();
		cleanPoly.SetInputData(smallBodyModel.getSmallBodyPolyData());
		cleanPoly.Update();
		vtkPolyData cleanPolyData = cleanPoly.GetOutput();

		vtkSelectPolyData loop = new vtkSelectPolyData();
		loop.SetInputData(cleanPolyData);
		loop.SetLoop(pts);
		loop.GenerateSelectionScalarsOn();
		loop.SetSelectionModeToSmallestRegion();
		loop.Update();
		vtkClipPolyData clipper = new vtkClipPolyData();
		clipper.SetInputData(loop.GetOutput());
		clipper.InsideOutOn();
		clipper.GenerateClipScalarsOff();
		clipper.Update();
		vInteriorRegPD = clipper.GetOutput();
		surfaceArea = PolyDataUtil.computeSurfaceArea(vInteriorRegPD);
	}

	@Override
	public Element toXmlDomElement(Document dom)
	{
		Element element = super.toXmlDomElement(dom);
		element.setAttribute(AREA, String.valueOf(surfaceArea));

		return element;
	}

	@Override
	public void fromXmlDomElement(PolyhedralModel smallBodyModel, Element element, String shapeModelName, boolean append)
	{
		super.fromXmlDomElement(smallBodyModel, element, shapeModelName, append);

		surfaceArea = element.hasAttribute(AREA) ? surfaceArea = Double.parseDouble(element.getAttribute(AREA)) : null;
	}

	@Override
	protected boolean isClosed()
	{
		return true;
	}

	private static Configuration formConfigurationFor(Polygon aPolygon)
	{
		Configuration lineConfiguration = Line.formConfigurationFor(aPolygon);
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
	private static boolean proxyInitialized = false;

	public static void initializeSerializationProxy()
	{
		if (!proxyInitialized)
		{
			LatLon.initializeSerializationProxy();

			InstanceGetter.defaultInstanceGetter().register(POLYGON_STRUCTURE_PROXY_KEY, source -> {
				int id = source.get(Key.of(ID.getId()));
				Polygon result = new Polygon(id);
				unpackMetadata(source, result);

				return result;
			}, Polygon.class, polygon -> {
//                Configuration configuration = polygon.getConfiguration();
				Configuration configuration = formConfigurationFor(polygon);
				return KeyValueCollectionMetadataManager.of(configuration.getVersion(), configuration.getCollection())
						.store();
			});

			proxyInitialized = true;
		}
	}

	public static final ContentKey<SettableValue<Double>> AREA_KEY = SettableValues.key("area");
	public static final ContentKey<SettableValue<Boolean>> SHOW_INTERIOR_KEY = SettableValues.key("showInterior");

	protected static void unpackMetadata(Metadata source, Polygon polygon)
	{
		Line.unpackMetadata(source, polygon);

		Key<Double> areaKey = Key.of(AREA_KEY.getId());
		if (source.hasKey(areaKey))
			polygon.surfaceArea = source.get(areaKey);

		Key<Boolean> showInteriorKey = Key.of(SHOW_INTERIOR_KEY.getId());
		if (source.hasKey(showInteriorKey))
			polygon.showInterior = source.get(showInteriorKey);
	}
}

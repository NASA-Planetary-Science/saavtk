package edu.jhuapl.saavtk.model.structure;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.settings.impl.Utilities;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import vtk.vtkClipPolyData;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkQuadricClustering;
import vtk.vtkSelectPolyData;

public class Polygon extends Line
{
	public vtkPolyData interiorPolyData;
	public vtkPolyData decimatedInteriorPolyData;
	private double surfaceArea = 0.0;
	private boolean showInterior = false;

	private static final String POLYGON = "polygon";
	private static final String AREA = "area";

	public Polygon(int id)
	{
		super(id);

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
		return "Area: " + decimalFormatter.format(surfaceArea) + " km^2, Length: " + decimalFormatter.format(getPathLength()) + " km, " + getControlPoints().size() + " vertices";
	}

	@Override
	public String getClickStatusBarText()
	{
		return "Polygon, Id = " + getId() + ", Length = " + decimalFormatter.format(getPathLength()) + " km" + ", Surface Area = " + decimalFormatter.format(surfaceArea) + " km^2" + ", Number of Vertices = " + getControlPoints().size();
	}

	public void setShowInterior(PolyhedralModel smallBodyModel, boolean showInterior)
	{
		this.showInterior = showInterior;

		if (showInterior)
		{
			//            smallBodyModel.drawPolygon(controlPoints, interiorPolyData, null);
			//            surfaceArea = PolyDataUtil.computeSurfaceArea(interiorPolyData);

			updateInteriorPolydata(smallBodyModel);

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
			surfaceArea = 0.0;
		}
	}

	protected void updateInteriorPolydata(PolyhedralModel smallBodyModel)
	{
		if (!showInterior)
			return;

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

	public boolean isShowInterior()
	{
		return showInterior;
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

		updateInteriorPolydata(smallBodyModel);
	}

	@Override
	protected boolean isClosed()
	{
		return true;
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
				return Utilities.provide(polygon.getConfiguration(), MetadataManager.class).store();
			});

			proxyInitialized = true;
		}
	}
}

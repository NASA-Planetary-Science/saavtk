package edu.jhuapl.saavtk.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import vtk.vtkPolyData;
import edu.jhuapl.saavtk.util.PolyDataUtil;

public class Polygon extends Line
{
    public vtkPolyData interiorPolyData;
    private PolyhedralModel smallBodyModel;
    private double surfaceArea = 0.0;
    private boolean showInterior = false;

    public static final String POLYGON = "polygon";
    public static final String AREA = "area";

    public Polygon(PolyhedralModel smallBodyModel, int id)
    {
        super(smallBodyModel, true, id);

        this.smallBodyModel = smallBodyModel;
        interiorPolyData = new vtkPolyData();
    }

    public String getType()
    {
        return POLYGON;
    }

    public String getInfo()
    {
        return "Area: " + decimalFormatter.format(surfaceArea) + " km^2, Length: " + decimalFormatter.format(getPathLength()) + " km, " + controlPoints.size() + " vertices";
    }

    public String getClickStatusBarText()
    {
        return "Polygon, Id = " + id
        + ", Length = " + decimalFormatter.format(getPathLength()) + " km"
        + ", Surface Area = " + decimalFormatter.format(surfaceArea) + " km^2"
        + ", Number of Vertices = " + controlPoints.size();
    }

    public void setShowInterior(boolean showInterior)
    {
        this.showInterior = showInterior;

        if (showInterior)
        {
            smallBodyModel.drawPolygon(controlPoints, interiorPolyData, null);
            surfaceArea = PolyDataUtil.computeSurfaceArea(interiorPolyData);
        }
        else
        {
            PolyDataUtil.clearPolyData(interiorPolyData);
            surfaceArea = 0.0;
        }
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
    public void fromXmlDomElement(Element element, String shapeModelName,
            boolean append)
    {
        super.fromXmlDomElement(element, shapeModelName, append);
        if (element.hasAttribute(AREA))
            surfaceArea = Double.parseDouble(element.getAttribute(AREA));
        else
            surfaceArea = 0.0;
    }
}

package edu.jhuapl.saavtk.model.structure;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import vtk.vtkCaptionActor2D;
import vtk.vtkPoints;
import vtk.vtkPolyData;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.StructureModel;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.Point3D;

public class Line extends StructureModel.Structure
{
    public String name = "default";
    public String label = "";
    public int id;

    // Note that controlPoints is what gets stored in the saved file.
    public List<LatLon> controlPoints = new ArrayList<LatLon>();

    // Note xyzPointList is what's displayed. There will usually be more of these points than
    // controlPoints in order to ensure the line is right above the surface of the asteroid.
    public List<Point3D> xyzPointList = new ArrayList<Point3D>();
    public List<Integer> controlPointIds = new ArrayList<Integer>();
    public int[] color;
    public double[] labelcolor={1,1,1};
    public boolean hidden = false;
    public int labelId=-1;
    public boolean editingLabel=false;
    public boolean labelHidden=false;

    private PolyhedralModel smallBodyModel;

    private static final int[] purpleColor = {255, 0, 255, 255}; // RGBA purple
    protected static final DecimalFormat decimalFormatter = new DecimalFormat("#.###");

    private boolean closed = false;
    public vtkCaptionActor2D caption;
    private static int maxId = 0;

    public static final String PATH = "path";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String VERTICES = "vertices";
    public static final String LENGTH = "length";
    public static final String COLOR = "color";
    public static final String LABEL = "label";
    public static final String LABELCOLOR = "labelcolor";

    public Line(PolyhedralModel smallBodyModel, boolean closed, int id)
    {
        this.smallBodyModel = smallBodyModel;
        this.closed = closed;
        this.id = id;
        color = (int[])purpleColor.clone();
    }

    public int getId()
    {
        return id;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return PATH;
    }

    public String getInfo()
    {
        return decimalFormatter.format(getPathLength()) + " km, " + controlPointIds.size() + " vertices";
    }

    public int[] getColor()
    {
        return color;
    }

    public void setColor(int[] color)
    {
        this.color = (int[])color.clone();
    }

    public Element toXmlDomElement(Document dom)
    {
        Element linEle = dom.createElement(getType());
        linEle.setAttribute(ID, String.valueOf(id));
        linEle.setAttribute(NAME, name);
        linEle.setAttribute(LABEL, label);
//        String labelcolorStr=labelcolor[0] + "," + labelcolor[1] + "," + labelcolor[2];
//        linEle.setAttribute(LABELCOLOR, labelcolorStr);
        linEle.setAttribute(LENGTH, String.valueOf(getPathLength()));

        String colorStr = color[0] + "," + color[1] + "," + color[2];
        linEle.setAttribute(COLOR, colorStr);

        String vertices = "";
        int size = controlPoints.size();

        for (int i=0;i<size;++i)
        {
            LatLon ll = controlPoints.get(i);
            double latitude = ll.lat*180.0/Math.PI;
            double longitude = ll.lon*180.0/Math.PI;
            if (longitude < 0.0)
                longitude += 360.0;

            vertices += latitude + " " + longitude + " " + ll.rad;

            if (i < size-1)
                vertices += " ";
        }

        linEle.setAttribute(VERTICES, vertices);

        return linEle;
    }

    public void fromXmlDomElement(Element element, String shapeModelName, boolean append)
    {
        controlPoints.clear();
        controlPointIds.clear();
        xyzPointList.clear();

        if (!append) // If appending, simply use maxId
            id = Integer.parseInt(element.getAttribute(ID));

        if (id > maxId)
            maxId = id;

        name = element.getAttribute(NAME);
        label = element.getAttribute(LABEL);
        String tmp = element.getAttribute(VERTICES);

        if (tmp.length() == 0)
            return;

        String[] tokens = tmp.split(" ");

        int count = 0;
        for (int i=0; i<tokens.length;)
        {
            double lat = Double.parseDouble(tokens[i++])*Math.PI/180.0;
            double lon = Double.parseDouble(tokens[i++])*Math.PI/180.0;
            double rad = Double.parseDouble(tokens[i++]);
            controlPoints.add(new LatLon(lat, lon, rad));

            if (shapeModelName == null || !shapeModelName.equals(smallBodyModel.getModelName()))
                shiftPointOnPathToClosestPointOnAsteroid(count);

            controlPointIds.add(xyzPointList.size());

            // Note, this point will be replaced with the correct values
            // when we call updateSegment
            double[] dummy = {0.0, 0.0, 0.0};
            xyzPointList.add(new Point3D(dummy));

            if (count > 0)
                this.updateSegment(count-1);

            ++count;
        }

        if (closed)
        {
            // In CLOSED mode need to add segment connecting final point to initial point
            this.updateSegment(controlPointIds.size()-1);
        }

        tmp = element.getAttribute(COLOR);
        if (tmp.length() == 0)
            return;
        tokens = tmp.split(",");
        color[0] = Integer.parseInt(tokens[0]);
        color[1] = Integer.parseInt(tokens[1]);
        color[2] = Integer.parseInt(tokens[2]);

//        String[] labelColors=element.getAttribute(LABELCOLOR).split(",");
//        labelcolor[0] = Double.parseDouble(labelColors[0]);
//        labelcolor[1] = Double.parseDouble(labelColors[1]);
//        labelcolor[2] = Double.parseDouble(labelColors[2]);

    }

    public String getClickStatusBarText()
    {
        return "Path, Id = " + id
        + ", Length = " + decimalFormatter.format(getPathLength()) + " km"
        + ", Number of Vertices = " + controlPoints.size();
    }

    public double getPathLength()
    {
        int size = xyzPointList.size();
        double length = 0.0;

        for (int i=1;i<size;++i)
        {
            double dist = xyzPointList.get(i-1).distanceTo(xyzPointList.get(i));
            length += dist;
        }

        if (closed && size > 1)
        {
            double dist = xyzPointList.get(size-1).distanceTo(xyzPointList.get(0));
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
        if (Math.abs(ll1.lat - ll2.lat) < 1e-8 &&
                Math.abs(ll1.lon - ll2.lon) < 1e-8 &&
                Math.abs(ll1.rad - ll2.rad) < 1e-8)
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
        int numberPointsToRemove = id2-id1-1;
        if (nextSegment == 0)
            numberPointsToRemove = xyzPointList.size()-id1-1;
        for (int i=0; i<numberPointsToRemove; ++i)
        {
            xyzPointList.remove(id1+1);
        }

        // Set the new points
        int numNewPoints = points.GetNumberOfPoints();
        for (int i=1; i<numNewPoints-1; ++i)
        {
            xyzPointList.add(id1+i, new Point3D(points.GetPoint(i)));
        }

        // Shift the control points ids from segment+1 till the end by the right amount.
        int shiftAmount = id1+numNewPoints-1 - id2;
        for (int i=segment+1; i<controlPointIds.size(); ++i)
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

        double[] centroid = {0.0, 0.0, 0.0};
        for (int i=0;i<size;++i)
        {
            LatLon ll = controlPoints.get(i);
            double[] p = MathUtil.latrec(ll);
            centroid[0] += p[0];
            centroid[1] += p[1];
            centroid[2] += p[2];
        }

        centroid[0] /= (double)size;
        centroid[1] /= (double)size;
        centroid[2] /= (double)size;

        double[] closestPoint = smallBodyModel.findClosestPoint(centroid);

        return closestPoint;
    }

    public double getSize()
    {
        int size = controlPoints.size();

        double[] centroid = getCentroid();
        double maxDistFromCentroid = 0.0;
        for (int i=0;i<size;++i)
        {
            LatLon ll = controlPoints.get(i);
            double[] p = MathUtil.latrec(ll);
            double dist = MathUtil.distanceBetween(centroid, p);
            if (dist > maxDistFromCentroid)
                maxDistFromCentroid = dist;
        }
        return maxDistFromCentroid;
    }

    public boolean getHidden()
    {
        return hidden;
    }

    public boolean getLabelHidden()
    {
        return labelHidden;
    }

    public void setHidden(boolean b)
    {
        hidden = b;
    }

    public void setLabelHidden(boolean b)
    {
        labelHidden=b;
    }
}

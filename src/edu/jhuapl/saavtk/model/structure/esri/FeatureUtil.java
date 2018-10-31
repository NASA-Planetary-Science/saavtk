package edu.jhuapl.saavtk.model.structure.esri;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.crs.DefaultGeocentricCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.CitationFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.structure.Line;
import edu.jhuapl.saavtk.model.structure.esri.EllipseStructure.Parameters;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;

public class FeatureUtil
{
    public final static SimpleFeatureType pointType;
    public final static SimpleFeatureType lineType;
    public final static SimpleFeatureType ellipseType;
    //	public final static SimpleFeatureType	patchType;
    //	private final static SimpleFeatureType polyhedralType;

    static
    {
        SimpleFeatureTypeBuilder pointTypeBuilder = new SimpleFeatureTypeBuilder();
        pointTypeBuilder.setName("POINT");
        pointTypeBuilder.setCRS(DefaultGeographicCRS.WGS84_3D);
        pointTypeBuilder.add("the_geom", com.vividsolutions.jts.geom.Point.class);
        pointTypeBuilder.add("label", String.class);
        pointTypeBuilder.add("color", String.class);
        pointTypeBuilder.add("size", Double.class);
        pointType = pointTypeBuilder.buildFeatureType();
        //
        SimpleFeatureTypeBuilder lineTypeBuilder = new SimpleFeatureTypeBuilder();
        lineTypeBuilder.setName("LINE");
        lineTypeBuilder.setCRS(DefaultGeographicCRS.WGS84_3D);
        lineTypeBuilder.add("the_geom", MultiLineString.class);
        lineTypeBuilder.add("label", String.class);
        lineTypeBuilder.add("linecolor", String.class);
        lineTypeBuilder.add("linewidth", Double.class);
        lineType = lineTypeBuilder.buildFeatureType();
        //
        SimpleFeatureTypeBuilder ellipseTypeBuilder = new SimpleFeatureTypeBuilder();
        ellipseTypeBuilder.setName("ELLIPSE");
        ellipseTypeBuilder.setCRS(DefaultGeographicCRS.WGS84_3D);
        ellipseTypeBuilder.add("the_geom", MultiLineString.class);
        ellipseTypeBuilder.add("label", String.class);
        ellipseTypeBuilder.add("linecolor", String.class);
        ellipseTypeBuilder.add("linewidth", Double.class);
        ellipseTypeBuilder.add("centerx", Double.class);
        ellipseTypeBuilder.add("centery", Double.class);
        ellipseTypeBuilder.add("centerz", Double.class);
        ellipseTypeBuilder.add("majorRad", Double.class);
        ellipseTypeBuilder.add("flattening", Double.class);
        ellipseTypeBuilder.add("angle", Double.class);
        ellipseType = ellipseTypeBuilder.buildFeatureType();
        //
        //		SimpleFeatureTypeBuilder patchTypeBuilder = new SimpleFeatureTypeBuilder();
        //		patchTypeBuilder.setName("PATCH");
        //		patchTypeBuilder.setCRS(DefaultGeographicCRS.WGS84);
        //		patchTypeBuilder.add("the_geom", Polygon.class);
        //		patchTypeBuilder.add("label", String.class);
        //		patchTypeBuilder.add("linecolor", String.class);
        //		patchTypeBuilder.add("linewidth", Double.class);
        //		patchTypeBuilder.add("fillcolor", String.class);
        //		patchType = patchTypeBuilder.buildFeatureType();
        //		//
        /*		SimpleFeatureTypeBuilder polyhedralTypeBuilder = new SimpleFeatureTypeBuilder();
        		polyhedralTypeBuilder.setName("PATCH");
        		polyhedralTypeBuilder.setCRS(DefaultEngineeringCRS.CARTESIAN_3D);
        		polyhedralTypeBuilder.add("the_geom",MultiPolygon.class);
        		polyhedralTypeBuilder.add("label", String.class);
        		polyhedralTypeBuilder.add("linecolor", String.class);
        		polyhedralTypeBuilder.add("linewidth", Double.class);
        		polyhedralTypeBuilder.add("fillcolor", String.class);
        		polyhedralType=polyhedralTypeBuilder.buildFeatureType();*/
    }

    private static SimpleFeatureBuilder pointBuilder = new SimpleFeatureBuilder(pointType);
    private static SimpleFeatureBuilder lineBuilder = new SimpleFeatureBuilder(lineType);
    private static SimpleFeatureBuilder ellipseBuilder = new SimpleFeatureBuilder(ellipseType);
    //	private static SimpleFeatureBuilder	patchBuilder	= new SimpleFeatureBuilder(patchType);
    //	private static SimpleFeatureBuilder polyhedralBuilder=new SimpleFeatureBuilder(polyhedralType);

    public static SimpleFeature createFeatureFrom(PointStructure ps)
    {
        double[] location = ps.getCentroid();
        LatLon latlon = MathUtil.reclat(location);
        Geometry geom = JTSFactoryFinder.getGeometryFactory().createPoint(new Coordinate(Math.toDegrees(latlon.lon), Math.toDegrees(latlon.lat)));
        // the coordinates are now in the DefaultGeocentric.CARTESIAN CRS
        pointBuilder.reset();
        pointBuilder.add(geom);
        pointBuilder.add(ps.getLabel());
        pointBuilder.add(StructureUtil.colorToHex(ps.getPointStyle().getColor()));
        pointBuilder.add(ps.getPointStyle().getSize());
        return pointBuilder.buildFeature(null);
    }

    public static SimpleFeature createMultiLineString(List<List<Vector3D>> lines)
    {
        List<LineString> lineStrings = Lists.newArrayList();
        for (int m = 0; m < lines.size(); m++)
        {
            List<Vector3D> linePoints = lines.get(m);

            for (int i = 0; i < linePoints.size() - 1; i++)
            {

                Vector3D p1 = linePoints.get(i);
                Vector3D p2 = linePoints.get(i + 1);
                Coordinate[] coords = new Coordinate[2];
                LatLon ll1 = MathUtil.reclat(p1.toArray());
                LatLon ll2 = MathUtil.reclat(p2.toArray());
                coords[0] = new Coordinate(Math.toDegrees(ll1.lon),Math.toDegrees(ll1.lat));//p1.getX(), p1.getY(), p1.getZ());
                coords[1] = new Coordinate(Math.toDegrees(ll2.lon),Math.toDegrees(ll2.lat));//p2.getX(), p2.getY(), p2.getZ());
                // the coordinates are now in the DefaultGeocentric.CARTESIAN CRS
                lineStrings.add(JTSFactoryFinder.getGeometryFactory().createLineString(coords));
            }
        }
        LineString[] ls = new LineString[lineStrings.size()];
        lineStrings.toArray(ls);
        // see createFeatureFrom(ls) with LineStructure argument
        Geometry geom = JTSFactoryFinder.getGeometryFactory().createMultiLineString(ls);
        lineBuilder.add(geom);
        lineBuilder.add("no label");
        lineBuilder.add(StructureUtil.colorToHex(new Color(100, 100, 100)));
        lineBuilder.add(1);
        return lineBuilder.buildFeature(null);

    }

    /*	public static SimpleFeature createPointFeatureFrom(List<Vector3D> points)
    	{
    	    for (int i=0; i<points.size(); i++)
    	    {
    	        double[] location = points.get(i).toArray();
    	        Geometry geom = JTSFactoryFinder.getGeometryFactory().
                .createPoint(new Coordinate(location[0], location[1], location[2]));
    	    }
        // the coordinates are now in the DefaultGeocentric.CARTESIAN CRS
        pointBuilder.reset();
        pointBuilder.add(geom);
        pointBuilder.add("fcuk");
        pointBuilder.add(StructureUtil.colorToHex(Color.LIGHT_GRAY));
        pointBuilder.add(1);
        return pointBuilder.buildFeature(null);
    	    
    	}*/

    public static SimpleFeature createFeatureFrom(LineStructure ls)
    {
        LineString[] lines = new LineString[ls.getNumberOfSegments()];
        for (int i = 0; i < ls.getNumberOfSegments(); i++)
        {
            LineSegment s = ls.getSegment(i);
            Coordinate[] coords = new Coordinate[2];
            LatLon ll1=MathUtil.reclat(s.getStart());
            LatLon ll2=MathUtil.reclat(s.getEnd());
            coords[0] = new Coordinate(Math.toDegrees(ll1.lon),Math.toDegrees(ll1.lat));//s.getStart()[0] * 1000, s.getStart()[1] * 1000, s.getStart()[2] * 1000);
            coords[1] = new Coordinate(Math.toDegrees(ll2.lon),Math.toDegrees(ll2.lat));//s.getEnd()[0] * 1000, s.getEnd()[1] * 1000, s.getEnd()[2] * 1000);
            // the coordinates are now in the DefaultGeocentric.CARTESIAN CRS
            lines[i] = JTSFactoryFinder.getGeometryFactory().createLineString(coords);
        }
        Geometry geom = JTSFactoryFinder.getGeometryFactory().createMultiLineString(lines);
        lineBuilder.add(geom);
        lineBuilder.add(ls.getLabel());
        lineBuilder.add(StructureUtil.colorToHex(ls.getStyle().getLineColor()));
        lineBuilder.add(ls.getStyle().getLineWidth());
        return lineBuilder.buildFeature(null);
    }

    public static SimpleFeature createFeatureFrom(EllipseStructure es)
    {
        LineString[] lines = new LineString[es.getNumberOfSegments()];
        for (int i = 0; i < es.getNumberOfSegments(); i++)
        {
            LineSegment s = es.getSegment(i);
            Coordinate[] coords = new Coordinate[2];
            LatLon ll1=MathUtil.reclat(s.getStart());
            LatLon ll2=MathUtil.reclat(s.getEnd());
            coords[0] = new Coordinate(Math.toDegrees(ll1.lon),Math.toDegrees(ll1.lat));//s.getStart()[0] * 1000, s.getStart()[1] * 1000, s.getStart()[2] * 1000);
            coords[1] = new Coordinate(Math.toDegrees(ll2.lon),Math.toDegrees(ll2.lat));//s.getEnd()[0] * 1000, s.getEnd()[1] * 1000, s.getEnd()[2] * 1000);
            // the coordinates are now in the DefaultGeocentric.CARTESIAN CRS
            lines[i] = JTSFactoryFinder.getGeometryFactory().createLineString(coords);
        }
        Geometry geom = JTSFactoryFinder.getGeometryFactory().createMultiLineString(lines);
        ellipseBuilder.add(geom);
        ellipseBuilder.add(es.getLabel());
        ellipseBuilder.add(StructureUtil.colorToHex(es.getStyle().getLineColor()));
        ellipseBuilder.add(es.getStyle().getLineWidth());
        double[] center = es.getParameters().center;
        ellipseBuilder.add(center[0]);
        ellipseBuilder.add(center[1]);
        ellipseBuilder.add(center[2]);
        ellipseBuilder.add(es.getParameters().majorRadius);
        ellipseBuilder.add(es.getParameters().flattening);
        ellipseBuilder.add(es.getParameters().angle);
        return ellipseBuilder.buildFeature(null);
    }

    static final String defaultLabel = "";
    static final Color defaultColor = Color.CYAN;
    static final double defaultLineWidth = 1;

    public static EllipseStructure createEllipseStructureFrom(SimpleFeature feature, GenericPolyhedralModel body)
    {
        String label = defaultLabel;
        Color lineColor = defaultColor;
        double lineWidth = defaultLineWidth;
        try
        {
            label = (String) feature.getAttribute("label");
        }
        catch (Exception e)
        {
        }

        try
        {
            lineColor = StructureUtil.hexToColor((String) feature.getAttribute("linecolor"));
        }
        catch (Exception e)
        {
        }

        try
        {
            lineWidth = (Double) feature.getAttribute("linewidth");
        }
        catch (Exception e)
        {
        }
        LineStyle style = new LineStyle(lineColor, lineWidth);
        //

        List<LineSegment> segments = Lists.newArrayList();

        try
        {
            MathTransform transform = CRS.findMathTransform(feature.getType().getCoordinateReferenceSystem(), DefaultGeocentricCRS.CARTESIAN, true);

            Geometry geom = (Geometry) feature.getAttribute("the_geom");
            if (geom instanceof MultiLineString)
                segments = parse((MultiLineString) geom, transform, body);
            //else if (geom instanceof MultiPolygon)
            //    segments = parse((MultiPolygon) geom, transform, body);
            //

        }
        catch (FactoryException | TransformException e)
        {
            e.printStackTrace();
            return null;
        }

        List<Vector3D> pts = Lists.newArrayList();
        for (int i = 0; i < segments.size(); i++)
            pts.add(new Vector3D(segments.get(i).start));

        Vector3D center = Vector3D.ZERO;
        for (int i = 0; i < pts.size(); i++)
            center = center.add(pts.get(i));
        center = center.scalarMultiply(1. / (double) pts.size());
        //
        try
        {
            double x = (Double) feature.getAttribute("centerx");
            double y = (Double) feature.getAttribute("centery");
            double z = (Double) feature.getAttribute("centerz");
            center = new Vector3D(x, y, z);
        }
        catch (Exception e)
        {
        }

        Vector3D normal = Vector3D.ZERO;
        for (int i = 0; i < pts.size(); i++)
        {
            int ip = (i + 1) % pts.size();
            Vector3D radial = pts.get(i).subtract(center).normalize();
            Vector3D tangent = pts.get(ip).subtract(pts.get(i));
            normal = normal.add(radial.crossProduct(tangent));
        }
        normal = normal.normalize();

        double majorRadius = Double.NEGATIVE_INFINITY;
        double minorRadius = Double.POSITIVE_INFINITY;
        for (int i = 0; i < pts.size(); i++)
        {
            double r = pts.get(i).subtract(center).crossProduct(normal).getNorm();
            if (r > majorRadius)
                majorRadius = r;
            if (r < minorRadius)
                minorRadius = r;
        }


//        double majorRadius=0;
        
        try
        {
            majorRadius = (Double) feature.getAttribute("majorRad");
        }
        catch (Exception e)
        {

        }

        double flattening = minorRadius / majorRadius;//(majorRadius - minorRadius) / majorRadius;
       // double flattening=0;
        try
        {
            flattening = (Double) feature.getAttribute("flattening");
        }
        catch (Exception e)
        {

        }

        double angle = Math.acos(minorRadius / majorRadius);
        //double angle=0;
        try
        {
            angle = (Double) feature.getAttribute("angle");
        }
        catch (Exception e)
        {

        }

        Parameters params = new Parameters(center.toArray(), majorRadius, flattening, angle);
        //
        LineSegment[] segs = new LineSegment[segments.size()];
        for (int i = 0; i < segments.size(); i++)
            segs[i] = segments.get(i);
        return new EllipseStructure(segs, style, label, params);

    }


    protected static List<LineSegment> parse(MultiLineString mls, MathTransform transform, GenericPolyhedralModel body) throws TransformException
    {

        List<LineSegment> segments = Lists.newArrayList();

        for (int i = 0; i < mls.getNumGeometries(); i++)
        {
            LineString ls = (LineString) mls.getGeometryN(i);
            Coordinate[] coords = ls.getCoordinates();
            segments.addAll(convertCoordsToSegments(coords, transform, false, body));
        }

        return segments;
    }

    protected static List<LineSegment> parse(MultiPolygon mp, MathTransform transform, GenericPolyhedralModel body) throws TransformException 
    {
        List<LineSegment> segments = Lists.newArrayList();

        for (int i = 0; i < mp.getNumGeometries(); i++)
        {
            Polygon p = (Polygon) mp.getGeometryN(i);
            Coordinate[] coords = p.getCoordinates();
            segments.addAll(convertCoordsToSegments(coords, transform, false, body));
        }
        return segments;
    }

    protected static List<LineSegment> convertCoordsToSegments(Coordinate[] coords, MathTransform transform, boolean closed, GenericPolyhedralModel body) throws TransformException // TODO: this needs to interpret coords.x=lon and coords.y=lat and coords.z=derived from radial body-ray intersection, not cartesian x,y,z
    {
        List<LineSegment> segments = Lists.newArrayList();
//        for (int j = 0; j < coords.length; j++)
//            JTS.transform(coords[j], coords[j], transform);
        if (!closed)
        {
            for (int j = 0; j < coords.length - 1; j++)
            {
                //double[] start = new double[] { coords[j].x, coords[j].y, coords[j].z };
                //double[] end = new double[] { coords[j + 1].x, coords[j + 1].y, coords[j + 1].z };
                double[] start=new double[3];
                double[] end=new double[3];
                body.getPointAndCellIdFromLatLon(Math.toRadians(coords[j].y), Math.toRadians(coords[j].x), start);
                body.getPointAndCellIdFromLatLon(Math.toRadians(coords[j+1].y), Math.toRadians(coords[j+1].x), end);
                LineSegment seg = new LineSegment(start, end);
                segments.add(seg);
            }
        }
        else
        {
            for (int j = 0; j < coords.length; j++)
            {
                int jp = (j + 1) % coords.length;
                //double[] start = new double[] { coords[j].x, coords[j].y, coords[j].z };
                //double[] end = new double[] { coords[jp].x, coords[jp].y, coords[jp].z };
                double[] start=new double[3];
                double[] end=new double[3];
                body.getPointAndCellIdFromLatLon(Math.toRadians(coords[j].y), Math.toRadians(coords[j].x), start);
                body.getPointAndCellIdFromLatLon(Math.toRadians(coords[jp].y), Math.toRadians(coords[jp].x), end);
                LineSegment seg = new LineSegment(start, end);
                segments.add(seg);
            }
        }
        return segments;
    }

    public static PointStructure createPointStructureFrom(SimpleFeature feature, GenericPolyhedralModel body)
    {
        String label = defaultLabel;
        double size = defaultLineWidth;
        Color color = StructureUtil.defaultColor;
        //
        try
        {
            label = (String) feature.getAttribute("label");
        }
        catch (Exception e)
        {
        }
        try
        {
            color = StructureUtil.hexToColor((String) feature.getAttribute("color"));
        }
        catch (Exception e)
        {
        }
        try
        {
            size = (Double) feature.getAttribute("size");
        }
        catch (Exception e)
        {
        }
        //
        try
        {
            MathTransform transform = CRS.findMathTransform(feature.getType().getCoordinateReferenceSystem(), DefaultGeocentricCRS.CARTESIAN, true);
            Geometry geom = (Geometry) feature.getAttribute("the_geom");
            PointStyle style = new PointStyle(color, size);
            Coordinate coord = geom.getCoordinate();
    //        JTS.transform(coord, coord, transform);
/*            double[] location = new double[] { coord.x, coord.y, coord.z };
            location[0] /= 1000;
            location[1] /= 1000;
            location[2] /= 1000;*/
            double[] location=MathUtil.latrec(new LatLon(Math.toRadians(coord.y), Math.toRadians(coord.x)));
            return new PointStructure(location, style, label);
        }
        catch (FactoryException e)//| TransformException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        //
    }

    public static LineStructure createLineStructureFrom(SimpleFeature feature, GenericPolyhedralModel body)
    {
        String label = defaultLabel;
        double lineWidth = defaultLineWidth;
        Color lineColor = StructureUtil.defaultColor;
        //
        try
        {
            label = (String) feature.getAttribute("label");
        }
        catch (Exception e)
        {
        }
        try
        {
            lineColor = StructureUtil.hexToColor((String) feature.getAttribute("linecolor"));
        }
        catch (Exception e)
        {
        }
        try
        {
            lineWidth = (Double) feature.getAttribute("linewidth");
        }
        catch (Exception e)
        {
        }

        List<LineSegment> segments = Lists.newArrayList();

        MathTransform transform = null;
        try
        {
            transform = CRS.findMathTransform(feature.getType().getCoordinateReferenceSystem(), DefaultGeocentricCRS.CARTESIAN, true);

            Geometry geom = (Geometry) feature.getAttribute("the_geom");
            if (geom instanceof MultiLineString)
                segments = parse((MultiLineString) geom, transform, body);
            else if (geom instanceof MultiPolygon)
                segments = parse((MultiPolygon) geom, transform, body);
        }
        catch (FactoryException | TransformException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        LineStyle style = new LineStyle(lineColor, lineWidth);
        LineSegment[] segs = new LineSegment[segments.size()];
        for (int i = 0; i < segments.size(); i++)
            segs[i] = segments.get(i);
        return new LineStructure(segs, style, label);

    }

    /*	public static SimpleFeature createFeatureFrom(PatchStructure ps)
    	{
    		double[][] pts = ps.getPoints();
    		Coordinate[] coords = new Coordinate[pts.length];
    		for (int i = 0; i < pts.length; i++)
    		{
    			double[] pt = pts[i];
    			coords[i] = new Coordinate(pt[0], pt[1], pt[2]);
    		}
    		Geometry geom = JTSFactoryFinder.getGeometryFactory().createLineString(coords);
    		patchBuilder.add(geom);
    		patchBuilder.add(ps.getLabel());
    		patchBuilder.add(StructureUtil.colorToHex(ps.getStyle().getLineStyle().getLineColor()));
    		patchBuilder.add(ps.getStyle().getLineStyle().getLineWidth());
    		patchBuilder.add(ps.getStyle().getFillColor());
    		return patchBuilder.buildFeature(null);
    	}*/

    /*	public static SimpleFeature createFeatureFrom(PolyhedralStructure ps)
    	{
    		double[][] points=ps.getPoints();
    		int[][] faces=ps.getFaces();
    		Coordinate[] coords=new Coordinate[points.length];
    		for (int i=0; i<points.length; i++)
    		{
    			double[] pt=points[i];
    			coords[i]=new Coordinate(pt[0],pt[1],pt[2]);
    		}
    		Polygon[] polys=new Polygon[faces.length];
    		for (int j=0; j<faces.length; j++)
    		{
    			Coordinate[] faceCoords=new Coordinate[faces[j].length];
    			for (int k=0; k<faces[j].length; k++)
    				faceCoords[k]=coords[faces[j][k]];
    			polys[j]=JTSFactoryFinder.getGeometryFactory().createPolygon(faceCoords);
    		}
    		Geometry geom=JTSFactoryFinder.getGeometryFactory().createMultiPolygon(polys);
    		polyhedralBuilder.add(geom);
    		polyhedralBuilder.add(ps.getLabel());
    		polyhedralBuilder.add(StructureUtil.colorToHex(ps.getStyle().getLineStyle().getLineColor()));
    		polyhedralBuilder.add(ps.getStyle().getLineStyle().getLineWidth());
    		polyhedralBuilder.add(StructureUtil.colorToHex(ps.getStyle().getFillColor()));
    		return pointBuilder.buildFeature(null);
    	}*/

/*    public static PointStructure createPointStructureFrom(SimpleFeature feature)
    {
        String label = defaultLabel;
        double size = defaultLineWidth;
        Color color = StructureUtil.defaultColor;
        //
        try
        {
            label = (String) feature.getAttribute("label");
        }
        catch (Exception e)
        {
        }
        try
        {
            color = StructureUtil.hexToColor((String) feature.getAttribute("color"));
        }
        catch (Exception e)
        {
        }
        try
        {
            size = (Double) feature.getAttribute("size");
        }
        catch (Exception e)
        {
        }
        //
        try
        {
            MathTransform transform = CRS.findMathTransform(feature.getType().getCoordinateReferenceSystem(), DefaultGeocentricCRS.CARTESIAN, true);
            Geometry geom = (Geometry) feature.getAttribute("the_geom");
            PointStyle style = new PointStyle(color, size);
            Coordinate coord = geom.getCoordinate();
            JTS.transform(coord, coord, transform);
            double[] location = new double[] { coord.x, coord.y, coord.z };
            location[0] /= 1000;
            location[1] /= 1000;
            location[2] /= 1000;
            return new PointStructure(location, style, label);
        }
        catch (FactoryException | TransformException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        //
    }

    public static LineStructure createLineStructureFrom(SimpleFeature feature)
    {
        String label = defaultLabel;
        double lineWidth = defaultLineWidth;
        Color lineColor = StructureUtil.defaultColor;
        //
        try
        {
            label = (String) feature.getAttribute("label");
        }
        catch (Exception e)
        {
        }
        try
        {
            lineColor = StructureUtil.hexToColor((String) feature.getAttribute("linecolor"));
        }
        catch (Exception e)
        {
        }
        try
        {
            lineWidth = (Double) feature.getAttribute("linewidth");
        }
        catch (Exception e)
        {
        }

        List<LineSegment> segments = Lists.newArrayList();

        MathTransform transform = null;
        try
        {
            transform = CRS.findMathTransform(feature.getType().getCoordinateReferenceSystem(), DefaultGeocentricCRS.CARTESIAN, true);

            Geometry geom = (Geometry) feature.getAttribute("the_geom");
            if (geom instanceof MultiLineString)
                segments = parse((MultiLineString) geom, transform);
            else if (geom instanceof MultiPolygon)
                segments = parse((MultiPolygon) geom, transform);
        }
        catch (FactoryException | TransformException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        LineStyle style = new LineStyle(lineColor, lineWidth);
        LineSegment[] segs = new LineSegment[segments.size()];
        for (int i = 0; i < segments.size(); i++)
            segs[i] = segments.get(i);
        return new LineStructure(segs, style, label);

    }

    @Deprecated
    protected static List<LineSegment> parse(MultiPolygon mp, MathTransform transform) throws TransformException // assumes cartesian coords not lon lat
    {
        List<LineSegment> segments = Lists.newArrayList();

        for (int i = 0; i < mp.getNumGeometries(); i++)
        {
            Polygon p = (Polygon) mp.getGeometryN(i);
            Coordinate[] coords = p.getCoordinates();
            segments.addAll(convertCoordsToSegments(coords, transform, false));
        }
        return segments;
    }

    @Deprecated
    protected static List<LineSegment> parse(MultiLineString mls, MathTransform transform) throws TransformException    // assumes cartesian coords not lon lat
    {

        List<LineSegment> segments = Lists.newArrayList();

        for (int i = 0; i < mls.getNumGeometries(); i++)
        {
            LineString ls = (LineString) mls.getGeometryN(i);
            Coordinate[] coords = ls.getCoordinates();
            segments.addAll(convertCoordsToSegments(coords, transform, false));
        }

        return segments;
    }

    @Deprecated
    protected static List<LineSegment> convertCoordsToSegments(Coordinate[] coords, MathTransform transform, boolean closed) throws TransformException // TODO: this needs to interpret coords.x=lon and coords.y=lat and coords.z=derived from radial body-ray intersection, not cartesian x,y,z
    {
        List<LineSegment> segments = Lists.newArrayList();
        for (int j = 0; j < coords.length; j++)
        {
            JTS.transform(coords[j], coords[j], transform);
            coords[j].x /= 1000;
            coords[j].y /= 1000;
            coords[j].z /= 1000;
        }
        if (!closed)
        {
            for (int j = 0; j < coords.length - 1; j++)
            {
                double[] start = new double[] { coords[j].x, coords[j].y, coords[j].z };
                double[] end = new double[] { coords[j + 1].x, coords[j + 1].y, coords[j + 1].z };
                LineSegment seg = new LineSegment(start, end);
                segments.add(seg);
            }
        }
        else
        {
            for (int j = 0; j < coords.length; j++)
            {
                int jp = (j + 1) % coords.length;
                double[] start = new double[] { coords[j].x, coords[j].y, coords[j].z };
                double[] end = new double[] { coords[jp].x, coords[jp].y, coords[jp].z };
                LineSegment seg = new LineSegment(start, end);
                segments.add(seg);
            }
        }
        return segments;
    }

    static final String defaultLabel = "";
    static final Color defaultColor = Color.CYAN;
    static final double defaultLineWidth = 1;

    public static EllipseStructure createEllipseStructureFrom(SimpleFeature feature)
    {
        String label = defaultLabel;
        Color lineColor = defaultColor;
        double lineWidth = defaultLineWidth;
        try
        {
            label = (String) feature.getAttribute("label");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            lineColor = StructureUtil.hexToColor((String) feature.getAttribute("linecolor"));
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }

        try
        {
            lineWidth = (Double) feature.getAttribute("linewidth");
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }
        LineStyle style = new LineStyle(lineColor, lineWidth);
        //

        List<LineSegment> segments = Lists.newArrayList();

        try
        {
            MathTransform transform = CRS.findMathTransform(feature.getType().getCoordinateReferenceSystem(), DefaultGeocentricCRS.CARTESIAN, true);

            Geometry geom = (Geometry) feature.getAttribute("the_geom");
            if (geom instanceof MultiLineString)
                segments = parse((MultiLineString) geom, transform);
            else if (geom instanceof MultiPolygon)
                segments = parse((MultiPolygon) geom, transform);
            //

        }
        catch (FactoryException | TransformException e)
        {
            e.printStackTrace();
            return null;
        }

        List<Vector3D> pts = Lists.newArrayList();
        for (int i = 0; i < segments.size(); i++)
            pts.add(new Vector3D(segments.get(i).start));

        Vector3D center = Vector3D.ZERO;
        for (int i = 0; i < pts.size(); i++)
            center = center.add(pts.get(i));
        center = center.scalarMultiply(1. / (double) pts.size());
        //
        try
        {
            double x = (Double) feature.getAttribute("centerx");
            double y = (Double) feature.getAttribute("centery");
            double z = (Double) feature.getAttribute("centerz");
            center = new Vector3D(x, y, z);
        }
        catch (Exception e)
        {
        }

        Vector3D normal = Vector3D.ZERO;
        for (int i = 0; i < pts.size(); i++)
        {
            int ip = (i + 1) % pts.size();
            Vector3D radial = pts.get(i).subtract(center).normalize();
            Vector3D tangent = pts.get(ip).subtract(pts.get(i));
            normal = normal.add(radial.crossProduct(tangent));
        }
        normal = normal.normalize();

        double majorRadius = Double.NEGATIVE_INFINITY;
        double minorRadius = Double.POSITIVE_INFINITY;
        for (int i = 0; i < pts.size(); i++)
        {
            double r = pts.get(i).subtract(center).crossProduct(normal).getNorm();
            if (r > majorRadius)
                majorRadius = r;
            if (r < minorRadius)
                minorRadius = r;
        }

        try
        {
            majorRadius = (Double) feature.getAttribute("majorRad");
        }
        catch (Exception e)
        {

        }

        double flattening = minorRadius / majorRadius;//(majorRadius - minorRadius) / majorRadius;
        try
        {
            flattening = (Double) feature.getAttribute("flattening");
        }
        catch (Exception e)
        {

        }

        double angle = Math.acos(minorRadius / majorRadius);
        try
        {
            angle = (Double) feature.getAttribute("angle");
        }
        catch (Exception e)
        {

        }

        Parameters params = new Parameters(center.toArray(), majorRadius, flattening, angle);
        //
        LineSegment[] segs = new LineSegment[segments.size()];
        for (int i = 0; i < segments.size(); i++)
            segs[i] = segments.get(i);
        return new EllipseStructure(segs, style, label, params);

    }

//    	public static PatchStructure createPatchStructureFrom(SimpleFeature feature)
//    	{
//    		String label = (String) feature.getAttribute("label");
//    		Color lineColor = StructureUtil.hexToColor((String) feature.getAttribute("linecolor"));
//    		double lineWidth = (Double) feature.getAttribute("linewidth");
//    		Color fillColor = StructureUtil.hexToColor((String) feature.getAttribute("fillcolor"));
//    		Geometry geom = (Geometry) feature.getAttribute("the_geom");
//    		LineStyle lineStyle = new LineStyle(lineColor, lineWidth);
//    		PatchStyle patchStyle = new PatchStyle(fillColor, lineStyle);
//    		double[][] points = pointsFromCoordinates(geom.getCoordinates());
//    		return new PatchStructure(points, patchStyle, label);
//    
//    	}

    @Deprecated
    public static double[][] pointsFromCoordinates(Coordinate[] coords) // TODO: this needs to interpret coords.x=lon and coords.y=lat and coords.z=derived from radial body-ray intersection, not cartesian x,y,z
    {
        double[][] pts = new double[coords.length][3];
        for (int i = 0; i < coords.length; i++)
        {
            pts[i][0] = coords[i].x;
            pts[i][1] = coords[i].y;
            pts[i][2] = coords[i].z;
        }
        return pts;
    }*/
}

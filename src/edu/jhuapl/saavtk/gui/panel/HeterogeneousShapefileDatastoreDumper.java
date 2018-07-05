package edu.jhuapl.saavtk.gui.panel;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDumper;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.GridReaderLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.DefaultCylindricalProjection;
import org.geotools.referencing.operation.builder.MathTransformBuilder;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.resources.coverage.FeatureUtilities;
import org.geotools.styling.ChannelSelection;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.SelectedChannelType;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.swing.DefaultRenderingExecutor;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.JMapPane;
import org.geotools.swing.RenderingExecutor;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.tool.FeatureLayerHelper;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.cs.CylindricalCS;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PrimeMeridian;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.style.ContrastMethod;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.gui.render.axes.Axes;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.model.structure.geotools.FeatureUtil;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.LinearSpace;
import edu.jhuapl.saavtk.util.MathUtil;
import vtk.vtkCell;
import vtk.vtkCutter;
import vtk.vtkLine;
import vtk.vtkPolyData;
import vtk.vtkSphere;

public class HeterogeneousShapefileDatastoreDumper
{
//    MapContent map = new MapContent();
//    GTRenderer renderer = new StreamingRenderer();
//    RenderingExecutor executor = new DefaultRenderingExecutor();
//    JMapPane mapPane = new JMapPane(map, executor, renderer);

    //List<SimpleFeature> lineFeatures;
    // JSplitPane splitPane=new JSplitPane();
    // JScrollPane scrollPane = new JScrollPane();

    double primeMeridianDeg = 0;
//    int numberOfMapContours=10;

    Collection<SimpleFeature> features;
    //GenericPolyhedralModel shapeModel;

/*    class ExportShapefileAction extends SafeAction
    {
        public ExportShapefileAction()
        {
            super("Export...");
            putValue(Action.SHORT_DESCRIPTION, "Export using current crs");
        }

        @Override
        public void action(ActionEvent arg0) throws Throwable
        {
            System.out.println("Export method called...");

        }
    }*/

    public HeterogeneousShapefileDatastoreDumper(Collection<SimpleFeature> features)//, GenericPolyhedralModel shapeModel)
    {
        this.features = features;
     //  this.shapeModel = shapeModel;

/*        enableToolBar(true);
        enableStatusBar(true);
        enableLayerTable(true);
        getToolBar().addSeparator();
        //        getToolBar().add(new JButton(new ExportShapefileAction()));

        setSize(800, 600);
        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);*/

/*        vtkPolyData polyData=shapeModel.getSmallBodyPolyData();
        double rmin=Double.POSITIVE_INFINITY;
        double rmax=Double.NEGATIVE_INFINITY;
        for (int i=0; i<polyData.GetNumberOfPoints(); i++)
        {
            double r=new Vector3D(polyData.GetPoint(i)).getNorm();
            if (r<rmin)
                rmin=r;
            if (r>rmax)
                rmax=r;
        }
        
        lineFeatures=Lists.newArrayList();
        double[] contourRadii=LinearSpace.create(rmin, rmax, numberOfMapContours);
        for (int i=0; i<contourRadii.length; i++)
        {
            vtkSphere sphere=new vtkSphere();
            sphere.SetRadius(contourRadii[i]);
            vtkCutter cutter=new vtkCutter();
            cutter.SetInputData(polyData);
            cutter.SetCutFunction(sphere);
            cutter.Update();
            //
            vtkPolyData contour=cutter.GetOutput();
            
            List<List<Vector3D>> lines=Lists.newArrayList();
            for (int m=0; m<contour.GetNumberOfCells(); m++)
            {
                vtkCell cell=contour.GetCell(m);
                if (cell instanceof vtkLine)
                {
                    List<Vector3D> linePoints=Lists.newArrayList();
                    for (int n=0; n<cell.GetNumberOfPoints(); n++)
                    {
                        linePoints.add(new Vector3D(cell.GetPoints().GetPoint(n)));
                    }
                    lines.add(linePoints);
                }
            }
            lineFeatures.add(FeatureUtil.createMultiLineString(lines));
        }*/
    }

    public void write(Path shapeFileDirectory)
    {
        if (features.size() == 0)
            return;

        DefaultFeatureCollection collection = new DefaultFeatureCollection();
        collection.addAll(features);

//        map = new MapContent();

        CoordinateReferenceSystem worldCRS = null;

        CRSFactory crsFactory = ReferencingFactoryFinder.getCRSFactory(null);
        DatumFactory datumFactory = ReferencingFactoryFinder.getDatumFactory(null);
        CSFactory csFactory = ReferencingFactoryFinder.getCSFactory(null);
        Map<String, Object> hm = Maps.newConcurrentMap();
        try
        {
            hm.put("name", "N");
            CoordinateSystemAxis northAxis = csFactory.createCoordinateSystemAxis(hm, "N", AxisDirection.NORTH, NonSI.DEGREE_ANGLE);
            hm.clear();
            hm.put("name", "E");
            CoordinateSystemAxis eastAxis = csFactory.createCoordinateSystemAxis(hm, "E", AxisDirection.EAST, NonSI.DEGREE_ANGLE);
            hm.clear();
            //            hm.put("name", "U");
            //            CoordinateSystemAxis upAxis=csFactory.createCoordinateSystemAxis(hm, "U", AxisDirection.UP, SI.METER);
            //            hm.clear();
            hm.put("name", "bennu prime meridian");
            PrimeMeridian greenwichMeridian = datumFactory.createPrimeMeridian(hm, primeMeridianDeg, NonSI.DEGREE_ANGLE);

            hm.clear();
            hm.put("name", "bennu 256m sphere");
            Ellipsoid bennuEllipsoid = datumFactory.createEllipsoid(hm, 0.256, 0.256, SI.KILOMETER);

            hm.clear();
            hm.put("name", "bennu body-fixed height datum");

            GeodeticDatum wgs84Datum = datumFactory.createGeodeticDatum(hm, bennuEllipsoid, greenwichMeridian);
            hm.clear();
            hm.put("name", "bennu body-fixed coord sys");
            EllipsoidalCS cs = csFactory.createEllipsoidalCS(hm, eastAxis, northAxis);

            worldCRS = crsFactory.createGeographicCRS(hm, wgs84Datum, cs);
        }
        catch (FactoryException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

/*        try
        {
            ShapefileDataStoreFactory dsfac=new ShapefileDataStoreFactory();

            Map<String, Serializable> params = new HashMap<String, Serializable>();
            params.put("create spatial index", Boolean.TRUE);
                params.put("url", shapeFile.toFile().toURI().toURL());
                ShapefileDataStore store=(ShapefileDataStore)dsfac.createNewDataStore(params);

                store.create
                
                Transaction transaction = new DefaultTransaction("create");
                for (SimpleFeature f : collection)
                {
                    SimpleFeatureBuilder.retype(f, SimpleFeatureTypeBuilder.retype(f.getType(), worldCRS));
                    
                }

        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
            
            try
            {
                new ShapefileDumper(shapeFileDirectory.toFile()).dump(collection);
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
                    
        
/*        SimpleFeatureType type = SimpleFeatureTypeBuilder.retype(featureType, worldCRS);
        DefaultFeatureCollection coll2 = new DefaultFeatureCollection();
        for (SimpleFeature f : collection)
        {
            coll2.add(SimpleFeatureBuilder.retype(f, type));
        }
        sha


/*        SimpleFeatureType contourType = SimpleFeatureTypeBuilder.retype(FeatureUtil.lineType, worldCRS);
        DefaultFeatureCollection contours=new DefaultFeatureCollection();
        for (SimpleFeature f : lineFeatures)
        {
            contours.add(SimpleFeatureBuilder.retype(f, contourType));
            System.out.println("contour: "+f);
        }*/

//        Style style = SLD.createSimpleStyle(coll2.getSchema());
//        Layer layer = new FeatureLayer(DataUtilities.source(coll2), style);
//        map.layers().add(layer);
       
   /*     File file=FileCache.getFileFromServer(shapeModel.getImageMapNames()[0]);
        AbstractGridFormat format=GridFormatFinder.findFormat(file);
        GridCoverage2DReader reader=format.getReader(file);
        Style rasterStyle = createGreyscaleStyle(1);
        Layer rasterLayer=new GridReaderLayer(reader, rasterStyle);
        map.addLayer(rasterLayer);*/
        
        //Style style2= SLD.createSimpleStyle(contours.getSchema());
        //Layer contourLayer=new FeatureLayer(contours, style2);
        //map.layers().add(contourLayer);

 //       this.setMapContent(map);
    }
    
   /* private Style createGreyscaleStyle(int band) {  // copied from http://docs.geotools.org/latest/userguide/tutorial/raster/image.html
        StyleFactory sf=CommonFactoryFinder.getStyleFactory();
        FilterFactory2 ff=CommonFactoryFinder.getFilterFactory2();
        ContrastEnhancement ce = sf.contrastEnhancement(ff.literal(1.0), ContrastMethod.NORMALIZE);
        SelectedChannelType sct = sf.createSelectedChannelType(String.valueOf(band), ce);

        RasterSymbolizer sym = sf.getDefaultRasterSymbolizer();
        ChannelSelection sel = sf.channelSelection(sct);
        sym.setChannelSelection(sel);

        return SLD.wrapSymbolizers(sym);
    }*/


}

package edu.jhuapl.saavtk.structure.io;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.FeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CRSFactory;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.cs.AxisDirection;
import org.geotools.api.referencing.cs.CSFactory;
import org.geotools.api.referencing.cs.CoordinateSystemAxis;
import org.geotools.api.referencing.cs.EllipsoidalCS;
import org.geotools.api.referencing.datum.DatumFactory;
import org.geotools.api.referencing.datum.Ellipsoid;
import org.geotools.api.referencing.datum.GeodeticDatum;
import org.geotools.api.referencing.datum.PrimeMeridian;
import org.geotools.data.shapefile.ShapefileDumper;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.referencing.ReferencingFactoryFinder;

import com.google.common.collect.Maps;

import si.uom.NonSI;
import si.uom.SI;

public class BennuStructuresEsriIO
{

    public static final double primeMeridianDeg = 0;

    public static void write(Path shapeFile, DefaultFeatureCollection collection, SimpleFeatureType featureType)
    {
        if (collection.isEmpty())
            return;

  //      System.out.println(collection.getSchema().getCoordinateReferenceSystem().toString());
        
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
            PrimeMeridian bennuPrimeMeridian = datumFactory.createPrimeMeridian(hm, primeMeridianDeg, NonSI.DEGREE_ANGLE);

            hm.clear();
            hm.put("name", "bennu 256m sphere");
            Ellipsoid bennuEllipsoid = datumFactory.createEllipsoid(hm, 0.256, 0.256, SI.METRE.multiply(1000)); // TODO: replace this with data from shape model input file

            hm.clear();
            hm.put("name", "bennu body-fixed height datum");
            GeodeticDatum bennuDatum = datumFactory.createGeodeticDatum(hm, bennuEllipsoid, bennuPrimeMeridian);
            hm.clear();
            hm.put("name", "bennu body-fixed coord sys");
            EllipsoidalCS cs = csFactory.createEllipsoidalCS(hm, eastAxis, northAxis);

            // lon-lats (-180-180 lon)
            // TODO: draw structures on earth model and output with default crs -- send to erika
            // TODO: try to brute force output lat lons 
            // TODO: look at off-limb rendering again
            
            
/*            worldCRS = crsFactory.createGeographicCRS(hm, bennuDatum, cs);

            SimpleFeatureType type = SimpleFeatureTypeBuilder.retype(featureType, worldCRS);
            DefaultFeatureCollection coll2 = new DefaultFeatureCollection();
            for (SimpleFeature f : collection)
            {
                coll2.add(SimpleFeatureBuilder.retype(f, type));
            }

            new ShapefileDumper(shapeFile.getParent().toFile())
            {
                protected String getShapeName(SimpleFeatureType schema, String geometryType)
                {
                    //System.out.println(FilenameUtils.getBaseName(shapeFile.getFileName().toString()));
                    return FilenameUtils.getBaseName(shapeFile.getFileName().toString());
                };
            }.dump(coll2);*/

            new ShapefileDumper(shapeFile.getParent().toFile())
            {
                protected String getShapeName(SimpleFeatureType schema, String geometryType)
                {
     //               System.out.println(shapeFile.toAbsolutePath().toString());
                    return FilenameUtils.getBaseName(shapeFile.getFileName().toString());
                };
            }.dump(collection);
        }
        catch (FactoryException | IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

	public static FeatureCollection read(Path shapeFile, SimpleFeatureType type) {
		DataStore dataStore = null;
		try {

			File file = shapeFile.toFile();
			Map<String, Object> map = Maps.newHashMap();
			try {
				map.put("url", file.toURI().toURL());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
				dataStore = DataStoreFinder.getDataStore(map);

				String typeName = dataStore.getTypeNames()[0];

				FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);
				FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures();

				return collection;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} finally {
			if (dataStore != null)
				dataStore.dispose();
		}
		return null;
	}
}

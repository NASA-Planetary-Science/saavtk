package edu.jhuapl.saavtk.model.structure.geotools;

import java.awt.Color;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureReader;
import org.geotools.data.FileDataStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDumper;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureTypes;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.geotools.referencing.operation.builder.ProjectiveTransformBuilder;
import org.geotools.referencing.operation.projection.EquidistantCylindrical;
import org.geotools.renderer.Renderer;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.operation.MathTransform;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFactory;
import com.vividsolutions.jts.geom.CoordinateSequences;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequenceFactory;
import com.vividsolutions.jts.math.Vector3D;

import vtk.vtkCellArray;
import vtk.vtkExtractEdges;
import vtk.vtkFeatureEdges;
import vtk.vtkIdList;
import vtk.vtkNativeLibrary;
import vtk.vtkOBJReader;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;
import vtk.vtkTriangleFilter;

public class ShapefileUtil
{
	public static void write(Collection<SimpleFeature> features, Path shapeFile) throws IOException
	{
	
		DefaultFeatureCollection collection = new DefaultFeatureCollection();
		collection.addAll(features);

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", shapeFile.toUri().toURL());
		params.put("create spatial index", Boolean.TRUE);

		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
		if (!features.iterator().hasNext())
			throw new IOException("No features to write");
		SimpleFeatureType type = features.iterator().next().getFeatureType();
		newDataStore.createSchema(type);

		Transaction transaction = new DefaultTransaction("create");
		String typeName = newDataStore.getTypeNames()[0];
		SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

		if (featureSource instanceof SimpleFeatureStore)
		{
			SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
			
			featureStore.setTransaction(transaction);
			try
			{
				featureStore.addFeatures(collection);
				transaction.commit();

			} catch (Exception problem)
			{
				problem.printStackTrace();
				transaction.rollback();

			} finally
			{
				transaction.close();
			}
		} else
		{
			transaction.close();
			throw new IOException(type.getTypeName() + " does not support read/write access");
		}
	}

	private static Collection<SimpleFeature> read(Path shapeFileName) throws IOException
	{
		ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
		FileDataStore dataStore = factory.createDataStore(shapeFileName.toUri().toURL());
		SimpleFeatureCollection fc = dataStore.getFeatureSource().getFeatures();
		List<SimpleFeature> fl = Lists.newArrayList();
		SimpleFeatureIterator fit = fc.features();
		while (fit.hasNext())
			fl.add(fit.next());
		fit.close();
		return fl;
	}

	public static Collection<PointStructure> readPointStructures(Path shapeFile) throws IOException
	{
		Collection<SimpleFeature> sfc = read(shapeFile);
		Set<PointStructure> ps = Sets.newHashSet();
		for (SimpleFeature sf : sfc)
			ps.add(FeatureUtil.createPointStructureFrom(sf));
		return ps;
	}

	public static Collection<LineStructure> readLineStructures(Path shapeFile) throws IOException
	{
		Collection<SimpleFeature> sfc = read(shapeFile);
		Set<LineStructure> ls = Sets.newHashSet();
		for (SimpleFeature sf : sfc)
		{
			LineStructure s=FeatureUtil.createLineStructureFrom(sf);
			ls.add(s);
		}
		return ls;
	}

	public static Collection<EllipseStructure> readEllipseStructures(Path shapeFile) throws IOException
	{
		Collection<SimpleFeature> sfc = read(shapeFile);
		Set<EllipseStructure> ls = Sets.newHashSet();
		for (SimpleFeature sf : sfc)
			ls.add(FeatureUtil.createEllipseStructureFrom(sf));
		return ls;
	}

/*	public static Collection<PatchStructure> readPatchStructures(Path shapeFile) throws IOException
	{
		Collection<SimpleFeature> sfc = read(shapeFile);
		Set<PatchStructure> ps = Sets.newHashSet();
		for (SimpleFeature sf : sfc)
			ps.add(FeatureUtil.createPatchStructureFrom(sf));
		return ps;
	}*/
	
	public static void writePointStructures(Collection<PointStructure> psc, Path shapeFile) throws IOException
	{
		List<SimpleFeature> fl = Lists.newArrayList();
		for (PointStructure ps : psc)
		{
			SimpleFeature f = FeatureUtil.createFeatureFrom(ps);
			fl.add(f);
		}
		ShapefileUtil.write(fl, shapeFile);
	}

	public static void writeLineStructures(Collection<LineStructure> lsc, Path shapeFile) throws IOException
	{
		List<SimpleFeature> fl = Lists.newArrayList();
		for (LineStructure ls : lsc)
		{
			SimpleFeature f = FeatureUtil.createFeatureFrom(ls);
			fl.add(f);
		}
		ShapefileUtil.write(fl, shapeFile);
	}

	public static void writeEllipseStructures(Collection<EllipseStructure> lsc, Path shapeFile) throws IOException
	{
		List<SimpleFeature> fl = Lists.newArrayList();
		for (EllipseStructure ls : lsc)
		{
			SimpleFeature f = FeatureUtil.createFeatureFrom(ls);
			fl.add(f);
		}
		ShapefileUtil.write(fl, shapeFile);
	}

/*	public static void writePatchStructures(Collection<PatchStructure> psc, Path shapeFile) throws IOException
	{
		List<SimpleFeature> fl = Lists.newArrayList();
		for (PatchStructure ps : psc)
		{
			SimpleFeature f = FeatureUtil.createFeatureFrom(ps);
			fl.add(f);
		}
		ShapefileUtil.write(fl, shapeFile);
	}*/

	public static void test1()
	{
		List<PointStructure> points = Lists.newArrayList();
		int nPoints = 10;
		for (int i = 0; i < nPoints; i++)
			points.add(new PointStructure(StructureUtil.random()));

		List<LineStructure> lines = Lists.newArrayList();
		int nLines = 10;
		int nPointsPerLine = 5;
		for (int i = 0; i < nLines; i++)
		{
			double[] start=StructureUtil.random();
			double[] end=StructureUtil.random();
			LineSegment[] segments=new LineSegment[]{new LineSegment(start,end)};
			lines.add(new LineStructure(segments));
		}

		List<PatchStructure> patches = Lists.newArrayList();
		int nPolygons = 10;
		int nPointsPerPolygon = 5;
		for (int i = 0; i < nPolygons; i++)
			patches.add(new PatchStructure(StructureUtil.random(nPointsPerPolygon)));

		try
		{
			Path pointsShapeFile = Paths.get("/Users/zimmemi1/Desktop/shape/myPoints.shp");
			ShapefileUtil.writePointStructures(points, pointsShapeFile);
			Collection<PointStructure> pointsRead = ShapefileUtil.readPointStructures(pointsShapeFile);
//			for (PointStructure ps : pointsRead)
//				System.out.println(ps);

			Path linesShapeFile = Paths.get("/Users/zimmemi1/Desktop/shape/myLines.shp");
			ShapefileUtil.writeLineStructures(lines, linesShapeFile);
			Collection<LineStructure> linesRead = ShapefileUtil.readLineStructures(linesShapeFile);
//			for (LineStructure ls : linesRead)
//				System.out.println(ls);

/*			Path patchesShapeFile = Paths.get("/Users/zimmemi1/Desktop/shape/myPatches.shp");
			ShapefileUtil.writePatchStructures(patches, patchesShapeFile);
			Collection<PatchStructure> patchesRead = ShapefileUtil.readPatchStructures(patchesShapeFile);
			for (PatchStructure ps : patchesRead)
				System.out.println(ps);*/

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void test2()
	{
		vtkNativeLibrary.LoadAllNativeLibraries();
		
		vtkOBJReader reader=new vtkOBJReader();
		reader.SetFileName("/Users/zimmemi1/monkey.obj");
		reader.Update();
		
		vtkTriangleFilter triangleFilter=new vtkTriangleFilter();
		triangleFilter.SetInputData(reader.GetOutput());
		triangleFilter.Update();
		vtkPolyData polyData=triangleFilter.GetOutput();
		
		List<PointStructure> points=Lists.newArrayList();
		for (int i=0; i<polyData.GetNumberOfPoints(); i++)
			points.add(new PointStructure(polyData.GetPoint(i),"p"+i));
		
		vtkExtractEdges edgeFilter=new vtkExtractEdges();
		edgeFilter.SetInputData(polyData);
		edgeFilter.Update();
		vtkPolyData edgePolyData=edgeFilter.GetOutput();
		
		vtkPolyDataWriter writer=new vtkPolyDataWriter();
		writer.SetFileName("/Users/zimmemi1/Desktop/test.vtk");
		writer.SetFileTypeToBinary();
		writer.SetInputData(edgeFilter.GetOutput());
		writer.Write();
		
		List<LineStructure> lines=Lists.newArrayList();
		for (int i=0; i<edgePolyData.GetNumberOfCells(); i++)
		{
			vtkPoints edgePoints=edgePolyData.GetCell(i).GetPoints();
			if (edgePoints.GetNumberOfPoints()<2)
				continue;
			LineSegment[] segments=new LineSegment[edgePoints.GetNumberOfPoints()-1];
			for (int j=0; j<edgePoints.GetNumberOfPoints()-1; j++)
				segments[j]=new LineSegment(edgePoints.GetPoint(j), edgePoints.GetPoint(j+1));
			lines.add(new LineStructure(segments,"l"+i));
		}
		
		List<PatchStructure> patches=Lists.newArrayList();
		for (int i=0; i<polyData.GetNumberOfCells(); i++)
		{
			vtkPoints triPoints=polyData.GetCell(i).GetPoints();
			double[][] pts=new double[triPoints.GetNumberOfPoints()][];
			for (int j=0; j<triPoints.GetNumberOfPoints(); j++)
				pts[j]=triPoints.GetPoint(j);
			patches.add(new PatchStructure(pts,"P"+i));
		}

	
		try
		{
			Path pointsShapeFile = Paths.get("/Users/zimmemi1/Desktop/shape/myPoints.shp");
			ShapefileUtil.writePointStructures(points, pointsShapeFile);
			Collection<PointStructure> pointsRead = ShapefileUtil.readPointStructures(pointsShapeFile);
//			for (PointStructure ps : pointsRead)
//				System.out.println(ps);

			Path linesShapeFile = Paths.get("/Users/zimmemi1/Desktop/shape/myLines.shp");
			ShapefileUtil.writeLineStructures(lines, linesShapeFile);
			Collection<LineStructure> linesRead = ShapefileUtil.readLineStructures(linesShapeFile);
//			for (LineStructure ls : linesRead)
//				System.out.println(ls);

/*			Path patchesShapeFile = Paths.get("/Users/zimmemi1/Desktop/shape/myPatches.shp");
			ShapefileUtil.writePatchStructures(patches, patchesShapeFile);
			Collection<PatchStructure> patchesRead = ShapefileUtil.readPatchStructures(patchesShapeFile);
			for (PatchStructure ps : patchesRead)
				System.out.println(ps);*/

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		//test1();
		test2();
	}
}

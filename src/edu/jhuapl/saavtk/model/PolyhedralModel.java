package edu.jhuapl.saavtk.model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.color.table.ColorMapAttr;
import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.LatLon;
import vtk.vtkDataArray;
import vtk.vtkPointLocator;
import vtk.vtkPolyData;
import vtk.vtksbCellLocator;

public abstract class PolyhedralModel extends AbstractModel implements PolyModel
{
	public static final String LIST_SEPARATOR = ",";
	public static final String CELL_DATA_PATHS = "CellDataPaths"; // for backwards compatibility
	public static final String CELL_DATA_FILENAMES = "CellDataFilenames";
	public static final String CELL_DATA_NAMES = "CellDataNames";
	public static final String CELL_DATA_UNITS = "CellDataUnits";
	public static final String CELL_DATA_HAS_NULLS = "CellDataHasNulls";
	public static final String CELL_DATA_RESOLUTION_LEVEL = "CellDataResolutionLevel";

	public static final String LIDAR_DATASOURCE_PATHS = "LidarDatasourcePaths";
	public static final String LIDAR_DATASOURCE_NAMES = "LidarDatasourceNames";

	public static final int FITS_SCALAR_COLUMN_INDEX = 4;

	public enum ColoringValueType
	{
		POINT_DATA,
		CELLDATA
	}

	public enum ShadingType
	{
		FLAT,
		SMOOTH,
	}

	public enum Format
	{
		TXT,
		FIT,
		UNKNOWN
	}

	static public final String SlopeStr = "Slope";
	static public final String ElevStr = "Elevation";
	static public final String GravAccStr = "Gravitational Acceleration";
	static public final String GravPotStr = "Gravitational Potential";
	static public final String SlopeUnitsStr = "deg";
	static public final String ElevUnitsStr = "m";
	static public final String GravAccUnitsStr = "m/s^2";
	static public final String GravPotUnitsStr = "J/kg";

	static public final String FlatShadingStr = "Flat";
	static public final String SmoothShadingStr = "Smooth";

	private final ViewConfig config;

	protected PolyhedralModel(ViewConfig config)
	{
		this.config = config;
	}

	public abstract vtksbCellLocator getCellLocator();

	public abstract vtkPointLocator getPointLocator();

	public abstract BoundingBox getBoundingBox();

	public abstract vtkPolyData getSmallBodyPolyData();

	public abstract boolean isEllipsoid();

	public abstract void showScalarsAsContours(boolean flag);

	public abstract void setContourLineWidth(double width);

	public abstract String getCustomDataFolder();

	public abstract String getConfigFilename();

	public abstract String getPlateConfigFilename();

	public abstract vtkDataArray getGravityVectorData();

	public abstract List<LidarDataSource> getLidarDataSourceList();

	public abstract int getModelResolution();

	public abstract TreeSet<Integer> getIntersectingCubes(vtkPolyData polydata);

	public abstract TreeSet<Integer> getIntersectingCubes(BoundingBox bb);

	public abstract void addCustomLidarDataSource(LidarDataSource info) throws IOException;

	public abstract void setCustomLidarDataSource(int index, LidarDataSource info) throws IOException;

	public abstract void loadCustomLidarDataSource();

	public abstract void removeCustomLidarDataSource(int index) throws IOException;

	public abstract CustomizableColoringDataManager getColoringDataManager();

	public abstract int getNumberResolutionLevels();

	public abstract boolean isResolutionLevelAvailable(int resolutionLevel);

	/**
	 * Gets the {@link ColorMapAttr} that is used to colorize this model's data.
	 */
	public abstract ColorMapAttr getColorMapAttr();

	/**
	 * Sets in the {@link ColorMapAttr} that is used to colorize this model's data.
	 */
	public abstract void setColorMapAttr(ColorMapAttr aCMA);

	// public abstract Config getSmallBodyConfig();

	public abstract void drawRegularPolygonLowRes(double[] center, double radius, int numberOfSides,
			vtkPolyData outputInterior, vtkPolyData outputBoundary);

	public abstract String getCustomDataRootFolder();

	public abstract String getDEMConfigFilename();

	public abstract void setShowSmallBody(boolean show);

	public abstract void setModelResolution(int level) throws IOException;

	public abstract void setFalseColoring(int redChannel, int greenChannel, int blueChannel) throws IOException;

	public abstract boolean isFalseColoringEnabled();

	public abstract int[] getFalseColoring();

	public abstract double getSurfaceArea();

	public abstract double getVolume();

	public abstract double getMeanCellArea();

	public abstract double getMinCellArea();

	public abstract double getMaxCellArea();

	public abstract void savePlateData(File file) throws IOException;

	public abstract void reloadShapeModel() throws IOException;

	public ViewConfig getConfig()
	{
		return config;
	}

	@Override
	public void setOpacity(@SuppressWarnings("unused") double opacity)
	{
		// Do nothing. Subclasses should redefine this if they support opacity.
	}

	@Override
	public double getOpacity()
	{
		// Subclasses should redefine this if they support opacity.
		return 1.0;
	}

	@Override
	public double getDefaultOffset()
	{
		// Subclasses should redefine this if they support offset.
		return 0.0;
	}

	public List<vtkPolyData> getSmallBodyPolyDatas()
	{
		return null;
	}

	public abstract void setPointSize(double value);

	public abstract void setLineWidth(double value);

	public abstract void setSpecularCoefficient(double value);

	public abstract void setSpecularPower(double value);

	public abstract void setRepresentationToSurface();

	public abstract void setRepresentationToWireframe();

	public abstract void setRepresentationToPoints();

	public abstract void setRepresentationToSurfaceWithEdges();

	public abstract void setCullFrontface(boolean enable);

	public abstract void setShadingToFlat();

	public abstract void setShadingToSmooth();

	public abstract int getColoringIndex();

	public abstract void setColoringIndex(int index) throws IOException;

	public abstract double[] getCurrentColoringRange(int coloringIndex);

	public abstract void setCurrentColoringRange(int coloringIndex, double[] range) throws IOException;

	public abstract double[] getDefaultColoringRange(int coloringIndex);

	public abstract String getColoringName(int i);

	@Override
	public abstract double getBoundingBoxDiagonalLength();

	public abstract void shiftPolyLineInNormalDirection(vtkPolyData polyLine, double shiftAmount);

	public abstract int getPointAndCellIdFromLatLon(double lat, double lon, double[] intersectPoint);

	public abstract double[] getNormalAtPoint(double[] point);

	public abstract boolean isColoringDataAvailable();

	public abstract List<ColoringData> getAllColoringData();

	public abstract double[] getAllColoringValues(double[] pt) throws IOException;

	public abstract double[] getGravityVector(double[] pt);

	public abstract double getMinShiftAmount();

	public abstract void savePlateDataInsidePolydata(vtkPolyData polydata, File file) throws IOException;

	public abstract FacetColoringData[] getPlateDataInsidePolydata(vtkPolyData polydata);

	public abstract String getModelName();

	public abstract List<String> getModelFileNames();

	public abstract double[] findClosestPoint(double[] pt);

	public abstract double getColoringValue(int index, double[] pt);

	public abstract int getNumberOfColors();

	public abstract String getColoringUnits(int i);

	public abstract double getDensity();

	public abstract double getRotationRate();

	public abstract double getReferencePotential();

	public abstract double[] getClosestNormal(double[] point);

	public abstract void drawPolygon(List<LatLon> controlPoints, vtkPolyData outputInterior, vtkPolyData outputBoundary);

	@Override
	public Vector3D getAverageSurfaceNormal()
	{
		return Vector3D.ZERO;
	}

	@Override
	public Vector3D getGeometricCenterPoint()
	{
		return Vector3D.ZERO;
	}

	@Override
	public boolean isPolyhedron()
	{
		return true;
	}

}

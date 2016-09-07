package edu.jhuapl.saavtk.model;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.List;
import java.util.TreeSet;

import vtk.vtkDataArray;
import vtk.vtkPolyData;
import vtk.vtksbCellLocator;

import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.LatLon;


public abstract class PolyhedralModel extends AbstractModel
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

    public enum ColoringValueType {
        POINT_DATA,
        CELLDATA
    }

    public enum ShadingType {
        FLAT,
        SMOOTH,
    }

    public enum Format {
        TXT,
        FIT,
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

    private ViewConfig config;
    public PolyhedralModel(ViewConfig config)
    {
        this.config = config;
    }

    public abstract void updateScaleBarValue(double pixelSizeInKm);

    public abstract void updateScaleBarPosition(int windowWidth, int windowHeight);

    public abstract vtksbCellLocator getCellLocator();

    public abstract BoundingBox getBoundingBox();

    public abstract void setShowScaleBar(boolean enabled);

    public abstract boolean getShowScaleBar();

    public abstract vtkPolyData getSmallBodyPolyData();

    public abstract boolean isEllipsoid();


    public abstract String getCustomDataFolder();

    public abstract String getConfigFilename();

    public abstract vtkDataArray getGravityVectorData();

    public abstract List<LidarDatasourceInfo> getLidarDasourceInfoList();

    public abstract int getLidarDatasourceIndex();

    public abstract void setLidarDatasourceIndex(int index);

    public abstract String getLidarDatasourceName(int i);

    public abstract String getLidarDatasourcePath(int i);

    public abstract int getNumberOfLidarDatasources();

    public abstract int getModelResolution();

    public abstract TreeSet<Integer> getIntersectingCubes(vtkPolyData polydata);

    public abstract TreeSet<Integer> getIntersectingCubes(BoundingBox bb);

    public abstract void addCustomLidarDatasource(LidarDatasourceInfo info) throws IOException;

    public abstract void setCustomLidarDatasource(int index, LidarDatasourceInfo info) throws IOException;


    public abstract void loadCustomLidarDatasourceInfo();

    public abstract void removeCustomLidarDatasource(int index) throws IOException;

    public abstract void setCustomPlateData(int index, ColoringInfo info) throws IOException;

    public abstract void addCustomPlateData(ColoringInfo info) throws IOException;

    public abstract void removeCustomPlateData(int index) throws IOException;

    public abstract List<ColoringInfo> getColoringInfoList();

    public abstract int getNumberOfCustomColors();

    public abstract void saveAsPLT(File file) throws IOException;

    public abstract void saveAsOBJ(File file) throws IOException;

    public abstract void saveAsVTK(File file) throws IOException;

    public abstract void saveAsSTL(File file) throws IOException;

    public abstract int getNumberResolutionLevels();

//    public abstract Config getSmallBodyConfig();

    public abstract void drawRegularPolygonLowRes(
            double[] center,
            double radius,
            int numberOfSides,
            vtkPolyData outputInterior,
            vtkPolyData outputBoundary);

    public abstract String getCustomDataRootFolder();

    public abstract String getDEMConfigFilename();

    public abstract boolean isImageMapAvailable();

    public abstract int getNumberOfBuiltInColors();

    public abstract void setShowSmallBody(boolean show);

    public abstract void setModelResolution(int level) throws IOException;

    public abstract void setFalseColoring(int redChannel, int greenChannel, int blueChannel) throws IOException;

    public abstract String[] getImageMapNames();

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

    public void setOpacity(double opacity)
    {
        // Do nothing. Subclasses should redefine this if they support opacity.
    }

    public double getOpacity()
    {
        // Subclasses should redefine this if they support opacity.
        return 1.0;
    }


    public double getDefaultOffset()
    {
        // Subclasses should redefine this if they support offset.
        return 0.0;
    }

    public List<vtkPolyData> getSmallBodyPolyDatas() { return null; }

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

    public abstract void drawEllipticalPolygon(
            double[] center,
            double radius,
            double flattening,
            double angle,
            int numberOfSides,
            vtkPolyData outputInterior,
            vtkPolyData outputBoundary);

    public abstract double getBoundingBoxDiagonalLength();

    public abstract void shiftPolyLineInNormalDirection(
            vtkPolyData polyLine,
            double shiftAmount);

    public abstract int getPointAndCellIdFromLatLon(double lat, double lon, double[] intersectPoint);

    public abstract double[] getNormalAtPoint(double[] point);

    public abstract boolean isColoringDataAvailable();

    public abstract double[] getAllColoringValues(double[] pt);

    public abstract double[] getGravityVector(double[] pt);

    public abstract double getMinShiftAmount();

    public abstract void savePlateDataInsidePolydata(vtkPolyData polydata, File file) throws IOException;

    public abstract String getModelName();

    public abstract vtkPolyData drawPath(
            double[] pt1,
            double[] pt2);

    public abstract double[] findClosestPoint(double[] pt);

    public abstract double getColoringValue(int index, double[] pt);

    public abstract int getNumberOfColors();

    public abstract String getColoringUnits(int i);

    public abstract double getDensity();

    public abstract double getRotationRate();

    public abstract double getReferencePotential();

    public abstract double[] getClosestNormal(double[] point);

    public abstract void drawPolygon(
            List<LatLon> controlPoints,
            vtkPolyData outputInterior,
            vtkPolyData outputBoundary);







}

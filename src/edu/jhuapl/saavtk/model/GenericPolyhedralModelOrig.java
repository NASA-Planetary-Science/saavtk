package edu.jhuapl.saavtk.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import vtk.vtkAbstractPointLocator;
import vtk.vtkActor;
import vtk.vtkActor2D;
import vtk.vtkCell;
import vtk.vtkCellArray;
import vtk.vtkCellData;
import vtk.vtkCoordinate;
import vtk.vtkDataArray;
import vtk.vtkFloatArray;
import vtk.vtkGenericCell;
import vtk.vtkIdList;
import vtk.vtkLookupTable;
import vtk.vtkMassProperties;
import vtk.vtkPointLocator;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataMapper2D;
import vtk.vtkPolyDataNormals;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkScalarBarActor;
import vtk.vtkTextActor;
import vtk.vtkTextProperty;
import vtk.vtkTriangle;
import vtk.vtkUnsignedCharArray;
import vtk.vtksbCellLocator;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.Preferences;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.util.SmallBodyCubes;

public class GenericPolyhedralModelOrig extends PolyhedralModel
{




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




    @Override
    public int getLidarDatasourceIndex()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setLidarDatasourceIndex(int index)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public String getLidarDatasourceName(int i)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLidarDatasourcePath(int i)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getNumberOfLidarDatasources()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void loadCustomLidarDatasourceInfo()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeCustomLidarDatasource(int index) throws IOException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public String getCustomDataRootFolder()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDEMConfigFilename()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public static final String CELL_DATA_PATHS = "CellDataPaths"; // for backwards compatibility
    public static final String CELL_DATA_FILENAMES = "CellDataFilenames";
    public static final String CELL_DATA_NAMES = "CellDataNames";
    public static final String CELL_DATA_UNITS = "CellDataUnits";
    public static final String CELL_DATA_HAS_NULLS = "CellDataHasNulls";
    public static final String CELL_DATA_RESOLUTION_LEVEL = "CellDataResolutionLevel";

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

    ArrayList<ColoringInfo> coloringInfo = new ArrayList<ColoringInfo>();
    private ColoringValueType coloringValueType;
    private int coloringIndex = -1;
    // If true, a false color will be used by using 3 of the existing
    // colors for the red, green, and blue channels
    private boolean useFalseColoring = false;
    private int redFalseColor = -1; // red channel for false coloring
    private int greenFalseColor = -1; // green channel for false coloring
    private int blueFalseColor = -1; // blue channel for false coloring
    private vtkUnsignedCharArray colorData;
    private vtkUnsignedCharArray falseColorArray;

    private vtkPolyData smallBodyPolyData;
    private vtkPolyData lowResSmallBodyPolyData;
    private vtkActor smallBodyActor;
    private vtkPolyDataMapper smallBodyMapper;
    private ArrayList<vtkProp> smallBodyActors = new ArrayList<vtkProp>();
    private vtksbCellLocator cellLocator;
    private vtkPointLocator pointLocator;
    private vtkPointLocator lowResPointLocator;
    private vtkScalarBarActor scalarBarActor;
    private SmallBodyCubes smallBodyCubes;
    private String defaultModelFileName;
    private File defaultModelFile;
    private int resolutionLevel = 0;
    private vtkGenericCell genericCell;
    private String[] modelNames;
    public String[] getModelNames()
    {
        return modelNames;
    }

    private String[] modelFiles;
    public String[] getModelFiles()
    {
        return modelFiles;
    }

    private String[] imageMapNames = null;
    private BoundingBox boundingBox = null;
    private vtkIdList idList; // to avoid repeated allocations
    private vtkFloatArray gravityVector;

    private vtkFloatArray cellNormals;
    private double surfaceArea = -1.0;
    private double volume = -1.0;
    private double minCellArea = -1.0;
    private double maxCellArea = -1.0;
    private double meanCellArea = -1.0;

    // variables related to the scale bar (note the scale bar is different
    // from the scalar bar)
    private vtkPolyData scaleBarPolydata;
    private vtkPolyDataMapper2D scaleBarMapper;
    private vtkActor2D scaleBarActor;
    private vtkTextActor scaleBarTextActor;
    private int scaleBarWidthInPixels = 0;
    private double scaleBarWidthInKm = -1.0;
    private boolean showScaleBar = true;

    /**
     * Default constructor. Must be followed by a call to setSmallBodyPolyData.
     */
    public GenericPolyhedralModelOrig(GenericPolyhedralModelConfig config)
    {
        super(config);
        smallBodyPolyData = new vtkPolyData();
        genericCell = new vtkGenericCell();
        idList = new vtkIdList();
    }

    /**
     * Note that name is used to name this small body model as a whole including all
     * resolution levels whereas modelNames is an array of names that is specific
     * for each resolution level.
     */
    public GenericPolyhedralModelOrig(
            GenericPolyhedralModelConfig config,
            String[] modelNames,
            String[] modelFiles,
            String[] coloringFiles,
            String[] coloringNames,
            String[] coloringUnits,
            boolean[] coloringHasNulls,
            String[] imageMapNames,
            ColoringValueType coloringValueType,
            boolean lowestResolutionModelStoredInResource)
//                    super(config, null, null, null, null, null, null, null, ColoringValueType.CELLDATA, true);

    {
        super(config);
        this.modelNames = modelNames;
        this.modelFiles = modelFiles;
        setDefaultModelFileName(this.modelFiles[0]);
        this.imageMapNames = imageMapNames;
        this.coloringValueType = coloringValueType;
        if (coloringNames != null)
        {
            for (int i=0; i<coloringNames.length; ++i)
            {
                ColoringInfo info = new ColoringInfo();
                info.coloringName = coloringNames[i];
                info.coloringFile = coloringFiles[i];
                info.coloringUnits = coloringUnits[i];
                if (coloringHasNulls != null)
                    info.coloringHasNulls = coloringHasNulls[i];
                coloringInfo.add(info);
            }
        }

        colorData = new vtkUnsignedCharArray();
        smallBodyPolyData = new vtkPolyData();
        genericCell = new vtkGenericCell();
        idList = new vtkIdList();
    }

    public List<vtkPolyData> getSmallBodyPolyDatas()
    {
        return null;
    }

    public void setDefaultModelFileName(String defaultModelFileName)
    {
        this.defaultModelFileName = defaultModelFileName;
        defaultModelFile = new File(defaultModelFileName);
    }

    public void setSmallBodyPolyData(vtkPolyData polydata,
            vtkFloatArray[] coloringValues,
            String[] coloringNames,
            String[] coloringUnits,
            ColoringValueType coloringValueType)
    {
        smallBodyPolyData.DeepCopy(polydata);
        coloringInfo.clear();
        for (int i=0; i<coloringNames.length; ++i)
        {
            ColoringInfo info = new ColoringInfo();
            info.coloringName = coloringNames[i];
            info.coloringUnits = coloringUnits[i];
            info.coloringValues = coloringValues[i];
            coloringInfo.add(info);
        }
        this.coloringValueType = coloringValueType;

        initializeLocators();
        initializeColoringRanges();
    }

    public GenericPolyhedralModelConfig getDefaultModelConfig()
    {
        return (GenericPolyhedralModelConfig)getPolyhedralModelConfig();
    }

    public boolean isBuiltIn()
    {
        return true;
    }

    public String getCustomDataFolder()
    {
        String imagesDir = null;
        if (isBuiltIn())
        {
            imagesDir = Configuration.getCustomDataFolderForBuiltInViews() + File.separator + getDefaultModelConfig().getUniqueName();
        }
        else
        {
            imagesDir = Configuration.getImportedShapeModelsDir() + File.separator + getModelName();
        }

        // if the directory does not exist, create it
        File dir = new File(imagesDir);
        if (!dir.exists())
        {
            dir.mkdirs();
        }

        return imagesDir;
    }

    public String getConfigFilename()
    {
        return getCustomDataFolder() + File.separator + "config.txt";
    }

    private void clearCustomColoringInfo()
    {
        for (int i=coloringInfo.size()-1; i>=0; --i)
        {
            if (!coloringInfo.get(i).builtIn)
                coloringInfo.remove(i);
        }
    }

    public void loadCustomColoringInfo() throws IOException
    {
        String prevColoringName = null;
        if (coloringIndex >= 0)
            prevColoringName = coloringInfo.get(coloringIndex).coloringName;

        clearCustomColoringInfo();

        String configFilename = getConfigFilename();

        if (!(new File(configFilename).exists()))
            return;

        MapUtil configMap = new MapUtil(configFilename);

//        convertOldConfigFormatToNewVersion(configMap);

        if (configMap.containsKey(GenericPolyhedralModelOrig.CELL_DATA_FILENAMES) &&
                configMap.containsKey(GenericPolyhedralModelOrig.CELL_DATA_NAMES) &&
                configMap.containsKey(GenericPolyhedralModelOrig.CELL_DATA_UNITS) &&
                configMap.containsKey(GenericPolyhedralModelOrig.CELL_DATA_HAS_NULLS))
        {
            String[] cellDataFilenames = configMap.get(GenericPolyhedralModelOrig.CELL_DATA_FILENAMES).split(",", -1);
            String[] cellDataNames = configMap.get(GenericPolyhedralModelOrig.CELL_DATA_NAMES).split(",", -1);
            String[] cellDataUnits = configMap.get(GenericPolyhedralModelOrig.CELL_DATA_UNITS).split(",", -1);
            String[] cellDataHasNulls = configMap.get(GenericPolyhedralModelOrig.CELL_DATA_HAS_NULLS).split(",", -1);
            String[] cellDataResolutionLevels = null;
            if (configMap.containsKey(GenericPolyhedralModelOrig.CELL_DATA_RESOLUTION_LEVEL))
                cellDataResolutionLevels = configMap.get(GenericPolyhedralModelOrig.CELL_DATA_RESOLUTION_LEVEL).split(",", -1);

            for (int i=0; i<cellDataFilenames.length; ++i)
            {
                ColoringInfo info = new ColoringInfo();
                info.coloringFile = cellDataFilenames[i];
                if (!info.coloringFile.trim().isEmpty())
                {
                    info.coloringName = cellDataNames[i];
                    info.coloringUnits = cellDataUnits[i];
                    info.coloringHasNulls = Boolean.parseBoolean(cellDataHasNulls[i]);
                    info.builtIn = false;
                    if (cellDataResolutionLevels != null)
                    {
                        info.resolutionLevel = Integer.parseInt(cellDataResolutionLevels[i]);
                        if (info.resolutionLevel == getModelResolution())
                            coloringInfo.add(info);
                    }
                    else
                    {
                        info.resolutionLevel = 0;
                        coloringInfo.add(info);
                    }
                }
            }
        }

        // See if there's color of the same name as previously shown and set it to that.
        coloringIndex = -1;
        for (int i=0; i<coloringInfo.size(); ++i)
        {
            if (prevColoringName != null && prevColoringName.equals(coloringInfo.get(i).coloringName))
            {
                coloringIndex = i;
                break;
            }
        }
    }

    private boolean defaultModelInitialized;
    public boolean isDefaultModelInitialized() { return defaultModelInitialized; }

    public void initializeDefaultModel()
    {
        // Load in custom plate data
        try
        {
            loadCustomColoringInfo();

            smallBodyPolyData.ShallowCopy(
                    PolyDataUtil.loadShapeModel(defaultModelFile.getAbsolutePath()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        initializeLocators();

        this.computeShapeModelStatistics();

        //this.computeLargestSmallestEdgeLength();
        //this.computeSurfaceArea();

        defaultModelInitialized = true;
    }

    protected void initializeLocators()
    {
        if (cellLocator == null)
        {
            cellLocator = new vtksbCellLocator();
            pointLocator = new vtkPointLocator();
        }

        // Initialize the cell locator
        cellLocator.FreeSearchStructure();
        cellLocator.SetDataSet(smallBodyPolyData);
        cellLocator.CacheCellBoundsOn();
        cellLocator.AutomaticOn();
        //cellLocator.SetMaxLevel(10);
        //cellLocator.SetNumberOfCellsPerNode(5);
        cellLocator.BuildLocator();

        pointLocator.FreeSearchStructure();
        pointLocator.SetDataSet(smallBodyPolyData);
        pointLocator.BuildLocator();
    }

    private void initializeLowResData()
    {
        if (lowResPointLocator == null)
        {
            lowResSmallBodyPolyData = new vtkPolyData();

            try
            {
                lowResSmallBodyPolyData.ShallowCopy(
                        PolyDataUtil.loadShapeModel(defaultModelFile.getAbsolutePath()));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            lowResPointLocator = new vtkPointLocator();
            lowResPointLocator.SetDataSet(lowResSmallBodyPolyData);
            lowResPointLocator.BuildLocator();
        }
    }

    public vtkPolyData getSmallBodyPolyData()
    {
        return smallBodyPolyData;
    }

    public vtkPolyData getLowResSmallBodyPolyData()
    {
        initializeLowResData();

        return lowResSmallBodyPolyData;
    }

    public vtksbCellLocator getCellLocator()
    {
        return cellLocator;
    }

    public vtkAbstractPointLocator getPointLocator()
    {
        return pointLocator;
    }

    public SmallBodyCubes getSmallBodyCubes()
    {
        if (smallBodyCubes == null)
        {
            // The number 38.66056033363347 is used here so that the cube size
            // comes out to 1 km for Eros.

            // Compute bounding box diagonal length of lowest res shape model
            double diagonalLength =
                    new BoundingBox(getLowResSmallBodyPolyData().GetBounds()).getDiagonalLength();
            double cubeSize = diagonalLength / 38.66056033363347;
            smallBodyCubes = new SmallBodyCubes(
                    getLowResSmallBodyPolyData(),
                    cubeSize,
                    0.01 * cubeSize,
                    true);
        }

        return smallBodyCubes;
    }

    public TreeSet<Integer> getIntersectingCubes(vtkPolyData polydata)
    {
        return getSmallBodyCubes().getIntersectingCubes(polydata);
    }

    public TreeSet<Integer> getIntersectingCubes(BoundingBox bb)
    {
        return getSmallBodyCubes().getIntersectingCubes(bb);
    }

    public int getCubeId(double[] point)
    {
        return getSmallBodyCubes().getCubeId(point);
    }

    public vtkFloatArray getCellNormals()
    {
        // Compute the normals of necessary. For now don't add the normals to the cell
        // data of the small body model since doing so might create problems.
        // TODO consider adding normals to cell data without creating problems
        if (cellNormals == null)
        {
            vtkPolyDataNormals normalsFilter = new vtkPolyDataNormals();
            normalsFilter.SetInputData(smallBodyPolyData);
            normalsFilter.SetComputeCellNormals(1);
            normalsFilter.SetComputePointNormals(0);
            normalsFilter.SplittingOff();
            normalsFilter.Update();

            vtkPolyData normalsFilterOutput = normalsFilter.GetOutput();
            vtkCellData normalsFilterOutputCellData = normalsFilterOutput.GetCellData();
            vtkFloatArray normals = (vtkFloatArray)normalsFilterOutputCellData.GetNormals();

            cellNormals = new vtkFloatArray();
            cellNormals.DeepCopy(normals);

            normals.Delete();
            normalsFilterOutputCellData.Delete();
            normalsFilterOutput.Delete();
            normalsFilter.Delete();
        }

        return cellNormals;
    }

    public void setShowSmallBody(boolean show)
    {
        if (show)
        {
            if (!smallBodyActors.contains(smallBodyActor))
            {
                smallBodyActors.add(smallBodyActor);
                this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
            }
        }
        else
        {
            if (smallBodyActors.contains(smallBodyActor))
            {
                smallBodyActors.remove(smallBodyActor);
                this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
            }
        }

    }

    public vtkPolyData computeFrustumIntersection(
            double[] origin,
            double[] ul,
            double[] ur,
            double[] lr,
            double[] ll)
    {
        return PolyDataUtil.computeFrustumIntersection(smallBodyPolyData, cellLocator, pointLocator, origin, ul, ur, lr, ll);
    }

    public vtkPolyData computeMultipleFrustumIntersection(ArrayList<Frustum> frustums)
    {
        return PolyDataUtil.computeMultipleFrustumIntersection(smallBodyPolyData, cellLocator, pointLocator, frustums);
    }

    /**
     * Given 2 points on the surface of the body, draw a nice looking path between the 2
     * that is not obscured anywhere or too distant from the surface. Return this
     * path as a vtkPolyData
     * @param pt1
     * @param pt2
     * @return
     */
    public vtkPolyData drawPath(
            double[] pt1,
            double[] pt2)
    {
        return PolyDataUtil.drawPathOnPolyData(smallBodyPolyData, pointLocator, pt1, pt2);
    }

    public void drawPolygon(
            ArrayList<LatLon> controlPoints,
            vtkPolyData outputInterior,
            vtkPolyData outputBoundary)
    {
        PolyDataUtil.drawPolygonOnPolyData(
                smallBodyPolyData,
                pointLocator,
                controlPoints,
                outputInterior,
                outputBoundary);
    }

    public void drawRegularPolygon(
            double[] center,
            double radius,
            int numberOfSides,
            vtkPolyData outputInterior,
            vtkPolyData outputBoundary)
    {
        PolyDataUtil.drawRegularPolygonOnPolyData(
                smallBodyPolyData,
                pointLocator,
                center,
                radius,
                numberOfSides,
                outputInterior,
                outputBoundary);
    }

    public void drawEllipticalPolygon(
            double[] center,
            double radius,
            double flattening,
            double angle,
            int numberOfSides,
            vtkPolyData outputInterior,
            vtkPolyData outputBoundary)
    {
        PolyDataUtil.drawEllipseOnPolyData(
                smallBodyPolyData,
                pointLocator,
                center,
                radius,
                flattening,
                angle,
                numberOfSides,
                outputInterior,
                outputBoundary);
    }

    public void drawRegularPolygonLowRes(
            double[] center,
            double radius,
            int numberOfSides,
            vtkPolyData outputInterior,
            vtkPolyData outputBoundary)
    {
        if (resolutionLevel == 0)
        {
            drawRegularPolygon(center, radius, numberOfSides, outputInterior, outputBoundary);
            return;
        }

        initializeLowResData();

        PolyDataUtil.drawRegularPolygonOnPolyData(
                lowResSmallBodyPolyData,
                lowResPointLocator,
                center,
                radius,
                numberOfSides,
                outputInterior,
                outputBoundary);
    }

    public void drawCone(
            double[] vertex,
            double[] axis,
            double angle,
            int numberOfSides,
            vtkPolyData outputInterior,
            vtkPolyData outputBoundary)
    {
        PolyDataUtil.drawConeOnPolyData(
                smallBodyPolyData,
                pointLocator,
                vertex,
                axis,
                angle,
                numberOfSides,
                outputInterior,
                outputBoundary);
    }

    public void shiftPolyLineInNormalDirection(
            vtkPolyData polyLine,
            double shiftAmount)
    {
        PolyDataUtil.shiftPolyLineInNormalDirectionOfPolyData(
                polyLine,
                smallBodyPolyData,
                pointLocator,
                shiftAmount);
    }

    public double[] getNormalAtPoint(double[] point)
    {
        return PolyDataUtil.getPolyDataNormalAtPoint(point, smallBodyPolyData, pointLocator);
    }

    public double[] getClosestNormal(double[] point)
    {
        int closestCell = findClosestCell(point);
        return getCellNormals().GetTuple3(closestCell);
    }

    /**
     * This returns the closest point to the model to pt. Note the returned point need
     * not be a vertex of the model and can lie anywhere on a plate.
     * @param pt
     * @return
     */
    public double[] findClosestPoint(double[] pt)
    {
        double[] closestPoint = new double[3];
        int[] cellId = new int[1];
        int[] subId = new int[1];
        double[] dist2 = new double[1];

        cellLocator.FindClosestPoint(pt, closestPoint, genericCell, cellId, subId, dist2);

        return closestPoint;
    }

    public long findClosestVertex(double[] pt)
    {
        return pointLocator.FindClosestPoint(pt);
    }

    /**
     * This returns the index of the closest cell in the model to pt.
     * The closest point within the cell is returned in closestPoint
     * @param pt
     * @param closestPoint the closest point within the cell is returned here
     * @return
     */
    public int findClosestCell(double[] pt, double[] closestPoint)
    {
        int[] cellId = new int[1];
        int[] subId = new int[1];
        double[] dist2 = new double[1];

        // Use FindClosestPoint rather the FindCell since not sure what tolerance to use in the latter.
        cellLocator.FindClosestPoint(pt, closestPoint, genericCell, cellId, subId, dist2);

        return cellId[0];
    }

    /**
     * This returns the index of the closest cell in the model to pt.
     * @param pt
     * @return
     */
    public int findClosestCell(double[] pt)
    {
        double[] closestPoint = new double[3];
        return findClosestCell(pt, closestPoint);
    }

    /**
     * Compute the point on the asteroid that has the specified latitude and longitude. Returns the
     * cell id of the cell containing that point. This is done by shooting a ray from the origin in the
     * specified direction.
     * @param lat - in radians
     * @param lon - in radians
     * @param intersectPoint
     * @return the cellId of the cell containing the intersect point
     */
    public int getPointAndCellIdFromLatLon(double lat, double lon, double[] intersectPoint)
    {
        LatLon lla = new LatLon(lat, lon);
        double[] lookPt = MathUtil.latrec(lla);

        // Move in the direction of lookPt until we are definitely outside the asteroid
        BoundingBox bb = getBoundingBox();
        double largestSide = bb.getLargestSide() * 1.1;
        lookPt[0] *= largestSide;
        lookPt[1] *= largestSide;
        lookPt[2] *= largestSide;

        double[] origin = {0.0, 0.0, 0.0};
        double tol = 1e-6;
        double[] t = new double[1];
        double[] x = new double[3];
        double[] pcoords = new double[3];
        int[] subId = new int[1];
        int[] cellId = new int[1];

        int result = cellLocator.IntersectWithLine(origin, lookPt, tol, t, x, pcoords, subId, cellId, genericCell);

        intersectPoint[0] = x[0];
        intersectPoint[1] = x[1];
        intersectPoint[2] = x[2];

        if (result > 0)
            return cellId[0];
        else
            return -1;
    }

    /**
     * Compute the intersection of a ray with the asteroid. Returns the
     * cell id of the cell containing that point. This is done by shooting
     * a ray from the specified origin in the specified direction.
     * @param origin point
     * @param direction vector (must be unit vector)
     * @param intersectPoint (returned)
     * @return the cellId of the cell containing the intersect point
     */
    public int computeRayIntersection(double[] origin, double[] direction, double[] intersectPoint)
    {
        double distance = MathUtil.vnorm(origin);
        double[] lookPt = new double[3];
        lookPt[0] = origin[0] + 2.0*distance*direction[0];
        lookPt[1] = origin[1] + 2.0*distance*direction[1];
        lookPt[2] = origin[2] + 2.0*distance*direction[2];

        double tol = 1e-6;
        double[] t = new double[1];
        double[] x = new double[3];
        double[] pcoords = new double[3];
        int[] subId = new int[1];
        int[] cellId = new int[1];

        int result = cellLocator.IntersectWithLine(origin, lookPt, tol, t, x, pcoords, subId, cellId, genericCell);

        intersectPoint[0] = x[0];
        intersectPoint[1] = x[1];
        intersectPoint[2] = x[2];

        if (result > 0)
            return cellId[0];
        else
            return -1;
    }

    public void reinitialize()
    {
        smallBodyActor = null;
    }

    protected void initializeActorsAndMappers()
    {
        if (smallBodyActor == null)
        {
            smallBodyMapper = new vtkPolyDataMapper();
            smallBodyMapper.SetInputData(smallBodyPolyData);
            vtkLookupTable lookupTable = new vtkLookupTable();
            smallBodyMapper.SetLookupTable(lookupTable);
            smallBodyMapper.UseLookupTableScalarRangeOn();

            smallBodyActor = new vtkActor();
            smallBodyActor.SetMapper(smallBodyMapper);
            vtkProperty smallBodyProperty = smallBodyActor.GetProperty();
            smallBodyProperty.SetInterpolationToGouraud();
            //smallBodyProperty.SetSpecular(.1);
            //smallBodyProperty.SetSpecularPower(100);

            smallBodyActors.add(smallBodyActor);

            scalarBarActor = new vtkScalarBarActor();
            vtkCoordinate coordinate = scalarBarActor.GetPositionCoordinate();
            coordinate.SetCoordinateSystemToNormalizedViewport();
            coordinate.SetValue(0.2, 0.01);
            scalarBarActor.SetOrientationToHorizontal();
            scalarBarActor.SetWidth(0.6);
            scalarBarActor.SetHeight(0.1275);
            vtkTextProperty tp = new vtkTextProperty();
            tp.SetFontSize(10);
            scalarBarActor.SetTitleTextProperty(tp);

            setupScaleBar();
        }
    }

    public ArrayList<vtkProp> getProps()
    {
        initializeActorsAndMappers();

        return smallBodyActors;
    }

    public void setShadingToFlat()
    {
        initializeActorsAndMappers();

        vtkProperty property = smallBodyActor.GetProperty();

        if (property.GetInterpolation() != 0) // The value 0 corresponds to flat (see vtkProperty.h)
        {
            property.SetInterpolationToFlat();
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    public void setShadingToSmooth()
    {
        initializeActorsAndMappers();

        vtkProperty property = smallBodyActor.GetProperty();

        if (property.GetInterpolation() != 1) // The value 1 corresponds to gouraud (see vtkProperty.h)
        {
            property.SetInterpolationToGouraud();
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    public BoundingBox getBoundingBox()
    {
        if (boundingBox == null)
        {
            smallBodyPolyData.ComputeBounds();
            boundingBox = new BoundingBox(smallBodyPolyData.GetBounds());
        }

        return boundingBox;

        /*
        BoundingBox bb = new BoundingBox();
        vtkPoints points = smallBodyPolyData.GetPoints();
        int numberPoints = points.GetNumberOfPoints();
        for (int i=0; i<numberPoints; ++i)
        {
            double[] pt = points.GetPoint(i);
            bb.update(pt[0], pt[1], pt[2]);
        }

        return bb;
        */
    }

    public double getBoundingBoxDiagonalLength()
    {
        return getBoundingBox().getDiagonalLength();
    }

    /**
     * Get the minimum shift amount needed so shift an object away from
     * the model so it is not obscured by the model and looks like it's
     * laying on the model
     * @return
     */
    public double getMinShiftAmount()
    {
        return getBoundingBoxDiagonalLength() / 38660.0;
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        if (coloringIndex >= 0)
        {
            float value = (float)getColoringValue(coloringIndex, pickPosition);
            ColoringInfo info = coloringInfo.get(coloringIndex);
            return info.coloringName + ": " + value + " " + info.coloringUnits;
        }
        else if (useFalseColoring)
        {
            float red = (float)getColoringValue(redFalseColor, pickPosition);
            float green = (float)getColoringValue(greenFalseColor, pickPosition);
            float blue = (float)getColoringValue(blueFalseColor, pickPosition);
            ColoringInfo redInfo = coloringInfo.get(redFalseColor);
            ColoringInfo greenInfo = coloringInfo.get(greenFalseColor);
            ColoringInfo blueInfo = coloringInfo.get(blueFalseColor);
            return redInfo.coloringName + ": " + red + " " + redInfo.coloringUnits + ", " +
                   greenInfo.coloringName + ": " + green + " " + greenInfo.coloringUnits +  ", " +
                   blueInfo.coloringName + ": " + blue + " " + blueInfo.coloringUnits;
        }
        return "";
    }

    public double[] computeLargestSmallestMeanEdgeLength()
    {
        double[] largestSmallestMean = new double[3];

        double minLength = Double.MAX_VALUE;
        double maxLength = 0.0;
        double meanLength = 0.0;

        int numberOfCells = smallBodyPolyData.GetNumberOfCells();

        System.out.println(numberOfCells);

        for (int i=0; i<numberOfCells; ++i)
        {
            vtkCell cell = smallBodyPolyData.GetCell(i);
            vtkPoints points = cell.GetPoints();
            double[] pt0 = points.GetPoint(0);
            double[] pt1 = points.GetPoint(1);
            double[] pt2 = points.GetPoint(2);
            double dist0 = MathUtil.distanceBetween(pt0, pt1);
            double dist1 = MathUtil.distanceBetween(pt1, pt2);
            double dist2 = MathUtil.distanceBetween(pt2, pt0);
            if (dist0 < minLength)
                minLength = dist0;
            if (dist0 > maxLength)
                maxLength = dist0;
            if (dist1 < minLength)
                minLength = dist1;
            if (dist1 > maxLength)
                maxLength = dist1;
            if (dist2 < minLength)
                minLength = dist2;
            if (dist2 > maxLength)
                maxLength = dist2;

            meanLength += (dist0 + dist1 + dist2);
            points.Delete();
            cell.Delete();
        }

        meanLength /= ((double)(numberOfCells * 3));

        System.out.println("minLength  " + minLength);
        System.out.println("maxLength  " + maxLength);
        System.out.println("meanLength  " + meanLength);

        largestSmallestMean[0] = minLength;
        largestSmallestMean[1] = maxLength;
        largestSmallestMean[2] = meanLength;

        return largestSmallestMean;
    }

    protected void computeShapeModelStatistics()
    {
        vtkMassProperties massProp = new vtkMassProperties();
        massProp.SetInputData(smallBodyPolyData);
        massProp.Update();

        surfaceArea = massProp.GetSurfaceArea();
        volume = massProp.GetVolume();
        meanCellArea = surfaceArea / (double)smallBodyPolyData.GetNumberOfCells();
        minCellArea = massProp.GetMinCellArea();
        maxCellArea = massProp.GetMaxCellArea();

        /*

        // The following computes the surface area directly rather than using vtkMassProperties
        // It gives exactly the same results as vtkMassProperties but is much slower.

        int numberOfCells = smallBodyPolyData.GetNumberOfCells();

        System.out.println(numberOfCells);
        double totalArea = 0.0;
        minCellArea = Double.MAX_VALUE;
        maxCellArea = 0.0;
        for (int i=0; i<numberOfCells; ++i)
        {
            vtkCell cell = smallBodyPolyData.GetCell(i);
            vtkPoints points = cell.GetPoints();
            double[] pt0 = points.GetPoint(0);
            double[] pt1 = points.GetPoint(1);
            double[] pt2 = points.GetPoint(2);
            double area = MathUtil.triangleArea(pt0, pt1, pt2);
            totalArea += area;
            if (area < minCellArea)
                minCellArea = area;
            if (area > maxCellArea)
                maxCellArea = area;
        }

        meanCellArea = totalArea / (double)(numberOfCells);


        System.out.println("Surface area   " + massProp.GetSurfaceArea());
        System.out.println("Surface area2  " + totalArea);
        System.out.println("min cell area  " + massProp.GetMinCellArea());
        System.out.println("min cell area2 " + minCellArea);
        System.out.println("max cell area  " + massProp.GetMaxCellArea());
        System.out.println("max cell area2 " + maxCellArea);
        System.out.println("Volume " + massProp.GetVolume());
        */
    }

    public double getSurfaceArea()
    {
        return surfaceArea;
    }

    public double getVolume()
    {
        return volume;
    }

    public double getMeanCellArea()
    {
        return meanCellArea;
    }

    public double getMinCellArea()
    {
        return minCellArea;
    }

    public double getMaxCellArea()
    {
        return maxCellArea;
    }

    public void setModelResolution(int level) throws IOException
    {
        if (level == resolutionLevel)
            return;

        resolutionLevel = level;
        if (level < 0)
            resolutionLevel = 0;
        else if (level > getNumberResolutionLevels()-1)
            resolutionLevel = getNumberResolutionLevels()-1;

        reloadShapeModel();
    }

    public void reloadShapeModel() throws IOException
    {
        smallBodyCubes = null;
        for (ColoringInfo info : coloringInfo)
        {
            info.coloringValues = null;
            info.defaultColoringRange = null;
        }

        cellNormals = null;
        gravityVector = null;
        boundingBox = null;

        if (resolutionLevel > 0)
        {
            defaultModelFile = FileCache.getFileFromServer(modelFiles[resolutionLevel]);
        }

        this.initializeDefaultModel();

        // Repaint the asteroid if we're currently showing any type of coloring
        if (coloringIndex >= 0 || useFalseColoring)
            paintBody();

        this.pcs.firePropertyChange(Properties.MODEL_RESOLUTION_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public int getModelResolution()
    {
        return resolutionLevel;
    }

    public int getNumberResolutionLevels()
    {
        return modelFiles.length;
    }

    public String getModelName()
    {
        if (resolutionLevel >= 0 && resolutionLevel < modelNames.length)
            return modelNames[resolutionLevel];
        else
            return null;
    }

    /**
     * This file loads the coloring data.
     * @throws IOException
     */
    private void loadColoringData() throws IOException
    {
        for (ColoringInfo info : coloringInfo)
        {
            // If not null, that means we've already loaded it.
            if (info.coloringValues != null)
                continue;

            String filename = info.coloringFile;
            if (!info.builtIn)
                filename = FileCache.FILE_PREFIX + getCustomDataFolder() + File.separator + filename;
            if (!filename.startsWith(FileCache.FILE_PREFIX))
                filename += "_res" + resolutionLevel + ".txt.gz";
            File file = FileCache.getFileFromServer(filename);
            if (file == null)
                throw new IOException("Unable to download " + filename);

            FileInputStream fs =  new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fs);
            BufferedReader in = new BufferedReader(isr);

            vtkFloatArray array = new vtkFloatArray();

            array.SetNumberOfComponents(1);
            if (coloringValueType == ColoringValueType.POINT_DATA)
                array.SetNumberOfTuples(smallBodyPolyData.GetNumberOfPoints());
            else
                array.SetNumberOfTuples(smallBodyPolyData.GetNumberOfCells());

            String line;
            int j = 0;
            while ((line = in.readLine()) != null)
            {
                array.SetTuple1(j, Float.parseFloat(line));
                ++j;
            }

            in.close();

            info.coloringValues = array;
        }

        initializeColoringRanges();
    }

    private void invertLookupTableCharArray(vtkUnsignedCharArray table)
    {
        int numberOfValues = table.GetNumberOfTuples();
        for (int i=0; i<numberOfValues/2; ++i)
        {
            double[] v1 = table.GetTuple4(i);
            double[] v2 = table.GetTuple4(numberOfValues-i-1);
            table.SetTuple4(i, v2[0], v2[1], v2[2], v2[3]);
            table.SetTuple4(numberOfValues-i-1, v1[0], v1[1], v1[2], v1[3]);
        }
    }

    /**
     * Invert the lookup table so that red is high values
     * and blue is low values (rather than the reverse).
     */
    private void invertLookupTable()
    {
        vtkLookupTable lookupTable = (vtkLookupTable)smallBodyMapper.GetLookupTable();
        vtkUnsignedCharArray table = lookupTable.GetTable();

        invertLookupTableCharArray(table);
//        int numberOfValues = table.GetNumberOfTuples();
//        for (int i=0; i<numberOfValues/2; ++i)
//        {
//            double[] v1 = table.GetTuple4(i);
//            double[] v2 = table.GetTuple4(numberOfValues-i-1);
//            table.SetTuple4(i, v2[0], v2[1], v2[2], v2[3]);
//            table.SetTuple4(numberOfValues-i-1, v1[0], v1[1], v1[2], v1[3]);
//        }

        lookupTable.SetTable(table);
        smallBodyMapper.Modified();
    }

    public void setColoringIndex(int index) throws IOException
    {
        if (coloringIndex != index || useFalseColoring)
        {
            coloringIndex = index;
            useFalseColoring = false;

            paintBody();
        }
    }

    public int getColoringIndex()
    {
        return coloringIndex;
    }

    public void setFalseColoring(int redChannel, int greenChannel, int blueChannel) throws IOException
    {
        coloringIndex = -1;
        useFalseColoring = true;
        redFalseColor = redChannel;
        greenFalseColor = greenChannel;
        blueFalseColor = blueChannel;

        paintBody();
    }

    public int[] getFalseColoring()
    {
        return new int[]{redFalseColor, greenFalseColor, blueFalseColor};
    }

    public boolean isFalseColoringEnabled()
    {
        return useFalseColoring;
    }

    public boolean isColoringDataAvailable()
    {
        return coloringInfo.size() > 0;
    }

    public boolean isImageMapAvailable()
    {
        return imageMapNames != null && imageMapNames.length > 0;
    }

    public String[] getImageMapNames()
    {
        return imageMapNames;
    }

    public int getNumberOfColors()
    {
        return coloringInfo.size();
    }

    public int getNumberOfCustomColors()
    {
        int num = 0;
        for (ColoringInfo info : coloringInfo)
        {
            if (!info.builtIn)
                ++num;
        }
        return num;
    }

    public int getNumberOfBuiltInColors()
    {
        int num = 0;
        for (ColoringInfo info : coloringInfo)
        {
            if (info.builtIn)
                ++num;
        }
        return num;
    }

    public String getColoringName(int i)
    {
        if (i < coloringInfo.size())
            return coloringInfo.get(i).coloringName;
        else
            return null;
    }

    public String getColoringUnits(int i)
    {
        if (i < coloringInfo.size())
            return coloringInfo.get(i).coloringUnits;
        else
            return null;
    }

    private double getColoringValue(double[] pt, vtkFloatArray pointOrCellData)
    {
        double[] closestPoint = new double[3];
        int cellId = findClosestCell(pt, closestPoint);
        if (coloringValueType == ColoringValueType.POINT_DATA)
        {
            return PolyDataUtil.interpolateWithinCell(
                    smallBodyPolyData, pointOrCellData, cellId, closestPoint, idList);
        }
        else
        {
            return pointOrCellData.GetTuple1(cellId);
        }
    }

    /**
     * Get value assuming pt is exactly on the asteroid and cellId is provided
     * @param pt
     * @param pointOrCellData
     * @param cellId
     * @return
     */
    private double getColoringValue(double[] pt, vtkFloatArray pointOrCellData, int cellId)
    {
        if (coloringValueType == ColoringValueType.POINT_DATA)
        {
            return PolyDataUtil.interpolateWithinCell(
                    smallBodyPolyData, pointOrCellData, cellId, pt, idList);
        }
        else
        {
            return pointOrCellData.GetTuple1(cellId);
        }
    }

    public double getColoringValue(int index, double[] pt)
    {
        try
        {
            if (index >= 0 && index < coloringInfo.size() && coloringInfo.get(index).coloringValues == null)
                loadColoringData();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0.0;
        }

        return getColoringValue(pt, coloringInfo.get(index).coloringValues);
    }

    public double[] getAllColoringValues(double[] pt)
    {
        try
        {
            loadColoringData();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        double[] closestPoint = new double[3];
        int cellId = findClosestCell(pt, closestPoint);

        int numColors = coloringInfo.size();
        double[] values = new double[numColors];
        for (int i=0; i<numColors; ++i)
        {
            values[i] = getColoringValue(closestPoint, coloringInfo.get(i).coloringValues, cellId);
        }

        return values;
    }

    /**
     * Subclass must override this method if it wants to support loading
     * gravity vector.
     *
     * @param resolutionLevel
     * @return
     */
    protected String getGravityVectorFilePath(int resolutionLevel)
    {
        return null;
    }

    public double[] getGravityVector(double[] pt)
    {
        try
        {
            if (gravityVector == null)
            {
                boolean success = loadGravityVectorData();
                if (!success)
                    return null;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        double[] closestPoint = new double[3];
        int cellId = findClosestCell(pt, closestPoint);

        return gravityVector.GetTuple3(cellId);
    }

    public vtkDataArray getGravityVectorData()
    {
        try
        {
            if (gravityVector == null)
                loadGravityVectorData();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return gravityVector;
    }

    private boolean loadGravityVectorData() throws IOException
    {
        String filePath = getGravityVectorFilePath(resolutionLevel);
        if (filePath == null)
            return false;

        // Only cell data is supported now.
        if (coloringValueType == ColoringValueType.POINT_DATA)
            return false;

        File file = FileCache.getFileFromServer(filePath);

        gravityVector = new vtkFloatArray();
        gravityVector.SetNumberOfComponents(3);
        gravityVector.SetNumberOfTuples(smallBodyPolyData.GetNumberOfCells());

        FileReader ifs = new FileReader(file);
        BufferedReader in = new BufferedReader(ifs);

        String line;
        int j = 0;
        while ((line = in.readLine()) != null)
        {
            String[] vals = line.trim().split("\\s+");
            double x = Float.parseFloat(vals[0]);
            double y = Float.parseFloat(vals[1]);
            double z = Float.parseFloat(vals[2]);
            gravityVector.SetTuple3(j, x, y, z);
            ++j;
        }

        in.close();

        return true;
    }

    // Compute the range of an array but account for the fact that for some datasets,
    // some of the data is missing as represented by the lowest valued. So compute
    // the range ignoring this lowest value (i.e take the lowest value to be the value
    // just higher than the lowest value).
    private double[] computeDefaultColoringRange(int index)//, boolean adjustForColorTable)
    {
        ColoringInfo info = coloringInfo.get(index);

        double[] range = new double[2];
        info.coloringValues.GetRange(range);

        if (!info.coloringHasNulls)
        {
            return range;
        }
        else
        {
            vtkFloatArray array = info.coloringValues;
            int numberValues = array.GetNumberOfTuples();
            double adjustedMin = range[1];
            for (int i=0; i<numberValues; ++i)
            {
                double v = array.GetValue(i);
                if (v < adjustedMin && v > range[0])
                    adjustedMin = v;
            }

            range[0] = adjustedMin;

            return range;
        }
    }

    private void initializeColoringRanges()
    {
        int numberOfColors = getNumberOfColors();

        for (int i=0; i<numberOfColors; ++i)
        {
            double[] range = computeDefaultColoringRange(i);
            ColoringInfo info = coloringInfo.get(i);
            if (info.defaultColoringRange == null)
            {
                info.defaultColoringRange = new double[2];
                info.defaultColoringRange[0] = range[0];
                info.defaultColoringRange[1] = range[1];

                info.currentColoringRange = new double[2];
                info.currentColoringRange[0] = range[0];
                info.currentColoringRange[1] = range[1];
            }
        }
    }

    public double[] getDefaultColoringRange(int coloringIndex)
    {
        return coloringInfo.get(coloringIndex).defaultColoringRange;
    }

    public double[] getCurrentColoringRange(int coloringIndex)
    {
        return coloringInfo.get(coloringIndex).currentColoringRange;
    }

    public void setCurrentColoringRange(int coloringIndex, double[] range) throws IOException
    {
        ColoringInfo info = coloringInfo.get(coloringIndex);
        if (range[0] != info.currentColoringRange[0] ||
            range[1] != info.currentColoringRange[1])
        {
            info.currentColoringRange[0] = range[0];
            info.currentColoringRange[1] = range[1];

            paintBody();
        }
    }

    /**
     *  Update the false color point or cell data if
     */
    private void updateFalseColorArray()
    {
        if (falseColorArray == null)
        {
            falseColorArray = new vtkUnsignedCharArray();
            falseColorArray.SetNumberOfComponents(3);
        }

        vtkFloatArray red = coloringInfo.get(redFalseColor).coloringValues;
        vtkFloatArray green = coloringInfo.get(greenFalseColor).coloringValues;
        vtkFloatArray blue = coloringInfo.get(blueFalseColor).coloringValues;

        double[] redRange = getCurrentColoringRange(redFalseColor);
        double[] greenRange = getCurrentColoringRange(greenFalseColor);
        double[] blueRange = getCurrentColoringRange(blueFalseColor);
        double redExtent = redRange[1] - redRange[0];
        double greenExtent = greenRange[1] - greenRange[0];
        double blueExtent = blueRange[1] - blueRange[0];

        int numberTuples = red.GetNumberOfTuples();
        falseColorArray.SetNumberOfTuples(numberTuples);

        for (int i=0; i<numberTuples; ++i)
        {
            double redValue = 255.0 * (red.GetTuple1(i) - redRange[0]) / redExtent;
            double greenValue = 255.0 * (green.GetTuple1(i) - greenRange[0]) / greenExtent;
            double blueValue = 255.0 * (blue.GetTuple1(i) - blueRange[0]) / blueExtent;

            // Map invalid data to white
            if (redValue < 0.0)   redValue   = 255.0;
            if (greenValue < 0.0) greenValue = 255.0;
            if (blueValue < 0.0)  blueValue  = 255.0;

            falseColorArray.SetTuple3(i, redValue, greenValue, blueValue);
        }
    }

    private void paintBody() throws IOException
    {
        initializeActorsAndMappers();

        loadColoringData();

        vtkDataArray array = null;

        if (coloringIndex >= 0)
        {
            ColoringInfo info = coloringInfo.get(coloringIndex);
            array = info.coloringValues;
            String title = info.coloringName;
            if (!info.coloringUnits.isEmpty())
                title += " (" + info.coloringUnits + ")";
            scalarBarActor.SetTitle(title);
        }
        else if (useFalseColoring)
        {
            updateFalseColorArray();
            array = falseColorArray;
        }

        if (coloringIndex < 0)
        {
            if (smallBodyActors.contains(scalarBarActor))
                smallBodyActors.remove(scalarBarActor);
        }
        else
        {
            double[] range = getCurrentColoringRange(coloringIndex);
            smallBodyMapper.GetLookupTable().SetRange(range);
            ((vtkLookupTable)smallBodyMapper.GetLookupTable()).ForceBuild();
            this.invertLookupTable();

            ColoringInfo info = coloringInfo.get(coloringIndex);

            // If there's missing data (invalid data), we need to set the
            // color data manually rather than using the lookup table of
            // the mapper
            if (info.coloringHasNulls)
            {
                vtkLookupTable lookupTable = (vtkLookupTable)smallBodyMapper.GetLookupTable();
                mapScalarsThroughLookupTable(info.coloringValues, lookupTable, colorData);
                array = colorData;
            }

            if (!smallBodyActors.contains(scalarBarActor))
                smallBodyActors.add(scalarBarActor);

            scalarBarActor.SetLookupTable(smallBodyMapper.GetLookupTable());
        }

        if (coloringValueType == ColoringValueType.POINT_DATA)
            this.smallBodyPolyData.GetPointData().SetScalars(array);
        else
            this.smallBodyPolyData.GetCellData().SetScalars(array);


        if (coloringIndex < 0 && useFalseColoring == false)
        {
            smallBodyMapper.ScalarVisibilityOff();
            smallBodyMapper.SetScalarModeToDefault();
        }
        else
        {
            smallBodyMapper.ScalarVisibilityOn();
            if (coloringValueType == ColoringValueType.POINT_DATA)
                smallBodyMapper.SetScalarModeToUsePointData();
            else
                smallBodyMapper.SetScalarModeToUseCellData();
        }


        this.smallBodyPolyData.Modified();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    private void mapScalarsThroughLookupTable(vtkDataArray scalarData,
            vtkLookupTable lookupTable,
            vtkUnsignedCharArray colorArray)
    {
        double nullValue = scalarData.GetRange()[0];
        int numberValues = scalarData.GetNumberOfTuples();
        colorArray.SetNumberOfComponents(3);
        colorArray.SetNumberOfTuples(numberValues);
        double[] rgb = new double[3];
        for (int i=0; i<numberValues; ++i)
        {
            double v = scalarData.GetTuple1(i);
            if (v != nullValue)
            {
                lookupTable.GetColor(v, rgb);
                colorArray.SetTuple3(i, 255.0*rgb[0], 255.0*rgb[1], 255.0*rgb[2]);
            }
            else
            {
                // Map null values to white
                colorArray.SetTuple3(i, 255.0, 255.0, 255.0);
            }
        }
    }

    public void setOpacity(double opacity)
    {
        vtkProperty smallBodyProperty = smallBodyActor.GetProperty();
        smallBodyProperty.SetOpacity(opacity);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setSpecularCoefficient(double value)
    {
        smallBodyActor.GetProperty().SetSpecular(value);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setSpecularPower(double value)
    {
        smallBodyActor.GetProperty().SetSpecularPower(value);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setRepresentationToSurface()
    {
        smallBodyActor.GetProperty().SetRepresentationToSurface();
        smallBodyActor.GetProperty().EdgeVisibilityOff();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setRepresentationToWireframe()
    {
        smallBodyActor.GetProperty().SetRepresentationToWireframe();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setRepresentationToPoints()
    {
        smallBodyActor.GetProperty().SetRepresentationToPoints();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setRepresentationToSurfaceWithEdges()
    {
        smallBodyActor.GetProperty().SetRepresentationToSurface();
        smallBodyActor.GetProperty().EdgeVisibilityOn();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setPointSize(double value)
    {
        smallBodyActor.GetProperty().SetPointSize(value);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setLineWidth(double value)
    {
        smallBodyActor.GetProperty().SetLineWidth(value);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setCullFrontface(boolean enable)
    {
        smallBodyActor.GetProperty().SetFrontfaceCulling(enable ? 1 : 0);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }


    public void delete()
    {
        if (smallBodyPolyData != null) smallBodyPolyData.Delete();
        if (lowResSmallBodyPolyData != null) lowResSmallBodyPolyData.Delete();
        if (smallBodyActor != null) smallBodyActor.Delete();
        if (smallBodyMapper != null) smallBodyMapper.Delete();
        for (vtkProp prop : smallBodyActors)
            if (prop != null) prop.Delete();
        if (cellLocator != null) cellLocator.Delete();
        if (pointLocator != null) pointLocator.Delete();
        if (lowResPointLocator != null) lowResPointLocator.Delete();
        if (scalarBarActor != null) scalarBarActor.Delete();
        if (genericCell != null) genericCell.Delete();
    }

    private void setupScaleBar()
    {
        scaleBarPolydata = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray polys = new vtkCellArray();
        scaleBarPolydata.SetPoints(points);
        scaleBarPolydata.SetPolys(polys);

        points.SetNumberOfPoints(4);

        vtkIdList idList = new vtkIdList();
        idList.SetNumberOfIds(4);
        for (int i=0; i<4; ++i)
            idList.SetId(i, i);
        polys.InsertNextCell(idList);

        scaleBarMapper = new vtkPolyDataMapper2D();
        scaleBarMapper.SetInputData(scaleBarPolydata);

        scaleBarActor = new vtkActor2D();
        scaleBarActor.SetMapper(scaleBarMapper);

        scaleBarTextActor = new vtkTextActor();

        smallBodyActors.add(scaleBarActor);
        smallBodyActors.add(scaleBarTextActor);

        scaleBarActor.GetProperty().SetColor(1.0, 1.0, 1.0);
        scaleBarActor.GetProperty().SetOpacity(0.5);
        scaleBarTextActor.GetTextProperty().SetColor(0.0, 0.0, 0.0);
        scaleBarTextActor.GetTextProperty().SetJustificationToCentered();
        scaleBarTextActor.GetTextProperty().BoldOn();

        scaleBarActor.VisibilityOff();
        scaleBarTextActor.VisibilityOff();

        showScaleBar = Preferences.getInstance().getAsBoolean(Preferences.SHOW_SCALE_BAR, true);
    }

    public void updateScaleBarPosition(int windowWidth, int windowHeight)
    {
        vtkPoints points = scaleBarPolydata.GetPoints();

        int newScaleBarWidthInPixels = (int)Math.min(0.75*windowWidth, 150.0);

        scaleBarWidthInPixels = newScaleBarWidthInPixels;
        int scaleBarHeight = scaleBarWidthInPixels/9;
        int buffer = scaleBarWidthInPixels/20;
        int x = windowWidth - scaleBarWidthInPixels - buffer; // lower left corner x
        int y = buffer; // lower left corner y

        points.SetPoint(0, x, y, 0.0);
        points.SetPoint(1, x+scaleBarWidthInPixels, y, 0.0);
        points.SetPoint(2, x+scaleBarWidthInPixels, y+scaleBarHeight, 0.0);
        points.SetPoint(3, x, y+scaleBarHeight, 0.0);

        scaleBarTextActor.SetPosition(x+scaleBarWidthInPixels/2, y+2);
        scaleBarTextActor.GetTextProperty().SetFontSize(scaleBarHeight-4);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void updateScaleBarValue(double pixelSizeInKm)
    {
        if (scaleBarWidthInPixels <= 0 ||
            scaleBarWidthInKm == scaleBarWidthInPixels * pixelSizeInKm)
        {
            return;
        }

        scaleBarWidthInKm = scaleBarWidthInPixels * pixelSizeInKm;

        if (pixelSizeInKm > 0.0 && showScaleBar)
        {
            scaleBarActor.VisibilityOn();
            scaleBarTextActor.VisibilityOn();
        }
        else
        {
            scaleBarActor.VisibilityOff();
            scaleBarTextActor.VisibilityOff();
        }

        if (pixelSizeInKm > 0.0)
        {
            if (scaleBarWidthInKm < 1.0)
                scaleBarTextActor.SetInput(String.format("%.2f m", 1000.0*scaleBarWidthInKm));
            else
                scaleBarTextActor.SetInput(String.format("%.2f km", scaleBarWidthInKm));
        }

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setShowScaleBar(boolean enabled)
    {
        this.showScaleBar = enabled;
        // The following forces the scale bar to be redrawn.
        scaleBarWidthInKm = -1.0;
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        // Note that we call firePropertyChange *twice*. Not really sure why.
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public boolean getShowScaleBar()
    {
        return showScaleBar;
    }

    public void saveAsPLT(File file) throws IOException
    {
        PolyDataUtil.saveShapeModelAsPLT(smallBodyPolyData, file.getAbsolutePath());
    }

    public void saveAsOBJ(File file) throws IOException
    {
        PolyDataUtil.saveShapeModelAsOBJ(smallBodyPolyData, file.getAbsolutePath());
    }

    public void saveAsVTK(File file) throws IOException
    {
        PolyDataUtil.saveShapeModelAsVTK(smallBodyPolyData, file.getAbsolutePath());
    }

    public void saveAsSTL(File file) throws IOException
    {
//        PolyDataUtil.saveShapeModelAsSTL(smallBodyPolyData, file.getAbsolutePath());
    }


    /**
     * Return if this model is an ellipsoid. If so, some operations on ellipsoids
     * are much easier than general shape models. By default return false, unless
     * a subclasses overrides it.
     * @return
     */
    public boolean isEllipsoid()
    {
        return false;
    }

    /**
     * Return the index of the elevation coloring. Return -1 if no elevation
     * is available.
     *
     * @return
     */
    public int getElevationDataColoringIndex()
    {
        int numberOfColoringTypes = getNumberOfColors();
        for (int i = 0;i<numberOfColoringTypes; ++i)
        {
            String name = getColoringName(i);
            if (GenericPolyhedralModelOrig.ElevStr.toLowerCase().equals(name.toLowerCase()))
            {
                return i;
            }
        }

        return -1;
    }

    /**
     * Subclass should override this if it needs it. Currently only
     * shape models with lidar data need this.
     * Return density of shape model in g/cm^3.
     * @return
     */
    public double getDensity()
    {
        return 0.0;
    }

    /**
     * Subclass should override this if it needs it. Currently only
     * shape models with lidar data need this.
     * Return rotation rate in radians/sec.
     * @return
     */
    public double getRotationRate()
    {
        return 0.0;
    }

    /**
     * Subclass should override this if it needs it. Currently only
     * shape models with lidar data need this.
     * Return reference potential in m^2/sec^2. The reference potential
     * is defined as SUM(P_p*A_p)/SUM(A_p), where P_p is the potential at the
     * center of plate p, A_p is the area of plate p, and the sum is over
     * all plates in the shape model.
     * @return
     */
    public double getReferencePotential()
    {
        return 0.0;
    }

    /**
     * Subclass should override this if it needs it. Currently only
     * shape models with lidar data need this.
     * Return path on server to shape model in PLT format. Needed because
     * gravity program only supports PLT format, not VTK format.
     * @return
     */
    public String getServerPathToShapeModelFileInPlateFormat()
    {
        return null;
    }

    @Override
    public boolean supports2DMode()
    {
        return true;
    }

    @Override
    public void set2DMode(boolean enable)
    {
        if (enable)
        {
            smallBodyMapper.SetInputData(null);
            smallBodyMapper.SetInputConnection(projectTo2D(smallBodyPolyData));
        }
        else
        {
            smallBodyMapper.SetInputData(smallBodyPolyData);
        }

        smallBodyMapper.Update();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    /**
     * Saves out file with information about each plate of shape model that contains these columns.
     * Each row contains information about a single plate in the order the plates define the model.
     *
     * 1. Surface area of plate
     * 2. X coordinate of center of plate
     * 3. Y coordinate of center of plate
     * 4. Z coordinate of center of plate
     * 5. Latitude of center of plate (in degrees)
     * 6. Longitude of center of plate (in degrees)
     * 7. Distance of center of plate to origin
     * 8+. coloring data of plate, both built-in and custom
     *
     * @param polydata
     * @param file
     * @throws IOException
     */
    public void savePlateData(File file) throws IOException
    {
        loadColoringData();

        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);

        final String lineSeparator = System.getProperty("line.separator");

        out.write("Area (km^2)");
        out.write(",Center X (km)");
        out.write(",Center Y (km)");
        out.write(",Center Z (km)");
        out.write(",Center Latitude (deg)");
        out.write(",Center Longitude (deg)");
        out.write(",Center Radius (km)");
        int numColors = getNumberOfColors();
        for (int i=0; i<numColors; ++i)
        {
            out.write("," + getColoringName(i));
            String units = getColoringUnits(i);
            if (units != null && !units.isEmpty())
                out.write(" (" + units + ")");
        }
        out.write(lineSeparator);

        vtkTriangle triangle = new vtkTriangle();

        vtkPoints points = smallBodyPolyData.GetPoints();
        int numberCells = smallBodyPolyData.GetNumberOfCells();
        smallBodyPolyData.BuildCells();
        vtkIdList idList = new vtkIdList();
        double[] pt0 = new double[3];
        double[] pt1 = new double[3];
        double[] pt2 = new double[3];
        double[] center = new double[3];
        for (int i=0; i<numberCells; ++i)
        {
            smallBodyPolyData.GetCellPoints(i, idList);
            int id0 = idList.GetId(0);
            int id1 = idList.GetId(1);
            int id2 = idList.GetId(2);
            points.GetPoint(id0, pt0);
            points.GetPoint(id1, pt1);
            points.GetPoint(id2, pt2);

            double area = triangle.TriangleArea(pt0, pt1, pt2);
            triangle.TriangleCenter(pt0, pt1, pt2, center);
            LatLon llr = MathUtil.reclat(center);

            out.write(area + ",");
            out.write(center[0] + ",");
            out.write(center[1] + ",");
            out.write(center[2] + ",");
            out.write((llr.lat*180.0/Math.PI) + ",");
            out.write((llr.lon*180.0/Math.PI) + ",");
            out.write(String.valueOf(llr.rad));

            for (int j=0; j<numColors; ++j)
                out.write("," + coloringInfo.get(j).coloringValues.GetTuple1(i));

            out.write(lineSeparator);
        }

        triangle.Delete();
        idList.Delete();
        out.close();
    }

    /**
     * Given a polydata that is coincident with part of the shape model, save out the
     * plate data for all cells of the shape model that touch the polydata (even a
     * little bit).
     *
     * @param polydata
     * @param file
     * @throws IOException
     */
    public void savePlateDataInsidePolydata(vtkPolyData polydata, File file) throws IOException
    {
        // Go through every cell inside the polydata and find the closest cell to it
        // in the shape model and get the plate data for that cell.
        // First put the cells into an ordered set, so we don't save out the
        // same cell twice.

        TreeSet<Integer> cellIds = new TreeSet<Integer>();

        int numCells = polydata.GetNumberOfCells();

        double[] pt0 = new double[3];
        double[] pt1 = new double[3];
        double[] pt2 = new double[3];
        double[] center = new double[3];
        double[] closestPoint = new double[3];

        for (int i=0; i<numCells; ++i)
        {
            vtkTriangle cell = (vtkTriangle) polydata.GetCell(i);
            vtkPoints points = cell.GetPoints();
            points.GetPoint(0, pt0);
            points.GetPoint(1, pt1);
            points.GetPoint(2, pt2);
            cell.TriangleCenter(pt0, pt1, pt2, center);

            int cellId = findClosestCell(center, closestPoint);

            cellIds.add(cellId);

            points.Delete();
            cell.Delete();
        }

        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);
        String nl = System.getProperty("line.separator");

        out.write("Plate Id\tLatitude\tLongitude\t");
        int numColoringData = getNumberOfColors();
        for (int j=0; j<numColoringData; ++j)
        {
            out.write(getColoringName(j));
            if (j < numColoringData-1)
                out.write("\t");
        }
        out.write(nl);

        numCells = cellIds.size();
        for (int cellId : cellIds)
        {
            vtkTriangle cell = (vtkTriangle)smallBodyPolyData.GetCell(cellId);
            vtkPoints points = cell.GetPoints();
            points.GetPoint(0, pt0);
            points.GetPoint(1, pt1);
            points.GetPoint(2, pt2);
            cell.TriangleCenter(pt0, pt1, pt2, center);

            LatLon llr = MathUtil.reclat(center);
            double lat = llr.lat*180.0/Math.PI;
            double lon = llr.lon*180.0/Math.PI;
            if (lon < 0.0)
                lon += 360.0;

            String str = cellId + "\t" + lat + "\t" + lon + "\t";

            double[] values = getAllColoringValues(center);
            for (int j=0; j<values.length; ++j)
            {
                str += values[j];
                if (j < values.length-1)
                    str += "\t";
            }

            str += nl;

            out.write(str);

            points.Delete();
            cell.Delete();
        }

        out.close();
    }

    public void addCustomPlateData(ColoringInfo info) throws IOException
    {
        info.builtIn = false;
        info.resolutionLevel = resolutionLevel;
        info.coloringValues = null;
        info.defaultColoringRange = null;
        coloringInfo.add(info);

        if (coloringIndex >= 0)
            loadColoringData();
    }

    public void setCustomPlateData(int index, ColoringInfo info) throws IOException
    {
        if (coloringInfo.get(index).builtIn)
            return;

        info.builtIn = false;
        info.coloringValues = null;
        info.defaultColoringRange = null;

        coloringInfo.set(index, info);

        if (coloringIndex >= 0)
        {
            loadColoringData();
            paintBody();
        }
    }

    public void removeCustomPlateData(int index) throws IOException
    {
        if (coloringInfo.get(index).builtIn)
            return;

        boolean needToRepaint = coloringIndex >= 0 || useFalseColoring;

        coloringInfo.remove(index);

        if (useFalseColoring)
        {
            if (redFalseColor == index)
                redFalseColor = -1;
            else if (redFalseColor > index)
                --redFalseColor;
            if (greenFalseColor == index)
                greenFalseColor = -1;
            else if (greenFalseColor > index)
                --greenFalseColor;
            if (blueFalseColor == index)
                blueFalseColor = -1;
            else if (blueFalseColor > index)
                --blueFalseColor;

            if (redFalseColor < 0 || greenFalseColor < 0 || blueFalseColor < 0)
                useFalseColoring = false;
        }
        else
        {
            if (coloringIndex == index)
                coloringIndex = -1;
            else if (coloringIndex > index)
                --coloringIndex;
        }

        if (needToRepaint)
            paintBody();
    }

    public void reloadColoringData() throws IOException
    {
        for (ColoringInfo info : coloringInfo)
        {
            info.coloringValues = null;
            info.defaultColoringRange = null;
        }

        loadColoringData();
    }

    public ArrayList<ColoringInfo> getColoringInfoList()
    {
        return coloringInfo;
    }

    @Override
    public ArrayList<LidarDatasourceInfo> getLidarDasourceInfoList()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addCustomLidarDatasource(LidarDatasourceInfo info)
            throws IOException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setCustomLidarDatasource(int index, LidarDatasourceInfo info)
            throws IOException
    {
        // TODO Auto-generated method stub

    }



//    @Override
//    public PolyhedralModelConfig getSmallBodyConfig()
//    {
//        // TODO Auto-generated method stub
//        return null;
//    }
}

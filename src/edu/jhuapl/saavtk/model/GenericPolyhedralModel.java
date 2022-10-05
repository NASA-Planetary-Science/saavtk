package edu.jhuapl.saavtk.model;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import crucible.crust.metadata.impl.gson.Serializers;
import edu.jhuapl.saavtk.color.table.ColorMapAttr;
import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.model.plateColoring.BasicColoringDataManager;
import edu.jhuapl.saavtk.model.plateColoring.ColoringData;
import edu.jhuapl.saavtk.model.plateColoring.CustomizableColoringDataManager;
import edu.jhuapl.saavtk.model.plateColoring.FacetColoringData;
import edu.jhuapl.saavtk.model.plateColoring.ColoringDataFactory;
import edu.jhuapl.saavtk.model.plateColoring.ColoringDataUtils;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.ConvertResourceToFile;
import edu.jhuapl.saavtk.util.Debug;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.util.SafeURLPaths;
import edu.jhuapl.saavtk.util.SmallBodyCubes;
import edu.jhuapl.saavtk.util.file.IndexableTuple;
import edu.jhuapl.saavtk.view.lod.LodMode;
import edu.jhuapl.saavtk.view.lod.LodUtil;
import edu.jhuapl.saavtk.view.lod.VtkLodActor;
import edu.jhuapl.saavtk.vtk.VtkDrawUtil;
import vtk.vtkActor;
import vtk.vtkCamera;
import vtk.vtkCell;
import vtk.vtkCellData;
import vtk.vtkCellDataToPointData;
import vtk.vtkContourFilter;
import vtk.vtkCoordinate;
import vtk.vtkCubeSource;
import vtk.vtkDataArray;
import vtk.vtkDepthSortPolyData;
import vtk.vtkFloatArray;
import vtk.vtkGenericCell;
import vtk.vtkIdList;
import vtk.vtkIdTypeArray;
import vtk.vtkLookupTable;
import vtk.vtkMassProperties;
import vtk.vtkPointLocator;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataNormals;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkScalarBarActor;
import vtk.vtkTextProperty;
import vtk.vtkTriangle;
import vtk.vtkUnsignedCharArray;
import vtk.vtksbCellLocator;

public class GenericPolyhedralModel extends PolyhedralModel
{
	//This is a placeholder for enabling a series of diagnostic tools we hope to bring into the renderer.  Currently in place but with no UI hooks to enable it (yet) is a
	//block of code that can display the body cubes used during a database search that allows you to see what exactly it is you're choosing.
	private boolean diagnosticModeEnabled = false;
	private List<vtkProp> diagnosticCubes = new ArrayList<>();

    private static final SafeURLPaths SAFE_URL_PATHS = SafeURLPaths.instance();

    private final CustomizableColoringDataManager coloringDataManager;

    private ColoringValueType coloringValueType;

    public ColoringValueType getColoringValueType()
    {
        return coloringValueType;
    }

    private int coloringIndex = -1;
    // If true, a false color will be used by using 3 of the existing
    // colors for the red, green, and blue channels
    private boolean useFalseColoring = false;
    private int redFalseColor = -1; // red channel for false coloring
    private int greenFalseColor = -1; // green channel for false coloring
    private int blueFalseColor = -1; // blue channel for false coloring
    private vtkUnsignedCharArray falseColorArray;

    private List<LidarDataSource> lidarDataSourceL = new ArrayList<>();

    private vtkPolyData smallBodyPolyData;
    private vtkPolyData lowResSmallBodyPolyData;
	private VtkLodActor smallBodyActor;
    private vtkPolyDataMapper smallBodyMapper;

    private List<vtkProp> smallBodyActors = new ArrayList<>();

    public List<vtkProp> getSmallBodyActors()
    {
        return smallBodyActors;
    }

    private vtksbCellLocator cellLocator;
    private vtkPointLocator pointLocator;
    private vtkPointLocator lowResPointLocator;
    private vtkScalarBarActor scalarBarActor;
    private SmallBodyCubes smallBodyCubes;
    private File defaultModelFile;
    private int resolutionLevel = 0;
    private vtkGenericCell genericCell;
    private String[] modelNames;
    private String[] modelFiles;

    private BoundingBox boundingBox = null;
    private vtkIdList idList; // to avoid repeated allocations
    private vtkFloatArray gravityVector;

    private vtkFloatArray cellNormals;
    private double surfaceArea = -1.0;
    private double volume = -1.0;
    private double minCellArea = -1.0;
    private double maxCellArea = -1.0;
    private double meanCellArea = -1.0;

    vtkIdTypeArray cellIds = new vtkIdTypeArray();
    public static final String cellIdsArrayName = "cellIds";

    private Colormap colormap = null;
    private boolean colormapInitialized = false;
    private boolean showColorsAsContourLines = false;
    private double contourLineWidth = 1;
    private vtkPolyDataMapper linesMapper;
	private VtkLodActor linesActor;

	private double cubeSize;
	private double cubeOverlapCheck;

    // Heuristic to avoid computationally expensive paint operations when possible.
    private Map<String, Object> paintingAttributes = null;

    /**
     * Default constructor. Must be followed by a call to setSmallBodyPolyData.
     */
    public GenericPolyhedralModel(String uniqueModelId)
    {
        super(null);
        coloringDataManager = CustomizableColoringDataManager.of(uniqueModelId);
        smallBodyPolyData = new vtkPolyData();
        genericCell = new vtkGenericCell();
        idList = new vtkIdList();
    }

    /**
     * Convenience method for initializing a GenericPolyhedralModel with just a
     * vtkPolyData.
     *
     * @param polyData
     */
    public GenericPolyhedralModel(String uniqueModelId, vtkPolyData polyData)
    {
        this(uniqueModelId);
        vtkFloatArray[] coloringValues = {};
        String[] coloringNames = {};
        String[] coloringUnits = {};
        ColoringValueType coloringValueType = ColoringValueType.CELLDATA;
        modelNames = new String[] {uniqueModelId};
        setSmallBodyPolyData(polyData, coloringValues, coloringNames, coloringUnits, coloringValueType);
    }

    /**
     * Note that name is used to name this small body model as a whole including all
     * resolution levels whereas modelNames is an array of names that is specific
     * for each resolution level.
     */
	public GenericPolyhedralModel(ViewConfig config, String[] modelNames, String[] modelFiles, String[] coloringFiles,
			String[] coloringNames, String[] coloringUnits, boolean[] coloringHasNulls,
			ColoringValueType coloringValueType, boolean lowestResolutionModelStoredInResource)
    {
        super(config);
        this.coloringDataManager = CustomizableColoringDataManager.of(config.getUniqueName());
		initializeColoringDataManager(coloringDataManager, config.getResolutionNumberElements(), coloringFiles,
				coloringNames, coloringUnits, coloringHasNulls);
        this.modelNames = modelNames;
        this.modelFiles = modelFiles;
        setDefaultModelFileName(this.modelFiles[0]);
        this.coloringValueType = coloringValueType;

        new vtkUnsignedCharArray();
        smallBodyPolyData = new vtkPolyData();
        genericCell = new vtkGenericCell();
        idList = new vtkIdList();

        if (Configuration.useFileCache())
        {
            if (lowestResolutionModelStoredInResource)
				defaultModelFile = ConvertResourceToFile.convertResourceToRealFile(this.getClass(), modelFiles[0],
						Configuration.getApplicationDataDir());
            else
                defaultModelFile = FileCache.getFileFromServer(modelFiles[0]);
        }
        else
        {
            defaultModelFile = new File(modelFiles[0]);
        }

        if (!defaultModelFile.exists())
        {
            throw new RuntimeException("Shape model file not found: " + defaultModelFile.getPath());
        }

        initialize(defaultModelFile);

        this.coloringDataManager.addPropertyChangeListener(event -> {
            if (BasicColoringDataManager.COLORING_DATA_CHANGE.equals(event.getPropertyName()))
            {
                try
                {
                    coloringDataManager.saveCustomMetadata(getCustomDataFolder());
                }
                catch (Exception e)
                {
                    // This should not fail, but if it does it should not disrupt what the user is
                    // doing.
                    // Thus in this case it is appropriate to log the problem and then continue.
                    e.printStackTrace();
                }

            }
        });

    }

    private static final String COLORING_METADATA_ID = "Coloring Metadata";

	private static void initializeColoringDataManager(CustomizableColoringDataManager coloringDataManager,
			ImmutableList<Integer> numberElements, String[] coloringFiles, String[] coloringNames, String[] coloringUnits,
			boolean[] coloringHasNulls)
    {
        Preconditions.checkNotNull(coloringDataManager);
        if (coloringNames == null)
            return;
        Preconditions.checkNotNull(coloringFiles);
        Preconditions.checkArgument(coloringFiles.length > 0);
        Preconditions.checkArgument(coloringFiles.length == coloringNames.length);

        Preconditions.checkNotNull(numberElements);
        Preconditions.checkArgument(numberElements.size() > 0);

        // Don't trust the other inputs, but don't throw, just make them blank.
        if (coloringUnits == null)
            coloringUnits = new String[] {};
        if (coloringHasNulls == null)
            coloringHasNulls = new boolean[] {};

        String metadataFileName = BasicColoringDataManager.getMetadataFileName(Serializers.of().getVersion());
		metadataFileName = SAFE_URL_PATHS.getString(SAFE_URL_PATHS.get(coloringFiles[0]).toFile().getParent(),
				metadataFileName);
        try
        {
            File metadataFile = FileCache.getFileFromServer(metadataFileName);
            if (metadataFile.exists())
            {
                try
                {
                    BasicColoringDataManager builtInColoring = BasicColoringDataManager.of(coloringDataManager.getId());
                    Serializers.deserialize(metadataFile, COLORING_METADATA_ID, builtInColoring.getMetadataManager());
                    for (String builtInName : builtInColoring.getNames())
                    {
                        for (int builtInNumberElements : builtInColoring.getResolutions())
                        {
                            if (builtInColoring.has(builtInName, builtInNumberElements))
                            {
                                coloringDataManager.addBuiltIn(builtInColoring.get(builtInName, builtInNumberElements));
                            }
                        }
                    }
                }
                catch (IOException e)
                {
                    System.err.println("Exception when trying to load plate coloring metadata file");
                    e.printStackTrace();
                }
                return;
            }
        }
        catch (Exception e)
        {
			// Ignore this exception -- this file is optional. Fall through to continue with
			// legacy behavior.
        }

        Exception firstException = null;

        try
        {
            boolean sbmt2style = false;
            for (int index = 0; index < coloringFiles.length; ++index)
            {
                String baseFileName = coloringFiles[index];
                Format fileFormat = guessFormat(baseFileName);
                for (int resolutionLevel = 0; resolutionLevel < numberElements.size(); ++resolutionLevel)
                {
                    try
                    {
                        String fileName = getColoringFileName(baseFileName, resolutionLevel, fileFormat, sbmt2style);
                        if (fileName != null)
                        {
                            fileFormat = guessFormat(fileName);
                            sbmt2style = guessSbmt2Style(fileName);
                            String name = coloringNames[index];
                            ImmutableList<String> elementNames = ImmutableList.of(name);
                            String units = coloringUnits.length > index ? coloringUnits[index] : "";
                            boolean hasNulls = coloringHasNulls.length > index ? coloringHasNulls[index] : false;
                            coloringDataManager.addBuiltIn(ColoringDataFactory.of(name, units, numberElements.get(resolutionLevel), elementNames, hasNulls, fileName));
                        }                        
                    }
                    catch (Exception e)
                    {
                        if (firstException == null)
                        {
                            firstException = e;
                        }
                        e.printStackTrace(Debug.of().err());
                    }
                }
            }
            if (firstException != null)
            {
                throw new RuntimeException("One or more colorings failed to load. First exception was ", firstException);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private static Format guessFormat(String baseFileName)
    {
		Format fileFormat = baseFileName.matches(".*\\.[Ff][Ii][Tt][Ss]?$") ? Format.FIT
				: baseFileName.matches(".*//.[Tt][Xx][Tt]$") ? Format.TXT : Format.UNKNOWN;
        if (fileFormat == Format.UNKNOWN)
        {
			fileFormat = baseFileName.matches(".*\\.[Ff][Ii][Tt][Ss]?\\.[Gg][Zz]$") ? Format.FIT
					: baseFileName.matches(".*//.[Tt][Xx][Tt]\\.[Gg][Zz]$") ? Format.TXT : Format.UNKNOWN;
        }

        if (fileFormat == Format.UNKNOWN)
        {
            fileFormat = Format.TXT;
        }

        return fileFormat;
    }

    private static boolean guessSbmt2Style(String fileName)
    {
        return !fileName.contains("_res");
    }

    // Note this change has been merged back into sbmt1dev, but not
    // all SBMT2 changes were initially.
    // SBMT 2 constructor
    public GenericPolyhedralModel(ViewConfig config)
    {
        super(config);
        this.coloringDataManager = CustomizableColoringDataManager.of(config.getUniqueName());
    }

	protected void initializeConfigParameters(String[] modelFiles, String[] coloringFiles, String[] coloringNames,
			String[] coloringUnits, boolean[] coloringHasNulls, ColoringValueType coloringValueType,
			boolean lowestResolutionModelStoredInResource)
    {
        this.modelFiles = modelFiles;
        setDefaultModelFileName(this.modelFiles[0]);
        this.coloringValueType = coloringValueType;
		initializeColoringDataManager(coloringDataManager, getConfig().getResolutionNumberElements(), coloringFiles,
				coloringNames, coloringUnits, coloringHasNulls);

        new vtkUnsignedCharArray();
        smallBodyPolyData = new vtkPolyData();
        genericCell = new vtkGenericCell();
        idList = new vtkIdList();

        if (Configuration.useFileCache())
        {
            if (lowestResolutionModelStoredInResource)
				defaultModelFile = ConvertResourceToFile.convertResourceToRealFile(this.getClass(), modelFiles[0],
						Configuration.getApplicationDataDir());
            else
                defaultModelFile = FileCache.getFileFromServer(modelFiles[0]);
        }
        else
        {
            defaultModelFile = new File(modelFiles[0]);
        }

        if (!defaultModelFile.exists())
        {
            throw new RuntimeException("Shape model file not found: " + defaultModelFile.getPath());
        }
        initialize(defaultModelFile);
    }

	@Override
	public ColorMapAttr getColorMapAttr()
	{
		return colormap.getColorMapAttr();
	}

	@Override
	public void setColorMapAttr(ColorMapAttr aCMA)
	{
		// Bail if nothing has changed
		ColorMapAttr tmpCMA = ColorMapAttr.Invalid;
		if (colormap != null)
			tmpCMA = colormap.getColorMapAttr();
		if (tmpCMA.equals(aCMA) == true)
			return;

		// Synthesize the new colormap
		colormap = Colormaps.getNewInstanceOfBuiltInColormap(aCMA.getColorTable().getName());
		colormap.setLogScale(aCMA.getIsLogScale());
		colormap.setNumberOfLevels(aCMA.getNumLevels());
		colormap.setRangeMin(aCMA.getMinVal());
		colormap.setRangeMax(aCMA.getMaxVal());

		try
		{
			paintBody();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    protected void initColormap()
    {
        if (colormap == null)
            colormap = Colormaps.getNewInstanceOfBuiltInColormap(Colormaps.getDefaultColormapName());
        if (!colormapInitialized && !(colormap == null) && coloringIndex > -1)
        {
            double[] range = getCurrentColoringRange(coloringIndex);
            colormap.setRangeMin(range[0]);
            colormap.setRangeMax(range[1]);
            colormapInitialized = true;
        }
    }

    @Override
    public void showScalarsAsContours(boolean flag)
    {
        showColorsAsContourLines = flag;
        try
        {
            paintBody();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void setContourLineWidth(double width)
    {
        contourLineWidth = width;
    }

    @Override
    public List<vtkPolyData> getSmallBodyPolyDatas()
    {
        return null;
    }

    public void setDefaultModelFileName(String defaultModelFileName)
    {
        defaultModelFile = new File(defaultModelFileName);
    }
    
    public void setSmallBodyPolyData(vtkPolyData polydata)
    {
    	if (polydata != null)
        {
            smallBodyPolyData.DeepCopy(polydata);
            initializeLocators();
            initializeCellIds();
            getCellNormals();
            lowResSmallBodyPolyData = smallBodyPolyData;
            lowResPointLocator = pointLocator;
        }
    }

	public void setSmallBodyPolyData(vtkPolyData polydata, vtkFloatArray[] coloringValues, String[] coloringNames,
			String[] coloringUnits, ColoringValueType coloringValueType)
    {
        if (polydata != null)
        {
            smallBodyPolyData.DeepCopy(polydata);
        }
        for (int i = 0; i < coloringNames.length; ++i)
        {
            int numberElements = coloringValues[i].GetNumberOfTuples();
            ImmutableList<String> elementNames = ImmutableList.of(coloringNames[i]);

            coloringDataManager.addBuiltIn(ColoringDataFactory.of(coloringNames[i], coloringUnits[i], numberElements, elementNames, false, coloringValues[i]));
        }
        this.coloringValueType = coloringValueType;

        initializeLocators();
        initializeCellIds();
        getCellNormals();
        lowResSmallBodyPolyData = smallBodyPolyData;
        lowResPointLocator = pointLocator;
    }

    private void initializeCellIds()
    {
        cellIds = new vtkIdTypeArray();
        cellIds.SetName(cellIdsArrayName);
        for (int i = 0; i < smallBodyPolyData.GetNumberOfCells(); i++)
            cellIds.InsertNextValue(i);
        smallBodyPolyData.GetCellData().AddArray(cellIds);
    }

    public ViewConfig getDefaultModelConfig()
    {
        return getConfig();
    }

    @Override
    public boolean isBuiltIn()
    {
        return true;
    }

    private void convertOldConfigFormatToNewVersion(MapUtil configMap)
    {
        // In the old format of the config file, the platefiles were assumed
        // to be named platedata0.txt, platedata1.txt, etc. The CELL_DATA_PATHS key
        // only stored the original path to the file which the user specified. Now
        // in the new way, we no longer record in the config file the original path
        // but instead store the actual filename (not fullpath) of the plate data as
        // copied over to the custom_data subfolder (within the .neartool folder).
        // These filenames are now stored in the CELL_DATA_FILENAMES key. Therefore,
        // in this function we delete the CELL_DATA_PATHS key and create a new
        // CELL_DATA_FILENAMES key.
        if (configMap.containsKey(GenericPolyhedralModel.CELL_DATA_PATHS))
        {
            String[] cellDataPaths = configMap.get(GenericPolyhedralModel.CELL_DATA_PATHS).split(",", -1);
            String cellDataFilenames = "";

            for (int i = 0; i < cellDataPaths.length; ++i)
            {
                cellDataFilenames += "platedata" + i + ".txt";
                if (i < cellDataPaths.length - 1)
                    cellDataFilenames += GenericPolyhedralModel.LIST_SEPARATOR;
            }

            configMap.put(GenericPolyhedralModel.CELL_DATA_FILENAMES, cellDataFilenames);
            configMap.remove(GenericPolyhedralModel.CELL_DATA_PATHS);
        }
    }

    @Override
    public String getCustomDataFolder()
    {
        String imagesDir = null;
        if (isBuiltIn())
        {
            imagesDir = Configuration.getCustomDataFolderForBuiltInViews() + File.separator + getConfig().getUniqueName();
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

    @Override
    public String getCustomDataRootFolder()
    {
        String customDataRootDir = Configuration.getCustomDataFolderForBuiltInViews();

        // If the directory does not exist, create it
        File dir = new File(customDataRootDir);
        if (!dir.exists())
        {
            dir.mkdirs();
        }

        return customDataRootDir;
    }

    @Override
    public String getConfigFilename()
    {
        return getCustomDataFolder() + File.separator + "config.txt";
    }

    @Override
    public String getPlateConfigFilename()
    {
        return getCustomDataFolder() + File.separator + "plateConfig.txt";
    }

    @Override
    public String getDEMConfigFilename()
    {
        return getCustomDataFolder() + File.separator + "demConfig.txt";
    }

    public void loadCustomColoringInfo() throws IOException
    {
        ViewConfig config = getConfig();
        // Get the current selection so we can try to restore it at the end.
        String prevColoringName = null;
        int numberElements = config.getResolutionNumberElements().get(getModelResolution());
        if (coloringIndex >= 0)
        {
            ImmutableList<String> names = coloringDataManager.getNames();
            if (coloringIndex < names.size())
            {
                String name = names.get(coloringIndex);
                if (coloringDataManager.has(name, numberElements))
                {
                    prevColoringName = name;
                }
            }
        }

        coloringDataManager.clearCustom();

        try
        {
            coloringDataManager.loadCustomMetadata(getCustomDataFolder());
        }
        catch (@SuppressWarnings("unused") Exception e)
        {
            // Assume this just means metadata have not been saved before now.
            // Fall through to the old way of loading metadata.
            String configFilename = getPlateConfigFilename();

            if (!(new File(configFilename).exists()))
                return;

            MapUtil configMap = new MapUtil(configFilename);

            convertOldConfigFormatToNewVersion(configMap);

			if (configMap.containsKey(GenericPolyhedralModel.CELL_DATA_FILENAMES)
					&& configMap.containsKey(GenericPolyhedralModel.CELL_DATA_NAMES)
					&& configMap.containsKey(GenericPolyhedralModel.CELL_DATA_UNITS)
                    && configMap.containsKey(GenericPolyhedralModel.CELL_DATA_HAS_NULLS))
            {
                String[] cellDataFilenames = configMap.get(GenericPolyhedralModel.CELL_DATA_FILENAMES).split(",", -1);
                String[] cellDataNames = configMap.get(GenericPolyhedralModel.CELL_DATA_NAMES).split(",", -1);
                String[] cellDataUnits = configMap.get(GenericPolyhedralModel.CELL_DATA_UNITS).split(",", -1);
                String[] cellDataHasNulls = configMap.get(GenericPolyhedralModel.CELL_DATA_HAS_NULLS).split(",", -1);
                String[] cellDataResolutionLevels = null;
                if (configMap.containsKey(GenericPolyhedralModel.CELL_DATA_RESOLUTION_LEVEL))
					cellDataResolutionLevels = configMap.get(GenericPolyhedralModel.CELL_DATA_RESOLUTION_LEVEL).split(",",
							-1);

                for (int i = 0; i < cellDataFilenames.length; ++i)
                {
                    String coloringFile = cellDataFilenames[i];
                    if (!coloringFile.trim().isEmpty())
                    {
                        coloringFile = SAFE_URL_PATHS.getUrl(SAFE_URL_PATHS.getString(getCustomDataFolder(), coloringFile));
                        String coloringName = cellDataNames[i];
                        String coloringUnits = cellDataUnits[i];
                        boolean coloringHasNulls = Boolean.parseBoolean(cellDataHasNulls[i]);
                        int resolutionLevel;
                        if (cellDataResolutionLevels != null)
                        {
                            resolutionLevel = Integer.parseInt(cellDataResolutionLevels[i]);
                        }
                        else
                        {
                            resolutionLevel = 0;
                        }
                        int customNumberElements = config.getResolutionNumberElements().get(resolutionLevel);
                        coloringDataManager.addCustom(ColoringDataFactory.of( //
                                coloringName, //
                                coloringUnits, //
                                customNumberElements, //
                                ImmutableList.of(coloringName), //
                                coloringHasNulls, coloringFile //
                        ));
                    }
                }
            }
        }

        // Now that we're done loading the custom colors, see if it's possible again to
        // select the original selection.
        coloringIndex = -1;
        ImmutableList<String> names = coloringDataManager.getNames();
        for (int index = 0; index < names.size(); ++index)
        {
            if (names.get(index).equals(prevColoringName))
            {
                coloringIndex = index;
                break;
            }
        }
    }

    @Override
    public void loadCustomLidarDataSource()
    {
        lidarDataSourceL = new ArrayList<>();

        String configFilename = getConfigFilename();

        if (!(new File(configFilename).exists()))
            return;

        MapUtil configMap = new MapUtil(configFilename);

        convertOldConfigFormatToNewVersion(configMap);

		if (configMap.containsKey(GenericPolyhedralModel.LIDAR_DATASOURCE_NAMES)
				&& configMap.containsKey(GenericPolyhedralModel.LIDAR_DATASOURCE_NAMES))
        {
            String[] nameArr = configMap.get(GenericPolyhedralModel.LIDAR_DATASOURCE_NAMES).split(",", -1);
            String[] pathArr = configMap.get(GenericPolyhedralModel.LIDAR_DATASOURCE_PATHS).split(",", -1);

            for (int i = 0; i < nameArr.length; ++i)
            {
                LidarDataSource info = new LidarDataSource(nameArr[i], pathArr[i]);
                lidarDataSourceL.add(info);
            }
        }
    }

    private void initialize(File modelFile)
    {
        // Load in custom plate data
        try
        {
            if (!getConfig().customTemporary)
            {
                loadCustomColoringInfo();
                loadCustomLidarDataSource();
            }

            smallBodyPolyData.ShallowCopy(PolyDataUtil.loadShapeModel(modelFile.getAbsolutePath()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        initializeLocators();
        initializeCellIds();
        getCellNormals();
        this.computeShapeModelStatistics();

        // this.computeLargestSmallestEdgeLength();
        // this.computeSurfaceArea();
    }

    private boolean defaultModelInitialized;

    public void initializeDefaultModel()
    {
        if (defaultModelInitialized)
        {
            return;
        }

        // Load in custom plate data
        try
        {
            loadCustomColoringInfo();

            smallBodyPolyData.ShallowCopy(PolyDataUtil.loadShapeModel(defaultModelFile.getAbsolutePath()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        initializeLocators();

        this.computeShapeModelStatistics();

        // this.computeLargestSmallestEdgeLength();
        // this.computeSurfaceArea();

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
        // cellLocator.SetMaxLevel(10);
        // cellLocator.SetNumberOfCellsPerNode(5);
        cellLocator.SetTolerance(1e-15);
        cellLocator.BuildLocator();

        pointLocator.FreeSearchStructure();
        pointLocator.SetDataSet(smallBodyPolyData);
        pointLocator.SetTolerance(1e-15);
        pointLocator.BuildLocator();
    }

    private void initializeLowResData()
    {
        if (lowResPointLocator == null)
        {
            lowResSmallBodyPolyData = new vtkPolyData();

            try
            {
                lowResSmallBodyPolyData.ShallowCopy(PolyDataUtil.loadShapeModel(defaultModelFile.getAbsolutePath()));
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

    @Override
    public vtkPolyData getSmallBodyPolyData()
    {
        return smallBodyPolyData;
    }

    public vtkActor getSmallBodyActor()
    {
        return smallBodyActor;
    }

    public vtkPolyDataMapper getSmallBodyMapper()
    {
        return smallBodyMapper;
    }

    public vtkPolyData getLowResSmallBodyPolyData()
    {
        initializeLowResData();

        return lowResSmallBodyPolyData;
    }

    @Override
    public vtksbCellLocator getCellLocator()
    {
        return cellLocator;
    }

    @Override
    public vtkPointLocator getPointLocator()
    {
        return pointLocator;
    }

    public void calculateCubeSize(boolean useCustomBodyCubeSizeIfAvailable, double overlap)
    {
    	cubeOverlapCheck = overlap;
        if (useCustomBodyCubeSizeIfAvailable && getConfig().hasCustomBodyCubeSize)
        {
            // Custom specified cube size
            cubeSize = getConfig().customBodyCubeSize;
        }
        else
        {
            // Old way of determining cube size, most models still use this
            double diagonalLength = new BoundingBox(getLowResSmallBodyPolyData().GetBounds()).getDiagonalLength();
            // The number 38.66056033363347 is used here so that the cube size
            // comes out to 1 km for Eros (whose diagonaLength equals 38.6605...).
            cubeSize = diagonalLength / 38.66056033363347;
        }
    }

    public void clearCubes()
    {
    	smallBodyCubes = null;
    }

    public SmallBodyCubes getSmallBodyCubes()
    {
        if (smallBodyCubes == null)
        {
        	if (cubeSize == 0.0) calculateCubeSize(false, 0);
            // Generate cubes based on chosen resolution
            smallBodyCubes = new SmallBodyCubes(getLowResSmallBodyPolyData(), cubeSize, 0.01 * cubeSize, true, cubeOverlapCheck);

            //TODO: This needs to be exposed through a developer only UI for diagnosis purposes.
            if (diagnosticModeEnabled == true)
            {
//	            int index = 0;
////	            Integer[] sampleIndices = new Integer[] { 1, 2, 5, 6, 7, 8, 9, 13, 14, 15, 16, 17, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43, 46, 47, 48, 49, 50, 51, 53, 54, 55, 56, 57, 61, 62, 63, 64, 69, 70, 71, 72, 73, 78, 79, 80, 81, 82, 83, 89, 90, 91, 92, 93, 98, 99, 100, 101, 106, 107, 108, 109, 113, 114, 115, 120, 121, 122, 123, 124, 125, 126, 130, 131, 132, 133, 134, 135, 136, 139, 140, 141, 142, 143, 144, 145, 148, 149, 151, 152, 157, 158, 159, 160, 166, 167, 168, 169, 170, 171, 176, 177, 178, 179, 180, 181, 182, 187, 188, 189, 190, 191, 195, 196, 197, 198, 202, 203, 204, 208, 209, 210, 214, 215, 216, 221, 222, 223, 228, 229, 230, 231, 232, 238, 239, 240, 241, 242, 243, 244, 248, 249, 250, 251, 252, 253, 254, 259, 260, 261, 262, 265, 266, 267, 272, 273, 274, 275, 276, 282, 283, 284, 285, 286, 287, 288, 293, 294, 295, 296, 297, 298, 303, 304, 305, 306, 311, 312, 313, 314, 318, 319, 320, 325, 326, 327, 331, 332, 333, 338, 339, 340, 345, 346, 347, 352, 353, 354, 355, 359, 360, 361, 362, 368, 369, 370, 371, 372, 373, 374, 379, 380, 381, 382, 383, 384, 387, 388, 389, 390, 391, 394, 395, 396, 397, 402, 403, 404, 405, 406, 407, 413, 414, 415, 416, 417, 418, 422, 423, 424, 425, 426, 430, 431, 432, 433, 437, 438, 439, 443, 444, 445, 449, 450, 451, 454, 455, 456, 459, 460, 461, 464, 465, 469, 470, 471, 476, 477, 478, 482, 483, 484, 485, 490, 491, 492, 493, 494, 500, 501, 502, 503, 504, 505, 506, 510, 511, 512, 513, 514, 515, 516, 517, 519, 520, 521, 522, 523, 524, 527, 528, 529, 530, 534, 535, 536, 537, 538, 539, 540, 547, 548, 549, 550, 551, 552, 553, 559, 560, 561, 562, 563, 568, 569, 570, 575, 576, 577, 581, 582, 583, 587, 588, 589, 593, 594, 595, 598, 599, 600, 603, 604, 607, 608, 612, 613, 614, 618, 619, 620, 625, 626, 627, 628, 633, 634, 635, 636, 641, 642, 643, 644, 650, 651, 652, 653, 654, 655, 656, 660, 661, 662, 663, 664, 665, 666, 667, 669, 670, 671, 672, 673, 678, 679, 680, 681, 682, 683, 689, 690, 691, 692, 693, 694, 695, 701, 702, 703, 704, 705, 710, 711, 712, 717, 718, 719, 724, 725, 729, 730, 731, 735, 736, 740, 741, 744, 745, 746, 750, 751, 755, 756, 757, 761, 762, 763, 767, 768, 772, 773, 774, 779, 780, 781, 786, 787, 788, 789, 794, 795, 796, 797, 803, 804, 805, 806, 807, 808, 809, 814, 815, 816, 817, 818, 819, 821, 822, 827, 828, 829, 830, 831, 838, 839, 840, 841, 842, 843, 844, 850, 851, 852, 853, 858, 859, 860, 864, 865, 866, 871, 872, 876, 877, 878, 882, 883, 887, 888, 892, 893, 897, 898, 902, 903, 907, 908, 912, 913, 916, 917, 921, 922, 927, 928, 933, 934, 935, 939, 940, 941, 942, 946, 947, 948, 949, 950, 957, 958, 959, 960, 961, 962, 963, 967, 968, 969, 970, 971, 972, 977, 978, 979, 980, 981, 982, 983, 991, 992, 993, 994, 995, 1000, 1001, 1002, 1007, 1008, 1009, 1014, 1015, 1019, 1020, 1021, 1025, 1026, 1029, 1030, 1031, 1034, 1035, 1038, 1039, 1040, 1043, 1044, 1047, 1048, 1051, 1052, 1055, 1056, 1057, 1060, 1061, 1065, 1066, 1067, 1070, 1071, 1072, 1076, 1077, 1078, 1079, 1083, 1084, 1085, 1086, 1091, 1092, 1093, 1094, 1100, 1101, 1102, 1103, 1104, 1105, 1110, 1111, 1112, 1113, 1114, 1115, 1116, 1119, 1120, 1121, 1128, 1129, 1130, 1131, 1132, 1133, 1138, 1139, 1140, 1144, 1145, 1146, 1150, 1151, 1152, 1156, 1157, 1161, 1162, 1165, 1166, 1167, 1169, 1170, 1172, 1173, 1175, 1176, 1178, 1179, 1181, 1182, 1184, 1185, 1187, 1188, 1191, 1192, 1193, 1196, 1197, 1198, 1202, 1203, 1204, 1208, 1209, 1210, 1213, 1214, 1215, 1218, 1219, 1220, 1221, 1224, 1225, 1226, 1227, 1228, 1235, 1236, 1237, 1238, 1239, 1240, 1241, 1243, 1244, 1245, 1246, 1247, 1254, 1255, 1256, 1257, 1258, 1259, 1265, 1266, 1267, 1272, 1273, 1277, 1278, 1279, 1283, 1284, 1288, 1289, 1292, 1293, 1294, 1297, 1298, 1300, 1302, 1303, 1305, 1306, 1308, 1310, 1312, 1315, 1316, 1319, 1320, 1321, 1325, 1326, 1330, 1331, 1335, 1336, 1337, 1340, 1341, 1342, 1345, 1346, 1347, 1348, 1353, 1354, 1355, 1356, 1357, 1358, 1360, 1361, 1362, 1363, 1364, 1365, 1371, 1372, 1373, 1374, 1375, 1376, 1382, 1383, 1384, 1385, 1386, 1391, 1392, 1393, 1398, 1399, 1400, 1405, 1406, 1409, 1410, 1414, 1415, 1416, 1419, 1420, 1423, 1424, 1427, 1428, 1430, 1431, 1433, 1434, 1436, 1437, 1440, 1441, 1444, 1445, 1446, 1449, 1450, 1454, 1455, 1456, 1459, 1460, 1461, 1465, 1466, 1467, 1471, 1472, 1473, 1478, 1479, 1480, 1481, 1487, 1488, 1489, 1490, 1491, 1492, 1494, 1495, 1496, 1497, 1498, 1499, 1503, 1504, 1505, 1506, 1507, 1513, 1514, 1515, 1516, 1517, 1518, 1519, 1520, 1527, 1528, 1529, 1530, 1535, 1536, 1537, 1542, 1543, 1544, 1548, 1549, 1550, 1553, 1554, 1555, 1559, 1560, 1561, 1565, 1566, 1569, 1570, 1573, 1574, 1577, 1578, 1581, 1582, 1583, 1587, 1588, 1589, 1593, 1594, 1598, 1599, 1600, 1604, 1605, 1609, 1610, 1611, 1616, 1617, 1618, 1623, 1624, 1625, 1631, 1632, 1633, 1634, 1635, 1636, 1640, 1641, 1642, 1643, 1644, 1645, 1646, 1647, 1650, 1651, 1652, 1653, 1654, 1657, 1658, 1663, 1664, 1665, 1666, 1667, 1668, 1669, 1677, 1678, 1679, 1680, 1681, 1682, 1687, 1688, 1689, 1690, 1695, 1696, 1701, 1702, 1703, 1708, 1709, 1710, 1714, 1715, 1716, 1719, 1720, 1721, 1724, 1725, 1728, 1729, 1733, 1734, 1738, 1739, 1743, 1744, 1745, 1749, 1750, 1751, 1755, 1756, 1757, 1761, 1762, 1763, 1768, 1769, 1770, 1775, 1776, 1777, 1783, 1784, 1785, 1786, 1787, 1788, 1793, 1794, 1795, 1796, 1797, 1798, 1799, 1800, 1803, 1804, 1805, 1806, 1807, 1808, 1812, 1813, 1814, 1820, 1821, 1822, 1823, 1824, 1825, 1831, 1832, 1833, 1834, 1835, 1840, 1841, 1842, 1843, 1848, 1849, 1850, 1855, 1856, 1857, 1860, 1861, 1862, 1865, 1866, 1867, 1871, 1872, 1876, 1877, 1878, 1882, 1883, 1884, 1888, 1889, 1890, 1895, 1896, 1897, 1901, 1902, 1903, 1907, 1908, 1913, 1914, 1915, 1919, 1920, 1921, 1922, 1927, 1928, 1929, 1930, 1931, 1932, 1937, 1938, 1939, 1940, 1941, 1942, 1943, 1944, 1945, 1948, 1949, 1950, 1951, 1952, 1953, 1954, 1955, 1956, 1960, 1961, 1962, 1963, 1969, 1970, 1971, 1972, 1973, 1974, 1980, 1981, 1982, 1983, 1984, 1989, 1990, 1991, 1992, 1997, 1998, 1999, 2003, 2004, 2005, 2009, 2010, 2011, 2012, 2016, 2017, 2018, 2022, 2023, 2024, 2028, 2029, 2030, 2034, 2035, 2039, 2040, 2041, 2045, 2046, 2047, 2051, 2052, 2053, 2054, 2059, 2060, 2061, 2062, 2063, 2068, 2069, 2070, 2071, 2072, 2077, 2078, 2079, 2080, 2081, 2082, 2083, 2084, 2088, 2089, 2090, 2091, 2092, 2093, 2094, 2095, 2097, 2098, 2099, 2100, 2105, 2106, 2107, 2108, 2109, 2116, 2117, 2118, 2119, 2120, 2121, 2127, 2128, 2129, 2130, 2131, 2136, 2137, 2138, 2139, 2144, 2145, 2146, 2151, 2152, 2153, 2154, 2158, 2159, 2160, 2163, 2164, 2165, 2169, 2170, 2174, 2175, 2176, 2180, 2181, 2182, 2186, 2187, 2188, 2192, 2193, 2194, 2195, 2196, 2200, 2201, 2202, 2203, 2204, 2205, 2210, 2211, 2212, 2213, 2214, 2215, 2216, 2220, 2221, 2222, 2223, 2224, 2225, 2228, 2229, 2230, 2237, 2238, 2239, 2240, 2246, 2247, 2248, 2249, 2250, 2256, 2257, 2258, 2259, 2264, 2265, 2266, 2267, 2272, 2273, 2274, 2278, 2279, 2280, 2284, 2285, 2286, 2290, 2291, 2292, 2297, 2298, 2299, 2303, 2304, 2305, 2306, 2310, 2311, 2312, 2313, 2318, 2319, 2320, 2321, 2322, 2323, 2328, 2329, 2330, 2331, 2332, 2333, 2337, 2338, 2339, 2340, 2341, 2344, 2345, 2346, 2353, 2354, 2355, 2356, 2362, 2363, 2364, 2365, 2366, 2373, 2374, 2375, 2376, 2377, 2382, 2383, 2384, 2385, 2389, 2390, 2391, 2396, 2397, 2398, 2403, 2404, 2405, 2406, 2411, 2412, 2413, 2414, 2418, 2419, 2420, 2421, 2422, 2426, 2427, 2428, 2429, 2430, 2435, 2436, 2437, 2438, 2439, 2440, 2444, 2445, 2446, 2447, 2450, 2451, 2455, 2456, 2457, 2462, 2463, 2464, 2465, 2470, 2471, 2472, 2473, 2479, 2480, 2481, 2482, 2488, 2489, 2490, 2491, 2497, 2498, 2499, 2500, 2504, 2505, 2506, 2507, 2508, 2512, 2513, 2514, 2515, 2516, 2517, 2520, 2521, 2522, 2523, 2524, 2525, 2527, 2528, 2529, 2530, 2538, 2539, 2540, 2544, 2545, 2546, 2549, 2550, 2551, 2552, 2555, 2556, 2557, 2558, 2561, 2562, 2563, 2564, 2566, 2567, 2568, 2569, 2571, 2572};
//	            Integer[] sampleIndices = new Integer[] { 977 };
//	            TreeSet<Integer> sampleSet = new TreeSet<Integer>();
//	            sampleSet.addAll(Arrays.asList(sampleIndices));
//	            for (BoundingBox bb : smallBodyCubes.getAllCubes())
//	            {
//	            	vtkCubeSource cube = new vtkCubeSource();
//	            	cube.SetBounds(bb.getBounds());
//	            	vtkPolyDataMapper mapper = new vtkPolyDataMapper();
//	            	mapper.SetInputConnection(cube.GetOutputPort());
//	            	vtkActor actor = new vtkActor();
//	            	actor.SetMapper(mapper);
//	            	actor.GetProperty().SetOpacity(0.5);
//	            	if (sampleSet.contains(index))
//	            	{
//	            		actor.GetProperty().SetColor(1.0, 1.0, 0.0);
//	            		smallBodyActors.add(actor);
//	            	}
//	            	index++;
//	            }
//	            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
            }
        }

        return smallBodyCubes;
    }

    public void setCubeVisibility(TreeSet<Integer> indices)
    {
    	if (diagnosticModeEnabled == false) return;
    	smallBodyActors.removeAll(diagnosticCubes);
    	diagnosticCubes.clear();
    	for (int index : indices)
    	{
	    	vtkCubeSource cube = new vtkCubeSource();
	    	cube.SetBounds(smallBodyCubes.getCube(index).getBounds());
	    	vtkPolyDataMapper mapper = new vtkPolyDataMapper();
	    	mapper.SetInputConnection(cube.GetOutputPort());
	    	vtkActor actor = new vtkActor();
	    	actor.SetMapper(mapper);
	    	actor.GetProperty().SetOpacity(0.5);
	    	actor.GetProperty().SetColor(1.0, 0.0, 0.0);
	    	diagnosticCubes.add(actor);
			smallBodyActors.add(actor);
    	}
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
    public TreeSet<Integer> getIntersectingCubes(vtkPolyData polydata)
    {
        return getSmallBodyCubes().getIntersectingCubes(polydata);
    }

    @Override
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
            vtkFloatArray normals = (vtkFloatArray) normalsFilterOutputCellData.GetNormals();

            // Bail if no data
            if (normals == null)
            {
            	cellNormals = new vtkFloatArray();
            	return cellNormals;
            }

            cellNormals = new vtkFloatArray();
            cellNormals.DeepCopy(normals);

            normals.Delete();
            normalsFilterOutputCellData.Delete();
            normalsFilterOutput.Delete();
            normalsFilter.Delete();
        }

        return cellNormals;
    }

    @Override
    public void setShowSmallBody(boolean show)
    {
        if (show)
        {
            if (!smallBodyActors.contains(smallBodyActor))
                smallBodyActors.add(smallBodyActor);
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
        else
        {
            if (smallBodyActors.contains(smallBodyActor))
                smallBodyActors.remove(smallBodyActor);
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    public void sortPolydata(vtkCamera camera)
    {
    	vtkDepthSortPolyData depthSorter = new vtkDepthSortPolyData();
		depthSorter.SetInputData(smallBodyPolyData);
		depthSorter.SetDirectionToBackToFront();
		depthSorter.SetCamera(camera);
		smallBodyMapper.SetInputConnection(depthSorter.GetOutputPort());
		depthSorter.Update();
    }

    public vtkPolyData computeFrustumIntersection(double[] origin, double[] ul, double[] ur, double[] lr, double[] ll)
    {
		return PolyDataUtil.computeFrustumIntersection(smallBodyPolyData, cellLocator, pointLocator, origin, ul, ur, lr,
				ll);
		
//		return PolyDataUtil.computeVTKFrustumIntersection(smallBodyPolyData, origin, ul, ur, lr, ll);
    }

    public vtkPolyData computeMultipleFrustumIntersection(List<Frustum> frustums)
    {
        return PolyDataUtil.computeMultipleFrustumIntersection(smallBodyPolyData, cellLocator, pointLocator, frustums);
    }

    @Override
    public void drawPolygon(List<LatLon> controlPoints, vtkPolyData outputInterior, vtkPolyData outputBoundary)
    {
		PolyDataUtil.drawPolygonOnPolyData(smallBodyPolyData, pointLocator, controlPoints, outputInterior,
				outputBoundary);
    }

	@Override
	public void drawRegularPolygonLowRes(double[] aCenterArr, double aRadius, int aNumSides, vtkPolyData aRetInteriorPD,
			vtkPolyData aRetExteriorPD)
	{
		Vector3D center = new Vector3D(aCenterArr);
		double flattening = 1.0;
		double angle = 0.0;

		// Determine the VTK vars to use (ensure low res data)
		vtkPolyData vSurfacePD = smallBodyPolyData;
		vtkPointLocator vSurfacePL = pointLocator;

		if (resolutionLevel != 0)
		{
			initializeLowResData();

			vSurfacePD = lowResSmallBodyPolyData;
			vSurfacePL = lowResPointLocator;
		}

		// Render the circle
		VtkDrawUtil.drawEllipseOn(vSurfacePD, vSurfacePL, center, aRadius, flattening, angle, aNumSides,
				aRetInteriorPD, aRetExteriorPD);
	}

	public void drawCone(double[] vertex, double[] axis, double angle, int numberOfSides, vtkPolyData outputInterior,
			vtkPolyData outputBoundary)
    {
		PolyDataUtil.drawConeOnPolyData(smallBodyPolyData, pointLocator, vertex, axis, angle, numberOfSides,
				outputInterior, outputBoundary);
    }

    @Override
    public void shiftPolyLineInNormalDirection(vtkPolyData polyLine, double shiftAmount)
    {
        PolyDataUtil.shiftPolyLineInNormalDirectionOfPolyData(polyLine, smallBodyPolyData, pointLocator, shiftAmount);
    }

    @Override
    public double[] getNormalAtPoint(double[] point)
    {
        return PolyDataUtil.getPolyDataNormalAtPoint(point, smallBodyPolyData, pointLocator);
    }

    @Override
    public double[] getClosestNormal(double[] point)
    {
        int closestCell = findClosestCell(point);
        return getCellNormals().GetTuple3(closestCell);
    }

	@Override
	public Vector3D calcInterceptBetween(Vector3D aBegPos, Vector3D aEndPos)
	{
		double[] ptBeg = aBegPos.toArray();
		double[] ptEnd = aEndPos.toArray();

		double tol = 1e-6;
		double[] t = new double[1];
		double[] x = new double[3];
		double[] pcoords = new double[3];
		int[] subId = new int[1];
		int[] cellId = new int[1];

		Vector3D retIntersectPos = null;
		vtkGenericCell vTmpGC = new vtkGenericCell();
		boolean isPass = getCellLocator().IntersectWithLine(ptBeg, ptEnd, tol, t, x, pcoords, subId, cellId, vTmpGC) > 0;
		if (isPass == true)
			retIntersectPos = new Vector3D(x[0], x[1], x[2]);
		vTmpGC.Delete();

		return retIntersectPos;
	}

	@Override
	public Vector3D findClosestPoint(Vector3D aPoint)
	{
		// Delegate
		double[] tmpPointArr = findClosestPoint(aPoint.toArray());
		return new Vector3D(tmpPointArr);
	}

    /**
     * This returns the closest point to the model to pt. Note the returned point
     * need not be a vertex of the model and can lie anywhere on a plate.
     *
     * @param pt
     * @return
     */
    @Override
    public double[] findClosestPoint(double[] pt)
    {
        double[] closestPoint = new double[3];
        int[] cellId = new int[1];
        int[] subId = new int[1];
        double[] dist2 = new double[1];

        cellLocator.FindClosestPoint(pt, closestPoint, genericCell, cellId, subId, dist2);

        return closestPoint;
    }

    /**
     * This returns the closest vertex in the shape model to pt. Unlike
     * findClosestPoin this functions only returns one of the vertices of the shape
     * model not an arbitrary point lying on a cell.
     *
     * @param pt
     * @return
     */
    public double[] findClosestVertex(double[] pt)
    {
        int id = pointLocator.FindClosestPoint(pt);
        return smallBodyPolyData.GetPoint(id).clone();
    }

    public long findClosestVertexId(double[] pt)
    {
        return pointLocator.FindClosestPoint(pt);
    }

    /**
     * This returns the index of the closest cell in the model to pt. The closest
     * point within the cell is returned in closestPoint
     *
     * @param pt
     * @param closestPoint the closest point within the cell is returned here
     * @return
     */
    public int findClosestCell(double[] pt, double[] closestPoint)
    {
        int[] cellId = new int[1];
        int[] subId = new int[1];
        double[] dist2 = new double[1];

        // Use FindClosestPoint rather the FindCell since not sure what tolerance to use
        // in the latter.
        cellLocator.FindClosestPoint(pt, closestPoint, genericCell, cellId, subId, dist2);

        return cellId[0];
    }

    /**
     * This returns the index of the closest cell in the model to pt.
     *
     * @param pt
     * @return
     */
    public int findClosestCell(double[] pt)
    {
        double[] closestPoint = new double[3];
        return findClosestCell(pt, closestPoint);
    }

    /**
     * Compute the point on the asteroid that has the specified latitude and
     * longitude. Returns the cell id of the cell containing that point. This is
     * done by shooting a ray from the origin in the specified direction.
     *
     * @param lat - in radians
     * @param lon - in radians
     * @param intersectPoint
     * @return the cellId of the cell containing the intersect point
     */
    @Override
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

        double[] origin = { 0.0, 0.0, 0.0 };
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
     * Compute the intersection of a ray with the asteroid. Returns the cell id of
     * the cell containing that point. This is done by shooting a ray from the
     * specified origin in the specified direction.
     *
     * @param origin point
     * @param direction vector (must be unit vector)
     * @param intersectPoint (returned)
     * @return the cellId of the cell containing the intersect point
     */
    public int computeRayIntersection(double[] origin, double[] direction, double[] intersectPoint)
    {
        double distance = MathUtil.vnorm(origin);
        if (distance == 0)
            distance = getBoundingBoxDiagonalLength();

        double[] lookPt = new double[3];
        lookPt[0] = origin[0] + 2.0 * distance * direction[0];
        lookPt[1] = origin[1] + 2.0 * distance * direction[1];
        lookPt[2] = origin[2] + 2.0 * distance * direction[2];

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

    protected void initializeActorsAndMappers()
    {
        if (smallBodyActor == null)
        {
            smallBodyMapper = new vtkPolyDataMapper();
            smallBodyMapper.SetInputData(smallBodyPolyData);
            vtkLookupTable lookupTable = new vtkLookupTable();
            smallBodyMapper.SetLookupTable(lookupTable);
            smallBodyMapper.UseLookupTableScalarRangeOn();

			smallBodyActor = new VtkLodActor(this);
			smallBodyActor.setDefaultMapper(smallBodyMapper);
			smallBodyActor.setLodMapper(LodMode.MaxQuality, smallBodyMapper);

			vtkPolyDataMapper tmpDecimatedPDM = LodUtil.createQuadricDecimatedMapper(smallBodyPolyData);
			smallBodyActor.setLodMapper(LodMode.MaxSpeed, tmpDecimatedPDM);
			tmpDecimatedPDM.SetLookupTable(lookupTable);
			tmpDecimatedPDM.UseLookupTableScalarRangeOn();
            vtkProperty smallBodyProperty = smallBodyActor.GetProperty();
            smallBodyProperty.SetInterpolationToGouraud();
            // smallBodyProperty.SetSpecular(.1);
            // smallBodyProperty.SetSpecularPower(100);

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

            linesMapper = new vtkPolyDataMapper();
			linesActor = new VtkLodActor(this);
            smallBodyActors.add(linesActor);

        }
    }

    @Override
    public List<vtkProp> getProps()
    {
        initializeActorsAndMappers();

        return smallBodyActors;
    }

    @Override
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

    @Override
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

    @Override
    public BoundingBox getBoundingBox()
    {
        if (boundingBox == null)
        {
            smallBodyPolyData.ComputeBounds();
            boundingBox = new BoundingBox(smallBodyPolyData.GetBounds());
        }

        return boundingBox;

        /*
         * BoundingBox bb = new BoundingBox(); vtkPoints points =
         * smallBodyPolyData.GetPoints(); int numberPoints = points.GetNumberOfPoints();
         * for (int i=0; i<numberPoints; ++i) { double[] pt = points.GetPoint(i);
         * bb.update(pt[0], pt[1], pt[2]); }
         *
         * return bb;
         */
    }

    @Override
    public double getBoundingBoxDiagonalLength()
    {
        return getBoundingBox().getDiagonalLength();
    }

    /**
     * Get the minimum shift amount needed so shift an object away from the model so
     * it is not obscured by the model and looks like it's laying on the model
     *
     * @return
     */
    @Override
    public double getMinShiftAmount()
    {
        return getBoundingBoxDiagonalLength() / 38660.0;
    }

    private String getColoringValueLabel(int coloringIndex, float value)
    {
        ColoringData data = getColoringData(coloringIndex);
        return data.getName() + ": " + value + " " + data.getUnits();
    }

    @Override
	public String getClickStatusBarText(@SuppressWarnings("unused") vtkProp prop, @SuppressWarnings("unused") int cellId,
			double[] pickPosition)
    {
        if (coloringIndex >= 0)
        {
            float value = (float) getColoringValue(coloringIndex, pickPosition);
            return getColoringValueLabel(coloringIndex, value);
        }
        else if (useFalseColoring)
        {
            String result = "";
            boolean firstColor = true;
            if (isColoringAvailable(redFalseColor))
            {
                if (firstColor)
                {
                    firstColor = false;
                }
                else
                {
                    result += ", ";
                }
                float red = (float) getColoringValue(redFalseColor, pickPosition);
                result += getColoringValueLabel(redFalseColor, red);
            }
            if (isColoringAvailable(greenFalseColor))
            {
                if (firstColor)
                {
                    firstColor = false;
                }
                else
                {
                    result += ", ";
                }
                float green = (float) getColoringValue(greenFalseColor, pickPosition);
                result += getColoringValueLabel(greenFalseColor, green);
            }
            if (isColoringAvailable(blueFalseColor))
            {
                if (firstColor)
                {
                    firstColor = false;
                }
                else
                {
                    result += ", ";
                }
                float blue = (float) getColoringValue(blueFalseColor, pickPosition);
                result += getColoringValueLabel(blueFalseColor, blue);
            }
            return result;
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

        for (int i = 0; i < numberOfCells; ++i)
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

        meanLength /= (numberOfCells * 3);

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
        meanCellArea = surfaceArea / smallBodyPolyData.GetNumberOfCells();
        minCellArea = massProp.GetMinCellArea();
        maxCellArea = massProp.GetMaxCellArea();

        /*
         *
         * // The following computes the surface area directly rather than using
         * vtkMassProperties // It gives exactly the same results as vtkMassProperties
         * but is much slower.
         *
         * int numberOfCells = smallBodyPolyData.GetNumberOfCells();
         *
         * System.out.println(numberOfCells); double totalArea = 0.0; minCellArea =
         * Double.MAX_VALUE; maxCellArea = 0.0; for (int i=0; i<numberOfCells; ++i) {
         * vtkCell cell = smallBodyPolyData.GetCell(i); vtkPoints points =
         * cell.GetPoints(); double[] pt0 = points.GetPoint(0); double[] pt1 =
         * points.GetPoint(1); double[] pt2 = points.GetPoint(2); double area =
         * MathUtil.triangleArea(pt0, pt1, pt2); totalArea += area; if (area <
         * minCellArea) minCellArea = area; if (area > maxCellArea) maxCellArea = area;
         * }
         *
         * meanCellArea = totalArea / (double)(numberOfCells);
         *
         *
         * System.out.println("Surface area   " + massProp.GetSurfaceArea());
         * System.out.println("Surface area2  " + totalArea);
         * System.out.println("min cell area  " + massProp.GetMinCellArea());
         * System.out.println("min cell area2 " + minCellArea);
         * System.out.println("max cell area  " + massProp.GetMaxCellArea());
         * System.out.println("max cell area2 " + maxCellArea);
         * System.out.println("Volume " + massProp.GetVolume());
         */
    }

    @Override
    public double getSurfaceArea()
    {
        return surfaceArea;
    }

    @Override
    public double getVolume()
    {
        return volume;
    }

    @Override
    public double getMeanCellArea()
    {
        return meanCellArea;
    }

    @Override
    public double getMinCellArea()
    {
        return minCellArea;
    }

    @Override
    public double getMaxCellArea()
    {
        return maxCellArea;
    }

    @Override
    public void setModelResolution(int level) throws IOException
    {
        if (level == resolutionLevel)
            return;

        resolutionLevel = level;
        if (level < 0)
            resolutionLevel = 0;
        else if (level > getNumberResolutionLevels() - 1)
            resolutionLevel = getNumberResolutionLevels() - 1;

        reloadShapeModel();
        
    }

    @Override
    public void reloadShapeModel() throws IOException
    {
        smallBodyCubes = null;
        for (ColoringData data : getAllColoringData())
        {
            data.clear();
        }

        cellNormals = null;
        gravityVector = null;
        boundingBox = null;

        File smallBodyFile = FileCache.getFileFromServer(modelFiles[resolutionLevel]);

        if (!smallBodyFile.exists())
        {
            throw new IOException("Unable to load shape model " + smallBodyFile.getName());
        }
        this.initializeDefaultModel();

        this.initialize(smallBodyFile);

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.MODEL_RESOLUTION_CHANGED, null, null);

    }

    @Override
    public int getModelResolution()
    {
        return resolutionLevel;
    }

    @Override
    public int getNumberResolutionLevels()
    {
        return modelFiles.length;
    }

    @Override
    public boolean isResolutionLevelAvailable(int resolutionLevel)
    {
        if (resolutionLevel >= 0 && resolutionLevel < modelFiles.length)
        {
            return FileCache.instance().isAccessible(modelFiles[resolutionLevel]);
        }

        return false;
    }

    @Override
    public String getModelName()
    {
        if (modelNames == null)
            return null;
        if (resolutionLevel >= 0 && resolutionLevel < modelNames.length)
            return modelNames[resolutionLevel];
        else
            return null;
    }

    @Override
    public List<String> getModelFileNames()
    {
        return ImmutableList.copyOf(modelFiles);
    }

    /**
     * Load just the current plate coloring identified by coloringIndex.
     *
     * @throws IOException if the plate coloring fails to load
     */
    protected void loadColoringData() throws IOException
    {
        ColoringData coloringData = getColoringData(coloringIndex);
        coloringData.getData();
    }

	private static String getColoringFileName(String baseFileName, int resolutionLevel, Format format,
			boolean sbmt2Style)
    {
        String fileName = null;
        if (!SAFE_URL_PATHS.hasFileProtocol(baseFileName))
        {
            if (sbmt2Style)
            {
                fileName = getColoringFileName(baseFileName + resolutionLevel, format);
                if (fileName == null)
                {
                    fileName = getColoringFileName(baseFileName + "_res" + resolutionLevel, format);
                }
            }
            else
            {
                fileName = getColoringFileName(baseFileName + "_res" + resolutionLevel, format);
                if (fileName == null)
                {
                    fileName = getColoringFileName(baseFileName + resolutionLevel, format);
                }
            }
        }
        else
        {
            fileName = baseFileName;
        }

        return fileName;
    }

    private static String getColoringFileName(String baseFileName, Format format)
    {
        String fileName = null;
        switch (format)
        {
        case TXT:
            fileName = baseFileName + ".txt.gz";
            break;
        case FIT:
            fileName = baseFileName + ".fits.gz";
            break;
        default:
            throw new AssertionError("Unhandled case " + format);
        }

        return fileName;
    }

    /**
     * This file loads the coloring data.
     *
     * @throws IOException
     */
    protected void loadAllColoringData() throws IOException
    {
        for (ColoringData data : getAllColoringData())
        {
            data.getData();
        }
    }

    // private void invertLookupTableCharArray(vtkUnsignedCharArray table)
    // {
    // int numberOfValues = table.GetNumberOfTuples();
    // for (int i = 0; i < numberOfValues / 2; ++i)
    // {
    // double[] v1 = table.GetTuple4(i);
    // double[] v2 = table.GetTuple4(numberOfValues - i - 1);
    // table.SetTuple4(i, v2[0], v2[1], v2[2], v2[3]);
    // table.SetTuple4(numberOfValues - i - 1, v1[0], v1[1], v1[2], v1[3]);
    // }
    // }
    //
    // /**
    // * Invert the lookup table so that red is high values and blue is low values
    // * (rather than the reverse).
    // */
    // private void invertLookupTable()
    // {
    // vtkLookupTable lookupTable = (vtkLookupTable)
    // smallBodyMapper.GetLookupTable();
    // vtkUnsignedCharArray table = lookupTable.GetTable();
    //
    // invertLookupTableCharArray(table);
    // // int numberOfValues = table.GetNumberOfTuples();
    // // for (int i=0; i<numberOfValues/2; ++i)
    // // {
    // // double[] v1 = table.GetTuple4(i);
    // // double[] v2 = table.GetTuple4(numberOfValues-i-1);
    // // table.SetTuple4(i, v2[0], v2[1], v2[2], v2[3]);
    // // table.SetTuple4(numberOfValues-i-1, v1[0], v1[1], v1[2], v1[3]);
    // // }
    //
    // lookupTable.SetTable(table);
    // smallBodyMapper.Modified();
    // }
    //
    @Override
    public void setColoringIndex(int index) throws IOException
    {
        if (coloringIndex != index || useFalseColoring)
        {
            final int currentIndex = coloringIndex;
            final boolean currentFalseColoring = useFalseColoring;

            coloringIndex = index;
            useFalseColoring = false;

            if (index != -1)
            {
                try
                {
                    loadColoringData();
                }
                catch (Throwable t)
                {
                    coloringIndex = currentIndex;
                    useFalseColoring = currentFalseColoring;
                    throw t;
                }

                smallBodyActor.GetMapper().SetLookupTable(colormap.getLookupTable());

                double[] range = getColoringData(coloringIndex).getDefaultRange();
                setCurrentColoringRange(coloringIndex, range);
            }

            paintBody();
        }
    }

    @Override
    public int getColoringIndex()
    {
        return coloringIndex;
    }

    @Override
    public void setFalseColoring(int redChannel, int greenChannel, int blueChannel) throws IOException
    {
        redFalseColor = redChannel;
        greenFalseColor = greenChannel;
        blueFalseColor = blueChannel;

		if (isColoringAvailable(redFalseColor) || isColoringAvailable(greenFalseColor)
				|| isColoringAvailable(blueFalseColor))
        {
            coloringIndex = -1;
            useFalseColoring = true;
        }
        else
        {
            useFalseColoring = false;
        }

        paintBody();
    }

    @Override
    public int[] getFalseColoring()
    {
        return new int[] { redFalseColor, greenFalseColor, blueFalseColor };
    }

    @Override
    public boolean isFalseColoringEnabled()
    {
        return useFalseColoring;
    }

    @Override
    public boolean isColoringDataAvailable()
    {
        return !getAllColoringData().isEmpty();
    }

    @Override
    public int getNumberOfColors()
    {
        return coloringDataManager.getNames().size();
    }

    @Override
    public String getColoringName(int i)
    {
        return getColoringData(i).getName();
    }

    @Override
    public String getColoringUnits(int i)
    {
        return getColoringData(i).getUnits();
    }

    /**
     * Get value assuming pt is exactly on the asteroid and cellId is provided
     *
     * @param pt
     * @param pointOrCellData
     * @return
     */
    private double getScalarValue(double[] pt, IndexableTuple pointOrCellData)
    {
        double[] closestPointIgnored = new double[3];
        int cellId = findClosestCell(pt, closestPointIgnored);

        if (coloringValueType == ColoringValueType.POINT_DATA)
        {
            return PolyDataUtil.interpolateWithinCell(smallBodyPolyData, pointOrCellData, cellId, pt, idList, 1)[0];
        }
        else
        {
            return pointOrCellData.get(cellId).get(0);
        }
    }

    private double[] getVectorValue(double[] pt, IndexableTuple pointOrCellData, int cellId, int numberAxes)
    {
        double[] result = null;
        if (coloringValueType == ColoringValueType.POINT_DATA)
        {
			result = PolyDataUtil.interpolateWithinCell(smallBodyPolyData, pointOrCellData, cellId, pt, idList,
					numberAxes);
        }
        else
        {
            if (numberAxes >= 1 && numberAxes <= 3)
            {
                result = pointOrCellData.get(cellId).get();
            }
            else
            {
                throw new IllegalArgumentException("Cannot get a vector with " + numberAxes + " axes");
            }
        }
        return result;
    }

    @Override
    public double getColoringValue(int index, double[] pt)
    {
        ColoringData coloringData = getColoringData(index);

        return getScalarValue(pt, coloringData.getData());
    }

    @Override
    public ImmutableList<ColoringData> getAllColoringData()
    {
        ImmutableList<Integer> resolutions = coloringDataManager.getResolutions();
		return resolutions.size() > resolutionLevel ? coloringDataManager.get(resolutions.get(resolutionLevel))
				: ImmutableList.of();
    }

    @Override
    public double[] getAllColoringValues(double[] pt) throws IOException
    {
        loadAllColoringData();

        double[] closestPoint = new double[3];
        int cellId = findClosestCell(pt, closestPoint);

        ImmutableList<ColoringData> coloringData = getAllColoringData();
        int numColorColumns = 0;
        for (ColoringData data : coloringData)
        {
            numColorColumns += data.getFieldNames().size();
        }

        double[] result = new double[numColorColumns];
        int valueIndex = 0;
        for (ColoringData data : coloringData)
        {
            double[] coloringVector = getVectorValue(closestPoint, data.getData(), cellId, data.getFieldNames().size());
            for (int index = 0; index < coloringVector.length; ++index, ++valueIndex)
            {
                result[valueIndex] = coloringVector[index];
            }
        }

        return result;
    }

    /**
     * Subclass must override this method if it wants to support loading gravity
     * vector.
     *
     * @param resolutionLevel
     * @return
     */
    protected String getGravityVectorFilePath(int resolutionLevel)
    {
        return null;
    }

    @Override
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

    @Override
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

    @Override
    public double[] getDefaultColoringRange(int coloringIndex)
    {
    	double[] defaultRange = getColoringData(coloringIndex).getDefaultRange();
    	if (defaultRange[0] == 0.0 && defaultRange[1] == 0.0) defaultRange[1] = 1.0;
        return defaultRange;
    }

    @Override
    public double[] getCurrentColoringRange(int coloringIndex)
    {
        if (colormap != null)
            return new double[] { colormap.getRangeMin(), colormap.getRangeMax() };

        return getDefaultColoringRange(coloringIndex);
    }

    @Override
    public void setCurrentColoringRange(@SuppressWarnings("unused") int coloringIndex, double[] range) throws IOException
    {
        boolean doSet = false;
        if (colormap == null)
        {
            initColormap();
            doSet = true;
        }
        else
        {
            double min = colormap.getRangeMin();
            double max = colormap.getRangeMax();
            if (Double.compare(min, range[0]) != 0 || Double.compare(max, range[1]) != 0)
            {
                doSet = true;
            }
        }
        if (doSet)
        {
            colormap.setRangeMin(range[0]);
            colormap.setRangeMax(range[1]);
        }

        paintBody();
    }

    private interface Indexable<T>
    {
        int size();

        T get(int index);
    }

    private Indexable<Double> getIndexable(final int coloringIndex, final int missingTuple) throws IOException
    {
        if (isColoringAvailable(coloringIndex))
        {
            ColoringData coloringData = getColoringData(coloringIndex);
            int size = coloringData.getNumberElements();

            IndexableTuple tuples = coloringData.getData();
            final double[] range = coloringData.getDefaultRange();
            final double extent = range[1] - range[0];
            if (Double.compare(extent, 0.) < 0)
            {
                throw new IllegalStateException();
            }

            return new Indexable<Double>() {
                final double scale = 255.;

                @Override
                public int size()
                {
                    return size;
                }

                @Override
                public Double get(int index)
                {
                    return scale * (tuples.get(index).get(0) - range[0]) / extent;
                }

            };
        }
        return new Indexable<Double>() {
            final double colorMissing = 0.;

            @Override
            public int size()
            {
                return missingTuple;
            }

            @Override
            public Double get(@SuppressWarnings("unused") int index)
            {
                return colorMissing;
            }

        };
    }

    /**
     * Update the false color point or cell data if
     *
     * @throws IOException
     */
    private void updateFalseColorArray() throws IOException
    {
        final int missingTuple = -1;
        Indexable<Double> red = getIndexable(redFalseColor, missingTuple);
        Indexable<Double> green = getIndexable(greenFalseColor, missingTuple);
        Indexable<Double> blue = getIndexable(blueFalseColor, missingTuple);

        int numberTuples = Math.max(red.size(), Math.max(green.size(), blue.size()));
        if (numberTuples <= 0)
        {
            return;
        }

        if (falseColorArray == null)
        {
            falseColorArray = new vtkUnsignedCharArray();
            falseColorArray.SetNumberOfComponents(3);
        }

        falseColorArray.SetNumberOfTuples(numberTuples);

        for (int index = 0; index < numberTuples; ++index)
        {
            falseColorArray.SetTuple3(index, red.get(index), green.get(index), blue.get(index));
        }
    }

    private boolean isColoringAvailable(int coloringIndex)
    {
        boolean result = false;
        if (coloringIndex >= 0)
        {
            ImmutableList<String> names = coloringDataManager.getNames();
            ImmutableList<Integer> resolutions = coloringDataManager.getResolutions();
            if (names.size() > coloringIndex && resolutions.size() > resolutionLevel)
            {
                String name = names.get(coloringIndex);
                int numberElements = resolutions.get(resolutionLevel);
                return coloringDataManager.has(name, numberElements);
            }
        }
        return result;
    }

    private ColoringData getColoringData(int coloringIndex)
    {
        if (!isColoringAvailable(coloringIndex))
        {
			throw new RuntimeException(
					"Coloring number " + coloringIndex + " is not available for resolution level " + resolutionLevel);
        }
        String name = coloringDataManager.getNames().get(coloringIndex);
        int numberElements = coloringDataManager.getResolutions().get(resolutionLevel);
        return coloringDataManager.get(name, numberElements);
    }

    private void paintBody() throws IOException
    {
        initializeActorsAndMappers();

        Map<String, Object> newPaintingAttributes = new HashMap<>();

        boolean doPaint = false;
        doPaint |= checkAndSave("coloringIndex", coloringIndex, newPaintingAttributes);

        if (coloringIndex >= 0 && isColoringAvailable(coloringIndex))
        {
            loadColoringData();

            ColoringData coloringData = getColoringData(coloringIndex);

            String title = coloringData.getName();
            String units = coloringData.getUnits();
            if (!units.isEmpty())
            {
                title += " (" + units + ")";
            }
            doPaint |= checkAndSave("title", title, newPaintingAttributes);
            scalarBarActor.SetTitle(title);

            vtkFloatArray floatArray = new vtkFloatArray();
            ColoringDataUtils.copyIndexableToVtkArray(coloringData.getData(), floatArray);
            doPaint |= checkAndSave("floatArray", floatArray, newPaintingAttributes);

            initColormap();

            doPaint |= checkAndSave("showColorsAsContourLines", showColorsAsContourLines, newPaintingAttributes);

            doPaint |= checkAndSave("rangeMin", colormap.getRangeMin(), newPaintingAttributes);
            doPaint |= checkAndSave("rangeMax", colormap.getRangeMax(), newPaintingAttributes);
            doPaint |= checkAndSave("numberOfLevels", colormap.getNumberOfLevels(), newPaintingAttributes);
            doPaint |= checkAndSave("numberOfLabels", colormap.getNumberOfLabels(), newPaintingAttributes);
            doPaint |= checkAndSave("isLogScale", colormap.isLogScale(), newPaintingAttributes);

            if (!showColorsAsContourLines)
            {

                vtkLookupTable lookupTable = colormap.getLookupTable();
                doPaint |= checkAndSave("lookupTable", lookupTable, newPaintingAttributes);
                if (doPaint)
                {
                    if (smallBodyActors.contains(linesActor))
                        smallBodyActors.remove(linesActor);

                    vtkUnsignedCharArray rgbColorData = new vtkUnsignedCharArray();
                    rgbColorData.SetNumberOfComponents(3);
                    for (int index = 0; index < floatArray.GetNumberOfTuples(); ++index)
                    {
                        double value = floatArray.GetValue(index);
                        Color c = colormap.getColor(value);
                        rgbColorData.InsertNextTuple3(c.getRed(), c.getGreen(), c.getBlue());
                    }

                    smallBodyMapper.SetLookupTable(colormap.getLookupTable());

					vtkPolyDataMapper tmpDecimatedPDM = LodUtil.createQuadricDecimatedMapper(smallBodyPolyData);
					smallBodyActor.setLodMapper(LodMode.MaxSpeed, tmpDecimatedPDM);
					tmpDecimatedPDM.SetLookupTable(colormap.getLookupTable());
					tmpDecimatedPDM.UseLookupTableScalarRangeOn();

                    if (coloringValueType == ColoringValueType.POINT_DATA)
                        this.smallBodyPolyData.GetPointData().SetScalars(rgbColorData);
                    else
                        this.smallBodyPolyData.GetCellData().SetScalars(rgbColorData);

                    smallBodyMapper.ScalarVisibilityOn();
                }

            }
            else
            {
                doPaint |= checkAndSave("contourLineWidth", contourLineWidth, newPaintingAttributes);
                if (doPaint)
                {
					vtkPolyDataMapper tmpDecimatedPDM = LodUtil.createQuadricDecimatedMapper(smallBodyPolyData);
					smallBodyActor.setLodMapper(LodMode.MaxSpeed, tmpDecimatedPDM);
					tmpDecimatedPDM.ScalarVisibilityOff();
                    smallBodyMapper.ScalarVisibilityOff();

                    vtkPolyData polyData;
                    if (coloringValueType == ColoringValueType.POINT_DATA)
                    {
                        smallBodyPolyData.GetPointData().SetScalars(floatArray);
                        polyData = smallBodyPolyData;
                    }
                    else
                    {
                        smallBodyPolyData.GetCellData().SetScalars(floatArray);
                        vtkCellDataToPointData converter = new vtkCellDataToPointData();
                        converter.SetInputData(smallBodyPolyData);
                        converter.PassCellDataOff();
                        converter.Update();
                        polyData = converter.GetPolyDataOutput(); // contour filter requires point data with one
                                                                  // component
                    }

                    vtkContourFilter contourFilter = new vtkContourFilter();
                    contourFilter.SetInputData(polyData);
					contourFilter.GenerateValues(colormap.getNumberOfLevels(), colormap.getRangeMin(),
							colormap.getRangeMax());
                    contourFilter.Update();

					linesMapper = LodUtil.createQuadricDecimatedMapper(contourFilter.GetOutput());
                    linesMapper.SetInputData(contourFilter.GetOutput());
                    linesMapper.ScalarVisibilityOn();
                    linesMapper.SetScalarModeToDefault();
                    linesMapper.SetLookupTable(colormap.getLookupTable());
                    linesMapper.UseLookupTableScalarRangeOn();

                    linesActor.VisibilityOn();
					linesActor.setDefaultMapper(linesMapper);
					linesActor.setLodMapper(LodMode.MaxQuality, linesMapper);
					linesActor.setLodMapper(LodMode.MaxSpeed, linesMapper);
                    linesActor.GetProperty().SetLineWidth(contourLineWidth);

                    if (!smallBodyActors.contains(linesActor))
                        smallBodyActors.add(linesActor);
                }
            }
        }
        else
        {
            doPaint |= checkAndSave("useFalseColoring", useFalseColoring, newPaintingAttributes);
            doPaint |= checkAndSave("redFalseColor", redFalseColor, newPaintingAttributes);
            doPaint |= checkAndSave("greenFalseColor", greenFalseColor, newPaintingAttributes);
            doPaint |= checkAndSave("blueFalseColor", blueFalseColor, newPaintingAttributes);

            if (doPaint)
            {

                if (smallBodyActors.contains(scalarBarActor))
                    smallBodyActors.remove(scalarBarActor);
                if (smallBodyActors.contains(linesActor))
                    smallBodyActors.remove(linesActor);

                if (useFalseColoring)
                {
					if (isColoringAvailable(redFalseColor) || isColoringAvailable(greenFalseColor)
							|| isColoringAvailable(blueFalseColor))
                    {
                        updateFalseColorArray();
                        if (coloringValueType == ColoringValueType.POINT_DATA)
                            this.smallBodyPolyData.GetPointData().SetScalars(falseColorArray);
                        else
                            this.smallBodyPolyData.GetCellData().SetScalars(falseColorArray);
                        smallBodyMapper.ScalarVisibilityOn();
						vtkPolyDataMapper tmpDecimatedPDM = LodUtil.createQuadricDecimatedMapper(smallBodyPolyData);
						smallBodyActor.setLodMapper(LodMode.MaxSpeed, tmpDecimatedPDM);
                    }
                }
                else
                {
					vtkPolyDataMapper tmpDecimatedPDM = LodUtil.createQuadricDecimatedMapper(smallBodyPolyData);
					smallBodyActor.setLodMapper(LodMode.MaxSpeed, tmpDecimatedPDM);
					tmpDecimatedPDM.ScalarVisibilityOff();
                    smallBodyMapper.ScalarVisibilityOff();
                }
            }
        }

        if (doPaint)
        {
            paintingAttributes = newPaintingAttributes;

            this.smallBodyPolyData.Modified();

            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    private <T> boolean checkAndSave(String attribute, T value, Map<String, Object> newPaintingAttributes)
    {
        newPaintingAttributes.put(attribute, value);
        boolean changed = true;

        if (paintingAttributes != null)
        {
            Object storedObject = paintingAttributes.get(attribute);

            if (storedObject == value)
                changed = false;
            else if (storedObject != null)
                changed = !storedObject.equals(value);
        }

        return changed;
    }

    @Override
    public void setOpacity(double opacity)
    {
        vtkProperty smallBodyProperty = smallBodyActor.GetProperty();
        smallBodyProperty.SetOpacity(opacity);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
    public void setSpecularCoefficient(double value)
    {
        smallBodyActor.GetProperty().SetSpecular(value);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
    public void setSpecularPower(double value)
    {
        smallBodyActor.GetProperty().SetSpecularPower(value);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
    public void setRepresentationToSurface()
    {
        smallBodyActor.GetProperty().SetRepresentationToSurface();
        smallBodyActor.GetProperty().EdgeVisibilityOff();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
    public void setRepresentationToWireframe()
    {
        smallBodyActor.GetProperty().SetRepresentationToWireframe();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
    public void setRepresentationToPoints()
    {
        smallBodyActor.GetProperty().SetRepresentationToPoints();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
    public void setRepresentationToSurfaceWithEdges()
    {
        smallBodyActor.GetProperty().SetRepresentationToSurface();
        smallBodyActor.GetProperty().EdgeVisibilityOn();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
    public void setPointSize(double value)
    {
        smallBodyActor.GetProperty().SetPointSize(value);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
    public void setLineWidth(double value)
    {
        smallBodyActor.GetProperty().SetLineWidth(value);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
    public void setCullFrontface(boolean enable)
    {
        smallBodyActor.GetProperty().SetFrontfaceCulling(enable ? 1 : 0);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setCullBackface(boolean enable)
    {
        smallBodyActor.GetProperty().SetBackfaceCulling(enable ? 1 : 0);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
    public void delete()
    {
        if (smallBodyPolyData != null)
            smallBodyPolyData.Delete();
        if (lowResSmallBodyPolyData != null)
            lowResSmallBodyPolyData.Delete();
        if (smallBodyActor != null)
            smallBodyActor.Delete();
        if (smallBodyMapper != null)
            smallBodyMapper.Delete();
        for (vtkProp prop : smallBodyActors)
            if (prop != null)
                prop.Delete();
        if (cellLocator != null)
            cellLocator.Delete();
        if (pointLocator != null)
            pointLocator.Delete();
        // if (lowResPointLocator != null) lowResPointLocator.Delete();
        if (genericCell != null)
            genericCell.Delete();
        if (scalarBarActor != null)
            scalarBarActor.Delete();
        if (smallBodyPolyData != null)
            smallBodyPolyData.Delete();
        // if (lowResSmallBodyPolyData != null) lowResSmallBodyPolyData.Delete();
    }

    /**
     * Return if this model is an ellipsoid. If so, some operations on ellipsoids
     * are much easier than general shape models. By default return false, unless a
     * subclasses overrides it.
     *
     * @return
     */
    @Override
    public boolean isEllipsoid()
    {
        return false;
    }

    /**
     * Subclass should override this if it needs it. Currently only shape models
     * with lidar data need this. Return density of shape model in g/cm^3.
     *
     * @return
     */
    @Override
    public double getDensity()
    {
        return 0.0;
    }

    /**
     * Subclass should override this if it needs it. Currently only shape models
     * with lidar data need this. Return rotation rate in radians/sec.
     *
     * @return
     */
    @Override
    public double getRotationRate()
    {
        return 0.0;
    }

    /**
     * Subclass should override this if it needs it. Currently only shape models
     * with lidar data need this. Return reference potential in m^2/sec^2. The
     * reference potential is defined as SUM(P_p*A_p)/SUM(A_p), where P_p is the
     * potential at the center of plate p, A_p is the area of plate p, and the sum
     * is over all plates in the shape model.
     *
     * @return
     */
    @Override
    public double getReferencePotential()
    {
        try
        {
            ImmutableList<Integer> resolutions = coloringDataManager.getResolutions();
            if (resolutions.size() <= resolutionLevel)
            {
                throw new RuntimeException("No colorings available at resolution level " + resolutionLevel);
            }
			ColoringData gravColoring = coloringDataManager.get(GravPotStr,
					coloringDataManager.getResolutions().get(resolutionLevel));
            IndexableTuple tuples = gravColoring.getData();

            double potTimesAreaSum = 0.0;
            double totalArea = 0.0;
            double minRefPot = Double.MAX_VALUE;
            int numFaces = smallBodyPolyData.GetNumberOfCells();
            for (int i = 0; i < numFaces; ++i)
            {
                double potential = tuples.get(i).get(0);
                if (potential < minRefPot)
                {
                    minRefPot = potential;
                }
                double area = ((vtkTriangle) smallBodyPolyData.GetCell(i)).ComputeArea();

                potTimesAreaSum += potential * area;
                totalArea += area;
            }

            if (getConfig().useMinimumReferencePotential)
            {
                return minRefPot;
            }
            else
            {
                return potTimesAreaSum / totalArea;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return Double.MAX_VALUE;
        }
    }

    /**
     * Subclass should override this if it needs it. Currently only shape models
     * with lidar data need this. Return path on server to shape model in PLT
     * format. Needed because gravity program only supports PLT format, not VTK
     * format.
     *
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

 	@Override
 	public void setPlainColor(Color aColor)
 	{
 		try
 		{
 			setColoringIndex(-1);
 		}
 		catch (IOException aExp)
 		{
 			aExp.printStackTrace();
 		}

 		smallBodyActor.GetProperty().SetColor(aColor.getRed() / 255.0, aColor.getGreen() / 255.0,
 				aColor.getBlue() / 255.0);
 		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
 	}

    /**
     * Saves out file with information about each plate of shape model that contains
     * these columns. Each row contains information about a single plate in the
     * order the plates define the model.
     *
     * 1. Surface area of plate 2. X coordinate of center of plate 3. Y coordinate
     * of center of plate 4. Z coordinate of center of plate 5. Latitude of center
     * of plate (in degrees) 6. Longitude of center of plate (in degrees) 7.
     * Distance of center of plate to origin 8+. coloring data of plate, both
     * built-in and custom
     *
     * @param polydata
     * @param file
     * @throws IOException
     */
    @Override
    public void savePlateData(File file) throws IOException
    {
        savePlateData(new Indexable<Integer>() {

            @Override
            public int size()
            {
                return smallBodyPolyData.GetNumberOfCells();
            }

            @Override
            public Integer get(int index)
            {
                return index;
            }

        }, file);
    }

    /**
     * Given a polydata that is coincident with part of the shape model, save out
     * the plate data for all cells of the shape model that touch the polydata (even
     * a little bit).
     *
     * @param polydata
     * @param file
     * @throws IOException
     */
    @Override
    public void savePlateDataInsidePolydata(vtkPolyData polydata, File file) throws IOException
    {
        ImmutableList<Integer> cellIdList = getClosestCellList(polydata);
        savePlateData(new Indexable<Integer>() {

            @Override
            public int size()
            {
                return cellIdList.size();
            }

            @Override
            public Integer get(int index)
            {
                return cellIdList.get(index);
            }

        }, file);
    }

    /**
     * Given a polydata that is coincident with part of the shape model, save out
     * the plate data for all cells of the shape model that touch the polydata (even
     * a little bit).
     *
     * @param polydata
     * @param file
     * @throws IOException
     */
    @Override
    public FacetColoringData[] getPlateDataInsidePolydata(vtkPolyData polydata)
    {
        ImmutableList<Integer> cellIdList = getClosestCellList(polydata);
        FacetColoringData[] coloringData = getColoringDataFor(new Indexable<Integer>() {

            @Override
            public int size()
            {
                return cellIdList.size();
            }

            @Override
            public Integer get(int index)
            {
                return cellIdList.get(index);
            }

        });
        return coloringData;

    }

    public ImmutableList<Integer> getClosestCellList(vtkPolyData polydata)
    {
        // Go through every cell inside the polydata and find the closest cell to it
        // in the shape model and get the plate data for that cell.
        // First put the cells into an ordered set, so we don't save out the
        // same cell twice.
        TreeSet<Integer> cellIds = new TreeSet<>();

        int numCells = polydata.GetNumberOfCells();

        double[] pt0 = new double[3];
        double[] pt1 = new double[3];
        double[] pt2 = new double[3];
        double[] center = new double[3];
        double[] closestPoint = new double[3];

        for (int i = 0; i < numCells; ++i)
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
        return ImmutableList.copyOf(cellIds);
    }

    @Override
    public CustomizableColoringDataManager getColoringDataManager()
    {
        return coloringDataManager;
    }

    @Override
    public void addCustomLidarDataSource(LidarDataSource info) throws IOException
    {
        lidarDataSourceL.add(info);
    }

    @Override
    public void setCustomLidarDataSource(int index, LidarDataSource info) throws IOException
    {
        lidarDataSourceL.set(index, info);
    }

    @Override
    public void removeCustomLidarDataSource(int index) throws IOException
    {
        lidarDataSourceL.remove(index);
    }

    @Override
    public List<LidarDataSource> getLidarDataSourceList()
    {
        return lidarDataSourceL;
    }

    private void savePlateData(Indexable<Integer> indexable, File file) throws IOException
    {
        loadAllColoringData();

        try (FileWriter fstream = new FileWriter(file))
        {
            try (BufferedWriter out = new BufferedWriter(fstream))
            {
                final String lineSeparator = System.getProperty("line.separator");

                out.write("Plate Id");
                out.write(",Area (km^2)");
                out.write(",Center X (km)");
                out.write(",Center Y (km)");
                out.write(",Center Z (km)");
                out.write(",Center Latitude (deg)");
                out.write(",Center Longitude (deg)");
                out.write(",Center Radius (km)");
                ImmutableList<ColoringData> allColoringData = getAllColoringData();
                for (ColoringData data : allColoringData)
                {
                    String units = data.getUnits();
                    for (String name : data.getFieldNames())
                    {
                        out.write("," + name);
                        if (units != null && !units.isEmpty())
                            out.write(" (" + units + ")");
                    }

                }
                out.write(lineSeparator);

                vtkTriangle triangle = new vtkTriangle();

                vtkPoints points = smallBodyPolyData.GetPoints();
                int numberCells = smallBodyPolyData.GetNumberOfCells();
                smallBodyPolyData.BuildCells();
                vtkIdList idList = new vtkIdList();

                for (int index = 0; index < indexable.size(); ++index)
                {
                    int cellId = indexable.get(index);
                    FacetColoringData facetData = new FacetColoringData(cellId, allColoringData);
                    facetData.generateDataFromPolydata(smallBodyPolyData, numberCells, triangle, points, idList);
                    facetData.writeTo(out);
                    out.write(lineSeparator);
                }
                triangle.Delete();
                idList.Delete();
            }
        }
    }

    private FacetColoringData[] getColoringDataFor(Indexable<Integer> indexable)
    {
        FacetColoringData[] data = new FacetColoringData[indexable.size()];
        ImmutableList<ColoringData> allColoringData = getAllColoringData();
        for (int index = 0; index < indexable.size(); ++index)
        {
            int cellId = indexable.get(index);
            FacetColoringData facetData = new FacetColoringData(cellId, allColoringData);
            facetData.generateDataFromPolydata(smallBodyPolyData);
            data[index] = facetData;
        }
        return data;
    }

}

package edu.jhuapl.saavtk.model;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.colormap.Colormap;
import edu.jhuapl.saavtk.colormap.Colormaps;
import edu.jhuapl.saavtk.config.ViewConfig;
import edu.jhuapl.saavtk.metadata.serialization.Serializers;
import edu.jhuapl.saavtk.util.BoundingBox;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.ConvertResourceToFile;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.Frustum;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.Preferences;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.util.SaavtkLODActor;
import edu.jhuapl.saavtk.util.SafePaths;
import edu.jhuapl.saavtk.util.SmallBodyCubes;
import vtk.vtkActor;
import vtk.vtkActor2D;
import vtk.vtkCell;
import vtk.vtkCellArray;
import vtk.vtkCellData;
import vtk.vtkCellDataToPointData;
import vtk.vtkContourFilter;
import vtk.vtkCoordinate;
import vtk.vtkDataArray;
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

public class GenericPolyhedralModel extends PolyhedralModel implements PropertyChangeListener
{
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
	private vtkUnsignedCharArray colorData;
	private vtkUnsignedCharArray falseColorArray;

	private List<LidarDatasourceInfo> lidarDatasourceInfo = new ArrayList<LidarDatasourceInfo>();
	private int lidarDatasourceIndex = -1;

	private vtkPolyData smallBodyPolyData;
	private vtkPolyData lowResSmallBodyPolyData;
	private vtkActor smallBodyActor;
	private vtkPolyDataMapper smallBodyMapper;

	private List<vtkProp> smallBodyActors = new ArrayList<vtkProp>();

	public List<vtkProp> getSmallBodyActors()
	{
		return smallBodyActors;
	}

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
	private String[] modelFiles;

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

	vtkIdTypeArray cellIds = new vtkIdTypeArray();
	public static final String cellIdsArrayName = "cellIds";

	private Colormap colormap = null;
	private boolean colormapInitialized = false;
	private boolean showColorsAsContourLines = false;
	private double contourLineWidth = 1;
	private vtkPolyDataMapper linesMapper;
	private vtkActor linesActor;

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

		setSmallBodyPolyData(polyData, coloringValues, coloringNames, coloringUnits, coloringValueType);
	}

	/**
	 * Note that name is used to name this small body model as a whole including all
	 * resolution levels whereas modelNames is an array of names that is specific
	 * for each resolution level.
	 */
	public GenericPolyhedralModel(ViewConfig config, String[] modelNames, String[] modelFiles, String[] coloringFiles, String[] coloringNames, String[] coloringUnits, boolean[] coloringHasNulls, String[] imageMapNames, ColoringValueType coloringValueType, boolean lowestResolutionModelStoredInResource)
	{
		super(config);
		this.coloringDataManager = CustomizableColoringDataManager.of(config.getUniqueName());
		initializeColoringDataManager(coloringDataManager, config.getResolutionNumberElements(), coloringFiles, coloringNames, coloringUnits, coloringHasNulls);
		this.modelNames = modelNames;
		this.modelFiles = modelFiles;
		setDefaultModelFileName(this.modelFiles[0]);
		this.imageMapNames = imageMapNames;
		this.coloringValueType = coloringValueType;

		colorData = new vtkUnsignedCharArray();
		smallBodyPolyData = new vtkPolyData();
		genericCell = new vtkGenericCell();
		idList = new vtkIdList();

		if (Configuration.useFileCache())
		{
			if (lowestResolutionModelStoredInResource)
				defaultModelFile = ConvertResourceToFile.convertResourceToRealFile(this, modelFiles[0], Configuration.getApplicationDataDir());
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

	private static final String COLORING_METADATA_ID = "Coloring Metadata";

	private static void initializeColoringDataManager(CustomizableColoringDataManager coloringDataManager, ImmutableList<Integer> numberElements, String[] coloringFiles, String[] coloringNames, String[] coloringUnits, boolean[] coloringHasNulls)
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
		metadataFileName = SafePaths.getString(SafePaths.get(coloringFiles[0]).toFile().getParent(), metadataFileName);
		if (FileCache.isFileGettable(metadataFileName))
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

		boolean sbmt2style = false;
		for (int index = 0; index < coloringFiles.length; ++index)
		{
			String baseFileName = coloringFiles[index];
			Format fileFormat = guessFormat(baseFileName);
			for (int resolutionLevel = 0; resolutionLevel < numberElements.size(); ++resolutionLevel)
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
					coloringDataManager.addBuiltIn(ColoringData.of(name, fileName, elementNames, units, numberElements.get(resolutionLevel), hasNulls));
				}
			}
		}

		//		metadataFileName = SafePaths.getString(Configuration.getCacheDir(), metadataFileName);
		//		try
		//		{
		//			Serializers.serialize(COLORING_METADATA_ID, coloringDataManager.getMetadataManager(false).store(), new File(metadataFileName));
		//		}
		//		catch (IOException e)
		//		{
		//			System.err.println("Exception when trying to save plate coloring metadata file");
		//			e.printStackTrace();
		//		}
	}

	private static Format guessFormat(String baseFileName)
	{
		Format fileFormat = baseFileName.matches(".*\\.[Ff][Ii][Tt][Ss]?$") ? Format.FIT : baseFileName.matches(".*//.[Tt][Xx][Tt]$") ? Format.TXT : Format.UNKNOWN;
		if (fileFormat == Format.UNKNOWN)
		{
			fileFormat = baseFileName.matches(".*\\.[Ff][Ii][Tt][Ss]?\\.[Gg][Zz]$") ? Format.FIT : baseFileName.matches(".*//.[Tt][Xx][Tt]\\.[Gg][Zz]$") ? Format.TXT : Format.UNKNOWN;
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

	protected void initializeConfigParameters(String[] modelFiles, String[] coloringFiles, String[] coloringNames, String[] coloringUnits, boolean[] coloringHasNulls, String[] imageMapNames, ColoringValueType coloringValueType, boolean lowestResolutionModelStoredInResource)
	{
		this.modelFiles = modelFiles;
		setDefaultModelFileName(this.modelFiles[0]);
		this.imageMapNames = imageMapNames;
		this.coloringValueType = coloringValueType;
		initializeColoringDataManager(coloringDataManager, getConfig().getResolutionNumberElements(), coloringFiles, coloringNames, coloringUnits, coloringHasNulls);

		colorData = new vtkUnsignedCharArray();
		smallBodyPolyData = new vtkPolyData();
		genericCell = new vtkGenericCell();
		idList = new vtkIdList();

		if (Configuration.useFileCache())
		{
			if (lowestResolutionModelStoredInResource)
				defaultModelFile = ConvertResourceToFile.convertResourceToRealFile(this, modelFiles[0], Configuration.getApplicationDataDir());
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
	public void setColormap(Colormap colormap)
	{
		if (this.colormap != null)
			this.colormap.removePropertyChangeListener(this);
		initializeActorsAndMappers();
		this.colormap = colormap;
		if (coloringIndex != -1)
		{
			double[] range = getColoringData(coloringIndex).getData().GetRange();
			colormap.setRangeMin(range[0]);
			colormap.setRangeMax(range[1]);
		}
		this.colormap.addPropertyChangeListener(this);
		smallBodyActor.GetMapper().SetLookupTable(colormap.getLookupTable());
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
	public Colormap getColormap()
	{
		return colormap;
	}

	protected void initColormap()
	{
		if (colormap == null)
			colormap = Colormaps.getNewInstanceOfBuiltInColormap(Colormaps.getDefaultColormapName());
		if (!colormapInitialized && !(colormap == null) && coloringIndex > -1)
		{
			double[] range = getCurrentColoringRange(coloringIndex);
			colormap.removePropertyChangeListener(this);
			colormap.setRangeMin(range[0]);
			colormap.setRangeMax(range[1]);
			colormapInitialized = true;
			colormap.addPropertyChangeListener(this);
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
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getSource().equals(colormap))
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
	public List<vtkPolyData> getSmallBodyPolyDatas()
	{
		return null;
	}

	public void setDefaultModelFileName(String defaultModelFileName)
	{
		this.defaultModelFileName = defaultModelFileName;
		defaultModelFile = new File(defaultModelFileName);
	}

	public void setSmallBodyPolyData(vtkPolyData polydata, vtkFloatArray[] coloringValues, String[] coloringNames, String[] coloringUnits, ColoringValueType coloringValueType)
	{
		if (polydata != null)
		{
			smallBodyPolyData.DeepCopy(polydata);
		}
		coloringDataManager.clearCustom();
		for (int i = 0; i < coloringNames.length; ++i)
		{
			int numberElements = coloringValues[i].GetNumberOfTuples();
			ImmutableList<String> elementNames = ImmutableList.of(coloringNames[i]);
			coloringDataManager.addCustom(ColoringData.of(coloringNames[i], elementNames, coloringUnits[i], numberElements, false, coloringValues[i]));
		}
		this.coloringValueType = coloringValueType;

		initializeLocators();
		initializeCellIds();

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
	public String getDEMConfigFilename()
	{
		return getCustomDataFolder() + File.separator + "demConfig.txt";
	}

	private void clearCustomColoringInfo()
	{
		coloringDataManager.clearCustom();
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
		clearCustomColoringInfo();

		String configFilename = getConfigFilename();

		if (!(new File(configFilename).exists()))
			return;

		MapUtil configMap = new MapUtil(configFilename);

		convertOldConfigFormatToNewVersion(configMap);

		if (configMap.containsKey(GenericPolyhedralModel.CELL_DATA_FILENAMES) && configMap.containsKey(GenericPolyhedralModel.CELL_DATA_NAMES) && configMap.containsKey(GenericPolyhedralModel.CELL_DATA_UNITS) && configMap.containsKey(GenericPolyhedralModel.CELL_DATA_HAS_NULLS))
		{
			String[] cellDataFilenames = configMap.get(GenericPolyhedralModel.CELL_DATA_FILENAMES).split(",", -1);
			String[] cellDataNames = configMap.get(GenericPolyhedralModel.CELL_DATA_NAMES).split(",", -1);
			String[] cellDataUnits = configMap.get(GenericPolyhedralModel.CELL_DATA_UNITS).split(",", -1);
			String[] cellDataHasNulls = configMap.get(GenericPolyhedralModel.CELL_DATA_HAS_NULLS).split(",", -1);
			String[] cellDataResolutionLevels = null;
			if (configMap.containsKey(GenericPolyhedralModel.CELL_DATA_RESOLUTION_LEVEL))
				cellDataResolutionLevels = configMap.get(GenericPolyhedralModel.CELL_DATA_RESOLUTION_LEVEL).split(",", -1);

			for (int i = 0; i < cellDataFilenames.length; ++i)
			{
				String coloringFile = cellDataFilenames[i];
				if (!coloringFile.trim().isEmpty())
				{
					coloringFile = FileCache.FILE_PREFIX + getCustomDataFolder() + "/" + coloringFile;
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
					coloringDataManager.addCustom(ColoringData.of(coloringName, coloringFile, ImmutableList.of(coloringName), coloringUnits, customNumberElements, coloringHasNulls));
				}
			}
		}

		// Now that we're done loading the custom colors, see if it's possible again to select the original selection.
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

	private void clearCustomLidarDatasourceInfo()
	{
		for (int i = lidarDatasourceInfo.size() - 1; i >= 0; --i)
		{
			lidarDatasourceInfo.remove(i);
		}
		lidarDatasourceIndex = -1;
	}

	@Override
	public void loadCustomLidarDatasourceInfo()
	{
		String prevLidarDatasourceName = null;
		String prevLidarDatasourcePath = null;
		lidarDatasourceInfo = new ArrayList<LidarDatasourceInfo>();

		if (lidarDatasourceIndex >= 0 && lidarDatasourceIndex < lidarDatasourceInfo.size())
		{
			prevLidarDatasourceName = lidarDatasourceInfo.get(lidarDatasourceIndex).name;
			prevLidarDatasourcePath = lidarDatasourceInfo.get(lidarDatasourceIndex).path;
		}

		clearCustomLidarDatasourceInfo();

		String configFilename = getConfigFilename();

		if (!(new File(configFilename).exists()))
			return;

		MapUtil configMap = new MapUtil(configFilename);

		convertOldConfigFormatToNewVersion(configMap);

		if (configMap.containsKey(GenericPolyhedralModel.LIDAR_DATASOURCE_NAMES) && configMap.containsKey(GenericPolyhedralModel.LIDAR_DATASOURCE_NAMES))
		{
			String[] lidarDatasourceNames = configMap.get(GenericPolyhedralModel.LIDAR_DATASOURCE_NAMES).split(",", -1);
			String[] lidarDatasourcePaths = configMap.get(GenericPolyhedralModel.LIDAR_DATASOURCE_PATHS).split(",", -1);

			for (int i = 0; i < lidarDatasourceNames.length; ++i)
			{
				LidarDatasourceInfo info = new LidarDatasourceInfo();
				info.name = lidarDatasourceNames[i];
				info.path = lidarDatasourcePaths[i];
				if (!info.path.trim().isEmpty() && !info.name.trim().isEmpty())
				{
					info.name = lidarDatasourceNames[i];
					info.path = lidarDatasourcePaths[i];
				}
				lidarDatasourceInfo.add(info);
			}
		}

		// See if there's a Lidar datasource of the same name as previously shown and set it to that.
		lidarDatasourceIndex = -1;
		for (int i = 0; i < lidarDatasourceInfo.size(); ++i)
		{
			if (prevLidarDatasourceName != null && prevLidarDatasourceName.equals(lidarDatasourceInfo.get(i).name))
			{
				lidarDatasourceIndex = i;
				break;
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
				loadCustomLidarDatasourceInfo();
			}

			smallBodyPolyData.ShallowCopy(PolyDataUtil.loadShapeModel(modelFile.getAbsolutePath()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		initializeLocators();
		initializeCellIds();

		this.computeShapeModelStatistics();

		//this.computeLargestSmallestEdgeLength();
		//this.computeSurfaceArea();
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

	public SmallBodyCubes getSmallBodyCubes()
	{
		if (smallBodyCubes == null)
		{
			// Compute bounding box diagonal length of lowest res shape model
			double cubeSize;
			if (getConfig().hasCustomBodyCubeSize)
			{
				// Custom specified cube size
				cubeSize = getConfig().customBodyCubeSize;
			}
			else
			{
				// Old way of determining cube size, most models still use this
				double diagonalLength =
						new BoundingBox(getLowResSmallBodyPolyData().GetBounds()).getDiagonalLength();
				// The number 38.66056033363347 is used here so that the cube size
				// comes out to 1 km for Eros (whose diagonaLength equals 38.6605...).
				cubeSize = diagonalLength / 38.66056033363347;
			}

			// Generate cubes based on chosen resolution
			smallBodyCubes = new SmallBodyCubes(getLowResSmallBodyPolyData(), cubeSize, 0.01 * cubeSize, true);
		}

		return smallBodyCubes;
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

	public vtkPolyData computeFrustumIntersection(double[] origin, double[] ul, double[] ur, double[] lr, double[] ll)
	{
		return PolyDataUtil.computeFrustumIntersection(smallBodyPolyData, cellLocator, pointLocator, origin, ul, ur, lr, ll);
	}

	public vtkPolyData computeMultipleFrustumIntersection(List<Frustum> frustums)
	{
		return PolyDataUtil.computeMultipleFrustumIntersection(smallBodyPolyData, cellLocator, pointLocator, frustums);
	}

	/**
	 * Given 2 points on the surface of the body, draw a nice looking path between
	 * the 2 that is not obscured anywhere or too distant from the surface. Return
	 * this path as a vtkPolyData
	 * 
	 * @param pt1
	 * @param pt2
	 * @return
	 */
	@Override
	public vtkPolyData drawPath(double[] pt1, double[] pt2)
	{
		return PolyDataUtil.drawPathOnPolyData(smallBodyPolyData, pointLocator, pt1, pt2);
	}

	@Override
	public void drawPolygon(List<LatLon> controlPoints, vtkPolyData outputInterior, vtkPolyData outputBoundary)
	{
		PolyDataUtil.drawPolygonOnPolyData(smallBodyPolyData, pointLocator, controlPoints, outputInterior, outputBoundary);
	}

	public void drawRegularPolygon(double[] center, double radius, int numberOfSides, vtkPolyData outputInterior, vtkPolyData outputBoundary)
	{
		PolyDataUtil.drawRegularPolygonOnPolyData(smallBodyPolyData, pointLocator, center, radius, numberOfSides, outputInterior, outputBoundary);
	}

	@Override
	public void drawEllipticalPolygon(double[] center, double radius, double flattening, double angle, int numberOfSides, vtkPolyData outputInterior, vtkPolyData outputBoundary)
	{
		PolyDataUtil.drawEllipseOnPolyData(smallBodyPolyData, pointLocator, center, radius, flattening, angle, numberOfSides, outputInterior, outputBoundary);
	}

	@Override
	public void drawRegularPolygonLowRes(double[] center, double radius, int numberOfSides, vtkPolyData outputInterior, vtkPolyData outputBoundary)
	{
		if (resolutionLevel == 0)
		{
			drawRegularPolygon(center, radius, numberOfSides, outputInterior, outputBoundary);
			return;
		}

		initializeLowResData();

		PolyDataUtil.drawRegularPolygonOnPolyData(lowResSmallBodyPolyData, lowResPointLocator, center, radius, numberOfSides, outputInterior, outputBoundary);
	}

	public void drawCone(double[] vertex, double[] axis, double angle, int numberOfSides, vtkPolyData outputInterior, vtkPolyData outputBoundary)
	{
		PolyDataUtil.drawConeOnPolyData(smallBodyPolyData, pointLocator, vertex, axis, angle, numberOfSides, outputInterior, outputBoundary);
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

		// Use FindClosestPoint rather the FindCell since not sure what tolerance to use in the latter.
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

			//smallBodyActor = new vtkActor();
			smallBodyActor = new SaavtkLODActor();
			smallBodyActor.SetMapper(smallBodyMapper);

			vtkPolyDataMapper decimatedMapper =
					((SaavtkLODActor) smallBodyActor).setQuadricDecimatedLODMapper(smallBodyPolyData);
			decimatedMapper.SetLookupTable(lookupTable);
			decimatedMapper.UseLookupTableScalarRangeOn();
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

			linesMapper = new vtkPolyDataMapper();
			linesActor = new SaavtkLODActor();
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
	public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
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

		// Repaint the asteroid if we're currently showing any type of coloring
		if (coloringIndex >= 0 || useFalseColoring)
			paintBody();

		this.pcs.firePropertyChange(Properties.MODEL_RESOLUTION_CHANGED, null, null);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
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
			return FileCache.isFileGettable(modelFiles[resolutionLevel]);
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
	public String getLidarDatasourceName(int i)
	{
		if (i < 0)
			return "Default";
		if (i < lidarDatasourceInfo.size())
			return lidarDatasourceInfo.get(i).name;
		else
			return null;
	}

	@Override
	public String getLidarDatasourcePath(int i)
	{
		if (i < 0)
			return "/NLR/cubes";
		if (i < lidarDatasourceInfo.size())
			return lidarDatasourceInfo.get(i).path;
		else
			return null;
	}

	@Override
	public int getLidarDatasourceIndex()
	{
		return lidarDatasourceIndex;
	}

	@Override
	public void setLidarDatasourceIndex(int index)
	{
		if (lidarDatasourceIndex != index)
		{
			lidarDatasourceIndex = index;
		}
	}

	@Override
	public int getNumberOfLidarDatasources()
	{
		return lidarDatasourceInfo.size();
	}

	/**
	 * Load just the current plate coloring identified by coloringIndex.
	 * 
	 * @throws IOException if the plate coloring fails to load
	 */
	protected void loadColoringData() throws IOException
	{
		ColoringData coloringData = getColoringData(coloringIndex);
		coloringData.load();
	}

	private static String getColoringFileName(String baseFileName, int resolutionLevel, Format format, boolean sbmt2Style)
	{
		String fileName = null;
		if (!baseFileName.startsWith(FileCache.FILE_PREFIX))
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
			fileName = FileCache.isFileGettable(baseFileName) ? baseFileName : null;
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
			if (FileCache.isFileGettable(fileName))
			{
				return fileName;
			}
			fileName = baseFileName + ".fits.gz";
			break;
		case FIT:
		case UNKNOWN:
			fileName = baseFileName + ".fits.gz";
			if (FileCache.isFileGettable(fileName))
			{
				return fileName;
			}
			fileName = baseFileName + ".txt.gz";
			break;
		default:
			throw new AssertionError("Unhandled case " + format);
		}
		return FileCache.isFileGettable(fileName) ? fileName : null;
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
			data.load();
		}
	}

	private void invertLookupTableCharArray(vtkUnsignedCharArray table)
	{
		int numberOfValues = table.GetNumberOfTuples();
		for (int i = 0; i < numberOfValues / 2; ++i)
		{
			double[] v1 = table.GetTuple4(i);
			double[] v2 = table.GetTuple4(numberOfValues - i - 1);
			table.SetTuple4(i, v2[0], v2[1], v2[2], v2[3]);
			table.SetTuple4(numberOfValues - i - 1, v1[0], v1[1], v1[2], v1[3]);
		}
	}

	/**
	 * Invert the lookup table so that red is high values and blue is low values
	 * (rather than the reverse).
	 */
	private void invertLookupTable()
	{
		vtkLookupTable lookupTable = (vtkLookupTable) smallBodyMapper.GetLookupTable();
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
				double[] range = getCurrentColoringRange(coloringIndex);
				if (colormap == null)
					initColormap();
				colormap.setRangeMin(range[0]);
				colormap.setRangeMax(range[1]);
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

		if (isColoringAvailable(redFalseColor) || isColoringAvailable(greenFalseColor) || isColoringAvailable(blueFalseColor))
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
	public boolean isImageMapAvailable()
	{
		return imageMapNames != null && imageMapNames.length > 0;
	}

	@Override
	public String[] getImageMapNames()
	{
		return imageMapNames;
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

	private double getScalarValue(double[] pt, vtkFloatArray pointOrCellData)
	{
		double[] closestPoint = new double[3];
		int cellId = findClosestCell(pt, closestPoint);
		return getScalarValue(closestPoint, pointOrCellData, cellId);
	}

	/**
	 * Get value assuming pt is exactly on the asteroid and cellId is provided
	 * 
	 * @param pt
	 * @param pointOrCellData
	 * @param cellId
	 * @return
	 */
	private double getScalarValue(double[] pt, vtkFloatArray pointOrCellData, int cellId)
	{
		if (coloringValueType == ColoringValueType.POINT_DATA)
		{
			return PolyDataUtil.interpolateWithinCell(smallBodyPolyData, pointOrCellData, cellId, pt, idList);
		}
		else
		{
			return pointOrCellData.GetTuple1(cellId);
		}
	}

	private double[] getVectorValue(double[] pt, vtkFloatArray pointOrCellData, int numberAxes)
	{
		double[] closestPoint = new double[3];
		int cellId = findClosestCell(pt, closestPoint);
		return getVectorValue(closestPoint, pointOrCellData, cellId);
	}

	private double[] getVectorValue(double[] pt, vtkFloatArray pointOrCellData, int cellId, int numberAxes)
	{
		double[] result = null;
		if (coloringValueType == ColoringValueType.POINT_DATA)
		{
			result = PolyDataUtil.interpolateWithinCell(smallBodyPolyData, pointOrCellData, cellId, pt, idList, numberAxes);
		}
		else
		{
			if (numberAxes == 1)
			{
				result = new double[] { pointOrCellData.GetTuple1(cellId) };
			}
			else if (numberAxes == 2)
			{
				result = pointOrCellData.GetTuple2(cellId);
			}
			else if (numberAxes == 3)
			{
				result = pointOrCellData.GetTuple3(cellId);
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
		try
		{
			coloringData.load();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		return getScalarValue(pt, coloringData.getData());
	}

	@Override
	public ImmutableList<ColoringData> getAllColoringData()
	{
		ImmutableList<Integer> resolutions = coloringDataManager.getResolutions();
		return resolutions.size() > resolutionLevel ? coloringDataManager.get(resolutions.get(resolutionLevel)) : ImmutableList.of();
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
			numColorColumns += data.getElementNames().size();
		}

		double[] result = new double[numColorColumns];
		int valueIndex = 0;
		for (ColoringData data : coloringData)
		{
			double[] coloringVector = getVectorValue(closestPoint, data.getData(), cellId, data.getElementNames().size());
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
		return getColoringData(coloringIndex).getDefaultRange();
	}

	@Override
	public double[] getCurrentColoringRange(int coloringIndex)
	{
		// Delegate to default color range
		if (colormap == null)
			return getDefaultColoringRange(coloringIndex);

		return new double[] { colormap.getRangeMin(), colormap.getRangeMax() };
	}

	@Override
	public void setCurrentColoringRange(int coloringIndex, double[] range) throws IOException
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

			paintBody();
		}
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
			coloringData.load();
			int size = coloringData.getNumberElements();

			final vtkFloatArray floatArray = coloringData.getData();
			final double[] range = floatArray.GetRange();
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
					return scale * (floatArray.GetTuple1(index) - range[0]) / extent;
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

	private void loadLidarDatasourceData() throws IOException
	{}

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
			throw new RuntimeException("Coloring number " + coloringIndex + " is not available for resolution level " + resolutionLevel);
		}
		String name = coloringDataManager.getNames().get(coloringIndex);
		int numberElements = coloringDataManager.getResolutions().get(resolutionLevel);
		return coloringDataManager.get(name, numberElements);
	}

	private void paintBody() throws IOException
	{

		initializeActorsAndMappers();

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
			scalarBarActor.SetTitle(title);

			vtkFloatArray floatArray = coloringData.getData();

			initColormap();
			if (!showColorsAsContourLines)
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

				vtkPolyDataMapper decimatedMapper =
						((SaavtkLODActor) smallBodyActor).setQuadricDecimatedLODMapper(smallBodyPolyData);
				decimatedMapper.SetLookupTable(colormap.getLookupTable());
				decimatedMapper.UseLookupTableScalarRangeOn();

				if (coloringValueType == ColoringValueType.POINT_DATA)
					this.smallBodyPolyData.GetPointData().SetScalars(rgbColorData);
				else
					this.smallBodyPolyData.GetCellData().SetScalars(rgbColorData);

				smallBodyMapper.ScalarVisibilityOn();

			}
			else
			{
				vtkPolyDataMapper decimatedMapper =
						((SaavtkLODActor) smallBodyActor).setQuadricDecimatedLODMapper(smallBodyPolyData);
				decimatedMapper.ScalarVisibilityOff();
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
					polyData = converter.GetPolyDataOutput(); // contour filter requires point data with one component
				}

				vtkContourFilter contourFilter = new vtkContourFilter();
				contourFilter.SetInputData(polyData);
				contourFilter.GenerateValues(colormap.getNumberOfLevels(), colormap.getRangeMin(), colormap.getRangeMax());
				contourFilter.Update();

				linesMapper =
						((SaavtkLODActor) linesActor).setQuadricDecimatedLODMapper(contourFilter.GetOutput());

				linesMapper.SetInputData(contourFilter.GetOutput());
				linesMapper.ScalarVisibilityOn();
				linesMapper.SetScalarModeToDefault();
				linesMapper.SetLookupTable(colormap.getLookupTable());
				linesMapper.UseLookupTableScalarRangeOn();

				linesActor.VisibilityOn();
				linesActor.SetMapper(linesMapper);
				linesActor.GetProperty().SetLineWidth(contourLineWidth);

				if (!smallBodyActors.contains(linesActor))
					smallBodyActors.add(linesActor);
			}
		}
		else
		{
			if (smallBodyActors.contains(scalarBarActor))
				smallBodyActors.remove(scalarBarActor);
			if (smallBodyActors.contains(linesActor))
				smallBodyActors.remove(linesActor);

			if (useFalseColoring)
			{
				if (isColoringAvailable(redFalseColor) || isColoringAvailable(greenFalseColor) || isColoringAvailable(blueFalseColor))
				{

					updateFalseColorArray();
					if (coloringValueType == ColoringValueType.POINT_DATA)
						this.smallBodyPolyData.GetPointData().SetScalars(falseColorArray);
					else
						this.smallBodyPolyData.GetCellData().SetScalars(falseColorArray);
					smallBodyMapper.ScalarVisibilityOn();
					vtkPolyDataMapper decimatedMapper =
							((SaavtkLODActor) smallBodyActor).setQuadricDecimatedLODMapper(smallBodyPolyData);

				}
			}
			else
			{
				vtkPolyDataMapper decimatedMapper =
						((SaavtkLODActor) smallBodyActor).setQuadricDecimatedLODMapper(smallBodyPolyData);
				decimatedMapper.ScalarVisibilityOff();
				smallBodyMapper.ScalarVisibilityOff();
			}
		}

		this.smallBodyPolyData.Modified();

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
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
		//if (lowResPointLocator != null) lowResPointLocator.Delete();
		if (genericCell != null)
			genericCell.Delete();
		if (scalarBarActor != null)
			scalarBarActor.Delete();
		if (smallBodyPolyData != null)
			smallBodyPolyData.Delete();
		//if (lowResSmallBodyPolyData != null) lowResSmallBodyPolyData.Delete();
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
		for (int i = 0; i < 4; ++i)
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

	@Override
	public void updateScaleBarPosition(int windowWidth, int windowHeight)
	{
		vtkPoints points = scaleBarPolydata.GetPoints();

		int newScaleBarWidthInPixels = (int) Math.min(0.75 * windowWidth, 150.0);

		scaleBarWidthInPixels = newScaleBarWidthInPixels;
		int scaleBarHeight = scaleBarWidthInPixels / 9;
		int buffer = scaleBarWidthInPixels / 20;
		int x = windowWidth - scaleBarWidthInPixels - buffer; // lower left corner x
		int y = buffer; // lower left corner y

		points.SetPoint(0, x, y, 0.0);
		points.SetPoint(1, x + scaleBarWidthInPixels, y, 0.0);
		points.SetPoint(2, x + scaleBarWidthInPixels, y + scaleBarHeight, 0.0);
		points.SetPoint(3, x, y + scaleBarHeight, 0.0);

		scaleBarTextActor.SetPosition(x + scaleBarWidthInPixels / 2, y + 2);
		scaleBarTextActor.GetTextProperty().SetFontSize(scaleBarHeight - 4);

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public void updateScaleBarValue(double pixelSizeInKm)
	{
		if (scaleBarWidthInPixels <= 0 || scaleBarWidthInKm == scaleBarWidthInPixels * pixelSizeInKm)
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
				scaleBarTextActor.SetInput(String.format("%.2f m", 1000.0 * scaleBarWidthInKm));
			else
				scaleBarTextActor.SetInput(String.format("%.2f km", scaleBarWidthInKm));
		}

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public void setShowScaleBar(boolean enabled)
	{
		this.showScaleBar = enabled;
		// The following forces the scale bar to be redrawn.
		scaleBarWidthInKm = -1.0;
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		// Note that we call firePropertyChange *twice*. Not really sure why.
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public boolean getShowScaleBar()
	{
		return showScaleBar;
	}

	@Override
	public void saveAsPLT(File file) throws IOException
	{
		PolyDataUtil.saveShapeModelAsPLT(smallBodyPolyData, file.getAbsolutePath());
	}

	@Override
	public void saveAsOBJ(File file) throws IOException
	{
		PolyDataUtil.saveShapeModelAsOBJ(smallBodyPolyData, file.getAbsolutePath());
	}

	@Override
	public void saveAsVTK(File file) throws IOException
	{
		PolyDataUtil.saveShapeModelAsVTK(smallBodyPolyData, file.getAbsolutePath());
	}

	@Override
	public void saveAsSTL(File file) throws IOException
	{
		PolyDataUtil.saveShapeModelAsSTL(smallBodyPolyData, file.getAbsolutePath());
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
			ColoringData gravColoring = coloringDataManager.get(GravPotStr, coloringDataManager.getResolutions().get(resolutionLevel));
			gravColoring.load();
			vtkFloatArray floatArray = gravColoring.getData();

			double potTimesAreaSum = 0.0;
			double totalArea = 0.0;
			double minRefPot = Double.MAX_VALUE;
			int numFaces = smallBodyPolyData.GetNumberOfCells();
			for (int i = 0; i < numFaces; ++i)
			{
				double potential = floatArray.GetTuple1(i);
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

	private ImmutableList<Integer> getClosestCellList(vtkPolyData polydata)
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
	public void addCustomLidarDatasource(LidarDatasourceInfo info) throws IOException
	{
		lidarDatasourceInfo.add(info);
	}

	@Override
	public void setCustomLidarDatasource(int index, LidarDatasourceInfo info) throws IOException
	{
		lidarDatasourceInfo.set(index, info);
	}

	@Override
	public void removeCustomLidarDatasource(int index) throws IOException
	{
		lidarDatasourceInfo.remove(index);

		if (lidarDatasourceIndex == index)
			lidarDatasourceIndex = -1;
		else if (lidarDatasourceIndex > index)
			--lidarDatasourceIndex;
	}

	public void reloadLidarDatasources() throws IOException
	{
		for (LidarDatasourceInfo info : lidarDatasourceInfo)
		{
			info.name = null;
			info.path = null;
		}

		loadLidarDatasourceData();
	}

	@Override
	public List<LidarDatasourceInfo> getLidarDasourceInfoList()
	{
		return lidarDatasourceInfo;
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
					for (String name : data.getElementNames())
					{
						out.write("," + name);
						if (units != null && !units.isEmpty())
							out.write(" (" + units + ")");
					}

				}
				out.write(lineSeparator);

				for (int index = 0; index < indexable.size(); ++index)
				{
					int cellId = indexable.get(index);
					FacetColoringData facetData = new FacetColoringData(cellId, allColoringData);
					facetData.generateDataFromPolydata(smallBodyPolyData);
					facetData.writeTo(out);
					out.write(lineSeparator);
				}
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

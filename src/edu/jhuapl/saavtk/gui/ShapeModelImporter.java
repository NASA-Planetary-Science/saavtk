package edu.jhuapl.saavtk.gui;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import org.apache.commons.io.FileUtils;

import edu.jhuapl.saavtk.io.readers.StlReader;
import edu.jhuapl.saavtk.model.ShapeModel;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.MapUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import vtk.vtkPolyData;
import vtk.vtkPolyDataWriter;
import vtk.vtkSphereSource;
import vtk.vtkTransform;
import vtk.vtkTransformPolyDataFilter;

public class ShapeModelImporter
{
    /**
     * The file format of the shape model being imported
     */
    public enum FormatType
    {
        PDS,
        OBJ,
        VTK,
        FIT,
        FITS,
        STL
    }

    /**
     * The type of shape model. If ELLIPSOID, and ellipsoid is generated
     * by this classes using the radii and resolution specified. If
     * FILE, the shape model is loaded from the file specified. If
     * POLYDATA, the specified vtkPolyData object containing the shape model
     * is used.
     */
    public enum ShapeModelType
    {
        ELLIPSOID,
        FILE,
        POLYDATA
    }

    private String name;
    private ShapeModelType shapeModelType = ShapeModelType.FILE;
    private double equatorialRadiusX;
    private double equatorialRadiusY;
    private double polarRadius;
    private int resolution;
    private String modelPath;
    private FormatType format;
    private vtkPolyData shapeModelPolydata;

    public boolean importShapeModel(String[] errorMessage, boolean edit)
    {
        String validationErrorMessage = validateInput(edit);
        if (validationErrorMessage != null)
        {
            errorMessage[0] = validationErrorMessage;
            return false;
        }

        LinkedHashMap<String, String> configMap = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> copyMap = new LinkedHashMap<String, String>();
        
        configMap.put(ShapeModel.NAME, name);

        vtkPolyData shapePoly = null;

        File newModelDir = new File(Configuration.getImportedShapeModelsDir() + File.separator + name);
        
        // First either load a shape model from file or create ellipsoidal shape model
        if (shapeModelType == ShapeModelType.ELLIPSOID)
        {
            vtkSphereSource sphereSource = new vtkSphereSource();
            sphereSource.SetRadius(equatorialRadiusX);
            sphereSource.SetCenter(0.0, 0.0, 0.0);
            sphereSource.SetLatLongTessellation(0);
            sphereSource.SetThetaResolution(resolution);
            sphereSource.SetPhiResolution(Math.max(3, resolution/2 + 1));
            sphereSource.Update();
            shapePoly = sphereSource.GetOutput();

            if (equatorialRadiusX != polarRadius || equatorialRadiusX != equatorialRadiusY)
            {
                // Turn it into ellipsoid
                vtkTransformPolyDataFilter filter = new vtkTransformPolyDataFilter();
                filter.SetInputConnection(sphereSource.GetOutputPort());

                vtkTransform transform = new vtkTransform();
                transform.Scale(1.0, equatorialRadiusY/equatorialRadiusX, polarRadius/equatorialRadiusX);

                filter.SetTransform(transform);
                filter.Update();

                shapePoly.Delete();
                shapePoly = filter.GetOutput();
            }

            configMap.put(ShapeModel.TYPE, ShapeModel.ELLIPSOID);
            configMap.put(ShapeModel.EQUATORIAL_RADIUS_X, String.valueOf(equatorialRadiusX));
            configMap.put(ShapeModel.EQUATORIAL_RADIUS_Y, String.valueOf(equatorialRadiusY));
            configMap.put(ShapeModel.POLAR_RADIUS, String.valueOf(polarRadius));
            configMap.put(ShapeModel.RESOLUTION, String.valueOf(resolution));
        }
        else if (shapeModelType == ShapeModelType.POLYDATA)
        {
            shapePoly = new vtkPolyData();
            shapePoly.DeepCopy(shapeModelPolydata);
        }
        else
        {
            configMap.put(ShapeModel.TYPE, ShapeModel.CUSTOM);
            configMap.put(ShapeModel.CUSTOM_SHAPE_MODEL_PATH, modelPath);
            
            // Set format based on file extension from modelPath
            if (modelPath.toLowerCase().endsWith(".pds"))
            	format = FormatType.PDS;
            else if (modelPath.toLowerCase().endsWith(".obj"))
            	format = FormatType.OBJ;
            else if (modelPath.toLowerCase().endsWith(".vtk"))
            	format = FormatType.VTK;
            else if (modelPath.toLowerCase().endsWith(".fits") || modelPath.toLowerCase().endsWith(".fit") || modelPath.toLowerCase().endsWith(".fts"))
            	format = FormatType.FITS;
            else if (modelPath.toLowerCase().endsWith(".stl"))
            	format = FormatType.STL;
            else
            	format = null;
            	
            
            if (format == FormatType.PDS)
            {
                try
                {
                    shapePoly = PolyDataUtil.loadPDSShapeModel(modelPath);
                }
                catch (Exception ex)
                {
                    errorMessage[0] = "There was an error loading " + modelPath + ".\nAre you sure you specified the right format?";
                    return false;
                }

                configMap.put(ShapeModel.CUSTOM_SHAPE_MODEL_FORMAT, ShapeModel.PDS_FORMAT);
            }
            else if (format == FormatType.OBJ)
            {
                try
                {
                    shapePoly = PolyDataUtil.loadOBJShapeModel(modelPath);
                }
                catch (Exception ex)
                {
                    errorMessage[0] = "There was an error loading " + modelPath + ".\nAre you sure you specified the right format?";
                    return false;
                }

                configMap.put(ShapeModel.CUSTOM_SHAPE_MODEL_FORMAT, ShapeModel.OBJ_FORMAT);
            }
            else if (format == FormatType.VTK)
            {
                try
                {
                    shapePoly = PolyDataUtil.loadVTKShapeModel(modelPath);
                }
                catch (Exception ex)
                {
                    errorMessage[0] = "There was an error loading " + modelPath + ".\nAre you sure you specified the right format?";
                    return false;
                }

                configMap.put(ShapeModel.CUSTOM_SHAPE_MODEL_FORMAT, ShapeModel.VTK_FORMAT);
            }
            else if (format == FormatType.FIT || format == FormatType.FITS)
            {
                try
                {
                    // Parse the ALTWG FITs file and turn it into polydata
                    shapePoly = PolyDataUtil.loadFITShapeModel(modelPath);
                    
                    // Make a copy of the FITs file for loading in the future
                    copyMap.put(modelPath, newModelDir.getAbsolutePath() + File.separator + "model.fit");
                }
                catch (Exception ex)
                {
                    errorMessage[0] = "There was an error loading " + modelPath + ".\nAre you sure you specified the right format?";
                    ex.printStackTrace();
                    return false;
                }

                configMap.put(ShapeModel.CUSTOM_SHAPE_MODEL_FORMAT, ShapeModel.FIT_FORMAT);
            }
            else if (format == FormatType.STL)
            {
            	StlReader reader = new StlReader();
            	reader.SetFileName(modelPath);
            	reader.Update();
            	shapePoly = reader.GetOutput();
            }
            else
            {
            	errorMessage[0] = "There was an error loading " + modelPath + ".\nFile extension indicates invalid type.";
            	return false;
            }
        }

        // Now save the shape model to the users home folder within the
        // custom-shape-models folders
        FileUtils.deleteQuietly(newModelDir);
        newModelDir.mkdirs();

        vtkPolyDataWriter writer = new vtkPolyDataWriter();
        writer.SetInputData(shapePoly);
        writer.SetFileName(newModelDir.getAbsolutePath() + File.separator + "model.vtk");
        writer.SetFileTypeToBinary();
        writer.Write();

        if (modelPath != null)
        {
	        File jsonFile = new File(FileUtil.removeExtension(modelPath) + ".json");
	        if (jsonFile.exists())
	        {
		        File jsonFileDestination = new File(newModelDir.getAbsolutePath() + File.separator + "model.json");
		        
		        try {
					FileUtil.copyFile(jsonFile, jsonFileDestination);
				} catch (IOException e) {
		            errorMessage[0] = "There was an error copying " + jsonFile + " to " + jsonFileDestination;
		            e.printStackTrace();
		            return false;
				}    
	        }
	
	        // Copy any files as needed
	        for(String key : copyMap.keySet())
	        {   
	            // Copy the file to the model directory
	            try {
					FileUtil.copyFile(key, copyMap.get(key));
				} catch (IOException e) {
	                errorMessage[0] = "There was an error copying " + modelPath + " to " + copyMap.get(key);
	                return false;
				}
	        }
        }
        
        // Save out all information about this shape model to the config.txt file
        MapUtil map = new MapUtil(newModelDir.getAbsolutePath() + File.separator + "shapeConfig.txt");
        map.put(configMap);

        return true;
    }

    private String validateInput(boolean edit)
    {
        if (name == null || name.trim().isEmpty())
            return "Please enter a name for the shape model.";

        // Make sure name is not empty and does not contain spaces or slashes
        if (name.contains("/") || name.contains("\\") || name.contains(" ") || name.contains("\t"))
            return "Name may not contain spaces or slashes.";

        // Check if name is already being used by another imported shape model.
        // Do not check in edit mode.
        File modelsDir = new File(Configuration.getImportedShapeModelsDir());
        File[] dirs = modelsDir.listFiles();
        if (dirs != null && dirs.length > 0)
        {
            for (File dir : dirs)
            {
                if (dir.getName().equalsIgnoreCase(name)&&edit==false)
                    return "Name already exists.";
            }
        }

        if (shapeModelType == ShapeModelType.ELLIPSOID)
        {
            if (equatorialRadiusX <= 0.0)
                return "Equatorial radius X must be positive.";
            if (equatorialRadiusY <= 0.0)
                return "Equatorial radius Y must be positive.";
            if (polarRadius <= 0.0)
                return "Polar radius must be positive.";
            if (resolution < 3 || resolution > 1024)
                return "Resolution may not be less than 3 or greater than 1024.";
        }
        else if (shapeModelType == ShapeModelType.FILE)
        {
            if (modelPath == null || modelPath.trim().isEmpty())
                return "Please enter the path to a shape model.";

            File file = new File(modelPath);
            if (!file.exists() || !file.canRead() || !file.isFile())
                return modelPath + " does not exist or is not readable.";
        }

        return null;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setShapeModelType(ShapeModelType type)
    {
        this.shapeModelType = type;
    }

    public void setEquRadiusX(double equRadiusX)
    {
        this.equatorialRadiusX = equRadiusX;
    }

    public void setEquRadiusY(double equRadiusY)
    {
        this.equatorialRadiusY = equRadiusY;
    }

    public void setPolarRadius(double polarRadius)
    {
        this.polarRadius = polarRadius;
    }

    public void setResolution(int resolution)
    {
        this.resolution = resolution;
    }

    public void setModelPath(String modelPath)
    {
    	if (modelPath.startsWith("file://")) this.modelPath = modelPath.substring(6);
    	else this.modelPath = modelPath;
    }

    public void setFormat(FormatType format)
    {
        this.format = format;
    }

    public void setShapeModelPolydata(vtkPolyData polydata)
    {
        this.shapeModelPolydata = polydata;
    }

}

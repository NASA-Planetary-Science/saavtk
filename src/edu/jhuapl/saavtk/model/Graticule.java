package edu.jhuapl.saavtk.model;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import vtk.vtkActor;
import vtk.vtkAppendPolyData;
import vtk.vtkCaptionActor2D;
import vtk.vtkCone;
import vtk.vtkCutter;
import vtk.vtkImplicitFunction;
import vtk.vtkPlane;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkPolyDataReader;
import vtk.vtkProp;
import vtk.vtkTransform;
import edu.jhuapl.saavtk.model.structure.OccludingCaptionActor;
import edu.jhuapl.saavtk.util.FileCache;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.Properties;

public class Graticule extends AbstractModel implements PropertyChangeListener
{
    private PolyhedralModel smallBodyModel;
    private List<vtkProp> actors = new ArrayList<vtkProp>();
    private vtkActor actor;
    private vtkPolyDataMapper mapper;
    private vtkAppendPolyData appendFilter;
    private vtkPolyData polyData;
    private vtkPlane plane;
    private vtkCone cone;
    private vtkCutter cutPolyData;
    private vtkTransform transform;
    private vtkPolyDataReader reader;
    private String[] gridFiles;
    private double shiftFactor = 7.0;
    private double longitudeSpacing = 10.;
    private double latitudeSpacing = 10.;
    
	List<vtkCaptionActor2D>		captionActors	= Lists.newArrayList();


    public Graticule(PolyhedralModel smallBodyModel)
    {
        this(smallBodyModel, null);
    }

    public Graticule(PolyhedralModel smallBodyModel, String[] gridFiles)
    {
        if (smallBodyModel != null)
        {
            this.smallBodyModel = smallBodyModel;
            smallBodyModel.addPropertyChangeListener(this);
        }

        this.gridFiles = gridFiles;

        appendFilter = new vtkAppendPolyData();
        plane = new vtkPlane();
        cone = new vtkCone();
        cutPolyData = new vtkCutter();
        transform = new vtkTransform();
        polyData = new vtkPolyData();
        reader = new vtkPolyDataReader();
    }

    public void setShiftFactor(double factor)
    {
        shiftFactor = factor;
    }

    public void generateGrid(vtkPolyData smallBodyPolyData)
    {
    	
    	captionActors.clear();
    	
		int numberLonCircles = (int) (180.0 / longitudeSpacing);
		int numberLatCircles = (int) (90.0 / latitudeSpacing);

		double[] origin = { 0.0, 0.0, 0.0 };
		double[] zaxis = { 0.0, 0.0, 1.0 };

		appendFilter.UserManagedInputsOn();
		appendFilter.SetNumberOfInputs(numberLatCircles + numberLonCircles);
		vtkPolyData[] tmps = new vtkPolyData[numberLatCircles + numberLonCircles];

		cutPolyData.SetInputData(smallBodyPolyData);

		// First do the longitudes.
		for (int i = 0; i < numberLonCircles; ++i)
		{
			double lon = longitudeSpacing * (double) i * Math.PI / 180.0;
			double[] vec = MathUtil.latrec(new LatLon(0.0, lon, 1.0));
			double[] normal = new double[3];
			MathUtil.vcrss(vec, zaxis, normal);

			plane.SetOrigin(origin);
			plane.SetNormal(normal);

			cutPolyData.SetCutFunction(plane);
			cutPolyData.Update();

			tmps[i] = new vtkPolyData();
			tmps[i].DeepCopy(cutPolyData.GetOutput());
			appendFilter.SetInputDataByNumber(i, tmps[i]);
		}

		String fmt = "%.0f";
		String deg = String.valueOf(Character.toChars(0x00B0));

		for (int i = 0; i <= numberLonCircles; i++)
		{
			double lat = 0;
			double lon = longitudeSpacing * (double) i * Math.PI / 180.0;
			// create caption
			double[] intersectPoint = new double[3];
			smallBodyModel.getPointAndCellIdFromLatLon(lat, lon, intersectPoint);
			String captionLabel = String.format(fmt, lon / Math.PI * 180) + deg;
			if (i != 0 && i != numberLonCircles)
				captionLabel += "E";
			vtkCaptionActor2D caption = new OccludingCaptionActor(intersectPoint, null, smallBodyModel);
			caption.SetCaption(captionLabel);
			caption.BorderOff();
			caption.SetAttachmentPoint(intersectPoint);
			captionActors.add(caption);
		}

		for (int i = 1; i < numberLonCircles; i++) // don't do 0
		{
			double lat = 0;
			double lon = longitudeSpacing * (double) i * Math.PI / 180.0;
			// create caption
			double[] intersectPoint = new double[3];
			smallBodyModel.getPointAndCellIdFromLatLon(lat, 2 * Math.PI - lon, intersectPoint);
			String captionLabel = String.format(fmt, lon / Math.PI * 180) + deg + "W";
			vtkCaptionActor2D caption = new OccludingCaptionActor(intersectPoint, null, smallBodyModel);
			caption.SetCaption(captionLabel);
			caption.BorderOff();
			caption.SetAttachmentPoint(intersectPoint);
			captionActors.add(caption);

		}

		double[] yaxis = { 0.0, 1.0, 0.0 };
		transform.Identity();
		transform.RotateWXYZ(90.0, yaxis);
		for (int i = 0; i < numberLatCircles; ++i)
		{
			vtkImplicitFunction cutFunction = null;
			if (i == 0)
			{
				plane.SetOrigin(origin);
				plane.SetNormal(zaxis);
				cutFunction = plane;
			} else
			{
				cone.SetTransform(transform);
				cone.SetAngle(latitudeSpacing * (double) i);
				cutFunction = cone;
			}

			cutPolyData.SetCutFunction(cutFunction);
			cutPolyData.Update();

			int idx = numberLonCircles + i;
			tmps[idx] = new vtkPolyData();
			tmps[idx].DeepCopy(cutPolyData.GetOutput());
			appendFilter.SetInputDataByNumber(idx, tmps[idx]);
		}

		appendFilter.Update();

		polyData.DeepCopy(appendFilter.GetOutput());

		int nlon=3;
		for (double lon = 0; lon < 360; lon += 360./(double)nlon)	// do meridians at nlon intervals from 0
		{
			
			// northern hemisphere
			for (int i = 1; i < numberLatCircles; i++) // don't do 0 since longitude already took care of it
			{
				double lat = latitudeSpacing * (double) i * Math.PI / 180.0;
				// create caption
				double[] intersectPoint = new double[3];
				smallBodyModel.getPointAndCellIdFromLatLon(lat, lon/180*Math.PI, intersectPoint);
				String captionLabel = String.format(fmt, lat / Math.PI * 180) + deg + "N";
				vtkCaptionActor2D caption = new OccludingCaptionActor(intersectPoint, null, smallBodyModel);
				caption.SetCaption(captionLabel);
				caption.BorderOff();
				caption.SetAttachmentPoint(intersectPoint);
				captionActors.add(caption);

			}

			// southern hemisphere
			for (int i = 1; i < numberLatCircles; i++) // don't do 0 since longitude already took care of it
			{
				double lat = latitudeSpacing * (double) i * Math.PI / 180.0;
				// create caption
				double[] intersectPoint = new double[3];
				smallBodyModel.getPointAndCellIdFromLatLon(-lat, lon/180*Math.PI, intersectPoint);
				String captionLabel = String.format(fmt, lat / Math.PI * 180) + deg + "S";
				vtkCaptionActor2D caption = new OccludingCaptionActor(intersectPoint, null, smallBodyModel);
				caption.SetCaption(captionLabel);
				caption.BorderOff();
				caption.SetAttachmentPoint(intersectPoint);
				captionActors.add(caption);

			}
		}
		
		setShowCaptions(false);

    }

	public void setShowCaptions(boolean show)
	{
		for (vtkCaptionActor2D a : captionActors)
		{
			OccludingCaptionActor caption=(OccludingCaptionActor)a;
			caption.setEnabled(show);
			if (!show)
				caption.VisibilityOff();
		}
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

    /**
     * Returns the grid as a vtkPolyData. Note that generateGrid() must be called first.
     * @return
     */
    public vtkPolyData getGridAsPolyData()
    {
        return polyData;
    }

    private void update()
    {
    	
        // There is no need to regenerate the data if generated is true
 

//        if (gridFiles == null)
//        {
            this.generateGrid(smallBodyModel.getSmallBodyPolyData());
/*        }
        else
        {
            int level = smallBodyModel.getModelResolution();


            File modelFile = null;
            switch(level)
            {
            case 1:
                modelFile = FileCache.getFileFromServer(gridFiles[1]);
                break;
            case 2:
                modelFile = FileCache.getFileFromServer(gridFiles[2]);
                break;
            case 3:
                modelFile = FileCache.getFileFromServer(gridFiles[3]);
                break;
            default:
                modelFile = FileCache.getFileFromServer(gridFiles[0]);
                break;
            }

            reader.SetFileName(modelFile.getAbsolutePath());
            reader.Update();

            polyData.DeepCopy(reader.GetOutput());
        }*/

        smallBodyModel.shiftPolyLineInNormalDirection(
                polyData,
                shiftFactor * smallBodyModel.getMinShiftAmount());

        if (mapper == null)
            mapper = new vtkPolyDataMapper();
        mapper.SetInputData(polyData);
        mapper.Update();

        if (actor == null)
            actor = new vtkActor();
        actor.SetMapper(mapper);
        actor.GetProperty().SetColor(0.2, 0.2, 0.2);

    }

    public void setShowGraticule(boolean show)
    {

		if (show == true && actors.size() == 0)
		{
			update();
			actors.add(actor);
			actors.addAll(captionActors);
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		} else if (show == false && actors.size() > 0)
		{
			actors.clear();
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
    }

    public void setLineWidth(double value)
    {
        actor.GetProperty().SetLineWidth(value);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public double getLineWidth()
    {
        return actor.GetProperty().GetLineWidth();
    }

    public void setColor(Color color)
    {
        float[] c = color.getRGBColorComponents(null);
        actor.GetProperty().SetColor(c[0], c[1], c[2]);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public Color getColor()
    {
        double[] c = actor.GetProperty().GetColor();
        return new Color((float)c[0], (float)c[1], (float)c[2]);
    }

    public void setLongitudeSpacing(double longitudeSpacing) {
    	this.longitudeSpacing = longitudeSpacing;
    }

    public double getLongitudeSpacing() {
    	return longitudeSpacing;
    }

    public void setLatitudeSpacing(double latitudeSpacing) {
    	this.latitudeSpacing = latitudeSpacing;
    }

    public double getLatitudeSpacing() {
    	return latitudeSpacing;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
        {
            if (actors.size() > 0)
                update();
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    public List<vtkProp> getProps()
    {
        return actors;
    }

}

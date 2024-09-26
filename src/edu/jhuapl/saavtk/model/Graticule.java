package edu.jhuapl.saavtk.model;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import edu.jhuapl.saavtk.model.structure.OccludingCaptionActor;
import edu.jhuapl.saavtk.util.Configuration;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.Properties;
import vtk.vtkActor;
import vtk.vtkAppendPolyData;
import vtk.vtkCaptionActor2D;
import vtk.vtkCone;
import vtk.vtkCutter;
import vtk.vtkImplicitFunction;
import vtk.vtkPlane;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkTransform;

public class Graticule extends AbstractModel implements PropertyChangeListener
{
    private final PolyhedralModel smallBodyModel;
    private final vtkActor actor;
    private final vtkPolyDataMapper mapper;
    private final vtkAppendPolyData appendFilter;
    private final vtkPolyData polyData;
    private final vtkPlane plane;
    private final vtkCone cone;
    private final vtkCutter cutPolyData;
    private final vtkTransform transform;
//    private String[] gridFiles;
    private final List<vtkProp> actors;
    private final List<vtkCaptionActor2D> captionActors;
    private double shiftFactor;
    private double longitudeSpacing;
    private double latitudeSpacing;    
	private boolean showGraticule;
	private boolean showCaptions;

    public Graticule(PolyhedralModel smallBodyModel)
    {
        this(smallBodyModel, null);
    }

    /**
	 * @param gridFiles ignored for now. 
	 */
    public Graticule(PolyhedralModel smallBodyModel, String[] gridFiles)
    {
    	this.smallBodyModel = smallBodyModel;
    	this.smallBodyModel.addPropertyChangeListener(this);

        actor = new vtkActor();
        mapper = new vtkPolyDataMapper();
        appendFilter = new vtkAppendPolyData();
        polyData = new vtkPolyData();
        plane = new vtkPlane();
        cone = new vtkCone();
        cutPolyData = new vtkCutter();
        transform = new vtkTransform();
        //this.gridFiles = gridFiles;
        actors = new ArrayList<>();
        captionActors = new ArrayList<>();
        shiftFactor = 7.0;
        longitudeSpacing = 10.;
        latitudeSpacing = 10.;    
    	showGraticule = false;
    	showCaptions = false;
    	actor.GetProperty().SetColor(0.2, 0.2, 0.2);
    }

    public void setShiftFactor(double factor)
    {
        shiftFactor = factor;
    }

    public void generateGrid(vtkPolyData smallBodyPolyData)
    {
    	actors.clear();
    	captionActors.clear();
    	
		int numberLonCircles = (int) Math.ceil(180.0 / longitudeSpacing);
		int numberLatCircles = (int) Math.ceil(90.0 / latitudeSpacing);

		double[] origin = { 0.0, 0.0, 0.0 };
		double[] zaxis = { 0.0, 0.0, 1.0 };

		appendFilter.UserManagedInputsOn();
		appendFilter.SetNumberOfInputs(numberLatCircles + numberLonCircles);
		vtkPolyData[] tmps = new vtkPolyData[numberLatCircles + numberLonCircles];

		cutPolyData.SetInputData(smallBodyPolyData);

		// First do the longitudes.
		for (int i = 0; i < numberLonCircles; ++i)
		{
			double lon = longitudeSpacing * i * Math.PI / 180.0;
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
		if (Configuration.isWindows())
			deg = " ";

		for (int i = 0; i <= numberLonCircles; i++)
		{
			double lat = 0;
			double lon = longitudeSpacing * i * Math.PI / 180.0;
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
			double lon = longitudeSpacing * i * Math.PI / 180.0;
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
				cone.SetAngle(latitudeSpacing * i);
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
		for (double lon = 0; lon < 360; lon += 360./nlon)	// do meridians at nlon intervals from 0
		{
			
			// northern hemisphere
			for (int i = 1; i < numberLatCircles; i++) // Start at one to skip labeling the pole.
			{
				// Compute labels from the poles toward the equator so they coincide with parallels from the grid
				// no matter what spacing.
				double lat = 90 - latitudeSpacing * i;

				// Convert to radians.
				lat *=  Math.PI / 180.0;

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
			for (int i = 1; i < numberLatCircles; i++) // Start at one to skip labeling the pole.
			{
				// Compute labels from the poles toward the equator so they coincide with parallels from the grid
				// no matter what spacing.
				double lat = 90 - latitudeSpacing * i;

				// Convert to radians.
				lat *=  Math.PI / 180.0;

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
		mapper.ScalarVisibilityOff();
		actors.add(actor);
		actors.addAll(captionActors);
    }

	public void setShowCaptions(boolean show)
	{
		showCaptions = show;
		update();
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
    	if (showGraticule)
    	{
    		generateGrid(smallBodyModel.getSmallBodyPolyData());
    		smallBodyModel.shiftPolyLineInNormalDirection(
    				polyData,
    				shiftFactor * smallBodyModel.getMinShiftAmount());

    		mapper.SetInputData(polyData);
    		mapper.Update();

    		actor.SetMapper(mapper);
//    		actor.GetProperty().SetColor(0.2, 0.2, 0.2);

    	} else {
    		actors.clear();
    	}
		for (vtkCaptionActor2D a : captionActors)
		{
			OccludingCaptionActor caption=(OccludingCaptionActor)a;
			caption.setEnabled(showCaptions);
			if (!showCaptions)
				caption.VisibilityOff();
		}
    }

    public void setShowGraticule(boolean show)
    {
    	showGraticule = show;
    	update();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void setLineWidth(double value)
    {
        actor.GetProperty().SetLineWidth((float)value);
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

    @Override
	public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
        {
            update();
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    @Override
	public List<vtkProp> getProps()
    {
        return actors;
    }

}

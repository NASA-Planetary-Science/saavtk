package edu.jhuapl.saavtk.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import vtk.vtkActor;
import vtk.vtkAppendPolyData;
import vtk.vtkCaptionActor2D;
import vtk.vtkCellArray;
import vtk.vtkCellData;
import vtk.vtkIdTypeArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkTransform;
import vtk.vtkUnsignedCharArray;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.Properties;

/**
 * Model of regular polygon structures drawn on a body.
 */

abstract public class AbstractEllipsePolygonModel extends StructureModel implements PropertyChangeListener
{
    private ArrayList<EllipsePolygon> polygons = new ArrayList<EllipsePolygon>();
    private ArrayList<vtkProp> actors = new ArrayList<vtkProp>();

    private vtkPolyData boundaryPolyData;
    private vtkAppendPolyData boundaryAppendFilter;
    private vtkPolyDataMapper boundaryMapper;
    private vtkActor boundaryActor;

    private vtkPolyData interiorPolyData;
    private vtkAppendPolyData interiorAppendFilter;
    private vtkPolyDataMapper interiorMapper;
    private vtkActor interiorActor;

    private vtkUnsignedCharArray boundaryColors;
    private vtkUnsignedCharArray interiorColors;

    private vtkPolyData emptyPolyData;
    private PolyhedralModel smallBodyModel;
    private double defaultRadius = 0.25; // radius for new polygons drawn
    private double maxRadius = 5.0;
    private int numberOfSides = 4;
    private int[] defaultColor = {0, 191, 255};
//    private int[] defaultBoundaryColor = {0, 191, 255};
//    private int[] defaultInteriorColor = {0, 191, 255};
    private double interiorOpacity = 0.3;
    private String type;
    private int[] selectedStructures = {};
    private int maxPolygonId = 0;
    private double offset;

    public enum Mode
    {
        POINT_MODE,
        CIRCLE_MODE,
        ELLIPSE_MODE
    }

    private Mode mode;

    public static class EllipsePolygon extends StructureModel.Structure
    {
        public String name = "default";
        public int id;
        public String label = "";
        public int label_id=-1;
        public vtkCaptionActor2D caption;

        public double[] center;
        public double radius; // or semimajor axis
        public double flattening; // ratio of semiminor axis to semimajor axis
        public double angle;
        public boolean hidden = false;
        public boolean labelHidden = false;

        public vtkPolyData boundaryPolyData;
        public vtkPolyData interiorPolyData;
        public int numberOfSides;
        public String type;
        public int[] color;
        public double[] labelcolor = {1,1,1};
        private Mode mode;
        private boolean editingLabel=false;

        private static DecimalFormat df = new DecimalFormat("#.#####");

        public EllipsePolygon(int numberOfSides, String type, int[] color, Mode mode, int id, String label)
        {
            this.id = id;
            boundaryPolyData = new vtkPolyData();
            interiorPolyData = new vtkPolyData();
            this.numberOfSides = numberOfSides;
            this.type = type;
            this.color = (int[])color.clone();
            this.mode = mode;
        }

        public int getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getType()
        {
            return type;
        }

        public String getInfo()
        {
            String str = "Diameter = " + df.format(2.0*radius) + " km";
            if (mode == Mode.ELLIPSE_MODE)
            {
                str += ", Flattening = " + df.format(flattening);
                str += ", Angle = " + df.format(angle);
            }

            return str;
        }

        public int[] getColor()
        {
            return color;
        }

        public void setColor(int[] color)
        {
            this.color = (int[])color.clone();
        }

        public int getLabelID()
        {
            return label_id;
        }

        public void setLabelID(int label_id)
        {
            this.label_id = label_id;
        }

        public vtkPolyData getBoundaryPolyData()
        {
            return boundaryPolyData;
        }

        public vtkPolyData getInteriorPolyData()
        {
            return interiorPolyData;
        }

        public void updatePolygon(
                PolyhedralModel sbModel,
                double[] center,
                double radius,
                double flattening,
                double angle)
        {
            this.center = center;
            this.radius = radius;
            this.flattening = flattening;
            this.angle = angle;

            if (!hidden)
            {
                sbModel.drawEllipticalPolygon(center, radius, flattening, angle, numberOfSides, interiorPolyData, boundaryPolyData);
            }
            else
            {
                PolyDataUtil.clearPolyData(interiorPolyData);
                PolyDataUtil.clearPolyData(boundaryPolyData);
            }
        }

        public String getClickStatusBarText()
        {
            return type + ", Id = " + id + ", Diameter = " + 2.0*radius + " km";
        }

        @Override
        public void setLabel(String label)
        {
            this.label=label;
        }

        @Override
        public String getLabel()
        {
            return label;
        }

        public boolean getHidden()
        {
            return hidden;
        }

        public boolean getLabelHidden()
        {
            return labelHidden;
        }

        public void setHidden(boolean b)
        {
            hidden = b;
        }

        public void setLabelHidden(boolean b)
        {
            labelHidden=b;
        }

    }

    public AbstractEllipsePolygonModel(
            PolyhedralModel smallBodyModel,
            int numberOfSides,
            Mode mode,
            String type)
    {
        this.smallBodyModel = smallBodyModel;

        this.offset = getDefaultOffset();

        defaultRadius = smallBodyModel.getBoundingBoxDiagonalLength() / 155.0;
        maxRadius = smallBodyModel.getBoundingBoxDiagonalLength() / 8.0;

        this.smallBodyModel.addPropertyChangeListener(this);

        emptyPolyData = new vtkPolyData();

        this.numberOfSides = numberOfSides;
        this.mode = mode;
        this.type = type;

        boundaryColors = new vtkUnsignedCharArray();
        boundaryColors.SetNumberOfComponents(3);

        interiorColors = new vtkUnsignedCharArray();
        interiorColors.SetNumberOfComponents(3);

        boundaryPolyData = new vtkPolyData();
        boundaryAppendFilter = new vtkAppendPolyData();
        boundaryAppendFilter.UserManagedInputsOn();
        boundaryMapper = new vtkPolyDataMapper();
        boundaryActor = new vtkActor();
        vtkProperty boundaryProperty = boundaryActor.GetProperty();
        boundaryProperty.LightingOff();
        boundaryProperty.SetLineWidth(2.0);

        actors.add(boundaryActor);

        interiorPolyData = new vtkPolyData();
        interiorAppendFilter = new vtkAppendPolyData();
        interiorAppendFilter.UserManagedInputsOn();
        interiorMapper = new vtkPolyDataMapper();
        interiorActor = new vtkActor();
        vtkProperty interiorProperty = interiorActor.GetProperty();
        interiorProperty.LightingOff();
        interiorProperty.SetOpacity(interiorOpacity);
        //interiorProperty.SetLineWidth(2.0);

        actors.add(interiorActor);
    }

    public void setDefaultColor(int[] color)
    {
        this.defaultColor = (int[])color.clone();
    }

    public int[] getDefaultColor()
    {
        return defaultColor;
    }

    public void setPolygonColor(int i, int[] color)
    {
        this.polygons.get(i).setColor(color);
    }

    public int[] getPolygonColor(int i)
    {
        return this.polygons.get(i).color;
    }

    /*
    public int[] getDefaultBoundaryColor()
    {
        return defaultBoundaryColor;
    }


    public void setDefaultBoundaryColor(int[] color)
    {
        this.defaultBoundaryColor = (int[])color.clone();
    }

    public int[] getDefaultInteriorColor()
    {
        return defaultInteriorColor;
    }

    public void setDefaultInteriorColor(int[] color)
    {
        this.defaultInteriorColor = (int[])color.clone();
    }
    */

    public double getInteriorOpacity()
    {
        return interiorOpacity;
    }

    public void setInteriorOpacity(double opacity)
    {
        this.interiorOpacity = opacity;
        interiorActor.GetProperty().SetOpacity(opacity);
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }


    private void updatePolyData()
    {
        if (polygons.size() > 0)
        {
            boundaryAppendFilter.SetNumberOfInputs(polygons.size());
            interiorAppendFilter.SetNumberOfInputs(polygons.size());

            for (int i=0; i<polygons.size(); ++i)
            {
                vtkPolyData poly = polygons.get(i).boundaryPolyData;
                if (poly != null)
                    boundaryAppendFilter.SetInputDataByNumber(i, poly);
                poly = polygons.get(i).interiorPolyData;
                if (poly != null)
                    interiorAppendFilter.SetInputDataByNumber(i, poly);
            }

            boundaryAppendFilter.Update();
            interiorAppendFilter.Update();

            vtkPolyData boundaryAppendFilterOutput = boundaryAppendFilter.GetOutput();
            vtkPolyData interiorAppendFilterOutput = interiorAppendFilter.GetOutput();
            boundaryPolyData.DeepCopy(boundaryAppendFilterOutput);
            interiorPolyData.DeepCopy(interiorAppendFilterOutput);

            smallBodyModel.shiftPolyLineInNormalDirection(boundaryPolyData, offset);
            PolyDataUtil.shiftPolyDataInNormalDirection(interiorPolyData, offset);

            boundaryColors.SetNumberOfTuples(boundaryPolyData.GetNumberOfCells());
            interiorColors.SetNumberOfTuples(interiorPolyData.GetNumberOfCells());
            for (int i=0; i<polygons.size(); ++i)
            {
                int[] color = polygons.get(i).color;

                if (Arrays.binarySearch(this.selectedStructures, i) >= 0)
                {
                    CommonData commonData = getCommonData();
                    if (commonData != null)
                        color = commonData.getSelectionColor();
                }

                IdPair range = this.getCellIdRangeOfPolygon(i, false);
                for (int j=range.id1; j<range.id2; ++j)
                    boundaryColors.SetTuple3(j, color[0], color[1], color[2]);

                range = this.getCellIdRangeOfPolygon(i, true);
                for (int j=range.id1; j<range.id2; ++j)
                    interiorColors.SetTuple3(j, color[0], color[1], color[2]);
            }
            vtkCellData boundaryCellData = boundaryPolyData.GetCellData();
            vtkCellData interiorCellData = interiorPolyData.GetCellData();

            boundaryCellData.SetScalars(boundaryColors);
            interiorCellData.SetScalars(interiorColors);

            boundaryAppendFilterOutput.Delete();
            interiorAppendFilterOutput.Delete();
            boundaryCellData.Delete();
            interiorCellData.Delete();
        }
        else
        {
            boundaryPolyData.DeepCopy(emptyPolyData);
            interiorPolyData.DeepCopy(emptyPolyData);
        }


        boundaryMapper.SetInputData(boundaryPolyData);
        boundaryMapper.Update();
        interiorMapper.SetInputData(interiorPolyData);
        interiorMapper.Update();

        boundaryActor.SetMapper(boundaryMapper);
        boundaryActor.Modified();
        interiorActor.SetMapper(interiorMapper);
        interiorActor.Modified();
    }

    public List<vtkProp> getProps()
    {
        return actors;
    }

    public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
    {
        if (prop == boundaryActor || prop == interiorActor)
        {
            int polygonId = this.getPolygonIdFromCellId(cellId, prop == interiorActor);
            EllipsePolygon pol = polygons.get(polygonId);
            return pol.getClickStatusBarText();
        }
        else
        {
            return "";
        }
    }

    public int getNumberOfStructures()
    {
        return polygons.size();
    }

    public Structure getStructure(int polygonId)
    {
        return polygons.get(polygonId);
    }

    public vtkActor getBoundaryActor()
    {
        return boundaryActor;
    }

    public vtkActor getInteriorActor()
    {
        return interiorActor;
    }

    public void addNewStructure()
    {
        // do nothing
    }

    public void addNewStructure(double[] pos, double radius, double flattening, double angle)
    {
        EllipsePolygon pol = new EllipsePolygon(numberOfSides, type, defaultColor, mode, ++maxPolygonId, "");
        polygons.add(pol);

        pol.updatePolygon(smallBodyModel, pos, radius, flattening, angle);
        selectedStructures = new int[]{polygons.size()-1};
        updatePolyData();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.STRUCTURE_ADDED, null, null);
    }

    public void addNewStructure(double[] pos)
    {
        EllipsePolygon pol = new EllipsePolygon(numberOfSides, type, defaultColor, mode, ++maxPolygonId, "");
        polygons.add(pol);

        pol.updatePolygon(smallBodyModel, pos, defaultRadius, 1.0, 0.0);
        selectedStructures = new int[]{polygons.size()-1};
        updatePolyData();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.STRUCTURE_ADDED, null, null);
    }

    public void removeStructure(int polygonId)
    {
        if(polygons.get(polygonId).caption!=null) polygons.get(polygonId).caption.VisibilityOff();
        polygons.get(polygonId).setLabelID(-1);
        polygons.get(polygonId).caption=null;
        polygons.remove(polygonId);
        updatePolyData();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.STRUCTURE_REMOVED, null, polygonId);
    }

    public void removeStructures(int[] polygonIds)
    {
        if (polygonIds == null || polygonIds.length == 0)
            return;

        Arrays.sort(polygonIds);
        for (int i=polygonIds.length-1; i>=0; --i)
        {
            if(polygons.get(polygonIds[i]).caption!=null) polygons.get(polygonIds[i]).caption.VisibilityOff();
            polygons.get(polygonIds[i]).setLabelID(-1);
            polygons.get(polygonIds[i]).caption=null;
            polygons.remove(polygonIds[i]);
            this.pcs.firePropertyChange(Properties.STRUCTURE_REMOVED, null, polygonIds[i]);
        }

        updatePolyData();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void removeAllStructures()
    {
        for(int i =0;i<polygons.size();i++)
        {
            if(polygons.get(i).caption!=null) polygons.get(i).caption.VisibilityOff();
            polygons.get(i).setLabelID(-1);
            polygons.get(i).caption=null;
        }
        polygons.clear();

        updatePolyData();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        this.pcs.firePropertyChange(Properties.ALL_STRUCTURES_REMOVED, null, null);
    }

    public void movePolygon(int polygonId, double[] newCenter)
    {
        EllipsePolygon pol = polygons.get(polygonId);
        pol.updatePolygon(smallBodyModel, newCenter, pol.radius, pol.flattening, pol.angle);
        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    /**
     * Move the polygon to the specified latitude and longitude.
     *
     * @param polygonId
     * @param latitude - in radians
     * @param longitude - in radians
     */
    public void movePolygon(int polygonId, double latitude, double longitude)
    {
        double[] newCenter = new double[3];
        smallBodyModel.getPointAndCellIdFromLatLon(latitude, longitude, newCenter);
        movePolygon(polygonId, newCenter);
    }

    public void changeRadiusOfPolygon(int polygonId, double[] newPointOnPerimeter)
    {
        EllipsePolygon pol = polygons.get(polygonId);
        double newRadius = Math.sqrt(
                (pol.center[0]-newPointOnPerimeter[0])*(pol.center[0]-newPointOnPerimeter[0]) +
                (pol.center[1]-newPointOnPerimeter[1])*(pol.center[1]-newPointOnPerimeter[1]) +
                (pol.center[2]-newPointOnPerimeter[2])*(pol.center[2]-newPointOnPerimeter[2]));
        if (newRadius > maxRadius)
            newRadius = maxRadius;

        pol.updatePolygon(smallBodyModel, pol.center, newRadius, pol.flattening, pol.angle);
        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    protected double computeFlatteningOfPolygon(double[] center, double radius, double angle, double[] newPointOnPerimeter)
    {
        // The following math does this: we need to find the direction of
        // the semimajor axis of the ellipse. Then once we have that
        // we need to find the distance to that line from the point the mouse
        // is hovering, where that point is first projected onto the
        // tangent plane of the asteroid at the ellipse center.
        // This distance divided by the semimajor axis of the ellipse
        // is what we call the flattening.

        // First compute cross product of normal and z axis
        double[] normal = smallBodyModel.getNormalAtPoint(center);
        double[] zaxis = {0.0, 0.0, 1.0};
        double[] cross = new double[3];
        MathUtil.vcrss(zaxis, normal, cross);
        // Compute angle between normal and zaxis
        double sepAngle = MathUtil.vsep(normal, zaxis) * 180.0 / Math.PI;

        vtkTransform transform = new vtkTransform();
        transform.Translate(center);
        transform.RotateWXYZ(sepAngle, cross);
        transform.RotateZ(angle);

        double[] xaxis = {1.0, 0.0, 0.0};
        xaxis = transform.TransformDoubleVector(xaxis);
        MathUtil.vhat(xaxis, xaxis);

        // Project newPoint onto the plane perpendicular to the
        // normal of the shape model.
        double[] projPoint = new double[3];
        MathUtil.vprjp(newPointOnPerimeter, normal, center, projPoint);
        double[] projDir = new double[3];
        MathUtil.vsub(projPoint, center, projDir);

        double[] proj = new double[3];
        MathUtil.vproj(projDir, xaxis, proj);
        double[] distVec = new double[3];
        MathUtil.vsub(projDir, proj, distVec);
        double newRadius = MathUtil.vnorm(distVec);

        double newFlattening = 1.0;
        if (radius > 0.0)
            newFlattening = newRadius / radius;

        if (newFlattening < 0.001)
            newFlattening = 0.001;
        else if (newFlattening > 1.0)
            newFlattening = 1.0;

        transform.Delete();

        return newFlattening;
    }

    public void changeFlatteningOfPolygon(int polygonId, double[] newPointOnPerimeter)
    {
        EllipsePolygon pol = polygons.get(polygonId);

        double newFlattening = computeFlatteningOfPolygon(pol.center, pol.radius, pol.angle, newPointOnPerimeter);

        pol.updatePolygon(smallBodyModel, pol.center, pol.radius, newFlattening, pol.angle);
        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    protected double computeAngleOfPolygon(double[] center, double[] newPointOnPerimeter)
    {
        // The following math does this: we need to find the direction of
        // the semimajor axis of the ellipse. Then once we have that
        // we need to find the angular distance between the axis and the
        // vector from the ellipse center to the point the mouse
        // is hovering, where that vector is first projected onto the
        // tangent plane of the asteroid at the ellipse center.
        // This angular distance is what we rotate the ellipse by.


        // First compute cross product of normal and z axis
        double[] normal = smallBodyModel.getNormalAtPoint(center);
        double[] zaxis = {0.0, 0.0, 1.0};
        double[] cross = new double[3];
        MathUtil.vcrss(zaxis, normal, cross);
        // Compute angle between normal and zaxis
        double sepAngle = MathUtil.vsep(normal, zaxis) * 180.0 / Math.PI;

        vtkTransform transform = new vtkTransform();
        transform.Translate(center);
        transform.RotateWXYZ(sepAngle, cross);

        double[] xaxis = {1.0, 0.0, 0.0};
        xaxis = transform.TransformDoubleVector(xaxis);
        MathUtil.vhat(xaxis, xaxis);

        // Project newPoint onto the plane perpendicular to the
        // normal of the shape model.
        double[] projPoint = new double[3];
        MathUtil.vprjp(newPointOnPerimeter, normal, center, projPoint);
        double[] projDir = new double[3];
        MathUtil.vsub(projPoint, center, projDir);
        MathUtil.vhat(projDir, projDir);

        // Compute angular distance between projected direction and transformed x-axis
        double newAngle = MathUtil.vsep(projDir, xaxis) * 180.0 / Math.PI;

        // We need to negate this angle under certain conditions.
        if (newAngle != 0.0)
        {
            MathUtil.vcrss(xaxis, projDir, cross);
            double a = MathUtil.vsep(cross, normal) * 180.0 / Math.PI;
            if (a > 90.0)
                newAngle = -newAngle;
        }

        transform.Delete();

        return newAngle;
    }

    public void changeAngleOfPolygon(int polygonId, double[] newPointOnPerimeter)
    {
        EllipsePolygon pol = polygons.get(polygonId);

        double newAngle = computeAngleOfPolygon(pol.center, newPointOnPerimeter);

        pol.updatePolygon(smallBodyModel, pol.center, pol.radius, pol.flattening, newAngle);
        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void changeRadiusOfAllPolygons(double newRadius)
    {
        for (EllipsePolygon pol : this.polygons)
        {
            pol.updatePolygon(smallBodyModel, pol.center, newRadius, pol.flattening, pol.angle);
        }

        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void activateStructure(int idx)
    {
        // Do nothing. RegularPolygonModel does not support activation.
    }

    /**
     * A picker picking the actor of this model will return a
     * cellId. But since there are many cells per RegularPolygon, we need to be
     * able to figure out which RegularPolygon was picked
     */
    private int getPolygonIdFromCellId(int cellId, boolean interior)
    {
        int numberCellsSoFar = 0;
        for (int i=0; i<polygons.size(); ++i)
        {
            if (interior)
                numberCellsSoFar += polygons.get(i).interiorPolyData.GetNumberOfCells();
            else
                numberCellsSoFar += polygons.get(i).boundaryPolyData.GetNumberOfCells();
            if (cellId < numberCellsSoFar)
                return i;
        }
        return -1;
    }

    public int getPolygonIdFromBoundaryCellId(int cellId)
    {
        return this.getPolygonIdFromCellId(cellId, false);
    }

    public int getPolygonIdFromInteriorCellId(int cellId)
    {
        return this.getPolygonIdFromCellId(cellId, true);
    }

    private IdPair getCellIdRangeOfPolygon(int polygonId, boolean interior)
    {
        int startCell = 0;
        for (int i=0; i<polygonId; ++i)
        {
            if (interior)
                startCell += polygons.get(i).interiorPolyData.GetNumberOfCells();
            else
                startCell += polygons.get(i).boundaryPolyData.GetNumberOfCells();
        }

        int endCell = startCell;
        if (interior)
            endCell += polygons.get(polygonId).interiorPolyData.GetNumberOfCells();
        else
            endCell += polygons.get(polygonId).boundaryPolyData.GetNumberOfCells();

        return new IdPair(startCell, endCell);
    }

    public void loadModel(File file, boolean append) throws IOException
    {
        ArrayList<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
        ArrayList<String> labels= new ArrayList<String>();
        ArrayList<double[]> colors= new ArrayList<double[]>();
        ArrayList<EllipsePolygon> newPolygons = new ArrayList<EllipsePolygon>();
        for (int i=0; i<lines.size(); ++i)
        {
            EllipsePolygon pol = new EllipsePolygon(numberOfSides, type, defaultColor, mode, ++maxPolygonId, "");
            pol.center = new double[3];

            String[] words = lines.get(i).trim().split("\\s+");

            // The latest version of this file format has 16 columns. The previous version had
            // 10 columns for circles and 13 columns for points. We still want to support loading
            // both versions, so look at how many columns are in the line.

            // The first 8 columns are the same in both the old and new formats.
            if (!append) // If appending, simply use maxPolygonId
                pol.id = Integer.parseInt(words[0]);
            pol.name = words[1];
            pol.center[0] = Double.parseDouble(words[2]);
            pol.center[1] = Double.parseDouble(words[3]);
            pol.center[2] = Double.parseDouble(words[4]);

            if (pol.id > maxPolygonId)
                maxPolygonId = pol.id;

            // Note the next 3 words in the line (the point in spherical coordinates) are not used

            // For the new format and the points file in the old format, the next 4 columns (slope,
            // elevation, acceleration, and potential) are not used.

            if (words.length < 16)
            {
                // OLD VERSION of file
                if (mode == Mode.CIRCLE_MODE || mode == Mode.ELLIPSE_MODE)
                    pol.radius = Double.parseDouble(words[8]) / 2.0; // read in diameter not radius
                else
                    pol.radius = defaultRadius;
            }
            else
            {
                // NEW VERSION of file
                pol.radius = Double.parseDouble(words[12]) / 2.0; // read in diameter not radius
            }

            if (mode == Mode.ELLIPSE_MODE && words.length >= 16)
            {
                pol.flattening = Double.parseDouble(words[13]);
                pol.angle = Double.parseDouble(words[14]);
            }
            else
            {
                pol.flattening = 1.0;
                pol.angle = 0.0;
            }

            // If there are 9 or more columns in the file, the last column is the color in both
            // the new and old formats.
            if (words.length > 9)
            {
                int colorIdx = words.length - 3;
                if (words.length == 17)
                    colorIdx = 15;

                String[] colorStr = words[colorIdx].split(",");
                if (colorStr.length == 3)
                {
                    pol.color[0] = Integer.parseInt(colorStr[0]);
                    pol.color[1] = Integer.parseInt(colorStr[1]);
                    pol.color[2] = Integer.parseInt(colorStr[2]);
                }
            }

            pol.updatePolygon(smallBodyModel, pol.center, pol.radius, pol.flattening, pol.angle);
            newPolygons.add(pol);

            //Second to last word is the label, last string is the color
            if(words[words.length-2].substring(0, 2).equals("l:"))
            {
                pol.label=words[words.length-2].substring(2);
                labels.add(pol.label);
            }
            // new format means no color
            else if (words[words.length-1].startsWith("\""))  // labels in quotations
            {
                pol.label = words[words.length-1];
                pol.label = pol.label.substring(1, pol.label.length()-1);
                labels.add(pol.label);
            }
//            else
//            {
//                pol.label = words[words.length-1];
//                labels.add(pol.label);
//            }

//            if(words[words.length-1].substring(0, 3).equals("lc:"))
//            {
//                double[] labelcoloradd = {1.0,1.0,1.0};
//                String[] labelColors=words[words.length-1].substring(3).split(",");
//                labelcoloradd[0] = Double.parseDouble(labelColors[0]);
//                labelcoloradd[1] = Double.parseDouble(labelColors[1]);
//                labelcoloradd[2] = Double.parseDouble(labelColors[2]);
//                pol.labelcolor=labelcoloradd;
//                colors.add(labelcoloradd);
//            }
        }

        // Only if we reach here and no exception is thrown do we modify this class
        if (append)
        {
            int init = polygons.size()-1;
            polygons.addAll(newPolygons);

            for(int i=init;i<init+labels.size();i++)
            {
                if(polygons.get(i).label!=null)
                {
                    setStructureLabel(i, labels.get(i-init));
//                    if(polygons.get(i).labelcolor!=null)
//                    {
//                        colorLabel(i,colors.get((i-init)));
//                    }
                }
            }
        }
        else
        {
            polygons = newPolygons;
            for(int i=0;i<labels.size();i++)
            {
                if(polygons.get(i).label!=null)
                {
                    setStructureLabel(i, labels.get(i));
//                    if(polygons.get(i).labelcolor!=null)
//                    {
//                        colorLabel(i,colors.get(i));
//                    }
                }
            }
        }

        updatePolyData();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void saveModel(File file) throws IOException
    {
        FileWriter fstream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fstream);

        for (EllipsePolygon pol : polygons)
        {
            String name = pol.name;
            if (name.length() == 0)
                name = "default";

            // Since tab is used as the delimiter, replace any tabs in the name with spaces.
            name = name.replace('\t', ' ');

            LatLon llr = MathUtil.reclat(pol.center);
            double lat = llr.lat*180.0/Math.PI;
            double lon = llr.lon*180.0/Math.PI;
            if (lon < 0.0)
                lon += 360.0;

            String str =
                pol.id + "\t" +
                name + "\t" +
                pol.center[0] + "\t" +
                pol.center[1] + "\t" +
                pol.center[2] + "\t" +
                lat + "\t" +
                lon + "\t" +
                llr.rad;


            str += "\t";
            double[] values = getColoringValuesAtPolygon(pol);
            if (values == null || values.length != 4)
            {
                str += "NA\tNA\tNA\tNA";
            }
            else
            {
                for (int i=0; i<values.length; ++i)
                {
                    str += values[i];
                    if (i < values.length-1)
                        str += "\t";
                }
            }

            str += "\t" + 2.0*pol.radius; // save out as diameter, not radius

            str += "\t" + pol.flattening + "\t" + pol.angle;

            str += "\t" + pol.color[0] + "," + pol.color[1] + "," + pol.color[2];

             if (mode == Mode.ELLIPSE_MODE)
             {
                 Double gravityAngle = getEllipseAngleRelativeToGravityVector(pol);
                 if (gravityAngle != null)
                     str += "\t" + gravityAngle;
                 else
                     str += "\t" + "NA";
             }

            str+="\t" + "\"" + pol.label + "\"";

//            String labelcolorStr="\tlc:"+pol.labelcolor[0] + "," + pol.labelcolor[1] + "," + pol.labelcolor[2];
//            str+=labelcolorStr;

            str += "\n";

            out.write(str);
        }

        out.close();
    }

    public int getActivatedStructureIndex()
    {
        return -1;
    }

    public boolean supportsActivation()
    {
        return false;
    }

    public double getDefaultRadius()
    {
        return defaultRadius;
    }

    public void setDefaultRadius(double radius)
    {
        this.defaultRadius = radius;
    }

    public void selectStructures(int[] indices)
    {
        this.selectedStructures = indices.clone();
        Arrays.sort(selectedStructures);
        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public int[] getSelectedStructures()
    {
        return selectedStructures;
    }

    public int getStructureIndexFromCellId(int cellId, vtkProp prop)
    {
        if (prop == boundaryActor)
        {
            return getPolygonIdFromBoundaryCellId(cellId);
        }
        else if (prop == interiorActor)
        {
            return getPolygonIdFromInteriorCellId(cellId);
        }

        return -1;
    }

    public void redrawAllStructures()
    {
        for (EllipsePolygon pol : this.polygons)
        {
            pol.updatePolygon(smallBodyModel, pol.center, pol.radius, pol.flattening, pol.angle);
        }

        updatePolyData();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
        {
            redrawAllStructures();
        }
    }

    public void setStructureColor(int idx, int[] color)
    {
        polygons.get(idx).setColor(color);
        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    private double[] getColoringValuesAtPolygon(EllipsePolygon pol)
    {
        if (!smallBodyModel.isColoringDataAvailable())
            return null;

        if (mode == Mode.POINT_MODE)
        {
            return smallBodyModel.getAllColoringValues(pol.center);
        }
        else
        {
            // For circles compute the slope and elevation averaged over the rim of the circle.
            // For acceleration and potential simply compute at the center.
            double[] values = smallBodyModel.getAllColoringValues(pol.center);
            if (values == null || values.length != 4)
                return null;

            values[0] = 0.0;
            values[1] = 0.0;

            vtkCellArray lines = pol.boundaryPolyData.GetLines();
            vtkPoints points = pol.boundaryPolyData.GetPoints();

            vtkIdTypeArray idArray = lines.GetData();
            int size = idArray.GetNumberOfTuples();

            double totalLength = 0.0;
            double[] midpoint = new double[3];
            for (int i=0; i<size; i+=3)
            {
                if (idArray.GetValue(i) != 2)
                {
                    System.out.println("Big problem: polydata corrupted");
                    return null;
                }

                double[] pt1 = points.GetPoint(idArray.GetValue(i+1));
                double[] pt2 = points.GetPoint(idArray.GetValue(i+2));

                MathUtil.midpointBetween(pt1, pt2, midpoint);
                double dist = MathUtil.distanceBetween(pt1, pt2);
                totalLength += dist;

                double[] valuesAtMidpoint = smallBodyModel.getAllColoringValues(midpoint);

                values[0] += valuesAtMidpoint[0]*dist;
                values[1] += valuesAtMidpoint[1]*dist;
            }

            values[0] /= totalLength;
            values[1] /= totalLength;

            return values;
        }
    }

    private Double getEllipseAngleRelativeToGravityVector(EllipsePolygon pol)
    {
        double[] gravityVector = smallBodyModel.getGravityVector(pol.center);
        if (gravityVector == null)
            return null;
        MathUtil.vhat(gravityVector, gravityVector);


        // First compute cross product of normal and z axis
        double[] normal = smallBodyModel.getNormalAtPoint(pol.center);
        double[] zaxis = {0.0, 0.0, 1.0};
        double[] cross = new double[3];
        MathUtil.vcrss(zaxis, normal, cross);
        // Compute angle between normal and zaxis
        double sepAngle = -MathUtil.vsep(normal, zaxis) * 180.0 / Math.PI;



        // Rotate gravity vector and center of ellipse by amount
        // such that normal of ellipse faces positive z-axis
        vtkTransform transform = new vtkTransform();
        transform.RotateWXYZ(sepAngle, cross);

        gravityVector = transform.TransformDoubleVector(gravityVector);
        double[] center = transform.TransformDoublePoint(pol.center);

        // project gravity into xy plane
        double[] gravityPoint = {
                center[0] + gravityVector[0],
                center[1] + gravityVector[1],
                center[2] + gravityVector[2],
        };
        double[] projGravityPoint = new double[3];
        MathUtil.vprjp(gravityPoint, zaxis, center, projGravityPoint);
        double[] projGravityVector = new double[3];
        MathUtil.vsub(projGravityPoint, center, projGravityVector);
        MathUtil.vhat(projGravityVector, projGravityVector);



        // Compute direction of semimajor axis (both directions) in xy plane
        transform.Delete();
        transform = new vtkTransform();
        transform.RotateZ(pol.angle);

        // Positive x direction
        double[] xaxis = {1.0, 0.0, 0.0};
        double[] semimajoraxis1 = transform.TransformDoubleVector(xaxis);

        // Negative x direction
        double[] mxaxis = {-1.0, 0.0, 0.0};
        double[] semimajoraxis2 = transform.TransformDoubleVector(mxaxis);



        // Compute angular separation of projected gravity vector
        // with respect to x-axis using atan2
        double gravAngle = Math.atan2(projGravityVector[1], projGravityVector[0]) * 180.0 / Math.PI;
        if (gravAngle < 0.0)
            gravAngle += 360.0;



        // Compute angular separations of semimajor axes vectors (both directions)
        // with respect to x-axis using atan2
        double smaxisangle1 = Math.atan2(semimajoraxis1[1], semimajoraxis1[0]) * 180.0 / Math.PI;
        if (smaxisangle1 < 0.0)
            smaxisangle1 += 360.0;

        double smaxisangle2 = Math.atan2(semimajoraxis2[1], semimajoraxis2[0]) * 180.0 / Math.PI;
        if (smaxisangle2 < 0.0)
            smaxisangle2 += 360.0;



        // Compute angular separations between semimajor axes and gravity vector.
        // The smaller one is the one we want, which should be between 0 and 180 degrees.
        double sepAngle1 = smaxisangle1 - gravAngle;
        if (sepAngle1 < 0.0)
            sepAngle1 += 360.0;

        double sepAngle2 = smaxisangle2 - gravAngle;
        if (sepAngle2 < 0.0)
            sepAngle2 += 360.0;


        transform.Delete();

        return Math.min(sepAngle1, sepAngle2);
    }

    public double getDefaultOffset()
    {
        return 5.0*smallBodyModel.getMinShiftAmount();
    }

    public void setOffset(double offset)
    {
        this.offset = offset;

        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public double getOffset()
    {
        return offset;
    }

    @Override
    public double getLineWidth()
    {
        vtkProperty boundaryProperty = boundaryActor.GetProperty();
        return boundaryProperty.GetLineWidth();
    }

    @Override
    public void setLineWidth(double width)
    {
        if (width >= 1.0)
        {
            vtkProperty boundaryProperty = boundaryActor.GetProperty();
            boundaryProperty.SetLineWidth(width);
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    public void setVisible(boolean b)
    {
        boolean needToUpdate = false;
        for (EllipsePolygon pol : polygons)
        {
            if (pol.hidden == b)
            {
                pol.hidden = !b;
                if(pol.caption!=null&&pol.hidden==true)
                    pol.caption.VisibilityOff();
                else if(pol.caption!=null&&pol.hidden==false)
                {
                    if(pol.labelHidden)
                        pol.caption.VisibilityOff();
                    else
                       pol.caption.VisibilityOn();
                }
                pol.updatePolygon(smallBodyModel, pol.center, pol.radius, pol.flattening, pol.angle);
                needToUpdate = true;
            }
        }
        if (needToUpdate)
        {
            updatePolyData();
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    public void setLabelsVisible(boolean b)
    {
        for (EllipsePolygon pol : polygons)
        {
            pol.labelHidden=!b;
            if(pol.caption!=null&&!pol.getHidden())
            {
                pol.caption.SetVisibility(b ? 1 : 0);
            }
        }

        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);

    }

    public void savePlateDataInsideStructure(int idx, File file) throws IOException
    {
        vtkPolyData polydata = polygons.get(idx).interiorPolyData;
        smallBodyModel.savePlateDataInsidePolydata(polydata, file);
    }

    @Override
    public void setStructuresHidden(int[] polygonIds, boolean hidden, boolean labelHidden)
    {
        for (int i=0; i<polygonIds.length; ++i)
        {
            EllipsePolygon pol = polygons.get(polygonIds[i]);
            if (pol.hidden != hidden&&pol!=null)
            {
                if(pol.caption!=null)
                {
                    if(!hidden&&!labelHidden)
                        pol.caption.VisibilityOn();
                    else
                    {
                        pol.caption.VisibilityOff();
                    }
                }
                pol.hidden = hidden;
                pol.updatePolygon(smallBodyModel, pol.center, pol.radius, pol.flattening, pol.angle);
            }
        }

        updatePolyData();

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    @Override
    public boolean isStructureHidden(int id)
    {
        return polygons.get(id).hidden;
    }

    public boolean isLabelHidden(int id)
    {
        return polygons.get(id).labelHidden;
    }


    @Override
    public double[] getStructureCenter(int id)
    {
        return polygons.get(id).center;
    }

    @Override
    public double[] getStructureNormal(int id)
    {
        double[] center = getStructureCenter(id);
        return smallBodyModel.getNormalAtPoint(center);
    }

    @Override
    public double getStructureSize(int id)
    {
        return 2.0*polygons.get(id).radius;
    }


    @Override
    public boolean setStructureLabel(int idx, String label)
    {
        polygons.get(idx).setLabel(label);
        int numLetters = label.length();
        if(polygons.get(idx).editingLabel)
        {
            if(label==null||label.equals(""))
            {
                polygons.get(idx).caption.VisibilityOff();
                for(int i=0; i<actors.size();i++)
                {
                    if(polygons.get(idx).caption==actors.get(i))
                    {
                        actors.remove(i);
                        i--;
                    }
                }
                polygons.get(idx).editingLabel=false;
                polygons.get(idx).setLabelID(-1);
                polygons.get(idx).caption=null;
            }
            else
            {
                polygons.get(idx).caption.SetCaption(label);
                polygons.get(idx).caption.SetPosition2(numLetters*0.0025+0.03, numLetters*0.001+0.02);
                polygons.get(idx).caption.GetCaptionTextProperty().Modified();
            }
        }
        else
        {
            if(label==null||label.equals(""))
            {
                return true;
            }
            vtkCaptionActor2D v = new vtkCaptionActor2D();
            v.GetCaptionTextProperty().SetColor(1.0, 1.0, 1.0);
            v.GetCaptionTextProperty().SetJustificationToCentered();
            v.GetCaptionTextProperty().BoldOn();
            v.GetCaptionTextProperty().SetJustificationToLeft();
            v.VisibilityOn();
            v.BorderOff();
            v.ThreeDimensionalLeaderOn();
            v.SetAttachmentPoint(polygons.get(idx).center);
            v.SetPosition(0, 0);
            v.SetPosition2(numLetters*0.0025+0.03, numLetters*0.001+0.02);
            v.SetCaption(polygons.get(idx).getLabel());

            polygons.get(idx).setLabelID(actors.size()-1);
            polygons.get(idx).caption=v;
            actors.add(polygons.get(idx).caption);
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, idx);

            polygons.get(idx).editingLabel=true;
        }
        updatePolyData();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, idx);
        return true;
    }

    public void showLabel(int index)
    {
        if(polygons.get(index).caption==null||polygons.get(index).caption.GetCaption().equals(""))
        {
            setStructureLabel(index, " ");
        }

        if(!polygons.get(index).getHidden())
            polygons.get(index).caption.SetVisibility(1-polygons.get(index).caption.GetVisibility());
        boolean b = (polygons.get(index).caption.GetVisibility() == 0);
        polygons.get(index).labelHidden=b;

        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, index);
    }

    public void showBorders()
    {
        for (int index : selectedStructures)
        {
            vtkCaptionActor2D v =polygons.get(index).caption;
            v.SetBorder(1-v.GetBorder());
        }
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void colorLabel(int[] colors)
    {
        for (int index : selectedStructures)
        {
            vtkCaptionActor2D v =polygons.get(index).caption;
            v.GetCaptionTextProperty().SetColor(colors[0]/256.0, colors[1]/256.0, colors[2]/256.0);
            double color[] = {colors[0]/256.0, colors[1]/256.0, colors[2]/256.0};
            polygons.get(index).labelcolor=color;
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    private void colorLabel(int loc, double[]colors)
    {
        vtkCaptionActor2D v =polygons.get(loc).caption;
        if(v==null)
            return;
        v.GetCaptionTextProperty().SetColor(colors);
    }

    public void changeFont(int font_size, int structure)
    {
        int len = (polygons.get(structure).caption.GetCaption().length());
        polygons.get(structure).caption.SetPosition2((len*0.0025+0.03)*(font_size/12.0), (len*0.001+0.02)*(font_size/12.0));
        polygons.get(structure).caption.GetCaptionTextProperty().Modified();
        polygons.get(structure).caption.Modified();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }

    public void changeFontType(int structure)
    {
        vtkCaptionActor2D v =polygons.get(structure).caption;
        Object[] options = { "Times", "Arial", "Courier" };
        int option = JOptionPane.showOptionDialog(null, "Pick the font you wish to use", "Choose", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if(option==0)
            v.GetCaptionTextProperty().SetFontFamilyToTimes();
        else if(option==1)
            v.GetCaptionTextProperty().SetFontFamilyToArial();
        else
            v.GetCaptionTextProperty().SetFontFamilyToCourier();
        v.GetCaptionTextProperty().Modified();
        this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
    }
}

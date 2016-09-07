package edu.jhuapl.saavtk.model.structure;

import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProperty;
import vtk.vtkTransform;
import vtk.vtkTriangle;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.Properties;


/**
 * Model of circle structures drawn on a body. This class adds the ability
 * to create a new circle by simply clicking any 3 points on the perimeter of
 * the circle. The circle is formed by using VTK's vtkTriangle::Circumcircle
 * function.
 */
public class CircleModel extends AbstractEllipsePolygonModel
{
    private PolyhedralModel smallBodyModel;
    private vtkPolyData activationPolyData;
    private vtkPolyDataMapper activationMapper;
    private vtkActor activationActor;
    private double[] unshiftedPoint1;
    private double[] unshiftedPoint2;


    public CircleModel(PolyhedralModel smallBodyModel)
    {
        super(smallBodyModel, 20, Mode.CIRCLE_MODE, "circle");

        this.smallBodyModel = smallBodyModel;

        setInteriorOpacity(0.0);
        int[] color = {255, 0, 255};
        setDefaultColor(color);

        activationPolyData = new vtkPolyData();
        vtkPoints points = new vtkPoints();
        vtkCellArray cells = new vtkCellArray();
        activationPolyData.SetPoints(points);
        activationPolyData.SetVerts(cells);

        activationMapper = new vtkPolyDataMapper();
        activationMapper.SetInputData(activationPolyData);
        activationMapper.Update();

        activationActor = new vtkActor();
        activationActor.PickableOff();
        vtkProperty lineActivationProperty = activationActor.GetProperty();
        lineActivationProperty.SetColor(0.0, 0.0, 1.0);
        lineActivationProperty.SetPointSize(7.0);

        activationActor.SetMapper(activationMapper);
        activationActor.Modified();

        getProps().add(activationActor);
    }

    /**
     * Adds a new point on the perimeter of the circle. When the point
     * count equals 3, a circle is created and the intermediate points are
     * deleted.
     *
     * @param pt
     * @return
     */
    public boolean addCircumferencePoint(double[] pt)
    {
        vtkPoints points = activationPolyData.GetPoints();
        vtkCellArray vert = activationPolyData.GetVerts();

        int numPoints = points.GetNumberOfPoints();

        if (numPoints < 2)
        {
            vtkIdList idList = new vtkIdList();
            idList.SetNumberOfIds(1);
            idList.SetId(0, numPoints);

            points.InsertNextPoint(pt);
            vert.InsertNextCell(idList);

            idList.Delete();

            if (numPoints == 0)
            {
                unshiftedPoint1 = pt.clone();
            }
            if (numPoints == 1)
            {
                unshiftedPoint2 = pt.clone();
                // Since we shift the points afterwards, reset the first
                // point to the original unshifted position.
                points.SetPoint(0, unshiftedPoint1);
            }

            smallBodyModel.shiftPolyLineInNormalDirection(activationPolyData, getOffset());

            activationPolyData.Modified();
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
        else
        {
            // Take the 3 points and compute a circle that passes through them.
            // To do this, first form a triangle from these 3 points.
            // Compute the normal and rotate the 3 points so that its normal
            // is facing the positive z direction. Then use VTK's Circumcircle
            // function which computes the center and radius of a circle that
            // passes through these points.
            double[] pt1 = unshiftedPoint1;
            double[] pt2 = unshiftedPoint2;
            double[] pt3 = pt;
            double[] normal = new double[3];

            vtkTriangle tri = new vtkTriangle();

            // Note Circumcircle ignores z component, so first need to rotate
            // triangle so normal points in z direction.
            tri.ComputeNormal(pt1, pt2, pt3, normal);

            if (MathUtil.vnorm(normal) == 0.0)
            {
                // Cannot fit a circle so reset and return
                resetCircumferencePoints();
                return false;
            }

            double[] zaxis = {0.0, 0.0, 1.0};
            double[] cross = new double[3];
            MathUtil.vcrss(zaxis, normal, cross);
            // Compute angle between normal and zaxis
            double sepAngle = -MathUtil.vsep(normal, zaxis) * 180.0 / Math.PI;

            vtkTransform transform = new vtkTransform();
            transform.RotateWXYZ(sepAngle, cross);

            pt1 = transform.TransformDoublePoint(pt1);
            pt2 = transform.TransformDoublePoint(pt2);
            pt3 = transform.TransformDoublePoint(pt3);

            double[] center = new double[3];
            double radius = Math.sqrt(tri.Circumcircle(pt1, pt2, pt3, center));
            // Note Circumcircle ignores z component, so set it here.
            center[2] = pt1[2];

            center = transform.GetInverse().TransformDoublePoint(center);

            setDefaultRadius(radius);
            addNewStructure(center);

            resetCircumferencePoints();
        }

        return true;
    }

    /**
     * Cancels the new circle currently being created so you can start again.
     * Intermediate points are deleted.
     */
    public void resetCircumferencePoints()
    {
        if (activationPolyData.GetNumberOfPoints() > 0)
        {
            unshiftedPoint1 = null;
            unshiftedPoint2 = null;

            vtkPoints points = new vtkPoints();
            vtkCellArray cells = new vtkCellArray();
            activationPolyData.SetPoints(points);
            activationPolyData.SetVerts(cells);

            activationPolyData.Modified();
            this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
        }
    }

    public int getNumberOfCircumferencePoints()
    {
        return activationPolyData.GetNumberOfPoints();
    }

    public void removeAllStructures()
    {
        super.removeAllStructures();
        this.resetCircumferencePoints();
    }
}
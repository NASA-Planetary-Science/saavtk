package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.util.EllipseUtil;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.Properties;
import vtk.vtkActor;
import vtk.vtkCellArray;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProperty;

public class EllipseModel extends AbstractEllipsePolygonModel
{
    private PolyhedralModel smallBodyModel;
    private vtkPolyData activationPolyData;
    private vtkPolyDataMapper activationMapper;
    private vtkActor activationActor;
    private double[] unshiftedPoint1;
    private double[] unshiftedPoint2;

    public EllipseModel(PolyhedralModel smallBodyModel)
    {
        super(smallBodyModel, 20, Mode.ELLIPSE_MODE, "ellipse");

        this.smallBodyModel = smallBodyModel;

        setInteriorOpacity(0.0);
        setDefaultColor(Color.MAGENTA);

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
     * Adds a new point on the perimeter of the ellipse. When the point
     * count equals 3, am ellipse is created and the intermediate points are
     * deleted.
     *
     * @param aPtArr
     * @return
     */
    public boolean addCircumferencePoint(double[] aPtArr)
    {
        vtkPoints points = activationPolyData.GetPoints();
        vtkCellArray vert = activationPolyData.GetVerts();

        int numPoints = points.GetNumberOfPoints();


        if (!getProps().contains(activationActor))
        	getProps().add(activationActor);

        if (numPoints < 2)
        {
            vtkIdList idList = new vtkIdList();
            idList.SetNumberOfIds(1);
            idList.SetId(0, numPoints);

            points.InsertNextPoint(aPtArr);
            vert.InsertNextCell(idList);

            idList.Delete();

            if (numPoints == 0)
            {
                unshiftedPoint1 = aPtArr.clone();
            }
            if (numPoints == 1)
            {
                unshiftedPoint2 = aPtArr.clone();
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
            // Take the 3 points and compute an ellipse that passes through them.
            // To do this, assume that the first 2 points lie on the end-points of the major axis
            // and that the third point lies on one of the end-points of the minor axis.
            double[] pt1Arr = unshiftedPoint1;
            double[] pt2Arr = unshiftedPoint2;
            double[] pt3Arr = aPtArr;

            double radius = 0.5 * MathUtil.distanceBetween(pt1Arr, pt2Arr);
            if (radius == 0.0)
            {
                // Cannot fit an ellipse so reset and return
                resetCircumferencePoints();
                return false;
            }

            // First find the point on the asteroid that is midway between
            // the first 2 points. This is the center of the ellipse.
            double[] centerArr = new double[3];
            MathUtil.midpointBetween(pt1Arr, pt2Arr, centerArr);
            Vector3D center = new Vector3D(centerArr);

            Vector3D pt2 = new Vector3D(pt2Arr);
            double angle = EllipseUtil.computeAngleOfPolygon(smallBodyModel, center, pt2);

            Vector3D pt3 = new Vector3D(pt3Arr);
            double flattening = EllipseUtil.computeFlatteningOfPolygon(smallBodyModel, center, radius, angle, pt3);

            addNewStructure(center, radius, flattening, angle);

            resetCircumferencePoints();
        }

        return true;
    }

    /**
     * Cancels the new ellipse currently being created so you can start again.
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
    @Override
	public void removeAllStructures()
    {
        super.removeAllStructures();
        this.resetCircumferencePoints();
    }
}

package edu.jhuapl.saavtk.model;

import vtk.vtkLine;

/**
 * Checks whether or not a point is within a cylinder. This cylinder is
 * oriented in the direction normal to the point of the small body model
 * passed into the constructor of this class.
 */
public class PointInCylinderChecker extends PointInRegionChecker
{
    private double[] center;
    private double radiusSquared = Double.MAX_VALUE;
    private vtkLine line = new vtkLine();
    private double[] anotherPointOnLine;

    /**
     * Form a cylinder centered at center oriented in the direction normal
     * to the point on smallBodyModel closest to center with the specified
     * radius.
     */
    public PointInCylinderChecker(
            PolyhedralModel smallBodyModel,
            double[] center,
            double radius)
    {
        this.center = center.clone();
        this.radiusSquared = radius * radius;

        if (center != null)
        {
            double[] normal = smallBodyModel.getNormalAtPoint(center);
            anotherPointOnLine = new double[]{
                center[0] + normal[0],
                center[1] + normal[1],
                center[2] + normal[2],
            };
            radiusSquared = radius * radius;
        }
    }

    @Override
    public boolean checkPointIsInRegion(double[] point)
    {
        double dist2 = 0.0;
        if (center != null)
            dist2 = line.DistanceToLine(point, center, anotherPointOnLine);
        if (dist2 <= radiusSquared)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public double[] getCenter()
    {
        return center;
    }

    public double getRadius()
    {
        return Math.sqrt(radiusSquared);
    }
}

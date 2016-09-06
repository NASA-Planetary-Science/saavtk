package edu.jhuapl.saavtk.util;

public class BoundingBox
{
    public double xmin = Double.MAX_VALUE;
    public double xmax = -Double.MAX_VALUE;
    public double ymin = Double.MAX_VALUE;
    public double ymax = -Double.MAX_VALUE;
    public double zmin = Double.MAX_VALUE;
    public double zmax = -Double.MAX_VALUE;

    public BoundingBox()
    {
    }

    public BoundingBox(double[] bounds)
    {
        setBounds(bounds);
    }

    public void setBounds(double[] bounds)
    {
        xmin = bounds[0];
        xmax = bounds[1];
        ymin = bounds[2];
        ymax = bounds[3];
        zmin = bounds[4];
        zmax = bounds[5];
    }

    public double[] getBounds()
    {
        return new double[] {xmin, xmax, ymin, ymax, zmin, zmax};
    }

    public void update(double x, double y, double z)
    {
        if (xmin > x)
            xmin = x;
        if (xmax < x)
            xmax = x;
        if (ymin > y)
            ymin = y;
        if (ymax < y)
            ymax = y;
        if (zmin > z)
            zmin = z;
        if (zmax < z)
            zmax = z;
    }

    public boolean intersects(BoundingBox other)
    {
        if (other.xmax >= xmin && other.xmin <= xmax &&
            other.ymax >= ymin && other.ymin <= ymax &&
            other.zmax >= zmin && other.zmin <= zmax)
            return true;
        else
            return false;
    }

    /**
     * Returns the largest side of the box.
     * @return
     */
    public double getLargestSide()
    {
        return Math.max(xmax-xmin, Math.max(ymax-ymin, zmax-zmin));
    }

    public double[] getCenterPoint()
    {
        double[] center = {
                xmin + (xmax-xmin)/2.0,
                ymin + (ymax-ymin)/2.0,
                zmin + (zmax-zmin)/2.0
        };

        return center;
    }

    public double getDiagonalLength()
    {
        double[] vec = {
                xmax-xmin,
                ymax-ymin,
                zmax-zmin
        };

        return MathUtil.vnorm(vec);
    }

    /**
     * Returns whether or not the given point is contained in the box.
     * @param pt
     * @return
     */
    public boolean contains(double[] pt)
    {
        if (pt[0] < xmin || pt[0] > xmax ||
            pt[1] < ymin || pt[1] > ymax ||
            pt[2] < zmin || pt[2] > zmax)
            return false;
        else
            return true;
    }

    /**
     * Increase the size of the bounding box by adding (subtracting)
     * to each side a specified percentage of the bounding box
     * diagonal
     *
     * @param fractionOfDiagonalLength must be positive
     */
    public void increaseSize(double fractionOfDiagonalLength)
    {
        if (fractionOfDiagonalLength > 0.0)
        {
            double size = fractionOfDiagonalLength * getDiagonalLength();
            xmin -= size;
            xmax += size;
            ymin -= size;
            ymax += size;
            zmin -= size;
            zmax += size;
        }
    }

    public String toString()
    {
        return "xmin: " + xmin + " xmax: " + xmax +
               " ymin: " + ymin + " ymax: " + ymax +
               " zmin: " + zmin + " zmax: " + zmax;
    }

    @Override
    public boolean equals(Object obj)
    {
        BoundingBox b = (BoundingBox)obj;
        return this.xmin == b.xmin &&
        this.xmax == b.xmax &&
        this.ymin == b.ymin &&
        this.ymax == b.ymax &&
        this.zmin == b.zmin &&
        this.zmax == b.zmax;
    }

    @Override
    public Object clone()
    {
        return new BoundingBox(getBounds());
    }
}

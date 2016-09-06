package edu.jhuapl.saavtk.model;

/**
 * Abstract class which provides function which returns whether or not a point
 * is in a region. It is up to subclasses to determine how this is done.
 */
public abstract class PointInRegionChecker
{
    abstract public boolean checkPointIsInRegion(double[] point);
}

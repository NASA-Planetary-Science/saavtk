package edu.jhuapl.saavtk.util;

import java.util.Vector;

import vtk.vtkPolyData;

public class RecursiveBoundingBox extends BoundingBox
{
//	public static RecursiveBoundingBox fromObj(String filename)
//	{
//		
//		vtkOBJReader reader=new vtkOBJReader();
//		reader.SetFileName(filename);
//		reader.Update();
//		
//		RecursiveBoundingBox bbox=new RecursiveBoundingBox(reader.GetOutput().GetBounds());
//		return bbox;
//	}
	
    private int level = 0;
    private RecursiveBoundingBox[] children;
    private double cubeSize;
    private int levelNodeID = 0;
    private int parentNodeID = -1;
    private String nodePath = "-1";
    
    public RecursiveBoundingBox()
    {
    	level = 0;
    	makeChildren();
    }
    
    public RecursiveBoundingBox(int level, double cubeSize)
    {
    	this.level = level;
    	this.cubeSize = cubeSize;
    	makeChildren();
    }
    
    public RecursiveBoundingBox(int level, double[] bounds, double cubeSize, int levelNodeID, int parentNodeID, String nodePath)
    {
    	this.level = level;
    	this.cubeSize = cubeSize;
    	this.levelNodeID = levelNodeID;
    	this.parentNodeID = parentNodeID;
    	this.nodePath = nodePath + "-" + levelNodeID;
    	setBounds(bounds);
//		System.out.println("RecursiveBoundingBox: made " + this);
    	makeChildren();
    }

    public RecursiveBoundingBox(double[] bounds, double cubeSize)
    {
    	this.cubeSize = cubeSize;
    	nodePath = "0";
        setBounds(bounds);
//		System.out.println("RecursiveBoundingBox: made " + this + " with cube size " + cubeSize);
        makeChildren();
    }
    
    private void makeChildren()
    {
    	if (level == 5) return;	//no more than 5 levels down
    	BoundingBox boundingBox = new BoundingBox(getBounds());
    	children = new RecursiveBoundingBox[8];
    	int ii = 0;
    	for (int k=0; k<2; ++k)
    		for (int j=0; j<2; ++j)
    			for (int i=0; i<2; ++i)
    			{
    				double[] bounds = makeBoundsForCubeIJKAndCubeSize(boundingBox, i, j, k, cubeSize/2.0);
    				children[ii++] = new RecursiveBoundingBox(level+1, bounds, cubeSize/2.0, ii, levelNodeID, nodePath);
    			}
    }
    
    private double[] makeBoundsForCubeIJKAndCubeSize(BoundingBox boundingBox, int i, int j, int k, double cubeSize)
    {
//    	System.out.println("RecursiveBoundingBox: makeBoundsForCubeIJKAndCubeSize: cubesize " + cubeSize);
    	double cubeSizeX = (boundingBox.xmax - boundingBox.xmin)/2;
    	double cubeSizeY = (boundingBox.ymax - boundingBox.ymin)/2;
    	double cubeSizeZ = (boundingBox.zmax - boundingBox.zmin)/2;
    	double zmin = boundingBox.zmin + k * cubeSize;
        double zmax = boundingBox.zmin + (k+1) * cubeSize;
        double ymin = boundingBox.ymin + j * cubeSize;
        double ymax = boundingBox.ymin + (j+1) * cubeSize;
    	double xmin = boundingBox.xmin + i * cubeSize;
        double xmax = boundingBox.xmin + (i+1) * cubeSize;
        return new double[] { xmin, xmax, ymin, ymax, zmin, zmax};
    }
    
//    for (int k=0; k<2; ++k)
//    {
//        double zmin = boundingBox.zmin + k * cubeSize;
//        double zmax = boundingBox.zmin + (k+1) * cubeSize;
//        for (int j=0; j<2; ++j)
//        {
//            double ymin = boundingBox.ymin + j * cubeSize;
//            double ymax = boundingBox.ymin + (j+1) * cubeSize;
//            for (int i=0; i<2; ++i)
//            {
//                double xmin = boundingBox.xmin + i * cubeSize;
//                double xmax = boundingBox.xmin + (i+1) * cubeSize;
//                BoundingBox bb = new BoundingBox();
//                bb.xmin = xmin;
//                bb.xmax = xmax;
//                bb.ymin = ymin;
//                bb.ymax = ymax;
//                bb.zmin = zmin;
//                bb.zmax = zmax;
//                allCubes.add(bb);
//            }
//        }
//    }

    public String toString()
    {
        return "Level: " + level + "(parent node ID: " + parentNodeID + " node ID: " + levelNodeID + "; nodePath: " + nodePath + ")" + " xmin: " + xmin + " xmax: " + xmax +
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
        return new RecursiveBoundingBox(getBounds(), cubeSize);
    }
    
    public String getNodePath()
    {
    	return nodePath;
    }
    
    
    public Vector<String> findIntersectionsInChildren(vtkPolyData polydata)
    {
    	Vector<String> intersectingPaths = new Vector<String>();
    	
    	if (children == null)
		{
    		intersectingPaths.add(nodePath);
    		return intersectingPaths;
		}
    	for (RecursiveBoundingBox bbox : children)
    	{
    		if (bbox.intersects(new BoundingBox(polydata.GetBounds())))
    		{
//    			System.out.println("RecursiveBoundingBox: findIntersectionsInChildren(vtkPoly): now checking " + bbox.getNodePath());
    			intersectingPaths.addAll(bbox.findIntersectionsInChildren(polydata));
    		}
    	}
    	return intersectingPaths;
    }
    
    public Vector<String> findIntersectionsInChildren(BoundingBox boundingBox)
    {
    	Vector<String> intersectingPaths = new Vector<String>();
    	
    	if (children == null)
		{
    		intersectingPaths.add(nodePath);
    		return intersectingPaths;
		}
    	for (RecursiveBoundingBox bbox : children)
    	{
//    		System.out.println("RecursiveBoundingBox: findIntersectionsInChildren: checking intersection for " + bbox.getNodePath());
    		if (bbox.intersects(boundingBox))
    		{
//    			System.out.println("RecursiveBoundingBox: findIntersectionsInChildren: passed in bounding box matches!");
//    			System.out.println("RecursiveBoundingBox: findIntersectionsInChildren(BBox): now checking children of " + bbox.getNodePath());
    			intersectingPaths.addAll(bbox.findIntersectionsInChildren(boundingBox));
    		}
    	}
    	return intersectingPaths;
    }
    
    
    public static void main(String[] args)
    {
    	RecursiveBoundingBox bb = new RecursiveBoundingBox(new double[] {0.0, 10.0, 0.0, 10.0, 0.0, 10.0}, 10.0);
    	BoundingBox box = new BoundingBox(new double[] { 1.0, 3.0, 2.0, 4.0, 0.0, 2.0});
    	Vector<String> findIntersectionsInChildren = bb.findIntersectionsInChildren(box);
    	for (String intersections : findIntersectionsInChildren)
    	{
    		System.out.println("RecursiveBoundingBox: main: path intersection " + intersections);
    	}
    }

}


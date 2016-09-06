package edu.jhuapl.saavtk.util;

import java.util.ArrayList;
import java.util.List;

import vtk.vtkActor;
import vtk.vtkAlgorithmOutput;
import vtk.vtkDataObject;
import vtk.vtkDataSet;
import vtk.vtkMapper;
import vtk.vtkPolyDataMapper;
import vtk.vtkQuadricClustering;

/**
 * VTK actor which stores multiple mappers and where user can switch
 * between them dynamically.  User can switch between the them dynamically.
 *
 * For Paraview-like LOD switching, add observers to the render window
 * interactor for StartInteractionEvent and EndInteractionEvent that
 * searches for SaavtkLODActors in the scene and calls set methods on them
 *
 * @author twupy1
 *
 */

public class SaavtkLODActor extends vtkActor
{

    // Keeps track of the actor's mappers
    protected List<vtkMapper> mappers;

    /**
     * Constructor
     */
    public SaavtkLODActor()
    {
        super();

        // By default no mappers have been assigned
        mappers = new ArrayList<vtkMapper>();
    }

    /**
     * Get all mappers currently associated with actor
     * @return
     */
    public List<vtkMapper> getMappers()
    {
        return new ArrayList<vtkMapper>(mappers);
    }

    /**
     * Add a mapper without setting it
     * @param mapper
     * @return
     */
    public boolean addMapper(vtkMapper mapper)
    {
        // Check if we already have this exact mapper
        for(vtkMapper m : mappers)
        {
            if(m == mapper)
            {
                return false;
            }
        }

        // If we made it here, we don't currently have the mapper
        // Go ahead and add it
        mappers.add(mapper);
        return true;
    }

    /**
     * Remove a specific mapper
     * @param mapper
     * @return
     */
    public boolean removeMapper(vtkMapper mapper)
    {
        return mappers.remove(mapper);
    }

    /**
     * Removes all mappers
     */
    public void removeAllMappers()
    {
        mappers.clear();
    }

    /**
     * Adds the mapper (if not already added) and sets it
     * Naming of this method matches that of parent class instead of standard Java convention
     * @override Same as parent class's SetMapper() but also adds mapper to list
     */
    public void SetMapper(vtkMapper mapper)
    {
        // Set the mapper as usual
        super.SetMapper(mapper);

        // Add it to the list
        addMapper(mapper);
    }

    /**
     * Activates the mapper with largest cell count that does not exceed maxCellCount
     * If no such mapper is found, will try to use mapper with smallest cell count
     * If that also does not work, then will not set any mapper
     * @param maxCellCount
     * @return
     */
    public boolean selectMapper(int maxCellCount)
    {
        // Variables for keeping track of mappers
        vtkMapper currMaxMapper = null;
        vtkMapper currMinMapper = null;
        int currMaxCells = Integer.MIN_VALUE;
        int currMinCells = Integer.MAX_VALUE;

        // Check out each mapper
        for(vtkMapper m : mappers)
        {
            // Only consider non-null mappers
            if(m != null)
            {
                // Update the mapper first to get the correct dataset
                m.Update();
                vtkDataSet dataSet = m.GetInputAsDataSet();

                // Proceed if dataset is also non-null
                if(dataSet != null)
                {
                    // Save number of cells
                    int numCells = dataSet.GetNumberOfCells();

                    // Keep track of mapper with max constrained cells
                    if(numCells <= maxCellCount && currMaxCells < numCells)
                    {
                        currMaxMapper = m;
                        currMaxCells = numCells;
                    }

                    // Keep track of mapper with min cells
                    if(numCells < currMinCells)
                    {
                        currMinMapper = m;
                        currMinCells = numCells;
                    }
                }
            }
        }

        // Determine which mapper, if any, to set
        if(currMaxMapper != null)
        {
            // Found a mapper with max cell count that satisfies constraints, set it
            SetMapper(currMaxMapper);
            return true;
        }
        else if(currMinMapper != null)
        {
            // No mapper found with cell count that could satisfy constraint
            // Going with mapper that has smallest cell coutn instead
            SetMapper(currMinMapper);
            return false;
        }
        else
        {
            // No valid mappers (with datasets) have been found in general
            // Can't do anything
            return false;
        }
    }

    /**
     * Takes input data object and sets the LOD mapper as a decimated version using
     * quadric clustering where the number of divisions is auto selected
     * @return
     */
    public vtkPolyDataMapper addQuadricDecimatedLODMapper(vtkDataObject dataObject)
    {
        // Set decimator input
        vtkQuadricClustering decimator = new vtkQuadricClustering();
        decimator.SetInputDataObject(dataObject);

        // Call helper
        return addQuadricDecimatedLODMapper(decimator);
    }

    /**
     * Takes algorithm output and sets the LOD mapper as a decimated version using
     * quadric clustering where the number of divisions is auto selected
     * @return
     */
    public vtkPolyDataMapper addQuadricDecimatedLODMapper(vtkAlgorithmOutput algorithmOutput)
    {
        // Set decimator input
        vtkQuadricClustering decimator = new vtkQuadricClustering();
        decimator.SetInputConnection(algorithmOutput);

        // Call helper
        return addQuadricDecimatedLODMapper(decimator);
    }

    /**
     * Takes algorithm output and sets the LOD mapper as a decimated version using
     * quadric clustering where the number of divisions is auto selected
     * @param decimator Assumed to already have input data/connection set
     */
    private vtkPolyDataMapper addQuadricDecimatedLODMapper(vtkQuadricClustering decimator)
    {
        // Decimate the input data
        decimator.CopyCellDataOn();
        decimator.AutoAdjustNumberOfDivisionsOn();
        decimator.Update();

        // Link to mapper and save
        vtkPolyDataMapper lodMapper = new vtkPolyDataMapper();
        lodMapper.SetInputConnection(decimator.GetOutputPort());

        // Add the mapper
        addMapper(lodMapper);
        return lodMapper;
    }

    /**
     * Takes its input data object and sets the LOD mapper as a decimated version using
     * quadric clustering where the number of divisions in each x,y,z dimension is specified as input
     * @param numDivisions
     * @return
     */
    public vtkPolyDataMapper addQuadricDecimatedLODMapper(vtkDataObject dataObject, int divX, int divY, int divZ)
    {
        // Set decimator input
        vtkQuadricClustering decimator = new vtkQuadricClustering();
        decimator.SetInputDataObject(dataObject);

        // Call helper
        return addQuadricDecimatedLODMapper(decimator, divX, divY, divZ);
    }

    /**
     * Takes algorithm output and sets the LOD mapper as a decimated version using
     * quadric clustering where the number of divisions in each x,y,z dimension is specified as input
     * @param numDivisions
     * @return
     */
    public vtkPolyDataMapper addQuadricDecimatedLODMapper(vtkAlgorithmOutput algorithmOutput, int divX, int divY, int divZ)
    {
        // Set decimator input
        vtkQuadricClustering decimator = new vtkQuadricClustering();
        decimator.SetInputConnection(algorithmOutput);

        // Call helper
        return addQuadricDecimatedLODMapper(decimator, divX, divY, divZ);
    }

    /**
     * Takes algorithm output and sets the LOD mapper as a decimated version using
     * quadric clustering where the number of divisions in each x,y,z dimension is specified as input
     * @param decimator Assumed to already have input data/connection set
     * @param divX
     * @param divY
     * @param divZ
     * @return
     */
    private vtkPolyDataMapper addQuadricDecimatedLODMapper(vtkQuadricClustering decimator, int divX, int divY, int divZ)
    {
        if(divX > 1 && divY > 1 && divZ > 1)
        {
            // Decimate the input data
            decimator.CopyCellDataOn();
            decimator.SetNumberOfDivisions(divX, divY, divZ);
            decimator.Update();

            // Link to mapper and save
            vtkPolyDataMapper lodMapper = new vtkPolyDataMapper();
            lodMapper.SetInputConnection(decimator.GetOutputPort());

            // Add the mapper
            addMapper(lodMapper);

            // Let user know we were successful
            return lodMapper;
        }
        else
        {
            // Divisions are invalid
            return null;
        }
    }
}

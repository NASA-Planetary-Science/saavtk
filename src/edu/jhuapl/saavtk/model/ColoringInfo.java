package edu.jhuapl.saavtk.model;

import vtk.vtkFloatArray;
import edu.jhuapl.saavtk.model.PolyhedralModel.Format;

// Class storing info related to plate data used to color shape model
public class ColoringInfo
{
    public String coloringName = null;
    public String coloringUnits = null;
    public boolean coloringHasNulls = false;
    public int resolutionLevel = 0;
    public double[] defaultColoringRange = null;
    public double[] currentColoringRange = null;
    public vtkFloatArray coloringValues = null;
    public String coloringFile = null;
    public boolean builtIn = true;
    public Format format = Format.TXT;

    @Override
    public String toString()
    {
        String str = coloringName;
        if (coloringUnits != null && !coloringUnits.isEmpty())
            str += ", " + coloringUnits;
        if (format != Format.TXT)
            str += ", " + format.toString();
        if (coloringHasNulls)
            str += ", contains invalid data";
        if (builtIn)
            str += ", (built-in and cannot be modified)";
        return str;
    }
}

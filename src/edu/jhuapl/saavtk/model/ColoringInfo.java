package edu.jhuapl.saavtk.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.model.PolyhedralModel.Format;
import vtk.vtkFloatArray;

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
	public Format format = Format.UNKNOWN;

	ColoringData toCustomColoringData(int numberElements)
	{
		Preconditions.checkArgument(coloringValues == null || coloringValues.GetNumberOfTuples() == numberElements);
		return ColoringData.of(coloringName, coloringFile, ImmutableList.of(coloringName), coloringUnits, numberElements, coloringHasNulls, coloringValues);
	}

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

package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;

import vtk.vtkUnsignedCharArray;

/**
 * Collection of VTK based utility methods.
 *
 * @author lopeznr1
 */
public class VtkUtil
{
	/**
	 * Utility method that takes the color and sets them into the specified
	 * {@link vtkUnsignedCharArray} at the specified index, aIdx.
	 * <P>
	 * Note only 3 values (r,g,b) are set at the specified index.
	 *
	 * @param aUCA
	 * @param aIdx   The index of interest.
	 * @param aColor
	 */
	public static void setColorOnUCA3(vtkUnsignedCharArray aUCA, int aIdx, Color aColor)
	{
		aUCA.SetTuple3(aIdx, aColor.getRed(), aColor.getGreen(), aColor.getBlue());
	}

	/**
	 * Utility method that takes the color and sets them into the specified
	 * {@link vtkUnsignedCharArray} at the specified index, aIdx.
	 * <P>
	 * Note 4 values (r,g,b,a) are set at the specified index.
	 *
	 * @param aUCA
	 * @param aIdx   The index of interest.
	 * @param aColor
	 */
	public static void setColorOnUCA4(vtkUnsignedCharArray aUCA, int aIdx, Color aColor)
	{
		aUCA.SetTuple4(aIdx, aColor.getRed(), aColor.getGreen(), aColor.getBlue(), aColor.getAlpha());
	}

}

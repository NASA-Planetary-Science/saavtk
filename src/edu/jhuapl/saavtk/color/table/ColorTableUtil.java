package edu.jhuapl.saavtk.color.table;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.swing.ImageIcon;

import com.google.common.collect.ImmutableList;

import vtk.vtkColorTransferFunction;
import vtk.vtkLookupTable;

/**
 * Collection of utility methods for working with {@link ColorTable}s.
 * <P>
 * The following functionality is supported:
 * <UL>
 * <LI>Support for a getting and setting the available system
 * {@link ColorTable}s.
 * <LI>Support for a getting and setting the default system {@link ColorTable}s.
 * <LI>Creation of an icon representing a {@link ColorMapAttr} /
 * {@link ColorTable}
 * <LI>Updating a {@link vtkLookupTable} to reflect a {@link ColorMapAttr}
 * </UL>
 *
 * @author lopeznr1
 */
public class ColorTableUtil
{
	/** List of fail safe ColorTables. */
	private static ImmutableList<ColorTable> ColorTableFailSafeL = ImmutableList.of(ColorTable.Rainbow);

	/** List of (configurable) system ColorTables. */
	private static ColorTable systemColorTableDefault = null;
	private static ImmutableList<ColorTable> systemColorTableL = null;

	/**
	 * Utility method that returns an icon for the specified {@link ColorMapAttr}.
	 *
	 * @param aColorMapAttr The ColorMapAttr of interest
	 * @param aWidth        The width of the icon
	 * @param aHeight       The height of the icon
	 */
	public static ImageIcon createIcon(ColorMapAttr aColorMapAttr, int aWidth, int aHeight)
	{
		// Set up the VTK lookup table
		vtkLookupTable vColorLT = new vtkLookupTable();
		updateLookUpTable(vColorLT, aColorMapAttr);

		// Draw the icon
		BufferedImage image = new BufferedImage(aWidth, aHeight, java.awt.color.ColorSpace.TYPE_RGB);
		for (int i = 0; i < aWidth; i++)
		{
			double val = (double) i / (double) (image.getWidth() - 1);
			for (int j = 0; j < aHeight; j++)
			{
				double[] colorArr = vColorLT.GetColor(val);
				Color tmpColor = new Color((float) colorArr[0], (float) colorArr[1], (float) colorArr[2]);
				image.setRGB(i, j, tmpColor.getRGB());
			}
		}

		vColorLT.Delete();

		ImageIcon retIcon = new ImageIcon(image);
		return retIcon;
	}

	/**
	 * Utility method that returns an icon for the specified {@link ColorTable}.
	 *
	 * @param aColorTable The ColorTable of interest
	 * @param aWidth      The width of the icon
	 * @param aHeight     The height of the icon
	 */
	public static ImageIcon createIcon(ColorTable aColorTable, int aWidth, int aHeight)
	{
		// Delegate
		ColorMapAttr tmpCMA = new ColorMapAttr(aColorTable, 0.0, 1.0, 128, false);
		return createIcon(tmpCMA, aWidth, aHeight);
	}

	/**
	 * Utility method that returns the default system {@link ColorTable}
	 */
	public static synchronized ColorTable getSystemColorTableDefault()
	{
		if (systemColorTableDefault == null)
		{
			getSystemColorTableList();
			systemColorTableDefault = systemColorTableL.get(systemColorTableL.size() - 1);
		}

		return systemColorTableDefault;
	}

	/**
	 * Utility method that returns the list of system {@link ColorTable}s.
	 * <P>
	 * If no system color tables have been set then a predefined set of system
	 * colors will be located and installed.
	 */
	public static synchronized ImmutableList<ColorTable> getSystemColorTableList()
	{
		// Utilize the installed system ColorTables
		if (systemColorTableL != null)
			return systemColorTableL;

		// Locate and install the predefined ColorTables
		systemColorTableL = loadSystemColorTables();
		return systemColorTableL;
	}

	/**
	 * Utility method that sets the default system {@link ColorTable}.
	 * <P>
	 * Values of null will cause the default to be set to the original state.
	 */
	public static synchronized void setSystemColorTableDefault(ColorTable aColorTable)
	{
		systemColorTableDefault = aColorTable;
	}

	/**
	 * Utility method that returns the list of system color tables.
	 * <P>
	 * Passing in null or an empty list will result in the system {@link ColorTable}
	 * being set to fail safe setting. Fail safe settings will include the
	 * {@link ColorTable#Rainbow}
	 */
	public static synchronized void setSystemColorTableList(Collection<ColorTable> aColorTableC)
	{
		if (aColorTableC == null || aColorTableC.isEmpty() == true)
			aColorTableC = ColorTableFailSafeL;

		systemColorTableL = ImmutableList.copyOf(aColorTableC);
	}

	/**
	 * Utility method that will update the provided {@link vtkLookupTable} to
	 * reflect the specified {@link ColorMapAttr}.
	 * <P>
	 * This logic for this method originated from the class:
	 * edu.jhuapl.saavtk.colormap.RgbColormap.
	 * <P>
	 * It has been cleaned up and turned into a utility method to allow for general
	 * usage.
	 */
	public static void updateLookUpTable(vtkLookupTable aColorLT, ColorMapAttr aColorAttr)
	{
		ColorTable tmpCT = aColorAttr.getColorTable();
		List<Color> colorL = tmpCT.getColorList();
		List<Double> interpolateL = tmpCT.getInteropolateList();
		ColorSpace colorSpace = tmpCT.getColorSpace();
		Color nanColor = tmpCT.getNanColor();

		int numLevels = aColorAttr.getNumLevels();
		double dataMin = aColorAttr.getMinVal();
		double dataMax = aColorAttr.getMaxVal();
		boolean isLog = aColorAttr.getIsLogScale();

		// Bail if the values are inverted
		if (dataMax < dataMin)
			return;

		vtkColorTransferFunction ctf = new vtkColorTransferFunction();
		ctf.SetAlpha(1);
		if (isLog)
			ctf.SetScaleToLog10();
		else
			ctf.SetScaleToLinear();
		switch (colorSpace)
		{
			case RGB:
				ctf.SetColorSpaceToRGB();
				break;
			case HSV:
				ctf.SetColorSpaceToHSV();
				break;
			case LAB:
				ctf.SetColorSpaceToLab();
				break;
			case DIVERGING:
				ctf.SetColorSpaceToDiverging();
				break;
			default:
				ctf.SetColorSpaceToRGB();
		}
		double rangeMin = Float.POSITIVE_INFINITY;
		double rangeMax = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < interpolateL.size(); i++)
		{
			if (interpolateL.get(i) < rangeMin)
				rangeMin = interpolateL.get(i);
			if (interpolateL.get(i) > rangeMax)
				rangeMax = interpolateL.get(i);
		}
		for (int i = 0; i < interpolateL.size(); i++)
		{
			Color c = colorL.get(i);
			float[] comp = c.getRGBComponents(null);
			ctf.AddRGBPoint((interpolateL.get(i) - rangeMin) / (rangeMax - rangeMin), comp[0], comp[1], comp[2]);
		}

		aColorLT.GlobalWarningDisplayOff();
		ctf.GlobalWarningDisplayOff();

		float[] comp = nanColor.getRGBColorComponents(null);
		aColorLT.SetNanColor(comp[0], comp[1], comp[2], 1);
		aColorLT.SetNumberOfTableValues(numLevels);
		aColorLT.ForceBuild();

		if (isLog)
		{
			double minValue = dataMin;
			double maxValue = dataMax;
			if (minValue < 0)
				minValue = Double.MIN_VALUE;
			if (maxValue < 0)
				maxValue = Double.MIN_VALUE;
			aColorLT.SetTableRange(minValue, maxValue);
			aColorLT.SetValueRange(minValue, maxValue);
			aColorLT.SetRange(minValue, maxValue);
//			System.out.println("Warning: negative values in range clipped to smallest possible value greater than zero ("
//					+ Double.MIN_VALUE + ")");
		}
		else
		{
			aColorLT.SetTableRange(dataMin, dataMax);
			aColorLT.SetValueRange(dataMin, dataMax);
			aColorLT.SetRange(dataMin, dataMax);
		}

		if (isLog)
			aColorLT.SetScaleToLog10();
		else
			aColorLT.SetScaleToLinear();

		for (int i = 0; i < numLevels; i++)
		{
			double val = i / (double) numLevels;// *(dataMax-dataMin)+dataMin;
			// lut.SetTableValue(i, ctf.GetColor(val));
			double[] col = ctf.GetColor(val);
			aColorLT.SetTableValue(i, col[0], col[1], col[2], 1);
		}

	}

	/**
	 * Utility helper method that loads and returns the preinstalled
	 * {@link ColorTable}s.
	 * <P>
	 * On failure a fail safe list will be returned.
	 */
	private static ImmutableList<ColorTable> loadSystemColorTables()
	{
		URL colorMapURL = ClassLoader.getSystemResource("resources/colors/ColorMaps.xml");
		if (colorMapURL == null)
		{
			System.err.println("Failed to locate predefined ColorMaps.xml");
			return ColorTableFailSafeL;
		}

		try (InputStream tmpStream = colorMapURL.openStream())
		{
			// Delegate
			List<ColorTable> tmpItemL = ColorTableIoUtil.loadFromXml(tmpStream);

			// Return the ordered ColorTables
			tmpItemL.sort(Comparator.comparing(ColorTable::getName));
			return ImmutableList.copyOf(tmpItemL);
		}
		catch (IOException aExp)
		{
			aExp.printStackTrace();
		}

		// Return the fail safe list
		return ColorTableFailSafeL;
	}

}

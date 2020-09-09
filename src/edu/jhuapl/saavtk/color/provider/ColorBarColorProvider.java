package edu.jhuapl.saavtk.color.provider;

import java.awt.Color;

import edu.jhuapl.saavtk.color.table.ColorMapAttr;
import edu.jhuapl.saavtk.color.table.ColorTableUtil;
import edu.jhuapl.saavtk.feature.FeatureType;
import vtk.vtkLookupTable;

/**
 * ColorProvider where the returned color will be a function of the specified
 * value and the reference {@link ColorMapAttr}.
 * <P>
 * A reference {@link FeatureType} is defined but is not used in actual color
 * computations. The FeatureType serves as a reference to the feature being
 * colored.
 *
 * @author lopeznr1
 */
public class ColorBarColorProvider implements ColorProvider
{
	// Attributes
	private final ColorMapAttr refColorMapAttr;
	private final FeatureType refFeatureType;

	// Vtk vars
	private final vtkLookupTable vColorLT;

	/** Standard Constructor */
	public ColorBarColorProvider(ColorMapAttr aColorMapAttr, FeatureType aFeatureType)
	{
		refColorMapAttr = aColorMapAttr;
		refFeatureType = aFeatureType;

		// Synthesize a vtkLookupTable scaled to range: [0.0, 1.0]
		ColorMapAttr tmpCMA = new ColorMapAttr(aColorMapAttr.getColorTable(), 0.0, 1.0, aColorMapAttr.getNumLevels(),
				aColorMapAttr.getIsLogScale());
		vColorLT = new vtkLookupTable();
		ColorTableUtil.updateLookUpTable(vColorLT, tmpCMA);
	}

	/**
	 * Returns the reference {@link ColorMapAttr}.
	 */
	public ColorMapAttr getColorMapAttr()
	{
		return refColorMapAttr;
	}

	@Override
	public Color getBaseColor()
	{
		// Color bars have no base line color
		return null;
	}

	@Override
	public Color getColor(double aMinVal, double aMaxVal, double aTargVal)
	{
		double minVal = refColorMapAttr.getMinVal();
		double maxVal = refColorMapAttr.getMaxVal();

		// Determine if we should just use the NaN color
		boolean isNaNColor = false;
		isNaNColor |= Double.isFinite(aTargVal) == false;
		isNaNColor |= Double.isNaN(aTargVal) == true;
		isNaNColor |= Double.isNaN(minVal) == true;
		isNaNColor |= Double.isNaN(maxVal) == true;
		isNaNColor |= minVal == maxVal;
		if (isNaNColor == true)
			return refColorMapAttr.getColorTable().getNanColor();

		// Rescale aTargVal to the range: [0.0, 1.0]
		// Note this will not work well if the scale is log based
		double tmpVal = (aTargVal - minVal) / (maxVal - minVal);

		double[] rgbArr = vColorLT.GetColor(tmpVal);
		return new Color((float) rgbArr[0], (float) rgbArr[1], (float) rgbArr[2]);
	}

	@Override
	public FeatureType getFeatureType()
	{
		return refFeatureType;
	}

}

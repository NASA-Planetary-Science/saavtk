package edu.jhuapl.saavtk.feature;

import java.util.Arrays;

/**
 * Class used to collect values associated with a specific Feature.
 * <P>
 * This class is mutable and should only be used during "construction" time of
 * FeatureAttr.
 *
 * @author lopeznr1
 */
public class FeatureAttrBuilder
{
	// State vars
	private double minVal;
	private double maxVal;
	private double[] valArr;
	private int numPts;

	/**
	 * Standard Constuctor
	 */
	public FeatureAttrBuilder(int aExpectedNumPts)
	{
		minVal = Double.POSITIVE_INFINITY;
		maxVal = Double.NEGATIVE_INFINITY;
		valArr = new double[aExpectedNumPts];
		numPts = 0;
	}

	/**
	 * Simplified Constructor
	 */
	public FeatureAttrBuilder()
	{
		this(16 * 1024);
	}

	/**
	 * Adds a value to this builder.
	 *
	 * @param aVal
	 */
	public void addValue(double aVal)
	{
		// Store the value
		if (valArr.length <= numPts)
			valArr = Arrays.copyOf(valArr, valArr.length * 2);
		valArr[numPts] = aVal;

		// Update stats
		minVal = (aVal < minVal) ? aVal : minVal;
		maxVal = (aVal > maxVal) ? aVal : maxVal;

		numPts++;
	}

	/**
	 * Builds the corresponding FeatureAttr.
	 * <P>
	 * Note that once this method is called this builder will be invalidated and no
	 * more values can be added to this builder.
	 */
	public FeatureAttr build()
	{
		// Get copy of internal state
		double tmpMinVal = minVal;
		double tmpMaxVal = maxVal;
		double[] tmpArr = valArr;
		if (tmpArr.length != numPts)
			tmpArr = Arrays.copyOf(tmpArr, numPts);

		// Trash internal state
		valArr = null;
		minVal = Double.NaN;
		maxVal = Double.NaN;
		numPts = Integer.MAX_VALUE;

		return new PlainFeatureAttr(tmpArr, tmpMinVal, tmpMaxVal);
	}

	/**
	 * Returns the minimum value associated with the feature.
	 */
	public double getMinVal()
	{
		return minVal;
	}

	/**
	 * Returns the maximum value associated with the feature.
	 */
	public double getMaxVal()
	{
		return maxVal;
	}

	/**
	 * Returns the number of items in this FeaturAttrBuilder
	 */
	public int getNumItems()
	{
		return numPts;
	}

}

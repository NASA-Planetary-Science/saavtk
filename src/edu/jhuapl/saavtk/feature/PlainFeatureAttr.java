package edu.jhuapl.saavtk.feature;

/**
 * Immutable implementation of FeatureAttr that is backed by a fixed double
 * array.
 *
 * @author lopeznr1
 */
public class PlainFeatureAttr implements FeatureAttr
{
	// Attributes
	private final double minVal;
	private final double maxVal;
	private final double[] valArr;

	/**
	 * Standard Constructor
	 *
	 * @param aValArr An array containing all of the values associated with the
	 *                feature. This array must be effectively immutable since a copy
	 *                will NOT be made.
	 * @param aMinVal The minimum value that occurs in aValArr.
	 * @param aMaxVal The maximum value that occurs in aValArr.
	 */
	public PlainFeatureAttr(double[] aValArr, double aMinVal, double aMaxVal)
	{
		minVal = aMinVal;
		maxVal = aMaxVal;
		valArr = aValArr;
	}

	/**
	 * Standard Constructor
	 *
	 * @param aValArr An array containing all of the values associated with the
	 *                feature. This array must be effectively immutable since a copy
	 *                will NOT be made.
	 */
	public PlainFeatureAttr(double[] aValArr)
	{
		double tmpMin = Double.POSITIVE_INFINITY;
		double tmpMax = Double.NEGATIVE_INFINITY;
		for (double aVal : aValArr)
		{
			tmpMin = (aVal < tmpMin) ? aVal : tmpMin;
			tmpMax = (aVal > tmpMax) ? aVal : tmpMax;
		}

		valArr = aValArr;
		minVal = tmpMin;
		maxVal = tmpMax;
	}

	@Override
	public void dispose()
	{
		; // Nothing to do
	}

	@Override
	public double getMinVal()
	{
		return minVal;
	}

	@Override
	public double getMaxVal()
	{
		return maxVal;
	}

	@Override
	public int getNumVals()
	{
		return valArr.length;
	}

	@Override
	public double getValAt(int aIdx)
	{
		return valArr[aIdx];
	}

}

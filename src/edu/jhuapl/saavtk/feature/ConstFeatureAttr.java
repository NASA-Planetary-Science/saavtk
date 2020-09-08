package edu.jhuapl.saavtk.feature;

/**
 * Immutable implementation of FeatureAttr that always returns the same value.
 *
 * @author lopeznr1
 */
public class ConstFeatureAttr implements FeatureAttr
{
	// Attributes
	private final int numVals;
	private final double minVal;
	private final double maxVal;
	private final double constVal;

	/**
	 * Standard Constructor
	 *
	 * @param aValArr An array containing all of the values associated with the
	 *                feature. This array must be effectively immutable since a copy
	 *                will NOT be made.
	 * @param aMinVal The minimum value that occurs in aValArr.
	 * @param aMaxVal The maximum value that occurs in aValArr.
	 */
	public ConstFeatureAttr(int aNumVals, double aMinVal, double aMaxVal, double aConstVal)
	{
		numVals = aNumVals;
		minVal = aMinVal;
		maxVal = aMaxVal;
		constVal = aConstVal;
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
		return numVals;
	}

	@Override
	public double getValAt(int aIdx)
	{
		return constVal;
	}

}

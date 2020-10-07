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
	 * @param aNumVals  The number of (constant) values contained by this
	 *                  {@link FeatureAttr}.
	 * @param aMinVal   The minimum value that would have potentially occurred in
	 *                  this {@link FeatureAttr}.
	 * @param aMinVal   The maximum value that would have potentially occurred in
	 *                  this {@link FeatureAttr}.
	 * @param aConstVal The constant value that all values will be equal to.
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

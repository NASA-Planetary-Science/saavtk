package edu.jhuapl.saavtk.feature;

/**
 * Invalid FeatrueAttr useful as a test harness / place holder.
 *
 * @author lopeznr1
 */
public class InvalidFeatureAttr implements FeatureAttr
{
	/** Singleton instance */
	public static final InvalidFeatureAttr Instance = new InvalidFeatureAttr();

	private InvalidFeatureAttr()
	{
		; // Nothing to do
	}

	@Override
	public void dispose()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double getMinVal()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double getMaxVal()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int getNumVals()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double getValAt(int aIdx)
	{
		throw new UnsupportedOperationException();
	}

}

package edu.jhuapl.saavtk.feature;

/**
 * Interface that defines the methods to access properties associated with a
 * physical quality. Implementations of this attribute should be immutable.
 *
 * Currently the following access is provided:
 * <UL>
 * <LI>All values associated with a feature
 * <LI>The minimum value associated with the feature
 * <LI>The maximum value associated with the feature
 * <UL>
 *
 * @author lopeznr1
 */
public interface FeatureAttr
{
	/**
	 * Notification that this object will not be used again.
	 * <P>
	 * This method will release any low level system resources. After this method is
	 * called the state of this object may become undefined.
	 */
	public void dispose();

	/**
	 * Returns the minimum value associated with the feature.
	 */
	public double getMinVal();

	/**
	 * Returns the maximum value associated with the feature.
	 */
	public double getMaxVal();

	/**
	 * Returns the number of values associated with this feature.
	 */
	public int getNumVals();

	/**
	 * Returns the value at the specified index.
	 */
	public double getValAt(int aIdx);

}

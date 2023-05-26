package edu.jhuapl.saavtk.feature;

import vtk.vtkDoubleArray;

/**
 * Immutable implementation of FeatureAttr that is backed by a vtkDoubleArray.
 * <P>
 * It is imperative that there are no outside references to the provided
 * vtkDoubleArray in order to enforce the immutablity of this object.
 *
 * @author lopeznr1
 */
public class VtkFeatureAttr implements FeatureAttr
{
	// Attributes
	private final vtkDoubleArray refValueDA;
	private final double minVal;
	private final double maxVal;
	private final int numPts;

	/**
	 * Standard Constructor
	 *
	 * @param aValueDA
	 */
	public VtkFeatureAttr(vtkDoubleArray aValueDA)
	{
		refValueDA = aValueDA;

		double[] valueArr = refValueDA.GetValueRange();
		minVal = valueArr[0];
		maxVal = valueArr[1];

		numPts = refValueDA.GetMaxId() + 1;
	}

	/**
	 * Causes the backing vtkDoubleArray to be deleted.
	 * <P>
	 * It is imperative that no further calls to {@link #getValAt()} are made as the
	 * backing array will be released!
	 */
	@Override
	public void dispose()
	{
		refValueDA.Delete();
	}

	/**
	 * Causes the backing vtkDoubleArray to be deleted.
	 * <P>
	 * It is imperative that no further calls to {@link #getValAt()} are made as the
	 * backing array will be released!
	 */
	public void delete()
	{
		refValueDA.Delete();
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
		return numPts;
	}

	@Override
	public double getValAt(int aIdx)
	{
		return refValueDA.GetValue(aIdx);
	}

}

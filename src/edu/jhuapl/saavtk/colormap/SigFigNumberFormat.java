package edu.jhuapl.saavtk.colormap;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

/**
 * NumberFormat used to display values with a desired number of significant
 * figures.
 * <P>
 * TODO: This class is incomplete and was rushed just to support simple display
 * of values with a requested number of significant digits. This class should be
 * in a different package when completed.
 */
public class SigFigNumberFormat extends NumberFormat
{
	// Constants
	private static final long serialVersionUID = 1L;

	// Attributes
	private final int numSigFigs;

	/**
	 * Standard Constructor
	 * 
	 * @param aNumSigFigs The number of significant figures to display
	 */
	public SigFigNumberFormat(int aNumSigFigs)
	{
		numSigFigs = aNumSigFigs;
	}

	@Override
	public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition)
	{

		BigDecimal tmpBD = getValueRoundedToSigFigs(number, numSigFigs);
		return result.append("" + tmpBD);
	}

	@Override
	public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos)
	{
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public Number parse(String source, ParsePosition parsePosition)
	{
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	/**
	 * Utility method that returns a BigDecimal rounded to the specified number of
	 * significant figures.
	 * <P>
	 * Source: https://stackoverflow.com/questions/7548841
	 * <P>
	 * TODO: Consider promoting this method to a utility class.
	 * 
	 * @param aValue      The double value to be rounded
	 * @param aNumSigFigs The number of significant figures of interest.
	 */
	private static BigDecimal getValueRoundedToSigFigs(double aValue, int aNumSigFigs)
	{
		BigDecimal retBD = new BigDecimal(aValue);
		retBD = retBD.round(new MathContext(aNumSigFigs));

		return retBD;
	}

}
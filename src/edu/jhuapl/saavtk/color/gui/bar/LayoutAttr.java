package edu.jhuapl.saavtk.color.gui.bar;

import edu.jhuapl.saavtk.color.painter.ColorBarPainter;
import plotkit.cadence.Cadence;

/**
 * Immutable class that defines the attributes associated with the layout of a
 * {@link ColorBarPainter} configuration.
 *
 * @author lopeznr1
 */
public class LayoutAttr
{
	// Constants
	/** The "invalid" layout attribute */
	public static final LayoutAttr Invalid = new LayoutAttr(false, false, 0, 0, 0, false, Cadence.Invalid);

	// Attributes
	private final boolean isHorizontal;
	private final boolean isReverseOrder;

	private final int numTicks;
	private final int barLength;
	private final int barWidth;

	private final boolean isCadenceEnabled;
	private final Cadence cadence;

	/** Standard Constructor */
	public LayoutAttr(boolean aIsHorizontal, boolean aIsReverseOrder, int aNumTicks, int aBarLength, int aBarWidth,
			boolean aIsCadenceEnabled, Cadence aCadence)
	{
		isHorizontal = aIsHorizontal;
		isReverseOrder = aIsReverseOrder;

		numTicks = aNumTicks;
		barLength = aBarLength;
		barWidth = aBarWidth;

		isCadenceEnabled = aIsCadenceEnabled;
		cadence = aCadence;
	}

	/**
	 * Returns a {@link LayoutAttr} equal to this one but with the specified number
	 * of ticks.
	 */
	public LayoutAttr cloneWithNumberOfTicks(int aNumTicks)
	{
		return new LayoutAttr(isHorizontal, isReverseOrder, aNumTicks, barLength, barWidth, isCadenceEnabled, cadence);
	}

	public boolean getIsHorizontal()
	{
		return isHorizontal;
	}

	public boolean getIsReverseOrder()
	{
		return isReverseOrder;
	}

	public int getNumTicks()
	{
		return numTicks;
	}

	public int getBarLength()
	{
		return barLength;
	}

	public int getBarWidth()
	{
		return barWidth;
	}

	public Cadence getCadence()
	{
		return cadence;
	}

	public boolean getIsCadenceEnabled()
	{
		return isCadenceEnabled;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + barLength;
		result = prime * result + barWidth;
		result = prime * result + ((cadence == null) ? 0 : cadence.hashCode());
		result = prime * result + (isCadenceEnabled ? 1231 : 1237);
		result = prime * result + (isHorizontal ? 1231 : 1237);
		result = prime * result + (isReverseOrder ? 1231 : 1237);
		result = prime * result + numTicks;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LayoutAttr other = (LayoutAttr) obj;
		if (barLength != other.barLength)
			return false;
		if (barWidth != other.barWidth)
			return false;
		if (cadence == null)
		{
			if (other.cadence != null)
				return false;
		}
		else if (!cadence.equals(other.cadence))
			return false;
		if (isCadenceEnabled != other.isCadenceEnabled)
			return false;
		if (isHorizontal != other.isHorizontal)
			return false;
		if (isReverseOrder != other.isReverseOrder)
			return false;
		if (numTicks != other.numTicks)
			return false;
		return true;
	}

}

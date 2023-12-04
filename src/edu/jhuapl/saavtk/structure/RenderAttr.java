package edu.jhuapl.saavtk.structure;

/**
 * Record that defines the attributes associated with rendering a list of {@link Structure}s.
 * <p>
 * The following attributes are defined:
 * <ul>
 * <li>lineWidth: The width of the rendered lines
 * <li>radialOffset: The offset to shift all items (radially) from the surface
 * <li>numRoundSides: Defines the number of straight edges used for a round structure
 * <li>numPointSides: Defines the number of straight edges used for a point structure
 * <li>pointRadius: The radius for point structures
 * </ul>
 *
 * @author lopeznr1
 */
public record RenderAttr(double lineWidth, double radialOffset, int numRoundSides, int numPointSides,
		double pointRadius)
{
	/** Returns a copy of this object but with the alternative lineWidth. */
	public RenderAttr withLineWidth(double aLineWidth)
	{
		return new RenderAttr(aLineWidth, radialOffset, numRoundSides, numPointSides, pointRadius);
	}

	/** Returns a copy of this object but with the alternative normalOffset. */
	public RenderAttr withRadialOffset(double aOffset)
	{
		return new RenderAttr(lineWidth, aOffset, numRoundSides, numPointSides, pointRadius);
	}

}

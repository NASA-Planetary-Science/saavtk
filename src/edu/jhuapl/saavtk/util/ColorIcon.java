package edu.jhuapl.saavtk.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

/**
 * Immutable class used to render an icon with a solid color and draw a thin
 * border.
 */
public class ColorIcon implements Icon
{
	// Attributes
	private final Color fillColor;
	private final Color drawColor;
	private final int width;
	private final int height;

	/**
	 * Standard Constructor
	 * 
	 * @param aFillColor The color used to fill the icon.
	 * @param aDrawColor The color used to draw the border.
	 * @param aWidth     The width of the icon.
	 * @param aHeight    The height of the icon.
	 */
	public ColorIcon(Color aFillColor, Color aDrawColor, int aWidth, int aHeight)
	{
		fillColor = aFillColor;
		drawColor = aDrawColor;
		width = aWidth;
		height = aHeight;
	}

	/**
	 * Simplified Constructor: Forms a ColorIcon with default dimensions of 12x12
	 * and a draw color of black.
	 * 
	 * @param aFillColor The fill color of the icon.
	 */
	public ColorIcon(Color aFillColor)
	{
		this(aFillColor, Color.BLACK, 12, 12);
	}

	/**
	 * Returns the color of the icon.
	 */
	public Color getColor()
	{
		return fillColor;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y)
	{
		Graphics2D g2d = (Graphics2D) g.create();

		g2d.setColor(fillColor);
		g2d.fillRect(x + 1, y + 1, width - 2, height - 2);

		g2d.setColor(drawColor);
		g2d.drawRect(x + 1, y + 1, width - 2, height - 2);

		g2d.dispose();
	}

	@Override
	public int getIconWidth()
	{
		return width;
	}

	@Override
	public int getIconHeight()
	{
		return height;
	}

}
package edu.jhuapl.saavtk.colormap;

import java.awt.Color;
import java.awt.Component;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * Collection of utilities useful for working with Colormaps.
 */
public class ColormapUtil
{
	/**
	 * Utility method that returns an icon for the specified Colormap.
	 *
	 * @param aColormap The color map of interest
	 * @param aWidth    The width of the icon
	 * @param aHeight   The height of the icon
	 */
	public static ImageIcon createIcon(Colormap aColormap, int aWidth, int aHeight)
	{
		aColormap.setRangeMin(0);
		aColormap.setRangeMax(1);
		BufferedImage image = new BufferedImage(aWidth, aHeight, ColorSpace.TYPE_RGB);
		for (int i = 0; i < aWidth; i++)
		{
			double val = (double) i / (double) (image.getWidth() - 1);
			for (int j = 0; j < aHeight; j++)
				image.setRGB(i, j, aColormap.getColor(val).getRGB());
		}
		return new ImageIcon(image);
	}

	/**
	 * Utility method that returns a ListCellRenderer used to render fancy color map
	 * labels.
	 * <P>
	 * Fancy color map labels include an iconic representation of the actual
	 * Colormap.
	 * <P>
	 * This is the original version.
	 */
	public static ListCellRenderer<Colormap> getFancyColormapRender()
	{
		return new FancyColormapComboBoxRenderer();
	}

	/**
	 * Class that provides the (original) custom fancy Renderer for the Colormap ComboBox.
	 */
	private static class FancyColormapComboBoxRenderer extends JLabel implements ListCellRenderer<Colormap>
	{
		// Constants
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<? extends Colormap> list, Colormap value, int index,
				boolean isSelected, boolean cellHasFocus)
		{
			if (isSelected)
			{
				setBackground(Color.DARK_GRAY);
				setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
			}
			else
			{
				setBackground(list.getBackground());
				setBorder(null);
			}

			setIcon(ColormapUtil.createIcon(value, 100, 30));
			setText(value.getName());
			return this;
		}

	}

}

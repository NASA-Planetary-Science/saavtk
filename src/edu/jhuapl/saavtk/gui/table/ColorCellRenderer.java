package edu.jhuapl.saavtk.gui.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 * {@link TableCellRenderer} used to display a filled color rectangle for a
 * table cell where the data model is a {@link Color}
 */
public class ColorCellRenderer extends JLabel implements TableCellRenderer
{
	// Constants
	private static final long serialVersionUID = 1L;

	// State vars
	private boolean showToolTips;

	// Cache vars
	private Border cUnselectedBorder = null;
	private Border cSelectedBorder = null;

	public ColorCellRenderer(boolean aShowToolTips)
	{
		showToolTips = aShowToolTips;

		setOpaque(true); // MUST do this for background to show up.
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus,
			int row, int column)
	{
		Color newColor = (Color) color;
		setBackground(newColor);

		if (isSelected)
		{
			if (cSelectedBorder == null)
				cSelectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
			setBorder(cSelectedBorder);
		}
		else
		{
			if (cUnselectedBorder == null)
				cUnselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
			setBorder(cUnselectedBorder);
		}

		String toolTipStr = null;
		if (showToolTips == true && newColor != null)
			toolTipStr = "RGB value: " + newColor.getRed() + ", " + newColor.getGreen() + ", " + newColor.getBlue();
		setToolTipText(toolTipStr);

		return this;
	}

}

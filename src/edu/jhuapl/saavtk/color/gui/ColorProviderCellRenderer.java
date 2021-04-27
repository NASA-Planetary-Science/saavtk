package edu.jhuapl.saavtk.color.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

import edu.jhuapl.saavtk.color.provider.ColorProvider;

/**
 * Class that used to display a filled color rectangle for a table cell where
 * the data model is a {@link ColorProvider}.
 *
 * @author lopeznr1
 */
public class ColorProviderCellRenderer extends DefaultTableCellRenderer
{
	// Constants
	private static final long serialVersionUID = 1L;

	// Attributes
	private final boolean showToolTips;

	// Cache vars
	private Border cSelectedBorder;
	private Border cUnselectedBorder;

	/** Standard Constructor */
	public ColorProviderCellRenderer(boolean aShowToolTips)
	{
		showToolTips = aShowToolTips;

		cSelectedBorder = null;
		cUnselectedBorder = null;
	}

	@Override
	public Component getTableCellRendererComponent(JTable aTable, Object aObject, boolean isSelected, boolean hasFocus,
			int aRow, int aColumn)
	{
		JLabel retComp = (JLabel) super.getTableCellRendererComponent(aTable, aObject, isSelected, hasFocus, aRow,
				aColumn);

		ColorProvider tmpCP = (ColorProvider) aObject;

		String tmpMsg = null;
		Color tmpColor = null;
		if (tmpCP != null)
		{
			tmpColor = tmpCP.getBaseColor();
			if (tmpColor == null)
				tmpMsg = "---";
		}

		retComp.setBackground(tmpColor);
		retComp.setHorizontalAlignment(JLabel.CENTER);
		retComp.setText(tmpMsg);

		boolean isOpaque = tmpColor != null;
		retComp.setOpaque(isOpaque);

		Border tmpBorder = null;
		if (isSelected == true)
		{
			if (cSelectedBorder == null)
				cSelectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, aTable.getSelectionBackground());
			tmpBorder = cSelectedBorder;
		}
		else
		{
			if (cUnselectedBorder == null)
				cUnselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, aTable.getBackground());
			tmpBorder = cUnselectedBorder;
		}
		retComp.setBorder(tmpBorder);

		String toolTipStr = null;
		if (showToolTips == true && tmpColor != null)
			toolTipStr = "RGB value: " + tmpColor.getRed() + ", " + tmpColor.getGreen() + ", " + tmpColor.getBlue();
		retComp.setToolTipText(toolTipStr);

		return retComp;
	}
}

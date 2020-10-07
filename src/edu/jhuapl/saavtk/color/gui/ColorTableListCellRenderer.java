package edu.jhuapl.saavtk.color.gui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import edu.jhuapl.saavtk.color.table.ColorTable;
import edu.jhuapl.saavtk.color.table.ColorTableUtil;

/**
 * Implementation of {@link ListCellRenderer} used to render a
 * {@link ColorTable}.
 * <P>
 * This renderer will show the color table (as an iconic representation).
 *
 * @author lopeznr1
 */
public class ColorTableListCellRenderer extends DefaultListCellRenderer
{
	// Attributes
	private final JComboBox<?> refParentBox;

	/** Standard Constructor */
	public ColorTableListCellRenderer(JComboBox<?> aParentBox)
	{
		refParentBox = aParentBox;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object aObj, int index, boolean isSelected,
			boolean hasFocus)
	{
		JLabel retL = (JLabel) super.getListCellRendererComponent(list, aObj, index, isSelected, hasFocus);
		if (aObj instanceof ColorTable == false)
			return retL;

		ColorTable tmpColorTable = (ColorTable) aObj;
		String tmpStr = tmpColorTable.getName();
		retL.setText(tmpStr);

		int iconW = 100;
		int iconH = refParentBox.getHeight();
		if (iconH < 16)
			iconH = 16;
		iconH = 16;

		Icon tmpIcon = null;
		if (index != -1)
			tmpIcon = ColorTableUtil.createIcon(tmpColorTable, iconW, iconH);
		retL.setIcon(tmpIcon);

		return retL;
	}
}
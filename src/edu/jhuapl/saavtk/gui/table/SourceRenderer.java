package edu.jhuapl.saavtk.gui.table;

import java.awt.Component;
import java.io.File;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * {@link TableCellRenderer} used to display the source.
 * <P>
 * If the source is a file, then the file name will be displayed while the tool
 * tip will reveal the parent folder.
 *
 * @author lopeznr1
 */
public class SourceRenderer extends DefaultTableCellRenderer
{
	// Constants
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object aItem, boolean isSelected, boolean hasFocus,
			int row, int column)
	{
		String regularStr = null;
		String tooltipStr = null;

		if (aItem instanceof File)
		{
			File tmpFile = (File) aItem;
			regularStr = tmpFile.getName();
			tooltipStr = tmpFile.getParent();
		}

//		setText(regularStr);
		setToolTipText(tooltipStr);

		return super.getTableCellRendererComponent(table, regularStr, isSelected, hasFocus, row, column);
	}

}

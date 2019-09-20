package edu.jhuapl.saavtk.gui.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import edu.jhuapl.saavtk.gui.dialog.ColorChooser;

/**
 * {@link TableCellEditor} used to edit colors for a table cell where the data
 * model is a {@link Color}. It will be activated only if one item is selected.
 * <P>
 * This editor, when activated will present a popup color chooser dialog.
 *
 * @author lopeznr1
 */
public class ColorCellEditor extends AbstractCellEditor implements TableCellEditor
{
	// Constants
	private static final long serialVersionUID = 1L;

	// Gui vars
	private ColorCellRenderer dispComp;

	// State vars
	private Color currColor;

	/**
	 * Standard Constructor
	 */
	public ColorCellEditor()
	{
		dispComp = new ColorCellRenderer(false);

		currColor = null;
	}

	@Override
	public Object getCellEditorValue()
	{
		return currColor;
	}

	@Override
	public Component getTableCellEditorComponent(JTable aTable, Object aValue, boolean aIsSelected, int aRow, int aCol)
	{
		// Bail if we are not selected
		if (aIsSelected == false)
			return null;

		// Color editing will only be allowed if 1 item is selected
		if (aTable.getSelectedRows().length != 1)
			return null;

		// Prompt the user to select a color
		Color oldColor = (Color) aValue;
		Color tmpColor = ColorChooser.showColorChooser(JOptionPane.getFrameForComponent(aTable), oldColor);
		if (tmpColor == null)
			return null;

		// Update our internal renderer to display the user's selection
		currColor = tmpColor;
		dispComp.getTableCellRendererComponent(aTable, tmpColor, aIsSelected, false, aRow, aCol);

		// There is no further editing since it occurs within the popup dialog
		// Note, stopCellEditing must be called after all pending AWT events
		SwingUtilities.invokeLater(() -> stopCellEditing());

		return dispComp;
	}

}

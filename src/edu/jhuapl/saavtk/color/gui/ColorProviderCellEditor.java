package edu.jhuapl.saavtk.color.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import edu.jhuapl.saavtk.color.provider.ColorProvider;
import edu.jhuapl.saavtk.color.provider.ConstColorProvider;
import edu.jhuapl.saavtk.gui.dialog.ColorChooser;

/**
 * {@link TableCellEditor} used to edit colors for a table cell where the data
 * model is a {@link ColorProvider}. It will be activated only if one item is
 * selected.
 * <P>
 * This editor, when activated will present a popup color chooser dialog.
 *
 * @author lopeznr1
 */
public class ColorProviderCellEditor<G1> extends AbstractCellEditor implements TableCellEditor
{
	// Constants
	private static final long serialVersionUID = 1L;

	// Gui vars
	private ColorProviderCellRenderer dispComp;

	// State vars
	private ColorProvider currCP;

	/**
	 * Standard Constructor
	 */
	public ColorProviderCellEditor()
	{
		dispComp = new ColorProviderCellRenderer(false);

		currCP = null;
	}

	@Override
	public Object getCellEditorValue()
	{
		return currCP;
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
		Color oldColor = ((ColorProvider) aValue).getBaseColor();
		if (oldColor == null)
			oldColor = Color.BLACK;
		Color tmpColor = ColorChooser.showColorChooser(JOptionPane.getFrameForComponent(aTable), oldColor);
		if (tmpColor == null)
			return null;

		// Update our internal renderer to display the user's selection
		currCP = new ConstColorProvider(tmpColor);
		dispComp.getTableCellRendererComponent(aTable, currCP, aIsSelected, false, aRow, aCol);

		// There is no further editing since it occurs within the popup dialog
		// Note, stopCellEditing must be called after all pending AWT events
		SwingUtilities.invokeLater(() -> {
			stopCellEditing();
		});

		return dispComp;
	}

}

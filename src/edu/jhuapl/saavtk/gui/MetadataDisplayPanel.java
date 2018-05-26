package edu.jhuapl.saavtk.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.google.common.base.Preconditions;

import edu.jhuapl.saavtk.metadata.Key;
import edu.jhuapl.saavtk.metadata.Metadata;
import edu.jhuapl.saavtk.metadata.SettableMetadata;
import edu.jhuapl.saavtk.metadata.Version;

public class MetadataDisplayPanel
{
	//	private static final int EM_WIDTH = new JLabel("M").getPreferredSize().width;
	//	private static final int EM_HEIGHT = new JLabel("M").getPreferredSize().height;
	private static final double VERT_SCROLL_BAR_WIDTH = new JScrollBar(JScrollBar.VERTICAL).getPreferredSize().getWidth();
	private static final double HOR_SCROLL_BAR_HEIGHT = new JScrollBar(JScrollBar.HORIZONTAL).getPreferredSize().getHeight();
	private final JPanel jPanel;

	protected MetadataDisplayPanel(String keyColumnLabel, String valueColumnLabel, Collection<Key<?>> keys, Metadata metadata)
	{
		Preconditions.checkNotNull(keyColumnLabel);
		Preconditions.checkNotNull(valueColumnLabel);
		Preconditions.checkNotNull(keys);
		Preconditions.checkNotNull(metadata);

		JPanel jPanel = new JPanel();
		jPanel.setLayout(new GridBagLayout());

		// Keep track of size needed to display the table without scrollbars.
		final Dimension dim0 = getDimension(keyColumnLabel);
		dim0.setSize(dim0.getWidth(), dim0.getHeight() + 5.); // Title row seems to need a little padding.
		final Dimension dim1 = getDimension(valueColumnLabel);
		dim1.setSize(dim1.getWidth(), dim1.getHeight() + 5.); // Title row seems to need a little padding.

		// Use arguments to construct the labels for the columns.
		Vector<Object> columnNames = new Vector<>(2);
		columnNames.add(0, keyColumnLabel);
		columnNames.add(1, valueColumnLabel);

		// Extract key-value pairs from Metadata.
		Vector<Vector<Object>> rowData = new Vector<>(keys.size());
		for (Key<?> key : keys)
		{
			Object value = metadata.get(key);
			if (value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Character)
			{
				// Add to the table rows.
				Vector<Object> row = new Vector<>(2);
				row.add(0, key);
				row.add(1, value);
				rowData.add(row);

				// Update the dimension needed for the table.
				addVerticalSpace(key, dim0);
				addVerticalSpace(value, dim1);
			}
		}

		Dimension tableDim = addHorizontalSpace(addHorizontalSpace(dim0, dim1), new Dimension((int) VERT_SCROLL_BAR_WIDTH, (int) HOR_SCROLL_BAR_HEIGHT));

		// Create the table to be displayed.
		JTable jTable = createTable(rowData, columnNames);

		TableColumnModel columnModel = jTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth((int) dim0.getWidth());
		columnModel.getColumn(1).setPreferredWidth((int) dim1.getWidth());

		// Put the table in a scroll pane.
		JScrollPane jScrollPane = new JScrollPane(jTable);
		final Dimension maxInitialSize = new Dimension(800, 600 - 22); // 22 is the the amount of room a JFrame title bar typically occupies.

		// Add the scroll pane to the panel.
		GridBagConstraints gbc = new GridBagConstraints(-1, -1, 1, 1, 1., 1., GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
		jPanel.add(jScrollPane, gbc);
		jPanel.setPreferredSize(getMinimum(tableDim, maxInitialSize));
		// This may be safe but for now leaving it commented out until we establish that the line lengths really are perfect in all contexts.
		// If the maximum size is set too small it may be really awkward or impossible to view a large table, and/or get rid of scroll bars.
		//		jPanel.setMaximumSize(tableDim);
		this.jPanel = jPanel;
	}

	public JPanel getPanel()
	{
		return jPanel;
	}

	/**
	 * Create a read-only table that won't try to resize it. This is for putting in
	 * a scroll pane.
	 * 
	 * @param rowData the table's data
	 * @param columnNames column names
	 * @return the table
	 */
	@SuppressWarnings("serial")
	private final JTable createTable(Vector<?> rowData, Vector<?> columnNames)
	{
		JTable jTable = new JTable();
		jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jTable.setModel(new DefaultTableModel(rowData, columnNames) {
			@Override
			public boolean isCellEditable(@SuppressWarnings("unused") int row, @SuppressWarnings("unused") int column)
			{
				return false;
			}
		});
		return jTable;
	}

	/**
	 * Return the dimensions needed to display the supplied object as text.
	 * 
	 * @param object the object to display
	 * @return the space needed to display it
	 */
	private Dimension getDimension(Object object)
	{
		// Base size on how large a JLabel to display the object would be.
		final Dimension result = new JLabel(object.toString()).getPreferredSize();
		// Pad the space because for short strings this isn't accurate.
		result.setSize(Math.ceil(result.getWidth() + 6.), Math.ceil(result.getHeight()));
		return result;
	}

	/**
	 * Compute the space an object will need to be shown in a column. This method
	 * adds enough vertical space to the column's height to show the object, and
	 * figures out the column width necessary to display the object and the rest of
	 * the column. The result is put back into the Dimension supplied as the second
	 * argument.
	 * 
	 * @param object the object that will be added to the column.
	 * @param columnDim the column size, which is modified to add space for the
	 *            object.
	 */
	private void addVerticalSpace(Object object, Dimension columnDim)
	{
		final Dimension objectDim = getDimension(object);

		// Width is the larger of the two widths.
		double width = Math.max(objectDim.getWidth(), columnDim.getWidth());

		// Height is the sum of the two heights.
		double height = objectDim.getHeight() + columnDim.getHeight();

		columnDim.setSize(width, height);
	}

	/**
	 * Return the space two objects would need to be displayed side-by-side. The
	 * width is the sum of the widths, whereas the height is the height of the
	 * tallest object.
	 * 
	 * @param dim0 first object (column) dimensions
	 * @param dim1 second object (column) dimensions.
	 * @return
	 */
	private Dimension addHorizontalSpace(Dimension dim0, Dimension dim1)
	{
		// Width is the sum of the two widths + one Em for each.
		int width = (int) (dim0.getWidth() + dim1.getWidth());

		// Height is the maximum of the two heights + one Em.
		int height = (int) (Math.max(dim0.getHeight(), dim1.getHeight()));

		return new Dimension(width, height);
	}

	/**
	 * Return a size whose height and width are the smallest of the two supplied
	 * size objects.
	 * 
	 * @param dim0
	 * @param dim1
	 * @return
	 */
	private Dimension getMinimum(Dimension dim0, Dimension dim1)
	{
		int width = (int) Math.min(dim0.getWidth(), dim1.getWidth());
		int height = (int) Math.min(dim0.getHeight(), dim1.getHeight());
		return new Dimension(width, height);
	}

	// Test code.
	public static void main(String[] args)
	{
		ArrayList<Key<?>> list = new ArrayList<>();

		SettableMetadata metadata = SettableMetadata.of(Version.of(0, 1));
		metadata.put(keyOf("SIMPLE", list), Boolean.TRUE);
		metadata.put(keyOf("NAXIS1", list), 0);
		//		metadata.put(keyOf(÷"DATE", list), "20180525");
		//		metadata.put(keyOf("DATE long title should make the column wider", list), "20180525");
		//		metadata.put(keyOf("DATE", list), "20180525 Sure and tis a very long string, to be sure, oh yes oy what else could I say to make it longer still?");
		metadata.put(keyOf("DATE", list), "20180525 Sure and tis a very long string, to be sure, oh yes oy what else could I say to make it longer still? I mean so ridiculatloyl long that it exceeds the width of a normal labptop screeen  to ever even conceive of diplaying it without cutting off the dsmn table?");
		//		metadata.put(keyOf("X", list), 0.5);
		for (int index = 0; index < 80; ++index)
		{
			metadata.put(keyOf("Spud" + index, list), "Potato " + index);
		}

		MetadataDisplayPanel displayPanel = new MetadataDisplayPanel("Keyword", "Value", list, metadata);
		JFrame jFrame = new JFrame("Test MetatdataDisplayPanel");
		JPanel jPanel = displayPanel.getPanel();

		Dimension dim = jPanel.getPreferredSize();
		dim.setSize(dim.getWidth() + 1., dim.getHeight() + 22.);
		jFrame.setPreferredSize(dim);

		dim = jPanel.getMaximumSize();
		dim.setSize(dim.getWidth() + 1., dim.getHeight() + 22.);
		jFrame.setMaximumSize(dim);

		jFrame.add(displayPanel.getPanel());

		jFrame.pack();
		jFrame.validate();
		jFrame.setVisible(true);
	}

	// Just needed for the test main.
	private static <T> Key<T> keyOf(String text, List<Key<?>> keyList)
	{
		Key<T> result = Key.of(text);
		keyList.add(result);
		return result;
	}
}

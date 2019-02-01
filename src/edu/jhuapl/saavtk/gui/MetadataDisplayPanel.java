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
import com.google.common.collect.ImmutableList;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;

/**
 * Panel capable of displaying metadata that meet one of the following criteria:
 * 
 * 1) Key-value pairs where the value is a scalar, e.g., String, Double, etc.
 * 
 * 2) Key-value pairs where the value is an iterable. No recursion is performed
 * for iterables of iterable etc.
 *
 * @author peachjm1
 *
 */
public class MetadataDisplayPanel
{
	public static MetadataDisplayPanel of(Metadata metadata, String keyColumnLabel, Collection<String> valueColumnLabels)
	{
		Preconditions.checkNotNull(metadata);
		return new MetadataDisplayPanel(metadata, metadata.getKeys(), keyColumnLabel, valueColumnLabels);
	}

	public static MetadataDisplayPanel of(Metadata metadata, Collection<Key<?>> keys, String keyColumnLabel, Collection<String> valueColumnLabels)
	{
		return new MetadataDisplayPanel(metadata, keys, keyColumnLabel, valueColumnLabels);
	}

	public static void display(Metadata metadata, String keyColumnLabel, Collection<String> valueColumnLabels)
	{
		display(metadata, metadata.getKeys(), keyColumnLabel, valueColumnLabels);
	}

	public static void display(Metadata metadata, Collection<Key<?>> keys, String keyColumnLabel, Collection<String> valueColumnLabels)
	{
		MetadataDisplayPanel displayPanel = new MetadataDisplayPanel(metadata, keys, keyColumnLabel, valueColumnLabels);
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

	private static final double VERT_SCROLL_BAR_WIDTH = new JScrollBar(JScrollBar.VERTICAL).getPreferredSize().getWidth();
	private static final double HOR_SCROLL_BAR_HEIGHT = new JScrollBar(JScrollBar.HORIZONTAL).getPreferredSize().getHeight();
	private final JPanel jPanel;

	/**
	 * Construct a display of the provided metadata, using the supplied labels for
	 * the key column and value columns, and a collection of keys that must be
	 * contained in the metadata (this allows for a subset of the metadata to be
	 * displayed).
	 * 
	 * @param metadata the metadata to display
	 * @param keys the keys whose key/value to display
	 * @param keyColumnLabel the label displayed for the "key" column
	 * @param valueColumnLabels the labels displayed for the "value" columns
	 */
	protected MetadataDisplayPanel(Metadata metadata, Collection<Key<?>> keys, String keyColumnLabel, Collection<String> valueColumnLabels)
	{
		Preconditions.checkNotNull(metadata);
		Preconditions.checkNotNull(keys);
		Preconditions.checkNotNull(keyColumnLabel);
		Preconditions.checkNotNull(valueColumnLabels);

		JPanel jPanel = new JPanel();
		jPanel.setLayout(new GridBagLayout());

		// Keep track of size needed to display the table without scrollbars.
		List<Dimension> dims = new ArrayList<>();
		Dimension dim0 = getTextDimension(keyColumnLabel);
		dim0.setSize(dim0.getWidth(), dim0.getHeight() + 5.); // Title row seems to need a little padding.
		dims.add(dim0);
		for (String label : valueColumnLabels)
		{
			Dimension dim = getTextDimension(label);
			dim.setSize(dim.getWidth(), dim.getHeight() + 5.); // Title row seems to need a little padding.
			dims.add(dim);
		}

		// Use arguments to construct the labels for the columns.
		Vector<Object> columnNames = new Vector<>(1 + valueColumnLabels.size());
		columnNames.add(keyColumnLabel);
		columnNames.addAll(valueColumnLabels);

		// Extract key-value pairs from Metadata.
		Vector<Vector<Object>> rowData = new Vector<>(keys.size());
		for (Key<?> key : keys)
		{
			Object value = metadata.get(key);
			Vector<Object> row = new Vector<>();
			if (value instanceof String || value instanceof Number || value instanceof Boolean || value instanceof Character)
			{
				// Add scalar to the table rows.
				row.add(key);
				row.add(value);
			}
			else if (value instanceof Iterable)
			{
				Iterable<?> iterable = (Iterable<?>) value;

				// Add iterable to the table rows.
				row.add(key);
				for (Object object : iterable)
				{
					row.add(object);
				}
			}
			else
			{
				// row == null in this case; don't fall through the if.
				continue;
			}

			// Add this row.
			rowData.add(row);

			// Update space needed for display. Need to be careful because there is no
			// guarantee that rows (unpacked from metadata)
			// and dims (key label + value column labels) to have the same size.
			int size = Math.min(row.size(), dims.size());
			for (int index = 0; index < size; ++index)
			{
				addVerticalSpace(row.get(index), dims.get(index));
			}
		}

		Dimension tableDim = addHorizontalSpace(addHorizontalSpace(dims), new Dimension((int) VERT_SCROLL_BAR_WIDTH, (int) HOR_SCROLL_BAR_HEIGHT));

		// Create the table to be displayed.
		JTable jTable = createTable(rowData, columnNames);

		TableColumnModel columnModel = jTable.getColumnModel();
		for (int index = 0; index < columnModel.getColumnCount(); ++index)
		{
			columnModel.getColumn(index).setPreferredWidth((int) dims.get(index).getWidth());
		}

		// Put the table in a scroll pane.
		JScrollPane jScrollPane = new JScrollPane(jTable);
		Dimension maxInitialSize = new Dimension(800, 600 - 22); // 22 is the the amount of room a JFrame title bar
																	// typically occupies.

		// Add the scroll pane to the panel.
		GridBagConstraints gbc = new GridBagConstraints(-1, -1, 1, 1, 1., 1., GridBagConstraints.FIRST_LINE_START, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
		jPanel.add(jScrollPane, gbc);
		jPanel.setPreferredSize(getMinimum(tableDim, maxInitialSize));
		// This may be safe but for now leaving it commented out until we establish that
		// the line lengths really are perfect in all contexts.
		// If the maximum size is set too small it may be really awkward or impossible
		// to view a large table, and/or get rid of scroll bars.
		// jPanel.setMaximumSize(tableDim);
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
	private Dimension getTextDimension(Object object)
	{
		// Base size on how large a JLabel to display the object would be.
		Dimension result = new JLabel(object.toString()).getPreferredSize();
		// Pad the space because for short strings this isn't accurate.
		result.setSize(Math.ceil(result.getWidth() + 5.), Math.ceil(result.getHeight()));
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
		if (object == null)
		{
			return;
		}
		Dimension objectDim = getTextDimension(object);

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
		return addHorizontalSpace(ImmutableList.of(dim0, dim1));
	}

	private Dimension addHorizontalSpace(Iterable<Dimension> dims)
	{
		double width = 0;
		double height = 0;
		for (Dimension dim : dims)
		{
			// Width is the sum of the widths.
			width += dim.getWidth();

			// Height is the larger of the heights.
			height = Math.max(height, dim.getHeight());
		}
		return new Dimension((int) width, (int) height);
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
		double width = Math.min(dim0.getWidth(), dim1.getWidth());
		double height = Math.min(dim0.getHeight(), dim1.getHeight());
		return new Dimension((int) width, (int) height);
	}

	// Test code.
	public static void main(String[] args)
	{
		ArrayList<Key<?>> list = new ArrayList<>();

		SettableMetadata metadata = SettableMetadata.of(Version.of(0, 1));
		metadata.put(keyOf("SIMPLE", list), Boolean.TRUE);
		metadata.put(keyOf("NAXIS1", list), 0);
		metadata.put(keyOf("DATE", list), ImmutableList.of("20180525", "/ Here's a fits comment", "Fictitious third element"));
		// metadata.put(keyOf("DATE long title should make the column wider", list),
		// "20180525");
		// metadata.put(keyOf("DATE", list), "20180525 Sure and tis a very long string,
		// to be sure, oh yes oy what else could I say to make it longer still?");
		// metadata.put(keyOf("DATE", list), "20180525 Sure and tis a very long string,
		// to be sure, oh yes oy what else could I say to make it longer still? I mean
		// so ridiculatloyl long that it exceeds the width of a normal labptop screeen
		// to ever even conceive of diplaying it without cutting off the dsmn table?");
		metadata.put(keyOf("X", list), 0.5);
		metadata.put(Key.of("DO NOT DISPLAY THIS!"), "This key isn't tracked, so it should not show up in the table");
		for (int index = 0; index < 30; ++index)
		{
			metadata.put(keyOf("Spud" + index, list), "Potato " + index);
		}

		// Uncomment this to confirm it makes the test fail to start.
		// list.add(Key.of("Illegal key not bound to metadata"));

		display(metadata, list, "Keyword", ImmutableList.of("Value", "Comment"));
	}

	// Just needed for the test main.
	private static <T> Key<T> keyOf(String text, List<Key<?>> keyList)
	{
		Key<T> result = Key.of(text);
		keyList.add(result);
		return result;
	}
}

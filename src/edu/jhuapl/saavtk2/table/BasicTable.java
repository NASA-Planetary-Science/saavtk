package edu.jhuapl.saavtk2.table;

import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

import edu.jhuapl.saavtk2.event.BasicEventSource;

/**
 * 
 * @author zimmemi1
 *
 * Basic implementation of a Table with mutable rows and event firing support. The column specifications are immutable, i.e. the {@link TableColumn} ordering and content cannot be changed.
 * 
 */
public class BasicTable extends BasicEventSource implements Table {

	/**
	 * The unique bi-directional mapping between column index and TableColumn specification.
	 */
	BiMap<Integer, TableColumn> columnSpecs=HashBiMap.create();
	
	/**
	 * The ordered list of Object arrays making up the row data.
	 */
	protected List<Object[]> rowData=Lists.newArrayList();
	
	/**
	 * 
	 * @param columns List of column specifications, in the order in which they are to appear in the table.
	 */
	public BasicTable(List<TableColumn> columns) {
		for (int i=0; i<columns.size(); i++)
			columnSpecs.put(i, columns.get(i));
	}

	@Override
	public int getNumberOfRows() {
		return rowData.size();
	}

	@Override
	public int getNumberOfColumns() {
		return columnSpecs.size();
	}

	@Override
	public TableColumn getColumnSpec(int c) {
		return columnSpecs.get(c);
	}

	@Override
	public Object[] getRow(int r) {
		return rowData.get(r);
	}

	@Override
	public Object getValue(int r, int c) {
		return rowData.get(r)[c];
	}
	

	/**
	 * 
	 * @param spec Column specification
	 * @return Integer index of the column in this table
	 */
	protected int getColumnNumber(TableColumn spec)
	{
		return columnSpecs.inverse().get(spec);
	}
	
	/**
	 * Append one row of data to the end of the table.
	 * 
	 * @param data An Object array representing one row of data.
	 */
	public void appendRow(Object[] data)
	{
		if (data.length!=getNumberOfColumns())
			throw new Error("Row dimension mismatch ("+data.length+"!="+getNumberOfColumns()+")");
		rowData.add(data);
		fire(new TableRowAddedEvent(this, getNumberOfRows()-1));
	}

	/**
	 * Remove one row of data from the tab
	 * @param r The row index to remove.
	 */
	public void removeRow(int r)
	{
		rowData.remove(r);
		fire(new TableRowRemovedEvent(this, r));
	}
	


	
}

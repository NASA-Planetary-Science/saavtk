package edu.jhuapl.saavtk2.table;

import edu.jhuapl.saavtk2.event.Event;
import edu.jhuapl.saavtk2.event.EventSource;
import edu.jhuapl.saavtk2.table.search.ImageSearchResultsTable;
import edu.jhuapl.saavtk2.table.search.MapAndShowTable;


/**
 * 
 * @author zimmemi1
 *
 * Interface for a read-only table with rows and columns of data. Rows of data are returned as one Object array per row number. Single values are accessed by their row and column number.
 * Implementations can fire {@link Event}s to signal changes in the data.
 * The {@link TableColumn} interface allows flexible specification of column ordering and response to events fired (cf. {@link BasicTable}) without having to know the actual integer index of the column. This also eases the burden of inheriting one table type from another, e.g. adding new columns to existing ones, possibly in a different order, to create a new Table implementation (cf. {@link MapAndShowTable} and {@link ImageSearchResultsTable}).
 */

public interface Table extends EventSource {
	/**
	 * 
	 * @return Number of rows currently in the table.
	 */
	public int getNumberOfRows();
	
	/**
	 * 
	 * @return Number of columns in the table.
	 */
	public int getNumberOfColumns();
	
	
	/**
	 * 
	 * @param r Row index
	 * @return The specified row of data, as an Object array.
	 */
	public Object[] getRow(int r);
	
	/**
	 * 
	 * @param r Row index
	 * @param c Column index
	 * @return The data element at the specified row and column, as an Object.
	 */
	public Object getValue(int r, int c);

	/**
	 * 
	 * @param c Column index
	 * @return Column specification object, which abstracts the definition of a column from its actual position in the table. 
	 */
	public TableColumn getColumnSpec(int c);

	
}

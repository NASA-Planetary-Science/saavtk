package edu.jhuapl.saavtk2.table;

import edu.jhuapl.saavtk2.event.BasicEvent;
import edu.jhuapl.saavtk2.event.EventSource;


public class TableEntryChangedEvent<T> extends BasicEvent<T> {

	int r;
	TableColumn spec;
	
	/**
	 * 
	 * @param source The table that fired this event
	 * @param r The row containing the entry that changed
	 * @param spec The column specification (associated with the actual column index) containing the entry that changed; this is used instead of the integer column index so that listeners don't need to worry about where the column actually is in the table
	 * @param value The new value of the respective entry
	 */
	public TableEntryChangedEvent(Table source, int r, TableColumn spec, T value) {
		super(source, value);
		this.r=r;
		this.spec=spec;
	}

	/**
	 * 
	 * @return The row containing the entry that changed
	 */
	public int getRow() {
		return r;
	}

	/**
	 * 
	 * @return The column specification (associated with the actual column index) containing the entry that changed; this is used instead of the integer column index so that listeners don't need to worry about where the column actually is in the table
	 */
	public TableColumn getTableColumn() {
		return spec;
	}
	
}

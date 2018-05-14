package edu.jhuapl.saavtk2.table;

import edu.jhuapl.saavtk2.event.BasicEvent;
import edu.jhuapl.saavtk2.event.EventSource;

/**
 * 
 * @author zimmemi1
 *
 * @param <T> Optional object type associated with this event (usually the type of a single value that changed within the row) 
 */
public class TableRowEvent<T> extends BasicEvent<T> {

	/**
	 * Index of row associated with this event
	 */
	int row;
	
	/**
	 * 
	 * @param source Object that fired the event
	 * @param row Index of row associated with this event
	 * @param value Optional data value associated with this event
	 */
	public TableRowEvent(EventSource source, int row, T value) {
		super(source, value);
		this.row=row;
	}
	
	/**
	 * 
	 * @return Index of row associated with this event
	 */
	public int getRow() {
		return row;
	}

}

package edu.jhuapl.saavtk2.table;

/**
 * 
 * @author zimmemi1
 *
 * Event that can be fired by a {@link Table} when a row is added at a particular index.
 */
public class TableRowAddedEvent extends TableRowEvent<Void> 
{

	/**
	 * 
	 * @param source The table that fired this event
	 * @param row The index at which the new row was inserted
	 */
	public TableRowAddedEvent(Table source, int row) {
		super(source, row, null);
	}
	
}

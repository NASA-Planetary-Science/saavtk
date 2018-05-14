package edu.jhuapl.saavtk2.table;

/**
 * 
 * @author zimmemi1
 *
 * Event that can be fired by a {@link Table} when a row is removed at a particular index.
 */
public class TableRowRemovedEvent extends TableRowEvent<Void> 
{
	/**
	 * 
	 * @param source The table that fired this event
	 * @param row The index at which the row was removed
	 */
	public TableRowRemovedEvent(Table source, int row) {
		super(source, row, null);
	}
	
}
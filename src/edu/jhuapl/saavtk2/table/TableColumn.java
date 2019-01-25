package edu.jhuapl.saavtk2.table;

/**
 * 
 * @author zimmemi1
 *
 * Specification of a column in a {@link Table}, independent of its actual position in the {@link Table}.
 */
public interface TableColumn {
	/**
	 * 
	 * @return Name of the column, typically representing the column header
	 */
	public String getName();
	
	/**
	 * 
	 * @return Type of data that can be stored in this column
	 */
	public Class<?> getType();
}

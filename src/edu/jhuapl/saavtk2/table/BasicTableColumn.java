package edu.jhuapl.saavtk2.table;

/**
 * 
 * @author zimmemi1
 *
 * Boilerplate implementation of a {@link TableColumn}
 */
public class BasicTableColumn implements TableColumn {
	
	String name;
	Class<?> type;
	
	public BasicTableColumn(String name, Class<?> type) {
		super();
		this.name = name;
		this.type = type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getType() {
		return type;
	}

}

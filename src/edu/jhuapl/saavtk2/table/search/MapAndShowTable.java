package edu.jhuapl.saavtk2.table.search;


import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk2.event.Event;
import edu.jhuapl.saavtk2.event.EventListener;
import edu.jhuapl.saavtk2.table.BasicTable;
import edu.jhuapl.saavtk2.table.TableSwingWrapper;
import edu.jhuapl.saavtk2.table.TableColumn;
import edu.jhuapl.saavtk2.table.TableEntryChangedEvent;

public class MapAndShowTable extends BasicTable {

	protected static enum Columns implements TableColumn
	{
		Map(Boolean.class),Show(Boolean.class),Desc(String.class);
		
		Class<?> type;
		
		private Columns(Class<?> type) {
			this.type=type;
		}

		@Override
		public String getName() {
			return name();
		}

		@Override
		public Class<?> getType() {
			return type;
		}
		
	}
	
	public MapAndShowTable()
	{
		this(Lists.newArrayList(Columns.values()));
	}
	
	public MapAndShowTable(List<TableColumn> customColumns)
	{
		super(customColumns);
	}
	
	public void setItemMapped(int r, boolean map)
	{
		rowData.get(r)[getColumnNumber(Columns.Map)]=map;
		//fire(new TableItemMappedEvent(this, r, map));
	}
	
	public void showItem(int r, boolean show)
	{
		if (itemIsMapped(r))
		{
			rowData.get(r)[getColumnNumber(Columns.Show)]=show;
			//fire(new TableItemShownEvent(this, r, show));
		}
	}
	
	public boolean itemIsMapped(int r)
	{
		return (Boolean)rowData.get(r)[getColumnNumber(Columns.Map)];
	}
	
	public boolean itemIsShown(int r)
	{
		return (Boolean)rowData.get(r)[getColumnNumber(Columns.Show)];
	}
	
	public static TableSwingWrapper createSwingWrapper(MapAndShowTable table) 
	{
		TableSwingWrapper swingTable=new TableSwingWrapper(table);
		swingTable.setColumnEditable(Columns.Show, false);
		table.addListener(new EventListener() {	
			
			@SuppressWarnings("rawtypes")
			@Override
			public void handle(Event event) {
				if (event instanceof TableEntryChangedEvent) // this event handler adds the behavior where show checkbox should be marked "false" when map checkbox is marked "false"
				{
					TableEntryChangedEvent eventCast=(TableEntryChangedEvent)event;
					if (eventCast.getTableColumn()==Columns.Map)
					{
						int row=eventCast.getRow();
						Boolean map=(Boolean)event.getValue();
						table.removeListener(swingTable);
						table.setItemMapped(row, map);
						swingTable.setCellEditable(row, Columns.Show, map);	// if (not) mapped then the show checkbox is (not) editable
						if (!map) 	// update the swing gui table model before re-enabling event listing
							swingTable.getComponent().setValueAt(false, row, table.getColumnNumber(Columns.Show));
						table.addListener(swingTable);
					}
				}
			}
		});
		return swingTable;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				JFrame frame=new BasicFrame();
				MapAndShowTable table=new MapAndShowTable();
				TableSwingWrapper swingTable=MapAndShowTable.createSwingWrapper(table);
				JScrollPane scrollPane=new JScrollPane(swingTable.getComponent());
				frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

				for (int i=0; i<100; i++)
				{
					table.appendRow(new Object[]{false,false,"Test "+i});
				}

				for (int i=0; i<75; i++)
				{
					int idx=(int)((double)table.getNumberOfRows()*Math.random());
					table.removeRow(idx);
				}
				
			}
		});
	}
		
}

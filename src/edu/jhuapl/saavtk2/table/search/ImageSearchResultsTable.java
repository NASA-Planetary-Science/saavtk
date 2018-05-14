package edu.jhuapl.saavtk2.table.search;

import java.awt.BorderLayout;
import java.sql.Date;
import java.time.Instant;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk2.event.Event;
import edu.jhuapl.saavtk2.event.EventListener;
import edu.jhuapl.saavtk2.table.TableSwingWrapper;
import edu.jhuapl.saavtk2.table.TableColumn;
import edu.jhuapl.saavtk2.table.TableEntryChangedEvent;

public class ImageSearchResultsTable extends MapAndShowTable {

	protected static enum Columns implements TableColumn
	{
		Proj(Boolean.class),Bndr(Boolean.class);
		
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
	
	public static class ImageMetaData
	{
		String desc;
		
		public ImageMetaData() {
			desc=Date.from(Instant.now()).toString();
		}
		
		@Override
		public String toString() {
			return desc;
		}
	}
	
	public static class ImageFactory
	{
		public void create(ImageMetaData metaData)
		{
			System.out.println("Creating image from metadata: "+metaData.toString());
		}

		public void delete(ImageMetaData metaData)
		{
			System.out.println("Deleting image from metadata: "+metaData.toString());
		}
		
		public void show(ImageMetaData metaData)
		{
			System.out.println("Showing image from metadata: "+metaData.toString());
		}

		public void hide(ImageMetaData metaData)
		{
			System.out.println("Hiding image from metadata: "+metaData.toString());
		}
	}
	
	final ImageFactory factory;
	final List<ImageMetaData> metaData=Lists.newArrayList();
	
	public ImageSearchResultsTable(ImageFactory factory) {
		super(Lists.newArrayList(new TableColumn[]{
				MapAndShowTable.Columns.Map,
				MapAndShowTable.Columns.Show,
				ImageSearchResultsTable.Columns.Proj,
				ImageSearchResultsTable.Columns.Bndr,
				MapAndShowTable.Columns.Desc
		}));
		this.factory=factory;
	}
	
	@Deprecated
	@Override
	public void appendRow(Object[] data) {
		throw new Error("Use appendRow(ImageSearchResult) method instead");
	}
	
	public void appendRow(ImageMetaData metaData)
	{
		this.metaData.add(metaData);
		super.appendRow(new Object[]{false,false,false,false, generateDescription(metaData)});
	}
		
	protected String generateDescription(ImageMetaData metaData)
	{
		return metaData.toString();
	}
	
	@Override
	public void removeRow(int r) {
		metaData.remove(r);
		super.removeRow(r);
	}
	
	public static TableSwingWrapper createSwingWrapper(ImageSearchResultsTable table) 
	{
		TableSwingWrapper swingTable=new TableSwingWrapper(table);
		swingTable.setColumnEditable(MapAndShowTable.Columns.Show, false);
		swingTable.setColumnEditable(ImageSearchResultsTable.Columns.Proj, false);
		swingTable.setColumnEditable(ImageSearchResultsTable.Columns.Bndr, false);
		table.addListener(new EventListener() {	
			
			@Override
			public void handle(Event event) {
				if (event instanceof TableEntryChangedEvent) // this event handler adds the behavior where show checkbox should be marked "false" when map checkbox is marked "false"
				{
					TableEntryChangedEvent eventCast=(TableEntryChangedEvent)event;
					if (eventCast.getTableColumn()==MapAndShowTable.Columns.Map)
					{
						int row=eventCast.getRow();
						Boolean map=(Boolean)event.getValue();
						if (map)
						{
							table.factory.create(table.metaData.get(row));
						}
						else
						{
							table.factory.delete(table.metaData.get(row));
						}
						//
						table.setItemMapped(row, map);
						swingTable.setCellEditable(row, MapAndShowTable.Columns.Show, map);	// if mapped then the show checkbox is editable
						swingTable.setCellEditable(row, ImageSearchResultsTable.Columns.Proj, map);
						swingTable.setCellEditable(row, ImageSearchResultsTable.Columns.Bndr, map);
						if (!map)	// update the swing gui table model before re-enabling event listing 
						{
							swingTable.getComponent().setValueAt(false, row, table.getColumnNumber(MapAndShowTable.Columns.Show));
							swingTable.getComponent().setValueAt(false, row, table.getColumnNumber(ImageSearchResultsTable.Columns.Proj));
							swingTable.getComponent().setValueAt(false, row, table.getColumnNumber(ImageSearchResultsTable.Columns.Bndr));
						}
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
				ImageFactory factory=new ImageFactory();
				ImageSearchResultsTable table=new ImageSearchResultsTable(factory);
				TableSwingWrapper swingTable=ImageSearchResultsTable.createSwingWrapper(table);
				JScrollPane scrollPane=new JScrollPane(swingTable.getComponent());
				frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

				for (int i=0; i<100; i++)
				{
					table.appendRow(new ImageMetaData());
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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

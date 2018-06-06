package edu.jhuapl.saavtk2.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Map;
import java.util.concurrent.Future;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;

import edu.jhuapl.saavtk2.event.Event;
import edu.jhuapl.saavtk2.event.EventListener;
import edu.jhuapl.saavtk2.gui.BasicFrame;
import edu.jhuapl.saavtk2.table.TableRowAddedEvent;
import edu.jhuapl.saavtk2.table.TableRowRemovedEvent;

public class TableSwingWrapper implements EventListener, TableModelListener {


	protected DefaultTableModel model;
	protected JTable jTable;
	//TableModelListener modelListener;
	protected BasicTable backingTable;
	protected Map<TableColumn, Boolean> columnEditable=Maps.newHashMap();
	protected com.google.common.collect.Table<Integer, TableColumn, Boolean> cellEditable=HashBasedTable.create();	
	
	public JTable getComponent()
	{
		return jTable;
	}
	
	public TableSwingWrapper(BasicTable table) {
		this.backingTable=table;
		initModel();
		initData();
		initComponent();
		table.addListener(this);
		jTable.getModel().addTableModelListener(this);
	}
	
	public void setColumnEditable(TableColumn spec, boolean editable)
	{
		columnEditable.put(spec, editable);
	}
	
	public void setCellEditable(int row, TableColumn spec, boolean editable)
	{
		cellEditable.put(row, spec, editable);
	}

	@SuppressWarnings("serial")
	protected void initModel()
	{
		model = new DefaultTableModel() {
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return backingTable.getColumnSpec(columnIndex).getType();
			}
			
			@Override
			public int getColumnCount() {
				return backingTable.getNumberOfColumns();
			}
			
			@Override
			public String getColumnName(int column) {
				return backingTable.getColumnSpec(column).getName();
			}
			
			@Override
			public boolean isCellEditable(int row, int column) {
				TableColumn columnSpec=backingTable.getColumnSpec(column);
				Boolean cellEditOk=cellEditable.get(row, backingTable.getColumnSpec(column));
				if (columnEditable.containsKey(columnSpec))
				{
					if (cellEditOk==null)
						return columnEditable.get(columnSpec);
					else
						return cellEditOk;	// cell edit flag overrides column edit flag
				}
				else
					return true;	// editable by default 
			}
		};
		
	}
	
	protected void initData()
	{
		// populate any data that is already in the enclosing Table
		for (int r=0; r<backingTable.getNumberOfRows(); r++)
		{
			model.addRow(backingTable.getRow(r));
		}
	}


	@SuppressWarnings("serial")
	protected void initComponent()
	{
		//
		//
		jTable = new JTable(model) {
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {	// add very tasteful alternating stripes
				Component c=super.prepareRenderer(renderer, row, column);
				if (!isRowSelected(row))
				{
					c.setBackground(new Color(255,255,255));
					if (row%2==1)
					{
						c.setBackground(new Color(240,240,240));
					}
				}
				return c;
			}

		};
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				JFrame frame=new BasicFrame();
				MapAndShowTable table=new MapAndShowTable();
				TableSwingWrapper swingTable=new TableSwingWrapper(table);
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

	@Override
	public void handle(Event event) {
		if (event instanceof TableRowAddedEvent)
		{
			int row=((TableRowAddedEvent) event).getRow();
			model.addRow(backingTable.getRow(row));
			jTable.revalidate();
		}
		else if (event instanceof TableRowRemovedEvent)
		{
			int row=((TableRowRemovedEvent) event).getRow();
			model.removeRow(row);
			jTable.revalidate();
		}
		else if (event instanceof TableEntryChangedEvent)
		{
			int row=((TableEntryChangedEvent) event).getRow();
			model.removeTableModelListener(this);
			model.setValueAt(event.getValue(), row, backingTable.getColumnNumber(((TableEntryChangedEvent) event).getTableColumn()));
			model.addTableModelListener(this);
			jTable.revalidate();
		}
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		if (e.getType()==TableModelEvent.UPDATE)
		{
			int col=e.getColumn();
			int row=e.getFirstRow();
			model.removeTableModelListener(this);
			backingTable.removeListener(this);
			backingTable.fire(new TableEntryChangedEvent(backingTable, row, backingTable.getColumnSpec(col), jTable.getValueAt(row,col)));
			backingTable.addListener(this);
			model.addTableModelListener(this);
		}
	}
}

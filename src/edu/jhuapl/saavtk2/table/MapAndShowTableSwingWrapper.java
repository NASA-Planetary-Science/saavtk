package edu.jhuapl.saavtk2.table;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import edu.jhuapl.saavtk2.event.Event;
import edu.jhuapl.saavtk2.event.EventListener;
import edu.jhuapl.saavtk2.gui.BasicFrame;
import edu.jhuapl.saavtk2.table.MapAndShowTable.Columns;

public class MapAndShowTableSwingWrapper extends TableSwingWrapper
{

    public MapAndShowTableSwingWrapper(MapAndShowTable table)
    {
        super(table);
        setColumnEditable(Columns.Show, false);
        table.addListener(new CustomListener());
    }

    protected class CustomListener implements EventListener
    {
        @Override
        public void handle(Event event)
        {
            if (event instanceof TableEntryChangedEvent) // this event handler adds the behavior where show checkbox should be marked "false" when map checkbox is marked "false"
            {
                TableEntryChangedEvent eventCast = (TableEntryChangedEvent) event;
                if (eventCast.getTableColumn() == Columns.Map)
                {
                    int row = eventCast.getRow();
                    Boolean map = (Boolean) event.getValue();
                    backingTable.removeListener(this);
                    ((MapAndShowTable) backingTable).setItemMapped(row, map);
                    setCellEditable(row, Columns.Show, map); // if (not) mapped then the show checkbox is (not) editable
                    if (!map) // update the swing gui table model before re-enabling event listing
                        getComponent().setValueAt(false, row, backingTable.getColumnNumber(Columns.Show));
                    backingTable.addListener(this);
                }
            }
        }
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {

            @Override
            public void run()
            {
                JFrame frame = new BasicFrame();
                MapAndShowTable table = new MapAndShowTable();
                MapAndShowTableSwingWrapper wrapper = new MapAndShowTableSwingWrapper(table);
                JScrollPane scrollPane = new JScrollPane(wrapper.getComponent());
                frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

                for (int i = 0; i < 5; i++)
                {
                    table.appendRow(new Object[] { false, false, "Test " + i });
                }

                for (int i = 0; i < 3; i++)
                {
                    int idx = (int) ((double) table.getNumberOfRows() * Math.random());
                    table.removeRow(idx);
                }

            }
        });
    }

}

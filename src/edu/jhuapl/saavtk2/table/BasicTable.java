package edu.jhuapl.saavtk2.table;

import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

import edu.jhuapl.saavtk2.event.BasicEvent;
import edu.jhuapl.saavtk2.event.BasicEventSource;
import edu.jhuapl.saavtk2.event.EventSource;

/**
 * 
 * @author zimmemi1
 *
 *         Basic implementation of a Table with mutable rows and event firing support. The column specifications are immutable, i.e. the {@link TableColumn} ordering and content cannot be changed.
 * 
 */
public class BasicTable extends BasicEventSource implements Table
{

    /**
     * The unique bi-directional mapping between column index and TableColumn specification.
     */
    BiMap<Integer, TableColumn> columnSpecs = HashBiMap.create();

    /**
     * The ordered list of Object arrays making up the row data.
     */
    protected List<Object[]> rowData = Lists.newArrayList();

    /**
     * 
     * @param columns
     *            List of column specifications, in the order in which they are to appear in the table.
     */
    public BasicTable(List<TableColumn> columns)
    {
        for (int i = 0; i < columns.size(); i++)
            columnSpecs.put(i, columns.get(i));
    }

    @Override
    public int getNumberOfRows()
    {
        return rowData.size();
    }

    @Override
    public int getNumberOfColumns()
    {
        return columnSpecs.size();
    }

    @Override
    public TableColumn getColumnSpec(int c)
    {
        return columnSpecs.get(c);
    }

    @Override
    public Object[] getRow(int r)
    {
        return rowData.get(r);
    }

    @Override
    public Object getValue(int r, int c)
    {
        return rowData.get(r)[c];
    }

    /**
     * 
     * @param spec
     *            Column specification
     * @return Integer index of the column in this table
     */
    protected int getColumnNumber(TableColumn spec)
    {
        return columnSpecs.inverse().get(spec);
    }

    /**
     * Append one row of data to the end of the table.
     * 
     * @param data
     *            An Object array representing one row of data.
     */
    public void appendRow(Object[] data)
    {
        if (data.length != getNumberOfColumns())
            throw new Error("Row dimension mismatch (" + data.length + "!=" + getNumberOfColumns() + ")");
        rowData.add(data);
        fire(new TableRowAddedEvent(this, rowData.size()-1));
    }

    /**
     * Remove one row of data from the tab
     * 
     * @param r
     *            The row index to remove.
     */
    public void removeRow(int r)
    {
        rowData.remove(r);
        fire(new TableRowRemovedEvent(this, r));
    }

    /**
     * 
     * @author zimmemi1
     *
     * @param <T>
     *            Optional object type associated with this event (usually the type of a single value that changed within the row)
     */
    static public class TableRowEvent<T> extends BasicEvent<T>
    {

        /**
         * Index of row associated with this event
         */
        int row;

        /**
         * 
         * @param source
         *            Object that fired the event
         * @param row
         *            Index of row associated with this event
         * @param value
         *            Optional data value associated with this event
         */
        public TableRowEvent(EventSource source, int row, T value)
        {
            super(source, value);
            this.row = row;
        }

        /**
         * 
         * @return Index of row associated with this event
         */
        public int getRow()
        {
            return row;
        }

    }

    static public class TableRowAddedEvent extends TableRowEvent<Void>
    {

        /**
         * 
         * @param source
         *            The table that fired this event
         * @param row
         *            The index at which the new row was inserted
         */
        public TableRowAddedEvent(Table source, int row)
        {
            super(source, row, null);
        }

    }

    /**
     * 
     * @author zimmemi1
     *
     *         Event that can be fired by a {@link Table} when a row is removed at a particular index.
     */
    public class TableRowRemovedEvent extends TableRowEvent<Void>
    {
        /**
         * 
         * @param source
         *            The table that fired this event
         * @param row
         *            The index at which the row was removed
         */
        public TableRowRemovedEvent(Table source, int row)
        {
            super(source, row, null);
        }

    }

}

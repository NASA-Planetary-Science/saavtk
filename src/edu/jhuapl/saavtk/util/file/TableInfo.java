package edu.jhuapl.saavtk.util.file;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class TableInfo extends DataObjectInfo
{
	public static class ColumnInfo
	{
		public static ColumnInfo of(String name, String units)
		{
			return new ColumnInfo(name, units);
		}

		private final String name;
		private final String units;

		protected ColumnInfo(String name, String units)
		{
			Preconditions.checkNotNull(name);
			Preconditions.checkNotNull(units);

			this.name = name;
			this.units = units;
		}

		public String getName()
		{
			return name;
		}

		public String getUnits()
		{
			return units;
		}

		@Override
		public String toString()
		{
			String units = getUnits();
			return "Column " + getName() + (units.isEmpty() ? "" : " (" + units + ")");
		}
	}

	public static TableInfo of(String title, Description description, int numberRows, List<? extends ColumnInfo> columnInfo)
	{
		return new TableInfo(title, description, numberRows, columnInfo);
	}

	private final int numberRows;
	private final ImmutableList<ColumnInfo> columnInfo;

	protected TableInfo(String title, Description description, int numberRows, List<? extends ColumnInfo> columnInfo)
	{
		super(title, description);
		this.numberRows = numberRows;
		this.columnInfo = ImmutableList.copyOf(columnInfo);
	}

	public int getNumberRows()
	{
		return numberRows;
	}

	public int getNumberColumns()
	{
		return columnInfo.size();
	}

	public ColumnInfo getColumnInfo(int columnNumber)
	{
		return columnInfo.get(columnNumber);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("; table with ");
		builder.append(getNumberRows());
		builder.append(" row(s), ");
		builder.append(getNumberColumns());
		builder.append(" column(s): ");
		boolean firstTime = true;
		for (ColumnInfo info : columnInfo)
		{
			if (firstTime)
			{
				firstTime = false;
			}
			else
			{
				builder.append(", ");
			}
			builder.append(info.getName());
		}
		return builder.toString();
	}

}

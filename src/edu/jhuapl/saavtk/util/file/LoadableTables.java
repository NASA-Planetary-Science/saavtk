package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import com.google.common.collect.ImmutableList;

import vtk.vtkFloatArray;

public class LoadableTables
{
	public static LoadableTable of(File file, int tableHduNumber, int columnNumber)
	{
		return new FitsLoadableTable(file, tableHduNumber, ImmutableList.of(columnNumber)) {

			@Override
			protected void setTuple(ImmutableList<Double> tuple, int index, vtkFloatArray data)
			{
				data.SetTuple1(index, tuple.get(0));
			}

			@Override
			public double[] getTuple(int recordIndex)
			{
				return new double[] { getData().GetTuple1(recordIndex) };
			}

		};
	}

	public static LoadableTable of(File file, int tableHduNumber, int xColumnNumber, int yColumnNumber, int zColumnNumber)
	{
		return new FitsLoadableTable(file, tableHduNumber, ImmutableList.of(xColumnNumber, yColumnNumber, zColumnNumber)) {

			@Override
			protected void setTuple(ImmutableList<Double> tuple, int index, vtkFloatArray data)
			{
				data.SetTuple3(index, tuple.get(0), tuple.get(1), tuple.get(2));
			}

			@Override
			public double[] getTuple(int recordIndex)
			{
				return getData().GetTuple3(recordIndex);
			}

		};
	}

	public static LoadableTable of(File file, int tableHduNumber, Collection<Integer> columns)
	{
		Iterator<Integer> iterator = columns.iterator();
		if (columns.size() == 1)
		{
			return of(file, tableHduNumber, iterator.next());
		}
		else if (columns.size() == 3)
		{
			Integer xColumnNumber = iterator.next();
			Integer yColumnNumber = iterator.next();
			Integer zColumnNumber = iterator.next();
			return of(file, tableHduNumber, xColumnNumber, yColumnNumber, zColumnNumber);
		}
		throw new IllegalArgumentException();
	}

	private static abstract class FitsOrCSVTable extends LoadableTable
	{
		private final File file;
		private final int tableHduNumber;

		protected FitsOrCSVTable(File file, int tableHduNumber, ImmutableList<?> columnIdentifiers)
		{
			super(columnIdentifiers);
			this.file = file;
			this.tableHduNumber = tableHduNumber;
		}

		@Override
		protected ImmutableList<ImmutableList<Double>> doLoad() throws IOException
		{
			ImmutableList<ImmutableList<Double>> result = null;
			try
			{
				// First try this to see if it's a FITS file.
				@SuppressWarnings("unchecked")
				FitsLoadableTable table = new FitsLoadableTable(file, tableHduNumber, (ImmutableList<Integer>) getColumnIdentifiers()) {

					@Override
					public double[] getTuple(int recordIndex)
					{
						return FitsOrCSVTable.this.getTuple(recordIndex);
					}

					@Override
					protected void setTuple(ImmutableList<Double> immutableList, int index, vtkFloatArray data)
					{
						FitsOrCSVTable.this.setTuple(immutableList, index, data);
					}
				};
				result = table.doLoad();
			}
			catch (@SuppressWarnings("unused") IOException e)
			{
				// 
				@SuppressWarnings("unchecked")
				CSVLoadableTable table = new CSVLoadableTable(file, (ImmutableList<String>) getColumnIdentifiers()) {

					@Override
					public double[] getTuple(int recordIndex)
					{
						return FitsOrCSVTable.this.getTuple(recordIndex);
					}

					@Override
					protected void setTuple(ImmutableList<Double> immutableList, int index, vtkFloatArray data)
					{
						FitsOrCSVTable.this.setTuple(immutableList, index, data);
					}
				};
				return table.doLoad();
			}
			return result;
		}

	}
}

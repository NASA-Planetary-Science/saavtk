package edu.jhuapl.saavtk.util.file;

import java.io.File;

import com.google.common.collect.ImmutableList;

import vtk.vtkFloatArray;

public class FitsLoadableTables
{
	public static LoadableTable of(File file, int tableHduNumber, int columnNumber)
	{
		return new FitsLoadableTable(file, tableHduNumber, ImmutableList.of(columnNumber)) {

			@Override
			protected void setTuple(ImmutableList<float[]> columns, int index, vtkFloatArray data)
			{
				data.SetTuple1(index, columns.get(0)[index]);
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
			protected void setTuple(ImmutableList<float[]> columns, int index, vtkFloatArray data)
			{
				data.SetTuple3(index, columns.get(0)[index], columns.get(1)[index], columns.get(2)[index]);
			}

			@Override
			public double[] getTuple(int recordIndex)
			{
				return getData().GetTuple3(recordIndex);
			}

		};
	}

}

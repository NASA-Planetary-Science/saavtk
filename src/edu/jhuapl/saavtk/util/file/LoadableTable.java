package edu.jhuapl.saavtk.util.file;

import java.io.IOException;
import java.util.Collection;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import vtk.vtkFloatArray;

public abstract class LoadableTable
{
	private final ImmutableList<?> columnIdentifiers;
	private vtkFloatArray data;
	private double[] defaultRange;
	private boolean loadFailed;

	protected LoadableTable(ImmutableList<?> columnIdentifiers)
	{
		checkColumnIdentifiers(columnIdentifiers);
		this.columnIdentifiers = columnIdentifiers;
		this.data = null;
		this.defaultRange = null;
		this.loadFailed = false;
	}

	public abstract double[] getTuple(int recordIndex);

	protected abstract void setTuple(ImmutableList<Double> immutableList, int index, vtkFloatArray data);

	protected abstract ImmutableList<ImmutableList<Double>> doLoad() throws IOException;

	public void load() throws IOException
	{
		if (loadFailed)
		{
			throw new IOException("Data failed to load previously");
		}
		if (!isLoaded())
		{
			loadFailed = true;

			ImmutableList<ImmutableList<Double>> tuples = doLoad();

			vtkFloatArray data = new vtkFloatArray();
			double[] defaultRange = null;

			if (tuples.isEmpty())
			{
				defaultRange = new double[] { 0., 0. };
			}
			else
			{
				data.SetNumberOfComponents(tuples.get(0).size());
				data.SetNumberOfTuples(tuples.size());
				for (int index = 0; index < tuples.size(); ++index)
				{
					setTuple(tuples.get(index), index, data);
				}
				defaultRange = data.GetRange();
				defaultRange = new double[] { defaultRange[0], defaultRange[1] };
			}

			this.data = data;
			this.defaultRange = defaultRange;
			loadFailed = false;
		}
	}

	public void reload() throws IOException
	{
		data = null;
		defaultRange = null;
		loadFailed = false;
		load();
	}

	public vtkFloatArray getData()
	{
		Preconditions.checkState(isLoaded());
		return data;
	}

	public double[] getDefaultRange()
	{
		Preconditions.checkState(isLoaded());
		return new double[] { defaultRange[0], defaultRange[1] };
	}

	protected ImmutableList<?> getColumnIdentifiers()
	{
		return columnIdentifiers;
	}

	private void checkColumnIdentifiers(Collection<?> columnIdentifiers)
	{
		Preconditions.checkNotNull(columnIdentifiers);

		final int numberColumns = columnIdentifiers.size();
		Preconditions.checkArgument(numberColumns == 1 || numberColumns == 2 || numberColumns == 3 || numberColumns == 4 || numberColumns == 6 || numberColumns == 9);

		for (Object object : columnIdentifiers)
		{
			if (object instanceof Number)
			{
				Number number = (Number) object;
				Preconditions.checkArgument(Double.compare(number.longValue(), 0) >= 0);
			}
		}
	}

	private boolean isLoaded()
	{
		return (data != null && defaultRange != null);
	}

}

package edu.jhuapl.saavtk.util.file;

import java.io.IOException;

import com.google.common.base.Preconditions;

import vtk.vtkFloatArray;

public abstract class LoadableTable
{
	private vtkFloatArray data;
	private double[] defaultRange;

	public abstract double[] getTuple(int recordIndex);

	protected abstract void doLoad() throws IOException;

	public final void load() throws IOException
	{
		if (!isLoaded())
		{
			doLoad();
		}
	}

	public final void reload() throws IOException
	{
		unload();
		doLoad();
	}

	public void unload()
	{
		set(null, null);
	}

	public boolean isLoaded()
	{
		return (data != null && defaultRange != null);
	}

	public vtkFloatArray getData()
	{
		Preconditions.checkState(isLoaded());
		return data;
	}

	public double[] getDefaultRange()
	{
		Preconditions.checkState(isLoaded());
		return defaultRange;
	}

	protected final void set(vtkFloatArray data, double[] defaultRange)
	{
		Preconditions.checkState((data == null && defaultRange != null) || (data != null && defaultRange == null));
		this.data = data;
		this.defaultRange = defaultRange;
	}
}

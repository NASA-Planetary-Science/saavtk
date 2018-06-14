package edu.jhuapl.saavtk.util.file;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class DataFileInfo
{
	public static DataFileInfo of(List<? extends DataObjectInfo> dataObjectInfo)
	{
		return new DataFileInfo(dataObjectInfo);
	}

	private final ImmutableList<DataObjectInfo> dataObjectInfo;

	protected DataFileInfo(List<? extends DataObjectInfo> dataObjectInfo)
	{
		Preconditions.checkNotNull(dataObjectInfo);
		this.dataObjectInfo = ImmutableList.copyOf(dataObjectInfo);
	}

	public ImmutableList<DataObjectInfo> getDataObjectInfo()
	{
		return dataObjectInfo;
	}

}

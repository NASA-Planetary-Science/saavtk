package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class DataFileInfo
{
	public enum FileFormat
	{
		CSV,
		FITS,
		;
	}

	public static DataFileInfo of(File file, FileFormat format, List<? extends DataObjectInfo> dataObjectInfo)
	{
		return new DataFileInfo(file, format, dataObjectInfo);
	}

	private final File file;
	private final FileFormat format;
	private final ImmutableList<DataObjectInfo> dataObjectInfo;

	protected DataFileInfo(File file, FileFormat format, List<? extends DataObjectInfo> dataObjectInfo)
	{
		Preconditions.checkNotNull(file);
		Preconditions.checkNotNull(format);
		Preconditions.checkNotNull(dataObjectInfo);
		this.file = file;
		this.format = format;
		this.dataObjectInfo = ImmutableList.copyOf(dataObjectInfo);
	}

	public File getFile()
	{
		return file;
	}

	public ImmutableList<DataObjectInfo> getDataObjectInfo()
	{
		return dataObjectInfo;
	}

	@Override
	public String toString()
	{
		int numberObjects = dataObjectInfo.size();
		return format + " format file " + file + ": " + numberObjects + " data object" + (numberObjects != 1 ? "s: " : ": ") + dataObjectInfo;
	}
}

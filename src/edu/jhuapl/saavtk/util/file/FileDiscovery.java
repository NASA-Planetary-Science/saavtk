package edu.jhuapl.saavtk.util.file;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.metadata.Serializers;
import edu.jhuapl.saavtk.model.BasicColoringDataManager;
import edu.jhuapl.saavtk.model.ColoringData;
import edu.jhuapl.saavtk.model.ColoringDataManager;
import edu.jhuapl.saavtk.model.GenericPolyhedralModel;
import edu.jhuapl.saavtk.util.SafePaths;
import edu.jhuapl.saavtk.util.file.TableInfo.ColumnInfo;

public class FileDiscovery
{
	private final File topDirectory;
	private final File coloringDirectory;
	private final BasicColoringDataManager coloringDataManager;

	protected FileDiscovery(String[] args)
	{
		Preconditions.checkNotNull(args);
		Preconditions.checkArgument(args.length > 2, "Too few arguments");

		String dataId = args[0];

		File topDirectory = new File(args[1]);
		Preconditions.checkArgument(topDirectory.isDirectory(), "Not a directory " + topDirectory);

		File coloringDirectory = new File(args[2]);
		Preconditions.checkArgument(coloringDirectory.isDirectory(), "Not a directory " + coloringDirectory);

		this.topDirectory = topDirectory;
		this.coloringDirectory = coloringDirectory;
		this.coloringDataManager = BasicColoringDataManager.of(dataId);
	}

	public void run() throws IOException
	{
		for (File file : coloringDirectory.listFiles())
		{
			if (file.isFile())
			{
				try
				{
					DataFileInfo fileInfo = DataFileReader.of().readFileInfo(file);
					System.err.println(fileInfo);
					extractColorings(fileInfo);
				}
				catch (Exception e)
				{
					reportThrowable(e);
					System.err.println("Skipping file " + file);
				}
			}
		}
		Serializers.serialize("Coloring Metadata", coloringDataManager.getMetadataManager(), SafePaths.get(coloringDirectory.getPath(), "coloring.smd").toFile());
	}

	public ColoringDataManager getColoringDataManager()
	{
		return coloringDataManager.copy();
	}

	protected void extractColorings(DataFileInfo fileInfo)
	{
		for (DataObjectInfo objectInfo : fileInfo.getDataObjectInfo())
		{
			if (objectInfo instanceof TableInfo)
			{
				TableInfo tableInfo = (TableInfo) objectInfo;
				int numberColumns = tableInfo.getNumberColumns();
				File file = fileInfo.getFile();

				// This has got to go, obviously.
				if (numberColumns == 1)
				{
					// One scalar coloring, presumably from text file.
					addScalarColoring(file, tableInfo, 0);
				}
				else if (numberColumns == 3)
				{
					// One vector coloring, presumably from text file.
				}
				else if (numberColumns == 6)
				{
					// While this could be a text file with vector + vector error, the more likely scenario is
					// a FITS file with scalar coloring in column 4, sigma in column 5. Assume that to be true.
					addScalarColoring(file, tableInfo, 4);
					addScalarColoring(file, tableInfo, 5);
				}
				else if (numberColumns == 10)
				{
					// Assume FITS file with vector coloring in 4, 6, 8, vector sigma in 5, 7, 9.
					addVectorColoring(file, tableInfo, 4, 6, 8);
					addVectorColoring(file, tableInfo, 5, 7, 9);
				}
				else
				{
					throw new UnsupportedOperationException("Don't know how to handle a table with " + numberColumns + " columns in data object " + objectInfo.getTitle() + " in file " + fileInfo.getFile());
				}
			}
		}
	}

	protected void addScalarColoring(File file, TableInfo tableInfo, int columnNumber)
	{
		String name = null;
		try
		{
			name = getColoringName(file, tableInfo, columnNumber);
			if (name == null)
			{
				throw new IllegalArgumentException("Cannot deduce scalar coloring name for column " + tableInfo.getColumnInfo(columnNumber));
			}

			String units = getUnits(name, tableInfo, columnNumber);

			file = new File(file.getAbsolutePath().replace(topDirectory.getAbsolutePath(), ""));
			coloringDataManager.add(ColoringData.of(name, file.toString(), ImmutableList.of(name), units, tableInfo.getNumberRows(), false));
		}
		catch (Exception e)
		{
			reportThrowable(e);
			System.err.println("Skipping scalar coloring " + name);
		}
	}

	protected void addVectorColoring(File file, TableInfo tableInfo, int xColumn, int yColumn, int zColumn)
	{
		String name = null;
		try
		{
			name = getColoringName(file, tableInfo, xColumn);
			if (name == null)
			{
				throw new IllegalArgumentException("Cannot deduce vector coloring name for table " + tableInfo);
			}

			String units = getUnits(name, tableInfo, xColumn);
			if (units == null ? getUnits(name, tableInfo, yColumn) != null : !units.equalsIgnoreCase(getUnits(name, tableInfo, yColumn)))
			{
				throw new IllegalArgumentException("Units of vector coloring must be the same for all elements in table " + tableInfo);
			}
			if (units == null ? getUnits(name, tableInfo, zColumn) != null : !units.equalsIgnoreCase(getUnits(name, tableInfo, zColumn)))
			{
				throw new IllegalArgumentException("Units of vector coloring must be the same for all elements in table " + tableInfo);
			}

			if (units == null)
			{
				units = "";
			}
			file = new File(file.getAbsolutePath().replace(topDirectory.getAbsolutePath() + File.separator, ""));
			coloringDataManager.add(ColoringData.of(name, file.toString(), ImmutableList.of(name + "X", name + "Y", name + "Z"), units, tableInfo.getNumberRows(), false));
		}
		catch (Exception e)
		{
			reportThrowable(e);
			System.err.println("Skipping vector coloring " + name);
		}
	}

	private String getColoringName(File file, TableInfo tableInfo, int columnNumber)
	{
		ColumnInfo info = tableInfo.getColumnInfo(columnNumber);
		final String columnName = info.getName();

		String name = guessColoringName(file.getName());
		if (name != null)
		{
			String lowerCaseColumnName = columnName.toLowerCase();
			if (lowerCaseColumnName.contains("err") || lowerCaseColumnName.contains("sig"))
			{
				name = name + " Error";
			}
		}
		else
		{
			name = guessColoringName(columnName);
			if (name == null)
			{
				name = validateString(columnName, info);
			}
		}
		return name;
	}

	private String getUnits(String name, TableInfo tableInfo, int columnNumber)
	{
		try
		{
			ColumnInfo columnInfo = tableInfo.getColumnInfo(columnNumber);
			return validateString(columnInfo.getUnits(), columnInfo);
		}
		catch (@SuppressWarnings("unused") IllegalArgumentException e)
		{
			return getUnits(name);
		}
	}

	private String guessColoringName(String string)
	{
		string = string.toLowerCase();
		if (string.matches(".*slo?p.*"))
		{
			return GenericPolyhedralModel.SlopeStr;
		}
		else if (string.matches(".*ele?v.*"))
		{
			return GenericPolyhedralModel.ElevStr;
		}
		else if (string.matches(".*acc.*"))
		{
			return GenericPolyhedralModel.GravAccStr;
		}
		else if (string.matches(".*pot.*"))
		{
			return GenericPolyhedralModel.GravPotStr;
		}
		return null;
	}

	private String validateString(String string, ColumnInfo info)
	{
		try
		{
			if (string != null && string.matches(".*\\S.*"))
			{
				Double.parseDouble(string);
			}
			// Success is bad in this case. It means the name looks like a number.
			throw new IllegalArgumentException("String " + string + " looks like a number in column " + info);
		}
		catch (@SuppressWarnings("unused") NumberFormatException e)
		{
			// That's good actually!
			return string;
		}

	}

	private String getUnits(String name)
	{
		if (GenericPolyhedralModel.SlopeStr.equals(name))
		{
			return GenericPolyhedralModel.SlopeUnitsStr;
		}
		else if (GenericPolyhedralModel.ElevStr.equals(name))
		{
			return GenericPolyhedralModel.ElevUnitsStr;
		}
		else if (GenericPolyhedralModel.GravAccStr.equals(name))
		{
			return GenericPolyhedralModel.GravAccUnitsStr;
		}
		else if (GenericPolyhedralModel.GravPotStr.equals(name))
		{
			return GenericPolyhedralModel.GravPotUnitsStr;
		}
		return null;
	}

	private static void reportThrowable(Throwable t)
	{
		String message = t.getLocalizedMessage();
		if (message != null)
		{
			System.err.println(message);
		}
		else
		{
			t.printStackTrace();
		}
	}

	private static void usage()
	{
		System.err.println("Usage:\tdiscovery unique-model-id model-top-directory-name coloring-file-directory-name");
	}

	public static void main(String[] args)
	{
		try
		{
			FileDiscovery discovery = new FileDiscovery(args);
			discovery.run();
		}
		catch (IllegalArgumentException e)
		{
			reportThrowable(e);
			usage();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

}

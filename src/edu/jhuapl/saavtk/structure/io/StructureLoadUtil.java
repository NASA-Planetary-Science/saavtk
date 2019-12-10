package edu.jhuapl.saavtk.structure.io;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.model.structure.CircleModel;
import edu.jhuapl.saavtk.model.structure.EllipseModel;
import edu.jhuapl.saavtk.model.structure.EllipsePolygon;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.model.structure.PolygonModel;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.util.FileUtil;

/**
 * Collection of utility methods to support loading of SBMT structures and
 * related objects.
 * <P>
 * Note some of the methods in this class are transitional and will eventually
 * go away. Other methods have been refactored out so as to keep serialization
 * code separate from model based classes.
 *
 * @author lopeznr1
 */
public class StructureLoadUtil
{
	/**
	 * Utility method to convert a Color into an int array of 4 elements: RGBA.
	 * <P>
	 * This method is a transitional method and may eventually go away.
	 */
	@Deprecated
	public static int[] convertColorToRgba(Color aColor)
	{
		int r = aColor.getRed();
		int g = aColor.getGreen();
		int b = aColor.getBlue();
		int a = aColor.getAlpha();
		int[] retArr = { r, g, b, a };
		return retArr;
	}

	/**
	 * Utility method to convert an int array of 3 or 4 elements into a Color. Order
	 * of elements is assumed to be RGB (and optional alpha). Each element should
	 * have a value in the range of [0 - 255].
	 * <P>
	 * This method is a transitional method and may eventually go away.
	 */
	@Deprecated
	public static Color convertRgbaToColor(int[] aArr)
	{
		int rVal = aArr[0];
		int gVal = aArr[1];
		int bVal = aArr[2];
		int aVal = 255;
		if (aArr.length >= 4)
			aVal = aArr[3];

		return new Color(rVal, gVal, bVal, aVal);
	}

	/**
	 * Utility method to load in a list of {@link EllipsePolygon}s from the
	 * specified input file.
	 * <P>
	 * Note the returned list of {@link EllipsePolygon}s will not have any
	 * initialized VTK state data. Each returned {@link EllipsePolygon} will need to
	 * have it's VTK state data initialized via updatePolygon().
	 */
	public static List<EllipsePolygon> loadEllipsePolygons(File aFile, Mode aMode, double aDefaultRadius,
			Color aDefaultColor, int aNumberOfSides, String aType) throws IOException
	{
		Pattern workPattern = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");

		List<String> lineL = FileUtil.getFileLinesAsStringList(aFile.getAbsolutePath());
		List<EllipsePolygon> retL = new ArrayList<>();
		for (String aLine : lineL)
		{
			// Skip over empty lines / line comments
			String tmpStr = aLine.trim();
			if (tmpStr.isEmpty() == true || tmpStr.startsWith("#") == true)
				continue;

			// Tokenize the input line
			List<String> wordL = new ArrayList<>();
			Matcher m = workPattern.matcher(aLine);
			while (m.find())
				wordL.add(m.group(1));
			String[] words = new String[wordL.size()];
			wordL.toArray(words);

			// The latest version of this file format has 16 columns. The previous version
			// had 10 columns for circles and 13 columns for points. We still want to
			// support loading both versions, so look at how many columns are in the line.

			// The first 8 columns are the same in both the old and new formats.
			int id = Integer.parseInt(words[0]);
			String name = words[1];

			double[] center = new double[3];
			center[0] = Double.parseDouble(words[2]);
			center[1] = Double.parseDouble(words[3]);
			center[2] = Double.parseDouble(words[4]);

			// Vars that we will need initialized
			String label = "";
			Color color = null;
			double radius;
			double flattening;
			double angle;

			// Note the next 3 words in the line (the point in spherical coordinates) are
			// not used

			// LatLon latLon=MathUtil.reclat(pol.center);
			// System.out.println(words[5]+" "+(360-Double.parseDouble(words[6]))+"
			// "+Math.toDegrees(latLon.lat)+" "+Math.toDegrees(latLon.lon));

			// For the new format and the points file in the old format, the next 4 columns
			// (slope, elevation, acceleration, and potential) are not used.
			if (words.length == 18)
			{
				radius = Double.parseDouble(words[12]) / 2.0; // read in diameter not radius
				if (aMode == Mode.ELLIPSE_MODE)
				{
					flattening = Double.parseDouble(words[13]);
					angle = Double.parseDouble(words[14]);
				}
				else
				{
					flattening = 1.0;
					angle = 0.0;
				}
				int colorIdx = 15;
				color = transformStringToColorArr(words[colorIdx]);

				if (words[words.length - 1].startsWith("\"")) // labels in quotations
				{
					label = words[words.length - 1];
					label = label.substring(1, label.length() - 1);
				}
			}
			else
			{

				if (words.length < 16)
				{
					// OLD VERSION of file
					if (aMode == Mode.CIRCLE_MODE || aMode == Mode.ELLIPSE_MODE)
						radius = Double.parseDouble(words[8]) / 2.0; // read in diameter not radius
					else
						radius = aDefaultRadius;
				}
				else
				{
					// NEW VERSION of file
					radius = Double.parseDouble(words[12]) / 2.0; // read in diameter not radius
				}

				if (aMode == Mode.ELLIPSE_MODE && words.length >= 16)
				{
					flattening = Double.parseDouble(words[13]);
					angle = Double.parseDouble(words[14]);
				}
				else
				{
					flattening = 1.0;
					angle = 0.0;
				}

				// If there are 9 or more columns in the file, the last column is the color in
				// both the new and old formats.
				if (words.length > 9)
				{
					int colorIdx = words.length - 3;
					if (words.length == 17)
						colorIdx = 15;

					color = transformStringToColorArr(words[colorIdx]);
				}

				// Second to last word is the label, last string is the color
				if (words[words.length - 2].substring(0, 2).equals("l:"))
				{
					label = words[words.length - 2].substring(2);
				}
				// new format means no color
				else if (words[words.length - 1].startsWith("\"")) // labels in quotations
				{
					label = words[words.length - 1];
					label = label.substring(1, label.length() - 1);
				}
//				else
//				{
//					label = words[words.length - 1];
//				}

				// if(words[words.length-1].substring(0, 3).equals("lc:"))
				// {
				// double[] labelcoloradd = {1.0,1.0,1.0};
				// String[] labelColors=words[words.length-1].substring(3).split(",");
				// labelcoloradd[0] = Double.parseDouble(labelColors[0]);
				// labelcoloradd[1] = Double.parseDouble(labelColors[1]);
				// labelcoloradd[2] = Double.parseDouble(labelColors[2]);
				// pol.labelcolor=labelcoloradd;
				// colors.add(labelcoloradd);
				// }
			}

			// Utilize the defaultColor if failed to read one in
			if (color == null)
				color = aDefaultColor;

			// Synthesize the EllipsePolygon
			EllipsePolygon tmpPoly = new EllipsePolygon(aNumberOfSides, aType, color, aMode, id, label);
			tmpPoly.setAngle(angle);
			tmpPoly.setCenter(center);
			tmpPoly.setFlattening(flattening);
			tmpPoly.setRadius(radius);
			tmpPoly.setName(name);
			tmpPoly.setLabel(label);
			boolean tmpBool = label != null && label.equals("") == false;
			tmpPoly.setLabelVisible(tmpBool);

			retL.add(tmpPoly);
		}

		return retL;
	}

	/**
	 * Utility method that will load a list of {@link Structure}s from the specified
	 * file and return a manager ({@link StructureManager}) which contains the list of
	 * structures.
	 *
	 * @param aFile The file of interest.
	 * @param aName Enum which describes the type of structures stored in the file.
	 * @param aBody {@link PolyhedralModel} where the structures will be associated
	 *              with.
	 * @return
	 * @throws Exception
	 */
	public static StructureManager<?> loadStructureManagerFromFile(File aFile, ModelNames aName, PolyhedralModel aBody)
			throws Exception
	{
		StructureManager<?> retManager = null;
		switch (aName)
		{
			case CIRCLE_STRUCTURES:
				retManager = new CircleModel(aBody);
				break;
			case ELLIPSE_STRUCTURES:
				retManager = new EllipseModel(aBody);
				break;
			case POINT_STRUCTURES:
				retManager = new PointModel(aBody);
				break;
			case POLYGON_STRUCTURES:
				retManager = new PolygonModel(aBody);
				break;
			case LINE_STRUCTURES:
				retManager = new LineModel<>(aBody);
				break;
			default:
				throw new Error(aName.name() + " is not a valid structures type");
		}

		retManager.loadModel(aFile, false, null);
		if (retManager.getNumItems() == 0)
			throw new Exception("No valid " + aName.name() + " found");
		return retManager;
	}

	/**
	 * Utility method that converts the specified string into a color (3 element int
	 * array).
	 * <P>
	 * The string should be composed of 3 values (integers in range of [0-255]
	 * separated by commas with no whitespace).
	 * <P>
	 * If the string is improperly formatted then null will be returned. On failure
	 * to parse the integers a NumberFormatException will be thrown.
	 */
	public static Color transformStringToColorArr(String aStr)
	{
		Color retColor = null;

		String[] strArr = aStr.split(",");
		if (strArr.length == 3)
		{
			int rVal = Integer.parseInt(strArr[0]);
			int gVal = Integer.parseInt(strArr[1]);
			int bVal = Integer.parseInt(strArr[2]);
			retColor = new Color(rVal, gVal, bVal);
		}

		return retColor;
	}

}

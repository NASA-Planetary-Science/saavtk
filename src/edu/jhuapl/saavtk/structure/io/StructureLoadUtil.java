package edu.jhuapl.saavtk.structure.io;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.w3c.dom.Element;

import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.Point;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureType;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.vtk.font.FontAttr;
import glum.io.token.TokenUtil;

/**
 * Collection of utility methods to support loading of SBMT structures and related objects.
 * <p>
 * Most (if not all) SBMT structure file formats should be loaded via the method {@link #loadStructures(File)}.
 * <p>
 * Internally, heuristics are used to determine the appropriate deserializer for a specific file.
 *
 * @author lopeznr1
 */
public class StructureLoadUtil
{
	/**
	 * Utility method that will load the structures from the specified file.
	 * <p>
	 * Returns the list of loaded structures. Note that the returned list may not consist of various structure types.
	 * <p>
	 * This method supports the loading the following structure file types:
	 * <ul>
	 * <li>SBMT CSV Structures
	 * <li>SBMT XML Structures
	 * <li>TODO: Add support for: ESRI Structures
	 * </ul>
	 */
	public static List<Structure> loadStructures(File aFile) throws IOException
	{
		// Attempt to load the structure as a hard-edge structure
		List<Structure> retL;
		try
		{
			retL = loadHardEdgeStructures(aFile);
		}
		catch (Exception aExp)
		{
			// Attempt to load the structure as a round-edge structure
			try
			{
				retL = loadRoundEdgeStructures(aFile);
			}
			catch (Exception aExp2)
			{
				System.err.println("[StructureLoadUtil] Failed to load file: " + aFile + " Skipping...");
				retL = new ArrayList<>();
			}
		}

		return retL;
	}

	/**
	 * Helper method for loading structures from an XML file.
	 * <p>
	 * Currently only hard-edge structures are stored in an XML file.
	 */
	private static List<Structure> loadHardEdgeStructures(File aFile) throws IOException
	{
		// Load the XML file
		Element rootElement = XmlLoadUtil.loadRoot(aFile);
		if (rootElement == null)
			throw new IOException("No root element in XML doc!");

		var retItemL = XmlLoadUtil.loadPolyLinesFromElement(aFile, rootElement);

		// Install the associated shape model name
		String shapeModelName = XmlLoadUtil.getShapeModelNameFrom(rootElement);
		for (var aItem : retItemL)
			aItem.setShapeModelId(shapeModelName);

		return retItemL;
	}

	/**
	 * Helper method for loading structures from a SBMT text file.
	 * <p>
	 * Currently only round-edge structures are stored in plain SBMT structure text files.
	 */
	private static List<Structure> loadRoundEdgeStructures(File aFile) throws IOException
	{
		StructureType tmpTypeHint = null;

		// Load the Ellipse file
		List<Structure> tmpItemL = null;
		try
		{
			tmpItemL = StructureLoadUtil.loadEllipses(aFile, false);

			double initRadius = Double.NaN;
			if (tmpItemL.size() > 0)
			{
				initRadius = 0.0;
				if (tmpItemL.get(0) instanceof Ellipse aEllipse)
					initRadius = aEllipse.getRadius();
			}

			// Determine if we loaded ellipses, circles, or points
			// This is possible since all items in the same file must be the same type
			boolean isAllRadiusConst = true;
			boolean isAllCircles = true;
			for (var aItem : tmpItemL)
			{
				var evalAngle = 0.0;
				var evalFlattening = 1.0;
				var evalRadius = initRadius;
				if (aItem instanceof Ellipse aEllipse)
				{
					evalAngle = aEllipse.getAngle();
					evalFlattening = aEllipse.getFlattening();
					evalRadius = aEllipse.getRadius();
				}

				isAllCircles &= evalAngle == 0.0;
				isAllCircles &= evalFlattening == 1.0;

				isAllRadiusConst &= evalRadius == initRadius;
			}

			// Perform heuristics
			if (isAllCircles == false)
				tmpTypeHint = StructureType.Ellipse;
			else if (isAllRadiusConst == false && tmpItemL.size() >= 2)
				tmpTypeHint = StructureType.Circle;
		}
		catch (Exception aExp)
		{
			// Must be points or unsupported
			tmpItemL = StructureLoadUtil.loadEllipses(aFile, true);
		}

		// Update to reflect it's source
		var retItemL = new ArrayList<Structure>();
		for (var aItem : tmpItemL)
		{
			var center = (Vector3D) null;
			var radius = Double.NaN;
			var angle = Double.NaN;
			var flattening = Double.NaN;
			if (aItem instanceof Point aPoint)
				center = aPoint.getCenter();
			else if (aItem instanceof Ellipse aEllipse)
			{
				center = aEllipse.getCenter();
				angle = aEllipse.getAngle();
				flattening = aEllipse.getFlattening();
				radius = aEllipse.getRadius();
			}
			else
				throw new Error("Unexpected type: " + aItem.getClass());

			var tmpType = aItem.getType();
			if (tmpType == null)
				tmpType = tmpTypeHint;

			var tmpItem = (Structure) null;
			if (tmpType == StructureType.Point)
				tmpItem = new Point(aItem.getId(), aFile, center, aItem.getColor());
			else
				tmpItem = new Ellipse(aItem.getId(), aFile, tmpType, center, radius, angle, flattening, aItem.getColor());
			tmpItem.setLabel(aItem.getLabel());
			tmpItem.setName(aItem.getName());
			retItemL.add(tmpItem);
		}

		return retItemL;
	}

	/**
	 * Utility method to load in a list of {@link Ellipse}s from the specified input file.
	 * <p>
	 * Note the returned list of {@link Ellipse}s will not have any initialized VTK state data. Each returned
	 * {@link Ellipse} will need to have it's VTK state data initialized via updatePolygon().
	 * <p>
	 * This method originated from (~2019Oct07):<br/>
	 * edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.java
	 */
	private static List<Structure> loadEllipses(File aFile, boolean aIsForcePointMode) throws IOException
	{
		var workPattern = Pattern.compile("([^\"]\\S*|\".*?\")\\s*");

		StructureType tmpType = null;
		if (aIsForcePointMode == true)
			tmpType = StructureType.Point;

		var retItemL = new ArrayList<Structure>();

		List<String> lineL = FileUtil.getFileLinesAsStringList(aFile.getAbsolutePath());
		for (String aLine : lineL)
		{
			// Skip over empty lines / line comments
			String tmpStr = aLine.trim();

			// Check for comment that signals the type of structures to follow
			tmpStr = tmpStr.replaceAll("\\s+", "").toLowerCase();
			if (tmpStr.equals("#type,circle") == true)
				tmpType = StructureType.Circle;
			else if (tmpStr.equals("#type,ellipse") == true)
				tmpType = StructureType.Ellipse;
			else if (tmpStr.equals("#type,point") == true)
				tmpType = StructureType.Point;

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
			String name = TokenUtil.getRawStr(words[1]);

			double xVal = Double.parseDouble(words[2]);
			double yVal = Double.parseDouble(words[3]);
			double zVal = Double.parseDouble(words[4]);
			Vector3D center = new Vector3D(xVal, yVal, zVal);

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
				if (tmpType != StructureType.Point)
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
					radius = Double.NaN;
					if (tmpType != StructureType.Point)
						radius = Double.parseDouble(words[8]) / 2.0; // read in diameter not radius
				}
				else
				{
					// NEW VERSION of file
					radius = Double.parseDouble(words[12]) / 2.0; // read in diameter not radius
				}

				flattening = 1.0;
				angle = 0.0;
				if (tmpType != StructureType.Point && words.length >= 16)
				{
					flattening = Double.parseDouble(words[13]);
					angle = Double.parseDouble(words[14]);
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

			// Synthesize the Structure
			var tmpItem = (Structure) null;
			if (tmpType == StructureType.Point)
				tmpItem = new Point(id, aFile, center, color);
			else
				tmpItem = new Ellipse(id, aFile, tmpType, center, radius, angle, flattening, color);
			tmpItem.setName(name);
			tmpItem.setLabel(label);
			var tmpFA = tmpItem.getLabelFontAttr();
			tmpFA = new FontAttr(tmpFA.getFace(), tmpFA.getColor(), tmpFA.getSize(), false);
			tmpItem.setLabelFontAttr(tmpFA);

			retItemL.add(tmpItem);
		}

		return retItemL;
	}

	/**
	 * Utility method that converts the specified string into a color (3 element int array).
	 * <p>
	 * The string should be composed of 3 values (integers in range of [0-255] separated by commas with no whitespace).
	 * <p>
	 * If the string is improperly formatted then null will be returned. On failure to parse the integers a
	 * NumberFormatException will be thrown.
	 */
	private static Color transformStringToColorArr(String aStr)
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

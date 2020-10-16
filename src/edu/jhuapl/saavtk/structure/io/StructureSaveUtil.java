package edu.jhuapl.saavtk.structure.io;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.plateColoring.ColoringData;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.util.EllipseUtil;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;

/**
 * Collection of utility methods for serializing structures (or aspects of a
 * structure).
 * <P>
 * A number of methods originated from the package (~2019Oct07):
 * edu.jhuapl.saavtk.model.structure.*
 * <P>
 * These methods have been factored out so that serialization logic is separate
 * from classes of type {@link StructureManager}.
 *
 * @author lopeznr1
 */
public class StructureSaveUtil
{
	/**
	 * Utility method for saving the content of a list of {@link Ellipse}s.
	 * <P>
	 * This method originated from (~2019Oct07):
	 * edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.java
	 */
	public static void saveModel(File aFile, AbstractEllipsePolygonModel aManager, List<Ellipse> aStructureL,
			PolyhedralModel aSmallBody) throws IOException
	{
		FileWriter fstream = new FileWriter(aFile);
		BufferedWriter out = new BufferedWriter(fstream);

		String[] standardColoringUnitArr = EllipseUtil.getStandardColoringUnits(aSmallBody);

		// Write the header comments
		Mode tmpMode = aManager.getMode();
		writeHeaderComments(out, tmpMode, standardColoringUnitArr);

		// Write the data content
		for (Ellipse aEllipse : aStructureL)
		{
			String name = aEllipse.getName();
			if (name.length() == 0)
				name = "default";

			// Since tab is used as the delimiter, replace any tabs in the name with spaces.
			name = name.replace('\t', ' ');

			LatLon llr = MathUtil.reclat(aEllipse.getCenter().toArray());
			double lat = llr.lat * 180.0 / Math.PI;
			double lon = llr.lon * 180.0 / Math.PI;
			if (lon < 0.0)
				lon += 360.0;

			double[] centerArr = aEllipse.getCenter().toArray();
			String str = "" + aEllipse.getId() + "\t" + name + "\t" + centerArr[0] + "\t" + centerArr[1] + "\t"
					+ centerArr[2] + "\t" + lat + "\t" + lon + "\t" + llr.rad;

			str += "\t";

			double[] values = EllipseUtil.getStandardColoringValues(aManager, aEllipse, aSmallBody);
			for (int i = 0; i < values.length; ++i)
			{
				str += Double.isNaN(values[i]) ? "NA" : values[i];
				if (i < values.length - 1)
					str += "\t";
			}

			str += "\t" + 2.0 * aEllipse.getRadius(); // save out as diameter, not radius

			str += "\t" + aEllipse.getFlattening() + "\t" + aEllipse.getAngle();

			Color color = aEllipse.getColor();
			str += "\t" + color.getRed() + "," + color.getGreen() + "," + color.getBlue();

			if (tmpMode == Mode.ELLIPSE_MODE)
			{
				Double gravityAngle = EllipseUtil.getEllipseAngleRelativeToGravityVector(aEllipse, aSmallBody);
				if (gravityAngle != null)
					str += "\t" + gravityAngle;
				else
					str += "\t" + "NA";
			}

			str += "\t" + "\"" + aEllipse.getLabel() + "\"";

			// String labelcolorStr="\tlc:"+pol.labelcolor[0] + "," + pol.labelcolor[1] +
			// "," + pol.labelcolor[2];
			// str+=labelcolorStr;

			str += "\n";

			out.write(str);
		}

		out.close();
	}

	/**
	 * Save out a file which contains the value of the various coloring data as a
	 * function of distance along the profile. A profile is path with only 2 control
	 * points.
	 * <P>
	 * Before this method was refactored out, it was required that the provided 3D
	 * points originated from a line with only 2 control points. It is left to the
	 * caller to ensure that this requirement is met or that consumers of the output
	 * file are robust to handle 3D points output that does not originate from 2
	 * control points.
	 * <P>
	 * This method originated from (~2019Oct07):
	 * edu.jhuapl.saavtk.model.structure.LineModel.java
	 *
	 * @param aFile           File where the content will be saved to.
	 * @param aSmallBodyModel {@link PolyhedralModel} associated with the provided
	 *                        points.
	 * @param aXyzPointL      A list of points along the specified
	 *                        {@link PolyhedralModel}. These points are assumed to
	 *                        have come from a line with only 2 control points!
	 */
	public static void saveProfile(File aFile, PolyhedralModel aSmallBodyModel, List<Vector3D> aXyzPointL)
			throws Exception
	{
		final String lineSeparator = System.getProperty("line.separator");

		FileWriter fstream = new FileWriter(aFile);
		BufferedWriter out = new BufferedWriter(fstream);

		// write header
		out.write("Distance (m)");
		out.write(",X (m)");
		out.write(",Y (m)");
		out.write(",Z (m)");
		out.write(",Latitude (deg)");
		out.write(",Longitude (deg)");
		out.write(",Radius (m)");

		List<ColoringData> colorings = aSmallBodyModel.getAllColoringData();
		for (ColoringData coloring : colorings)
		{
			String units = coloring.getUnits();
			for (String element : coloring.getTupleNames())
			{
				out.write("," + element);
				if (!units.isEmpty())
					out.write(" (" + units + ")");
			}
		}
		out.write(lineSeparator);

		// For each point in aXyzPointL, find the cell containing that
		// point and then, using barycentric coordinates find the value
		// of the height at that point
		//
		// To compute the distance, assume we have a straight line connecting the first
		// and last points of aXyzPointL. For each point, p, in aXyzPointL, find the
		// point on the line closest to p. The distance from p to the start of the line
		// is what is placed in heights. Use SPICE's nplnpt function for this.

		double[] first = aXyzPointL.get(0).toArray();
		double[] last = aXyzPointL.get(aXyzPointL.size() - 1).toArray();
		double[] lindir = new double[3];
		lindir[0] = last[0] - first[0];
		lindir[1] = last[1] - first[1];
		lindir[2] = last[2] - first[2];

		// The following can be true if the user clicks on the same point twice
		boolean zeroLineDir = MathUtil.vzero(lindir);

		double[] pnear = new double[3];
		double[] notused = new double[1];

		for (Vector3D aPt : aXyzPointL)
		{
			double[] xyzArr = aPt.toArray();

			double distance = 0.0;
			if (!zeroLineDir)
			{
				MathUtil.nplnpt(first, lindir, xyzArr, pnear, notused);
				distance = 1000.0 * MathUtil.distanceBetween(first, pnear);
			}

			out.write(String.valueOf(distance));

			double[] vals = aSmallBodyModel.getAllColoringValues(xyzArr);

			out.write("," + 1000.0 * aPt.getX());
			out.write("," + 1000.0 * aPt.getY());
			out.write("," + 1000.0 * aPt.getZ());

			LatLon llr = MathUtil.reclat(xyzArr).toDegrees();
			out.write("," + llr.lat);
			out.write("," + llr.lon);
			out.write("," + 1000.0 * llr.rad);

			for (double val : vals)
				out.write("," + val);

			out.write(lineSeparator);
		}

		out.close();
	}

	/**
	 * Utility helper method for writing out the header comments
	 *
	 * @param aBW                      {@link BufferedWriter} where comments will be
	 *                                 written to.
	 * @param aMode                    The mode associated with the associated round
	 *                                 structures.
	 * @param aStandardColoringUnitArr 4-element array containing the standard
	 *                                 coloring units. If the standard coloring
	 *                                 units are not available then null should be
	 *                                 at the relevant index.
	 */
	private static void writeHeaderComments(BufferedWriter aBW, Mode aMode, String[] aStandardColoringUnitArr)
			throws IOException
	{
		// Extract the text to utilize as the coloring unit info string
		String unitStrArr[] = new String[4];
		for (int c1 = 0; c1 < aStandardColoringUnitArr.length; c1++)
		{
			String tmpStr = "NA";
			if (aStandardColoringUnitArr[c1] != null)
				tmpStr = aStandardColoringUnitArr[c1];
			unitStrArr[c1] = tmpStr;
		}
		String coloringUnitStr = String.format("slope (%s), elevation (%s), acceleration (%s), potential (%s)",
				unitStrArr[0], unitStrArr[1], unitStrArr[2], unitStrArr[3]);

		String dataTypeStr = "ellipse";
		if (aMode == Mode.CIRCLE_MODE)
			dataTypeStr = "circle";
		else if (aMode == Mode.POINT_MODE)
			dataTypeStr = "point";

		boolean alwaysTrue = true;
		boolean isEllipseF = aMode != Mode.ELLIPSE_MODE;
		boolean isEllipseT = aMode == Mode.ELLIPSE_MODE;

		String columnDefStr = "<id> <name> <centerXYZ[3]> <centerLLR[3]> <coloringValue[4]> <diameter> <flattening> <regularAngle> <colorRGB> <gravityAngle> <label>";
		if (isEllipseF == true)
			columnDefStr = "<id> <name> <centerXYZ[3]> <centerLLR[3]> <coloringValue[4]> <diameter> <flattening> <regularAngle> <colorRGB> <label>";

		writeLine(aBW, alwaysTrue, "# SBMT Structure File");
		writeLine(aBW, alwaysTrue, "# type," + dataTypeStr);
		writeLine(aBW, alwaysTrue, "# ------------------------------------------------------------------------------");
		writeLine(aBW, alwaysTrue, "# File consists of a list of structures on each line.");
		writeLine(aBW, alwaysTrue, "#");
		writeLine(aBW, isEllipseF, "# Each line is defined by 17 columns with the following:");
		writeLine(aBW, isEllipseT, "# Each line is defined by 18 columns with the following:");
		writeLine(aBW, alwaysTrue, "# " + columnDefStr + "*");
		writeLine(aBW, alwaysTrue, "#");
		writeLine(aBW, alwaysTrue, "#               id: Id of the structure");
		writeLine(aBW, alwaysTrue, "#             name: Name of the structure");
		writeLine(aBW, alwaysTrue, "#     centerXYZ[3]: 3 columns that define the structure center in 3D space");
		writeLine(aBW, alwaysTrue, "#     centerLLR[3]: 3 columns that define the structure center in lat,lon,radius");
		writeLine(aBW, alwaysTrue, "# coloringValue[4]: 4 columns that define the ellipse “standard” colorings. The");
		writeLine(aBW, alwaysTrue, "#                   colorings are: " + coloringUnitStr);
		writeLine(aBW, alwaysTrue, "#         diameter: Diameter of (semimajor) axis of ellipse");
		writeLine(aBW, alwaysTrue, "#       flattening: Flattening factor of ellipse. Range: [0.0, 1.0]");
		writeLine(aBW, alwaysTrue, "#     regularAngle: Angle between the semimajor axis and the line of longitude");
		writeLine(aBW, alwaysTrue, "#                   as projected onto the surface");
		writeLine(aBW, alwaysTrue, "#         colorRGB: 1 column (of RGB values [0, 255] separated by commas with no");
		writeLine(aBW, alwaysTrue, "#                   spaces). This column appears as a single textual column.");
		writeLine(aBW, isEllipseT, "#     gravityAngle: Angle between the semimajor axis of the ellipse and the gravity");
		writeLine(aBW, isEllipseT, "#                   acceleration vector... (Description continues in user manual)");
		writeLine(aBW, alwaysTrue, "#            label: Label of the structure");
		writeLine(aBW, alwaysTrue, "#");
		writeLine(aBW, alwaysTrue, "#");
		writeLine(aBW, alwaysTrue, "# Please note the following:");
		writeLine(aBW, alwaysTrue, "# - Each line is composed of columns separated by a tab character.");
		writeLine(aBW, alwaysTrue, "# - Blank lines or lines that start with '#' are ignored.");
		writeLine(aBW, alwaysTrue, "# - Angle units: degrees");
		writeLine(aBW, alwaysTrue, "# - Length units: kilometers");
		writeLine(aBW, alwaysTrue, "");
	}

	/**
	 * Helper method that optionally writes out the specified line.
	 *
	 * @param aBool If true then aMsg will be output to the {@link BufferedWriter}.
	 */
	private static void writeLine(BufferedWriter aBW, boolean aBool, String aMsg) throws IOException
	{
		if (aBool == false)
			return;

		aBW.write(aMsg + "\n");
	}

}

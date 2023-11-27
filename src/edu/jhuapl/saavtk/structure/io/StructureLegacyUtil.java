package edu.jhuapl.saavtk.structure.io;

import java.awt.Color;
import java.io.File;

import edu.jhuapl.saavtk.gui.render.QuietSceneChangeNotifier;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.status.QuietStatusNotifier;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import glum.task.SilentTask;

/**
 * Collection of legacy utility methods.
 * <p>
 * Notes:
 * <ul>
 * <li>These methods do not fit the overall design of the redesigned structure package.
 * <li>No new code should rely (primarily) on these methods.
 * </ul>
 * Note some of the methods in this class are transitional and will eventually go away.
 *
 * @author lopeznr1
 */
@Deprecated
public class StructureLegacyUtil
{
	/**
	 * Utility method to convert a Color into an int array of 4 elements: RGBA.
	 * <p>
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
	 * Utility method to convert an int array of 3 or 4 elements into a Color. Order of elements is assumed to be RGB
	 * (and optional alpha). Each element should have a value in the range of [0 - 255].
	 * <p>
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
	 * Utility method that will load a list of {@link Structure}s from the specified file and return a manager
	 * ({@link StructureManager}) which contains the list of structures.
	 * <p>
	 * Please do not use this method in new code. @See {@link StructureLegacyUtil}
	 *
	 * @param aFile
	 *    The file of interest.
	 * @param aName
	 *    Enum which describes the type of structures stored in the file.
	 * @param aBody
	 *    {@link PolyhedralModel} where the structures will be associated with.
	 * @return
	 * @throws Exception
	 */
	public static AnyStructureManager loadStructureManagerFromFile(File aFile, PolyhedralModel aBody) throws Exception
	{
		var tmpSceneChangeNotifier = QuietSceneChangeNotifier.Instance;
		var tmpStatusNotifier = QuietStatusNotifier.Instance;
		var retManager = new AnyStructureManager(tmpSceneChangeNotifier, tmpStatusNotifier, aBody);

		var fullItemL = StructureLoadUtil.loadStructures(aFile);

		var tmpTask = new SilentTask();
		retManager.installItems(tmpTask, fullItemL);

		return retManager;
	}

}

package edu.jhuapl.saavtk.structure.io;

import java.awt.Color;
import java.io.File;
import java.util.List;

import edu.jhuapl.saavtk.gui.render.QuietSceneChangeNotifier;
import edu.jhuapl.saavtk.gui.render.SceneChangeNotifier;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.model.structure.CircleModel;
import edu.jhuapl.saavtk.model.structure.EllipseModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.model.structure.PolygonModel;
import edu.jhuapl.saavtk.status.QuietStatusNotifier;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import glum.task.SilentTask;

/**
 * Collection of legacy utility methods.
 * <p>
 * Notes:
 * <ul>
 * <li>These methods do not fit the overall design of the redesigned structure
 * package.
 * <li>No new code should rely (primarily) on these methods.
 * </ul>
 * Note some of the methods in this class are transitional and will eventually
 * go away.
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
	 * Utility method to convert an int array of 3 or 4 elements into a Color. Order
	 * of elements is assumed to be RGB (and optional alpha). Each element should
	 * have a value in the range of [0 - 255].
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
	 * Utility method that will load a list of {@link Structure}s from the specified
	 * file and return a manager ({@link StructureManager}) which contains the list
	 * of structures.
	 * <p>
	 * Please do not use this method in new code. @See {@link StructureLegacyUtil}
	 *
	 * @param aFile The file of interest.
	 * @param aName Enum which describes the type of structures stored in the file.
	 * @param aBody {@link PolyhedralModel} where the structures will be associated
	 *              with.
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static StructureManager<?> loadStructureManagerFromFile(File aFile, ModelNames aName, PolyhedralModel aBody)
			throws Exception
	{
		List<Structure> fullL = StructureLoadUtil.loadStructures(aFile);
		List tmpL;

		SceneChangeNotifier tmpSceneChangeNotifier = QuietSceneChangeNotifier.Instance;
		StatusNotifier tmpStatusNotifier = QuietStatusNotifier.Instance;
		StructureManager<?> retManager = null;
		switch (aName)
		{
			case CIRCLE_STRUCTURES:
				tmpL = StructureMiscUtil.getEllipsesFrom(fullL, Mode.CIRCLE_MODE);
				retManager = new CircleModel(tmpSceneChangeNotifier, tmpStatusNotifier, aBody);
				break;
			case ELLIPSE_STRUCTURES:
				tmpL = StructureMiscUtil.getEllipsesFrom(fullL, Mode.ELLIPSE_MODE);
				retManager = new EllipseModel(tmpSceneChangeNotifier, tmpStatusNotifier, aBody);
				break;
			case POINT_STRUCTURES:
				tmpL = StructureMiscUtil.getEllipsesFrom(fullL, Mode.POINT_MODE);
				retManager = new PointModel(tmpSceneChangeNotifier, tmpStatusNotifier, aBody);
				break;
			case POLYGON_STRUCTURES:
				tmpL = StructureMiscUtil.getPolygonsFrom(fullL);
				retManager = new PolygonModel(tmpSceneChangeNotifier, tmpStatusNotifier, aBody);
				break;
			case LINE_STRUCTURES:
				tmpL = StructureMiscUtil.getPathsFrom(fullL);
				retManager = new LineModel<>(tmpSceneChangeNotifier, tmpStatusNotifier, aBody);
				break;
			default:
				throw new Error(aName.name() + " is not a valid structures type");
		}

		SilentTask tmpTask = new SilentTask();
		retManager.installItems(tmpTask, tmpL);

		if (tmpL.size() == 0)
			throw new Exception("No valid " + aName.name() + " found");
		return retManager;
	}

}

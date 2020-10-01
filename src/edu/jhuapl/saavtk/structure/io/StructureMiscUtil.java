package edu.jhuapl.saavtk.structure.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import edu.jhuapl.saavtk.model.PolyModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.model.structure.Line;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.gui.load.InstallMode;
import edu.jhuapl.saavtk.structure.util.ControlPointUtil;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.vtk.font.FontAttr;
import glum.item.IdGenerator;
import glum.item.IncrIdGenerator;
import glum.task.Task;

/**
 * Collection of utility methods for working with Structures /
 * StructureManagers.
 * <P>
 * The functionality provided by this class can be categorized by:
 * <UL>
 * <LI>Determining the ID that should be used for a new structure.
 * <LI>Getting the specific structures from a list of general structures.
 * </UL>
 *
 * @author lopeznr1
 */
public class StructureMiscUtil
{
	/**
	 * Utility method that returns the next id that should be used for the specified
	 * StructureManager.
	 */
	public static <G1 extends Structure> int calcNextId(StructureManager<G1> aManager)
	{
		int maxId = 0;
		for (G1 aItem : aManager.getAllItems())
		{
			int tmpId = aItem.getId();
			if (tmpId > maxId)
				maxId = tmpId;
		}

		return maxId + 1;
	}

	/**
	 * Returns the list of round structures that match the specified mode.
	 */
	public static List<Ellipse> getEllipsesFrom(List<Structure> aItemL, Mode aMode)
	{
		List<Ellipse> retL = new ArrayList<>();

		for (Structure aItem : aItemL)
		{
			if (aItem instanceof Ellipse == false)
				continue;

			Ellipse tmpItem = (Ellipse) aItem;
			if (tmpItem.getMode() == aMode)
				retL.add(tmpItem);
		}

		return retL;
	}

	/**
	 * Returns the list of {@link Line} structures that are not {@link Polygon}s.
	 */
	public static List<PolyLine> getPathsFrom(List<Structure> aItemL)
	{
		List<PolyLine> retL = new ArrayList<>();

		for (Structure aItem : aItemL)
		{
			if (aItem instanceof PolyLine && aItem instanceof Polygon == false)
				retL.add((PolyLine) aItem);
		}

		return retL;
	}

	/**
	 * Returns the list of {@link Polygon} structures.
	 */
	public static List<Polygon> getPolygonsFrom(List<Structure> aItemL)
	{
		List<Polygon> retL = new ArrayList<>();

		for (Structure aItem : aItemL)
		{
			if (aItem instanceof Polygon)
				retL.add((Polygon) aItem);
		}

		return retL;
	}

	/**
	 * Helper method that will install the structures into the specified.
	 *
	 * @param aManager: The {@link StructureManager} of interest.
	 * @param aItemL    The list of items that should be installed.
	 * @param aMode     Describes how the items should be installed.
	 *
	 * @see InstallMode
	 */
	public static <G1 extends Structure> void installStructures(Task aTask, StructureManager<G1> aManager,
			List<G1> aItemL, InstallMode aMode)
	{
		// Bail if aTask is aborted
		if (aTask.isActive() == false)
			return;

		// Gather the list of current installed items
		List<G1> origItemL = new ArrayList<>();
		if (aMode != InstallMode.ReplaceAll)
			origItemL.addAll(aManager.getAllItems());

		// Form the appropriate ID generator
		IdGenerator tmpIG = null;
		if (aMode == InstallMode.AppendWithUniqueId)
			tmpIG = new IncrIdGenerator(StructureMiscUtil.calcNextId(aManager));

		// Perform the merge of the two lists
		Multimap<Integer, G1> mergeMM = ArrayListMultimap.create();

		for (G1 aItem : origItemL)
			mergeMM.put(aItem.getId(), aItem);

		for (G1 aItem : aItemL)
		{
			int tmpId = aItem.getId();
			if (aMode == InstallMode.AppendWithOriginalId)
				; // Nothing to do
			else if (aMode == InstallMode.AppendWithUniqueId)
				tmpId = tmpIG.getNextId();
			else if (aMode == InstallMode.ReplaceCollidingId)
				mergeMM.removeAll(tmpId);

			if (tmpId != aItem.getId())
				aItem = cloneItemWithId(aItem, tmpId);

			// Labels are always loaded with visibility turned off
			FontAttr tmpFA = aItem.getLabelFontAttr();
			if (tmpFA.getIsVisible() == true)
			{
				tmpFA = new FontAttr(tmpFA.getFace(), tmpFA.getColor(), tmpFA.getSize(), false);
				aItem.setLabelFontAttr(tmpFA);
			}

			mergeMM.put(tmpId, aItem);
		}

		// Install the items
		List<G1> fullItemL = new ArrayList<>(mergeMM.values());
		aManager.installItems(aTask, fullItemL);
	}

	/**
	 * Utility method that will project the control points associated with specified
	 * list of structures onto the provided shape model.
	 * <P>
	 * This method will only adjust control points for which the {@link Structure}'s
	 * shapeModelId does not match the passed in shapeModelId.
	 * <P>
	 * Throws a {@link RuntimeException} if the structure type is not recognized.
	 */
	public static void projectControlPointsToShapeModel(PolyModel aPolyModel, String aShapeModelId,
			List<? extends Structure> aItemL)
	{
		for (Structure aItem : aItemL)
		{
			String tmpShapeModelId = aItem.getShapeModelId();
			if (Objects.equals(aShapeModelId, tmpShapeModelId) == true)
				continue;

			// Update the structure's control points
			if (aItem instanceof PolyLine)
			{
				PolyLine tmpItem = (PolyLine) aItem;
				List<LatLon> tmpControlPointL = tmpItem.getControlPoints();
				tmpControlPointL = ControlPointUtil.shiftControlPointsToNearestPointOnBody(aPolyModel, tmpControlPointL);
				tmpItem.setControlPoints(tmpControlPointL);
			}
			else if (aItem instanceof Ellipse)
			{
				Ellipse tmpItem = (Ellipse) aItem;
				Vector3D tmpPos = tmpItem.getCenter();
				tmpPos = aPolyModel.findClosestPoint(tmpPos);
				tmpItem.setCenter(tmpPos);
			}
			else
			{
				throw new RuntimeException("Structure type is not recognized: " + aItem.getClass());
			}

			// Update the structure's associated shape model
			aItem.setShapeModelId(aShapeModelId);
		}
	}

	/**
	 * Utility method that will clone a structure but with an id that matches the
	 * specified id.
	 *
	 * This method supports the following structures:
	 * <UL>
	 * <LI>{@link Ellipse}.
	 * <LI>{@link Line}.
	 * <LI>{@link Polygon}.
	 * <UL>
	 *
	 * If a structure type is not recognized an
	 * {@link UnsupportedOperationException} will be thrown.
	 */
	@SuppressWarnings("unchecked")
	private static <G1 extends Structure> G1 cloneItemWithId(G1 aItem, int aId)
	{
		if (aItem.getClass() == PolyLine.class)
		{

			PolyLine tmpItem = (PolyLine) aItem;

			PolyLine retItem = new PolyLine(aId, tmpItem.getSource(), tmpItem.getControlPoints());
			retItem.setName(tmpItem.getName());
			retItem.setColor(tmpItem.getColor());
			retItem.setLabelFontAttr(tmpItem.getLabelFontAttr());
			retItem.setVisible(tmpItem.getVisible());
			return (G1) retItem;
		}
		else if (aItem.getClass() == Polygon.class)
		{

			Polygon tmpItem = (Polygon) aItem;

			Polygon retItem = new Polygon(aId, tmpItem.getSource(), tmpItem.getControlPoints());
			retItem.setName(tmpItem.getName());
			retItem.setColor(tmpItem.getColor());
			retItem.setLabelFontAttr(tmpItem.getLabelFontAttr());
			retItem.setVisible(tmpItem.getVisible());

			retItem.setSurfaceArea(tmpItem.getSurfaceArea());
			retItem.setShowInterior(tmpItem.getShowInterior());
			return (G1) retItem;
		}
		else if (aItem.getClass() == Ellipse.class)
		{
			Ellipse tmpItem = (Ellipse) aItem;

			Ellipse retItem = new Ellipse(aId, tmpItem.getSource(), tmpItem.getMode(), tmpItem.getCenter(),
					tmpItem.getRadius(), tmpItem.getAngle(), tmpItem.getFlattening(), tmpItem.getColor(),
					tmpItem.getLabel());

			retItem.setName(tmpItem.getName());
			retItem.setLabelFontAttr(tmpItem.getLabelFontAttr());
			retItem.setVisible(tmpItem.getVisible());
			return (G1) retItem;
		}

		throw new UnsupportedOperationException("Unrecognized structure type: " + aItem.getClass());
	}

}

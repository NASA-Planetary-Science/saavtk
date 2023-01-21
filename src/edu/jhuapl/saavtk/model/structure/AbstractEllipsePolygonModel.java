package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.google.common.primitives.Doubles;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.SettableMetadata;
import edu.jhuapl.saavtk.gui.render.SceneChangeNotifier;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.EllipseManager;
import edu.jhuapl.saavtk.structure.io.StructureLegacyUtil;
import edu.jhuapl.saavtk.structure.io.StructureMiscUtil;
import edu.jhuapl.saavtk.structure.util.EllipseUtil;
import edu.jhuapl.saavtk.util.Properties;
import glum.task.Task;

/**
 * Model of regular polygon structures drawn on a body.
 */
public class AbstractEllipsePolygonModel extends EllipseManager implements MetadataManager, PropertyChangeListener
{
	// Attributes
	private final PolyhedralModel refSmallBody;
	private final Mode mode;

	// State vars
	private Color defaultColor;
	private double defaultRadius;

	public enum Mode
	{
		POINT_MODE("Point"),

		CIRCLE_MODE("Circle"),

		ELLIPSE_MODE("Ellipse");

		private final String label;

		private Mode(String aLabel)
		{
			label = aLabel;
		}

		/**
		 * Returns a user friendly label of the mode.
		 */
		public String getLabel()
		{
			return label;
		}
	}

	/** Standard Constructor */
	public AbstractEllipsePolygonModel(SceneChangeNotifier aSceneChangeNotifier, StatusNotifier aStatusNotifier,
			PolyhedralModel aSmallBody, int aNumSides, Mode aMode)
	{
		super(aSceneChangeNotifier, aStatusNotifier, aSmallBody, aNumSides);

		refSmallBody = aSmallBody;
		mode = aMode;

		defaultColor = new Color(0, 191, 255);
		defaultRadius = EllipseUtil.getDefRadius(refSmallBody);

		refSmallBody.addPropertyChangeListener(this);
	}

	public Color getDefaultColor()
	{
		return defaultColor;
	}

	public void setDefaultColor(Color aColor)
	{
		defaultColor = aColor;
	}

	public double getInteriorOpacity()
	{
		return getVtkMultiPainter().getInteriorOpacity();
	}

	public void setInteriorOpacity(double opacity)
	{
		getVtkMultiPainter().setInteriorOpacity(opacity);
	}

	public Mode getMode()
	{
		return mode;
	}

	public int getNumberOfSides()
	{
		return getVtkMultiPainter().getNumberOfSides();
	}

	@Override
	public void installItems(Task aTask, List<Ellipse> aItemL)
	{
		// Ensure all items are fully initialized
		for (Ellipse aItem : aItemL)
		{
			if (aItem.getColor() == null)
				aItem.setColor(defaultColor);
			if (Double.isNaN(aItem.getRadius()) == true)
				aItem.setRadius(defaultRadius);
		}

		super.installItems(aTask, aItemL);
	}

	public void addNewStructure(Vector3D aCenter, double aRadius, double aFlattening, double aAngle)
	{
		int tmpId = StructureMiscUtil.calcNextId(this);
		Ellipse tmpItem = new Ellipse(tmpId, null, mode, aCenter, aRadius, aAngle, aFlattening, defaultColor, "");

		addItem(tmpItem);
	}

	public void addNewStructure(Vector3D aCenter)
	{
		addNewStructure(aCenter, defaultRadius, 1.0, 0.);
	}

	/**
	 * Removes all of the items from this manager.
	 */
	public void removeAllStructures()
	{
		// Delegate
		removeItems(getAllItems());
	}

	public void redrawAllStructures()
	{
		for (Ellipse aItem : getAllItems())
			markPainterStale(aItem);

		getVtkMultiPainter().updatePolyData();
	}

	public double getDefaultRadius()
	{
		return defaultRadius;
	}

	public void setDefaultRadius(double radius)
	{
		defaultRadius = radius;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
		{
			redrawAllStructures();
		}
	}

	private static final Key<List<Ellipse>> ELLIPSE_POLYGON_KEY = Key.of("ellipses");
	private static final Key<Double> DEFAULT_RADIUS_KEY = Key.of("defaultRadius");
	private static final Key<int[]> DEFAULT_COLOR_KEY = Key.of("defaultColor");
	private static final Key<Double> INTERIOR_OPACITY_KEY = Key.of("interiorOpacity");
	private static final Key<int[]> SELECTED_STRUCTURES_KEY = Key.of("selectedStructures");
	private static final Key<Double> LINE_WIDTH_KEY = Key.of("lineWidth");
	private static final Key<Double> OFFSET_KEY = Key.of("offset");

	@Override
	public Metadata store()
	{
		var vMultiPainter = getVtkMultiPainter();
		double interiorOpacity = vMultiPainter.getInteriorOpacity();
		double lineWidth = vMultiPainter.getLineWidth();

		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));

		result.put(ELLIPSE_POLYGON_KEY, getAllItems());
		result.put(DEFAULT_RADIUS_KEY, defaultRadius);
		result.put(DEFAULT_COLOR_KEY, StructureLegacyUtil.convertColorToRgba(defaultColor));
		result.put(INTERIOR_OPACITY_KEY, interiorOpacity);

		List<Ellipse> fullL = getAllItems();
		Set<Ellipse> pickS = getSelectedItems();
		int[] idArr = new int[pickS.size()];// List<Integer> idL = new ArrayList<>();
		int cntA = 0;
		for (Ellipse aItem : pickS)
		{
			int tmpIdx = fullL.indexOf(aItem);
			idArr[cntA] = tmpIdx;
			cntA++;
		}
		result.put(SELECTED_STRUCTURES_KEY, idArr);

		result.put(LINE_WIDTH_KEY, lineWidth);
		result.put(OFFSET_KEY, getOffset());

		return result;
	}

	@Override
	public void retrieve(Metadata source)
	{
		// The order of these operations is significant to try to keep the object state
		// consistent. First get everything from the metadata into local variables.
		// Don't touch the model yet in case there's a problem.
		double defaultRadius = source.get(DEFAULT_RADIUS_KEY);
		int[] defaultColor = source.get(DEFAULT_COLOR_KEY);
		double interiorOpacity = source.get(INTERIOR_OPACITY_KEY);
		int[] selectedStructures = source.get(SELECTED_STRUCTURES_KEY);
		double lineWidth = source.get(LINE_WIDTH_KEY);
		double offset = source.get(OFFSET_KEY);
		List<Ellipse> restoredPolygons = source.get(ELLIPSE_POLYGON_KEY);

		// Ensure the "default" radius is properly clamped
		double minRadius = EllipseUtil.getMinRadius(refSmallBody);
		double maxRadius = EllipseUtil.getMaxRadius(refSmallBody);
		defaultRadius = Doubles.constrainToRange(defaultRadius, minRadius, maxRadius);

		// Now we're committed. Get rid of whatever's currently in this model and then
		// add the restored polygons.
		// Finally, change the rest of the fields.
		this.defaultRadius = defaultRadius;
		this.defaultColor = StructureLegacyUtil.convertRgbaToColor(defaultColor);

		List<Ellipse> pickL = new ArrayList<>();
		for (int aIdx : selectedStructures)
			pickL.add(restoredPolygons.get(aIdx));

		// Put the restored polygons in the list.
		setAllItems(restoredPolygons);
		setSelectedItems(pickL);

		var vMultiPainter = getVtkMultiPainter();
		vMultiPainter.setInteriorOpacity(interiorOpacity);
		vMultiPainter.setOffset(offset);
		setLineWidth(lineWidth);
	}

}

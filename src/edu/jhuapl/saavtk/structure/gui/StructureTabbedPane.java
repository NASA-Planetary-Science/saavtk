package edu.jhuapl.saavtk.structure.gui;

import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.pick.CirclePicker;
import edu.jhuapl.saavtk.pick.ControlPointsPicker;
import edu.jhuapl.saavtk.pick.EllipsePicker;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.pick.PointPicker;
import edu.jhuapl.saavtk.structure.BaseStructureManager;
import edu.jhuapl.saavtk.structure.StructureManager;

/**
 * UI component used to display a list of 5 child {@link StructurePanel}s. These
 * 5 (line, polygon, circle, ellipse, points) panels allow the user to
 * manipulate the structures associated with the panel.
 *
 * @author lopeznr1
 */
public class StructureTabbedPane extends JTabbedPane
{
	// Constants
	private static final long serialVersionUID = 1L;

	// Ref vars
	private final PickManager refPickManager;
	private final Renderer refRenderer;
	private final ModelManager refModelManager;

	/**
	 * Standard Constructor
	 */
	public StructureTabbedPane(PickManager aPickManager, Renderer aRenderer, ModelManager aModelManager)
	{
		refPickManager = aPickManager;
		refRenderer = aRenderer;
		refModelManager = aModelManager;

		StructurePanel<?> linePanel = formStructurePanel(ModelNames.LINE_STRUCTURES);
		StructurePanel<?> polygonPanel = formStructurePanel(ModelNames.POLYGON_STRUCTURES);
		StructurePanel<?> circlePanel = formStructurePanel(ModelNames.CIRCLE_STRUCTURES);
		StructurePanel<?> ellipsePanel = formStructurePanel(ModelNames.ELLIPSE_STRUCTURES);
		StructurePanel<?> pointsPanel = formStructurePanel(ModelNames.POINT_STRUCTURES);

		addTab("Paths", linePanel);
		addTab("Polygons", polygonPanel);
		addTab("Circles", circlePanel);
		addTab("Ellipses", ellipsePanel);
		addTab("Points", pointsPanel);
	}

	/**
	 * Helper method to form the proper {@link StructurePanel} for the specified
	 * arguments.
	 *
	 * @param aModelNames
	 */
	private StructurePanel<?> formStructurePanel(ModelNames aModelNames)
	{
		StructureManager<?> tmpStructureManager = (StructureManager<?>) refModelManager.getModel(aModelNames);

		// Manually register for events of interest
		refPickManager.getDefaultPicker().addListener((BaseStructureManager<?, ?>) tmpStructureManager);

		// Instantiate the appropriate Picker
		PolyhedralModel tmpSmallBody = refModelManager.getPolyhedralModel();

		Picker tmpPicker = null;
		if (aModelNames == ModelNames.LINE_STRUCTURES || aModelNames == ModelNames.POLYGON_STRUCTURES)
			tmpPicker = new ControlPointsPicker<>(refRenderer, refPickManager, tmpSmallBody,
					(LineModel<?>) tmpStructureManager);
		else if (aModelNames == ModelNames.CIRCLE_STRUCTURES)
			tmpPicker = new CirclePicker(refRenderer, tmpSmallBody, tmpStructureManager);
		else if (aModelNames == ModelNames.ELLIPSE_STRUCTURES)
			tmpPicker = new EllipsePicker(refRenderer, tmpSmallBody, tmpStructureManager);
		else if (aModelNames == ModelNames.POINT_STRUCTURES)
			tmpPicker = new PointPicker(refRenderer, tmpSmallBody, tmpStructureManager);
		else
			throw new RuntimeException("Unrecognized ModelName: " + aModelNames);

		StructurePanel<?> retPanel = new StructurePanel<>(tmpStructureManager, refPickManager, tmpPicker, refRenderer,
				tmpSmallBody, refModelManager);
		return retPanel;
	}

}

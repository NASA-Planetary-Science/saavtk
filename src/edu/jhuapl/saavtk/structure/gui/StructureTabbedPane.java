package edu.jhuapl.saavtk.structure.gui;

import javax.swing.JTabbedPane;

import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.pick.CirclePicker;
import edu.jhuapl.saavtk.pick.ControlPointsPicker;
import edu.jhuapl.saavtk.pick.EllipsePicker;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.pick.PointPicker;
import edu.jhuapl.saavtk.popup.PopupManager;
import edu.jhuapl.saavtk.popup.PopupMenu;
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
	private final ModelManager refModelManager;
	private final PickManager refPickManager;
	private final Renderer refRenderer;
	private final PopupManager refPopupManager;
	private final StatusBar statusBar;

	/**
	 * Standard Constructor
	 */
	public StructureTabbedPane(ModelManager aModelManager, PickManager aPickManager, Renderer aRenderer,
			StatusBar aStatusBar, PopupManager aPopupManager)
	{
		refModelManager = aModelManager;
		refPickManager = aPickManager;
		refRenderer = aRenderer;
		refPopupManager = aPopupManager;
		statusBar = aStatusBar;

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
		PopupMenu tmpPopupMenu = refPopupManager.getPopup((Model) tmpStructureManager);

		// Instantiate the appropriate Picker
		Picker tmpPicker = null;
		if (aModelNames == ModelNames.LINE_STRUCTURES || aModelNames == ModelNames.POLYGON_STRUCTURES)
			tmpPicker = new ControlPointsPicker<>(refRenderer, refPickManager, refModelManager,
					(LineModel<?>) tmpStructureManager);
		else if (aModelNames == ModelNames.CIRCLE_STRUCTURES)
			tmpPicker = new CirclePicker(refRenderer, refModelManager);
		else if (aModelNames == ModelNames.ELLIPSE_STRUCTURES)
			tmpPicker = new EllipsePicker(refRenderer, refModelManager);
		else if (aModelNames == ModelNames.POINT_STRUCTURES)
			tmpPicker = new PointPicker(refRenderer, refModelManager);
		else
			throw new RuntimeException("Unrecognized ModelName: " + aModelNames);

		StructurePanel<?> retPanel = new StructurePanel<>(refModelManager, tmpStructureManager, refPickManager, tmpPicker,
				statusBar, tmpPopupMenu);
		return retPanel;
	}

}

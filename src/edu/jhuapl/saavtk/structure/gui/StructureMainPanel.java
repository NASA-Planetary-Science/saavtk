package edu.jhuapl.saavtk.structure.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.funk.PopupButton;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.gui.action.LoadEsriShapeFileAction;
import edu.jhuapl.saavtk.structure.gui.load.AmbiguousRoundStructurePanel;
import edu.jhuapl.saavtk.structure.gui.load.LoadPanel;
import edu.jhuapl.saavtk.structure.io.StructureLoadUtil;
import glum.gui.GuiUtil;
import net.miginfocom.swing.MigLayout;

public class StructureMainPanel extends JPanel implements ActionListener
{
	// Constants
	private static final long serialVersionUID = 0L;

	// Ref vars
	private final ModelManager refModelManager;

	// GUI vars
	private JDialog loadDialog;
	private LoadPanel loadPanel;
	private JLabel structuresFileL;
	private JButton loadB;
	private JButton saveB;

	/**
	 * Standard Constructor
	 */
	public StructureMainPanel(PickManager aPickManager, Renderer aRenderer, StatusBar aStatusBar,
			ModelManager aModelManager)
	{
		refModelManager = aModelManager;

		setLayout(new MigLayout("", "", "[]"));

		PopupButton loadEsriB = formEsriLoadButton(aStatusBar);

		loadB = GuiUtil.createJButton("Load", this);
		saveB = GuiUtil.createJButton("Save", this);
		JLabel fileNameL = new JLabel("File: ");
		structuresFileL = new JLabel("<no file loaded>");
		add(fileNameL, "span,split");
		add(structuresFileL, "growx,pushx,w 100:100:");
		add(loadB, "sg g0");
//		add(saveB, "sg g0");
		add(loadEsriB, "wrap");

		StructureTabbedPane structureTP = new StructureTabbedPane(aPickManager, aRenderer, aModelManager);
		add(structureTP, "growx,growy,pushy,span");
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();

		if (source == loadB)
			doLoadAction();
		else if (source == saveB)
			doSaveAction();
	}

	/**
	 * Helper method that handles the load action.
	 */
	private void doLoadAction()
	{
		// Prompt the user for files to load
		File[] fileArr = CustomFileChooser.showOpenDialog(this, "Select Structure Files", null, true);
		if (fileArr == null)
			return;
		List<File> tmpFileL = Arrays.asList(fileArr);
		tmpFileL.sort(null);

		// Load the files
		Map<File, Exception> failM = new LinkedHashMap<>();

		List<Structure> fullL = new ArrayList<>();
		for (File aFile : tmpFileL)
		{
			try
			{
				List<Structure> tmpL;
				tmpL = StructureLoadUtil.loadStructures(aFile);
				fullL.addAll(tmpL);
			}
			catch (IOException aExp)
			{
				aExp.printStackTrace();
				failM.put(aFile, aExp);
			}
		}

		// Check for ambiguous structures and transform no non-ambiguous structures
		fullL = handleAmbiguousStructuresOrAbort(fullL);
		if (fullL == null)
			return;

		// Lazy init
		if (loadDialog == null)
		{
			loadDialog = new JDialog();
			loadDialog.setTitle("Load Structures");
			loadDialog.setModal(true);
			loadPanel = new LoadPanel(loadDialog, refModelManager);
			loadDialog.pack();
			loadDialog.setLocationRelativeTo(this);
		}

		// Prompt the user for how to load the structures
		loadPanel.setStructuresToLoad(fullL);
		loadDialog.setVisible(true);

		updateFileLabelUI();
	}

	/**
	 * Helper method that handles the save action.
	 */
	private void doSaveAction()
	{
		// TODO Auto-generated method stub
	}

	/**
	 * Helper method to form the PopupButton used to load ESRI data structures.
	 */
	private PopupButton formEsriLoadButton(StatusBar aStatusBar)
	{
		PopupButton retB = new PopupButton("ESRI...");

		StructureManager<?> pathStructureManager = (StructureManager<?>) refModelManager
				.getModel(ModelNames.LINE_STRUCTURES);
		retB.getPopup().add(new JMenuItem(new LoadEsriShapeFileAction<>(this, "Load Path Shapefile Datastore",
				pathStructureManager, refModelManager, aStatusBar)));

		StructureManager<?> polygonStructureManager = (StructureManager<?>) refModelManager
				.getModel(ModelNames.POLYGON_STRUCTURES);
		retB.getPopup().add(new JMenuItem(new LoadEsriShapeFileAction<>(this, "Load Polygon Shapefile Datastore",
				polygonStructureManager, refModelManager, aStatusBar)));

		JMenuItem circleMI = new JMenuItem("Load Circle Shapefile Datastore");
		circleMI.setToolTipText("ESRI circles can be imported using the Polygons tab");
		circleMI.setEnabled(false);
		retB.getPopup().add(circleMI);

		JMenuItem ellipseMI = new JMenuItem("Load Ellipse Shapefile Datastore");
		ellipseMI.setToolTipText("ESRI ellipses can be imported using the Polygons tab");
		ellipseMI.setEnabled(false);
		retB.getPopup().add(ellipseMI);

		StructureManager<?> pointStructureManager = (StructureManager<?>) refModelManager
				.getModel(ModelNames.POINT_STRUCTURES);
		retB.getPopup().add(new JMenuItem(new LoadEsriShapeFileAction<>(this, "Load Point Shapefile Datastore",
				pointStructureManager, refModelManager, aStatusBar)));

		return retB;
	}

	/**
	 * Returns the list of all structures associated with the StrutureManager(s).
	 */
	private List<Structure> getAllStructures()
	{
		List<Structure> retL = new ArrayList<>();

		ModelNames keyArr[] = { ModelNames.LINE_STRUCTURES, ModelNames.POLYGON_STRUCTURES, ModelNames.CIRCLE_STRUCTURES,
				ModelNames.ELLIPSE_STRUCTURES, ModelNames.POINT_STRUCTURES };
		for (ModelNames aKey : keyArr)
		{
			StructureManager<?> tmpStructureManager = (StructureManager<?>) refModelManager.getModel(aKey);
			retL.addAll(tmpStructureManager.getAllItems());
		}

		return retL;
	}

	/**
	 * Helper method that will determine if there are ambiguous and if so prompt the
	 * user for how to handle them. If abort is canceled then null will be returned
	 * and the load process should be aborted.
	 */
	private List<Structure> handleAmbiguousStructuresOrAbort(List<Structure> aItemL)
	{
		// Determine the ambiguous structures (source file to structure)
		Multimap<Object, Structure> ambigouseMM = LinkedListMultimap.create();
		for (Structure aStructure : aItemL)
		{
			if (aStructure instanceof Ellipse && ((Ellipse) aStructure).getMode() == null)
				ambigouseMM.put(aStructure.getSource(), aStructure);
		}

		// Bail since there is no ambiguous structures
		if (ambigouseMM.size() == 0)
			return aItemL;

		// Prompt the user for how to handle the ambiguous structures.
		AmbiguousRoundStructurePanel tmpPrompPanel = new AmbiguousRoundStructurePanel(this, 550, 225);
		tmpPrompPanel.setAmbigousMap(ambigouseMM);
		tmpPrompPanel.setVisibleAsModal();

		// Bail if no mode selected
		Mode pickMode = tmpPrompPanel.getSelection();
		if (pickMode == null)
			return null;

		// Transform the ambiguous Structures with the user's selection
		List<Structure> retItemL = new ArrayList<>();
		for (Structure aItem : aItemL)
		{
			// Skip to next if not an ellipse
			if (aItem instanceof Ellipse == false)
			{
				retItemL.add(aItem);
				continue;
			}

			// Skip to next if not an (ambiguous) ellipse
			Ellipse tmpItem = (Ellipse) aItem;
			if (tmpItem.getMode() != null)
			{
				retItemL.add(tmpItem);
				continue;
			}

			tmpItem = new Ellipse(tmpItem.getId(), tmpItem.getSource(), pickMode, tmpItem.getCenter(), tmpItem.getRadius(),
					tmpItem.getAngle(), tmpItem.getFlattening(), tmpItem.getColor(), tmpItem.getLabel());
			retItemL.add(tmpItem);
		}

		return retItemL;
	}

	/**
	 * Helper method responsible for updating the fileL UI element
	 */
	private void updateFileLabelUI()
	{
		// Retrieve the list of all source files
		Set<File> tmpFileS = new HashSet<>();
		for (Structure aStructure : getAllStructures())
		{
			Object source = aStructure.getSource();
			if (source instanceof File)
				tmpFileS.add((File) source);
		}

		// Update the UI
		String priStr = "<no file loaded>";
		String secStr = null;
		if (tmpFileS.size() == 1)
		{
			File tmpFile = tmpFileS.iterator().next();
			priStr = tmpFile.getName();
			secStr = tmpFile.getAbsolutePath();
		}
		else if (tmpFileS.size() > 1)
		{
			priStr = "Multiple Files: " + tmpFileS.size();
		}
		structuresFileL.setText(priStr);
		structuresFileL.setToolTipText(secStr);
	}

}

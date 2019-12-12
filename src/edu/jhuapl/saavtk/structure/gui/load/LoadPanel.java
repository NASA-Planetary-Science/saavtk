package edu.jhuapl.saavtk.structure.gui.load;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.io.StructureMiscUtil;
import glum.gui.GuiUtil;
import glum.task.SilentTask;
import glum.task.Task;
import net.miginfocom.swing.MigLayout;

/**
 * Load panel that provides a custom UI for loading up structues.
 * <P>
 * This panel provides the following features:
 * <UL>
 * <LI>Ability to specify the structure types to be loaded
 * <LI>Responsive status feedback associated with user input
 * <LI>Support for defining how structures are loaded and merged into the
 * system.
 * </UL>
 *
 * @author lopeznr1
 */
public class LoadPanel extends JPanel implements ActionListener
{
	// Constants
	private static final String ERR_MSG_NO_STRUCTURES_LOADED = "Failed to load any structures.";
	private static final String ERR_MSG_NO_STRUCTURES_SELECTED = "Please select at least one of the available structure types.";
	private static final long serialVersionUID = 0L;
	// TODO: Move constant elsewhere
	private static final Color failColor = Color.RED.darker();

	// Ref vars
	private final JDialog refDialog;

	private final StructureManager<PolyLine> refPathStructureManager;
	private final StructureManager<Polygon> refPolyStructureManager;
	private final StructureManager<Ellipse> refCircleStructureManager;
	private final StructureManager<Ellipse> refEllipseStructureManager;
	private final StructureManager<Ellipse> refPointStructureManager;

	// State vars
	private ImmutableList<Structure> fullL;

	// GUI vars
	private JRadioButton replaceAllRB;
	private JRadioButton replaceCollideRB;
	private JRadioButton appendWithOrignalRB;
	private JRadioButton appendWithUniqueRB;

	private JCheckBox circleCountCB;
	private JCheckBox ellipseCountCB;
	private JCheckBox polygonCountCB;
	private JCheckBox pathCountCB;
	private JCheckBox pointCountCB;

	private JLabel statusL;

	private JButton cancelB;
	private JButton acceptB;

	/**
	 * Standard Constructor
	 */
	@SuppressWarnings("unchecked")
	public LoadPanel(JDialog aDialog, ModelManager aModelManager)
	{
		refDialog = aDialog;
		refDialog.setContentPane(this);

		refPathStructureManager = (StructureManager<PolyLine>) aModelManager.getModel(ModelNames.LINE_STRUCTURES);
		refPolyStructureManager = (StructureManager<Polygon>) aModelManager.getModel(ModelNames.POLYGON_STRUCTURES);
		refCircleStructureManager = (StructureManager<Ellipse>) aModelManager.getModel(ModelNames.CIRCLE_STRUCTURES);
		refEllipseStructureManager = (StructureManager<Ellipse>) aModelManager.getModel(ModelNames.ELLIPSE_STRUCTURES);
		refPointStructureManager = (StructureManager<Ellipse>) aModelManager.getModel(ModelNames.POINT_STRUCTURES);

		fullL = ImmutableList.of();

		setLayout(new MigLayout("", "", "[]"));

		replaceAllRB = GuiUtil.createJRadioButton("Replace all structures", this);
		replaceAllRB.setSelected(true);
		replaceCollideRB = GuiUtil.createJRadioButton("Replace structures with colliding ids", this);
		appendWithOrignalRB = GuiUtil.createJRadioButton("Append structures with original ids", this);
		appendWithUniqueRB = GuiUtil.createJRadioButton("Append structures with unique ids", this);

		pathCountCB = GuiUtil.createJCheckBox("Paths: ", this);
		polygonCountCB = GuiUtil.createJCheckBox("Polygons: ", this);
		circleCountCB = GuiUtil.createJCheckBox("Circles: ", this);
		ellipseCountCB = GuiUtil.createJCheckBox("Ellipses: ", this);
		pointCountCB = GuiUtil.createJCheckBox("Points: ", this);

		JPanel westPanel = formWestPanel();
		JPanel eastPanel = formEastPanel();
		add(westPanel, "growy");
		add(GuiUtil.createDivider(), "growy,pushy,w 4!");
		add(eastPanel, "growy,pushx,wrap");

		statusL = new JLabel(ERR_MSG_NO_STRUCTURES_SELECTED);
		add(statusL, "growx,span,wrap");

		cancelB = GuiUtil.formButton(this, "Cancel");
		acceptB = GuiUtil.formButton(this, "Accept");
		add(cancelB, "align right,span,split");
		add(acceptB, "gap right 3");

		refDialog.pack();
	}

	/**
	 * Sets in the structures to be loaded.
	 */
	public void setStructuresToLoad(List<Structure> aItemL)
	{
		boolean isEnabled;

		fullL = ImmutableList.copyOf(aItemL);

		int cntPath = StructureMiscUtil.getPathsFrom(fullL).size();
		isEnabled = cntPath > 0;
		pathCountCB.setEnabled(isEnabled);
		pathCountCB.setSelected(isEnabled);
		pathCountCB.setText("Paths: " + cntPath);

		int cntPolygon = StructureMiscUtil.getPolygonsFrom(fullL).size();
		isEnabled = cntPolygon > 0;
		polygonCountCB.setEnabled(isEnabled);
		polygonCountCB.setSelected(isEnabled);
		polygonCountCB.setText("Polygons: " + cntPolygon);

		int cntCircle = StructureMiscUtil.getEllipsesFrom(fullL, Mode.CIRCLE_MODE).size();
		isEnabled = cntCircle > 0;
		circleCountCB.setEnabled(isEnabled);
		circleCountCB.setSelected(isEnabled);
		circleCountCB.setText("Circles: " + cntCircle);

		int cntEllipse = StructureMiscUtil.getEllipsesFrom(fullL, Mode.ELLIPSE_MODE).size();
		isEnabled = cntEllipse > 0;
		ellipseCountCB.setEnabled(isEnabled);
		ellipseCountCB.setSelected(isEnabled);
		ellipseCountCB.setText("Ellipses: " + cntEllipse);

		int cntPoint = StructureMiscUtil.getEllipsesFrom(fullL, Mode.POINT_MODE).size();
		isEnabled = cntPoint > 0;
		pointCountCB.setEnabled(isEnabled);
		pointCountCB.setSelected(isEnabled);
		pointCountCB.setText("Points: " + cntPoint);

		updateGui();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == acceptB)
			doActionAccept();
		if (source == cancelB)
			refDialog.setVisible(false);

		updateGui();
	}

	/**
	 * Helper method to handle the accept action.
	 */
	private void doActionAccept()
	{

		// Retrieve the user configured mode and selected structures
		InstallMode tmpMode = getLoadMode();

		List<Structure> loadL = getLoadItems();

		// Install the various structures
		Thread tmpThread = new Thread(() -> installStructures(loadL, tmpMode));
		tmpThread.start();
	}

	/**
	 * Helper method to form the load details panel.
	 */
	private JPanel formEastPanel()
	{
		JPanel retPanel = new JPanel(new MigLayout("", "", "[]"));
		retPanel.add(pathCountCB, "wrap");
		retPanel.add(polygonCountCB, "wrap");
		retPanel.add(circleCountCB, "wrap");
		retPanel.add(ellipseCountCB, "wrap");
		retPanel.add(pointCountCB, "wrap");

		return retPanel;
	}

	/**
	 * Helper method to form the load-mode panel.
	 */
	private JPanel formWestPanel()
	{
		GuiUtil.linkRadioButtons(replaceAllRB, replaceCollideRB, appendWithOrignalRB, appendWithUniqueRB);

		JPanel retPanel = new JPanel(new MigLayout("", "", "[]"));
		retPanel.add(replaceAllRB, "wrap");
		retPanel.add(replaceCollideRB, "wrap");
		retPanel.add(appendWithOrignalRB, "wrap");
		retPanel.add(appendWithUniqueRB, "wrap");

		return retPanel;
	}

	/**
	 * Helper method that returns the user specified structures to load.
	 */
	private List<Structure> getLoadItems()
	{
		List<Structure> retL = new ArrayList<>();

		if (pathCountCB.isSelected() == true)
			retL.addAll(StructureMiscUtil.getPathsFrom(fullL));
		if (polygonCountCB.isSelected() == true)
			retL.addAll(StructureMiscUtil.getPolygonsFrom(fullL));
		if (circleCountCB.isSelected() == true)
			retL.addAll(StructureMiscUtil.getEllipsesFrom(fullL, Mode.CIRCLE_MODE));
		if (ellipseCountCB.isSelected() == true)
			retL.addAll(StructureMiscUtil.getEllipsesFrom(fullL, Mode.ELLIPSE_MODE));
		if (pointCountCB.isSelected() == true)
			retL.addAll(StructureMiscUtil.getEllipsesFrom(fullL, Mode.POINT_MODE));

		return retL;
	}

	/**
	 * Helper method that returns the user specified LoadMode.
	 */
	private InstallMode getLoadMode()
	{
		InstallMode retMode = InstallMode.AppendWithUniqueId;
		if (replaceAllRB.isSelected() == true)
			retMode = InstallMode.ReplaceAll;
		else if (replaceCollideRB.isSelected() == true)
			retMode = InstallMode.ReplaceCollidingId;
		else if (appendWithOrignalRB.isSelected() == true)
			retMode = InstallMode.AppendWithOriginalId;

		return retMode;
	}

	/**
	 * Helper method that take the provided structures (of various types) and
	 * install the structures into the appropriate managers.
	 */
	private void installStructures(List<Structure> aFullL, InstallMode aMode)
	{
		// Split structures into various type
		// TODO: Eventually this step should not be needed
		List<PolyLine> tmpPathL = StructureMiscUtil.getPathsFrom(aFullL);
		List<Polygon> tmpPolyL = StructureMiscUtil.getPolygonsFrom(aFullL);
		List<Ellipse> tmpCircleL = StructureMiscUtil.getEllipsesFrom(aFullL, Mode.CIRCLE_MODE);
		List<Ellipse> tmpEllipseL = StructureMiscUtil.getEllipsesFrom(aFullL, Mode.ELLIPSE_MODE);
		List<Ellipse> tmpPointL = StructureMiscUtil.getEllipsesFrom(aFullL, Mode.POINT_MODE);

		Task tmpTask = new SilentTask();
		StructureMiscUtil.installStructures(tmpTask, refPathStructureManager, tmpPathL, aMode);
		StructureMiscUtil.installStructures(tmpTask, refPolyStructureManager, tmpPolyL, aMode);
		StructureMiscUtil.installStructures(tmpTask, refCircleStructureManager, tmpCircleL, aMode);
		StructureMiscUtil.installStructures(tmpTask, refEllipseStructureManager, tmpEllipseL, aMode);
		StructureMiscUtil.installStructures(tmpTask, refPointStructureManager, tmpPointL, aMode);

		refDialog.setVisible(false);
	}

	/**
	 * Helper method that keeps the GUI synchronized with user input.
	 */
	private void updateGui()
	{
		// Determine if there are errors
		String errMsg = null;
		if (fullL.size() == 0)
			errMsg = ERR_MSG_NO_STRUCTURES_LOADED;

		int numItemsToAdd = getLoadItems().size();
		if (errMsg == null && numItemsToAdd == 0)
			errMsg = ERR_MSG_NO_STRUCTURES_SELECTED;

		// Update the status label
		String regMsg = "Structures to be loaded: " + numItemsToAdd;
		String tmpMsg = regMsg;
		if (errMsg != null)
			tmpMsg = errMsg;
		statusL.setText(tmpMsg);

		Color fgColor = Color.BLACK;
		if (errMsg != null)
			fgColor = failColor;
		statusL.setForeground(fgColor);

		boolean isEnabled = errMsg == null;
		acceptB.setEnabled(isEnabled);
	}

}

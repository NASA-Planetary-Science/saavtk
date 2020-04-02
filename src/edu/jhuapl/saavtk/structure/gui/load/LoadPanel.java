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
import javax.swing.SwingUtilities;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.gui.util.Colors;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.io.StructureMiscUtil;
import glum.gui.GuiUtil;
import glum.task.NotifyTask;
import glum.task.PartialTask;
import glum.task.Task;
import glum.task.TaskListener;
import glum.unit.NumberUnit;
import glum.unit.TimeCountUnit;
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
public class LoadPanel extends JPanel implements ActionListener, TaskListener
{
	// Constants
	private static final long serialVersionUID = 0L;
	private static final String ERR_MSG_NO_STRUCTURES_LOADED = "Failed to load any structures.";
	private static final String ERR_MSG_NO_STRUCTURES_SELECTED = "Please select at least one of the available structure types.";

	// Ref vars
	private final JDialog refDialog;
	private final PolyhedralModel refSmallBody;

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
	private JCheckBox projToBodyCB;

	private JCheckBox circleCountCB;
	private JCheckBox ellipseCountCB;
	private JCheckBox polygonCountCB;
	private JCheckBox pathCountCB;
	private JCheckBox pointCountCB;

	private NotifyTask loadTask;
	private JLabel progL, statusL;

	private JButton acceptB;
	private JButton cancelB;
	private JButton closeB;

	/**
	 * Standard Constructor
	 */
	@SuppressWarnings("unchecked")
	public LoadPanel(JDialog aDialog, ModelManager aModelManager)
	{
		refDialog = aDialog;
		refDialog.setContentPane(this);
		refDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		refSmallBody = aModelManager.getPolyhedralModel();

		refPathStructureManager = (StructureManager<PolyLine>) aModelManager.getModel(ModelNames.LINE_STRUCTURES);
		refPolyStructureManager = (StructureManager<Polygon>) aModelManager.getModel(ModelNames.POLYGON_STRUCTURES);
		refCircleStructureManager = (StructureManager<Ellipse>) aModelManager.getModel(ModelNames.CIRCLE_STRUCTURES);
		refEllipseStructureManager = (StructureManager<Ellipse>) aModelManager.getModel(ModelNames.ELLIPSE_STRUCTURES);
		refPointStructureManager = (StructureManager<Ellipse>) aModelManager.getModel(ModelNames.POINT_STRUCTURES);

		fullL = ImmutableList.of();

		setLayout(new MigLayout("", "", "[]"));

		appendWithOrignalRB = GuiUtil.createJRadioButton("Append structures with original ids", this);
		appendWithOrignalRB.setSelected(true);
		appendWithUniqueRB = GuiUtil.createJRadioButton("Append structures with unique ids", this);
		replaceAllRB = GuiUtil.createJRadioButton("Replace all structures", this);
		replaceCollideRB = GuiUtil.createJRadioButton("Replace structures with colliding ids", this);
		projToBodyCB = GuiUtil.createJCheckBox("Project to body", this);
		projToBodyCB.setToolTipText("Project structures to the surface of the shape model.");

		pathCountCB = GuiUtil.createJCheckBox("Paths: ", this);
		polygonCountCB = GuiUtil.createJCheckBox("Polygons: ", this);
		circleCountCB = GuiUtil.createJCheckBox("Circles: ", this);
		ellipseCountCB = GuiUtil.createJCheckBox("Ellipses: ", this);
		pointCountCB = GuiUtil.createJCheckBox("Points: ", this);

		JPanel westPanel = formPanelWest();
		JPanel eastPanel = formPanelEast();
		add(westPanel, "growy");
		add(GuiUtil.createDivider(), "growy,pushy,w 4!");
		add(eastPanel, "growy,pushx,wrap");

		statusL = new JLabel(ERR_MSG_NO_STRUCTURES_SELECTED);
		add(statusL, "growx,span,wrap");

		loadTask = new NotifyTask(this);
		acceptB = GuiUtil.formButton(this, "Accept");
		cancelB = GuiUtil.formButton(this, "Cancel");
		closeB = GuiUtil.formButton(this, "Close");
		progL = new JLabel();
		add(progL, "");
		add(cancelB, "align right,span,split");
		add(acceptB, "");
		add(closeB, "gap right 3");

		refDialog.pack();
	}

	/**
	 * Sets in the structures to be loaded.
	 */
	public void setStructuresToLoad(List<Structure> aItemL)
	{
		boolean isEnabled;

		fullL = ImmutableList.copyOf(aItemL);
		loadTask.reset();

		// Update GUI state of various tasks
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

		GuiUtil.setEnabled(true, appendWithOrignalRB, appendWithUniqueRB, replaceAllRB, replaceCollideRB);
		GuiUtil.setEnabled(true, projToBodyCB);

		updateGui();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();
		if (source == acceptB)
			doActionAccept();
		if (source == cancelB)
			doActionCancel();
		if (source == closeB)
			doActionClose();

		updateGui();
	}

	@Override
	public void taskUpdate(Task aTask)
	{
		String tmpMsg = formProgressMsg(loadTask);
		SwingUtilities.invokeLater(() -> progL.setText(tmpMsg));
	}

	/**
	 * Helper method to handle the accept action.
	 */
	private void doActionAccept()
	{
		// Progress the task out of the init state
		loadTask.markInitDone();

		// Update UI to reflect the active load
		acceptB.setEnabled(false);
		GuiUtil.setEnabled(false, appendWithOrignalRB, appendWithUniqueRB, replaceAllRB, replaceCollideRB);
		GuiUtil.setEnabled(false, circleCountCB, ellipseCountCB, pointCountCB, pathCountCB, polygonCountCB);
		GuiUtil.setEnabled(false, projToBodyCB);

		// Retrieve the user configured mode and selected structures
		InstallMode tmpMode = getLoadMode();
		List<Structure> loadL = getLoadItems();

		// Install the various structures
		Thread tmpThread = new Thread(() -> installStructures(loadL, loadTask, tmpMode));
		tmpThread.start();
	}

	/**
	 * Helper method to handle the cancel action.
	 */
	private void doActionCancel()
	{
		if (loadTask.isInit() == true)
			refDialog.setVisible(false);

		loadTask.abort();
	}

	/**
	 * Helper method to handle the close action.
	 */
	private void doActionClose()
	{
		refDialog.setVisible(false);

		loadTask.reset();
		loadTask.abort();
	}

	/**
	 * Helper method to form the load details panel.
	 */
	private JPanel formPanelEast()
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
	private JPanel formPanelWest()
	{
		GuiUtil.linkRadioButtons(replaceAllRB, replaceCollideRB, appendWithOrignalRB, appendWithUniqueRB);

		JPanel retPanel = new JPanel(new MigLayout("", "", "[]"));
		retPanel.add(appendWithOrignalRB, "wrap");
		retPanel.add(appendWithUniqueRB, "wrap");
		retPanel.add(replaceCollideRB, "wrap");
		retPanel.add(replaceAllRB, "wrap");

		retPanel.add(projToBodyCB, "gapy 7 0");

		return retPanel;
	}

	/**
	 * Helper method that returns the appropriate progress message.
	 */
	private static String formProgressMsg(NotifyTask aTask)
	{
		// Display nothing if the task is in the init state
		if (aTask.isInit() == true)
			return "";

		double progress = aTask.getProgress();
		NumberUnit perU = new NumberUnit("", "", 1.0, "0.00 %");
		String retStr = "Progress: " + perU.getString(progress);

		Long runTime = null;
		if (aTask.isActive() == true || progress >= 0)
			runTime = aTask.getTimeEnd() - aTask.getTimeBeg();
		TimeCountUnit timeU = new TimeCountUnit(2);
		retStr += "    Time: " + timeU.getString(runTime);

		return retStr;
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
	private void installStructures(List<Structure> aFullL, Task aTask, InstallMode aMode)
	{
		// Project the structures onto the surface of the shape model
		if (projToBodyCB.isSelected() == true)
			StructureMiscUtil.projectControlPointsToShapeModel(refSmallBody, aFullL);

		// Split structures into various type
		// TODO: Eventually this step should not be needed
		List<PolyLine> tmpPathL = StructureMiscUtil.getPathsFrom(aFullL);
		List<Polygon> tmpPolyL = StructureMiscUtil.getPolygonsFrom(aFullL);
		List<Ellipse> tmpCircleL = StructureMiscUtil.getEllipsesFrom(aFullL, Mode.CIRCLE_MODE);
		List<Ellipse> tmpEllipseL = StructureMiscUtil.getEllipsesFrom(aFullL, Mode.ELLIPSE_MODE);
		List<Ellipse> tmpPointL = StructureMiscUtil.getEllipsesFrom(aFullL, Mode.POINT_MODE);

		double fullCnt = aFullL.size();

		double progOff = 0;
		double progTot = tmpPathL.size() / fullCnt;
		Task tmpTask = new PartialTask(aTask, progOff, progTot);
		StructureMiscUtil.installStructures(tmpTask, refPathStructureManager, tmpPathL, aMode);
		if (aTask.isActive() == false)
			return;

		progOff += progTot;
		progTot = tmpPolyL.size() / fullCnt;
		tmpTask = new PartialTask(aTask, progOff, progTot);
		StructureMiscUtil.installStructures(tmpTask, refPolyStructureManager, tmpPolyL, aMode);
		if (aTask.isActive() == false)
			return;

		progOff += progTot;
		progTot = tmpCircleL.size() / fullCnt;
		tmpTask = new PartialTask(aTask, progOff, progTot);
		StructureMiscUtil.installStructures(tmpTask, refCircleStructureManager, tmpCircleL, aMode);
		if (aTask.isActive() == false)
			return;

		progOff += progTot;
		progTot = tmpEllipseL.size() / fullCnt;
		tmpTask = new PartialTask(aTask, progOff, progTot);
		StructureMiscUtil.installStructures(tmpTask, refEllipseStructureManager, tmpEllipseL, aMode);
		if (aTask.isActive() == false)
			return;

		progOff += progTot;
		progTot = tmpPointL.size() / fullCnt;
		tmpTask = new PartialTask(aTask, progOff, progTot);
		StructureMiscUtil.installStructures(tmpTask, refPointStructureManager, tmpPointL, aMode);
		if (aTask.isActive() == false)
			return;

		progOff += progTot;
		aTask.setProgress(progOff);
		aTask.abort();
		SwingUtilities.invokeLater(() -> updateGui());
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

		Color fgColor = Colors.getPassFG();
		if (errMsg != null)
			fgColor = Colors.getFailFG();
		statusL.setForeground(fgColor);

		boolean isEnabled = errMsg == null;
		isEnabled &= loadTask.isInit() == true;
		acceptB.setEnabled(isEnabled);

		isEnabled = loadTask.isDone() == false;
		cancelB.setEnabled(isEnabled);

		isEnabled = loadTask.isDone() == true;
		closeB.setEnabled(isEnabled);

		loadTask.forceUpdate();
	}

}

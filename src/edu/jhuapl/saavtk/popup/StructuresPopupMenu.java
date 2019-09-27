package edu.jhuapl.saavtk.popup;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.coloringData.ColoringInfoWindow;
import edu.jhuapl.saavtk.gui.dialog.ChangeLatLonDialog;
import edu.jhuapl.saavtk.gui.dialog.ColorChooser;
import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.FacetColoringData;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.util.MathUtil;
import vtk.vtkCamera;
import vtk.vtkProp;
import vtk.rendering.vtkAbstractComponent;

public class StructuresPopupMenu<G1 extends Structure> extends PopupMenu
{
	// Ref vars
	private StructureManager<G1> refManager;
	private PolyhedralModel smallBodyModel;
	private Renderer renderer;

	// Gui vars
	private JMenuItem changeLatLonAction;
	private JMenuItem exportPlateDataAction;
	private JMenuItem plateStatisticsAction;
	private JMenuItem editAction;
	private JMenuItem centerStructureMenuItem;
	private JMenuItem centerStructurePreserveDistanceMenuItem;
	private JMenuItem displayInteriorMenuItem;
	private JMenuItem setLabelButton;
	private JCheckBoxMenuItem hideLabelButton;
	private JMenu labelProperties;
	private JMenuItem changeFontButton;
	private JMenuItem changeFontTypeButton;
	private JMenuItem changeLabelColorButton;
	private JCheckBoxMenuItem setLabelBorder;
	private JCheckBoxMenuItem hideMenuItem;

	public StructuresPopupMenu(ModelManager aModelManager, Renderer aRenderer, ModelNames aModelNames)
	{
		refManager = (StructureManager<G1>) aModelManager.getModel(aModelNames);
		smallBodyModel = aModelManager.getPolyhedralModel();
		renderer = aRenderer;

		// Determine the extended capabilities (based on structure type)
		boolean showChangeLatLon = false;
		showChangeLatLon |= aModelNames == ModelNames.POINT_STRUCTURES;
		showChangeLatLon |= aModelNames == ModelNames.CIRCLE_STRUCTURES;
		showChangeLatLon |= aModelNames == ModelNames.ELLIPSE_STRUCTURES;

		boolean showExportPlateDataInsidePolygon = false;
		showExportPlateDataInsidePolygon |= aModelNames == ModelNames.POLYGON_STRUCTURES;
		showExportPlateDataInsidePolygon |= aModelNames == ModelNames.CIRCLE_STRUCTURES;
		showExportPlateDataInsidePolygon |= aModelNames == ModelNames.ELLIPSE_STRUCTURES;

		boolean showDisplayInterior = aModelNames == ModelNames.POLYGON_STRUCTURES;

		// Set up the UI
		editAction = new JMenuItem(new EditAction());
		editAction.setText("Edit");
		// this.add(mi); // don't show for now

		JMenuItem changeColorAction = new JMenuItem(new ChangeColorAction());
		changeColorAction.setText("Change Color...");
		this.add(changeColorAction);

		hideMenuItem = new JCheckBoxMenuItem(new ShowHideAction());
		hideMenuItem.setText("Hide");
		this.add(hideMenuItem);

		setLabelButton = new JMenuItem(new SetLabelAction());
		setLabelButton.setText("Edit Label Text");
		this.add(setLabelButton);

//      hideLabelButton = new JCheckBoxMenuItem(new ShowLabelAction());
//      hideLabelButton.setText("Hide Label"); this.add(hideLabelButton);

		labelProperties = new JMenu();
		labelProperties.setText("Edit Label Properties...");

		// disable for now until bugs are fixed -turnerj1
		// this.add(labelProperties);

		changeFontButton = new JMenuItem(new changeFontSizeAction());
		changeFontButton.setText("Change Font Size");
		labelProperties.add(changeFontButton);

		changeFontTypeButton = new JMenuItem(new changeFontTypeAction());
		changeFontButton.setText("Change Font");
		labelProperties.add(changeFontTypeButton);

		changeLabelColorButton = new JMenuItem(new changeLabelColorAction());
		changeLabelColorButton.setText("Change Label Color");
		labelProperties.add(changeLabelColorButton);

		setLabelBorder = new JCheckBoxMenuItem(new showLabelBorderAction());
		setLabelBorder.setText("Show the label border");
		labelProperties.add(setLabelBorder);

		JMenuItem deleteAction = new JMenuItem(new DeleteAction());
		deleteAction.setText("Delete");
		this.add(deleteAction);

		centerStructureMenuItem = new JMenuItem(new CenterStructureAction(false));
		centerStructureMenuItem.setText("Center in Window (Close Up)");
		this.add(centerStructureMenuItem);

		centerStructurePreserveDistanceMenuItem = new JMenuItem(new CenterStructureAction(true));
		centerStructurePreserveDistanceMenuItem.setText("Center in Window (Preserve Distance)");
		this.add(centerStructurePreserveDistanceMenuItem);

		if (showChangeLatLon)
		{
			changeLatLonAction = new JMenuItem(new ChangeLatLonAction());
			changeLatLonAction.setText("Change Latitude/Longitude...");
			this.add(changeLatLonAction);
		}

		if (showExportPlateDataInsidePolygon)
		{
			exportPlateDataAction = new JMenuItem(new ExportPlateDataInsidePolygon());
			exportPlateDataAction.setText("Save plate data inside structure...");
			this.add(exportPlateDataAction);

			plateStatisticsAction = new JMenuItem(new ShowPlateStatisticsInfo());
			plateStatisticsAction.setText("Show plate data statistics inside structure...");
			this.add(plateStatisticsAction);
		}

		if (showDisplayInterior)
		{
			displayInteriorMenuItem = new JCheckBoxMenuItem(new DisplayInteriorAction());
			displayInteriorMenuItem.setText("Display Interior");
			this.add(displayInteriorMenuItem);
		}

	}

	/**
	 * Returns the reference StructureManager
	 */
	protected StructureManager<G1> getManager()
	{
		return refManager;
	}

	@Override
	public void show(Component invoker, int x, int y)
	{
		Set<G1> pickS = refManager.getSelectedItems();

		// Update the enable state of various UI elements
		boolean isEnabled = pickS.size() == 1;
		editAction.setEnabled(isEnabled);
		centerStructureMenuItem.setEnabled(isEnabled);
		centerStructurePreserveDistanceMenuItem.setEnabled(isEnabled);
		if (changeLatLonAction != null)
			changeLatLonAction.setEnabled(isEnabled);

		isEnabled = false;
		for (G1 aItem : pickS)
			isEnabled |= aItem.getVisible() == false;
		hideMenuItem.setSelected(isEnabled);

		boolean havePlateData = smallBodyModel.isColoringDataAvailable();
		if (exportPlateDataAction != null)
			exportPlateDataAction.setEnabled(havePlateData);
		if (plateStatisticsAction != null)
			plateStatisticsAction.setEnabled(havePlateData);

//        isEnabled = false;
//        for (G1 aItem : pickS)
//      	  isEnabled |= aItem.getLabelHidden() == true;
//        hideLabelButton.setSelected(isEnabled);
//
		isEnabled = false;
		for (G1 aItem : pickS)
			isEnabled |= refManager.isShowStructureInterior(aItem);
		if (displayInteriorMenuItem != null)
			displayInteriorMenuItem.setSelected(isEnabled);
//      	  isEnabled |= aItem.getLabelHidden() == true;

		// If any of the selected structures are displaying interior then show
		// the display interior menu item as unchecked. Otherwise show it checked.
		if (displayInteriorMenuItem != null)
		{
			displayInteriorMenuItem.setSelected(true);

			for (G1 aItem : pickS)
			{
				if (refManager.isShowStructureInterior(aItem) == false)
				{
					displayInteriorMenuItem.setSelected(false);
					break;
				}
			}
		}

		super.show(invoker, x, y);
	}

	@Override
	public void showPopup(MouseEvent e, vtkProp pickedProp, int pickedCellId, double[] pickedPosition)
	{
		show(e.getComponent(), e.getX(), e.getY());
	}

	protected class EditAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			List<G1> pickL = refManager.getSelectedItems().asList();
			if (pickL.size() == 1)
				refManager.activateStructure(pickL.get(0));
		}
	}

	protected class ChangeColorAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent actionEvent)
		{
			List<G1> pickL = refManager.getSelectedItems().asList();
			if (pickL.size() == 0)
				return;

			// Use the color of the first item as the default to show
			Color color = ColorChooser.showColorChooser(getInvoker(), pickL.get(0).getColor());
			if (color == null)
				return;

			for (G1 aItem : pickL)
				refManager.setStructureColor(aItem, color);
		}
	}

	protected class ShowHideAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			Set<G1> pickS = refManager.getSelectedItems();
			boolean isVisible = !hideMenuItem.isSelected();
			refManager.setStructureVisible(pickS, isVisible);
		}
	}

	protected class DeleteAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			Set<G1> pickS = refManager.getSelectedItems();
			refManager.removeStructures(pickS);
		}
	}

	private class CenterStructureAction extends AbstractAction
	{
		private boolean preserveCurrentDistance = false;

		public CenterStructureAction(boolean preserveCurrentDistance)
		{
			this.preserveCurrentDistance = preserveCurrentDistance;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			List<G1> pickL = refManager.getSelectedItems().asList();
			if (pickL.size() != 1)
				return;
			G1 tmpItem = pickL.get(0);

			double viewAngle = renderer.getCameraViewAngle();
			double[] focalPoint = refManager.getStructureCenter(tmpItem);
			double[] normal = refManager.getStructureNormal(tmpItem);
			vtkAbstractComponent renWin = renderer.getRenderWindowPanel();

			double distanceToStructure = 0.0;
			if (preserveCurrentDistance)
			{
				vtkCamera activeCamera = renWin.getRenderer().GetActiveCamera();
				double[] pos = activeCamera.GetPosition();
				double[] closestPoint = smallBodyModel.findClosestPoint(pos);
				distanceToStructure = MathUtil.distanceBetween(pos, closestPoint);
			}
			else
			{
				double size = refManager.getStructureSize(tmpItem);
				distanceToStructure = size / Math.tan(Math.toRadians(viewAngle) / 2.0);
			}

			double[] newPos = { focalPoint[0] + distanceToStructure * normal[0],
					focalPoint[1] + distanceToStructure * normal[1], focalPoint[2] + distanceToStructure * normal[2] };

			// compute up vector
			double[] dir = { focalPoint[0] - newPos[0], focalPoint[1] - newPos[1], focalPoint[2] - newPos[2] };
			MathUtil.vhat(dir, dir);
			double[] zAxis = { 0.0, 0.0, 1.0 };
			double[] upVector = new double[3];
			MathUtil.vcrss(dir, zAxis, upVector);

			if (upVector[0] != 0.0 || upVector[1] != 0.0 || upVector[2] != 0.0)
				MathUtil.vcrss(upVector, dir, upVector);
			else
				upVector = new double[] { 1.0, 0.0, 0.0 };

			renderer.setCameraOrientation(newPos, focalPoint, upVector, viewAngle);
		}
	}

	protected class ChangeLatLonAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent actionEvent)
		{
			List<G1> pickL = refManager.getSelectedItems().asList();
			if (pickL.size() != 1)
				return;

			ChangeLatLonDialog<?> dialog = new ChangeLatLonDialog<>(refManager, pickL.get(0));
			dialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(getInvoker()));
			dialog.setVisible(true);
		}
	}

	protected class ExportPlateDataInsidePolygon extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			File file = CustomFileChooser.showSaveDialog(getInvoker(), "Save Plate Data", "platedata.csv");
			if (file != null)
			{
				try
				{
					List<G1> pickL = refManager.getSelectedItems().asList();
					refManager.savePlateDataInsideStructure(pickL.get(0), file);
//					if (selectedStructures.length == 1)
//						model.savePlateDataInsideStructure(selectedStructures[0], file);
				}
				catch (Exception e1)
				{
					JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getInvoker()),
							"Unable to save file to " + file.getAbsolutePath(), "Error Saving File",
							JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		}
	}

	protected class ShowPlateStatisticsInfo extends AbstractAction
	{

		@Override
		public void actionPerformed(ActionEvent e)
		{
			Set<G1> pickS = refManager.getSelectedItems();

//			if (selectedStructures.length == 1)
//			{
			FacetColoringData[] data = refManager.getPlateDataInsideStructure(pickS);
			try
			{
				ColoringInfoWindow window = new ColoringInfoWindow(data);
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
//		}

	}

	protected class SetLabelAction extends AbstractAction
	{
		public SetLabelAction()
		{
			super("Set Label");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			List<G1> pickL = refManager.getSelectedItems().asList();
			if (pickL.size() == 0)
				return;

			String infoMsg = "Enter structure label text. Leave blank to remove label.";
			String oldVal = pickL.get(0).getLabel();
			String newVal = JOptionPane.showInputDialog(infoMsg, oldVal);
			if (newVal == null)
				return;

			for (G1 aItem : pickL)
				refManager.setStructureLabel(aItem, newVal);
		}
	}

	protected class ShowLabelAction extends AbstractAction
	{
		public ShowLabelAction()
		{
			super("Hide Label");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			Set<G1> pickS = refManager.getSelectedItems();
			if (pickS.isEmpty() == true)
				return;

			boolean isVisible = hideLabelButton.isSelected() == false;
			refManager.setLabelVisible(pickS, isVisible);
		}
	}

	protected class changeFontSizeAction extends AbstractAction
	{
		public changeFontSizeAction()
		{
			super("Change Font Size");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			Set<G1> pickS = refManager.getSelectedItems();
			if (pickS.isEmpty() == true)
				return;

			String option = JOptionPane.showInputDialog("Enter font size. Font is 12 by default.");
			int op = Integer.parseInt(option);

			refManager.setLabelFontSize(pickS, op);
		}
	}

	protected class changeFontTypeAction extends AbstractAction
	{
		public changeFontTypeAction()
		{
			super("Change Font Type");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			List<G1> pickL = refManager.getSelectedItems().asList();
			if (pickL.isEmpty() == true)
				return;

			// Prompt the user for a choice
			String[] options = { "Times", "Arial", "Courier" };
			int optIdx = JOptionPane.showOptionDialog(null, "Pick the font you wish to use", "Choose",
					JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (optIdx == -1)
				return;

			refManager.setLabelFontType(pickL, options[optIdx]);
		}
	}

	protected class changeLabelColorAction extends AbstractAction
	{
		public changeLabelColorAction()
		{
			super("Change Label Color");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			List<G1> pickL = refManager.getSelectedItems().asList();
			if (pickL.isEmpty() == true)
				return;

			// Use the color of the first item as the default to show
			Color color = ColorChooser.showColorChooser(getInvoker(), pickL.get(0).getColor());
			if (color == null)
				return;

			refManager.setLabelColor(pickL, color);
		}
	}

	protected class DisplayInteriorAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			Set<G1> pickS = refManager.getSelectedItems();
			refManager.setShowStructuresInterior(pickS, displayInteriorMenuItem.isSelected());
		}
	}

	protected class showLabelBorderAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			refManager.showBorders();
		}
	}
}

package edu.jhuapl.saavtk.structure.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import edu.jhuapl.saavtk.gui.ProfilePlot;
import edu.jhuapl.saavtk.gui.StatusBar;
import edu.jhuapl.saavtk.gui.dialog.NormalOffsetChangerDialog;
import edu.jhuapl.saavtk.gui.funk.PopupButton;
import edu.jhuapl.saavtk.gui.table.ColorCellEditor;
import edu.jhuapl.saavtk.gui.table.ColorCellRenderer;
import edu.jhuapl.saavtk.gui.table.TablePopupHandler;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManagerListener;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.popup.PopupMenu;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.gui.action.LoadEsriShapeFileAction;
import edu.jhuapl.saavtk.structure.gui.action.LoadSbmtStructuresFileAction;
import edu.jhuapl.saavtk.structure.gui.action.SaveEsriShapeFileAction;
import edu.jhuapl.saavtk.structure.gui.action.SaveSbmtStructuresFileAction;
import edu.jhuapl.saavtk.structure.gui.action.SaveVtkFileAction;
import edu.jhuapl.saavtk.util.ColorIcon;
import glum.gui.GuiUtil;
import glum.gui.component.GNumberFieldSlider;
import glum.gui.icon.EmptyIcon;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;
import glum.item.ItemManagerUtil;
import net.miginfocom.swing.MigLayout;

/**
 * Panel used to display a list of structures.
 * <P>
 * The following functionality is supported:
 * <UL>
 * <LI>Display a list of structures in a table.
 * <LI>Allow user to show, hide, add, edit, or delete structures
 * <LI>Allow user to load or save structures.
 * <LI>
 *
 * @author lopeznr1
 */
public class StructurePanel<G1 extends Structure> extends JPanel
		implements ActionListener, ItemEventListener, PickManagerListener
{
	// Constants
	private static final long serialVersionUID = 1L;

	// Ref vars
	private final StructureManager<G1> refStructureManager;
	private final ModelManager refModelManager;
	private final PickManager refPickManager;
	private final Picker refPicker;

	// State vars
	private ImmutableSet<G1> prevPickS;
	private File structuresFile;

	// GUI vars
	private JLabel structuresFileL;
	private ItemListPanel<G1> itemILP;
	private JFrame profileWindow;

	private NormalOffsetChangerDialog changeOffsetDialog;
	private JLabel tableHeadL;
	private JButton selectAllB, selectInvertB, selectNoneB;
	private JButton createB, deleteB;
	private JToggleButton editB;
	private JLabel labelTitleL, structTitleL;
	private JButton structColorB, structHideB, structShowB;
	private JButton labelColorB, labelHideB, labelShowB;
	private JButton changeOffsetB;
	private GNumberFieldSlider fontSizeNFS;
	private GNumberFieldSlider lineWidthNFS;

	private PopupButton saveB;
	private PopupButton loadB;

	/**
	 * Standard Constructor
	 */
	public StructurePanel(ModelManager aModelManager, StructureManager<G1> aStructureManager, PickManager aPickManager,
			Picker aPicker, StatusBar aStatusBar)
	{
		refModelManager = aModelManager;
		refStructureManager = aStructureManager;
		refPickManager = aPickManager;
		refPicker = aPicker;

		prevPickS = ImmutableSet.of();

		changeOffsetDialog = null;

		loadB = new PopupButton("Load...");
		saveB = new PopupButton("Save...");

		loadB.getPopup().add(new JMenuItem(new LoadSbmtStructuresFileAction<>(this, refStructureManager)));
		JMenuItem esriLoadMI = loadB.getPopup()
				.add(new JMenuItem(new LoadEsriShapeFileAction<>(this, refStructureManager, refModelManager, aStatusBar)));
		if (!(refStructureManager instanceof PointModel) && !(refStructureManager instanceof LineModel))
		{
			// don't let user import esri shapes as ellipses or circles; these require extra
			// information in order to be upgraded (downgraded?) to "SBMT" structures ...
			// instead they can use the polygons tab
			esriLoadMI.setEnabled(false);
			esriLoadMI.setToolTipText("ESRI circles and ellipses can be imported using the Polygons tab");
		}

		saveB.getPopup().add(new JMenuItem(new SaveSbmtStructuresFileAction<>(this, refStructureManager)));
		saveB.getPopup().add(new JMenuItem(new SaveEsriShapeFileAction<>(this, refStructureManager, refModelManager)));
		saveB.getPopup().add(new JMenuItem(new SaveVtkFileAction<>(refStructureManager)));

		setLayout(new MigLayout("", "", "[]"));

		JLabel fileNameL = new JLabel("File: ");
		structuresFileL = new JLabel("<no file loaded>");
		add(fileNameL, "span,split");
		add(structuresFileL, "growx,pushx,w 100:100:");
		add(loadB, "sg g0");
		add(saveB, "sg g0,wrap");

		// Table header
		selectInvertB = GuiUtil.formButton(this, IconUtil.getSelectInvert());
		selectInvertB.setToolTipText(ToolTipUtil.getSelectInvert());

		selectNoneB = GuiUtil.formButton(this, IconUtil.getSelectNone());
		selectNoneB.setToolTipText(ToolTipUtil.getSelectNone());

		selectAllB = GuiUtil.formButton(this, IconUtil.getSelectAll());
		selectAllB.setToolTipText(ToolTipUtil.getSelectAll());

		tableHeadL = new JLabel("Structures: 0");
		add(tableHeadL, "growx,span,split");
		add(selectInvertB, "w 24!,h 24!");
		add(selectNoneB, "w 24!,h 24!");
		add(selectAllB, "w 24!,h 24!,wrap 2");

		// Table content
		QueryComposer<LookUp> tmpComposer = new QueryComposer<>();
		tmpComposer.addAttribute(LookUp.IsVisible, Boolean.class, "Show", 40);
		tmpComposer.addAttribute(LookUp.Color, Color.class, "Color", 50);
		tmpComposer.addAttribute(LookUp.Id, Integer.class, "Id", "1,987");
		tmpComposer.addAttribute(LookUp.Type, String.class, "Type", "-Ellipses-");
		tmpComposer.addAttribute(LookUp.Name, String.class, "Name", null);
		tmpComposer.addAttribute(LookUp.Details, String.class, "Details", null);
		tmpComposer.addAttribute(LookUp.Label, String.class, "Label", null);

		tmpComposer.setEditor(LookUp.IsVisible, new BooleanCellEditor());
		tmpComposer.setRenderer(LookUp.IsVisible, new BooleanCellRenderer());
		tmpComposer.setEditor(LookUp.Color, new ColorCellEditor());
		tmpComposer.setRenderer(LookUp.Color, new ColorCellRenderer(false));
		tmpComposer.setEditor(LookUp.Name, new DefaultCellEditor(new JTextField()));
		tmpComposer.setEditor(LookUp.Label, new DefaultCellEditor(new JTextField()));
		tmpComposer.getItem(LookUp.Id).maxSize *= 2;
		tmpComposer.getItem(LookUp.Details).defaultSize *= 3;
		tmpComposer.getItem(LookUp.Name).defaultSize *= 2;
		tmpComposer.getItem(LookUp.Label).defaultSize *= 2;

		ItemHandler<G1> tmpIH = new StructureItemHandler<>(refStructureManager, tmpComposer);
		ItemProcessor<G1> tmpIP = refStructureManager;
		itemILP = new ItemListPanel<>(tmpIH, tmpIP, true);
		itemILP.setSortingEnabled(true);

		PopupMenu structuresPopupMenu = refPickManager.getPopupManager().getPopup(refStructureManager);
		JTable structureTable = itemILP.getTable();
		structureTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		structureTable.addMouseListener(new TablePopupHandler(refStructureManager, structuresPopupMenu));
		add(new JScrollPane(structureTable), "growx,growy,pushx,pushy,span,wrap");

		// Specialized code to handle deletion via the keyboard
		structureTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e)
			{
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE)
					doDeleteSelectedStructures();
			}
		});

		createB = new JButton("New");
		createB.addActionListener(this);
		createB.setVisible(refStructureManager.supportsActivation());
		deleteB = new JButton("Delete");
		deleteB.addActionListener(this);
		editB = new JToggleButton("Edit");
		editB.addActionListener(this);
		add(createB, "sg g1,span,split");
		add(editB, "sg g1");
		add(deleteB, "sg g1,wrap");

		labelTitleL = new JLabel("Labels:", JLabel.RIGHT);
		labelColorB = new JButton("");
		labelColorB.addActionListener(this);
		labelHideB = new JButton("Hide");
		labelHideB.addActionListener(this);
		labelShowB = new JButton("Show");
		labelShowB.addActionListener(this);
		add(labelTitleL, "span,split,sg g2");
		add(labelHideB, "sg g3");
		add(labelShowB, "sg g3");
		add(labelColorB, "gapy 0,sgy g3,wrap");

		structTitleL = new JLabel("Structs:", JLabel.RIGHT);
		structColorB = new JButton("");
		structColorB.addActionListener(this);
		structHideB = new JButton("Hide");
		structHideB.addActionListener(this);
		structShowB = new JButton("Show");
		structShowB.addActionListener(this);
		add(structTitleL, "span,split,sg g2");
		add(structHideB, "sg g3");
		add(structShowB, "sg g3");
		add(structColorB, "gapy 0,sgy g3,wrap");

		changeOffsetB = new JButton("Change Normal Offset...");
		changeOffsetB.addActionListener(this);
		changeOffsetB
				.setToolTipText("<html>Structures displayed on a shape model need to be shifted slightly away from<br>"
						+ "the shape model in the direction normal to the plates as otherwise they will<br>"
						+ "interfere with the shape model itself and may not be visible. Click this<br>"
						+ "button to show a dialog that will allow you to explicitely set the offset<br>"
						+ "amount in meters.</html>");
		add(changeOffsetB, "sg g5,span,split");

		// Add support for profile plots if the StructureManager is of type LineModel
		JButton openProfilePlotB = new JButton("Show Profile Plot...");
		openProfilePlotB.setVisible(false);
		if (refStructureManager.getClass() == LineModel.class)
		{
			// Only add profile plots for line structures
			ProfilePlot profilePlot = new ProfilePlot((LineModel) refStructureManager,
					(PolyhedralModel) aModelManager.getModel(ModelNames.SMALL_BODY));

			// Create a new frame/window with profile
			profileWindow = new JFrame();
			ImageIcon icon = new ImageIcon(getClass().getResource("/edu/jhuapl/saavtk/data/black-sphere.png"));
			profileWindow.setTitle("Profile Plot");
			profileWindow.setIconImage(icon.getImage());
			profileWindow.getContentPane().add(profilePlot.getChartPanel());
			profileWindow.setSize(600, 400);
			profileWindow.setVisible(false);

			openProfilePlotB.setVisible(true);
			openProfilePlotB.addActionListener((aEvent) -> {
				profileWindow.setVisible(true);
			});
		}
		add(openProfilePlotB, "sg g5,wrap");

		fontSizeNFS = new GNumberFieldSlider(this, "Font Size:", 8, 120);
		fontSizeNFS.setNumSteps(67);
		fontSizeNFS.setNumColumns(3);
		add(fontSizeNFS, "growx,sg g6,span,wrap");

		lineWidthNFS = new GNumberFieldSlider(this, "Line Width:", 1, 100);
		lineWidthNFS.setNumSteps(100);
		lineWidthNFS.setNumColumns(3);

		// Create the pointDiameterPanel if the StructureManager is of type PointModel
		PointDiameterPanel pointDiameterPanel = null;
		if (refStructureManager instanceof PointModel)
			pointDiameterPanel = new PointDiameterPanel((PointModel) refStructureManager);

		// Either the lineWidthNFS or the pointDiameterPanel will be visible
		if (pointDiameterPanel != null)
			add(pointDiameterPanel, "span");
		else
			add(lineWidthNFS, "growx,sg g6,span");

		// Hack to force fontSizeNFS label to have the same size as lineWidthNFS
		fontSizeNFS.getLabelComponent().setPreferredSize(lineWidthNFS.getLabelComponent().getPreferredSize());

		updateControlGui();

		// Register for events of interest
		PickUtil.autoDeactivatePickerWhenComponentHidden(refPickManager, refPicker, this);
		refStructureManager.addListener(this);
		refPickManager.addListener(this);

		// TODO: This registration should be done by the refStructureManager
		refStructureManager.registerDefaultPickerHandler(refPickManager.getDefaultPicker());
	}

	/**
	 * Returns the file associated with the {@link Structure}s.
	 */
	public File getStructureFile()
	{
		return structuresFile;
	}

	/**
	 * Notifies the panel of the file that is associated with the loaded
	 * {@link Structure}s.
	 */
	public void notifyFileLoaded(File aFile)
	{
		structuresFileL.setText(aFile.getName());
		structuresFileL.setToolTipText(aFile.getAbsolutePath());
		structuresFile = aFile;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		List<G1> pickL = refStructureManager.getSelectedItems().asList();

		Object source = actionEvent.getSource();
		if (source == createB)
			doActionCreate();
		else if (source == editB)
			doActionEdit();
		else if (source == selectAllB)
			ItemManagerUtil.selectAll(refStructureManager);
		else if (source == selectNoneB)
			ItemManagerUtil.selectNone(refStructureManager);
		else if (source == selectInvertB)
			ItemManagerUtil.selectInvert(refStructureManager);
		else if (source == deleteB)
			doDeleteSelectedStructures();
		else if (source == labelColorB)
		{
			Color defColor = refStructureManager.getLabelColor(pickL.get(0));
			Color tmpColor = JColorChooser.showDialog(this, "Color Chooser Dialog", defColor);
			if (tmpColor == null)
				return;

			refStructureManager.setLabelColor(pickL, tmpColor);
		}
		else if (source == labelHideB)
			refStructureManager.setLabelVisible(pickL, false);
		else if (source == labelShowB)
			refStructureManager.setLabelVisible(pickL, true);
		else if (source == structColorB)
		{
			Color defColor = refStructureManager.getStructureColor(pickL.get(0));
			Color tmpColor = JColorChooser.showDialog(this, "Color Chooser Dialog", defColor);
			if (tmpColor == null)
				return;

			refStructureManager.setStructureColor(pickL, tmpColor);
		}
		else if (source == structHideB)
			refStructureManager.setStructureVisible(pickL, false);
		else if (source == structShowB)
			refStructureManager.setStructureVisible(pickL, true);
		else if (source == changeOffsetB)
		{
			// Lazy init
			if (changeOffsetDialog == null)
			{
				changeOffsetDialog = new NormalOffsetChangerDialog(refStructureManager);
				changeOffsetDialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(this));
			}

			changeOffsetDialog.setVisible(true);
		}
		else if (source == fontSizeNFS)
			doUpdateFontSize();
		else if (source == lineWidthNFS)
			doUpdateLineWidth();

		updateControlGui();
	}

	@Override
	public void handleItemEvent(Object aSource, ItemEventType aEventType)
	{
		if (aEventType == ItemEventType.ItemsSelected)
		{
			ImmutableSet<G1> currPickS = refStructureManager.getSelectedItems();

			G1 tmpItem = null;
			if (currPickS.size() > 0)
				tmpItem = currPickS.asList().get(currPickS.size() - 1);

			// Scroll only if the selection source was not from our refStructureManager
			if (aSource != refStructureManager)
			{
				// Scroll only if the previous pickL does not contains all of
				// selected items. This means that an item was deselected and
				// we should not scroll on deselections.
				if (prevPickS.containsAll(currPickS) == false)
					itemILP.scrollToItem(tmpItem);

				prevPickS = currPickS;
			}
		}

		updateControlGui();
	}

	@Override
	public void pickerChanged()
	{
		boolean tmpBool = refPicker == refPickManager.getActivePicker();
		editB.setSelected(tmpBool);

		// Clear the activated structure if not in edit mode
		if (tmpBool == false)
			refStructureManager.activateStructure(null);

		// TODO: In the future the structure table should never be disabled
		// When refStructureManager supports activation then update table enable state
		if (refStructureManager.supportsActivation() == true)
		{
			JTable structuresTable = itemILP.getTable();

			if (tmpBool == true)
				structuresTable.setEnabled(false);
			else
				structuresTable.setEnabled(true);
		}

		updateControlGui();
	}

	/**
	 * Helper method that handles the create action.
	 */
	private void doActionCreate()
	{
		// Ensure all structures are visible (in case any are hidden)
		refStructureManager.setVisible(true);

		G1 tmpItem = refStructureManager.addNewStructure();
		refPickManager.setActivePicker(refPicker);

		List<G1> tmp2L = ImmutableList.of(tmpItem);
		refStructureManager.setSelectedItems(tmp2L);
	}

	/**
	 * Helper method that handles the edit action.
	 */
	private void doActionEdit()
	{
		boolean isEditMode = editB.isSelected();

		// Force everything to be visible - in case the user hid anything
		if (isEditMode == true)
			refStructureManager.setVisible(true);

		// Switch to the proper picker
		Picker tmpPicker = null;
		if (isEditMode == true)
			tmpPicker = refPicker;
		refPickManager.setActivePicker(tmpPicker);

		// Activate the relevant structure
		if (refStructureManager.supportsActivation() == true)
		{
			G1 tmpItem = null;
			List<G1> pickL = refStructureManager.getSelectedItems().asList();
			if (isEditMode == true && pickL.size() == 1)
				tmpItem = pickL.get(0);
			refStructureManager.activateStructure(tmpItem);
		}
	}

	/**
	 * Helper method to delete the selected structures
	 */
	private void doDeleteSelectedStructures()
	{
		Set<G1> pickS = refStructureManager.getSelectedItems();

		// Request confirmation when deleting multiple items
		if (pickS.size() > 0)
		{
			String infoMsg = "Are you sure you want to delete " + pickS.size() + " structures?";
			int result = JOptionPane.showConfirmDialog(JOptionPane.getFrameForComponent(this), infoMsg, "Confirm Deletion",
					JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.NO_OPTION)
				return;
		}

		// Update internal state vars
		refPickManager.setActivePicker(null);
		refStructureManager.activateStructure(null);

		// Remove the structures
		refStructureManager.removeStructures(pickS);

		// Update GUI
		updateControlGui();
	}

	/**
	 * Helper method to update the (selected) models to reflect the user selected
	 * font size.
	 */
	private void doUpdateFontSize()
	{
		if (fontSizeNFS.isValidInput() == false)
			return;

		// Retrieve the fontSize
		int fontSize = (int) fontSizeNFS.getValue();

		// Update the relevant structures
		Set<G1> pickS = refStructureManager.getSelectedItems();
		refStructureManager.setLabelFontSize(pickS, fontSize);
	}

	/**
	 * Helper method to update the specified structures to reflect the user selected
	 * line width.
	 */
	private void doUpdateLineWidth()
	{
		if (lineWidthNFS.isValidInput() == false)
			return;

		// Retrieve the lineWidth
		int lineWidth = (int) lineWidthNFS.getValue();

// TODO: Currently individual structures can not have different line widths from
// TODO: their parent StructureManager. In the future that may be a request
		// Update the relevant structures
//		Set<G1> pickS = refStructureManager.getSelectedItems();
//		structureModel.setLineWidth(pickS, lineWidth);
		refStructureManager.setLineWidth(lineWidth);
	}

	/**
	 * Helper method to update the colored icons/text for labelIconB and structIconB
	 */
	private void updateColoredButtons()
	{
		// These values may need to be fiddled with if there are sizing issues
		int iconW = 60;
		int iconH = (int) (structHideB.getHeight() * 0.50);

		// Update the label / struct colors
		Icon labelIcon = null;
		Icon structIcon = null;

		List<G1> pickL = refStructureManager.getSelectedItems().asList();
		boolean isEnabled = pickL.size() > 0;
		if (isEnabled == true)
		{
			// Determine label color attributes
			boolean isMixed = false;
			Color tmpColor = refStructureManager.getLabelColor(pickL.get(0));
			for (G1 aItem : pickL)
				isMixed |= Objects.equals(tmpColor, refStructureManager.getLabelColor(aItem)) == false;

			if (isMixed == false)
				labelIcon = new ColorIcon(tmpColor, Color.BLACK, iconW, iconH);
			else
				labelIcon = new ColorIcon(Color.LIGHT_GRAY, Color.GRAY, iconW, iconH);

			// Determine structure color attributes
			isMixed = false;
			tmpColor = refStructureManager.getStructureColor(pickL.get(0));
			for (G1 aItem : pickL)
				isMixed |= Objects.equals(tmpColor, refStructureManager.getStructureColor(aItem)) == false;

			if (isMixed == false)
				structIcon = new ColorIcon(tmpColor, Color.BLACK, iconW, iconH);
			else
				structIcon = new ColorIcon(Color.LIGHT_GRAY, Color.GRAY, iconW, iconH);
		}
		else
		{
			labelIcon = new EmptyIcon(iconW, iconH);
			structIcon = labelIcon;
		}

		labelColorB.setIcon(labelIcon);
		structColorB.setIcon(structIcon);
	}

	/**
	 * Helper method to update control UI. Triggered when selection has changed.
	 */
	private void updateControlGui()
	{
		// Update various buttons
		int cntFullItems = refStructureManager.getNumItems();
		boolean isEnabled = cntFullItems > 0;
		selectInvertB.setEnabled(isEnabled);

		List<G1> pickL = refStructureManager.getSelectedItems().asList();
		int cntPickItems = pickL.size();
		isEnabled = cntPickItems > 0;
		selectNoneB.setEnabled(isEnabled);
		deleteB.setEnabled(isEnabled);
		labelColorB.setEnabled(isEnabled);
		structColorB.setEnabled(isEnabled);

		isEnabled = refStructureManager.supportsActivation() == false | pickL.size() == 1;
		editB.setEnabled(isEnabled);

		isEnabled = cntFullItems > 0 && cntPickItems < cntFullItems;
		selectAllB.setEnabled(isEnabled);

		isEnabled = cntPickItems > 0 && editB.isSelected() == false;
		labelTitleL.setEnabled(isEnabled);
		labelHideB.setEnabled(isEnabled);
		labelShowB.setEnabled(isEnabled);
//		labelColorB.setEnabled(isEnabled);
		structTitleL.setEnabled(isEnabled);
		structHideB.setEnabled(isEnabled);
		structShowB.setEnabled(isEnabled);
//		structColorB.setEnabled(isEnabled);
		updateColoredButtons();

		isEnabled = cntPickItems > 0;
		fontSizeNFS.setEnabled(isEnabled);

		int fontSize = -1;
		if (cntPickItems > 0)
			fontSize = refStructureManager.getLabelFontSize(pickL.get(0));
		fontSizeNFS.setValue(fontSize);

		double lineWidth = -1;
		lineWidth = refStructureManager.getLineWidth();
// TODO: Enable if lineWidths are customizable on a per structure basis
//		if (pickL.size() > 0)
//			lineWidth = refStructureManager.getLineWidth(pickL.get(0));
		lineWidthNFS.setValue(lineWidth);

		// Table title
		DecimalFormat cntFormat = new DecimalFormat("#,###");
		String infoStr = "Structures: " + cntFormat.format(cntFullItems);
		if (cntPickItems > 0)
			infoStr += "  (Selected: " + cntFormat.format(cntPickItems) + ")";

		tableHeadL.setText(infoStr);
	}

}

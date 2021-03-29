package edu.jhuapl.saavtk.structure.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
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
import com.google.common.collect.Range;

import edu.jhuapl.saavtk.gui.ProfilePlot;
import edu.jhuapl.saavtk.gui.dialog.NormalOffsetChangerDialog;
import edu.jhuapl.saavtk.gui.funk.PopupButton;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.table.ColorCellEditor;
import edu.jhuapl.saavtk.gui.table.ColorCellRenderer;
import edu.jhuapl.saavtk.gui.table.SourceRenderer;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.saavtk.model.Model;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.structure.CircleModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.model.structure.PointModel;
import edu.jhuapl.saavtk.model.structure.PolygonModel;
import edu.jhuapl.saavtk.pick.ControlPointsPicker;
import edu.jhuapl.saavtk.pick.PickListener;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManagerListener;
import edu.jhuapl.saavtk.pick.PickMode;
import edu.jhuapl.saavtk.pick.PickTarget;
import edu.jhuapl.saavtk.pick.PickUtil;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.structure.ControlPointsHandler;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.gui.action.SaveEsriShapeFileAction;
import edu.jhuapl.saavtk.structure.gui.action.SaveSbmtStructuresFileAction;
import edu.jhuapl.saavtk.structure.gui.action.SaveVtkFileAction;
import edu.jhuapl.saavtk.util.ColorIcon;
import glum.gui.GuiUtil;
import glum.gui.action.PopupMenu;
import glum.gui.component.GNumberFieldSlider;
import glum.gui.icon.EmptyIcon;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemHandler;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.ItemProcessor;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.gui.table.NumberRenderer;
import glum.gui.table.TablePopupHandler;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;
import glum.item.ItemManagerUtil;
import net.miginfocom.swing.MigLayout;

/**
 * Panel used to display a list of structures.
 * <p>
 * The following functionality is supported:
 * <ul>
 * <li>Display a list of structures in a table.
 * <li>Allow user to show, hide, add, edit, or delete structures
 * <li>Allow user to load or save structures.
 * <ul>
 *
 * @author lopeznr1
 */
public class StructurePanel<G1 extends Structure> extends JPanel
		implements ActionListener, ItemEventListener, PickListener, PickManagerListener
{
	// Constants
	private static final Range<Double> FontSizeRange = Range.closed(8.0, 120.0);
	private static final Range<Double> LineWidthRange = Range.closed(1.0, 100.0);

	// Ref vars
	private final StructureManager<G1> refStructureManager;
	private final PickManager refPickManager;
	private final Picker refPicker;

	// State vars
	private ImmutableSet<G1> prevPickS;
	private File structuresFile;

	// GUI vars
	private final PopupMenu<?> popupMenu;
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
	private JLabel fontSizeL, lineWidthL;
	private GNumberFieldSlider fontSizeNFS;
	private GNumberFieldSlider lineWidthNFS;

	private PopupButton saveB;

	/**
	 * Standard Constructor
	 */
	public StructurePanel(StructureManager<G1> aStructureManager, PickManager aPickManager, Picker aPicker,
			Renderer aRenderer, PolyhedralModel aSmallBody, ModelManager aModelManager)
	{
		refStructureManager = aStructureManager;
		refPickManager = aPickManager;
		refPicker = aPicker;

		prevPickS = ImmutableSet.of();

		changeOffsetDialog = null;

		setLayout(new MigLayout("", "", "[]"));

		// Legacy save UI
		saveB = new PopupButton("Save...");
		saveB.getPopup().add(new JMenuItem(new SaveSbmtStructuresFileAction<>(this, refStructureManager, aSmallBody)));
		saveB.getPopup().add(new JMenuItem(new SaveEsriShapeFileAction<>(this, refStructureManager, aModelManager)));
		saveB.getPopup().add(new JMenuItem(new SaveVtkFileAction<>(refStructureManager)));
		add(saveB, "align right,span,split,wrap");

		// Popup menu
		popupMenu = StructureGuiUtil.formPopupMenu(refStructureManager, aRenderer, aSmallBody, this);

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
		tmpComposer.addAttribute(LookUp.Id, Integer.class, "Id", "98");
		tmpComposer.addAttribute(LookUp.Type, String.class, "Type", "Polygon");
		tmpComposer.addAttribute(LookUp.Name, String.class, "Name", null);
		tmpComposer.addAttribute(LookUp.Label, String.class, "Label", null);
		if (aStructureManager instanceof AbstractEllipsePolygonModel)
		{
			String maxStr = "Diam.: km"; // "9,876.987"
			tmpComposer.addAttribute(LookUp.Diameter, Double.class, "Diam: km", maxStr);
			tmpComposer.setRenderer(LookUp.Diameter, new NumberRenderer("#.####", "---"));
			tmpComposer.getItem(LookUp.Diameter).maxSize *= 2;
			boolean isPointManager = aStructureManager instanceof PointModel;
			if (isPointManager == false && aStructureManager instanceof CircleModel == false)
			{
				tmpComposer.addAttribute(LookUp.Angle, Double.class, "Angle", maxStr);
				tmpComposer.setRenderer(LookUp.Angle, new NumberRenderer("#.###", "---"));
				tmpComposer.addAttribute(LookUp.Flattening, Double.class, "Flat.", "0.2020");
				tmpComposer.setRenderer(LookUp.Flattening, new NumberRenderer("#.####", "---"));
				tmpComposer.getItem(LookUp.Flattening).maxSize *= 2;
			}
		}
		else
		{
			String maxStr = "Len.: km"; // "9,876.987"
			tmpComposer.addAttribute(LookUp.Length, Double.class, "Length: km", maxStr);
			tmpComposer.setRenderer(LookUp.Length, new NumberRenderer("#.#####", "----"));
			tmpComposer.getItem(LookUp.Length).maxSize *= 2;
			tmpComposer.addAttribute(LookUp.VertexCount, Integer.class, "# Pts", "999");
			tmpComposer.getItem(LookUp.VertexCount).maxSize *= 2;
			if (aStructureManager instanceof PolygonModel)
			{
				String tmpStr = "Area: km" + (char) 0x00B2;
				tmpComposer.addAttribute(LookUp.Area, Double.class, tmpStr, maxStr);
				tmpComposer.setRenderer(LookUp.Area, new NumberRenderer("#.#####", "----"));
				tmpComposer.getItem(LookUp.Area).maxSize *= 2;
			}
		}
		tmpComposer.addAttribute(LookUp.Source, String.class, "Source", null);
		tmpComposer.getItem(LookUp.Source).defaultSize *= 4;

		tmpComposer.setEditor(LookUp.IsVisible, new BooleanCellEditor());
		tmpComposer.setRenderer(LookUp.IsVisible, new BooleanCellRenderer());
		tmpComposer.setEditor(LookUp.Color, new ColorCellEditor());
		tmpComposer.setRenderer(LookUp.Color, new ColorCellRenderer(false));
		tmpComposer.setEditor(LookUp.Name, new DefaultCellEditor(new JTextField()));
		tmpComposer.setEditor(LookUp.Label, new DefaultCellEditor(new JTextField()));
		tmpComposer.setRenderer(LookUp.Source, new SourceRenderer());
		tmpComposer.getItem(LookUp.Id).maxSize *= 3;
		tmpComposer.getItem(LookUp.Name).defaultSize *= 2;
		tmpComposer.getItem(LookUp.Label).defaultSize *= 2;

		ItemHandler<G1> tmpIH = new StructureItemHandler<>(refStructureManager, tmpComposer);
		ItemProcessor<G1> tmpIP = refStructureManager;
		itemILP = new ItemListPanel<>(tmpIH, tmpIP, true);
		itemILP.setSortingEnabled(true);

		JTable structureTable = itemILP.getTable();
		structureTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		structureTable.addMouseListener(new TablePopupHandler(refStructureManager, popupMenu));
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

		fontSizeL = new JLabel("Font Size:");
		fontSizeNFS = new GNumberFieldSlider(this, new DecimalFormat("0"), FontSizeRange);
		fontSizeNFS.setIntegralSteps();
		fontSizeNFS.setNumColumns(3);
		add(fontSizeL, "sg g6,span,split");
		add(fontSizeNFS, "growx,sg g7,wrap");

		lineWidthL = new JLabel("Line Width:");
		lineWidthNFS = new GNumberFieldSlider(this, new DecimalFormat("0"), LineWidthRange);
		lineWidthNFS.setIntegralSteps();
		lineWidthNFS.setNumColumns(3);

		// Create the pointDiameterPanel if the StructureManager is of type PointModel
		PointDiameterPanel pointDiameterPanel = null;
		if (refStructureManager instanceof PointModel)
			pointDiameterPanel = new PointDiameterPanel((PointModel) refStructureManager);

		// Either the lineWidthNFS or the pointDiameterPanel will be visible
		if (pointDiameterPanel != null)
			add(pointDiameterPanel, "span");
		else
		{
			add(lineWidthL, "sg g6,span,split");
			add(lineWidthNFS, "growx,sg g7");
		}

		updateControlGui();

		// Register for events of interest
		PickUtil.autoDeactivatePickerWhenComponentHidden(refPickManager, refPicker, this);
		refStructureManager.addListener(this);
		refPickManager.addListener(this);
		refPickManager.getDefaultPicker().addListener(this);
	}

	/**
	 * Returns the file associated with the {@link Structure}s.
	 */
	public File getStructureFile()
	{
		return structuresFile;
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
			Color defColor = pickL.get(0).getLabelFontAttr().getColor();
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
			Color defColor = refStructureManager.getColor(pickL.get(0));
			Color tmpColor = JColorChooser.showDialog(this, "Color Chooser Dialog", defColor);
			if (tmpColor == null)
				return;

			refStructureManager.setColor(pickL, tmpColor);
		}
		else if (source == structHideB)
			refStructureManager.setIsVisible(pickL, false);
		else if (source == structShowB)
			refStructureManager.setIsVisible(pickL, true);
		else if (source == changeOffsetB)
		{
			// Lazy init
			if (changeOffsetDialog == null)
			{
				changeOffsetDialog = new NormalOffsetChangerDialog((Model) refStructureManager);
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

			// Scroll only if the selection source was not from our refStructureManager
			if (aSource != refStructureManager)
			{
				G1 tmpItem = null;
				if (currPickS.size() > 0)
					tmpItem = currPickS.asList().get(currPickS.size() - 1);

				// Scroll only if the previous pickL does not contains all of
				// selected items. This means that an item was deselected and
				// we should not scroll on deselections.
				if (prevPickS.containsAll(currPickS) == false)
					itemILP.scrollToItem(tmpItem);

				prevPickS = currPickS;
			}

			// Switch to the appropriate activation structure (if necessary)
			boolean isEditMode = refPicker == refPickManager.getActivePicker();
			if (isEditMode == true)
				updateActivatedItem();
		}

		updateControlGui();
	}

	@Override
	public void handlePickAction(InputEvent aEvent, PickMode aMode, PickTarget aPrimaryTarg, PickTarget aSurfaceTarg)
	{
		// Bail if we are are not associated with the primary PickTarget
		if (StructureGuiUtil.isAssociatedPickTarget(aPrimaryTarg, refStructureManager) == false)
			return;

		// Bail if not a valid popup action
		if (PickUtil.isPopupTrigger(aEvent) == false || aMode != PickMode.ActiveSec)
			return;

		// Show the popup (if appropriate)
		Component tmpComp = aEvent.getComponent();
		int posX = ((MouseEvent) aEvent).getX();
		int posY = ((MouseEvent) aEvent).getY();
		popupMenu.show(tmpComp, posX, posY);
	}

	@Override
	public void pickerChanged()
	{
		boolean tmpBool = refPicker == refPickManager.getActivePicker();
		editB.setSelected(tmpBool);

		// Update the activated structure
		updateActivatedItem();
		updateControlGui();
	}

	/**
	 * Helper method that handles the create action.
	 */
	private void doActionCreate()
	{
		refStructureManager.setSelectedItems(ImmutableList.of());

		ControlPointsPicker<?> workPicker = (ControlPointsPicker<?>) refPicker;
		workPicker.startNewItem();

		refPickManager.setActivePicker(refPicker);
	}

	/**
	 * Helper method that handles the edit action.
	 */
	private void doActionEdit()
	{
		boolean isEditMode = editB.isSelected();

		// Switch to the proper picker
		Picker tmpPicker = null;
		if (isEditMode == true)
			tmpPicker = refPicker;
		refPickManager.setActivePicker(tmpPicker);
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

		// Remove the structures
		refStructureManager.removeItems(pickS);

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
	 * Helper method that updates the manager to reflect the activated item.
	 * <p>
	 * Note: Currently not all {@link StructureManager}s support activation.
	 */
	@SuppressWarnings("unchecked")
	private void updateActivatedItem()
	{
		// Bail if the StructureManager does not support ControlPoints
		if (refStructureManager instanceof ControlPointsHandler == false)
			return;

		// Activate a structure if the following is true:
		// - We are in edit mode
		// - There is only 1 selected structure
		boolean isEditMode = refPicker == refPickManager.getActivePicker();

		G1 tmpItem = null;
		ImmutableSet<G1> currPickS = refStructureManager.getSelectedItems();
		if (currPickS.size() == 1 && isEditMode == true)
			tmpItem = currPickS.asList().get(0);

		((ControlPointsHandler<G1>) refStructureManager).setActivatedItem(tmpItem);
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
			Color tmpColor = pickL.get(0).getLabelFontAttr().getColor();
			for (G1 aItem : pickL)
				isMixed |= Objects.equals(tmpColor, aItem.getLabelFontAttr().getColor()) == false;

			if (isMixed == false)
				labelIcon = new ColorIcon(tmpColor, Color.BLACK, iconW, iconH);
			else
				labelIcon = new ColorIcon(Color.LIGHT_GRAY, Color.GRAY, iconW, iconH);

			// Determine structure color attributes
			isMixed = false;
			tmpColor = refStructureManager.getColor(pickL.get(0));
			for (G1 aItem : pickL)
				isMixed |= Objects.equals(tmpColor, refStructureManager.getColor(aItem)) == false;

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
		labelTitleL.setEnabled(isEnabled);
		labelColorB.setEnabled(isEnabled);
		structTitleL.setEnabled(isEnabled);
		structColorB.setEnabled(isEnabled);

		isEnabled = refStructureManager.supportsActivation() == false | cntFullItems > 0;
		editB.setEnabled(isEnabled);

		isEnabled = cntFullItems > 0 && cntPickItems < cntFullItems;
		selectAllB.setEnabled(isEnabled);

		int numItemsVisibleStruct = 0;
		int numItemsVisibleLabel = 0;
		for (G1 aItem : pickL)
		{
			if (aItem.getVisible() == true)
				numItemsVisibleStruct++;
			if (aItem.getLabelFontAttr().getIsVisible() == true)
				numItemsVisibleLabel++;
		}

		isEnabled = cntPickItems > 0;
		labelHideB.setEnabled(isEnabled && numItemsVisibleLabel > 0);
		labelShowB.setEnabled(isEnabled && numItemsVisibleLabel < pickL.size());
		structHideB.setEnabled(isEnabled && numItemsVisibleStruct > 0);
		structShowB.setEnabled(isEnabled && numItemsVisibleStruct < pickL.size());
		updateColoredButtons();

		isEnabled = cntPickItems > 0;
		fontSizeL.setEnabled(isEnabled);
		fontSizeNFS.setEnabled(isEnabled);

		int fontSize = -1;
		if (cntPickItems > 0)
			fontSize = pickL.get(0).getLabelFontAttr().getSize();
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

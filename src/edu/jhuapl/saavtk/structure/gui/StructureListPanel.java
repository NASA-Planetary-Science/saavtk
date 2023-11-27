package edu.jhuapl.saavtk.structure.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.table.ColorCellEditor;
import edu.jhuapl.saavtk.gui.table.ColorCellRenderer;
import edu.jhuapl.saavtk.gui.table.SourceRenderer;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.Ellipse;
import edu.jhuapl.saavtk.structure.Point;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.gui.load.AmbiguousRoundStructurePanel;
import edu.jhuapl.saavtk.structure.gui.load.LoadPanel;
import edu.jhuapl.saavtk.structure.gui.load.SavePanel;
import edu.jhuapl.saavtk.structure.io.StructureLoadUtil;
import glum.gui.GuiUtil;
import glum.gui.action.PopupMenu;
import glum.gui.misc.BooleanCellEditor;
import glum.gui.misc.BooleanCellRenderer;
import glum.gui.panel.itemList.ItemListPanel;
import glum.gui.panel.itemList.query.QueryComposer;
import glum.gui.table.NumberRenderer;
import glum.gui.table.TablePopupHandler;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;
import glum.item.ItemManagerUtil;
import net.miginfocom.swing.MigLayout;

/**
 * Panel used to display a list of {@link Structure}s.
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
public class StructureListPanel extends JPanel implements ActionListener, ItemEventListener
{
	// Ref vars
	private final AnyStructureManager refManager;
	private final PolyhedralModel refSmallBody;

	// State vars
	private ImmutableSet<Structure> prevPickS;

	// GUI vars
	private LoadPanel loadPanel;
	private SavePanel savePanel;

	private final JLabel sourceValueL;
	private ItemListPanel<Structure> itemILP;

	private final JLabel titleL;
	private final JButton loadB, saveB;
	private final JButton itemDelB;
	private final JButton selectAllB, selectInvertB, selectNoneB;

	/** Standard Constructor */
	public StructureListPanel(AnyStructureManager aManager, PolyhedralModel aSmallBody, StatusNotifier aStatusNotifier,
			PopupMenu<?> aPopupMenu)
	{
		refManager = aManager;
		refSmallBody = aSmallBody;

		prevPickS = ImmutableSet.of();

		// Form the gui
		loadPanel = null;
		savePanel = null;

		setLayout(new MigLayout("ins 0", "[]", "[]"));

		// Load, Save, File info area
		var loadEsriB = StructureGuiUtil.formEsriLoadButton(aSmallBody, aStatusNotifier, refManager, this);

		loadB = GuiUtil.createJButton("Load", this);
		saveB = GuiUtil.createJButton("Save", this);
		var sourceL = new JLabel("Source: ");
		sourceValueL = new JLabel("<no file loaded>");
		add(sourceL, "span,split");
		add(sourceValueL, "growx,pushx,w 100:100:");
		add(loadEsriB, "");
		add(loadB, "sg g0");
		add(saveB, "sg g0,wrap");

		// Table header
		titleL = new JLabel("Structures: 0");

		itemDelB = GuiUtil.createJButton(IconUtil.getItemDel(), this, ToolTipUtil.getItemDel());
		selectInvertB = GuiUtil.formButton(this, IconUtil.getSelectInvert());
		selectInvertB.setToolTipText(ToolTipUtil.getSelectInvert());

		selectNoneB = GuiUtil.formButton(this, IconUtil.getSelectNone());
		selectNoneB.setToolTipText(ToolTipUtil.getSelectNone());

		selectAllB = GuiUtil.formButton(this, IconUtil.getSelectAll());
		selectAllB.setToolTipText(ToolTipUtil.getSelectAll());

		add(titleL, "growx,span,split");
		add(itemDelB, "gapleft 32,w 24!,h 24!");
		add(selectInvertB, "gapleft 32,w 24!,h 24!");
		add(selectNoneB, "w 24!,h 24!");
		add(selectAllB, "w 24!,h 24!,wrap 2");

		// Table content
		var tmpComposer = new QueryComposer<LookUp>();
		tmpComposer.addAttribute(LookUp.IsVisible, Boolean.class, "Show", 40);
		tmpComposer.addAttribute(LookUp.Color, Color.class, "Color", 50);
		tmpComposer.addAttribute(LookUp.Id, Integer.class, "Id", "98");
		tmpComposer.addAttribute(LookUp.Type, String.class, "Type", "Polygon");
		tmpComposer.addAttribute(LookUp.Name, String.class, "Name", null);
		tmpComposer.addAttribute(LookUp.Label, String.class, "Label", null);

		var maxStr = "Diam.: km"; // "9,876.987"
		tmpComposer.addAttribute(LookUp.Diameter, Double.class, "Diam: km", maxStr);
		tmpComposer.setRenderer(LookUp.Diameter, new NumberRenderer("#.####", "---"));
		tmpComposer.getItem(LookUp.Diameter).maxSize *= 2;

		tmpComposer.addAttribute(LookUp.Angle, Double.class, "Angle", maxStr);
		tmpComposer.setRenderer(LookUp.Angle, new NumberRenderer("#.###", "---"));
		tmpComposer.addAttribute(LookUp.Flattening, Double.class, "Flat.", "0.2020");
		tmpComposer.setRenderer(LookUp.Flattening, new NumberRenderer("#.####", "---"));
		tmpComposer.getItem(LookUp.Flattening).maxSize *= 2;

		maxStr = "Len.: km"; // "9,876.987"
		tmpComposer.addAttribute(LookUp.Length, Double.class, "Length: km", maxStr, true);
		tmpComposer.setRenderer(LookUp.Length, new NumberRenderer("#.#####", "----"));
		tmpComposer.getItem(LookUp.Length).maxSize *= 2;
		tmpComposer.addAttribute(LookUp.VertexCount, Integer.class, "# Pts", "999");
		tmpComposer.getItem(LookUp.VertexCount).maxSize *= 2;

		var tmpStr = "Area: km" + (char) 0x00B2;
		tmpComposer.addAttribute(LookUp.Area, Double.class, tmpStr, maxStr, true);
		tmpComposer.setRenderer(LookUp.Area, new NumberRenderer("#.#####", "----"));
		tmpComposer.getItem(LookUp.Area).maxSize *= 2;

		tmpComposer.addAttribute(LookUp.Source, String.class, "Source", null, true);
		tmpComposer.getItem(LookUp.Source).defaultSize *= 4;

		tmpComposer.setEditor(LookUp.IsVisible, new BooleanCellEditor());
		tmpComposer.setRenderer(LookUp.IsVisible, new BooleanCellRenderer());
		tmpComposer.setEditor(LookUp.Color, new ColorCellEditor());
		tmpComposer.setRenderer(LookUp.Color, new ColorCellRenderer(false));
		tmpComposer.setEditor(LookUp.Name, new DefaultCellEditor(new JTextField()));
		tmpComposer.setEditor(LookUp.Label, new DefaultCellEditor(new JTextField()));
		tmpComposer.setRenderer(LookUp.Source, new SourceRenderer());
		tmpComposer.getItem(LookUp.Name).defaultSize *= 2;
		tmpComposer.getItem(LookUp.Label).defaultSize *= 2;

		var tmpIH = new StructureItemHandler<>(refManager, tmpComposer);
		var tmpIP = refManager;
		itemILP = new ItemListPanel<>(tmpIH, tmpIP, true);
		itemILP.setSortingEnabled(true);

		var structureTable = itemILP.getTable();
		structureTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		structureTable.addMouseListener(new TablePopupHandler(refManager, aPopupMenu));
		add(new JScrollPane(structureTable), "growx,growy,pushy,span,wrap");

		// Specialized code to handle deletion via the keyboard
		structureTable.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e)
			{
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE)
					doActionItemDel();
			}
		});

		updateGui();

		// Register for events of interest
		refManager.addListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		var source = actionEvent.getSource();
		if (source == loadB)
			doActionLoad();
		else if (source == saveB)
			doActionSave();
		else if (source == itemDelB)
			doActionItemDel();
		else if (source == selectAllB)
			ItemManagerUtil.selectAll(refManager);
		else if (source == selectNoneB)
			ItemManagerUtil.selectNone(refManager);
		else if (source == selectInvertB)
			ItemManagerUtil.selectInvert(refManager);
		else
			throw new Error("Logic error. Unsupported source: " + source);

		updateGui();
	}

	@Override
	public void handleItemEvent(Object aSource, ItemEventType aEventType)
	{
		if (aEventType == ItemEventType.ItemsSelected)
		{
			var currPickS = refManager.getSelectedItems();

			// Scroll only if the selection source was not from our refStructureManager
			if (aSource != refManager)
			{
				var tmpItem = (Structure) null;
				if (currPickS.size() > 0)
					tmpItem = currPickS.asList().get(currPickS.size() - 1);

				// Scroll only if the previous pickL does not contains all of
				// selected items. This means that an item was deselected and
				// we should not scroll on deselections.
				if (prevPickS.containsAll(currPickS) == false)
					itemILP.scrollToItem(tmpItem);

				prevPickS = currPickS;
			}
		}

		updateGui();
	}

	/**
	 * Helper method to handle the item delete action.
	 */
	private void doActionItemDel()
	{
		// Delegate
		var pickItemS = refManager.getSelectedItems();
		StructureGuiUtil.promptAndDelete(this, refManager, pickItemS);
	}

	/**
	 * Helper method that handles the load action.
	 */
	private void doActionLoad()
	{
		// Prompt the user for files to load
		var fileArr = CustomFileChooser.showOpenDialog(this, "Select Structure Files", null, true);
		if (fileArr == null)
			return;
		var tmpFileL = Arrays.asList(fileArr);
		tmpFileL.sort(null);

		// Load the files
		var failM = new LinkedHashMap<File, Exception>();

		List<Structure> fullItemL = new ArrayList<>();
		for (var aFile : tmpFileL)
		{
			try
			{
				var tmpItemL = StructureLoadUtil.loadStructures(aFile);
				fullItemL.addAll(tmpItemL);
			}
			catch (IOException aExp)
			{
				aExp.printStackTrace();
				failM.put(aFile, aExp);
			}
		}

		// Check for ambiguous structures and transform to non-ambiguous structures
		fullItemL = handleAmbiguousStructuresOrAbort(fullItemL);
		if (fullItemL == null)
			return;

		// Lazy init
		if (loadPanel == null)
			loadPanel = new LoadPanel(this, refSmallBody, refManager);

		// Prompt the user for how to load the structures
		loadPanel.setStructuresToLoad(fullItemL);
		loadPanel.setVisibleAsModal();

		updateFileLabelUI();
	}

	/**
	 * Helper method that handles the save action.
	 */
	private void doActionSave()
	{
		// Lazy init
		if (savePanel == null)
			savePanel = new SavePanel(this, refSmallBody, refManager);

		var fullItemL = refManager.getAllItems();
		var pickItemS = refManager.getSelectedItems();

		savePanel.setStructuresToSave(fullItemL, pickItemS);
		savePanel.setVisibleAsModal();
	}

	/**
	 * Helper method that will determine if there are ambiguous and if so prompt the user for how to handle them. If
	 * abort is canceled then null will be returned and the load process should be aborted.
	 */
	private List<Structure> handleAmbiguousStructuresOrAbort(List<Structure> aItemL)
	{
		// Determine the ambiguous structures (source file to structure)
		Multimap<Object, Structure> ambigouseMM = LinkedListMultimap.create();
		for (Structure aStructure : aItemL)
		{
			if (aStructure instanceof Ellipse && aStructure.getType() == null)
				ambigouseMM.put(aStructure.getSource(), aStructure);
		}

		// Bail since there is no ambiguous structures
		if (ambigouseMM.size() == 0)
			return aItemL;

		// Prompt the user for how to handle the ambiguous structures.
		var tmpPrompPanel = new AmbiguousRoundStructurePanel(this, 550, 225);
		tmpPrompPanel.setAmbigousMap(ambigouseMM);
		tmpPrompPanel.setVisibleAsModal();

		// Bail if no mode selected
		var pickType = tmpPrompPanel.getSelection();
		if (pickType == null)
			return null;

		// Transform the ambiguous Structures with the user's selection
		var retItemL = new ArrayList<Structure>();
		for (var aItem : aItemL)
		{
			// Skip to next if not an ellipse
			if (aItem instanceof Ellipse == false)
			{
				retItemL.add(aItem);
				continue;
			}

			// Skip to next if not an (ambiguous) ellipse
			var tmpItem = (Ellipse) aItem;
			if (tmpItem.getType() != null)
			{
				retItemL.add(tmpItem);
				continue;
			}

			var newItem = switch (pickType)
			{
				case Point:
					yield new Point(tmpItem.getId(), tmpItem.getSource(), tmpItem.getCenter(), tmpItem.getColor());
				default:
					yield new Ellipse(tmpItem.getId(), tmpItem.getSource(), pickType, tmpItem.getCenter(),
							tmpItem.getRadius(), tmpItem.getAngle(), tmpItem.getFlattening(), tmpItem.getColor());
			};

			newItem.setLabel(aItem.getLabel());
			newItem.setName(aItem.getName());
			retItemL.add(newItem);
		}

		return retItemL;
	}

	/**
	 * Helper method that keeps various UI elements synchronized.
	 */
	private void updateGui()
	{
		// Update various buttons
		var cntFullItems = refManager.getNumItems();
		var isEnabled = cntFullItems > 0;
		selectInvertB.setEnabled(isEnabled);

		var pickL = refManager.getSelectedItems().asList();
		var cntPickItems = pickL.size();
		isEnabled = cntPickItems > 0;
		selectNoneB.setEnabled(isEnabled);
		itemDelB.setEnabled(isEnabled);

		isEnabled = cntFullItems > 0 && cntPickItems < cntFullItems;
		selectAllB.setEnabled(isEnabled);

		// Table title
		var cntFormat = new DecimalFormat("#,###");
		var infoStr = "Structures: " + cntFormat.format(cntFullItems);
		if (cntPickItems > 0)
			infoStr += "  (Selected: " + cntFormat.format(cntPickItems) + ")";

		titleL.setText(infoStr);
	}

	/**
	 * Helper method responsible for updating the fileL UI element
	 */
	private void updateFileLabelUI()
	{
		// Retrieve the list of all source files
		var tmpFileS = new HashSet<File>();
		for (var aStructure : refManager.getAllItems())
		{
			var source = aStructure.getSource();
			if (source instanceof File)
				tmpFileS.add((File) source);
		}

		// Update the UI
		var priStr = "<no file loaded>";
		var secStr = (String) null;
		if (tmpFileS.size() == 1)
		{
			var tmpFile = tmpFileS.iterator().next();
			priStr = tmpFile.getName();
			secStr = tmpFile.getAbsolutePath();
		}
		else if (tmpFileS.size() > 1)
		{
			priStr = "Multiple Files: " + tmpFileS.size();
		}
		sourceValueL.setText(priStr);
		sourceValueL.setToolTipText(secStr);
	}

}

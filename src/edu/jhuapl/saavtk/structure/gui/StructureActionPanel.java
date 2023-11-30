package edu.jhuapl.saavtk.structure.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.pick.PickManager;
import edu.jhuapl.saavtk.pick.PickManagerListener;
import edu.jhuapl.saavtk.pick.Picker;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.AnyStructurePicker;
import edu.jhuapl.saavtk.structure.AnyStructurePicker.Mode;
import edu.jhuapl.saavtk.structure.gui.misc.create.CreatePanel;
import edu.jhuapl.saavtk.structure.gui.misc.create.EditPanel;
import edu.jhuapl.saavtk.structure.gui.misc.view.ViewPanel;
import glum.gui.GuiUtil;
import glum.gui.panel.CardPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that provides the "action" area of a the structure UI.
 * <p>
 * The following (child) action panels are supported:
 * <ul>
 * <li>{@link ViewPanel}
 * <li>{@link CreatePanel}
 * <li>{@link EditPanel}
 * </ul>
 *
 * @author lopeznr1
 */
public class StructureActionPanel extends JPanel implements ActionListener, PickManagerListener
{
	// Ref vars
	private final AnyStructureManager refStructureManager;
	private final AnyStructurePicker refPicker;
	private final PickManager refPickManager;

	// Gui vars
	private final JButton renderPropsB;
	private final JToggleButton itemAddTB, itemEditTB;

	private final CardPanel<JPanel> actionPanel;
	private final ViewPanel viewPanel;
	private final JPanel editPanel;
	private final CreatePanel createPanel;
	private final RenderAttrPanel renderAttrPanel;

	/** Standard Constructor */
	public StructureActionPanel(AnyStructureManager aStructureManager, Renderer aRenderer, PolyhedralModel aSmallBody,
			StatusNotifier aStatusNotifier, PickManager aPickManager, AnyStructurePicker aPicker)
	{
		refStructureManager = aStructureManager;
		refPickManager = aPickManager;
		refPicker = aPicker;

		// Form the gui
		renderAttrPanel = new RenderAttrPanel(this, aSmallBody);
		renderAttrPanel.addActionListener(this);

		setLayout(new MigLayout("ins 0", "[]", "[]"));
		createPanel = new CreatePanel(refPicker);
		editPanel = new EditPanel(refStructureManager, refPicker);
		viewPanel = new ViewPanel(aSmallBody, refStructureManager, refPicker);

		// Action area
		itemAddTB = GuiUtil.formToggleButton(this, IconUtil.getItemAddFalse(), IconUtil.getItemAddTrue());
		itemAddTB.setToolTipText(ToolTipUtil.getItemAdd());

		itemEditTB = GuiUtil.formToggleButton(this, IconUtil.getItemEditFalse(), IconUtil.getItemEditTrue());
		itemEditTB.setToolTipText(ToolTipUtil.getItemEdit());

		renderPropsB = GuiUtil.formButton(this, IconUtil.getActionConfig());
		renderPropsB.setToolTipText("Render Properties");

		var leftPanel = formLeftPanel();

		actionPanel = new CardPanel<JPanel>();
		actionPanel.addCard(viewPanel, viewPanel);
		actionPanel.addCard(createPanel, createPanel);
		actionPanel.addCard(editPanel, editPanel);

		add(leftPanel, "growy,span,split");
		add(GuiUtil.createDivider(), "growy,w 4!");
		add(actionPanel, "growx,growy,pushx,wrap");

		// Register for events of interest
		refPickManager.addListener(this);

		// Initial update
		updateActionPanel();
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		var source = actionEvent.getSource();
		if (source == itemAddTB || source == itemEditTB)
			doActionChangePicker(source);
		else if (source == renderPropsB)
			doActionShowRenderAttrPanel();
		else if (source == renderAttrPanel)
			doActionRenderAttrPanel();
		else
			throw new Error("Logic error. Unsupported source: " + source);

		updateActionPanel();
	}

	@Override
	public void pickerChanged()
	{
		updateActionPanel();
	}

	/**
	 * Helper method that handles the "Change Picker" action.
	 */
	private void doActionChangePicker(Object aSource)
	{
		var tmpMode = Mode.None;
		var tmpPicker = (Picker) null;
		if (aSource == itemAddTB && itemAddTB.isSelected() == true)
		{
			tmpMode = Mode.Create;
			tmpPicker = refPicker;
		}
		else if (aSource == itemEditTB && itemEditTB.isSelected() == true)
		{
			tmpMode = Mode.Edit;
			tmpPicker = refPicker;
		}

		refPicker.setWorkMode(tmpMode);
		refPickManager.setActivePicker(tmpPicker);
	}

	/**
	 * Helper method that handles the actions from the {@link RenderAttrPanel}.
	 */
	private void doActionRenderAttrPanel()
	{
		var renderAttr = renderAttrPanel.getAttr();
		refStructureManager.setRenderAttr(renderAttr);
	}

	/**
	 * Helper method that handles the action: "Show props panel"
	 */
	private void doActionShowRenderAttrPanel()
	{
		var renderAttr = refStructureManager.getRenderAttr();
		renderAttrPanel.setAttr(renderAttr);
		renderAttrPanel.setVisible(true);
	}

	/**
	 * Helper method that form the panel which contains controls for switching the actionPanel
	 */
	private JPanel formLeftPanel()
	{
		var retPanel = new JPanel(new MigLayout("ins 0", "", ""));

		retPanel.add(itemAddTB, "w 24!,h 24!,ay top,wrap");
		retPanel.add(itemEditTB, "w 24!,h 24!,wrap");

		retPanel.add(renderPropsB, "w 24!,h 24!,ay bottom,pushy");

		return retPanel;
	}

	/**
	 * Helper method that keeps the proper action panel displayed.
	 */
	private void updateActionPanel()
	{
		var tmpPanel = (JPanel) viewPanel;
		if (refPickManager.getActivePicker() == refPicker)
		{
			tmpPanel = createPanel;
			if (refPicker.getWorkMode() == Mode.Edit)
				tmpPanel = editPanel;
		}
		actionPanel.switchToCard(tmpPanel);

		updateGui();
	}

	/**
	 * Helper method that keeps various UI elements synchronized.
	 */
	private void updateGui()
	{
		var tmpBool = refPicker == refPickManager.getActivePicker();
		tmpBool &= refPicker.getWorkMode() == Mode.Create;
		itemAddTB.setSelected(tmpBool);

		tmpBool = refPicker == refPickManager.getActivePicker();
		tmpBool &= refPicker.getWorkMode() == Mode.Edit;
		itemEditTB.setSelected(tmpBool);
	}

}

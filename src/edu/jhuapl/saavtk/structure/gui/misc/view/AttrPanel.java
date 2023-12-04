package edu.jhuapl.saavtk.structure.gui.misc.view;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.common.collect.Range;

import edu.jhuapl.saavtk.gui.util.IconUtil;
import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.ClosedShape;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.gui.StructureGuiUtil;
import edu.jhuapl.saavtk.util.ColorIcon;
import glum.gui.GuiExeUtil;
import glum.gui.GuiUtil;
import glum.gui.component.GNumberField;
import glum.gui.component.GSlider;
import glum.gui.icon.EmptyIcon;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that provides UI elements to allow the user to edit a set of {@link Structure}s.
 *
 * @author lopeznr1
 */
public class AttrPanel extends JPanel implements ActionListener, ItemEventListener
{
	// Constants
	private static final Range<Double> FontSizeRange = Range.closed(8.0, 120.0);
	private static final int FontSizeDefault = 16;

	// Ref vars
	private final AnyStructureManager refManager;

	// Gui vars
	private final JCheckBox labelShowCB, extShowCB, intShowCB;
	private final JButton labelColorB, structColorB;
	private final JLabel fontSizeL;
	private final JButton fontSizeResetB;
	private final GNumberField fontSizeNF;
	private final GSlider fontSizeS;

	/** Standard Constructor */
	public AttrPanel(AnyStructureManager aManager)
	{
		refManager = aManager;

		labelShowCB = GuiUtil.createJCheckBox("Labels:", this);
		labelColorB = new JButton("");
		labelColorB.addActionListener(this);

		extShowCB = GuiUtil.createJCheckBox("Struct:", this);
		structColorB = new JButton("");
		structColorB.addActionListener(this);

		intShowCB = GuiUtil.createJCheckBox("Fill Interior", this);

		fontSizeResetB = GuiUtil.formButton(this, IconUtil.getActionReset());
		fontSizeResetB.setToolTipText(ToolTipUtil.getItemResetMsg("" + FontSizeDefault));
		fontSizeL = new JLabel("Font Size:");
		fontSizeNF = new GNumberField(this, new DecimalFormat("0"), FontSizeRange);
		fontSizeNF.setColumns(4);
		fontSizeS = new GSlider(this, FontSizeRange);
		fontSizeS.setNumSteps(113);

		// Form the gui
		setLayout(new MigLayout("ins 0", "[]", "[]"));

		var col1Panel = formPanelCol1();
		var col2Panel = formPanelCol2();
		add(col1Panel, "");
		add(col2Panel, "ay top");

		// Register for events of interest
		refManager.addListener(this);

		// Initial update
		GuiExeUtil.executeOnceWhenShowing(this, () -> updateGui());
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		var pickL = refManager.getSelectedItems().asList();

		var source = actionEvent.getSource();

		if (source == fontSizeResetB)
			doActionFontSizeReset(pickL);
		else if (source == fontSizeNF || source == fontSizeS)
			doActionFontSize(pickL, source);
		else if (source == labelColorB)
		{
			var defColor = pickL.get(0).getLabelFontAttr().getColor();
			var tmpColor = JColorChooser.showDialog(this, "Color Chooser Dialog", defColor);
			if (tmpColor == null)
				return;

			refManager.setLabelColor(pickL, tmpColor);
		}
		else if (source == labelShowCB)
			refManager.setLabelVisible(pickL, labelShowCB.isSelected());
		else if (source == structColorB)
		{
			var defColor = pickL.get(0).getColor();
			var tmpColor = JColorChooser.showDialog(this, "Color Chooser Dialog", defColor);
			if (tmpColor == null)
				return;

			refManager.setColor(pickL, tmpColor);
		}
		else if (source == extShowCB)
			refManager.setIsVisible(pickL, extShowCB.isSelected());
		else if (source == intShowCB)
			doActionInteriorShow(pickL);
		else
			throw new Error("Unsupported source: " + source);

		updateGui();
	}

	@Override
	public void handleItemEvent(Object aSource, ItemEventType aEventType)
	{
		updateGui();
	}

	/**
	 * Helper method that handles the fontSize action.
	 */
	private void doActionFontSize(List<Structure> aItemL, Object aSource)
	{
		if (aSource == fontSizeNF && fontSizeNF.isValidInput() == false)
			return;

		var fontSize = 16;
		if (aSource == fontSizeNF)
			fontSize = fontSizeNF.getValueAsInt(16);
		else if (aSource == fontSizeS)
			fontSize = (int) fontSizeS.getModelValue();

		refManager.setLabelFontSize(aItemL, fontSize);
	}

	/**
	 * Helper method that handles the fontSize reset action.
	 */
	private void doActionFontSizeReset(List<Structure> aItemL)
	{
		fontSizeNF.setValue(FontSizeDefault);
		fontSizeS.setModelValue(FontSizeDefault);
		refManager.setLabelFontSize(aItemL, FontSizeDefault);
	}

	/**
	 * Helper method that handles the action: "fill interior".
	 */
	private void doActionInteriorShow(List<Structure> aItemL)
	{
		var isShown = intShowCB.isSelected() == true;
		for (var aItem : aItemL)
		{
			if (aItem instanceof ClosedShape aClosedShape)
				aClosedShape.setShowInterior(isShown);
		}

		refManager.notifyItemsMutated(aItemL);
	}

	/**
	 * Helper method that returns a panel consisting of UI elements in visual column 1.
	 */
	private JPanel formPanelCol1()
	{
		var retPanel = new JPanel(new MigLayout("ins 0", "[]", "[]"));

		// Area: Exterior, structure color
		retPanel.add(extShowCB, "");
		retPanel.add(structColorB, "gapy 0,sgy g1,wrap");

		// Area: Label
		retPanel.add(labelShowCB, "");
		retPanel.add(labelColorB, "gapy 0,sgy g1,wrap");

		// Area: Font
		retPanel.add(GuiUtil.createDivider(), "growx,h 4!,span,wrap");
		retPanel.add(fontSizeResetB, "w 24!,h 24!,span,split");
		retPanel.add(fontSizeL, "");
		retPanel.add(fontSizeNF, "growx,span,wrap");
		retPanel.add(fontSizeS, "growx,span,wrap");

		return retPanel;
	}

	/**
	 * Helper method that returns a panel consisting of UI elements in visual column 2.
	 */
	private JPanel formPanelCol2()
	{
		var retPanel = new JPanel(new MigLayout("ins 0", "[]", "[]"));

		// Area: Interior
		retPanel.add(intShowCB, "");

		return retPanel;
	}

	/**
	 * Helper method to update the colored icons/text for labelIconB and structIconB
	 */
	private void updateColoredButtons()
	{
		// These values may need to be fiddled with if there are sizing issues
		var iconW = 60;
		var iconH = (int) (extShowCB.getHeight() * 0.50);

		// Update the label / struct colors
		Icon labelIcon = null;
		Icon structIcon = null;

		var pickL = refManager.getSelectedItems().asList();
		boolean isEnabled = pickL.size() > 0;
		if (isEnabled == true)
		{
			// Determine label color attributes
			var isMixed = false;
			var tmpColor = pickL.get(0).getLabelFontAttr().getColor();
			for (var aItem : pickL)
				isMixed |= Objects.equals(tmpColor, aItem.getLabelFontAttr().getColor()) == false;

			if (isMixed == false)
				labelIcon = new ColorIcon(tmpColor, Color.BLACK, iconW, iconH);
			else
				labelIcon = new ColorIcon(Color.LIGHT_GRAY, Color.GRAY, iconW, iconH);

			// Determine structure color attributes
			isMixed = false;
			tmpColor = pickL.get(0).getColor();
			for (var aItem : pickL)
				isMixed |= Objects.equals(tmpColor, aItem.getColor()) == false;

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
	 * Helper method that keeps various UI elements synchronized.
	 */
	private void updateGui()
	{
		var pickL = refManager.getSelectedItems();
		var cntPickItems = pickL.size();

		var isEnabled = cntPickItems > 0;
		GuiUtil.setEnabled(isEnabled, fontSizeL, fontSizeNF, fontSizeS);
		GuiUtil.setEnabled(isEnabled, labelColorB, labelShowCB);
		GuiUtil.setEnabled(isEnabled, structColorB, extShowCB);

		// Area: FontSize
		var fontSize = StructureGuiUtil.getUnifiedFontSizeFor(pickL);
		fontSizeNF.setValue(fontSize);
		fontSizeS.setModelValue(fontSize);

		isEnabled = cntPickItems > 0;
		isEnabled &= fontSize != FontSizeDefault;
		fontSizeResetB.setEnabled(isEnabled);

		// Area: Labels
		var isSelected = pickL.size() > 0;
		for (var aItem : pickL)
			isSelected &= aItem.getLabelFontAttr().getIsVisible() == true;
		labelShowCB.setSelected(isSelected);

		// Area: Show Structs
		isSelected = pickL.size() > 0;
		for (var aItem : pickL)
			isSelected &= aItem.getVisible() == true;
		extShowCB.setSelected(isSelected);

		// Area: Interior
		isEnabled = false;
		isSelected = true;
		for (var aItem : pickL)
		{
			if (aItem instanceof ClosedShape aClosedShape)
			{
				isEnabled = true;
				isSelected &= aClosedShape.getShowInterior() == true;
			}
		}
		isSelected &= isEnabled;

		GuiUtil.setEnabled(isEnabled, intShowCB);
		intShowCB.setSelected(isSelected);

		updateColoredButtons();
	}

}

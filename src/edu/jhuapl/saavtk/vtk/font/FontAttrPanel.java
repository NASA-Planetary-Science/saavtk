package edu.jhuapl.saavtk.vtk.font;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import glum.gui.GuiUtil;
import glum.gui.component.GComboBox;
import glum.gui.component.GNumberFieldSlider;
import glum.gui.panel.ColorInputPanel;
import glum.gui.panel.GPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that allows a user to configure a {@link FontAttr}.
 *
 * @author lopeznr1
 */
public class FontAttrPanel extends GPanel implements ActionListener
{
	// Constants
	private static final ImmutableList<String> FontFaceList = ImmutableList.of("Arial", "Courier", "Times");
	private static final Range<Double> FontSizeRange = Range.closed(8.0, 120.0);

	// Gui vars
	private final JCheckBox visibleCB;
	private final ColorInputPanel colorCIP;
	private final JLabel faceL, sizeL;
	private final GComboBox<String> faceBox;
	private final GNumberFieldSlider sizeNFS;
	private final JCheckBox boldCB, italicCB;

	/** Standard Constructor */
	public FontAttrPanel()
	{
		// Form the gui
		setLayout(new MigLayout("", "[]", "[]"));

		visibleCB = GuiUtil.createJCheckBox("Show", this);
		faceL = new JLabel("Font:");
		faceBox = new GComboBox<>(this, FontFaceList);
		add(visibleCB, "span,split");
		add(faceL, "gapleft 20");
		add(faceBox, "wrap");

		boldCB = GuiUtil.createJCheckBox("Bold", this);
		italicCB = GuiUtil.createJCheckBox("Italic", this);
		add(boldCB, "span,split");
		add(italicCB, "gapleft 20,wrap");

		sizeL = new JLabel("Size:");
		sizeNFS = new GNumberFieldSlider(this, new DecimalFormat("0"), FontSizeRange);
		sizeNFS.setIntegralSteps();
		add(sizeL, "span,split");
		add(sizeNFS, "growx,pushx,span,wrap");

		colorCIP = new ColorInputPanel(true, true, false);
		colorCIP.addActionListener(this);
		add(colorCIP, "growx,span");
	}

	/**
	 * Returns the {@link FontAttr} as configured in the GUI.
	 */
	public FontAttr getFontAttr()
	{
		String face = faceBox.getChosenItem();
		Color color = colorCIP.getColorConfig();
		int size = sizeNFS.getValueAsInt(FontSizeRange.lowerEndpoint().intValue());

		boolean isVisible = visibleCB.isSelected();
		boolean isBold = boldCB.isSelected();
		boolean isItalic = italicCB.isSelected();

		FontAttr retFA = new FontAttr(face, color, size, isVisible, isBold, isItalic);
		return retFA;
	}

	/**
	 * Configures the GUI to reflect the specified {@link FontAttr}.
	 */
	public void setFontAttr(FontAttr aAttr)
	{
		faceBox.setChosenItem(aAttr.getFace());
		colorCIP.setColorConfig(aAttr.getColor());
		sizeNFS.setValue(aAttr.getSize());

		visibleCB.setSelected(aAttr.getIsVisible());
		boldCB.setSelected(aAttr.getIsBold());
		italicCB.setSelected(aAttr.getIsItalic());

		updateGui();
	}

	/**
	 * Allows for hiding of the font size UI area. This is needed since some objects
	 * ignore the font size. Rather than leave an end user confused just "remove"
	 * the relevant components.
	 */
	public void setVisibleFontSizeUI(boolean aBool)
	{
		sizeL.setVisible(aBool);
		sizeNFS.setVisible(aBool);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		notifyListeners(this, 0);
		updateGui();
	}

	/**
	 * Helper method to keep the GUI synchronized.
	 */
	private void updateGui()
	{
		boolean isEnabled = visibleCB.isSelected() == true;
		GuiUtil.setEnabled(isEnabled, colorCIP, faceL, faceBox, sizeL, sizeNFS);
		GuiUtil.setEnabled(isEnabled, boldCB, italicCB);
	}

}

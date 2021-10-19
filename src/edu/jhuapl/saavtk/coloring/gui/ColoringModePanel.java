package edu.jhuapl.saavtk.coloring.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import edu.jhuapl.saavtk.coloring.ColoringMode;
import edu.jhuapl.saavtk.coloring.gui.standard.StandardPlateColorPanel;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import glum.gui.component.GComboBox;
import glum.gui.misc.CustomLCR;
import glum.gui.panel.CardPanel;
import glum.gui.panel.GPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that allows a user to configure the coloring mode on a small body.
 *
 * @author lopeznr1
 */
public class ColoringModePanel extends GPanel implements ActionListener
{
	// Gui vars
	private final GComboBox<ColoringMode> colorTypeBox;
	private final CardPanel<EditColoringModeGui> cardPanel;

	/**
	 * Standard Constructor
	 * <p>
	 * This constructor takes 4 combo boxes which are later passed to child panels.
	 * <p>
	 * The management of these combo boxes is completely external to the child
	 * panels. This is a defective design due to the poorly designed
	 * {@link PolyhedralModel} class.
	 */
	public ColoringModePanel(Renderer aRenderer, PolyhedralModel aSmallBody, JComboBox<String> aPropertyBox,
			JComboBox<String> aRedBox, JComboBox<String> aGreenBox, JComboBox<String> aBlueBox)
	{
		setLayout(new MigLayout("", "", ""));

		var tmpCustomLCR = new CustomLCR();
		tmpCustomLCR.addMapping(ColoringMode.Plain, "Plain");
		tmpCustomLCR.addMapping(ColoringMode.PlateColorStandard, "Plate Coloring: Standard");
		tmpCustomLCR.addMapping(ColoringMode.PlateColorRGB, "Plate Coloring: RGB");
		colorTypeBox = new GComboBox<>(this, ColoringMode.values());
		colorTypeBox.setRenderer(tmpCustomLCR);
		add(new JLabel("Coloring Mode:"), "");
		add(colorTypeBox, "pushx,wrap");

		cardPanel = new CardPanel<>();
		cardPanel.addCard(ColoringMode.Plain, new PlainColorPanel(aSmallBody));
		cardPanel.addCard(ColoringMode.PlateColorRGB, new RgbPlateColorPanel(aSmallBody, aRedBox, aGreenBox, aBlueBox));
		cardPanel.addCard(ColoringMode.PlateColorStandard, new StandardPlateColorPanel(aRenderer, aSmallBody, //
				aPropertyBox));
		cardPanel.switchToCard(ColoringMode.Plain);
		add(cardPanel, "growx,span,w 50::,wrap 0");
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		updateGui();
	}

	/**
	 * Helper method to keep the gui synchronized.
	 */
	private void updateGui()
	{
		// Deactivate the old panel
		var prevPanel = cardPanel.getActiveCard();
		prevPanel.deactivate();

		// Switch to the user selection and activate the panel
		var tmpType = colorTypeBox.getChosenItem();
		cardPanel.switchToCard(tmpType);

		var nextPanel = cardPanel.getActiveCard();
		nextPanel.activate(this);
	}

}

package edu.jhuapl.saavtk.color.gui.bar;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import glum.gui.component.GComboBox;
import glum.gui.panel.ColorInputPanel;
import glum.gui.panel.GPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that allows a user to configure a {@link BackgroundAttr}.
 *
 * @author lopeznr1
 */
public class BackgroundAttrPanel extends GPanel implements ActionListener
{
	// Gui vars
	private final GComboBox<ShowMode> modeBox;
	private final ColorInputPanel backgroundCIP;

	/** Standard Constructor */
	public BackgroundAttrPanel()
	{
		// Form the gui
		setLayout(new MigLayout("", "[]", "[]"));

		JLabel tmpL = new JLabel("Visible:");
		modeBox = new GComboBox<>(this, ShowMode.values());
		add(tmpL, "span,split");
		add(modeBox, "wrap");

		backgroundCIP = new ColorInputPanel(true, true, true);
		backgroundCIP.addActionListener(this);
		backgroundCIP.setBorder(new EmptyBorder(7, 7, 7, 7));
		add(backgroundCIP, "span,growx,pushx");
	}

	/**
	 * Returns the {@link BackgroundAttr} as configured in the GUI.
	 */
	public BackgroundAttr getAttr()
	{
		ShowMode mode = modeBox.getChosenItem();
		Color color = backgroundCIP.getColorConfig();

		BackgroundAttr retAttr = new BackgroundAttr(mode, color);
		return retAttr;
	}

	/**
	 * Configures the GUI to reflect the specified {@link BackgroundAttr}.
	 */
	public void setAttr(BackgroundAttr aAttr)
	{
		backgroundCIP.setColorConfig(aAttr.getColor());
		modeBox.setChosenItem(aAttr.getMode());
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		notifyListeners(this, 0);
	}

}

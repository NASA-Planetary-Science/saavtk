package edu.jhuapl.saavtk.color.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.color.provider.ConstGroupColorProvider;
import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
import edu.jhuapl.saavtk.color.provider.SimpleColorProvider;
import edu.jhuapl.saavtk.util.ColorIcon;
import glum.gui.GuiUtil;
import net.miginfocom.swing.MigLayout;

/**
 * {@link EditGroupColorPanel} that provides simplistic color configuration of
 * data.
 * <p>
 * The configuration options are:
 * <ul>
 * <li>A single color for all data.
 * </ul>
 *
 * @author lopeznr1
 */
public class SimplePanel extends JPanel implements ActionListener, EditGroupColorPanel
{
	// Reference vars
	private final ActionListener refListener;

	// Gui vars
	private JLabel colorL;
	private JButton colorB;

	// State vars
	private Color color;

	/**
	 * Standard Constructor
	 *
	 * @param aListener Listener to be notified of any state change.
	 * @param aLabel    UI textual label for color control.
	 * @param aColor    Initial color the color control will be configured to.
	 */
	public SimplePanel(ActionListener aListener, String aLabel, Color aColor)
	{
		refListener = aListener;

		// Setup the GUI
		setLayout(new MigLayout("", "[]", ""));

		colorL = new JLabel(aLabel, JLabel.RIGHT);
		colorB = GuiUtil.formButton(this, "");
		add(colorL, "");
		add(colorB, "");

		color = aColor;

		updateGui();
	}

	/**
	 * Sets in the installed colors.
	 */
	public void setColor(Color aColor)
	{
		color = aColor;
		updateGui();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		// Process the event
		Object source = aEvent.getSource();
		if (source == colorB)
			doUpdateColor();

		// Notify our refListener
		refListener.actionPerformed(new ActionEvent(this, 0, ""));
	}

	@Override
	public void activate(boolean aIsActive)
	{
		updateGui();
	}

	@Override
	public GroupColorProvider getGroupColorProvider()
	{
		return new ConstGroupColorProvider(new SimpleColorProvider(color));
	}

	/**
	 * Helper method that handles the action for colorB
	 */
	private void doUpdateColor()
	{
		// Prompt the user for a color
		Color tmpColor = JColorChooser.showDialog(this, "Color Chooser Dialog", color);
		if (tmpColor == null)
			return;
		color = tmpColor;

		updateGui();
	}

	/**
	 * Helper method that will update the UI to reflect the user selected colors.
	 */
	private void updateGui()
	{
		int iconW = (int) (colorL.getWidth() * 0.60);
		int iconH = (int) (colorL.getHeight() * 0.80);

		// Update the color icon
		Icon tmpIcon = new ColorIcon(color, Color.BLACK, iconW, iconH);
		colorB.setIcon(tmpIcon);
	}

}

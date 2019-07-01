package edu.jhuapl.saavtk.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;

/**
 * Collection of AWT/Swing utilities.
 */
public class GuiUtil
{
	/**
	 * Utility helper method to create a JButton with the specified configuration.
	 * 
	 * @param aListener A Listener registered with the JButton.
	 * @param aTitle    The text title of the JButton.
	 */
	public static JButton formButton(ActionListener aListener, String aTitle)
	{
		JButton retB = new JButton();
		retB.addActionListener(aListener);
		retB.setText(aTitle);
		return retB;
	}

	/**
	 * Utility helper method to create a JButton with the specified configuration.
	 * 
	 * @param aListener A Listener registered with the JButton.
	 * @param aImage    The image to be used as an icon.
	 * @param aToolTip  The tool tip associated with the JButton.
	 */
	public static JButton formButton(ActionListener aListener, BufferedImage aImage, String aToolTip)
	{
		Icon tmpIcon = new ImageIcon(aImage);

		JButton retB = new JButton();
		retB.setIcon(tmpIcon);
		retB.addActionListener(aListener);
		retB.setToolTipText(aToolTip);

		return retB;
	}

	/**
	 * Utility helper method to create a JToggleButton with the specified
	 * configuration.
	 * 
	 * @param aListener A Listener registered with the JButton.
	 * @param aPriImage The image to be used as the primary icon.
	 * @param aSecImage The image to be used when the JToggleButton is selected.
	 * @param aToolTip  The tool tip associated with the JToggleButton.
	 */
	public static JToggleButton formToggleButton(ActionListener aListener, Image aPriImage, Image aSecImage,
			String aToolTip)
	{
		Icon priIcon = new ImageIcon(aPriImage);
		Icon secIcon = new ImageIcon(aSecImage);

		JToggleButton retTB = new JToggleButton(priIcon, false);
		retTB.setSelectedIcon(secIcon);
		retTB.setToolTipText(aToolTip);
		retTB.addActionListener(aListener);

		return retTB;
	}

	/**
	 * Utility method to recursively change the enable state of all Components
	 * contained by the specified Container.
	 * 
	 * @param aContainer The Container of interest.
	 * @param aBool      Boolean used to define the enable state.
	 */
	public static void setEnabled(Container aContainer, boolean aBool)
	{
		for (Component aComp : aContainer.getComponents())
		{
			aComp.setEnabled(aBool);
			if (aComp instanceof Container)
				setEnabled((Container) aComp, aBool);
		}
	}

	/**
	 * Utility method to ensure the proper cursor is configured.
	 * <P>
	 * The Component's cursor will only be changed if it does not already match the
	 * specified cursor type.
	 */
	public static void updateCursor(Component aComp, int aCursorType)
	{
		// Switch to the proper cursor (if necessary)
		if (aComp.getCursor().getType() != aCursorType)
			aComp.setCursor(new Cursor(aCursorType));
	}

}

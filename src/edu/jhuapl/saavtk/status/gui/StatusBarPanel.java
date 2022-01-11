package edu.jhuapl.saavtk.status.gui;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import edu.jhuapl.saavtk.status.StatusNotifier;
import net.miginfocom.swing.MigLayout;

/**
 * Implementation of {@link StatusNotifier} where status updates are sent to a
 * status bar panel. This component is typically installed at the bottom of a
 * window.
 * <P>
 * Primary notification is sent to the left side and secondary notification is
 * sent to the right side.
 *
 * @author lopeznr1
 */
public class StatusBarPanel extends JPanel implements StatusNotifier
{
	// Gui vars
	private final JTextArea eastTA, westTA;

	/** Standard Constructor */
	public StatusBarPanel()
	{
		// Set up the GUI
		setLayout(new MigLayout("", "0[]0", "0[]0"));

		westTA = formTextArea();
		eastTA = formTextArea();

		var splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westTA, eastTA);
		splitPane.setDividerLocation(0.5);
		splitPane.setResizeWeight(0.5);
		splitPane.setContinuousLayout(true);
		add(splitPane);
		setPreferredSize(new Dimension(700, splitPane.getPreferredSize().height));

		// Switch to a monospaced font
		var tmpFont = westTA.getFont();
		tmpFont = new Font(Font.MONOSPACED, tmpFont.getStyle(), tmpFont.getSize());
		setFont(tmpFont, tmpFont);
	}

	/**
	 * Sets the font to be used by the primary and secondary areas.
	 */
	public void setFont(Font aPriFont, Font aSecFont)
	{
		westTA.setFont(aPriFont);
		eastTA.setFont(aSecFont);
	}

	@Override
	public void setPriStatus(String aBriefMsg, String aDetailMsg)
	{
		westTA.setText(aBriefMsg);
		westTA.setToolTipText(aDetailMsg);
	}

	@Override
	public void setSecStatus(String aBriefMsg, String aDetailMsg)
	{
		eastTA.setText(aBriefMsg);
		eastTA.setToolTipText(aDetailMsg);
	}

	/**
	 * Helper method to form a {@link JTextArea} that will be used as a uneditable
	 * info label.
	 */
	private JTextArea formTextArea()
	{
		var retTA = new JTextArea(1, 7500);
		retTA.setEditable(false);

		var tmpDim = retTA.getPreferredSize();
		retTA.setMinimumSize(new Dimension(40, tmpDim.height));
		retTA.setMaximumSize(new Dimension(7500, 200));
		return retTA;
	}

}

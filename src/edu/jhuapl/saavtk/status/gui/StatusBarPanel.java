package edu.jhuapl.saavtk.status.gui;

import java.awt.Dimension;

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
	private final JTextArea eastL, westL;

	/** Standard Constructor */
	public StatusBarPanel()
	{
		// Set up the GUI
		setLayout(new MigLayout("", "0[]0", "0[]0"));

		westL = formTextArea();
		eastL = formTextArea();

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westL, eastL);
		splitPane.setDividerLocation(0.5);
		splitPane.setResizeWeight(0.5);
		splitPane.setContinuousLayout(true);
		add(splitPane);
		setPreferredSize(new Dimension(700, splitPane.getPreferredSize().height));
	}

	@Override
	public void setPriStatus(String aBriefMsg, String aDetailMsg)
	{
		westL.setText(aBriefMsg);
		westL.setToolTipText(aDetailMsg);
	}

	@Override
	public void setSecStatus(String aBriefMsg, String aDetailMsg)
	{
		eastL.setText(aBriefMsg);
		eastL.setToolTipText(aDetailMsg);
	}

	/**
	 * Helper method to form a {@link JTextArea} that will be used as a uneditable
	 * info label.
	 */
	private JTextArea formTextArea()
	{
		JTextArea retTA = new JTextArea(1, 7500);
		retTA.setEditable(false);

		Dimension tmpDim = retTA.getPreferredSize();
		retTA.setMinimumSize(new Dimension(40, tmpDim.height));
		retTA.setMaximumSize(new Dimension(7500, 200));
		return retTA;
	}

}

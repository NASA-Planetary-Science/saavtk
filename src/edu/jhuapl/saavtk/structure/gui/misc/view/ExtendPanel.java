package edu.jhuapl.saavtk.structure.gui.misc.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.jhuapl.saavtk.gui.ProfilePlot;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.StructureType;
import glum.gui.GuiUtil;
import glum.gui.panel.generic.MessagePanel;
import glum.item.ItemEventListener;
import glum.item.ItemEventType;
import net.miginfocom.swing.MigLayout;

/**
 * UI component that holds miscellaneous "extra" settings.
 *
 * @author lopeznr1
 */
public class ExtendPanel extends JPanel implements ActionListener, ItemEventListener
{
	// Ref vars
	private final AnyStructureManager refManager;

	// Gui vars
	private final JFrame profileWindow;
	private final JButton showProfileB;

	/** Standard Constructor */
	public ExtendPanel(PolyhedralModel aSmallBody, AnyStructureManager aManager)
	{
		refManager = aManager;

		// Form the gui
		setLayout(new MigLayout("ins 0", "[]", "[]"));

		showProfileB = GuiUtil.createJButton("Show Profile", this);
		add(showProfileB, "");

		// Form the profile display
		var profilePlot = new ProfilePlot(aManager, aSmallBody);

		profileWindow = new JFrame();
		var icon = new ImageIcon(getClass().getResource("/edu/jhuapl/saavtk/data/black-sphere.png"));
		profileWindow.setTitle("Profile Plot");
		profileWindow.setIconImage(icon.getImage());
		profileWindow.getContentPane().add(profilePlot.getChartPanel());
		profileWindow.setSize(600, 400);
		profileWindow.setLocationRelativeTo(this);

		// Register for events of interest
		refManager.addListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent)
	{
		var source = actionEvent.getSource();
		if (source == showProfileB)
			doActionShowProfilePanel();
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
	 * Helper method that handles the action: "Show Profile"
	 */
	private void doActionShowProfilePanel()
	{
		// Determine if there are profiles
		var hasProfiles = false;
		for (var aItem : refManager.getAllItems())
			hasProfiles |= aItem.getType() == StructureType.Path;

		// Bail if there are no profiles
		if (hasProfiles == false)
		{
			var msg = "There are no paths available to show the profile plot.\n";
			msg += "Please create or load some path structures.";
			var tmpPanel = new MessagePanel(this, "No Profiles Available.", 420, 160);
			tmpPanel.setInfo(msg);
			tmpPanel.setVisible(true);
			return;
		}

		profileWindow.setVisible(true);
	}

	/**
	 * Helper method that keeps various UI elements synchronized.
	 */
	private void updateGui()
	{
		; // Nothing to do
	}

}

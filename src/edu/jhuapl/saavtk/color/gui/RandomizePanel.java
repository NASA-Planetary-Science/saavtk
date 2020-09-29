package edu.jhuapl.saavtk.color.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import edu.jhuapl.saavtk.color.provider.GroupColorProvider;
import edu.jhuapl.saavtk.color.provider.RandomizeGroupColorProvider;
import glum.gui.component.GNumberField;
import net.miginfocom.swing.MigLayout;

/**
 * {@link EditGroupColorPanel} that provides color configuration based on a
 * "random" seed.
 * <P>
 * The configuration options are:
 * <UL>
 * <LI>A seed (integer) which will seed the random function.
 * </UL>
 *
 * @author lopeznr1
 */
public class RandomizePanel extends JPanel implements ActionListener, EditGroupColorPanel
{
	// Reference vars
	private final ActionListener refListener;

	// Gui vars
	private final JLabel seedL;
	private final GNumberField seedNF;
	private final JButton applyB;
	private final JToggleButton syncB;

	/** Standard Constructor */
	public RandomizePanel(ActionListener aListener, long aInitSeed)
	{
		refListener = aListener;

		// Setup the GUI
		setLayout(new MigLayout("", "[]", ""));

		// Config components
		seedL = new JLabel("Seed:", JLabel.RIGHT);
		seedNF = new GNumberField(this);
		seedNF.setValue(aInitSeed);

		// Action buttons
		syncB = new JToggleButton("Sync", true);
		syncB.addActionListener(this);
		applyB = new JButton("Apply");
		applyB.addActionListener(this);

		// Construct the GUI
		buildGui();
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		// Process the event
		Object source = aEvent.getSource();
		if (source == seedNF)
			doActionSeed();
		else if (source == applyB)
			notifyListeners();
		else if (source == syncB)
			doActionSync();
	}

	@Override
	public void activate(boolean aIsActive)
	{
		updateControlArea();
	}

	@Override
	public GroupColorProvider getGroupColorProvider()
	{
		long seed = seedNF.getValueAsInt(0);
		return new RandomizeGroupColorProvider(seed);
	}

	/**
	 * Helper method which layouts the panel.
	 */
	private void buildGui()
	{
		setLayout(new MigLayout("", "[right][120::,fill]", "[][]"));

		// Seed area
		add(seedL, "");
		add(seedNF, "span,wrap");

		// Control area
		add(syncB, "span,split");
		add(applyB, "");
	}

	/**
	 * Helper method that handles the events for seedNF
	 */
	private void doActionSeed()
	{
		updateControlArea();

		// Notify the listeners
		if (syncB.isSelected() == true)
			notifyListeners();
	}

	/**
	 * Helper method that handles the events for syncB
	 */
	private void doActionSync()
	{
		updateControlArea();

		if (syncB.isSelected() == true)
			notifyListeners();
	}

	/**
	 * Helper method to determine if the input is valid.
	 */
	private boolean isValidInput()
	{
		return seedNF.isValidInput();
	}

	/**
	 * Helper method that sends out notification to our listeners
	 */
	private void notifyListeners()
	{
		// Notify our refListener
		refListener.actionPerformed(new ActionEvent(this, 0, ""));
	}

	/**
	 * Helper method to configure the various UI elements in the control section.
	 * <P>
	 * UI elements in the control area will be disabled for invalid configuration.
	 */
	private void updateControlArea()
	{
		boolean isEnabled;

		// Update enable state of applyB
		isEnabled = syncB.isSelected() != true;
		isEnabled &= isValidInput();
		applyB.setEnabled(isEnabled);
	}
}

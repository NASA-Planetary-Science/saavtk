package edu.jhuapl.saavtk.colormap;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import glum.gui.GuiUtil;
import glum.gui.component.GNumberField;
import net.miginfocom.swing.MigLayout;

/**
 * Class that provides UI controls and mechanism for the standard plate coloring
 * panel.
 * <P>
 * TODO: Consider making the min,max fields match the textual output of the
 * ColorBar when defaults are specified
 */
public class StandardPlatePanel extends JPanel implements ActionListener, ChangeListener
{
	// Constants
	private static final long serialVersionUID = 1L;
	public static final String EVT_ColormapChanged = "Event: ColormapChanged";

	// State vars
	private double defaultMin, defaultMax;

	// Cache vars
	private Colormap cColormap;

	// GUI vars
	private final JComboBox<Colormap> colormapComboBox;
	private final JLabel minValueL, maxValueL;
	private final GNumberField minValueNF, maxValueNF;
	private final GNumberField numColorLevelsNF;
	private final JSpinner numTicksSpinner;
	private final JCheckBox logScaleCB;
	private final JButton resetButton;
	private final JButton applyButton;
	private final JToggleButton syncButton;
	private final JComponent extraComp;

	/**
	 * Standard Constructor
	 * 
	 * @param aExtraComp Extra component that should be associated with this panel.
	 *                   May be null if nothing is to be associated. The extra panel
	 *                   will be displayed at the bottom.
	 */
	public StandardPlatePanel(JComponent aExtraComp)
	{
		// Set up state / cache vars
		defaultMin = Double.NaN;
		defaultMax = Double.NaN;

		cColormap = Colormaps.getNewInstanceOfBuiltInColormap(Colormaps.getDefaultColormapName());

		// Instantiate the various GUI controls
		colormapComboBox = new JComboBox<>();
		ListCellRenderer<Colormap> tmpRenderer = ColormapUtil.getFancyColormapRender();
		((Component) tmpRenderer).setEnabled(false);
		colormapComboBox.setRenderer(tmpRenderer);
		for (String aStr : Colormaps.getAllBuiltInColormapNames())
		{
			Colormap cmap = Colormaps.getNewInstanceOfBuiltInColormap(aStr);
			colormapComboBox.addItem(cmap);
			if (cmap.getName().equals(Colormaps.getDefaultColormapName()))
				colormapComboBox.setSelectedItem(cmap);
		}
		colormapComboBox.addActionListener(this);

		minValueL = new JLabel("Min Value");
		maxValueL = new JLabel("Max Value");
		minValueNF = new GNumberField(this, new SigFigNumberFormat(3));
		maxValueNF = new GNumberField(this, new SigFigNumberFormat(3));
		numColorLevelsNF = new GNumberField(this);
		numColorLevelsNF.setValue(32);
		numTicksSpinner = new JSpinner(new SpinnerNumberModel(5, 0, 20, 1));
		numTicksSpinner.addChangeListener(this);

		logScaleCB = new JCheckBox("Log scale");
		logScaleCB.addActionListener(this);
		resetButton = new JButton("Range Reset");
		resetButton.setActionCommand(EVT_ColormapChanged);
		resetButton.addActionListener(this);
		syncButton = new JToggleButton("Sync", true);
		syncButton.addActionListener(this);
		applyButton = new JButton("Apply");
		applyButton.addActionListener(this);

		extraComp = aExtraComp;

		// Construct the GUI
		buildGui();
	}

	/**
	 * Returns the current Colormap associated with this panel.
	 */
	public Colormap getColormap()
	{
		return cColormap;
	}

	/**
	 * Returns the min,max range values.
	 */
	public double[] getCurrentMinMax()
	{
		return new double[] { minValueNF.getValue(), maxValueNF.getValue() };
	}

	/**
	 * Updates the GUI to reflect the new min,max range values.
	 * <P>
	 * This method will not trigger an event.
	 */
	public void setCurrentMinMax(double aMin, double aMax)
	{
		minValueNF.setValue(aMin);
		maxValueNF.setValue(aMax);
	}

	/**
	 * Sets in the default values for the range. This will have an effect on the
	 * resetB UI.
	 */
	public void setDefaultRange(double aMin, double aMax)
	{
		defaultMin = aMin;
		defaultMax = aMax;
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		Object source = aEvent.getSource();

		// Reset the defaults
		if (source == resetButton)
		{
			setCurrentMinMax(defaultMin, defaultMax);
			updateControlArea();

			if (isColorMapConfigValid() == false)
				return;

			syncColorMapToGui();
			firePropertyChange(EVT_ColormapChanged, null, null);
			return;
		}

		// Apply the settings
		else if (source == applyButton)
		{
			syncColorMapToGui();
			firePropertyChange(EVT_ColormapChanged, null, null);
			return;
		}

		// Sync toggle
		else if (source == syncButton)
		{
			updateControlArea();
			doAutoSync();
		}

		// LogScale UI
		else if (source == logScaleCB)
		{
			syncColorMapToGui();
			firePropertyChange(EVT_ColormapChanged, null, null);
		}

		// ColorMap ComboBox UI
		else if (source == colormapComboBox)
		{
			doAutoSync();
		}

		// Keep the Render View synchronized
		if (syncButton.isSelected() == true)
		{
			if (source == minValueNF || source == maxValueNF || source == numColorLevelsNF)
			{
				doAutoSync();
			}
		}

		if (source == minValueNF || source == maxValueNF || source == numColorLevelsNF)
			updateControlArea();
	}

	@Override
	public void setEnabled(boolean aEnabled)
	{
		GuiUtil.setEnabled(this, aEnabled);

		// Allow the extraComp to properly configure
		// it's enable state
		if (extraComp != null)
			extraComp.setEnabled(aEnabled);

		if (aEnabled == true)
			updateControlArea();
	}

	@Override
	public void stateChanged(ChangeEvent aEvent)
	{
		Object source = aEvent.getSource();

		// NumTicks UI
		if (source == numTicksSpinner)
		{
			cColormap.setNumberOfLabels((Integer) numTicksSpinner.getValue());
			firePropertyChange(EVT_ColormapChanged, null, null);
		}
	}

	/**
	 * Helper method which layouts the panel.
	 */
	private void buildGui()
	{
		setLayout(new MigLayout("", "[right][120::,fill]15[left]", "0[]"));

		// Colormap selector area
		add(colormapComboBox, "span,wrap");

		// Range, # Color levels, and # Ticks area
		add(minValueL, "");
		add(minValueNF, "");
		add(resetButton, "sg g1,wrap");

		add(maxValueL, "");
		add(maxValueNF, "");
		add(syncButton, "sg g1,wrap");

		add(new JLabel("# Color Levels"), "");
		add(numColorLevelsNF, "");
		add(applyButton, "sg g1,wrap");

		add(new JLabel("# Ticks"), "");
		add(numTicksSpinner, "");
		add(logScaleCB, "");

		if (extraComp != null)
			add(extraComp, "align left,newline,span,wrap 0");
	}

	/**
	 * Helper method that synchronizes the ColorMap when the sync toggle is enabled
	 */
	private void doAutoSync()
	{
		// Bail if action GUI is not in a valid state
		if (isColorMapConfigValid() == false)
			return;

		// Synchronize our cached ColorMap
		syncColorMapToGui();

		// Send out notification of the changes
		firePropertyChange(EVT_ColormapChanged, null, null);
	}

	/**
	 * Helper method to determine if the Colormap configuration is even valid
	 */
	private boolean isColorMapConfigValid()
	{
		boolean isValid = true;
		isValid &= minValueNF.isValidInput();
		isValid &= maxValueNF.isValidInput();
		isValid &= minValueNF.getValue() <= maxValueNF.getValue();
		isValid &= numColorLevelsNF.isValidInput();

		return isValid;
	}

	/**
	 * Helper method that will synchronize relevant ColorMap properties to the
	 * corresponding GUI elements.
	 */
	private void syncColorMapToGui()
	{
		String name = ((Colormap) colormapComboBox.getSelectedItem()).getName();

		// Bail on invalid configuration
		if (isColorMapConfigValid() == false)
			return;

		cColormap = Colormaps.getNewInstanceOfBuiltInColormap(name);
		cColormap.setRangeMin(minValueNF.getValue());
		cColormap.setRangeMax(maxValueNF.getValue());
		cColormap.setNumberOfLevels(numColorLevelsNF.getValueAsInt(-1));
		cColormap.setNumberOfLabels((Integer) numTicksSpinner.getValue());
		cColormap.setLogScale(logScaleCB.isSelected());
	}

	/**
	 * Helper method to configure the various UI elements in the control section.
	 * <P>
	 * UI elements in the control area will be disabled for invalid configuration.
	 */
	private void updateControlArea()
	{
		boolean isEnabled, isSwapped;

		// Update MinValue MaxValue UI elements
		String errMsg = null;
		Color fgColor = Color.BLACK;
		isSwapped = minValueNF.getValue() > maxValueNF.getValue();
		isSwapped &= minValueNF.isValidInput() == true;
		isSwapped &= maxValueNF.isValidInput() == true;
		if (isSwapped == true)
		{
			errMsg = "Min, Max values are swapped.";
			fgColor = minValueNF.getFailColor();
		}
		minValueL.setForeground(fgColor);
		maxValueL.setForeground(fgColor);
		minValueL.setToolTipText(errMsg);
		maxValueL.setToolTipText(errMsg);

		// Update enable state of applyB
		isEnabled = syncButton.isSelected() != true;
		isEnabled &= isColorMapConfigValid();
		applyButton.setEnabled(isEnabled);
	}

}

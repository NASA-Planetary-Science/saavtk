package edu.jhuapl.saavtk.view.light.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.google.common.collect.Range;

import edu.jhuapl.saavtk.gui.util.ToolTipUtil;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.view.light.LightCfg;
import edu.jhuapl.saavtk.view.light.LightingType;
import glum.gui.GuiUtil;
import glum.gui.component.GNumberField;
import glum.gui.component.GNumberFieldSlider;
import glum.gui.panel.GPanel;
import net.miginfocom.swing.MigLayout;

/**
 * Panel that allows the user to configure a {@link LightCfg}.
 *
 * @author lopeznr1
 */
public class LightCfgPanel extends GPanel implements ActionListener, ItemListener
{
	// Constants
	private static final Range<Double> LatRange = Range.closed(-90.0, 90.0);
	private static final Range<Double> LonRange = Range.closed(-180.0, 180.0);
	private static final Range<Double> IntensityRange = Range.closed(0.0, 1.0);
	private static final int NumCols = 6;

	// Gui vars
	private final JRadioButton fixedLightRB;
	private final JRadioButton headlightRB;
	private final JRadioButton lightKitRB;
	private final JPanel positionPanel;

	private final JLabel intensityL, locationDistL, locationLatL, locationLonL;
	private final GNumberField distanceNF;
	private final GNumberFieldSlider intensityNFS;
	private final GNumberFieldSlider locationLatNFS;
	private final GNumberFieldSlider locationLonNFS;

	/** Standard Constructor */
	public LightCfgPanel()
	{
		fixedLightRB = GuiUtil.createJRadioButton(this, "Fixed Light");
		fixedLightRB.setToolTipText(ToolTipUtil.getLightFixed());
		headlightRB = GuiUtil.createJRadioButton(this, "Headlight");
		headlightRB.setToolTipText(ToolTipUtil.getLightHeadlight());
		lightKitRB = GuiUtil.createJRadioButton(this, "Light Kit");
		lightKitRB.setToolTipText(ToolTipUtil.getLightKit());
		GuiUtil.linkRadioButtons(lightKitRB, headlightRB, fixedLightRB);

		DecimalFormat intensityDF = new DecimalFormat("0.000");
		intensityNFS = new GNumberFieldSlider(this, intensityDF, IntensityRange);
		intensityL = new JLabel("Intensity:");

		DecimalFormat locationDF = new DecimalFormat("#.###");
		locationLatL = new JLabel("Lat:");
		locationLatNFS = new GNumberFieldSlider(this, locationDF, LatRange, NumCols);
		locationLonL = new JLabel("Lon:");
		locationLonNFS = new GNumberFieldSlider(this, locationDF, LonRange, NumCols);
		locationDistL = new JLabel("Distance:");
		distanceNF = new GNumberField(this);
		positionPanel = formPositionPanel();

		// Form the GUI
		setLayout(new MigLayout("", "0[][]0", "0[][]"));

		// Type area
		add(formTypePanel(), "span,wrap");

		// Intensity area
		add(GuiUtil.createDivider(), "growx,h 4!,span,wrap");
		add(intensityL, "");
		add(intensityNFS, "growx,pushx,span,wrap");

		// Position area
		add(GuiUtil.createDivider(), "growx,h 4!,span,wrap");
		add(positionPanel, "growx,span,wrap 0");
	}

	/**
	 * Returns the {@link LightCfg} as configured in by this panel.
	 */
	public LightCfg getLightCfg()
	{
		LightingType type = LightingType.NONE;
		if (fixedLightRB.isSelected() == true)
			type = LightingType.FIXEDLIGHT;
		else if (headlightRB.isSelected() == true)
			type = LightingType.HEADLIGHT;
		else if (lightKitRB.isSelected() == true)
			type = LightingType.LIGHT_KIT;

		double intensity = intensityNFS.getValue();

		double tmpLat = locationLatNFS.getValue();
		double tmpLon = locationLonNFS.getValue();
		double tmpAlt = distanceNF.getValue();
		LatLon positionLL = new LatLon(tmpLat, tmpLon, tmpAlt).toRadians();

		return new LightCfg(type, positionLL, intensity);
	}

	/**
	 * Method that returns a list of errors associated with this panel.
	 */
	public List<String> getMsgFailList()
	{
		List<String> retErrL = new ArrayList<>();

		if (distanceNF.isValidInput() == false)
			retErrL.add("Invalid Light Altitude.");

		boolean isUtilized = headlightRB.isSelected() == true || fixedLightRB.isSelected() == true;
		if (isUtilized == true && intensityNFS.isValidInput() == false)
			retErrL.add(String.format("Invalid Intensity. Range: [%1.1f, %1.1f]", IntensityRange.lowerEndpoint(),
					IntensityRange.upperEndpoint()));

		isUtilized = fixedLightRB.isSelected() == true;
		if (isUtilized == true && locationLatNFS.isValidInput() == false)
			retErrL.add(String.format("Invalid Latitude. Range: [%1.0f, %1.0f]", LatRange.lowerEndpoint(),
					LatRange.upperEndpoint()));

		if (isUtilized == true && locationLonNFS.isValidInput() == false)
			retErrL.add(String.format("Invalid Longitude. Range: [%1.0f, %1.0f]", LonRange.lowerEndpoint(),
					LonRange.upperEndpoint()));

		return retErrL;
	}

	/**
	 * Configures this panel to reflect the specified {@link LightCfg}.
	 */
	public void setLightCfg(LightCfg aLightCfg)
	{
		LightingType type = aLightCfg.getType();

		boolean tmpBool = type == LightingType.FIXEDLIGHT;
		fixedLightRB.setSelected(tmpBool);

		tmpBool = type == LightingType.HEADLIGHT;
		headlightRB.setSelected(tmpBool);

		tmpBool = type == LightingType.LIGHT_KIT;
		lightKitRB.setSelected(tmpBool);

		double intensity = aLightCfg.getIntensity();
		intensityNFS.setValue(intensity);

		LatLon positionLL = aLightCfg.getPositionLL().toDegrees();
		distanceNF.setValue(positionLL.rad);
		locationLatNFS.setValue(positionLL.lat);
		locationLonNFS.setValue(positionLL.lon);
	}

	@Override
	public void actionPerformed(ActionEvent aEvent)
	{
		notifyListeners(this);
		updateGui();
	}

	@Override
	public void itemStateChanged(ItemEvent aEvent)
	{
		// Ignore deselects
		if (aEvent.getStateChange() == ItemEvent.DESELECTED)
			return;

		notifyListeners(this);
		updateGui();
	}

	@Override
	public void setEnabled(boolean aBool)
	{
		super.setEnabled(aBool);
		updateGui();
	}

	/**
	 * Helper method to form the type Panel
	 */
	private JPanel formPositionPanel()
	{
		JPanel retPanel = new JPanel(new MigLayout("", "0[][][]0", "0[][]"));

		JLabel titleL = new JLabel("Position", JLabel.CENTER);
		retPanel.add(titleL, "growx,span,wrap");

		retPanel.add(locationLatL, "ax right");
		retPanel.add(locationLatNFS, "growx,pushx");
		retPanel.add(new JLabel("deg"), "wrap");

		retPanel.add(locationLonL, "ax right");
		retPanel.add(locationLonNFS, "growx");
		retPanel.add(new JLabel("deg"), "wrap");

		retPanel.add(locationDistL, "span,split");
		retPanel.add(distanceNF, "growx");
		retPanel.add(new JLabel("km"), "wrap 0");

		return retPanel;
	}

	/**
	 * Helper method to form the type Panel
	 */
	private JPanel formTypePanel()
	{
		JPanel retPanel = new JPanel(new MigLayout("", "0[][][]0", "0[]0"));

		retPanel.add(lightKitRB, "gapright 5");
		retPanel.add(headlightRB, "gapright 5");
		retPanel.add(fixedLightRB, "");

		return retPanel;
	}

	/**
	 * Helper method that updates the various UI elements to keep them synchronized.
	 */
	private void updateGui()
	{
		boolean isMainEnabled = isEnabled();

		boolean isEnabled = isMainEnabled;
		GuiUtil.setEnabled(isEnabled, fixedLightRB, headlightRB, lightKitRB);

		isEnabled = isMainEnabled;
		isEnabled &= fixedLightRB.isSelected() == true || headlightRB.isSelected() == true;
		GuiUtil.setEnabled(isEnabled, intensityL, intensityNFS);

		isEnabled = isMainEnabled;
		isEnabled &= fixedLightRB.isSelected() == true;
		GuiUtil.setEnabled(positionPanel, isEnabled);
	}

}

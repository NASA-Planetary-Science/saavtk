package edu.jhuapl.saavtk.gui.panel;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import edu.jhuapl.saavtk.model.PolyhedralModel;

/**
 * Swing panel for editing a vtkProperty
 */
public class DisplayPropertyEditorPanel extends JPanel implements ChangeListener, ItemListener
{
    private PolyhedralModel smallBodyModel;
    private JLabel opacityLabel;
    private JSpinner opacitySpinner;
    private JRadioButton flatShadingButton;
    private JRadioButton smoothShadingButton;
    private ButtonGroup shadingButtonGroup;
    private JRadioButton surfaceButton;
    private ButtonGroup representationButtonGroup;
    private JRadioButton wireframeButton;
    private JRadioButton pointsButton;
    private JRadioButton surfaceWithEdgesButton;
    private JCheckBox cullFrontfaceCheckBox;
    private JLabel specularCoefLabel;
    private JSpinner specularCoefSpinner;
    private JLabel specularPowerLabel;
    private JSpinner specularPowerSpinner;
    private JLabel pointSizeLabel;
    private JSpinner pointSizeSpinner;
    private JLabel lineWidthLabel;
    private JSpinner lineWidthSpinner;
    private boolean initialized = false;

    private static final String SURFACE = "Surface";
    private static final String WIREFRAME = "Wireframe";
    private static final String POINTS = "Points";
    private static final String SURFACE_WITH_EDGES = "Surface with Edges";

    public enum DisplayMode
    {
        SURFACE,
        POINTS
    }

    private DisplayMode displayMode = DisplayMode.SURFACE;

    public DisplayPropertyEditorPanel(PolyhedralModel smallBodyModel)
    {
        super(new MigLayout("insets 0"));
        this.smallBodyModel = smallBodyModel;

        JLabel shadingLabel = new JLabel("Shading");

        flatShadingButton = new JRadioButton(PolyhedralModel.FlatShadingStr);
        flatShadingButton.setActionCommand(PolyhedralModel.FlatShadingStr);
        flatShadingButton.addItemListener(this);
        flatShadingButton.setEnabled(true);

        smoothShadingButton = new JRadioButton(PolyhedralModel.SmoothShadingStr);
        smoothShadingButton.setActionCommand(PolyhedralModel.SmoothShadingStr);
        smoothShadingButton.addItemListener(this);
        smoothShadingButton.setEnabled(true);

        shadingButtonGroup = new ButtonGroup();
        shadingButtonGroup.add(flatShadingButton);
        shadingButtonGroup.add(smoothShadingButton);
        shadingButtonGroup.setSelected(smoothShadingButton.getModel(), true);

        JLabel representationLabel = new JLabel("Representation");

        surfaceButton = new JRadioButton(SURFACE);
        surfaceButton.setActionCommand(SURFACE);
        surfaceButton.addItemListener(this);
        surfaceButton.setEnabled(true);

        wireframeButton = new JRadioButton(WIREFRAME);
        wireframeButton.setActionCommand(WIREFRAME);
        wireframeButton.addItemListener(this);
        wireframeButton.setEnabled(true);

        pointsButton = new JRadioButton(POINTS);
        pointsButton.setActionCommand(POINTS);
        pointsButton.addItemListener(this);
        pointsButton.setEnabled(true);

        surfaceWithEdgesButton = new JRadioButton(SURFACE_WITH_EDGES);
        surfaceWithEdgesButton.setActionCommand(SURFACE_WITH_EDGES);
        surfaceWithEdgesButton.addItemListener(this);
        surfaceWithEdgesButton.setEnabled(true);

        representationButtonGroup = new ButtonGroup();
        representationButtonGroup.add(surfaceButton);
        representationButtonGroup.add(wireframeButton);
        representationButtonGroup.add(pointsButton);
        representationButtonGroup.add(surfaceWithEdgesButton);
        representationButtonGroup.setSelected(surfaceButton.getModel(), true);

        pointSizeLabel = new JLabel("Point Size");
        pointSizeSpinner = new JSpinner(new SpinnerNumberModel(1.0, 1.0, 100.0, 1.0));
        pointSizeSpinner.setEditor(new JSpinner.NumberEditor(pointSizeSpinner, "0.00"));
        pointSizeSpinner.setPreferredSize(new Dimension(80, 21));
        pointSizeSpinner.addChangeListener(this);
        pointSizeLabel.setEnabled(displayMode == DisplayMode.POINTS);
        pointSizeSpinner.setEnabled(displayMode == DisplayMode.POINTS);
        String pointSizeTooltip = "The point size when Representation is set to Points";
        pointSizeLabel.setToolTipText(pointSizeTooltip);
        pointSizeSpinner.setToolTipText(pointSizeTooltip);

        lineWidthLabel = new JLabel("Line Width");
        lineWidthSpinner = new JSpinner(new SpinnerNumberModel(1.0, 1.0, 100.0, 1.0));
        lineWidthSpinner.setEditor(new JSpinner.NumberEditor(lineWidthSpinner, "0.00"));
        lineWidthSpinner.setPreferredSize(new Dimension(80, 21));
        lineWidthSpinner.addChangeListener(this);
        lineWidthLabel.setEnabled(false);
        lineWidthSpinner.setEnabled(false);
        String lineWidthTooltip = "The line width when Representation is set to Wireframe or Surface with Edges";
        lineWidthLabel.setToolTipText(lineWidthTooltip);
        lineWidthSpinner.setToolTipText(lineWidthTooltip);

        specularCoefLabel = new JLabel("Specular Coefficient");
        specularCoefSpinner = new JSpinner(new SpinnerNumberModel(0.1, 0.0, 1.0, 0.1));
        specularCoefSpinner.setEditor(new JSpinner.NumberEditor(specularCoefSpinner, "0.00"));
        specularCoefSpinner.setPreferredSize(new Dimension(80, 21));
        specularCoefSpinner.addChangeListener(this);
        specularCoefLabel.setEnabled(true);
        specularCoefSpinner.setEnabled(true);

        specularPowerLabel = new JLabel("Specular Power");
        specularPowerSpinner = new JSpinner(new SpinnerNumberModel(100.0, 0.0, 128.0, 1.0));
        specularPowerSpinner.setEditor(new JSpinner.NumberEditor(specularPowerSpinner, "0"));
        specularPowerSpinner.setPreferredSize(new Dimension(80, 21));
        specularPowerSpinner.addChangeListener(this);
        specularPowerLabel.setEnabled(true);
        specularPowerSpinner.setEnabled(true);

        cullFrontfaceCheckBox = new JCheckBox();
        cullFrontfaceCheckBox.setText("Cull Frontface");
        cullFrontfaceCheckBox.setSelected(false);
        cullFrontfaceCheckBox.addItemListener(this);

        opacityLabel = new JLabel("Opacity");
        opacitySpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1.0, 0.1));
        opacitySpinner.setEditor(new JSpinner.NumberEditor(opacitySpinner, "0.00"));
        opacitySpinner.setPreferredSize(new Dimension(80, 21));
        opacitySpinner.addChangeListener(this);
        opacityLabel.setEnabled(true);
        opacitySpinner.setEnabled(true);


        add(shadingLabel, "wrap");
        add(flatShadingButton, "wrap, gapleft 25");
        add(smoothShadingButton, "wrap, gapleft 25");
        add(representationLabel, "wrap");
        add(surfaceButton, "wrap, gapleft 25");
        add(wireframeButton, "wrap, gapleft 25");
        add(pointsButton, "wrap, gapleft 25");
        add(surfaceWithEdgesButton, "wrap, gapleft 25");
        add(pointSizeLabel, "split 2");
        add(pointSizeSpinner, "wrap");
        add(lineWidthLabel, "split 2");
        add(lineWidthSpinner, "wrap");
        //add(cullFrontfaceCheckBox, "wrap");
        //add(specularCoefLabel, "split 2");
        //add(specularCoefSpinner, "wrap");
        //add(specularPowerLabel, "split 2");
        //add(specularPowerSpinner, "wrap");
        //add(opacityLabel, "split 2");
        //add(opacitySpinner, "wrap");

        initialized = true;
    }

    public void stateChanged(ChangeEvent e)
    {
        if (!initialized)
            return;

        if (e.getSource() == opacitySpinner)
        {
            double val = (Double)opacitySpinner.getValue();
            smallBodyModel.setOpacity(val);
        }
        else if (e.getSource() == pointSizeSpinner)
        {
            double val = (Double)pointSizeSpinner.getValue();
            smallBodyModel.setPointSize(val);
        }
        else if (e.getSource() == lineWidthSpinner)
        {
            double val = (Double)lineWidthSpinner.getValue();
            smallBodyModel.setLineWidth(val);
        }
        else if (e.getSource() == specularCoefSpinner)
        {
            double val = (Double)specularCoefSpinner.getValue();
            smallBodyModel.setSpecularCoefficient(val);
        }
        else if (e.getSource() == specularPowerSpinner)
        {
            double val = (Double)specularPowerSpinner.getValue();
            smallBodyModel.setSpecularPower(val);
        }
    }

    public void itemStateChanged(ItemEvent e)
    {
        if (!initialized)
            return;

        if (((JRadioButton)e.getSource()).isSelected())
        {

            if (e.getItemSelectable() == surfaceButton)
            {
                if (surfaceButton.isSelected())
                    smallBodyModel.setRepresentationToSurface();
            }
            else if (e.getItemSelectable() == wireframeButton)
            {
                if (wireframeButton.isSelected())
                    smallBodyModel.setRepresentationToWireframe();
            }
            else if (e.getItemSelectable() == pointsButton)
            {
                if (pointsButton.isSelected())
                    smallBodyModel.setRepresentationToPoints();
            }
            else if (e.getItemSelectable() == surfaceWithEdgesButton)
            {
                if (surfaceWithEdgesButton.isSelected())
                    smallBodyModel.setRepresentationToSurfaceWithEdges();
            }
            else if (e.getItemSelectable() == flatShadingButton)
            {
                if (flatShadingButton.isSelected())
                    smallBodyModel.setShadingToFlat();
            }
            else if (e.getItemSelectable() == smoothShadingButton)
            {
                if (smoothShadingButton.isSelected())
                    smallBodyModel.setShadingToSmooth();
            }
            else if (e.getItemSelectable() == cullFrontfaceCheckBox)
            {
                smallBodyModel.setCullFrontface(cullFrontfaceCheckBox.isSelected());
            }
        }

        if (displayMode == DisplayMode.SURFACE)
        {
            boolean enableLineWidthSpinner = wireframeButton.isSelected() || surfaceWithEdgesButton.isSelected();
            lineWidthLabel.setEnabled(enableLineWidthSpinner);
            lineWidthSpinner.setEnabled(enableLineWidthSpinner);
            boolean enablePointSizeSpinner = pointsButton.isSelected();
            pointSizeLabel.setEnabled(enablePointSizeSpinner);
            pointSizeSpinner.setEnabled(enablePointSizeSpinner);
        }
    }
}

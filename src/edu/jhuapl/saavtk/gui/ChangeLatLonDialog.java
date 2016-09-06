package edu.jhuapl.saavtk.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.jhuapl.saavtk.model.AbstractEllipsePolygonModel;
import edu.jhuapl.saavtk.model.StructureModel;
import edu.jhuapl.saavtk.model.AbstractEllipsePolygonModel.EllipsePolygon;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;

/**
 * The purpose of this dialog is to change
 */
public class ChangeLatLonDialog extends JDialog implements ActionListener
{
    private StructureModel structureModel;
    private JButton applyButton;
    private JButton okayButton;
    private JButton cancelButton;
    private JFormattedTextField latTextField;
    private JFormattedTextField lonTextField;
    private int structureIndex;

    public ChangeLatLonDialog(StructureModel structureModel, int structureIndex)
    {
        this.structureModel = structureModel;
        this.structureIndex = structureIndex;

        setTitle("Change Latitude/Longitude");

        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout());

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(6);

        JLabel latLabel = new JLabel("Latitude (deg)");
        latTextField = new JFormattedTextField(nf);
        latTextField.setPreferredSize(new Dimension(125, 23));
        JLabel lonLabel = new JLabel("Longitude (deg)");
        lonTextField = new JFormattedTextField(nf);
        lonTextField.setPreferredSize(new Dimension(125, 23));

        JPanel buttonPanel = new JPanel(new MigLayout());
        applyButton = new JButton("Apply");
        applyButton.addActionListener(this);
        okayButton = new JButton("OK");
        okayButton.addActionListener(this);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(applyButton);
        buttonPanel.add(okayButton);
        buttonPanel.add(cancelButton);

        panel.add(latLabel);
        panel.add(latTextField);
        panel.add(lonLabel);
        panel.add(lonTextField, "wrap");

        panel.add(buttonPanel, "span, align right");

        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);

        add(panel, BorderLayout.CENTER);
        pack();
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == applyButton || e.getSource() == okayButton)
        {
            try
            {
                if (structureModel instanceof AbstractEllipsePolygonModel)
                {
                    double latitude = Double.parseDouble(latTextField.getText());
                    double longitude = Double.parseDouble(lonTextField.getText());

                    ((AbstractEllipsePolygonModel)structureModel).movePolygon(
                            structureIndex,
                            (Math.PI/180.0)*latitude,
                            (Math.PI/180.0)*longitude);

                    double[] center = ((AbstractEllipsePolygonModel.EllipsePolygon)structureModel.getStructure(structureIndex)).center;

                    LatLon ll = MathUtil.reclat(center);
                    if (ll.lon < 0.0)
                        ll.lon += 2.0*Math.PI;

                    // Reset the text fields in case the requested lat/lon change was not
                    // fully fulfilled.
                    latTextField.setValue((180.0/Math.PI)*ll.lat);
                    lonTextField.setValue((180.0/Math.PI)*ll.lon);
                }
            }
            catch (NumberFormatException ex)
            {
                return;
            }
        }

        if (e.getSource() == okayButton || e.getSource() == cancelButton)
        {
            super.setVisible(false);
        }
    }

    public void setVisible(boolean b)
    {
        double[] center = ((AbstractEllipsePolygonModel.EllipsePolygon)structureModel.getStructure(structureIndex)).center;

        LatLon ll = MathUtil.reclat(center);
        if (ll.lon < 0.0)
            ll.lon += 2.0*Math.PI;
        latTextField.setValue((180.0/Math.PI)*ll.lat);
        lonTextField.setValue((180.0/Math.PI)*ll.lon);

        super.setVisible(b);
    }
}

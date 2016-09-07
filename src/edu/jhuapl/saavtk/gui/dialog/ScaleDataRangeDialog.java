package edu.jhuapl.saavtk.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import edu.jhuapl.saavtk.model.PolyhedralModel;

public class ScaleDataRangeDialog extends JDialog implements ActionListener
{
    PolyhedralModel smallBodyModel;
    private JButton applyButton;
    private JButton resetButton;
    private JButton okayButton;
    private JButton cancelButton;
    private JFormattedTextField minTextField;
    private JFormattedTextField maxTextField;

    public ScaleDataRangeDialog(PolyhedralModel smallBodyModel)
    {
        this.smallBodyModel = smallBodyModel;

        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout());

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(6);

        JLabel minLabel = new JLabel("Minimum");
        minTextField = new JFormattedTextField(nf);
        minTextField.setPreferredSize(new Dimension(125, 23));
        JLabel maxLabel = new JLabel("Maximum");
        maxTextField = new JFormattedTextField(nf);
        maxTextField.setPreferredSize(new Dimension(125, 23));

        JPanel buttonPanel = new JPanel(new MigLayout());
        applyButton = new JButton("Apply");
        applyButton.addActionListener(this);
        resetButton = new JButton("Reset");
        resetButton.addActionListener(this);
        okayButton = new JButton("OK");
        okayButton.addActionListener(this);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(applyButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(okayButton);
        buttonPanel.add(cancelButton);

        panel.add(minLabel);
        panel.add(minTextField);
        panel.add(maxLabel);
        panel.add(maxTextField, "wrap");

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
                double[] newRange = {
                        Double.parseDouble(minTextField.getText()),
                        Double.parseDouble(maxTextField.getText())
                };

                int index = smallBodyModel.getColoringIndex();
                if (newRange[1] > newRange[0])
                {
                    smallBodyModel.setCurrentColoringRange(index, newRange);
                }

                // Reset the text fields in case the requested range scale change was not
                // fully fulfilled (e.g. the max was too high or the min was too low)
                double[] range = smallBodyModel.getCurrentColoringRange(index);
                minTextField.setValue(range[0]);
                maxTextField.setValue(range[1]);
            }
            catch (NumberFormatException ex)
            {
                return;
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        else if (e.getSource() == resetButton)
        {
            try
            {
                int index = smallBodyModel.getColoringIndex();
                double[] defaultRange = smallBodyModel.getDefaultColoringRange(index);
                if (defaultRange[1] > defaultRange[0])
                {
                    smallBodyModel.setCurrentColoringRange(index, defaultRange);
                }

                double[] range = smallBodyModel.getCurrentColoringRange(index);
                minTextField.setValue(range[0]);
                maxTextField.setValue(range[1]);
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }

        if (e.getSource() == okayButton || e.getSource() == cancelButton)
        {
            super.setVisible(false);
        }
    }

    public void setVisible(boolean b)
    {
        int index = smallBodyModel.getColoringIndex();
        setTitle("Rescale Range of " + smallBodyModel.getColoringName(index));

        double[] range = smallBodyModel.getCurrentColoringRange(index);
        minTextField.setValue(range[0]);
        maxTextField.setValue(range[1]);

        super.setVisible(b);
    }
}

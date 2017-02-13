package edu.jhuapl.saavtk.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import edu.jhuapl.saavtk.model.Model;

public class NormalOffsetChangerDialog extends JDialog implements ActionListener
{
    private Model model;
    private JButton applyButton;
    private JButton resetButton;
    private JButton okayButton;
    private JButton cancelButton;
    private JFormattedTextField offsetField;
    private String lastGood = "";

    public NormalOffsetChangerDialog(Model model)
    {
        this.model = model;

        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout());

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(6);

        JLabel offsetLabel = new JLabel("Normal Offset");
        offsetField = new JFormattedTextField(nf);
        offsetField.setPreferredSize(new Dimension(125, 23));
        offsetField.setInputVerifier(new DoubleVerifier());
        JLabel kmLabel = new JLabel("meters");


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


        String tooltipText =
                "<html>Objects displayed on a shape model need to be shifted slightly away from <br>" +
                "the shape model in the direction normal to the plates as otherwise they will <br>" +
                "interfere with the shape model itself and may not be visible. This dialog allows <br>" +
                "you to explicitely set the offset amount in meters. In general, the smallest positive <br>" +
                "value should be chosen such that the objects are visible. To revert the offset <br>" +
                "to the default value, press the Reset button.</html>";
        applyButton.setToolTipText(tooltipText);
        resetButton.setToolTipText(tooltipText);
        okayButton.setToolTipText(tooltipText);
        cancelButton.setToolTipText(tooltipText);
        offsetLabel.setToolTipText(tooltipText);
        offsetField.setToolTipText(tooltipText);
        kmLabel.setToolTipText(tooltipText);


        panel.add(offsetLabel);
        panel.add(offsetField);
        panel.add(kmLabel, "wrap");

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
                double newOffset = Double.parseDouble(offsetField.getText());
                newOffset /= 1000.0;

                model.setOffset(newOffset);

                // Reset the text field in case the requested offset change was not
                // fully fulfilled (e.g. was negative)
                double offset = model.getOffset();
                offsetField.setValue(1000.0 * offset);
            }
            catch (NumberFormatException ex)
            {
                return;
            }
        }
        else if (e.getSource() == resetButton)
        {
            double defaultOffset = model.getDefaultOffset();

            model.setOffset(defaultOffset);

            // Reset the text field in case the requested offset change was not
            // fully fulfilled.
            double offset = model.getOffset();
            offsetField.setValue(1000.0 * offset);
        }

        if (e.getSource() == okayButton || e.getSource() == cancelButton)
        {
            super.setVisible(false);
        }
    }

    public void setVisible(boolean b)
    {
        setTitle("Change Normal Offset");

        offsetField.setValue(1000.0 * model.getOffset());
        lastGood = offsetField.getText();

        super.setVisible(b);
    }

    private class DoubleVerifier extends InputVerifier
    {
        public boolean verify(JComponent input)
        {
            JTextField text = (JTextField)input;
            String value = text.getText().trim();
            try
            {
                double v = Double.parseDouble(value);
                if (v < 0.0)
                    throw new NumberFormatException();
                lastGood = value;
            }
            catch (NumberFormatException e)
            {
                text.setText(lastGood);
                return false;
            }
            return true;
        }
    }
}

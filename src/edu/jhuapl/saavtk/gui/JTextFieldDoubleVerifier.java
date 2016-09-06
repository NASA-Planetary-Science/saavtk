package edu.jhuapl.saavtk.gui;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Input verifier on a JTextField which makes sure that input can be parsed as double.
 * This is meant to replace using a JFormattedTextField with a NumberFormat since it
 * is difficult to make the latter accept any possible valid double string (e.g.
 * any form of scientific notation). With this class, any string that can be parsed as
 * a double will be accepted in the JTextField. If it is not a valid double, it will
 * be reverted to the previous valid value.
 */
public class JTextFieldDoubleVerifier extends InputVerifier
{
    private String lastGood;
    private double min = -Double.MAX_VALUE;
    private double max = Double.MAX_VALUE;

    public static InputVerifier getVerifier(JTextField textField)
    {
        return new JTextFieldDoubleVerifier(textField);
    }

    public static InputVerifier getVerifier(JTextField textField, double min, double max)
    {
        return new JTextFieldDoubleVerifier(textField, min, max);
    }

    public JTextFieldDoubleVerifier(JTextField textField)
    {
        this(textField, -Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public JTextFieldDoubleVerifier(final JTextField textField, double min, double max)
    {
        lastGood = textField.getText();
        this.min = min;
        this.max = max;

        // We need a document listener to verify changes that are done programmatically
        // (not via the keyboard or mouse). We only do this update if the text field
        // does not have the focus so that the field is not reverted while the user is
        // editing it.
        textField.getDocument().addDocumentListener(new DocumentListener()
        {
            public void removeUpdate(DocumentEvent e)
            {
                changedUpdate(e);
            }

            public void insertUpdate(DocumentEvent e)
            {
                changedUpdate(e);
            }

            public void changedUpdate(DocumentEvent arg0)
            {
                if (!textField.isFocusOwner())
                    verify(textField);
            }
        });
    }

    public boolean verify(JComponent input)
    {
        final JTextField textField = (JTextField)input;
        String text = textField.getText().trim();

        try
        {
            double v = Double.parseDouble(text);
            if (v < min || v > max)
                throw new NumberFormatException();
            lastGood = text;
        }
        catch (NumberFormatException e)
        {
            javax.swing.SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    textField.setText(lastGood);
                }
            });
        }

        return true;
    }

}

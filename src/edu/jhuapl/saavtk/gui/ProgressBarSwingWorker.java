package edu.jhuapl.saavtk.gui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

abstract public class ProgressBarSwingWorker extends SwingWorker<Void, Void>
        implements PropertyChangeListener
{
    private JProgressBar progressBar;
    private JButton cancelButton;
    private JLabel label;
    private ProgressDialog dialog;
    private volatile boolean indeterminate = false;
    private volatile String labelText = " ";
    private volatile boolean enableCancelButton = true;
    private volatile double completionTimeEstimate = -1.0; // in seconds

    private class ProgressDialog extends JDialog implements ActionListener
    {

        public ProgressDialog(Component c)
        {
            super(JOptionPane.getFrameForComponent(c));
            JPanel panel = new JPanel(new MigLayout());
            setPreferredSize(new Dimension(375, 150));

            label = new JLabel(" ");

            progressBar = new JProgressBar(0, 100);
            progressBar.setPreferredSize(new Dimension(350, 20));
            panel.add(label, "wrap");
            panel.add(progressBar, "wrap");

            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(this);

            setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            panel.add(cancelButton, "align center");

            setLocationRelativeTo(JOptionPane.getFrameForComponent(c));

            add(panel);
            pack();
        }

        public void actionPerformed(ActionEvent e)
        {
            cancel(true);

            dialog.setVisible(false);
            dialog.dispose();
        }
    }

    public ProgressBarSwingWorker(Component c, String title)
    {
        dialog = new ProgressDialog(c);
        dialog.setTitle(title);

        addPropertyChangeListener(this);
    }

    /**
     * Executes the worker and shows the dialog. Also updates the dialog text
     * if setLabelText was called.
     */
    public void executeDialog()
    {
        label.setText(labelText);

        // Note execute must be called BEFORE setVisible. Otherwise, the worker thread
        // won't run since setVisible blocks until the dialog closes.
        execute();

        while (true)
        {
            if (completionTimeEstimate >= 4.0 || completionTimeEstimate < 0.0)
            {
                break;
            }
            else if (isDone())
            {
                dialog.dispose();
                return;
            }

            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
            }
        }

        dialog.setVisible(true);
    }

    protected void setIndeterminate(boolean indeterminate)
    {
        this.indeterminate = indeterminate;
    }

    protected void setLabelText(String labelText)
    {
        this.labelText = labelText;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if ("progress".equals(evt.getPropertyName()))
        {
            int progress = (Integer) evt.getNewValue();

            if (indeterminate)
                progressBar.setIndeterminate(true);
            else
                progressBar.setValue(progress);

            label.setText(labelText);

            if (cancelButton.isEnabled() != enableCancelButton)
                cancelButton.setEnabled(enableCancelButton);
        }

        if (evt.getNewValue().equals(SwingWorker.StateValue.DONE))
        {
            dialog.setVisible(false);
            dialog.dispose();
        }
    }

    public void setCancelButtonEnabled(boolean b)
    {
        enableCancelButton = b;
    }

    public void setCompletionTimeEstimate(double completionTimeEstimate)
    {
        // Only allow setting completion time once
        if (this.completionTimeEstimate < 0.0)
            this.completionTimeEstimate = completionTimeEstimate;
    }
}

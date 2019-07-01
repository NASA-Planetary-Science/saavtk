package edu.jhuapl.saavtk.gui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

public class ProgressMonitor
{
    protected static final DecimalFormat PF = new DecimalFormat("0%");

    public static ProgressMonitor of(Component c, String title, SwingWorker<?, ?> swingWorker, boolean indeterminate)
    {
        ProgressMonitor result = new ProgressMonitor(c, title, swingWorker, indeterminate);

        return result;
    }

    protected static void addUpdateListener(ProgressMonitor monitor, SwingWorker<?, ?> swingWorker)
    {
        swingWorker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName()))
            {
                monitor.updateProgress((int) evt.getNewValue());
            }
            else if (evt.getNewValue().equals(SwingWorker.StateValue.DONE))
            {
                monitor.dismiss();
            }
        });
    }

    private final SwingWorker<?, ?> swingWorker;
    private final JDialog dialog;
    private final JLabel label;
    private final JProgressBar progressBar;

    protected ProgressMonitor(Component c, String title, SwingWorker<?, ?> swingWorker, boolean indeterminate)
    {
        JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(c));
        dialog.setTitle(title);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setLocationRelativeTo(JOptionPane.getFrameForComponent(c));

        JLabel label = new JLabel(" ");

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(350, 20));
        progressBar.setIndeterminate(indeterminate);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            // Note that the argument of cancel indicates whether or not to generate an
            // interrupt.
            swingWorker.cancel(true);

            dialog.setVisible(false);
            dialog.dispose();
        });

        JPanel panel = new JPanel(new MigLayout());
        panel.setPreferredSize(new Dimension(375, 200));

        panel.add(label, "wrap");
        panel.add(progressBar, "wrap");
        panel.add(cancelButton, "align center");

        dialog.add(panel);

        swingWorker.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName()))
            {
                updateProgress((int) evt.getNewValue());
            }
            else if (evt.getNewValue().equals(SwingWorker.StateValue.DONE))
            {
                dismiss();
            }
        });

        this.swingWorker = swingWorker;
        this.dialog = dialog;
        this.label = label;
        this.progressBar = progressBar;
    }

    public void execute()
    {
        dialog.pack();

        // Note execute must be called BEFORE setVisible. Otherwise, the worker thread
        // won't run for a modal dialog box, since setVisible blocks until the dialog
        // closes.
        swingWorker.execute();

        // Wait a little before showing the dialog, in case the job just finishes
        // quickly.
        try
        {
            Thread.sleep(333);
        }
        catch (@SuppressWarnings("unused") InterruptedException ignored)
        {}

        if (!swingWorker.isDone())
        {
            dialog.setVisible(true);
        }
    }

    public void updateProgress(int progress)
    {
        progressBar.setValue(progress);
        label.setText(createProgressMessage());
    }

    public void dismiss()
    {
        dialog.setVisible(false);
        dialog.dispose();
    }

    protected SwingWorker<?, ?> getSwingWorker()
    {
        return swingWorker;
    }

    protected String createProgressMessage()
    {
        return PF.format(swingWorker.getProgress() / 100.) + " complete";
    }

}

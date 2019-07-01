package edu.jhuapl.saavtk.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import net.miginfocom.swing.MigLayout;

public abstract class ProgressBarSwingWorker extends SwingWorker<Void, Void>
{
    private final ProgressDialog dialog;
    private final double completionTimeThreshold;
    private volatile double completionTimeEstimate;

    private class ProgressDialog extends JDialog implements ActionListener
    {
        private static final long serialVersionUID = 1L;
        private final JLabel label;
        private final JProgressBar progressBar;
        private final JButton cancelButton;

        private ProgressDialog(Component c, boolean indeterminate)
        {
            super(JOptionPane.getFrameForComponent(c), ModalityType.APPLICATION_MODAL);
            JPanel panel = new JPanel(new MigLayout());
            setPreferredSize(new Dimension(375, 150));

            label = new JLabel(" ");

            progressBar = new JProgressBar(0, 100);
            progressBar.setPreferredSize(new Dimension(350, 20));
            progressBar.setIndeterminate(indeterminate);

            panel.add(label, "wrap");
            panel.add(progressBar, "wrap");

            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(this);

            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            panel.add(cancelButton, "align center");

            setLocationRelativeTo(JOptionPane.getFrameForComponent(c));

            add(panel);
            pack();
        }

        @Override
        public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
        {
            cancel(true);

            dialog.setVisible(false);
            dialog.dispose();
        }
    }

    public ProgressBarSwingWorker(Component c, String title, boolean indeterminate)
    {
        this(c, title, null, indeterminate);
    }

    public ProgressBarSwingWorker(Component c, String title, String labelText, boolean indeterminate)
    {
        this.dialog = new ProgressDialog(c, indeterminate);
        this.completionTimeThreshold = 4.0;
        this.completionTimeEstimate = -1.;

        dialog.setTitle(title);

        if (labelText != null)
        {
            dialog.label.setText(labelText);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * The dialog will only be shown if the time-to-complete the task exceeds the
     * completion time threshold. The time-to-complete is set by calling
     * updateProgressDialog. Thus, implementers of doInBackground must call
     * updateProgressDialog() at least once in order to (possibly) display the
     * dialog.
     */
    @Override
    protected abstract Void doInBackground() throws Exception;

    /**
     * Execute the task provided by this swing worker (as specified in
     * doInBackground) on a background thread and wait for it to finish on the Event
     * Dispatch Thread, showing a progress dialog if the estimated completion time
     * exceeds the completion time threshold provided to the constructor.
     */
    public void executeDialog()
    {
        Runnable runnable = () -> {
            try
            {
                // Dialog will be shown as needed via a call to updateProgressDialog
                // after execution begins.
                execute();

                while (!isDone())
                {
                    if (dialog.progressBar.isIndeterminate() || completionTimeEstimate > completionTimeThreshold)
                    {
                        dialog.setVisible(true);
                        break;
                    }

                    try
                    {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                        break;
                    }
                }
            }
            finally
            {
                dialog.setVisible(false);
                dialog.dispose();
            }
        };

        if (EventQueue.isDispatchThread())
        {
            runnable.run();
        }
        else
        {
            try
            {
                EventQueue.invokeAndWait(runnable);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void done()
    {
        super.done();
        dialog.setVisible(false);
        dialog.dispose();
    }

    protected void updateCompletionTimeEstimate(double completionTimeEstimate)
    {
        this.completionTimeEstimate = completionTimeEstimate;
    }

    protected void updateProgressDialog(String labelText)
    {
        Runnable runnable = () -> {
            dialog.label.setText(labelText);
            dialog.progressBar.setIndeterminate(true);
        };

        runOnEdt(runnable);
    }

    protected void updateProgressDialog(int progress)
    {
        setProgress(progress);

        Runnable runnable = () -> {
            dialog.progressBar.setValue(progress);
        };

        runOnEdt(runnable);
    }

    protected void updateProgressDialog(String labelText, int progress)
    {
        setProgress(progress);

        Runnable runnable = () -> {
            dialog.label.setText(labelText);
            dialog.progressBar.setValue(progress);
        };

        runOnEdt(runnable);
    }

    protected int computeProgress(long unpackedByteCount, long totalUnpackedByteCount)
    {
        double progress = 0.;
        if (totalUnpackedByteCount > 0)
        {
            progress = Math.min(Math.max(100. * unpackedByteCount / totalUnpackedByteCount, 0.), 100.);
        }

        return (int) progress;
    }

    protected void checkNotCanceled(String hint) throws InterruptedException
    {
        if (isCancelled())
        {
            throw new InterruptedException(hint);
        }
    }

    protected void runOnEdt(Runnable runnable)
    {
        if (EventQueue.isDispatchThread())
        {
            runnable.run();
        }
        else
        {
            EventQueue.invokeLater(() -> {
                runnable.run();
            });
        }
    }

}

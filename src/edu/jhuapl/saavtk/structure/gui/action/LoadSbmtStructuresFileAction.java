package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.gui.StructurePanel;
import edu.jhuapl.saavtk.util.ProgressListener;

/**
 * Object that defines the behavior associated with this action.
 * <P>
 * This class was originally part of the single monolithic file:
 * edu.jhuaple.saavtk.gui.panel.AbstractStructureMappingControlPanel.
 * <P>
 * Subclasses that were implementations of {@link Action} have been refactored
 * to the package edu.jhuapl.saavtk.structure.gui.action on ~2019Sep09.
 */
public class LoadSbmtStructuresFileAction<G1 extends Structure> extends AbstractAction implements PropertyChangeListener
{
	// Constants
	private static final long serialVersionUID = 1L;

	// Ref vars
	private final StructurePanel<G1> refParent;
	private final StructureManager<G1> refStructureManager;

	private ProgressMonitor structuresLoadingProgressMonitor;
	private StructuresLoadingTask task;

	public LoadSbmtStructuresFileAction(StructurePanel<G1> aParent, StructureManager<G1> aManager)
	{
		super("SBMT Structures File...");

		refParent = aParent;
		refStructureManager = aManager;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		File file = CustomFileChooser.showOpenDialog(refParent, "Select File");
		if (file == null)
			return;

		try
		{
			// If there are already structures, ask user if they want to
			// append or overwrite them
			boolean append = false;
			if (refStructureManager.getNumItems() > 0)
			{
				Object[] options = { "Append", "Replace" };
				int n = JOptionPane.showOptionDialog(refParent,
						"Would you like to append to or replace the existing structures?", "Append or Replace?",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				append = (n == 0 ? true : false);
			}
			structuresLoadingProgressMonitor = new ProgressMonitor(null, "Loading Structures...", "", 0, 100);
			structuresLoadingProgressMonitor.setProgress(0);

			task = new StructuresLoadingTask(file, append);
			task.addPropertyChangeListener(this);
			task.execute();

		}
		catch (Exception aExp)
		{
			JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(refParent),
					"There was an error reading the file.", "Error", JOptionPane.ERROR_MESSAGE);

			aExp.printStackTrace();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if ("progress".equals(evt.getPropertyName()) == true)
		{
			int progress = (Integer) evt.getNewValue();
			structuresLoadingProgressMonitor.setProgress(progress);
			String message = String.format("Completed %d%%.\n", progress);
			structuresLoadingProgressMonitor.setNote(message);
			if (structuresLoadingProgressMonitor.isCanceled() || task.isDone())
			{
				if (structuresLoadingProgressMonitor.isCanceled())
				{
					task.cancel(true);
				}
			}
		}
	}

	class StructuresLoadingTask extends SwingWorker<Void, Void>
	{
		private final File file;
		private final boolean append;

		public StructuresLoadingTask(File file, boolean append)
		{
			this.file = file;
			this.append = append;
		}

		@Override
		protected Void doInBackground() throws Exception
		{
			refStructureManager.loadModel(file, append, new ProgressListener() {
				@Override
				public void setProgress(int progress)
				{
					task.setProgress(progress);
				}

			});

			refParent.notifyFileLoaded(file);
			return null;
		}

		@Override
		protected void done()
		{
			// TODO Auto-generated method stub
			super.done();
		}

	}

}

package edu.jhuapl.saavtk.popup;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.model.ModelManager;
import edu.jhuapl.saavtk.model.ModelNames;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.io.StructureSaveUtil;

public class LinesPopupMenu extends StructuresPopupMenu<PolyLine>
{
	private final PolyhedralModel refSmallBody;
	private final JMenuItem saveProfileMI;

	public LinesPopupMenu(ModelManager modelManager, Renderer renderer)
	{
		super(modelManager, renderer, ModelNames.LINE_STRUCTURES);

		refSmallBody = modelManager.getPolyhedralModel();

		saveProfileMI = new JMenuItem(new SaveProfileAction());
		saveProfileMI.setText("Save Profile...");
		add(saveProfileMI);
	}

	@Override
	public void show(Component invoker, int x, int y)
	{
		// Disable certain items if more than one structure is selected
		boolean isEnabled = getManager().getSelectedItems().size() == 1;
		saveProfileMI.setEnabled(isEnabled);

		super.show(invoker, x, y);
	}

	private class SaveProfileAction extends AbstractAction
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			// Save only if one line is selected
			List<PolyLine> pickL = getManager().getSelectedItems().asList();
			if (pickL.size() != 1)
				return;

			// Prompt the user for a file
			File file = CustomFileChooser.showSaveDialog(getInvoker(), "Save Profile", "profile.csv");
			if (file == null)
				return;

			try
			{
				// Saving of profiles requires at 2 control points
				PolyLine tmpLine = pickL.get(0);
				if (tmpLine.getControlPoints().size() != 2)
					throw new Exception("Line must contain exactly 2 control points.");

				// Delegate actual saving
				List<Vector3D> xyzPointL = ((LineModel<PolyLine>) getManager()).getXyzPointsFor(tmpLine);
				StructureSaveUtil.saveProfile(file, refSmallBody, xyzPointL);
			}
			catch (Exception aExp)
			{
				aExp.printStackTrace();
				JOptionPane.showMessageDialog(getInvoker(),
						aExp.getMessage() != null ? aExp.getMessage() : "An error occurred saving the profile.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
	}
}

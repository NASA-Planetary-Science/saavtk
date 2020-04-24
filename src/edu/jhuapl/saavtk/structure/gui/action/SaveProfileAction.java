package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Component;
import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureManager;
import edu.jhuapl.saavtk.structure.io.StructureSaveUtil;
import glum.gui.action.PopAction;

/**
 * {@link PopAction} that will save the profile plot of a single
 * {@link PolyLine}.
 * <P>
 * The {@link PolyLine} must have only 2 points.
 * 
 * @author lopeznr1
 */
public class SaveProfileAction<G1 extends Structure> extends PopAction<G1>
{
	// Ref vars
	private final StructureManager<G1> refManager;
	private final PolyhedralModel refSmallBody;
	private final Component refParent;

	/**
	 * Standard Constructor
	 */
	public SaveProfileAction(StructureManager<G1> aManager, PolyhedralModel aSmallBody, Component aParent)
	{
		refManager = aManager;
		refSmallBody = aSmallBody;
		refParent = aParent;
	}

	@Override
	public void executeAction(List<G1> aItemL)
	{
		// Bail if a single item is not selected
		if (aItemL.size() != 1)
			return;

		// Prompt the user for a file
		File file = CustomFileChooser.showSaveDialog(refParent, "Save Profile", "profile.csv");
		if (file == null)
			return;

		try
		{
			// Saving of profiles requires exactly 2 control points
			PolyLine tmpLine = (PolyLine) aItemL.get(0);
			if (tmpLine.getControlPoints().size() != 2)
				throw new Exception("Line must contain exactly 2 control points.");

			// Delegate actual saving
			List<Vector3D> xyzPointL = ((LineModel<PolyLine>) refManager).getXyzPointsFor(tmpLine);
			StructureSaveUtil.saveProfile(file, refSmallBody, xyzPointL);
		}
		catch (Exception aExp)
		{
			aExp.printStackTrace();
			JOptionPane.showMessageDialog(refParent,
					aExp.getMessage() != null ? aExp.getMessage() : "An error occurred saving the profile.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	@Override
	public void setChosenItems(Collection<G1> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Enable the item if the number of selected items == 1
		boolean isEnabled = aItemC.size() == 1;
		aAssocMI.setEnabled(isEnabled);
	}

}

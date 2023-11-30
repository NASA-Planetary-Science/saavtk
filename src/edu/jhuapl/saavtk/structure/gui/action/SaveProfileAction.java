package edu.jhuapl.saavtk.structure.gui.action;

import java.awt.Component;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import edu.jhuapl.saavtk.gui.dialog.CustomFileChooser;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.structure.AnyStructureManager;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.StructureType;
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
public class SaveProfileAction extends PopAction<Structure>
{
	// Ref vars
	private final AnyStructureManager refManager;
	private final PolyhedralModel refSmallBody;
	private final Component refParent;

	/**
	 * Standard Constructor
	 */
	public SaveProfileAction(AnyStructureManager aManager, PolyhedralModel aSmallBody, Component aParent)
	{
		refManager = aManager;
		refSmallBody = aSmallBody;
		refParent = aParent;
	}

	@Override
	public void executeAction(List<Structure> aItemL)
	{
		// Bail if a single item is not selected
		if (aItemL.size() != 1)
			return;

		// Prompt the user for a file
		var file = CustomFileChooser.showSaveDialog(refParent, "Save Profile", "profile.csv");
		if (file == null)
			return;

		try
		{
			// Saving of profiles requires exactly 2 control points
			PolyLine tmpLine = (PolyLine) aItemL.get(0);
			if (tmpLine.getControlPoints().size() != 2)
				throw new Exception("Line must contain exactly 2 control points.");

			// Delegate actual saving
			var xyzPointL = refManager.getXyzPointsFor(tmpLine);
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
	public void setChosenItems(Collection<Structure> aItemC, JMenuItem aAssocMI)
	{
		super.setChosenItems(aItemC, aAssocMI);

		// Enable the item only if 1 item and the type is a Path
		var isEnabled = aItemC.size() == 1;
		isEnabled &= aItemC.iterator().next().getType() == StructureType.Path;
		aAssocMI.setEnabled(isEnabled);
	}

}

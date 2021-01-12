package edu.jhuapl.saavtk.model.structure;

import java.text.DecimalFormat;
import java.util.Collection;

import edu.jhuapl.saavtk.gui.render.SceneChangeNotifier;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.status.StatusNotifier;
import edu.jhuapl.saavtk.structure.Ellipse;

/**
 * Model of circle structures drawn on a body.
 */
public class CircleSelectionModel extends AbstractEllipsePolygonModel
{
	// State vars
	private DecimalFormat decimalFormat;

	/** Standard Constructor */
	public CircleSelectionModel(SceneChangeNotifier aSceneChangeNotifier, StatusNotifier aStatusNotifier,
			PolyhedralModel aSmallBody)
	{
		super(aSceneChangeNotifier, aStatusNotifier, aSmallBody, 20, Mode.CIRCLE_MODE);

		decimalFormat = new DecimalFormat("#.#####");
	}

	@Override
	protected void updateStatus(StatusNotifier aStatusNotifier, Collection<Ellipse> aItemC, boolean aIsUpdate)
	{
		String tmpMsg = null;
		if (aItemC.size() == 1)
		{
			Ellipse tmpItem = aItemC.iterator().next();
			double tmpDiam = 2.0 * tmpItem.getRadius();
			tmpMsg = "Selection, Id: " + tmpItem.getId();
			tmpMsg += ", Diam: " + decimalFormat.format(tmpDiam) + " km";
		}
		else if (aItemC.size() > 1)
		{
			if (aIsUpdate == true)
				tmpMsg = "Multiple selections mutated: " + aItemC.size();
			else
				tmpMsg = "Multiple selections selected: " + aItemC.size();
		}

		aStatusNotifier.setPriStatus(tmpMsg, null);
	}

}

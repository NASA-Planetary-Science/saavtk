package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;

import edu.jhuapl.saavtk.gui.render.SceneChangeNotifier;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.status.StatusNotifier;

/**
 * Model of line structures drawn on a body.
 */
public class PointModel extends AbstractEllipsePolygonModel
{
	/** Standard Constructor */
	public PointModel(SceneChangeNotifier aSceneChangeNotifier, StatusNotifier aStatusNotifier,
			PolyhedralModel aSmallBody)
	{
		super(aSceneChangeNotifier, aStatusNotifier, aSmallBody, 4, Mode.POINT_MODE);
		setInteriorOpacity(1.0);
		setDefaultColor(Color.MAGENTA);

		// Size the radius relative to a circle whose area equals the minimum cell area.
		// Overall multiplier found experimentally to be good for getting the points big enough.
		final double multiplier = 2.;

		double radius = multiplier * Math.sqrt(aSmallBody.getMinCellArea() / Math.PI);
		// Unfortunately, there is no guarantee that getMinCellArea will return a
		// physically reasonable value.
		if (Double.compare(radius, 0.) <= 0)
		{
			radius = multiplier * Math.sqrt(aSmallBody.getMeanCellArea() / Math.PI);
		}

		// Unfortunately, there is no guarantee that getMeanCellArea will return a
		// physically reasonable value either.
		if (Double.compare(radius, 0.) <= 0)
		{
			radius = 100.; // 100 m = best guess at a reasonably "safe" default value.
		}

		// Make the value more pretty to look at/rounded reasonably.
		double factor = 1.;
		while (radius < 1.)
		{
			radius *= 10.;
			factor *= 10.;
		}
		radius = Math.ceil(radius) / factor;
		setDefaultRadius(radius);
	}
}

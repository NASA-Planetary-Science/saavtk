package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;

import edu.jhuapl.saavtk.model.PolyhedralModel;


/**
 * Model of line structures drawn on a body.
 */
public class PointModel extends AbstractEllipsePolygonModel
{
    public PointModel(PolyhedralModel smallBodyModel)
    {
        super(smallBodyModel, 4, Mode.POINT_MODE, "point");
        setInteriorOpacity(1.0);
        setDefaultColor(Color.MAGENTA);
        // Size the radius relative to a circle whose area equals the minimum cell area.
        // Overall multiplier found experimentally to be good for getting the points big enough.
        final double multiplier = 2.;

        double radius = multiplier * Math.sqrt(smallBodyModel.getMinCellArea() / Math.PI);
        // Unfortunately, there is no guarantee that getMinCellArea will return a physically reasonable value.
        if (Double.compare(radius, 0.) <= 0)
        {
        	radius = multiplier * Math.sqrt(smallBodyModel.getMeanCellArea() / Math.PI);
        }

        // Unfortunately, there is no guarantee that getMeanCellArea will return a physically reasonable value either.
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

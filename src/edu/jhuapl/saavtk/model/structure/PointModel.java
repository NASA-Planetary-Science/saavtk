package edu.jhuapl.saavtk.model.structure;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;


/**
 * Model of line structures drawn on a body.
 */
public class PointModel extends AbstractEllipsePolygonModel
{
    public PointModel(PolyhedralModel smallBodyModel)
    {
        super(smallBodyModel, 4, Mode.POINT_MODE, "point");
        setInteriorOpacity(1.0);
        int[] color = {255, 0, 255};
        setDefaultColor(color);
    }
}

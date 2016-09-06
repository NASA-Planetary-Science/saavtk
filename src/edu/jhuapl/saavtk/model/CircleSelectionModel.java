package edu.jhuapl.saavtk.model;


/**
 * Model of circle structures drawn on a body.
 */
public class CircleSelectionModel extends AbstractEllipsePolygonModel
{
    public CircleSelectionModel(PolyhedralModel smallBodyModel)
    {
        super(smallBodyModel, 20, Mode.CIRCLE_MODE, "Selection");
        //setInteriorOpacity(1.0);
        //int[] color = {255, 0, 255};
        //setDefaultColor(color);
    }
}
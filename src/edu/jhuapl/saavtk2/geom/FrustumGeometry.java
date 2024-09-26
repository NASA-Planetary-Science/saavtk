package edu.jhuapl.saavtk2.geom;

import edu.jhuapl.saavtk2.geom.euclidean.BoundedFrustum;

public class FrustumGeometry extends BasicGeometry {

	public FrustumGeometry(BoundedFrustum frustum) {
		super(BoundedFrustum.createPolyDataRepresentation(frustum));
	}

}

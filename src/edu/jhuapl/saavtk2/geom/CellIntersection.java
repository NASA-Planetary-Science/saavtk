package edu.jhuapl.saavtk2.geom;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class CellIntersection {

	int cellId;
	Vector3D hitPosition;

	public CellIntersection(int cellId, Vector3D hitPosition) {
		this.cellId = cellId;
		this.hitPosition = hitPosition;
	}
	public int getCellId() {
		return cellId;
	}
	public Vector3D getHitPosition() {
		return hitPosition;
	}
	
	
}

package edu.jhuapl.saavtk2.geom;

import java.util.Comparator;

import edu.jhuapl.saavtk2.geom.euclidean.Line;

public class CellIntersectionComparator implements Comparator<CellIntersection> {

	BarycentricVectorComparator vecCompare;
	
	public CellIntersectionComparator(Line line) {
		vecCompare=new BarycentricVectorComparator(line);
	}

	@Override
	public int compare(CellIntersection o1, CellIntersection o2) {
		return vecCompare.compare(o1.getHitPosition(), o2.getHitPosition());
	}

}

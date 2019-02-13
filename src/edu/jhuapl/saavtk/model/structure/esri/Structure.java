package edu.jhuapl.saavtk.model.structure.esri;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface Structure
{
	public String getLabel();
	public Vector3D getCentroid();
}

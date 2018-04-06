package edu.jhuapl.saavtk2.image.keys;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;

public interface ImageKey
{
	int getNumberOfBandKeys();
	BandKey getSubKey(int band);
	GenericPolyhedralModel getBodyModel();
	String getDisplayName();
}

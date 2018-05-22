package edu.jhuapl.saavtk2.image.keys;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;

public abstract class SingleBandImageKey implements ImageKey
{
	BandKey subKey;
	GenericPolyhedralModel bodyModel;
	
	public SingleBandImageKey(GenericPolyhedralModel bodyModel, BandKey subKey)
	{
		this.subKey=subKey;
		this.bodyModel=bodyModel;
	}
	
	@Override
	public int getNumberOfBandKeys()
	{
		return 1;
	}

	@Override
	public BandKey getSubKey(int band)
	{
		return subKey;
	}
	
	@Override
	public GenericPolyhedralModel getBodyModel()
	{
		return bodyModel;
	} 
	
}

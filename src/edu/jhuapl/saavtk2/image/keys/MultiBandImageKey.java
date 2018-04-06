package edu.jhuapl.saavtk2.image.keys;

import java.util.List;

import com.google.common.collect.Lists;

import edu.jhuapl.saavtk.model.GenericPolyhedralModel;

public abstract class MultiBandImageKey implements ImageKey
{
	List<BandKey> subKeys=Lists.newArrayList();
	GenericPolyhedralModel bodyModel;
	
	public MultiBandImageKey(GenericPolyhedralModel bodyModel, BandKey... subKeys)
	{
		this.bodyModel=bodyModel;
		for (int i=0; i<subKeys.length; i++)
			this.subKeys.add(subKeys[i]);
	}
	
	public MultiBandImageKey(GenericPolyhedralModel bodyModel, List<BandKey> subKeys)
	{
		this.bodyModel=bodyModel;
		for (int i=0; i<subKeys.size(); i++)
			this.subKeys.add(subKeys.get(i));
	}

	@Override
	public int getNumberOfBandKeys()
	{
		return subKeys.size();
	}

	@Override
	public BandKey getSubKey(int band)
	{
		return subKeys.get(band);
	}
	
	@Override
	public GenericPolyhedralModel getBodyModel()
	{
		return bodyModel;
	} 
	
}

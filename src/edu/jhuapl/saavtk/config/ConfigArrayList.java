package edu.jhuapl.saavtk.config;

import java.util.ArrayList;

public class ConfigArrayList<L extends IViewConfig> extends ArrayList<L>
{

	public ConfigArrayList()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean add(L e)
	{
		if (!contains(e))
		{
			return super.add(e);
		}
		return false;
	}
	
}

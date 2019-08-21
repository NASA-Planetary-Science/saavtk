package edu.jhuapl.saavtk.config;

import java.util.ArrayList;

public class ConfigArrayList extends ArrayList<ViewConfig>
{

	public ConfigArrayList()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean add(ViewConfig e)
	{
		if (!contains(e))
		{
			return super.add(e);
		}
		return false;
	}
	
}

package edu.jhuapl.saavtk.model;

import java.util.List;

public interface IPositionOrientationManager<B>
{
	public List<B> getUpdatedBodies();
		
	public void run(double time) throws Exception;
}

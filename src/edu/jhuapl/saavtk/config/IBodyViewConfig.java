package edu.jhuapl.saavtk.config;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;

public interface IBodyViewConfig extends IViewConfig 
{
	ShapeModelType getAuthor();
	
	void setAuthor(ShapeModelType author);
	
	String getVersion();
	
	ShapeModelBody getBody();
	
	void setCustomTemporary(boolean customTemp);
	
	public boolean isCustomTemporary();
	
	public boolean isHasCustomBodyCubeSize();

	public double getCustomBodyCubeSize();
	
	public boolean isUseMinimumReferencePotential();
	
	public default boolean hasSystemBodies() { return false; };
}

package edu.jhuapl.saavtk.config;

import edu.jhuapl.saavtk.model.ShapeModelBody;
import edu.jhuapl.saavtk.model.ShapeModelType;

public interface IBodyViewConfig extends IViewConfig 
{
	ShapeModelType getAuthor();
	
	public void setAuthor(ShapeModelType author);
	
	String getVersion();
	
	public ShapeModelBody getBody();
	
	public void setModelLabel(String modelLabel);
	
	public void setCustomTemporary(boolean customTemp);
	
	public boolean isCustomTemporary();
	
	public boolean isHasCustomBodyCubeSize();

	public double getCustomBodyCubeSize();
	
	public boolean isUseMinimumReferencePotential();
	
	public default boolean hasSystemBodies() { return false; };
	
	public String getRootDirOnServer();
	
	public boolean hasColoringData();
	
	public double getDensity();
	
	public double getRotationRate();
	
	public String serverPath(String fileName);
}

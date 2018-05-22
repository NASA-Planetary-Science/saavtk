package edu.jhuapl.saavtk2.image.projection.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import edu.jhuapl.saavtk2.util.Frustum;
import edu.jhuapl.saavtk2.image.projection.CylindricalProjection;
import edu.jhuapl.saavtk2.image.projection.PerspectiveProjection;
import edu.jhuapl.saavtk2.image.projection.Projection;

public class ProjFileReader implements ProjectionReader, ProjectionWriter
{
	
	static enum ProjectionKeys
	{
		CLASSNAME,ORIGIN,UPPER_LEFT_UNITVEC,UPPER_RIGHT_UNITVEC,LOWER_LEFT_UNITVEC,LOWER_RIGHT_UNITVEC,HORZ_MIN,HORZ_MAX,VERT_MIN,VERT_MAX;
	}

	public static boolean extensionIsSupported(String ext)
	{
		return ext.toLowerCase().equals("proj");
	}
	
	@Override
	public Projection read(File file)
	{
		Properties properties=new Properties();
		try
		{
			properties.load(new BufferedInputStream(new FileInputStream(file)));
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Vector3D origin=vectorFromString(properties.getProperty(ProjectionKeys.ORIGIN.toString()));
		Vector3D ul=vectorFromString(properties.getProperty(ProjectionKeys.UPPER_LEFT_UNITVEC.toString()));
		Vector3D ur=vectorFromString(properties.getProperty(ProjectionKeys.UPPER_RIGHT_UNITVEC.toString()));
		Vector3D ll=vectorFromString(properties.getProperty(ProjectionKeys.LOWER_LEFT_UNITVEC.toString()));
		Vector3D lr=vectorFromString(properties.getProperty(ProjectionKeys.LOWER_RIGHT_UNITVEC.toString()));
		double horzmin=doubleFromString(properties.getProperty(ProjectionKeys.HORZ_MIN.toString()));
		double horzmax=doubleFromString(properties.getProperty(ProjectionKeys.HORZ_MAX.toString()));
		double vertmin=doubleFromString(properties.getProperty(ProjectionKeys.VERT_MIN.toString()));
		double vertmax=doubleFromString(properties.getProperty(ProjectionKeys.VERT_MAX.toString()));
		String classname=properties.getProperty(ProjectionKeys.CLASSNAME.toString());
		if (classname.equals(PerspectiveProjection.class.getName()))
			return new PerspectiveProjection(new Frustum(origin, ul, ur, ll, lr));
		else if (classname.equals(CylindricalProjection.class.getName()))
			return new CylindricalProjection(vertmin, vertmax, horzmin, horzmax);
		else
			return null;
	}

	@Override
	public void write(Projection projection, File file)
	{
		Properties properties=new Properties();
		properties.setProperty(ProjectionKeys.CLASSNAME.toString(), projection.getClass().getName());
		properties.setProperty(ProjectionKeys.ORIGIN.toString(), toString(projection.getRayOrigin()));
		properties.setProperty(ProjectionKeys.UPPER_LEFT_UNITVEC.toString(), toString(projection.getUpperLeftUnit()));
		properties.setProperty(ProjectionKeys.UPPER_RIGHT_UNITVEC.toString(), toString(projection.getUpperRightUnit()));
		properties.setProperty(ProjectionKeys.LOWER_LEFT_UNITVEC.toString(), toString(projection.getLowerLeftUnit()));
		properties.setProperty(ProjectionKeys.LOWER_RIGHT_UNITVEC.toString(), toString(projection.getLowerRightUnit()));
		properties.setProperty(ProjectionKeys.HORZ_MIN.toString(), toString(projection.getHorizontalMin()));
		properties.setProperty(ProjectionKeys.HORZ_MAX.toString(), toString(projection.getHorizontalMax()));
		properties.setProperty(ProjectionKeys.VERT_MIN.toString(), toString(projection.getVerticalMin()));
		properties.setProperty(ProjectionKeys.VERT_MAX.toString(), toString(projection.getVerticalMax()));
		try
		{
			properties.store(new BufferedOutputStream(new FileOutputStream(file)), "");
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected String toString(double d)
	{
		return BigDecimal.valueOf(d).toString();
	}
	
	protected String toString(Vector3D v)
	{
		return toString(v.getX())+","+toString(v.getY())+","+toString(v.getZ());
	}
	
	protected double doubleFromString(String str)
	{
		return new BigDecimal(str).doubleValue();
	}
	
	protected Vector3D vectorFromString(String str)
	{
		String[] tokens=str.split(",");
		double x=Double.valueOf(tokens[0]);
		double y=Double.valueOf(tokens[1]);
		double z=Double.valueOf(tokens[2]);
		return new Vector3D(x,y,z);
	}
	
}

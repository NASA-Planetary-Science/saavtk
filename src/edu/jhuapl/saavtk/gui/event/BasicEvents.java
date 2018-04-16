package edu.jhuapl.saavtk.gui.event;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class BasicEvents
{
	public static class VoidEvent extends BasicEvent<Void>
	{

		public VoidEvent(EventSource source)
		{
			super(source);
			// TODO Auto-generated constructor stub
		}
		
	}
	
    public static class BooleanEvent extends BasicEvent<Boolean>
    {

		public BooleanEvent(EventSource source, Boolean value)
		{
			super(source, value);
			// TODO Auto-generated constructor stub
		}

    }

    public static class DoubleEvent extends BasicEvent<Double>
    {

		public DoubleEvent(EventSource source, Double value)
		{
			super(source, value);
			// TODO Auto-generated constructor stub
		}

    }

    public static class StringEvent extends BasicEvent<String>
    {

		public StringEvent(EventSource source, String value)
		{
			super(source, value);
			// TODO Auto-generated constructor stub
		}

    }

    public static class DoubleArrayEvent extends BasicEvent<double[]>
    {

		public DoubleArrayEvent(EventSource source, double[] value)
		{
			super(source, value);
			// TODO Auto-generated constructor stub
		}

    }

    public static class Vector3DEvent extends BasicEvent<Vector3D>
    {

		public Vector3DEvent(EventSource source, Vector3D value)
		{
			super(source, value);
			// TODO Auto-generated constructor stub
		}
    }

}

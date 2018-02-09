package edu.jhuapl.saavtk.util;

import java.io.PrintStream;

import org.apache.commons.io.output.NullOutputStream;

public class Debug
{
	private static final PrintStream NULL_STREAM = new PrintStream(new NullOutputStream());
	private static boolean enabled = false;

	public static boolean isEnabled()
	{
		return enabled;
	}

	public static void setEnabled(boolean enabled)
	{
		Debug.enabled = enabled;
	}

	public static PrintStream err()
	{
		return Debug.enabled ? System.err : NULL_STREAM;
	}

	public static PrintStream out()
	{
		return Debug.enabled ? System.out : NULL_STREAM;
	}

	private Debug()
	{
		throw new AssertionError();
	}
}

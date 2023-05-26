package edu.jhuapl.saavtk.config;

import com.google.common.collect.ImmutableList;

public interface IViewConfig
{

	boolean isAccessible();

	void setResolution(Iterable<Integer> resolutionNumberElements);

	void setResolution(Iterable<String> resolutionLabels, Iterable<Integer> resolutionNumberElements);

	ImmutableList<String> getResolutionLabels();

	ImmutableList<Integer> getResolutionNumberElements();

	/**
	 * Return a unique name for this model. No other model may have this name. Note
	 * that only applies within built-in models or custom models but a custom model
	 * can share the name of a built-in one or vice versa. By default simply return
	 * the author concatenated with the name if the author is not null or just the
	 * name if the author is null.
	 *
	 * @return
	 */
	String getUniqueName();

	String getShapeModelName();

	String[] getShapeModelFileNames();

	boolean isEnabled();
		
	String getModelLabel();
	
	void setModelLabel(String modelLabel);
	
	String getVersion();

	public void enable(boolean enabled);
	
	public ViewConfig clone();

}
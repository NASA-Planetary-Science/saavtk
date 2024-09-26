package edu.jhuapl.saavtk.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import edu.jhuapl.saavtk.util.Properties;
import glum.item.BaseItemManager;
import glum.item.ItemManager;
import vtk.vtkProp;

/**
 * Base ItemManager class that provides the standard functionality for an
 * {@link ItemManager} and {@link Model}.
 * <P>
 * The methods that override the Model interface are copied from the
 * {@link AbstractModel} class. ItemManager should not need to implement the the
 * {@link Model} interface and at some point this class will probably go away.
 *
 * @author lopeznr1
 */
public abstract class SaavtkItemManager<G1> extends BaseItemManager<G1> implements Model
{
	// State vars
	protected final PropertyChangeSupport pcs;
	private boolean visible = true;
	private CommonData commonData;

	/**
	 * Standard Constructor
	 */
	public SaavtkItemManager()
	{
		pcs = new PropertyChangeSupport(this);
		visible = true;
		commonData = null;
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.removePropertyChangeListener(listener);
	}

	@Override
	public void setCommonData(CommonData aCommonData)
	{
		commonData = aCommonData;
	}

	@Override
	public CommonData getCommonData()
	{
		return commonData;
	}

	@Override
	public boolean isBuiltIn()
	{
		return true;
	}

	@Override
	public boolean isVisible()
	{
		return visible;
	}

	@Override
	public void setVisible(boolean b)
	{
		if (visible != b)
		{
			visible = b;
			pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
	}

	@Override
	public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
	{
		return "";
	}

	@Override
	public void setOpacity(double opacity)
	{
		// Do nothing. Subclasses should redefine this if they support opacity.
	}

	@Override
	public double getOpacity()
	{
		// Subclasses should redefine this if they support opacity.
		return 1.0;
	}

	@Override
	public void setOffset(double offset)
	{
		// Do nothing. Subclasses should redefine this if they support offset.
	}

	@Override
	public double getOffset()
	{
		// Subclasses should redefine this if they support offset.
		return 0.0;
	}

	@Override
	public double getDefaultOffset()
	{
		// Subclasses should redefine this if they support offset.
		return 0.0;
	}

	@Override
	public void delete()
	{

	}

	@Override
	public void set2DMode(boolean enable)
	{
		// do nothing by default. Subclass must override if it supports 2D mode
		// and create actors that are in 2D.
	}

	@Override
	public boolean supports2DMode()
	{
		// By default, a model does not support 2D and this function returns false
		// unless model overrides this function to return true. Model must
		// also override the function set2DMode.
		return false;
	}

}

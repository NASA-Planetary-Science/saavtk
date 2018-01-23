package edu.jhuapl.saavtk.gui.render.toolbar;

import edu.jhuapl.saavtk.gui.render.Event;
import edu.jhuapl.saavtk.gui.render.axes.CartesianAxis;
import edu.jhuapl.saavtk.gui.render.axes.CartesianViewDirection;

public class RenderToolbarEvent extends Event<RenderToolbar>
{

	public RenderToolbarEvent(RenderToolbar source)
	{
		super(source);
		// TODO Auto-generated constructor stub
	}
	
	public static class ConstrainRotationAxisEvent extends RenderToolbarEvent
	{
		CartesianAxis axis;
		
		public ConstrainRotationAxisEvent(RenderToolbar source, CartesianAxis axis)
		{
			super(source);
			this.axis=axis;
		}
		
		public CartesianAxis getAxis()
		{
			return axis;
		}
	}
	
	public static class LookAlongAxisEvent extends RenderToolbarEvent
	{
		CartesianViewDirection direction;

		public LookAlongAxisEvent(RenderToolbar source, CartesianViewDirection direction)
		{
			super(source);
			this.direction=direction;
		}
		
		public CartesianViewDirection getDirection()
		{
			return direction;
		}
	}
	
	public static class ViewAllEvent extends RenderToolbarEvent
	{

		public ViewAllEvent(RenderToolbar source)
		{
			super(source);
			// TODO Auto-generated constructor stub
		}
		
	}
	
	public static class ToggleAxesVisibilityEvent extends RenderToolbarEvent
	{
		boolean show;
		
		public ToggleAxesVisibilityEvent(RenderToolbar source, boolean show)
		{
			super(source);
			this.show=show;
		}
		
		public boolean show()
		{
			return show;
		}
	}
}

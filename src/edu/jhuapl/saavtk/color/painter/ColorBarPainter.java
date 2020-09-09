package edu.jhuapl.saavtk.color.painter;

import java.awt.Color;
import java.util.Collection;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.color.table.ColorMapAttr;
import edu.jhuapl.saavtk.color.table.ColorTableUtil;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.VtkPropProvider;
import edu.jhuapl.saavtk.structure.FontAttr;
import edu.jhuapl.saavtk.vtk.VtkFontUtil;
import edu.jhuapl.saavtk.vtk.VtkResource;
import vtk.vtkLookupTable;
import vtk.vtkProp;
import vtk.vtkProperty2D;
import vtk.vtkRenderWindowInteractor;
import vtk.vtkScalarBarActor;
import vtk.vtkScalarBarWidget;

/**
 * Painter used to render a color bar onto a vtk scene.
 *
 * @author lopeznr1
 */
public class ColorBarPainter implements VtkPropProvider, VtkResource
{
	// Reference vars
	private final Renderer refRenderer;

	// State vars
	private boolean isVisible;
	private String title;

	// VTK vars
	private vtkLookupTable vColorLT;
	private vtkScalarBarWidget vWidgetSCW;
	private vtkScalarBarActor vActorSBA;
	private vtkRenderWindowInteractor vInteractor;

	// Cache vars
	private ColorMapAttr cColorMapAttr;
	private boolean cIsHorizontal;
	private String cTitle;

	/** Standard Constructor */
	public ColorBarPainter(Renderer aRenderer)
	{
		refRenderer = aRenderer;

		isVisible = true;
		title = " ";

		vColorLT = new vtkLookupTable();
		vActorSBA = new vtkScalarBarActor();
		vActorSBA.SetOrientationToHorizontal();
		vActorSBA.SetTitle(" ");
		setBackgroundColor(new Color(0.50f, 0.50f, 0.50f, 0.50f));

		vWidgetSCW = new vtkScalarBarWidget();
		vWidgetSCW.CreateDefaultRepresentation();
		vWidgetSCW.ResizableOn();
		vWidgetSCW.SetScalarBarActor(vActorSBA);

		// Set up interaction logic
		vInteractor = refRenderer.getRenderWindowPanel().getRenderWindowInteractor();
		vInteractor.AddObserver("RenderEvent", this, "interactionKludge");
		vInteractor.AddObserver("MouseMoveEvent", this, "interactionKludge");
		vWidgetSCW.SetInteractor(vInteractor);
		vWidgetSCW.EnabledOn();

		cColorMapAttr = ColorMapAttr.Invalid;
		cIsHorizontal = vActorSBA.GetOrientation() == 0;
		cTitle = null;
	}

	/**
	 * Returns the background color of this painter.
	 */
	public Color getBackgroundColor()
	{
		vtkProperty2D vTmpP2D = vActorSBA.GetBackgroundProperty();
		double[] colorArr = vTmpP2D.GetColor();
		double opacity = vTmpP2D.GetOpacity();
		return new Color((float) colorArr[0], (float) colorArr[1], (float) colorArr[2], (float) opacity);
	}

	/**
	 * Returns the {@link FontAttr} associated with the tick labels.
	 */
	public FontAttr getFontAttrLabel()
	{
		// Delegate
		return VtkFontUtil.getFontAttr(vActorSBA.GetLabelTextProperty());
	}

	/**
	 * Returns the {@link FontAttr} associated with the title.
	 */
	public FontAttr getFontAttrTitle()
	{
		// Delegate
		return VtkFontUtil.getFontAttr(vActorSBA.GetTitleTextProperty());
	}

	/**
	 * Returns the painter's visibility.
	 */
	public boolean getIsVisible()
	{
		return isVisible;
	}

	/**
	 * Returns the number of labels.
	 */
	public int getNumberOfLabels()
	{
		return vActorSBA.GetNumberOfLabels();
	}

	/**
	 * Sets the background color of this painter.
	 */
	public void setBackgroundColor(Color aColor)
	{
		vtkProperty2D vTmpP2D = vActorSBA.GetBackgroundProperty();
		vTmpP2D.SetColor(aColor.getRed() / 255.0, aColor.getGreen() / 255.0, aColor.getBlue() / 255.0);
		vTmpP2D.SetOpacity(aColor.getAlpha() / 255.0);
		vTmpP2D.Modified();

		refRenderer.notifySceneChange();
	}

	/**
	 * Sets the {@link ColorMapAttr} for which the painter will be configured to.
	 *
	 * @param aColorMapAttr
	 */
	public void setColorMapAttr(ColorMapAttr aColorMapAttr)
	{
		// Bail if nothing has changed
		if (cColorMapAttr.equals(aColorMapAttr) == true)
			return;
		cColorMapAttr = aColorMapAttr;

		// Update the lookup table to reflect the color map
		ColorTableUtil.updateLookUpTable(vColorLT, aColorMapAttr);
		vActorSBA.SetLookupTable(vColorLT);
		vActorSBA.Modified();

		refRenderer.notifySceneChange();
	}

	/**
	 * Sets in the {@link FontAttr} associated with the tick labels.
	 */
	public void setFontAttrLabel(FontAttr aFontAttr)
	{
		// Delegate
		VtkFontUtil.setFontAttr(vActorSBA.GetLabelTextProperty(), aFontAttr);

		refRenderer.notifySceneChange();
	}

	/**
	 * Sets in the {@link FontAttr} associated with the title.
	 */
	public void setFontAttrTitle(FontAttr aFontAttr)
	{
		// Delegate
		VtkFontUtil.setFontAttr(vActorSBA.GetTitleTextProperty(), aFontAttr);

		refRenderer.notifySceneChange();
	}

	/**
	 * Sets this painter's visibility.
	 */
	public void setIsVisible(boolean aBool)
	{
		// Bail if nothing has changed
		if (isVisible == aBool)
			return;
		isVisible = aBool;

		refRenderer.notifySceneChange();
	}

	/**
	 * Sets in the number of labels.
	 */
	public void setNumberOfLabels(int aNumLabels)
	{
		// Bail if nothing has changed
		if (vActorSBA.GetNumberOfLabels() == aNumLabels)
			return;
		vActorSBA.SetNumberOfLabels(aNumLabels);

		refRenderer.notifySceneChange();
	}

	/**
	 * Sets the title of the color bar
	 */
	public void setTitle(String aTitle)
	{
		title = aTitle;
		vtkUpdateState();
	}

	/**
	 * Method that handles the on the fly font sizing / background drawing of the
	 * color bar.
	 * <P>
	 * This method is made available for VTK callback and outside code should not
	 * call this method. This method will be removed in the future.
	 */
	public void interactionKludge()
	{
		if (vWidgetSCW.GetBorderRepresentation().GetInteractionState() > 0 && isVisible)
		{
			vActorSBA.DrawBackgroundOn();
			if (vInteractor.GetControlKey() == 1)
				vWidgetSCW.GetScalarBarRepresentation().ProportionalResizeOn();
			else
				vWidgetSCW.GetScalarBarRepresentation().ProportionalResizeOff();
		}
		else
		{
			vActorSBA.DrawBackgroundOff();
		}

		vtkUpdateState();
	}

	@Override
	public Collection<vtkProp> getProps()
	{
		// Bail if not visible
		if (isVisible == false)
			return ImmutableList.of();

		// Return the list of all vtkProps
		return ImmutableList.of(vActorSBA);
	}

	@Override
	public void vtkDispose()
	{
		vWidgetSCW.Delete();
	}

	@Override
	public void vtkUpdateState()
	{
		// Bail if nothing has changed
		boolean isHorizontal = vActorSBA.GetOrientation() == 0;

		boolean isChanged = false;
		isChanged |= cIsHorizontal != isHorizontal;
		isChanged |= cTitle != title;
		if (isChanged == false)
			return;
		cIsHorizontal = isHorizontal;
		cTitle = title;

		// Determine the title (horizontal layout vs vertical layout)
		String tmpTitle = title;
		if (cIsHorizontal == false && tmpTitle.length() > 16)
			tmpTitle = tmpTitle.replaceAll("\\s+", "\n");

		// Update the vtk state
		vActorSBA.SetTitle(tmpTitle);
		vWidgetSCW.Modified();

		refRenderer.notifySceneChange();
	}

}

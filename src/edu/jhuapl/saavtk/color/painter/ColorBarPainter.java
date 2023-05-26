package edu.jhuapl.saavtk.color.painter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.swing.SwingUtilities;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.color.gui.bar.BackgroundAttr;
import edu.jhuapl.saavtk.color.gui.bar.LayoutAttr;
import edu.jhuapl.saavtk.color.gui.bar.ShowMode;
import edu.jhuapl.saavtk.color.table.ColorMapAttr;
import edu.jhuapl.saavtk.color.table.ColorTableUtil;
import edu.jhuapl.saavtk.gui.render.Renderer;
import edu.jhuapl.saavtk.gui.render.VtkPropProvider;
import edu.jhuapl.saavtk.util.ScreenUtil;
import edu.jhuapl.saavtk.view.ViewActionListener;
import edu.jhuapl.saavtk.view.ViewChangeReason;
import edu.jhuapl.saavtk.vtk.Location;
import edu.jhuapl.saavtk.vtk.VtkFontUtil;
import edu.jhuapl.saavtk.vtk.VtkResource;
import edu.jhuapl.saavtk.vtk.font.FontAttr;
import edu.jhuapl.saavtk.vtk.painter.VtkRectPainter;
import plotkit.cadence.Cadence;
import vtk.vtkLookupTable;
import vtk.vtkProp;
import vtk.vtkRenderWindowInteractor;
import vtk.vtkScalarBarActor;
import vtk.vtkScalarBarWidget;
import vtk.vtkTextActor;

/**
 * Painter used to render a color bar into a VTK scene.
 *
 * @author lopeznr1
 */
public class ColorBarPainter implements ViewActionListener, VtkPropProvider, VtkResource
{
	// Constants
	/** Defines the maximum number of ticks that will be rendered. */
	public final static int MaxNumTicks = 20;

	// Reference vars
	private final Renderer refRenderer;

	// State vars
	private List<ColorBarChangeListener> listenerL;
	private boolean isVisible;
	private String title;

	private BackgroundAttr mainBA;
	private ColorMapAttr mainCMA;
	private LayoutAttr mainLA;
	private FontAttr titleFA;
	private FontAttr labelFA;

	// VTK vars
	private VtkRectPainter vBorderRP;
	private vtkLookupTable vColorLT;
	private vtkScalarBarActor vBarSBA;
	private vtkScalarBarWidget vBarSCW;
	private vtkTextActor vTitleTA;
	private List<vtkTextActor> vLabelL;

	// Cache vars
	private List<vtkTextActor> cUtilizedLabelL;
	private Location cLocation;

	/** Standard Constructor */
	public ColorBarPainter(Renderer aRenderer)
	{
		refRenderer = aRenderer;

		listenerL = new ArrayList<>();
		isVisible = true;
		title = "";

		mainBA = new BackgroundAttr(ShowMode.Auto, new Color(0.50f, 0.50f, 0.50f, 0.50f));
		mainCMA = ColorMapAttr.Invalid;
		mainLA = new LayoutAttr(false, false, 5, 400, 40, false, Cadence.Invalid);
		titleFA = new FontAttr("Arial", Color.WHITE, 20, true, true, true);
		labelFA = new FontAttr("Arial", Color.WHITE, 16, true, false, false);

		vBorderRP = new VtkRectPainter();
		vColorLT = new vtkLookupTable();
		vBarSBA = new vtkScalarBarActor();

		vBarSBA.SetNumberOfLabels(0);
		vBarSBA.SetDrawTickLabels(0);
		vBarSBA.SetTitle("");

		vBarSBA.SetBarRatio(1.0);
		vBarSBA.DrawFrameOff();
		vBarSBA.DrawTickLabelsOff();
		vBarSBA.SetTextPad(0);

		vBarSCW = new vtkScalarBarWidget();
		vBarSCW.CreateDefaultRepresentation();
		vBarSCW.GetScalarBarRepresentation().SetAutoOrient(false);
		vBarSCW.GetScalarBarRepresentation().SetShowBorderToOff();
		vBarSCW.ResizableOn();
		vBarSCW.SetScalarBarActor(vBarSBA);
		vBarSCW.GetBorderRepresentation().ProportionalResizeOff();
		vBarSCW.GetBorderRepresentation().SetShowBorder(0);

		int orientVal = 0;
		if (mainLA.getIsHorizontal() == false)
			orientVal = 1;
		vBarSBA.SetOrientation(orientVal);
		vBarSCW.GetScalarBarRepresentation().SetOrientation(orientVal);

		vTitleTA = new vtkTextActor();
		vTitleTA.GetTextProperty().SetJustificationToLeft();
		vTitleTA.GetTextProperty().SetVerticalJustificationToBottom();

		vLabelL = new ArrayList<>();

		cUtilizedLabelL = ImmutableList.of();
		cLocation = null;

		// Set up interaction logic
		vtkRenderWindowInteractor vInteractor = refRenderer.getRenderWindowPanel().getRenderWindowInteractor();
		vInteractor.AddObserver("LeaveEvent", this, "callBack1");
		vInteractor.AddObserver("MouseMoveEvent", this, "callBack2");
		vInteractor.AddObserver("RenderEvent", this, "callBack2");
		vBarSCW.SetInteractor(vInteractor);
		vBarSCW.EnabledOn();

		// Register for events of interest
		refRenderer.addViewChangeListener(this);
	}

	/**
	 * Registers a {@link ColorBarChangeListener} with this painter.
	 */
	public void addListener(ColorBarChangeListener aListener)
	{
		listenerL.add(aListener);
	}

	/**
	 * Deregisters a {@link ColorBarChangeListener} with this painter.
	 */
	public void delListener(ColorBarChangeListener aListener)
	{
		listenerL.remove(aListener);
	}

	/**
	 * Method that handles the VTK LeaveEvent callback.
	 * <P>
	 * This method is made available for VTK callback and outside code should not
	 * call this method. This method will be removed in the future.
	 */
	public void callBack1()
	{
		updateBackgroundVisibility(false);

		refRenderer.notifySceneChange();
	}

	/**
	 * Method that handles the VTK MouseMoveEvent and Render callback.
	 * <P>
	 * This method is made available for VTK callback and outside code should not
	 * call this method. This method will be removed in the future.
	 */
	public void callBack2()
	{
		// Bail if the bar does not have a location
		if (cLocation == null)
			return;

		// Toggle background if necessary
		boolean showBorder = isVisible;
		showBorder &= vBarSCW.GetBorderRepresentation().GetInteractionState() > 0;
		updateBackgroundVisibility(showBorder);

		// Bail if location has not changed
		Location tmpLoc = retrieveBarLocation();
		if (Objects.equals(cLocation, tmpLoc) == true)
		{
			refRenderer.notifySceneChange();
			return;
		}

		// Bail if the dimensions are effectively the same
		boolean isEffectiveSame = true;
		isEffectiveSame &= Math.abs(cLocation.getDimX() - tmpLoc.getDimX()) <= 0.01;
		isEffectiveSame &= Math.abs(cLocation.getDimY() - tmpLoc.getDimY()) <= 0.01;
		if (isEffectiveSame == true)
		{
			refRenderer.notifySceneChange();
			return;
		}

		// Update the location only if it valid. Keep the color bar dimensions.
		if (tmpLoc.isValid() == true)
			cLocation = new Location(tmpLoc.getPosX(), tmpLoc.getPosY(), cLocation.getDimX(), cLocation.getDimY());

		updateLayout();
		refRenderer.notifySceneChange();
	}

	/**
	 * Returns the {@link BackgroundAttr} associated with this painter.
	 */
	public BackgroundAttr getBackgroundAttr()
	{
		return mainBA;
	}

	/**
	 * Returns the {@link ColorMapAttr} associated with this painter.
	 */
	public ColorMapAttr getColorMapAttr()
	{
		return mainCMA;
	}

	/**
	 * Returns the {@link LayoutAttr} associated with this painter.
	 */
	public LayoutAttr getLayoutAttr()
	{
		return mainLA;
	}

	/**
	 * Returns the {@link FontAttr} associated with the (tick) labels.
	 */
	public FontAttr getFontAttrLabel()
	{
		return labelFA;
	}

	/**
	 * Returns the {@link FontAttr} associated with the title.
	 */
	public FontAttr getFontAttrTitle()
	{
		return titleFA;
	}

	/**
	 * Returns the painter's visibility.
	 */
	public boolean getIsVisible()
	{
		return isVisible;
	}

	/**
	 * Causes the painter's location to be reset
	 */
	public void resetLocation()
	{
		cLocation = calcResetLocation();

		updateLayout();
		refRenderer.notifySceneChange();

		notifyListeners(ColorBarChangeType.Location);
	}

	/**
	 * Sets the {@link BackgroundAttr} for which the painter will be configured to.
	 */
	public void setBackgroundAttr(BackgroundAttr aAttr)
	{
		// Bail if nothing has changed
		if (mainBA.equals(aAttr) == true)
			return;
		mainBA = aAttr;

		updateBackgroundColor();
		updateBackgroundVisibility(false);
		refRenderer.notifySceneChange();

		notifyListeners(ColorBarChangeType.Background);
	}

	/**
	 * Sets the {@link ColorMapAttr} for which the painter will be configured to.
	 *
	 * @param aAttr
	 */
	public void setColorMapAttr(ColorMapAttr aAttr)
	{
		// Bail if nothing has changed
		if (mainCMA.equals(aAttr) == true)
			return;
		mainCMA = aAttr;

		updateColorMap();
		updateLayout();
		refRenderer.notifySceneChange();

		notifyListeners(ColorBarChangeType.ColorMap);
	}

	/**
	 * Sets in the {@link FontAttr} associated with the tick labels.
	 */
	public void setFontAttrLabel(FontAttr aAttr)
	{
		// Bail if nothing has changed
		if (labelFA.equals(aAttr) == true)
			return;
		labelFA = aAttr;

		// Delegate
		for (vtkTextActor aTmpTA : vLabelL)
			VtkFontUtil.setFontAttr(aTmpTA.GetTextProperty(), labelFA);

		updateLayout();
		refRenderer.notifySceneChange();

		notifyListeners(ColorBarChangeType.Label);
	}

	/**
	 * Sets in the {@link FontAttr} associated with the title.
	 */
	public void setFontAttrTitle(FontAttr aAttr)
	{
		// Bail if nothing has changed
		if (titleFA.equals(aAttr) == true)
			return;
		titleFA = aAttr;

		// Delegate
		VtkFontUtil.setFontAttr(vTitleTA.GetTextProperty(), titleFA);

		updateLayout();
		refRenderer.notifySceneChange();

		notifyListeners(ColorBarChangeType.Title);
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
	 * Sets the {@link LayoutAttr} for which the painter will be configured to.
	 */
	public void setLayoutAttr(LayoutAttr aAttr)
	{
		// Bail if nothing has changed
		if (mainLA.equals(aAttr) == true)
			return;

		LayoutAttr prevLA = mainLA;
		mainLA = aAttr;

		// Update the orientation
		boolean prevIsHoriz = vBarSBA.GetOrientation() == 0;
		boolean currIsHoriz = mainLA.getIsHorizontal();
		if (prevIsHoriz != currIsHoriz)
		{
			int orientVal = 0;
			if (mainLA.getIsHorizontal() == false)
				orientVal = 1;
			vBarSBA.SetOrientation(orientVal);
			vBarSCW.GetScalarBarRepresentation().SetOrientation(orientVal);

			updateTitleContent();

			cLocation = calcResetLocation();
		}

		// Update the bar dimension
		double pX = cLocation.getPosX();
		double pY = cLocation.getPosY();
		int barLen = mainLA.getBarLength();
		int barWid = mainLA.getBarWidth();
		if (prevLA.getBarLength() != barLen)
		{
			int lenDiff = barLen - prevLA.getBarLength();
			if (currIsHoriz == true)
				cLocation = new Location(pX - lenDiff / 2.0, pY, barLen, barWid);
			else
				cLocation = new Location(pX, pY - lenDiff / 2.0, barWid, barLen);
		}
		if (prevLA.getBarWidth() != barWid)
		{
			if (currIsHoriz == true)
				cLocation = new Location(pX, pY, barLen, barWid);
			else
				cLocation = new Location(pX, pY, barWid, barLen);
		}

		updateColorMap();
		updateLayout();
		refRenderer.notifySceneChange();

		notifyListeners(ColorBarChangeType.Layout);
	}

	/**
	 * Sets the title of the color bar
	 */
	public void setTitle(String aTitle)
	{
		// Bail if nothing has changed
		if (Objects.equals(title, aTitle) == true)
			return;
		title = aTitle;

		updateTitleContent();
		updateLayout();
		refRenderer.notifySceneChange();
	}

	@Override
	public Collection<vtkProp> getProps()
	{
		// Bail if not visible
		if (isVisible == false)
			return ImmutableList.of();

		// Perform initialization
		if (cLocation == null)
		{
			SwingUtilities.invokeLater(() -> initPainter());
			return ImmutableList.of();
		}

		// Return the list of all vtkProps
		List<vtkProp> retL = new ArrayList<>();
		if (vBorderRP.getIsVisible() == true)
			retL.addAll(vBorderRP.getProps());
		retL.add(vBarSBA);
		if (titleFA.getIsVisible() == true)
			retL.add(vTitleTA);
		if (labelFA.getIsVisible() == true)
			retL.addAll(cUtilizedLabelL);
		return retL;
	}

	@Override
	public void handleViewAction(Object aSource, ViewChangeReason aReason)
	{
		// Bail if the bar does not have a location
		if (cLocation == null)
			return;

		// Bail if location has not changed
		Location tmpLoc = retrieveBarLocation();
		if (Objects.equals(cLocation, tmpLoc) == true)
			return;

		// Update the location only if it valid. Keep the color bar dimensions.
		if (tmpLoc.isValid() == true)
			cLocation = new Location(tmpLoc.getPosX(), tmpLoc.getPosY(), cLocation.getDimX(), cLocation.getDimY());

		updateLayout();
		refRenderer.notifySceneChange();
	}

	@Override
	public void vtkDispose()
	{
		vTitleTA.Delete();

		vBarSCW.Delete();
	}

	@Override
	public void vtkUpdateState()
	{
		; // Update logic is done elsewhere
	}

	/**
	 * Helper method to update the internal layout of various components.
	 */
	private void updateLayout()
	{
		// Bail if the bar does not have a location
		if (cLocation == null)
			return;

		// Update the list (and normalized positioning) of utilized labels
		drawComponentsToNormalizedCoordinates();

		// Retrieve the pixel dimension of the panel
		var scale = ScreenUtil.getScreenScale(refRenderer.getRenderWindowPanel());
		var panelW = refRenderer.getRenderWindowPanel().getComponent().getWidth() * scale;
		var panelH = refRenderer.getRenderWindowPanel().getComponent().getHeight() * scale;

		// Update the actor and widget associated with the color bar
		double posX = cLocation.getPosX();
		double posY = cLocation.getPosY();
		double dimX = cLocation.getDimX();
		double dimY = cLocation.getDimY();

		double relPX = posX / panelW;
		double relPY = posY / panelH;
		double relDX = dimX / panelW;
		double relDY = dimY / panelH;
		vBarSCW.GetScalarBarRepresentation().SetPosition(relPX, relPY);
		vBarSCW.GetScalarBarRepresentation().SetPosition2(relDX, relDY);
		vBarSCW.Modified();
		vBarSBA.SetPosition(relPX, relPY);
		vBarSBA.SetPosition2(relDX, relDY);
		vBarSBA.Modified();

		// Update the background location
		Location tmpLocation = calcBorderLocation(cLocation);
		vBorderRP.setLocation(tmpLocation);

		// Apply the translation to the utilized labels
		for (vtkTextActor aTmpTA : cUtilizedLabelL)
		{
			double[] posArr = aTmpTA.GetPosition();
			double tmpX = cLocation.getPosX() + posArr[0];
			double tmpY = cLocation.getPosY() + posArr[1];
			aTmpTA.SetPosition(tmpX, tmpY);
		}

		// Apply the translation to the utilized title
		if (titleFA.getIsVisible() == true)
		{
			double[] posArr = vTitleTA.GetPosition();
			double tmpX = cLocation.getPosX() + posArr[0];
			double tmpY = cLocation.getPosY() + posArr[1];
			vTitleTA.SetPosition(tmpX, tmpY);
		}
	}

	/**
	 * Returns the {@link Location} where the border (background) should be placed.
	 *
	 * @param aBarLoc The location of the color bar.
	 */
	private Location calcBorderLocation(Location aBarLoc)
	{
		// Color bar boundaries
		double barPosX = aBarLoc.getPosX();
		double barPosY = aBarLoc.getPosY();
		double barDimX = aBarLoc.getDimX();
		double barDimY = aBarLoc.getDimY();

		double[] bbArr = new double[4];

		// Calculate label boundaries
		double labelMinX = Double.POSITIVE_INFINITY, labelMinY = Double.POSITIVE_INFINITY;
		double labelMaxX = Double.NEGATIVE_INFINITY, labelMaxY = Double.NEGATIVE_INFINITY;
		double labelW = 0;
		double labelH = 0;
		for (vtkTextActor aTmpTA : cUtilizedLabelL)
		{
			double[] tmpPosArr = aTmpTA.GetPosition();
			aTmpTA.GetBoundingBox(refRenderer.getRenderWindowPanel().getRenderer(), bbArr);

			double tmpLabelW = bbArr[1] - bbArr[0];
			double tmpLabelH = bbArr[3] - bbArr[2];

			double adjMinX = 0, adjMaxX = 0;
			double adjMinY = 0, adjMaxY = 0;
			if (mainLA.getIsHorizontal() == true)
			{
				adjMinX = -tmpLabelW / 2.0;
				adjMaxX = +tmpLabelW / 2.0;
			}
			else
			{
				adjMaxX = +tmpLabelW;
				adjMinY = -tmpLabelH / 2.0;
				adjMaxY = +tmpLabelH / 2.0;
			}

			double tmpMinX = barPosX + tmpPosArr[0] + adjMinX;
			double tmpMaxX = barPosX + tmpPosArr[0] + adjMaxX;
			double tmpMinY = barPosY + tmpPosArr[1] + adjMinY;
			double tmpMaxY = barPosY + tmpPosArr[1] + adjMaxY;

			if (tmpLabelW > labelW)
				labelW = tmpLabelW;
			if (tmpLabelH > labelH)
				labelH = tmpLabelH;

			if (labelMinX > tmpMinX)
				labelMinX = tmpMinX;
			if (labelMaxX < tmpMaxX)
				labelMaxX = tmpMaxX;
			if (labelMinY > tmpMinY)
				labelMinY = tmpMinY;
			if (labelMaxY < tmpMaxY)
				labelMaxY = tmpMaxY;
		}

		// Calculate title boundary
		double titleMinX = Double.POSITIVE_INFINITY, titleMinY = Double.POSITIVE_INFINITY;
		double titleMaxX = Double.NEGATIVE_INFINITY, titleMaxY = Double.NEGATIVE_INFINITY;
		double titleW = 0;
		double titleH = 0;
		if (titleFA.getIsVisible() == true)
		{
			double[] tmpPosArr = vTitleTA.GetPosition();
			vTitleTA.GetBoundingBox(refRenderer.getRenderWindowPanel().getRenderer(), bbArr);

			titleW = bbArr[1] - bbArr[0];
			titleH = bbArr[3] - bbArr[2];

			double adjMinX = 0, adjMaxX = 0;
			double adjMinY = 0, adjMaxY = 0;
			if (mainLA.getIsHorizontal() == true)
			{
				adjMinX = -titleW / 2.0;
				adjMaxX = +titleW / 2.0;
				adjMaxY = +titleH;
			}
			else
			{
				adjMinY = 0;
				adjMaxY = titleH;
			}

			titleMinX = barPosX + tmpPosArr[0] + adjMinX;
			titleMaxX = barPosX + tmpPosArr[0] + adjMaxX;
			titleMinY = barPosY + tmpPosArr[1] + adjMinY;
			titleMaxY = barPosY + tmpPosArr[1] + adjMaxY;
		}

		// Calculate border boundaries
		boolean isHoriz = mainLA.getIsHorizontal();
		if (isHoriz == true)
		{
			barPosY -= labelH;
			barDimY += labelH;
			barDimY += titleH;

			if (labelMinX < barPosX)
				barPosX = labelMinX;
			if (labelMaxX > barPosX + barDimX)
				barDimX = labelMaxX - barPosX;

			if (titleMinX < barPosX)
				barPosX = titleMinX;
			if (titleMaxX > barPosX + barDimX)
				barDimX = titleMaxX - barPosX;

			if (titleMaxY > barPosY + barDimY)
				barDimY = titleMaxY - barPosY;
		}
		else
		{
			barDimX += labelW;
			if (titleW > barDimX)
				barDimX = titleW;

			if (labelMaxX > barPosX + barDimX)
				barDimX = labelMaxX - barPosX;

			if (labelMinY < barPosY)
				barPosY = labelMinY;
			if (labelMaxY > barPosY + barDimY)
				barDimY = labelMaxY - barPosY;

			if (titleMinY < barPosY)
				barPosY = titleMinY;
			if (titleMaxY > barPosY + barDimY)
				barDimY = titleMaxY - barPosY;
		}

		Location retLocation = new Location(barPosX, barPosY, barDimX, barDimY);
		return retLocation;
	}

	/**
	 * Helper method that computes and returns the "reset" location of the color
	 * bar.
	 */
	private Location calcResetLocation()
	{
		// Update the list (and normalized positioning) of utilized labels
		drawComponentsToNormalizedCoordinates();

		double[] bbArr = new double[4];
		double labelH = 0;
		if (labelFA.getIsVisible() == true && mainLA.getNumTicks() > 0 && vLabelL.size() > 0)
		{
			vtkTextActor vTmpTA = vLabelL.get(0);
			vTmpTA.GetBoundingBox(refRenderer.getRenderWindowPanel().getRenderer(), bbArr);
			labelH = bbArr[3] - bbArr[2];
		}

		// Take into account the (precomputed) border
		int dimX = mainLA.getBarWidth();
		int dimY = mainLA.getBarLength();
		if (mainLA.getIsHorizontal() == true)
		{
			dimX = mainLA.getBarLength();
			dimY = mainLA.getBarWidth();
		}

		Location mainLoc = new Location(0, 0, dimX, dimY);
		Location bordLoc = calcBorderLocation(mainLoc);

		// Compute the "reset" location of the color bar
		var scale = ScreenUtil.getScreenScale(refRenderer.getRenderWindowPanel());
		var panelW = refRenderer.getRenderWindowPanel().getComponent().getWidth() * scale;
		var panelH = refRenderer.getRenderWindowPanel().getComponent().getHeight() * scale;

		int barWid = mainLA.getBarWidth();
		int barLen = mainLA.getBarLength();
		if (mainLA.getIsHorizontal() == false)
		{
			double cenY = panelH / 2.0;
			double posX = panelW - (20 + bordLoc.getDimX());
			double posY = cenY - (mainLoc.getDimY() / 2.0);
			return new Location(posX, posY, barWid, barLen);
		}
		else
		{
			double cenX = panelW / 2.0;
			double posX = cenX - (mainLoc.getDimX() / 2.0);
			double posY = (20 + labelH);
			return new Location(posX, posY, barLen, barWid);
		}
	}

	/**
	 * Helper method that will draw the various sub components to a "normalized"
	 * position. The normalized position will <B>assume</B> the color bar is located
	 * at the origin (0, 0).
	 * <P>
	 * After this method each label will need to have a 2D translation applied
	 * (relative to the color bar) in order that they will be positioned properly
	 * (relative to the color bar).
	 */
	private void drawComponentsToNormalizedCoordinates()
	{
		// Update the labels
		cUtilizedLabelL = ImmutableList.of();
		if (labelFA.getIsVisible() == true)
		{
			// Ensure we have sufficient number of (label) vtkTextActors
			int numLabels = ColorBarDrawUtil.computeNumLabelsNeededFor(mainLA, mainCMA);
			if (numLabels > MaxNumTicks)
				numLabels = MaxNumTicks;

			while (vLabelL.size() < numLabels)
			{
				vtkTextActor vTmpTA = new vtkTextActor();
				VtkFontUtil.setFontAttr(vTmpTA.GetTextProperty(), labelFA);
				vLabelL.add(vTmpTA);
			}

			// Draw the labels
			cUtilizedLabelL = ColorBarDrawUtil.drawLabels(vLabelL, mainCMA, mainLA, labelFA);
		}

		// Update the title
		ColorBarDrawUtil.drawTitle(vTitleTA, mainLA, labelFA, titleFA, cUtilizedLabelL);
	}

	/**
	 * Helper method that will complete the painter's initialization.
	 */
	private void initPainter()
	{
		// Bail once we have a location
		if (cLocation != null)
			return;

		// Bail if we do not have a valid scale
		var scale = ScreenUtil.getScreenScale(refRenderer.getRenderWindowPanel());
		if (Double.isNaN(scale) == true || scale <= 0.0)
			return;

		// Perform the initial font configuration
		titleFA = new FontAttr("Arial", Color.WHITE, (int) (20 * scale), true, true, true);
		labelFA = new FontAttr("Arial", Color.WHITE, (int) (16 * scale), true, false, false);
		VtkFontUtil.setFontAttr(vTitleTA.GetTextProperty(), titleFA);

		// Compute the initial bar length / width
		var panelW = refRenderer.getRenderWindowPanel().getComponent().getWidth() * scale;
		var panelH = refRenderer.getRenderWindowPanel().getComponent().getHeight() * scale;
		var isHoriz = mainLA.getIsHorizontal();

		int barLen, barWid;
		if (isHoriz == true)
		{
			barLen = (int) (panelW * 0.75);
			barWid = (int) (panelH * 0.05);
		}
		else
		{
			barLen = (int) (panelH * 0.75);
			barWid = (int) (panelW * 0.05);
		}
		if (barWid < 40)
			barWid = 40;
		else if (barWid > 80)
			barWid = 80;

		// Synthesize a mainLA with the appropriate bar length/ width
		boolean isReverse = mainLA.getIsReverseOrder();
		boolean isCadenceEnabled = mainLA.getIsCadenceEnabled();
		Cadence cadence = mainLA.getCadence();
		mainLA = new LayoutAttr(isHoriz, isReverse, mainLA.getNumTicks(), barLen, barWid, isCadenceEnabled, cadence);

		// Set in the cache cLocation
		cLocation = calcResetLocation();

		updateBackgroundColor();
		updateBackgroundVisibility(false);
		updateLayout();
		refRenderer.notifySceneChange();

		// Send out notification that the layout has changed
		notifyListeners(ColorBarChangeType.Layout);
	}

	/**
	 * Helper method to send out notification of configuration change to listeners.
	 */
	private void notifyListeners(ColorBarChangeType aType)
	{
		for (ColorBarChangeListener aListener : listenerL)
			aListener.handleColorBarChanged(this, aType);
	}

	/**
	 * Returns the {@link Location} of the color bar.
	 */
	private Location retrieveBarLocation()
	{
		var scale = ScreenUtil.getScreenScale(refRenderer.getRenderWindowPanel());
		var panelW = refRenderer.getRenderWindowPanel().getComponent().getWidth() * scale;
		var panelH = refRenderer.getRenderWindowPanel().getComponent().getHeight() * scale;

		double[] posArr = vBarSBA.GetPosition();
		double[] dimArr = vBarSBA.GetPosition2();
		double posX = posArr[0] * panelW;
		double posY = posArr[1] * panelH;
		double dimX = dimArr[0] * panelW;
		double dimY = dimArr[1] * panelH;

		return new Location(posX, posY, dimX, dimY);
	}

	/**
	 * Helper method to update the background visibility.
	 */
	private void updateBackgroundColor()
	{
		Color tmpColor = mainBA.getColor();
		vBorderRP.setColor(tmpColor);
	}

	/**
	 * Helper method to update the background visibility.
	 */
	private void updateBackgroundVisibility(boolean aIsAutoShow)
	{
		boolean isShown = false;
		isShown |= mainBA.getMode() == ShowMode.Always;
		isShown |= mainBA.getMode() == ShowMode.Auto && aIsAutoShow == true;

		// Hack to allow for the vBorderRP to not overlap other elements
		int visibleVal = 0;
		if (isShown == true)
			visibleVal = 1;
		vBorderRP.getProps().iterator().next().SetVisibility(visibleVal);
	}

	/**
	 * Helper method to update the foreground color map
	 */
	private void updateColorMap()
	{
		ColorMapAttr tmpCMA = mainCMA;
		if (mainLA.getIsReverseOrder() == true)
			tmpCMA = new ColorMapAttr(mainCMA.getColorTable().reverse(), mainCMA.getMinVal(), mainCMA.getMaxVal(),
					mainCMA.getNumLevels(), mainCMA.getIsLogScale());

		ColorTableUtil.updateLookUpTable(vColorLT, tmpCMA);
		vBarSBA.SetLookupTable(vColorLT);
		vBarSBA.Modified();
	}

	/**
	 * Helper method to update the title content.
	 */
	private void updateTitleContent()
	{
		// Update the title actor
		String tmpTitle = title;
		if (mainLA.getIsHorizontal() == false && tmpTitle.length() > 16)
			tmpTitle = tmpTitle.replaceAll("\\s+", "\n");

		vTitleTA.SetInput(tmpTitle);
		vTitleTA.Modified();
	}

}

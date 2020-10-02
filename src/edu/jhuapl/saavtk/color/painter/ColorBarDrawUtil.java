package edu.jhuapl.saavtk.color.painter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.jhuapl.saavtk.color.gui.bar.LayoutAttr;
import edu.jhuapl.saavtk.color.table.ColorMapAttr;
import edu.jhuapl.saavtk.vtk.font.FontAttr;
import glum.text.SigFigNumberFormat;
import plotkit.AxisTransform;
import plotkit.cadence.Cadence;
import plotkit.cadence.ModelLogIterator;
import plotkit.cadence.PlainModelCadence;
import plotkit.tranform.LogAxisTransform;
import plotkit.tranform.PlainAxisTransform;
import vtk.vtkTextActor;

/**
 * Collection of utility methods associated with drawing different components of
 * a {@link ColorBarPainter}. The draw routines will draw the components to a
 * "normalized" coordinate system. The normalized coordinate system will
 * <B>assume</B> the color bar is located at the origin (0, 0).
 *
 * @author lopeznr1
 */
public class ColorBarDrawUtil
{
	/**
	 * Utility method that returns the (approximate) number of labels needed to
	 * render all the labels with the specified configuration.
	 */
	public static int computeNumLabelsNeededFor(LayoutAttr aTmpLA, ColorMapAttr aTmpCMA)
	{
		Cadence tmpCadence = aTmpLA.getCadence();

		// If the cadence will be auto generated then just use the number of ticks
		boolean isAutoCadence = false;
		isAutoCadence |= aTmpCMA.getIsLogScale() == true;
		isAutoCadence |= aTmpLA.getIsCadenceEnabled() == false;
		isAutoCadence |= tmpCadence == Cadence.Invalid;
		if (isAutoCadence == true)
			return aTmpLA.getNumTicks();

		// Retrieve the range of data values
		double minDV = aTmpCMA.getMinVal();
		double maxDV = aTmpCMA.getMaxVal();

		if (tmpCadence instanceof PlainModelCadence)
		{
			double beat = ((PlainModelCadence) tmpCadence).getBeat();

			int retVal = (int) ((maxDV - minDV) / beat) + 1;
			return retVal;
		}

		throw new RuntimeException("Unsupported cadence type: " + tmpCadence.getClass().getName());
	}

	/**
	 * Utility method that will draw the numeric labels along the color bar. Note
	 * the labels will be positioned at a "normalized" position. The normalized
	 * position will <B>assume</B> the color bar is located at the origin (0, 0).
	 * <P>
	 * The list of utilized (vtkTextActor) labels will be returned.
	 *
	 * @param aLabelL The list of available ({@link vtkTextActor}) labels
	 * @param aTmpCMA The {@link ColorMapAttr} associated with the color bar.
	 * @param aTmpLA  The {@link LayoutAttr} associated with the color bar
	 */
	public static List<vtkTextActor> drawLabels(List<vtkTextActor> aLabelL, ColorMapAttr aTmpCMA, LayoutAttr aTmpLA)
	{
		// Retrieve the range of data values
		double minDV = aTmpCMA.getMinVal();
		double maxDV = aTmpCMA.getMaxVal();

		// Bail if we do not have a valid data range
		boolean isValidDataRange = true;
		isValidDataRange &= Double.isFinite(minDV) == true;
		isValidDataRange &= Double.isFinite(maxDV) == true;
		isValidDataRange &= Double.isNaN(minDV) == false;
		isValidDataRange &= Double.isNaN(maxDV) == false;
		isValidDataRange &= maxDV > minDV;
		if (isValidDataRange == false)
			return ImmutableList.of();

		double diffDV = maxDV - minDV;
		double axisLen = aTmpLA.getBarLength();
		double scaleFact = diffDV / axisLen;

		// Retrieve the cadence
		int numTicks = aTmpLA.getNumTicks();
		Cadence tmpCadence = aTmpLA.getCadence();
		if (aTmpLA.getIsCadenceEnabled() == false || tmpCadence == Cadence.Invalid)
		{
			// Bail if no ticks are to be drawn
			if (numTicks == 0)
				return ImmutableList.of();

			double tmpBeat = diffDV;
			double tmpMark = minDV + (diffDV / 2.0);
			if (numTicks > 1)
			{
				tmpBeat = diffDV / (numTicks - 1);
				tmpMark = minDV;
			}
			tmpCadence = new PlainModelCadence(tmpBeat, tmpMark);
		}

		// Retrieve the value Iterator and the AxisTransform
		AxisTransform tmpAxisTransform = new PlainAxisTransform(minDV, maxDV, scaleFact, 0.0);
		Iterator<Double> tmpIter = tmpCadence.getIter(tmpAxisTransform, 0, axisLen + 0.01);
		if (aTmpCMA.getIsLogScale() == true)
		{
			tmpAxisTransform = new LogAxisTransform(minDV, maxDV, scaleFact, 0.0);
			tmpIter = new ModelLogIterator(minDV, maxDV, numTicks);
		}

		// Delegate
		if (aTmpLA.getIsHorizontal() == true)
			return drawLabelsHorizontal(aLabelL, aTmpLA, tmpAxisTransform, tmpIter);
		else
			return drawLabelsVertical(aLabelL, aTmpLA, tmpAxisTransform, tmpIter);
	}

	/**
	 * Utility method that will draw the title along the color bar. Note the title
	 * will be positioned at a "normalized" position. The normalized position will
	 * <B>assume</B> the color bar is located at the origin (0, 0).
	 *
	 * @param aVtkTitleTA     The {@link vtkTextActor} used to render the title.
	 * @param aTmpLA          The {@link LayoutAttr} associated with the color bar
	 * @param aUtilizedLabelL The list of utilized labels. These labels should be
	 *                        positioned in a "normalized" position.
	 */
	public static void drawTitle(vtkTextActor aVtkTitleTA, LayoutAttr aTmpLA, FontAttr aLabelFA,
			List<vtkTextActor> aUtilizedLabelL)
	{
		boolean isHorizontal = aTmpLA.getIsHorizontal();
		double normX, normY;
		if (isHorizontal == true)
		{
			normX = 0 + aTmpLA.getBarLength() / 2.0;
			normY = 0 + aTmpLA.getBarWidth();
			aVtkTitleTA.GetTextProperty().SetJustificationToCentered();
			aVtkTitleTA.GetTextProperty().SetVerticalJustificationToBottom();
		}
		else
		{
			normX = 0;
			normY = 0 + aTmpLA.getBarLength();

			// Determine the label with the maximum y-position
			double maxLabelY = 0;
			for (vtkTextActor aTmpTA : aUtilizedLabelL)
			{
				double[] tmpPosArr = aTmpTA.GetPosition();
				double tmpLabelY = tmpPosArr[1] + aLabelFA.getSize() / 2.0;
				if (tmpLabelY > maxLabelY)
					maxLabelY = tmpLabelY;
			}

			if (maxLabelY > normY)
				normY = maxLabelY;

			aVtkTitleTA.GetTextProperty().SetJustificationToLeft();
			aVtkTitleTA.GetTextProperty().SetVerticalJustificationToBottom();
		}
		aVtkTitleTA.SetPosition(normX, normY);
	}

	/**
	 * Helper utility method that will draw the the axis labels along the x-axis
	 * direction.
	 */
	private static List<vtkTextActor> drawLabelsHorizontal(List<vtkTextActor> aLabelL, LayoutAttr aTmpLA,
			AxisTransform tmpAxisTransform, Iterator<Double> tmpIter)
	{
		SigFigNumberFormat numFormat = new SigFigNumberFormat(3);
		double axisLen = aTmpLA.getBarLength();

		List<vtkTextActor> retLabelL = new ArrayList<>();
		for (int c1 = 0; c1 < aLabelL.size(); c1++)
		{
			// Bail once we run out of values
			if (tmpIter.hasNext() == false)
				return retLabelL;

			double rawVal = tmpIter.next();

			double absX = tmpAxisTransform.getAxisValForPlotVal(rawVal);
			double normY = 0.0;
			double normX = absX;
			if (aTmpLA.getIsReverseOrder() == true)
				normX = axisLen - absX;

			vtkTextActor vTmpTA = aLabelL.get(c1);
			vTmpTA.SetInput(numFormat.format(rawVal));
			vTmpTA.SetPosition(normX, normY);
			vTmpTA.GetTextProperty().SetJustificationToCentered();
			vTmpTA.GetTextProperty().SetVerticalJustificationToTop();
			retLabelL.add(vTmpTA);
		}

		return retLabelL;
	}

	/**
	 * Helper utility method that will draw the the axis labels along the y-axis
	 * direction.
	 */
	private static List<vtkTextActor> drawLabelsVertical(List<vtkTextActor> aLabelL, LayoutAttr aTmpLA,
			AxisTransform tmpAxisTransform, Iterator<Double> tmpIter)
	{
		SigFigNumberFormat numFormat = new SigFigNumberFormat(3);
		double axisLen = aTmpLA.getBarLength();

		List<vtkTextActor> retLabelL = new ArrayList<>();
		for (int c1 = 0; c1 < aLabelL.size(); c1++)
		{
			// Bail once we run out of values
			if (tmpIter.hasNext() == false)
				return retLabelL;

			double rawVal = tmpIter.next();

			double absY = tmpAxisTransform.getAxisValForPlotVal(rawVal);
			double normX = aTmpLA.getBarWidth();
			double normY = absY;
			if (aTmpLA.getIsReverseOrder() == true)
				normY = axisLen - absY;

			vtkTextActor vTmpTA = aLabelL.get(c1);
			vTmpTA.SetInput(numFormat.format(rawVal));
			vTmpTA.SetPosition(normX, normY);
			vTmpTA.GetTextProperty().SetJustificationToLeft();
			vTmpTA.GetTextProperty().SetVerticalJustificationToCentered();
			retLabelL.add(vTmpTA);
		}

		return retLabelL;
	}

}

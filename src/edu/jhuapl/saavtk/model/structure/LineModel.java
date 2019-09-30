package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.ImmutableList;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.settings.api.Configuration;
import crucible.crust.settings.api.Content;
import crucible.crust.settings.api.ContentKey;
import crucible.crust.settings.api.SettableValue;
import crucible.crust.settings.api.Value;
import crucible.crust.settings.api.Version;
import crucible.crust.settings.impl.Configurations;
import crucible.crust.settings.impl.KeyValueCollections;
import crucible.crust.settings.impl.SettableValues;
import crucible.crust.settings.impl.Values;
import crucible.crust.settings.impl.metadata.KeyValueCollectionMetadataManager;
import edu.jhuapl.saavtk.model.ColoringData;
import edu.jhuapl.saavtk.model.CommonData;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.Point3D;
import edu.jhuapl.saavtk.util.ProgressListener;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.util.SaavtkLODActor;
import glum.item.ItemEventType;
import vtk.vtkActor;
import vtk.vtkCaptionActor2D;
import vtk.vtkCellArray;
import vtk.vtkCellData;
import vtk.vtkIdList;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkUnsignedCharArray;

/**
 * Model of line structures drawn on a body.
 */
public class LineModel<G1 extends Line> extends ControlPointsStructureModel<G1>
		implements PropertyChangeListener, MetadataManager
{
	public enum Mode
	{
		DEFAULT, PROFILE, CLOSED
	}

	// Constants
	private static final String LINES = "lines";
	private static final String SHAPE_MODEL_NAME = "shapemodel";
	private static final Color redColor = Color.RED;
	private static final Color greenColor = Color.GREEN;
	private static final Color blueColor = Color.BLUE;

	// Ref vars
	private final PolyhedralModel refSmallBodyModel;

	// State vars
	private final Mode mode;
	private double offset;
	private double lineWidth;
	private G1 activatedLine;
	private int currentLineVertex = -1000;

	private int maximumVerticesPerLine = Integer.MAX_VALUE;
	private int maxPolygonId = 0;

	// VTK vars
	private vtkPolyData vLinesRegPD;
	private vtkPolyData vLinesDecPD;
	private vtkPolyData vActivationPD;

	private List<vtkProp> actorL = new ArrayList<>();
	private vtkPolyDataMapper vLineMapperRegPDM;
	private vtkPolyDataMapper vLineMapperDecPDM;
	private vtkPolyDataMapper vLineActivationMapperPDM;
	private vtkActor lineActor;
	private vtkActor lineActivationActor;
	private vtkIdList vIdRegIL;
	private vtkIdList vIdDecIL;

	private vtkPolyData vEmptyPD;

	/**
	 * Standard Constructor
	 */
	public LineModel(PolyhedralModel aSmallBodyModel, Mode aMode)
	{
		refSmallBodyModel = aSmallBodyModel;
		activatedLine = null;
		mode = aMode;
		offset = getDefaultOffset();

		if (hasProfileMode())
			setMaximumVerticesPerLine(2);

		refSmallBodyModel.addPropertyChangeListener(this);

		vIdRegIL = new vtkIdList();
		vIdDecIL = new vtkIdList();

		lineActor = new SaavtkLODActor();
		vtkProperty lineProperty = lineActor.GetProperty();

		lineWidth = 2.0;
		lineProperty.SetLineWidth(lineWidth);

		if (hasProfileMode())
			lineProperty.SetLineWidth(3.0);

		lineActivationActor = new vtkActor();
		vtkProperty lineActivationProperty = lineActivationActor.GetProperty();
		lineActivationProperty.SetColor(1.0, 0.0, 0.0);
		lineActivationProperty.SetPointSize(7.0);

		// Initialize an empty polydata for resetting
		vEmptyPD = new vtkPolyData();
		vtkPoints points = new vtkPoints();
		vtkCellArray cells = new vtkCellArray();
		vtkUnsignedCharArray colors = new vtkUnsignedCharArray();
		colors.SetNumberOfComponents(4);
		vEmptyPD.SetPoints(points);
		vEmptyPD.SetLines(cells);
		vEmptyPD.SetVerts(cells);
		vtkCellData cellData = vEmptyPD.GetCellData();
		cellData.SetScalars(colors);

		vLinesRegPD = new vtkPolyData();
		vLinesDecPD = new vtkPolyData();
		vLinesRegPD.DeepCopy(vEmptyPD);
		vLinesDecPD.DeepCopy(vEmptyPD);

		vActivationPD = new vtkPolyData();
		vActivationPD.DeepCopy(vEmptyPD);

		vLineMapperRegPDM = new vtkPolyDataMapper();
		vLineMapperDecPDM = new vtkPolyDataMapper();

		vLineActivationMapperPDM = new vtkPolyDataMapper();
		vLineActivationMapperPDM.SetInputData(vActivationPD);
		vLineActivationMapperPDM.Update();

		lineActivationActor.SetMapper(vLineActivationMapperPDM);
		lineActivationActor.Modified();

		actorL.add(lineActivationActor);
	}

	public LineModel(PolyhedralModel smallBodyModel)
	{
		this(smallBodyModel, false);
	}

	public LineModel(PolyhedralModel smallBodyModel, boolean profileMode)
	{
		this(smallBodyModel, profileMode ? Mode.PROFILE : Mode.DEFAULT);
	}

	protected String getType()
	{
		return LINES;
	}

	public Element toXmlDomElement(Document dom)
	{
		Element rootEle = dom.createElement(getType());
		if (refSmallBodyModel.getModelName() != null)
			rootEle.setAttribute(SHAPE_MODEL_NAME, refSmallBodyModel.getModelName());

		for (Line lin : getAllItems())
		{
			rootEle.appendChild(lin.toXmlDomElement(dom));
		}

		return rootEle;
	}

	public void fromXmlDomElement(Element element, boolean append)
	{
		List<G1> tmpL = new ArrayList<>(getAllItems());
		if (!append)
			tmpL.clear();

		String shapeModelName = null;
		if (element.hasAttribute(SHAPE_MODEL_NAME))
			shapeModelName = element.getAttribute(SHAPE_MODEL_NAME);

		Line dummyLine = createStructure();
		NodeList nl = element.getElementsByTagName(dummyLine.getType());
		if (nl != null && nl.getLength() > 0)
		{
			for (int i = 0; i < nl.getLength(); i++)
			{
				Element el = (Element) nl.item(i);

				G1 tmpItem = createStructure();

				tmpItem.fromXmlDomElement(refSmallBodyModel, el, shapeModelName, append);

				tmpL.add(tmpItem);
				setStructureLabel(tmpItem, tmpItem.getLabel());
			}
		}

		setAllItems(tmpL);
		lineActor.SetMapper(vLineMapperRegPDM);
		updatePolyData();
	}

	@Override
	protected void updatePolyData()
	{
		actorL.clear();

		vLinesRegPD.DeepCopy(vEmptyPD);
		vtkPoints points = vLinesRegPD.GetPoints();
		vtkCellArray lineCells = vLinesRegPD.GetLines();
		vtkCellData cellData = vLinesRegPD.GetCellData();
		vtkUnsignedCharArray colors = (vtkUnsignedCharArray) cellData.GetScalars();

		List<G1> tmpL = getAllItems();

		Color pickColor = null;
		CommonData commonData = getCommonData();
		if (commonData != null)
			pickColor = commonData.getSelectionColor();

		int c = 0;
		for (Line aItem : tmpL)
		{
			Color tmpColor = aItem.getColor();
			if (pickColor != null && getSelectedItems().contains(aItem) == true)
				tmpColor = pickColor;

			int size = aItem.xyzPointList.size();
			if (mode == Mode.CLOSED && size > 2)
				vIdRegIL.SetNumberOfIds(size + 1);
			else
				vIdRegIL.SetNumberOfIds(size);

			int startId = 0;
			for (int i = 0; i < size; ++i)
			{
				if (i == 0)
					startId = c;

				points.InsertNextPoint(aItem.xyzPointList.get(i).xyz);
				if (aItem.getVisible() == false)
					vIdRegIL.SetId(i, 0); // set to degenerate line if hidden
				else
					vIdRegIL.SetId(i, c);
				++c;
			}

			if (mode == Mode.CLOSED && size > 2)
			{
				if (aItem.getVisible() == false)
					vIdRegIL.SetId(size, 0);
				else
					vIdRegIL.SetId(size, startId);
			}

			lineCells.InsertNextCell(vIdRegIL);
			colors.InsertNextTuple4(tmpColor.getRed(), tmpColor.getGreen(), tmpColor.getBlue(), 255);

		}

		// Repeat for decimated data
		vLinesDecPD.DeepCopy(vEmptyPD);
		vtkPoints decimatedPoints = vLinesDecPD.GetPoints();
		vtkCellArray decimatedLineCells = vLinesDecPD.GetLines();
		vtkCellData decimatedCellData = vLinesDecPD.GetCellData();
		vtkUnsignedCharArray decimatedColors = (vtkUnsignedCharArray) decimatedCellData.GetScalars();

		c = 0;
		for (Line aItem : tmpL)
		{
			Color tmpColor = aItem.getColor();
			if (pickColor != null && getSelectedItems().contains(aItem) == true)
				tmpColor = pickColor;

			int size = aItem.controlPointIds.size();
			// int size = lin.xyzPointList.size();
			if (mode == Mode.CLOSED && size > 2)
				vIdDecIL.SetNumberOfIds(size + 1);
			else
				vIdDecIL.SetNumberOfIds(size);

			int startId = 0;
			for (int i = 0; i < size; ++i)
			{
				if (i == 0)
					startId = c;

				decimatedPoints.InsertNextPoint(aItem.xyzPointList.get(aItem.controlPointIds.get(i)).xyz);
				if (aItem.getVisible() == false)
					vIdDecIL.SetId(i, 0); // set to degenerate line if hidden
				else
					vIdDecIL.SetId(i, c);
				++c;
			}

			if (mode == Mode.CLOSED && size > 2)
			{
				if (aItem.getVisible() == false)
					vIdDecIL.SetId(size, 0);
				else
					vIdDecIL.SetId(size, startId);
			}

			decimatedLineCells.InsertNextCell(vIdDecIL);
			decimatedColors.InsertNextTuple4(tmpColor.getRed(), tmpColor.getGreen(), tmpColor.getBlue(), 255);
		}

		// Setup mapper, actor, etc.
		double offset = getOffset();
		refSmallBodyModel.shiftPolyLineInNormalDirection(vLinesRegPD, offset);
		refSmallBodyModel.shiftPolyLineInNormalDirection(vLinesDecPD, offset);

		vLineMapperRegPDM.SetInputData(vLinesRegPD);
		vLineMapperDecPDM.SetInputData(vLinesDecPD);
		vLineMapperRegPDM.Update();
		vLineMapperDecPDM.Update();

		if (!actorL.contains(lineActor))
			actorL.add(lineActor);

		c = 0;
		for (G1 aItem : tmpL)
		{
			aItem.vDrawId = c;
			c++;

			vtkCaptionActor2D caption = updateStructure(aItem);
			if (caption != null)
				actorL.add(caption);
		}

		// Notify model change listeners
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public List<vtkProp> getProps()
	{
		return actorL;
	}

	@Override
	public String getClickStatusBarText(vtkProp prop, int cellId, double[] pickPosition)
	{
		if (prop != lineActor)
			return "";

		Line tmpItem = getStructure(cellId);
		if (tmpItem == null)
			return "";

		return tmpItem.getClickStatusBarText();
	}

	@Override
	public G1 getActivatedStructure()
	{
		return activatedLine;
	}

	public vtkActor getStructureActor()
	{
		return lineActor;
	}

	@Override
	public vtkActor getActivationActor()
	{
		return lineActivationActor;
	}

	/**
	 * Return the total number of points in all the lines combined.
	 *
	 * @return
	 */
	public int getTotalNumberOfPoints()
	{
		int numberOfPoints = 0;
		for (Line aItem : getAllItems())
		{
			numberOfPoints += aItem.getControlPoints().size();
		}
		return numberOfPoints;
	}

	@Override
	public G1 addNewStructure()
	{
		G1 retItem = createStructure();

		List<G1> fullL = new ArrayList<>(getAllItems());
		fullL.add(retItem);
		setAllItems(fullL);

		activateStructure(retItem);

		lineActor.SetMapper(vLineMapperRegPDM);
		((SaavtkLODActor) lineActor).setLODMapper(vLineMapperDecPDM);
		lineActor.Modified();

		return retItem;
	}

	@Override
	public void updateActivatedStructureVertex(int aVertexId, double[] aNewPoint)
	{
		int numVertices = activatedLine.getControlPoints().size();

		LatLon ll = MathUtil.reclat(aNewPoint);
		activatedLine.setControlPoint(aVertexId, ll);

		// If we're modifying the last vertex
		if (aVertexId == numVertices - 1)
		{
			activatedLine.updateSegment(refSmallBodyModel, aVertexId - 1);
			if (mode == Mode.CLOSED)
				activatedLine.updateSegment(refSmallBodyModel, aVertexId);
		}
		// If we're modifying the first vertex
		else if (aVertexId == 0)
		{
			if (mode == Mode.CLOSED)
				activatedLine.updateSegment(refSmallBodyModel, numVertices - 1);
			activatedLine.updateSegment(refSmallBodyModel, aVertexId);
		}
		// If we're modifying a middle vertex
		else
		{
			activatedLine.updateSegment(refSmallBodyModel, aVertexId - 1);
			activatedLine.updateSegment(refSmallBodyModel, aVertexId);
		}

		updatePolyData();

		updateLineActivation();

		notifyListeners(this, ItemEventType.ItemsMutated);
		pcs.firePropertyChange(Properties.VERTEX_POSITION_CHANGED, null, activatedLine);
	}

	@Override
	public void insertVertexIntoActivatedStructure(double[] aNewPoint)
	{
		if (activatedLine == null)
			return;

		Line tmpLine = activatedLine;

		if (tmpLine.controlPointIds.size() == maximumVerticesPerLine)
			return;

		if (currentLineVertex < -1 || currentLineVertex >= tmpLine.controlPointIds.size())
			System.out.println("Error: currentLineVertex is invalid");

		LatLon ll = MathUtil.reclat(aNewPoint);

		tmpLine.addControlPoint(currentLineVertex + 1, ll);

		// Remove points BETWEEN the 2 control points (If adding a point in the middle)
		if (currentLineVertex < tmpLine.controlPointIds.size() - 1)
		{
			int id1 = tmpLine.controlPointIds.get(currentLineVertex);
			int id2 = tmpLine.controlPointIds.get(currentLineVertex + 1);
			int numberPointsRemoved = id2 - id1 - 1;
			for (int i = 0; i < id2 - id1 - 1; ++i)
			{
				tmpLine.xyzPointList.remove(id1 + 1);
			}

			tmpLine.xyzPointList.add(id1 + 1, new Point3D(aNewPoint));
			tmpLine.controlPointIds.add(currentLineVertex + 1, id1 + 1);

			// Shift the control points ids from currentLineVertex+2 till the end by the
			// right amount.
			for (int i = currentLineVertex + 2; i < tmpLine.controlPointIds.size(); ++i)
			{
				tmpLine.controlPointIds.set(i, tmpLine.controlPointIds.get(i) - (numberPointsRemoved - 1));
			}
		}
		else
		{
			tmpLine.xyzPointList.add(new Point3D(aNewPoint));
			tmpLine.controlPointIds.add(tmpLine.xyzPointList.size() - 1);
		}

		if (tmpLine.controlPointIds.size() >= 2)
		{
			if (currentLineVertex < 0)
			{
				// Do nothing
			}
			else if (currentLineVertex < tmpLine.controlPointIds.size() - 2)
			{
				tmpLine.updateSegment(refSmallBodyModel, currentLineVertex);
				tmpLine.updateSegment(refSmallBodyModel, currentLineVertex + 1);
			}
			else
			{
				tmpLine.updateSegment(refSmallBodyModel, currentLineVertex);
				if (mode == Mode.CLOSED)
					tmpLine.updateSegment(refSmallBodyModel, currentLineVertex + 1);
			}
		}

		++currentLineVertex;

		updatePolyData();

		updateLineActivation();

		notifyListeners(this, ItemEventType.ItemsMutated);
		pcs.firePropertyChange(Properties.VERTEX_INSERTED_INTO_LINE, null, activatedLine);
	}

	@Override
	public void removeCurrentStructureVertex()
	{
		Line tmpLine = activatedLine;

		if (currentLineVertex < 0 || currentLineVertex >= tmpLine.controlPointIds.size())
			return;

		int vertexId = currentLineVertex;

		tmpLine.removeControlPoint(vertexId);

		// If not in CLOSED mode:
		// If one of the end points is being removed, then we only need to remove the
		// line connecting the end point to the adjacent point. If we're removing a
		// non-end point, we need to remove the line segments connecting the 2 adjacent
		// control points and in addition, we need to draw a new line connecting the 2
		// adjacent control points.
		//
		// But if in CLOSED mode:
		// We always need to remove 2 adjacent segments to the control point that was
		// removed and draw a new line connecting the 2 adjacent control point.
		if (tmpLine.controlPointIds.size() > 1)
		{
			// Remove initial point
			if (vertexId == 0)
			{
				int id2 = tmpLine.controlPointIds.get(vertexId + 1);
				int numberPointsRemoved = id2;
				for (int i = 0; i < numberPointsRemoved; ++i)
				{
					tmpLine.xyzPointList.remove(0);
				}
				tmpLine.controlPointIds.remove(vertexId);

				for (int i = 0; i < tmpLine.controlPointIds.size(); ++i)
					tmpLine.controlPointIds.set(i, tmpLine.controlPointIds.get(i) - numberPointsRemoved);

				if (mode == Mode.CLOSED)
				{
					int id = tmpLine.controlPointIds.get(tmpLine.controlPointIds.size() - 1);
					numberPointsRemoved = tmpLine.xyzPointList.size() - id - 1;
					;
					for (int i = 0; i < numberPointsRemoved; ++i)
					{
						tmpLine.xyzPointList.remove(id + 1);
					}

					// redraw segment connecting last point to first
					tmpLine.updateSegment(refSmallBodyModel, tmpLine.controlPointIds.size() - 1);
				}
			}
			// Remove final point
			else if (vertexId == tmpLine.controlPointIds.size() - 1)
			{
				if (mode == Mode.CLOSED)
				{
					int id = tmpLine.controlPointIds.get(tmpLine.controlPointIds.size() - 1);
					int numberPointsRemoved = tmpLine.xyzPointList.size() - id - 1;
					;
					for (int i = 0; i < numberPointsRemoved; ++i)
					{
						tmpLine.xyzPointList.remove(id + 1);
					}
				}

				int id1 = tmpLine.controlPointIds.get(vertexId - 1);
				int id2 = tmpLine.controlPointIds.get(vertexId);
				int numberPointsRemoved = id2 - id1;
				for (int i = 0; i < numberPointsRemoved; ++i)
				{
					tmpLine.xyzPointList.remove(id1 + 1);
				}
				tmpLine.controlPointIds.remove(vertexId);

				if (mode == Mode.CLOSED)
				{
					// redraw segment connecting last point to first
					tmpLine.updateSegment(refSmallBodyModel, tmpLine.controlPointIds.size() - 1);
				}
			}
			// Remove a middle point
			else
			{
				// Remove points BETWEEN the 2 adjacent control points
				int id1 = tmpLine.controlPointIds.get(vertexId - 1);
				int id2 = tmpLine.controlPointIds.get(vertexId + 1);
				int numberPointsRemoved = id2 - id1 - 1;
				for (int i = 0; i < numberPointsRemoved; ++i)
				{
					tmpLine.xyzPointList.remove(id1 + 1);
				}
				tmpLine.controlPointIds.remove(vertexId);

				for (int i = vertexId; i < tmpLine.controlPointIds.size(); ++i)
					tmpLine.controlPointIds.set(i, tmpLine.controlPointIds.get(i) - numberPointsRemoved);

				tmpLine.updateSegment(refSmallBodyModel, vertexId - 1);
			}
		}
		else if (tmpLine.controlPointIds.size() == 1)
		{
			tmpLine.controlPointIds.remove(vertexId);
			tmpLine.xyzPointList.clear();
		}

		--currentLineVertex;
		if (currentLineVertex < 0 && tmpLine.controlPointIds.size() > 0)
			currentLineVertex = 0;

		updatePolyData();

		updateLineActivation();

		notifyListeners(this, ItemEventType.ItemsMutated);
		pcs.firePropertyChange(Properties.VERTEX_REMOVED_FROM_LINE, null, activatedLine);
	}

	@Override
	public void removeStructures(Collection<G1> aItemC)
	{
		if (aItemC.isEmpty() == true)
			return;

		// Update VTK state
		for (G1 aItem : aItemC)
		{
			aItem.setVisible(false);
			updateStructure(aItem);

			pcs.firePropertyChange(Properties.STRUCTURE_REMOVED, null, aItem);
		}

		List<G1> fullL = new ArrayList<>(getAllItems());
		fullL.removeAll(aItemC);
		setAllItems(fullL);

		updatePolyData();

		if (hasProfileMode())
			updateLineActivation();

		if (aItemC.contains(activatedLine) == true)
			activateStructure(null);
	}

	@Override
	public void removeAllStructures()
	{
		for (G1 aItem : getAllItems())
		{
			aItem.setVisible(false);
			updateStructure(aItem);
		}

		setAllItems(ImmutableList.of());
		updatePolyData();

		if (hasProfileMode())
			updateLineActivation();

		activateStructure(null);

		pcs.firePropertyChange(Properties.ALL_STRUCTURES_REMOVED, null, null);
	}

	@Override
	public PolyhedralModel getPolyhedralModel()
	{
		return refSmallBodyModel;
	}

	@Override
	public void moveActivationVertex(int vertexId, double[] newPoint)
	{
		vtkPoints points = vActivationPD.GetPoints();
		points.SetPoint(vertexId, newPoint);
		vActivationPD.Modified();

		notifyListeners(this, ItemEventType.ItemsMutated);
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	protected void updateLineActivation()
	{
		if (hasProfileMode())
		{
			vActivationPD.DeepCopy(vEmptyPD);
			vtkPoints points = vActivationPD.GetPoints();
			vtkCellArray vert = vActivationPD.GetVerts();
			vtkCellData cellData = vActivationPD.GetCellData();
			vtkUnsignedCharArray colors = (vtkUnsignedCharArray) cellData.GetScalars();

			vIdRegIL.SetNumberOfIds(1);

			int count = 0;
			for (Line aItem : getAllItems())
			{
				for (int i = 0; i < aItem.controlPointIds.size(); ++i)
				{
					int idx = aItem.controlPointIds.get(i);

					points.InsertNextPoint(aItem.xyzPointList.get(idx).xyz);
					vIdRegIL.SetId(0, count++);
					vert.InsertNextCell(vIdRegIL);

					Color tmpColor = redColor;
					if (i == 0)
						tmpColor = greenColor;
					colors.InsertNextTuple4(tmpColor.getRed(), tmpColor.getGreen(), tmpColor.getBlue(), tmpColor.getAlpha());
				}
			}

			refSmallBodyModel.shiftPolyLineInNormalDirection(vActivationPD, refSmallBodyModel.getMinShiftAmount());
		}
		else
		{
			Line tmpLine = activatedLine;
			if (tmpLine == null)
			{
				if (actorL.contains(lineActivationActor))
					actorL.remove(lineActivationActor);

				pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
				return;
			}

			vActivationPD.DeepCopy(vEmptyPD);
			vtkPoints points = vActivationPD.GetPoints();
			vtkCellArray vert = vActivationPD.GetVerts();
			vtkCellData cellData = vActivationPD.GetCellData();
			vtkUnsignedCharArray colors = (vtkUnsignedCharArray) cellData.GetScalars();

			int numPoints = tmpLine.controlPointIds.size();

			points.SetNumberOfPoints(numPoints);

			vIdRegIL.SetNumberOfIds(1);

			for (int i = 0; i < numPoints; ++i)
			{
				int idx = tmpLine.controlPointIds.get(i);
				points.SetPoint(i, tmpLine.xyzPointList.get(idx).xyz);
				vIdRegIL.SetId(0, i);
				vert.InsertNextCell(vIdRegIL);

				Color tmpColor = redColor;
				if (i == currentLineVertex)
					tmpColor = blueColor;
				colors.InsertNextTuple4(tmpColor.getRed(), tmpColor.getGreen(), tmpColor.getBlue(), tmpColor.getAlpha());
			}

			refSmallBodyModel.shiftPolyLineInNormalDirection(vActivationPD, getOffset());

			if (!actorL.contains(lineActivationActor))
				actorL.add(lineActivationActor);
		}

		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public void activateStructure(G1 aItem)
	{
		if (aItem == activatedLine)
			return;

		activatedLine = aItem;

		currentLineVertex = -1000;
		if (aItem != null)
			currentLineVertex = aItem.controlPointIds.size() - 1;

		updateLineActivation();

		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void selectCurrentStructureVertex(int idx)
	{
		currentLineVertex = idx;

		updateLineActivation();

		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public void loadModel(File file, boolean append, ProgressListener listener) throws Exception
	{
		// get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		// Using factory get an instance of document builder
		DocumentBuilder db = dbf.newDocumentBuilder();

		// parse using builder to get DOM representation of the XML file
		Document dom = db.parse(file);

		// get the root element
		Element docEle = dom.getDocumentElement();

		if (getType().equals(docEle.getTagName()))
			fromXmlDomElement(docEle, append);
	}

	@Override
	public void saveModel(File file) throws Exception
	{
		// get an instance of factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		// get an instance of builder
		DocumentBuilder db = dbf.newDocumentBuilder();

		// create an instance of DOM
		Document dom = db.newDocument();

		dom.appendChild(toXmlDomElement(dom));

		try
		{
			Source source = new DOMSource(dom);

			OutputStream fout = new FileOutputStream(file);
			Result result = new StreamResult(new OutputStreamWriter(fout, "utf-8"));

			TransformerFactory tf = TransformerFactory.newInstance();
			tf.setAttribute("indent-number", new Integer(4));

			Transformer xformer = tf.newTransformer();
			xformer.setOutputProperty(OutputKeys.INDENT, "yes");

			xformer.transform(source, result);
		}
		catch (TransformerConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (TransformerException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean supportsActivation()
	{
		return true;
	}

	@Override
	public G1 getStructureFromCellId(int aCellId, vtkProp aProp)
	{
		if (aProp == lineActor)
			return getStructure(aCellId);
		else if (aProp == lineActivationActor)
			return activatedLine;
		else
			return null;
	}

	public void redrawAllStructures()
	{
		for (Line aItem : getAllItems())
			aItem.updateAllSegments(refSmallBodyModel);

		updatePolyData();

		updateLineActivation();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
		{
			redrawAllStructures();
		}
	}

	public void setMaximumVerticesPerLine(int max)
	{
		maximumVerticesPerLine = max;
	}

	@Override
	public boolean hasProfileMode()
	{
		return mode == Mode.PROFILE;
	}

	/**
	 * PROFILE MODE ONLY!! Get the vertex id of the line the specified vertex
	 * belongs. Only 0 or 1 can be returned.
	 *
	 * @param idx
	 * @return
	 */
	@Override
	public int getVertexIdFromActivationCellId(int idx)
	{
		for (Line aItem : getAllItems())
		{
			int size = aItem.controlPointIds.size();

			if (idx == 0)
			{
				return 0;
			}
			else if (idx == 1 && size == 2)
			{
				return 1;
			}
			else
			{
				idx -= size;
			}
		}

		return -1;
	}

	/**
	 * PROFILE MODE ONLY!! Get which line the specified vertex belongs to
	 *
	 * @param idx
	 * @return
	 */
	@Override
	public G1 getStructureFromActivationCellId(int idx)
	{
		int count = 0;
		for (G1 tmpItem : getAllItems())
		{
			int size = tmpItem.controlPointIds.size();
			count += size;
			if (idx < count)
				return tmpItem;
		}

		return null;
	}

	@Override
	public double getDefaultOffset()
	{
		return 5.0 * refSmallBodyModel.getMinShiftAmount();
	}

	@Override
	public void setOffset(double aOffset)
	{
		offset = aOffset;

		updatePolyData();

		notifyListeners(this, ItemEventType.ItemsMutated);
	}

	@Override
	public double getOffset()
	{
		return offset;
	}

	public void generateProfile(List<Point3D> xyzPointList, List<Double> profileValues, List<Double> profileDistances,
			int coloringIndex) throws Exception
	{
		profileValues.clear();
		profileDistances.clear();

		// For each point in xyzPointList, find the cell containing that
		// point and then, using barycentric coordinates find the value
		// of the height at that point
		//
		// To compute the distance, assume we have a straight line connecting the first
		// and last points of xyzPointList. For each point, p, in xyzPointList, find the
		// point on the line closest to p. The distance from p to the start of the line
		// is what is placed in heights. Use SPICE's nplnpt function for this.

		double[] first = xyzPointList.get(0).xyz;
		double[] last = xyzPointList.get(xyzPointList.size() - 1).xyz;
		double[] lindir = new double[3];
		lindir[0] = last[0] - first[0];
		lindir[1] = last[1] - first[1];
		lindir[2] = last[2] - first[2];

		// The following can be true if the user clicks on the same point twice
		boolean zeroLineDir = MathUtil.vzero(lindir);

		double[] pnear = new double[3];
		double[] notused = new double[1];

		double distance = 0.0;
		double val = 0.0;

		for (Point3D p : xyzPointList)
		{
			distance = 0.0;
			if (!zeroLineDir)
			{
				MathUtil.nplnpt(first, lindir, p.xyz, pnear, notused);
				distance = 1000.0 * MathUtil.distanceBetween(first, pnear);
			}

			// Save out the distance
			profileDistances.add(distance);

			// Save out the profile value
			if (coloringIndex >= 0)
			{
				// Base the value off the plate coloring
				val = 1000.0 * refSmallBodyModel.getColoringValue(coloringIndex, p.xyz);
			}
			else
			{
				// Base the value off the radius (m)
				val = 1000.0 * 1000.0 * MathUtil.reclat(p.xyz).rad;
			}
			profileValues.add(val);
		}
	}

	@Override
	public void saveProfile(Line aItem, File aFile) throws Exception
	{
		if (aItem.controlPointIds.size() != 2)
			throw new Exception("Line must contain exactly 2 control points.");

		final String lineSeparator = System.getProperty("line.separator");

		FileWriter fstream = new FileWriter(aFile);
		BufferedWriter out = new BufferedWriter(fstream);

		// write header
		out.write("Distance (m)");
		out.write(",X (m)");
		out.write(",Y (m)");
		out.write(",Z (m)");
		out.write(",Latitude (deg)");
		out.write(",Longitude (deg)");
		out.write(",Radius (m)");

		List<ColoringData> colorings = refSmallBodyModel.getAllColoringData();
		for (ColoringData coloring : colorings)
		{
			String units = coloring.getUnits();
			for (String element : coloring.getElementNames())
			{
				out.write("," + element);
				if (!units.isEmpty())
					out.write(" (" + units + ")");
			}
		}
		out.write(lineSeparator);

		List<Point3D> xyzPointList = aItem.xyzPointList;

		// For each point in xyzPointList, find the cell containing that
		// point and then, using barycentric coordinates find the value
		// of the height at that point
		//
		// To compute the distance, assume we have a straight line connecting the first
		// and last points of xyzPointList. For each point, p, in xyzPointList, find the
		// point on the line closest to p. The distance from p to the start of the line
		// is what is placed in heights. Use SPICE's nplnpt function for this.

		double[] first = xyzPointList.get(0).xyz;
		double[] last = xyzPointList.get(xyzPointList.size() - 1).xyz;
		double[] lindir = new double[3];
		lindir[0] = last[0] - first[0];
		lindir[1] = last[1] - first[1];
		lindir[2] = last[2] - first[2];

		// The following can be true if the user clicks on the same point twice
		boolean zeroLineDir = MathUtil.vzero(lindir);

		double[] pnear = new double[3];
		double[] notused = new double[1];

		for (Point3D p : xyzPointList)
		{
			double distance = 0.0;
			if (!zeroLineDir)
			{
				MathUtil.nplnpt(first, lindir, p.xyz, pnear, notused);
				distance = 1000.0 * MathUtil.distanceBetween(first, pnear);
			}

			out.write(String.valueOf(distance));

			double[] vals = refSmallBodyModel.getAllColoringValues(p.xyz);

			out.write("," + 1000.0 * p.xyz[0]);
			out.write("," + 1000.0 * p.xyz[1]);
			out.write("," + 1000.0 * p.xyz[2]);

			LatLon llr = MathUtil.reclat(p.xyz).toDegrees();
			out.write("," + llr.lat);
			out.write("," + llr.lon);
			out.write("," + 1000.0 * llr.rad);

			for (double val : vals)
				out.write("," + val);

			out.write(lineSeparator);
		}

		out.close();
	}

	@Override
	public double getLineWidth()
	{
		return lineWidth;
	}

	@Override
	public void setLineWidth(double aWidth)
	{
		if (aWidth >= 1.0)
		{
			lineWidth = aWidth;
			vtkProperty lineProperty = lineActor.GetProperty();
			lineProperty.SetLineWidth(lineWidth);

			notifyListeners(this, ItemEventType.ItemsMutated);
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
	}

	@Override
	public double[] getStructureCenter(G1 aItem)
	{
		return aItem.getCentroid(refSmallBodyModel);
	}

	@Override
	public double[] getStructureNormal(G1 aItem)
	{
		double[] center = getStructureCenter(aItem);
		return refSmallBodyModel.getNormalAtPoint(center);
	}

	@Override
	public double getStructureSize(G1 aItem)
	{
		return aItem.getSize(refSmallBodyModel);
	}

	protected G1 createStructure()
	{
		Line retLine = new Line(++maxPolygonId);
		if (mode == Mode.PROFILE)
			;
//			retLine.setLineWidth(3.0);

		return (G1) retLine;
	}

	private static final Version CONFIGURATION_VERSION = Version.of(1, 0);
	private static final ContentKey<SettableValue<Double>> OFFSET_KEY = SettableValues.key("offset");
	private static final ContentKey<SettableValue<Double>> LINE_WIDTH_KEY = SettableValues.key("lineWidth");
	private final ContentKey<Value<List<G1>>> LINES_KEY = Values.fixedKey("lineStructures");

	@Override
	public Metadata store()
	{
		KeyValueCollections.Builder<Content> builder = KeyValueCollections.instance().builder();

		builder.put(LINE_WIDTH_KEY, SettableValues.instance().of(lineWidth));
		builder.put(OFFSET_KEY, SettableValues.instance().of(offset));
		builder.put(LINES_KEY, SettableValues.instance().of(getAllItems()));

		Configuration configuration = Configurations.instance().of(CONFIGURATION_VERSION, builder.build());

		return KeyValueCollectionMetadataManager.of(configuration.getVersion(), configuration.getCollection()).store();
	}

	@Override
	public void retrieve(Metadata source)
	{
//		int evaluateMe().evaluateMe..evaluateMe;
//		KeyValueCollection<Content> collection = configuration.getCollection();

		double lineWidth = source.get(Key.of(LINE_WIDTH_KEY.getId()));
		double offset = source.get(Key.of(OFFSET_KEY.getId()));

		List<G1> sourceLines = source.get(Key.of(LINES_KEY.getId()));

		removeAllStructures();

		for (G1 line : sourceLines)
		{
			line.updateAllSegments(refSmallBodyModel);
			if (line instanceof Polygon)
			{
				Polygon polygon = (Polygon) line;
				if (source.hasKey(Key.of(Polygon.SHOW_INTERIOR_KEY.getId())) == true)
					polygon.setShowInterior(refSmallBodyModel, source.get(Key.of(Polygon.SHOW_INTERIOR_KEY.getId())));
			}
		}

		setAllItems(sourceLines);

		setLineWidth(lineWidth);
		setOffset(offset);

		lineActor.SetMapper(vLineMapperRegPDM);
		updatePolyData();
	}

	@Override
	protected void updateVtkColorsFor(Collection<G1> aItemC, boolean aSendNotification)
	{
		Color pickColor = null;
		CommonData commonData = getCommonData();
		if (commonData != null)
			pickColor = commonData.getSelectionColor();

		// Gather VTK vars of interest
		vtkCellData regCD = vLinesRegPD.GetCellData();
		vtkUnsignedCharArray regColorUCA = (vtkUnsignedCharArray) regCD.GetScalars();

		vtkCellData decCD = vLinesDecPD.GetCellData();
		vtkUnsignedCharArray decColorUCA = (vtkUnsignedCharArray) decCD.GetScalars();

		// Update internal VTK state
		for (G1 aItem : aItemC)
		{
			// Skip to next if not visible
			if (aItem.getVisible() == false)
				continue;

			// Skip to next as VTK draw state has not been initialized
			if (aItem.vDrawId == -1)
				continue;

			// Update the color related state
			Color tmpColor = aItem.getColor();
			if (pickColor != null && getSelectedItems().contains(aItem) == true)
				tmpColor = pickColor;

			int tmpId = aItem.vDrawId;
			VtkUtil.setColorOnUCA4(regColorUCA, tmpId, tmpColor);
			VtkUtil.setColorOnUCA4(decColorUCA, tmpId, tmpColor);
		}

		regColorUCA.Modified();
		decColorUCA.Modified();

		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

}

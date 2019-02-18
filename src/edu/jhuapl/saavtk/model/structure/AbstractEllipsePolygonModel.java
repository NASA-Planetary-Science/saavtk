package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Metadata;
import crucible.crust.metadata.api.MetadataManager;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;
import edu.jhuapl.saavtk.model.ColoringData;
import edu.jhuapl.saavtk.model.CommonData;
import edu.jhuapl.saavtk.model.FacetColoringData;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.StructureModel;
import edu.jhuapl.saavtk.util.FileUtil;
import edu.jhuapl.saavtk.util.IdPair;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.util.MathUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import edu.jhuapl.saavtk.util.ProgressListener;
import edu.jhuapl.saavtk.util.Properties;
import edu.jhuapl.saavtk.util.SaavtkLODActor;
import vtk.vtkActor;
import vtk.vtkAppendPolyData;
import vtk.vtkCaptionActor2D;
import vtk.vtkCellArray;
import vtk.vtkCellData;
import vtk.vtkIdTypeArray;
import vtk.vtkPoints;
import vtk.vtkPolyData;
import vtk.vtkPolyDataMapper;
import vtk.vtkProp;
import vtk.vtkProperty;
import vtk.vtkQuadricClustering;
import vtk.vtkTransform;
import vtk.vtkUnsignedCharArray;

/**
 * Model of regular polygon structures drawn on a body.
 */

abstract public class AbstractEllipsePolygonModel extends StructureModel implements PropertyChangeListener, MetadataManager
{
	private final List<EllipsePolygon> polygons = new ArrayList<>();
	private final List<vtkProp> actors = new ArrayList<>();

	private vtkPolyData boundaryPolyData;
	private vtkPolyData decimatedBoundaryPolyData;
	private vtkAppendPolyData boundaryAppendFilter;
	private vtkAppendPolyData decimatedBoundaryAppendFilter;
	private vtkPolyDataMapper boundaryMapper;
	private vtkPolyDataMapper decimatedBoundaryMapper;
	private vtkActor boundaryActor;

	private vtkPolyData interiorPolyData;
	private vtkPolyData decimatedInteriorPolyData;
	private vtkAppendPolyData interiorAppendFilter;
	private vtkAppendPolyData decimatedInteriorAppendFilter;
	private vtkPolyDataMapper interiorMapper;
	private vtkPolyDataMapper decimatedInteriorMapper;
	private vtkActor interiorActor;

	private vtkUnsignedCharArray boundaryColors;
	private vtkUnsignedCharArray decimatedBoundaryColors;
	private vtkUnsignedCharArray interiorColors;
	private vtkUnsignedCharArray decimatedInteriorColors;

	private vtkPolyData emptyPolyData;
	private final PolyhedralModel smallBodyModel;
	private double defaultRadius;
	private final double maxRadius;
	private final int numberOfSides;
	private int[] defaultColor = { 0, 191, 255 };
	// private int[] defaultBoundaryColor = {0, 191, 255};
	// private int[] defaultInteriorColor = {0, 191, 255};
	private double interiorOpacity = 0.3;
	private final String type;
	private int[] selectedStructures = {};
	private int maxPolygonId = 0;
	private double offset;

	public enum Mode
	{
		POINT_MODE, CIRCLE_MODE, ELLIPSE_MODE
	}

	private Mode mode;

	public static class EllipsePolygon extends StructureModel.Structure
	{
		public String name = "default";
		public final int id;
		public String label = "";
		public vtkCaptionActor2D caption;

		public double[] center;
		public double radius; // or semimajor axis
		public double flattening; // ratio of semiminor axis to semimajor axis
		public double angle;
		public boolean hidden = false;
		public boolean labelHidden = false;

		public vtkPolyData boundaryPolyData;
		public vtkPolyData decimatedBoundaryPolyData;
		public vtkPolyData interiorPolyData;
		public vtkPolyData decimatedInteriorPolyData;
		public int numberOfSides;
		public String type;
		public int[] color;
		private Mode mode;

		private static final DecimalFormat DF = new DecimalFormat("#.#####");

		public EllipsePolygon(int numberOfSides, String type, int[] color, Mode mode, int id, String label)
		{
			this.id = id;
			boundaryPolyData = new vtkPolyData();
			decimatedBoundaryPolyData = new vtkPolyData();
			interiorPolyData = new vtkPolyData();
			decimatedInteriorPolyData = new vtkPolyData();
			this.numberOfSides = numberOfSides;
			this.type = type;
			this.color = color.clone();
			this.mode = mode;
			this.label = label;
		}

		@Override
		public int getId()
		{
			return id;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public void setName(String name)
		{
			this.name = name;
		}

		@Override
		public String getType()
		{
			return type;
		}

		@Override
		public String getInfo()
		{
			String str = "Diameter = " + DF.format(2.0 * radius) + " km";
			if (mode == Mode.ELLIPSE_MODE)
			{
				str += ", Flattening = " + DF.format(flattening);
				str += ", Angle = " + DF.format(angle);
			}

			return str;
		}

		@Override
		public int[] getColor()
		{
			return color;
		}

		@Override
		public void setColor(int[] color)
		{
			this.color = color.clone();
		}

		public vtkPolyData getBoundaryPolyData()
		{
			return boundaryPolyData;
		}

		public vtkPolyData getInteriorPolyData()
		{
			return interiorPolyData;
		}

		public void updatePolygon(PolyhedralModel sbModel, double[] center, double radius, double flattening, double angle)
		{
			this.center = center;
			this.radius = radius;
			this.flattening = flattening;
			this.angle = angle;

			if (!hidden)
			{
				sbModel.drawEllipticalPolygon(center, radius, flattening, angle, numberOfSides, interiorPolyData, boundaryPolyData);

				// LatLon ll=MathUtil.reclat(center);
				// System.out.println(Math.toDegrees(ll.lat)+" "+Math.toDegrees(ll.lon));

				// Setup decimator
				vtkQuadricClustering decimator = new vtkQuadricClustering();

				// Decimate interior
				decimator.SetInputData(interiorPolyData);
				decimator.AutoAdjustNumberOfDivisionsOn();
				decimator.CopyCellDataOn();
				decimator.Update();
				decimatedInteriorPolyData.DeepCopy(decimator.GetOutput());

				// Decimate boundary
				decimator.SetInputData(boundaryPolyData);
				decimator.SetNumberOfXDivisions(2);
				decimator.SetNumberOfYDivisions(2);
				decimator.SetNumberOfZDivisions(2);
				decimator.CopyCellDataOn();
				decimator.Update();
				decimatedBoundaryPolyData.DeepCopy(decimator.GetOutput());

				// Destroy decimator
				decimator.Delete();
			}
			else
			{
				PolyDataUtil.clearPolyData(interiorPolyData);
				PolyDataUtil.clearPolyData(decimatedInteriorPolyData);
				PolyDataUtil.clearPolyData(boundaryPolyData);
				PolyDataUtil.clearPolyData(decimatedBoundaryPolyData);
			}
		}

		@Override
		public String getClickStatusBarText()
		{
			return type + ", Id = " + id + ", Diameter = " + 2.0 * radius + " km";
		}

		@Override
		public void setLabel(String label)
		{
			this.label = label;
		}

		@Override
		public String getLabel()
		{
			return label;
		}

		@Override
		public boolean getHidden()
		{
			return hidden;
		}

		@Override
		public boolean getLabelHidden()
		{
			return labelHidden;
		}

		@Override
		public void setHidden(boolean b)
		{
			hidden = b;
		}

		@Override
		public void setLabelHidden(boolean b)
		{
			labelHidden = b;
		}

		private static final Key<EllipsePolygon> ELLIPSE_POLYGON_KEY = Key.of("ellipsePolygon");
		private static final Key<Integer> NUMBER_SIDES_KEY = Key.of("numberSides");
		private static final Key<String> TYPE_KEY = Key.of("type");
		private static final Key<int[]> COLOR_KEY = Key.of("color");
		private static final Key<String> MODE_KEY = Key.of("mode");
		private static final Key<Integer> ID_KEY = Key.of("id");
		private static final Key<String> LABEL_KEY = Key.of("label");
		private static final Key<String> NAME_KEY = Key.of("name");
		private static final Key<double[]> CENTER_KEY = Key.of("center");
		private static final Key<Double> RADIUS_KEY = Key.of("radius");
		private static final Key<Double> FLATTENING_KEY = Key.of("flattening");
		private static final Key<Double> ANGLE_KEY = Key.of("angle");
		private static final Key<Boolean> HIDDEN_KEY = Key.of("hidden");
		private static final Key<Boolean> LABEL_HIDDEN_KEY = Key.of("labelHidden");

		public static void initializeSerializationProxy()
		{
			InstanceGetter.defaultInstanceGetter().register(ELLIPSE_POLYGON_KEY, (source) -> {
				int numberSides = source.get(NUMBER_SIDES_KEY);
				String type = source.get(TYPE_KEY);
				int[] color = source.get(COLOR_KEY);
				Mode mode = Mode.valueOf(source.get(MODE_KEY));
				int id = source.get(ID_KEY);
				String label = source.get(LABEL_KEY);

				EllipsePolygon result = new EllipsePolygon(numberSides, type, color, mode, id, label);

				result.name = source.get(NAME_KEY);
				result.center = source.get(CENTER_KEY);
				result.radius = source.get(RADIUS_KEY);
				result.flattening = source.get(FLATTENING_KEY);
				result.angle = source.get(ANGLE_KEY);
				result.hidden = source.get(HIDDEN_KEY);
				result.labelHidden = source.get(LABEL_HIDDEN_KEY);

				return result;
			}, EllipsePolygon.class, polygon -> {
				SettableMetadata result = SettableMetadata.of(Version.of(1, 0));

				result.put(NUMBER_SIDES_KEY, polygon.numberOfSides);
				result.put(TYPE_KEY, polygon.type);
				result.put(COLOR_KEY, polygon.color);
				result.put(MODE_KEY, polygon.mode.name());
				result.put(ID_KEY, polygon.id);
				result.put(LABEL_KEY, polygon.label);

				result.put(NAME_KEY, polygon.name);
				result.put(CENTER_KEY, polygon.center);
				result.put(RADIUS_KEY, polygon.radius);
				result.put(FLATTENING_KEY, polygon.flattening);
				result.put(ANGLE_KEY, polygon.angle);
				result.put(HIDDEN_KEY, polygon.hidden);
				result.put(LABEL_HIDDEN_KEY, polygon.labelHidden);

				return result;

			});
		}

	}

	public AbstractEllipsePolygonModel(PolyhedralModel smallBodyModel, int numberOfSides, Mode mode, String type)
	{
		this.smallBodyModel = smallBodyModel;

		this.offset = getDefaultOffset();

		defaultRadius = smallBodyModel.getBoundingBoxDiagonalLength() / 155.0;
		maxRadius = smallBodyModel.getBoundingBoxDiagonalLength() / 8.0;

		this.smallBodyModel.addPropertyChangeListener(this);

		emptyPolyData = new vtkPolyData();

		this.numberOfSides = numberOfSides;
		this.mode = mode;
		this.type = type;

		boundaryColors = new vtkUnsignedCharArray();
		decimatedBoundaryColors = new vtkUnsignedCharArray();
		boundaryColors.SetNumberOfComponents(3);
		decimatedBoundaryColors.SetNumberOfComponents(3);

		interiorColors = new vtkUnsignedCharArray();
		decimatedInteriorColors = new vtkUnsignedCharArray();
		interiorColors.SetNumberOfComponents(3);
		decimatedInteriorColors.SetNumberOfComponents(3);

		boundaryPolyData = new vtkPolyData();
		decimatedBoundaryPolyData = new vtkPolyData();
		boundaryAppendFilter = new vtkAppendPolyData();
		boundaryAppendFilter.UserManagedInputsOn();
		decimatedBoundaryAppendFilter = new vtkAppendPolyData();
		decimatedBoundaryAppendFilter.UserManagedInputsOn();
		boundaryMapper = new vtkPolyDataMapper();
		decimatedBoundaryMapper = new vtkPolyDataMapper();
		boundaryActor = new SaavtkLODActor();
		vtkProperty boundaryProperty = boundaryActor.GetProperty();
		boundaryProperty.LightingOff();
		boundaryProperty.SetLineWidth(2.0);

		actors.add(boundaryActor);

		interiorPolyData = new vtkPolyData();
		decimatedInteriorPolyData = new vtkPolyData();
		interiorAppendFilter = new vtkAppendPolyData();
		interiorAppendFilter.UserManagedInputsOn();
		decimatedInteriorAppendFilter = new vtkAppendPolyData();
		decimatedInteriorAppendFilter.UserManagedInputsOn();
		interiorMapper = new vtkPolyDataMapper();
		decimatedInteriorMapper = new vtkPolyDataMapper();
		interiorActor = new SaavtkLODActor();
		vtkProperty interiorProperty = interiorActor.GetProperty();
		interiorProperty.LightingOff();
		interiorProperty.SetOpacity(interiorOpacity);
		// interiorProperty.SetLineWidth(2.0);

		actors.add(interiorActor);
	}

	public void setDefaultColor(int[] color)
	{
		this.defaultColor = color.clone();
	}

	public int[] getDefaultColor()
	{
		return defaultColor;
	}

	public void setPolygonColor(int i, int[] color)
	{
		this.polygons.get(i).setColor(color);
	}

	public int[] getPolygonColor(int i)
	{
		return this.polygons.get(i).color;
	}

	/*
	 * public int[] getDefaultBoundaryColor() { return defaultBoundaryColor; }
	 * 
	 * 
	 * public void setDefaultBoundaryColor(int[] color) { this.defaultBoundaryColor
	 * = (int[])color.clone(); }
	 * 
	 * public int[] getDefaultInteriorColor() { return defaultInteriorColor; }
	 * 
	 * public void setDefaultInteriorColor(int[] color) { this.defaultInteriorColor
	 * = (int[])color.clone(); }
	 */

	public double getInteriorOpacity()
	{
		return interiorOpacity;
	}

	public void setInteriorOpacity(double opacity)
	{
		this.interiorOpacity = opacity;
		interiorActor.GetProperty().SetOpacity(opacity);
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	private void updatePolyData()
	{
		actors.clear();

		if (polygons.size() > 0)
		{
			boundaryAppendFilter.SetNumberOfInputs(polygons.size());
			decimatedBoundaryAppendFilter.SetNumberOfInputs(polygons.size());
			interiorAppendFilter.SetNumberOfInputs(polygons.size());
			decimatedInteriorAppendFilter.SetNumberOfInputs(polygons.size());

			for (int i = 0; i < polygons.size(); ++i)
			{
				vtkPolyData poly = polygons.get(i).boundaryPolyData;
				if (poly != null)
					boundaryAppendFilter.SetInputDataByNumber(i, poly);
				poly = polygons.get(i).decimatedBoundaryPolyData;
				if (poly != null)
					decimatedBoundaryAppendFilter.SetInputDataByNumber(i, poly);
				poly = polygons.get(i).interiorPolyData;
				if (poly != null)
					interiorAppendFilter.SetInputDataByNumber(i, poly);
				poly = polygons.get(i).decimatedInteriorPolyData;
				if (poly != null)
					decimatedInteriorAppendFilter.SetInputDataByNumber(i, poly);
			}
			for (int j = 0; j < polygons.size(); ++j)
			{
				EllipsePolygon lin = polygons.get(j);
				if (lin.label != null && !lin.labelHidden && !lin.hidden && lin.caption != null)
				{
					lin.caption.SetAttachmentPoint(lin.center);
					actors.add(lin.caption);
				}
			}

			boundaryAppendFilter.Update();
			decimatedBoundaryAppendFilter.Update();
			interiorAppendFilter.Update();
			decimatedInteriorAppendFilter.Update();

			vtkPolyData boundaryAppendFilterOutput = boundaryAppendFilter.GetOutput();
			vtkPolyData decimatedBoundaryAppendFilterOutput = decimatedBoundaryAppendFilter.GetOutput();
			vtkPolyData interiorAppendFilterOutput = interiorAppendFilter.GetOutput();
			vtkPolyData decimatedInteriorAppendFilterOutput = decimatedInteriorAppendFilter.GetOutput();
			boundaryPolyData.DeepCopy(boundaryAppendFilterOutput);
			decimatedBoundaryPolyData.DeepCopy(decimatedBoundaryAppendFilterOutput);
			interiorPolyData.DeepCopy(interiorAppendFilterOutput);
			decimatedInteriorPolyData.DeepCopy(decimatedInteriorAppendFilterOutput);

			smallBodyModel.shiftPolyLineInNormalDirection(boundaryPolyData, offset);
			smallBodyModel.shiftPolyLineInNormalDirection(decimatedBoundaryPolyData, offset);
			PolyDataUtil.shiftPolyDataInNormalDirection(interiorPolyData, offset);
			PolyDataUtil.shiftPolyDataInNormalDirection(decimatedInteriorPolyData, offset);

			boundaryColors.SetNumberOfTuples(boundaryPolyData.GetNumberOfCells());
			decimatedBoundaryColors.SetNumberOfTuples(decimatedBoundaryPolyData.GetNumberOfCells());
			interiorColors.SetNumberOfTuples(interiorPolyData.GetNumberOfCells());
			decimatedInteriorColors.SetNumberOfTuples(decimatedInteriorPolyData.GetNumberOfCells());
			for (int i = 0; i < polygons.size(); ++i)
			{
				int[] color = polygons.get(i).color;

				if (Arrays.binarySearch(this.selectedStructures, i) >= 0)
				{
					CommonData commonData = getCommonData();
					if (commonData != null)
						color = commonData.getSelectionColor();
				}

				IdPair range = this.getCellIdRangeOfPolygon(i, false);
				for (int j = range.id1; j < range.id2; ++j)
					boundaryColors.SetTuple3(j, color[0], color[1], color[2]);

				range = this.getCellIdRangeOfDecimatedPolygon(i, false);
				for (int j = range.id1; j < range.id2; ++j)
					decimatedBoundaryColors.SetTuple3(j, color[0], color[1], color[2]);

				range = this.getCellIdRangeOfPolygon(i, true);
				for (int j = range.id1; j < range.id2; ++j)
					interiorColors.SetTuple3(j, color[0], color[1], color[2]);

				range = this.getCellIdRangeOfDecimatedPolygon(i, true);
				for (int j = range.id1; j < range.id2; ++j)
					decimatedInteriorColors.SetTuple3(j, color[0], color[1], color[2]);

			}
			vtkCellData boundaryCellData = boundaryPolyData.GetCellData();
			vtkCellData decimatedBoundaryCellData = decimatedBoundaryPolyData.GetCellData();
			vtkCellData interiorCellData = interiorPolyData.GetCellData();
			vtkCellData decimatedInteriorCellData = decimatedInteriorPolyData.GetCellData();

			actors.add(interiorActor);
			actors.add(boundaryActor);

			boundaryCellData.SetScalars(boundaryColors);
			decimatedBoundaryCellData.SetScalars(decimatedBoundaryColors);
			interiorCellData.SetScalars(interiorColors);
			decimatedInteriorCellData.SetScalars(decimatedInteriorColors);

			boundaryAppendFilterOutput.Delete();
			decimatedBoundaryAppendFilterOutput.Delete();
			interiorAppendFilterOutput.Delete();
			decimatedInteriorAppendFilterOutput.Delete();
			boundaryCellData.Delete();
			decimatedBoundaryCellData.Delete();
			interiorCellData.Delete();
			decimatedInteriorCellData.Delete();
		}
		else
		{
			boundaryPolyData.DeepCopy(emptyPolyData);
			decimatedBoundaryPolyData.DeepCopy(emptyPolyData);
			interiorPolyData.DeepCopy(emptyPolyData);
			decimatedInteriorPolyData.DeepCopy(emptyPolyData);
		}

		boundaryMapper.SetInputData(boundaryPolyData);
		decimatedBoundaryMapper.SetInputData(decimatedBoundaryPolyData);
		interiorMapper.SetInputData(interiorPolyData);
		decimatedInteriorMapper.SetInputData(decimatedInteriorPolyData);

		boundaryActor.SetMapper(boundaryMapper);
		((SaavtkLODActor) boundaryActor).setLODMapper(decimatedBoundaryMapper);
		interiorActor.SetMapper(interiorMapper);
		((SaavtkLODActor) interiorActor).setLODMapper(decimatedInteriorMapper);

		boundaryActor.Modified();
		interiorActor.Modified();
	}

	@Override
	public List<vtkProp> getProps()
	{
		return actors;
	}

	@Override
	public String getClickStatusBarText(vtkProp prop, int cellId, @SuppressWarnings("unused") double[] pickPosition)
	{
		if (prop == boundaryActor || prop == interiorActor)
		{
			int polygonId = this.getPolygonIdFromCellId(cellId, prop == interiorActor);
			if (polygonId == -1)
				return "";

			EllipsePolygon pol = polygons.get(polygonId);
			return pol.getClickStatusBarText();
		}

		return "";
	}

	@Override
	public int getNumberOfStructures()
	{
		return polygons.size();
	}

	@Override
	public Structure getStructure(int polygonId)
	{
		return polygons.get(polygonId);
	}

	public vtkActor getBoundaryActor()
	{
		return boundaryActor;
	}

	public vtkActor getInteriorActor()
	{
		return interiorActor;
	}

	@Override
	public Structure addNewStructure()
	{
		return null;
	}

	public void addNewStructure(double[] pos, double radius, double flattening, double angle)
	{
		EllipsePolygon pol = new EllipsePolygon(numberOfSides, type, defaultColor, mode, ++maxPolygonId, "");
		polygons.add(pol);

		pol.updatePolygon(smallBodyModel, pos, radius, flattening, angle);
		selectedStructures = new int[] { polygons.size() - 1 };
		updatePolyData();

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		this.pcs.firePropertyChange(Properties.STRUCTURE_ADDED, null, null);
	}

	public void addNewStructure(double[] pos)
	{
		addNewStructure(pos, defaultRadius, 1.0, 0.);
	}

	@Override
	public void removeStructure(int polygonId)
	{
		if (polygons.get(polygonId).caption != null)
			polygons.get(polygonId).caption.VisibilityOff();
		polygons.get(polygonId).caption = null;
		polygons.remove(polygonId);

		updatePolyData();

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		this.pcs.firePropertyChange(Properties.STRUCTURE_REMOVED, null, polygonId);
	}

	@Override
	public void removeStructures(int[] polygonIds)
	{
		if (polygonIds == null || polygonIds.length == 0)
			return;

		Arrays.sort(polygonIds);
		for (int i = polygonIds.length - 1; i >= 0; --i)
		{
			if (polygons.get(polygonIds[i]).caption != null)
				polygons.get(polygonIds[i]).caption.VisibilityOff();
			polygons.get(polygonIds[i]).caption = null;
			polygons.remove(polygonIds[i]);
			this.pcs.firePropertyChange(Properties.STRUCTURE_REMOVED, null, polygonIds[i]);
		}

		updatePolyData();

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public void removeAllStructures()
	{
		for (int i = 0; i < polygons.size(); i++)
		{
			if (polygons.get(i).caption != null)
				polygons.get(i).caption.VisibilityOff();
			polygons.get(i).caption = null;

		}
		polygons.clear();

		updatePolyData();

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		this.pcs.firePropertyChange(Properties.ALL_STRUCTURES_REMOVED, null, null);
	}

	public void movePolygon(int polygonId, double[] newCenter)
	{
		EllipsePolygon pol = polygons.get(polygonId);
		pol.updatePolygon(smallBodyModel, newCenter, pol.radius, pol.flattening, pol.angle);
		updatePolyData();

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	/**
	 * Move the polygon to the specified latitude and longitude.
	 *
	 * @param polygonId
	 * @param latitude - in radians
	 * @param longitude - in radians
	 */
	public void movePolygon(int polygonId, double latitude, double longitude)
	{
		double[] newCenter = new double[3];
		smallBodyModel.getPointAndCellIdFromLatLon(latitude, longitude, newCenter);
		double[] center = getStructureCenter(polygonId);

		Vector3D centerVec = new Vector3D(center);
		Vector3D newCenterVec = new Vector3D(newCenter);
		newCenterVec = newCenterVec.scalarMultiply(centerVec.getNorm() / newCenterVec.getNorm());// there is sometimes a
																									// radial offset
																									// (parallel to both
																									// center and
																									// newCenter) that
																									// needs to be
																									// corrected

		// System.out.println(newCenterVec+" "+centerVec+"
		// "+newCenterVec.crossProduct(centerVec));
		// LatLon ll=MathUtil.reclat(centerVec.toArray());
		// LatLon ll2=MathUtil.reclat(newCenterVec.toArray());
		// System.out.println(Math.toDegrees(ll.lat)+" "+Math.toDegrees(ll.lon)+"
		// "+Math.toDegrees(ll2.lat)+" "+Math.toDegrees(ll2.lon));
		movePolygon(polygonId, newCenterVec.toArray());
	}

	public void changeRadiusOfPolygon(int polygonId, double[] newPointOnPerimeter)
	{
		EllipsePolygon pol = polygons.get(polygonId);
		double newRadius =
				Math.sqrt((pol.center[0] - newPointOnPerimeter[0]) * (pol.center[0] - newPointOnPerimeter[0]) + (pol.center[1] - newPointOnPerimeter[1]) * (pol.center[1] - newPointOnPerimeter[1]) + (pol.center[2] - newPointOnPerimeter[2]) * (pol.center[2] - newPointOnPerimeter[2]));
		if (newRadius > maxRadius)
			newRadius = maxRadius;

		pol.updatePolygon(smallBodyModel, pol.center, newRadius, pol.flattening, pol.angle);
		updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	protected double computeFlatteningOfPolygon(double[] center, double radius, double angle, double[] newPointOnPerimeter)
	{
		// The following math does this: we need to find the direction of
		// the semimajor axis of the ellipse. Then once we have that
		// we need to find the distance to that line from the point the mouse
		// is hovering, where that point is first projected onto the
		// tangent plane of the asteroid at the ellipse center.
		// This distance divided by the semimajor axis of the ellipse
		// is what we call the flattening.

		// First compute cross product of normal and z axis
		double[] normal = smallBodyModel.getNormalAtPoint(center);
		double[] zaxis = { 0.0, 0.0, 1.0 };
		double[] cross = new double[3];
		MathUtil.vcrss(zaxis, normal, cross);
		// Compute angle between normal and zaxis
		double sepAngle = MathUtil.vsep(normal, zaxis) * 180.0 / Math.PI;

		vtkTransform transform = new vtkTransform();
		transform.Translate(center);
		transform.RotateWXYZ(sepAngle, cross);
		transform.RotateZ(angle);

		double[] xaxis = { 1.0, 0.0, 0.0 };
		xaxis = transform.TransformDoubleVector(xaxis);
		MathUtil.vhat(xaxis, xaxis);

		// Project newPoint onto the plane perpendicular to the
		// normal of the shape model.
		double[] projPoint = new double[3];
		MathUtil.vprjp(newPointOnPerimeter, normal, center, projPoint);
		double[] projDir = new double[3];
		MathUtil.vsub(projPoint, center, projDir);

		double[] proj = new double[3];
		MathUtil.vproj(projDir, xaxis, proj);
		double[] distVec = new double[3];
		MathUtil.vsub(projDir, proj, distVec);
		double newRadius = MathUtil.vnorm(distVec);

		double newFlattening = 1.0;
		if (radius > 0.0)
			newFlattening = newRadius / radius;

		if (newFlattening < 0.001)
			newFlattening = 0.001;
		else if (newFlattening > 1.0)
			newFlattening = 1.0;

		transform.Delete();

		return newFlattening;
	}

	public void changeFlatteningOfPolygon(int polygonId, double[] newPointOnPerimeter)
	{
		EllipsePolygon pol = polygons.get(polygonId);

		double newFlattening = computeFlatteningOfPolygon(pol.center, pol.radius, pol.angle, newPointOnPerimeter);

		pol.updatePolygon(smallBodyModel, pol.center, pol.radius, newFlattening, pol.angle);

		updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	protected double computeAngleOfPolygon(double[] center, double[] newPointOnPerimeter)
	{
		// The following math does this: we need to find the direction of
		// the semimajor axis of the ellipse. Then once we have that
		// we need to find the angular distance between the axis and the
		// vector from the ellipse center to the point the mouse
		// is hovering, where that vector is first projected onto the
		// tangent plane of the asteroid at the ellipse center.
		// This angular distance is what we rotate the ellipse by.

		// First compute cross product of normal and z axis
		double[] normal = smallBodyModel.getNormalAtPoint(center);
		double[] zaxis = { 0.0, 0.0, 1.0 };
		double[] cross = new double[3];
		MathUtil.vcrss(zaxis, normal, cross);
		// Compute angle between normal and zaxis
		double sepAngle = MathUtil.vsep(normal, zaxis) * 180.0 / Math.PI;

		vtkTransform transform = new vtkTransform();
		transform.Translate(center);
		transform.RotateWXYZ(sepAngle, cross);

		double[] xaxis = { 1.0, 0.0, 0.0 };
		xaxis = transform.TransformDoubleVector(xaxis);
		MathUtil.vhat(xaxis, xaxis);

		// Project newPoint onto the plane perpendicular to the
		// normal of the shape model.
		double[] projPoint = new double[3];
		MathUtil.vprjp(newPointOnPerimeter, normal, center, projPoint);
		double[] projDir = new double[3];
		MathUtil.vsub(projPoint, center, projDir);
		MathUtil.vhat(projDir, projDir);

		// Compute angular distance between projected direction and transformed x-axis
		double newAngle = MathUtil.vsep(projDir, xaxis) * 180.0 / Math.PI;

		// We need to negate this angle under certain conditions.
		if (newAngle != 0.0)
		{
			MathUtil.vcrss(xaxis, projDir, cross);
			double a = MathUtil.vsep(cross, normal) * 180.0 / Math.PI;
			if (a > 90.0)
				newAngle = -newAngle;
		}

		transform.Delete();

		return newAngle;
	}

	public void changeAngleOfPolygon(int polygonId, double[] newPointOnPerimeter)
	{
		EllipsePolygon pol = polygons.get(polygonId);

		double newAngle = computeAngleOfPolygon(pol.center, newPointOnPerimeter);

		pol.updatePolygon(smallBodyModel, pol.center, pol.radius, pol.flattening, newAngle);
		updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	public void changeRadiusOfAllPolygons(double newRadius)
	{
		for (EllipsePolygon pol : this.polygons)
		{
			pol.updatePolygon(smallBodyModel, pol.center, newRadius, pol.flattening, pol.angle);
		}

		updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public void activateStructure(@SuppressWarnings("unused") int idx)
	{
		// Do nothing. RegularPolygonModel does not support activation.
	}

	/**
	 * A picker picking the actor of this model will return a cellId. But since
	 * there are many cells per RegularPolygon, we need to be able to figure out
	 * which RegularPolygon was picked
	 */
	private int getPolygonIdFromCellId(int cellId, boolean interior)
	{
		int numberCellsSoFar = 0;
		for (int i = 0; i < polygons.size(); ++i)
		{
			if (interior)
				numberCellsSoFar += polygons.get(i).interiorPolyData.GetNumberOfCells();
			else
				numberCellsSoFar += polygons.get(i).boundaryPolyData.GetNumberOfCells();
			if (cellId < numberCellsSoFar)
				return i;
		}
		return -1;
	}

	public int getPolygonIdFromBoundaryCellId(int cellId)
	{
		return this.getPolygonIdFromCellId(cellId, false);
	}

	public int getPolygonIdFromInteriorCellId(int cellId)
	{
		return this.getPolygonIdFromCellId(cellId, true);
	}

	private IdPair getCellIdRangeOfPolygon(int polygonId, boolean interior)
	{
		int startCell = 0;
		for (int i = 0; i < polygonId; ++i)
		{
			if (interior)
				startCell += polygons.get(i).interiorPolyData.GetNumberOfCells();
			else
				startCell += polygons.get(i).boundaryPolyData.GetNumberOfCells();
		}

		int endCell = startCell;
		if (interior)
			endCell += polygons.get(polygonId).interiorPolyData.GetNumberOfCells();
		else
			endCell += polygons.get(polygonId).boundaryPolyData.GetNumberOfCells();

		return new IdPair(startCell, endCell);
	}

	private IdPair getCellIdRangeOfDecimatedPolygon(int polygonId, boolean interior)
	{
		int startCell = 0;
		for (int i = 0; i < polygonId; ++i)
		{
			if (interior)
				startCell += polygons.get(i).decimatedInteriorPolyData.GetNumberOfCells();
			else
				startCell += polygons.get(i).decimatedBoundaryPolyData.GetNumberOfCells();
		}

		int endCell = startCell;
		if (interior)
			endCell += polygons.get(polygonId).decimatedInteriorPolyData.GetNumberOfCells();
		else
			endCell += polygons.get(polygonId).decimatedBoundaryPolyData.GetNumberOfCells();

		return new IdPair(startCell, endCell);
	}

	@Override
	public void loadModel(File file, boolean append, ProgressListener listener) throws IOException
	{
		List<String> lines = FileUtil.getFileLinesAsStringList(file.getAbsolutePath());
		List<String> labels = new ArrayList<>();
		List<EllipsePolygon> newPolygons = new ArrayList<>();
		// int maxPolygonId = append ? this.maxPolygonId : 0;
		for (int i = 0; i < lines.size(); ++i)
		{
			if (listener != null)
				listener.setProgress(i * 100 / lines.size());
			// String[] words = lines.get(i).trim().split("\\s+");
			List<String> list = new ArrayList<>();
			Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(lines.get(i));
			while (m.find())
				list.add(m.group(1));
			String[] words = new String[list.size()];
			list.toArray(words);
			// The latest version of this file format has 16 columns. The previous version
			// had
			// 10 columns for circles and 13 columns for points. We still want to support
			// loading
			// both versions, so look at how many columns are in the line.
			EllipsePolygon pol = new EllipsePolygon(numberOfSides, type, defaultColor, mode, Integer.parseInt(words[0]), "");
			// maxPolygonId = Integer.parseInt(words[0]) + 1;
			pol.center = new double[3];

			// The first 8 columns are the same in both the old and new formats.
			pol.name = words[1];
			pol.center[0] = Double.parseDouble(words[2]);
			pol.center[1] = Double.parseDouble(words[3]);
			pol.center[2] = Double.parseDouble(words[4]);

			if (pol.id > maxPolygonId)
				maxPolygonId = pol.id;

			// Note the next 3 words in the line (the point in spherical coordinates) are
			// not used

			// LatLon latLon=MathUtil.reclat(pol.center);
			// System.out.println(words[5]+" "+(360-Double.parseDouble(words[6]))+"
			// "+Math.toDegrees(latLon.lat)+" "+Math.toDegrees(latLon.lon));

			// For the new format and the points file in the old format, the next 4 columns
			// (slope,
			// elevation, acceleration, and potential) are not used.
			if (words.length == 18)
			{
				pol.radius = Double.parseDouble(words[12]) / 2.0; // read in diameter not radius
				if (mode == Mode.ELLIPSE_MODE)
				{
					pol.flattening = Double.parseDouble(words[13]);
					pol.angle = Double.parseDouble(words[14]);
				}
				else
				{
					pol.flattening = 1.0;
					pol.angle = 0.0;
				}
				int colorIdx = 15;
				String[] colorStr = words[colorIdx].split(",");
				if (colorStr.length == 3)
				{
					pol.color[0] = Integer.parseInt(colorStr[0]);
					pol.color[1] = Integer.parseInt(colorStr[1]);
					pol.color[2] = Integer.parseInt(colorStr[2]);
				}
				pol.updatePolygon(smallBodyModel, pol.center, pol.radius, pol.flattening, pol.angle);
				newPolygons.add(pol);

				if (words[words.length - 1].startsWith("\"")) // labels in quotations
				{
					pol.label = words[words.length - 1];
					pol.label = pol.label.substring(1, pol.label.length() - 1);
					labels.add(pol.label);
				}
			}
			else
			{

				if (words.length < 16)
				{
					// OLD VERSION of file
					if (mode == Mode.CIRCLE_MODE || mode == Mode.ELLIPSE_MODE)
						pol.radius = Double.parseDouble(words[8]) / 2.0; // read in diameter not radius
					else
						pol.radius = defaultRadius;
				}
				else
				{
					// NEW VERSION of file
					pol.radius = Double.parseDouble(words[12]) / 2.0; // read in diameter not radius
				}

				if (mode == Mode.ELLIPSE_MODE && words.length >= 16)
				{
					pol.flattening = Double.parseDouble(words[13]);
					pol.angle = Double.parseDouble(words[14]);
				}
				else
				{
					pol.flattening = 1.0;
					pol.angle = 0.0;
				}

				// If there are 9 or more columns in the file, the last column is the color in
				// both
				// the new and old formats.
				if (words.length > 9)
				{
					int colorIdx = words.length - 3;
					if (words.length == 17)
						colorIdx = 15;

					String[] colorStr = words[colorIdx].split(",");
					if (colorStr.length == 3)
					{
						pol.color[0] = Integer.parseInt(colorStr[0]);
						pol.color[1] = Integer.parseInt(colorStr[1]);
						pol.color[2] = Integer.parseInt(colorStr[2]);
					}
				}

				pol.updatePolygon(smallBodyModel, pol.center, pol.radius, pol.flattening, pol.angle);
				newPolygons.add(pol);

				// System.out.println(pol.name+" "+Arrays.toString(pol.center));

				// Second to last word is the label, last string is the color
				if (words[words.length - 2].substring(0, 2).equals("l:"))
				{
					pol.label = words[words.length - 2].substring(2);
					labels.add(pol.label);
				}
				// new format means no color
				else if (words[words.length - 1].startsWith("\"")) // labels in quotations
				{
					pol.label = words[words.length - 1];
					pol.label = pol.label.substring(1, pol.label.length() - 1);
					labels.add(pol.label);
				}
				// else
				// {
				// pol.label = words[words.length-1];
				// labels.add(pol.label);
				// }

				// if(words[words.length-1].substring(0, 3).equals("lc:"))
				// {
				// double[] labelcoloradd = {1.0,1.0,1.0};
				// String[] labelColors=words[words.length-1].substring(3).split(",");
				// labelcoloradd[0] = Double.parseDouble(labelColors[0]);
				// labelcoloradd[1] = Double.parseDouble(labelColors[1]);
				// labelcoloradd[2] = Double.parseDouble(labelColors[2]);
				// pol.labelcolor=labelcoloradd;
				// colors.add(labelcoloradd);
				// }
			}
		}
		if (listener != null)
			listener.setProgress(100);
		// Only if we reach here and no exception is thrown do we modify this class
		if (append)
		{
			int init = polygons.size() - 1;
			polygons.addAll(newPolygons);

			for (int i = init; i < init + labels.size(); i++)
			{
				if (polygons.get(i).label != null)
				{
					setStructureLabel(i, labels.get(i - init));
					// if(polygons.get(i).labelcolor!=null)
					// {
					// colorLabel(i,colors.get((i-init)));
					// }
				}
			}
		}
		else
		{
			polygons.clear();
			polygons.addAll(newPolygons);
			for (int i = 0; i < labels.size(); i++)
			{
				if (polygons.get(i).label != null)
				{
					setStructureLabel(i, labels.get(i));
					// if(polygons.get(i).labelcolor!=null)
					// {
					// colorLabel(i,colors.get(i));
					// }
				}
			}
		}

		updatePolyData();

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public void saveModel(File file) throws IOException
	{
		FileWriter fstream = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstream);

		for (EllipsePolygon pol : polygons)
		{
			String name = pol.name;
			if (name.length() == 0)
				name = "default";

			// Since tab is used as the delimiter, replace any tabs in the name with spaces.
			name = name.replace('\t', ' ');

			LatLon llr = MathUtil.reclat(pol.center);
			double lat = llr.lat * 180.0 / Math.PI;
			double lon = llr.lon * 180.0 / Math.PI;
			if (lon < 0.0)
				lon += 360.0;

			String str = pol.id + "\t" + name + "\t" + pol.center[0] + "\t" + pol.center[1] + "\t" + pol.center[2] + "\t" + lat + "\t" + lon + "\t" + llr.rad;

			str += "\t";

			double[] values = getStandardColoringValuesAtPolygon(pol);
			for (int i = 0; i < values.length; ++i)
			{
				str += Double.isNaN(values[i]) ? "NA" : values[i];
				if (i < values.length - 1)
					str += "\t";
			}

			str += "\t" + 2.0 * pol.radius; // save out as diameter, not radius

			str += "\t" + pol.flattening + "\t" + pol.angle;

			str += "\t" + pol.color[0] + "," + pol.color[1] + "," + pol.color[2];

			if (mode == Mode.ELLIPSE_MODE)
			{
				Double gravityAngle = getEllipseAngleRelativeToGravityVector(pol);
				if (gravityAngle != null)
					str += "\t" + gravityAngle;
				else
					str += "\t" + "NA";
			}

			str += "\t" + "\"" + pol.label + "\"";

			// String labelcolorStr="\tlc:"+pol.labelcolor[0] + "," + pol.labelcolor[1] +
			// "," + pol.labelcolor[2];
			// str+=labelcolorStr;

			str += "\n";

			out.write(str);
		}

		out.close();
	}

	@Override
	public int getActivatedStructureIndex()
	{
		return -1;
	}

	@Override
	public boolean supportsActivation()
	{
		return false;
	}

	public double getDefaultRadius()
	{
		return defaultRadius;
	}

	public void setDefaultRadius(double radius)
	{
		this.defaultRadius = radius;
	}

	@Override
	public void selectStructures(int[] indices)
	{
		this.selectedStructures = indices.clone();
		Arrays.sort(selectedStructures);
		updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public int[] getSelectedStructures()
	{
		return selectedStructures;
	}

	@Override
	public int getStructureIndexFromCellId(int cellId, vtkProp prop)
	{
		if (prop == boundaryActor)
		{
			return getPolygonIdFromBoundaryCellId(cellId);
		}
		else if (prop == interiorActor)
		{
			return getPolygonIdFromInteriorCellId(cellId);
		}

		return -1;
	}

	public void redrawAllStructures()
	{
		for (EllipsePolygon pol : this.polygons)
		{
			pol.updatePolygon(smallBodyModel, pol.center, pol.radius, pol.flattening, pol.angle);
		}

		updatePolyData();

		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (Properties.MODEL_RESOLUTION_CHANGED.equals(evt.getPropertyName()))
		{
			redrawAllStructures();
		}
	}

	@Override
	public void setStructureColor(int idx, int[] color)
	{
		polygons.get(idx).setColor(color);
		updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	private double[] getStandardColoringValuesAtPolygon(EllipsePolygon pol) throws IOException
	{
		// Output array of 4 standard colorings (Slope, Elevation, GravAccel,
		// GravPotential).
		// Assume at the outset that none of the standard colorings are available.
		final double[] standardValues = new double[] { Double.NaN, Double.NaN, Double.NaN, Double.NaN };

		if (!smallBodyModel.isColoringDataAvailable())
			return standardValues;

		int slopeIndex = -1;
		int elevationIndex = -1;
		int accelerationIndex = -1;
		int potentialIndex = -1;

		// Locate any of the 4 standard plate colorings in the list of all colorings
		// available for this resolution.
		// Usually the standard colorings are first in the list, so the loop could
		// terminate after all
		// 4 are >= 0, but omitting this check for brevity and readability.
		List<ColoringData> coloringDataList = smallBodyModel.getAllColoringData();
		for (int index = 0; index < coloringDataList.size(); ++index)
		{
			String name = coloringDataList.get(index).getName();
			if (name.equalsIgnoreCase(PolyhedralModel.SlopeStr))
			{
				slopeIndex = index;
			}
			else if (name.equalsIgnoreCase(PolyhedralModel.ElevStr))
			{
				elevationIndex = index;
			}
			else if (name.equalsIgnoreCase(PolyhedralModel.GravAccStr))
			{
				accelerationIndex = index;
			}
			// This is a hack -- unfortunately, in at least OREx's case, this vector is
			// given a different name.
			else if (name.equalsIgnoreCase("Gravitational Magnitude"))
			{
				accelerationIndex = index;
			}
			else if (name.equalsIgnoreCase(PolyhedralModel.GravPotStr))
			{
				potentialIndex = index;
			}
		}

		// Get all the coloring values interpolated at the center of the polygon.
		double[] allValues = smallBodyModel.getAllColoringValues(pol.center);
		if (mode != Mode.POINT_MODE)
		{
			// Replace slope and/or elevation central values with the average over the rim
			// of the circle.
			if (slopeIndex != -1 || elevationIndex != -1)
			{
				if (slopeIndex != -1)
					allValues[slopeIndex] = 0.; // Accumulate weighted sum in situ.
				if (elevationIndex != -1)
					allValues[elevationIndex] = 0.; // Accumulate weighted sum in situ.

				vtkCellArray lines = pol.boundaryPolyData.GetLines();
				vtkPoints points = pol.boundaryPolyData.GetPoints();

				vtkIdTypeArray idArray = lines.GetData();
				int size = idArray.GetNumberOfTuples();

				double totalLength = 0.0;
				double[] midpoint = new double[3];
				for (int i = 0; i < size; i += 3)
				{
					if (idArray.GetValue(i) != 2)
					{
						System.out.println("Big problem: polydata corrupted");
						return standardValues;
					}

					double[] pt1 = points.GetPoint(idArray.GetValue(i + 1));
					double[] pt2 = points.GetPoint(idArray.GetValue(i + 2));

					MathUtil.midpointBetween(pt1, pt2, midpoint);
					double dist = MathUtil.distanceBetween(pt1, pt2);
					totalLength += dist;

					double[] valuesAtMidpoint = smallBodyModel.getAllColoringValues(midpoint);

					// Accumulate sums weighted by the length of this polygon segment.
					if (slopeIndex != -1)
						allValues[slopeIndex] += valuesAtMidpoint[slopeIndex] * dist;
					if (elevationIndex != -1)
						allValues[elevationIndex] += valuesAtMidpoint[elevationIndex] * dist;
				}

				// Normalize by the total (perimeter).
				if (slopeIndex != -1)
					allValues[slopeIndex] /= totalLength;
				if (elevationIndex != -1)
					allValues[elevationIndex] /= totalLength;
			}
		}

		// Use whichever standard coloring values are present to populate the output
		// array.
		if (slopeIndex != -1)
			standardValues[0] = allValues[slopeIndex];
		if (elevationIndex != -1)
			standardValues[1] = allValues[elevationIndex];
		if (accelerationIndex != -1)
			standardValues[2] = allValues[accelerationIndex];
		if (potentialIndex != -1)
			standardValues[3] = allValues[potentialIndex];

		return standardValues;
	}

	private Double getEllipseAngleRelativeToGravityVector(EllipsePolygon pol)
	{
		double[] gravityVector = smallBodyModel.getGravityVector(pol.center);
		if (gravityVector == null)
			return null;
		MathUtil.vhat(gravityVector, gravityVector);

		// First compute cross product of normal and z axis
		double[] normal = smallBodyModel.getNormalAtPoint(pol.center);
		double[] zaxis = { 0.0, 0.0, 1.0 };
		double[] cross = new double[3];
		MathUtil.vcrss(zaxis, normal, cross);
		// Compute angle between normal and zaxis
		double sepAngle = -MathUtil.vsep(normal, zaxis) * 180.0 / Math.PI;

		// Rotate gravity vector and center of ellipse by amount
		// such that normal of ellipse faces positive z-axis
		vtkTransform transform = new vtkTransform();
		transform.RotateWXYZ(sepAngle, cross);

		gravityVector = transform.TransformDoubleVector(gravityVector);
		double[] center = transform.TransformDoublePoint(pol.center);

		// project gravity into xy plane
		double[] gravityPoint = { center[0] + gravityVector[0], center[1] + gravityVector[1], center[2] + gravityVector[2], };
		double[] projGravityPoint = new double[3];
		MathUtil.vprjp(gravityPoint, zaxis, center, projGravityPoint);
		double[] projGravityVector = new double[3];
		MathUtil.vsub(projGravityPoint, center, projGravityVector);
		MathUtil.vhat(projGravityVector, projGravityVector);

		// Compute direction of semimajor axis (both directions) in xy plane
		transform.Delete();
		transform = new vtkTransform();
		transform.RotateZ(pol.angle);

		// Positive x direction
		double[] xaxis = { 1.0, 0.0, 0.0 };
		double[] semimajoraxis1 = transform.TransformDoubleVector(xaxis);

		// Negative x direction
		double[] mxaxis = { -1.0, 0.0, 0.0 };
		double[] semimajoraxis2 = transform.TransformDoubleVector(mxaxis);

		// Compute angular separation of projected gravity vector
		// with respect to x-axis using atan2
		double gravAngle = Math.atan2(projGravityVector[1], projGravityVector[0]) * 180.0 / Math.PI;
		if (gravAngle < 0.0)
			gravAngle += 360.0;

		// Compute angular separations of semimajor axes vectors (both directions)
		// with respect to x-axis using atan2
		double smaxisangle1 = Math.atan2(semimajoraxis1[1], semimajoraxis1[0]) * 180.0 / Math.PI;
		if (smaxisangle1 < 0.0)
			smaxisangle1 += 360.0;

		double smaxisangle2 = Math.atan2(semimajoraxis2[1], semimajoraxis2[0]) * 180.0 / Math.PI;
		if (smaxisangle2 < 0.0)
			smaxisangle2 += 360.0;

		// Compute angular separations between semimajor axes and gravity vector.
		// The smaller one is the one we want, which should be between 0 and 180
		// degrees.
		double sepAngle1 = smaxisangle1 - gravAngle;
		if (sepAngle1 < 0.0)
			sepAngle1 += 360.0;

		double sepAngle2 = smaxisangle2 - gravAngle;
		if (sepAngle2 < 0.0)
			sepAngle2 += 360.0;

		transform.Delete();

		return Math.min(sepAngle1, sepAngle2);
	}

	@Override
	public double getDefaultOffset()
	{
		return 5.0 * smallBodyModel.getMinShiftAmount();
	}

	@Override
	public void setOffset(double offset)
	{
		this.offset = offset;

		updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public double getOffset()
	{
		return offset;
	}

	@Override
	public double getLineWidth()
	{
		vtkProperty boundaryProperty = boundaryActor.GetProperty();
		return boundaryProperty.GetLineWidth();
	}

	@Override
	public void setLineWidth(double width)
	{
		if (width >= 1.0)
		{
			vtkProperty boundaryProperty = boundaryActor.GetProperty();
			boundaryProperty.SetLineWidth(width);
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
	}

	@Override
	public void setVisible(boolean b)
	{
		boolean needToUpdate = false;
		for (EllipsePolygon pol : polygons)
		{
			if (pol.hidden == b)
			{
				pol.hidden = !b;
				if (pol.caption != null && pol.hidden == true)
					pol.caption.VisibilityOff();
				else if (pol.caption != null && pol.hidden == false)
				{
					if (pol.labelHidden)
						pol.caption.VisibilityOff();
					else
						pol.caption.VisibilityOn();
				}
				pol.updatePolygon(smallBodyModel, pol.center, pol.radius, pol.flattening, pol.angle);
				needToUpdate = true;
			}
		}
		if (needToUpdate)
		{
			updatePolyData();
			this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
		}
	}

	@Override
	public boolean isLabelVisible(int aIdx)
	{
		return !polygons.get(aIdx).labelHidden;
	}

	@Override
	public void setLabelVisible(int[] aIdxArr, boolean aIsVisible)
	{
		for (int aIdx : aIdxArr)
		{
			EllipsePolygon tmpStruct = polygons.get(aIdx);
			tmpStruct.labelHidden = !aIsVisible;
			if (tmpStruct.caption != null)
				tmpStruct.caption.SetVisibility(aIsVisible ? 1 : 0);
		}

		updatePolyData();
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public Color getStructureColor(int aIdx)
	{
		int[] rgbArr = polygons.get(aIdx).getColor();
		return new Color(rgbArr[0], rgbArr[1], rgbArr[2]);
	}

	@Override
	public void setStructureColor(int[] aIdxArr, Color aColor)
	{
		int[] rgbArr = { aColor.getRed(), aColor.getGreen(), aColor.getBlue() };
		for (int aIdx : aIdxArr)
			polygons.get(aIdx).setColor(rgbArr);

		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public boolean isStructureVisible(int aIdx)
	{
		return !polygons.get(aIdx).hidden;
	}

	@Override
	public void setStructureVisible(int[] aIdxArr, boolean aIsVisible)
	{
		for (int aIdx : aIdxArr)
		{
			EllipsePolygon tmpStruct = polygons.get(aIdx);
			tmpStruct.hidden = !aIsVisible;

			tmpStruct.updatePolygon(smallBodyModel, tmpStruct.center, tmpStruct.radius, tmpStruct.flattening, tmpStruct.angle);
		}

		updatePolyData();
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	public void savePlateDataInsideStructure(int idx, File file) throws IOException
	{
		vtkPolyData polydata = polygons.get(idx).interiorPolyData;
		smallBodyModel.savePlateDataInsidePolydata(polydata, file);
	}

	@Override
	public FacetColoringData[] getPlateDataInsideStructure(int idx)
	{
		vtkPolyData polydata = polygons.get(idx).interiorPolyData;
		return smallBodyModel.getPlateDataInsidePolydata(polydata);
	}

	@Override
	public double[] getStructureCenter(int id)
	{
		return polygons.get(id).center;
	}

	@Override
	public double[] getStructureNormal(int id)
	{
		double[] center = getStructureCenter(id);
		return smallBodyModel.getNormalAtPoint(center);
	}

	@Override
	public double getStructureSize(int id)
	{
		return 2.0 * polygons.get(id).radius;
	}

	@Override
	public void setStructureLabel(int aIdx, String aLabel)
	{
		EllipsePolygon tmpStruct = polygons.get(aIdx);
		tmpStruct.setLabel(aLabel);

		// Clear the caption if the string is empty or null
		if (aLabel == null || aLabel.equals(""))
		{
			if (tmpStruct.caption == null)
				return;

			tmpStruct.caption.VisibilityOff();
			tmpStruct.caption = null;

			updatePolyData();
			pcs.firePropertyChange(Properties.MODEL_CHANGED, null, aIdx);
			return;
		}

		// Create a caption if necessary
		if (tmpStruct.caption == null)
		{
			vtkCaptionActor2D tmpCaption = formCaption(smallBodyModel, tmpStruct.center, tmpStruct.name, aLabel);
			tmpCaption.GetCaptionTextProperty().SetJustificationToLeft();
			tmpStruct.caption = tmpCaption;
		}

		// Update the caption and send out notification
		tmpStruct.caption.SetCaption(aLabel);
		updatePolyData();
		pcs.firePropertyChange(Properties.MODEL_CHANGED, null, aIdx);
	}

	@Override
	public void showBorders()
	{
		for (int index : selectedStructures)
		{
			vtkCaptionActor2D v = polygons.get(index).caption;
			v.SetBorder(1 - v.GetBorder());
		}
		this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

	@Override
	protected vtkCaptionActor2D getCaption(int aIndex)
	{
		return polygons.get(aIndex).caption;
	}

	private static final Key<List<EllipsePolygon>> ELLIPSE_POLYGON_KEY = Key.of("ellipses");
	private static final Key<Double> DEFAULT_RADIUS_KEY = Key.of("defaultRadius");
	private static final Key<int[]> DEFAULT_COLOR_KEY = Key.of("defaultColor");
	private static final Key<Double> INTERIOR_OPACITY_KEY = Key.of("interiorOpacity");
	private static final Key<int[]> SELECTED_STRUCTURES_KEY = Key.of("selectedStructures");
	private static final Key<Double> OFFSET_KEY = Key.of("offset");

	@Override
	public Metadata store()
	{
		SettableMetadata result = SettableMetadata.of(Version.of(1, 0));

		result.put(ELLIPSE_POLYGON_KEY, polygons);
		result.put(DEFAULT_RADIUS_KEY, defaultRadius);
		result.put(DEFAULT_COLOR_KEY, defaultColor);
		result.put(INTERIOR_OPACITY_KEY, interiorOpacity);
		result.put(SELECTED_STRUCTURES_KEY, selectedStructures);
		result.put(OFFSET_KEY, offset);

		return result;
	}

	@Override
	public void retrieve(Metadata source)
	{
		// The order of these operations is significant to try to keep the object state consistent.
		// First get everything from the metadata into local variables. Don't touch the model yet
		// in case there's a problem.
		double defaultRadius = source.get(DEFAULT_RADIUS_KEY);
		int[] defaultColor = source.get(DEFAULT_COLOR_KEY);
		double interiorOpacity = source.get(INTERIOR_OPACITY_KEY);
		int[] selectedStructures = source.get(SELECTED_STRUCTURES_KEY);
		double offset = source.get(OFFSET_KEY);
		List<EllipsePolygon> restoredPolygons = source.get(ELLIPSE_POLYGON_KEY);

		// Now set the state of the individual restored polygons, again without directly changing the model.
		// TODO Note that the smallBodyModel is changed here though -- need to make sure this doesn't cause
		// problems if something throws before the whole retrieve operation is done.
		for (EllipsePolygon polygon : restoredPolygons)
		{
			polygon.updatePolygon(smallBodyModel, polygon.center, polygon.radius, polygon.flattening, polygon.angle);
		}

		// Now we're committed. Get rid of whatever's currently in this model and then add the restored polygons.
		polygons.clear();

		// Finally, change the rest of the fields.
		this.defaultRadius = defaultRadius;
		this.defaultColor = defaultColor;
		this.interiorOpacity = interiorOpacity;
		this.selectedStructures = selectedStructures;
		this.offset = offset;

		// Put the restored polygons in the list.
		polygons.addAll(restoredPolygons);

		// Sync everything up.
		updatePolyData();

		AbstractEllipsePolygonModel.this.pcs.firePropertyChange(Properties.MODEL_CHANGED, null, null);
	}

}

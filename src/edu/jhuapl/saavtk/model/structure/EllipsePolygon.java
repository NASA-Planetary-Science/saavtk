package edu.jhuapl.saavtk.model.structure;

import java.text.DecimalFormat;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.StructureModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import vtk.vtkCaptionActor2D;
import vtk.vtkPolyData;
import vtk.vtkQuadricClustering;

public class EllipsePolygon extends StructureModel.Structure
{
	private String name = "default";
	public final int id;
	private String label = "";
	private vtkCaptionActor2D caption;

	private double[] center;
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
		this.label = label != null ? label : "";
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
		this.name = name != null ? name : "";
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

	public double[] getCenter()
	{
		return center;
	}

	public void setCenter(double[] center)
	{
		this.center = center;
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
		this.setCenter(center);
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
		this.label = label != null ? label : "";
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

	@Override
	public double[] getCentroid(PolyhedralModel smallBodyModel)
	{
		return smallBodyModel.findClosestPoint(getCenter());
	}

	@Override
	public vtkCaptionActor2D getCaption()
	{
		return caption;
	}

	@Override
	public void setCaption(vtkCaptionActor2D caption)
	{
		this.caption = caption;
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

			result.setName(source.get(NAME_KEY));
			result.setCenter(source.get(CENTER_KEY));
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
			result.put(LABEL_KEY, polygon.getLabel());

			result.put(NAME_KEY, polygon.getName());
			result.put(CENTER_KEY, polygon.getCenter());
			result.put(RADIUS_KEY, polygon.radius);
			result.put(FLATTENING_KEY, polygon.flattening);
			result.put(ANGLE_KEY, polygon.angle);
			result.put(HIDDEN_KEY, polygon.hidden);
			result.put(LABEL_HIDDEN_KEY, polygon.labelHidden);

			return result;

		});
	}

}
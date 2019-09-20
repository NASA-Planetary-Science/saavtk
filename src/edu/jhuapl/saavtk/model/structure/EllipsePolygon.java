package edu.jhuapl.saavtk.model.structure;

import java.awt.Color;
import java.text.DecimalFormat;

import crucible.crust.metadata.api.Key;
import crucible.crust.metadata.api.Version;
import crucible.crust.metadata.impl.InstanceGetter;
import crucible.crust.metadata.impl.SettableMetadata;
import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.AbstractEllipsePolygonModel.Mode;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.structure.io.StructureLoadUtil;
import edu.jhuapl.saavtk.util.PolyDataUtil;
import vtk.vtkCaptionActor2D;
import vtk.vtkPolyData;
import vtk.vtkQuadricClustering;

public class EllipsePolygon implements Structure
{
	// Constants
	private static final DecimalFormat DF = new DecimalFormat("#.#####");

	// Attributes
	private final Mode mode;
	private final int id;
	private final String type;

	// State vars
	private String name = "default";
	private String label = "";
	private double[] center;
	private double radius; // or semimajor axis
	private double flattening; // ratio of semiminor axis to semimajor axis
	private double angle;
	private boolean visible;
	private boolean labelVisible;
	private int numberOfSides;
	private Color color;
	private Color labelColor;
	private int labelFontSize;

	// VTK vars
	protected final vtkPolyData vExteriorDecPD;
	protected final vtkPolyData vExteriorRegPD;
	protected final vtkPolyData vInteriorDecPD;
	protected final vtkPolyData vInteriorRegPD;
	private vtkCaptionActor2D vCaption;
	protected boolean vIsStale;

	public EllipsePolygon(int aNumberOfSides, String aType, Color aColor, Mode aMode, int aId, String aLabel)
	{
		mode = aMode;
		id = aId;
		type = aType;

		numberOfSides = aNumberOfSides;
		color = aColor;
		label = aLabel != null ? aLabel : "";
		labelColor = Color.BLACK;
		labelFontSize = 16;
		visible = true;
		labelVisible = true;

		vExteriorDecPD = new vtkPolyData();
		vExteriorRegPD = new vtkPolyData();
		vInteriorDecPD = new vtkPolyData();
		vInteriorRegPD = new vtkPolyData();
		vCaption = null;
		vIsStale = true;
	}

	public double getAngle()
	{
		return angle;
	}

	public double getFlattening()
	{
		return flattening;
	}

	public int getNumberOfSides()
	{
		return numberOfSides;
	}

	public double getRadius()
	{
		return radius;
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
	public void setName(String aName)
	{
		name = aName != null ? aName : "";
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
	public Color getColor()
	{
		return color;
	}

	@Override
	public void setColor(Color aColor)
	{
		color = aColor;
	}

	public double[] getCenter()
	{
		return center;
	}

	public void setAngle(double aAngle)
	{
		angle = aAngle;
		vIsStale = true;
	}

	public void setCenter(double[] aCenter)
	{
		center = aCenter;
		vIsStale = true;
	}

	public void setFlattening(double aFlattening)
	{
		flattening = aFlattening;
		vIsStale = true;
	}

	public void setRadius(double aRadius)
	{
		radius = aRadius;
		vIsStale = true;
	}

	/**
	 * Returns the exterior vtkPolyData.
	 * <P>
	 * Note the returned VtkPolyData is the regular one - not the decimated one.
	 */
	public vtkPolyData getVtkExteriorPolyData()
	{
		return vExteriorRegPD;
	}

	/**
	 * Returns the interior vtkPolyData.
	 * <P>
	 * Note the returned VtkPolyData is the regular one - not the decimated one.
	 */
	public vtkPolyData getVtkInteriorPolyData()
	{
		return vInteriorRegPD;
	}

	/**
	 * Method that will update the internal VTK state of this EllipsePolygon.
	 * 
	 * @param aPolyhedralModel
	 */
	public void updateVtkState(PolyhedralModel aPolyhedralModel)
	{
		// Bail if the VTK state is not stale
		if (vIsStale == false)
			return;
		vIsStale = false;

		aPolyhedralModel.drawEllipticalPolygon(center, radius, flattening, angle, numberOfSides, vInteriorRegPD,
				vExteriorRegPD);

		// LatLon ll=MathUtil.reclat(center);
		// System.out.println(Math.toDegrees(ll.lat)+" "+Math.toDegrees(ll.lon));

		// Setup decimator
		vtkQuadricClustering decimator = new vtkQuadricClustering();

		// Decimate interior
		decimator.SetInputData(vInteriorRegPD);
		decimator.AutoAdjustNumberOfDivisionsOn();
		decimator.CopyCellDataOn();
		decimator.Update();
		vInteriorDecPD.DeepCopy(decimator.GetOutput());

		// Decimate boundary
		decimator.SetInputData(vExteriorRegPD);
		decimator.SetNumberOfXDivisions(2);
		decimator.SetNumberOfYDivisions(2);
		decimator.SetNumberOfZDivisions(2);
		decimator.CopyCellDataOn();
		decimator.Update();
		vExteriorDecPD.DeepCopy(decimator.GetOutput());

		// Destroy decimator
		decimator.Delete();
	}

	@Override
	public String getClickStatusBarText()
	{
		return type + ", Id = " + id + ", Diameter = " + 2.0 * radius + " km";
	}

	@Override
	public void setLabel(String aLabel)
	{
		label = aLabel != null ? aLabel : "";
	}

	@Override
	public String getLabel()
	{
		return label;
	}

	@Override
	public Color getLabelColor()
	{
		return labelColor;
	}

	@Override
	public void setLabelColor(Color aColor)
	{
		labelColor = aColor;
	}

	@Override
	public int getLabelFontSize()
	{
		return labelFontSize;
	}

	@Override
	public void setLabelFontSize(int aFontSize)
	{
		labelFontSize = aFontSize;
	}

	@Override
	public boolean getVisible()
	{
		return visible;
	}

	@Override
	public boolean getLabelVisible()
	{
		return labelVisible;
	}

	@Override
	public void setVisible(boolean aBool)
	{
		visible = aBool;
	}

	@Override
	public void setLabelVisible(boolean aBool)
	{
		labelVisible = aBool;
	}

	@Override
	public double[] getCentroid(PolyhedralModel smallBodyModel)
	{
		return smallBodyModel.findClosestPoint(getCenter());
	}

	@Override
	public vtkCaptionActor2D getCaption()
	{
		return vCaption;
	}

	@Override
	public void setCaption(vtkCaptionActor2D aCaption)
	{
		vCaption = aCaption;
	}

	/**
	 * Helper method to release the VTK state vars
	 * <P>
	 * TODO: Currently not known if this properly releases the VTK resources.
	 * <P>
	 * TODO: This should be be simplified
	 */
	protected void clearVtkState()
	{
		visible = false;

		PolyDataUtil.clearPolyData(vInteriorRegPD);
		PolyDataUtil.clearPolyData(vInteriorDecPD);
		PolyDataUtil.clearPolyData(vExteriorRegPD);
		PolyDataUtil.clearPolyData(vExteriorDecPD);

		if (getCaption() != null)
		{
			getCaption().VisibilityOff();
			setCaption(null);
		}
	}

	private static final Key<EllipsePolygon> ELLIPSE_POLYGON_KEY = Key.of("ellipsePolygon");
	private static final Key<Integer> NUMBER_SIDES_KEY = Key.of("numberSides");
	private static final Key<String> TYPE_KEY = Key.of("type");
	private static final Key<int[]> COLOR_KEY = Key.of("color");
	private static final Key<String> MODE_KEY = Key.of("mode");
	private static final Key<Integer> ID_KEY = Key.of("id");
	private static final Key<String> LABEL_KEY = Key.of("label");
	private static final Key<int[]> LABEL_COLOR_KEY = Key.of("labelColor");
	private static final Key<Integer> LABEL_FONT_SIZE_KEY = Key.of("labelFontSize");
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
			Color color = StructureLoadUtil.convertRgbaToColor(source.get(COLOR_KEY));
			Mode mode = Mode.valueOf(source.get(MODE_KEY));
			int id = source.get(ID_KEY);
			String label = source.get(LABEL_KEY);

			EllipsePolygon result = new EllipsePolygon(numberSides, type, color, mode, id, label);

			result.setName(source.get(NAME_KEY));
			result.setCenter(source.get(CENTER_KEY));
			result.radius = source.get(RADIUS_KEY);
			result.flattening = source.get(FLATTENING_KEY);
			result.angle = source.get(ANGLE_KEY);
			result.visible = !source.get(HIDDEN_KEY);
			result.labelVisible = !source.get(LABEL_HIDDEN_KEY);
			result.labelColor = StructureLoadUtil.convertRgbaToColor(source.get(LABEL_COLOR_KEY));
			result.labelFontSize = source.get(LABEL_FONT_SIZE_KEY);

			return result;
		}, EllipsePolygon.class, polygon -> {
			SettableMetadata result = SettableMetadata.of(Version.of(1, 0));

			result.put(NUMBER_SIDES_KEY, polygon.numberOfSides);
			result.put(TYPE_KEY, polygon.type);
			result.put(COLOR_KEY, StructureLoadUtil.convertColorToRgba(polygon.color));
			result.put(MODE_KEY, polygon.mode.name());
			result.put(ID_KEY, polygon.id);
			result.put(LABEL_KEY, polygon.getLabel());

			result.put(NAME_KEY, polygon.getName());
			result.put(CENTER_KEY, polygon.getCenter());
			result.put(RADIUS_KEY, polygon.radius);
			result.put(FLATTENING_KEY, polygon.flattening);
			result.put(ANGLE_KEY, polygon.angle);
			result.put(HIDDEN_KEY, !polygon.visible);
			result.put(LABEL_HIDDEN_KEY, !polygon.labelVisible);
			result.put(LABEL_COLOR_KEY, StructureLoadUtil.convertColorToRgba(polygon.getLabelColor()));
			result.put(LABEL_FONT_SIZE_KEY, polygon.getLabelFontSize());

			return result;

		});
	}

}

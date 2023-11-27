package edu.jhuapl.saavtk.structure.io;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.jhuapl.saavtk.model.PolyhedralModel;
import edu.jhuapl.saavtk.model.structure.LineModel;
import edu.jhuapl.saavtk.structure.PolyLine;
import edu.jhuapl.saavtk.structure.Polygon;
import edu.jhuapl.saavtk.structure.Structure;
import edu.jhuapl.saavtk.util.LatLon;
import edu.jhuapl.saavtk.vtk.font.FontAttr;
import glum.io.ParseUtil;

/**
 * Collection of utility methods to support (de)serialization of SBMT structures
 * from/to XML files.
 * <P>
 * Note some methods in this class have been refactored out so as to keep
 * serialization code separate from model based classes.
 *
 * @author lopeznr1
 */
public class XmlLoadUtil
{
	// Constants
	private static final String XML_ATTR_SHAPE_MODEL_NAME = "shapemodel";

	private static final String XML_ATTR_ID = "id";
	private static final String XML_ATTR_NAME = "name";
	private static final String XML_ATTR_VERTICES = "vertices";
	private static final String XML_ATTR_COLOR = "color";
	private static final String XML_ATTR_LABEL = "label";
	private static final String XML_ATTR_AREA = "area";
	private static final String XML_ATTR_LENGTH = "length";

	public static final String RAW_TYPE_PATH = "path";
	public static final String RAW_TYPE_POLYGON = "polygon";

	/**
	 * Utility method that will load a list of lines / polygons from the specified
	 * {@link Element}.
	 * <P>
	 * Returns the list of loaded lines / polygons.
	 */
	public static List<Structure> loadPolyLinesFromElement(Object aSource, Element aElement)
	{
		List<Structure> retL = new ArrayList<>();

		// Retrieve all of the child elements
		NodeList tmpNL = aElement.getElementsByTagName("*");

		// Transform elements into the appropriate structures
		for (int i = 0; i < tmpNL.getLength(); i++)
		{
			Element tmpE = (Element) tmpNL.item(i);

			// Retrieve the structures id
			// Invalid ids will be assigned value: -1
			String tmpStr = tmpE.getAttribute(XML_ATTR_ID);
			int id = ParseUtil.readInt(tmpStr, -1);

			// Retrieve the control points
			List<LatLon> controlPointL = readControlPointsFrom(tmpE);

			// Synthesize the structure or skip to next if not a supported type
			PolyLine tmpItem = null;

			String tagName = tmpE.getTagName();
			if (tagName.equals(RAW_TYPE_PATH) == true)
				tmpItem = new PolyLine(id, aSource, controlPointL);
			else if (tagName.equals(RAW_TYPE_POLYGON) == true)
				tmpItem = new Polygon(id, aSource, controlPointL);
			else
				continue;

			String name = tmpE.getAttribute(XML_ATTR_NAME);
			String label = tmpE.getAttribute(XML_ATTR_LABEL);
			tmpItem.setName(name);
			tmpItem.setLabel(label);
			FontAttr tmpFA = tmpItem.getLabelFontAttr();
			tmpFA = new FontAttr(tmpFA.getFace(), tmpFA.getColor(), tmpFA.getSize(), false);
			tmpItem.setLabelFontAttr(tmpFA);

			tmpStr = tmpE.getAttribute(XML_ATTR_COLOR);
			if (tmpStr.length() != 0)
			{
				String[] tokenArr = tmpStr.split(",");
				int rVal = Integer.parseInt(tokenArr[0]);
				int gVal = Integer.parseInt(tokenArr[1]);
				int bVal = Integer.parseInt(tokenArr[2]);
				tmpItem.setColor(new Color(rVal, gVal, bVal));
			}

			if (tmpItem instanceof Polygon && tmpE.hasAttribute(XML_ATTR_AREA) == true)
			{
				tmpStr = tmpE.getAttribute(XML_ATTR_AREA);
				double surfaceArea = Double.parseDouble(tmpE.getAttribute(XML_ATTR_AREA));
				((Polygon) tmpItem).setSurfaceArea(surfaceArea);
			}

			retL.add(tmpItem);
		}

		return retL;
	}

	/**
	 * Retrieves the "shape model name" from the specified document.
	 * <P>
	 * Returns null if there is no "shape model name".
	 */
	public static String getShapeModelNameFrom(Element aElement)
	{
		if (aElement.hasAttribute(XML_ATTR_SHAPE_MODEL_NAME) == true)
			return aElement.getAttribute(XML_ATTR_SHAPE_MODEL_NAME);

		return null;
	}

	/**
	 * Takes the provided XML file and returns the top level element.
	 * <P>
	 * On failure an {@link IOException} will be thrown.
	 */
	public static Element loadRoot(File aFile) throws IOException
	{
		try
		{
			// get the factory
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setErrorHandler(null);

			// parse using builder to get DOM representation of the XML file
			Document dom = db.parse(aFile);

			// get the root element
			Element rootElement = dom.getDocumentElement();
			return rootElement;
		}
		catch (Exception aExp)
		{
			throw new IOException(aExp);
		}
	}

	/**
	 * Utility method that will save the content of the specified {@link LineModel}
	 * to the provided file.
	 * <P>
	 * The output format of the file will be XML.
	 */
	public static <G1 extends PolyLine> void saveManager(File aFile, LineModel<G1> aManager, Collection<G1> aItemC, PolyhedralModel aSmallBody)
			throws Exception
	{
		// get an instance of factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		// get an instance of builder
		DocumentBuilder db = dbf.newDocumentBuilder();

		// create an instance of DOM
		Document dom = db.newDocument();

		Element rootElement = dom.createElement(aManager.getType());
		if (aSmallBody.getModelName() != null)
			rootElement.setAttribute(XML_ATTR_SHAPE_MODEL_NAME, aSmallBody.getModelName());

		savePolyLinesToElement(rootElement, aItemC);
		dom.appendChild(rootElement);

		try
		{
			OutputStream aStream = new FileOutputStream(aFile);
			Result result = new StreamResult(new OutputStreamWriter(aStream, "utf-8"));

			TransformerFactory tf = TransformerFactory.newInstance();
			tf.setAttribute("indent-number", 4);

			Transformer xformer = tf.newTransformer();
			xformer.setOutputProperty(OutputKeys.INDENT, "yes");

			Source source = new DOMSource(dom);
			xformer.transform(source, result);
		}
		catch (TransformerException aExp)
		{
			aExp.printStackTrace();
		}
	}

	/**
	 * Utility helper method that will save a list of lines / polygons to the
	 * specified {@link Element}.
	 */
	public static <G1 extends PolyLine> void savePolyLinesToElement(Element aElement, Collection<G1> aItemC)
	{
		Document tmpDoc = aElement.getOwnerDocument();
		for (G1 aItem : aItemC)
			aElement.appendChild(formElementForPolyLine(tmpDoc, aItem));
	}

	/**
	 * Utility helper method that will return an {@link Element} with the content of
	 * the specified line / polygon.
	 * <P>
	 * The element is expected to have an attribute with the name of: vertices
	 */
	private static Element formElementForPolyLine(Document aDoc, PolyLine aItem)
	{
		String typeStr = getTypeFor(aItem);
		Element retElement = aDoc.createElement(typeStr);
		retElement.setAttribute(XML_ATTR_ID, String.valueOf(aItem.getId()));
		retElement.setAttribute(XML_ATTR_NAME, aItem.getName());
		retElement.setAttribute(XML_ATTR_LABEL, aItem.getLabel());
//		String labelcolorStr=labelcolor[0] + "," + labelcolor[1] + "," + labelcolor[2];
//		linEle.setAttribute(LABELCOLOR, labelcolorStr);
		retElement.setAttribute(XML_ATTR_LENGTH, String.valueOf(aItem.getPathLength()));

		Color color = aItem.getColor();
		String colorStr = color.getRed() + "," + color.getGreen() + "," + color.getBlue();
		retElement.setAttribute(XML_ATTR_COLOR, colorStr);

		String vertices = "";
		int size = aItem.getControlPoints().size();

		for (int i = 0; i < size; ++i)
		{
			LatLon ll = aItem.getControlPoints().get(i);
			double latitude = ll.lat * 180.0 / Math.PI;
			double longitude = ll.lon * 180.0 / Math.PI;
			if (longitude < 0.0)
				longitude += 360.0;

			vertices += latitude + " " + longitude + " " + ll.rad;

			if (i < size - 1)
				vertices += " ";
		}

		retElement.setAttribute(XML_ATTR_VERTICES, vertices);

		// Polygon specific code
		if (aItem instanceof Polygon)
			retElement.setAttribute(XML_ATTR_AREA, String.valueOf(((Polygon) aItem).getSurfaceArea()));

		return retElement;
	}

	/**
	 * Utility helper method that returns the string used to describe the specified
	 * structure.
	 * <P>
	 * Note that this string is used in serialized packets and thus should not
	 * change.
	 */
	private static String getTypeFor(Structure aItem)
	{
		Class<?> tmpClass = aItem.getClass();
		if (tmpClass == PolyLine.class)
			return RAW_TYPE_PATH;
		else if (tmpClass == Polygon.class)
			return RAW_TYPE_POLYGON;

		throw new RuntimeException("Unrecognized type: " + tmpClass);
	}

	/**
	 * Utility helper method that will read the list of vertices from the specified
	 * element.
	 * <P>
	 * The element is expected to have an attribute with the name of: vertices
	 */
	private static List<LatLon> readControlPointsFrom(Element aElement)
	{
		List<LatLon> retL = new ArrayList<>();

		String tmp = aElement.getAttribute(XML_ATTR_VERTICES);
		if (tmp.length() == 0)
			return retL;

		String[] tokenArr = tmp.split(" ");
		for (int aIdx = 0; aIdx < tokenArr.length;)
		{
			double lat = Double.parseDouble(tokenArr[aIdx++]) * Math.PI / 180.0;
			double lon = Double.parseDouble(tokenArr[aIdx++]) * Math.PI / 180.0;
			double rad = Double.parseDouble(tokenArr[aIdx++]);
			retL.add(new LatLon(lat, lon, rad));
		}

		return retL;
	}

}

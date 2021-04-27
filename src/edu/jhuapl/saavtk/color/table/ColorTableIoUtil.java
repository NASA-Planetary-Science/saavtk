package edu.jhuapl.saavtk.color.table;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

/**
 * Collection of utility methods associated with accessing serialized
 * {@link ColorTable}s.
 *
 * @author lopeznr1
 */
public class ColorTableIoUtil
{
	/**
	 * Utility method to return a list of {@link ColorTable}s from the specified
	 * {@link InputStream}.
	 * <P>
	 * The input stream should correspond to an XML document.
	 * <P>
	 * This method was originally sourced from: edu.jhuapl.saavtk.colormap.Colormaps
	 */
	public static List<ColorTable> loadFromXml(InputStream aStream) throws IOException
	{
		List<ColorTable> retItemL = new ArrayList<>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document doc;
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(aStream);
		}
		catch (ParserConfigurationException | SAXException aExp)
		{
			throw new IOException("XML Errors", aExp);
		}

		Element elem = doc.getDocumentElement();
		NodeList nodes = elem.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++)
		{
			String nodeName = nodes.item(i).getNodeName();
			NamedNodeMap attributes = nodes.item(i).getAttributes();
			if (nodeName.equals("ColorMap") && attributes != null)
			{
				String name = attributes.getNamedItem("name").getNodeValue();
				NodeList points = nodes.item(i).getChildNodes();
				Color nanColor = Color.white;
				List<Double> interpLevels = Lists.newArrayList();
				List<Color> colors = Lists.newArrayList();
				for (int p = 0; p < points.getLength(); p++)
				{
					Node point = points.item(p);
					if (point.getNodeName().equals("#text"))
						continue;
					if (point.getNodeName().equals("NaN"))
					{
						double r = Double.valueOf(point.getAttributes().getNamedItem("r").getNodeValue());
						double g = Double.valueOf(point.getAttributes().getNamedItem("g").getNodeValue());
						double b = Double.valueOf(point.getAttributes().getNamedItem("b").getNodeValue());
						nanColor = new Color((float) r, (float) g, (float) b);
						continue;
					}
					double x = Double.valueOf(point.getAttributes().getNamedItem("x").getNodeValue());
//					double o = Double.valueOf(point.getAttributes().getNamedItem("o").getNodeValue());
					double r = Double.valueOf(point.getAttributes().getNamedItem("r").getNodeValue());
					double g = Double.valueOf(point.getAttributes().getNamedItem("g").getNodeValue());
					double b = Double.valueOf(point.getAttributes().getNamedItem("b").getNodeValue());
					interpLevels.add(x);
					colors.add(new Color((float) r, (float) g, (float) b));
				}

				String colorSpaceName = attributes.getNamedItem("space").getNodeValue();
				ColorSpace colorSpace = ColorSpace.valueOf(colorSpaceName.toUpperCase());

				ColorTable tmpItem = new ColorTable(name, interpLevels, colors, nanColor, colorSpace);
				retItemL.add(tmpItem);
			}
		}

		return retItemL;
	}

}

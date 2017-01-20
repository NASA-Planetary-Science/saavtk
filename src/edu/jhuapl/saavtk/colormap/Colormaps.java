package edu.jhuapl.saavtk.colormap;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.google.common.collect.Maps;

import edu.jhuapl.saavtk.colormap.RgbColormap.ColorSpace;
import edu.jhuapl.saavtk.util.FileCache;
import vtk.vtkObject;
import vtk.vtkOutputWindow;

public class Colormaps
{
	private static Map<String, RgbColormap> builtInColormaps=null;

	public static String getDefaultColormapName()
	{
	    return "rainbow";
	}

	public static Set<String> getAllBuiltInColormapNames()
	{
		if (builtInColormaps==null)
			initBuiltInColorMaps();
		return builtInColormaps.keySet();
	}

	public static Colormap getNewInstanceOfBuiltInColormap(String colormapName)
	{
		if (builtInColormaps==null)
			initBuiltInColorMaps();
		return RgbColormap.copy(builtInColormaps.get(colormapName));
	}

	private static void initBuiltInColorMaps()
	{
		builtInColormaps=Maps.newTreeMap();
		loadFromXml("ColorMaps.xml");
	}

	private static void loadFromXml(String resourceName)
	{
		try
		{
			DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
			DocumentBuilder builder=factory.newDocumentBuilder();
			InputStream stream=Colormaps.class.getResourceAsStream(resourceName);
			//InputStream stream=new FileInputStream(new File(resourceName));	// load as file instead of resource since sbmt build breaks local source tree resource loading
			Document doc=builder.parse(new BufferedInputStream(stream));
			Element elem=doc.getDocumentElement();
			NodeList nodes=elem.getChildNodes();
			for (int i=0; i<nodes.getLength(); i++)
			{
				String nodeName=nodes.item(i).getNodeName();
				NamedNodeMap attributes=nodes.item(i).getAttributes();
				if (nodeName.equals("ColorMap") && attributes!=null)
				{
					String name=attributes.getNamedItem("name").getNodeValue();
					NodeList points=nodes.item(i).getChildNodes();
					Color nanColor=Color.white;
					List<Double> interpLevels=Lists.newArrayList();
					List<Color> colors=Lists.newArrayList();
					int m=0;
					for (int p=0; p<points.getLength(); p++)
					{
						Node point=points.item(p);
						if (point.getNodeName().equals("#text"))
							continue;
						if (point.getNodeName().equals("NaN"))
						{
							double r=Double.valueOf(point.getAttributes().getNamedItem("r").getNodeValue());
							double g=Double.valueOf(point.getAttributes().getNamedItem("g").getNodeValue());
							double b=Double.valueOf(point.getAttributes().getNamedItem("b").getNodeValue());
							nanColor=new Color((float)r, (float)g, (float)b);
							continue;
						}
						double x=Double.valueOf(point.getAttributes().getNamedItem("x").getNodeValue());
						double o=Double.valueOf(point.getAttributes().getNamedItem("o").getNodeValue());
						double r=Double.valueOf(point.getAttributes().getNamedItem("r").getNodeValue());
						double g=Double.valueOf(point.getAttributes().getNamedItem("g").getNodeValue());
						double b=Double.valueOf(point.getAttributes().getNamedItem("b").getNodeValue());
						interpLevels.add(x);
						colors.add(new Color((float)r,(float)g,(float)b));
						m++;
					}
					String colorSpaceName=attributes.getNamedItem("space").getNodeValue();
					ColorSpace colorSpace=ColorSpace.valueOf(colorSpaceName.toUpperCase());
					RgbColormap colormap= new RgbColormap(interpLevels,colors,64,nanColor,colorSpace);
					colormap.setName(name);
					colormap.setNumberOfLevels(128);
					builtInColormaps.put(name,colormap);
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}

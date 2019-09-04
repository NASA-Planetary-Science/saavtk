package edu.jhuapl.saavtk.colormap;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.jhuapl.saavtk.colormap.RgbColormap.ColorSpace;
import edu.jhuapl.saavtk.util.Preferences;
import vtk.vtkNativeLibrary;

/**
 * This is an all static method class for managing a global/static map of (@link
 * Colormap} objects, keyed on their names as Strings. The overall philosophy of
 * this class is for public methods never to return null or throw exceptions for
 * any input, so that the caller may trust that the returned color map
 * objects/names may safely be used.
 * 
 * This class is not thread-safe.
 */
public class Colormaps
{
    // Failsafe hard-wired default color map name.
    private static final String RAINBOW = "rainbow";

    // Use lazy initialialization to keep open possibilities of influencing how
    // these fields are initialized/changed.
    private static Map<String, RgbColormap> builtInColormaps = null;
    private static String defaultColormapName = null;
    private static String currentColormapName = null;

    /**
     * Return the default color map name. This method *may* initialize the
     * global/static map of color map objects.
     * 
     * If the default color map name was not explicitly set by a prior call to
     * setDefaultColormapName, invoking this method causes the default color map
     * name to be set to 1) the user's preferred color map as defined using the
     * {@link Preferences} facility, or 2) the "rainbow" color map defined in the
     * resource file or 3) a built-in color map (also called "rainbow"), whichever
     * is found first. In no case will this method return a null string.
     * 
     * Note that the default colormap name is mutable, and settable by calling code.
     * There is therefore no guarantee that the string returned refers to a valid
     * {@link Colormap} object in the global/static map of color maps. This is by
     * design, as in practice the methods of this class that provide
     * {@link Colormap} objects based on the name will make a substitution for any
     * color map name that is not available.
     * 
     * @return the default colormap name.
     */
    public static String getDefaultColormapName()
    {
        if (defaultColormapName == null)
        {
            initialize();
        }

        return defaultColormapName;
    }

    /**
     * Set the default color map name. This method performs no validation whatsoever
     * on the supplied argument, so callers are permitted to pass null or a name
     * that does not refer to a {@link Colormap} object in the global/static map of
     * color maps.
     * 
     * If this method is called with a null argument, the default color map name
     * will in effect be reset to its original default value.
     * 
     * @param colormapName
     */
    public static void setDefaultColormapName(String colormapName)
    {
        defaultColormapName = colormapName;
    }

    /**
     * Return the current color map name. This method *may* initialize the
     * global/static map of color map objects.
     * 
     * If the current color map name was not explicitly set by a prior call to
     * setCurrentColormapName, the current color map name is set to the default
     * color map name.
     * 
     * Note that the current colormap name is mutable, and settable by calling code.
     * There is therefore no guarantee that the string returned refers to a valid
     * {@link Colormap} object in the global/static map of color maps. This is by
     * design, as in practice the methods that provide {@link Colormap} objects
     * based on the name will make a substitution for any color map name that is not
     * available.
     * 
     * @return the current colormap name.
     */
    public static String getCurrentColormapName()
    {
        if (currentColormapName == null)
        {
            initialize();
        }

        return currentColormapName;
    }

    /**
     * Set the current color map name. This method performs no validation whatsoever
     * on the supplied argument, so callers are permitted to pass null or a name
     * that does not refer to a {@link Colormap} object in the global/static map of
     * color maps.
     * 
     * If this method is called with a null argument, the current color map name
     * will in effect be reset to the original default value.
     * 
     * @param colormapName
     */
    public static void setCurrentColormapName(String colormapName)
    {
        currentColormapName = colormapName;
    }

    /**
     * Return the list of built-in/available color map names in lexically sorted
     * order. The global/static map of color maps will be initialized first, if it
     * has not already been initialized.
     * 
     * Each color map name in the returned list is guaranteed to be valid, that is,
     * guaranteed to refer to one of the {@link Colormap} objects in the
     * global/static map of color maps.
     * 
     * @return the list of color map names
     */
    public static ImmutableList<String> getAllBuiltInColormapNames()
    {
        initialize();

        return ImmutableList.copyOf(builtInColormaps.keySet());
    }

    /**
     * Return a copy of the built-in {@link Colormap} identified by the supplied
     * name. The global/static map of color maps will be initialized first, if it
     * has not already been initialized.
     * 
     * If the supplied name does not refer to a {@link Colormap} object in the
     * global/static map of color maps, the best available match to the requested
     * color map will be returned instead. The returned object is thus guaranteed to
     * be valid, but not necessarily the one that was requested.
     * 
     * @param colormapName the name of the map to return
     * 
     * @return a copy of the named color map, or the best available color map
     * 
     */
    public static Colormap getNewInstanceOfBuiltInColormap(String colormapName)
    {
        initialize();

        return RgbColormap.copy(findBestMatchingValidColormap(colormapName));
    }

    /**
     * Initialize the state of the global/static map of color maps if it has not
     * already been initialized. In addition, if the default color map name is set
     * to null, this method will initialize the default color map name using the
     * user's preferences along with the global/static map of color maps.
     */
    private static void initialize()
    {
        if (builtInColormaps == null)
        {
            builtInColormaps = Maps.newTreeMap();

            // This may silently fail to find any color maps.
            loadFromXml("ColorMaps.xml");

            // Ensure that at least one color map, "rainbow", will be available.
            if (builtInColormaps.isEmpty())
            {
                builtInColormaps.put(RAINBOW, createRainbowColormapByHand());
            }
        }

        String savedColormapName = null;
        if (defaultColormapName == null || currentColormapName == null)
        {
            try
            {
                savedColormapName = Preferences.getInstance().get(Preferences.DEFAULT_COLORMAP_NAME);
            }
            catch (Exception e)
            {

            }
        }

        if (defaultColormapName == null)
        {
            if (savedColormapName != null)
            {
                defaultColormapName = savedColormapName;
            }
            else
            {
                defaultColormapName = RAINBOW;
            }
        }

        if (currentColormapName == null)
        {
            if (savedColormapName != null)
            {
                currentColormapName = savedColormapName;
            }
            else
            {
                currentColormapName = defaultColormapName;
            }
        }
    }

    /**
     * Load the color maps from a resource file. Catches all exceptions so if there
     * is a problem, one could end up with an incomplete or empty map of color maps.
     * 
     * @param resourceName the resource identifier
     */
    private static void loadFromXml(String resourceName)
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream stream = Colormaps.class.getResourceAsStream(resourceName);
            // InputStream stream=new FileInputStream(new File(resourceName)); // load as
            // file instead of resource since sbmt build breaks local source tree resource
            // loading
            try (InputStream inputStream = new BufferedInputStream(stream))
            {
                Document doc = builder.parse(inputStream);
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
//                            double o = Double.valueOf(point.getAttributes().getNamedItem("o").getNodeValue());
                            double r = Double.valueOf(point.getAttributes().getNamedItem("r").getNodeValue());
                            double g = Double.valueOf(point.getAttributes().getNamedItem("g").getNodeValue());
                            double b = Double.valueOf(point.getAttributes().getNamedItem("b").getNodeValue());
                            interpLevels.add(x);
                            colors.add(new Color((float) r, (float) g, (float) b));
                        }
                        String colorSpaceName = attributes.getNamedItem("space").getNodeValue();
                        ColorSpace colorSpace = ColorSpace.valueOf(colorSpaceName.toUpperCase());
                        RgbColormap colormap = new RgbColormap(interpLevels, colors, 64, nanColor, colorSpace);
                        colormap.setName(name);
                        colormap.setNumberOfLevels(128);
                        builtInColormaps.put(name, colormap);
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Unable to load color maps from resource file");
            e.printStackTrace();
        }
    }

    /**
     * A failsafe method to stand up a simple, valid ("rainbow") color map by hand.
     * 
     * @return the color map
     */
    private static RgbColormap createRainbowColormapByHand()
    {
        List<Double> interpLevels = ImmutableList.of(-1.0, -0.5, 0.0, 0.5, 1.0);

        List<Color> colors = ImmutableList.of( //
                new Color(0.0f, 0.0f, 1.0f), //
                new Color(0.0f, 1.0f, 1.0f), //
                new Color(0.0f, 1.0f, 0.0f), //
                new Color(1.0f, 1.0f, 0.0f), //
                new Color(1.0f, 0.0f, 0.0f));

        Color nanColor = Color.white;

        ColorSpace colorSpace = ColorSpace.valueOf("RGB");

        RgbColormap colormap = new RgbColormap(interpLevels, colors, 64, nanColor, colorSpace);
        colormap.setName(RAINBOW);
        colormap.setNumberOfLevels(128);

        return colormap;
    }

    /**
     * Return the best available matching {@link Colormap} object from the
     * global/static map of color maps. If the supplied color map name is valid, a
     * copy of the corresponding {@link Colormap} object will be returned. If the
     * supplied argument is null, or otherwise does not refer to a color map object
     * in the global/static list of color maps, the current color map will be
     * returned. If no current color map is set, the default color map will be
     * returned.
     * 
     * @param colormapName the name of the desired colormap
     * @return the {@link Colormap} object identified by the colormapName argument,
     *         or the best available colormap object
     */
    private static RgbColormap findBestMatchingValidColormap(String colormapName)
    {
        return builtInColormaps.get(findBestMatchingValidColormapName(colormapName));
    }

    /**
     * This internal very low-level method assumes the global/static map has been
     * initialized and contains at least one color map. The returned result is
     * guaranteed to be in the global/static map.
     * 
     * @param colormapName the preferred name
     * @return a valid color map name
     */
    private static String findBestMatchingValidColormapName(String colormapName)
    {
        Preconditions.checkNotNull(builtInColormaps, "Map of color maps must be initialized first");
        Preconditions.checkState(!builtInColormaps.isEmpty(), "Map of color maps must be initialized first");

        String result;
        if (colormapName != null && builtInColormaps.containsKey(colormapName))
        {
            result = colormapName;
        }
        else if (currentColormapName != null && builtInColormaps.containsKey(currentColormapName))
        {
            result = currentColormapName;
        }
        else if (defaultColormapName != null && builtInColormaps.containsKey(defaultColormapName))
        {
            result = defaultColormapName;
        }
        else
        {
            result = builtInColormaps.keySet().iterator().next();
        }

        return result;
    }

    private Colormaps()
    {
        throw new AssertionError("Not an instantiable class");
    }

    /**
     * Test code below here.
     */
    public static void main(String[] args)
    {
        vtkNativeLibrary.LoadAllNativeLibraries();
        initialize();
        JFrame frame = new JFrame();
        frame.setVisible(true);
        int w = 100;
        int h = 30;
        JLabel label = new JLabel();
        frame.add(label);
        label.setSize(w, h);
        frame.setSize(w, h);
        for (String name : builtInColormaps.keySet())
        {
            Colormap colormap = builtInColormaps.get(name);
            label.setIcon(createIcon(colormap, w, h));
            label.repaint();
            BufferedImage im = new BufferedImage(label.getWidth(), label.getHeight(), BufferedImage.TYPE_INT_ARGB);
            label.paint(im.getGraphics());
            try
            {
                ImageIO.write(im, "PNG", new File("/Users/zimmemi1/Desktop/colormaps/" + name + ".png"));
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private static ImageIcon createIcon(Colormap cmap, int w, int h)
    {
        cmap.setRangeMin(0);
        cmap.setRangeMax(1);
        BufferedImage image = new BufferedImage(w, h, java.awt.color.ColorSpace.TYPE_RGB);
        for (int i = 0; i < w; i++)
        {
            double val = (double) i / (double) (image.getWidth() - 1);
            for (int j = 0; j < h; j++)
                image.setRGB(i, j, cmap.getColor(val).getRGB());
        }
        return new ImageIcon(image);
    }

}

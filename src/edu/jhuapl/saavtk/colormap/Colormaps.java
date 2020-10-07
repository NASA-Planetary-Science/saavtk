package edu.jhuapl.saavtk.colormap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import edu.jhuapl.saavtk.color.table.ColorTable;
import edu.jhuapl.saavtk.color.table.ColorTableUtil;
import edu.jhuapl.saavtk.util.Preferences;

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
            updateSystemColorTableDefault(currentColormapName);
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
        updateSystemColorTableDefault(colormapName);
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

            // Retrieve the system ColorTables
            List<ColorTable> tmpItemL = ColorTableUtil.getSystemColorTableList();
            if (tmpItemL.isEmpty() == true)
            	tmpItemL = ImmutableList.of(ColorTable.Rainbow);

            // Transform the ColorTables into Colormaps and install
            for (ColorTable aItem : tmpItemL)
            {
               RgbColormap tmpColormap = new RgbColormap(aItem);
               tmpColormap.setNumberOfLevels(128);
               builtInColormaps.put(aItem.getName(), tmpColormap);
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
     * Helper utility method to update the default system {@link ColorTable}.
     */
    private static void updateSystemColorTableDefault(String aName)
    {
        ColorTable tmpCT = null;
        for (ColorTable aItem : ColorTableUtil.getSystemColorTableList())
        {
     	      if (Objects.equals(aItem.getName(), aName) == true)
                tmpCT = aItem;
        }
        ColorTableUtil.setSystemColorTableDefault(tmpCT);
    }

}

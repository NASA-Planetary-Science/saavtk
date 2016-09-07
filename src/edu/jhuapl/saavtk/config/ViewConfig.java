package edu.jhuapl.saavtk.config;

import java.util.ArrayList;
import java.util.List;

import edu.jhuapl.saavtk.model.ShapeModelAuthor;
import edu.jhuapl.saavtk.model.ShapeModelBody;



/**
 * A Config is a class for storing models should be instantiated
 * together for a specific tool. Should be subclassed for each tool
 * application instance. This class is also used when creating (to know which tabs
 * to create).
 */
public class ViewConfig implements Cloneable
{
    public String customName;
    public boolean customTemporary = false;
    public ShapeModelAuthor author; // e.g. Gaskell
    public String version; // e.g. 2.0
    public ShapeModelBody body; // e.g. EROS or ITOKAWA

    public boolean useMinimumReferencePotential = false; // uses average otherwise
    public boolean hasCustomBodyCubeSize = false;
    // if hasCustomBodyCubeSize is true, the following must be filled in and valid
    public double customBodyCubeSize; // km
    public String[] smallBodyLabelPerResolutionLevel; // only needed when number resolution levels > 1
    public int[] smallBodyNumberOfPlatesPerResolutionLevel; // only needed when number resolution levels > 1



    public ViewConfig clone() // throws CloneNotSupportedException
    {
        ViewConfig c = null;
        try {
            c = (ViewConfig)super.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }

        c.author = this.author;
        c.version = this.version;

        c.customName = this.customName;
        c.customTemporary = this.customTemporary;

        c.useMinimumReferencePotential = this.useMinimumReferencePotential;
        c.hasCustomBodyCubeSize = this.hasCustomBodyCubeSize;
        c.customBodyCubeSize = this.customBodyCubeSize;

        if (this.smallBodyLabelPerResolutionLevel != null)
            c.smallBodyLabelPerResolutionLevel = this.smallBodyLabelPerResolutionLevel.clone();
        if (this.smallBodyNumberOfPlatesPerResolutionLevel != null)
            c.smallBodyNumberOfPlatesPerResolutionLevel = this.smallBodyNumberOfPlatesPerResolutionLevel.clone();

        return c;
    }

    /**
     * Returns model as a path. e.g. "Asteroid > Near-Earth > Eros > Image Based > Gaskell"
     */
     public String getPathRepresentation()
     {
         if (ShapeModelAuthor.CUSTOM == author)
         {
             return ShapeModelAuthor.CUSTOM + " > " + customName;
         }
         else
             return "DefaultPath";
     }

     /**
      * Return a unique name for this model. No other model may have this
      * name. Note that only applies within built-in models or custom models
      * but a custom model can share the name of a built-in one or vice versa.
      * By default simply return the author concatenated with the
      * name if the author is not null or just the name if the author
      * is null.
      * @return
      */

     public String getUniqueName()
     {
         if (ShapeModelAuthor.CUSTOM == author)
             return author + "/" + customName;
         else
             return "DefaultName";
     }

     public String getShapeModelName()
     {
         if (author == ShapeModelAuthor.CUSTOM)
             return customName;
         else
         {
             String ver = "";
             if (version != null)
                 ver += " (" + version + ")";
             return "DefaultName" + ver;
         }
     }

     static private List<ViewConfig> builtInConfigs = new ArrayList<ViewConfig>();
     static public List<ViewConfig> getBuiltInConfigs() { return builtInConfigs; }

     /**
      * Get a Config of a specific name and author.
      * Note a Config is uniquely described by its name, author, and version.
      * No two small body configs can have all the same. This version of the function
      * assumes the version is null (unlike the other version in which you can specify
      * the version).
      *
      * @param name
      * @param author
      * @return
      */
     static public ViewConfig getConfig(ShapeModelBody name, ShapeModelAuthor author)
     {
         return getConfig(name, author, null);
     }

     /**
      * Get a Config of a specific name, author, and version.
      * Note a Config is uniquely described by its name, author, and version.
      * No two small body configs can have all the same.
      *
      * @param name
      * @param author
      * @param version
      * @return
      */
     static public ViewConfig getConfig(ShapeModelBody name, ShapeModelAuthor author, String version)
     {
         for (ViewConfig config : getBuiltInConfigs())
         {
             if (((ViewConfig)config).body == name && config.author == author &&
                     ((config.version == null && version == null) || (version != null && version.equals(config.version)))
                     )
                 return (ViewConfig)config;
         }

         System.err.println("Error: Cannot find Config with name " + name +
                 " and author " + author + " and version " + version);

         return null;
     }



}

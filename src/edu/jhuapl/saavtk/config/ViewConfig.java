package edu.jhuapl.saavtk.config;

import java.util.ArrayList;
import java.util.List;

import edu.jhuapl.saavtk.model.ShapeModelType;
import edu.jhuapl.saavtk.model.ShapeModelBody;



/**
 * A Config is a class for storing models should be instantiated
 * together for a specific tool. Should be subclassed for each tool
 * application instance. This class is also used when creating (to know which tabs
 * to create).
 */
public abstract class ViewConfig implements Cloneable
{
    public String modelLabel;
    public boolean customTemporary = false;
    public ShapeModelType author; // e.g. Gaskell
    public String version; // e.g. 2.0
    public ShapeModelBody body; // e.g. EROS or ITOKAWA
    public boolean hasFlybyData; // for flyby path data
    public boolean hasStateHistory; // for bodies with state history tabs

    public boolean useMinimumReferencePotential = false; // uses average otherwise
    public boolean hasCustomBodyCubeSize = false;
    // if hasCustomBodyCubeSize is true, the following must be filled in and valid
    public double customBodyCubeSize; // km
    public String[] smallBodyLabelPerResolutionLevel; // only needed when number resolution levels > 1
    public int[] smallBodyNumberOfPlatesPerResolutionLevel; // only needed when number resolution levels > 1
    private boolean enabled = true;


    public abstract boolean isAccessible();

    @Override
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

        c.modelLabel = this.modelLabel;
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
         if (ShapeModelType.CUSTOM == author)
         {
             return ShapeModelType.CUSTOM + " > " + modelLabel;
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
         if (ShapeModelType.CUSTOM == author)
             return author + "/" + modelLabel;
         else
             return "DefaultName";
     }

     public String getShapeModelName()
     {
         if (author == ShapeModelType.CUSTOM)
             return modelLabel;
         else
         {
             String ver = "";
             if (version != null)
                 ver += " (" + version + ")";
             return "DefaultName" + ver;
         }
     }

     public boolean isEnabled()
     {
    	 return enabled;
     }

     public void enable(boolean enabled)
     {
    	 this.enabled = enabled;
     }

     static private List<ViewConfig> builtInConfigs = new ArrayList<>();
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
     static public ViewConfig getConfig(ShapeModelBody name, ShapeModelType author)
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
     static public ViewConfig getConfig(ShapeModelBody name, ShapeModelType author, String version)
     {
         for (ViewConfig config : getBuiltInConfigs())
         {
             if (config.body == name && config.author == author &&
                     ((config.version == null && version == null) || (version != null && version.equals(config.version)))
                     )
                 return config;
         }

         System.err.println("Error: Cannot find Config with name " + name +
                 " and author " + author + " and version " + version);

         return null;
     }



}

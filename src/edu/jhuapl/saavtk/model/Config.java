package edu.jhuapl.saavtk.model;

import java.util.ArrayList;



/**
 * A Config is a class for storing models should be instantiated
 * together for a specific tool. Should be subclassed for each tool
 * application instance. This class is also used when creating (to know which tabs
 * to create).
 */
public class Config implements Cloneable
{
    public String customName;
    public boolean customTemporary = false;
    public ShapeModelAuthor author; // e.g. Gaskell
    public String version; // e.g. 2.0

    public Config clone() // throws CloneNotSupportedException
    {
        Config c = null;
        try {
            c = (Config)super.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }

        c.author = this.author;
        c.version = this.version;

        c.customName = this.customName;
        c.customTemporary = this.customTemporary;

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

     static private ArrayList<Config> builtInConfigs = new ArrayList<Config>();
     static public ArrayList<Config> getBuiltInConfigs() { return builtInConfigs; }

}

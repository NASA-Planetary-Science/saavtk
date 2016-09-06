package edu.jhuapl.saavtk.gui;

import java.io.File;

public class FileChooserBase
{
    static private File lastDirectory = null;

    static protected void setLastDirectory(File dir)
    {
        lastDirectory = dir;
    }

    static protected File getLastDirectory()
    {
        return lastDirectory;
    }
}

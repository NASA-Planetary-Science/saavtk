package edu.jhuapl.saavtk.model;

// Class storing info related to plate data used to color shape model
public class LidarDatasourceInfo
{
    public String name = null;
    public String path = null;

    @Override
    public String toString()
    {
        String str = name + " (" + path + ")";
        return str;
    }
}

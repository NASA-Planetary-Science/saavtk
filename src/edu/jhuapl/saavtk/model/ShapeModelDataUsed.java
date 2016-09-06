package edu.jhuapl.saavtk.model;

// Data used to construct shape model (either images, radar, lidar, or enhanced)
public enum ShapeModelDataUsed
{
    IMAGE_BASED("Image Based"),
    RADAR_BASED("Radar Based"),
    LIDAR_BASED("Lidar Based"),
    ENHANCED("Enhanced");

    final private String str;
    private ShapeModelDataUsed(String str)
    {
        this.str = str;
    }

    @Override
    public String toString()
    {
        return str;
    }
}
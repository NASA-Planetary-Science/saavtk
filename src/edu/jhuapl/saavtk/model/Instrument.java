package edu.jhuapl.saavtk.model;

// Names of instruments
public enum Instrument
{
    MSI("MSI"),
    NLR("NLR"),
    NIS("NIS"),
    AMICA("AMICA"),
    LIDAR("LIDAR"),
    FC("FC"),
    SSI("SSI"),
    OSIRIS("OSIRIS"),
    OLA("OLA"),
    MAPCAM("MAPCAM"),
    POLYCAM("POLYCAM"),
    IMAGING_DATA("Imaging Data"),
    MVIC("MVIC"),
    LEISA("LEISA"),
    LORRI("LORRI"),
    GENERIC("GENERIC");

    final private String str;
    private Instrument(String str)
    {
        this.str = str;
    }

    @Override
    public String toString()
    {
        return str;
    }
}
package edu.jhuapl.saavtk.model;

// Names of authors
public enum ShapeModelType
{
    GASKELL("Gaskell"),
    THOMAS("Thomas"),
    STOOKE("Stooke"),
    HUDSON("Hudson"),
    DUXBURY("Duxbury"),
    OSTRO("Ostro"),
    JORDA("Jorda"),
    NOLAN("Nolan"),
    CUSTOM("Custom"),
    EROSNAV("NAV"),
    EROSNLR("NLR"),
    EXPERIMENTAL("Experimental"),
    LORRI("LORRI"),
    MVIC("MVIC"),
    CARRY("Carry"),
    DLR("DLR"),
    BLENDER("Zimmerman"),
    OREX("Orex"),
    HAYABUSA2("Hayabusa2"),
    TRUTH("Truth");

    final private String str;
    private ShapeModelType(String str)
    {
        this.str = str;
    }

    @Override
    public String toString()
    {
        return str;
    }
}
package edu.jhuapl.saavtk.model;

// Names of authors
public enum ShapeModelAuthor
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
    DLR("DLR");

    final private String str;
    private ShapeModelAuthor(String str)
    {
        this.str = str;
    }

    @Override
    public String toString()
    {
        return str;
    }
}
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
	JAXA_001("JAXA-001"),
	NASA_001("NASA-001"),
	OREX("OSIRIS-REx"),
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
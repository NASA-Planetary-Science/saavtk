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
	JAXA_SFM_v20180627("JAXA-SFM-v20180627"),
	JAXA_SFM_v20180714("JAXA-SFM-v20180714"),
	JAXA_SFM_v20180725_2("JAXA-SFM-v20180725_2"),
	JAXA_SPC_v20180705("JAXA-SPC-v20180705"),
	JAXA_SPC_v20180717("JAXA-SPC-v20180717"),
	JAXA_SPC_v20180719("JAXA-SPC-v20180719"),
	JAXA_SPC_v20180731("JAXA-SPC-v20180731"),
	NASA_001("NASA-001"),
	NASA_002("NASA-002"),
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
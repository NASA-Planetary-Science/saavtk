package edu.jhuapl.saavtk.model;

// Names of built-in small body models
public enum ShapeModelBody
{
	EROS("433 Eros"),
	ITOKAWA("25143 Itokawa"),
	VESTA("4 Vesta"),
	CERES("1 Ceres"),
	MIMAS("Mimas"),
	PHOEBE("Phoebe"),
	PHOBOS("Phobos"),
	RQ36("101955 Bennu"),
	DIONE("Dione"),
	TELESTO("Telesto"),
	RHEA("Rhea"),
	TETHYS("Tethys"),
	LUTETIA("21 Lutetia"),
	IDA("243 Ida"),
	GASPRA("951 Gaspra"),
	MATHILDE("253 Mathilde"),
	DEIMOS("Deimos"),
	JANUS("Janus"),
	EPIMETHEUS("Epimetheus"),
	HYPERION("Hyperion"),
	TEMPEL_1("9P/Tempel 1"),
	HALLEY("1P/Halley"),
	JUPITER("Jupiter"),
	AMALTHEA("Amalthea"),
	CALLISTO("Callisto"),
	EUROPA("Europa"),
	GANYMEDE("Ganymede"),
	IO("Io"),
	LARISSA("Larissa"),
	PROTEUS("Proteus"),
	PROMETHEUS("Prometheus"),
	PANDORA("Pandora"),
	GEOGRAPHOS("1620 Geographos"),
	KY26("1998 KY26"),
	BACCHUS("2063 Bacchus"),
	KLEOPATRA("216 Kleopatra"),
	TOUTATIS("4179 Toutatis"),
	CASTALIA("4769 Castalia"),
	_52760_1998_ML14("(52760) 1998 ML14"),
	GOLEVKA("6489 Golevka"),
	WILD_2("81P/Wild 2"),
	STEINS("2867 Steins"),
	HARTLEY("103P/Hartley 2"),
	PLUTO("Pluto"),
	CHARON("Charon"),
	HYDRA("Hydra"),
	KERBEROS("Kerberos"),
	NIX("Nix"),
	STYX("Styx"),
	_1950DAPROGRADE("(29075) 1950 DA Prograde"),
	_1950DARETROGRADE("(29075) 1950 DA Retrograde"),
	BETULIA("1580 Betulia"),
	CCALPHA("(136617) 1994 CC"),
	CE26("(276049) 2002 CE26"),
	EV5("(341843) 2008 EV5"),
	HW1("(8567) 1996 HW1"),
	KW4A("(66391) 1999 KW4 A"),
	KW4B("(66391) 1999 KW4 B"),
	MITHRA("4486 Mithra"),
	NEREUS("4660 Nereus"),
	RASHALOM("2100 Ra-Shalom"),
	SK("(10115) 1992 SK"),
	WT24("(33342) 1998 WT24"),
	YORP("54509 YORP"),
	PALLAS("2 Pallas"),
	DAPHNE("41 Daphne"),
	HERMIONE("121 Hermione"),
	_67P("67P/Churyumov-Gerasimenko"),
	EARTH("Earth"),
	CALYPSO("Calypso"),
	PAN("Pan"),
	ENCELADUS("Enceladus"),
	IAPETUS("Iapetus"),
	ATLAS("Atlas"),
	HELENE("Helene"),
	RYUGU("162173 Ryugu"),
	MU69("2014 MU69"),
	;

	final private String str;

	private ShapeModelBody(String str)
	{
		this.str = str;
	}

	@Override
	public String toString()
	{
		return str;
	}
}
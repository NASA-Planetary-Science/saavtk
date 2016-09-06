package edu.jhuapl.saavtk.model;

// Names of built-in small body models
public enum ShapeModelBody
{
    EROS("Eros"),
    ITOKAWA("Itokawa"),
    VESTA("Vesta"),
    CERES("Ceres"),
    MIMAS("Mimas"),
    PHOEBE("Phoebe"),
    PHOBOS("Phobos"),
    RQ36("Bennu"),
    DIONE("Dione"),
    RHEA("Rhea"),
    TETHYS("Tethys"),
    LUTETIA("Lutetia"),
    IDA("Ida"),
    GASPRA("Gaspra"),
    MATHILDE("Mathilde"),
    DEIMOS("Deimos"),
    JANUS("Janus"),
    EPIMETHEUS("Epimetheus"),
    HYPERION("Hyperion"),
    TEMPEL_1("Tempel 1"),
    HALLEY("Halley"),
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
    GEOGRAPHOS("Geographos"),
    KY26("KY26"),
    BACCHUS("Bacchus"),
    KLEOPATRA("Kleopatra"),
    TOUTATIS_LOW_RES("Toutatis (Low Res)"),
    TOUTATIS_HIGH_RES("Toutatis (High Res)"),
    CASTALIA("Castalia"),
    _52760_1998_ML14("52760 (1998 ML14)"),
    GOLEVKA("Golevka"),
    WILD_2("Wild 2"),
    STEINS("Steins"),
    HARTLEY("Hartley"),
    PLUTO("Pluto"),
    CHARON("Charon"),
    HYDRA("Hydra"),
    KERBEROS("Kerberos"),
    NIX("Nix"),
    STYX("Styx"),
    _1950DAPROGRADE("1950 DA Prograde"),
    _1950DARETROGRADE("1950 DA Retrograde"),
    BETULIA("Betulia"),
    CCALPHA("1994 CC Alpha"),
    CE26("CE26 Alpha"),
    EV5("EV5"),
    HW1("1996 HW1"),
    KW4A("KW4 Alpha"),
    KW4B("KW4 Beta"),
    MITHRA("Mithra"),
    NEREUS("Nereus"),
    RASHALOM("Ra-Shalom"),
    SK("SK"),
    WT24("WT24"),
    YORP("YORP"),
    PALLAS("Pallas"),
    DAPHNE("Daphne"),
    HERMIONE("Hermione"),
    _67P("67P");

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
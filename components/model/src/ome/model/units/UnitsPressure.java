
package ome.model.units;

public enum UnitsPressure {
    YOTTAPA("YPa"),
    ZETTAPA("ZPa"),
    EXAPA("EPa"),
    PETAPA("PPa"),
    TERAPA("TPa"),
    GIGAPA("GPa"),
    MEGAPA("MPa"),
    KPA("kPa"),
    HPA("hPa"),
    DAPA("daPa"),
    PA("Pa"),
    DPA("dPa"),
    CPA("cPa"),
    MPA("mPa"),
    MICROPA("µPa"),
    NPA("nPa"),
    PPA("pPa"),
    FPA("fPa"),
    APA("aPa"),
    ZPA("zPa"),
    YPA("yPa"),
    BAR("bar"),
    MEGABAR("Mbar"),
    KBAR("kBar"),
    DBAR("dbar"),
    CBAR("cbar"),
    MBAR("mbar"),
    ATM("atm"),
    PSI("psi"),
    TORR("Torr"),
    MTORR("mTorr"),
    MMHG("mm Hg");

    protected String value;

    private UnitsPressure(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
};


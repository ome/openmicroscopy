
package ome.model.enums;

public enum UnitsElectricPotential {
    YOTTAV("YV"),
    ZETTAV("ZV"),
    EXAV("EV"),
    PETAV("PV"),
    TERAV("TV"),
    GIGAV("GV"),
    MEGAV("MV"),
    KV("kV"),
    HV("hV"),
    DAV("daV"),
    V("V"),
    DV("dV"),
    CV("cV"),
    MV("mV"),
    MICROV("µV"),
    NV("nV"),
    PV("pV"),
    FV("fV"),
    AV("aV"),
    ZV("zV"),
    YV("yV");

    protected String value;

    private UnitsElectricPotential(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
};


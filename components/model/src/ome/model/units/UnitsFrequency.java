
package ome.model.units;

public enum UnitsFrequency {
    YOTTAHZ("YHz"),
    ZETTAHZ("ZHz"),
    EXAHZ("EHz"),
    PETAHZ("PHz"),
    TERAHZ("THz"),
    GIGAHZ("GHz"),
    MEGAHZ("MHz"),
    KHZ("kHz"),
    HHZ("hHz"),
    DAHZ("daHz"),
    HZ("Hz"),
    DHZ("dHz"),
    CHZ("cHz"),
    MHZ("mHz"),
    MICROHZ("µHz"),
    NHZ("nHz"),
    PHZ("pHz"),
    FHZ("fHz"),
    AHZ("aHz"),
    ZHZ("zHz"),
    YHZ("yHz");

    protected String value;

    private UnitsFrequency(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
};


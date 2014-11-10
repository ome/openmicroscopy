
package ome.model.enums;

public enum UnitsPower {
    YOTTAW("YW"),
    ZETTAW("ZW"),
    EXAW("EW"),
    PETAW("PW"),
    TERAW("TW"),
    GIGAW("GW"),
    MEGAW("MW"),
    KW("kW"),
    HW("hW"),
    DAW("daW"),
    W("W"),
    DW("dW"),
    CW("cW"),
    MW("mW"),
    MICROW("µW"),
    NW("nW"),
    PW("pW"),
    FW("fW"),
    AW("aW"),
    ZW("zW"),
    YW("yW");

    protected String value;

    private UnitsPower(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
};


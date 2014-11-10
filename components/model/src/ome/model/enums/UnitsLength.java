
package ome.model.enums;

public enum UnitsLength {
    YOTTAM("Ym"),
    ZETTAM("Zm"),
    EXAM("Em"),
    PETAM("Pm"),
    TERAM("Tm"),
    GIGAM("Gm"),
    MEGAM("Mm"),
    KM("km"),
    HM("hm"),
    DAM("dam"),
    M("m"),
    DM("dm"),
    CM("cm"),
    MM("mm"),
    MICROM("µm"),
    NM("nm"),
    PM("pm"),
    FM("fm"),
    AM("am"),
    ZM("zm"),
    YM("ym"),
    ANGSTROM("Å"),
    UA("ua"),
    LY("ly"),
    PC("pc"),
    THOU("thou"),
    LI("li"),
    IN("in"),
    FT("ft"),
    YD("yd"),
    MI("mi"),
    PT("pt"),
    PIXEL("Pixel"),
    REFERENCEFRAME("ReferenceFrame");

    protected String value;

    private UnitsLength(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
};


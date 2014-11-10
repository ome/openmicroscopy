
package ome.model.units;

public enum UnitsTime {
    YOTTAS("Ys"),
    ZETTAS("Zs"),
    EXAS("Es"),
    PETAS("Ps"),
    TERAS("Ts"),
    GIGAS("Gs"),
    MEGAS("Ms"),
    KS("ks"),
    HS("hs"),
    DAS("das"),
    S("s"),
    DS("ds"),
    CS("cs"),
    MS("ms"),
    MICROS("µs"),
    NS("ns"),
    PS("ps"),
    FS("fs"),
    AS("as"),
    ZS("zs"),
    YS("ys"),
    SECOND("s"),
    MIN("min"),
    H("h"),
    D("d");

    protected String value;

    private UnitsTime(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
};


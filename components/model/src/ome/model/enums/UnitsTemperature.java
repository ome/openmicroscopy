
package ome.model.enums;

public enum UnitsTemperature {
    K("K"),
    DEGREEC("°C"),
    DEGREEF("°F"),
    DEGREER("°R");

    protected String value;

    private UnitsTemperature(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
};


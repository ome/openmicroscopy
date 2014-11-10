
package ome.model.units;

public enum UnitsTemperature {
    DEGREEC("°C"),
    DEGREEF("°F"),
    DEGREER("°R"),
    K("K");

    protected String value;

    private UnitsTemperature(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
};


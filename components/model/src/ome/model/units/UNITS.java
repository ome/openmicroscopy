package ome.model.units;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public enum UNITS {

    ELECTRICPOTENTIAL(
            ElectricPotential.class,
            UnitsElectricPotential.class,
            6001),
    FREQUENCY(
            Frequency.class,
            UnitsFrequency.class,
            6002),
    LENGTH(
            Length.class,
            UnitsLength.class,
            6003),
    PRESSURE(
            Pressure.class,
            UnitsPressure.class,
            6004),
    POWER(
            Power.class,
            UnitsPower.class,
            6005),
    TEMPERATURE(
            Temperature.class,
            UnitsTemperature.class,
            6006),
    TIME(
            Time.class,
            UnitsTime.class,
            6007);

    Class<?> quantityType;
    Class<? extends Enum<?>> enumType;
    int sqlType;
    Enum<? extends Enum<?>>[] values;
    Map<String, Enum<? extends Enum<?>>> enumMap;
    Map<Enum<? extends Enum<?>>, String> valueMap;

    UNITS(Class<?> quantityType, Class<? extends Enum<?>> enumType, int sqlType) {
        this.quantityType = quantityType;
        this.enumType = enumType;
        this.sqlType = sqlType;
        this.values = enumType.getEnumConstants();
        enumMap = new HashMap<String, Enum<? extends Enum<?>>>();
        valueMap = new HashMap<Enum<? extends Enum<?>>, String>();
        try {
            Method m = this.enumType.getMethod("getValue");
            for (Enum<? extends Enum<?>> e : values) {
                String value = (String) m.invoke(e);
                enumMap.put(value, e);
                valueMap.put(e, value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse units", e);
        }
    }

    public static Map<String, Integer> listSqlTypes() {
        try {
            Map<String, Integer> rv = new HashMap<String, Integer>();
            for (UNITS u : values()) {
                rv.put(u.enumType.getSimpleName(), u.sqlType);
            }
            return rv;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load sql types", e);
        }
    }

}

package ome.model.units;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import ome.model.enums.*;

public enum UNITS {

    ELECTRICPOTENTIAL(
            ElectricPotential.class,
            UnitsElectricPotential.class,
            3001),
    FREQUENCY(
            Frequency.class,
            UnitsFrequency.class,
            3002),
    LENGTH(
            Length.class,
            UnitsLength.class,
            3003),
    PRESSURE(
            Pressure.class,
            UnitsPressure.class,
            3004),
    POWER(
            Power.class,
            UnitsPower.class,
            3005),
    TEMPERATURE(
            Temperature.class,
            UnitsTemperature.class,
            3006),
    TIME(
            Time.class,
            UnitsTime.class,
            3007);

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
            Method m = this.enumType.getMethod("getSymbol");
            for (Enum<? extends Enum<?>> e : values) {
                String symbol = (String) m.invoke(e);
                enumMap.put(symbol, e);
                valueMap.put(e, symbol);
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

    /**
     * Map from the CODE-based enums which are used in Java, Ice, etc.
     * to the SYMBOL-based enum present in the DB which contain invalid
     * characters for most languages.
     *
     * @param obj can't be null
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String todbvalue(Object obj) {
        Enum e = Enum.valueOf((Class)enumType, obj.toString());
        return valueMap.get(e);
    }

    /**
     * Perform the reverse lookup from {@link #todbvalue(Object)} converting
     * the DB's enums which contain invalid characters to the upper-cased
     * CODE-based enums used elsewhere.
     */
    @SuppressWarnings("rawtypes")
    public Enum fromdbvalue(String obj) {
        return enumMap.get(obj);
    }

}

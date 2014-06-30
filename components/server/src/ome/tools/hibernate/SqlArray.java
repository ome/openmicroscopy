package ome.tools.hibernate;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

/**
 * Hibernate type to store a java array using SQL ARRAY.
 *
 * @author Sylvain
 *
 *         References : http://forum.hibernate.org/viewtopic.php?t=946973
 *         http://archives.postgresql.org/pgsql-jdbc/2003-02/msg00141.php
 */
public class SqlArray<T> implements Array {

    public final static ListAsSQLArrayUserType.ArrayFactory FACTORY =
        new ListAsSQLArrayUserType.ArrayFactory() {

            public Array BOOLEAN(Connection conn, List<Boolean> value) {
                return new BOOLEAN(value);
            }

            public Array DATE(Connection conn, List<Date> value) {
                return new DATE(value);
            }

            public Array DOUBLE(Connection conn, List<Double> value) {
                return new DOUBLE(value);
            }

            public Array FLOAT(Connection conn, List<Float> value) {
                return new FLOAT(value);
            }

            public Array INTEGER(Connection conn, List<Integer> value) {
                return new INTEGER(value);
            }

            public Array STRING(Connection conn, List<String> value) throws SQLException {
                return new STRING(value);
            }

            public Array STRING2(Connection conn, List<String[]> value) throws SQLException {
                return new STRING2(value);
            }

        };

    private List<T> data;
    private int baseType;
    private String baseTypeName = null;

    protected SqlArray(List<T> data, int baseType) {
        this.data = data;
        this.baseType = baseType;
    }

    protected SqlArray(List<T> data, int baseType, String baseTypeName) {
        this(data, baseType);
        this.baseTypeName = baseTypeName;
    }

    public static class BOOLEAN extends SqlArray<Boolean> {
        public BOOLEAN(List<Boolean> data) {
            super(data, Types.BIT);
        }
    }

    public static class INTEGER extends SqlArray<Integer> {
        public INTEGER(List<Integer> data) {
            super(data, Types.INTEGER);
        }
    }

    public static class FLOAT extends SqlArray<Float> {
        public FLOAT(List<Float> data) {
            super(data, Types.FLOAT);
        }
    }

    public static class DOUBLE extends SqlArray<Double> {
        public DOUBLE(List<Double> data) {
            super(data, Types.DOUBLE);
        }
    }

    public static class STRING extends SqlArray<String> {
        public STRING(List<String> data) {
            super(data, Types.VARCHAR, "text");
        }
    }

    public static class STRING2 extends SqlArray<String[]> {
        public STRING2(List<String[]> data) {
            super(data, Types.VARCHAR, "text");
        }
    }

    public static class DATE extends SqlArray<Date> {
        public DATE(List<Date> data) {
            super(data, Types.TIMESTAMP);
        }
    }

    public String getBaseTypeName() {
        if (baseTypeName != null) {
            return baseTypeName;
        } else {
            throw new RuntimeException("No baseTypeName");
            // return SessionsManager.getSettings().getDialect().getTypeName(
            // baseType );
        }
    }

    public int getBaseType() {
        return baseType;
    }

    public Object getArray() {
        return data.toArray();
    }

    public Object getArray(long index, int count) {
        int lastIndex = count - (int) index;
        if (lastIndex > data.size())
            lastIndex = data.size();

        return data.subList((int) (index - 1), lastIndex).toArray();
    }

    @SuppressWarnings("unused")
    public Object getArray(Map<String, Class<?>> arg0) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unused")
    public Object getArray(long arg0, int arg1, Map<String, Class<?>> arg2) {
        throw new UnsupportedOperationException();
    }

    public ResultSet getResultSet() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unused")
    public ResultSet getResultSet(Map<String, Class<?>> arg0) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unused")
    public ResultSet getResultSet(long index, int count) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unused")
    public ResultSet getResultSet(long arg0, int arg1,
            Map<String, Class<?>> arg2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void free() {
        // do nothing. Required by Java 6
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append('{');
        boolean first = true;

        for (T t : data) {
            if (first)
                first = false;
            else
                result.append(',');

            if (t == null) {
                result.append("null");
                continue;
            }

            switch (baseType) {
            case Types.BIT:
            case Types.BOOLEAN:
                result.append(((Boolean) t).booleanValue() ? "true" : "false");
                break;

            case Types.INTEGER:
            case Types.FLOAT:
            case Types.DOUBLE:
            case Types.REAL:
            case Types.NUMERIC:
            case Types.DECIMAL:
                result.append(t);
                break;

            case Types.VARCHAR:
                if (t instanceof String[]) {
                    String[] arr = (String[])t;
                    result.append('{');
                    for (int i = 0; i < arr.length; i++) {
                        if (i>0) {
                            result.append(",");
                        }
                        appendString(result, arr[i]);
                    }
                    result.append('}');
                } else {
                    String s = (String) t;
                    appendString(result, s);
                }
                break;

            case Types.TIMESTAMP:
                Date d = (Date) t;
                result.append('\'');
                appendDate(result, d);
                result.append(d);
                result.append('\'');
                break;

            default:
                throw new UnsupportedOperationException("Unsupported type "
                        + baseType + " / " + getBaseTypeName());
            }
        }

        result.append('}');

        return result.toString();
    }

    /**
     * Refactored out by Josh for text[][] support
     */
    private void appendString(StringBuilder result, String s) {
        // Escape the string
        result.append('\"');
        for (int p = 0; p < s.length(); ++p) {
            char ch = s.charAt(p);
            if (ch == '\0')
                throw new IllegalArgumentException(
                        "Zero bytes may not occur in string parameters.");
            if (ch == '\\' || ch == '"')
                result.append('\\');
            result.append(ch);
        }
        result.append('\"');
    }

    private static GregorianCalendar calendar = null;

    protected void appendDate(StringBuilder sb, Date date) {
        if (calendar == null)
            calendar = new GregorianCalendar();

        calendar.setTime(date);

        // Append Date
        {
            int l_year = calendar.get(Calendar.YEAR);
            // always use at least four digits for the year so very
            // early years, like 2, don't get misinterpreted
            //
            int l_yearlen = String.valueOf(l_year).length();
            for (int i = 4; i > l_yearlen; i--)
                sb.append("0");

            sb.append(l_year);
            sb.append('-');
            int l_month = calendar.get(Calendar.MONTH) + 1;
            if (l_month < 10)
                sb.append('0');
            sb.append(l_month);
            sb.append('-');
            int l_day = calendar.get(Calendar.DAY_OF_MONTH);
            if (l_day < 10)
                sb.append('0');
            sb.append(l_day);
        }

        sb.append(' ');

        // Append Time
        {
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            if (hours < 10)
                sb.append('0');
            sb.append(hours);

            sb.append(':');
            int minutes = calendar.get(Calendar.MINUTE);
            if (minutes < 10)
                sb.append('0');
            sb.append(minutes);

            sb.append(':');
            int seconds = calendar.get(Calendar.SECOND);
            if (seconds < 10)
                sb.append('0');
            sb.append(seconds);

            if (date instanceof Timestamp) {
                // Add nanoseconds.
                // This won't work for postgresql versions < 7.2 which only want
                // a two digit fractional second.

                Timestamp t = (Timestamp) date;
                char[] decimalStr = { '0', '0', '0', '0', '0', '0', '0', '0',
                        '0' };
                char[] nanoStr = Integer.toString(t.getNanos()).toCharArray();
                System.arraycopy(nanoStr, 0, decimalStr, decimalStr.length
                        - nanoStr.length, nanoStr.length);
                sb.append('.');
                sb.append(decimalStr, 0, 6);
            }
        }

        // Append Time Zone offset
        {
            // int offset = -(date.getTimezoneOffset());
            int offset = (calendar.get(Calendar.ZONE_OFFSET) + calendar
                    .get(Calendar.DST_OFFSET))
                    / (60 * 1000);
            int absoff = Math.abs(offset);
            int hours = absoff / 60;
            int mins = absoff - hours * 60;

            sb.append((offset >= 0) ? "+" : "-");

            if (hours < 10)
                sb.append('0');
            sb.append(hours);

            if (mins < 10)
                sb.append('0');
            sb.append(mins);
        }

        // Append Era
        if (calendar.get(Calendar.ERA) == GregorianCalendar.BC)
            sb.append(" BC");
    }
}

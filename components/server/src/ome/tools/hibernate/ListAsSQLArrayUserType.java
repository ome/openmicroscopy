package ome.tools.hibernate;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

/**
 * Hibernate type to store Lists of primitives using SQL ARRAY.
 *
 * @author Sylvain
 *
 *         References : http://forum.hibernate.org/viewtopic.php?t=946973
 *         http://archives.postgresql.org/pgsql-jdbc/2003-02/msg00141.php
 */
public abstract class ListAsSQLArrayUserType<T> implements UserType, ParameterizedType {

    public interface ArrayFactory {
        Array BOOLEAN(Connection conn, List<Boolean> value) throws SQLException;
        Array DATE(Connection conn, List<Date> value) throws SQLException;
        Array DOUBLE(Connection conn, List<Double> value) throws SQLException;
        Array FLOAT(Connection conn, List<Float> value) throws SQLException;
        Array INTEGER(Connection conn, List<Integer> value) throws SQLException;
        Array STRING(Connection conn, List<String> value) throws SQLException;
        Array STRING2(Connection conn, List<String[]> value) throws SQLException;
    }

    private static final int SQL_TYPE = Types.ARRAY;
    private static final int[] SQL_TYPES = { SQL_TYPE };
    private /*final*/ String profile;
    protected ArrayFactory factory;

    public void setParameterValues(Properties parameters) {
        profile = parameters.getProperty("profile");
        try {
            Class FACTORY = Class.forName("ome.tools.hibernate." + profile.toUpperCase());
            Field field = FACTORY.getField("ARRAY_FACTORY");
            factory = (ArrayFactory) field.get(null);
        } catch (ClassNotFoundException e) {
            factory = SqlArray.FACTORY; // DEFAULT
        } catch (Exception e) {
            throw new RuntimeException("Failed to acquire factory for profile " + profile, e);
        }
    }

    abstract protected Array getDataAsArray(Connection conn, Object value) throws SQLException;

    abstract protected List<T> getDataFromArray(Object primitivesArray);

    /**
     * To use, define : hibernate.property
     * type="ome.tools.hibernate.ListAsSQLArrayUserType$BOOLEAN"
     * hibernate.column name="fieldName" sql-type="bool[]"
     */
    public static class BOOLEAN extends ListAsSQLArrayUserType<Boolean> {
        @Override
        @SuppressWarnings("unchecked")
        protected Array getDataAsArray(Connection conn, Object value) throws SQLException {
            return factory.BOOLEAN(conn, (List<Boolean>) value);
        }

        @Override
        protected List<Boolean> getDataFromArray(Object array) {
            boolean[] booleans = (boolean[]) array;
            ArrayList<Boolean> result = new ArrayList<Boolean>(booleans.length);
            for (boolean b : booleans)
                result.add(b);

            return result;
        }
    }

    /**
     * To use, define : hibernate.property
     * type="ome.tools.hibernate.ListAsSQLArrayUserType$INTEGER"
     * hibernate.column name="fieldName" sql-type="int[]"
     */
    public static class INTEGER extends ListAsSQLArrayUserType<Integer> {
        @Override
        @SuppressWarnings("unchecked")
        protected Array getDataAsArray(Connection conn, Object value) throws SQLException {
            return factory.INTEGER(conn, (List<Integer>) value);
        }

        @Override
        protected List<Integer> getDataFromArray(Object array) {
            int[] ints = (int[]) array;
            ArrayList<Integer> result = new ArrayList<Integer>(ints.length);
            for (int i : ints)
                result.add(i);

            return result;
        }
    }

    /**
     * To use, define : hibernate.property
     * type="ome.tools.hibernate.ListAsSQLArrayUserType$FLOAT"
     * hibernate.column name="fieldName" sql-type="real[]"
     */
    public static class FLOAT extends ListAsSQLArrayUserType<Float> {
        @Override
        @SuppressWarnings("unchecked")
        protected Array getDataAsArray(Connection conn, Object value) throws SQLException {
            return factory.FLOAT(conn, (List<Float>) value);
        }

        @Override
        protected List<Float> getDataFromArray(Object array) {
            float[] floats = (float[]) array;
            ArrayList<Float> result = new ArrayList<Float>(floats.length);
            for (float f : floats)
                result.add(f);

            return result;
        }
    }

    /**
     * To use, define : hibernate.property
     * type="ome.tools.hibernate.ListAsSQLArrayUserType$DOUBLE"
     * hibernate.column name="fieldName" sql-type="float8[]"
     */
    public static class DOUBLE extends ListAsSQLArrayUserType<Double> {
        @Override
        @SuppressWarnings("unchecked")
        protected Array getDataAsArray(Connection conn, Object value) throws SQLException {
            return factory.DOUBLE(conn, (List<Double>) value);
        }

        @Override
        protected List<Double> getDataFromArray(Object array) {
            double[] doubles = (double[]) array;
            ArrayList<Double> result = new ArrayList<Double>(doubles.length);
            for (double d : doubles)
                result.add(d);

            return result;
        }
    }

    /**
     * To use, define : hibernate.property
     * type="ome.tools.hibernate.ListAsSQLArrayUserType$STRING"
     * hibernate.column name="fieldName" sql-type="text[]"
     */
    public static class STRING extends ListAsSQLArrayUserType<String> {
        @Override
        @SuppressWarnings("unchecked")
        protected Array getDataAsArray(Connection conn, Object value) throws SQLException {
            return factory.STRING(conn, (List<String>) value);
        }

        @Override
        protected List<String> getDataFromArray(Object array) {
            String[] strings = (String[]) array;
            ArrayList<String> result = new ArrayList<String>(strings.length);
            for (String s : strings)
                result.add(s);

            return result;
        }
    }

    /**
     * To use, define : hibernate.property
     * type="ome.tools.hibernate.ListAsSQLArrayUserType$STRING2"
     * hibernate.column name="fieldName" sql-type="text[]"
     *
     * Added by Josh
     */
    public static class STRING2 extends ListAsSQLArrayUserType<String[]> {
        @Override
        @SuppressWarnings("unchecked")
        protected Array getDataAsArray(Connection conn, Object value) throws SQLException {
            return factory.STRING2(conn, (List<String[]>) value);
        }

        @Override
        protected List<String[]> getDataFromArray(Object array) {
            if (String[][].class.isAssignableFrom(array.getClass())) {
                String[][] strings = (String[][]) array;
                ArrayList<String[]> result = new ArrayList<String[]>(strings.length);
                for (String[] s : strings)
                    result.add(s);
                return result;
            } else {
                // ticket:2290
                if (String[].class.isAssignableFrom(array.getClass())) {
                    String[] strings = (String[]) array;
                    if (strings.length == 0) {
                        // ok. String[0][] got changed to String[0]
                        return new ArrayList<String[]>(0);
                    }
                }
                throw new RuntimeException("ticket:2290 - bad array type: " + array);
            }

        }
    }

    /**
     * To use, define : hibernate.property
     * type="ome.tools.hibernate.ListAsSQLArrayUserType$DATE"
     * hibernate.column name="fieldName" sql-type="timestamp[]"
     */
    public static class DATE extends ListAsSQLArrayUserType<Date> {
        @Override
        @SuppressWarnings("unchecked")
        protected Array getDataAsArray(Connection conn, Object value) throws SQLException {
            return factory.DATE(conn, (List<Date>) value);
        }

        @Override
        protected List<Date> getDataFromArray(Object array) {
            Date[] dates = (Date[]) array;
            ArrayList<Date> result = new ArrayList<Date>(dates.length);
            for (Date d : dates)
                result.add(d);

            return result;
        }
    }

    /**
     * Warning, this one is special. You have to define a class that extends
     * ENUM_LIST&lt;E&gt; and that has a no arguments constructor. For example :
     * class MyEnumsList extends ENUM_LIST&&ltMyEnumType&gt; { public
     * MyEnumList(){ super( MyEnum.values() ); } } Then, define :
     * hibernate.property type="com.myPackage.MyEnumsList" hibernate.column
     * name="fieldName" sql-type="int[]"
     */
    public static class ENUM<E extends Enum<E>> extends
            ListAsSQLArrayUserType<E> {
        private E[] theEnumValues;

        /**
         * @param theEnumValues
         *            The values of enum (by invoking .values()).
         */
        protected ENUM(E[] theEnumValues) {
            this.theEnumValues = theEnumValues;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Array getDataAsArray(Connection conn, Object value) throws SQLException {
            List<E> enums = (List<E>) value;
            List<Integer> integers = new ArrayList<Integer>(enums.size());
            for (E theEnum : enums)
                integers.add(theEnum.ordinal());

            return factory.INTEGER(conn, integers);
        }

        @Override
        protected List<E> getDataFromArray(Object array) {
            int[] ints = (int[]) array;
            ArrayList<E> result = new ArrayList<E>(ints.length);
            for (int val : ints) {
                for (int i = 0; i < theEnumValues.length; i++) {
                    if (theEnumValues[i].ordinal() == val) {
                        result.add(theEnumValues[i]);
                        break;
                    }
                }
            }

            if (result.size() != ints.length)
                throw new RuntimeException("Error attempting to convert "
                        + array + " into an array of enums (" + theEnumValues
                        + ").");

            return result;
        }
    }

    public Class returnedClass() {
        return List.class;
    }

    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    public Object deepCopy(Object value) {
        return value;
    }

    public boolean isMutable() {
        return true;
    }

    public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
            throws HibernateException, SQLException {

        Array sqlArray = resultSet.getArray(names[0]);
        if (resultSet.wasNull())
            return null;

        return getDataFromArray(sqlArray.getArray());
    }

    public void nullSafeSet(PreparedStatement preparedStatement, Object value,
            int index) throws HibernateException, SQLException {
        if (null == value)
            preparedStatement.setNull(index, SQL_TYPE);
        else
            preparedStatement.setArray(index,
                    getDataAsArray(preparedStatement.getConnection(), value));
    }

    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == y)
            return true;
        if (null == x || null == y)
            return false;
        Class<?> javaClass = returnedClass();
        if (!javaClass.equals(x.getClass()) || !javaClass.equals(y.getClass()))
            return false;

        return x.equals(y);
    }

    public Object assemble(Serializable cached, Object owner)
            throws HibernateException {
        return cached;
    }

    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    public Object replace(Object original, Object target, Object owner)
            throws HibernateException {
        return original;
    }
}

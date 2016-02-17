package omero.gateway.model;

import java.util.Arrays;

/**
 * A simple data 'container' for an OMERO.table
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class TableData {

    /** The table header */
    private String columnNames[];

    /** Column descriptions */
    private String descriptions[];

    /** The data in form data['column index']['row data'] */
    private Object[][] data;

    /** The data types of the columns */
    private Class<?>[] types;

    /**
     * Creates a new instance
     * 
     * @param columnNames
     *            The headers; can be <code>null</code>
     * @param descriptions
     *            Column descriptions; can be <code>null</code>
     * @param types
     *            The data types of the columns
     * @param data
     *            The data in form data['column index']['row data']
     */
    public TableData(String[] columnNames, String[] descriptions,
            Class<?>[] types, Object[][] data) {
        this.columnNames = columnNames;
        this.descriptions = descriptions;
        this.data = data;
        this.types = types;
    }

    /**
     * Get the headers
     * 
     * @return See above
     */
    public String[] getColumnNames() {
        return columnNames;
    }

    /**
     * Get the column descriptions
     * 
     * @return See above
     */
    public String[] getDescriptions() {
        return descriptions;
    }

    /**
     * Get the data
     * 
     * @return See above
     */
    public Object[][] getData() {
        return data;
    }

    /**
     * Get the data types
     * 
     * @return See above
     */
    public Class<?>[] getTypes() {
        return types;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(columnNames);
        result = prime * result + Arrays.hashCode(types);
        result = prime * result + objectArrayHashCode(data, types);
        result = prime * result + Arrays.hashCode(descriptions);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TableData other = (TableData) obj;
        if (Arrays.hashCode(types) != Arrays.hashCode(other.types))
            return false;
        if (stringArrayHashCode(columnNames) != stringArrayHashCode(other.columnNames))
            return false;
        if (stringArrayHashCode(descriptions) != stringArrayHashCode(other.descriptions))
            return false;
        if (objectArrayHashCode(data, types) != objectArrayHashCode(other.data,
                other.types))
            return false;
        return true;
    }

    /**
     * Generates a hash code for a String array, ignoring empty and
     * <code>null</code> Strings
     * 
     * @param array
     *            The String array to generate the hash code for
     * @return See above.
     */
    private int stringArrayHashCode(String[] array) {
        StringBuilder sb = new StringBuilder();
        if (array != null) {
            for (String s : array)
                sb.append(s);
        }
        return sb.toString().hashCode();
    }

    /**
     * Generates a hash code by iterating over all elements and casting them to
     * their proposed classes
     * 
     * @param objects
     *            The array to generate the object for
     * @param types
     *            The types of the elements in the array; it's assumed that
     *            every element of a sub array (<code>object[i][]</code>) has
     *            the same type (<code>types[i]</code>).
     * @return See above
     */
    private int objectArrayHashCode(Object[][] objects, Class[] types) {

        // The reason for this method is, that we can't use Arrays.hashCode()
        // method on Object[][] arrays, because an Object[][] array can be
        // for example an array of Object arrays, but also an array of String
        // arrays, in which case they have a different hash codes and are
        // *not* equal even if they contain in fact equal elements.
        // An example for this:
        // Object[][] test = new String[1][1];
        // test[0] = new String[1];
        // test[0][0] = new String("test");
        // Object[][] test2 = new Object[1][1];
        // test2[0][0] = new String("test");
        // --> Arrays.hashCode(test) != Arrays.hashCode(test2)

        final int prime = 31;
        int result = 1;

        for (int i = 0; i < objects.length; i++) {
            Object[] col = objects[i];

            for (int j = 0; j < col.length; j++) {
                Object castedObject = types[i].cast(col[j]);
                if (types[i].isArray())
                    result = prime * result
                            + Arrays.hashCode((Object[]) castedObject);
                else
                    result = prime * result + castedObject.hashCode();
            }
        }

        return result;
    }
}

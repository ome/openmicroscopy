/*
 * Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
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
     * The offset, if this TableData represents only a subset of the original
     * table
     */
    private long offset = 0;

    /** The Id of the original file */
    private long originalFileId = -1;

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

    /**
     * Get the row offset (if this {@link TableData} represents only a subset of
     * the original table)
     * 
     * @return See above
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Get the original file id
     * 
     * @return See above
     */
    public long getOriginalFileId() {
        return originalFileId;
    }

    /**
     * Set the originalfile id
     * 
     * @param originalFileId
     *            The originalfile id
     */
    public void setOriginalFileId(long originalFileId) {
        this.originalFileId = originalFileId;
    }

    /**
     * Set the row offset (if this {@link TableData} represents only a subset of
     * the original table)
     * 
     * @param offset
     *            The row offset
     */
    public void setOffset(long offset) {
        this.offset = offset;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime + (int) offset;
        result = prime * result + (int) originalFileId;
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
        if (originalFileId != other.getOriginalFileId())
            return false;
        if (offset != other.getOffset())
            return false;
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

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
import java.util.List;

/**
 * A simple data 'container' for an OMERO.table
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class TableData {

    /** The column definitions */
    private TableDataColumn columns[];

    /** The data in form data['column index']['row data'] */
    private Object[][] data;

    /**
     * The offset, if this TableData represents only a subset of the original
     * table
     */
    private long offset = 0;

    /** The Id of the original file */
    private long originalFileId = -1;

    /**
     * Number of rows in the original table (this doesn't have to match
     * data[x].length, depending on how many rows are loaded)
     */
    private long numberOfRows = 0;

    /**
     * Creates a new instance
     * 
     * @param columns
     *            The column definitions
     * @param data
     *            The data in form of List of columns
     */
    public TableData(List<TableDataColumn> columns, List<List<Object>> data) {
        this.columns = new TableDataColumn[columns.size()];
        this.columns = columns.toArray(this.columns);

        int nRows = !data.isEmpty() ? data.get(0).size() : 0;
        this.data = new Object[data.size()][nRows];
        for (int i = 0; i < data.size(); i++) {
            List<Object> columnData = data.get(i);
            for (int j = 0; j < columnData.size(); j++)
                this.data[i][j] = columnData.get(j);
        }
    }

    /**
     * Creates a new instance
     * 
     * @param columns
     *            The column definitions
     * @param data
     *            The data in form data['column index']['row data']
     */
    public TableData(TableDataColumn[] columns, Object[][] data) {
        this.columns = columns;
        this.data = data;
    }

    /**
     * Get the headers
     * 
     * @return See above
     */
    public TableDataColumn[] getColumns() {
        return columns;
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

    /**
     * @return <code>true</code> if the last available row is contained,
     *         <code>false</code> if there's more data available in the original
     *         table
     */
    public boolean isCompleted() {
        if (data == null || data.length == 0)
            return true;

        return (offset + data[0].length) == numberOfRows;
    }

    /**
     * Manually set completed state (sets the {@link TableData#numberOfRows} to
     * the last row in the {@link TableData#data} array)
     */
    public void setCompleted() {
        this.numberOfRows = (data == null || data.length == 0) ? 0 : offset
                + data[0].length;
    }

    /**
     * @return The total number of rows in the original table (this doesn't have
     *         to match data[x].length, depending on how many rows are loaded)
     */
    public long getNumberOfRows() {
        return numberOfRows;
    }

    /**
     * Set the total number of rows in the original table
     * 
     * @param numberOfRows
     *            See above
     */
    public void setNumberOfRows(long numberOfRows) {
        this.numberOfRows = numberOfRows;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime + (int) offset;
        result = prime * result + (int) numberOfRows;
        result = prime * result + (int) originalFileId;
        result = prime * result + Arrays.hashCode(columns);
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
        if (numberOfRows != other.numberOfRows)
            return false;
        if (Arrays.hashCode(columns) != Arrays.hashCode(other.columns))
            return false;
        if (objectArrayHashCode(data, columns) != objectArrayHashCode(
                other.data, other.columns))
            return false;
        return true;
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
    private int objectArrayHashCode(Object[][] objects,
            TableDataColumn[] columns) {

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
                Object castedObject = columns[i].getType().cast(col[j]);
                if (columns[i].getType().isArray())
                    result = prime * result
                            + Arrays.hashCode((Object[]) castedObject);
                else
                    result = prime * result + castedObject.hashCode();
            }
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (columns != null) {
            for (int i = 0; i < columns.length; i++) {
                sb.append(columns[i]);
                sb.append('\t');
            }
            sb.append('\n');
        }

        if (data == null || data.length == 0)
            return sb.toString();

        int nRows = 0;
        if (data[0] != null)
            nRows = data[0].length;
        for (int r = 0; r < nRows; r++) {
            for (int c = 0; c < data.length; c++) {
                sb.append(data[c][r]);
                sb.append('\t');
            }
            sb.append('\n');
        }

        return sb.toString();
    }

}

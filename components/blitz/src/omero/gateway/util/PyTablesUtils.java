/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway.util;

import java.util.HashMap;
import java.util.Map;

import omero.gateway.exception.DSAccessException;
import omero.gateway.model.TableResult;
import omero.grid.BoolColumn;
import omero.grid.Column;
import omero.grid.Data;
import omero.grid.DoubleColumn;
import omero.grid.ImageColumn;
import omero.grid.LongColumn;
import omero.grid.RoiColumn;
import omero.grid.StringColumn;
import omero.grid.TablePrx;
import omero.grid.WellColumn;

//Java imports

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class PyTablesUtils {

    /** Maximum number of rows to retrieve at one time from a table. */
    private static final int MAX_TABLE_ROW_RETRIEVAL = 100000;

    /**
     * Transforms the passed table data for a given image.
     *
     * @param table
     *            The table to convert.
     * @param key
     *            The key of the <code>where</code> clause.
     * @param id
     *            The identifier of the object to retrieve rows for.
     * @return See above
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMEDS
     *             service.
     */
    public static TableResult createTableResult(TablePrx table, String key,
            long id) throws DSAccessException {
        if (table == null)
            return null;
        try {
            key = "(" + key + "==%d)";
            long totalRowCount = table.getNumberOfRows();
            long[] rows = table.getWhereList(String.format(key, id), null, 0,
                    totalRowCount, 1L);
            return createTableResult(table, rows);
        } catch (Exception e) {
            try {
                if (table != null)
                    table.close();
            } catch (Exception ex) {
                // Digest exception
            }
            throw new DSAccessException("Unable to read the table.", e);
        }
    }

    /**
     * Transforms a set of rows for the passed table.
     *
     * @param table
     *            The table to convert.
     * @param rows
     *            The rows of the table to convert.
     * @return See above
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMEDS
     *             service.
     */
    public static TableResult createTableResult(TablePrx table, long[] rows)
            throws DSAccessException {
        if (table == null)
            return null;
        try {
            Column[] cols = table.getHeaders();
            String[] headers = new String[cols.length];
            String[] headersDescriptions = new String[cols.length];
            for (int i = 0; i < cols.length; i++) {
                headers[i] = cols[i].name;
                headersDescriptions[i] = cols[i].description;
            }
            int totalRowCount = rows.length;
            Object[][] data = new Object[totalRowCount][cols.length];
            Data d;
            long[] columns = new long[cols.length];
            for (int i = 0; i < cols.length; i++) {
                columns[i] = i;
            }

            int rowOffset = 0;
            int rowCount = 0;
            int rowsToGo = totalRowCount;
            long[] rowSubset;
            Map<Integer, Integer> indexes = new HashMap<Integer, Integer>();
            while (rowsToGo > 0) {
                rowCount = (int) Math.min(MAX_TABLE_ROW_RETRIEVAL,
                        totalRowCount - rowOffset);
                rowSubset = new long[rowCount];
                System.arraycopy(rows, rowOffset, rowSubset, 0, rowCount);
                d = table.slice(columns, rowSubset);
                for (int i = 0; i < cols.length; i++) {
                    translateTableResult(d, data, rowOffset, rowCount, indexes);
                }
                rowOffset += rowCount;
                rowsToGo -= rowCount;
            }
            table.close();
            TableResult tr = new TableResult(data, headers);
            tr.setIndexes(indexes);
            return tr;
        } catch (Exception e) {
            try {
                if (table != null)
                    table.close();
            } catch (Exception ex) {
                // Digest exception
            }
            throw new DSAccessException("Unable to read the table.", e);
        }
    }

    /**
     * Translates a set of table results into an array.
     * 
     * @param src
     *            Source data from the table.
     * @param dst
     *            Destination array.
     * @param offset
     *            Offset within the destination array from which to copy data
     *            into.
     * @param length
     *            Number of rows of data to be copied.
     */
    private static void translateTableResult(Data src, Object[][] dst,
            int offset, int length, Map<Integer, Integer> indexes) {
        Column[] cols = src.columns;
        Column column;
        for (int i = 0; i < cols.length; i++) {
            column = cols[i];
            if (column instanceof LongColumn) {
                for (int j = 0; j < length; j++) {
                    dst[j + offset][i] = ((LongColumn) column).values[j];
                }
            } else if (column instanceof DoubleColumn) {
                for (int j = 0; j < length; j++) {
                    dst[j + offset][i] = ((DoubleColumn) column).values[j];
                }
            } else if (column instanceof StringColumn) {
                for (int j = 0; j < length; j++) {
                    dst[j + offset][i] = ((StringColumn) column).values[j];
                }
            } else if (column instanceof BoolColumn) {
                for (int j = 0; j < length; j++) {
                    dst[j + offset][i] = ((BoolColumn) column).values[j];
                }
            } else if (column instanceof RoiColumn) {
                indexes.put(TableResult.ROI_COLUMN_INDEX, i);
                for (int j = 0; j < length; j++) {
                    dst[j + offset][i] = ((RoiColumn) column).values[j];
                }
            } else if (column instanceof ImageColumn) {
                indexes.put(TableResult.IMAGE_COLUMN_INDEX, i);
                for (int j = 0; j < length; j++) {
                    dst[j + offset][i] = ((ImageColumn) column).values[j];
                }
            } else if (column instanceof WellColumn) {
                indexes.put(TableResult.WELL_COLUMN_INDEX, i);
                for (int j = 0; j < length; j++) {
                    dst[j + offset][i] = ((WellColumn) column).values[j];
                }
            }
        }
    }

}

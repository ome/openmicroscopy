/*
 * Copyright (C) 2016-2017 University of Dundee & Open Microscopy Environment.
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
package omero.gateway.facility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import omero.IllegalArgumentException;
import omero.ServerError;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.TableData;
import omero.gateway.model.TableDataColumn;
import omero.gateway.util.Pojos;
import omero.grid.Column;
import omero.grid.Data;
import omero.grid.SharedResourcesPrx;
import omero.grid.TablePrx;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;

/**
 * {@link Facility} to interact with OMERO.tables
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class TablesFacility extends Facility {

    /** The mimetype of an omero tables file */
    public static final String TABLES_MIMETYPE = "OMERO.tables";

    /** Maximum number of rows to fetch if not specified otherwise */
    public static final int DEFAULT_MAX_ROWS_TO_FETCH = 1000;

    /**
     * Creates a new instance
     *
     * @param gateway
     *            Reference to the {@link Gateway}
     */
    TablesFacility(Gateway gateway) {
        super(gateway);
    }

    /**
     * Adds a new table with the provided data
     *
     * @param ctx
     *            The {@link SecurityContext}
     * @param target
     *            The object to attach the table to
     * @param name
     *            A name for the table (can be <code>null</code>)
     * @param data
     *            The data
     * @return The {@link TableData}
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public TableData addTable(SecurityContext ctx, DataObject target,
            String name, TableData data) throws DSOutOfServiceException,
            DSAccessException {
        if (!Pojos.hasID(target))
            return null;

        TablePrx table = null;
        try {
            if (name == null)
                name = UUID.randomUUID().toString();

            TablesFacilityHelper helper = new TablesFacilityHelper(this);
            helper.parseTableData(data);

            SharedResourcesPrx sr = gateway.getSharedResources(ctx);
            if (!sr.areTablesEnabled()) {
                throw new DSAccessException(
                        "Tables feature is not enabled on this server!");
            }
            long repId = sr.repositories().descriptions.get(0).getId()
                    .getValue();
            table = sr.newTable(repId, name);
            table.initialize(helper.getGridColumns());
            table.addData(helper.getGridColumns());

            DataManagerFacility dm = gateway
                    .getFacility(DataManagerFacility.class);
            BrowseFacility browse = gateway.getFacility(BrowseFacility.class);

            OriginalFile file = table.getOriginalFile();
            file = (OriginalFile) browse.findIObject(ctx, file);

            FileAnnotation anno = new FileAnnotationI();
            anno.setFile(file);
            FileAnnotationData annotation = new FileAnnotationData(anno);
            annotation.setDescription(name);

            annotation = (FileAnnotationData) dm.saveAndReturnObject(ctx,
                    annotation);
            dm.attachAnnotation(ctx, annotation, target);

            data.setOriginalFileId(file.getId().getValue());
            data.setNumberOfRows(table.getNumberOfRows());
        } catch (Exception e) {
            handleException(this, e, "Could not add table");
        } finally {
            if (table != null)
                try {
                    table.close();
                } catch (ServerError e) {
                    logError(this, "Could not close table", e);
                }
        }
        return data;
    }

    /**
     * Get basic information about a table.
     *
     * @param ctx
     *            The {@link SecurityContext}
     * @param fileId
     *            The id of the {@link OriginalFile} which stores the table
     * @return An 'empty' {@link TableData} object without the actual table data
     *         loaded; which only contains information about the columns and the
     *         size of the table.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public TableData getTableInfo(SecurityContext ctx, long fileId)
            throws DSOutOfServiceException, DSAccessException {
        return getTable(ctx, fileId, 0, 0);
    }

    /**
     * Load the data from a table (Note: limited to
     * {@link TablesFacility#DEFAULT_MAX_ROWS_TO_FETCH} number of rows)
     *
     * @param ctx
     *            The {@link SecurityContext}
     * @param fileId
     *            The id of the {@link OriginalFile} which stores the table
     * @return The data which the table contains
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public TableData getTable(SecurityContext ctx, long fileId)
            throws DSOutOfServiceException, DSAccessException {
        return getTable(ctx, fileId, 0, DEFAULT_MAX_ROWS_TO_FETCH - 1);
    }

    /**
     * Load data from a table
     *
     * @param ctx
     *            The {@link SecurityContext}
     * @param fileId
     *            The id of the {@link OriginalFile} which stores the table
     * @param rowFrom
     *            The start row (inclusive)
     * @param rowTo
     *            The end row (inclusive) (can be <code>-1</code> in which case
     *            {@link TablesFacility#DEFAULT_MAX_ROWS_TO_FETCH} rows will be
     *            fetched)
     * @param columns
     *            The columns to take into account (can be left unspecified, in
     *            which case all columns will used)
     * @return The specified data
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public TableData getTable(SecurityContext ctx, long fileId, long rowFrom,
            long rowTo, int... columns) throws DSOutOfServiceException,
            DSAccessException {
        long[] lcolumns = null;
        if (columns != null) {
            lcolumns = new long[columns.length];
            for (int i = 0; i < columns.length; i++) {
                lcolumns[i] = columns[i];
            }
        }
        return getTable(ctx, fileId, rowFrom, rowTo, lcolumns);
    }

    /**
     * Perform a query on the table
     *
     * @param ctx
     *            The {@link SecurityContext}
     * @param fileId
     *            The id of the {@link OriginalFile} which stores the table
     * @param condition
     *            The query string
     * @return A list of row indices matching the condition
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public long[] query(SecurityContext ctx, long fileId,
            String condition) throws DSOutOfServiceException, DSAccessException {
        return query(ctx, fileId, condition, 0, 0, 0);
    }

    /**
     * Perform a query on the table
     *
     * @param ctx
     *            The {@link SecurityContext}
     * @param fileId
     *            The id of the {@link OriginalFile} which stores the table
     * @param condition
     *            The query string
     * @param start
     *            The index of the first row to consider (optional, default: 0)
     * @param stop
     *            The index of the last+1 row to consider (optional, default:
     *            number of rows of the table)
     * @param step
     *            The stepping interval between the start and stop rows to
     *            consider (optional, default: no stepping)
     * @return A list of row indices matching the condition
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public long[] query(SecurityContext ctx, long fileId, String condition,
            long start, long stop, long step) throws DSOutOfServiceException,
            DSAccessException {
        if (fileId < 0)
            return new long[0];

        TablePrx table = null;
        try {
            OriginalFile file = new OriginalFileI(fileId, false);
            SharedResourcesPrx sr = gateway.getSharedResources(ctx);
            if (!sr.areTablesEnabled()) {
                throw new DSAccessException(
                        "Tables feature is not enabled on this server!");
            }

            table = sr.openTable(file);
            if (start < 0)
                start = 0;
            if (stop <= 0)
                stop = table.getNumberOfRows();
            if (step < 0)
                step = 0;

            if (start > stop)
                throw new IllegalArgumentException(
                        "start value can't be greater than stop value");

            if (start + step > stop)
                throw new IllegalArgumentException(
                        "step value is greater than the specified range");

            return table.getWhereList(condition, null, start, stop, step);
        } catch (Exception e) {
            handleException(this, e, "Could not load table data");
        } finally {
            if (table != null)
                try {
                    table.close();
                } catch (ServerError e) {
                    logError(this, "Could not close table", e);
                }
        }
        return new long[0];
    }

    /**
     * Load data from a table
     *
     * @param ctx
     *            The {@link SecurityContext}
     * @param fileId
     *            The id of the {@link OriginalFile} which stores the table
     * @param rows
     *            The rows to get
     * @return The specified data
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public TableData getTable(SecurityContext ctx, long fileId, List<Long> rows)
            throws DSOutOfServiceException, DSAccessException {
        long[] rowsArray = new long[rows.size()];
        for (int i = 0; i < rows.size(); i++)
            rowsArray[i] = rows.get(i);
        return getTable(ctx, fileId, rowsArray);
    }

    /**
     * Load data from a table
     *
     * @param ctx
     *            The {@link SecurityContext}
     * @param fileId
     *            The id of the {@link OriginalFile} which stores the table
     * @param rows
     *            The rows to get
     * @return The specified data
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public TableData getTable(SecurityContext ctx, long fileId, long... rows)
            throws DSOutOfServiceException, DSAccessException {
        if (fileId < 0)
            return null;

        TablePrx table = null;
        try {
            OriginalFile file = new OriginalFileI(fileId, false);
            SharedResourcesPrx sr = gateway.getSharedResources(ctx);
            if (!sr.areTablesEnabled()) {
                throw new DSAccessException(
                        "Tables feature is not enabled on this server!");
            }
            table = sr.openTable(file);

            Data data = table.readCoordinates(rows);

            Column[] cols = table.getHeaders();

            TableDataColumn[] header = new TableDataColumn[cols.length];
            for (int i = 0; i < cols.length; i++) {
                header[i] = new TableDataColumn(cols[i].name,
                        cols[i].description, i,
                        Object.class);
            }

            TablesFacilityHelper helper = new TablesFacilityHelper(this);
            helper.parseData(data, header);

            TableData result = new TableData(header, helper.getDataArray());
            result.setOriginalFileId(fileId);
            result.setNumberOfRows(helper.getNRows());
            return result;

        } catch (Exception e) {
            handleException(this, e, "Could not load table data");
        } finally {
            if (table != null)
                try {
                    table.close();
                } catch (ServerError e) {
                    logError(this, "Could not close table", e);
                }
        }
        return null;
    }

    /**
     * Load data from a table
     *
     * @param ctx
     *            The {@link SecurityContext}
     * @param fileId
     *            The id of the {@link OriginalFile} which stores the table
     * @param rowFrom
     *            The start row (inclusive)
     * @param rowTo
     *            The end row (inclusive) (can be <code>-1</code> in which case
     *            {@link TablesFacility#DEFAULT_MAX_ROWS_TO_FETCH} rows will be
     *            fetched)
     * @param columns
     *            The columns to take into account (can be left unspecified, in
     *            which case all columns will used)
     * @return The specified data
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public TableData getTable(SecurityContext ctx, long fileId, long rowFrom,
            long rowTo, long... columns) throws DSOutOfServiceException,
            DSAccessException {
        if (fileId < 0)
            return null;

        TablePrx table = null;
        try {
            OriginalFile file = new OriginalFileI(fileId, false);
            SharedResourcesPrx sr = gateway.getSharedResources(ctx);
            if (!sr.areTablesEnabled()) {
                throw new DSAccessException(
                        "Tables feature is not enabled on this server!");
            }

            table = sr.openTable(file);

            Column[] cols = table.getHeaders();

            if (columns == null || columns.length == 0) {
                columns = new long[cols.length];
                for (int i = 0; i < cols.length; i++) {
                    columns[i] = i;
                }
            }

            TableDataColumn[] header = new TableDataColumn[columns.length];
            for (int i = 0; i < columns.length; i++) {
                int columnIndex = (int) columns[i];
                header[i] = new TableDataColumn(cols[columnIndex].name,
                        cols[columnIndex].description, columnIndex,
                        Object.class);
            }

            if (table.getNumberOfRows() == 0)
                return new TableData(header, new Object[columns.length][0]);

            if (rowFrom < 0)
                rowFrom = 0;

            long maxRow = table.getNumberOfRows() - 1;

            if (rowTo < 0)
                rowTo = rowFrom + DEFAULT_MAX_ROWS_TO_FETCH;
            if (rowTo > maxRow)
                rowTo = maxRow;

            if (rowTo - rowFrom > Integer.MAX_VALUE)
                throw new Exception("Can't fetch more than "
                        + (Integer.MAX_VALUE - 1) + " rows at once.");

            TableData result = null;

            Data data = table.read(columns, rowFrom, rowTo + 1);

            TablesFacilityHelper helper = new TablesFacilityHelper(this);
            helper.parseData(data, header);

            result = new TableData(header, helper.getDataArray());
            result.setOffset(rowFrom);
            result.setOriginalFileId(fileId);
            result.setNumberOfRows(maxRow + 1);
            return result;
        } catch (Exception e) {
            handleException(this, e, "Could not load table data");
        } finally {
            if (table != null)
                try {
                    table.close();
                } catch (ServerError e) {
                    logError(this, "Could not close table", e);
                }
        }
        return null;
    }

    /**
     * Get all available tables for a the specified object
     *
     * @param ctx
     *            The {@link SecurityContext}
     * @param parent
     *            The {@link DataObject}
     * @return See above
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public Collection<FileAnnotationData> getAvailableTables(
            SecurityContext ctx, DataObject parent)
            throws DSOutOfServiceException, DSAccessException {
        if (!Pojos.hasID(parent))
            return Collections.emptyList();

        Collection<FileAnnotationData> result = new ArrayList<FileAnnotationData>();
        try {
            MetadataFacility mf = gateway.getFacility(MetadataFacility.class);

            List<Class<? extends AnnotationData>> types = new ArrayList<Class<? extends AnnotationData>>(
                    1);
            types.add(FileAnnotationData.class);

            List<AnnotationData> annos = mf.getAnnotations(ctx, parent, types,
                    null);
            for (AnnotationData anno : annos) {
                FileAnnotationData fad = (FileAnnotationData) anno;
                if (fad.getOriginalMimetype().equals(TABLES_MIMETYPE)) {
                    result.add(fad);
                }
            }

        } catch (Exception e) {
            handleException(this, e, "Could not load tables");
        }
        return result;
    }

    /**
     * Saves the (modified) {@link TableData} back to the server.
     * Note:
     * - Addition/Removal of columns/rows is not supported, only modification of
     *   the values.
     * - The size of Double/Float/Long arrays can't be changed!
     *
     * @param ctx
     *            The {@link SecurityContext}
     * @param data
     *            The {@link TableData} to save
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public void updateTable(SecurityContext ctx, TableData data)
            throws DSOutOfServiceException, DSAccessException {
        TablePrx table = null;
        try {
            if (data.getOriginalFileId() < 0)
                throw new IllegalArgumentException(
                        "This TableData object is not associated with a table yet, use addTable method instead.");

            OriginalFile file = new OriginalFileI(data.getOriginalFileId(),
                    false);
            SharedResourcesPrx sr = gateway.getSharedResources(ctx);
            if (!sr.areTablesEnabled()) {
                throw new DSAccessException(
                        "Tables feature is not enabled on this server!");
            }

            table = sr.openTable(file);

            long[] colIndex = new long[data.getColumns().length];
            for (int i = 0; i < data.getColumns().length; i++)
                colIndex[i] = data.getColumns()[i].getIndex();

            Data toUpdate = table.read(colIndex, data.getOffset(),
                    data.getOffset() + data.getData()[0].length);

            TablesFacilityHelper.updateData(toUpdate, data);

            table.update(toUpdate);
        } catch (Exception e) {
            handleException(this, e, "Could not udpate table");
        } finally {
            if (table != null)
                try {
                    table.close();
                } catch (ServerError e) {
                    logError(this, "Could not close table", e);
                }
        }
    }

}

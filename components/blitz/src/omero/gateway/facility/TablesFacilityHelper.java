/*
 * Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
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

import omero.IllegalArgumentException;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.ImageData;
import omero.gateway.model.MaskData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ROIData;
import omero.gateway.model.TableData;
import omero.gateway.model.TableDataColumn;
import omero.gateway.model.WellData;
import omero.gateway.model.WellSampleData;
import omero.grid.BoolColumn;
import omero.grid.Column;
import omero.grid.Data;
import omero.grid.DoubleArrayColumn;
import omero.grid.DoubleColumn;
import omero.grid.FileColumn;
import omero.grid.FloatArrayColumn;
import omero.grid.ImageColumn;
import omero.grid.LongArrayColumn;
import omero.grid.LongColumn;
import omero.grid.MaskColumn;
import omero.grid.PlateColumn;
import omero.grid.RoiColumn;
import omero.grid.StringColumn;
import omero.grid.WellColumn;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Plate;
import omero.model.PlateI;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.WellSample;
import omero.model.WellSampleI;

/**
 * Helper class which deals with the various conversions from omero.grid objects
 * into plain Java, respectively gateway.model objects
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class TablesFacilityHelper {

    /** The data array (after parsing omero.grid.Data) */
    private Object[][] dataArray;

    /** The number of columns */
    private int nCols;

    /** The number of rows */
    private int nRows;

    /** The omero.grid.Columns (after parsing TableData) */
    private Column[] gridColumns;

    /** Reference to the TablesFacility */
    private TablesFacility fac;
    
    /**
     * Create a new instance
     * @param fac Reference to the TablesFacility
     */
    TablesFacilityHelper(TablesFacility fac) {
        this.fac = fac;
    }

    /**
     * Turn a {@link TableData} into {@link Column}s; Get the result with
     * {@link #getGridColumns()}
     * 
     * @param data
     *            The TableData
     */
    void parseTableData(TableData data) {
        if (data == null)
            return;
        
        TableDataColumn[] columns = data.getColumns() != null ? data
                .getColumns() : new TableDataColumn[0];

        gridColumns = new Column[data.getColumns().length];
        for (int i = 0; i < data.getColumns().length; i++) {
            String cname = columns.length > i ? columns[i].getName() : "";
            String desc = columns.length > i ? columns[i].getDescription() : "";
            Object[] d = data.getData().length > i ? data.getData()[i]
                    : new Object[0];
            gridColumns[i] = createColumn(cname, desc,
                    data.getColumns()[i].getType(), d);
        }
    }

    /**
     * Turn an omero.grid.Data object into plain Java respectively gateway.model
     * objects and {@link TableDataColumn}s;
     * 
     * Get the result with {@link #getDataArray()}
     * 
     * @param data
     *            The omero.grid.Data object
     * @param header
     *            The header (which will be updated with the correct column
     *            types)
     */
    void parseData(Data data, TableDataColumn[] header) {
        if (data == null || header == null)
            return;
        if (header.length != data.columns.length)
            throw new IllegalArgumentException(
                    "Number of column definitions must match the number of columns of the table");

        nCols = data.columns.length;
        nRows = data.rowNumbers.length;

        dataArray = new Object[nCols][nRows];

        for (int i = 0; i < data.columns.length; i++) {
            Column col = data.columns[i];
            if (col instanceof BoolColumn) {
                Boolean[] rowData = new Boolean[nRows];
                boolean tableData[] = ((BoolColumn) col).values;
                for (int j = 0; j < nRows; j++)
                    rowData[j] = tableData[j];
                dataArray[i] = rowData;
                header[i].setType(Boolean.class);
            }
            if (col instanceof DoubleArrayColumn) {
                Double[][] rowData = new Double[nRows][];
                double tableData[][] = ((DoubleArrayColumn) col).values;
                for (int j = 0; j < nRows; j++) {
                    Double[] tmp = new Double[tableData[j].length];
                    for (int k = 0; k < tableData[j].length; k++) {
                        tmp[k] = tableData[j][k];
                    }
                    rowData[j] = tmp;
                }
                dataArray[i] = rowData;
                header[i].setType(Double[].class);
            }
            if (col instanceof DoubleColumn) {
                Double[] rowData = new Double[nRows];
                double tableData[] = ((DoubleColumn) col).values;
                for (int j = 0; j < nRows; j++)
                    rowData[j] = tableData[j];
                dataArray[i] = rowData;
                header[i].setType(Double.class);
            }
            if (col instanceof FileColumn) {
                FileAnnotationData[] rowData = new FileAnnotationData[nRows];
                long tableData[] = ((FileColumn) col).values;
                for (int j = 0; j < nRows; j++) {
                    FileAnnotation f = new FileAnnotationI(tableData[j], false);
                    rowData[j] = new FileAnnotationData(f);
                }
                dataArray[i] = rowData;
                header[i].setType(FileAnnotationData.class);
            }
            if (col instanceof FloatArrayColumn) {
                Float[][] rowData = new Float[nRows][];
                float tableData[][] = ((FloatArrayColumn) col).values;
                for (int j = 0; j < nRows; j++) {
                    Float[] tmp = new Float[tableData[j].length];
                    for (int k = 0; k < tableData[j].length; k++) {
                        tmp[k] = tableData[j][k];
                    }
                    rowData[j] = tmp;
                }
                dataArray[i] = rowData;
                header[i].setType(Float[].class);
            }
            if (col instanceof ImageColumn) {
                ImageData[] rowData = new ImageData[nRows];
                long tableData[] = ((ImageColumn) col).values;
                for (int j = 0; j < nRows; j++) {
                    Image im = new ImageI(tableData[j], false);
                    rowData[j] = new ImageData(im);
                }
                dataArray[i] = rowData;
                header[i].setType(ImageData.class);
            }
            if (col instanceof LongArrayColumn) {
                Long[][] rowData = new Long[nRows][];
                long tableData[][] = ((LongArrayColumn) col).values;
                for (int j = 0; j < nRows; j++) {
                    Long[] tmp = new Long[tableData[j].length];
                    for (int k = 0; k < tableData[j].length; k++) {
                        tmp[k] = tableData[j][k];
                    }
                    rowData[j] = tmp;
                }
                dataArray[i] = rowData;
                header[i].setType(Long[].class);
            }
            if (col instanceof LongColumn) {
                Long[] rowData = new Long[nRows];
                long tableData[] = ((LongColumn) col).values;
                for (int j = 0; j < nRows; j++)
                    rowData[j] = tableData[j];
                dataArray[i] = rowData;
                header[i].setType(Long.class);
            }
            if (col instanceof MaskColumn) {
                MaskColumn mc = ((MaskColumn) col);
                MaskData[] rowData = new MaskData[nRows];
                for (int j = 0; j < nRows; j++) {
                    MaskData md = new MaskData(mc.x[j], mc.y[j], mc.w[j],
                            mc.h[j], mc.bytes[j]);
                    md.setZ(mc.theZ[j]);
                    md.setT(mc.theT[j]);
                    rowData[j] = md;
                }
                dataArray[i] = rowData;
                header[i].setType(MaskData.class);
            }
            if (col instanceof PlateColumn) {
                PlateData[] rowData = new PlateData[nRows];
                long tableData[] = ((PlateColumn) col).values;
                for (int j = 0; j < nRows; j++) {
                    Plate p = new PlateI(tableData[j], false);
                    rowData[j] = new PlateData(p);
                }
                dataArray[i] = rowData;
                header[i].setType(PlateData.class);
            }
            if (col instanceof RoiColumn) {
                ROIData[] rowData = new ROIData[nRows];
                long tableData[] = ((RoiColumn) col).values;
                for (int j = 0; j < nRows; j++) {
                    Roi p = new RoiI(tableData[j], false);
                    rowData[j] = new ROIData(p);
                }
                dataArray[i] = rowData;
                header[i].setType(ROIData.class);
            }
            if (col instanceof StringColumn) {
                dataArray[i] = ((StringColumn) col).values;
                header[i].setType(String.class);
            }
            if (col instanceof WellColumn) {
                WellSampleData[] rowData = new WellSampleData[nRows];
                long tableData[] = ((WellColumn) col).values;
                for (int j = 0; j < nRows; j++) {
                    WellSample p = new WellSampleI(tableData[j], false);
                    rowData[j] = new WellSampleData(p);
                }
                dataArray[i] = rowData;
                header[i].setType(WellSampleData.class);
            }
        }
    }

    /**
     * Create a {@link Column} with the specified data
     * 
     * @param header
     *            The header (column name)
     * @param description
     *            Description
     * @param type
     *            The type of data
     * @param data
     *            The data
     * @return The {@link Column}
     */
    private Column createColumn(String header, String description,
            Class<?> type, Object[] data) {
        Column c = null;

        if (type.equals(Boolean.class)) {
            boolean[] d = new boolean[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = (Boolean) data[i];
            c = new BoolColumn(header, description, d);
        }

        else if (type.equals(Double[].class)) {
            double[][] d = new double[data.length][];
            int l = 0;
            for (int i = 0; i < data.length; i++) {
                Double[] src = (Double[]) data[i];
                double[] dst = new double[src.length];
                for (int j = 0; j < src.length; j++)
                    dst[j] = src[j];
                d[i] = dst;
                l = dst.length;
            }
            c = new DoubleArrayColumn(header, description, l, d);
        }

        else if (type.equals(Double.class)) {
            double[] d = new double[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = (Double) data[i];
            c = new DoubleColumn(header, description, d);
        }

        else if (type.equals(FileAnnotationData.class)) {
            long[] d = new long[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = ((FileAnnotationData) data[i]).getFileID();
            c = new FileColumn(header, description, d);
        }

        else if (type.equals(Float[].class)) {
            float[][] d = new float[data.length][];
            int l = 0;
            for (int i = 0; i < data.length; i++) {
                Float[] src = (Float[]) data[i];
                float[] dst = new float[src.length];
                for (int j = 0; j < src.length; j++)
                    dst[j] = src[j];
                d[i] = dst;
                l = dst.length;
            }
            c = new FloatArrayColumn(header, description, l, d);
        }

        else if (type.equals(ImageData.class)) {
            long[] d = new long[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = ((ImageData) data[i]).getId();
            c = new ImageColumn(header, description, d);
        }

        else if (type.equals(Long[].class)) {
            long[][] d = new long[data.length][];
            int l = 0;
            for (int i = 0; i < data.length; i++) {
                Long[] src = (Long[]) data[i];
                long[] dst = new long[src.length];
                for (int j = 0; j < src.length; j++)
                    dst[j] = src[j];
                d[i] = dst;
                l = dst.length;
            }
            c = new LongArrayColumn(header, description, l, d);
        }

        else if (type.equals(Long.class)) {
            long[] d = new long[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = (Long) data[i];
            c = new LongColumn(header, description, d);
        }

        else if (type.equals(MaskData.class)) {
            long[] imageId = new long[data.length];
            int[] theZ = new int[data.length];
            int[] theT = new int[data.length];
            double[] x = new double[data.length];
            double[] y = new double[data.length];
            double[] w = new double[data.length];
            double[] h = new double[data.length];
            byte[][] bytes = new byte[data.length][];

            for (int i = 0; i < data.length; i++) {
                MaskData md = (MaskData) data[i];
                // TODO: Where get the imageId from!?
                theZ[i] = md.getZ();
                theT[i] = md.getT();
                x[i] = md.getX();
                y[i] = md.getY();
                w[i] = md.getWidth();
                h[i] = md.getHeight();
                bytes[i] = md.getMask();
            }

            c = new MaskColumn(header, description, imageId, theZ, theT, x, y,
                    w, h, bytes);
        }

        else if (type.equals(PlateData.class)) {
            long[] d = new long[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = ((PlateData) data[i]).getId();
            c = new PlateColumn(header, description, d);
        }

        else if (type.equals(ROIData.class)) {
            long[] d = new long[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = ((ROIData) data[i]).getId();
            c = new RoiColumn(header, description, d);
        }

        else if (type.equals(String.class)) {
            String[] d = new String[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = (String) data[i];
            c = new StringColumn(header, description, Short.MAX_VALUE, d);
        }

        else if (type.equals(WellSampleData.class)) {
            long[] d = new long[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = ((WellSampleData) data[i]).getId();
            c = new WellColumn(header, description, d);
        }

        else if (type.equals(Object.class)) {
            fac.logWarn(this, "No concrete type specified for column '"
                    + header + "', using Object.toString()", null);
            String[] d = new String[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = data[i].toString();
            c = new StringColumn(header, description, Short.MAX_VALUE, d);
        }
        
        return c;
    }

    /**
     * Get the number of columns ({@link #parseData(Data, TableDataColumn[])} needs
     * to be called first)
     * 
     * @return See above
     */
    int getNCols() {
        return nCols;
    }

    /**
     * Get the number of rows ({@link #parseData(Data, TableDataColumn[])}
     * needs to be called first)
     * 
     * @return See above
     */
    int getNRows() {
        return nRows;
    }

    /**
     * Get the data array ({@link #parseData(Data, TableDataColumn[])} needs to
     * be called first)
     * 
     * @return See above
     */
    Object[][] getDataArray() {
        return dataArray;
    }

    /**
     * Get the grid columns; ({@link #parseTableData(TableData)} needs to be
     * called first)
     * 
     * @return See above
     */
    protected Column[] getGridColumns() {
        return gridColumns;
    }

    /**
     * Update omero.grid.Data with the provided {@link TableData} data.
     * Note:
     * - Size and data types must match!
     * - Size of Double/Float/Long arrays can't be changed!
     * 
     * @param toUpdate
     *            The omero.grid.Data object to update
     * @param data
     *            The new data
     */
    static void updateData(Data toUpdate, TableData data) {
        if (toUpdate.columns.length != data.getData().length)
            throw new IllegalArgumentException("Column size is different!");
        if (toUpdate.rowNumbers.length != data.getData()[0].length)
            throw new IllegalArgumentException("Row size is different!");

        for (int c = 0; c < data.getColumns().length; c++) {
            Column col = toUpdate.columns[c];

            for (int r = 0; r < data.getData()[0].length; r++) {
                if (col instanceof BoolColumn) {
                    if (!data.getColumns()[c].getType().equals(Boolean.class))
                        throw new IllegalArgumentException(
                                "Boolean type expected for column "
                                        + c
                                        + ", but is "
                                        + data.getColumns()[c].getType()
                                                .getSimpleName() + " !");
                    ((BoolColumn) col).values[r] = (Boolean) data.getData()[c][r];
                }
                if (col instanceof DoubleArrayColumn) {
                    if (!data.getColumns()[c].getType().equals(Double[].class))
                        throw new IllegalArgumentException(
                                "Double[] type expected for column "
                                        + c
                                        + ", but is "
                                        + data.getColumns()[c].getType()
                                                .getSimpleName() + " !");
                    if (((DoubleArrayColumn) col).size != ((Double[]) data
                            .getData()[c][r]).length)
                        throw new IllegalArgumentException(
                                "Can't change the length of the array");

                    Double[] a = (Double[]) data.getData()[c][r];
                    double[] b = new double[a.length];
                    for (int i = 0; i < a.length; i++)
                        b[i] = a[i].doubleValue();
                    ((DoubleArrayColumn) col).values[r] = b;
                }
                if (col instanceof DoubleColumn) {
                    if (!data.getColumns()[c].getType().equals(Double.class))
                        throw new IllegalArgumentException(
                                "Double type expected for column "
                                        + c
                                        + ", but is "
                                        + data.getColumns()[c].getType()
                                                .getSimpleName() + " !");
                    ((DoubleColumn) col).values[r] = (Double) data.getData()[c][r];
                }
                if (col instanceof FileColumn) {
                    if (!data.getColumns()[c].getType().equals(
                            FileAnnotationData.class))
                        throw new IllegalArgumentException(
                                "FileAnnotationData type expected for column "
                                        + c
                                        + ", but is "
                                        + data.getColumns()[c].getType()
                                                .getSimpleName() + " !");
                    ((FileColumn) col).values[r] = ((FileAnnotationData) data
                            .getData()[c][r]).getFileID();
                }
                if (col instanceof FloatArrayColumn) {
                    if (!data.getColumns()[c].getType().equals(Float[].class))
                        throw new IllegalArgumentException(
                                "Float[] type expected for column "
                                        + c
                                        + ", but is "
                                        + data.getColumns()[c].getType()
                                                .getSimpleName() + " !");
                    if (((FloatArrayColumn) col).size != ((Float[]) data
                            .getData()[c][r]).length)
                        throw new IllegalArgumentException(
                                "Can't change the length of the array");

                    Float[] a = (Float[]) data.getData()[c][r];
                    float[] b = new float[a.length];
                    for (int i = 0; i < a.length; i++)
                        b[i] = a[i].floatValue();
                    ((FloatArrayColumn) col).values[r] = b;
                }
                if (col instanceof ImageColumn) {
                    if (!data.getColumns()[c].getType().equals(ImageData.class))
                        throw new IllegalArgumentException(
                                "ImageData type expected for column "
                                        + c
                                        + ", but is "
                                        + data.getColumns()[c].getType()
                                                .getSimpleName() + " !");
                    ((ImageColumn) col).values[r] = ((ImageData) data.getData()[c][r])
                            .getId();
                }
                if (col instanceof LongArrayColumn) {
                    if (!data.getColumns()[c].getType().equals(Long[].class))
                        throw new IllegalArgumentException(
                                "Long[] type expected for column "
                                        + c
                                        + ", but is "
                                        + data.getColumns()[c].getType()
                                                .getSimpleName() + " !");
                    if (((LongArrayColumn) col).size != ((Long[]) data
                            .getData()[c][r]).length)
                        throw new IllegalArgumentException(
                                "Can't change the length of the array");

                    Long[] a = (Long[]) data.getData()[c][r];
                    long[] b = new long[a.length];
                    for (int i = 0; i < a.length; i++)
                        b[i] = a[i].longValue();
                    ((LongArrayColumn) col).values[r] = b;
                }
                if (col instanceof LongColumn) {
                    if (!data.getColumns()[c].getType().equals(Long.class))
                        throw new IllegalArgumentException(
                                "Long type expected for column "
                                        + c
                                        + ", but is "
                                        + data.getColumns()[c].getType()
                                                .getSimpleName() + " !");
                    ((LongColumn) col).values[r] = (Long) data.getData()[c][r];
                }
                if (col instanceof MaskColumn) {
                    if (!data.getColumns()[c].getType().equals(MaskData.class))
                        throw new IllegalArgumentException(
                                "MaskData type expected for column "
                                        + c
                                        + ", but is "
                                        + data.getColumns()[c].getType()
                                                .getSimpleName() + " !");
                    MaskData md = (MaskData) data.getData()[c][r];
                    ((MaskColumn) col).bytes[r] = md.getMask();
                    ((MaskColumn) col).x[r] = md.getX();
                    ((MaskColumn) col).y[r] = md.getY();
                    ((MaskColumn) col).w[r] = md.getWidth();
                    ((MaskColumn) col).h[r] = md.getHeight();
                    ((MaskColumn) col).theZ[r] = md.getZ();
                    ((MaskColumn) col).theT[r] = md.getT();
                }
                if (col instanceof PlateColumn) {
                    if (!data.getColumns()[c].getType().equals(PlateData.class))
                        throw new IllegalArgumentException(
                                "PlateData type expected for column "
                                        + c
                                        + ", but is "
                                        + data.getColumns()[c].getType()
                                                .getSimpleName() + " !");
                    ((PlateColumn) col).values[r] = ((PlateData) data.getData()[c][r])
                            .getId();
                }
                if (col instanceof RoiColumn) {
                    if (!data.getColumns()[c].getType().equals(ROIData.class))
                        throw new IllegalArgumentException(
                                "ROIData type expected for column "
                                        + c
                                        + ", but is "
                                        + data.getColumns()[c].getType()
                                                .getSimpleName() + " !");
                    ((RoiColumn) col).values[r] = ((ROIData) data.getData()[c][r])
                            .getId();
                }
                if (col instanceof StringColumn) {
                    if (!data.getColumns()[c].getType().equals(String.class))
                        throw new IllegalArgumentException(
                                "String type expected for column "
                                        + c
                                        + ", but is "
                                        + data.getColumns()[c].getType()
                                                .getSimpleName() + " !");
                    ((StringColumn) col).values[r] = (String) data.getData()[c][r];
                }
                if (col instanceof WellColumn) {
                    if (!data.getColumns()[c].getType().equals(WellData.class))
                        throw new IllegalArgumentException(
                                "WellData type expected for column "
                                        + c
                                        + ", but is "
                                        + data.getColumns()[c].getType()
                                                .getSimpleName() + " !");
                    ((WellColumn) col).values[r] = ((WellData) data.getData()[c][r])
                            .getId();
                }
            }
        }
    }
}

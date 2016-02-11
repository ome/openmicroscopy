/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
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
package omero.gateway.facility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.model.AnnotationData;
import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.FileData;
import omero.gateway.model.ImageData;
import omero.gateway.model.MaskData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ROIData;
import omero.gateway.model.TableData;
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
import omero.grid.SharedResourcesPrx;
import omero.grid.StringColumn;
import omero.grid.TablePrx;
import omero.grid.WellColumn;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.Plate;
import omero.model.PlateI;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.WellSample;
import omero.model.WellSampleI;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * {@link Facility} to interact with OMERO.tables
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class TablesFacility extends Facility {
    
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
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public void addTable(SecurityContext ctx, DataObject target, String name,
            TableData data) throws DSOutOfServiceException, DSAccessException {
        try {
            if (name == null)
                name = UUID.randomUUID().toString();

            String[] columnNames = data.getColumnNames() != null ? data
                    .getColumnNames() : new String[0];

            String[] description = data.getDescriptions() != null ? data
                    .getDescriptions() : new String[0];

            Column[] columns = new Column[data.getTypes().length];
            for (int i = 0; i < data.getTypes().length; i++) {
                String cname = columnNames.length > i ? columnNames[i] : "";
                String desc = description.length > i ? description[i] : "";
                Object[] d = data.getData().length > i ? data.getData()[i]
                        : new Object[0];
                columns[i] = createColumn(cname, desc, data.getTypes()[i], d);
            }

            SharedResourcesPrx sr = gateway.getSharedResources(ctx);
            long repId = sr.repositories().descriptions.get(0).getId()
                    .getValue();
            TablePrx table = sr.newTable(repId, name);
            table.initialize(columns);
            table.addData(columns);
            table.close();

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
        } catch (Exception e) {
            handleException(this, e, "Could not add table");
        }
    }

    /**
     * Load the data from a table
     * 
     * @param ctx
     *            The {@link SecurityContext}
     * @param fileId
     *            The if of the {@link OriginalFile} which stores the table
     * @return All data which the table contains
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public TableData getTable(SecurityContext ctx, long fileId)
            throws DSOutOfServiceException, DSAccessException {
        try {
            OriginalFile file = new OriginalFileI(fileId, false);
            SharedResourcesPrx sr = gateway.getSharedResources(ctx);
            TablePrx table = sr.openTable(file);

            Column[] columns = table.getHeaders();

            String[] header = new String[columns.length];
            String[] descriptions = new String[columns.length];
            Class<?>[] types = new Class<?>[columns.length];
            long[] colNumbers = new long[columns.length];
            for (int i = 0; i < columns.length; i++) {
                header[i] = columns[i].name;
                descriptions[i] = columns[i].description;
                colNumbers[i] = i;
            }

            if (table.getNumberOfRows() == 0) {
                for (int i = 0; i < columns.length; i++) {
                    types[i] = Object.class;
                }
                return new TableData(header, descriptions, types,
                        new Object[columns.length][0]);
            }

            int nRows = (int) table.getNumberOfRows();
            Object[][] dataArray = new Object[columns.length][nRows];
            Data data = table.read(colNumbers, 0, table.getNumberOfRows());
            for (int i = 0; i < data.columns.length; i++) {
                Column col = data.columns[i];
                if (col instanceof BoolColumn) {
                    Boolean[] rowData = new Boolean[nRows];
                    boolean tableData[] = ((BoolColumn) col).values;
                    for (int j = 0; j < nRows; j++)
                        rowData[j] = tableData[j];
                    dataArray[i] = rowData;
                    types[i] = Boolean.class;
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
                    types[i] = Double[].class;
                }
                if (col instanceof DoubleColumn) {
                    Double[] rowData = new Double[nRows];
                    double tableData[] = ((DoubleColumn) col).values;
                    for (int j = 0; j < nRows; j++)
                        rowData[j] = tableData[j];
                    dataArray[i] = rowData;
                    types[i] = Double.class;
                }
                if (col instanceof FileColumn) {
                    FileAnnotationData[] rowData = new FileAnnotationData[nRows];
                    long tableData[] = ((FileColumn) col).values;
                    for (int j = 0; j < nRows; j++) {
                        FileAnnotation f = new FileAnnotationI(tableData[j],
                                false);
                        rowData[j] = new FileAnnotationData(f);
                    }
                    dataArray[i] = rowData;
                    types[i] = FileAnnotationData.class;
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
                    types[i] = Float[].class;
                }
                if (col instanceof ImageColumn) {
                    ImageData[] rowData = new ImageData[nRows];
                    long tableData[] = ((ImageColumn) col).values;
                    for (int j = 0; j < nRows; j++) {
                        Image im = new ImageI(tableData[j], false);
                        rowData[j] = new ImageData(im);
                    }
                    dataArray[i] = rowData;
                    types[i] = ImageData.class;
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
                    types[i] = Long[].class;
                }
                if (col instanceof LongColumn) {
                    Long[] rowData = new Long[nRows];
                    long tableData[] = ((LongColumn) col).values;
                    for (int j = 0; j < nRows; j++)
                        rowData[j] = tableData[j];
                    dataArray[i] = rowData;
                    types[i] = Long.class;
                }
                if (col instanceof MaskColumn) {
                    MaskColumn mc = ((MaskColumn) col);
                    MaskData[] rowData = new MaskData[nRows];
                    for (int j = 0; j < nRows; j++) {
                        MaskData md = new MaskData(mc.x[j], mc.y[j], mc.w[j],
                                mc.h[j], mc.bytes[j]);
                        rowData[j] = md;
                    }
                    dataArray[i] = rowData;
                    types[i] = MaskData.class;
                }
                if (col instanceof PlateColumn) {
                    PlateData[] rowData = new PlateData[nRows];
                    long tableData[] = ((PlateColumn) col).values;
                    for (int j = 0; j < nRows; j++) {
                        Plate p = new PlateI(tableData[j], false);
                        rowData[j] = new PlateData(p);
                    }
                    dataArray[i] = rowData;
                    types[i] = PlateData.class;
                }
                if (col instanceof RoiColumn) {
                    ROIData[] rowData = new ROIData[nRows];
                    long tableData[] = ((RoiColumn) col).values;
                    for (int j = 0; j < nRows; j++) {
                        Roi p = new RoiI(tableData[j], false);
                        rowData[j] = new ROIData(p);
                    }
                    dataArray[i] = rowData;
                    types[i] = ROIData.class;
                }
                if (col instanceof StringColumn) {
                    String[] rowData = ((StringColumn) col).values;
                    dataArray[i] = rowData;
                    types[i] = String.class;
                }
                if (col instanceof WellColumn) {
                    WellSampleData[] rowData = new WellSampleData[nRows];
                    long tableData[] = ((WellColumn) col).values;
                    for (int j = 0; j < nRows; j++) {
                        WellSample p = new WellSampleI(tableData[j], false);
                        rowData[j] = new WellSampleData(p);
                    }
                    dataArray[i] = rowData;
                    types[i] = ROIData.class;
                }
            }
            return new TableData(header, descriptions, types, dataArray);
        } catch (Exception e) {
            handleException(this, e, "Could not load table data");
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
    public Collection<FileData> getAvailableTables(SecurityContext ctx,
            DataObject parent) throws DSOutOfServiceException,
            DSAccessException {
        Collection<FileData> result = new ArrayList<FileData>();
        try {
            MetadataFacility mf = gateway.getFacility(MetadataFacility.class);
            List<AnnotationData> annos = mf.getAnnotations(ctx, parent,
                    Arrays.asList(new Class[] { FileAnnotationData.class }),
                    null);
            for (AnnotationData anno : annos) {
                FileAnnotationData fanno = (FileAnnotationData) anno;
                if (fanno.isTablesFile())
                    result.add(new FileData(((FileAnnotation) fanno
                            .asAnnotation()).getFile()));
            }
        } catch (Exception e) {
            handleException(this, e, "Could not load tables");
        }
        return result;
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

        if (type.equals(Double[].class)) {
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

        if (type.equals(Double.class)) {
            double[] d = new double[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = (Double) data[i];
            c = new DoubleColumn(header, description, d);
        }

        if (type.equals(FileAnnotationData.class)) {
            long[] d = new long[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = ((FileAnnotationData) data[i]).getFileID();
            c = new FileColumn(header, description, d);
        }

        if (type.equals(Float[].class)) {
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

        if (type.equals(ImageData.class)) {
            long[] d = new long[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = ((ImageData) data[i]).getId();
            c = new ImageColumn(header, description, d);
        }

        if (type.equals(Long[].class)) {
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

        if (type.equals(Long.class)) {
            long[] d = new long[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = (Long) data[i];
            c = new LongColumn(header, description, d);
        }

        if (type.equals(MaskData.class)) {
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

        if (type.equals(PlateData.class)) {
            long[] d = new long[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = ((PlateData) data[i]).getId();
            c = new PlateColumn(header, description, d);
        }

        if (type.equals(ROIData.class)) {
            long[] d = new long[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = ((ROIData) data[i]).getId();
            c = new RoiColumn(header, description, d);
        }

        if (type.equals(String.class)) {
            String[] d = new String[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = (String) data[i];
            c = new StringColumn(header, description, Short.MAX_VALUE, d);
        }

        if (type.equals(WellSampleData.class)) {
            long[] d = new long[data.length];
            for (int i = 0; i < data.length; i++)
                d[i] = ((WellSampleData) data[i]).getId();
            c = new WellColumn(header, description, d);
        }

        return c;
    }
}

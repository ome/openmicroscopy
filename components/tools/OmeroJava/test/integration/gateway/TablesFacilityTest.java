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
package integration.gateway;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.UUID;

import omero.gateway.facility.TablesFacility;
import omero.gateway.model.DatasetData;
import omero.gateway.model.DataObject;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.ImageData;
import omero.gateway.model.MaskData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ROIData;
import omero.gateway.model.TableData;
import omero.gateway.model.TableDataColumn;
import omero.gateway.model.WellData;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TablesFacilityTest extends GatewayTest {

    // Note: Use @Test(dependsOnMethods = { "xyz" })
    //       to make sure tests run in the order they are listed!
    
    private static Random rand = new Random();

    private static final int nCols = 12;

    // must be > DEFAULT_MAX_ROWS_TO_FETCH
    // otherwise testThreshold() is useless
    private static final int nRows = 2000;

    private DatasetData ds;

    private TableData original;

    private final String searchForThis = "searchForThis";
    
    private final long searchForThisResult = 123456789l;

    @Override
    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {
        super.setUp();

        DatasetData ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        this.ds = (DatasetData) datamanagerFacility.createDataset(rootCtx, ds,
                null);
    }

    @Test(timeOut = 60000)
    public void testAddTable() throws Exception {
        Class<?>[] types = new Class<?>[] { String.class, Long.class,
                Double.class, Double[].class, Float[].class,
                Boolean.class, ImageData.class, PlateData.class,
                WellData.class,  FileAnnotationData.class,
                ROIData.class, MaskData.class };
        TableDataColumn[] header = new TableDataColumn[nCols];
        for (int i = 0; i < header.length; i++) {
            header[i] = new TableDataColumn("column" + i, i, types[i]);
        }

        Object[][] data = new Object[header.length][nRows];
        for (int c = 0; c < nCols; c++) {
            Object[] column = new Object[nRows];
            Class<?> type = header[c].getType();
            for (int r = 0; r < nRows; r++) {
                if (type.equals(String.class)) {
                    if (r < 2)
                        column[r] = searchForThis;
                    else
                        column[r] = "" + rand.nextInt();
                } else if (type.equals(Long.class)) {
                    if (r < 2)
                        column[r] = searchForThisResult;
                    else
                        column[r] = rand.nextLong();
                } else if (type.equals(Double.class)) {
                    column[r] = rand.nextDouble();
                } else if (type.equals(Double[].class)) {
                    Double[] d = new Double[4];
                    for (int i = 0; i < 4; i++)
                        d[i] = rand.nextDouble();
                    column[r] = d;
                } else if (type.equals(Float[].class)) {
                    Float[] d = new Float[4];
                    for (int i = 0; i < 4; i++)
                        d[i] = rand.nextFloat();
                    column[r] = d;
                } else if (type.equals(Boolean.class)) {
                    column[r] = Boolean.TRUE;
                } else if (type.equals(ImageData.class)) {
                    ImageData img = new ImageData();
                    img.setName("" + rand.nextInt());
                    column[r] = img;
                } else if (type.equals(WellData.class)) {
                    column[r] = new WellData();
                } else if (type.equals(PlateData.class)) {
                    PlateData plate = new PlateData();
                    plate.setName("" + rand.nextInt());
                    column[r] = plate;
                } else if (type.equals(ROIData.class)) {
                    column[r] = new ROIData(); 
                } else if (type.equals(MaskData.class)) {
                    column[r] = new MaskData();
                } else if (type.equals(FileAnnotationData.class)) {
                    File f = File.createTempFile("Annotation", ".tmp");
                    f.deleteOnExit();
                    column[r] = new FileAnnotationData(f);
                }
            }
            data[c] = column;
        }

        original = new TableData(header, data);
        TableData stored = tablesFacility.addTable(rootCtx, ds, "Table",
                original);
        Assert.assertEquals(stored.getNumberOfRows(), nRows);
        original.setOriginalFileId(stored.getOriginalFileId());
    }

    @Test(dependsOnMethods = { "testAddTable" })
    public void testGetTableInfo() throws Exception {
        TableData info = tablesFacility.getTableInfo(rootCtx,
                original.getOriginalFileId());
        Assert.assertEquals(info.getNumberOfRows(), nRows);
        Assert.assertEquals(info.getColumns(), original.getColumns());
    }

    @Test(dependsOnMethods = { "testGetTableInfo" })
    public void testSearch() throws Exception {
        long[] rows = tablesFacility.query(rootCtx,
                original.getOriginalFileId(), "(column0=='" + searchForThis
                        + "')");
        Assert.assertEquals(rows.length, 2);

        TableData td = tablesFacility.getTable(rootCtx,
                original.getOriginalFileId(), rows);
        Assert.assertEquals(td.getNumberOfRows(), 2);

        TableDataColumn[] cols = td.getColumns();
        Object[][] data = td.getData();
        for (int col = 0; col < data.length; col++)
            for (int row = 0; row < data[col].length; row++) {
                if (col == 0)
                    Assert.assertEquals(data[col][row], searchForThis);
                if (cols[col].getType() == Long.class)
                    Assert.assertEquals(data[col][row], searchForThisResult);
            }
    }

    @Test(dependsOnMethods = { "testSearch" })
    public void testInvalidParams() throws Exception {
        try {
            // start > stop
            tablesFacility.query(rootCtx, original.getOriginalFileId(),
                    "(column0=='" + searchForThis + "')", 10, 5, 0);
            Assert.fail("Invalid parameters, an exception should have been thrown.");
        } catch (Exception e) {
            // expected
        }

        try {
            // step > range
            tablesFacility.query(rootCtx, original.getOriginalFileId(),
                    "(column0=='" + searchForThis + "')", 0, 5, 10);
            Assert.fail("Invalid parameters, an exception should have been thrown.");
        } catch (Exception e) {
            // expected
        }
        
        try {
            // Table column type is not supported
            TableDataColumn[] columns = new TableDataColumn[1];
            columns[0] = new TableDataColumn("column1", 0, ProjectData.class);
            Object[][] data = new Object[1][1];
            data[0][0] = new ProjectData();
            TableData td = new TableData(columns, data);
            tablesFacility.addTable(rootCtx, ds, "invalid", td);
            Assert.fail("Invalid column type, an exception should have been thrown.");
        } catch (Exception e) {
            // expected
        }
    }

    @Test(dependsOnMethods = { "testInvalidParams" })
    public void testObjectColumnType() throws Exception {
        // Create an object where the table can be attached to
        // (can't use this.ds to not interfere with other tests)
        DatasetData attachTo = new DatasetData();
        attachTo.setName(UUID.randomUUID().toString());
        attachTo = (DatasetData) datamanagerFacility.createDataset(rootCtx, attachTo,
                null);
        
        // Create any object which gets added to the table
        ProjectData proj = new ProjectData();
        proj.setName("test");
        
        // If no concrete type is specified, just `Object.class`,
        // the table should still be created but with a String
        // column and the `Object.toString()` value (also a warning
        // will be logged).
        TableDataColumn[] columns = new TableDataColumn[1];
        columns[0] = new TableDataColumn("column1", 0, Object.class);
        Object[][] data = new Object[1][1];
        data[0][0] = proj;
        
        TableData td = new TableData(columns, data);
        td = tablesFacility.addTable(rootCtx, attachTo, "Object column test", td);
        
        TableData td2 = tablesFacility.getTable(rootCtx, td.getOriginalFileId());
        // check that the data has been saved as String column
        Assert.assertEquals(td2.getColumns()[0].getType(), String.class);
        Assert.assertEquals(td2.getNumberOfRows(), 1);
    }

    @Test(dependsOnMethods = { "testObjectColumnType" })
    public void testGetAvailableTables() throws Exception {
        Collection<FileAnnotationData> tablesFiles = tablesFacility
                .getAvailableTables(rootCtx, ds);
        Assert.assertEquals(1, tablesFiles.size());
        Assert.assertEquals(tablesFiles.iterator().next().getFileID(),
                original.getOriginalFileId());
    }

    @Test(dependsOnMethods = { "testGetAvailableTables" }, invocationCount = 10)
    /**
     * Read a random subset from the table and compare to the original data
     * 
     * @throws Exception
     */
    public void testReadTable() throws Exception {
        Object[][] origData = original.getData();

        TableData info = tablesFacility.getTableInfo(rootCtx,
                original.getOriginalFileId());
        int rows = (int) info.getNumberOfRows();
        int cols = info.getColumns().length;

        // request maximum of 100 rows
        int rowFrom = rand.nextInt(rows - 100);
        int rowTo = rowFrom + rand.nextInt(100);
        int[] columns = new int[rand.nextInt(cols)];

        for (int x = 0; x < columns.length; x++)
            columns[x] = rand.nextInt(cols);

        TableData td2 = tablesFacility.getTable(rootCtx,
                original.getOriginalFileId(), rowFrom, rowTo, columns);

        Object[][] data2 = td2.getData();

        for (int r = rowFrom; r < rowTo; r++) {
            for (int c = 0; c < columns.length; c++) {
                int index = columns[c];
                Class<?> type = info.getColumns()[index].getType();
                if (type.equals(String.class)) {
                    Assert.assertEquals(
                            (String) data2[c][r - (int) td2.getOffset()],
                            (String) origData[index][r]);
                } else if (type.equals(Long.class)) {
                    Assert.assertEquals(
                            (Long) data2[c][r - (int) td2.getOffset()],
                            (Long) origData[index][r]);
                } else if (type.equals(Double.class)) {
                    Assert.assertEquals(
                            (Double) data2[c][r - (int) td2.getOffset()],
                            (Double) origData[index][r]);
                } else if (type.equals(Double[].class)) {
                    Assert.assertEquals(
                            (Double[]) data2[c][r - (int) td2.getOffset()],
                            (Double[]) origData[index][r]);
                } else if (type.equals(Boolean.class)) {
                    Assert.assertEquals(
                            (Boolean) data2[c][r - (int) td2.getOffset()],
                            (Boolean) origData[index][r]);
                } else if (type.equals(Float[].class)) {
                    Assert.assertEquals(
                            (Float[]) data2[c][r - (int) td2.getOffset()],
                            (Float[]) origData[index][r]);
                } else if (type.equals(ImageData.class) ||
                           type.equals(PlateData.class) ||
                           type.equals(WellData.class) ||
                           type.equals(ROIData.class) ||
                           type.equals(FileAnnotationData.class)) {
                    DataObject d1 = (DataObject) data2[c][r - (int) td2.getOffset()];
                    DataObject d2 = (DataObject) origData[index][r];
                    Assert.assertEquals(d1.getId(), d2.getId());
                }
            }
        }
    }

    @Test(dependsOnMethods = { "testReadTable" })
    /**
     * Test that unspecified requests are limited to TablesFacility.DEFAULT_MAX_ROWS_TO_FETCH rows
     * @throws Exception
     */
    public void testThreshold() throws Exception {
        TableData td = tablesFacility.getTable(rootCtx,
                original.getOriginalFileId());
        Assert.assertTrue(
                td.getNumberOfRows() > TablesFacility.DEFAULT_MAX_ROWS_TO_FETCH,
                "Test setup failure, nRows must be greater than DEFAULT_MAX_ROWS_TO_FETCH");
        Assert.assertEquals(td.getData()[0].length,
                TablesFacility.DEFAULT_MAX_ROWS_TO_FETCH);
    }
    
    @Test(dependsOnMethods = { "testThreshold" })
    public void testUpdateTable() throws Exception {
        // modify values for row 10 to 20, columns 5, 6 and 7
        TableData td = tablesFacility.getTable(rootCtx,
                original.getOriginalFileId(), 10, 20);

        for (int c = 0; c < td.getColumns().length; c++) {
            Class<?> type = td.getColumns()[c].getType();
            for (int r = 0; r < td.getData()[0].length; r++) {
                if (type.equals(String.class)) {
                    td.getData()[c][r] = "newValue";
                } else if (type.equals(Long.class)) {
                    td.getData()[c][r] = new Long(9999);
                } else if (type.equals(Double.class)) {
                    td.getData()[c][r] = new Double(9.999);
                } else if (type.equals(Double[].class)) {
                    td.getData()[c][r] = new Double[] { 6.666, 7.777, 8.888,
                            9.999 };
                } else if (type.equals(Float[].class)) {
                    td.getData()[c][r] = new Float[] { 6.666f, 7.777f, 8.888f,
                            9.999f };
                } else if (type.equals(Boolean.class)) {
                    td.getData()[c][r] = Boolean.FALSE;
                } else if (type.equals(ImageData.class)) {
                    td.getData()[c][r] = new ImageData();
                } else if (type.equals(WellData.class)) {
                    td.getData()[c][r] = new WellData();
                } else if (type.equals(ROIData.class)) {
                    td.getData()[c][r] = new ROIData();
                } else if (type.equals(MaskData.class)) {
                    td.getData()[c][r] = new MaskData();
                } else if (type.equals(FileAnnotationData.class)) {
                    File f = File.createTempFile("AnnotationNew", ".tmp");
                    f.deleteOnExit();
                    td.getData()[c][r] = new FileAnnotationData(f);
                }
            }
        }

        tablesFacility.updateTable(rootCtx, td);

        TableData td2 = tablesFacility.getTable(rootCtx,
                original.getOriginalFileId(), 0, 30);

        // check that the modified values were saved,
        // while the other values were not modified.
        for (int c = 0; c < td2.getColumns().length; c++) {
            for (int r = 0; r < td2.getData()[0].length; r++) {
                if (r < 10 || r > 20) {
                    checkData(td2.getData()[c][r], original.getData()[c][r]);
                } else {
                    checkData(td2.getData()[c][r], td.getData()[c][r - 10]);
                }
            }
        }
    }

    /** 
     * Compare the values.
     * @param newData The data to compare.
     * @param originalData The data to compare.
     */
    private void checkData(Object newData, Object originalData) {
        if (newData instanceof DataObject) {
            DataObject d1 = (DataObject) newData;
            DataObject d2 = (DataObject) originalData;
            Assert.assertEquals(d1.getId(), d2.getId());
        } else {
            Assert.assertEquals(newData, originalData);
        }

    }
}

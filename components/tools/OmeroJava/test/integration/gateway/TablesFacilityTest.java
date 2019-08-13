/*
 * Copyright (C) 2016-2021 University of Dundee & Open Microscopy Environment.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import omero.api.IQueryPrx;
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

import omero.model.OriginalFile;

import omero.model.FileAnnotation;
import omero.sys.ParametersI;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TablesFacilityTest extends GatewayTest {

    // Note: Use @Test(dependsOnMethods = { "xyz" })
    //       to make sure tests run in the order they are listed!
    
    private static Random rand = new Random();

    // must be > DEFAULT_MAX_ROWS_TO_FETCH
    // otherwise testThreshold() is useless
    private static final int nRows = 2000;

    private DatasetData ds;

    private TableData original;

    private Object[][] origData;

    private final String searchForThis = "searchForThis";
    
    private final long searchForThisResult = 123456789l;

    private final String ns = "some Namespace";

    @Override
    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {
        super.setUp();

        DatasetData ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        this.ds = (DatasetData) datamanagerFacility.createDataset(rootCtx, ds,
                null);
    }

    @Test(timeOut = 600000)
    public void testAddTable() throws Exception {
        Class<?>[] types = new Class<?>[] { String.class, Long.class,
                Double.class, Double[].class, Float[].class,
                Boolean.class, ImageData.class, // DatasetData.class,
                PlateData.class, WellData.class, FileAnnotationData.class, //  OriginalFile.class,
                ROIData.class};//, MaskData.class };
        int nCols = types.length;

        TableDataColumn[] header = new TableDataColumn[nCols];
        for (int i = 0; i < nCols; i++) {
            header[i] = new TableDataColumn("column" + i, i, types[i]);
        }

        ImageData img = createImage();
        ProjectData pr = createProject(rootCtx);
        DatasetData dat = createDataset(rootCtx, pr);
        PlateData plate = createPlateWithWells();
        ArrayList tmp = new ArrayList<ROIData>();
        tmp.add(createRectangleROI(0, 0, 10, 10, img.getId()));
        ROIData roi = (ROIData) roiFacility.saveROIs(rootCtx, img.getId(), tmp).iterator().next();
        File f = createFile(1);
        FileAnnotationData fa = datamanagerFacility.attachFile(rootCtx, f, null,
                null, null, pr).get();
        MaskData m = new MaskData(0, 0, 5, 5,new byte[] {0,0,0,0,0 ,0,1,1,1,0,0, 1,1,1,0, 0,1,1,1,0, 0,0,0,0,0});
        m.setImage(img);
        ROIData rd = new ROIData();
        rd.addShapeData(m);
        tmp.clear();
        tmp.add(rd);
        roiFacility.saveROIs(rootCtx, img.getId(), tmp).iterator().next();

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
                    column[r] = rand.nextBoolean();
                } else if (type.equals(ImageData.class)) {
                    column[r] = img;
                } else if (type.equals(DatasetData.class)) {
                    column[r] = dat;
                } else if (type.equals(WellData.class)) {
                    column[r] = new WellData(plate.asPlate().copyWells().get(0));
                } else if (type.equals(PlateData.class)) {
                    column[r] = plate;
                } else if (type.equals(ROIData.class)) {
                    column[r] = roi;
                } else if (type.equals(MaskData.class)) {
                    column[r] = m;
                } else if (type.equals(OriginalFile.class)) {
                    column[r] = fa;
                }
                else if (type.equals(FileAnnotationData.class)) {
                    column[r] = fa;
                }
            }
            data[c] = column;
        }

        original = new TableData(header, data);
        TableData stored = tablesFacility.addTable(rootCtx, ds, "Table", ns, original);
        origData = original.getData();
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

    @Test(dependsOnMethods = { "testAddTable" })
    public void testNameSpace() throws Exception {
        IQueryPrx qs = gw.getQueryService(rootCtx);
        ParametersI params = new ParametersI();
        params.add("fid", omero.rtypes.rlong(original.getOriginalFileId()));
        String query = "SELECT a from FileAnnotation a where a.file.id = :fid";
        FileAnnotationData fa = new FileAnnotationData((FileAnnotation) qs.findByQuery(query, params));
        Assert.assertEquals(fa.getNameSpace(), ns);
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

    @Test(dependsOnMethods = { "testAddTable" })
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

    @Test(dependsOnMethods = { "testAddTable" })
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

    @Test(dependsOnMethods = { "testAddTable" })
    public void testGetAvailableTables() throws Exception {
        Collection<FileAnnotationData> tablesFiles = tablesFacility
                .getAvailableTables(rootCtx, ds);
        Assert.assertEquals(1, tablesFiles.size());
        Assert.assertEquals(tablesFiles.iterator().next().getFileID(),
                original.getOriginalFileId());
    }

    @Test
    public void testMaskData() throws Exception {
        ImageData img = createImage();
        List<TableDataColumn> cols = new ArrayList<>();
        cols.add(new TableDataColumn("first", "blah", 0, MaskData.class));
        List<List<Object>> data = new ArrayList<>();
        MaskData m = new MaskData();
        m.setImage(img);
        m.setX(0);
        m.setY(0);
        m.setWidth(5);
        m.setHeight(5);
        m.setMask(new int[]{0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0});
        data.add(Collections.singletonList(m));

        TableData td = new TableData(cols, data);
        td = tablesFacility.addTable(rootCtx, img, "Table", td);
        Assert.assertTrue(td.getOriginalFileId() > -1);

        TableData td2 = tablesFacility.getTable(rootCtx, td.getOriginalFileId());
        Assert.assertEquals(td.getNumberOfRows(), 1);
        MaskData m2 = (MaskData) td2.getData()[0][0];
        Assert.assertEquals(m2.getMask().length, m.getMask().length);
        for(int i=0; i<m.getMask().length; i++) {
            Assert.assertEquals(m2.getMask()[i], m.getMask()[i]);
        }
    }

    @Test(dependsOnMethods = { "testAddTable" }, invocationCount = 5)
    /**
     * Read a random subset from the table and compare to the original data
     * 
     * @throws Exception
     */
    public void testReadTable() throws Exception {
        TableData info = tablesFacility.getTableInfo(rootCtx,
                original.getOriginalFileId());
        int rows = (int) info.getNumberOfRows();
        int cols = info.getColumns().length;

        // request maximum of 100 rows
        int rowFrom = rand.nextInt(rows - 100);
        int rowTo = rowFrom + rand.nextInt(100);
        int nCol = rand.nextInt(cols);
        ArrayList<Integer> tmp = new ArrayList<>(nCol);
        while (tmp.size() < nCol) {
            int t =  rand.nextInt(cols);
            while (tmp.contains(t))
                t =  rand.nextInt(cols);
            tmp.add(t);
        }
        int[] columns = new int[nCol];
        for (int i=0; i < nCol; i++)
            columns[i] = tmp.get(i);

        System.out.println("Request rows "+rowFrom+" to "+rowTo+"; columns "+Arrays.toString(columns));
        TableData td2 = tablesFacility.getTable(rootCtx,
                original.getOriginalFileId(), rowFrom, rowTo, columns);

        Object[][] data = td2.getData();

        for (int r = rowFrom; r < rowTo; r++) {
            for (int c = 0; c < columns.length; c++) {
                int index = columns[c];
                Class<?> type = info.getColumns()[index].getType();
                if (type.equals(String.class)) {
                    Assert.assertEquals(
                            (String) data[c][r - (int) td2.getOffset()],
                            (String) origData[index][r]);
                } else if (type.equals(Long.class)) {
                    Assert.assertEquals(
                            (Long) data[c][r - (int) td2.getOffset()],
                            (Long) origData[index][r]);
                } else if (type.equals(Double.class)) {
                    Assert.assertEquals(
                            (Double) data[c][r - (int) td2.getOffset()],
                            (Double) origData[index][r]);
                } else if (type.equals(Double[].class)) {
                    Assert.assertEquals(
                            (Double[]) data[c][r - (int) td2.getOffset()],
                            (Double[]) origData[index][r]);
                } else if (type.equals(Boolean.class)) {
                    Assert.assertEquals(
                            (Boolean) data[c][r - (int) td2.getOffset()],
                            (Boolean) origData[index][r]);
                } else if (type.equals(Float[].class)) {
                    Assert.assertEquals(
                            (Float[]) data[c][r - (int) td2.getOffset()],
                            (Float[]) origData[index][r]);
                } else if (type.equals(ImageData.class) ||
                           type.equals(PlateData.class) ||
                           type.equals(WellData.class) ||
                           type.equals(DatasetData.class) ||
                           type.equals(ROIData.class)) {
                    DataObject d1 = (DataObject) data[c][r - (int) td2.getOffset()];
                    DataObject d2 = (DataObject) origData[index][r];
                    Assert.assertEquals(d1.getId(), d2.getId());
                }
                else if (type.equals(OriginalFile.class)) {
                    OriginalFile d1 = (OriginalFile) data[c][r - (int) td2.getOffset()];
                    OriginalFile d2 = (OriginalFile) origData[index][r];
                    Assert.assertEquals(d1.getId(), d2.getId());
                }
                else if (type.equals(FileAnnotationData.class)) {
                    FileAnnotationData d1 = (FileAnnotationData) data[c][r - (int) td2.getOffset()];
                    FileAnnotationData d2 = (FileAnnotationData) origData[index][r];
                    Assert.assertEquals(d1.getFileID(), d2.getFileID());
                }
                else if (type.equals(MaskData.class)) {
                    MaskData d1 = (MaskData) data[c][r - (int) td2.getOffset()];
                    MaskData d2 = (MaskData) origData[index][r];
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

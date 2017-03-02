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

import java.util.Collection;
import java.util.Random;
import java.util.UUID;

import omero.gateway.facility.TablesFacility;
import omero.gateway.model.DatasetData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.TableData;
import omero.gateway.model.TableDataColumn;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TablesFacilityTest extends GatewayTest {

    private static Random rand = new Random();

    private static final int nCols = 10;

    private static final int nRows = 100000;

    private DatasetData ds;

    private TableData original;

    @Override
    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {
        super.setUp();

        DatasetData ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        this.ds = (DatasetData) datamanagerFacility.createDataset(rootCtx, ds,
                null);
    }

    @Test(timeOut = 300000)
    public void testAddTable() throws Exception {
        Class<?>[] types = new Class<?>[] { String.class, Long.class,
                Double.class, Double[].class };
        TableDataColumn[] header = new TableDataColumn[nCols];
        for (int i = 0; i < header.length; i++) {
            header[i] = new TableDataColumn("column-" + i, i,
                    types[rand.nextInt(types.length)]);
        }

        Object[][] data = new Object[header.length][nRows];
        for (int c = 0; c < nCols; c++) {
            Object[] column = new Object[nRows];
            Class<?> type = header[c].getType();
            for (int r = 0; r < nRows; r++) {
                if (type.equals(String.class)) {
                    column[r] = "" + rand.nextInt();
                } else if (type.equals(Long.class)) {
                    column[r] = rand.nextLong();
                } else if (type.equals(Double.class)) {
                    column[r] = rand.nextDouble();
                } else if (type.equals(Double[].class)) {
                    Double[] d = new Double[4];
                    for (int i = 0; i < 4; i++)
                        d[i] = rand.nextDouble();
                    column[r] = d;
                }
            }
            data[c] = column;
        }

        original = new TableData(header, data);
        long start = System.currentTimeMillis();
        TableData stored = tablesFacility.addTable(rootCtx, ds, "Big Table",
                original);
        long duration = System.currentTimeMillis() - start;

        original.setOriginalFileId(stored.getOriginalFileId());

        System.out.println("Storing table with " + (nCols * nRows)
                + " entries took " + duration + " ms");
    }

    @Test(dependsOnMethods = { "testAddTable" })
    public void testGetTableInfo() throws Exception {
        TableData info = tablesFacility.getTableInfo(rootCtx,
                original.getOriginalFileId());
        Assert.assertEquals(info.getNumberOfRows(), nRows);
        Assert.assertEquals(info.getColumns(), original.getColumns());
    }

    @Test(dependsOnMethods = { "testAddTable" })
    public void testGetAvailableTables() throws Exception {
        Collection<FileAnnotationData> tablesFiles = tablesFacility
                .getAvailableTables(rootCtx, ds);
        Assert.assertEquals(1, tablesFiles.size());
        Assert.assertEquals(tablesFiles.iterator().next().getFileID(),
                original.getOriginalFileId());
    }

    @Test(dependsOnMethods = { "testAddTable" }, invocationCount = 10)
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

        // request maximum of 1000 rows
        int rowFrom = rand.nextInt(rows - 1000);
        int rowTo = rowFrom + rand.nextInt(1000);
        int[] columns = new int[rand.nextInt(cols)];

        for (int x = 0; x < columns.length; x++)
            columns[x] = rand.nextInt(cols);

        long start = System.currentTimeMillis();
        TableData td2 = tablesFacility.getTable(rootCtx,
                original.getOriginalFileId(), rowFrom, rowTo, columns);
        System.out.println("Requesting random subset of size: "
                + ((rowTo - rowFrom) * columns.length) + " entries, took "
                + (System.currentTimeMillis() - start) + " ms");

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
                }
            }
        }
    }

    @Test(dependsOnMethods = { "testAddTable" })
    /**
     * Test that unspecified requests are limited to TablesFacility.DEFAULT_MAX_ROWS_TO_FETCH rows
     * @throws Exception
     */
    public void testThreshold() throws Exception {
        TableData td = tablesFacility.getTable(rootCtx,
                original.getOriginalFileId());
        Assert.assertEquals(td.getData()[0].length,
                TablesFacility.DEFAULT_MAX_ROWS_TO_FETCH);
    }

}

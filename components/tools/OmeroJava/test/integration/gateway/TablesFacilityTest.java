package integration.gateway;

import java.util.UUID;

import omero.gateway.model.DatasetData;
import omero.gateway.model.FileData;
import omero.gateway.model.TableData;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TablesFacilityTest extends GatewayTest {

    private DatasetData ds;

    private TableData data;

    @Override
    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {
        super.setUp();
        initData();
    }

    @Test
    public void testAddTable() throws Exception {
        // just check if it doesn't throw an exception;
        // verification is done by testGetTable()
        tablesFacility.addTable(rootCtx, ds, null, data);
    }

    @Test(dependsOnMethods = { "testAddTable" })
    public void testGetTable() throws Exception {
        FileData tablesFile = tablesFacility.getAvailableTables(rootCtx, ds)
                .iterator().next();
        TableData data2 = tablesFacility.getTable(rootCtx, tablesFile.getId());
        Assert.assertEquals(data, data2,
                "The tables data retrieved doesn't match the original");
    }

    private void initData() throws Exception {
        DatasetData ds = new DatasetData();
        ds.setName(UUID.randomUUID().toString());
        this.ds = (DatasetData) datamanagerFacility.createDataset(rootCtx, ds,
                null);

        String[] header = new String[] { "column0", "column1", "column2",
                "column3" };

        Class<?>[] types = new Class<?>[] { String.class, Long.class,
                Double.class, Double[].class };

        Object[][] objs = new Object[4][3];
        objs[0] = new Object[] { new String("test0"), new String("test1"),
                new String("test2") };
        objs[1] = new Object[] { new Long(0), new Long(1), new Long(1) };
        objs[2] = new Object[] { new Double(0.0), new Double(1.0),
                new Double(2.0) };
        objs[3] = new Object[] { new Double[] { 0.0, 1.0, 2.0 },
                new Double[] { 0.1, 1.1, 2.1 }, new Double[] { 0.2, 1.2, 2.2 } };

        this.data = new TableData(header, null, types, objs);

    }
}

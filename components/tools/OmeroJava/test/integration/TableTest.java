package integration;

import java.util.Date;
import java.util.UUID;

import omero.ApiUsageException;
import omero.ServerError;
import omero.grid.Column;
import omero.grid.LongColumn;
import omero.grid.StringColumn;
import omero.grid.Data;
import omero.grid.TablePrx;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

public class TableTest {

    protected int DEFAULT_BUFFER_SIZE = 3;
    
    protected int UID_COLUMN = 0;
    protected int LONG_COLUMN = 1;
    protected int STRING_COLUMN = 2;
    
    protected long[] ColNumbers = {UID_COLUMN, LONG_COLUMN, STRING_COLUMN};
    
    protected omero.client client = null;
    protected omero.api.ServiceFactoryPrx  sf = null;
    protected omero.api.IQueryPrx iQuery = null;
    protected omero.api.IAdminPrx iAdmin = null;
    protected omero.api.IUpdatePrx iUpdate = null;
    
    protected Column[] myColumns = null;
    protected TablePrx myTable = null;
    
    /**
     * Set up services
     * 
     * @throws Exception
     */
    @BeforeClass
    public void setUp() throws Exception {
        
        client = new omero.client();
        sf = client.createSession(); // Set ICE_CONFIG for configuration
       
        iQuery = sf.getQueryService();
        iAdmin = sf.getAdminService();
        iUpdate = sf.getUpdateService();
    }
	
    /**
     * Close session and clear variables
     * @throws Exception
     */
    @AfterClass
    public void tearDown() throws Exception {        
        client.closeSession();
        
        client = null;
        sf = null;
        iQuery = null;
        iAdmin = null;
        iUpdate = null;
    }
    
    
    /**
     * Create/initialize a new myTable
     * 
     * @throws ServerError
     */
    @SuppressWarnings("unused")
	@BeforeMethod
    private String createTable() throws ServerError
    {
		myColumns = createColumns(1);
		
		String uniqueTableFile = "TableTest" + UUID.randomUUID().toString();
		
    	// Create new unique table
		myTable = sf.sharedResources().newTable(1, uniqueTableFile);
		myTable.initialize(myColumns);
		
		return uniqueTableFile;
    }
    
    /**
     * Delete myTable
     * 
     * @throws ServerError
     */
    @SuppressWarnings("unused")
	@AfterMethod
    private void deleteTable() throws ServerError
    {
        iUpdate.deleteObject(myTable.getOriginalFile());
        myTable = null;
    }
    
    // Simple Tests
    
    /**
     * retrieve table's OriginalFile
     * 
     * @throws Exception
     */
    @Test
    public void getOriginalFileTest() throws Exception {;
    	myTable.getOriginalFile();
    }
    
    /**
     * Retrieve table header
     * 
     * @throws Exception
     */
    @Test
    public void getHeadersTest() throws Exception {
    	myTable.getHeaders();
    }

    /**
     * Add two rows of data to the table
     * 
     * @throws Exception
     */
    @Test
    public void addDataTest() throws Exception {
    	Column[] newRow = createColumns(2);

    	LongColumn uids = (LongColumn) newRow[UID_COLUMN];
    	LongColumn myLongs = (LongColumn) newRow[LONG_COLUMN];
    	StringColumn myStrings = (StringColumn) newRow[STRING_COLUMN];
    	
    	uids.values[0] = 0;
    	myLongs.values[0] = 0;
    	myStrings.values[0] = "zero";
    	uids.values[1] = 1;
    	myLongs.values[1] = 1;
    	myStrings.values[1] = "one";
    	
    	myTable.addData(newRow);
    }
    
    /**
     * @throws Exception
     */
    @Test
    public void getNumberOfRowsTest() throws Exception {
    	
    	assertTrue(myTable.getNumberOfRows() == 0);
    	
    	Column[] newRow = createColumns(1);

    	LongColumn uids = (LongColumn) newRow[UID_COLUMN];
    	LongColumn myLongs = (LongColumn) newRow[LONG_COLUMN];
    	StringColumn myStrings = (StringColumn) newRow[STRING_COLUMN];
    	
    	uids.values[0] = 0;
    	myLongs.values[0] = 0;
    	myStrings.values[0] = "none";
    	
    	myTable.addData(newRow);
    	
    	assertTrue(myTable.getNumberOfRows() == 1);
    }

    @Test
    public void getWhereListEmptyTableTest() throws Exception {
    	
		long[] ids = myTable.getWhereList("(Uid=="+ 0 +")", null, 0, myTable.getNumberOfRows(), 1);
		
		assertTrue(ids.length==0); 
    }

    @Test
    public void getWhereListManyRowsTest() throws Exception {
    	Column[] newRow = createColumns(3);

    	LongColumn uids = (LongColumn) newRow[UID_COLUMN];
    	LongColumn myLongs = (LongColumn) newRow[LONG_COLUMN];
    	StringColumn myStrings = (StringColumn) newRow[STRING_COLUMN];

    	uids.values[0] = 0;
    	myLongs.values[0] = 0;
    	myStrings.values[0] = "zero";
    	uids.values[1] = 1;
    	myLongs.values[1] = 1;
    	myStrings.values[1] = "one";
    	uids.values[2] = 2;
    	myLongs.values[2] = 2;
    	myStrings.values[2] = "two";
    	
    	myTable.addData(newRow);
    	
		long[] ids = myTable.getWhereList("(Uid=="+ 1 +")", null, 0, myTable.getNumberOfRows(), 1);
		
		// getWhereList should have returned one row
		assertTrue(ids.length==1); 
		
        //Retrieve data again
        Data myData = myTable.read(ColNumbers, 0L, myTable.getNumberOfRows());

        myStrings = (StringColumn) myData.columns[STRING_COLUMN];
        myLongs = (LongColumn) myData.columns[LONG_COLUMN];

        // Row's time string and value should be the same
        assertTrue((myLongs.values[(int) ids[0]])==1);
        assertTrue((myStrings.values[(int) ids[0]]).equals("one"));
    }

    /**
     * Test readCoordinates() with zero rows in table. This throws
     * an exception because there's no need to try to read zero data.
     *
     * @throws Exception
     */
    @Test(expectedExceptions = ApiUsageException.class)
    public void getReadCoordinates0RowsTest() throws Exception {
        myTable.readCoordinates(null);
    }

    /**
     * Test readCoordinates() with one row in table.
     * 
     * @throws Exception
     */
    @Test
    public void getReadCoordinates1RowsTest() throws Exception {
    	
    	Column[] newRow = createColumns(1);

    	LongColumn uids = (LongColumn) newRow[UID_COLUMN];
    	LongColumn myLongs = (LongColumn) newRow[LONG_COLUMN];
    	StringColumn myStrings = (StringColumn) newRow[STRING_COLUMN];
    	
    	uids.values[0] = 0;
    	myLongs.values[0] = 0;
    	myStrings.values[0] = "zero";
    	
    	myTable.addData(newRow);
    	myTable.readCoordinates(new long[]{0L}); 	
    }
    
    /**
     * Test readCoordinates() with two row in table
     * 
     * @throws Exception
     */
    @Test
    public void getReadCoordinates2RowsTest() throws Exception {
    	
    	Column[] newRow = createColumns(2);

    	LongColumn uids = (LongColumn) newRow[UID_COLUMN];
    	LongColumn myLongs = (LongColumn) newRow[LONG_COLUMN];
    	StringColumn myStrings = (StringColumn) newRow[STRING_COLUMN];
    	
    	uids.values[0] = 0;
    	myLongs.values[0] = 0;
    	myStrings.values[0] = "zero";
    	uids.values[1] = 1;
    	myLongs.values[1] = 1;
    	myStrings.values[1] = "one";
    	
    	myTable.addData(newRow);
    	myTable.readCoordinates(new long[]{0L,1L}); 	
    }
    
    /**
     * Test read() with no rows in table
     * 
     * @throws Exception
     */
    @Test
    public void read0RowsTest() throws Exception {
    	myTable.read(ColNumbers, 0L, myTable.getNumberOfRows());
    }
    
    /**
     * Test read() with one row in table
     * 
     * @throws Exception
     */
    @Test
    public void read1RowsTest() throws Exception {
    	
    	Column[] newRow = createColumns(1);

    	LongColumn uids = (LongColumn) newRow[UID_COLUMN];
    	LongColumn myLongs = (LongColumn) newRow[LONG_COLUMN];
    	StringColumn myStrings = (StringColumn) newRow[STRING_COLUMN];
    	
    	uids.values[0] = 0;
    	myLongs.values[0] = 0;
    	myStrings.values[0] = "none";
    	
    	myTable.addData(newRow);
    	myTable.read(ColNumbers, 0L, myTable.getNumberOfRows());  	
    }

    /**
     * Test read() with two rows in table
     * 
     * @throws Exception
     */
    @Test
    public void read2RowsTest() throws Exception {
    	Column[] newRow = createColumns(2);

    	LongColumn uids = (LongColumn) newRow[UID_COLUMN];
    	LongColumn myLongs = (LongColumn) newRow[LONG_COLUMN];
    	StringColumn myStrings = (StringColumn) newRow[STRING_COLUMN];
    	
    	uids.values[0] = 0;
    	myLongs.values[0] = 0;
    	myStrings.values[0] = "zero";
    	uids.values[1] = 1;
    	myLongs.values[1] = 1;
    	myStrings.values[1] = "one";
    	
    	myTable.addData(newRow);
    	myTable.read(ColNumbers, 0L, myTable.getNumberOfRows());
    }
    
    /**
     * @throws Exception
     */
    @Test
    public void slice0RowsTest() throws Exception {
    	myTable.slice(null, null);
    }

    /**
     * Read one row slice()
     * 
     * @throws Exception
     */
    @Test
    public void slice1RowsTest() throws Exception {
    	Column[] newRow = createColumns(1);

    	LongColumn uids = (LongColumn) newRow[UID_COLUMN];
    	LongColumn myLongs = (LongColumn) newRow[LONG_COLUMN];
    	StringColumn myStrings = (StringColumn) newRow[STRING_COLUMN];
    	
    	uids.values[0] = 0;
    	myLongs.values[0] = 0;
    	myStrings.values[0] = "zero";
    	
    	myTable.addData(newRow);
    	myTable.slice(ColNumbers, new long[]{0L});
    }
    
    
    /**
     * Read two row slice()
     * 
     * @throws Exception
     */
    @Test
    public void slice2RowsTest() throws Exception {
    	Column[] newRow = createColumns(2);

    	LongColumn uids = (LongColumn) newRow[UID_COLUMN];
    	LongColumn myLongs = (LongColumn) newRow[LONG_COLUMN];
    	StringColumn myStrings = (StringColumn) newRow[STRING_COLUMN];
    	
    	uids.values[0] = 0;
    	myLongs.values[0] = 0;
    	myStrings.values[0] = "zero";
    	uids.values[1] = 1;
    	myLongs.values[1] = 1;
    	myStrings.values[1] = "one";
    	
    	myTable.addData(newRow);
    	
    	myTable.slice(ColNumbers, new long[]{0L,1L});
    }
    
     /**
     * Add then update a table row, assert its validity
     * @throws Exception
     */
    @Test
    public void updateTableWith1RowsTest() throws Exception {
    	// Add a new row to table
    	Column[] newRow = createColumns(1);

    	LongColumn uids = (LongColumn) newRow[UID_COLUMN];
    	LongColumn myLongs = (LongColumn) newRow[LONG_COLUMN];
    	StringColumn myStrings = (StringColumn) newRow[STRING_COLUMN];

		Long oldTime = new Date().getTime();
    	
    	uids.values[0] = 1;
    	myLongs.values[0] = oldTime;
    	myStrings.values[0] = oldTime.toString();

    	myTable.addData(newRow);
    		
    	// Retrieve the table data
    	Data myData = myTable.read(ColNumbers, 0L, myTable.getNumberOfRows());	
    	
    	// Find the specific row we added
    	long[] ids = myTable.getWhereList("(Uid==" + 1 + ")", null, 0, myTable.getNumberOfRows(), 1);
    	
		// getWhereList should have returned one row
		assertTrue(ids.length==1); 
    	    	
    	// Update the row with new data
    	Long newTime = new Date().getTime();
		
    	((LongColumn) myData.columns[LONG_COLUMN]).values[(int) ids[0]] = newTime;
    	((StringColumn) myData.columns[STRING_COLUMN]).values[(int) ids[0]] = newTime.toString();
    	       
        myTable.update(myData);
        
        //Retrieve data again
        myData = myTable.read(ColNumbers, 0L, myTable.getNumberOfRows());

        myStrings = (StringColumn) myData.columns[STRING_COLUMN];
        myLongs = (LongColumn) myData.columns[LONG_COLUMN];

        // Row's time string and value should be the same
        assertTrue(newTime.toString().equals(myStrings.values[(int) ids[0]]));
        assertTrue(newTime==myLongs.values[(int) ids[0]]);
    } //updateTableRow()
	
    /**
     * Add then update a table row, assert its validity
     * @throws Exception
     */
    @Test
    public void updateTableWith2RowsTest() throws Exception {
    	// Add a new row to table
    	Column[] newRow = createColumns(2);

    	LongColumn uids = (LongColumn) newRow[UID_COLUMN];
    	LongColumn myLongs = (LongColumn) newRow[LONG_COLUMN];
    	StringColumn myStrings = (StringColumn) newRow[STRING_COLUMN];

		Long oldTime = new Date().getTime();
    	
    	uids.values[0] = 1;
    	myLongs.values[0] = oldTime;
    	myStrings.values[0] = oldTime.toString();

    	uids.values[1] = 2;
    	myLongs.values[1] = oldTime;
    	myStrings.values[1] = oldTime.toString();
    	
    	myTable.addData(newRow);
    		
    	// Retrieve the table data
    	Data myData = myTable.read(ColNumbers, 0L, myTable.getNumberOfRows());	
    	
    	// Find the specific row we added
    	long[] ids = myTable.getWhereList("(Uid==" + 1 + ")", null, 0, myTable.getNumberOfRows(), 1);
    	
		// getWhereList should have returned one row
		assertTrue(ids.length==1); 
    	    	
    	// Update the row with new data
    	Long newTime = new Date().getTime();
		
    	((LongColumn) myData.columns[LONG_COLUMN]).values[(int) ids[0]] = newTime;
    	((StringColumn) myData.columns[STRING_COLUMN]).values[(int) ids[0]] = newTime.toString();
    	       
        myTable.update(myData);
        
        //Retrieve data again
        myData = myTable.read(ColNumbers, 0L, myTable.getNumberOfRows());

        myStrings = (StringColumn) myData.columns[STRING_COLUMN];
        myLongs = (LongColumn) myData.columns[LONG_COLUMN];

        // Row's time string and value should be the same
        assertTrue(newTime.toString().equals(myStrings.values[(int) ids[0]]));
        assertTrue(newTime==myLongs.values[(int) ids[0]]);
    } //updateTableRow()
    
    // ~ Helpers
    // =========================================================================

    /**
     * Creates a number of empty rows of [rows] size for the table
     * 
     * @param rows
     * @return
     */
    private Column[] createColumns(int rows) {
        Column[] newColumns = new Column[3];
        newColumns[UID_COLUMN] = new LongColumn("Uid", "", new long[rows]);
        newColumns[LONG_COLUMN] = new LongColumn("MyLongColumn", "", new long[rows]);
        newColumns[STRING_COLUMN] = new StringColumn("MyStringColumn", "", 64, new String[rows]);
        return newColumns;
    }
}

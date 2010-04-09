package integration;

import java.util.Date;
import java.util.UUID;

import junit.framework.TestCase;
import omero.ServerError;
import omero.grid.Column;
import omero.grid.LongColumn;
import omero.grid.StringColumn;
import omero.grid.Data;
import omero.grid.TablePrx;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TableTest extends TestCase {

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
    
    @Override
    @BeforeClass
    public void setUp() throws Exception {
        
        client = new omero.client();
        sf = client.createSession(); // Set ICE_CONFIG for configuration

        iQuery = sf.getQueryService();
        iAdmin = sf.getAdminService();
        iUpdate = sf.getUpdateService();

		myColumns = createColumns(1);
		
    	// Create new unique table
		myTable = sf.sharedResources().newTable(1, "TableTest" + UUID.randomUUID().toString());
		myTable.initialize(myColumns);
		
		// TODO: There's a bug with table.read() which fails if
	    // the table has less then 2 rows in it. Therefore we need
    	// to prime base table with 2 blank rows
    	Column[] newRow = createColumns(2);

    	LongColumn uids = (LongColumn) newRow[UID_COLUMN];
    	LongColumn myLongs = (LongColumn) newRow[LONG_COLUMN];
    	StringColumn myStrings = (StringColumn) newRow[STRING_COLUMN];

    	uids.values[0] = 0;
    	myLongs.values[0] = 0;
    	myStrings.values[0] = "none";
    	uids.values[1] = 0;
    	myLongs.values[1] = 0;
    	myStrings.values[1] = "none";

    	myTable.addData(newRow);
    }
	
    @Override
    @AfterClass
    public void tearDown() throws Exception {
    	
        iUpdate.deleteObject(myTable.getOriginalFile());
        myTable = null;
        
        client.closeSession();
    }
    
	/**
	 * Add Table row to table and assert its validity
	 * @throws Exception
	 */
	@Test
	public void addTableRowTest() throws Exception {
			
		long oldNumOfRows = myTable.getNumberOfRows();
		
    	Column[] newRow = createColumns(1);

    	LongColumn uids = (LongColumn) newRow[UID_COLUMN];
    	LongColumn myLongs = (LongColumn) newRow[LONG_COLUMN];
    	StringColumn myStrings = (StringColumn) newRow[STRING_COLUMN];
    	
		long newUid = getHighestTableUid() + 1;
		Long uniqueTime = new Date().getTime();
    	
    	uids.values[0] = newUid;
    	myLongs.values[0] = uniqueTime;
    	myStrings.values[0] = uniqueTime.toString();

    	myTable.addData(newRow);
		
		// Should have added one table row
		assertTrue(myTable.getNumberOfRows() == oldNumOfRows+1);
		
		long[] ids = myTable.getWhereList("(Uid=="+ newUid +")", null, 0, myTable.getNumberOfRows(), 1);
		
		// getWhereList should have returned one row
		assertTrue(ids.length==1); 
		
		Data myData = myTable.read(ColNumbers, 0L, myTable.getNumberOfRows());
		
        myStrings = (StringColumn) myData.columns[STRING_COLUMN];
        myLongs = (LongColumn) myData.columns[LONG_COLUMN];
		
        // Row's time string and value should be the same
        assertTrue(uniqueTime.toString().equals(myStrings.values[(int) ids[0]]));
        assertTrue(uniqueTime==myLongs.values[(int) ids[0]]);
        	
	} //addTableRow()
	
    /**
     * Add then update a table row, assert its validity
     * @throws Exception
     */
    @Test
    public void updateTableRowTest() throws Exception {
    			
    	// Add a new row to table
    	Column[] newRow = createColumns(1);

    	LongColumn uids = (LongColumn) newRow[UID_COLUMN];
    	LongColumn myLongs = (LongColumn) newRow[LONG_COLUMN];
    	StringColumn myStrings = (StringColumn) newRow[STRING_COLUMN];

		Long oldTime = new Date().getTime();
		long newUid = getHighestTableUid() + 1;
    	
    	uids.values[0] = newUid;
    	myLongs.values[0] = oldTime;
    	myStrings.values[0] = oldTime.toString();

    	myTable.addData(newRow);
    		
    	// Retrieve the table data
    	Data myData = myTable.read(ColNumbers, 0L, myTable.getNumberOfRows());	
    	
    	// Find the specific row we added
    	long[] ids = myTable.getWhereList("(Uid==" + newUid + ")", null, 0, myTable.getNumberOfRows(), 1);
    	
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
	
    // Creates a number of empty rows of [rows] size for the table
    private Column[] createColumns(int rows) {
        Column[] newColumns = new Column[3];
        newColumns[UID_COLUMN] = new LongColumn("Uid", "", new long[rows]);
        newColumns[LONG_COLUMN] = new LongColumn("MyLongColumn", "", new long[rows]);
        newColumns[STRING_COLUMN] = new StringColumn("MyStringColumn", "", 64, new String[rows]);
        return newColumns;
    }
    
    /**
     * Return the highest Uid in the table
     * 
     * @return
     * @throws ServerError
     */
    private long getHighestTableUid() throws ServerError
    {
    	long highestUid = 0;
    	Data d = myTable.read(ColNumbers, 0L, myTable.getNumberOfRows());
    	LongColumn uids = (LongColumn) d.columns[UID_COLUMN];
    	int length = uids.values.length;
    	if (length == 0)
    		return highestUid;
    	else
    		for (long id : uids.values)
    		{
    			if (id > highestUid) highestUid = id;
    		}
    	return highestUid; 	
    }
}

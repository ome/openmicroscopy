/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

//Java imports
import java.util.Date;
import java.util.UUID;

//Third-party libraries
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

//Application-internal dependencies
import omero.api.IAdminPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.ApiUsageException;
import omero.ServerError;
import omero.grid.Column;
import omero.grid.LongColumn;
import omero.grid.StringColumn;
import omero.grid.Data;
import omero.grid.TablePrx;


/** 
 * Collections of tests for the <code>IUpdate</code> service.
 *
 * @author  Brian loranger &nbsp;&nbsp;&nbsp;&nbsp;
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class TableTest 
{

	/** The default size of a buffer. */
    protected int DEFAULT_BUFFER_SIZE = 3;
    
    /** Identifies the ID column. */
    protected int UID_COLUMN = 0;
    
    /** Identifies the Long column. */
    protected int LONG_COLUMN = 1;
    
    /** Identifies the String column. */
    protected int STRING_COLUMN = 2;
    
    protected long[] ColNumbers = {UID_COLUMN, LONG_COLUMN, STRING_COLUMN};
    
	/** 
	 * The client object, this is the entry point to the Server. 
	 */
    protected omero.client client = null;
    
    /** Helper reference to the <code>Service factory</code>. */
    protected ServiceFactoryPrx  sf;
    
    /** Helper reference to the <code>IQuery</code> service. */
    protected IQueryPrx iQuery;
    
    /** Helper reference to the <code>IAdmin</code> service. */
    protected IAdminPrx iAdmin;
    
    /** Helper reference to the <code>IUpdate</code> service. */
    protected IUpdatePrx iUpdate;
    
    /** Reference to the columns. */
    protected Column[] myColumns;
    
    /** Reference to the table. */
    protected TablePrx myTable = null;
    
    
    // ~ Helpers
    // =========================================================================

    /**
     * Creates a number of empty rows of [rows] size for the table
     * 
     * @param rows The number of rows.
     * @return See above.
     */
    private Column[] createColumns(int rows) 
    {
        Column[] newColumns = new Column[3];
        newColumns[UID_COLUMN] = new LongColumn("Uid", "", new long[rows]);
        newColumns[LONG_COLUMN] = new LongColumn("MyLongColumn", "", 
        		new long[rows]);
        newColumns[STRING_COLUMN] = new StringColumn("MyStringColumn", "", 
        		64, new String[rows]);
        return newColumns;
    }
    
	/**
     * Initializes the various services.
     * @throws Exception Thrown if an error occurred.
     */
    @BeforeClass
    public void setUp() 
    	throws Exception 
    { 
        client = new omero.client();
        sf = client.createSession(); 
        iQuery = sf.getQueryService();
        iAdmin = sf.getAdminService();
        iUpdate = sf.getUpdateService();
    }
	
    /**
     * Closes session and clear variables.
     *  @throws Exception Thrown if an error occurred.
     */
    @AfterClass
    public void tearDown() 
    	throws Exception
    {        
        client.closeSession();
        // This also calls sf.destroy();
    }
    
    
    /**
     * Create/initialize a new myTable.
     * @throws ServerError Thrown if an error occurred.
     */
    @SuppressWarnings("unused")
	@BeforeMethod
    private String createTable() 
    	throws ServerError
    {
		myColumns = createColumns(1);
		
		String uniqueTableFile = "TableTest" + UUID.randomUUID().toString();
		
    	// Create new unique table
		myTable = sf.sharedResources().newTable(1, uniqueTableFile);
		myTable.initialize(myColumns);
		
		return uniqueTableFile;
    }
    
    /**
     * Delete myTable.
     * @throws ServerError Thrown if an error occurred.
     */
    @SuppressWarnings("unused")
	@AfterMethod
    private void deleteTable() 
    	throws ServerError
    {
        iUpdate.deleteObject(myTable.getOriginalFile());
        myTable = null;
    }
    
    // Simple Tests
    
    /**
     * Retrieve table's OriginalFile.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void getOriginalFileTest() 
    	throws Exception
    {
    	myTable.getOriginalFile();
    }
    
    /**
     * Retrieve table header.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void getHeadersTest() 
    	throws Exception
    {
    	myTable.getHeaders();
    }

    /**
     * Add two rows of data to the table.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void addDataTest() 
    	throws Exception 
    {
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
     * Retrieves the number of rows.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void getNumberOfRowsTest() 
    	throws Exception
    {
    	
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

    /**
     * Tests the <code>WhereList</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void getWhereListEmptyTableTest() 
    	throws Exception 
    {
    	
		long[] ids = myTable.getWhereList("(Uid=="+ 0 +")", null, 0, 
				myTable.getNumberOfRows(), 1);
		
		assertTrue(ids.length==0); 
    }

    /**
     * Tests the <code>WhereList</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void getWhereListManyRowsTest() 
    	throws Exception {
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
    	
		long[] ids = myTable.getWhereList("(Uid=="+ 1 +")", null, 0, 
				myTable.getNumberOfRows(), 1);
		
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
     * Tests <code>readCoordinates()</code> with zero rows in table. This throws
     * an exception because there's no need to try to read zero data.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(expectedExceptions = ApiUsageException.class)
    public void getReadCoordinates0RowsTest() 
    	throws Exception 
    {
        myTable.readCoordinates(null);
    }

    /**
     * Tests <code>readCoordinates()</code> with one row in table.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void getReadCoordinates1RowsTest()
    	throws Exception
    {
    	
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
     * Tests <code>readCoordinates()</code> with two row in table
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void getReadCoordinates2RowsTest() 
    	throws Exception
    {
    	
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
     * Tests <code>read()</code> with no rows in table.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void read0RowsTest() 
    	throws Exception
    {
    	myTable.read(ColNumbers, 0L, myTable.getNumberOfRows());
    }
    
    /**
     * Tests <code>read</code> method with one row in table.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void read1RowsTest()
    	throws Exception
    {
    	
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
     * Test <code>read</code> method with two rows in table
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void read2RowsTest() 
    	throws Exception
    {
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
     * Tests <code>slice</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(expectedExceptions = ApiUsageException.class)
    public void slice0RowsTest()
    	throws Exception 
    {
    	myTable.slice(null, null);
    }

    /**
     * Tests <code>slice</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void slice1RowsTest() 
    	throws Exception
    {
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
     * Tests <code>slice</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void slice2RowsTest() 
    	throws Exception 
    {
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
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void updateTableWith1RowsTest() 
    	throws Exception 
    {
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
    	long[] ids = myTable.getWhereList("(Uid==" + 1 + ")", null, 0, 
    			myTable.getNumberOfRows(), 1);
    	
		// getWhereList should have returned one row
		assertTrue(ids.length==1); 
    	    	
    	// Update the row with new data
    	Long newTime = new Date().getTime();
		
    	((LongColumn) myData.columns[LONG_COLUMN]).values[
    	                                              (int) ids[0]] = newTime;
    	((StringColumn) myData.columns[STRING_COLUMN]).values[
    	                                      (int) ids[0]] = newTime.toString();
    	       
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
     * Add then update a table row, assert its validity.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void updateTableWith2RowsTest()
    	throws Exception
    {
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
    	long[] ids = myTable.getWhereList("(Uid==" + 1 + ")", null, 0, 
    			myTable.getNumberOfRows(), 1);
    	
		// getWhereList should have returned one row
		assertTrue(ids.length==1); 
    	    	
    	// Update the row with new data
    	Long newTime = new Date().getTime();
		
    	((LongColumn) myData.columns[LONG_COLUMN]).values[(int) ids[0]] 
    	                                                  = newTime;
    	((StringColumn) myData.columns[STRING_COLUMN]).values[(int) ids[0]] 
    	                                                  = newTime.toString();
    	       
        myTable.update(myData);
        
        //Retrieve data again
        myData = myTable.read(ColNumbers, 0L, myTable.getNumberOfRows());

        myStrings = (StringColumn) myData.columns[STRING_COLUMN];
        myLongs = (LongColumn) myData.columns[LONG_COLUMN];

        // Row's time string and value should be the same
        assertTrue(newTime.toString().equals(myStrings.values[(int) ids[0]]));
        assertTrue(newTime==myLongs.values[(int) ids[0]]);
    } 
    
}

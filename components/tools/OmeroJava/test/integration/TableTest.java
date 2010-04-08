package integration;

import java.io.File;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;
import ome.conditions.ValidationException;
import omero.ServerError;
import omero.grid.Column;
import omero.grid.LongColumn;
import omero.grid.StringColumn;
import omero.grid.Data;
import omero.grid.TablePrx;
import omero.model.OriginalFile;

import org.springframework.util.ResourceUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TableTest extends TestCase {

    protected int DEFAULT_BUFFER_SIZE = 3;
    
    protected int UID_COLUMN = 0;
    protected int LONG_COLUMN = 1;
    protected int STRING_COLUMN = 2;
    
    protected omero.client client = null;
    protected omero.api.ServiceFactoryPrx  sf = null;
    protected omero.api.IQueryPrx iQuery = null;
    protected omero.api.IAdminPrx iAdmin = null;
    protected omero.api.IUpdatePrx iUpdate = null;

    protected String dbName = "TableTest";
    
    protected Column[] myColumns = null;
    protected TablePrx myTable = null;
    
    @Override
    @BeforeMethod
    public void setUp() throws Exception {
        
        client = new omero.client();
        sf = client.createSession("root", client.getProperty("omero.rootpass"));
        
        //client = new omero.client("mage.openmicroscopy.org.uk", 4064);
        //sf = client.createSession("user", "password");
        
        iQuery = sf.getQueryService();
        iAdmin = sf.getAdminService();
        iUpdate = sf.getUpdateService();

		myColumns = createColumns(1);
		myTable = initializeTable(dbName);
    }
	
    @Override
    @AfterMethod
    public void tearDown() throws Exception {
    	
    	deleteTable(dbName);
        client.closeSession();
    }
    
	/**
	 * Add Table row to table and assert its validity
	 * @throws Exception
	 */
	@Test
	public void addTableRow() throws Exception {
		
		Long time = new Date().getTime();
		
		long oldMaxUid = getHighestTableUid();
		long oldNumOfRows = myTable.getNumberOfRows();
		
		long newMaxUid = addTableRow(time, time.toString());
		long newNumOfRows = myTable.getNumberOfRows();
		
		// Uid should be one higher
		assertTrue(newMaxUid == oldMaxUid+1);
		
		// Should have added one table row
		assertTrue(newNumOfRows == oldNumOfRows+1);
		
		long[] ids = myTable.getWhereList("(Uid=="+newMaxUid+")", null, 0, myTable.getNumberOfRows(), 1);
		
		// getWhereList should have returned one row
		assertTrue(ids.length==1); 
		
		Data myData = getTableData();
		
        StringColumn myStrings = (StringColumn) myData.columns[STRING_COLUMN];
        LongColumn myLongs = (LongColumn) myData.columns[LONG_COLUMN];
		
        int returnedRows = 0;
        if (ids != null)
        	returnedRows = ids.length;
        
        for (int h = 0; h < returnedRows; h++)
        {
        	int i = (int) ids[h];
        	
        	// Row's time string and value should be the same
        	assertTrue(time.toString().equals(myStrings.values[i]));
        	assertTrue(time==myLongs.values[i]);
        }
	} //addTableRow()
	
    /**
     * Add then update a table row, assert its validity
     * @throws Exception
     */
    @Test
    public void updateTableRow() throws Exception {
    	
		Long oldTime = new Date().getTime();
		
		long oldMaxUid = getHighestTableUid();
		long oldNumOfRows = myTable.getNumberOfRows();
		
		long newMaxUid = addTableRow(oldTime, oldTime.toString());
		long newNumOfRows = myTable.getNumberOfRows();
		
		// Uid should be one higher
		assertTrue(newMaxUid == oldMaxUid+1);
		
		// Should have added one table row
		assertTrue(newNumOfRows == oldNumOfRows+1); 
		
    	long[] ids = myTable.getWhereList("(Uid==" + newMaxUid + ")", null, 0, myTable.getNumberOfRows(), 1);
    	
		// getWhereList should have returned one row
		assertTrue(ids.length==1); 
    	
        int returnedRows = 0;
        if (ids != null)
        	returnedRows = ids.length;
        
    	Data myData = getTableData();	
    	
    	Long newTime = new Date().getTime();
    	
        for (int h = 0; h < returnedRows; h++)
        {
        	int i = (int) ids[h];
        	((LongColumn) myData.columns[LONG_COLUMN]).values[i] = newTime;
        	((StringColumn) myData.columns[STRING_COLUMN]).values[i] = newTime.toString();
        }
    	       
        myTable.update(myData);
        
        //Get data again
		myData = getTableData();
		
        StringColumn myStrings = (StringColumn) myData.columns[STRING_COLUMN];
        LongColumn myLongs = (LongColumn) myData.columns[LONG_COLUMN];
        
        for (int h = 0; h < returnedRows; h++)
        {
        	int i = (int) ids[h];
        	
        	// Row's time string and value should be the same
        	assertTrue(newTime.toString().equals(myStrings.values[i]));
        	assertTrue(newTime==myLongs.values[i]);
        }
        		
		
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
	 * Initialize a new table
	 * 
	 * @param dbName
	 * @return
	 * @throws ServerError
	 */
	private TablePrx initializeTable(String dbName) throws ServerError
    {           
		List<OriginalFile> tableFiles = getOriginalFiles(dbName);

		TablePrx table = null;
		
		if (tableFiles.isEmpty() || tableFiles == null)     
		{
			// Create new table
			table = sf.sharedResources().newTable(1, dbName);
			table.initialize(myColumns);
			
			// Prime base table with 2 blank rows to address bug
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

			table.addData(newRow);

		} else {
			// Use existing table
			table = sf.sharedResources().openTable(tableFiles.get(0));     
		}
		return table;
    }
	
	/**
	 * Get originalFile object for table
	 * 
	 * @param fileName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<OriginalFile> getOriginalFiles(String fileName)
    {
        try
        {
            final String queryString = "from OriginalFile as o where o.details.owner.id = '" + 
            	iAdmin.getEventContext().userId + "' and o.name = '" + fileName + "'";
            
        	List l = iQuery.findAllByQuery(queryString, null);
        	return (List<OriginalFile>) l;
        }
        catch (NullPointerException npe)
        {
        	return null;
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }
	
    /**
     * Add a table row, return new row's Uid
     * 
     * @param newLong
     * @param newString
     * @return
     * @throws ServerError
     */
    private long addTableRow(Long newLong, String newString) throws ServerError 
    {
    	Column[] newRow = createColumns(1);

    	LongColumn uids = (LongColumn) newRow[UID_COLUMN];
    	LongColumn myLongs = (LongColumn) newRow[LONG_COLUMN];
    	StringColumn myStrings = (StringColumn) newRow[STRING_COLUMN];

    	long lastUid = getHighestTableUid();
    	long newUid = lastUid + 1;

    	uids.values[0] = newUid;
    	myLongs.values[0] = newLong;
    	myStrings.values[0] = newString;

    	myTable.addData(newRow);

    	return newUid;
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
    	Data d = getTableData();
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
    
    /**
     * Retrieve all table data 
     * 
     * @return
     * @throws ServerError
     */
    public Data getTableData() throws ServerError
    {
     	
        	long rows = myTable.getNumberOfRows();
        	//System.err.println("Number of rows in Table: " + rows);
        	long[] ColNumbers = {UID_COLUMN, LONG_COLUMN, STRING_COLUMN};
            Data d = myTable.read(ColNumbers, 0L, rows);
            return d;
    }
    
    /**
     * Delete table
     * 
     * @param dbName
     * @throws ServerError
     * @throws ValidationException
     */
    private void deleteTable(String dbName) throws ServerError, ValidationException
    {        
        List<OriginalFile> testTableFiles = getOriginalFiles(dbName);
        
        if (testTableFiles == null || testTableFiles.isEmpty())
        {
        	return;
        }
        
        for (OriginalFile file : testTableFiles)
        {
        	iUpdate.deleteObject(file);
        }
        myTable = null;
    }
}

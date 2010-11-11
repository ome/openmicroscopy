/*
 * ome.formats.importer.gui.History
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package ome.formats.importer.gui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;

import ome.formats.OMEROMetadataStoreClient;
import omero.ServerError;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.grid.Column;
import omero.grid.Data;
import omero.grid.LongColumn;
import omero.grid.StringColumn;
import omero.grid.TablePrx;
import omero.model.OriginalFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

/**
 * @author Brian W. Loranger
 *
 */
public class HistoryTableStore extends HistoryTableAbstractDataSource
{
	private static boolean DEBUG = false;
	
	private static String SERVER = "warlock.openmicroscopy.org.uk";
	private static String USER = "root";
	private static String PASS = "omero";
	
	private static String baseDBNAME = "baseFile";
	private static String itemDBNAME = "itemFile";
	
    /** Logger for this class. */
    private static Log log = LogFactory.getLog(HistoryTableStore.class);
    
    public SimpleDateFormat day = new SimpleDateFormat("MMM d, ''yy");
    public SimpleDateFormat hour = new SimpleDateFormat("HH:mm");
    //private SimpleDateFormat sqlDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //private SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    private static final int DEFAULT_BUFFER_SIZE = 1;
    
    public static final int BASE_UID_COLUMN = 0;
    public static final int BASE_DATETIME_COLUMN = 1;
    public static final int BASE_STATUS_COLUMN = 2;
    
    public static final int ITEM_BASE_UID_COLUMN = 0;
    public static final int ITEM_FILENAME_COLUMN = 1;
    public static final int ITEM_PROJECTID_COLUMN = 2;
    public static final int ITEM_OBJECTID_COLUMN = 3;
    public static final int ITEM_DATETIME_COLUMN = 4;
    public static final int ITEM_FILEPATH_COLUMN = 5;
    public static final int ITEM_STATUS_COLUMN = 6;
    public static final int ITEM_FILENUMBER_COLUMN = 7;
    
    private OMEROMetadataStoreClient store;
    private ServiceFactoryPrx sf;
    private IQueryPrx iQuery;
    private TablePrx baseTable;
    private Column[] baseColumns;
    private TablePrx itemTable;
    private Column[] itemColumns;
	public boolean historyEnabled = true;
	private static long lastUid = 0;
	
	private Data baseData = null;
	private boolean baseDataDirty = true;
	
	private Data itemData = null;
	private boolean itemDataDirty = true;

	
	
	// General Methods ----------------------------------------------- //
	
    /**
     * Initialize the needed store services
     * 
     * @param store - OMEROmetadataStore
     * @throws ServerError - returned server error if unable to contact server
     */
    public void initialize(OMEROMetadataStoreClient store) throws ServerError {
    	this.store = store;
        this.sf = store.getServiceFactory();
        this.iQuery = sf.getQueryService();
        if (sf.sharedResources().areTablesEnabled() == true) 
        {        	
        	this.historyEnabled = true;
        	log.warn("History tables service Enabled.");
        } else
        {
        	this.historyEnabled = false;
        	log.warn("History tables service Disabled.");
        }
        
    }
    
	/* (non-Javadoc)
	 * @see ome.formats.importer.gui.IHistoryTableDataSource#initializeDataSource()
	 */
	public void initializeDataSource() throws ServerError
    {
		if (historyEnabled == false) return;
        baseColumns = createBaseColumns(DEFAULT_BUFFER_SIZE);
        itemColumns = createItemColumns(DEFAULT_BUFFER_SIZE);
    	initializeBaseTable();
    	initializeItemTable();
    }
     
    /* (non-Javadoc)
     * @see ome.formats.importer.gui.IHistoryTable#wipeUserHistory(java.lang.Long)
     */
    public boolean wipeDataSource(Long experimenterId) throws ServerError
    {
    	if (historyEnabled == false) return false;
    	clearTable(itemDBNAME);
    	clearTable(baseDBNAME);
    	initializeDataSource();	  
    	return true;
    }
    
    /**
     * Clear the table specified with 'dbName'
     * (Currently this deletes and rebuilds the original file used for 'dbName' table)
     * 
     * @param dbName - db to clear
     * @throws ServerError
     */
	private void clearTable(String dbName) throws ServerError
    {   
        List<OriginalFile> dbFiles = getOriginalFiles(dbName);
        
        if (dbFiles == null || dbFiles.isEmpty())
        {
        	if (DEBUG) log.debug("No " + dbName + " found.");
        	return;
        }
        
        for (OriginalFile file : dbFiles)
        {
        	if (DEBUG) log.debug("Deleting " + file.getName().getValue());
        	deleteOriginalFile(file);
        }
    }
    
	/* (non-Javadoc)
	 * @see ome.formats.importer.gui.IHistoryTableDataSource#shutdownDataSource()
	 */
	public void shutdownDataSource() throws Exception
	{
		// Not required. Does nothing.
	}

    /**
     * Retrieve the original file specified, or null
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
            	store.getExperimenterID() + "' and o.name = '" + fileName + "'";
            
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
     * Delete the original file specified. This does not handle any links.
     * 
     * @param file
     * @throws ServerError
     */
    private void deleteOriginalFile(final OriginalFile file) throws ServerError {
    	IUpdatePrx update = sf.getUpdateService();
    	try {
    		update.deleteObject(file);
    	} catch (ome.conditions.ValidationException e) {
    		throw new RuntimeException(e);
    	}
    }
    
    /**
     * Simple class to display table data from both tables. Used for testing.
     */
    public void displayTableData()
    {
    	try {
    		Data baseData = getBaseTableData();
			
        	LongColumn uids = (LongColumn) baseData.columns[BASE_UID_COLUMN];
        	LongColumn baseImportTimes = (LongColumn) baseData.columns[BASE_DATETIME_COLUMN];
        	StringColumn baseStatuses = (StringColumn) baseData.columns[BASE_STATUS_COLUMN];
        	
        	// item table data
			//log.debug("Rows in base table: " + baseTable.getNumberOfRows());
        	for (int i = 0; i < uids.values.length; i++)
        	{
        		log.debug("UID[" + uids.values[i] + "]: "
        				+ baseImportTimes.values[i] + ", '" 
        				+ baseStatuses.values[i].trim() + "'" 
        				);
        	}
        	
        	// item table data
			if (DEBUG) log.debug("Rows in item table: " + itemTable.getNumberOfRows());
			
			Data itemData = getItemTableData();
			
			LongColumn baseUids = (LongColumn) itemData.columns[ITEM_BASE_UID_COLUMN];
            StringColumn fileNames = (StringColumn) itemData.columns[ITEM_FILENAME_COLUMN];
            LongColumn projectIDs = (LongColumn) itemData.columns[ITEM_PROJECTID_COLUMN];
            LongColumn objectIDs = (LongColumn) itemData.columns[ITEM_OBJECTID_COLUMN];
        	LongColumn importTimes = (LongColumn) itemData.columns[ITEM_DATETIME_COLUMN];
            StringColumn filePaths = (StringColumn) itemData.columns[ITEM_FILEPATH_COLUMN];
            StringColumn statuses = (StringColumn) itemData.columns[ITEM_STATUS_COLUMN];
            LongColumn fileNumbers = (LongColumn) itemData.columns[ITEM_FILENUMBER_COLUMN];
        	
        	for (int i = 0; i < baseUids.values.length; i++)
        	{
        		log.debug("UID[" + baseUids.values[i] + "]: '"
        				+ fileNames.values[i].trim() + "', " 
        				+ projectIDs.values[i] + ", " 
        				+ objectIDs.values[i] + ", " 
        				+ importTimes.values[i] + ", '" 
        				+ filePaths.values[i].trim() + "', '" 
        				+ statuses.values[i].trim() + "'"
        				+ fileNumbers.values[i]
        				);
        	}
			
		} catch (ServerError e) {
			throw new RuntimeException(e);
		}
    }

	
	// Base Table Methods ----------------------------------------------- //
	
    /**
     * Creates a number of empty rows of [rows] size for the base history table
     * 
     * @param rows to add to column array (int)
     * @return new bae column array
     */
    private Column[] createBaseColumns(int rows) {
        Column[] newColumns = new Column[3];
        newColumns[BASE_UID_COLUMN] = new LongColumn("Uid", "", new long[rows]);
        newColumns[BASE_DATETIME_COLUMN] = new LongColumn("DateTime", "", new long[rows]);
        newColumns[BASE_STATUS_COLUMN] = new StringColumn("Status", "", 64, new String[rows]);
        return newColumns;
    }
    
    /**
     * Initialize a new base table or connect to exist one
     * 
     * @throws ServerError
     */
    private void initializeBaseTable() throws ServerError 
    {   	
        if (historyEnabled == false) return;
        
        List<OriginalFile> baseFiles = getOriginalFiles(baseDBNAME);
        
        if (baseFiles == null || baseFiles.isEmpty())     
        {
        	createBaseTable();
        } else {
            log.debug("Using existing " + baseDBNAME);
            baseTable = sf.sharedResources().openTable(baseFiles.get(0));
            if (baseTable == null)
            {
            	if (DEBUG) System.err.println("baseTable is null"); 
            }
            else
            {
            	historyEnabled = true;
                lastUid = getHighestBaseTableUid();
            }
        }
    }
    
    /**
     * Create the baseTable
     * 
     * @throws ServerError
     */
    private void createBaseTable() throws ServerError {
        log.debug("Creating new " + baseDBNAME);
        baseTable = sf.sharedResources().newTable(1, baseDBNAME);
        if (baseTable == null)
        {
        	if (DEBUG) System.err.println("baseTable is null");
        }
        else
        {
        	historyEnabled = true;

        	baseTable.initialize(baseColumns);

        	// Prime base table with 2 blank rows to address bug.
        	Column[] newRow = createBaseColumns(2);

        	LongColumn uids = (LongColumn) newRow[BASE_UID_COLUMN];
        	LongColumn importTimes = (LongColumn) newRow[BASE_DATETIME_COLUMN];
        	StringColumn statuses = (StringColumn) newRow[BASE_STATUS_COLUMN];

        	uids.values[0] = 0;
        	importTimes.values[0] = 0;
        	statuses.values[0] = String.format("%1$-64s", " ");
        	uids.values[1] = 0;
        	importTimes.values[1] = 0;
        	statuses.values[1] = String.format("%1$-64s", " ");

        	baseTable.addData(newRow);
        	baseDataDirty = true;
            lastUid = getHighestBaseTableUid();
        }
	}

	/* (non-Javadoc)
     * @see ome.formats.importer.gui.IHistoryTableDataSource#getLastBaseUid()
     */
    public int getLastBaseUid() throws ServerError
    {
    	return (int) lastUid;
    }
    
    /**
     * Determine what the highest uid is in the base table
     * 
     * @return the last base table uid in the uid column (or zero)
     * @throws ServerError
     */
    private long getHighestBaseTableUid() throws ServerError
    {
    	long highestUid = 0;
    	Data d = getBaseTableData();
    	LongColumn uids = (LongColumn) d.columns[BASE_UID_COLUMN];
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
     * Wrapper class for addBaseTableRow
     * 
     * @param experimenterID
     * @param import_status
     * @throws ServerError 
     */
    public int addBaseTableRow(Long experimenterID, String import_status) throws ServerError
    {
    	return (int) addBaseTableRow(import_status);
    }
    
    /**
     * Add a new row to the base table
     * 
     * @param import_status
     * @return the lastUid in the base table (which should increment if everything went ok)
     * @throws ServerError 
     */
    
    private long addBaseTableRow(String import_status) throws ServerError 
    {
    	Column[] newRow = createBaseColumns(1);

    	LongColumn uids = (LongColumn) newRow[BASE_UID_COLUMN];
    	LongColumn importTimes = (LongColumn) newRow[BASE_DATETIME_COLUMN];
    	StringColumn statuses = (StringColumn) newRow[BASE_STATUS_COLUMN];

    	long newUid = lastUid + 1;

    	uids.values[0] = newUid;
    	importTimes.values[0] = new Date().getTime();
    	statuses.values[0] = String.format("%1$-64s", import_status);

    	if (DEBUG) log.debug("Adding base row UID[" + uids.values[0] + "], " + importTimes.values[0] + ", " + statuses.values[0]);
    	baseTable.addData(newRow);
    	baseDataDirty = true;
    	
    	return lastUid = newUid;

    }
    
    /* (non-Javadoc)
     * @see ome.formats.importer.gui.IHistoryTableDataSource#updateBaseStatus(int, java.lang.String)
     */
    public Integer updateBaseStatus(int baseUid, String newStatus) throws ServerError
    {
    	Long uid = Long.valueOf(baseUid);
    	String searchString = "(Uid==" + uid.longValue() + ")";
    	long[] ids = baseTable.getWhereList(searchString, null, 0, baseTable.getNumberOfRows(), 1);
    	
        int returnedRows = ids.length;
        if (DEBUG) log.debug("updateBaseStatus returned rows: " + returnedRows);
        
    	Data baseData = getBaseTableData();	
    	
        for (int h = 0; h < returnedRows; h++)
        {
        	int i = (int) ids[h];
        	((StringColumn) baseData.columns[BASE_STATUS_COLUMN]).values[i] = newStatus;
        }
    	       
        baseTable.update(baseData);
    	baseDataDirty = true;
        
    	return returnedRows;
    }
    
    /* (non-Javadoc)
     * @see ome.formats.importer.gui.IHistoryTableDataSource#getBaseQuery(java.lang.Long)
     */
    public Vector<Object> getBaseQuery(Long ExperimenterID)
    {   

        Vector<Object> rows = new Vector<Object>();
    	
        try {
            Data d = getBaseTableData();
            int returnedRows = (int) getBaseTableNumberOfRows();
            
            LongColumn importTimes = (LongColumn) d.columns[BASE_DATETIME_COLUMN];
            StringColumn statuses = (StringColumn) d.columns[BASE_STATUS_COLUMN];
           
            for (int i = 0; i < returnedRows; i++)
            {
            	Vector<Object> row = new Vector<Object>();
            	row.add(new Date(importTimes.values[i]));
            	row.add(statuses.values[i].trim());
            	rows.add(row);
            }
            
        } catch (NullPointerException npe) {
        	
        } // results are null
        catch (Exception e) {
        	log.error("exception.", e);
        }
        return rows;
    }
   
    
    /* (non-Javadoc)
     * @see ome.formats.importer.gui.IHistoryTableDataSource#getBaseTableDataByDate(java.util.Date, java.util.Date)
     */
    public DefaultListModel getBaseTableDataByDate(Date start, Date end)
    {
        try
        {
            // Format the current time.
            String dayString, hourString, status;
            long uid = 0L, importTime = 0L;
            String icon;
            DefaultListModel list = new DefaultListModel();
            
        	String searchString = "(DateTime>=" + start.getTime() + ") & (DateTime<=" + end.getTime() + ")";
        	long[] ids = baseTable.getWhereList(searchString, null, 0, baseTable.getNumberOfRows(), 1);
	           
            int returnedRows = ids.length;
            if (DEBUG) log.debug("getBaseTableDataByDate returned rows: " + returnedRows);
        	
        	Data d = getBaseTableData();
        	
            LongColumn uids = (LongColumn) d.columns[BASE_UID_COLUMN];
        	LongColumn importTimes = (LongColumn) d.columns[BASE_DATETIME_COLUMN];
        	StringColumn statuses = (StringColumn) d.columns[BASE_STATUS_COLUMN];
            
            for (int h = 0; h < returnedRows; h++)
            {
            	int i = (int) ids[h];
            	
            	try {
            		uid = uids.values[i];
            		importTime = importTimes.values[i];
            		status = statuses.values[i].trim();
            	} catch (ArrayIndexOutOfBoundsException e)
            	{
            		System.err.println("ids["+h+"] not found in dataset.");
            		continue;
            	}

                if (status.equals("complete"))
                    icon = "gfx/import_done_16.png";
                else
                    icon = "gfx/warning_msg16.png";
                dayString = day.format(new Date(importTime));
                hourString = hour.format(new Date(importTime));

                if (day.format(new Date()).equals(dayString))
                    dayString = "Today";

                if (day.format(getYesterday()).equals(dayString))
                {
                    dayString = "Yesterday";
                }

                ImportEntry entry = new ImportEntry(dayString + " " + hourString, icon, (int) uid);
                list.addElement(entry);
            }
            return list;
        } 
        catch (Exception e)
        {
        	String s = String.format(
        			"Error retrieving base list from %s to %s.",
        			start.toString(), end.toString());
        	log.error(s, e);
        }
        return new DefaultListModel(); // return empty defaultlist
    }
    
    /**
     * Return all the base table data
     * @return
     * @throws ServerError 
     */
    public Data getBaseTableData() throws ServerError
    {
    	if (baseDataDirty)
    	{
        	long rows = baseTable.getNumberOfRows();
        	long[] ColNumbers = {BASE_UID_COLUMN, BASE_DATETIME_COLUMN, BASE_STATUS_COLUMN};
            baseData = baseTable.read(ColNumbers, 0L, rows);
            baseDataDirty = false;
        	if (DEBUG) log.debug("Getting " + rows + " rows in " + baseDBNAME);
    	}	
        return baseData;
    }
    
    /**
     * Returns the number of rows in the base table
     * @return
     */
    public long getBaseTableNumberOfRows()
    {
    	try {
			return baseTable.getNumberOfRows();
		} catch (ServerError e) {
			log.error("Error in getBaseTableNumberOfRows: ", e);
			e.printStackTrace();
			return 0L;
		}
    }
    
    
    
    // Item Table Methods ----------------------------------------------- //
    
    /**
     * Creates a number of empty rows of [rows] size for the item history table
     * 
     * @param rows to add to column array (int)
     * @return new item column array
     */
    private Column[] createItemColumns(int rows) {
        Column[] newColumns = new Column[8];
        newColumns[ITEM_BASE_UID_COLUMN] = new LongColumn("BaseUid", "", new long[rows]);
        newColumns[ITEM_FILENAME_COLUMN] = new StringColumn("Filename", "", 256, new String[rows]);
        newColumns[ITEM_PROJECTID_COLUMN] = new LongColumn("ProjectId", "", new long[rows]);
        newColumns[ITEM_OBJECTID_COLUMN] = new LongColumn("ObjectId", "", new long[rows]);
        newColumns[ITEM_DATETIME_COLUMN] = new LongColumn("DateTime", "", new long[rows]);
        newColumns[ITEM_FILEPATH_COLUMN] = new StringColumn("Filepath", "", 1024, new String[rows]);
        newColumns[ITEM_STATUS_COLUMN] = new StringColumn("Status", "", 32, new String[rows]);
        newColumns[ITEM_FILENUMBER_COLUMN] = new LongColumn("FileNumber", "", new long[rows]);
        return newColumns;
    }
    
    /**
     * Initialize the item table (or connect to existing one)
     * 
     * @throws ServerError
     */
    private void initializeItemTable() throws ServerError
    {
    	// No point in doing this if historytable is disabled
    	if (historyEnabled == false) return;
    	
        List<OriginalFile> itemFiles = getOriginalFiles(itemDBNAME);
        
        if (itemFiles.isEmpty())     
        {
            if (DEBUG) log.debug("Creating new " + itemDBNAME);
            itemTable = sf.sharedResources().newTable(1, itemDBNAME);           
            itemTable.initialize(itemColumns);
            
        	// Prime item table with 2 blank rows to address bug.
    		Column[] newRow = createItemColumns(2);
    		
            LongColumn baseUids = (LongColumn) newRow[ITEM_BASE_UID_COLUMN];
            StringColumn fileNames = (StringColumn) newRow[ITEM_FILENAME_COLUMN];
            LongColumn projectIDs = (LongColumn) newRow[ITEM_PROJECTID_COLUMN];
            LongColumn objectIDs = (LongColumn) newRow[ITEM_OBJECTID_COLUMN];
            LongColumn importTimes = (LongColumn) newRow[ITEM_DATETIME_COLUMN];
            StringColumn filePaths = (StringColumn) newRow[ITEM_FILEPATH_COLUMN];
            StringColumn Statuses = (StringColumn) newRow[ITEM_STATUS_COLUMN];
            LongColumn fileNumbers = (LongColumn) newRow[ITEM_FILENUMBER_COLUMN];

            baseUids.values[0] = 0;
    		fileNames.values[0] = String.format("%1$-256s", " ");
    		projectIDs.values[0] = -1;
    		objectIDs.values[0] = -1;
    		importTimes.values[0] = 0;
    		filePaths.values[0] = String.format("%1$-1024s", " ");
    		Statuses.values[0] = String.format("%1$-32s", " ");
    		fileNumbers.values[0] = -1;
    		
            baseUids.values[1] = 0;
    		fileNames.values[1] = String.format("%1$-256s", " ");
    		projectIDs.values[1] = -1;
    		objectIDs.values[1] = -1;
    		importTimes.values[1] = 0;
    		filePaths.values[1] = String.format("%1$-1024s", " ");
    		Statuses.values[1] = String.format("%1$-32s", " ");
    		fileNumbers.values[1] = -1;

    		itemTable.addData(newRow);
    		itemDataDirty = true;
        } else {
            if (DEBUG) log.debug("Using existing " + itemDBNAME);
            itemTable = sf.sharedResources().openTable(itemFiles.get(0));
            if (itemTable == null)
            	if (DEBUG) System.err.println("itemTable is null");
        } 
    }

    /* (non-Javadoc)
     * @see ome.formats.importer.gui.IHistoryTableDataSource#addItemTableRow(
     * java.lang.Long, java.lang.Integer, java.lang.Integer, java.lang.String, 
     * java.lang.Long, java.lang.Long, java.lang.String, java.io.File)
     */
    public int addItemTableRow(Long experimenterID, Integer baseUid, Integer fileNumber, 
        String fileName, Long projectID, Long objectID, String status, File file) throws ServerError
    {
    	addItemTableRow((long) baseUid, fileName, (long) fileNumber, projectID, objectID, 
    			new Date().getTime(), status, file.getAbsolutePath());
    	return -1; // not used
    }
            
    /**
     * add a row of information to the item table
     * 
     * @param baseUid
     * @param fileName
     * @param fileNumber
     * @param projectID
     * @param objectID
     * @param importTime
     * @param status
     * @param filePath
     * @throws ServerError
     */
    public void addItemTableRow(long baseUid, String fileName, long fileNumber, 
    	long projectID, long objectID, long importTime, String status, String filePath) throws ServerError
    {
    	Column[] newRow = createItemColumns(1);

    	LongColumn baseUids = (LongColumn) newRow[ITEM_BASE_UID_COLUMN];
    	StringColumn fileNames = (StringColumn) newRow[ITEM_FILENAME_COLUMN];
    	LongColumn projectIDs = (LongColumn) newRow[ITEM_PROJECTID_COLUMN];
    	LongColumn objectIDs = (LongColumn) newRow[ITEM_OBJECTID_COLUMN];
    	LongColumn importTimes = (LongColumn) newRow[ITEM_DATETIME_COLUMN];
    	StringColumn filePaths = (StringColumn) newRow[ITEM_FILEPATH_COLUMN];
    	StringColumn Statuses = (StringColumn) newRow[ITEM_STATUS_COLUMN];
    	LongColumn fileNumbers = (LongColumn) newRow[ITEM_FILENUMBER_COLUMN];

    	baseUids.values[0] = baseUid;
    	fileNames.values[0] = String.format("%1$-256s", fileName);
    	projectIDs.values[0] = projectID;
    	objectIDs.values[0] = objectID;
    	importTimes.values[0] = importTime;
    	filePaths.values[0] = String.format("%1$-1024s", filePath);
    	Statuses.values[0] = String.format("%1$-32s", status);
    	fileNumbers.values[0] = 0;
    	    	
    	itemTable.addData(newRow); 
		itemDataDirty = true;

    	if (DEBUG) displayTableData();
    }

    /* (non-Javadoc)
     * @see ome.formats.importer.gui.IHistoryTableDataSource#updateItemStatus(int, int, java.lang.String)
     */
    public Integer updateItemStatus(int baseUid, int index, String newStatus) throws ServerError
    {
    	Long uid = Long.valueOf(baseUid);
    	String searchString = "(BaseUid==" + uid.longValue() + ")";
    	long[] ids = itemTable.getWhereList(searchString, null, 0, itemTable.getNumberOfRows(), 1);
    	
        int returnedRows = ids.length;
        if (DEBUG) log.debug("updateItemStatus returned rows: " + returnedRows);
        
    	Data itemData = getItemTableData();	
    	
        for (int h = 0; h < returnedRows; h++)
        {
        	int i = (int) ids[h];
        	if (h == index)
        		((StringColumn) itemData.columns[ITEM_STATUS_COLUMN]).values[i] = newStatus;
        }
    	
        
        itemTable.update(itemData);
		itemDataDirty = true;
        
    	return returnedRows;
    }

    /**
     * Return an array of ids based on query and date
     * @param uid
     * @param queryString
     * @param from
     * @param to
     * @return
     */
    public long[] getItemTableIDsByQuery(Long baseId, String queryString, Date start, Date end)
    {
        try
        {   
        	String searchString = "";
        	if (start == null || end == null)
        	{
        		if (queryString.trim().length() > 0)
        		{
        			searchString = "(BaseUid=="+baseId+")"; // & (" + queryString + "in Filename)";
        		}
        		else
        		{
        			// Search by id
        			searchString = "(BaseUid=="+baseId+")";
        		}
        		
        	} else {
        		// Search by date
        		searchString = "(DateTime>=" + start.getTime() + ") & (DateTime<=" + end.getTime() + ")";
        	}
    		long[] ids = itemTable.getWhereList(searchString, null, 0, itemTable.getNumberOfRows(), 1);
        	
        	return ids;           
        } 
        catch (Exception e)
        {
        	String s = String.format(
        			"Error retrieving import list from %s to %s.",
        			start.toString(), end.toString());
        	log.error(s, e);
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see ome.formats.importer.gui.IHistoryTableDataSource#getItemQuery(java.lang.Long, java.lang.Long, java.lang.String, java.util.Date, java.util.Date)
     */
    public Vector<Object> getItemQuery(Long importID, Long experimenterID, String queryString, Date from, Date to)
    {   
        Vector<Object> rows = new Vector<Object>();
        
        try {
        	Data d = getItemTableData();
        	long[] ids = getItemTableIDsByQuery(importID, "", from, to);
        	
            StringColumn fileNames = (StringColumn) d.columns[ITEM_FILENAME_COLUMN];
            LongColumn projectIDs = (LongColumn) d.columns[ITEM_PROJECTID_COLUMN];
        	LongColumn objectIDs = (LongColumn) d.columns[ITEM_OBJECTID_COLUMN];
        	LongColumn importTimes = (LongColumn) d.columns[ITEM_DATETIME_COLUMN];
        	StringColumn filePaths = (StringColumn) d.columns[ITEM_FILENAME_COLUMN];
        	StringColumn statuses = (StringColumn) d.columns[ITEM_STATUS_COLUMN];
           
            // Format the current time.
            String fileName = "", filePath = "", status = "";
            Long objectID = 0L, projectID = 0L, importTime = 0L;
            
            int returnedRows = 0;
            if (ids != null)
            	returnedRows = ids.length;
            
            for (int h = 0; h < returnedRows; h++)
            {
            	int i = (int) ids[h];
            	fileName = fileNames.values[i].trim();
            	projectID = projectIDs.values[i];
            	objectID = objectIDs.values[i];
            	importTime = importTimes.values[i];
            	filePath = filePaths.values[i].trim();
            	status = statuses.values[i].trim();

                Vector<Object> row = new Vector<Object>();
                row.add(fileName);
                row.add(importTime);
                row.add(status);
                row.add(filePath);
                row.add(objectID);
                row.add(projectID);
                rows.add(row);
            }
        } catch (NullPointerException npe) {
        	log.error("Null pointer exception.", npe);
        } // results are null
        catch (Exception e) {
        	log.error("exception.", e);
        }
        return rows;
    }

    /**
     * Return all the base table data
     * @return
     */
    public Data getItemTableData() throws ServerError
    {
    	if (itemDataDirty)
    	{
        	long rows = itemTable.getNumberOfRows();
            long[] colNumbers = {0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L};
            itemData = itemTable.read(colNumbers, 0L, rows);
            itemDataDirty = false;
    	}
    	return itemData;
    }
        
    /**
     * Returns the number of rows in the base table
     * @return
     */
    public long getItemTableNumberOfRows()
    {
    	try {
			return itemTable.getNumberOfRows();
		} catch (ServerError e) {
			e.printStackTrace();
			return 0L;
		}
    }

    /**
     * Test code for this class.
     * 
     * @param args
     * @throws CannotCreateSessionException
     * @throws PermissionDeniedException
     * @throws ServerError
     */
    public static void main (String[] args) throws CannotCreateSessionException, PermissionDeniedException, ServerError 
    {
    	DEBUG = true;
    	
        OMEROMetadataStoreClient store = new OMEROMetadataStoreClient();

        store.initialize(USER, PASS, SERVER, 4064);

        boolean CLEAN = true;
        boolean TEST = false;
        
        HistoryTableStore hts = new HistoryTableStore();
        try
        {
            hts.initialize(store);
            
        	if (CLEAN)
        	{
                hts.clearTable(baseDBNAME);
                hts.clearTable(itemDBNAME);
        	}
            hts.initializeDataSource();
            lastUid = hts.getHighestBaseTableUid();
            if (DEBUG) log.debug("Last UID: " + lastUid);
            if (TEST)
            {
                long testid = hts.addBaseTableRow("test");
                if (DEBUG) log.debug("Last UID: " + lastUid);
                    	
                hts.addItemTableRow(testid, "nofile", 0, 0, 0, 0, "test", "nopath");
            }
            hts.displayTableData();
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        } finally {
        store.logout();
        }
        System.err.println("Done");
    }
}

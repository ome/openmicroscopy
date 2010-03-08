package ome.formats.importer.gui;

import java.io.File;
import java.util.Date;

import javax.swing.DefaultListModel;

/**
 * History Table Data Sources consist of two parts: 
 *  - a batch table containing the information about the current import 
 *    'base' batch (status, date), which returns a UID
 *  - a 'file' table which accepts the base UID and contains the information
 *    about the specific file imported in the batch
 * 
 * @author Brian W. Loranger
 */
public interface IHistoryTableDataSource
{
	
	/**
	 * Initalize the datasource tables needed (base and file tables), 
	 * either reconnecting to existing tables, or creating new ones.
	 * 
	 * @throws ServerError
	 */
	void initializeDataSource() throws Exception;
	
	/**
	 * Given experimenter's ID, wipe existing history tables
	 * @param experimenterID
	 * @return success
	 * @throws Exception
	 */
	boolean wipeDataSource(Long experimenterID) throws Exception;
	
	/**
	 * Shutdown existing datasources if required
	 * 
	 * @throws Exception
	 */
	void shutdownDataSource() throws Exception;

	/**
	 * @return return the last Uid in the base table
	 * @throws Exception
	 */
	int getLastBaseUid() throws Exception;

	/**
	 * Insert an import history item into the 'base' history table
	 * 
	 * @param experimenterID
	 * @param status
	 * @return Uid of table row added (or -1 if not applicable)
	 * @throws Exception
	 */
	int addBaseTableRow(Long experimenterID, String status) throws Exception;

    /**
     * @param experimenterID - experimenter's id
     * @param baseUID - UID from base table
     * @param fileNumber - file's placement in the import batch (ie: 1st, 2nd, etc file in the import batch)
     * @param fileName - file's name
     * @param projectID - project id linked into 
     * @param status - import status
     * @param file - file link
     * @param datasetID - container (dataset/well) linked into
     * @return returns the UID of the row inserted
     * @throws Exception
     */
    int addItemTableRow(Long experimenterID, Integer baseUID, Integer fileNumber, 
            String fileName, Long projectID, Long containerID, String status, File file) throws Exception;
	
    
    /**
     * return a data collection from the base table based on star and end dates
     * @param start date
     * @param end date
     * @return DefaultListModel containing all entries between start and end dates
     * @throws Exception
     */
    DefaultListModel getBaseTableDataByDate(Date start, Date end);
}


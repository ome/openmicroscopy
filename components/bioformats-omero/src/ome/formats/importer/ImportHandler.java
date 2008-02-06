/*
 * ome.formats.testclient.ImportHandler
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import loci.formats.FormatException;
import ome.formats.OMEROMetadataStore;
import ome.model.core.Pixels;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Importer is master file format importer for all supported formats and imports
 * the files to an OMERO database
 * 
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 * @basedOnCodeFrom Curtis Rueden ctrueden at wisc.edu
 */
public class ImportHandler
{

    private ImportLibrary   library;
    private OMEROWrapper    reader;

    private Main      viewer;
    private static boolean   runState = false;
    private Thread runThread;
    HistoryDB db = null;

    
    //private ProgressMonitor monitor;
    
    private FileQueueTable  qTable;

    @SuppressWarnings("unused")
    private static Log      log = LogFactory.getLog(ImportHandler.class);
    
    private OMEROMetadataStore store;
    
    private int numOfPendings = 0;
    private int numOfDone = 0;

    public ImportHandler(Main viewer, FileQueueTable qTable, OMEROMetadataStore store,
            OMEROWrapper reader, ImportContainer[] fads)
    {
        db = HistoryDB.getHistoryDB();
        
        if (runState == true)
        {
            log.error("ImportHandler running twice");
            if (runThread != null) log.error(runThread);
            throw new RuntimeException("ImportHandler running twice");
        }
        runState = true;
        try {
            this.viewer = viewer;
            this.store = store;
            this.qTable = qTable;
            this.reader = reader;
            this.library = new ImportLibrary(store, reader, fads);
            library.addObserver(qTable);
                       
            runThread = new Thread()
            {
                public void run()
                {
                    try
                    {
                        importImages();
                    }
                    catch (Throwable e)
                    {
                        new DebugMessenger(null, "Error Dialog", true, e);
                    }
                }
            };
            runThread.start();
        }
        finally {
            runState = false;
        }
}

    /**
     * Begin the import process, importing first the meta data, and then each
     * plane of the binary image data.
     */
    private void importImages()
    {
        long timestampIn;
        long timestampOut;
        long timestampDiff;
        long timeInSeconds;
        long hours, minutes, seconds;
        Date date = null;

        // record initial timestamp and record total running time for the import
        timestampIn = System.currentTimeMillis();
        date = new Date(timestampIn);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String myDate = formatter.format(date);

        viewer.appendToOutputLn("> Starting import at: " + myDate + "\n");
        viewer.statusBar.setStatusIcon("gfx/import_icon_16.png", "Now importing.");
        
        ImportContainer[] fads = library.getFilesAndDatasets();
        qTable.importBtn.setText("Cancel");
        qTable.importing = true;
        
        numOfPendings = 0;
        int importKey = 0;
        int importStatus = 0;
        
        try
        {
            db.insertImportHistory(store.getExperimenterID(), "pending");
            importKey = db.getLastKey();
        } catch (SQLException e) {  
            e.printStackTrace();
        }
        
        for(int i = 0; i < fads.length; i++)
        {                
           	if (qTable.setProgressPending(i))
           	{
                numOfPendings++;
               	try {
               	    db.insertFileHistory(importKey, store.getExperimenterID(), i, fads[i].imageName, 
               	         fads[i].projectID, fads[i].dataset.getId(), "pending");
               	} catch (Exception e) { 
               	    e.printStackTrace();
               	}
           	}
        }
        
        db.notifyObservers("QUICKBAR_UPDATE");
        viewer.statusBar.setProgressMaximum(numOfPendings);
        
        numOfDone = 0;
        for (int j = 0; j < fads.length; j++)
        {
            if (qTable.table.getValueAt(j, 2).equals("pending") 
                    && qTable.cancel == false)
            {
                numOfDone++;
                String filename = fads[j].file.getAbsolutePath();
                
                viewer.appendToOutputLn("> [" + j + "] Importing \"" + filename
                        + "\"");
                
                library.setDataset(fads[j].dataset);
                
                try
                {
                	importImage(fads[j].file, j,
                			    numOfPendings,
                			    fads[j].imageName,
                			    fads[j].archive);
                	store.createRoot();
                    try
                    {
                        db.updateFileStatus(importKey, j, "done");
                    } catch (SQLException e)
                    {
                        e.printStackTrace();
                    }

                }
                catch (FormatException fe)
                {
                    qTable.setProgressUnknown(j);
                    viewer.appendToOutputLn("> [" + j + "] Unknown format.");
                    if (importStatus < 0)   importStatus = -3;
                    else                    importStatus = -1;
                    
                    try
                    {
                        db.updateImportStatus(importKey, "incomplete");
                        db.updateFileStatus(importKey, j, "unknown format");
                    } catch (SQLException e)
                    {
                        e.printStackTrace();
                    }
                }
                catch (Exception e)
                {
                	qTable.setProgressFailed(j);
                    viewer.appendToOutputLn("> [" + j + "] Failure importing.");
                    new DebugMessenger(null, "Error Dialog", true, e);
                    if (importStatus < 0)   importStatus = -3;
                    else                    importStatus = -2;
                    
                    try
                    {
                        db.updateImportStatus(importKey, "incomplete");
                        db.updateFileStatus(importKey, j, "failed");
                    } catch (SQLException e1)
                    {
                        e1.printStackTrace();
                    }
                }
            }
        }
        qTable.importBtn.setText("Import"); 
        qTable.importBtn.setEnabled(true);
        qTable.queue.setRowSelectionAllowed(true);
        qTable.removeBtn.setEnabled(true);
        if (qTable.failedFiles == true) 
            qTable.clearFailedBtn.setEnabled(true);
        if (qTable.doneFiles == true) 
            qTable.clearDoneBtn.setEnabled(true);
        qTable.importing = false;
        qTable.cancel = false;
        
        viewer.statusBar.setProgress(false, 0, "");
        //monitor.close();
        viewer.statusBar.setStatusIcon("gfx/import_done_16.png", "Import complete.");
        //System.err.println("import status: " + importStatus);
        if (importStatus >= 0) try
        {
            db.updateImportStatus(importKey, "complete");
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        
        timestampOut = System.currentTimeMillis();
        timestampDiff = timestampOut - timestampIn;

        // calculate hour/min/sec time for the run
        timeInSeconds = timestampDiff / 1000;
        hours = timeInSeconds / 3600;
        timeInSeconds = timeInSeconds - (hours * 3600);
        minutes = timeInSeconds / 60;
        timeInSeconds = timeInSeconds - (minutes * 60);
        seconds = timeInSeconds;

        viewer.appendToOutputLn("> Total import time: " + hours + " hour(s), "
                + minutes + " minute(s), " + seconds + " second(s).");

        viewer.appendToOutputLn("> Image import completed!");
    }

    /**
     * @param file
     * @param index
     * @param total Import the actual image planes
     * @param b 
	 * @throws FormatException if there is an error parsing metadata.
	 * @throws IOException if there is an error reading the file.
     */
    private List<Pixels> importImage(File file, int index, int total, String imageName, 
            boolean archive)
    	throws FormatException, IOException
    {        
        String fileName = file.getAbsolutePath();
        String shortName = file.getName();
        
        viewer.appendToOutput("> [" + index + "] Loading image \"" + shortName
                + "\"...");

        qTable.setProgressPrepping(index);
        viewer.statusBar.setStatusIcon("gfx/import_icon_16.png", "Prepping file \"" + shortName + "\"");

        library.open(file.getAbsolutePath());
        
        viewer.appendToOutput(" Succesfully loaded.\n");

        viewer.statusBar.setProgress(true, 0, "Importing file " + 
                numOfDone + " of " + total);
        viewer.statusBar.setProgressValue(numOfDone - 1);

        viewer.appendToOutput("> [" + index + "] Importing metadata for "
                + "image \"" + shortName + "\"... ");

        qTable.setProgressAnalyzing(index);
        //System.err.println("index:" + index);
        viewer.statusBar.setStatusIcon("gfx/import_icon_16.png", 
                "Analyzing the metadata for file \"" + shortName + "\"");
        
        String[] fileNameList = reader.getUsedFiles();
        File[] files = new File[fileNameList.length];
        for (int i = 0; i < fileNameList.length; i++) 
        {
            files[i] = new File(fileNameList[i]); 
        }
        store.setOriginalFiles(files); 
        reader.getUsedFiles();
        
        List<Pixels> pixList = library.importMetadata(imageName);

        int seriesCount = reader.getSeriesCount();
        
//        if (seriesCount > 1)
//        {
//            System.err.println("Series Count: " + reader.getSeriesCount());
//            throw new RuntimeException("More then one image in series");
//        }
        
        for (int series = 0; series < seriesCount; series++)
        {
            int count = library.calculateImageCount(fileName, series);
            Long pixId = pixList.get(series).getId(); 

            viewer.appendToOutputLn("Successfully stored to dataset \""
                    + library.getDataset() + "\" with id \"" + pixId + "\".");
            viewer.appendToOutputLn("> [" + index + "] Importing pixel data for "
                    + "image \"" + shortName + "\"... ");

            viewer.statusBar.setStatusIcon("gfx/import_icon_16.png", "Importing the plane data for file \"" + shortName + "\"");
            
            qTable.setProgressInfo(index, count);
            
            //viewer.appendToOutput("> Importing plane: ");
            library.importData(pixId, fileName, series, new ImportLibrary.Step()
            {
                @Override
                public void step(int series, int step)
                {
                    if (step <= qTable.getMaximum()) 
                    {   
                        qTable.setImportProgress(reader.getSeriesCount(), series, step);
                    }
                }
            });
            
            viewer.appendToOutputLn("> Successfully stored with pixels id \""
                    + pixId + "\".");
            viewer.appendToOutputLn("> [" + index
                    + "] Image imported successfully!");

            if (archive == true)
            {
                qTable.setProgressArchiving(index);
                for (int i = 0; i < fileNameList.length; i++) 
                {
                    files[i] = new File(fileNameList[i]);
                    store.writeFilesToFileStore(files, pixId);   
                }
            }
        }
        qTable.setProgressDone(index);

        //System.err.println(iInfo.getFreeSpaceInKilobytes());
        
        return pixList;
        
    }
}

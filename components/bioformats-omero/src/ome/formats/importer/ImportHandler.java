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

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

import loci.formats.FormatException;
import ome.formats.OMEROMetadataStore;

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
            this.library = new ImportLibrary(store, reader, fads);
            library.addObserver(qTable);
            library.addObserver(viewer);
                       
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
        
        db.notifyObservers("QUICKBAR_UPDATE", null);
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
                	library.importImage(fads[j].file, j,
                			    numOfDone, numOfPendings,
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
                    fe.printStackTrace();
                    qTable.setProgressUnknown(j);
                    viewer.appendToOutputLn("> [" + j + "] Failure importing.");
                    if (importStatus < 0)   importStatus = -3;
                    else                    importStatus = -1;
                    
                    if (fe.getMessage() == "Cannot locate JPEG decoder")
                    {
                        qTable.setProgressFailed(j);

                        viewer.appendToOutputLn("> [" + j + "] Lossless JPEG not supported. See " +
                                "http://trac.openmicroscopy.org.uk/omero/wiki/LosslessJPEG for " +
                                "details on this error.");
                        JOptionPane
                        .showMessageDialog(
                                viewer,
                                "\nThe importer cannot import the lossless JPEG images used by the file" +
                                "\n" + fads[j].imageName +
                                "\n\nThere maybe be a native library available for your operating system" +
                                "\nthat will support this format. For details on this error, check:" +
                                "\nhttp://trac.openmicroscopy.org.uk/omero/wiki/LosslessJPEG");
                    }
                    
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
        viewer.statusBar.setStatusIcon("gfx/import_done_16.png", "Import complete.");
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
}

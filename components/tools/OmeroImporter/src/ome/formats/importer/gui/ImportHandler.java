/*
 * ome.formats.importer.gui.ImportHandler
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

package ome.formats.importer.gui;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.JOptionPane;

import loci.formats.FormatException;
import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportLibrary;
import omero.ResourceError;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Importer is master file format importer for all supported formats and imports
 * the files to an OMERO database
 * 
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 * @basedOnCodeFrom Curtis Rueden ctrueden at wisc.edu
 */
public class ImportHandler implements IObservable {

    private static Log log = LogFactory.getLog(ImportHandler.class);

    private static boolean runState = false;

    private final ImportLibrary library;
    private final ImportContainer[] importContainer;
    private final HistoryDB db; // THIS SHOULD NOT BE HERE!

    private final GuiImporter viewer;
    private final FileQueueTable qTable;

    private int numOfPendings = 0;
    private int numOfDone = 0;
    
    final ArrayList<IObserver> observers = new ArrayList<IObserver>();
    
    public ImportHandler(final ScheduledExecutorService ex, GuiImporter viewer, FileQueueTable qTable,
            ImportConfig config, ImportLibrary library,
            ImportContainer[] containers) {

        this.library = library;
        this.importContainer = containers;
        if (viewer.historyTable != null) {
            this.db = viewer.historyTable.db;
        } else {
            this.db = null;
        }

        if (runState == true) {
            log.error("ImportHandler running twice");
            throw new RuntimeException("ImportHandler running twice");
        }
        runState = true;
        try {
            this.viewer = viewer;
            this.qTable = qTable;
            library.addObserver(qTable);
            library.addObserver(viewer);
            library.addObserver(viewer.errorHandler.getDelegate());
            library.addObserver(new IObserver(){
                public void update(IObservable importLibrary, ImportEvent event) {
                    if (ex.isShutdown()) {
                        log.info("Cancelling import");
                        throw new RuntimeException("CLIENT SHUTDOWN");
                    }
                }});

            Runnable run = new Runnable() {
                public void run() {
                    log.info("Background: Importing images");
                    importImages();
                }
            };
            ex.execute(run);
        } finally {
            runState = false; // FIXME this should be set from inside the thread, right?
        }
    }

    /**
     * Begin the import process, importing first the meta data, and then each
     * plane of the binary image data.
     */
    private void importImages() {
        long timestampIn;
        long timestampOut;
        long timestampDiff;
        long timeInSeconds;
        long hours, minutes, seconds;
        Date date = null;

        notifyObservers(new ImportEvent.IMPORT_QUEUE_STARTED());
        
        // record initial timestamp and record total running time for the import
        timestampIn = System.currentTimeMillis();
        date = new Date(timestampIn);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String myDate = formatter.format(date);

        viewer.appendToOutputLn("> Starting import at: " + myDate + "\n");
        viewer.statusBar.setStatusIcon("gfx/import_icon_16.png",
                "Now importing.");

        numOfPendings = 0;
        int importKey = 0;
        int importStatus = 0;

        try {
            if (db != null) {
                db.insertImportHistory(library.getExperimenterID(), "pending");
                importKey = db.getLastKey();
            }
        } catch (SQLException e) {
            log.error("SQL exception updating history DB.", e);
        }

        for (int i = 0; i < importContainer.length; i++) {
            if (qTable.setProgressPending(i)) {
                numOfPendings++;
                try {
                    if (db != null) {
                        // FIXME: This is now "broken" with targets now able to
                        // be of type Screen or Dataset.
                        db.insertFileHistory(importKey, library
                                .getExperimenterID(), i,
                                importContainer[i].imageName,
                                importContainer[i].projectID,
                                importContainer[i].getTarget().getId()
                                        .getValue(), "pending",
                                importContainer[i].file);
                    }
                } catch (Exception e) {
                    log.error("Generic error while inserting history.", e);
                    viewer.appendToDebug(e.toString());
                }
            }
        }

        if (db != null)
            db.notifyObservers(new ImportEvent.QUICKBAR_UPDATE());
        viewer.statusBar.setProgressMaximum(numOfPendings);

        numOfDone = 0;
        for (int j = 0; j < importContainer.length; j++) {
            ImportContainer container = importContainer[j];
            if (qTable.table.getValueAt(j, 2).equals("pending")
                    && qTable.cancel == false) {
                numOfDone++;
                String filename = container.file.getAbsolutePath();

                viewer.appendToOutputLn("> [" + j + "] Importing \"" + filename
                        + "\"");


                try {
                    library.importImage(importContainer[j].file, j, numOfDone,
                            numOfPendings, importContainer[j]
                                    .getUserSpecifiedName(), null, // Description
                            container.getArchive(), true, // Metadata file creation
                            // (TODO: Enable in container and UI)
                            container.getUserPixels(), container.getTarget());
                    this.library.clear();
                    try {
                        if (db != null)
                            db.updateFileStatus(importKey, j, "done");
                    } catch (SQLException e) {
                        log.error("SQL exception updating history DB.", e);
                    }

                } catch (FormatException fe) {
                    log.error("Format exception while importing image.", fe);
                    qTable.setProgressFailed(j);
                    viewer.appendToOutputLn("> [" + j + "] Failure importing.");
                    if (importStatus < 0)
                        importStatus = -3;
                    else
                        importStatus = -1;

                    if (fe.getMessage() == "Cannot locate JPEG decoder") {
                        viewer.appendToOutputLn("> [" + j
                                + "] Lossless JPEG not supported.");
                        /*
                         * See " +
                         * "http://trac.openmicroscopy.org.uk/omero/wiki/LosslessJPEG for "
                         * + "details on this error.");
                         */
                        JOptionPane.showMessageDialog(viewer,
                                "\nThe importer cannot import the lossless JPEG images used by the file"
                                        + "\n" + importContainer[j].imageName
                                        + "");
                        /*
                         * "
                         * "\n\nThere maybe be a native library available for your operating system"
                         * +
                         * "\nthat will support this format. For details on this error, check:"
                         * +
                         * "\nhttp://trac.openmicroscopy.org.uk/omero/wiki/LosslessJPEG"
                         * );
                         */
                    } 
                    try {
                        if (db != null) {
                            db.updateImportStatus(importKey, "incomplete");
                            db.updateFileStatus(importKey, j, "failed");
                        }
                    } catch (SQLException e) {
                        log.error("SQL exception updating history DB.", e);
                    }
                } catch (IOException ioe) {
                    log.error("I/O error while importing image.", ioe);
                    qTable.setProgressUnknown(j);
                    viewer.appendToOutputLn("> [" + j + "] Failure importing.");
                    if (importStatus < 0)
                        importStatus = -3;
                    else
                        importStatus = -1;
                    try {
                        if (db != null) {
                            db.updateImportStatus(importKey, "incomplete");
                            db.updateFileStatus(importKey, j, "failed");
                        }
                    } catch (SQLException e) {
                        log.error("SQL exception updating history DB.", e);
                    }
                } catch (ResourceError e) {
                    log.error("Resource error while importing image.", e);
                    JOptionPane
                            .showMessageDialog(viewer,
                                    "The server is out of space and imports cannot continue.\n");
                    qTable.setProgressFailed(j);
                    if (importStatus < 0)
                        importStatus = -3;
                    else
                        importStatus = -2;
                    qTable.cancel = true;
                    qTable.abort = true;
                    qTable.importing = false;
                    try {
                        if (db != null) {
                            db.updateImportStatus(importKey, "incomplete");
                            db.updateFileStatus(importKey, j, "failed");
                        }
                    } catch (SQLException sqle) {
                        log.error("SQL exception updating history DB.", sqle);
                    }

                } catch (Throwable error) {
                    log.error("Generic error while importing image.", error);
                    viewer.appendToDebug(ome.formats.importer.util.ErrorHandler.getStackTrace(error));
                    qTable.setProgressFailed(j);
                    viewer.appendToOutputLn("> [" + j + "] Failure importing.");

                    if (importStatus < 0)
                        importStatus = -3;
                    else
                        importStatus = -2;

                    try {
                        if (db != null) {
                            db.updateImportStatus(importKey, "incomplete");
                            db.updateFileStatus(importKey, j, "failed");
                        }
                    } catch (SQLException sqle) {
                        log.error("SQL exception updating history DB.", sqle);
                    }
                }
            }
        }

        viewer.statusBar.setProgress(false, 0, "");
        viewer.statusBar.setStatusIcon("gfx/import_done_16.png",
                "Import complete.");
        if (importStatus >= 0)
            try {
                if (db != null)
                    db.updateImportStatus(importKey, "complete");
            } catch (SQLException e) {
                log.error("SQL exception when updating import status.", e);
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
        notifyObservers(new ImportEvent.IMPORT_QUEUE_DONE());
    }

    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }
    
    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);
        
    }

    public void notifyObservers(ImportEvent event)
    {
        for (IObserver observer:observers)
        {
            observer.update(this, event);
        }
    }
    
}

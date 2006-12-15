/*
 * ome.formats.testclient.ImportHandler
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import loci.formats.IFormatReader;
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
public class ImportHandler {

    private ImportLibrary library;

    private Main viewer;

    // private ProgressMonitor monitor;

    private FileQueueTable qTable;

    private static Log log = LogFactory.getLog(ImportHandler.class);

    private OMEROMetadataStore store;

    public ImportHandler(Main viewer, FileQueueTable qTable,
            OMEROMetadataStore store, IFormatReader reader,
            ImportContainer[] fads) {
        this.viewer = viewer;
        this.library = new ImportLibrary(store, reader, fads);
        this.store = store;
        this.qTable = qTable;

        new Thread() {

            public void run() {
                importImages();
            }
        }.start();
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

        // record initial timestamp and record total running time for the import
        timestampIn = System.currentTimeMillis();
        date = new Date(timestampIn);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String myDate = formatter.format(date);

        viewer.appendToOutputLn("> Starting import at: " + myDate + "\n");
        viewer.statusBar.setStatusIcon("gfx/import_icon_16.png",
                "Now importing.");

        viewer.statusBar
                .setProgressMaximum(library.getFilesAndDatasets().length);

        ImportContainer[] fads = library.getFilesAndDatasets();
        qTable.importBtn.setText("Cancel");
        qTable.importing = true;

        for (int i = 0; i < fads.length; i++) {
            qTable.setProgressPending(i);
        }

        for (int i = 0; i < fads.length; i++) {
            if (qTable.table.getValueAt(i, 2).equals("pending")
                    && qTable.cancel == false) {
                String filename = fads[i].file.getAbsolutePath();
                viewer.appendToOutputLn("> [" + i + "] Importing \"" + filename
                        + "\"");

                library.setDataset(fads[i].dataset);

                long pixId = importImage(fads[i].file, i, library
                        .getFilesAndDatasets().length, fads[i].imageName,
                        fads[i].archive);
            }
        }
        qTable.importBtn.setText("Import");
        qTable.importBtn.setEnabled(true);
        qTable.importing = false;
        qTable.cancel = false;

        viewer.statusBar.setProgress(false, 0, "");
        // monitor.close();
        viewer.statusBar.setStatusIcon("gfx/import_done_16.png",
                "Import complete.");

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
     * @param total
     *            Import the actual image planes
     * @param b
     */
    private long importImage(File file, int index, int total, String imageName,
            boolean archive) {
        String fileName = file.getAbsolutePath();
        String shortName = file.getName();

        viewer.waitCursor(true);
        viewer.appendToOutput("> [" + index + "] Loading image \"" + shortName
                + "\"...");
        open(file.getAbsolutePath());
        viewer.appendToOutput(" Succesfully loaded.\n");
        viewer.waitCursor(false);

        int count = library.calculateImageCount(fileName);

        viewer.statusBar.setProgress(true, 0, "Importing file " + (index + 1)
                + " of " + total);
        viewer.statusBar.setProgressValue(index);

        viewer.appendToOutput("> [" + index + "] Importing metadata for "
                + "image \"" + shortName + "\"... ");

        qTable.setProgressPrepping(index);

        if (archive == true) {
            File[] files = new File[1];
            ;
            files[0] = file;
            store.setOriginalFiles(files);
        }

        long pixId = library.importMetadata(imageName);

        viewer.appendToOutputLn("Successfully stored to dataset \""
                + library.getDataset() + "\" with id \"" + pixId + "\".");
        viewer.appendToOutputLn("> [" + index + "] Importing pixel data for "
                + "image \"" + shortName + "\"... ");

        qTable.setProgressInfo(index, count);

        // viewer.appendToOutput("> Importing plane: ");
        library.importData(pixId, fileName, new ImportLibrary.Step() {

            @Override
            public void step(int i) {
                if (i < qTable.getMaximum()) {
                    qTable.setImportProgress(i);
                }
            }
        });

        viewer.appendToOutputLn("> Successfully stored with pixels id \""
                + pixId + "\".");
        viewer.appendToOutputLn("> [" + index
                + "] Image imported successfully!");

        if (archive == true) {
            File[] files = new File[1];
            ;
            files[0] = file;
            qTable.setProgressArchiving(index);
            store.writeFilesToFileStore(files, pixId);
        }

        qTable.setProgressDone(index);

        return pixId;

    }

    /** Opens the given file using the ImageReader. */
    public void open(String fileName) {
        viewer.waitCursor(true);
        try {
            library.open(fileName);
        } catch (Exception exc) {
            exc.printStackTrace();
            viewer.appendToDebugLn(exc.toString());
            viewer.waitCursor(false);
            return;
        }
        viewer.waitCursor(false);
    }
}

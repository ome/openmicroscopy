/*
 *   Copyright (C) 2009-2014 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.importer.cli;

import static ome.formats.importer.ImportEvent.*;
import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportEvent;

import omero.model.IObject;
import omero.model.Pixels;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic import process monitor that writes information to the log.
 *
 * @author Chris Allan <callan@glencoesoftware.com>
 *
 */
public class LoggingImportMonitor implements IObserver
{
    private static Logger log = LoggerFactory.getLogger(LoggingImportMonitor.class);

    private final ImportSummary importSummary = new ImportSummary();

    public void update(IObservable importLibrary, ImportEvent event)
    {
        if (event instanceof IMPORT_DONE) {
            IMPORT_DONE ev = (IMPORT_DONE) event;
            log.info(event.toLog());

            // send the import results to stdout
            // to enable external tools integration
            outputGreppableResults(ev);
            importSummary.update(ev);
        } else if (event instanceof IMPORT_SUMMARY) {
            IMPORT_SUMMARY ev = (IMPORT_SUMMARY) event;
            importSummary.setTime(ev.importTime);
            importSummary.setErrors(ev.errorCount);
            importSummary.report();
        } else if (event instanceof FILESET_UPLOAD_PREPARATION) {
            log.info(event.toLog());
        } else if (event instanceof FILESET_UPLOAD_START) {
            log.info(event.toLog());
        } else if (event instanceof FILESET_UPLOAD_END) {
            log.info(event.toLog());
        } else if (event instanceof FILE_UPLOAD_STARTED) {
            FILE_UPLOAD_STARTED ev = (FILE_UPLOAD_STARTED) event;
            log.info(event.toLog() + ": " + ev.filename);
        } else if (event instanceof FILE_UPLOAD_COMPLETE) {
            FILE_UPLOAD_COMPLETE ev = (FILE_UPLOAD_COMPLETE) event;
            log.info(event.toLog() + ": " + ev.filename);
            importSummary.update(ev);
        } else if (event instanceof PROGRESS_EVENT) {
            log.info(event.toLog());
        } else if (log.isDebugEnabled()) {
            log.debug(event.toLog());
        }
    }

    /**
     * Displays a list of successfully imported Pixels IDs on standard output.
     *
     * Note that this behaviour is intended for other command line tools
     * to pipe/grep the import results, and should be kept as is.
     *
     * @param ev the end of import event.
     */
    private void outputGreppableResults(IMPORT_DONE ev) {
        System.err.println("Imported pixels:");
        for (Pixels p : ev.pixels) {
            System.out.println(p.getId().getValue());
        }

        System.err.println("Other imported objects:");
        System.err.print("Fileset:");
        System.err.println(ev.fileset.getId().getValue());
        for (IObject object : ev.objects) {
            if (object != null && object.getId() != null) {
                // Not printing to stdout since the contract at the moment
                // is that only pixel IDs hit stdout.
                String kls = object.getClass().getSimpleName();
                if (kls.endsWith("I")) {
                    kls = kls.substring(0,kls.length()-1);
                }
                System.err.print(kls);
                System.err.print(":");
                System.err.println(object.getId().getValue());
            }
        }
    }

    /**
     * Placeholder used for printing a final import summary.
     */
    private class ImportSummary {
        private final static String PLATE_CLASS = "PlateI";

        private int createdFilesets;
        private int createdPlates;
        private int errors;
        private int importedImages;
        private int uploadedFiles;

        /** Time taken by import in milliseconds. **/
        private long time;

        /**
         * Updates the state of the object using information held by given even
         * type.
         * @param event An import event.
         */
        public void update(IMPORT_DONE event) {
            importedImages += event.pixels.size();
            createdFilesets++;
            for (IObject object : event.objects) {
                if (PLATE_CLASS.equals(object.getClass().getSimpleName())) {
                    createdPlates++;
                }
            }
        }

        /**
         * Updates the state of the object using information held by given event
         * type.
         * @param event An import event.
         */
        public void update(FILE_UPLOAD_COMPLETE event) {
            uploadedFiles++;
        }

        /**
         * Sets the import error count to the given number.
         * @param errors Count.
         */
        public void setErrors(int errors) {
            this.errors = errors;
        }

        /**
         * Sets the time taken by import to given value.
         * @param time The time in milliseconds.
         */
        public void setTime(long time) {
            this.time = time;
        }

        /**
         * Prints out the import summary to stderr.
         */
        public void report() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n==> Summary\n");
            sb.append(entityCountToString("file", uploadedFiles));
            sb.append(" uploaded, ");
            sb.append(entityCountToString("fileset", createdFilesets));
            if (createdPlates != 0) {
                sb.append(", ");
                sb.append(entityCountToString("plate", createdPlates));
            }
            sb.append(" created, ");
            sb.append(entityCountToString("image", importedImages));
            sb.append(" imported, ");
            sb.append(entityCountToString("error", errors));
            sb.append(String.format(" in %s\n",
                    DurationFormatUtils.formatDurationHMS(time)));
            System.err.print(sb.toString());
        }

        /**
         * Returns a string with a digit and singular/plural form of the
         * provided entity name (e.g. "3 apples", "1 car").
         * @param name The name of the entity used in the output.
         * @param count The number of entity elements.
         * @return See above.
         */
        private String entityCountToString(String name, int count) {
            return String.format("%d %s%s", count, name, count != 1 ? "s" : "");
        }
    }
}

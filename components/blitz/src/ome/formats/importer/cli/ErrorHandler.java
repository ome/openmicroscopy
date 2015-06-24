/*
 *   $Id$
 *
 *   Copyright (C) 2008-2013 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer.cli;

import static ome.formats.importer.ImportEvent.*;
import loci.formats.FormatReader;
import loci.formats.FormatTools;
import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.ImportCandidates.SCANNING;
import ome.formats.importer.util.ErrorContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IObserver} based on the gui ErrorHandler code which collects all
 * exceptions during
 * {@link ImportLibrary#importCandidates(ome.formats.importer.ImportConfig, ome.formats.importer.ImportCandidates)}
 * and after the import is finished offers to submit them via the feedback
 * system.
 *
 * @since Beta4.1
 */
public class ErrorHandler extends ome.formats.importer.util.ErrorHandler {

    private final static Logger log = LoggerFactory.getLogger(ErrorHandler.class);

    public ErrorHandler(ImportConfig config) {
        super(config);
    }

    public void onUpdate(IObservable importLibrary, ImportEvent event) {

        if (event instanceof IMPORT_DONE) {
            log.info("Number of errors: " + errors.size());
        }

        else if (event instanceof SCANNING) {
            log.debug(event.toLog());
        }

        else if (event instanceof ImportEvent.DEBUG_SEND) {
            boolean plate = false;
            for (ErrorContainer error : errors) {
                error.setEmail(config.email.get());
                error.setComment("Sent from CLI");
                if (!plate) {
                    ImportContainer ic = icMap.get(
                            error.getSelectedFile().getAbsolutePath());
                    if (ic != null) {
                        Boolean b = ic.getIsSPW();
                        plate = (b != null && b.booleanValue());
                    }
                }
            }
            if (errors.size() > 0) {
                // Note: it wasn't the intent to have these variables set
                // here. This requires that subclasses know to call
                // super.onUpdate(). To prevent that, one could make this method
                // final and have an onOnUpdate, etc.
                sendFiles = ((ImportEvent.DEBUG_SEND) event).sendFiles;
                sendLogs = ((ImportEvent.DEBUG_SEND) event).sendLogs;
                if (plate) {
                    log.info("To submit HCS data, please e-mail us.");
                    sendFiles = false;
                }
                log.info("Sending error report "+ "(" + errors.size() + ")...");
                sendErrors();
                if (sendFiles) {
                    if (sendLogs)
                        log.info("Sent files and log file.");
                    else log.info("Sent files.");
                } else {
                    if (sendLogs) {
                        log.info("Sent log file.");
                    }
                }
            }

        }

    }

}

/*
 *   Copyright (C) 2009-2013 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.importer.cli;

import static ome.formats.importer.ImportEvent.*;
import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportEvent;
import omero.model.Pixels;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Basic import process monitor that writes information to the log.
 * 
 * @author Chris Allan <callan@glencoesoftware.com>
 *
 */
public class LoggingImportMonitor implements IObserver
{
    private static Log log = LogFactory.getLog(LoggingImportMonitor.class);
    
    public void update(IObservable importLibrary, ImportEvent event)
    {
        if (event instanceof IMPORT_DONE) {
            IMPORT_DONE ev = (IMPORT_DONE) event;
            log.info(event.toLog());

            // send the import results to stdout
            // to enable external tools integration
            outputGreppableResults(ev);
        }
        else if (log.isDebugEnabled()) {
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
    }
}

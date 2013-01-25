/*
 *   Copyright (C) 2009-2011 University of Dundee & Open Microscopy Environment.
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
        } else if (event instanceof FILE_UPLOAD_STARTED) {
            FILE_UPLOAD_STARTED ev = (FILE_UPLOAD_STARTED) event;
            log.info(event.toLog());
        } else if (event instanceof FILE_UPLOAD_COMPLETE) {
            FILE_UPLOAD_COMPLETE ev = (FILE_UPLOAD_COMPLETE) event;
            log.info(event.toLog() + ": " + ev.filename);
        } else if (event instanceof FILE_UPLOAD_FINISHED) {
            FILE_UPLOAD_FINISHED ev = (FILE_UPLOAD_FINISHED) event;
            log.info(event.toLog());
        } else if (log.isDebugEnabled()) {
            log.debug(event.toLog());
        }
    }
}

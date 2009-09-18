/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer.cli;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportLibrary;

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

    public ErrorHandler(ImportConfig config) {
        super(config);
    }

    public void update(IObservable importLibrary, ImportEvent event) {
        if (event instanceof ImportEvent.DEBUG_SEND) {
            sendFiles = ((ImportEvent.DEBUG_SEND) event).sendFiles;
            sendErrors();
        }
    }

}

/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import java.util.List;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.ImportCandidates.SCANNING;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  A helper class used to obtain the import candidates
 *
 * @since Beta4.1
 */
public class ImportableFiles implements IObserver {

    private final static Log log = LogFactory.getLog(ImportableFiles.class);
    private ImportConfig config;
    private ImportCandidates candidates;
    private List<ImportContainer> containers;


    ImportableFiles(String[] paths, int depth) {
        final IObserver self = this;
        // Default config for now, may need to set some details?
        config = new ImportConfig();
        //config.configureDebug(1);
        OMEROWrapper reader = new OMEROWrapper(config);
        candidates = new ImportCandidates(depth, reader, paths, self);
        containers = candidates.getContainers();  
    }

    public List<ImportContainer> getContainers() {
        return containers;
    }
    
    @SuppressWarnings("unchecked")
    /**
     * Report any raised events
     * 
     */
    public void update(IObservable observable, ImportEvent event)
    {
        if (event instanceof SCANNING) {
            log.info(event.toLog());
        }    
    }

}

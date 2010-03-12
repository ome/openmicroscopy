/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.OMEROWrapper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  A helper class used to obtain the mport candidates
 *
 * @since Beta4.1
 */
public class ImportableFiles implements IObserver {

    private final static Log log = LogFactory.getLog(ImportableFiles.class);
    private ImportConfig config;
    private ImportCandidates candidates;
    private List<ImportContainer> containers;


    ImportableFiles(String[] paths) {
        final IObserver self = this;
        // Default config for now, may need to set some details?
        config = new ImportConfig();
        config.configureDebug(1);
        OMEROWrapper reader = new OMEROWrapper(config);
        // The first argument here is depth, it doesn't seem to work.
        candidates = new ImportCandidates(0, reader, paths, self);
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
        // For now just log a dummy, need to resolve ImportEvents.
        log.info(String.format("ImportCanditates called updates."));
    }

}

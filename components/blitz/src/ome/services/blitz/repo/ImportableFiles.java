/*
 * ome.services.blitz.repo.ImportableFiles
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 *
 *
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
 * @author Colin Blackburn <cblackburn at dundee dot ac dot uk>
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

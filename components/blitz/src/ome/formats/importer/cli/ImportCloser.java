/*
 * Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.formats.importer.cli;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import loci.formats.in.DefaultMetadataOptions;
import loci.formats.in.MetadataLevel;
import loci.formats.meta.MetadataStore;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.exclusions.AbstractFileExclusion;
import ome.formats.importer.exclusions.FileExclusion;
import ome.formats.importer.transfers.AbstractFileTransfer;
import ome.formats.importer.transfers.CleanupFailure;
import ome.formats.importer.transfers.FileTransfer;
import ome.formats.importer.transfers.UploadFileTransfer;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;
import omero.cmd.HandlePrx;
import omero.cmd.Response;
import omero.grid.ImportProcessPrx;
import omero.grid.ImportProcessPrxHelper;
import omero.model.Annotation;
import omero.model.CommentAnnotationI;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stateful class for use by {@link CommandLineImporter} to interact
 * with server-side processes which are running.
 */
class ImportCloser {

    private final static Logger log = LoggerFactory.getLogger(ImportCloser.class);

    List<ImportProcessPrx> imports;
    int closed = 0;
    int errors = 0;
    int processed = 0;

    ImportCloser(OMEROMetadataStoreClient client) throws Exception {
        this.imports = getImports(client);
    }

    void closeCompleted() {
        for (ImportProcessPrx imPrx : imports) {
            try {
                processed++;
                String logName = imPrx.toString().split("\\s")[0];
                HandlePrx handle = imPrx.getHandle();
                if (handle != null) {
                    Response rsp = handle.getResponse();
                    if (rsp != null) {
                        log.info("Done: {}", logName);
                        imPrx.close();
                        closed++;
                        continue;
                    }
                }
                log.info("Running: {}", logName);
            } catch (Exception e) {
                errors++;
                log.warn("Failure accessing service", e);
            }
        }
    }

    int getClosed() {
        return closed;
    }

    int getErrors() {
        return errors;
    }

    int getProcessed() {
        return processed;
    }

    private static List<ImportProcessPrx> getImports(OMEROMetadataStoreClient client) throws Exception {
        final List<ImportProcessPrx> rv = new ArrayList<ImportProcessPrx>();
        final ServiceFactoryPrx sf = client.getServiceFactory();
        final List<String> active = sf.activeServices();
        for (String service : active) {
            try {
                final ServiceInterfacePrx prx = sf.getByName(service);
                final ImportProcessPrx imPrx = ImportProcessPrxHelper.checkedCast(prx);
                if (imPrx != null) {
                    try {
                        imPrx.ice_ping();
                        rv.add(imPrx);
                    } catch (Ice.ObjectNotExistException onee) {
                        // ignore
                    }
                }
            } catch (Exception e) {
                log.warn("Failure accessing active service", e);
            }
        }
        return rv;
    }
}

/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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

package omero.gateway;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.util.ProportionalTimeEstimatorImpl;
import ome.formats.importer.util.TimeEstimator;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumProviderFactoryImpl;
import omero.ChecksumValidationException;
import omero.cmd.HandlePrx;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.ImportException;
import omero.gateway.model.SecurityContext;
import omero.grid.ImportProcessPrx;
import pojos.DatasetData;
import pojos.ExperimenterData;

public class SimpleImporter {

    /* checksum provider factory for verifying file integrity in upload */
    private static final ChecksumProviderFactory checksumProviderFactory = new ChecksumProviderFactoryImpl();

    Gateway gateway;

    public SimpleImporter(Gateway gateway) {
        this.gateway = gateway;
    }

    public void importFile(SecurityContext ctx, File file, DatasetData dataset,
            IObserver observer, ExperimenterData user) throws ImportException,
            DSAccessException, DSOutOfServiceException {

        ImportCandidates ic = getImportCandidates(ctx, file, observer);
        ImportContainer c = ic.getContainers().get(0);
        c.setTarget(dataset.asIObject());
        importImageFile(ctx, c, observer, user);
    }

    ImportCandidates getImportCandidates(SecurityContext ctx, File file,
            IObserver observer) throws ImportException {
        OMEROWrapper reader = null;
        try {
            ImportConfig config = new ImportConfig();
            reader = new OMEROWrapper(config);
            String[] paths = new String[1];
            paths[0] = file.getAbsolutePath();
            ImportCandidates icans = new ImportCandidates(reader, paths,
                    observer);
            return icans;
        } catch (Throwable e) {
            throw new ImportException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    Object importImageFile(SecurityContext ctx, ImportContainer c,
            IObserver observer, ExperimenterData user)
            throws DSAccessException, DSOutOfServiceException {
        ImportConfig config = new ImportConfig();

        OMEROMetadataStoreClient omsc = null;
        OMEROWrapper reader = null;
        omsc = gateway.getImportStore(ctx, user.getUserName());
        reader = new OMEROWrapper(config);
        ImportLibrary library = new ImportLibrary(omsc, reader);
        library.addObserver(observer);

        try {
            final ImportProcessPrx proc = library.createImport(c);

            final String[] srcFiles = c.getUsedFiles();
            final List<String> checksums = new ArrayList<String>();
            final byte[] buf = new byte[omsc.getDefaultBlockSize()];

            final TimeEstimator estimator = new ProportionalTimeEstimatorImpl(
                    c.getUsedFilesTotalSize());

            for (int i = 0; i < srcFiles.length; i++) {
                checksums.add(library.uploadFile(proc, srcFiles, i,
                        checksumProviderFactory, estimator, buf));
            }

            HandlePrx handle = null;
            Map<Integer, String> failingChecksums = null;
            try {
                handle = proc.verifyUpload(checksums);
            } catch (ChecksumValidationException cve) {
                failingChecksums = cve.failingChecksums;
                return new ImportException(cve);
            } finally {
                try {
                    proc.close();
                } catch (Exception e) {
                    System.out.println("Cannot close import process.");
                }
                library.notifyObservers(new ImportEvent.FILESET_UPLOAD_END(
                        null, 0, srcFiles.length, null, null, srcFiles,
                        checksums, failingChecksums, null));
            }
            return library.createCallback(proc, handle, c);

        } catch (Throwable e) {
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception ex) {
            }

            return new ImportException(e);
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception ex) {
            }
            try {
                gateway.closeImport(ctx, user.getUserName());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}

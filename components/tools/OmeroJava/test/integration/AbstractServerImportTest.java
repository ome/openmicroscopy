/*
 * Copyright (C) 2012-2013 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package integration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import loci.formats.in.FakeReader;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.ImportLibrary.ImportCallback;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.util.ProportionalTimeEstimatorImpl;
import ome.formats.importer.util.TimeEstimator;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumProviderFactoryImpl;

import omero.cmd.HandlePrx;
import omero.grid.ImportLocation;
import omero.grid.ImportProcessPrx;
import omero.grid.ImportRequest;
import omero.model.IObject;

import org.testng.Assert;

public class AbstractServerImportTest extends AbstractServerTest {

    /**
     * Import the given files. Like {@link #importFileset(List, int)} but with
     * all the srcPaths to be uploaded.
     *
     * @param srcPaths
     *            the source paths
     * @return the resulting import location
     * @throws Exception
     *             unexpected
     */
    protected ImportLocation importFileset(List<String> srcPaths) throws Exception {
        return importFileset(srcPaths, srcPaths.size(), null);
    }

    /**
     * Import the given files.
     *
     * @param srcPaths
     *            the source paths
     * @param numberToUpload
     *            how many of the source paths to actually upload
     * @return the resulting import location
     * @throws Exception
     *             unexpected
     */
    protected ImportLocation importFileset(List<String> srcPaths, int numberToUpload, IObject targetObject) throws Exception {

        // Setup that should be easier, most likely a single ctor on IL
        OMEROMetadataStoreClient client = new OMEROMetadataStoreClient();
        client.initialize(this.client);
        OMEROWrapper wrapper = new OMEROWrapper(new ImportConfig());
        ImportLibrary lib = new ImportLibrary(client, wrapper);

        // This should also be simplified.
        ImportContainer container = new ImportContainer(new File(
                srcPaths.get(0)), targetObject /* target */, null /* user pixels */,
                FakeReader.class.getName(), srcPaths.toArray(new String[srcPaths.size()]),
                false /* isspw */);

        // Now actually use the library.
        ImportProcessPrx proc = lib.createImport(container);

        // The following is largely a copy of ImportLibrary.importImage
        final String[] srcFiles = container.getUsedFiles();
        final List<String> checksums = new ArrayList<String>();
        final byte[] buf = new byte[client.getDefaultBlockSize()];
        final ChecksumProviderFactory cpf = new ChecksumProviderFactoryImpl();
        final TimeEstimator estimator = new ProportionalTimeEstimatorImpl(
                container.getUsedFilesTotalSize());

        for (int i = 0; i < numberToUpload; i++) {
            checksums.add(lib.uploadFile(proc, srcFiles, i, cpf, estimator,
                    buf));
        }

        // At this point the import is running, check handle for number of
        // steps.
        final HandlePrx handle = proc.verifyUpload(checksums);
        final ImportRequest req = (ImportRequest) handle.getRequest();
        final ImportCallback cb = lib.createCallback(proc, handle, container);
        cb.loop(60 * 60, 1000); // Wait 1 hr per step.
        Assert.assertNotNull(cb.getImportResponse());
        return req.location;
    }
}

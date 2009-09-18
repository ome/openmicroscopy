/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.utests;

import junit.framework.TestCase;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportFixture;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.ImportReader;
import ome.formats.importer.ImportReport;
import ome.formats.importer.OMEROWrapper;
import omero.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;

/**
 * Simple tests which show the basic way to configure a client.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @see ImportFixture
 * @see ImportReader
 * @see ImportConfig
 * @see ImportLibrary
 * @see ImportCandidates
 * @see OMEROWrapper
 * @see OMEROMetadataStoreClient
 * 
 * @since Beta4.1
 */
public class BasicImporterClientTest extends TestCase {

    Log log = LogFactory.getLog(BasicImporterClientTest.class);

    /**
     * 
     */
    @Test
    public void testSimplestImport() {

        final ImportConfig config = new ImportConfig();

        OMEROMetadataStoreClient client = null;
        try {
            client = new OMEROMetadataStoreClient(config);
            OMEROWrapper reader = new OMEROWrapper(config);
            ImportCandidates candidates = new ImportCandidates(reader,
                    new String[] {});
            ImportLibrary library = new ImportLibrary(client, reader);
            library.importCandidates(config, candidates);
        } finally {
            if (client != null) {
                client.logout();
            }
            config.save(); // Saves any changes.
        }
    }

    @Test
    public void testErrorHandling() {

        final ImportConfig config = new ImportConfig();

        OMEROMetadataStoreClient client;
        try {
            client = new OMEROMetadataStoreClient(config);
            OMEROWrapper reader = new OMEROWrapper(config);
            ImportCandidates candidates = new ImportCandidates(reader,
                    new String[] {});
            ImportLibrary library = new ImportLibrary(client, reader);

            // importCandidates never throws exception
            library.importCandidates(config, candidates);

            // so to handle exceptions, we register an oberver.
            library.addObserver(FIXME);
            library.importCandidates(config, candidates);

        } finally {
            if (client != null) {
                client.logout();
            }
            config.save();
        }

    }

}

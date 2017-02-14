/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.utests;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportFixture;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.cli.ErrorHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Test(groups={"manual"})
public class BasicImporterClientTest {

    Logger log = LoggerFactory.getLogger(BasicImporterClientTest.class);

    @Test
    public void testSimpleClientWthErrorHandling() throws Exception {

        final ImportConfig config = new ImportConfig();
        final ErrorHandler handler = new ErrorHandler(config);

        OMEROMetadataStoreClient client = null;
        try {
            client = config.createStore();
            OMEROWrapper reader = new OMEROWrapper(config);
            ImportCandidates candidates = new ImportCandidates(reader, new String[] {}, handler);
            ImportLibrary library = new ImportLibrary(client, reader);

            // importCandidates never throws exception
            library.importCandidates(config, candidates);

            // so to handle exceptions, we register an observer.
            library.addObserver(new ErrorHandler(config));
            library.importCandidates(config, candidates);

        } finally {
            if (client != null) {
                client.logout();
            }
            config.saveAll();
        }

    }

}

/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.utests;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import junit.framework.TestCase;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportFixture;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.reactor.Connector;
import ome.formats.importer.reactor.Fileset;
import ome.formats.importer.reactor.ImportReactor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;

/**
 * Unit tests on the {@link Reactor}
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @see ImportFixture
 * @see ImportReader
 * @see ImportConfig
 * @see ImportLibrary
 * @see ImportCandidates
 * @see OMEROWrapper
 * @see OMEROMetadataStoreClient
 * @see IObserver
 * 
 * @since Beta4.1
 */
public class ReactorTest extends TestCase {

    Log log = LogFactory.getLog(ReactorTest.class);
    ImportReactor reactor;
    ImportConfig config;
    MockConnector connector;

    @Test
    public void testSimple() throws Exception {
        config = new ImportConfig();
        connector = new MockConnector();
        reactor = new ImportReactor(config, connector);
        reactor.resumeReactor();
        reactor.add(new File("/tmp/does-not-exist"), "foo", "bar");
        reactor.runSingle();
        assertEquals(1, connector.opened.size());
    }

}

class MockConnector implements Connector {
    final Map<String, Fileset> opened = new HashMap<String, Fileset>();
    final Map<String, ImportContainer> handled = new HashMap<String, ImportContainer>();
    final Set<String> closed = new HashSet<String>();
    final Set<String> failed = new HashSet<String>();

    public String openFileset(Fileset fixture) {
        String uuid = UUID.randomUUID().toString();
        opened.put(uuid, fixture);
        return uuid;
    }

    public void handleContainer(String uuid, ImportContainer container) {
        handled.put(uuid, container);
    }

    public void failFileset(String uuid, Exception exc) {
        failed.add(uuid);
    }

    public void closeFileset(String uuid) {
        closed.add(uuid);
    }

};

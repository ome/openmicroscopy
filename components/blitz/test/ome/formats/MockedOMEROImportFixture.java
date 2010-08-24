/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats;

import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.services.blitz.test.mock.MockFixture;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;
import omero.api.ServiceFactoryPrx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.MockObjectTestCase;

/**
 * test fixture for importing files without a GUI. Sample usage:
 * 
 * <pre>
 * OMEROMetadataStoreClient client = new OMEROMetadataStoreClient(sf);
 * OMEROImportFixture fixture = new OMEROImportFixture(client);
 * fixture.setUp();
 * fixture.setFile(ResourceUtils.getFile(&quot;classpath:tinyTest.d3d.dv&quot;));
 * fixture.setName(name);
 * fixture.doImport();
 * List&lt;Pixels&gt; p = fixture.getPixels();
 * fixture.tearDown();
 * i.setName(name);
 * i = userSave(i);
 * </pre>
 * 
 * This class is <em>not</em> thread safe.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @see OMEROMetadataStoreClient
 * @since 4.0
 */
public class MockedOMEROImportFixture extends OMEROImportFixture {

    Log log = LogFactory.getLog(MockedOMEROImportFixture.class);

    omero.client client;

    /**
     * Constructor for use when no blitz is available, like from server-side
     * tests.
     */
    public MockedOMEROImportFixture(ServiceFactory sf, String password)
            throws Exception {
        super(mockStore(sf, password), new OMEROWrapper(new ImportConfig()));
    }

    public static OMEROMetadataStoreClient mockStore(ServiceFactory sf,
            String password) throws Exception {

        OmeroContext inner = sf.getContext();
        OmeroContext outer = new OmeroContext(new String[] {
                "classpath:ome/formats/fixture.xml",
                "classpath:ome/services/blitz-servantDefinitions.xml",
                "classpath:ome/services/throttling/throttling.xml",
                "classpath:ome/config.xml" }, false);
        outer.setParent(inner);
        outer.refresh();

        String username = sf.getAdminService().getEventContext()
                .getCurrentUserName();

        MockFixture fixture = new MockFixture(new MockObjectTestCase() {
        }, outer);
        omero.client client = fixture.newClient();
        client.createSession(username, password);
        OMEROMetadataStoreClient store = new OMEROMetadataStoreClient();
        store.initialize(client);
        return store;
    }

    @Override
    public void tearDown() {
        try {
            super.tearDown();
        } catch (Exception e) {
            log.error("Error on tearDown in store.logout()", e);
        }
    }
}

/*
 * ome.formats.testclient.ExampleIntegrationTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.testclient;

import java.io.File;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

import ome.formats.OMEROMetadataStore;
import ome.formats.importer.ImportFixture;
import ome.formats.importer.ImportLibrary;
import ome.model.containers.Dataset;
import ome.system.Login;
import ome.system.ServiceFactory;

import junit.framework.TestCase;

@Test(groups = "integration")
public class ExampleIntegrationTest extends TestCase {

    private final static Log log = LogFactory
            .getLog(ExampleIntegrationTest.class);

    protected ImportFixture fixture;

    public void testUseHardCoded() throws Exception {
        ServiceFactory sf = new ServiceFactory(new Login("root", "ome"));

        Dataset d = new Dataset();
        d.setName(UUID.randomUUID().toString());
        d = sf.getUpdateService().saveAndReturnObject(d);

        OMEROMetadataStore store = new OMEROMetadataStore(sf);

        String file = "tinyTest.d3d.dv";
        File tinyTest = ResourceUtils.getFile("classpath:" + file);

        fixture = new ImportFixture(store);
        fixture.put(tinyTest, d);

        fixture.setUp();
        fixture.doImport(new ImportLibrary.Step() {
            @Override
            public void step(int n) {
                log.debug("Wrote plane:" + n);
            }
        });
        fixture.tearDown();

    }

}

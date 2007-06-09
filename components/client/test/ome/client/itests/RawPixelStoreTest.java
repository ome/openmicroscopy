/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client.itests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.*;

import junit.framework.TestCase;

import ome.api.IUpdate;
import ome.api.RawPixelsStore;
import ome.model.core.Pixels;
import ome.model.meta.Experimenter;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;

@Test(groups = { "client", "integration", "binary" })
public class RawPixelStoreTest extends TestCase {

    private static Log log = LogFactory.getLog(RawPixelStoreTest.class);

    ServiceFactory sf;
    RawPixelsStore raw;
    IUpdate iUpdate;

    @Configuration( beforeTestMethod = true )
    public void setup() {
        sf = new ServiceFactory();
        raw = sf.createRawPixelsStore();
        iUpdate = sf.getUpdateService();
    }

    @Test
    public void test_simpleDigest() throws Exception {
        try {
            sf.getQueryService().get(Experimenter.class, 0L);
        } catch (Exception e) {
            log.error("Exception logged.", e.getCause());
            // ok. http://bugs.openmicroscopy.org.uk/show_bug.cgi?id=649
        }

        Pixels pix = ObjectFactory.createPixelGraph(null);
        pix.setSizeX(16);
        pix.setSizeY(16);
        pix.setSizeZ(1);
        pix.setSizeT(1);
        pix.setSizeC(1);
        pix = iUpdate.saveAndReturnObject(pix);

        raw.setPixelsId(pix.getId());
        raw.calculateMessageDigest();

    }

}

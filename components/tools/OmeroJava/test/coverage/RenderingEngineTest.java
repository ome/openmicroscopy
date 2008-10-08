/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.coverage;

import java.io.File;

import ome.icy.model.itests.IceTest;
import omero.api.RenderingEnginePrx;

import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

@Test(groups = { "unfinished", "ignore", "ticket:607" })
public class RenderingEngineTest extends IceTest {

    omero.client ice;

    @Test
    public void testRenderingEngineInit() throws Exception {
        File f1 = ResourceUtils.getFile("classpath:ice.config");
        File f2 = ResourceUtils.getFile("classpath:local.properties");
        ice = new omero.client(f1, f2);
        ice.createSession(null, null);
        RenderingEnginePrx prx = ice.getServiceFactory()
                .createRenderingEngine();
        assertNotNull(prx);
        prx.load();
        ice.closeSession();
    }
}

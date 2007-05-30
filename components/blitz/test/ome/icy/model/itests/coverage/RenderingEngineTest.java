/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.coverage;

import ome.icy.model.itests.IceTest;
import ome.services.blitz.client.IceServiceFactory;
import omero.api.IConfigPrx;
import omero.api.IConfigPrxHelper;
import omero.api.IUpdatePrx;
import omero.api.IUpdatePrxHelper;
import omero.api.RenderingEnginePrx;
import omero.constants.CONFIGSERVICE;
import omero.constants.UPDATESERVICE;

import org.testng.annotations.Test;

@Test( groups = { "unfinished", "ignore", "ticket:607" })
public class RenderingEngineTest extends IceTest {

    IceServiceFactory ice;

    @Test
    public void testRenderingEngineInit() throws Exception {
        ice = new IceServiceFactory(null, null, null);
        ice.createSession();
        RenderingEnginePrx prx = ice.createRenderingEngine(null);
        assertNotNull( prx );
        prx.load();
        ice.destroy();
    }
}

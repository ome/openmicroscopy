/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.old;

import omero.api.IConfigPrx;
import omero.api.IConfigPrxHelper;
import omero.api.IUpdatePrx;
import omero.api.IUpdatePrxHelper;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;

import org.testng.annotations.Test;

public class ServiceFactoryTest extends IceTest {

    omero.client ice;

    @Test
    public void testProvidesIConfig() throws Exception {
        ice = new omero.client();
        ice.createSession(null, null);
        Ice.ObjectPrx base = ice.getSession().getConfigService();
        IConfigPrx prx = IConfigPrxHelper.checkedCast(base);
        assertNotNull(prx);
        ice.closeSession();
    }

    @Test
    public void testProvidesIUpdate() throws Exception {
        ice = new omero.client();
        ice.createSession(null, null);
        Ice.ObjectPrx base = ice.getSession().getUpdateService();
        IUpdatePrx prx = IUpdatePrxHelper.checkedCast(base);
        assertNotNull(prx);
        ice.closeSession();
    }

    @Test
    public void testProvidesRenderingEngine() throws Exception {
        ice = new omero.client();
        ice.createSession(null, null);
        RenderingEnginePrx prx = ice.getSession()
                .createRenderingEngine();
        assertNotNull(prx);
        ice.closeSession();
    }

    @Test
    public void testKeepAliveAndIsAliveWorkOnNewProxy() throws Exception {
        ice = new omero.client();
        ice.createSession(null, null);
        ServiceFactoryPrx session = ice.getSession();
        RenderingEnginePrx prx = session.createRenderingEngine();
        assertNotNull(prx);
        assertTrue(session.keepAlive(prx));
        assertTrue(0 == session.keepAllAlive(new ServiceInterfacePrx[] { prx }));
        ice.closeSession();
    }

    @Test
    public void testGetByNameFailsOnStatefulService() throws Exception {
        fail("NYI");
    }
}

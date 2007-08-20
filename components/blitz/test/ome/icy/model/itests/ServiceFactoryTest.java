/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests;

import ome.services.blitz.client.IceServiceFactory;
import omero.api.IConfigPrx;
import omero.api.IConfigPrxHelper;
import omero.api.IUpdatePrx;
import omero.api.IUpdatePrxHelper;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;

import org.testng.annotations.Test;

public class ServiceFactoryTest extends IceTest {

    IceServiceFactory ice;
    
    @Test
    public void testProvidesIConfig() throws Exception {
        ice = new IceServiceFactory(null, null, null);
        ice.createSession();
        Ice.ObjectPrx base = ice.getConfigService(null);
        IConfigPrx prx = IConfigPrxHelper.checkedCast(base);
        assertNotNull( prx );
        ice.destroy();
    }
    
    @Test
    public void testProvidesIUpdate() throws Exception {
        ice = new IceServiceFactory(null, null, null);
        ice.createSession();
        Ice.ObjectPrx base = ice.getUpdateService(null);
        IUpdatePrx prx = IUpdatePrxHelper.checkedCast(base);
        assertNotNull( prx );
        ice.destroy();
    }
    
    @Test
    public void testProvidesRenderingEngine() throws Exception {
        ice = new IceServiceFactory(null, null, null);
        ice.createSession();
        RenderingEnginePrx prx = ice.createRenderingEngine(null);
        assertNotNull( prx );
        ice.destroy();
    }
    
    @Test
    public void testKeepAliveAndIsAliveWorkOnNewProxy() throws Exception {
        ice = new IceServiceFactory(null, null, null);
        ice.createSession();
        ServiceFactoryPrx session = ice.getProxy();
        RenderingEnginePrx prx = ice.createRenderingEngine(null);
        assertNotNull( prx );
        assertTrue( session.keepAlive(prx));
        assertTrue( 0==session.keepAllAlive(new ServiceInterfacePrx[]{prx}));
        ice.destroy();
    }
    
    @Test
    public void testGetByNameFailsOnStatefulService() throws Exception {
    	fail("NYI");
    }
}

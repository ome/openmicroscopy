/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package coverage;

import java.io.File;

import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;

import org.springframework.util.ResourceUtils;
import org.testng.annotations.Test;

@Test(groups = { "unfinished", "ignore", "ticket:607" })
public class RenderingEngineTest 
	extends IceTest
{
    /**
     * Test the creation of a rendering engine.
     * @throws Exception If an error occurred.
     */
    @Test
    public void testRenderingEngineInit()
    	throws Exception
    {
        File f1 = ResourceUtils.getFile("classpath:ice.config");
        File f2 = ResourceUtils.getFile("classpath:local.properties");
        omero.client ice = new omero.client(f1, f2);
        ServiceFactoryPrx factory = ice.createSession(null, null);
        RenderingEnginePrx prx = factory.createRenderingEngine();
        assertNotNull(prx);
        prx.load();
        ice.closeSession();
        factory.destroy();
    }
    
}

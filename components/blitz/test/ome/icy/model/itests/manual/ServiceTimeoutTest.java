/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.itests.manual;

import ome.icy.fixtures.BlitzServerFixture;
import omero.api.RenderingEnginePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;

import org.testng.annotations.Test;

public class ServiceTimeoutTest extends MockedBlitzTest {

    @Test
    public void testkeepAllAliveAndkeepAliveWorkAfterPause() throws Exception {

        fixture = new BlitzServerFixture(200 /* not under test */, 2);
        fixture.methodCall();

        ServiceFactoryPrx session = fixture.createSession();

        RenderingEnginePrx prx1 = session.createRenderingEngine();
        RenderingEnginePrx prx2 = session.createRenderingEngine();
        assertNotNull(prx1);
        assertNotNull(prx2);
        assertTrue(session.keepAlive(prx1));
        assertTrue(0 == session
                .keepAllAlive(new ServiceInterfacePrx[] { prx1 }));
        Thread.sleep(1500L);
        assertTrue(session.keepAlive(prx1));
        assertTrue(0 == session
                .keepAllAlive(new ServiceInterfacePrx[] { prx1 }));
        Thread.sleep(1500L);
        assertTrue(session.keepAlive(prx1));
        assertTrue(0 == session
                .keepAllAlive(new ServiceInterfacePrx[] { prx1 }));
        Thread.sleep(1500L);
        assertTrue(session.keepAlive(prx1));
        assertTrue(0 == session
                .keepAllAlive(new ServiceInterfacePrx[] { prx1 }));
        Thread.sleep(1500L);
        assertFalse(session.keepAlive(prx2));
        assertFalse(0 == session
                .keepAllAlive(new ServiceInterfacePrx[] { prx2 }));

    }

}

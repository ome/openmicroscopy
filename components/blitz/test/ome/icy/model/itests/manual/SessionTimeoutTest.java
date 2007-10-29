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

import org.testng.annotations.Test;

public class SessionTimeoutTest extends MockedBlitzTest {

    @Test
    public void testkeepAllAliveKeepsSessionAlive() throws Exception {

        fixture = new BlitzServerFixture(3, 3);
        ServiceFactoryPrx session = fixture.createSession();

        RenderingEnginePrx prx = session.createRenderingEngine();
        assertNotNull(prx);
        assertTrue(session.keepAlive(prx));
        Thread.sleep(900L);
        assertTrue(session.keepAlive(prx));
        Thread.sleep(900L);
        assertTrue(session.keepAlive(prx));
        Thread.sleep(900L);
        assertTrue(session.keepAlive(prx));
        Thread.sleep(900L);
        assertTrue(session.keepAlive(prx));

    }

    @Test
    public void testSessionStillDiesThough() throws Exception {

        fixture = new BlitzServerFixture(3, 3);
        ServiceFactoryPrx session = fixture.createSession();

        RenderingEnginePrx prx = session.createRenderingEngine();
        assertNotNull(prx);
        assertTrue(session.keepAlive(prx));

        long time = System.currentTimeMillis();
        while (System.currentTimeMillis() < time + 4100L) {
            Thread.sleep(1000L);
        }

        try {
            assertFalse(session.keepAlive(prx));
            fail("This shouldn't succeed.");
        } catch (Exception e) {
            // ok
        }
    }
}

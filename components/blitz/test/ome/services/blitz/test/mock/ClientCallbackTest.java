/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.mock;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Cache;
import ome.model.meta.Session;
import ome.services.messages.DestroySessionMessage;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the use of the client callback on session destruction
 */
public class ClientCallbackTest extends MockObjectTestCase {

    Cache cache;
    Session session;
    omero.client client;
    MockFixture fixture;

    @BeforeMethod(groups = "integration")
    public void setup() throws Exception {
        fixture = new MockFixture(this);
        client = fixture.newClient();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws Exception {
        if (client != null) {
            // doesn't throw
            client.closeSession();
        }
        if (fixture != null) {
            fixture.tearDown();
        }
    }

    @Test(groups = "integration")
    public void testBasic() throws Exception {
        session = fixture.session("uuid-basic");
        cache = fixture.cache();
        fixture.prepareServiceFactory(session, cache);
        client.createSession("a", "b");


        fixture.prepareClose(0);
        client.closeSession();
    }

    @Test(groups = "integration")
    public void testServerCloses() throws Exception {
        session = fixture.session("uuid-serverCloses");
        cache = fixture.cache();
        fixture.prepareServiceFactory(session, cache);
        client.createSession("a", "b");

        fixture.prepareClose(0);
        fixture.ctx.publishEvent(new DestroySessionMessage(this,
                "uuid-serverCloses"));
    }

    @Test(groups = "integration")
    public void testClientReceivesHeartRequest() throws Exception {
        session = fixture.session("uuid-clientReceivesHeartRequest");
        cache = fixture.cache();
        fixture.prepareServiceFactory(session, cache);
        client.createSession("a", "b");

        fixture.prepareClose(0);
        final CyclicBarrier barrier = new CyclicBarrier(2);
        Runnable r = new Runnable(){
            public void run() {
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }};
            client.onHeartbeat(r);
            barrier.await(10, TimeUnit.SECONDS);
            assertEquals(0, barrier.getNumberWaiting());
            assertFalse(barrier.isBroken());
    }
}

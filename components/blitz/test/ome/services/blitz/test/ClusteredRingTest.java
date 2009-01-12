/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test;

import java.util.Set;

import ome.services.blitz.fire.Discovery;
import ome.services.blitz.fire.Ring;
import ome.services.blitz.test.mock.MockFixture;
import ome.system.OmeroContext;

import org.jmock.MockObjectTestCase;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
@Test(groups ={"integration","mock"})
public class ClusteredRingTest extends MockObjectTestCase {

    MockFixture fixture1, fixture2;
    Ring ring1, ring2;
    PlatformTransactionManager tm;
    TransactionStatus tx;

    @BeforeTest
    public void setup() throws Exception {

        // Setting up listener for discovery to speed up test
        final int[] count = new int[] { 0 };
        OmeroContext ctx = MockFixture.basicContext();
        ctx.addApplicationListener(new ApplicationListener() {
            public void onApplicationEvent(ApplicationEvent arg0) {
                if (arg0 instanceof Discovery.Finished) {
                    count[0]++;
                }
            }
        });

        // To run everything in a single transaction, uncomment;
        // tm = (PlatformTransactionManager) ctx.getBean("transactionManager");
        // TransactionAttribute ta = new DefaultTransactionAttribute(){
        // @Override
        // public boolean rollbackOn(Throwable ex) {
        // return false;
        // }
        // };
        // tx = tm.getTransaction(ta);

        fixture1 = new MockFixture(this);
        fixture2 = new MockFixture(this);
        long start = System.currentTimeMillis();
        while (count[0] < 2 && System.currentTimeMillis() < start + 10 * 1000L) {
            Thread.sleep(1000L);
        }
    }

    @AfterTest(alwaysRun = true)
    public void tearDown() throws Exception {
        if (fixture1 != null) {
            fixture1.tearDown();
        }
        if (fixture2 != null) {
            fixture2.tearDown();
        }
        // tm.commit(tx);
        Thread.sleep(1000L); // Give everything time to shutdown.
    }

    @Test
    public void testFirstTakesOver() throws Exception {
        ring1 = fixture1.blitz.getRing();
        ring2 = fixture2.blitz.getRing();
        assertEquals(ring1.getRedirect(), ring2.getRedirect());
    }

    @Test
    public void testAfterStartupNoNonActiveManagersArePresent()
            throws Exception {
        String key = "manager-foo";
        try {
            fixture1.jdbc.update(
                    "insert into session_ring (key, value) values (?,?)", key,
                    "bar");
        } catch (DataIntegrityViolationException dive) {
            // then this test has failed before. oh well.
        }

        final boolean[] called = new boolean[] { false };
        fixture1.ctx.addApplicationListener(new ApplicationListener() {
            public void onApplicationEvent(ApplicationEvent arg0) {
                if (arg0 instanceof Discovery.Finished) {
                    called[0] = true;
                }
            }
        });

        // Creating a new ring should automatically clean up "foo"
        MockFixture fixture3 = new MockFixture(this);
        long start = System.currentTimeMillis();
        while (! called[0] && System.currentTimeMillis() < start + 10*1000L) {
            Thread.sleep(1000L);
        }
        fixture3.tearDown();

        Set<String> managers = fixture1.blitz.getRing().knownManagers();
        assertFalse(managers.contains(key));
        assertEquals(0, fixture1.jdbc.queryForInt(
                "select count(key) from session_ring where key = ?", key));
    }

    @Test
    public void testHandlesMissingServers() throws Exception {
        fail();
    }

    @Test
    public void testRemovesUnreachable() throws Exception {
        fail();
    }

    @Test
    public void testReaddsSelfIfTemporarilyUnreachable() throws Exception {
        fail();
    }

    @Test
    public void testAllSessionRemovedIfDiscoveryFails() throws Exception {
        fail();
    }

    @Test
    public void testAllSessionsReassertedIfSessionComesBackOnline()
            throws Exception {
        fail();
    }
}
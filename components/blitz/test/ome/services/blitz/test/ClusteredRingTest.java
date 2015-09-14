/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import java.util.Set;

import ome.services.blitz.fire.Ring;
import ome.services.blitz.test.mock.MockFixture;
import ome.services.messages.CreateSessionMessage;

import org.jmock.MockObjectTestCase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
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

    @BeforeMethod
    public void createFixtures() throws Exception {

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
    }

    @AfterMethod(alwaysRun = true)
    public void teardownFixtures() throws Exception {
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
    public void testAfterStartupNoNonActiveManagersArePresent()
            throws Exception {
        String key = "manager-foo";
        try {
            /*
            MUST BE PORTED TO SQLACTION!
            fixture1.jdbc.update(
                    "insert into session_ring (key, value) values (?,?)", key,
                    "bar");
            */
        } catch (DataIntegrityViolationException dive) {
            // then this test has failed before. oh well.
        }

        // Creating a new ring should automatically clean up "foo"
        MockFixture fixture3 = new MockFixture(this);
        fixture3.tearDown();

        Set<String> managers = fixture1.blitz.getRing().knownManagers();
        assertFalse(managers.contains(key));
        /*
        MUST BE PORTED TO SQLACTION!
        assertEquals(0, fixture1.jdbc.queryForInt(
                "select count(key) from session_ring where key = ?", key));
        */
    }

    @Test
    public void testAddedSessionGetsUuidOfManager() throws Exception {
        fixture1.ctx.publishEvent(new CreateSessionMessage(this, "test-for-uuid"));
        assertTrue(fixture1.blitz.getRing().checkPassword("test-for-uuid"));
        /*
        MUST BE PORTED TO SQLACTION!
        String value = fixture1.jdbc.queryForObject("select value from session_ring where key = ?", String.class, "session-test-for-uuid");
        assertEquals(fixture1.blitz.getRing().uuid, value);
        */
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

    @Test
    public void testIfRedirectIsDeletedAnotherHostTakesOver() throws Exception {
        fail();
    }
}

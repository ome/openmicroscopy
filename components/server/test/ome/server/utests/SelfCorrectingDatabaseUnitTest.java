/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

import java.sql.SQLException;

import javax.sql.DataSource;

import ome.conditions.DatabaseBusyException;
import ome.services.db.SelfCorrectingDataSource;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { "db", "ticket:619" })
public class SelfCorrectingDatabaseUnitTest extends MockObjectTestCase {

    Mock mock;
    DataSource ds;
    SelfCorrectingDataSource self;

    class MySQLException extends SQLException {

    }

    @BeforeMethod
    public void setup() {
        mock = mock(DataSource.class);
        ds = (javax.sql.DataSource) mock.proxy();
        self = new SelfCorrectingDataSource(ds, 300000L, 0, 3000);
        mock.expects(atLeastOnce()).will(
                throwException(new MySQLException()));
    }

    @Test(expectedExceptions = DatabaseBusyException.class)
    public void testSimple() throws Exception {
        self.getConnection();
    }

    public void testLotsNoRetries() throws Exception {
        long backOff1 = assertFailsAndReturnBackOff();
        long backOff2 = assertFailsAndReturnBackOff();
        long backOff3 = assertFailsAndReturnBackOff();
        long backOff4 = assertFailsAndReturnBackOff();
        assertTrue(0 == backOff1);
        assertTrue(backOff1 <= backOff2);
        assertTrue(backOff2 <= backOff3);
        assertTrue(backOff3 <= backOff4);
    }

    public void testLotsWithReductionNoRetries() throws Exception {
        self = new SelfCorrectingDataSource(ds, 1L, 0, 3000); // Short time
        long backOff1 = assertFailsAndReturnBackOff();
        Thread.sleep(2L);
        long backOff2 = assertFailsAndReturnBackOff();
        Thread.sleep(2L);
        long backOff3 = assertFailsAndReturnBackOff();
        Thread.sleep(2L);
        long backOff4 = assertFailsAndReturnBackOff();
        assertTrue(0 == backOff1);
        assertTrue(backOff1 == backOff2);
        assertTrue(backOff2 == backOff3);
        assertTrue(backOff3 == backOff4);
    }

    @Test(timeOut = 30000)
    public void testRetries() throws Exception {
        self = new SelfCorrectingDataSource(ds, 30000L, 5, 3000); // 5 retries
        long backOff1 = assertFailsAndReturnBackOff();
        long backOff2 = assertFailsAndReturnBackOff();
        assertEquals(2000, backOff1);
        assertEquals(3000, backOff2);
    }
    // Helpers
    // =========================

    private long assertFailsAndReturnBackOff() throws SQLException {
        try {
            self.getConnection();
            throw new RuntimeException("should throw");
        } catch (DatabaseBusyException dbe) {
            return dbe.backOff;
        }
    }

}

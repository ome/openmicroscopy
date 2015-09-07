/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests.hibernate;

import java.sql.Connection;
import java.util.UUID;

import javax.sql.DataSource;

import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.model.core.Image;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.util.Executor;
import ome.system.ServiceFactory;
import ome.util.SqlAction;

import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "ticket:1176")
public class SequencesTest extends AbstractManagedContextTest {

    SqlAction sql;
    GenericGenerator gg;
    String seq_name;
    int incr_value;

    @BeforeClass
    public void setup() {
        gg = Image.class.getAnnotation(GenericGenerator.class);
        seq_name = gg.name();
        incr_value = -999990000;
        for (Parameter parameter : gg.parameters()) {
            if (parameter.name().equals("increment_size")) {
                incr_value = Integer.valueOf(parameter.value());
                break;
            }
        }
    }

    /**
     * Find the first roll-over spot, then create enough images to be one
     * creation before the next roll-over. That's where our test beings.
     */
    @BeforeMethod
    public void reset() {
        final long original = getCurrentNextValue(seq_name);
        long loop = -1L;
        do {
            incrementImage();
            loop = getCurrentNextValue(seq_name);
        } while (original == loop);

        for (int i = 0; i < incr_value - 1; i++) {
            incrementImage();
        }

        long current = getCurrentNextValue(seq_name);
        assertEquals(loop, current);
    }

    /**
     * For the other logic here to work properly, two consecutive calls to
     * DS.getConnection() should return different connections.
     */
    public void testConnectionUniqueness() throws Exception {
        DataSource ds = (DataSource) applicationContext.getBean("selfCorrectingDataSource");
        log.warn("XXXX: GETTING CONNECTIONS");
        Connection conn1 = ds.getConnection();
        Connection conn2 = ds.getConnection();
        assertFalse(conn1.equals(conn2));
        log.warn("XXXX: GOT 2 CONNECTIONS");
    }

    public void testBasics() throws Exception {
        String uuid = UUID.randomUUID().toString();
        assertEquals(-1, getCurrentNextValue(uuid));
        assertEquals(1, callNextValue(uuid, 1));
        assertEquals(2, getCurrentNextValue(uuid));
        assertEquals(2, callNextValue(uuid, 1));
        assertEquals(3, getCurrentNextValue(uuid));
        assertEquals(4, callNextValue(uuid, 2));
        assertEquals(5, getCurrentNextValue(uuid));
    }

    public void testSequences() throws Exception {

        long valueBefore = getCurrentNextValue(seq_name);
        long addedId = incrementImage();

        assertEquals(valueBefore, addedId);

        long valueAfterFirst = getCurrentNextValue(seq_name);
        assertEquals(valueBefore + incr_value, valueAfterFirst);

        // The second save shouldn't update seq_table
        addedId = incrementImage();
        assertEquals(valueBefore + 1, addedId);
        addedId = incrementImage();
        assertEquals(valueBefore + 2, addedId);

        // No change despite the various calls to incrementImage()
        long valueAfterSecond = getCurrentNextValue(seq_name);
        assertEquals(valueAfterFirst, valueAfterSecond);

        // The next 48 should also be the same
        for (int i = 0; i < incr_value - 3; i++) {
            addedId = incrementImage();
            long valueAfterLoop = getCurrentNextValue(seq_name);
            assertEquals("Differnt on loop " + i, valueAfterFirst,
                    valueAfterLoop);
        }

        // Now another loop of 50 should start
        addedId = incrementImage();
        assertEquals(valueAfterSecond, addedId);
        assertEquals(valueAfterSecond + incr_value,
                getCurrentNextValue(seq_name));

        // A manual load should increment by 1
        long bv = getCurrentNextValue(seq_name);
        long nv = sql.nextValue(seq_name, 1);
        long cv = getCurrentNextValue(seq_name);
        assertEquals(bv, nv);
        assertEquals(nv + 1, cv);

    }

    /**
     * Proper functioning of nextval() semantics may depend on the proper
     * out-of-transaction logic of Hibernate. Here we check that the Executor
     * class can properly wrap calls to Isolator.
     */
    public void testThatIsolatorWorksInASeparateThread() {

        final int BEFORE = 0;
        final int DURING = 1;
        final int AFTER = 2;
        final int OUTSIDE = 3;
        final int IMAGEID = 4;
        final long[] values = new long[5];

        try {
            log.warn("XXXX: EXECUTING");
            executor.execute(this.loginAop.p, new Executor.SimpleWork(this,
                    "isolated") {
                @Transactional(readOnly = false)
                public Object doWork(Session session, ServiceFactory sf) {

                    // First we create an image which should not exist after
                    // the tx is rolled back. Don't use incrementImage since
                    // that's
                    // wrapped with AOP
                    Image i = new Image("1176-inner");
                    log.warn("XXXX: SAVING");
                    i = sf.getUpdateService().saveAndReturnObject(i);
                    values[IMAGEID] = i.getId();

                    log.warn("XXXX: CHECKING NEXTVAL");
                    values[BEFORE] = getCurrentNextValue(seq_name);

                    // This was failing.
                    // Now we do nextval() in an isolated work
                    /*
                     * Isolater.doIsolatedWork(new IsolatedWork() { public void
                     * doWork(Connection connection) throws HibernateException {
                     * SingleConnectionDataSource ds = new
                     * SingleConnectionDataSource( connection, true);
                     * SimpleJdbcTemplate jdbc = new SimpleJdbcTemplate(ds);
                     * values[DURING] = jdbc.queryForLong(
                     * "select ome_nextval(?)", seq_name); } },
                     * (SessionImplementor) session);
                     */

                    // If instead we get our own datasoure then the connection
                    // should be an unwrapped one. We are duplicating what is
                    // done in TableIdGenerator here.
                    log.warn("XXXX: OME_NEXTVAL");
                    DataSource ds = (DataSource) applicationContext
                            .getBean("selfCorrectingDataSource");
                    PlatformTransactionManager tm = new DataSourceTransactionManager(
                            ds);
                    TransactionTemplate tt = new TransactionTemplate(tm);
                    final SimpleJdbcTemplate jdbc = new SimpleJdbcTemplate(ds);

                    tt.execute(new TransactionCallback() {
                        public Object doInTransaction(TransactionStatus status) {
                            values[DURING] = jdbc.queryForLong(
                                    "select ome_nextval(?)", seq_name);
                            return null;
                        }
                    });

                    log.warn("XXXX: CHECKING NEXTVAL");
                    values[AFTER] = getCurrentNextValue(seq_name);

                    throw new RuntimeException("Should rollback tx");
                }
            });
            fail("must throw");
        } catch (Exception e) {
            // good
        }

        log.warn("XXXX: AFTER EXECUTE. CHECKING NEXTVAL");
        values[OUTSIDE] = getCurrentNextValue(seq_name);

        // First the image must be gone. Most important!
        log.warn("XXXX: QUERY.FIND");
        assertNull(iQuery.find(Image.class, values[IMAGEID]));

        // Then value should have updated despite the rollback.
        assertEquals(values[BEFORE], values[DURING]);
        assertEquals(values[DURING] + 1, values[AFTER]);
        assertEquals(values[AFTER], values[OUTSIDE]);

    }

    /**
     * If the second image in a save throws an exception then the sequence value
     * should be updated, but the first image should not be present.
     */
    public void testImageIdIsAlsoRolledBack() {
        Image[] images = new Image[2];
        images[0] = new Image("image rollback");
        images[1] = new Image(/* no name */);
        try {
            iUpdate.saveAndReturnArray(images);
            fail("Must throw");
        } catch (ValidationException ve) {
            // good
        }
        long rolledBackId = images[0].getId();

        assertNull(iQuery.find(Image.class, rolledBackId));
        assertTrue(getCurrentNextValue(seq_name) > rolledBackId);

    }

    /**
     * The same test but from inside the executor
     */
    public void testImageIdIsAlsoRolledBackInExecutor() {
        final Image[] images = new Image[2];
        images[0] = new Image("image rollback");
        images[1] = new Image(/* no name */);

        try {
            executor.execute(this.loginAop.p, new Executor.SimpleWork(this,
                    "rollback") {
                @Transactional(readOnly = false)
                public Object doWork(Session session, ServiceFactory sf) {
                    return sf.getUpdateService().saveAndReturnArray(images);
                }

            });
            fail("Must throw");
        } catch (ValidationException ve) {
            // good
        }
        long rolledBackId = images[0].getId();
        assertNull(iQuery.find(Image.class, rolledBackId));
        assertEquals(getCurrentNextValue(seq_name), rolledBackId + incr_value);

    }

    private long incrementImage() {
        Image i;
        i = new Image("1176");
        i = iUpdate.saveAndReturnObject(i);
        return i.getId();
    }

    private <T extends IObject> long getCurrentNextValue(String seq_name) {
        return sql.currValue(seq_name);
    }

    private <T extends IObject> long callNextValue(String seq_name, int incr) {
        return sql.nextValue(seq_name, incr);
    }

}

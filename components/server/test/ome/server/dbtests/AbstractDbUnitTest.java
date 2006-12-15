/*
 * ome.server.dbtests.AbstractDbUnitTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.dbtests;

// Java imports
import javax.sql.DataSource;

// Third-party libraries
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.testng.annotations.Configuration;

// Application-internal dependencies
import ome.server.itests.*;

/**
 * @author josh
 * @DEV.TODO test "valid=false" sections of queries
 */
public abstract class AbstractDbUnitTest extends AbstractInternalContextTest {

    // ~ Testng Adapter
    // =========================================================================
    @Override
    @Configuration(beforeTestMethod = true)
    public void adaptSetUp() throws Exception {
        setUp();
    }

    @Override
    @Configuration(afterTestMethod = true)
    public void adaptTearDown() throws Exception {
        tearDown();
    }

    // =========================================================================

    protected static IDatabaseConnection c = null;

    protected DataSource ds = null;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AbstractDbUnitTest.class);
    }

    @Override
    protected void onSetUpInTransaction() throws Exception {
        ds = (DataSource) applicationContext.getBean("dataSource");
        if (null == c) {
            try {
                c = new DatabaseConnection(ds.getConnection());
                DatabaseOperation.CLEAN_INSERT.execute(c, getData());
            } catch (Exception e) {
                c = null;
                throw e;
            }
        }
    }

    public abstract IDataSet getData() throws Exception;

}

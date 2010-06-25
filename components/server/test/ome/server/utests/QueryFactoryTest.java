/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

import java.util.Arrays;

import junit.framework.TestCase;
import ome.conditions.ApiUsageException;
import ome.model.containers.Project;
import ome.parameters.Parameters;
import ome.services.query.ClassQuerySource;
import ome.services.query.NullQuerySource;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.services.query.Query;
import ome.services.query.QueryException;
import ome.services.query.QueryFactory;
import ome.services.query.QuerySource;
import ome.services.query.StringQuerySource;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class QueryFactoryTest extends TestCase {

    Query q;

    QueryFactory qf;

    QuerySource nullQS, stringQS, classQS;

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        nullQS = new NullQuerySource();
        stringQS = new StringQuerySource();
        classQS = new ClassQuerySource();
        /*
         * HibernateNamedQuerySource hnqs; // needs hibernateTemplate
         * DatabaseQuerySource dqs; // needs jdbcTemplate or IQuery
         * VelocityQuerySource vqs; // needs template path and macro path
         */

    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testQueryFactoryWithNullThrowsException() throws Exception {
        qf = new QueryFactory();
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testQueryFactoryWithEmptySourcesThrowsOnLookup()
            throws Exception {
        qf = new QueryFactory(new QuerySource[] {});
    }

    @Test
    public void testQueryFactoryWithoutStringQuerySourceThrowsUnfoundExceptionOnUnkownQuery()
            throws Exception {
        qf = new QueryFactory(nullQS);
        try {
            qf.lookup("UNKNOWN QUERY ID", null);
            fail("Should throw a query exception");
        } catch (QueryException e) {
            // Good.
        }
    }

    @Test
    public void testQueryFactoryWithStringQuerySourceNeverThrowsUnfoundException()
            throws Exception {
        qf = new QueryFactory(stringQS);
        q = qf.lookup("UNKNOWN QUERY ID BUT STILL WORKS", null);
        assertNotNull("We should have a string query", q);
    }

    @Test
    public void testQFWithClassQuerySource() throws Exception {
        qf = new QueryFactory(classQS);
        q = qf.lookup(PojosLoadHierarchyQueryDefinition.class.getName(),
                new Parameters().addClass(Project.class).addIds(
                        Arrays.asList(0L)).addString(Parameters.OWNER_ID, null));
        assertNotNull("We should have a Pojos Query", q);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void test_StringSourceDoesntTakeNull() throws Exception {
        q = stringQS.lookup(null, null);
    }

}

package ome.server.utests;

import java.util.Arrays;

import ome.model.containers.Project;
import ome.services.query.ClassQuerySource;
import ome.services.query.NullQuerySource;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.services.query.QP;
import ome.services.query.Query;
import ome.services.query.QueryException;
import ome.services.query.QueryFactory;
import ome.services.query.QuerySource;
import ome.services.query.StringQuerySource;

import junit.framework.TestCase;

public class QueryFactoryTest extends TestCase
{

    QueryFactory qf;
    QuerySource nullQS, stringQS, classQS;

    @Override
    protected void setUp() throws Exception
    {
        nullQS = new NullQuerySource();
        stringQS = new StringQuerySource();
        classQS = new ClassQuerySource();
        /*HibernateNamedQuerySource hnqs; // needs hibernateTemplate
        DatabaseQuerySource dqs; // needs jdbcTemplate or IQuery
        VelocityQuerySource vqs; // needs template path and macro path
        */
        
    }    

    public void testQueryFactoryWithoutStringQuerySourceThrowsUnfoundExceptionOnUnkownQuery()
            throws Exception
    {
        qf = new QueryFactory(nullQS);
        try
        {
            qf.lookup("UNKNOWN QUERY ID", null);
            fail("Should throw a query exception");
        } catch (QueryException e)
        {
            // Good.
        }
    }

    public void testQueryFactoryWithStringQuerySourceNeverThrowsUnfoundException() throws Exception
    {
        qf = new QueryFactory(stringQS);
        Query q = qf.lookup("UNKNOWN QUERY ID BUT STILL WORKS",null);
        assertNotNull("We should have a string query",q);
    }
    
    public void testQFWithClassQuerySource() throws Exception
    {
        qf = new QueryFactory(classQS);
        Query q = qf.lookup(PojosLoadHierarchyQueryDefinition.class.getName(),
                QP.Class("class",Project.class),
                QP.List("ids",Arrays.asList(0L)),
                QP.Null("ownerId"),
                QP.Null("options"));
        assertNotNull("We should have a Pojos Query",q);
    }
    
    
    
}

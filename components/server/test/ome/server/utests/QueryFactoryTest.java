package ome.server.utests;

import ome.services.query.NullQuerySource;
import ome.services.query.Query;
import ome.services.query.QueryException;
import ome.services.query.QueryFactory;
import ome.services.query.QuerySource;
import ome.services.query.StringQuerySource;

import junit.framework.TestCase;

public class QueryFactoryTest extends TestCase
{

    QueryFactory qf;
    QuerySource nullQS, stringQS;
    QuerySource[] nullOnly,stringOnly;

    @Override
    protected void setUp() throws Exception
    {
        nullQS = new NullQuerySource();
        nullOnly = new QuerySource[]{nullQS};
        stringQS = new StringQuerySource();
        stringOnly = new QuerySource[]{stringQS};
        /*HibernateNamedQuerySource hnqs; // needs hibernateTemplate
        DatabaseQuerySource dqs; // needs jdbcTemplate or IQuery
        VelocityQuerySource vqs; // needs template path and macro path
        */
        
    }    

    public void testQueryFactoryWithoutStringQuerySourceThrowsUnfoundExceptionOnUnkownQuery()
            throws Exception
    {
        qf = new QueryFactory(nullOnly);
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
        qf = new QueryFactory(stringOnly);
        Query q = qf.lookup("UNKNOWN QUERY ID BUT STILL WORKS",null);
        assertNotNull("We have a string query",q);
    }
    
    
    
}

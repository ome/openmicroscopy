package ome.server.itests.query;

import org.testng.annotations.Test;

import ome.conditions.InternalException;
import ome.model.IObject;
import ome.model.containers.Project;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.query.Query;
import ome.services.query.QueryFactory;


@Test( groups = {"ticket:72"} )
public class UniqueResultTest extends AbstractManagedContextTest
{
    
    @Test
    public void test_queryUnique() throws Exception
    {
        
        Project p = getProject();
        
        Query<IObject> q1 = query(false);
        try { 
            
            IObject o1 = iQuery.execute(q1); 
            fail("Should have thrown an exception.");
        } catch ( ClassCastException cce ) {
            // ok.
        } 

        // This should work
        Query<IObject> q2 = query( true );
        IObject o2 = iQuery.execute( q2 ); 
                
        // And this should work.
        IObject o3 = iQuery.findByQuery( 
                "select p from Project p where p.id = :id",
                new Parameters()
                    .addId( p.getId() )
                    .setFilter( new Filter().unique()) 
                );
    }

    @Test
    public void test_queryList() throws Exception
    {
        Project p = getProject();

        try { 
            
            iQuery.findAllByQuery( 
                    "select p from Project p where p.id = :id",
                    new Parameters()
                        .addId( p.getId() )
                        .setFilter( new Filter().unique() )
                    );
            fail("Should have thrown class cast exception.");
        } catch ( InternalException ie ) {
            assertTrue(ie.getMessage().contains("ClassCastException"));
            // ok.
        }

        
    }
    
    // ~ Private helpers
    // =========================================================================
    private Project getProject()
    {
        Project p = (Project)
        iQuery.findAll( Project.class, new Filter( ).page(0,1) ).get(0);
        return p;
    }
    
    private QueryFactory factory()
    {
        return (QueryFactory) applicationContext.getBean("queryFactory");
    }


    private Query query(boolean unique)
    {
        Filter f = new Filter();
        if ( unique )
            f.unique();

        Query q = factory().lookup(
                "select p from Project p where p.id = :id",
                new Parameters()
                    .addId( getProject().getId() )
                    .setFilter( f ) 
            );
        return q;
    }

    
}

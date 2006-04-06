package ome.client.utests.session;

import java.util.ConcurrentModificationException;

import ome.model.IObject;
import ome.model.containers.Project;

public class UseCasesTest extends AbstractTest
{

    Agent agent = new Agent();
    Project updated;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        updated = new Project( 1L );
        updated.setVersion( 1 );
    }
    
    public void test_agentCreatesNewItemAndSaves() throws Exception
    {
        //
        // Mock setup
        //
        updateReturnsSingleNewProject( );
        
        //
        // Use case
        //
        
        // agent creates project
        Project p = new Project();
        agent.objHolder = p;
        
        // project is passed to session
        session.register( p );
        
        // session is synchronized
        session.flush();
        
        // agent will need to check out 
        Project test = (Project) session.checkOut( p );

        //
        // Test
        //
        
        assertTrue("Must be updated", test == updated);
    }
    
    public void test_agentQueriesForExistingEntity() throws Exception
    {
        //
        // Use case
        //
        
        // agent does query, gets object, and registers it for updates
        Project p = agentGetsAndRegistersProject();
        
        // someone else does another query and new object is registered
        session.register( updated );

        // on checkOut, agent gets the new object
        Project test = (Project) session.checkOut( p );
        
        //
        // Test
        //
        
        assertTrue("Must be updated", test == updated);
    }
    
    public void test_agentDeletesAnExistingEntity() throws Exception
    {
        //
        // Use case
        //
        
        // agent does query and registers entity
        Project p = agentGetsAndRegistersProject( );
        
        // after considering the matter, agent deletes the entity
        session.delete( p );
        
        // anyone else checkouting out entity should get null
        Project test = (Project) session.checkOut( p );
        
        //
        // Test
        //
        assertNull("Must be null after delete.", test);
        
    }

    public void test_agentDeletesAnEntityAndUpdateIsRegistered() throws Exception
    {
        //
        // Use case
        //
        
        // agent does query and registers entity
        Project p = agentGetsAndRegistersProject( );
        
        // after considering the matter, agent deletes the entity
        session.delete( p );
        
        // anyone else registering entity should get an exception
        try { 
            session.register( updated );
            fail("Should have thrown exception on conflict.");
        } catch (ConcurrentModificationException e) {
            // good
        }
        
        
        
    }
    
    public void test_agentMarksEntityDirty() throws Exception
    {
        //
        // Mock setup
        //
        updateReturnsSingleUpdatedProject();
        
        //
        // Use case
        //
        
        // agent gets entity and marks it dirty
        Project p = agentGetsAndRegistersProject();
        session.markDirty( p );
        
        // on flush, it should be replaced
        session.flush();
        Project test = (Project) session.checkOut( p );
        
        // 
        // Test
        //
        assertTrue("Must be updated.", test == updated );
        
    }
    
    public void test_agentMarksDirtyAndUpdateIsRegistered() throws Exception
    {
        //
        // Use case
        //
        
        // agent gets object and marks as dirty
        Project p = agentGetsAndRegistersProject();
        session.markDirty( p );
        
        // "somewhere else" an updated version is registerd. boom.
        try { 
            session.register( updated );
            fail("Should have thrown exception on conflict.");
        } catch (ConcurrentModificationException e) {
            // good
        }
    }
    
    // ~ Helper methods
    // =========================================================================
    private Project agentGetsAndRegistersProject()
    {
        Project p = new Project( 1L );
        p.setVersion( 0 );
        agent.objHolder = p;
        session.register( p );
        return p;
    }

    private IObject[] updatedProjectArray()
    {
        updated = new Project( 1L );
        updated.setVersion( 1 );
        return new IObject[] { updated };
        
    }
    
    private void updateReturnsSingleNewProject()
    {
        serviceFactory.mockUpdate = updateMockForFlush( updatedProjectArray(), null );
    }
    
    private void updateReturnsSingleUpdatedProject()
    {
        serviceFactory.mockUpdate = updateMockForFlush( null, updatedProjectArray() );
    }

    
    
}

class Agent {
    public IObject objHolder;
}
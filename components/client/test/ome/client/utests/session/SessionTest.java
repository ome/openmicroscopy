package ome.client.utests.session;

import org.jmock.Mock;

import ome.api.IUpdate;
import ome.model.IObject;
import ome.model.containers.Project;

public class SessionTest extends AbstractTest
{

    public void test_initialTimeIsAfterNow() throws Exception
    {
        long last = session.lastModification();
        assertTrue("Didn't happen in past!!", last <= System.currentTimeMillis());
        // Use <= here because the interval can be too short.
    }
    
    public void test_modificationIncrements() throws Exception
    {
        long original = session.lastModification();
        session.register( new Project() );
        long updated = session.lastModification();
        
        assertTrue("Registering didn't update lastModification", original < updated);

    }
    
    public void test_registeredObjectCanBeFound() throws Exception
    {
        Project p = new Project( new Long(1L) );
        p.setVersion( new Integer(1) );
        session.register( p );
        Project p2 = (Project) session.find( Project.class, new Long(1L) );
        assertTrue( "Must be same instance", p == p2 );
    }

    public void test_mock_flush() throws Exception
    {
        Mock m = mock(IUpdate.class);
        serviceFactory.mockUpdate = m;
        m.expects( atLeastOnce() ).method( "saveAndReturnArray" )
            .will( returnValue( new IObject[]{} )).id("save");
        
        Project p = new Project( new Long(1L) );
        p.setVersion( new Integer(1) );
        session.markDirty( p );
        session.flush();
    }
    
    public void test_newAndThenCheckOut() throws Exception
    {
        Project p_new = new Project();
        Project p_old = new Project( new Long(1L) );
        p_old.setVersion( new Integer(1) );
        IObject[] arr = new IObject[]{ p_old };

        serviceFactory.mockUpdate = updateMockForFlush( arr, null );
        
        session.register( p_new );
        session.flush();
        
        Project p_test = (Project) session.checkOut( p_new );
        assertTrue( "Must be identical", p_old == p_test );
    }
    
    public void test_allMethodsThrowExceptionAfterClose() throws Exception
    {
        session.close();
        
        try {
            session.checkOut( null );
            fail("Should have thrown IllegalState.");
        } catch (IllegalStateException e){
            // good;
        }
        
        try {
            session.register( null );
            fail("Should have thrown IllegalState.");
        } catch (IllegalStateException e){
            // good;
        }

        try {
            session.delete( null );
            fail("Should have thrown IllegalState.");
        } catch (IllegalStateException e){
            // good;
        }

        try {
            session.find( null, null );
            fail("Should have thrown IllegalState.");
        } catch (IllegalStateException e){
            // good;
        }
        
        try {
            session.flush( );
            fail("Should have thrown IllegalState.");
        } catch (IllegalStateException e){
            // good;
        }
        
        try {
            session.lastModification(  );
            fail("Should have thrown IllegalState.");
        } catch (IllegalStateException e){
            // good;
        }
        
        try {
            session.markDirty( null );
            fail("Should have thrown IllegalState.");
        } catch (IllegalStateException e){
            // good;
        }
        
    }
    
}

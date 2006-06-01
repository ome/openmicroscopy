/*
 * ome.server.utests.UpdateFilterMockTest
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package ome.server.utests;

// Java imports
import java.util.Collection;
import java.util.Map;

// Third-party libraries
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.orm.hibernate3.HibernateOperations;
import org.testng.annotations.*;

// Application-internal dependencies
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.core.Image;
import ome.model.internal.Details;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.CurrentDetails;
import ome.tools.hibernate.UpdateFilter;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since Omero 2.0
 */
public class UpdateFilterMockTest extends MockObjectTestCase
{

    private static Long OWNER_ID = 0L;
    private static Long GROUP_ID = 0L;
    private static Long EVENT_ID = 0L;

    private static Experimenter OWNER = new Experimenter( OWNER_ID );
    private static ExperimenterGroup GROUP = new ExperimenterGroup( GROUP_ID );
    private static Event EVENT = new Event( EVENT_ID );
    
    protected UpdateFilter filter;
    protected HibernateOperations ops;
    protected Mock mockOps;

    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception
    {
        super.setUp();
        mockOps = mock( HibernateOperations.class );
        ops = (HibernateOperations) mockOps.proxy();
        filter = new UpdateFilter( ops );
        login();
        
    }
    
    protected void login()
    {
        CurrentDetails.setOwner( OWNER );
        CurrentDetails.setGroup( GROUP );
        CurrentDetails.setCreationEvent( EVENT );
    }
    
    @Configuration(afterTestMethod = true)
    protected void tearDown() throws Exception
    {
        super.verify();
        super.tearDown();
    }

    // ~ NON GRAPHS (single elements)
    // =========================================================================
    
    @Test
    public void test_filter_null() throws Exception
    {
        filter.filter( null, (Object) null );
        filter.filter( null, (IObject) null );
        filter.filter( null, (Details) null );
        filter.filter( null, (Map) null );
    }
    
    @Test( groups = "todo" )
    @ExpectedExceptions( RuntimeException.class )
    public void test_filter_null_collection() throws Exception
    {
        filter.filter( null, (Collection) null );
    }
    
    @Test
    public void test_transient() throws Exception
    {
        willCheckDetails();
        
        Image i = new Image();
        Image withNewDetails = (Image) filter.filter( null, i );
        assertTrue( i == withNewDetails );
        assertDetails( withNewDetails  );
    }
    
    @Test
    @ExpectedExceptions(IllegalStateException.class)
    public void test_unloaded_without_id() throws Exception
    {
        Image i = new Image();
        i.unload();
        filter.filter( null, i );
        
        // this image has no id and is seen as transient.
    }
    
    @Test
    public void test_unloaded() throws Exception
    {
        willLoadImage( 0L );
        
        Image i = new Image( 0L );
        i.unload();
        Image reloaded = (Image) filter.filter( null, i );
        
        // this image is unloaded with id 0, and the filter tries to load
        // it from the ops. 
        
    }

    @Test
    public void test_managed_with_replacement() throws Exception
    {
        Image i = managedImage();
        i.getGraphHolder().setReplacement( new Image( 0L ));
        filter.filter( null, i );
        filter.unloadReplacedObjects();
        
        assertFalse( "Image should be unloaded", i.isLoaded() );

        // if the image has a replacement, then it will come out of the 
        // filter.unloadedReplacedObjects() method unloaded.
        
    }
    
    @Test
    public void test_managed() throws Exception
    {
        willLoadImage( 0L );
        willCheckDetails();
        
        Image i = managedImage();
        filter.filter( null, i );
        filter.unloadReplacedObjects();
        
        assertTrue( "Image should be loaded", i.isLoaded() );

        // if the image doesn't have a replacement, then it should not 
        // be unloaded after filter.unloadedReplacedObjects() method.
        
    }

    // ~ GRAPHS (multiple levels)
    // =========================================================================

    // ~ SECURITY (attempting to change values)
    // =========================================================================
    @Test
    @ExpectedExceptions( SecurityViolation.class )
    public void test_change_owner() throws Exception
    {
        willLoadImage( 0L );
        
        Image i = new Image( 0L );
        Details myDetails = new Details();
        myDetails.setOwner( new Experimenter( 1L ));
        myDetails.setGroup( GROUP );
        myDetails.setCreationEvent( EVENT );
        i.setDetails( myDetails );
        
        filter.filter( null, i );
        
    }
    
   
    
    // ~ Private helpers
    // =========================================================================

    private Image managedImage()
    {
        Image i = new Image( 0L );
        Details managed = new Details();
        managed.setOwner( OWNER );
        managed.setGroup( GROUP );
        managed.setCreationEvent( EVENT );
        i.setDetails( managed );
        return i;
    }
    
    private void willLoadImage( Long id )
    {
        Image persistentImage = new Image( id );
        Details belongsToRoot = new Details();
        belongsToRoot.setOwner( OWNER );
        belongsToRoot.setGroup( GROUP );
        belongsToRoot.setCreationEvent( EVENT );
        persistentImage.setDetails( belongsToRoot );
        
        mockOps.expects( once() ).method( "load" )
            .with( eq( Image.class ), eq( id ))
            .will( returnValue( persistentImage ));
    }
    
    private void willCheckDetails(  )
    {
        willCheckDetails( OWNER_ID, GROUP_ID, EVENT_ID );
    }
    
    private void willCheckDetails( Long owner, Long group, Long event)
    {
        mockOps.expects( once() ).method( "load" )
            .with( eq( Event.class ), eq( event ))
            .will( returnValue( EVENT ));
        mockOps.expects( once() ).method( "load" )
            .with( eq( Experimenter.class ), eq( owner ))
            .will( returnValue( OWNER ));
        mockOps.expects( once() ).method( "load" )
            .with( eq( ExperimenterGroup.class ), eq( group ))
            .will( returnValue( GROUP ));
    }

    
    private void assertDetails( IObject o )
    {
        assertDetails( o, OWNER_ID, GROUP_ID, EVENT_ID );
    }
    
    private void assertDetails( IObject o, Long owner, Long group, Long event)
    {
        assertNotNull( o.getDetails() );
        assertNotNull( o.getDetails().getOwner() );
        assertNotNull( o.getDetails().getGroup() );
        assertNotNull( o.getDetails().getCreationEvent() );
        
        assertTrue( o.getDetails().getOwner().getId().equals( owner ));
        assertTrue( o.getDetails().getGroup().getId().equals( group ));
        assertTrue( o.getDetails().getCreationEvent().getId().equals( event ));
        
    }

}

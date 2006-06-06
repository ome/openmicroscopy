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

// Third-party libraries
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.orm.hibernate3.HibernateOperations;
import org.testng.annotations.*;

// Application-internal dependencies
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
public class AbstractLoginMockTest extends MockObjectTestCase
{

    public final static Long ROOT_OWNER_ID = 0L;
    public final static Long SYS_GROUP_ID = 0L;
    public final static Long INITIAL_EVENT_ID = 0L;

    public final static Experimenter ROOT = new Experimenter( ROOT_OWNER_ID );
    public final static ExperimenterGroup ROOT_GROUP = new ExperimenterGroup( SYS_GROUP_ID );
    public final static Event INITIAL_EVENT = new Event( INITIAL_EVENT_ID );

    public final static Long USER_OWNER_ID = 1L;
    public final static Long USER_GROUP_ID = 1L;

    public final static Experimenter USER = new Experimenter( USER_OWNER_ID );
    public final static ExperimenterGroup USER_GROUP = new ExperimenterGroup( USER_GROUP_ID );
    
    protected UpdateFilter filter;
    protected HibernateOperations ops;
    protected Mock mockOps;
    protected Event userEvent;
    
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception
    {
        super.setUp();
        mockOps = mock( HibernateOperations.class );
        ops = (HibernateOperations) mockOps.proxy();
        filter = new UpdateFilter( ops );
        rootLogin();
        
    }

    protected void rootLogin()
    {
        CurrentDetails.setOwner( ROOT );
        CurrentDetails.setGroup( ROOT_GROUP );
        CurrentDetails.setCreationEvent( INITIAL_EVENT );
    }

    protected void userLogin( )
    {
        userEvent = new Event( 4711L );
        
        CurrentDetails.setOwner( USER );
        CurrentDetails.setGroup( USER_GROUP );
        CurrentDetails.setCreationEvent( userEvent );
    }

    
    @Configuration(afterTestMethod = true)
    protected void tearDown() throws Exception
    {
        super.verify();
        super.tearDown();
        CurrentDetails.setOwner( null );
        CurrentDetails.setGroup( null );
        CurrentDetails.setCreationEvent( null );
    }
  
    
    // ~ Protected helpers
    // =========================================================================

    protected void checkSomeoneIsLoggedIn()
    {
        assertNotNull( CurrentDetails.getOwner() );
        assertNotNull( CurrentDetails.getGroup() );
        assertNotNull( CurrentDetails.getCreationEvent() );
    }
    
    /** creates a "managed image" (has ID) to the currently logged in user. */
    protected Image managedImage()
    {
        checkSomeoneIsLoggedIn();
        Image i = new Image( 0L );
        Details managed = new Details();
        managed.setOwner( CurrentDetails.getOwner() );
        managed.setGroup( CurrentDetails.getGroup() );
        managed.setCreationEvent( CurrentDetails.getCreationEvent() );
        i.setDetails( managed );
        return i;
    }

    protected void assertDetails( IObject o )
    {
        assertDetails( o, ROOT_OWNER_ID, SYS_GROUP_ID, INITIAL_EVENT_ID );
    }
    
    protected void assertDetails( IObject o, Long owner, Long group, Long event)
    {
        assertNotNull( o.getDetails() );
        assertNotNull( o.getDetails().getOwner() );
        assertNotNull( o.getDetails().getGroup() );
        assertNotNull( o.getDetails().getCreationEvent() );
        
        assertTrue( o.getDetails().getOwner().getId().equals( owner ));
        assertTrue( o.getDetails().getGroup().getId().equals( group ));
        assertTrue( o.getDetails().getCreationEvent().getId().equals( event ));
        
    }
    
    /** setups the mocking of hibernateTemplate.load(Image,id)
     * the argument provided should be created in exactly the same way as
     * the image passed into filter. E.g.:
     *  <code>
     *  Image i = managedImage();
     *  willLoadImage( managedImage() );
     *  filter.filter(null,i);
     *  </code>
     *  
     *  One exception to this rule is the testing of unloaded status where the 
     *  use will resemble:
     *  <code>
     *  Image i = new Image(...).unload();
     *  willLoadImage( new Image(...).setDetails(...);
     *  filter.filter(null,i);
     *  </code>
     */
    protected void willLoadImage( Image persistentImage )
    {
        mockOps.expects( once() ).method( "load" )
            .with( eq( Image.class ), eq( persistentImage.getId() ))
            .will( returnValue( persistentImage ));
    }
    
    protected void willLoadUser( Long id )
    {
        mockOps.expects( once() ).method( "load" )
        .with( eq( Experimenter.class ), eq( id ))
        .will( returnValue( new Experimenter( id ) ));
    }

    protected void willLoadEvent( Long id )
    {
        mockOps.expects( once() ).method( "load" )
        .with( eq( Event.class ), eq( id ))
        .will( returnValue( new Event( id ) ));
    }
    
    protected void willLoadGroup( Long id )
    {
        mockOps.expects( once() ).method( "load" )
        .with( eq( ExperimenterGroup.class ), eq( id ))
        .will( returnValue( new ExperimenterGroup( id ) ));
    }
    
    protected void willCheckRootDetails(  )
    {
        mockOps.expects( once() ).method( "load" )
        .with( eq( Event.class ), eq( INITIAL_EVENT_ID ))
        .will( returnValue( INITIAL_EVENT ));
        mockOps.expects( once() ).method( "load" )
        .with( eq( Experimenter.class ), eq( ROOT_OWNER_ID ))
        .will( returnValue( ROOT ));
        mockOps.expects( once() ).method( "load" )
        .with( eq( ExperimenterGroup.class ), eq( SYS_GROUP_ID ))
        .will( returnValue( ROOT_GROUP ));
    }
        
    protected void willCheckUserDetails( )
    {
        mockOps.expects( once() ).method( "load" )
        .with( eq( Event.class ), eq( userEvent.getId() ))
        .will( returnValue( userEvent ));
        mockOps.expects( once() ).method( "load" )
        .with( eq( Experimenter.class ), eq( USER_OWNER_ID ))
        .will( returnValue( USER ));
        mockOps.expects( once() ).method( "load" )
        .with( eq( ExperimenterGroup.class ), eq( USER_GROUP_ID ))
        .will( returnValue( USER_GROUP ));
    }
    
    protected void chown( IObject i, Long userId )
    {
        Details myDetails = i.getDetails() == null 
            ? new Details() : i.getDetails();
        myDetails.setOwner( new Experimenter( userId ));
        i.setDetails( myDetails );
    }

    protected void chgrp( IObject i, Long grpId )
    {
        Details myDetails = i.getDetails() == null 
            ? new Details() : i.getDetails();
        myDetails.setGroup( new ExperimenterGroup( grpId ));
        i.setDetails( myDetails );
    }

    protected void setRootDetails( IObject i )
    {
        setDetails( i, ROOT_OWNER_ID, SYS_GROUP_ID, INITIAL_EVENT_ID );
    }
    
    protected void setDetails( IObject i, Long rootId, Long groupId, Long eventId )
    {
        Details myDetails = new Details();
        myDetails.setOwner( new Experimenter( 1L ));
        myDetails.setGroup( ROOT_GROUP );
        myDetails.setCreationEvent( INITIAL_EVENT );
        i.setDetails( myDetails );
    }
    
}

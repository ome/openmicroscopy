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
import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.ITypes;
import ome.api.local.LocalQuery;
import ome.model.IObject;
import ome.model.core.Image;
import ome.model.enums.EventType;
import ome.model.internal.Details;
import ome.model.meta.Event;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.BasicSecuritySystem;
import ome.security.SecuritySystem;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.ThreadLocalEventContext;
import ome.testing.MockServiceFactory;
import ome.tools.hibernate.UpdateFilter;

/**
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since Omero 2.0
 */
@Test( groups = "broken" )
public class AbstractLoginMockTest extends MockObjectTestCase
{

    public final static Long ROOT_OWNER_ID = 0L;
    public final static Long SYS_GROUP_ID = 0L;
    public final static Long INITIAL_EVENT_ID = 0L;

    public final static Experimenter ROOT = new Experimenter( ROOT_OWNER_ID );
    public final static ExperimenterGroup ROOT_GROUP = new ExperimenterGroup( SYS_GROUP_ID );
    public final static Event INITIAL_EVENT = new Event( INITIAL_EVENT_ID );
    public final static EventType BOOTSTRAP = new EventType( INITIAL_EVENT_ID );
    public final static EventType USEREVENT = new EventType( INITIAL_EVENT_ID + 1);
    static {
    	BOOTSTRAP.setValue( "Bootstrap" );
    	USEREVENT.setValue( "User" );
    }

    public final static Long USER_OWNER_ID = 1L;
    public final static Long USER_GROUP_ID = 1L;

    public final static Experimenter USER = new Experimenter( USER_OWNER_ID );
    public final static ExperimenterGroup USER_GROUP = new ExperimenterGroup( USER_GROUP_ID );
    
    protected UpdateFilter filter;
    protected Event userEvent;
    
    protected SecuritySystem sec;
    protected MockServiceFactory sf;
    protected EventContext ec;
    
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception
    {
        super.setUp();
        
        sf = new MockServiceFactory();
        ec = new ThreadLocalEventContext();
        sec = new BasicSecuritySystem (sf,ec );
        
        sf.mockAdmin = mock(IAdmin.class);
        sf.mockQuery = mock(LocalQuery.class);
        sf.mockTypes = mock(ITypes.class);

        filter = new UpdateFilter( );
        
        rootLogin();
        
    }

    protected void rootLogin()
    {
        sf.mockAdmin.expects( atLeastOnce() ).method( "lookupExperimenter" )
        	.with(eq("root"))
			.will( returnValue( ROOT ));
        sf.mockAdmin.expects( atLeastOnce() ).method( "lookupGroup" )
			.with(eq("system"))
        	.will( returnValue( ROOT_GROUP ));
        sf.mockTypes.expects( atLeastOnce() ).method( "getEnumeration" )
    		.with(eq(EventType.class),eq("Bootstrap"))
        	.will( returnValue( BOOTSTRAP ));
    	ec.setPrincipal( new Principal("root","system","Bootstrap") );
    	sec.setCurrentDetails();
    }

    protected void userLogin( )
    {
        sf.mockAdmin.expects( atLeastOnce() ).method( "lookupExperimenter" )
    		.with(eq("user1"))
			.will( returnValue( USER ));
        sf.mockAdmin.expects( atLeastOnce() ).method( "lookupGroup" )
			.with(eq("user"))
			.will( returnValue( USER_GROUP ));
    	sf.mockTypes.expects( atLeastOnce() ).method( "getEnumeration" )
			.with(eq(EventType.class), eq("User"))
			.will( returnValue( USEREVENT ));
    	ec.setPrincipal( new Principal("user1","user","User") );
    	sec.setCurrentDetails();
    }

    
    @Configuration(afterTestMethod = true)
    protected void tearDown() throws Exception
    {
        super.verify();
        super.tearDown();
        sec.clearCurrentDetails();
    }
  
    
    // ~ Protected helpers
    // =========================================================================

    protected void checkSomeoneIsLoggedIn()
    {
        assertNotNull( sec.currentUser() );
        assertNotNull( sec.currentEvent() );
        assertNotNull( sec.currentEvent() );
    }
    
    /** creates a "managed image" (has ID) to the currently logged in user. */
    protected Image managedImage()
    {
        checkSomeoneIsLoggedIn();
        Image i = new Image( 0L );
        Details managed = new Details();
        managed.setOwner( sec.currentUser() );
        managed.setGroup( sec.currentGroup() );
        managed.setCreationEvent( sec.currentEvent() );
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
        // This is now null because secsys creates a new event
        // assertTrue( o.getDetails().getCreationEvent().getId().equals( event ));
        
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
        sf.mockQuery.expects( once() ).method( "get" )
            .with( eq( Image.class ), eq( persistentImage.getId() ))
            .will( returnValue( persistentImage ));
    }
    
    protected void willLoadUser( Long id )
    {
        sf.mockQuery.expects( atLeastOnce() ).method( "get" )
        .with( eq( Experimenter.class ), eq( id ))
        .will( returnValue( new Experimenter( id ) ));
    }

    protected void willLoadEvent( Long id )
    {
        sf.mockQuery.expects( once() ).method( "get" )
        .with( eq( Event.class ), eq( id ))
        .will( returnValue( new Event( id ) ));
    }

    protected void willLoadEventType( Long id )
    {
        sf.mockQuery.expects( once() ).method( "get" )
        .with( eq( EventType.class ), eq( id ))
        .will( returnValue( new EventType( id ) ));
    }
    
    protected void willLoadGroup( Long id )
    {
        sf.mockQuery.expects( atLeastOnce() ).method( "get" )
        .with( eq( ExperimenterGroup.class ), eq( id ))
        .will( returnValue( new ExperimenterGroup( id ) ));
    }
    
    protected void willCheckRootDetails(  )
    {
//        sf.mockQuery.expects( once() ).method( "get" )
//        .with( eq( Event.class ), eq( INITIAL_EVENT_ID ))
//        .will( returnValue( INITIAL_EVENT ));
      sf.mockQuery.expects( once() ).method( "get" )
      .with( eq( EventType.class ), eq( INITIAL_EVENT_ID ))
      .will( returnValue( BOOTSTRAP ));
        sf.mockQuery.expects( once() ).method( "get" )
        .with( eq( Experimenter.class ), eq( ROOT_OWNER_ID ))
        .will( returnValue( ROOT ));
        sf.mockQuery.expects( once() ).method( "get" )
        .with( eq( ExperimenterGroup.class ), eq( SYS_GROUP_ID ))
        .will( returnValue( ROOT_GROUP ));
    }
        
    protected void willCheckUserDetails( )
    {
//        sf.mockQuery.expects( once() ).method( "get" )
//        .with( eq( Event.class ), eq( userEvent.getId() ))
//        .will( returnValue( userEvent ));
        sf.mockQuery.expects( once() ).method( "get" )
        .with( eq( Experimenter.class ), eq( USER_OWNER_ID ))
        .will( returnValue( USER ));
        sf.mockQuery.expects( once() ).method( "get" )
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

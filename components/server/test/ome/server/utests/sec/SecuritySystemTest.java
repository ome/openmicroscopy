package ome.server.utests.sec;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import junit.framework.TestCase;

import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.ITypes;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.logic.QueryImpl;
import ome.model.IEnum;
import ome.model.IObject;
import ome.model.core.Image;
import ome.model.enums.EventType;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.meta.Event;
import ome.model.meta.EventDiff;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.AdminAction;
import ome.security.BasicSecuritySystem;
import ome.security.JBossLoginModule;
import ome.security.SecureAction;
import ome.security.SecuritySystem;
import ome.services.util.ServiceHandler;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.system.SimpleEventContext;
import ome.testing.MockServiceFactory;
import ome.tools.hibernate.SecurityFilter;
import ome.util.IdBlock;

import static ome.model.internal.Permissions.Right.*;
import static ome.model.internal.Permissions.Role.*;

import org.hibernate.Filter;
import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.springframework.aop.framework.ProxyFactory;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

@Test
public class SecuritySystemTest extends MockObjectTestCase {

	MockServiceFactory sf;
	SecuritySystem sec; 
	
	// login information
	Principal p;
	
	// "current" details
	Experimenter user;
	ExperimenterGroup group;
	EventType type;
	Event event;
	List<Long> leaderOfGroups, memberOfGroups;
	
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception
    {
        super.setUp();
        
        sf = new MockServiceFactory();
        sec = new BasicSecuritySystem(sf,new SimpleEventContext()); // hiding EvtCtx
           
    }
    
    protected void prepareMocksWithUserDetails()
    {
        // login
    	p = new Principal("test","test","test");
        sec.login(p);
        
        // context 
		user = new Experimenter(1L);
		group = new ExperimenterGroup(1L);
		type = new EventType(1L);
		event = new Event(1L);

		user.linkExperimenterGroup(group);
		leaderOfGroups = Collections.singletonList(1L);
		memberOfGroups = Collections.singletonList(1L);

		prepareMocks();
    }

    protected void prepareMocksWithRootDetails()
    {
        // login
    	p = new Principal("root","system","internal");
    	sec.login(p);
    	
        // context 
		user = new Experimenter(0L);
		group = new ExperimenterGroup(0L);
		type = new EventType(0L);
		event = new Event(0L);

		user.linkExperimenterGroup(group);
		leaderOfGroups = Collections.singletonList(0L);
		memberOfGroups = Arrays.asList(0L,1L);
		prepareMocks();
    }
    
    protected void prepareMocks()
    {
        // prepare mocks
		sf.mockAdmin = mock(LocalAdmin.class);
		sf.mockTypes = mock(ITypes.class);
		sf.mockUpdate = mock(LocalUpdate.class);
    
		sf.mockAdmin.expects(atLeastOnce()).method("userProxy")
			.will( returnValue( user ));
		sf.mockAdmin.expects(atLeastOnce()).method("groupProxy")
			.will( returnValue( group ));
		sf.mockAdmin.expects(atLeastOnce()).method("getMemberOfGroupIds")
		.will( returnValue( memberOfGroups ));
		sf.mockAdmin.expects(atLeastOnce()).method("getLeaderOfGroupIds")
			.will( returnValue( leaderOfGroups ));
		sf.mockTypes.expects(atLeastOnce()).method("getEnumeration")
			.will(returnValue( type ));
		sf.mockUpdate.expects(atLeastOnce()).method("saveAndReturnObject")
			.will(returnValue( event ));
	
    }
    
    @Configuration(afterTestMethod = true)
    protected void tearDown() throws Exception
    {
        super.verify();
        sec.clearCurrentDetails();
        super.tearDown();
    }
	
    // ~ TESTS
	// =========================================================================
    
	/*
	 * Test method for 'ome.security.SecuritySystem.isReady()'
	 */
	public void testIsReady() {
		prepareMocksWithUserDetails();
		
		assertFalse( sec.isReady() );
		sec.setCurrentDetails();
		assertTrue( sec.isReady() );
		sec.clearCurrentDetails();
		assertFalse( sec.isReady() );
		
		// don't need ready sec.sys.
		sec.isReady( );
		sec.isSystemType( null );	
		sec.allowLoad( user.getClass(), new Details() );
		sec.allowCreation( user );
		sec.allowUpdate( user, new Details() );
		sec.allowDelete( user, new Details() );
		sec.getRootId();
		sec.getRootName();
		sec.getSystemGroupName();
		sec.getSystemGroupId();
		sec.getUserGroupId();
		sec.getUserGroupName();
		sec.doAction(user,new SecureAction(){public <T extends IObject> T updateObject(T obj) {return null;};});
		sec.copyToken(user,user);
		sec.setCurrentDetails();
		sec.clearCurrentDetails();
		sec.newEvent(type);
		sec.setCurrentEvent(event);
		sec.emptyDetails();
		sec.disable("foo");
		sec.enable();
		sec.isDisabled("");
		
		// need ready sec.sys
		try { sec.enableReadFilter( null ); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.disableReadFilter( null ); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.transientDetails( null ); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.managedDetails( null, null ); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.currentUserId(); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.currentGroupId(); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.leaderOfGroups(); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.currentUser(); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.currentGroup(); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.currentEvent(); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.currentUserIsAdmin(); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.getCurrentEvent(); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.addLog("",Image.class,1L); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		
		// throw no matter what
		try { sec.throwLoadViolation( user ); fail("Should throw SecViol"); } catch (SecurityViolation sv) {};
		try { sec.throwCreationViolation( user ); fail("Should throw SecViol"); } catch (SecurityViolation sv) {};
		try { sec.throwUpdateViolation( user ); fail("Should throw SecViol"); } catch (SecurityViolation sv) {};
		try { sec.throwDeleteViolation( user ); fail("Should throw SecViol"); } catch (SecurityViolation sv) {};
		
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.isSystemType(Class<?
	 * extends IObject>)'
	 */
	public void testIsSystemType() {
		assertTrue(sec.isSystemType(Experimenter.class));
		assertTrue(sec.isSystemType(ExperimenterGroup.class));
		assertTrue(sec.isSystemType(Event.class));
		assertTrue(sec.isSystemType(EventLog.class));
		assertTrue(sec.isSystemType(EventDiff.class));
		assertTrue(sec.isSystemType(IEnum.class));
		assertFalse(sec.isSystemType(Image.class));
		// TODO what else
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.enableReadFilter(Object)'
	 * Test method for 'ome.security.SecuritySystem.disableReadFilter(Object)'
	 */
	public void testEnableAndDisableReadFilter() {
		prepareMocksWithUserDetails();
		
		Mock mockFilter = mock(Filter.class);
		Filter f = (Filter) mockFilter.proxy();
		mockFilter.expects(once()).method("setParameter")
			.with(eq(SecurityFilter.is_admin), eq(Boolean.FALSE))
			.will( returnValue( f ));
		mockFilter.expects(once()).method("setParameter")
			.with(eq(SecurityFilter.current_user), eq(user.getId()))
			.will( returnValue( f ));
		mockFilter.expects(atLeastOnce()).method("setParameterList")
			.with( eq(SecurityFilter.current_groups), eq(user.eachLinkedExperimenterGroup(new IdBlock())))
			.will( returnValue( f ));
		mockFilter.expects(atLeastOnce()).method("setParameterList")
			.with( eq(SecurityFilter.leader_of_groups),eq(leaderOfGroups))
			.will( returnValue( f ));	
		Mock mockSession = mock(Session.class);
		mockSession.expects(once()).method("enableFilter").with(eq("securityFilter"))
			.will( returnValue( f ));
		mockSession.expects(once()).method("disableFilter").with(eq("securityFilter"));
		Session s = (Session) mockSession.proxy();
	
		sec.setCurrentDetails();
		sec.enableReadFilter(s);
		sec.disableReadFilter(s);
		sec.clearCurrentDetails();
		
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.currentUserId()'
	 */
	public void testCurrentUserId() {
		prepareMocksWithUserDetails();
		sec.setCurrentDetails();
		assertEquals(sec.currentUserId(),(Long)1L);
		sec.clearCurrentDetails();
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.currentUser()'
	 */
	public void testCurrentUser() {
		prepareMocksWithUserDetails();
		sec.setCurrentDetails();
		assertEquals(sec.currentUser(),user);
		sec.clearCurrentDetails();
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.currentGroup()'
	 */
	public void testCurrentGroup() {
		prepareMocksWithUserDetails();
		sec.setCurrentDetails();
		assertEquals(sec.currentGroup(),group);
		sec.clearCurrentDetails();
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.currentEvent()'
	 */
	public void testCurrentEvent() {
		prepareMocksWithUserDetails();
		sec.setCurrentDetails();
		assertEquals(sec.currentEvent(),event);
		sec.clearCurrentDetails();
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.emptyDetails()'
	 */
	public void testEmptyDetails() {
		prepareMocksWithUserDetails();
		sec.clearCurrentDetails();
		assertTrue(sec.emptyDetails());
		sec.setCurrentDetails();
		assertFalse(sec.emptyDetails());
		sec.clearCurrentDetails();
		assertTrue(sec.emptyDetails());
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.addLog(String, Class, Long)'
	 */
	public void testAddLog() {
		prepareMocksWithUserDetails();
		sec.setCurrentDetails();
		assertTrue(event.sizeOfLogs()==0);
		sec.addLog("SHOULDN'T BE ADDED", Event.class, 1L); 
		assertTrue(event.sizeOfLogs()==0); 
		sec.addLog("SHOULD BE ADDED", Image.class, 2L);
		assertTrue(event.sizeOfLogs()==1);
		List<EventLog> logs = event.collectLogs(null);
		EventLog onlyLog = logs.iterator().next();
		assertEquals(onlyLog.getAction(),"SHOULD BE ADDED");
		assertEquals(onlyLog.getType(),Image.class.getName());
		assertEquals(onlyLog.getIdList(),"2");
		sec.clearCurrentDetails();
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.newEvent(EventType)'
	 */
	public void testNewEvent() {
		prepareMocksWithUserDetails();
		sec.setCurrentDetails();
		assertEquals(sec.currentEvent(),event);
		sec.newEvent(type);
		assertNotSame(event, sec.currentEvent());
		sec.clearCurrentDetails();
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.getCurrentEvent()'
	 */
	public void testGetCurrentEvent() {
		prepareMocksWithUserDetails();
		sec.setCurrentDetails();
		assertSame(sec.currentEvent(),event);
		sec.clearCurrentDetails();
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.setCurrentEvent(Event)'
	 */
	public void testSetCurrentEvent() {
		prepareMocksWithUserDetails();
		sec.setCurrentDetails();
		assertSame(sec.currentEvent(),event);
		Event newEvent = new Event();
		sec.setCurrentEvent(newEvent);
		assertNotSame(sec.currentEvent(),event);
		assertSame(sec.currentEvent(),newEvent);
		sec.clearCurrentDetails();
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.clearCurrentDetails()'
	 */
	public void testClearCurrentDetails() {
		prepareMocksWithUserDetails();
		assertFalse(sec.isReady());
		sec.setCurrentDetails();
		assertTrue(sec.isReady());
		sec.clearCurrentDetails();
		assertFalse(sec.isReady());
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.setCurrentDetails()'
	 */
	public void testSetCurrentDetails() {
		prepareMocksWithUserDetails();
		
		sec.setCurrentDetails();
		assertSame(user,sec.currentUser());
		assertSame(event,sec.currentEvent());
		assertSame(group,sec.currentGroup());
		assertTrue(sec.isReady());
		sec.clearCurrentDetails();
	}

	@Test
	public void testNullChecksOnAllMethods() throws Exception {
		prepareMocksWithRootDetails();
		sec.setCurrentDetails();
		
		// can handle nulls
		sec.isSystemType( null );	
		sec.copyToken( null,null);
		sec.newEvent( null );
		sec.setCurrentEvent( null );
		sec.enable(null);
		
		// uses Springs assert
		try { sec.allowLoad( null,null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.allowCreation( null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.allowUpdate( null,null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.allowDelete( null,null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.doAction( null,null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.addLog( null, null,null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.throwLoadViolation( null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.throwCreationViolation( null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.throwUpdateViolation( null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.throwDeleteViolation( null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		
		// api usage
		try { sec.enableReadFilter( null ); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.disableReadFilter( null ); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.transientDetails( null ); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.managedDetails( null, null ); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.isDisabled(null); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.disable(null); fail("Should throw ApiUSage"); } catch (ApiUsageException api) {};
		
	}
	
	@Test
	public void testIsSystemGroup() throws Exception {
		prepareMocksWithRootDetails();
		sec.setCurrentDetails();
		assertTrue(sec.isSystemGroup(group));
		sec.clearCurrentDetails();
	}
	
	@Test
	public void testLeaderOfGroups() throws Exception {
		prepareMocksWithUserDetails();
		sec.setCurrentDetails();
		assertEquals(sec.leaderOfGroups(),leaderOfGroups);
		sec.clearCurrentDetails();
	}

	@Test
	public void testDisblingSubSystems() throws Exception {
		assertFalse( sec.isDisabled("foo") );
		sec.disable( "foo" );
		assertTrue( sec.isDisabled("foo"));
		sec.enable( "foo" );
		assertFalse( sec.isDisabled("foo") );
		sec.disable( "foo" );
		assertTrue( sec.isDisabled("foo") );
		sec.enable();
		assertFalse( sec.isDisabled("foo") );		
	}
	
	// ~ CAN USE MORE WORK
	// =========================================================================
	
	/*
	 * Test method for 'ome.security.SecuritySystem.allowCreation(IObject)'
	 */
	public void testAllowCreation() {
	
		Experimenter e = new Experimenter();
		Image i = new Image();
		
		prepareMocksWithUserDetails();
		
		// 1. not system type
		sec.setCurrentDetails();
		assertFalse(sec.allowCreation(e));
		assertTrue(sec.allowCreation(i));
		sec.clearCurrentDetails();

		// 2. is privileged
		SecureAction checkAllowCreate = new SecureAction(){
			public <T extends IObject> T updateObject(T obj) {
				assertTrue(sec.allowCreation(obj));
				return null;
			}
		};
		sec.doAction(e, checkAllowCreate);
		sec.doAction(i, checkAllowCreate);
		
		// 3. user is admin.
		prepareMocksWithRootDetails();
		sec.setCurrentDetails();
		assertTrue(sec.allowCreation(e));
		assertTrue(sec.allowCreation(i));
		sec.clearCurrentDetails();
		
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.allowUpdate(IObject)'
	 */
	public void testAllowUpdate() {
		
		Experimenter e = new Experimenter();
		Image i = new Image();
		Details d = new Details();
		d.setPermissions( new Permissions() );
		
		prepareMocksWithUserDetails();
		
		// BASICS
		
		// 1. not system type
		sec.setCurrentDetails();
		assertFalse(sec.allowUpdate(e,d));
		assertTrue(sec.allowUpdate(i,d));
		sec.clearCurrentDetails();

		// 2. is privileged
		SecureAction checkAllowCreate = new SecureAction(){
			public <T extends IObject> T updateObject(T obj) {
				assertTrue(sec.allowUpdate(obj,obj.getDetails()));
				return null;
			}
		};
		sec.doAction(e, checkAllowCreate);
		sec.doAction(i, checkAllowCreate);
		
		// 3. user is admin.
		prepareMocksWithRootDetails();
		sec.setCurrentDetails();
		assertTrue(sec.allowUpdate(e,e.getDetails()));
		assertTrue(sec.allowUpdate(i,i.getDetails()));
		sec.clearCurrentDetails();
		
		// PERMISSIONS BASED
		prepareMocksWithUserDetails();
		sec.setCurrentDetails();
		
		// different owner but all permissions
		i = new Image(2L);
		i.getDetails().setOwner(new Experimenter(2L));
		i.getDetails().setGroup(new ExperimenterGroup(2L));
		i.getDetails().setPermissions(new Permissions());
		assertTrue(sec.allowUpdate(i,i.getDetails()));
		
		// now lower permissions
		i.getDetails().setPermissions(Permissions.READ_ONLY);
		assertFalse(sec.allowUpdate(i,i.getDetails()));
	
	}
	
	/*
	 * Test method for 'ome.security.SecuritySystem.allowDelete(IObject)'
	 */
	public void testAllowDelete() {
		Experimenter e = new Experimenter();
		Image i = new Image();
		Details d = new Details();
		d.setPermissions( new Permissions() );
		
		prepareMocksWithUserDetails();
		
		// 1. not system type
		sec.setCurrentDetails();
		assertFalse(sec.allowDelete(e,d));
		assertTrue(sec.allowDelete(i,d));
		sec.clearCurrentDetails();

		// 2. is privileged
		SecureAction checkAllowCreate = new SecureAction(){
			public <T extends IObject> T updateObject(T obj) {
				assertTrue(sec.allowDelete(obj,obj.getDetails()));
				return null;
			}
		};
		sec.doAction(e, checkAllowCreate);
		sec.doAction(i, checkAllowCreate);
		
		// 3. user is admin.
		prepareMocksWithRootDetails();
		sec.setCurrentDetails();
		assertTrue(sec.allowDelete(e,e.getDetails()));
		assertTrue(sec.allowDelete(i,i.getDetails()));
		sec.clearCurrentDetails();
		
		// PERMISSIONS BASED
		prepareMocksWithUserDetails();
		sec.setCurrentDetails();
		
		// different owner but all permissions
		i = new Image(2L);
		i.getDetails().setOwner(new Experimenter(2L));
		i.getDetails().setGroup(new ExperimenterGroup(2L));
		i.getDetails().setPermissions(new Permissions());
		assertTrue(sec.allowDelete(i,i.getDetails()));
		
		// now lower permissions
		i.getDetails().setPermissions(Permissions.READ_ONLY);
		assertFalse(sec.allowDelete(i,i.getDetails()));
		
		sec.clearCurrentDetails();
	}
	
	/*
	 * Test method for 'ome.security.SecuritySystem.allowLoad(IObject)'
	 */
	public void testAllowLoad() {

		prepareMocksWithUserDetails();
		
		Details d = new Details();
		d.setOwner(new Experimenter(2L));
		d.setGroup(new ExperimenterGroup(2L));
		d.setPermissions( new Permissions() );
		
		sec.setCurrentDetails();
		assertTrue(sec.allowLoad(Image.class,d));
		d.setPermissions(new Permissions().revoke(WORLD,READ));
		assertFalse(sec.allowLoad(Image.class, d));
		// now in my group where i'm PI
		d.setPermissions(new Permissions().revoke(GROUP,READ));
		d.setGroup(group);
		assertTrue(sec.allowLoad(Image.class,d));
		
		sec.clearCurrentDetails();
		
	}
	
	/*
	 * Test method for 'ome.security.SecuritySystem.transientDetails(IObject)'
	 */
	public void testTransientDetails() {
		prepareMocksWithUserDetails();
		sec.setCurrentDetails();
		
		Permissions p = new Permissions();
		Image i = new Image();
		
		// setting permissions
		i.getDetails().setOwner(new Experimenter(1L,false));
		i.getDetails().setPermissions(p);
		Details test = sec.transientDetails(i);
		assertEquals(p,test.getPermissions());
		assertEquals(test.getOwner().getId(),user.getId());
		assertEquals(test.getGroup().getId(),group.getId());
	
		// can't change that value
		i.getDetails().setOwner(new Experimenter(3L,false));
		i.getDetails().setPermissions(p);
		try { sec.transientDetails(i); fail("should throw sec. vio."); } catch (SecurityViolation sv) {}
			
		
		sec.clearCurrentDetails();
		
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.managedDetails(IObject,
	 * Details)'
	 */
	public void testManagedDetails() {
		prepareMocksWithUserDetails();
		sec.setCurrentDetails();
		
		Permissions p = new Permissions();
		Image i = new Image(1L);
		
		Details oldDetails = new Details();
		oldDetails.setOwner(user);
		oldDetails.setGroup(group);
		oldDetails.setCreationEvent(event);
		oldDetails.setPermissions(new Permissions());
		
		// setting permissions
		i.getDetails().setOwner(new Experimenter(1L,false));
		i.getDetails().setGroup(new ExperimenterGroup(1L,false));	
		i.getDetails().setCreationEvent(new Event(1L,false));
		i.getDetails().setPermissions(p);
		Details test = sec.managedDetails(i,oldDetails);
		assertTrue(p.sameRights(test.getPermissions()));
		assertEquals(test.getOwner().getId(),user.getId());
		assertEquals(test.getGroup().getId(),group.getId());
	
		// can't change that value
		i.getDetails().setOwner(new Experimenter(3L,false));
		i.getDetails().setPermissions(p);
		try { sec.managedDetails(i,oldDetails); fail("should throw sec. vio."); } catch (SecurityViolation sv) {}
			
		sec.clearCurrentDetails();

	}

	@Test
	public void testRunAsAdmin(){
		prepareMocksWithUserDetails();
		sec.setCurrentDetails();
		
		assertFalse(sec.currentUserIsAdmin());
		
		AdminAction action = new AdminAction(){
			public void runAsAdmin() {
				assertTrue(sec.currentUserIsAdmin());
			}
		};		
		sec.runAsAdmin(action);
		
		assertFalse(sec.currentUserIsAdmin());
	
	}

	@Test
	public void testDoAction() {
		fail("implement");
	}

	@Test
	public void testTokenFunctionality() throws Exception {
		fail("also can't access from outside");
	}
	
	@Test
	public void testLeaderOfGroupsProperlyFilled() throws Exception {
		fail("add to security interface.");
	}
		
}

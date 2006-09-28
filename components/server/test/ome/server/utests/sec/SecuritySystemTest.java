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
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.AdminAction;
import ome.security.basic.BasicSecuritySystem;
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
public class SecuritySystemTest extends AbstractBasicSecuritySystemTest {

	/*
	 * Test method for 'ome.security.SecuritySystem.isReady()'
	 */
	public void testIsReady() {
		prepareMocksWithUserDetails(false);
		
		assertFalse( sec.isReady() );
		sec.loadEventContext(false);
		assertTrue( sec.isReady() );
		sec.clearEventContext();
		assertFalse( sec.isReady() );
		
		// don't need ready sec.sys.
		sec.isReady( );
		sec.isSystemType( null );	
		sec.getACLVoter().allowLoad( user.getClass(), new Details() );
		sec.getACLVoter().allowCreation( user );
		sec.getACLVoter().allowUpdate( user, new Details() );
		sec.getACLVoter().allowDelete( user, new Details() );
		sec.getSecurityRoles();
		sec.doAction(user,new SecureAction(){public <T extends IObject> T updateObject(T obj) {return null;};});
		sec.copyToken(user,user);
		sec.loadEventContext(false);
		sec.clearEventContext();
		sec.newEvent(type);
		sec.setCurrentEvent(event);
		sec.isEmptyEventContext();
		sec.disable("foo");
		sec.enable();
		sec.isDisabled("");
		
		// need ready sec.sys
		try { sec.enableReadFilter( null ); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.disableReadFilter( null ); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.newTransientDetails( null ); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.checkManagedDetails( null, null ); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
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
		try { sec.getACLVoter().throwLoadViolation( user ); fail("Should throw SecViol"); } catch (SecurityViolation sv) {};
		try { sec.getACLVoter().throwCreationViolation( user ); fail("Should throw SecViol"); } catch (SecurityViolation sv) {};
		try { sec.getACLVoter().throwUpdateViolation( user ); fail("Should throw SecViol"); } catch (SecurityViolation sv) {};
		try { sec.getACLVoter().throwDeleteViolation( user ); fail("Should throw SecViol"); } catch (SecurityViolation sv) {};
		
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
		assertTrue(sec.isSystemType(IEnum.class));
		assertFalse(sec.isSystemType(Image.class));
		// TODO what else
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.enableReadFilter(Object)'
	 * Test method for 'ome.security.SecuritySystem.disableReadFilter(Object)'
	 */
	public void testEnableAndDisableReadFilter() {
		prepareMocksWithUserDetails(false);
		
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
	
		sec.loadEventContext(false);
		sec.enableReadFilter(s);
		sec.disableReadFilter(s);
		sec.clearEventContext();
		
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.currentUserId()'
	 */
	public void testCurrentUserId() {
		prepareMocksWithUserDetails(false);
		sec.loadEventContext(false);
		assertEquals(sec.currentUserId(),(Long)1L);
		sec.clearEventContext();
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.currentUser()'
	 */
	public void testCurrentUser() {
		prepareMocksWithUserDetails(false);
		sec.loadEventContext(false);
		assertEquals(sec.currentUser(),user);
		sec.clearEventContext();
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.currentGroup()'
	 */
	public void testCurrentGroup() {
		prepareMocksWithUserDetails(false);
		sec.loadEventContext(false);
		assertEquals(sec.currentGroup(),group);
		sec.clearEventContext();
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.currentEvent()'
	 */
	public void testCurrentEvent() {
		prepareMocksWithUserDetails(false);
		sec.loadEventContext(false);
		assertEquals(sec.currentEvent(),event);
		sec.clearEventContext();
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.emptyDetails()'
	 */
	public void testEmptyDetails() {
		prepareMocksWithUserDetails(false);
		sec.clearEventContext();
		assertTrue(sec.isEmptyEventContext());
		sec.loadEventContext(false);
		assertFalse(sec.isEmptyEventContext());
		sec.clearEventContext();
		assertTrue(sec.isEmptyEventContext());
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.addLog(String, Class, Long)'
	 */
	public void testAddLog() {
		prepareMocksWithUserDetails(false);
		sec.loadEventContext(false);
		assertTrue(sec.getLogs().size()==0);
		sec.addLog("SHOULDN'T BE ADDED", Event.class, 1L); 
		assertTrue(sec.getLogs().size()==0); 
		sec.addLog("SHOULD BE ADDED", Image.class, 2L);
		
		//ticket:328
		assertTrue(sec.getLogs().size()==1);
		EventLog onlyLog = sec.getLogs().get(0);
		assertEquals(onlyLog.getAction(),"SHOULD BE ADDED");
		assertEquals(onlyLog.getEntityType(),Image.class.getName());
		assertEquals(onlyLog.getEntityId(),new Long(2L));
		sec.clearEventContext();
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.newEvent(EventType)'
	 */
	public void testNewEvent() {
		prepareMocksWithUserDetails(false);
		sec.loadEventContext(false);
		assertEquals(sec.currentEvent(),event);
		sec.newEvent(type);
		assertNotSame(event, sec.currentEvent());
		sec.clearEventContext();
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.getCurrentEvent()'
	 */
	public void testGetCurrentEvent() {
		prepareMocksWithUserDetails(false);
		sec.loadEventContext(false);
		assertSame(sec.currentEvent(),event);
		sec.clearEventContext();
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.setCurrentEvent(Event)'
	 */
	public void testSetCurrentEvent() {
		prepareMocksWithUserDetails(false);
		sec.loadEventContext(false);
		assertSame(sec.currentEvent(),event);
		Event newEvent = new Event();
		sec.setCurrentEvent(newEvent);
		assertNotSame(sec.currentEvent(),event);
		assertSame(sec.currentEvent(),newEvent);
		sec.clearEventContext();
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.clearCurrentDetails()'
	 */
	public void testClearCurrentDetails() {
		prepareMocksWithUserDetails(false);
		assertFalse(sec.isReady());
		sec.loadEventContext(false);
		assertTrue(sec.isReady());
		sec.clearEventContext();
		assertFalse(sec.isReady());
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.setCurrentDetails()'
	 */
	public void testSetCurrentDetails() {
		prepareMocksWithUserDetails(false);
		
		sec.loadEventContext(false);
		assertSame(user,sec.currentUser());
		assertSame(event,sec.currentEvent());
		assertSame(group,sec.currentGroup());
		assertTrue(sec.isReady());
		sec.clearEventContext();
	}

	@Test
	public void testNullChecksOnAllMethods() throws Exception {
		prepareMocksWithRootDetails(false);
		sec.loadEventContext(false);
		
		// can handle nulls
		sec.isSystemType( null );	
		sec.copyToken( null,null);
		sec.newEvent( null );
		sec.setCurrentEvent( null );
		sec.enable(null);
		
		// uses Springs assert
		try { sec.getACLVoter().allowLoad( null,null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.getACLVoter().allowCreation( null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.getACLVoter().allowUpdate( null,null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.getACLVoter().allowDelete( null,null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.doAction( null,null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.addLog( null, null,null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.getACLVoter().throwLoadViolation( null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.getACLVoter().throwCreationViolation( null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.getACLVoter().throwUpdateViolation( null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		try { sec.getACLVoter().throwDeleteViolation( null ); fail("Should throw IllegalArg"); } catch (IllegalArgumentException iae) {};
		
		// api usage
		try { sec.enableReadFilter( null ); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.disableReadFilter( null ); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.newTransientDetails( null ); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.checkManagedDetails( null, null ); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.isDisabled(null); fail("Should throw ApiUsage"); } catch (ApiUsageException api) {};
		try { sec.disable(null); fail("Should throw ApiUSage"); } catch (ApiUsageException api) {};
		
	}
	
	@Test
	public void testIsSystemGroup() throws Exception {
		prepareMocksWithRootDetails(false);
		sec.loadEventContext(false);
		assertTrue(sec.getSecurityRoles().isSystemGroup(group));
		sec.clearEventContext();
	}
	
	@Test
	public void testLeaderOfGroups() throws Exception {
		prepareMocksWithUserDetails(false);
		sec.loadEventContext(false);
		assertEquals(sec.leaderOfGroups(),leaderOfGroups);
		sec.clearEventContext();
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
		
		prepareMocksWithUserDetails(false);
		
		// 1. not system type
		sec.loadEventContext(false);
		assertFalse(sec.getACLVoter().allowCreation(e));
		assertTrue(sec.getACLVoter().allowCreation(i));
		sec.clearEventContext();

		// 2. is privileged
		SecureAction checkAllowCreate = new SecureAction(){
			public <T extends IObject> T updateObject(T obj) {
				assertTrue(sec.getACLVoter().allowCreation(obj));
				return null;
			}
		};
		sec.doAction(e, checkAllowCreate);
		sec.doAction(i, checkAllowCreate);
		
		// 3. user is admin.
		prepareMocksWithRootDetails(false);
		sec.loadEventContext(false);
		assertTrue(sec.getACLVoter().allowCreation(e));
		assertTrue(sec.getACLVoter().allowCreation(i));
		sec.clearEventContext();
		
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.allowUpdate(IObject)'
	 */
	public void testAllowUpdate() {
		
		Experimenter e = new Experimenter();
		Image i = new Image();
		Details d = new Details();
		d.setPermissions( new Permissions() );
		
		prepareMocksWithUserDetails(false);
		
		// BASICS
		
		// 1. not system type
		sec.loadEventContext(false);
		assertFalse(sec.getACLVoter().allowUpdate(e,d));
		assertTrue(sec.getACLVoter().allowUpdate(i,d));
		sec.clearEventContext();

		// 2. is privileged
		SecureAction checkAllowCreate = new SecureAction(){
			public <T extends IObject> T updateObject(T obj) {
				assertTrue(sec.getACLVoter().allowUpdate(obj,obj.getDetails()));
				return null;
			}
		};
		sec.doAction(e, checkAllowCreate);
		sec.doAction(i, checkAllowCreate);
		
		// 3. user is admin.
		prepareMocksWithRootDetails(false);
		sec.loadEventContext(false);
		assertTrue(sec.getACLVoter().allowUpdate(e,e.getDetails()));
		assertTrue(sec.getACLVoter().allowUpdate(i,i.getDetails()));
		sec.clearEventContext();
		
		// PERMISSIONS BASED
		prepareMocksWithUserDetails(false);
		sec.loadEventContext(false);
		
		// different owner but all permissions
		i = new Image(2L);
		i.getDetails().setOwner(new Experimenter(2L));
		i.getDetails().setGroup(new ExperimenterGroup(2L));
		i.getDetails().setPermissions(new Permissions());
		assertTrue(sec.getACLVoter().allowUpdate(i,i.getDetails()));
		
		// now lower permissions
		i.getDetails().setPermissions(Permissions.READ_ONLY);
		assertFalse(sec.getACLVoter().allowUpdate(i,i.getDetails()));
	
	}
	
	/*
	 * Test method for 'ome.security.SecuritySystem.allowDelete(IObject)'
	 */
	public void testAllowDelete() {
		Experimenter e = new Experimenter();
		Image i = new Image();
		Details d = new Details();
		d.setPermissions( new Permissions() );
		
		prepareMocksWithUserDetails(false);
		
		// 1. not system type
		sec.loadEventContext(false);
		assertFalse(sec.getACLVoter().allowDelete(e,d));
		assertTrue(sec.getACLVoter().allowDelete(i,d));
		sec.clearEventContext();

		// 2. is privileged
		SecureAction checkAllowCreate = new SecureAction(){
			public <T extends IObject> T updateObject(T obj) {
				assertTrue(sec.getACLVoter().allowDelete(obj,obj.getDetails()));
				return null;
			}
		};
		sec.doAction(e, checkAllowCreate);
		sec.doAction(i, checkAllowCreate);
		
		// 3. user is admin.
		prepareMocksWithRootDetails(false);
		sec.loadEventContext(false);
		assertTrue(sec.getACLVoter().allowDelete(e,e.getDetails()));
		assertTrue(sec.getACLVoter().allowDelete(i,i.getDetails()));
		sec.clearEventContext();
		
		// PERMISSIONS BASED
		prepareMocksWithUserDetails(false);
		sec.loadEventContext(false);
		
		// different owner but all permissions
		i = new Image(2L);
		i.getDetails().setOwner(new Experimenter(2L));
		i.getDetails().setGroup(new ExperimenterGroup(2L));
		i.getDetails().setPermissions(new Permissions());
		assertTrue(sec.getACLVoter().allowDelete(i,i.getDetails()));
		
		// now lower permissions
		i.getDetails().setPermissions(Permissions.READ_ONLY);
		assertFalse(sec.getACLVoter().allowDelete(i,i.getDetails()));
		
		sec.clearEventContext();
	}
	
	/*
	 * Test method for 'ome.security.SecuritySystem.allowLoad(IObject)'
	 */
	public void testAllowLoad() {

		prepareMocksWithUserDetails(false);
		
		Details d = new Details();
		d.setOwner(new Experimenter(2L));
		d.setGroup(new ExperimenterGroup(2L));
		d.setPermissions( new Permissions() );
		
		sec.loadEventContext(false);
		assertTrue(sec.getACLVoter().allowLoad(Image.class,d));
		d.setPermissions(new Permissions().revoke(WORLD,READ));
		assertFalse(sec.getACLVoter().allowLoad(Image.class, d));
		// now in my group where i'm PI
		d.setPermissions(new Permissions().revoke(GROUP,READ));
		d.setGroup(group);
		assertTrue(sec.getACLVoter().allowLoad(Image.class,d));
		
		sec.clearEventContext();
		
	}
	
	/*
	 * Test method for 'ome.security.SecuritySystem.transientDetails(IObject)'
	 */
	public void testTransientDetails() {
		prepareMocksWithUserDetails(false);
		sec.loadEventContext(false);
		
		Permissions p = new Permissions();
		Image i = new Image();
		
		// setting permissions
		i.getDetails().setOwner(new Experimenter(1L,false));
		i.getDetails().setPermissions(p);
		Details test = sec.newTransientDetails(i);
		assertEquals(p,test.getPermissions());
		assertEquals(test.getOwner().getId(),user.getId());
		assertEquals(test.getGroup().getId(),group.getId());
	
		// can't change that value
		i.getDetails().setOwner(new Experimenter(3L,false));
		i.getDetails().setPermissions(p);
		try { sec.newTransientDetails(i); fail("should throw sec. vio."); } catch (SecurityViolation sv) {}
			
		
		sec.clearEventContext();
		
	}

	/*
	 * Test method for 'ome.security.SecuritySystem.managedDetails(IObject,
	 * Details)'
	 */
	public void testManagedDetails() {
		prepareMocksWithUserDetails(false);
		sec.loadEventContext(false);
		
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
		Details test = sec.checkManagedDetails(i,oldDetails);
		assertTrue(p.sameRights(test.getPermissions()));
		assertEquals(test.getOwner().getId(),user.getId());
		assertEquals(test.getGroup().getId(),group.getId());
	
		// can't change that value
		i.getDetails().setOwner(new Experimenter(3L,false));
		i.getDetails().setPermissions(p);
		try { sec.checkManagedDetails(i,oldDetails); fail("should throw sec. vio."); } catch (SecurityViolation sv) {}
			
		sec.clearEventContext();

	}

	@Test
	public void testRunAsAdmin(){
		prepareMocksWithUserDetails(false);
		sec.loadEventContext(false);
		
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
		prepareMocksWithUserDetails(false);
		sec.loadEventContext(false);
		try {
			sec.doAction(null, new SecureAction(){
				public <T extends IObject> T updateObject(T obj) {
					fail("implement");
					return null;
				}
			});
			fail("Where's the IllegalArgumentEx?");
		} catch (IllegalArgumentException iae) {
			// ok
		}
	}

	@Test
	public void testTokenFunctionality() throws Exception {
		
		Image i = new Image();
		assertFalse(sec.hasPrivilegedToken(i));
		
		prepareMocksWithUserDetails(false);
		sec.loadEventContext(false);
		sec.doAction(i, new SecureAction(){
			public <T extends IObject> T updateObject(T obj) {
				assertTrue(sec.hasPrivilegedToken(obj));
				Image test = new Image();
				sec.copyToken(obj, test);
				assertTrue(sec.hasPrivilegedToken(test));
				return null;
			}
		});
	
	}
	
	@Test
	public void testLeaderAndMemberOfGroupsProperlyFilled() throws Exception{
		prepareMocksWithUserDetails(false);
		sec.loadEventContext(false);
		
		List<Long> l;
		
		l = sec.getEventContext().getLeaderOfGroupsList();
		assertTrue(l.containsAll(leaderOfGroups));
		assertTrue(leaderOfGroups.containsAll(l));

		l = sec.getEventContext().getMemberOfGroupsList();
		assertTrue(l.containsAll(memberOfGroups));
		assertTrue(memberOfGroups.containsAll(l));

	}

	@Test
	public void testReadOnlyFunctionality() throws Exception {
		prepareMocksWithUserDetails(true);
		sec.loadEventContext(true);
		assertTrue(sec.getEventContext().isReadOnly());
		assertNull(sec.getEventContext().getCurrentEventId());
	}
	
}

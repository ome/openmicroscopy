/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sessions;

import java.util.Collections;
import java.util.List;

import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.services.sessions.SessionManagerImpl;
import ome.system.Principal;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class SessMgrUnitTest extends MockObjectTestCase {

    private Mock updateMock;
    
    private Mock queryMock;

    private SessionManagerImpl mgr;
    private LocalUpdate update;

    private LocalQuery query;

    private Session session;

    @BeforeTest
    public void config() {
        updateMock = mock(LocalUpdate.class);
        update = (LocalUpdate) updateMock.proxy();
        queryMock = mock(LocalQuery.class);
        query = (LocalQuery) queryMock.proxy();
        
        mgr = new SessionManagerImpl();
        mgr.setUpdateService(update);
        mgr.setQueryService(query);
        
        TestCache cache = new TestCache();
        mgr.setCache(cache);

        session = new Session();
        session.setUuid("uuid");
        session.setId(1L);
    }


	Experimenter user = new Experimenter(1L,true);
	ExperimenterGroup group = new ExperimenterGroup(1L, true);
	List<Long> m_ids = Collections.singletonList(1L);
	List<Long> l_ids = Collections.singletonList(1L);
	List<String> roles = Collections.singletonList("single");

    void prepareSessionCreation() {
    	queryMock.expects(once()).method("findAllByQuery")
		.will(returnValue(Collections.singletonList(new ExperimenterGroup())));
    	updateMock.expects(once()).method("saveAndReturnObject").will(new SetIdStub(1L));
    }
    
    @Test
    public void testCreateNewSession() throws Exception {
        /*
         * user logs in with: - non-sessioned principal / !E session =>
         * createSession - non-sessioned principal / E session => joinSession -
         * sessionedPrincipal => hasSession() != null, then ok
         */

    	prepareSessionCreation();
    	session = mgr.create(user,group,m_ids,l_ids,roles,"Test",null);
    	assertNotNull(session);
    	assertNotNull(session.getUuid());

    }

    @Test
    public void testThatPermissionsAreAlreadyNonReadable() throws Exception{
        testCreateNewSession();
        Permissions p = session.getDetails().getPermissions();
        assertFalse(p.isGranted(Role.GROUP, Right.READ));
        assertFalse(p.isGranted(Role.GROUP, Right.WRITE));
        assertFalse(p.isGranted(Role.WORLD, Right.READ));
        assertFalse(p.isGranted(Role.WORLD, Right.WRITE));
    }
    
    @Test
    public void testThatDefaultPermissionsAreAssigned() throws Exception{
        testCreateNewSession();
        assertNotNull(session.getDefaultPermissions());
    }

    @Test
    public void testThatDefaultTypeAreAssigned() throws Exception{
        testCreateNewSession();
        assertNotNull(session.getDefaultEventType());
    }

    @Test
    public void testThatStartedTimeIsSet() throws Exception{
        testCreateNewSession();
        assertNotNull(session.getStarted());
        assertTrue(session.getStarted().getTime() <= System.currentTimeMillis());
    }
    
    @Test
    public void testThatCreatedSessionIsFindable() throws Exception{
        testCreateNewSession();
        // now user has an active session, so basic security wiring will
        // allow method executions
        assertNotNull(mgr.find(session.getUuid()));
    }
    
    @Test
    public void testThatCreatedSessionIsUpdateable() throws Exception{
        
    	Session rv = new Session();
    	
    	testCreateNewSession();
        session.setDefaultEventType("somethingnew");
        updateMock.expects(once()).method("saveAndReturnObject").will(returnValue(rv));
        Session test = mgr.update(session);
        assertTrue(test == rv); // insists that a copy is performed
    }

    @Test
    public void testThatCopiesHaveAllTheRightFields() throws Exception{
        testCreateNewSession();
        Session copy = new Session();
        mgr.copy(session,copy);
        assertFalse(copy == session);
        assertNotNull(copy.getId());
        assertNotNull(copy.getStarted());
        assertNotNull(copy.getUuid());
        assertNotNull(copy.getDefaultEventType());
        assertNotNull(copy.getDefaultPermissions());
        assertNotNull(copy.getDetails().getPermissions());
    }    

    @Test
    public void testThatUpdateProperlyHandlesDetails() throws Exception{
        testCreateNewSession();
        
        Session updated = new Session();
        updated.setDetails(null);

        
    }    

    
    
    @Test
    public void testThatTimedOutSessionsAreMarkedAsSuch() throws Exception{
    	// With a restricted cache something should get push out.
    	mgr.setCache( new TestCache("quick",1,0,0,null));

    	prepareSessionCreation();
    	Session s1 = mgr.create(user,group,m_ids,l_ids,roles,"Test",null);
    	
    	prepareSessionCreation();
    	Session s2 = mgr.create(user,group,m_ids,l_ids,roles,"Test",null);
    	assertTrue(
    			mgr.find(s1.getUuid()) == null 
    			|| mgr.find(s2.getUuid()) == null);
    }
    
    @Test
    public void testThatManagerCanCloseASession() throws Exception {
    	testCreateNewSession();
    	Constraint closedSession = new Constraint() {
			public boolean eval(Object arg0) {
				Session s = (Session) arg0;
				return (System.currentTimeMillis() - s.getClosed().getTime()) < 1000L; 
			}

			public StringBuffer describeTo(StringBuffer arg0) {
				return arg0.append("closed time was less than 1 sec ago");
			}
    		
    	};
    	updateMock.expects(once()).method("saveObject").with(closedSession);
    	mgr.close(session.getUuid());
    	assertNull(mgr.find(session.getUuid()));

    }
    
    @Test
    public void testThatManagerHandleAnExceptionOnClose() throws Exception {
    	testCreateNewSession();
    	Constraint closedSession = new Constraint() {
			public boolean eval(Object arg0) {
				Session s = (Session) arg0;
				return (System.currentTimeMillis() - s.getClosed().getTime()) < 1000L; 
			}

			public StringBuffer describeTo(StringBuffer arg0) {
				return arg0.append("closed time was less than 1 sec ago");
			}
    		
    	};
    	updateMock.expects(once()).method("saveObject").with(closedSession);
    	mgr.close(session.getUuid());
    	assertNull(mgr.find(session.getUuid()));
    	fail("NYI");
    }
    
    @Test
    public void testThatManagerKeepsUpWithRolesPerSession() throws Exception {
    	testCreateNewSession();
    	
    	List<String> roles = mgr.getUserRoles(session.getUuid());
    	assertNotNull(roles);
    	assertTrue(roles.size() > 0);
    }
    
    @Test
    public void testThatManagerCanHandleEvent() throws Exception {
    	testCreateNewSession();
    	List<String> preUserRoles = mgr.getUserRoles(session.getUuid());
    	mgr.onApplicationEvent(null);
    	List<String> postUserRoles = mgr.getUserRoles(session.getUuid());
    	fail("Should here remove user from group and have roles updated.");
    }

    public void testReplacesNullGroupAndType() throws Exception {
        mgr.create(new Principal("fake",null,null));
    }
    
    
}

/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sessions;

import java.util.Collections;
import java.util.List;

import ome.api.local.LocalAdmin;
import ome.api.local.LocalQuery;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.services.sessions.SessionBean;
import ome.services.sessions.SessionManager;
import ome.system.Principal;
import ome.system.Roles;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class SessionBeanUnitTest extends MockObjectTestCase {

    private SessionBean bean;
    private Mock smMock;
    private SessionManager mgr;
    private LocalAdmin admin;
    private LocalQuery query;
    private Mock adminMock, queryMock;
    private Session session;
    private Principal principal;

    @BeforeTest
    public void config() {
        adminMock = mock(LocalAdmin.class);
        admin = (LocalAdmin) adminMock.proxy();
        queryMock = mock(LocalQuery.class);
        query = (LocalQuery) queryMock.proxy();
        smMock = mock(SessionManager.class);
        mgr = (SessionManager) smMock.proxy();
        bean = new SessionBean();
        bean.setLocalAdmin(admin);
        bean.setManager(mgr);
        bean.setLocalQuery(query);
        bean.setRoles(new Roles());
        session = new Session();
        session.setId(1L);
        session.setUuid("uuid");
        principal = new Principal("name", "group", "type");
    }
    
	Experimenter user = new Experimenter(1L,true);
	ExperimenterGroup group = new ExperimenterGroup(1L, true);
	List<Long> m_ids = Collections.singletonList(1L);
	List<Long> l_ids = Collections.singletonList(1L);
	List<String> userRoles = Collections.singletonList("user");
	List<ExperimenterGroup> nonUserDefaultGroups = Collections.singletonList(group);
    
	void prepareForCreateSession() {
		adminMock.expects(once()).method("userProxy").will(returnValue(user));
		adminMock.expects(once()).method("groupProxy").will(returnValue(group));
		adminMock.expects(once()).method("getMemberOfGroupIds").will(returnValue(m_ids));
		adminMock.expects(once()).method("getLeaderOfGroupIds").will(returnValue(l_ids));
		adminMock.expects(once()).method("getUserRoles").will(returnValue(userRoles));
    	adminMock.expects(once()).method("checkPassword").will(
                returnValue(true));
	}
    
    @Test(expectedExceptions = ApiUsageException.class)
    public void testCreateSessionFailsAUEOnNullPrincipal() throws Exception {
        adminMock.expects(once()).method("checkPassword").will(
                returnValue(false));
        bean.createSession(null, "password");
    }
    
    @Test(expectedExceptions = ApiUsageException.class)
    public void testCreateSessionFailsAUEOnNullOmeName() throws Exception {
        adminMock.expects(once()).method("checkPassword").will(
                returnValue(false));
        bean.createSession(new Principal(null,null,null), "password");
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testCreateSessionFailsSV() throws Exception {
        adminMock.expects(once()).method("checkPassword").will(
                returnValue(false));
    	bean.createSession(principal, "password");
    }

    @Test
    public void testCreateSessionPasses() throws Exception {
    	prepareForCreateSession();
        smMock.expects(once()).method("create").will(returnValue(session));
        assertEquals(session, bean.createSession(principal, "password"));
    }

    @Test
    public void testUpdate() throws Exception {
        testCreateSessionPasses();
        smMock.expects(once()).method("update").will(returnValue(session));
        session.setUserAgent("test");
        bean.updateSession(session);
    }

    @Test
    public void testClose() throws Exception {
        smMock.expects(once()).method("close");
        bean.closeSession(session);
    }
    
    @Test( expectedExceptions = SecurityViolation.class)
    public void testChecksForDefaultGroupsOnCreation() throws Exception {
    	prepareForCreateSession();
    	queryMock.expects(once()).method("findAllByQuery").will(returnValue(Collections.EMPTY_LIST));
    	bean.createSession(new Principal("user","user","User"),"user");
    }

}

/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sessions;

import ome.api.local.LocalAdmin;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.model.meta.Session;
import ome.services.sessions.SessionBean;
import ome.services.sessions.SessionManager;
import ome.system.Principal;

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

    private Mock adminMock;

    private Session session;

    private Principal principal;

    @BeforeTest
    public void config() {

        adminMock = mock(LocalAdmin.class);
        admin = (LocalAdmin) adminMock.proxy();
        smMock = mock(SessionManager.class);
        mgr = (SessionManager) smMock.proxy();
        bean = new SessionBean();
        bean.setLocalAdmin(admin);
        bean.setManager(mgr);
        session = new Session();
        session.setId(1L);
        session.setUuid("uuid");
        principal = new Principal("name", "group", "type");
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testCreateSessionFailsAUE() throws Exception {
        adminMock.expects(once()).method("checkPassword").will(
                returnValue(false));
        bean.createSession(null, "password");
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testCreateSessionFailsSV() throws Exception {
        adminMock.expects(once()).method("checkPassword").will(
                returnValue(false));
        bean.createSession(principal, "password");
    }

    @Test
    public void testCreateSessionPasses() throws Exception {
        adminMock.expects(once()).method("checkPassword").will(
                returnValue(true));
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

}

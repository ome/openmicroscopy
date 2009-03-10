/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sessions;

import ome.conditions.AuthenticationException;
import ome.conditions.SessionException;
import ome.model.meta.Session;
import ome.security.basic.CurrentDetails;
import ome.services.sessions.SessionBean;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
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

    private Executor ex;
    private SessionBean bean;
    private Mock smMock, exMock;
    private SessionManager mgr;
    private Session session;
    private Principal principal;

    @BeforeTest
    public void config() {
        smMock = mock(SessionManager.class);
        mgr = (SessionManager) smMock.proxy();
        
        exMock = mock(Executor.class);
        ex = (Executor) exMock.proxy();

        bean = new SessionBean(mgr, ex, new CurrentDetails());
        session = new Session();
        session.setId(1L);
        session.setUuid("uuid");
        principal = new Principal("name", "group", "type");
    }

    @Test(expectedExceptions = SessionException.class)
    public void testCreateWithNullSessionFailsWithSessionException()
            throws Exception {
        smMock.expects(once()).method("create").will(
                throwException(new AuthenticationException("")));
        bean.createSession(principal, "password");
    }

    @Test
    public void testCreateSessionPasses() throws Exception {
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
        smMock.expects(once()).method("close").will(returnValue(0));
        bean.closeSession(session);
    }

}
/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests.sessions;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ome.conditions.AuthenticationException;
import ome.conditions.SessionException;
import ome.model.internal.Permissions;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.security.basic.CurrentDetails;
import ome.security.basic.LightAdminPrivileges;
import ome.services.sessions.SessionBean;
import ome.services.sessions.SessionContextImpl;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.Roles;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Invocation;
import org.jmock.core.Stub;
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

        bean = new SessionBean(mgr, ex, new CurrentDetails(), new LightAdminPrivileges(new Roles()));
        session = new Session();
        session.setId(1L);
        session.setUuid("uuid");
        ExperimenterGroup group = new ExperimenterGroup();
        group.getDetails().setPermissions(new Permissions());
        session.getDetails().setGroup(group);
        principal = new Principal("name", "group", "type");
    }

    @Test(expectedExceptions = SessionException.class)
    public void testCreateWithNullSessionFailsWithSessionException()
            throws Exception {
        smMock.expects(once()).method("createWithAgent").will(
                throwException(new AuthenticationException("")));
        bean.createSession(principal, "password");
    }

    @Test
    public void testCreateSessionPasses() throws Exception {
        smMock.expects(once()).method("createWithAgent").will(returnValue(session));
        assertEquals(session, bean.createSession(principal, "password"));
    }

    @Test
    public void testUpdate() throws Exception {
        testCreateSessionPasses();
        expectsExecutorSubmit();
        smMock.expects(once()).method("update").will(returnValue(session));
        session.setUserAgent("test");
        bean.updateSession(session);
    }

    @Test
    public void testClose() throws Exception {
        expectsExecutorSubmit();
        smMock.expects(once()).method("close").will(returnValue(0));
        bean.closeSession(session);
    }

    @Test(groups = "ticket:3239")
    public void testProperIsAdminImplementation() throws Exception {
        List<Long> leaderOfGroupsIds = Arrays.asList(1L);
        List<Long> memberOfGroupsIds = Arrays.asList(0L, 1L);
        SessionContextImpl sessionContext = new SessionContextImpl(session,
                leaderOfGroupsIds, memberOfGroupsIds, Arrays.asList("user"),
                null, new Roles(), null);
        assertTrue(sessionContext.isCurrentUserAdmin());
    }


    private void expectsExecutorSubmit() {
        exMock.expects(once()).method("submit").will(new Stub(){

            public Object invoke(Invocation arg0) throws Throwable {
                Callable callable = (Callable) arg0.parameterValues.get(0);
                final Object rv = callable.call();
                return new Future() {

                    public boolean cancel(boolean arg0) {
                        throw new UnsupportedOperationException();
                    }

                    public Object get() throws InterruptedException,
                            ExecutionException {
                        return rv;
                    }

                    public Object get(long arg0, TimeUnit arg1)
                            throws InterruptedException, ExecutionException,
                            TimeoutException {
                        return rv;
                    }

                    public boolean isCancelled() {
                        throw new UnsupportedOperationException();
                    }

                    public boolean isDone() {
                        throw new UnsupportedOperationException();
                    }};
            }

            public StringBuffer describeTo(StringBuffer arg0) {
                arg0.append("calls submit");
                return arg0;
            }});

        exMock.expects(once()).method("get").will(new Stub(){

            public Object invoke(Invocation arg0) throws Throwable {
                return ((Future)arg0.parameterValues.get(0)).get();
            }

            public StringBuffer describeTo(StringBuffer arg0) {
                return arg0;
            }});
    }
}
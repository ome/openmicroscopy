/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sessions;

import java.util.List;

import ome.model.meta.Experimenter;
import ome.model.meta.Session;
import ome.server.itests.AbstractManagedContextTest;
import ome.system.Principal;

import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class SessionBeanIntegrationTest extends AbstractManagedContextTest {

    @Test
    public void testUserCreatesOwnSession() throws Exception {
        loginNewUser();
        String group = iAdmin.getEventContext().getCurrentGroupName();
        factory.getSessionService().createUserSession(0, 1000, group);
    }

    @Test
    public void testRootCreatesSession() throws Exception {
        Experimenter e = loginNewUser();
        Principal p = new Principal(e.getOmeName() ,"user","User");
        // TODO currently null for EventType is not allowed.
        loginRoot();
        factory.getSessionService().createSessionWithTimeout(p, 1000L);
    }

    @Test
    public void testUserClosesSession() throws Exception {
        Experimenter e = loginNewUser();
        String sessionUuid = this.loginAop.p.getName();
        Session s = new Session();
        s.setUuid(sessionUuid);
        factory.getSessionService().closeSession(s);
    }

    @Test(groups = "ticket:1975")
    public void testListingSessions() {
        Experimenter e = loginNewUser(); // First session
        loginUserKeepGroup(e); // Second session

        List<Session> sessions = iSession.getMyOpenSessions();
        assertEquals(2, sessions.size());

        sessions = iSession.getMyOpenAgentSessions(null);
        assertEquals(2, sessions.size());

        sessions = iSession.getMyOpenAgentSessions("OMERO.test");
        assertEquals(0, sessions.size());

        sessions = iSession.getMyOpenClientSessions();
        assertEquals(0, sessions.size());
    }
}

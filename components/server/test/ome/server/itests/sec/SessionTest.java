/*
 *   $Id$
 *
 *   Copyright 2007-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.ISession;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.server.itests.LoginInterceptor;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class SessionTest extends AbstractManagedContextTest {

    @Test
    public void testSimpleCreate() throws Exception {

        loginRoot();
        final Experimenter e = loginNewUser();

        final IAdmin a = iAdmin;
        final ServiceFactory f = factory;
        final LoginInterceptor aop = loginAop;
        final boolean[] success = new boolean[1];
        Thread t = new Thread() {
            @Override
            public void run() {
                ISession service = f.getServiceByClass(ISession.class);
                Session s = service.createSession(new Principal(e.getOmeName(), "user",
                        "Test"), "ome");

                aop.p = new Principal(s.getUuid(), "user", "Test");

                // Now we should be able to do something.
                EventContext ec = a.getEventContext();
                AbstractManagedContextTest.assertEquals(
                        ec.getCurrentUserName(), e.getOmeName());

                service.closeSession(s);
                success[0] = true;
            }
        };
        t.start();
        t.join();
        assertTrue(success[0]);
    }

    @Test
    public void testCreationByRoot() throws Exception {
        Experimenter e = loginNewUser();
        loginRoot();

        ISession service = this.factory.getSessionService();
        Principal p = new Principal(e.getOmeName(), "user", "Test");
        Session s = service.createSessionWithTimeout(p, 10 * 1000L);

    }

    @Test(groups = "ticket:1229")
    public void testUpdateDefaultGroup() throws Exception {
        
        ISession s = this.factory.getSessionService();
        IAdmin a = this.factory.getAdminService();

        Experimenter e = loginNewUser();
        ExperimenterGroup g = new ExperimenterGroup(uuid(), false);
        g = new ExperimenterGroup(a.createGroup(g), false);
        
        loginRoot();
        a.addGroups(e, g);
        
        loginUser(e.getOmeName());
        String uuid = a.getEventContext().getCurrentSessionUuid();
        sessionManager.setSecurityContext(new Principal(uuid), g);
        
    }
    
    /**
     * This test original used ISession.updateSession() which is where the bug
     * was. With setSecurityContext() this shouldn't be the case, but leaving
     * test for the moment.
     */
    @Test(groups = "ticket:1385")
    public void testUpdateDefaultGroupTwice() throws Exception {
        
        ISession s = this.factory.getSessionService();
        IAdmin a = this.factory.getAdminService();

        Experimenter e = loginNewUser();
        ExperimenterGroup g1 = new ExperimenterGroup(uuid(), false);
        g1 = new ExperimenterGroup(a.createGroup(g1), false);
        ExperimenterGroup g2 = new ExperimenterGroup(uuid(), false);
        g2 = new ExperimenterGroup(a.createGroup(g2), false);
        
        loginRoot();
        a.addGroups(e, g1, g2);
        
        loginUser(e.getOmeName());
        String uuid = a.getEventContext().getCurrentSessionUuid();
        Principal principal = new Principal(uuid);

        sessionManager.setSecurityContext(principal, g1);
        sessionManager.setSecurityContext(principal, g2);
        
        // But now if we try to get the session again, boom.
        s.getSession(uuid);
    }

    @Test(groups = "session-uuid")
    public void testQuerySession() throws Exception {

        final IAdmin a = this.factory.getAdminService();
        final IQuery q = this.factory.getQueryService();
        loginNewUser();
        final String uuid = a.getEventContext().getCurrentSessionUuid();

        loginNewUser();
        final List<Object[]> rv = q.projection(
                "select uuid from Session order by id desc", new Parameters().page(0, 100));

        final Set<String> uuids = new HashSet<String>();
        for (Object[] item : rv) {
            uuids.add(item[0].toString());
        }
        assertFalse(uuids.contains(uuid));
    }

    @Test(groups = "session-uuid")
    public void testSessionContext() throws Exception {
        final ISession s = this.factory.getSessionService();
        loginNewUser();
        assertNull(s.getMyOpenSessions().get(0).getDetails().contextAt(0));
    }
}

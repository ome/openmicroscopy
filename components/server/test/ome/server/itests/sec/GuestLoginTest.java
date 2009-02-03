/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import ome.annotations.PermitAll;
import ome.annotations.RolesAllowed;
import ome.api.ISession;
import ome.conditions.SecurityViolation;
import ome.model.meta.Experimenter;
import ome.model.meta.Session;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = { "integration", "security" })
public class GuestLoginTest extends AbstractManagedContextTest {

    Principal guest = new Principal("guest", "guest", "guest");
    Principal p;
    ISession srv;
    Session s;
    Executor ex;

    @BeforeMethod
    public void setup() {
        ex = (Executor) this.applicationContext.getBean("executor");
    }

    public void testGuestUserCreatesSession() throws Exception {
        srv = this.factory.getSessionService();
        s = srv.createSession(guest, "guest");
        p = new Principal(s.getUuid(), "guest", "guest");
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testGuestThenTriesToDoSomethingDisallowed() throws Exception {
        testGuestUserCreatesSession();
        ex.execute(p, new Executor.SimpleWork(this, "do something disallowed") {
            @RolesAllowed("user")
            @Transactional(readOnly = true)
            public Object doWork(org.hibernate.Session session,
                    ServiceFactory sf) {
                return sf.getQueryService().get(Experimenter.class, 0);
            }
        });
    }

    public void testGuestThenTriesToDoSomethingAllowed() throws Exception {
        testGuestUserCreatesSession();
        ex.execute(p, new Executor.SimpleWork(this, "test guest then tries") {
            @RolesAllowed("guest")
            public Object doWork(org.hibernate.Session session,
                    ServiceFactory sf) {
                return sf.getQueryService().get(Experimenter.class, 0);
            }
        });
    }

    public void testGuestThenTriesToDoSomethingVeryAllowed() throws Exception {
        testGuestUserCreatesSession();
        ex.execute(p,
                new Executor.SimpleWork(this, "do something very allowed") {
                    @PermitAll
                    public Object doWork(org.hibernate.Session session,
                            ServiceFactory sf) {
                        return sf.getQueryService().get(Experimenter.class, 0);
                    }
                });
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testButGuestCantMakeAdminCalls() throws Exception {
        testGuestUserCreatesSession();
        ex.execute(p, new Executor.SimpleWork(this, "cant make admin calls") {
            @RolesAllowed("system")
            public Object doWork(org.hibernate.Session session,
                    ServiceFactory sf) {
                return sf.getQueryService().get(Experimenter.class, 0);
            }
        });
    }
}

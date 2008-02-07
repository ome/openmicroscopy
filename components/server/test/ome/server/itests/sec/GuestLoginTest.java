/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;

import ome.api.ISession;
import ome.conditions.SecurityViolation;
import ome.model.meta.Experimenter;
import ome.model.meta.Session;
import ome.server.itests.AbstractManagedContextTest;
import ome.server.itests.Wrap;
import ome.system.Principal;

import org.testng.annotations.Test;

@Test(groups = { "integration", "security" })
public class GuestLoginTest extends AbstractManagedContextTest {

    Principal guest = new Principal("guest", "guest", "guest");
    Principal p;
    ISession srv;
    Session s;

    public void testGuestUserCreatesSession() throws Exception {
        srv = this.factory.getSessionService();
        s = srv.createSession(guest, "guest");
        p = new Principal(s.getUuid(), "guest", "guest");
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testGuestThenTriesToDoSomethingDisallowed() throws Exception {
        testGuestUserCreatesSession();
        new Wrap(p, new Wrap.QueryBackdoor() {
            @RolesAllowed("user")
            public void run() {
                this.get(Experimenter.class, 0);
            }
        });
    }

    public void testGuestThenTriesToDoSomethingAllowed() throws Exception {
        testGuestUserCreatesSession();
        new Wrap(p, new Wrap.QueryBackdoor() {
            @RolesAllowed("guest")
            public void run() {
                this.get(Experimenter.class, 0);
            }
        });
    }

    public void testGuestThenTriesToDoSomethingVeryAllowed() throws Exception {
        testGuestUserCreatesSession();
        new Wrap(p, new Wrap.QueryBackdoor() {
            @PermitAll
            public void run() {
                this.get(Experimenter.class, 0);
            }
        });
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testButGuestCantMakeAdminCalls() throws Exception {
        testGuestUserCreatesSession();
        new Wrap(p, new Wrap.QueryBackdoor() {
            @RolesAllowed("system")
            public void run() {
                this.get(Experimenter.class, 0);
            }
        });
    }
}

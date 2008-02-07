/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.sec;

import ome.api.ISession;
import ome.model.meta.Experimenter;
import ome.model.meta.Session;
import ome.server.itests.AbstractManagedContextTest;
import ome.system.EventContext;
import ome.system.Principal;

import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class SessionTest extends AbstractManagedContextTest {

    @Test
    public void testSimpleCreate() throws Exception {

        loginRoot();
        Experimenter e = loginNewUser();
        this.securitySystem.logout();

        ISession service = this.factory.getServiceByClass(ISession.class);
        Session s = service.createSession(new Principal(e.getOmeName(), "user",
                "Test"), "ome");

        // This is what then gets passed to the
        Principal p = new Principal(s.getUuid(), "user", "Test");
        this.securitySystem.login(p);

        // Now we should be able to do something.
        EventContext ec = this.iAdmin.getEventContext();
        assertEquals(ec.getCurrentUserName(), e.getOmeName());

        service.closeSession(s);
    }

    @Test
    public void testCreationByRoot() throws Exception {
        Experimenter e = loginNewUser();
        loginRoot();

        ISession service = this.factory.getSessionService();
        Principal p = new Principal(e.getOmeName(), "user", "Test");
        Session s = service.createSessionWithTimeout(p, 10L);

    }

}

/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

import java.util.UUID;

import junit.framework.TestCase;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.core.Image;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.security.SecuritySystem;
import ome.services.sessions.SessionManager;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.testng.annotations.AfterClass;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

@Test(groups = { "mockable", "security", "integration" })
public class LoginTest extends TestCase {

    protected void login(String user, String group, String eventType) {
        Session s = sm.create(new Principal(user, group, eventType));
        sec.login(new Principal(s.getUuid(), group, eventType));
    }

    protected OmeroContext ctx;

    protected ServiceFactory sf;

    protected IQuery q;

    protected IUpdate u;

    protected SecuritySystem sec;

    protected ome.services.sessions.SessionManager sm;

    @Configuration(beforeTestClass = true)
    public void config() {
        ctx = OmeroContext.getManagedServerContext();
        sf = new ServiceFactory(ctx);
        q = sf.getQueryService();
        u = sf.getUpdateService();
        sec = (SecuritySystem) ctx.getBean("securitySystem");
        sm = (SessionManager) ctx.getBean("sessionManager");
        while (sec.logout() > 0) {
            ;
        }

    }

    @AfterClass
    public void cleanup() {
        while (sec.logout() > 0) {
            // keep going;
        }
    }

    @Test
    public void testNoLoginThrowsException() throws Exception {
        try {
            q.find(Experimenter.class, 0l);
            fail("Non-logged-in call allowed!");
        } catch (RuntimeException e) {
            // ok.
        }
    }

    @Test
    public void testLoggedInAllowed() throws Exception {
        login("root", "system", "Test");
        q.find(Experimenter.class, 0l);
    }

    @Test
    public void testLoggedOutAfterCall() throws Exception {
        login("root", "system", "Test");
        q.find(Experimenter.class, 0l);
        assertTrue(!sec.isReady());
    }

    @Test
    public void testLoginWithInvalidThrowsException() throws Exception {
        try {
            login("unknown2349akljf9q283", "system", "Test");
            q.find(Experimenter.class, 0l);
            fail("Login allowed with unknown user.");
        } catch (RuntimeException r) {
        }
        // TODO Otherexception

        try {
            login("root", "baba9o38023984019", "Test");
            q.find(Experimenter.class, 0l);
            fail("Login allowed with unknown group.");
        } catch (RuntimeException r) {
        }
        // TODO Otherexception

        try {
            login("root", "system", "blarg23498239048230");
            q.find(Experimenter.class, 0l);
            fail("Login allowed with unknown type.");
        } catch (RuntimeException r) {
        }
        // TODO Otherexception

    }

    @Test(groups = "ticket:666")
    public void testLoginToNonMemberGroup() throws Exception {

        login("root", "system", "Test");

        String gname = UUID.randomUUID().toString();
        ExperimenterGroup g = new ExperimenterGroup();
        g.setName(gname);
        sf.getAdminService().createGroup(g);

        String uname = UUID.randomUUID().toString();
        Experimenter e = new Experimenter();
        e.setOmeName(uname);
        e.setFirstName("badgroup");
        e.setLastName("login");
        sf.getAdminService().createUser(e, "default");

        try {
            login(uname, gname, "Test");
            q.find(Experimenter.class, 0l);
            Image i = new Image();
            i.setName("belongs to wrong group");
            i = sf.getUpdateService().saveAndReturnObject(i);
            fail("Login allowed for user in non-member group.");
        } catch (RuntimeException r) {
        }
    }

}

/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

import java.util.UUID;

import org.testng.annotations.*;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.model.core.Image;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.SecuritySystem;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;

import junit.framework.TestCase;

@Test(groups = { "mockable", "security", "integration" })
public class LoginTest extends TestCase {

    protected void login(String user, String group, String eventType) {
        sec.login(new Principal(user, group, eventType));
    }

    protected OmeroContext ctx;

    protected ServiceFactory sf;

    protected IQuery q;

    protected IUpdate u;

    protected SecuritySystem sec;

    @Configuration(beforeTestClass = true)
    public void config() {
        ctx = OmeroContext.getManagedServerContext();
        sf = new ServiceFactory(ctx);
        q = sf.getQueryService();
        u = sf.getUpdateService();
        sec = (SecuritySystem) ctx.getBean("securitySystem");
    }

    @Test
    public void testNoLoginThrowsException() throws Exception {
        sec.logout();
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
        assertTrue(sec.isEmptyEventContext());
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

        login("root","system","Test");

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

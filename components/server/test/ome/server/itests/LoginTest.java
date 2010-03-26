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
import ome.security.basic.PrincipalHolder;
import ome.services.sessions.SessionManager;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

@Test(groups = { "security", "integration" })
public class LoginTest extends AbstractManagedContextTest {

    protected SecuritySystem sec;

    protected PrincipalHolder ph;

    @BeforeMethod
    public void config() {
        sec = (SecuritySystem) applicationContext.getBean("securitySystem");
        ph = (PrincipalHolder) applicationContext.getBean("principalHolder");
        while (ph.size() > 0) {
            ph.logout();
        }

    }

    @AfterClass
    public void cleanup() {
        while (ph.size() > 0) {
            ph.logout();
        }
    }

    @Test
    public void testNoLoginThrowsException() throws Exception {
        try {
            iQuery.find(Experimenter.class, 0l);
            fail("Non-logged-in call allowed!");
        } catch (RuntimeException e) {
            // ok.
        }
    }

    @Test
    public void testLoggedInAllowed() throws Exception {
        login("root", "system", "Test");
        iQuery.find(Experimenter.class, 0l);
    }

    @Test
    public void testLoggedOutAfterCall() throws Exception {
        login("root", "system", "Test");
        iQuery.find(Experimenter.class, 0l);
        assertTrue(!sec.isReady());
    }

    @Test(enabled = false)
    public void testLoginWithInvalidThrowsException() throws Exception {
        try {
            login("unknown2349akljf9q283", "system", "Test");
            iQuery.find(Experimenter.class, 0l);
            fail("Login allowed with unknown user.");
        } catch (RuntimeException r) {
        }
        // TODO Otherexception

        try {
            login("root", "baba9o38023984019", "Test");
            iQuery.find(Experimenter.class, 0l);
            fail("Login allowed with unknown group.");
        } catch (RuntimeException r) {
        }
        // TODO Otherexception

        try {
            login("root", "system", "blarg23498239048230");
            iQuery.find(Experimenter.class, 0l);
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
        iAdmin.createGroup(g);

        Experimenter e = loginNewUser();

        try {
            login(e.getOmeName(), gname, "Test");
            iQuery.find(Experimenter.class, 0l);
            Image i = new Image();
            i.setName("belongs to wrong group");
            i = iUpdate.saveAndReturnObject(i);
            fail("Login allowed for user in non-member group.");
        } catch (RuntimeException r) {
        }
    }

}

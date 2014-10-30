/*
 *   $Id$
 *
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

import java.util.NoSuchElementException;
import java.util.UUID;

import ome.model.core.Image;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.SecuritySystem;
import ome.security.basic.PrincipalHolder;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
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
            cleanup();
            loginAop.p = null;
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
        try {
            assertTrue(!sec.isReady());
        } catch (NoSuchElementException nsee) {
            // ok. that's our current meaning of "logged out"
        }
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
        g.setLdap(false);
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

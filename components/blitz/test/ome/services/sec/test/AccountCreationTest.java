/*
 *   $Id: AccountCreationTest.java 1898 2007-10-29 15:20:40Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sec.test;

import static omero.rtypes.*;

import java.util.UUID;

import omero.ServerError;
import omero.model.Experimenter;
import omero.model.ExperimenterI;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.api.ServiceFactoryPrx;

import org.testng.annotations.Test;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

@Test(enabled=false, groups = { "broken", "client", "integration", "security", "ticket:181",
        "ticket:199", "password" })
public class AccountCreationTest extends AbstractAccountTest {

    @Test(enabled=false)
    public void testSudoCreatesAccountThroughIUpdate() throws Exception {
        Experimenter e = createNewUser(getSudoUpdate("ome"));

        // passwords are no longer null by default
        removePasswordEntry(e);
        assertNull(getPasswordFromDb(e));

        assertCannotLogin(e.getUserName().getValue(), "ome");
        assertCannotLogin(e.getUserName().getValue(), "");

        doesNotHaveSystemPrivileges(e);

        getSudoAdmin("ome").changeUserPassword(e.getUserName().getValue(), rstring("test"));
        assertCanLogin(e.getUserName().getValue(), "test");
    }

    @Test(enabled=false)
    public void testSudoCreatesUserAccountThroughIAdmin() throws Exception {
        ExperimenterGroup g = new ExperimenterGroupI();
        g.setName(rstring(UUID.randomUUID().toString()));
        getSudoAdmin("ome").createGroup(g);
        Experimenter e = new ExperimenterI();
        e.setUserName(rstring(UUID.randomUUID().toString()));
        e.setFirstName(rstring("ticket:181"));
        e.setLastName(rstring("ticket:199"));
        e = getSudoAdmin("ome").getExperimenter(
                getSudoAdmin("ome").createUser(e, g.getName().getValue()));
        assertCanLogin(e.getUserName().getValue(), "");
        assertCanLogin(e.getUserName().getValue(), "ome");
        assertCanLogin(e.getUserName().getValue(), "bob");

        doesNotHaveSystemPrivileges(e);
    }

    @Test(enabled=false)
    public void testSudoCreatesSystemAccountThroughIAdmin() throws Exception {
        Experimenter e = new ExperimenterI();
        e.setUserName(rstring(UUID.randomUUID().toString()));
        e.setFirstName(rstring("ticket:181"));
        e.setLastName(rstring("ticket:199"));
        e = getSudoAdmin("ome").getExperimenter(
                getSudoAdmin("ome").createSystemUser(e));
        assertCanLogin(e.getUserName().getValue(), "");
        assertCanLogin(e.getUserName().getValue(), "ome");
        assertCanLogin(e.getUserName().getValue(), "bob");

        hasSystemPrivileges(e);

        getSudoAdmin("ome").changeUserPassword(e.getUserName().getValue(), rstring("bob"));

        assertCannotLogin(e.getUserName().getValue(), "");
        assertCannotLogin(e.getUserName().getValue(), "ome");
        assertCanLogin(e.getUserName().getValue(), "bob");

    }

    @Test(enabled=false)
    public void testSudoCreatesAccountThroughIAdmin() throws Exception {
        Experimenter e = new ExperimenterI();
        e.setUserName(rstring(UUID.randomUUID().toString()));
        e.setFirstName(rstring("ticket:181"));
        e.setLastName(rstring("ticket:199"));
        e = getSudoAdmin("ome").getExperimenter(
                getSudoAdmin("ome").createUser(e, "default"));
        assertCanLogin(e.getUserName().getValue(), "");
        assertCanLogin(e.getUserName().getValue(), "ome");
        assertCanLogin(e.getUserName().getValue(), "bob");

        doesNotHaveSystemPrivileges(e);

        getSudoAdmin("ome").changeUserPassword(e.getUserName().getValue(), rstring("bob"));

        assertCannotLogin(e.getUserName().getValue(), "");
        assertCannotLogin(e.getUserName().getValue(), "ome");
        assertCanLogin(e.getUserName().getValue(), "bob");

    }

    @Test(enabled=false)
    public void testSudoSysCreatesAccountThroughIAdmin() throws Exception {
        Experimenter e = new ExperimenterI();
        e.setUserName(rstring(UUID.randomUUID().toString()));
        e.setFirstName(rstring("ticket:181"));
        e.setLastName(rstring("ticket:199"));
        e = getSudoAdmin("ome").getExperimenter(
                getSudoAdmin("ome").createSystemUser(e));
        assertCanLogin(e.getUserName().getValue(), "");
        assertCanLogin(e.getUserName().getValue(), "ome");
        assertCanLogin(e.getUserName().getValue(), "bob");

        hasSystemPrivileges(e);

        getSudoAdmin("ome").changeUserPassword(e.getUserName().getValue(), rstring("bob"));

        assertCannotLogin(e.getUserName().getValue(), "");
        assertCannotLogin(e.getUserName().getValue(), "ome");
        assertCanLogin(e.getUserName().getValue(), "bob");

    }

    // ~ Helpers
    // =========================================================================

    private void hasSystemPrivileges(Experimenter e) {
        try
        {
            ServiceFactoryPrx sf = c.createSession(e.getUserName().getValue(), "");
            sf.getAdminService().synchronizeLoginCache();
        } catch (ServerError e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (CannotCreateSessionException e2)
        {
            // TODO Auto-generated catch block
            e2.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        } catch (PermissionDeniedException e3)
        {
            // TODO Auto-generated catch block
            e3.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }

    private void doesNotHaveSystemPrivileges(Experimenter e) {
        try {
            hasSystemPrivileges(e);
            fail("Should be security violation");
        } catch (Exception ex) {
            // ok.
        }
    }

}

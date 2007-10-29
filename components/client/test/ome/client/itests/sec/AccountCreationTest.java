/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client.itests.sec;

import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.system.Login;
import ome.system.ServiceFactory;

import org.jboss.util.id.GUID;
import org.testng.annotations.Test;

@Test(groups = { "client", "integration", "security", "ticket:181",
        "ticket:199", "password" })
public class AccountCreationTest extends AbstractAccountTest {

    @Test
    public void testSudoCreatesAccountThroughIUpdate() throws Exception {
        Experimenter e = createNewUser(getSudoUpdate("ome"));

        // passwords are no longer null by default
        removePasswordEntry(e);
        assertNull(getPasswordFromDb(e));

        assertCannotLogin(e.getOmeName(), "ome");
        assertCannotLogin(e.getOmeName(), "");

        doesNotHaveSystemPrivileges(e);

        getSudoAdmin("ome").changeUserPassword(e.getOmeName(), "test");
        assertCanLogin(e.getOmeName(), "test");
    }

    @Test
    public void testSudoCreatesUserAccountThroughIAdmin() throws Exception {
        ExperimenterGroup g = new ExperimenterGroup();
        g.setName(GUID.asString());
        getSudoAdmin("ome").createGroup(g);
        Experimenter e = new Experimenter();
        e.setOmeName(GUID.asString());
        e.setFirstName("ticket:181");
        e.setLastName("ticket:199");
        e = getSudoAdmin("ome").getExperimenter(
                getSudoAdmin("ome").createUser(e, g.getName()));
        assertCanLogin(e.getOmeName(), "");
        assertCanLogin(e.getOmeName(), "ome");
        assertCanLogin(e.getOmeName(), "bob");

        doesNotHaveSystemPrivileges(e);
    }

    @Test
    public void testSudoCreatesSystemAccountThroughIAdmin() throws Exception {
        Experimenter e = new Experimenter();
        e.setOmeName(new GUID().asString());
        e.setFirstName("ticket:181");
        e.setLastName("ticket:199");
        e = getSudoAdmin("ome").getExperimenter(
                getSudoAdmin("ome").createSystemUser(e));
        assertCanLogin(e.getOmeName(), "");
        assertCanLogin(e.getOmeName(), "ome");
        assertCanLogin(e.getOmeName(), "bob");

        hasSystemPrivileges(e);

        getSudoAdmin("ome").changeUserPassword(e.getOmeName(), "bob");

        assertCannotLogin(e.getOmeName(), "");
        assertCannotLogin(e.getOmeName(), "ome");
        assertCanLogin(e.getOmeName(), "bob");

    }

    @Test
    public void testSudoCreatesAccountThroughIAdmin() throws Exception {
        Experimenter e = new Experimenter();
        e.setOmeName(new GUID().asString());
        e.setFirstName("ticket:181");
        e.setLastName("ticket:199");
        e = getSudoAdmin("ome").getExperimenter(
                getSudoAdmin("ome").createUser(e, "default"));
        assertCanLogin(e.getOmeName(), "");
        assertCanLogin(e.getOmeName(), "ome");
        assertCanLogin(e.getOmeName(), "bob");

        doesNotHaveSystemPrivileges(e);

        getSudoAdmin("ome").changeUserPassword(e.getOmeName(), "bob");

        assertCannotLogin(e.getOmeName(), "");
        assertCannotLogin(e.getOmeName(), "ome");
        assertCanLogin(e.getOmeName(), "bob");

    }

    @Test
    public void testSudoSysCreatesAccountThroughIAdmin() throws Exception {
        Experimenter e = new Experimenter();
        e.setOmeName(new GUID().asString());
        e.setFirstName("ticket:181");
        e.setLastName("ticket:199");
        e = getSudoAdmin("ome").getExperimenter(
                getSudoAdmin("ome").createSystemUser(e));
        assertCanLogin(e.getOmeName(), "");
        assertCanLogin(e.getOmeName(), "ome");
        assertCanLogin(e.getOmeName(), "bob");

        hasSystemPrivileges(e);

        getSudoAdmin("ome").changeUserPassword(e.getOmeName(), "bob");

        assertCannotLogin(e.getOmeName(), "");
        assertCannotLogin(e.getOmeName(), "ome");
        assertCanLogin(e.getOmeName(), "bob");

    }

    // ~ Helpers
    // =========================================================================

    private void hasSystemPrivileges(Experimenter e) {
        new ServiceFactory(new Login(e.getOmeName(), "")).getAdminService()
                .synchronizeLoginCache();
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

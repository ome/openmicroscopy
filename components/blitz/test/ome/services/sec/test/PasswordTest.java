/*
 *   $Id: PasswordTest.java 2147 2008-02-07 11:21:51Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sec.test;

import ome.conditions.SecurityViolation;
import ome.system.Login;
import ome.system.ServiceFactory;

import static omero.rtypes.*;
import omero.model.Experimenter;
import omero.api.IAdminPrx;

import org.testng.annotations.Test;

@Test(enabled=false, groups = { "broken", "client", "integration", "security", "ticket:181",
        "ticket:199", "password" })
public class PasswordTest extends AbstractAccountTest {

    // design:
    // 1. who : sudo or user (doing sudo because playing with root is a pain)
    // 2. state : password filled, empty, missing
    // 3. action : change own, change other

    // ~ SUDO WITH FILLED PASSWORD
    // =========================================================================

    @Test(enabled=false)
    public void testSudoCanChangePassword() throws Exception {
        try {
            IAdminPrx sudoAdmin = getSudoAdmin("ome");
            sudoAdmin.changePassword(rstring("testing..."));
            assertCanLogin(sudo_name, "testing...");
            try {
                sudoAdmin.synchronizeLoginCache();
                // TODO original still works
                // fail("Old services should be unusable.");
            } catch (Exception ex) {
                // ok
            }
            assertCannotLogin(sudo_name, "ome");
        } finally {
            // return to normal.
            getSudoAdmin("testing...").changePassword(rstring("ome"));
        }
    }

    @Test(enabled=false)
    public void testSudoCanChangeOthersPassword() throws Exception {

        omero.model.Experimenter e = createNewUser(rootAdmin);
        resetPasswordTo_ome(e);
        assertCanLogin(e.getUserName().getValue(), "ome");

        getSudoAdmin("ome").changeUserPassword(e.getUserName().getValue(), rstring("foo"));
        assertCanLogin(e.getUserName().getValue(), "foo");
        assertCannotLogin(e.getUserName().getValue(), "bar");
        assertCannotLogin(e.getUserName().getValue(), "");

        getSudoAdmin("ome").changeUserPassword(e.getUserName().getValue(), rstring(""));
        assertCanLogin(e.getUserName().getValue(), "");
        assertCanLogin(e.getUserName().getValue(), "NOTCORRECT");

    }

    // ~ USER WITH FILLED PASSWORD
    // =========================================================================

    @Test(enabled=false)
    public void testUserCanChangeOwnPassword() throws Exception {
        Experimenter e = createNewUser(rootAdmin);
        resetPasswordTo_ome(e);
        assertCanLogin(e.getUserName().getValue(), "ome");

        ServiceFactory userServices = new ServiceFactory(
                new Login(e.getUserName().getValue(), "ome"));
        userServices.getAdminService().changePassword("test");
        assertCanLogin(e.getUserName().getValue(), "test");
        assertCannotLogin(e.getUserName().getValue(), "ome");

    }

    @Test(enabled=false, expectedExceptions = SecurityViolation.class)
    public void testUserCantChangeOthersPassword() throws Exception {
        Experimenter e = createNewUser(getSudoAdmin("ome"));
        resetPasswordTo_ome(e);
        assertCanLogin(e.getUserName().getValue(), "ome");

        Experimenter target = createNewUser(getSudoAdmin("ome"));
        resetPasswordTo_ome(target);
        assertCanLogin(target.getUserName().getValue(), "ome");

        ServiceFactory userServices = new ServiceFactory(
                new Login(e.getUserName().getValue(), "ome"));
        userServices.getAdminService().changeUserPassword(
                target.getUserName().getValue(),"test");

    }

    // ~ EMPTY PASSWORD
    // =========================================================================

    @Test(enabled=false)
    public void testAnyOneCanLoginWithEmptyPassword() throws Exception {

        Experimenter e = createNewUser(rootAdmin);
        setPasswordtoEmptyString(e);
        assertCanLogin(e.getUserName().getValue(), "bob");
        assertCanLogin(e.getUserName().getValue(), "");
        assertCanLogin(e.getUserName().getValue(), "ome");

        new ServiceFactory(new Login(e.getUserName().getValue(), "blah")).getAdminService()
                .changePassword("ome");

        assertCannotLogin(e.getUserName().getValue(), "bob");
        assertCannotLogin(e.getUserName().getValue(), "");
        assertCanLogin(e.getUserName().getValue(), "ome");

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        setPasswordtoEmptyString(sudo);
        assertCanLogin(sudo_name, "bob");
        assertCanLogin(sudo_name, "");
        assertCanLogin(sudo_name, "ome");

        getSudoAdmin("blah").changePassword(rstring("ome"));

        assertCannotLogin(sudo_name, "bob");
        assertCannotLogin(sudo_name, "");
        assertCanLogin(sudo_name, "ome");

    }

    // ~ MISSING PASSWORD (Locked account)
    // =========================================================================

    @Test(enabled=false)
    public void testNoOneCanLoginWithMissingPassword() throws Exception {

        Experimenter e = createNewUser(rootAdmin);
        removePasswordEntry(e);

        assertCannotLogin(e.getUserName().getValue(), "bob");
        assertCannotLogin(e.getUserName().getValue(), "");
        assertCannotLogin(e.getUserName().getValue(), "ome");

        resetPasswordTo_ome(e);

        assertCannotLogin(e.getUserName().getValue(), "bob");
        assertCannotLogin(e.getUserName().getValue(), "");
        assertCanLogin(e.getUserName().getValue(), "ome");

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        removePasswordEntry(sudo);

        assertCannotLogin(sudo_name, "bob");
        assertCannotLogin(sudo_name, "");
        assertCannotLogin(sudo_name, "ome");

        resetPasswordTo_ome(sudo);

        assertCannotLogin(sudo_name, "bob");
        assertCannotLogin(sudo_name, "");
        assertCanLogin(sudo_name, "ome");

    }

    @Test(enabled=false)
    public void testNoOneCanLoginWithNullPassword() throws Exception {

        Experimenter e = createNewUser(rootAdmin);
        nullPasswordEntry(e);

        assertCannotLogin(e.getUserName().getValue(), "bob");
        assertCannotLogin(e.getUserName().getValue(), "");
        assertCannotLogin(e.getUserName().getValue(), "ome");

        resetPasswordTo_ome(e);

        assertCannotLogin(e.getUserName().getValue(), "bob");
        assertCannotLogin(e.getUserName().getValue(), "");
        assertCanLogin(e.getUserName().getValue(), "ome");

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        nullPasswordEntry(sudo);

        assertCannotLogin(sudo_name, "bob");
        assertCannotLogin(sudo_name, "");
        assertCannotLogin(sudo_name, "ome");

        resetPasswordTo_ome(sudo);

        assertCannotLogin(sudo_name, "bob");
        assertCannotLogin(sudo_name, "");
        assertCanLogin(sudo_name, "ome");

    }

    @Test(enabled=false, groups = "special")
    public void testSpecialCaseOfSudosOldPassword() throws Exception {
        resetPasswordTo_ome(sudo);
        assertTrue(OME_HASH.equals(getPasswordFromDb(sudo)));

        assertCanLogin(sudo_name, "ome");
        assertCannotLogin(sudo_name, "bob");
        assertCannotLogin(sudo_name, "");

        assertTrue(OME_HASH.equals(getPasswordFromDb(sudo)));

        removePasswordEntry(sudo);
        assertNull(getPasswordFromDb(sudo));

        assertCannotLogin(sudo_name, "");
        assertCannotLogin(sudo_name, "bob");

        assertNull(getPasswordFromDb(sudo));

        assertCannotLogin(sudo_name, "ome");

        assertNull(getPasswordFromDb(sudo));

    }

}

/*
 *   $Id: ChmodClientTest.java 1167 2006-12-15 10:39:34Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sec.test;

import org.testng.annotations.*;

import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.system.Login;
import ome.system.ServiceFactory;

@Test(enabled=false, groups = { "broken", "client", "integration", "security", "ticket:365", "chmod" })
public class ChmodClientTest extends AbstractChangeDetailClientTest {

    // TODO : This series of tests (AbstractChangeDetailClientTest)
    // should be unified with AbstractPermissionsTest (both setup
    // UserOtherWorldPiRoot etc.)

    // design parameters:
    // 1. various permissions (belonging to root, user, or other)
    // 2. as user or root
    // 3. changing to various permissions

    @Test(enabled=false)
    public void test_user_RWRW_user_PUBLIC() throws Exception {
        newUserImagePermissionsAsUserToPermissions(true, asUser,
                Permissions.GROUP_PRIVATE, asUser, Permissions.PUBLIC);
    }

    @Test(enabled=false)
    public void test_user_RWRW_other_PUBLIC() throws Exception {
        newUserImagePermissionsAsUserToPermissions(false, asUser,
                Permissions.GROUP_PRIVATE, asOther, Permissions.PUBLIC);
    }

    @Test(enabled=false)
    public void test_user_RWRW_world_PUBLIC() throws Exception {
        newUserImagePermissionsAsUserToPermissions(false, asUser,
                Permissions.GROUP_PRIVATE, asWorld, Permissions.PUBLIC);
    }

    @Test(enabled=false)
    public void test_user_RWRW_pi_PUBLIC() throws Exception {
        newUserImagePermissionsAsUserToPermissions(true, asUser,
                Permissions.GROUP_PRIVATE, asPI, Permissions.PUBLIC);
    }

    @Test(enabled=false)
    public void test_user_RWRW_root_PUBLIC() throws Exception {
        newUserImagePermissionsAsUserToPermissions(true, asUser,
                Permissions.GROUP_PRIVATE, asRoot, Permissions.PUBLIC);
    }

    @Test(enabled=false, groups = { "ticket:397", "broken" })
    public void testCheckInitialParameters() throws Exception {
        fail("USER CAN CURRENTLY JUST PASS IN WHATEVER OWNER THEY WANT.");
        // UNTAINT
    }

    // ~ Helpers
    // =========================================================================

    protected void newUserImagePermissionsAsUserToPermissions(boolean ok,
            Login owner, Permissions orig, Login changer, Permissions target)
            throws ValidationException {
        ServiceFactory factory = new ServiceFactory(owner);
        ServiceFactory factory2 = new ServiceFactory(changer);

        Image i;

        // via IAdmin
        try {
            i = new Image();
            i.getDetails().setPermissions(orig);
            i.setName("test");
            i = factory.getUpdateService().saveAndReturnObject(i);

            factory2.getAdminService().changePermissions(i, target);
            if (!ok) {
                fail("secvio!");
            }
        } catch (SecurityViolation sv) {
            if (ok) {
                throw sv;
            }
        }

        // via Details
        try {
            i = new Image();
            i.getDetails().setPermissions(orig);
            i.setName("test");
            i = factory.getUpdateService().saveAndReturnObject(i);

            i.getDetails().setPermissions(target);
            factory2.getUpdateService().saveObject(i);
            if (!ok) {
                fail("secvio!");
            }
        } catch (SecurityViolation sv) {
            if (ok) {
                throw sv;
            }
        }
    }

}

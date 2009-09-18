/*
 *   $Id: UmaskSecurityTest.java 1167 2006-12-15 10:39:34Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sec.test;

import omero.api.ServiceFactoryPrx;
import omero.model.IObject;
import omero.model.Permissions;
import omero.model.PermissionsI;

import org.testng.annotations.Test;

@Test(groups = { "ticket:TODO", "security", "integration" })
public class UmaskSecurityTest extends AbstractPermissionsTest {

    @Override
    public void testSingleProject_R() throws Exception  {
        setSFPermissionsMask(r, new PermissionsI(RW_xx_xx.toString()));
        createProject(r, null, system_group);
        assertUmaskWorks(r, prj, new PermissionsI(RW_xx_xx.toString()));
    }

    @Override
    public void testSingleProject_U() throws Exception {
        setSFPermissionsMask(u, new PermissionsI(RW_xx_xx.toString()));
        createProject(u, null, user_other_group);
        assertUmaskWorks(u, prj, new PermissionsI(RW_xx_xx.toString()));
    }

    @Override
    public void testSingleProject_W() throws Exception {
        setSFPermissionsMask(w, new PermissionsI(RW_xx_xx.toString()));
        createProject(w, null, common_group);
        assertUmaskWorks(w, prj, new PermissionsI(RW_xx_xx.toString()));
    }

    @Override
    public void test_O_Pixels_And_U_Thumbnails() throws Exception {
    }

    @Override
    public void test_U_Image_U_Pixels() throws Exception {
    }

    @Override
    public void test_U_Instrument_And_U_Microscope() throws Exception {
    }

    @Override
    public void test_U_Pixels_And_O_Thumbnails() throws Exception {
    }

    @Override
    public void test_U_Pixels_And_R_Thumbnails() throws Exception {
    }

    @Override
    public void test_U_Pixels_And_U_Thumbnails() throws Exception {
    }

    @Override
    public void test_U_Projects_U_Datasets_U_Link() throws Exception {
    }

    // ~ Helpers
    // =========================================================================

    private void assertUmaskWorks(ServiceFactoryPrx sf, IObject _i,
            Permissions perms) {
        setSFPermissionsMask(sf, perms);
        IObject t = sf.getQueryService().get(_i.getClass().getName(), _i.getId().getValue());
        assertTrue(t + "!=" + perms, t.getDetails().getPermissions()
                .sameRights(perms));
    }

}

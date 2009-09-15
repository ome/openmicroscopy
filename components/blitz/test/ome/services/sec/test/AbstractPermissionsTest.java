/*
 *   $Id: AbstractPermissionsTest.java 2003 2008-01-04 14:16:00Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sec.test;

import static ome.model.internal.Permissions.Right.READ;
import static ome.model.internal.Permissions.Right.WRITE;
import static ome.model.internal.Permissions.Role.GROUP;
import static ome.model.internal.Permissions.Role.USER;
import static ome.model.internal.Permissions.Role.WORLD;

import java.util.UUID;

import ome.model.IObject;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.Microscope;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.Thumbnail;
import ome.model.enums.MicroscopeType;
import ome.model.internal.Details;
import ome.system.Login;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;

import static omero.rtypes.*;
import omero.ServerError;
import omero.model.Experimenter;
import omero.model.ExperimenterI;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.PermissionsI;
import omero.model.Project;
import omero.model.ProjectI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.Permissions;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

/**
 * The subclasses of {@link AbstractPermissionsTest} define the proper working
 * and the completeness of the security system.
 * 
 */
@Test(groups = { "security", "integration" })
public abstract class AbstractPermissionsTest extends AbstractSecurityTest {

    /*
     * factors:
     * -------------------------------------------------------------------------
     * SEPARATE TESTS 1. graph type: single, one-many, many-one, many-many
     * (links), special* 2. ownership : top user then other, top other then
     * user, all user, all other // NOTE: groups and world too! WITHIN TEST 3.
     * different permissions PER-GRAPHTYPE method (single,oneToMany,...) 4.
     * which hibernate api (load/get/createQuery/createCriteria/lazy-loading TBD
     * 5. second level cache, etc.
     * 
     * template:
     * -------------------------------------------------------------------------
     * a. create graph b. set permissions c. verify ownerships d. run queries
     * and check returns e. goto b.
     */

    final static protected Permissions RW_RW_RW = new PermissionsI(),
            RW_RW_xx = new PermissionsI().revoke(WORLD, READ, WRITE),
            RW_xx_xx = new PermissionsI().revoke(WORLD, READ, WRITE).revoke(
                    GROUP, READ, WRITE), xx_xx_xx = new Permissions().revoke(
                    WORLD, READ, WRITE).revoke(GROUP, READ, WRITE).revoke(USER,
                    READ, WRITE), RW_RW_Rx = new Permissions().revoke(WORLD,
                    WRITE), RW_Rx_Rx = new Permissions().revoke(WORLD, WRITE)
                    .revoke(GROUP, WRITE), Rx_Rx_Rx = new Permissions().revoke(
                    WORLD, WRITE).revoke(GROUP, WRITE).revoke(USER, WRITE),
            Rx_Rx_xx = new Permissions().revoke(WORLD, READ, WRITE).revoke(
                    GROUP, WRITE).revoke(USER, WRITE),
            Rx_xx_xx = new Permissions().revoke(WORLD, READ, WRITE).revoke(
                    GROUP, READ, WRITE).revoke(USER, WRITE);

    protected ExperimenterGroup system_group = new ExperimenterGroupI(0L, false),
            common_group = new ExperimenterGroupI(),
            user_other_group = new ExperimenterGroupI();

    protected Experimenter root = new ExperimenterI(0L, false),
            pi = new ExperimenterI(), user = new ExperimenterI(),
            other = new ExperimenterI(), world = new ExperimenterI();

    protected String gname, cname;

    protected ServiceFactory u, o, w, p, r;

    protected Project prj;

    protected Dataset ds;

    protected ProjectDatasetLink link;

    protected Pixels pix;

    protected Thumbnail tb;

    protected Image img;

    protected Microscope micro;

    protected Instrument instr;

    protected ServiceFactory ownsfA, ownsfB, ownsfC;

    protected Permissions permsA, permsB, permsC;

    protected Experimenter ownerA, ownerB, ownerC;

    protected ExperimenterGroup groupA, groupB, groupC;

    @Configuration(beforeTestClass = true)
    public void createUsersAndGroups() throws Exception {

        init();

        cname = UUID.randomUUID().toString();
        gname = UUID.randomUUID().toString();

        // shortcut for root service factory, created in super class
        r = rootServices;

        // create the common group
        common_group.setName(rstring(cname));
        common_group = new ExperimenterGroupI(rootAdmin
                .createGroup(common_group), false);

        // TODO -- this should be a task
        // create the new group
        user_other_group.setName(rstring(gname));
        user_other_group = new ExperimenterGroupI(rootAdmin
                .createGroup(user_other_group), false);

        // create the PI for the new group
        Login piLogin = new Login(UUID.randomUUID().toString(), "empty", gname,
                "Test");
        p = new ServiceFactory(piLogin);
        pi.setOmeName(rstring(piLogin.getName()));
        pi.setFirstName(rstring("read"));
        pi.setLastName(rstring("security -- leader of user_other_group"));
        pi = new ExperimenterI(rootAdmin.createUser(pi, gname), false);
        rootAdmin.addGroups(pi, common_group);

        // make the PI the group leader.
        rootAdmin.setGroupOwner(user_other_group, pi);
        // ENDTODO

        // create a new user in that group
        Login userLogin = new Login(UUID.randomUUID().toString(), "empty",
                gname, "Test");
        u = new ServiceFactory(userLogin);
        user.setOmeName(rstring(userLogin.getName()));
        user.setFirstName(rstring("read"));
        user.setLastName(rstring("security"));
        user = new Experimenter(rootAdmin.createUser(user, gname), false);
        rootAdmin.addGroups(user, user_other_group, common_group);

        // create another user in that group
        Login otherLogin = new Login(UUID.randomUUID().toString(), "empty",
                gname, "Test");
        o = new ServiceFactory(otherLogin);
        other.setOmeName(rstring(otherLogin.getName()));
        other.setFirstName(rstring("read"));
        other.setLastName(rstring("security2"));
        other = new Experimenter(rootAdmin.createUser(other, gname), false);
        rootAdmin.addGroups(other, user_other_group, common_group);

        // create a third regular user not in that group
        Login worldLogin = new Login(UUID.randomUUID().toString(), "empty" /*
         * not
         * gname!
         */);
        w = new ServiceFactory(worldLogin);
        world.setOmeName(rstring(worldLogin.getName()));
        world.setFirstName(rstring("read"));
        world.setLastName(rstring("Security -- not in their group"));
        world = new Experimenter(rootAdmin.createUser(world, cname), false);
        // not in same group

    }

    // ~ Tests
    // =========================================================================
    // single
    public abstract void testSingleProject_U() throws Exception;

    public abstract void testSingleProject_W() throws Exception;

    public abstract void testSingleProject_R() throws Exception;

    // bidirectional one-to-many
    public abstract void test_U_Pixels_And_U_Thumbnails() throws Exception;

    public abstract void test_O_Pixels_And_U_Thumbnails() throws Exception;

    public abstract void test_U_Pixels_And_O_Thumbnails() throws Exception;

    public abstract void test_U_Pixels_And_R_Thumbnails() throws Exception;

    // unidirectional many-to-one
    public abstract void test_U_Instrument_And_U_Microscope() throws Exception;

    // many-to-many with a mapping table
    public abstract void test_U_Projects_U_Datasets_U_Link() throws Exception;

    // special
    public abstract void test_U_Image_U_Pixels() throws Exception;

    // ~ Helpers
    // ========================================================================

    protected void verifyDetails(IObject _i, Experimenter _user,
            ExperimenterGroup _group, Permissions _perms) {

        IObject v;
        try
        {
            v = (IObject) rootQuery.get(_i.getClass().toString(), _i.getId());
            Details d = v.getDetails();
            assertEquals(d.getOwner().getId(), _user.getId());
            assertEquals(d.getGroup().getId(), _group.getId());
            assertTrue(_perms.sameRights(v.getDetails().getPermissions()));
        } catch (ServerError e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }

    protected void createProject(ServiceFactory sf, Permissions perms,
            ExperimenterGroup group) {
        prj = new ProjectI();
        prj.setName(rstring("single"));
        prj.getDetails().setPermissions(perms);
        prj.getDetails().setGroup(group);
        prj = sf.getUpdateService().saveAndReturnObject(prj);
    }

    protected void createDataset(ServiceFactory sf, Permissions perms,
            ExperimenterGroup group) {
        ds = new DatasetI();
        ds.setName("single");
        ds.getDetails().setPermissions(perms);
        ds.getDetails().setGroup(group);
        ds = sf.getUpdateService().saveAndReturnObject(ds);
    }

    protected void createPDLink(ServiceFactory sf, Permissions perms,
            ExperimenterGroup group) {
        link = new ProjectDatasetLink();
        link.link(prj, ds);
        link.getDetails().setPermissions(perms);
        link.getDetails().setGroup(group);
        link = sf.getUpdateService().saveAndReturnObject(link);
        ds = link.child();
        prj = link.parent();
    }

    protected void createPixels(ServiceFactory sf, ExperimenterGroup group,
            Permissions perms) {
        pix = ObjectFactory.createPixelGraph(null);
        pix.getDetails().setGroup(group);
        // pix.getDetails().setPermissions(perms); must be done for whole graph
        sf.setUmask(perms);
        pix = sf.getUpdateService().saveAndReturnObject(pix);
        sf.setUmask(null);
    }

    protected void createThumbnail(ServiceFactory sf, ExperimenterGroup group,
            Permissions perms, Pixels _p) {
        tb = ObjectFactory.createThumbnails(_p);
        tb.getDetails().setPermissions(perms);
        tb.getDetails().setGroup(group);
        tb = sf.getUpdateService().saveAndReturnObject(tb);
    }

    protected void createImage(ServiceFactory sf, ExperimenterGroup group,
            Permissions perms, Pixels p) {
        img = new Image();
        img.setName("special");
        Details d = img.getDetails();
        d.setGroup(group);
        d.setPermissions(perms);
        img.addPixels(p);
        img = sf.getUpdateService().saveAndReturnObject(img);
    }

    protected void createMicroscope(ServiceFactory sf, ExperimenterGroup group,
            Permissions perms) {
        MicroscopeType type = new MicroscopeType();
        type.setValue("Upright");
        micro = new Microscope();
        micro.setManufacturer("test");
        micro.setModel("model");
        micro.setSerialNumber("123456789");
        micro.setType(type);
        Details d = micro.getDetails();
        d.setGroup(group);
        d.setPermissions(perms);
        micro = sf.getUpdateService().saveAndReturnObject(micro);
    }

    protected void createInstrument(ServiceFactory sf, ExperimenterGroup group,
            Permissions perms, Microscope m) {
        instr = new Instrument();
        instr.setMicroscope(m);
        Details d = instr.getDetails();
        d.setGroup(group);
        d.setPermissions(perms);
        instr = sf.getUpdateService().saveAndReturnObject(instr);
    }

    protected String makeModifiedMessage() {
        return "user can modify:" + UUID.randomUUID();
    }

}

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
import static omero.rtypes.rstring;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ome.system.Login;
import ome.testing.ObjectFactory;
import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.Details;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Instrument;
import omero.model.InstrumentI;
import omero.model.Microscope;
import omero.model.MicroscopeI;
import omero.model.MicroscopeType;
import omero.model.MicroscopeTypeI;
import omero.model.Permissions;
import omero.model.PermissionsI;
import omero.model.Pixels;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.model.Thumbnail;
import omero.util.IceMapper;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * The subclasses of {@link AbstractPermissionsTest} define the proper working
 * and the completeness of the security system.
 * 
 */
@Test(enabled=false, groups = { "broken", "security", "integration" })
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

    final static protected ome.model.internal.Permissions RW_RW_RW = new ome.model.internal.Permissions(),
            RW_RW_xx = new ome.model.internal.Permissions().revoke(WORLD, READ, WRITE),
            RW_xx_xx = new ome.model.internal.Permissions().revoke(WORLD, READ, WRITE).revoke(
                    GROUP, READ, WRITE), xx_xx_xx = new ome.model.internal.Permissions().revoke(
                    WORLD, READ, WRITE).revoke(GROUP, READ, WRITE).revoke(USER,
                    READ, WRITE), RW_RW_Rx = new ome.model.internal.Permissions().revoke(WORLD,
                    WRITE), RW_Rx_Rx = new ome.model.internal.Permissions().revoke(WORLD, WRITE)
                    .revoke(GROUP, WRITE), Rx_Rx_Rx = new ome.model.internal.Permissions().revoke(
                    WORLD, WRITE).revoke(GROUP, WRITE).revoke(USER, WRITE),
            Rx_Rx_xx = new ome.model.internal.Permissions().revoke(WORLD, READ, WRITE).revoke(
                    GROUP, WRITE).revoke(USER, WRITE),
            Rx_xx_xx = new ome.model.internal.Permissions().revoke(WORLD, READ, WRITE).revoke(
                    GROUP, READ, WRITE).revoke(USER, WRITE);

    protected ExperimenterGroup system_group = new ExperimenterGroupI(0L, false),
            common_group = new ExperimenterGroupI(),
            user_other_group = new ExperimenterGroupI();
   
    protected Experimenter root = new ExperimenterI(0L, false),
            pi = new ExperimenterI(), user = new ExperimenterI(),
            other = new ExperimenterI(), world = new ExperimenterI();

    protected String gname, cname;

    ServiceFactoryPrx r, p, w, u, o;

    protected Project prj;

    protected Dataset ds;

    protected ProjectDatasetLink link;

    protected Pixels pix;

    protected Thumbnail tb;

    protected Image img;

    protected Microscope micro;

    protected Instrument instr;

    protected ServiceFactoryPrx ownsfA, ownsfB, ownsfC;

    protected Permissions permsA, permsB, permsC;

    protected Experimenter ownerA, ownerB, ownerC;

    protected ExperimenterGroup groupA, groupB, groupC;

    @BeforeClass
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
        p = c.createSession(piLogin.getName(), piLogin.getPassword());
        pi.setOmeName(rstring(piLogin.getName()));
        pi.setFirstName(rstring("read"));
        pi.setLastName(rstring("security -- leader of user_other_group"));
        pi = new ExperimenterI(rootAdmin.createUser(pi, gname), false);
        
        List<ExperimenterGroup> common_groups = new ArrayList<ExperimenterGroup>();
        common_groups.add(common_group);
        rootAdmin.addGroups(pi, common_groups);

        // make the PI the group leader.
        rootAdmin.setGroupOwner(user_other_group, pi);
        // ENDTODO

        List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
        groups.add(user_other_group);
        groups.add(common_group); 
        
        // create a new user in that group
        Login userLogin = new Login(UUID.randomUUID().toString(), "empty",
                gname, "Test");
        u = c.createSession(userLogin.getName(), userLogin.getPassword());
        user.setOmeName(rstring(userLogin.getName()));
        user.setFirstName(rstring("read"));
        user.setLastName(rstring("security"));
        user = new ExperimenterI(rootAdmin.createUser(user, gname), false);
        rootAdmin.addGroups(user, groups);

        // create another user in that group
        Login otherLogin = new Login(UUID.randomUUID().toString(), "empty",
                gname, "Test");
        o = c.createSession(otherLogin.getName(), otherLogin.getPassword());
        other.setOmeName(rstring(otherLogin.getName()));
        other.setFirstName(rstring("read"));
        other.setLastName(rstring("security2"));
        other = new ExperimenterI(rootAdmin.createUser(other, gname), false);
        rootAdmin.addGroups(other, groups);

        // create a third regular user not in that group
        Login worldLogin = new Login(UUID.randomUUID().toString(), "empty" /*
         * not
         * gname!
         */);
        w = c.createSession(worldLogin.getName(), worldLogin.getPassword());
        world.setOmeName(rstring(worldLogin.getName()));
        world.setFirstName(rstring("read"));
        world.setLastName(rstring("Security -- not in their group"));
        world = new ExperimenterI(rootAdmin.createUser(world, cname), false);
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
            v = (IObject) rootQuery.get(_i.getClass().getName(), _i.getId().getValue());
            Details d = v.getDetails();
            assertEquals(d.getOwner().getId(), _user.getId());
            assertEquals(d.getGroup().getId(), _group.getId());
            assertTrue(_perms.equals(v.getDetails().getPermissions()));
        } catch (ServerError e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unimplemented exception.");
        }
    }

    protected void createProject(ServiceFactoryPrx ownsfA2, Permissions perms,
            ExperimenterGroup group) throws ServerError {
        prj = new ProjectI();
        prj.setName(rstring("single"));
        prj.getDetails().setPermissions(perms);
        prj.getDetails().setGroup(group);
        prj = (Project) ownsfA2.getUpdateService().saveAndReturnObject(prj);
    }

    protected void createDataset(ServiceFactoryPrx ownsfB2, Permissions perms,
            ExperimenterGroup group) throws ServerError {
        ds = new DatasetI();
        ds.setName(rstring("single"));
        ds.getDetails().setPermissions(perms);
        ds.getDetails().setGroup(group);
        ds = (Dataset) ownsfB2.getUpdateService().saveAndReturnObject(ds);
    }

    protected void createPDLink(ServiceFactoryPrx ownsfC2, Permissions perms,
            ExperimenterGroup group) throws ServerError {
        link = new ProjectDatasetLinkI();
        link.link(prj, ds);
        link.getDetails().setPermissions(perms);
        link.getDetails().setGroup(group);
        link = (ProjectDatasetLink) ownsfC2.getUpdateService().saveAndReturnObject(link);
        ds = link.getChild();
        prj = link.getParent();
    }

    protected void createPixels(ServiceFactoryPrx ownsfA2, ExperimenterGroup group,
            Permissions perms) throws Exception {
        ome.model.core.Pixels _pix = ObjectFactory.createPixelGraph(null);
        IceMapper mapper = new IceMapper();
        pix = (Pixels) mapper.map(_pix);
        
        pix.getDetails().setGroup(group);
        pix = (Pixels) ownsfA2.getUpdateService().saveAndReturnObject(pix);
    }

    protected void createThumbnail(ServiceFactoryPrx ownsfB2, ExperimenterGroup group,
            Permissions perms, Pixels p) throws Exception {
        IceMapper mapper = new IceMapper();
        ome.model.core.Pixels _p = (ome.model.core.Pixels) mapper.reverse(p);
        ome.model.display.Thumbnail _tb = ObjectFactory.createThumbnails(_p);
        tb = (Thumbnail) mapper.map(_tb);
        
        tb.getDetails().setPermissions(new PermissionsI(perms.toString()));
        tb.getDetails().setGroup(group);
        tb = (omero.model.Thumbnail) ownsfB2.getUpdateService().saveAndReturnObject(tb);
    }

    protected void createImage(ServiceFactoryPrx ownsfA2, ExperimenterGroup group,
            Permissions perms, Pixels p) throws ServerError {
        img = new ImageI();
        img.setName(rstring("special"));
        Details d = img.getDetails();
        d.setGroup(group);
        d.setPermissions(perms);
        img.addPixels(p);
        img = (Image) ownsfA2.getUpdateService().saveAndReturnObject(img);
    }

    protected void createMicroscope(ServiceFactoryPrx ownsfB2, ExperimenterGroup group,
            Permissions perms) throws ServerError {
        MicroscopeType type = new MicroscopeTypeI();
        type.setValue(rstring("Upright"));
        micro = new MicroscopeI();
        micro.setManufacturer(rstring("test"));
        micro.setModel(rstring("model"));
        micro.setSerialNumber(rstring("123456789"));
        micro.setType(type);
        Details d = micro.getDetails();
        d.setGroup(group);
        d.setPermissions(perms);
        micro = (Microscope) ownsfB2.getUpdateService().saveAndReturnObject(micro);
    }

    protected void createInstrument(ServiceFactoryPrx ownsfA2, ExperimenterGroup group,
            Permissions perms, Microscope m) throws ServerError {
        instr = new InstrumentI();
        instr.setMicroscope(m);
        Details d = instr.getDetails();
        d.setGroup(group);
        d.setPermissions(perms);
        instr = (Instrument) ownsfA2.getUpdateService().saveAndReturnObject(instr);
    }

    protected String makeModifiedMessage() {
        return "user can modify:" + UUID.randomUUID();
    }

    //TODO ticket:1478
    protected void assertSameRights(Permissions p1, Permissions p2) {
        IceMapper mapper = new IceMapper();
        ome.model.internal.Permissions _p1 = mapper.convert(p1);
        ome.model.internal.Permissions _p2 = mapper.convert(p2);
        assertTrue(p1 + "!=" + p2, _p1.sameRights(_p2));
    }
}

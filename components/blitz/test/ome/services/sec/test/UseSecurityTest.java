/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sec.test;

import static omero.rtypes.rstring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ome.conditions.SecurityViolation;
import ome.model.core.Pixels;
import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.model.DatasetI;
import omero.model.Details;
import omero.model.DetailsI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Permissions;
import omero.model.PermissionsI;
import omero.model.Project;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;

import org.testng.annotations.Test;

@Test(enabled=false, groups = { "broken", "ticket:337", "security", "integration" })
public class UseSecurityTest extends AbstractPermissionsTest {

    // unlike READ and WRITE tests, we don't need to repeat the tests for each
    // user
    // create and the the test is what the user is trying to change.
    // what's allowed when locked. then it makes sense to also test root
    // too see if it will fail. don't need to test the unlinkable
    // configurations.

    boolean will_lock;

    // ~ single
    // =========================================================================

    // single plays little role in USE, but verify not locked
    @Override
    public void testSingleProject_U() throws Exception {
        createProject(u, new PermissionsI(RW_Rx_Rx.toString()), user_other_group);
        verifyDetails(prj, user, user_other_group, new PermissionsI(RW_Rx_Rx.toString()));
    }

    @Override
    public void testSingleProject_W() throws Exception {
        createProject(w, new PermissionsI(RW_Rx_Rx.toString()), common_group);
        verifyDetails(prj, world, common_group, new PermissionsI(RW_Rx_Rx.toString()));
    }

    @Override
    public void testSingleProject_R() throws Exception {
        createProject(r, new PermissionsI(RW_Rx_Rx.toString()), system_group);
        verifyDetails(prj, root, system_group, new PermissionsI(RW_Rx_Rx.toString()));
    }

    // ~ one-to-many
    // =========================================================================

    @Override
    public void test_U_Pixels_And_U_Thumbnails() throws Exception {
        ownsfA = u;
        ownerA = user;
        groupA = user_other_group;

        ownsfB = u;
        ownerB = user;
        groupB = user_other_group;

        will_lock = true;

        // RW_RW_RW / RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_RW.toString());
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_RW_Rx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_Rx.toString());
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_RW_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_xx.toString());
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_Rx_Rx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_Rx_Rx.toString());
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_xx_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, true, other);

        // RW_RW_Rx / RW_RW_Rx
        permsA = new PermissionsI(RW_RW_Rx.toString());
        permsB = new PermissionsI(RW_RW_Rx.toString());
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, true, other);

        // RW_RW_xx / RW_RW_xx
        permsA = new PermissionsI(RW_RW_xx.toString());
        permsB = new PermissionsI(RW_RW_xx.toString());
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, false, other);

        // RW_Rx_Rx / RW_Rx_Rx
        permsA = new PermissionsI(RW_Rx_Rx.toString());
        permsB = new PermissionsI(RW_Rx_Rx.toString());
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, true, other);

        // RW_xx_xx / RW_xx_xx
        permsA = new PermissionsI(RW_xx_xx.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        oneToMany(ownsfA, false, xx_xx_xx, common_group);
        oneToMany(r, false, other);

        // Rx_Rx_Rx / Rx_Rx_Rx
        permsA = new PermissionsI(Rx_Rx_Rx.toString());
        permsB = new PermissionsI(Rx_Rx_Rx.toString());
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, true, other);

        // xx_xx_xx / xx_xx_xx No need. can't create.
    }

    private void oneToMany(ServiceFactoryPrx ownsfA, boolean b,
            Permissions rwXxXx, omero.model.ExperimenterGroup commonGroup)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }

    @Override
    public void test_O_Pixels_And_U_Thumbnails() throws Exception {
        ownsfA = o;
        ownerA = other;
        groupA = user_other_group;

        ownsfB = u;
        ownerB = user;
        groupB = user_other_group;

        will_lock = true;

        // RW_RW_RW / RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_RW.toString());
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, user);

        // RW_RW_RW / RW_RW_Rx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_Rx.toString());
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, user);

        // RW_RW_RW / RW_RW_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_xx.toString());
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, user);

        // RW_RW_RW / RW_xx_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, user);

        // RW_RW_RW / xx_xx_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(xx_xx_xx.toString());
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, user);

        // RW_RW_xx / RW_xx_xx
        permsA = new PermissionsI(RW_RW_xx.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, false, user);

    }

    /** */
    @Override
    public void test_U_Pixels_And_O_Thumbnails() throws Exception {
        ownsfA = u;
        ownerA = user;
        groupA = user_other_group;

        ownsfB = o;
        ownerB = other;
        groupB = user_other_group;

        will_lock = true;

        // RW_RW_RW / RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_RW.toString());
        oneToMany(ownsfB, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_RW_Rx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_Rx.toString());
        oneToMany(ownsfB, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_RW_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_xx.toString());
        oneToMany(ownsfB, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_xx_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        oneToMany(ownsfB, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_RW / xx_xx_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(xx_xx_xx.toString());
        oneToMany(ownsfB, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_xx / RW_xx_xx
        permsA = new PermissionsI(RW_RW_xx.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        oneToMany(ownsfB, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, false, other);
    }

    @Override
    public void test_U_Pixels_And_R_Thumbnails() throws Exception {
        ownsfA = u;
        ownerA = user;
        groupA = user_other_group;

        ownsfB = r;
        ownerB = root;
        groupB = system_group;

        will_lock = true;

        // root can read everything and so can lock everything.

        // RW_RW_RW / RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_RW.toString());
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_RW_Rx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_Rx.toString());
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_RW_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_xx.toString());
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_xx_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_RW / xx_xx_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(xx_xx_xx.toString());
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_xx_xx / RW_xx_xx
        permsA = new PermissionsI(RW_xx_xx.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        oneToMany(ownsfA, false, common_group, xx_xx_xx, RW_RW_xx);
        oneToMany(r, false, other);

        // xx_xx_xx / xx_xx_xx
        permsA = new PermissionsI(xx_xx_xx.toString());
        permsB = new PermissionsI(xx_xx_xx.toString());
        oneToMany(ownsfA, false, common_group);
        oneToMany(r, false, other);

    }

    protected void oneToMany(ServiceFactoryPrx sf, boolean can_change,
            Object... details_changed) throws Exception {

        // whether or not this is valid is handled in the ReadSecurityTest.
        // an exception here means something went wrong elsewhere; most likely,
        // that one tried to creat objects with permissions xx_xx_xx
        createPixels(ownsfA, groupA, permsA);
        verifyDetails(pix, ownerA, groupA, permsA);
        createThumbnail(ownsfB, groupB, permsB, pix);
        verifyDetails(tb, ownerB, groupB, permsB);

    }

    // ~ unidirectional many-to-one
    // =========================================================================

    /** */
    @Override
    public void test_U_Instrument_And_U_Microscope() throws Exception {
        ownsfA = ownsfB = u;
        ownerA = ownerB = user;
        groupA = groupB = user_other_group;

        will_lock = true;

        // root can read everything and so can lock everything.

        // RW_RW_RW / RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_RW.toString());
        uniManyToOne(ownsfA, false, common_group, new PermissionsI(RW_xx_xx.toString()), new PermissionsI(RW_RW_xx.toString()));
        uniManyToOne(r, true, other);
    }

    protected void uniManyToOne(ServiceFactoryPrx sf, boolean can_change,
            Object... details_changed) throws Exception {

        // whether or not this is valid is handled in the ReadSecurityTest.
        // an exception here means something went wrong elsewhere; most likely,
        // that one tried to creat objects with permissions xx_xx_xx
        createMicroscope(ownsfA, groupA, permsA);
        verifyDetails(micro, ownerA, groupA, permsA);
        createInstrument(ownsfB, groupB, permsB, micro);
        verifyDetails(instr, ownerB, groupB, permsB);

        // it is not at all easy to clear a Pixels locked status, but
        // microscope should be easy enough
        if (will_lock) {
            r.getUpdateService().deleteObject(instr);
            List<IObject> micros = new ArrayList<IObject>();
            micros.add(micro);
            // boolean[] unlocked = r.getAdminService().unlock(micros);
            // assertTrue(unlocked[0]); ticket:2039 - TOO BE REMOVED
        }

    }

    // ~ many-to-many
    // =========================================================================

    @Override
    public void test_U_Projects_U_Datasets_U_Link() throws Exception {
        ownsfA = ownsfB = ownsfC = u;
        ownerA = ownerB = ownerC = user;
        groupA = groupB = groupC = user_other_group;

        // RW_RW_RW / RW_RW_RW / RW_RW_RW
        permsA = permsB = permsC = new PermissionsI(RW_RW_RW.toString());
        manyToMany();

        // RW_RW_RW / RW_RW_xx / RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_xx.toString());
        permsC = new PermissionsI(RW_RW_RW.toString());
        manyToMany();

        // RW_RW_RW / RW_xx_xx / RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        permsC = new PermissionsI(RW_RW_RW.toString());
        manyToMany();

    }

    // TODO add to abstract class.
    @Test(enabled=false, groups = "ticket:553")
    public void test_U_Projects_O_Datasets_O_Link() throws Exception {
        ownsfA = u;
        ownerA = user;
        groupA = user_other_group;

        ownsfB = ownsfC = o;
        ownerB = ownerC = other;
        groupB = groupC = user_other_group;

        will_lock = true;

        // RW_RW_RW / RW_RW_RW / RW_RW_RW
        permsA = permsB = permsC = new PermissionsI(RW_RW_RW.toString());
        manyToMany();

        // RW_RW_RW / RW_RW_xx / RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_xx.toString());
        permsC = new PermissionsI(RW_RW_RW.toString());
        manyToMany();

        // RW_RW_RW / RW_xx_xx / RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        permsC = new PermissionsI(RW_RW_RW.toString());
        manyToMany();

    }

    /**
     * performs various write operations on linked projects and datasets.
     */
    protected void manyToMany() throws Exception {

        createProject(ownsfA, permsA, groupA);
        verifyDetails(prj, ownerA, groupA, permsA);

        createDataset(ownsfB, permsB, groupB);
        verifyDetails(ds, ownerB, groupB, permsB);

        createPDLink(ownsfC, permsC, groupC);
        verifyDetails(link, ownerC, groupC, permsC);

        r.getUpdateService().deleteObject(link);
        // boolean[] unlocked = r.getAdminService().unlock(Arrays.asList(prj, ds));
        // assertTrue(unlocked[0]);
        // assertTrue(unlocked[1]);ticket:2039 - TOO BE REMOVED

    }

    // ~ Special: "tag" (e.g. Image/Pixels)
    // =========================================================================
    // Note: in 3.0-Beta3 the special tag was removed. This test is now
    // disabled.

    @Override
    @Test(enabled = false)
    public void test_U_Image_U_Pixels() throws Exception {
        ownsfA = ownsfB = u;
        ownerA = ownerB = user;
        groupA = groupB = user_other_group;

        will_lock = true;

        // RW_RW_RW / RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_RW.toString());
        imagePixels(u, false, false, common_group);
        imagePixels(r, true, true, other);

        // RW_RW_RW / RW_RW_Rx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_xx.toString());
        imagePixels(u, false, false, common_group);
        imagePixels(r, true, false, other);

        // RW_RW_RW / RW_xx_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        imagePixels(u, false, false, common_group);
        imagePixels(r, true, false, other);

    }

    protected void imagePixels(ServiceFactoryPrx sf, boolean can_change_img,
            boolean can_change_pix, Object... details_changed) throws Exception {

        // whether or not this is valid is handled in the ReadSecurityTest.
        // an exception here means something went wrong elsewhere; most likely,
        // that one tried to create objects with permissions xx_xx_xx
        createPixels(ownsfB, groupB, permsB);
        createImage(ownsfA, groupA, permsA, pix);
        verifyDetails(img, ownerA, groupA, permsA);
        verifyDetails(pix, ownerB, groupB, permsB);

    }

    // ~ Other
    // ========================================================================

    @Test(enabled=false)
    public void testNoLoadOnNonReadableProxy() throws Exception {

        Permissions USER_PRIVATE = new PermissionsI(ome.model.internal.Permissions.USER_PRIVATE.toString());
        
        prj = new ProjectI();
        prj.setName(rstring("noloadonnonreadable"));
        prj.getDetails().setPermissions(USER_PRIVATE);
        prj = (Project) u.getUpdateService().saveAndReturnObject(prj);

        prj.unload();
        ds = new DatasetI();
        ds.setName(rstring("tryingtoattachtononloadablenonreadable"));
        link = new ProjectDatasetLinkI();
        link.link(prj, ds);
        try {
            w.getUpdateService().saveObject(link);
            fail("secvio!: A user should not be able to pass in an unreadable, unloaded"
                    + " proxy and have it linked. This would allow RW to suffice"
                    + " for RW*U*");
        } catch (SecurityViolation sv) {
            // ok.
        }
    }

    @Test(enabled=false, groups = "ticket:339")
    public void test_HandlesExplicitPermissionReduction() throws Exception {

        prj = new ProjectI();
        prj.setName(rstring("ticket:339"));
        ds = new DatasetI();
        ds.setName(rstring("ticket:339"));
        prj.linkDataset(ds);

        Permissions perms = new PermissionsI(ome.model.internal.Permissions.READ_ONLY.toString()); // relatively common
        // use-case
        prj.getDetails().setPermissions(perms);

        Project t = (Project) u.getUpdateService().saveAndReturnObject(prj);

    }

    // ~ Helpers
    // =========================================================================

    private void assertSucceeds(ServiceFactoryPrx sf) throws ServerError {
        prj = (Project) sf.getUpdateService().saveAndReturnObject(prj);
    }

    private void assertFails(ServiceFactoryPrx sf) throws ServerError {
        try {
            assertSucceeds(sf);
            fail("secvio!");
        } catch (SecurityViolation sv) {
            // ok
        }
    }

    private void assertNoChange(ServiceFactoryPrx sf) throws Exception {
        Permissions p1 = prj.getDetails().getPermissions();
        prj = (Project) sf.getUpdateService().saveAndReturnObject(prj);
        Permissions p2 = prj.getDetails().getPermissions();
        assertSameRights(p1, p2);
    }

    protected Details d(IObject _i, Object _o) {
        fail("IMPLEMENT: ticket:1478");
        // Details retVal = _i.getDetails().copy();
        Details retVal = new DetailsI();
        // prevent error on different update event versions.
        retVal.setCreationEvent(null);
        retVal.setUpdateEvent(null);
        if (_o instanceof Experimenter) {
            Experimenter _e = (Experimenter) _o;
            retVal.setOwner(_e);
        } else if (_o instanceof ExperimenterGroup) {
            ExperimenterGroup _g = (ExperimenterGroup) _o;
            retVal.setGroup(_g);
        } else if (_o instanceof Permissions) {
            Permissions _p = (Permissions) _o;
            retVal.setPermissions(_p);
        } else {
            throw new IllegalArgumentException("Not user/group/permissions:"
                    + _o);
        }
        return retVal;
    }
}

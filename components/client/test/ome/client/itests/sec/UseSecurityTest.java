/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client.itests.sec;

import ome.conditions.SecurityViolation;
import ome.model.ILink;
import ome.model.IObject;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Flag;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Parameters;
import ome.system.ServiceFactory;
import ome.testing.ObjectFactory;

import org.testng.annotations.Test;

@Test(groups = { "ticket:337", "security", "integration" })
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
        createProject(u, RW_Rx_Rx, user_other_group);
        verifyDetails(prj, user, user_other_group, RW_Rx_Rx);
        verifyLockStatus(prj, false);
        verifyLocked(u, prj, d(prj, RW_xx_xx), true);
        verifyLocked(u, prj, d(prj, common_group), true);
    }

    @Override
    public void testSingleProject_W() throws Exception {
        createProject(w, RW_Rx_Rx, common_group);
        verifyDetails(prj, world, common_group, RW_Rx_Rx);
        verifyLockStatus(prj, false);
        verifyLocked(w, prj, d(prj, RW_xx_xx), true); // no other group
    }

    @Override
    public void testSingleProject_R() throws Exception {
        createProject(r, RW_Rx_Rx, system_group);
        verifyDetails(prj, root, system_group, RW_Rx_Rx);
        verifyLockStatus(prj, false);
        verifyLocked(r, prj, d(prj, RW_xx_xx), true);
        verifyLocked(r, prj, d(prj, common_group), true);
        verifyLocked(r, prj, d(prj, world), true);
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
        permsA = RW_RW_RW;
        permsB = RW_RW_RW;
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_RW_Rx
        permsA = RW_RW_RW;
        permsB = RW_RW_Rx;
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_RW_xx
        permsA = RW_RW_RW;
        permsB = RW_RW_xx;
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_Rx_Rx
        permsA = RW_RW_RW;
        permsB = RW_Rx_Rx;
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_xx_xx
        permsA = RW_RW_RW;
        permsB = RW_xx_xx;
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, true, other);

        // RW_RW_Rx / RW_RW_Rx
        permsA = RW_RW_Rx;
        permsB = RW_RW_Rx;
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, true, other);

        // RW_RW_xx / RW_RW_xx
        permsA = RW_RW_xx;
        permsB = RW_RW_xx;
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, false, other);

        // RW_Rx_Rx / RW_Rx_Rx
        permsA = RW_Rx_Rx;
        permsB = RW_Rx_Rx;
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, true, other);

        // RW_xx_xx / RW_xx_xx
        permsA = RW_xx_xx;
        permsB = RW_xx_xx;
        oneToMany(ownsfA, false, xx_xx_xx, common_group);
        oneToMany(r, false, other);

        // Rx_Rx_Rx / Rx_Rx_Rx
        permsA = Rx_Rx_Rx;
        permsB = Rx_Rx_Rx;
        oneToMany(ownsfA, false, RW_xx_xx, common_group);
        oneToMany(r, true, other);

        // xx_xx_xx / xx_xx_xx No need. can't create.
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
        permsA = RW_RW_RW;
        permsB = RW_RW_RW;
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, user);

        // RW_RW_RW / RW_RW_Rx
        permsA = RW_RW_RW;
        permsB = RW_RW_Rx;
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, user);

        // RW_RW_RW / RW_RW_xx
        permsA = RW_RW_RW;
        permsB = RW_RW_xx;
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, user);

        // RW_RW_RW / RW_xx_xx
        permsA = RW_RW_RW;
        permsB = RW_xx_xx;
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, user);

        // RW_RW_RW / xx_xx_xx
        permsA = RW_RW_RW;
        permsB = xx_xx_xx;
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, user);

        // RW_RW_xx / RW_xx_xx
        permsA = RW_RW_xx;
        permsB = RW_xx_xx;
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
        permsA = RW_RW_RW;
        permsB = RW_RW_RW;
        oneToMany(ownsfB, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_RW_Rx
        permsA = RW_RW_RW;
        permsB = RW_RW_Rx;
        oneToMany(ownsfB, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_RW_xx
        permsA = RW_RW_RW;
        permsB = RW_RW_xx;
        oneToMany(ownsfB, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_xx_xx
        permsA = RW_RW_RW;
        permsB = RW_xx_xx;
        oneToMany(ownsfB, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_RW / xx_xx_xx
        permsA = RW_RW_RW;
        permsB = xx_xx_xx;
        oneToMany(ownsfB, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_xx / RW_xx_xx
        permsA = RW_RW_xx;
        permsB = RW_xx_xx;
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
        permsA = RW_RW_RW;
        permsB = RW_RW_RW;
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_RW_Rx
        permsA = RW_RW_RW;
        permsB = RW_RW_Rx;
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_RW_xx
        permsA = RW_RW_RW;
        permsB = RW_RW_xx;
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_RW / RW_xx_xx
        permsA = RW_RW_RW;
        permsB = RW_xx_xx;
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_RW_RW / xx_xx_xx
        permsA = RW_RW_RW;
        permsB = xx_xx_xx;
        oneToMany(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        oneToMany(r, true, other);

        // RW_xx_xx / RW_xx_xx
        permsA = RW_xx_xx;
        permsB = RW_xx_xx;
        oneToMany(ownsfA, false, common_group, xx_xx_xx, RW_RW_xx);
        oneToMany(r, false, other);

        // xx_xx_xx / xx_xx_xx
        permsA = xx_xx_xx;
        permsB = xx_xx_xx;
        oneToMany(ownsfA, false, common_group);
        oneToMany(r, false, other);

    }

    protected void oneToMany(ServiceFactory sf, boolean can_change,
            Object... details_changed) {

        // whether or not this is valid is handled in the ReadSecurityTest.
        // an exception here means something went wrong elsewhere; most likely,
        // that one tried to creat objects with permissions xx_xx_xx
        createPixels(ownsfA, groupA, permsA);
        verifyDetails(pix, ownerA, groupA, permsA);
        createThumbnail(ownsfB, groupB, permsB, pix);
        verifyDetails(tb, ownerB, groupB, permsB);

        verifyLockStatus(pix, will_lock);
        for (Object object : details_changed) {
            verifyLocked(sf, pix, d(pix, object), can_change);
        }

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
        permsA = RW_RW_RW;
        permsB = RW_RW_RW;
        uniManyToOne(ownsfA, false, common_group, RW_xx_xx, RW_RW_xx);
        uniManyToOne(r, true, other);
    }

    protected void uniManyToOne(ServiceFactory sf, boolean can_change,
            Object... details_changed) {

        // whether or not this is valid is handled in the ReadSecurityTest.
        // an exception here means something went wrong elsewhere; most likely,
        // that one tried to creat objects with permissions xx_xx_xx
        createMicroscope(ownsfA, groupA, permsA);
        verifyDetails(micro, ownerA, groupA, permsA);
        createInstrument(ownsfB, groupB, permsB, micro);
        verifyDetails(instr, ownerB, groupB, permsB);

        verifyLockStatus(micro, will_lock);
        for (Object object : details_changed) {
            verifyLocked(sf, micro, d(micro, object), can_change);
        }
        // it is not at all easy to clear a Pixels locked status, but
        // microscope should be easy enough
        if (will_lock) {
            r.getUpdateService().deleteObject(instr);
            boolean[] unlocked = r.getAdminService().unlock(micro);
            assertTrue(unlocked[0]);
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
        permsA = permsB = permsC = RW_RW_RW;
        manyToMany();

        // RW_RW_RW / RW_RW_xx / RW_RW_RW
        permsA = RW_RW_RW;
        permsB = RW_RW_xx;
        permsC = RW_RW_RW;
        manyToMany();

        // RW_RW_RW / RW_xx_xx / RW_RW_RW
        permsA = RW_RW_RW;
        permsB = RW_xx_xx;
        permsC = RW_RW_RW;
        manyToMany();

    }

    // TODO add to abstract class.
    @Test(groups = "ticket:553")
    public void test_U_Projects_O_Datasets_O_Link() throws Exception {
        ownsfA = u;
        ownerA = user;
        groupA = user_other_group;

        ownsfB = ownsfC = o;
        ownerB = ownerC = other;
        groupB = groupC = user_other_group;

        will_lock = true;

        // RW_RW_RW / RW_RW_RW / RW_RW_RW
        permsA = permsB = permsC = RW_RW_RW;
        manyToMany();

        // RW_RW_RW / RW_RW_xx / RW_RW_RW
        permsA = RW_RW_RW;
        permsB = RW_RW_xx;
        permsC = RW_RW_RW;
        manyToMany();

        // RW_RW_RW / RW_xx_xx / RW_RW_RW
        permsA = RW_RW_RW;
        permsB = RW_xx_xx;
        permsC = RW_RW_RW;
        manyToMany();

    }

    /**
     * performs various write operations on linked projects and datasets.
     */
    protected void manyToMany() {

        createProject(ownsfA, permsA, groupA);
        verifyDetails(prj, ownerA, groupA, permsA);

        createDataset(ownsfB, permsB, groupB);
        verifyDetails(ds, ownerB, groupB, permsB);

        createPDLink(ownsfC, permsC, groupC);
        verifyDetails(link, ownerC, groupC, permsC);

        verifyLockStatus(prj, will_lock);
        verifyLockStatus(ds, will_lock);

        r.getUpdateService().deleteObject(link);
        boolean[] unlocked = r.getAdminService().unlock(prj, ds);
        assertTrue(unlocked[0]);
        assertTrue(unlocked[1]);

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
        permsA = RW_RW_RW;
        permsB = RW_RW_RW;
        imagePixels(u, false, false, common_group);
        imagePixels(r, true, true, other);

        // RW_RW_RW / RW_RW_Rx
        permsA = RW_RW_RW;
        permsB = RW_RW_xx;
        imagePixels(u, false, false, common_group);
        imagePixels(r, true, false, other);

        // RW_RW_RW / RW_xx_xx
        permsA = RW_RW_RW;
        permsB = RW_xx_xx;
        imagePixels(u, false, false, common_group);
        imagePixels(r, true, false, other);

    }

    protected void imagePixels(ServiceFactory sf, boolean can_change_img,
            boolean can_change_pix, Object... details_changed) {

        // whether or not this is valid is handled in the ReadSecurityTest.
        // an exception here means something went wrong elsewhere; most likely,
        // that one tried to create objects with permissions xx_xx_xx
        createPixels(ownsfB, groupB, permsB);
        createImage(ownsfA, groupA, permsA, pix);
        verifyDetails(img, ownerA, groupA, permsA);
        verifyDetails(pix, ownerB, groupB, permsB);

        // This is no longer true; the "default tag" metaphor no longer
        // exists, and so the image will not be automatically locked.
        // verifyLockStatus(img, will_lock);
        verifyLockStatus(pix, will_lock); // both locked. see
        // http://trac.openmicroscopy.org.uk/ome/ticket/357

        for (Object object : details_changed) {
            verifyLocked(sf, img, d(img, object), can_change_img);
        }
        for (Object object : details_changed) {
            verifyLocked(sf, pix, d(pix, object), can_change_pix);
        }

        // TODO should try to clear lock status.

    }

    // ~ Other
    // ========================================================================

    @Test
    public void testNoLoadOnNonReadableProxy() throws Exception {

        prj = new Project();
        prj.setName("noloadonnonreadable");
        prj.getDetails().setPermissions(Permissions.USER_PRIVATE);
        prj = u.getUpdateService().saveAndReturnObject(prj);
        assertFalse(prj.getDetails().getPermissions().isSet(Flag.LOCKED));

        prj.unload();
        ds = new Dataset();
        ds.setName("tryingtoattachtononloadablenonreadable");
        link = new ProjectDatasetLink();
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

    @Test
    public void testDeletingSingleLockedObject() throws Exception {
        Permissions perms = new Permissions().set(Flag.LOCKED);

        prj = new Project();
        prj.setName("deletinglocked");
        prj.getDetails().setPermissions(perms);
        prj = u.getUpdateService().saveAndReturnObject(prj);
        assertTrue(prj.getDetails().getPermissions().isSet(Flag.LOCKED));
        u.getUpdateService().deleteObject(prj);
    }

    // ~ Copy of server-side locking test. See:
    // http://trac.openmicroscopy.org.uk/ome/ticket/366
    // =========================================================================
    /** tests both transient and managed entities */
    public void test_ProjectIsLockedOnAddedDataset() throws Exception {

        prj = new Project();
        prj.setName("ticket:337");
        prj = u.getUpdateService().saveAndReturnObject(prj);

        assertFalse(prj.getDetails().getPermissions().isSet(Flag.LOCKED));

        ds = new Dataset();
        ds.setName("ticket:337");
        prj.linkDataset(ds);

        prj = u.getUpdateService().saveAndReturnObject(prj);
        ds = prj.linkedDatasetList().get(0);

        prj = u.getQueryService().find(prj.getClass(), prj.getId().longValue());
        ds = u.getQueryService().find(ds.getClass(), ds.getId().longValue());

        assertTrue(prj.getDetails().getPermissions().isSet(Flag.LOCKED));
        assertTrue(ds.getDetails().getPermissions().isSet(Flag.LOCKED));

    }

    @Test(dependsOnMethods = "test_ProjectIsLockedOnAddedDataset")
    public void test_RootCantOverride() throws Exception {
        reacquire(r);

        // try to change
        prj.getDetails().getPermissions().revoke(Role.USER, Right.READ);
        assertFails(r);

        reacquire(r);
        prj.getDetails().getPermissions().unSet(Flag.LOCKED);
        assertNoChange(r);

        // this succeeds because of loosened semantics. see:
        // http://trac.openmicroscopy.org.uk/ome/changeset/944
        // http://trac.openmicroscopy.org.uk/ome/ticket/337
        reacquire(r);
        prj.getDetails().setOwner(other);
        assertSucceeds(r);

        // now return it to the previous owner for testing.
        reacquire(r);
        prj.getDetails().setOwner(user);
        assertSucceeds(r);

        // but we can't change the group. too dynamic.
        reacquire(r);
        prj.getDetails().setGroup(common_group);
        assertFails(r);

    }

    @Test(dependsOnMethods = "test_ProjectIsLockedOnAddedDataset")
    public void test_UserCantOverride() throws Exception {
        reacquire(u);

        // try to change
        prj.getDetails().getPermissions().revoke(Role.USER, Right.READ);
        assertFails(u);

        reacquire(u);
        prj.getDetails().getPermissions().unSet(Flag.LOCKED);
        assertNoChange(u);

        // no set owner

        reacquire(u);
        prj.getDetails().setGroup(common_group);
        assertFails(u);

    }

    @Test(dependsOnMethods = { "test_RootCantOverride", "test_UserCantOverride" })
    public void test_OnceDatasetIsRemovedCanUnlock() throws Exception {

        ILink link = r.getQueryService().findByQuery(
                "select pdl from ProjectDatasetLink pdl "
                        + "where parent.id = :pid and child.id = :cid",
                new Parameters().addLong("pid", prj.getId()).addLong("cid",
                        ds.getId()));
        r.getUpdateService().deleteObject(link);

        r.getAdminService().unlock(prj);

        prj = r.getQueryService().find(prj.getClass(), prj.getId());
        assertFalse(prj.getDetails().getPermissions().isSet(Flag.LOCKED));
    }

    @Test
    public void test_AllowInitialLock() throws Exception {

        Permissions perms = new Permissions().set(Flag.LOCKED);

        prj = new Project();
        prj.setName("ticket:337");
        prj.getDetails().setPermissions(perms);

        Project t = u.getUpdateService().saveAndReturnObject(prj);
        assertTrue(t.getDetails().getPermissions().isSet(Flag.LOCKED));

        t = u.getUpdateService().saveAndReturnObject(prj); // cloning
        t.getDetails().getPermissions().set(Flag.LOCKED);
        t = u.getUpdateService().saveAndReturnObject(t); // save changes on
        // managed
        assertTrue(t.getDetails().getPermissions().isSet(Flag.LOCKED));

    }

    @Test(groups = "ticket:339")
    public void test_HandlesExplicitPermissionReduction() throws Exception {

        prj = new Project();
        prj.setName("ticket:339");
        ds = new Dataset();
        ds.setName("ticket:339");
        prj.linkDataset(ds);

        Permissions perms = Permissions.READ_ONLY; // relatively common
        // use-case
        prj.getDetails().setPermissions(perms);

        Project t = u.getUpdateService().saveAndReturnObject(prj);

    }

    @Test(groups = "ticket:357")
    public void test_OneToOnesGetLockedAsWell() throws Exception {

        img = new Image();
        img.setName("ticket:357");
        pix = ObjectFactory.createPixelGraph(null);
        img.addPixels(pix);

        img = u.getUpdateService().saveAndReturnObject(img);
        pix = img.iteratePixels().next();

        assertTrue(pix.getDetails().getPermissions().isSet(Flag.LOCKED));

    }

    // ~ Helpers
    // =========================================================================

    private void reacquire(ServiceFactory sf) {
        prj = sf.getQueryService()
                .find(prj.getClass(), prj.getId().longValue());
        assertTrue("Permissions should still be locked.", prj.getDetails()
                .getPermissions().isSet(Flag.LOCKED));
    }

    private void assertSucceeds(ServiceFactory sf) {
        prj = sf.getUpdateService().saveAndReturnObject(prj);
    }

    private void assertFails(ServiceFactory sf) {
        try {
            assertSucceeds(sf);
            fail("secvio!");
        } catch (SecurityViolation sv) {
            // ok
        }
    }

    private void assertNoChange(ServiceFactory sf) {
        Permissions p1 = prj.getDetails().getPermissions();
        prj = sf.getUpdateService().saveAndReturnObject(prj);
        Permissions p2 = prj.getDetails().getPermissions();
        assertTrue(p1.sameRights(p2));
    }

    protected void verifyLockStatus(IObject _i, boolean was_locked) {
        IObject v = rootQuery.get(_i.getClass(), _i.getId());
        Details d = v.getDetails();
        assertEquals(was_locked, d.getPermissions().isSet(Flag.LOCKED));
    }

    protected void verifyLocked(ServiceFactory sf, IObject _i, Details d,
            boolean can_change) {

        // shouldn't be able to remove read
        try {
            _i.getDetails().copy(d);
            sf.getUpdateService().saveObject(_i);
            if (!can_change) {
                fail("secvio!");
            }
        } catch (SecurityViolation sv) {
            if (can_change) {
                throw sv;
            }
        }
    }

    protected Details d(IObject _i, Object _o) {
        Details retVal = _i.getDetails().copy();
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

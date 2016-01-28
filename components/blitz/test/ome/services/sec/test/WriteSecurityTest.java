/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sec.test;

import java.util.Collection;
import java.util.Set;

import ome.conditions.SecurityViolation;
import ome.model.IEnum;

import omero.model.IObject;
import omero.model.PermissionsI;
import omero.RString;
import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.model.FilterI;
import omero.model.PixelsI;
import omero.model.Project;
import omero.model.Dataset;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.Microscope;
import omero.model.Instrument;
import omero.model.Filter;
import omero.model.Pixels;
import omero.model.Thumbnail;
import omero.model.ThumbnailI;
import omero.model.Image;
import omero.sys.Parameters;
import omero.sys.ParametersI;

import static omero.rtypes.rstring;

import org.testng.annotations.Test;

@Test(enabled=false, groups = { "broken", "ticket:236", "security", "integration" })
public class WriteSecurityTest extends AbstractPermissionsTest {

    /*
     * notes: ------ currently delete and write follow the same logic so both
     * are included here. if we develop separate logic (see allow{Update|Delete}
     * in BasicSecuritySystem) then we'll need to separate these out.
     * 
     * TODO this could all be moved to an excel table!
     */

    @Override
    public void testSingleProject_U() throws Exception {
        ownsfA = u;
        ownerA = user;
        groupA = user_other_group;

        // RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        single(u, true);
        single(o, true);
        single(w, true);
        single(p, true);
        single(r, true);

        // RW_RW_Rx
        permsA = new PermissionsI(RW_RW_Rx.toString());
        single(u, true);
        single(o, true);
        single(w, false);
        single(p, true);
        single(r, true);

        // RW_RW_xx
        permsA = new PermissionsI(RW_RW_xx.toString());
        single(u, true);
        single(o, true);
        single(w, false);
        single(p, true);
        single(r, true);

        // RW_Rx_Rx
        permsA = new PermissionsI(RW_Rx_Rx.toString());
        single(u, true);
        single(o, false);
        single(w, false);
        single(p, true);
        single(r, true);

        // RW_xx_xx
        permsA = new PermissionsI(RW_xx_xx.toString());
        single(u, true);
        single(o, false);
        single(w, false);
        single(p, true);
        single(r, true);

        // Rx_Rx_Rx
        permsA = new PermissionsI(Rx_Rx_Rx.toString());
        single(u, false);
        single(o, false);
        single(w, false);
        single(p, true);
        single(r, true);

        // xx_xx_xx
        permsA = new PermissionsI(xx_xx_xx.toString());
        single(u, false);
        single(o, false);
        single(w, false);
        single(p, true);
        single(r, true);

    }

    // don't need to test for OTHER because OTHER and USER are symmetric.

    @Override
    public void testSingleProject_W() throws Exception {
        ownsfA = w;
        ownerA = world;
        groupA = common_group;

        // RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        single(u, true);
        single(o, true);
        single(w, true);
        single(p, true);
        single(r, true);

        // RW_RW_Rx
        permsA = new PermissionsI(RW_RW_Rx.toString());
        single(u, true);
        single(o, true);
        single(w, true);
        single(p, true);
        single(r, true);

        // RW_RW_xx
        permsA = new PermissionsI(RW_RW_xx.toString());
        single(u, true);
        single(o, true);
        single(w, true);
        single(p, true);
        single(r, true);

        // RW_Rx_Rx
        permsA = new PermissionsI(RW_Rx_Rx.toString());
        single(u, false);
        single(o, false);
        single(w, true);
        single(p, false);
        single(r, true);

        // RW_xx_xx
        permsA = new PermissionsI(RW_xx_xx.toString());
        single(u, false);
        single(o, false);
        single(w, true);
        single(p, false);
        single(r, true);

        // Rx_Rx_Rx
        permsA = new PermissionsI(Rx_Rx_Rx.toString());
        single(u, false);
        single(o, false);
        single(w, false);
        single(p, false);
        single(r, true);

        // xx_xx_xx
        permsA = new PermissionsI(xx_xx_xx.toString());
        single(u, false);
        single(o, false);
        single(w, false);
        single(p, false);
        single(r, true);

    }

    // don't need to test PI because acts just like a group member

    @Override
    public void testSingleProject_R() throws Exception {
        ownsfA = r;
        ownerA = root;
        groupA = system_group;

        // RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        single(u, true);
        single(o, true);
        single(w, true);
        single(p, true);
        single(r, true);

        // RW_RW_Rx
        permsA = new PermissionsI(RW_RW_Rx.toString());
        single(u, false);
        single(o, false);
        single(w, false);
        single(p, false);
        single(r, true);

        // RW_RW_xx
        permsA = new PermissionsI(RW_RW_xx.toString());
        single(u, false);
        single(o, false);
        single(w, false);
        single(p, false);
        single(r, true);

        // Rx_Rx_Rx
        permsA = new PermissionsI(Rx_Rx_Rx.toString());
        single(u, false);
        single(o, false);
        single(w, false);
        single(p, false);
        single(r, true);

        // xx_xx_xx
        permsA = new PermissionsI(xx_xx_xx.toString());
        single(u, false);
        single(o, false);
        single(w, false);
        single(p, false);
        single(r, true);

    }

    /**
     * attempts to change the prj instance at all cost.
     * @throws ServerError 
     */
    protected void single(ServiceFactoryPrx u, boolean ok) throws ServerError {
        createProject(ownsfA, permsA, groupA);
        verifyDetails(prj, ownerA, groupA, permsA);

        Project t = null;
        String MSG = makeModifiedMessage();
        prj.setName(rstring(MSG));

        try {
            t = (Project) u.getUpdateService().saveAndReturnObject(prj);
            if (!ok) {
                fail("Secvio!");
            }
            assertTrue(MSG.equals(t.getName()));
        } catch (SecurityViolation sv) {
            if (ok) {
                throw sv;
            }
        }

        try {
            u.getUpdateService().deleteObject(prj);
            if (!ok) {
                fail("secvio!");
            }
        } catch (SecurityViolation sv) {
            if (ok) {
                throw sv;
            }
        }

    }

    // ~ one-to-many
    // =========================================================================

    @Override
    @Test(enabled=false, groups = { "broken", "ticket:374" })
    public void test_U_Pixels_And_U_Thumbnails() throws Exception {
        ownsfA = u;
        ownerA = user;
        groupA = user_other_group;

        ownsfB = u;
        ownerB = user;
        groupB = user_other_group;

        // RW_RW_RW / RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_RW.toString());
        oneToMany(u, true, true);
        oneToMany(o, true, true);
        oneToMany(w, true, true);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_RW_RW / RW_RW_Rx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_Rx.toString());
        oneToMany(u, true, true);
        oneToMany(o, true, true);
        oneToMany(w, true, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_RW_RW / RW_RW_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_xx.toString());
        oneToMany(u, true, true);
        oneToMany(o, true, true);
        oneToMany(w, true, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_RW_RW / RW_Rx_Rx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_Rx_Rx.toString());
        oneToMany(u, true, true);
        oneToMany(o, true, false);
        oneToMany(w, true, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_RW_RW / RW_xx_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        oneToMany(u, true, true);
        oneToMany(o, true, false);
        oneToMany(w, true, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_RW_Rx / RW_RW_Rx
        permsA = new PermissionsI(RW_RW_Rx.toString());
        permsB = new PermissionsI(RW_RW_Rx.toString());
        oneToMany(u, true, true);
        oneToMany(o, true, true);
        oneToMany(w, false, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_RW_xx / RW_RW_xx
        permsA = new PermissionsI(RW_RW_xx.toString());
        permsB = new PermissionsI(RW_RW_xx.toString());
        oneToMany(u, true, true);
        oneToMany(o, true, true);
        oneToMany(w, false, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_Rx_Rx / RW_Rx_Rx
        permsA = new PermissionsI(RW_Rx_Rx.toString());
        permsB = new PermissionsI(RW_Rx_Rx.toString());
        oneToMany(u, true, true);
        oneToMany(o, false, false);
        oneToMany(w, false, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_xx_xx / RW_xx_xx
        permsA = new PermissionsI(RW_xx_xx.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        oneToMany(u, true, true);
        oneToMany(o, false, false);
        oneToMany(w, false, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // Rx_Rx_Rx / Rx_Rx_Rx
        permsA = new PermissionsI(Rx_Rx_Rx.toString());
        permsB = new PermissionsI(Rx_Rx_Rx.toString());
        oneToMany(u, false, false);
        oneToMany(o, false, false);
        oneToMany(w, false, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // Rx_xx_xx / xx_xx_xx
        permsA = new PermissionsI(Rx_xx_xx.toString());
        permsB = new PermissionsI(xx_xx_xx.toString());
        oneToMany(u, false, false);
        oneToMany(o, false, false);
        oneToMany(w, false, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_RW_xx / RW_RW_RW
        permsA = new PermissionsI(RW_RW_xx.toString());
        permsB = new PermissionsI(RW_RW_RW.toString());
        oneToMany(u, true, true);
        oneToMany(o, true, true);
        oneToMany(w, false, true); // this should fail like U_instr_U_micro
        oneToMany(p, true, true);
        oneToMany(r, true, true);

    }

    @Override
    public void test_O_Pixels_And_U_Thumbnails() throws Exception {
        ownsfA = o;
        ownerA = other;
        groupA = user_other_group;

        ownsfB = u;
        ownerB = user;
        groupB = user_other_group;

        // RW_RW_RW / RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_RW.toString());
        oneToMany(u, true, true);
        oneToMany(o, true, true);
        oneToMany(w, true, true);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_RW_RW / RW_RW_Rx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_Rx.toString());
        oneToMany(u, true, true);
        oneToMany(o, true, true);
        oneToMany(w, true, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_RW_RW / RW_RW_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_xx.toString());
        oneToMany(u, true, true);
        oneToMany(o, true, true);
        oneToMany(w, true, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_RW_RW / RW_xx_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        oneToMany(u, true, true);
        oneToMany(o, true, false);
        oneToMany(w, true, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_RW_RW / xx_xx_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(xx_xx_xx.toString());
        oneToMany(u, true, false);
        oneToMany(o, true, false);
        oneToMany(w, true, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_xx_xx / RW_xx_xx
        permsA = new PermissionsI(RW_Rx_Rx.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        oneToMany(u, false, true);
        oneToMany(o, true, false);
        oneToMany(w, false, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // xx_xx_xx / xx_xx_xx
        permsA = new PermissionsI(Rx_Rx_Rx.toString());
        permsB = new PermissionsI(xx_xx_xx.toString());
        oneToMany(u, false, false);
        oneToMany(o, false, false);
        oneToMany(w, false, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

    }

    @Override
    public void test_U_Pixels_And_O_Thumbnails() throws Exception {
        ownsfA = u;
        ownerA = user;
        groupA = user_other_group;

        ownsfB = o;
        ownerB = other;
        groupB = user_other_group;

        // RW_RW_RW / RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_RW.toString());
        oneToMany(u, true, true);
        oneToMany(o, true, true);
        oneToMany(w, true, true);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_RW_RW / RW_RW_Rx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_Rx.toString());
        oneToMany(u, true, true);
        oneToMany(o, true, true);
        oneToMany(w, true, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_RW_RW / RW_RW_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_xx.toString());
        oneToMany(u, true, true);
        oneToMany(o, true, true);
        oneToMany(w, true, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_RW_RW / RW_xx_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        oneToMany(u, true, false);
        oneToMany(o, true, true);
        oneToMany(w, true, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_RW_RW / xx_xx_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(xx_xx_xx.toString());
        oneToMany(u, true, false);
        oneToMany(o, true, false);
        oneToMany(w, true, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_xx_xx / RW_xx_xx
        permsA = new PermissionsI(RW_Rx_Rx.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        oneToMany(u, true, false);
        oneToMany(o, false, true);
        oneToMany(w, false, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // xx_xx_xx / xx_xx_xx
        permsA = new PermissionsI(Rx_Rx_Rx.toString());
        permsB = new PermissionsI(xx_xx_xx.toString());
        oneToMany(u, false, false);
        oneToMany(o, false, false);
        oneToMany(w, false, false);
        oneToMany(p, true, true);
        oneToMany(r, true, true);
    }

    @Override
    public void test_U_Pixels_And_R_Thumbnails() throws Exception {
        ownsfA = u;
        ownerA = user;
        groupA = user_other_group;

        ownsfB = r;
        ownerB = root;
        groupB = system_group;

        // RW_RW_RW / RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_RW.toString());
        oneToMany(u, true, true);
        oneToMany(o, true, true);
        oneToMany(w, true, true);
        oneToMany(p, true, true);
        oneToMany(r, true, true);

        // RW_RW_RW / RW_RW_Rx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_Rx.toString());
        oneToMany(u, true, false);
        oneToMany(o, true, false);
        oneToMany(w, true, false);
        oneToMany(p, true, false);
        oneToMany(r, true, true);

        // RW_RW_RW / RW_RW_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_xx.toString());
        oneToMany(u, true, false);
        oneToMany(o, true, false);
        oneToMany(w, true, false);
        oneToMany(p, true, false);
        oneToMany(r, true, true);

        // RW_RW_RW / RW_xx_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        oneToMany(u, true, false);
        oneToMany(o, true, false);
        oneToMany(w, true, false);
        oneToMany(p, true, false);
        oneToMany(r, true, true);

        // RW_xx_xx / RW_xx_xx
        permsA = new PermissionsI(RW_xx_xx.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        oneToMany(u, true, false);
        oneToMany(o, false, false);
        oneToMany(w, false, false);
        oneToMany(p, true, false);
        oneToMany(r, true, true);

        // xx_xx_xx / xx_xx_xx
        permsA = new PermissionsI(xx_xx_xx.toString());
        permsB = new PermissionsI(xx_xx_xx.toString());
        oneToMany(u, false, false);
        oneToMany(o, false, false);
        oneToMany(w, false, false);
        oneToMany(p, true, false);
        oneToMany(r, true, true);

    }

    /**
     * first tries to change both the pixel and the thumbnail and the tries to
     * delete them
     */
    protected void oneToMany(ServiceFactoryPrx u, boolean pix_ok, boolean tb_ok) throws Exception {

        createPixels(ownsfA, groupA, permsA);
        createThumbnail(ownsfB, groupB, permsB, pix);
        pix = tb.getPixels();
        verifyDetails(pix, ownerA, groupA, permsA);
        verifyDetails(tb, ownerB, groupB, permsB);

        Pixels t = null;
        Thumbnail tB = null;
        String MSG = makeModifiedMessage();
        RString oldMsg = pix.getSha1();

        pix.setSha1(rstring(MSG));

        try {
        
            resetThumbnails();

            t = (Pixels) u.getUpdateService().saveAndReturnObject(pix);
            t.addThumbnail(tb); // used to update the pixel version in tb
            if (!pix_ok) {
                fail("secvio!");
            }
            assertTrue(MSG.equals(t.getSha1()));
        } catch (SecurityViolation sv) {
            // rollback
            pix.setSha1(oldMsg);
            if (pix_ok) {
                throw sv;
            }
        }

        tb.setRef(rstring(MSG));

        try {
            tB = (Thumbnail) u.getUpdateService().saveAndReturnObject(tb);
            if (!tb_ok) {
                fail("secvio!");
            }
            assertTrue(MSG.equals(tB.getRef()));
        } catch (SecurityViolation sv) {
            if (tb_ok) {
                throw sv;
            }
        }

        try {
            u.getUpdateService().deleteObject(tb);
            if (!tb_ok) {
                fail("secvio!");
            }

            // done for later recursive delete.
            pix.unloadThumbnails();
            // must be internal to try/catch otherwise, explodes
            // since tb still references the pix.
            deleteRecurisvely(u, pix);
            if (!pix_ok) {
                fail("secvio!");
            }

        } catch (SecurityViolation sv) {
            if (tb_ok && pix_ok) {
                throw sv;
            }
        }

    }

    private void resetThumbnails(Thumbnail...tbs) {
        PixelsI toCopy = new PixelsI(pix.getId(), true);
        for (Thumbnail thumbnail : tbs) {
            toCopy.addThumbnail(new ThumbnailI(thumbnail.getId(), false));            
        }
        pix.reloadThumbnails(toCopy);
    }

    // ~ unidirectional many-to-one
    // =========================================================================
    @Override
    @Test(enabled=false, groups = { "broken", "ticket:374" })
    public void test_U_Instrument_And_U_Microscope() throws Exception {

        ownsfA = u;
        ownerA = user;
        groupA = user_other_group;

        ownsfB = u;
        ownerB = user;
        groupB = user_other_group;

        // RW_RW_RW / RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_RW.toString());
        uniManyToOne(u, true, true);
        uniManyToOne(o, true, true);
        uniManyToOne(w, true, true);
        uniManyToOne(p, true, true);
        uniManyToOne(r, true, true);

        // RW_RW_RW / RW_RW_Rx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_Rx.toString());
        uniManyToOne(u, true, true);
        uniManyToOne(o, true, true);
        uniManyToOne(w, true, false);
        uniManyToOne(p, true, true);
        uniManyToOne(r, true, true);

        // RW_RW_RW / RW_RW_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_Rx.toString()); // Rx-->xx
        uniManyToOne(u, true, true);
        uniManyToOne(o, true, true);
        uniManyToOne(w, true, false); // fixme
        uniManyToOne(p, true, true);
        uniManyToOne(r, true, true);

        // RW_RW_RW / RW_Rx_Rx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_Rx_Rx.toString());
        uniManyToOne(u, true, true);
        uniManyToOne(o, true, false);
        uniManyToOne(w, true, false);
        uniManyToOne(p, true, true);
        uniManyToOne(r, true, true);

        // RW_RW_RW / RW_xx_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        uniManyToOne(u, true, true);
        uniManyToOne(o, true, false);
        uniManyToOne(w, true, false);
        uniManyToOne(p, true, true);
        uniManyToOne(r, true, true);

        // RW_RW_Rx / RW_RW_Rx
        permsA = new PermissionsI(RW_RW_Rx.toString());
        permsB = new PermissionsI(RW_RW_Rx.toString());
        uniManyToOne(u, true, true);
        uniManyToOne(o, true, true);
        uniManyToOne(w, false, false);
        uniManyToOne(p, true, true);
        uniManyToOne(r, true, true);

        // RW_RW_xx / RW_RW_xx
        permsA = new PermissionsI(RW_RW_xx.toString());
        permsB = new PermissionsI(RW_RW_xx.toString());
        uniManyToOne(u, true, true);
        uniManyToOne(o, true, true);
        uniManyToOne(w, false, false);
        uniManyToOne(p, true, true);
        uniManyToOne(r, true, true);

        // RW_Rx_Rx / RW_Rx_Rx
        permsA = new PermissionsI(RW_Rx_Rx.toString());
        permsB = new PermissionsI(RW_Rx_Rx.toString());
        uniManyToOne(u, true, true);
        uniManyToOne(o, false, false);
        uniManyToOne(w, false, false);
        uniManyToOne(p, true, true);
        uniManyToOne(r, true, true);

        // RW_xx_xx / RW_xx_xx
        permsA = new PermissionsI(RW_xx_xx.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        uniManyToOne(u, true, true);
        uniManyToOne(o, false, false);
        uniManyToOne(w, false, false);
        uniManyToOne(p, true, true);
        uniManyToOne(r, true, true);

        // Rx_Rx_Rx / Rx_Rx_Rx
        permsA = new PermissionsI(Rx_Rx_Rx.toString());
        permsB = new PermissionsI(Rx_Rx_Rx.toString());
        uniManyToOne(u, false, false);
        uniManyToOne(o, false, false);
        uniManyToOne(w, false, false);
        uniManyToOne(p, true, true);
        uniManyToOne(r, true, true);

        // Rx_xx_xx / xx_xx_xx
        permsA = new PermissionsI(Rx_xx_xx.toString());
        permsB = new PermissionsI(xx_xx_xx.toString());
        uniManyToOne(u, false, false);
        uniManyToOne(o, false, false);
        uniManyToOne(w, false, false);
        uniManyToOne(p, true, true);
        uniManyToOne(r, true, true);

    }

    protected void uniManyToOne(ServiceFactoryPrx u, boolean instr_ok,
            boolean micro_ok) throws ServerError {

        createMicroscope(ownsfB, groupB, permsB);
        createInstrument(ownsfA, groupA, permsA, micro);
        micro = instr.getMicroscope();
        verifyDetails(micro, ownerB, groupB, permsB);
        verifyDetails(instr, ownerA, groupA, permsA);

        Instrument tI = null;
        Microscope tM = null;
        String MSG = makeModifiedMessage();
        String oldMsg = micro.getModel().getValue();

        micro.setModel(rstring(MSG));

        try {
            tM = (Microscope) u.getUpdateService().saveAndReturnObject(micro);
            if (!micro_ok) {
                fail("secvio!");
            }
            assertTrue(MSG.equals(tM.getModel()));
            instr.setMicroscope(tM); // resetting to prevent version errors.
        } catch (SecurityViolation sv) {
            // rollback
            micro.setModel(rstring(oldMsg));
            if (micro_ok) {
                throw sv;
            }
        }

        Filter filter = new FilterI();
        instr.addFilter(filter);

        try {
            tI = (Instrument) u.getUpdateService().saveAndReturnObject(instr);
            if (!instr_ok) {
                fail("secvio!");
            }
            assertTrue(tI.sizeOfFilter() == 1);
        } catch (SecurityViolation sv) {
            if (instr_ok) {
                throw sv;
            }
        }

        assert tI != null;

        try {
            // done for recursive delete.
            tI.setMicroscope(null);
            deleteRecurisvely(u, tI);
            if (!instr_ok) {
                fail("secvio!");
            }

            u.getUpdateService().deleteObject(micro);
            if (!micro_ok) {
                fail("secvio!");
            }

        } catch (SecurityViolation sv) {
            if (instr_ok && micro_ok) {
                throw sv;
            }
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
        manyToMany(u, true, true, true);
        manyToMany(o, true, true, true);
        manyToMany(w, true, true, true);
        manyToMany(p, true, true, true);
        manyToMany(r, true, true, true);

        // RW_RW_RW / RW_RW_xx / RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_xx.toString());
        permsC = new PermissionsI(RW_RW_RW.toString());
        manyToMany(u, true, true, true);
        manyToMany(o, true, true, true);
        manyToMany(w, true, false, true);
        manyToMany(p, true, true, true);
        manyToMany(r, true, true, true);

        // RW_RW_RW / RW_xx_xx / RW_RW_RW
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        permsC = new PermissionsI(RW_RW_RW.toString());
        manyToMany(u, true, true, true);
        manyToMany(o, true, false, true);
        manyToMany(w, true, false, true);
        manyToMany(p, true, true, true);
        manyToMany(r, true, true, true);

    }

    /**
     * performs various write operations on linked projects and datasets.
     */
    protected void manyToMany(ServiceFactoryPrx u, boolean prj_ok, boolean ds_ok,
            boolean link_ok) throws Exception {

        createProject(ownsfA, permsA, groupA);
        createDataset(ownsfB, permsB, groupB);
        createPDLink(ownsfC, permsC, groupC);

        verifyDetails(prj, ownerA, groupA, permsA);
        verifyDetails(ds, ownerB, groupB, permsB);
        verifyDetails(link, ownerC, groupC, permsC);

        ProjectDatasetLink testC = null;
        Dataset testB = null;
        Project test = null;
        String MSG = makeModifiedMessage();

        ds.setName(rstring(MSG));
        try {
            ds.unloadProjectLinks();
            testB = (Dataset) u.getUpdateService().saveAndReturnObject(ds);
            if (!ds_ok) {
                fail("secvio!");
            }
            assertTrue(MSG.equals(testB.getName()));
        } catch (SecurityViolation sv) {
            if (ds_ok) {
                throw sv;
            }
        }

        prj.setName(rstring(MSG));

        try {
            prj.unloadDatasetLinks();
            test = (Project) u.getUpdateService().saveAndReturnObject(prj);
            if (!prj_ok) {
                fail("secvio!");
            }
            assertTrue(MSG.equals(test.getName()));
        } catch (SecurityViolation sv) {
            if (prj_ok) {
                throw sv;
            }
        }

        // try to change the link to point to something different.
        // should be immutable!!!

        try {
            u.getUpdateService().deleteObject( // proxy needed
                    new ProjectDatasetLinkI(link.getId(), false));
            if (!link_ok) {
                fail("secvio!");
            }
            prj.unloadDatasetLinks();
            ds.unloadProjectLinks();

        } catch (SecurityViolation sv) {
            if (link_ok) {
                throw sv;
            }
        }

        try {
            deleteRecurisvely(u, ds);
            if (!ds_ok) {
                fail("secvio!");
            }
        } catch (SecurityViolation sv) {
            if (ds_ok) {
                throw sv;
            }
        }

        try {
            deleteRecurisvely(u, prj);
            if (!prj_ok) {
                fail("secvio!");
            }
        } catch (SecurityViolation sv) {
            if (prj_ok) {
                throw sv;
            }
        }

    }

    // ~ Special: "tag" (e.g. Image/Pixels)
    // =========================================================================

    @Override
    @Test(enabled=false)
    public void test_U_Image_U_Pixels() throws Exception {
        ownsfA = ownsfB = u;
        ownerA = ownerB = user;
        groupA = groupB = user_other_group;

        // RW_RW_RW / RW_RW_RW
        permsA = permsB = new PermissionsI(RW_RW_RW.toString());
        imagePixels(u, true, true);
        imagePixels(o, true, true);
        imagePixels(w, true, true);
        imagePixels(p, true, true);
        imagePixels(r, true, true);

        // RW_RW_RW / RW_RW_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_RW_xx.toString());
        imagePixels(u, true, true);
        imagePixels(o, true, true);
        imagePixels(w, true, false);
        imagePixels(p, true, true);
        imagePixels(r, true, true);

        // RW_RW_RW / RW_xx_xx
        permsA = new PermissionsI(RW_RW_RW.toString());
        permsB = new PermissionsI(RW_xx_xx.toString());
        imagePixels(u, true, true);
        imagePixels(o, true, false);
        imagePixels(w, true, false);
        imagePixels(p, true, true);
        imagePixels(r, true, true);

    }

    protected void imagePixels(ServiceFactoryPrx w, boolean img_ok, boolean pix_ok) throws Exception {
        createPixels(ownsfB, groupB, permsB);
        verifyDetails(pix, ownerB, groupB, permsB);

        createImage(ownsfA, groupA, permsA, pix);
        verifyDetails(img, ownerA, groupA, permsA);

        String outerJoin = "select i from Image i "
                + "left outer join fetch i.pixels " + "where i.id = :id";
        Parameters params = new ParametersI().addId(img.getId().getValue());

        Image test = (Image) w.getQueryService().findByQuery(outerJoin, params);
        if (img_ok) {
            assertNotNull(test);
            if (pix_ok) {
                assertNotNull(test.getPrimaryPixels());
                assertTrue(test.sizeOfPixels() > 0);
            } else {
                assertTrue(test.sizeOfPixels() == 0); // TODO should it be
                // null?
            }

        } else {
            assertNull(test);
        }

    }

    // ~ Helpers
    // =========================================================================

    /**
     * WARNING: This method is for testing purposes ONLY!
     * 
     * It will need to eventually use meta-data to get the one-to-manys and
     * many-to-ones right.
     * 
     * @DEV.TODO Need to move to common helper
     */
    @Test(enabled = false)
    public static void deleteRecurisvely(ServiceFactoryPrx u, IObject target) {
        // Deleting all links to target
        fail("IMPLEMENT: ticket:1478");
        /*
        Set<String> fields = target.fields();
        for (String field : fields) {
            Object obj = target.retrieve(field);
            if (obj instanceof IObject && !(obj instanceof IEnum)) {

                IObject iobj = (IObject) obj;
                // deleteRecurisvely(sf, iobj);
            } else if (obj instanceof Collection) {
                Collection coll = (Collection) obj;
                for (Object object : coll) {
                    if (object instanceof IObject) {
                        IObject iobj = (IObject) object;
                        deleteRecurisvely(u, iobj);
                    }
                }
            }
        }
        // Now actually delete target
        u.getUpdateService().deleteObject(target);
        */
    }
}

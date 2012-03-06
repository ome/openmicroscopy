/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.perms42;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import ome.api.ThumbnailStore;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.internal.Permissions;
import ome.model.meta.ExperimenterGroup;
import ome.security.basic.CurrentDetails;

/**
 * Test of the omero.group functionality in Beta4.4. This adds on to the 4.2/4.3
 * permission system by allowing querying among several groups at one time.
 *
 * @since Beta-4.4.0
 *
 */
@Test(groups = "ticket:3529")
public class AllGroupsTest extends PermissionsTest {

    @AfterMethod
    public void resetLoginAop() {
        loginAop.callContext = null;
    }

    // Simples reads
    // =========================================================================

    public void testAdminAllGroups() {
        setup(Permissions.GROUP_READABLE);
        fixture.make_admin();
        assertRead();
    }

    public void testNonAdminAllGroups() {
        setup(Permissions.GROUP_READABLE);
        // fixture.make_admin(); Non-admin
        assertRead();
    }

    protected void assertRead() {
        // Create an image in the default group
        Image img = fixture.saveImage();

        // Change group and try to load
        ExperimenterGroup group = fixture.new_group();
        fixture.use_group(group);
        assertCantLoad(img);

        // Try to load using omero.group:-1
        loginAop.callContext = grpctx(-1L);
        assertCanLoad(img);
    }

    // Thumbnail usage
    // =========================================================================

    @Test(enabled = false) // Not yet supported.
    public void testThumbInAllShares() {
        setup(Permissions.GROUP_READABLE);
        assertThumb(sharectx(-1L));
    }

    public void testThumbInAllGroups() {
        setup(Permissions.GROUP_READABLE);
        assertThumb(grpctx(-1L));
    }

    protected void assertThumb(Map<String, String> ctx) {
        Pixels pix = makePixels();

        ThumbnailStore tb = factory.createThumbnailService();
        loginAop.callContext = ctx;

        if (!tb.setPixelsId(pix.getId())) {
            tb.resetDefaults();
            tb.setPixelsId(pix.getId());
        }
        tb.getThumbnail(64, 64);
    }

    // Helpers
    // =========================================================================

    CurrentDetails cd() {
        return applicationContext.getBean(CurrentDetails.class);
    }

    Map<String, String> grpctx(long id) {
       HashMap<String, String> map = new HashMap<String, String>();
       map.put("omero.group", ""+id);
       return map;
    }


    Map<String, String> sharectx(long id) {
       HashMap<String, String> map = new HashMap<String, String>();
       map.put("omero.share", ""+id);
       return map;
    }

    protected void assertCanLoad(IObject obj) {
        assertNotNull(lookup(obj));
    }

    protected void assertCantLoad(IObject obj) {
        try {
            lookup(obj);
            fail("secvio");
        } catch (SecurityViolation sv) {
            // ok.
        }
    }

}

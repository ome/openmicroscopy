/*
 *   Copyright 2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import org.testng.Assert;
import omero.model.CommentAnnotationI;
import omero.model.DetailsI;
import omero.model.PermissionsI;

/**
 * Tests for the updated group permissions of 4.3 and 4.4.
 *
 * @since 4.4.0
 */
public class PermissionsTest extends AbstractServerTest {

    // chmod
    // ==============================================

    /*
     * See #8277 permissions returned from the server should now be immutable.
     */
    public void testImmutablePermissions() throws Exception {

        // Test on the raw object
        PermissionsI p = new omero.model.PermissionsI();
        p.ice_postUnmarshal();
        try {
            p.setPerm1(1);
            Assert.fail("throw!");
        } catch (omero.ClientError err) {
            // good
        }

        // and on one returned from the server
        CommentAnnotationI c = new omero.model.CommentAnnotationI();
        c = (CommentAnnotationI) this.iUpdate.saveAndReturnObject(c);
        p = (PermissionsI) c.getDetails().getPermissions();
        try {
            p.setPerm1(1);
        } catch (omero.ClientError err) {
            // good
        }
    }

    public void testDisallow() {
        PermissionsI p = new omero.model.PermissionsI();
        Assert.assertTrue(p.canAnnotate());
        Assert.assertTrue(p.canEdit());
    }

    public void testClientSet() throws Exception {
        CommentAnnotationI c = new omero.model.CommentAnnotationI();
        c = (CommentAnnotationI) this.iUpdate.saveAndReturnObject(c);
        DetailsI d = (DetailsI) c.getDetails();
        Assert.assertNotNull(d.getClient());
        Assert.assertNotNull(d.getSession());
        Assert.assertNotNull(d.getCallContext());
        Assert.assertNotNull(d.getEventContext());
    }

}

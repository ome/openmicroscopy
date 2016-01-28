/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests.perms42;

import java.util.List;

import ome.conditions.SecurityViolation;
import ome.model.annotations.ImageAnnotationLink;
import ome.model.annotations.TagAnnotation;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

import org.testng.annotations.Test;

/**
 * Test of the re-enabled group permissions in Beta4.2
 *
 * @since Beta-4.2.0
 *
 */
@Test(groups = "ticket:1434")
public class GraphConsistencyTest extends PermissionsTest {


    //
    // Bugs
    //

    @Test
    public void testAdminOrPiInPrivateGroup() throws Exception {
        setup(Permissions.PRIVATE);
        fixture.make_leader();

        // INSERT
        Image i = fixture.saveImage();

        // UPDATE
        i.setName(uuid());
        i = iUpdate.saveAndReturnObject(i);

        // DELETE
        iUpdate.deleteObject(i);

        // Another user creates an image
        Experimenter e2 = loginNewUserInOtherUsersGroup(fixture.user);
        i = fixture.saveImage();

        // NO UPDATE
        fixture.log_in();
        i.setName(uuid());
        try {
            iUpdate.saveObject(i);
            fail("sec-vio");
        } catch (SecurityViolation sv) {
            // ok
        }

        // NO LINK
        loginUser(e2.getOmeName(), fixture.groupName);
        i = fixture.saveImage();
        fixture.log_in();
        try {
            ImageAnnotationLink link = new ImageAnnotationLink();
            link.link(i.proxy(), new TagAnnotation());
            iUpdate.saveObject(link);
            fail("sec-vio");
        } catch (SecurityViolation sv) {
            // ok
        }

    }

    //
    // Guarantee consistent graphs on read
    //

    @Test
    public void testReadOnlyReturnsCurrentGroup() throws Exception {

        // Create user/group/image
        setup(Permissions.GROUP_READABLE);
        Image i = fixture.saveImage();

        // Now create another group for that user and another image.
        ExperimenterGroup g = loginUserInNewGroup(fixture.user);
        Image i2 = fixture.saveImage();

        // Now get all images that belong to the user
        List<Image> images = allImagesForFixtureUser();
        assertEquals(1, images.size());

    }



    @Test
    public void testReadOnlyReturnsCurrentGroupEventForRoot() throws Exception {
        testReadOnlyReturnsCurrentGroup();
        loginRoot();
        // Logged into "system"
        List<Image> images = allImagesForFixtureUser();
        assertEquals(0, images.size());
        // Logged into the fixture group
        fixture.use_fixture_group();
        images = allImagesForFixtureUser();
        assertEquals(1, images.size());

    }

    //
    // Guarantee consistent graphs on write
    //

    @Test
    public void testUserInTwoGroupsCantMixWithLink() throws Exception {

        // Create an image as one user (one group)
        setup(Permissions.GROUP_PRIVATE);
        Image i = fixture.saveImage();

        // Create an image as another fixture
        Fixture fixture2 = new Fixture(Permissions.GROUP_PRIVATE);
        fixture2.log_in();

        // Be sure to add the new user to the previous user's group
        iAdmin.addGroups(fixture2.user, fixture.group());

        Dataset d = new Dataset("ticket:1434");
        d.linkImage(i);
        try {
            // Do we even need the check for mixed graphs on write,
            // if the read is disabled, then it won't be possible to
            // load the object for linking?
            d = iUpdate.saveAndReturnObject(d);
            fail("Mixed group should not allowed!");
        } catch (SecurityViolation sv) {
            // good
        }

    }

    //
    // Configurable default permissions removed in favor of using group
    //

    @Test
    public void testObjectCreatedWithGroupPermissions() throws Exception {
        setup(Permissions.USER_PRIVATE);
        Image image = fixture.saveImage();
        Permissions groupPermissions = fixture.group().getDetails()
                .getPermissions();
        Permissions imagePermissions = image.getDetails().getPermissions();
        assertTrue(groupPermissions.identical(imagePermissions));

    }

    @Test
    public void testObjectCreatedWithGroupPermissionsMinusUmask()
            throws Exception {
        setup(Permissions.USER_PRIVATE);
        fail();
    }

    //
    // Helpers
    //

    private List<Image> allImagesForFixtureUser() {
        List<Image> images = iQuery.findAllByQuery(
                "select i from Image i where i.details.owner.id = "
                        + fixture.user.getId(), null);
        return images;
    }
}

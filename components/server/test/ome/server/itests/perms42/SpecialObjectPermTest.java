/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.server.itests.perms42;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.api.IAdmin;
import ome.conditions.GroupSecurityViolation;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.ExperimenterAnnotationLink;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.TagAnnotation;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Parameters;

import org.testng.annotations.Test;

/**
 * Tests the creation of and linkage to special objects such as scripts and user
 * photos. The intent is that there is a space which certain objects can be placed
 * so that they are visible from all contexts (i.e. regardless of the current
 * active group)
 *
 * @since Beta-4.2.0
 */
@Test(groups = { "ticket:1434", "ticket:1784", "ticket:1791", "ticket:1794" })
public class SpecialObjectPermTest extends PermissionsTest {

    //
    // "user" group
    //

    @Test(expectedExceptions = SecurityViolation.class)
    public void testUserCantPlacePublicDataInUser() {
        setup(Permissions.PRIVATE);
        assertTag(uuid(), roles.getUserGroupId(), Permissions.PUBLIC);
    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testUsersCantPlaceDataInSystemGroup() {
        setup(Permissions.PRIVATE);
        assertTag(uuid(), roles.getSystemGroupId(), Permissions.PUBLIC);
    }

    @Test
    public void testAdminUsersCanCreateInUser() {
        setup(Permissions.PRIVATE);
        fixture.make_admin();
        assertTag(uuid(), roles.getUserGroupId(), Permissions.PUBLIC);
    }

    @Test
    public void testAndForTheMomentThePermissionsOnThatDataIsUnimportant() {
        setup(Permissions.PRIVATE);
        fixture.make_admin();
        assertTag(uuid(), roles.getUserGroupId(), Permissions.PRIVATE);
    }

    @Test(expectedExceptions = GroupSecurityViolation.class)
    public void testAndDataCantBeMovedIntoTheUserGroupAfterTheFactManually() {
        setup(Permissions.PRIVATE);
        Image i = fixture.saveImage();
        String gname = fixture.group().getName();

        setup(Permissions.PRIVATE); // New fixture for admin
        fixture.make_admin();
        login(fixture.user.getOmeName(), gname, "User");

        i.getDetails().setGroup(new ExperimenterGroup(roles.getUserGroupId(), false));
        i = iUpdate.saveAndReturnObject(i);
    }

    @Test
    public void testAndDataCanBeMovedIntoTheUserGroupAfterTheFactOrByIGroup() {
        setup(Permissions.PRIVATE);
        Image i = fixture.saveImage();
        String gname = fixture.group().getName();

        setup(Permissions.PRIVATE); // New fixture for admin
        fixture.make_admin();
        login(fixture.user.getOmeName(), gname, "User");
        // FIXME: could possibly login to the group automatically in moveTo()

        iAdmin.moveToCommonSpace(i);

    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testButNotByANonAdminNonPiEventIfDataVisible() {
        setup(Permissions.WORLD_WRITEABLE);
        Image i = fixture.saveImage();
        String gname = fixture.group().getName();

        setup(Permissions.PRIVATE); // New fixture for admin
        // OMIT THIS STEP fixture.make_admin();
        login(fixture.user.getOmeName(), gname, "User");
        // FIXME: could possibly login to the group automatically in moveTo()

        iAdmin.moveToCommonSpace(i);
    }

    //
    // "system" group
    //

    @Test
    public void testSystemCanPutPrivateDataInSystem() {
        login("root", "system", "User");
        TagAnnotation t = assertTag(uuid(), roles.getSystemGroupId(), Permissions.PRIVATE);
        // can't watch for the NPE, since admin's can read each others private data
        assertPrivate(t);
    }

    @Test
    public void testByDefaultTheSystemGroupIsPrivate() {
        login("root", "system", "User");
        TagAnnotation tag = new TagAnnotation();
        tag = iUpdate.saveAndReturnObject(tag);
        assertPrivate(tag);
    }

    //
    // misc
    //

    @Test
    public void testNewUserPhotoIsInUserGroup() {
        setup(Permissions.PRIVATE);
        iAdmin.uploadMyUserPhoto("foo", "image/jpeg", new byte[]{0,0,0,0});
        List<OriginalFile> fotos = iAdmin.getMyUserPhotos();
        OriginalFile foto = fotos.get(0);
        assertUserGroup(foto);
        // and the next version
        iAdmin.uploadMyUserPhoto("foo", "image/jpeg", new byte[]{0,0,0,0});
        fotos = iAdmin.getMyUserPhotos();
        fotos.get(0);
        assertUserGroup(foto);

    }

    private void assertUserGroup(IObject obj) {
        assertEquals(Long.valueOf(roles.getUserGroupId()), obj.getDetails().getGroup().getId());
    }

    /**
     * Previously the user was expected to do this manually. As of #1794, this
     * is no longer allowed, and must be done server side. See
     * {@link IAdmin#uploadMyUserPhoto(String, String, byte[])}
     */
    @Test(expectedExceptions = SecurityViolation.class)
    public void testUserCreatesImageInUserGroup() {

        final Long ugid = roles.getUserGroupId();

        setup(Permissions.PRIVATE);

        // Create an image in the "user" group
        FileAnnotation fa = new FileAnnotation();
        fa.setNs("my photo");
        fa.getDetails().setGroup(new ExperimenterGroup(ugid, false));
        fa = iUpdate.saveAndReturnObject(fa);

        // Make sure it belongs to the "user" group
        assertEquals(ugid, fa.getDetails().getGroup().getId());

        // Make sure we can load it
        iQuery.get(FileAnnotation.class, fa.getId());
        loadUserAnnotations(0);

        // Now link it to the user object
        ExperimenterAnnotationLink link = new ExperimenterAnnotationLink();
        link.link(fixture.user, fa);
        link.getDetails().setGroup(new ExperimenterGroup(ugid, false));
        iUpdate.saveObject(link);

        // And if we change groups we'll be able to load it?
        loginNewUser();
        loadUserAnnotations(1);

    }

    @Test(enabled = false, groups = "ticket:1798")
    public void test1798LinkNotInUserGroup() {

        final Long ugid = roles.getUserGroupId();

        setup(Permissions.PRIVATE);

        // Create an image in the "user" group
        FileAnnotation fa = new FileAnnotation();
        fa.setNs("my photo");
        fa.getDetails().setGroup(new ExperimenterGroup(ugid, false));
        fa = iUpdate.saveAndReturnObject(fa);

        // Make sure it belongs to the "user" group
        assertEquals(ugid, fa.getDetails().getGroup().getId());

        // Make sure we can load it
        iQuery.get(FileAnnotation.class, fa.getId());
        loadUserAnnotations(0);

        // Now link it to the user object
        ExperimenterAnnotationLink link = new ExperimenterAnnotationLink();
        link.link(fixture.user, fa);
        iUpdate.saveObject(link);

        // And if we change groups we'll be able to load it?
        loginNewUser();
        loadUserAnnotations(1);

    }

    // Helpers
    // =========================================================================

    private void loadUserAnnotations(int size) {
        Map<Long, Set<Annotation>> map =
            iMetadata.loadAnnotations(Experimenter.class,
                Collections.singleton(fixture.user.getId()),
                Collections.singleton(FileAnnotation.class.getName()),
                null, null);
        Set<Annotation> anns = map.get(fixture.user.getId());
        assertEquals(size, anns.size());
    }


    private TagAnnotation findTag(String uuid) {
        Parameters p = new Parameters().addString("uuid", uuid);
        return iQuery.findByQuery("select ta from TagAnnotation " +
                "ta where ta.textValue = :uuid", p);
    }


    private TagAnnotation assertTag(String uuid, long groupId, Permissions p) {
        assertEquals(null, findTag(uuid));
        TagAnnotation tag = new TagAnnotation();
        tag.setTextValue(uuid);
        tag.getDetails().setGroup(new ExperimenterGroup(groupId, false));
        tag.getDetails().setPermissions(p);
        tag = iUpdate.saveAndReturnObject(tag);

        // Now as another user in the same group let's try to find that
        Experimenter e =
            iAdmin.getExperimenter(iAdmin.getEventContext().getCurrentUserId());
        loginNewUserInOtherUsersGroup(e);
        TagAnnotation t = findTag(uuid);
        assertEquals(tag.getId(), t.getId()); // Not logged into user group
        return t;
    }


}

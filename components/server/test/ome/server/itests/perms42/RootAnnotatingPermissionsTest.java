/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.perms42;

import ome.conditions.GroupSecurityViolation;
import ome.model.annotations.ImageAnnotationLink;
import ome.model.annotations.TagAnnotation;
import ome.model.core.Image;
import ome.model.internal.Permissions;
import ome.model.meta.Experimenter;
import ome.services.util.Executor;
import ome.system.ServiceFactory;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

/**
 * For a private group, any annotations, thumbnails, or similar from root could
 * cause inconsistent graphs.
 *
 * @since Beta-4.2.0
 *
 */
@Test(groups = "ticket:1769")
public class RootAnnotatingPermissionsTest extends PermissionsTest {

    /**
     * Tests the method which determines whether or not a particular security
     * context has the possibility of corrupting consistent graphs.
     */
    @Test
    public void testAdminCorruptionMethod() throws Exception {
        setup(Permissions.USER_PRIVATE);
        fixture.log_in();
        assertGraphCriticalIs(false); // own, should always be ok.
        loginRootKeepGroup();
        assertGraphCriticalIs(true);

        Experimenter piToBe = loginNewUserInOtherUsersGroup(fixture.user);
        assertGraphCriticalIs(false); // Not a PI yet, so can't read
        makeGroupOwnerAndLogin(piToBe);
        assertGraphCriticalIs(true);

        setup(Permissions.GROUP_READABLE);
        fixture.log_in();
        assertGraphCriticalIs(false); // own, should always be ok.
        loginRootKeepGroup();
        assertGraphCriticalIs(false); // object will belong to the group
        iAdmin.addGroups(new Experimenter(0L, false), fixture.group());
        assertGraphCriticalIs(false); // root in shared group, ok.

        Experimenter pi2 = loginNewUserInOtherUsersGroup(fixture.user);
        assertGraphCriticalIs(false); // group-read, ok
        makeGroupOwnerAndLogin(pi2);
        assertGraphCriticalIs(false); // pi ok.

    }

    private void makeGroupOwnerAndLogin(Experimenter piToBe) {
        loginRootKeepGroup();
        iAdmin.setGroupOwner(fixture.group(), piToBe);
        loginUser(piToBe.getOmeName(), fixture.groupName);
    }

    private void assertGraphCriticalIs(boolean value) {
        final boolean[] rv = new boolean[1];
        executor.execute(loginAop.p, new Executor.SimpleWork(this, "isGraphCritical"){
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                rv[0] = securitySystem.isGraphCritical(null); // may throw
                return null;
            }});
        assertEquals(value, rv[0]);
    }

    @Test
    public void testRootCreatedObjectInSharedGroupBelongsToGroup() {
        setup(Permissions.USER_PRIVATE);
        loginRootKeepGroup();
        Image image = fixture.saveImage();
        assertEquals(fixture.group().getId(), image.getDetails().getGroup().getId());
    }

    @Test
    public void testPrivateGroupWithRoot() throws Exception {
        setup(Permissions.USER_PRIVATE);
        Image image = fixture.saveImage();
        loginRootKeepGroup();
        assertNumberOfImages(1);
        TagAnnotation tag = new TagAnnotation();
        ImageAnnotationLink link = new ImageAnnotationLink();
        link.link(new Image(image.getId(), false), tag);
        try {
            link = iUpdate.saveAndReturnObject(link);
            fail("group-security-violation");
        } catch (GroupSecurityViolation gsv) {
            // ok
        }
        assertNumberOfImages(1);
        assertEquals(1, iQuery.findAllByQuery(
                "select i from Image i left outer join fetch i.annotationLinks", null)
                .size());
    }

    private void assertNumberOfImages(int count) {
        assertEquals(count, iQuery.findAll(Image.class, null).size());
    }

}

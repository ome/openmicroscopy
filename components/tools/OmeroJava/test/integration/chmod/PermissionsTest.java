/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package integration.chmod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ome.model.internal.Permissions;
import ome.util.Utils;
import omero.ServerError;
import omero.cmd.Chmod2;
import omero.cmd.Delete2;
import omero.model.Annotation;
import omero.model.CommentAnnotationI;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.RectI;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.sys.EventContext;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

import integration.AbstractServerTest;

/**
 * Tests that only appropriate users may use {@link Chmod2} and that others' data is then deleted only when appropriate.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.1
 */
public class PermissionsTest extends AbstractServerTest {

    private final List<Long> testImages = Collections.synchronizedList(new ArrayList<Long>());

    private ExperimenterGroup systemGroup;
    private EventContext userOtherGroup, adminOtherGroup;

    /**
     * Set up admin and non-admin users who are not a member of the groups created by tests.
     * @throws Exception unexpected
     */
    @BeforeClass
    public void setupOtherGroup() throws Exception {
        systemGroup = new ExperimenterGroupI(iAdmin.getSecurityRoles().systemGroupId, false);
        userOtherGroup = newUserAndGroup("rw----");
        final ExperimenterGroup group = new ExperimenterGroupI(userOtherGroup.groupId, false);
        adminOtherGroup = newUserInGroup(group, false);
        addUsers(systemGroup, Collections.singletonList(adminOtherGroup.userId), false);
    }

    /**
     * Clear the list of test images.
     */
    @BeforeClass
    public void clearTestImages() {
        testImages.clear();
    }

    /**
     * Delete the test images then clear the list.
     * @throws Exception unexpected
     */
    @AfterClass
    public void deleteTestImages() throws Exception {
        final Delete2 delete = new Delete2();
        delete.targetObjects = ImmutableMap.of("Image", testImages);
        doChange(root, root.getSession(), delete, true);
        clearTestImages();
    }

    /**
     * Add a comment and a ROI to the given image.
     * @param imageId an image ID
     * @return the new model objects
     * @throws ServerError unexpected
     */
    private List<IObject> annotateImage(long imageId) throws ServerError {
        final List<IObject> annotationObjects = new ArrayList<IObject>();
        final Image image = (Image) iQuery.get("Image", imageId);

        for (final Annotation annotation : new Annotation[] {new CommentAnnotationI(), new TagAnnotationI()}) {
            ImageAnnotationLink link = new ImageAnnotationLinkI();
            link.setParent((Image) image.proxy());
            link.setChild(annotation);
            link = (ImageAnnotationLink) iUpdate.saveAndReturnObject(link);
            annotationObjects.add(link.proxy());
            annotationObjects.add(link.getChild().proxy());
        }

        Roi roi = new RoiI();
        roi.addShape(new RectI());
        roi.setImage((Image) image.proxy());
        roi = (Roi) iUpdate.saveAndReturnObject(roi);
        annotationObjects.add(roi.proxy());
        annotationObjects.add(roi.getShape(0).proxy());

        return annotationObjects;
    }

    /**
     * Assert that the given object still exists or not.
     * @param object a model object
     * @param isExists if the object should now exist
     * @throws ServerError unexpected
     */
    private void assertExistence(IObject object, boolean isExists) throws ServerError {
        assertExistence(Collections.singleton(object), isExists);
    }

    /**
     * Assert that the given objects still exist or not.
     * @param objects some model objects
     * @param isExists if the objects should now exist
     * @throws ServerError unexpected
     */
    private void assertExistence(Collection<IObject> objects, boolean isExists) throws ServerError {
        for (final IObject object : objects) {
            final String objectName = object.getClass().getName() + '[' + object.getId().getValue() + ']';
            final IObject retrieved = iQuery.find(object.getClass().getName(), object.getId().getValue());
            if (isExists) {
                Assert.assertNotNull(retrieved, objectName);
            } else {
                Assert.assertNull(retrieved, objectName);
            }
        }
    }

    /**
     * Test that a specific case of using {@link Chmod2} behaves as expected.
     * @param isGroupOwner if the user submitting the {@link Chmod2} request is an owner of the target group
     * @param isGroupMember if the user submitting the {@link Chmod2} request is a member of the target group
     * @param isDataOwner if the user submitting the {@link Chmod2} request owns the data in the group
     * @param isAdmin if the user submitting the {@link Chmod2} request is a member of the system group
     * @param fromPerms the permissions on the group before the chmod
     * @param toPerms the permissions on the group after the chmod
     * @param isExpectSuccess if the chmod is expected to succeed
     * @param isExpectDeleteOther if the chmod is expected to cause another user's annotations to be removed from a user's image
     * @throws Exception unexpected
     */
    @Test(dataProvider = "chmod test cases")
    public void testChmod(boolean isGroupOwner, boolean isGroupMember, boolean isDataOwner, boolean isAdmin,
            String fromPerms, String toPerms, boolean isExpectSuccess, boolean isExpectDeleteOther) throws Exception {

        /* set up the users and group for this test case */

        final EventContext importer, annotator, chmodder;
        final ExperimenterGroup dataGroup;

        importer = newUserAndGroup(fromPerms, isGroupOwner && isDataOwner);

        final long dataGroupId = importer.groupId;
        dataGroup = new ExperimenterGroupI(dataGroupId, false);

        if (isDataOwner) {
            chmodder = importer;
        } else if (isGroupMember) {
            chmodder = newUserInGroup(dataGroup, isGroupOwner);
        } else {
            chmodder = isAdmin ? adminOtherGroup : userOtherGroup;
        }

        if (isAdmin && chmodder != adminOtherGroup) {
            addUsers(systemGroup, Collections.singletonList(chmodder.userId), false);
        }

        if ("rwra--".equals(fromPerms)) {
            if (isGroupMember && !isDataOwner) {
                annotator = chmodder;
            } else {
                annotator = newUserInGroup(dataGroup, false);
            }
        } else {
            annotator = null;
        }

        /* note which objects were used to annotate an image */

        final List<IObject> ownerAnnotations, otherAnnotations;

        /* import and annotate an image */

        init(importer);
        final Image image = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage()).proxy();
        final long imageId = image.getId().getValue();
        testImages.add(imageId);
        ownerAnnotations = annotateImage(imageId);
        disconnect();

        /* perhaps have another user annotate the image */

        if (annotator == null) {
            otherAnnotations = Collections.<IObject>emptyList();
        } else {
            init(annotator);
            otherAnnotations = annotateImage(image.getId().getValue());
            disconnect();
        }

        /* perform the chmod */

        init(chmodder);
        final Chmod2 chmod = new Chmod2();
        chmod.targetObjects = ImmutableMap.of("ExperimenterGroup", Collections.singletonList(dataGroupId));
        chmod.permissions = toPerms;
        doChange(client, factory, chmod, isExpectSuccess);
        disconnect();

        if (!isExpectSuccess) {
            return;
        }

        /* check that the group's permissions are as requested */

        logRootIntoGroup(dataGroupId);
        final ExperimenterGroup changedGroup = (ExperimenterGroup) iQuery.get("ExperimenterGroup", dataGroupId);
        final long actualPermissions = changedGroup.getDetails().getPermissions().getPerm1();
        final long expectedPermissions = (Long) Utils.internalForm(Permissions.parseString(toPerms));
        Assert.assertEquals(actualPermissions, expectedPermissions);

        /* pluck the tag from the other user's annotations */

        final TagAnnotation tag;

        if (annotator == null) {
            tag = null;
        } else {
            final Iterator<IObject> annotationIterator = otherAnnotations.iterator();
            while (true) {
                final IObject annotation = annotationIterator.next();
                if (annotation instanceof TagAnnotation) {
                    annotationIterator.remove();
                    tag = (TagAnnotation) annotation;
                    break;
                }
            }
        }

        /* check that exactly the expected object deletions have occurred */

        assertExistence(image, true);
        if (tag != null) {
            assertExistence(tag, true);
        }
        assertExistence(ownerAnnotations, true);
        assertExistence(otherAnnotations, !isExpectDeleteOther);
        disconnect();
    }

    /**
     * @return a specific test case for chmod
     */
    @DataProvider(name = "chmod test cases (debug)")
    public Object[][] provideChmodCaseDebug() {
        int index = 0;
        final int IS_GROUP_OWNER = index++;
        final int IS_GROUP_MEMBER = index++;
        final int IS_DATA_OWNER = index++;
        final int IS_ADMIN = index++;
        final int FROM_PERMS = index++;
        final int TO_PERMS = index++;
        final int IS_EXPECT_SUCCESS = index++;
        final int IS_EXPECT_DELETE_OTHER = index++;

        final List<Object[]> testCases = new ArrayList<Object[]>();

        final Object[] testCase = new Object[index];
        testCase[IS_GROUP_OWNER] = true;
        testCase[IS_GROUP_MEMBER] = true;
        testCase[IS_DATA_OWNER] = true;
        testCase[IS_ADMIN] = false;
        testCase[FROM_PERMS] = "rwra--";
        testCase[TO_PERMS] = "rw----";
        testCase[IS_EXPECT_SUCCESS] = true;
        testCase[IS_EXPECT_DELETE_OTHER] = true;
        testCases.add(testCase);

        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return a variety of test cases for chmod
     */
    @DataProvider(name = "chmod test cases")
    public Object[][] provideChmodCases() {
        int index = 0;
        final int IS_GROUP_OWNER = index++;
        final int IS_GROUP_MEMBER = index++;
        final int IS_DATA_OWNER = index++;
        final int IS_ADMIN = index++;
        final int FROM_PERMS = index++;
        final int TO_PERMS = index++;
        final int IS_EXPECT_SUCCESS = index++;
        final int IS_EXPECT_DELETE_OTHER = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwra--"};

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final boolean isGroupOwner : booleanCases) {
            for (final boolean isGroupMember : booleanCases) {
                for (final boolean isDataOwner : booleanCases) {
                    for (final boolean isAdmin : booleanCases) {
                        if (!isGroupMember && (isGroupOwner || isDataOwner)) {
                            /* test case does not make sense */
                            continue;
                        }

                        for (final String fromPerms : permsCases) {
                            for (final String toPerms : permsCases) {
                                final Object[] testCase = new Object[index];
                                testCase[IS_GROUP_OWNER] = isGroupOwner;
                                testCase[IS_GROUP_MEMBER] = isGroupOwner;
                                testCase[IS_DATA_OWNER] = isDataOwner;
                                testCase[IS_ADMIN] = isAdmin;
                                testCase[FROM_PERMS] = fromPerms;
                                testCase[TO_PERMS] = toPerms;
                                testCase[IS_EXPECT_SUCCESS] = isAdmin || isGroupOwner;
                                testCase[IS_EXPECT_DELETE_OTHER] = "rwra--".equals(fromPerms) && "rw----".equals(toPerms);
                                testCases.add(testCase);
                            }
                        }
                    }
                }
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }
}

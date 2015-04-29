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

package integration.chown;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import omero.RLong;
import omero.RType;
import omero.ServerError;
import omero.cmd.Chown2;
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
 * Tests that only appropriate users may use {@link Chown2} and that others' data is left unchanged.
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
     * Assert that the given object is owned by the given owner.
     * @param object a model object
     * @param expectedOwner a user's event context
     * @throws ServerError unexpected
     */
    private void assertOwnedBy(IObject object, EventContext expectedOwner) throws ServerError {
        assertOwnedBy(Collections.singleton(object), expectedOwner);
    }

    /**
     * Assert that the given objects are owned by the given owner.
     * @param objects some model objects
     * @param expectedOwner a user's event context
     * @throws ServerError unexpected
     */
    private void assertOwnedBy(Collection<IObject> objects, EventContext expectedOwner) throws ServerError {
        for (final IObject object : objects) {
            final String objectName = object.getClass().getName() + '[' + object.getId().getValue() + ']';
            final String query = "SELECT details.owner.id FROM " + object.getClass().getSuperclass().getSimpleName() +
                    " WHERE id = " + object.getId().getValue();
            final List<List<RType>> results = iQuery.projection(query, null);
            final long actualOwnerId = ((RLong) results.get(0).get(0)).getValue();
            Assert.assertEquals(actualOwnerId, expectedOwner.userId, objectName);
        }
    }

    /**
     * Test that a specific case of using {@link Chown2} behaves as expected.
     * @param isDataOwner if the user submitting the {@link Chown2} request owns the data in the group
     * @param isAdmin if the user submitting the {@link Chown2} request is a member of the system group
     * @param isRecipientInGroup if the user receiving data by means of the {@link Chown2} request is a member of the data's group
     * @param isExpectSuccess if the chown is expected to succeed
     * @throws Exception unexpected
     */
    @Test(dataProvider = "chown test cases")
    public void testChown(boolean isDataOwner, boolean isAdmin, boolean isRecipientInGroup, boolean isExpectSuccess)
            throws Exception {

        /* set up the users and group for this test case */

        final EventContext importer, annotator, chowner, recipient;
        final ExperimenterGroup dataGroup;

        importer = newUserAndGroup("rwra--");

        final long dataGroupId = importer.groupId;
        dataGroup = new ExperimenterGroupI(dataGroupId, false);

        recipient = isRecipientInGroup ? newUserInGroup(dataGroup, false) : userOtherGroup;

        if (isDataOwner) {
            chowner = importer;
        } else {
            chowner = isAdmin ? adminOtherGroup : recipient;
        }

        if (isAdmin && chowner != adminOtherGroup) {
            addUsers(systemGroup, Collections.singletonList(chowner.userId), false);
        }

        annotator = newUserInGroup(dataGroup, false);

        /* note which objects were used to annotate an image */

        final List<IObject> ownerAnnotations, otherAnnotations;

        /* import and annotate an image */

        init(importer);
        final Image image = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage()).proxy();
        final long imageId = image.getId().getValue();
        testImages.add(imageId);
        ownerAnnotations = annotateImage(imageId);
        disconnect();

        /* have another user annotate the image */

        init(annotator);
        otherAnnotations = annotateImage(image.getId().getValue());
        disconnect();

        /* perform the chown */

        init(chowner);
        final Chown2 chown = new Chown2();
        chown.targetObjects = ImmutableMap.of("Image", Collections.singletonList(imageId));
        chown.userId = recipient.userId;
        doChange(client, factory, chown, isExpectSuccess);
        disconnect();

        if (!isExpectSuccess) {
            return;
        }

        /* check that the objects' ownership is all as expected */

        logRootIntoGroup(dataGroupId);
        assertOwnedBy(image, recipient);
        assertOwnedBy(ownerAnnotations, recipient);
        assertOwnedBy(otherAnnotations, annotator);
        disconnect();
    }

    /**
     * @return a specific test case for chown
     */
    @DataProvider(name = "chown test cases (debug)")
    public Object[][] provideChownCaseDebug() {
        int index = 0;
        final int IS_DATA_OWNER = index++;
        final int IS_ADMIN = index++;
        final int IS_RECIPIENT_IN_GROUP = index++;
        final int IS_EXPECT_SUCCESS = index++;

        final List<Object[]> testCases = new ArrayList<Object[]>();

        final Object[] testCase = new Object[index];
        testCase[IS_DATA_OWNER] = true;
        testCase[IS_ADMIN] = true;
        testCase[IS_RECIPIENT_IN_GROUP] = true;
        testCase[IS_EXPECT_SUCCESS] = true;
        testCases.add(testCase);

        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return a variety of test cases for chown
     */
    @DataProvider(name = "chown test cases")
    public Object[][] provideChownCases() {
        int index = 0;
        final int IS_DATA_OWNER = index++;
        final int IS_ADMIN = index++;
        final int IS_RECIPIENT_IN_GROUP = index++;
        final int IS_EXPECT_SUCCESS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final boolean isDataOwner : booleanCases) {
            for (final boolean isAdmin : booleanCases) {
                for (final boolean isRecipientInGroup : booleanCases) {
                    final Object[] testCase = new Object[index];
                    testCase[IS_DATA_OWNER] = isDataOwner;
                    testCase[IS_ADMIN] = isAdmin;
                    testCase[IS_RECIPIENT_IN_GROUP] = isRecipientInGroup;
                    testCase[IS_EXPECT_SUCCESS] = isAdmin || isDataOwner && isRecipientInGroup;
                    testCases.add(testCase);
                }
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }
}

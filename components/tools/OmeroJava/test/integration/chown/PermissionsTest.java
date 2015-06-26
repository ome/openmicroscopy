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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import omero.model.Pixels;
import omero.model.RectI;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.Thumbnail;
import omero.sys.EventContext;
import omero.sys.ParametersI;

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
 * @since 5.1.2
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
        userOtherGroup = newUserAndGroup("rwr---");
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
     * Add the given annotation to the given image.
     * @param image an image
     * @param annotation an annotation
     * @return the new loaded link from the image to the annotation
     * @throws ServerError unexpected
     */
    private ImageAnnotationLink annotateImage(Image image, Annotation annotation) throws ServerError {
        if (image.isLoaded() && image.getId() != null) {
            image = (Image) image.proxy();
        }
        if (annotation.isLoaded() && annotation.getId() != null) {
            annotation = (Annotation) annotation.proxy();
        }

        final ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.setParent(image);
        link.setChild(annotation);
        return (ImageAnnotationLink) iUpdate.saveAndReturnObject(link);
    }

    /**
     * Add a comment, tag and a ROI to the given image.
     * @param image an image
     * @return the new model objects
     * @throws ServerError unexpected
     */
    private List<IObject> annotateImage(Image image) throws ServerError {
        if (image.isLoaded() && image.getId() != null) {
            image = (Image) image.proxy();
        }

        final List<IObject> annotationObjects = new ArrayList<IObject>();

        for (final Annotation annotation : new Annotation[] {new CommentAnnotationI(), new TagAnnotationI()}) {
            final ImageAnnotationLink link = annotateImage(image, annotation);
            annotationObjects.add(link.proxy());
            annotationObjects.add(link.getChild().proxy());
        }

        Roi roi = new RoiI();
        roi.addShape(new RectI());
        roi.setImage((Image) image.proxy());
        roi = (Roi) iUpdate.saveAndReturnObject(roi);
        annotationObjects.add(roi.proxy());
        annotationObjects.add(roi.getShape(0).proxy());

        final ParametersI params = new ParametersI().addId(image.getId());
        final Pixels pixels = (Pixels) iQuery.findByQuery("FROM Pixels WHERE image.id = :id", params);

        Thumbnail thumbnail = mmFactory.createThumbnail();
        thumbnail.setPixels((Pixels) pixels.proxy());
        thumbnail = (Thumbnail) iUpdate.saveAndReturnObject(thumbnail);
        annotationObjects.add(thumbnail.proxy());

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
    private void assertOwnedBy(Collection<? extends IObject> objects, EventContext expectedOwner) throws ServerError {
        if (objects.isEmpty()) {
            throw new IllegalArgumentException("must assert about some objects");
        }
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
     * Test a specific case of using {@link Chown2} with owner's shared annotations in a private group.
     * @param isDataOwner if the user submitting the {@link Chown2} request owns the data in the group
     * @param isAdmin if the user submitting the {@link Chown2} request is a member of the system group
     * @param isRecipientInGroup if the user receiving data by means of the {@link Chown2} request is a member of the data's group
     * @param isExpectSuccess if the chown is expected to succeed
     * @throws Exception unexpected
     */
    @Test(dataProvider = "chown test cases")
    public void testChownPrivate(boolean isDataOwner, boolean isAdmin, boolean isRecipientInGroup, boolean isExpectSuccess)
            throws Exception {

        /* set up the users and group for this test case */

        final EventContext importer, chowner, recipient;
        final ExperimenterGroup dataGroup;

        importer = newUserAndGroup("rw----");

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

        /* note which objects were used to annotate an image */

        final List<IObject> imageAnnotations;
        final List<ImageAnnotationLink> tagLinksOnOtherImage = new ArrayList<ImageAnnotationLink>();

        /* import and annotate an image */

        init(importer);
        final Image image = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage()).proxy();
        final long imageId = image.getId().getValue();
        testImages.add(imageId);
        imageAnnotations = annotateImage(image);

        /* tag another image with the tags from the first image */

        final Image otherImage = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage()).proxy();
        testImages.add(otherImage.getId().getValue());
        for (final IObject annotation : imageAnnotations) {
            if (annotation instanceof TagAnnotation) {
                final ImageAnnotationLink link = (ImageAnnotationLink) annotateImage(otherImage, (TagAnnotation) annotation);
                tagLinksOnOtherImage.add((ImageAnnotationLink) link.proxy());
            }
        }
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

        final Set<Long> imageLinkIds = new HashSet<Long>();

        logRootIntoGroup(dataGroupId);
        assertOwnedBy(image, recipient);
        for (final IObject annotation : imageAnnotations) {
            if (annotation instanceof TagAnnotation) {
                assertOwnedBy(annotation, importer);
            } else if (annotation instanceof ImageAnnotationLink) {
                imageLinkIds.add(annotation.getId().getValue());
            } else {
                assertOwnedBy(annotation, recipient);
            }
        }
        assertOwnedBy(tagLinksOnOtherImage, importer);

        /* check that the image's links to the tags that were also linked to the other image were deleted */

        final String query = "SELECT COUNT(id) FROM ImageAnnotationLink WHERE id IN (:ids)";
        final ParametersI params = new ParametersI().addIds(imageLinkIds);
        final List<List<RType>> results = iQuery.projection(query, params);
        final long remainingLinkCount = ((RLong) results.get(0).get(0)).getValue();
        Assert.assertEquals(remainingLinkCount, imageLinkIds.size() - tagLinksOnOtherImage.size());
        disconnect();
    }

    /**
     * Test a specific case of using {@link Chown2} with owner's and others' annotations in a read-annotate group.
     * @param isDataOwner if the user submitting the {@link Chown2} request owns the data in the group
     * @param isAdmin if the user submitting the {@link Chown2} request is a member of the system group
     * @param isRecipientInGroup if the user receiving data by means of the {@link Chown2} request is a member of the data's group
     * @param isExpectSuccess if the chown is expected to succeed
     * @throws Exception unexpected
     */
    @Test(dataProvider = "chown test cases")
    public void testChownReadAnnotate(boolean isDataOwner, boolean isAdmin, boolean isRecipientInGroup, boolean isExpectSuccess)
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
        ownerAnnotations = annotateImage(image);
        disconnect();

        /* have another user annotate the image */

        init(annotator);
        otherAnnotations = annotateImage(image);
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
        for (final IObject annotation : ownerAnnotations) {
            if (annotation instanceof Thumbnail) {
                assertOwnedBy(annotation, importer);
            } else {
                assertOwnedBy(annotation, recipient);
            }
        }
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

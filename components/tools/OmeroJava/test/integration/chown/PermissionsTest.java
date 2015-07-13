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
import omero.cmd.Chmod2;
import omero.cmd.Chown2;
import omero.cmd.Delete2;
import omero.model.Annotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Experiment;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.Instrument;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.RectI;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.Thumbnail;
import omero.sys.EventContext;
import omero.sys.Parameters;
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

    private ExperimenterGroup systemGroup, otherGroup;

    /**
     * Set up admin and non-admin users who are not a member of the groups created by tests.
     * @throws Exception unexpected
     */
    @BeforeClass
    public void setupOtherGroup() throws Exception {
        systemGroup = new ExperimenterGroupI(iAdmin.getSecurityRoles().systemGroupId, false);
        otherGroup = new ExperimenterGroupI(iAdmin.getEventContext().groupId, false);
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
     * @param isGroupOwner if the user submitting the {@link Chown2} request owns the group itself
     * @param isRecipientInGroup if the user receiving data by means of the {@link Chown2} request is a member of the data's group
     * @param isExpectSuccess if the chown is expected to succeed
     * @throws Exception unexpected
     */
    @Test(dataProvider = "chown annotation test cases")
    public void testChownAnnotationPrivate(boolean isDataOwner, boolean isAdmin, boolean isGroupOwner, boolean isRecipientInGroup,
            boolean isExpectSuccess) throws Exception {

        /* set up the users and group for this test case */

        final EventContext importer, chowner, recipient;
        final ExperimenterGroup dataGroup;

        importer = newUserAndGroup("rw----", isDataOwner && isGroupOwner);

        final long dataGroupId = importer.groupId;
        dataGroup = new ExperimenterGroupI(dataGroupId, false);

        recipient = newUserInGroup(isRecipientInGroup ? dataGroup : otherGroup, false);

        if (isDataOwner) {
            chowner = importer;
        } else {
            chowner = newUserInGroup(dataGroup, isGroupOwner);
        }

        if (isAdmin) {
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
     * @param isGroupOwner if the user submitting the {@link Chown2} request owns the group itself
     * @param isRecipientInGroup if the user receiving data by means of the {@link Chown2} request is a member of the data's group
     * @param isExpectSuccess if the chown is expected to succeed
     * @throws Exception unexpected
     */
    @Test(dataProvider = "chown annotation test cases")
    public void testChownAnnotationReadAnnotate(boolean isDataOwner, boolean isAdmin, boolean isGroupOwner,
            boolean isRecipientInGroup, boolean isExpectSuccess) throws Exception {

        /* set up the users and group for this test case */

        final EventContext importer, annotator, chowner, recipient;
        final ExperimenterGroup dataGroup;

        importer = newUserAndGroup("rwra--", isDataOwner && isGroupOwner);

        final long dataGroupId = importer.groupId;
        dataGroup = new ExperimenterGroupI(dataGroupId, false);

        recipient = newUserInGroup(isRecipientInGroup ? dataGroup : otherGroup, false);

        if (isDataOwner) {
            chowner = importer;
        } else {
            chowner = newUserInGroup(dataGroup, isGroupOwner);
        }

        if (isAdmin) {
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
     * @return a variety of test cases for annotation chown
     */
    @DataProvider(name = "chown annotation test cases")
    public Object[][] provideChownAnnotationCases() {
        int index = 0;
        final int IS_DATA_OWNER = index++;
        final int IS_ADMIN = index++;
        final int IS_GROUP_OWNER = index++;
        final int IS_RECIPIENT_IN_GROUP = index++;
        final int IS_EXPECT_SUCCESS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final boolean isDataOwner : booleanCases) {
            for (final boolean isAdmin : booleanCases) {
                for (final boolean isGroupOwner : booleanCases) {
                    for (final boolean isRecipientInGroup : booleanCases) {
                        if (isAdmin && isGroupOwner) {
                            /* not an interesting case */
                            continue;
                        }
                        final Object[] testCase = new Object[index];
                        testCase[IS_DATA_OWNER] = isDataOwner;
                        testCase[IS_ADMIN] = isAdmin;
                        testCase[IS_GROUP_OWNER] = isGroupOwner;
                        testCase[IS_RECIPIENT_IN_GROUP] = isRecipientInGroup;
                        testCase[IS_EXPECT_SUCCESS] = isAdmin || isGroupOwner && isRecipientInGroup;
                        // DEBUG: if (isDataOwner == true && isAdmin == true && isGroupOwner == true &&
                        //            isRecipientInGroup == true)
                        testCases.add(testCase);
                    }
                }
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * Test a specific case of using {@link Chown2} on an image that is in a dataset.
     * @param isImageOwner if the user who owns the dataset also owns the image
     * @param isLinkOwner if the user who owns the dataset also linked the image to the dataset
     * @param groupPerms the permissions on the group in which the data exists
     * @throws Exception unexpected
     */
    @Test(dataProvider = "chown container test cases")
    public void testChownImageInDataset(boolean isImageOwner, boolean isLinkOwner, String groupPermissions) throws Exception {

        /* set up the users and group for this test case */

        final EventContext datasetOwner, imageOwner, linkOwner, recipient;
        final ExperimenterGroup dataGroup;

        datasetOwner = newUserAndGroup(groupPermissions, true);

        final long dataGroupId = datasetOwner.groupId;
        dataGroup = new ExperimenterGroupI(dataGroupId, false);

        recipient = newUserInGroup(dataGroup, false);

        if (isImageOwner) {
            imageOwner = datasetOwner;
            linkOwner = isLinkOwner ? datasetOwner : newUserInGroup(dataGroup, false);
        } else {
            imageOwner = newUserInGroup(dataGroup, true);
            linkOwner = isLinkOwner ? datasetOwner : imageOwner;
        }

        /* create a dataset */

        init(datasetOwner);
        final Dataset dataset = (Dataset) iUpdate.saveAndReturnObject(mmFactory.simpleDataset()).proxy();
        disconnect();

        /* create an image */

        init(imageOwner);
        final Image image = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage()).proxy();
        final long imageId = image.getId().getValue();
        testImages.add(imageId);
        disconnect();

        /* move the image into the dataset */

        init(linkOwner);
        DatasetImageLink link = new DatasetImageLinkI();
        link.setParent(dataset);
        link.setChild(image);
        link = (DatasetImageLink) iUpdate.saveAndReturnObject(link);
        disconnect();

        /* perform the chown */

        init(imageOwner);
        final Chown2 chown = new Chown2();
        chown.targetObjects = ImmutableMap.of("Image", Collections.singletonList(imageId));
        chown.userId = recipient.userId;
        doChange(client, factory, chown, true);
        disconnect();

        /* check that the objects' ownership is all as expected */

        logRootIntoGroup(dataGroupId);
        assertOwnedBy(dataset, datasetOwner);
        assertOwnedBy(image, recipient);
        final boolean isExpectLink = "rwrw--".equals(groupPermissions);
        if (isExpectLink) {
            assertExists(link);
            assertOwnedBy(link, linkOwner);
        } else {
            assertDoesNotExist(link);
        }
        disconnect();
    }

    /**
     * Test a specific case of using {@link Chown2} on a dataset that contains an image.
     * @param isImageOwner if the user who owns the dataset also owns the image
     * @param isLinkOwner if the user who owns the dataset also linked the image to the dataset
     * @param groupPerms the permissions on the group in which the data exists
     * @throws Exception unexpected
     */
    @Test(dataProvider = "chown container test cases")
    public void testChownDatasetWithImage(boolean isImageOwner, boolean isLinkOwner, String groupPermissions) throws Exception {

        /* set up the users and group for this test case */

        final EventContext datasetOwner, imageOwner, linkOwner, recipient;
        final ExperimenterGroup dataGroup;

        datasetOwner = newUserAndGroup(groupPermissions, true);

        final long dataGroupId = datasetOwner.groupId;
        dataGroup = new ExperimenterGroupI(dataGroupId, false);

        recipient = newUserInGroup(dataGroup, false);

        if (isImageOwner) {
            imageOwner = datasetOwner;
            linkOwner = isLinkOwner ? datasetOwner : newUserInGroup(dataGroup, false);
        } else {
            imageOwner = newUserInGroup(dataGroup, true);
            linkOwner = isLinkOwner ? datasetOwner : imageOwner;
        }

        /* create a dataset */

        init(datasetOwner);
        final Dataset dataset = (Dataset) iUpdate.saveAndReturnObject(mmFactory.simpleDataset()).proxy();
        final long datasetId = dataset.getId().getValue();
        disconnect();

        /* create an image */

        init(imageOwner);
        final Image image = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage()).proxy();
        final long imageId = image.getId().getValue();
        testImages.add(imageId);
        disconnect();

        /* move the image into the dataset */

        init(linkOwner);
        DatasetImageLink link = new DatasetImageLinkI();
        link.setParent(dataset);
        link.setChild(image);
        link = (DatasetImageLink) iUpdate.saveAndReturnObject(link);
        disconnect();

        /* perform the chown */

        init(datasetOwner);
        final Chown2 chown = new Chown2();
        chown.targetObjects = ImmutableMap.of("Dataset", Collections.singletonList(datasetId));
        chown.userId = recipient.userId;
        doChange(client, factory, chown, true);
        disconnect();

        /* check that the objects' ownership is all as expected */

        logRootIntoGroup(dataGroupId);
        assertOwnedBy(dataset, recipient);
        assertOwnedBy(image, isImageOwner ? recipient : imageOwner);
        assertOwnedBy(link, isImageOwner ? recipient : linkOwner);
        disconnect();
    }

    /**
     * @return a variety of test cases for container chown
     */
    @DataProvider(name = "chown container test cases")
    public Object[][] provideChownContainerCases() {
        int index = 0;
        final int IS_IMAGE_OWNER = index++;
        final int IS_LINK_OWNER = index++;
        final int GROUP_PERMS = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final boolean isImageOwner : booleanCases) {
            for (final boolean isLinkOwner : booleanCases) {
                for (final String groupPerms : permsCases) {
                    if (!(isImageOwner && isLinkOwner || "rwrw--".equals(groupPerms))) {
                        /* test case does not make sense */
                        continue;
                    }
                    final Object[] testCase = new Object[index];
                    testCase[IS_IMAGE_OWNER] = isImageOwner;
                    testCase[IS_LINK_OWNER] = isLinkOwner;
                    testCase[GROUP_PERMS] = groupPerms;
                    // DEBUG: if (isImageOwner == true && isLinkOwner == true && "rwr---".equals(groupPerms))
                    testCases.add(testCase);
                }
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * Test chown on an image whose instrument is shared with another's image.
     * @param groupPermissions the permissions on the group in which the chown is to occur
     * @throws Exception unexpected
     */
    @Test(dataProvider = "image sharing test cases")
    public void testSharedInstrument(String groupPermissions) throws Exception {

        /* set up the users and group for this test case */

        final EventContext imageOwner, projectionOwner, recipient;
        final ExperimenterGroup dataGroup;

        imageOwner = newUserAndGroup("rwrw--", true);

        final long dataGroupId = imageOwner.groupId;
        dataGroup = new ExperimenterGroupI(dataGroupId, false);

        projectionOwner = newUserInGroup(dataGroup, false);
        recipient = newUserInGroup(dataGroup, false);

        /* create an image with an instrument */

        init(imageOwner);
        Image image = mmFactory.createImage();
        image.setInstrument(mmFactory.createInstrument());
        image = (Image) iUpdate.saveAndReturnObject(image);
        final long imageId = image.getId().getValue();
        testImages.add(imageId);
        final Instrument instrument = (Instrument) image.getInstrument().proxy();
        image = (Image) image.proxy();
        disconnect();

        /* another user projects the image */

        init(projectionOwner);
        Image projection = mmFactory.createImage();
        projection.setInstrument(instrument);
        projection = (Image) iUpdate.saveAndReturnObject(projection);
        final long projectionId = projection.getId().getValue();
        testImages.add(projectionId);
        projection = (Image) projection.proxy();
        disconnect();

        /* chmod the group to the required permissions */

        logRootIntoGroup(dataGroupId);
        final Chmod2 chmod = new Chmod2();
        chmod.targetObjects = ImmutableMap.of("ExperimenterGroup", Collections.singletonList(dataGroupId));
        chmod.permissions = groupPermissions;
        doChange(client, factory, chmod, true);
        disconnect();

        /* perform the chown */

        init(imageOwner);
        final Chown2 chown = new Chown2();
        chown.targetObjects = ImmutableMap.of("Image", Collections.singletonList(imageId));
        chown.userId = recipient.userId;
        doChange(client, factory, chown, true);
        disconnect();

        /* check that the objects' ownership is all as expected */

        logRootIntoGroup(dataGroupId);
        assertOwnedBy(image, recipient);
        assertOwnedBy(projection, projectionOwner);
        assertOwnedBy(instrument, imageOwner);
        disconnect();
    }

    /**
     * Test chown on an image that is used in the same experiment as another's image.
     * @param groupPermissions the permissions on the group in which the chown is to occur
     * @throws Exception unexpected
     */
    @Test(dataProvider = "image sharing test cases")
    public void testSharedExperiment(String groupPermissions) throws Exception {

        /* set up the users and group for this test case */

        final EventContext imageOwner, otherImageOwner, recipient;
        final ExperimenterGroup dataGroup;

        imageOwner = newUserAndGroup("rwrw--", true);

        final long dataGroupId = imageOwner.groupId;
        dataGroup = new ExperimenterGroupI(dataGroupId, false);

        otherImageOwner = newUserInGroup(dataGroup, false);
        recipient = newUserInGroup(dataGroup, false);

        /* create an image with an experiment */

        init(imageOwner);
        Image image = mmFactory.createImage();
        image.setExperiment(mmFactory.simpleExperiment());
        image = (Image) iUpdate.saveAndReturnObject(image);
        final long imageId = image.getId().getValue();
        testImages.add(imageId);
        final Experiment experiment = (Experiment) image.getExperiment().proxy();
        image = (Image) image.proxy();
        disconnect();

        /* another user's image is part of the same experiment */

        init(otherImageOwner);
        Image otherImage = mmFactory.createImage();
        otherImage.setExperiment(experiment);
        otherImage = (Image) iUpdate.saveAndReturnObject(otherImage);
        final long otherImageId = otherImage.getId().getValue();
        testImages.add(otherImageId);
        otherImage = (Image) otherImage.proxy();
        disconnect();

        /* chmod the group to the required permissions */

        logRootIntoGroup(dataGroupId);
        final Chmod2 chmod = new Chmod2();
        chmod.targetObjects = ImmutableMap.of("ExperimenterGroup", Collections.singletonList(dataGroupId));
        chmod.permissions = groupPermissions;
        doChange(client, factory, chmod, true);
        disconnect();

        /* perform the chown */

        init(imageOwner);
        final Chown2 chown = new Chown2();
        chown.targetObjects = ImmutableMap.of("Image", Collections.singletonList(imageId));
        chown.userId = recipient.userId;
        doChange(client, factory, chown, true);
        disconnect();

        /* check that the objects' ownership is all as expected */

        logRootIntoGroup(dataGroupId);
        assertOwnedBy(image, recipient);
        assertOwnedBy(otherImage, otherImageOwner);
        assertOwnedBy(experiment, imageOwner);
        disconnect();
    }

    /**
     * @return group permissions for image sharing test cases
     */
    @DataProvider(name = "image sharing test cases")
    public Object[][] provideImageSharingCases() {
        int index = 0;
        final int GROUP_PERMS = index++;

        final String[] permsCases = new String[]{"rwr---", "rwra--", "rwrw--"};

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final String groupPerms : permsCases) {
            final Object[] testCase = new Object[index];
            testCase[GROUP_PERMS] = groupPerms;
            // DEBUG: if ("rwr---".equals(groupPerms))
            testCases.add(testCase);
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }

    /* for dataset to plate test cases */
    private enum Target { DATASET, IMAGES, PLATE };

    /**
     * Test chown on a dataset, plate, or images, where the plate's images are in the dataset.
     * @param groupPermissions the permissions on the group in which the chown is to occur
     * @param target the target of the chown operation
     * @throws Exception unexpected
     */
    @Test(dataProvider = "dataset to plate test cases")
    public void testDatasetToPlate(String groupPermissions, Target target) throws Exception {

        /* set up the users and group for this test case */

        final EventContext datasetOwner, plateOwner, recipient;
        final ExperimenterGroup dataGroup;

        datasetOwner = newUserAndGroup("rwrw--", true);

        final long dataGroupId = datasetOwner.groupId;
        dataGroup = new ExperimenterGroupI(dataGroupId, false);

        plateOwner = newUserInGroup(dataGroup, false);
        recipient = newUserInGroup(dataGroup, false);

        /* create a plate */

        init(plateOwner);
        Plate plate = mmFactory.createPlate(2, 2, 1, 1, false);
        plate = (Plate) iUpdate.saveAndReturnObject(plate).proxy();
        final long plateId = plate.getId().getValue();

        /* find the plate's images */

        final List<Long> imageIds = new ArrayList<Long>();
        final String hql = "SELECT image.id FROM WellSample where well.id IN (SELECT id FROM Well WHERE plate.id = :id)";
        final Parameters params = new ParametersI().addId(plateId);
        for (final List<RType> result : iQuery.projection(hql, params)) {
            final Long imageId = ((RLong) result.get(0)).getValue();
            imageIds.add(imageId);
        }
        disconnect();

        /* the images should be owned by the dataset owner */

        logRootIntoGroup(dataGroupId);
        final Experimenter datasetOwnerActual = new ExperimenterI(datasetOwner.userId, false);
        final List<IObject> images = iQuery.findAllByQuery("FROM Image WHERE id IN (:ids)", new ParametersI().addIds(imageIds));
        Assert.assertEquals(images.size(), imageIds.size());
        for (final IObject image : images) {
            image.getDetails().setOwner(datasetOwnerActual);
        }
        iUpdate.saveCollection(images);
        disconnect();

        /* create the dataset and link the images to it */

        init(datasetOwner);
        final Dataset dataset = (Dataset) iUpdate.saveAndReturnObject(mmFactory.simpleDataset()).proxy();
        final long datasetId = dataset.getId().getValue();

        final List<DatasetImageLink> links = new ArrayList<DatasetImageLink>(images.size());
        for (final IObject image : images) {
            DatasetImageLink link = new DatasetImageLinkI();
            link.setParent(dataset);
            link.setChild((Image) image.proxy());
            links.add((DatasetImageLink) iUpdate.saveAndReturnObject(link).proxy());
        }
        disconnect();

        /* check that the objects' ownership is all as expected */

        logRootIntoGroup(dataGroupId);
        assertOwnedBy(dataset, datasetOwner);
        assertOwnedBy(images, datasetOwner);
        assertOwnedBy(links, datasetOwner);
        assertOwnedBy(plate, plateOwner);
        disconnect();

        /* perform the chown */

        init(datasetOwner);
        final Chown2 chown = new Chown2();

        switch (target) {
        case DATASET:
            chown.targetObjects = ImmutableMap.of("Dataset", Collections.singletonList(datasetId));
            break;
        case IMAGES:
            chown.targetObjects = ImmutableMap.of("Image", imageIds);
            break;
        case PLATE:
            chown.targetObjects = ImmutableMap.of("Plate", Collections.singletonList(plateId));
            break;
        }

        chown.userId = recipient.userId;
        doChange(client, factory, chown, true);
        disconnect();

        logRootIntoGroup(dataGroupId);

            /* check that the objects' ownership is all as expected */

            switch (target) {
            case DATASET:
                assertOwnedBy(dataset, recipient);
                assertOwnedBy(images, datasetOwner);
                assertOwnedBy(links, datasetOwner);
                assertOwnedBy(plate, plateOwner);
                break;
            case IMAGES:
                assertOwnedBy(dataset, datasetOwner);
                assertOwnedBy(images, recipient);
                assertOwnedBy(links, datasetOwner);
                assertOwnedBy(plate, plateOwner);
                break;
            case PLATE:
                assertOwnedBy(dataset, datasetOwner);
                assertOwnedBy(images, datasetOwner);
                assertOwnedBy(links, datasetOwner);
                assertOwnedBy(plate, recipient);
            }

        /* delete the objects as clean-up */

        final Delete2 delete = new Delete2();
        delete.targetObjects = ImmutableMap.of(
                "Dataset", Collections.singletonList(datasetId),
                "Plate", Collections.singletonList(plateId));
        doChange(client, factory, delete, true);

        disconnect();
}

    /**
     * @return a variety of test cases for dataset to plate
     */
    @DataProvider(name = "dataset to plate test cases")
    public Object[][] provideDatasetToPlateCases() {
        int index = 0;
        final int GROUP_PERMS = index++;
        final int TARGET = index++;

        final String[] permsCases = new String[]{"rwr---", "rwra--", "rwrw--"};

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final String groupPerms : permsCases) {
            for (final Target target : Target.values()) {
                final Object[] testCase = new Object[index];
                testCase[GROUP_PERMS] = groupPerms;
                testCase[TARGET] = target;
                // DEBUG: if ("rwr---".equals(groupPerms) && target == Target.DATASET)
                testCases.add(testCase);
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }
}

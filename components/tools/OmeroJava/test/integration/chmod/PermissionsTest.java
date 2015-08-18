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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ome.model.internal.Permissions;
import ome.util.Utils;
import omero.RLong;
import omero.RType;
import omero.ServerError;
import omero.cmd.Chmod2;
import omero.cmd.Delete2;
import omero.gateway.util.Requests;
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
 * Tests that only appropriate users may use {@link Chmod2} and that others' data is then deleted only when appropriate.
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
        final ParametersI params = new ParametersI().addId(imageId);
        final Image image = (Image) iQuery.findByQuery("FROM Image i JOIN FETCH i.pixels WHERE i.id = :id", params);

        final List<IObject> annotationObjects = new ArrayList<IObject>();

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

        Thumbnail thumbnail = mmFactory.createThumbnail();
        thumbnail.setPixels((Pixels) image.getPrimaryPixels().proxy());
        thumbnail = (Thumbnail) iUpdate.saveAndReturnObject(thumbnail);
        annotationObjects.add(thumbnail.proxy());

        return annotationObjects;
    }

    /**
     * Test that a specific case of using {@link Chmod2} on an annotated image behaves as expected.
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
    @Test(dataProvider = "chmod annotation test cases")
    public void testChmodAnnotation(boolean isGroupOwner, boolean isGroupMember, boolean isDataOwner, boolean isAdmin,
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
        final Chmod2 chmod = Requests.chmod("ExperimenterGroup", dataGroupId, toPerms);
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

        assertExists(image);
        if (tag != null) {
            assertExists(tag);
        }
        assertAllExist(ownerAnnotations);
        if (isExpectDeleteOther) {
            assertNoneExist(otherAnnotations);
        } else {
            assertAllExist(otherAnnotations);
        }
        disconnect();
    }

    /**
     * @return a variety of test cases for annotation chmod
     */
    @DataProvider(name = "chmod annotation test cases")
    public Object[][] provideChmodAnnotationCases() {
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
                                if (fromPerms.equals(toPerms)) {
                                    /* not a permissions change */
                                    continue;
                                }
                                final Object[] testCase = new Object[index];
                                testCase[IS_GROUP_OWNER] = isGroupOwner;
                                testCase[IS_GROUP_MEMBER] = isGroupMember;
                                testCase[IS_DATA_OWNER] = isDataOwner;
                                testCase[IS_ADMIN] = isAdmin;
                                testCase[FROM_PERMS] = fromPerms;
                                testCase[TO_PERMS] = toPerms;
                                testCase[IS_EXPECT_SUCCESS] = isAdmin || isGroupOwner;
                                testCase[IS_EXPECT_DELETE_OTHER] = "rwra--".equals(fromPerms) && "rw----".equals(toPerms);
                                // DEBUG: if (isGroupOwner == true && isGroupMember == true && isDataOwner == true &&
                                //            isAdmin == true && "rwra--".equals(fromPerms) && "rw----".equals(toPerms))
                                testCases.add(testCase);
                            }
                        }
                    }
                }
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * Test a specific case of using {@link Chmod2} on an image that is in a dataset.
     * @param isImageOwner if the user who owns the dataset also owns the image
     * @param isLinkOwner if the user who owns the dataset also linked the image to the dataset
     * @throws Exception unexpected
     */
    @Test(dataProvider = "chmod container test cases")
    public void testChmodContainerReadWriteToPrivate(boolean isImageOwner, boolean isLinkOwner) throws Exception {

        /* set up the users and group for this test case */

        final EventContext datasetOwner, imageOwner, linkOwner, chmodder;
        final ExperimenterGroup dataGroup;

        datasetOwner = newUserAndGroup("rwrw--");

        final long dataGroupId = datasetOwner.groupId;
        dataGroup = new ExperimenterGroupI(dataGroupId, false);

        if (isImageOwner) {
            imageOwner = datasetOwner;
            linkOwner = isLinkOwner ? datasetOwner : newUserInGroup(dataGroup, false);
        } else {
            imageOwner = newUserInGroup(dataGroup, false);
            linkOwner = isLinkOwner ? datasetOwner : imageOwner;
        }

        chmodder = newUserInGroup(dataGroup, true);

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

        /* perform the chmod */

        init(chmodder);
        final Chmod2 chmod = Requests.chmod("ExperimenterGroup", dataGroupId, "rw----");
        doChange(client, factory, chmod, true);
        disconnect();

        /* check that exactly the expected object deletions have occurred */

        logRootIntoGroup(dataGroupId);
        assertExists(dataset);
        assertExists(image);
        if (datasetOwner == imageOwner) {
            assertExists(link);
        } else {
            assertDoesNotExist(link);
        }
        disconnect();
    }

    /**
     * @return a variety of test cases for container chmod
     */
    @DataProvider(name = "chmod container test cases")
    public Object[][] provideChmodContainerCases() {
        int index = 0;
        final int IS_IMAGE_OWNER = index++;
        final int IS_LINK_OWNER = index++;

        final boolean[] booleanCases = new boolean[]{false, true};

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final boolean isImageOwner : booleanCases) {
            for (final boolean isLinkOwner : booleanCases) {
                final Object[] testCase = new Object[index];
                testCase[IS_IMAGE_OWNER] = isImageOwner;
                testCase[IS_LINK_OWNER] = isLinkOwner;
                // DEBUG: if (isImageOwner == true && isLinkOwner == true)
                testCases.add(testCase);
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * Test chmod on a group wherein an image's instrument is shared with another's image.
     * @param toPerms the permissions on the group after the chmod
     * @throws Exception unexpected
     */
    @Test(dataProvider = "private-group failure test cases")
    public void testSharedInstrument(String toPerms) throws Exception {

        /* set up the users and group for this test case */

        final EventContext imageOwner, projectionOwner, chmodder;
        final ExperimenterGroup dataGroup;

        imageOwner = newUserAndGroup("rwrw--");

        final long dataGroupId = imageOwner.groupId;
        dataGroup = new ExperimenterGroupI(dataGroupId, false);

        projectionOwner = newUserInGroup(dataGroup, false);
        chmodder = newUserInGroup(dataGroup, true);

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

        /* perform the chmod */

        final boolean isExpectSuccess = !"rw----".equals(toPerms);

        init(chmodder);
        final Chmod2 chmod = Requests.chmod("ExperimenterGroup", dataGroupId, toPerms);
        doChange(client, factory, chmod, isExpectSuccess);
        disconnect();

        if (!isExpectSuccess) {
            return;
        }

        /* check that all the objects still exist */

        logRootIntoGroup(dataGroupId);
        assertExists(image);
        assertExists(projection);
        assertExists(instrument);
        disconnect();
    }


    /**
     * Test chmod on a group wherein an image is used in the same experiment as another's image.
     * @param toPerms the permissions on the group after the chmod
     * @throws Exception unexpected
     */
    @Test(dataProvider = "private-group failure test cases")
    public void testSharedExperiment(String toPerms) throws Exception {

        /* set up the users and group for this test case */

        final EventContext imageOwner, otherImageOwner, chmodder;
        final ExperimenterGroup dataGroup;

        imageOwner = newUserAndGroup("rwrw--");

        final long dataGroupId = imageOwner.groupId;
        dataGroup = new ExperimenterGroupI(dataGroupId, false);

        otherImageOwner = newUserInGroup(dataGroup, false);
        chmodder = newUserInGroup(dataGroup, true);

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

        /* perform the chmod */

        final boolean isExpectSuccess = !"rw----".equals(toPerms);

        init(chmodder);
        final Chmod2 chmod = Requests.chmod("ExperimenterGroup", dataGroupId, toPerms);
        doChange(client, factory, chmod, isExpectSuccess);
        disconnect();

        if (!isExpectSuccess) {
            return;
        }

        /* check that all the objects still exist */

        logRootIntoGroup(dataGroupId);
        assertExists(image);
        assertExists(otherImage);
        assertExists(experiment);
        disconnect();
    }

    /**
     * Test chmod on a group wherein a plate's images are owned by a different user who has them in their dataset.
     * @param toPerms the permissions on the group after the chmod
     * @throws Exception unexpected
     */
    @Test(dataProvider = "private-group failure test cases")
    public void testDatasetToPlate(String toPerms) throws Exception {

        /* set up the users and group for this test case */

        final EventContext datasetOwner, plateOwner, chmodder;
        final ExperimenterGroup dataGroup;

        datasetOwner = newUserAndGroup("rwrw--");

        final long dataGroupId = datasetOwner.groupId;
        dataGroup = new ExperimenterGroupI(dataGroupId, false);

        plateOwner = newUserInGroup(dataGroup, false);
        chmodder = newUserInGroup(dataGroup, true);

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

        /* perform the chmod */

        final boolean isExpectSuccess = !"rw----".equals(toPerms);

        init(chmodder);
        final Chmod2 chmod = Requests.chmod("ExperimenterGroup", dataGroupId, toPerms);
        doChange(client, factory, chmod, isExpectSuccess);
        disconnect();

        logRootIntoGroup(dataGroupId);

        if (isExpectSuccess) {

            /* check that all the objects still exist */

            assertExists(dataset);
            assertAllExist(images);
            assertAllExist(links);
            assertExists(plate);
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
     * @return group permissions for private-group failure test cases
     */
    @DataProvider(name = "private-group failure test cases")
    public Object[][] providePrivateGroupFailureCases() {
        int index = 0;
        final int GROUP_PERMS = index++;

        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};

        final List<Object[]> testCases = new ArrayList<Object[]>();

                for (final String groupPerms : permsCases) {
                    final Object[] testCase = new Object[index];
                    testCase[GROUP_PERMS] = groupPerms;
                    // DEBUG: if ("rwr---".equals(groupPerms))
                    testCases.add(testCase);
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }
}

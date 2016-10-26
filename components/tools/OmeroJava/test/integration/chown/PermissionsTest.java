/*
 * Copyright (C) 2015-2016 University of Dundee & Open Microscopy Environment.
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
import java.util.Map;
import java.util.Set;

import omero.RLong;
import omero.RType;
import omero.ServerError;
import omero.rtypes;
import omero.cmd.Chmod2;
import omero.cmd.Chown2;
import omero.cmd.Delete2;
import omero.gateway.util.Requests;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLink;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Experiment;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Folder;
import omero.model.FolderImageLink;
import omero.model.FolderImageLinkI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.Instrument;
import omero.model.MapAnnotation;
import omero.model.MapAnnotationI;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.RectangleI;
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SetMultimap;

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
        final Delete2 delete = Requests.delete().target("Image").id(testImages).build();
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
     * Add a comment, tag, MapAnnotation, FileAnnotation,
     * Thumbnail and a ROI to the given image.
     * @param image an image
     * @return the new model objects
     * @throws ServerError unexpected
     */
    private List<IObject> annotateImage(Image image) throws ServerError {
        if (image.isLoaded() && image.getId() != null) {
            image = (Image) image.proxy();
        }

        final List<IObject> annotationObjects = new ArrayList<IObject>();

        for (final Annotation annotation : new Annotation[] {new CommentAnnotationI(),
             new TagAnnotationI(), new FileAnnotationI(), new MapAnnotationI()}) {
            final ImageAnnotationLink link = annotateImage(image, annotation);
            annotationObjects.add(link.proxy());
            annotationObjects.add(link.getChild().proxy());
        }

        Roi roi = new RoiI();
        roi.addShape(new RectangleI());
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
     * Create tag sets
     * @param number number of tag sets to create
     * @return the newly created tag sets
     * @throws Exception unexpected
     */
    private List<TagAnnotation> createTagsets(int number) throws Exception {
        final List<TagAnnotation> tagsets = new ArrayList<TagAnnotation>();
        for (int i = 1; i <= number; i++) {
            final TagAnnotation tagset = new TagAnnotationI();
            tagset.setName(rtypes.rstring("tagset #" + i));
            tagset.setNs(rtypes.rstring(omero.constants.metadata.NSINSIGHTTAGSET.value));
            tagsets.add((TagAnnotation) iUpdate.saveAndReturnObject(tagset).proxy());
        }
        return tagsets;
    }

    /**
     * Create tags
     * @param number number of tags to create
     * @return the newly created tags
     * @throws Exception unexpected
     */
    private List<TagAnnotation> createTags(int number) throws Exception {
        final List<TagAnnotation> tags = new ArrayList<TagAnnotation>();
        for (int i = 1; i <= number; i++) {
            final TagAnnotation tag = new TagAnnotationI();
            tag.setName(rtypes.rstring("tag #" + i));
            tags.add((TagAnnotation) iUpdate.saveAndReturnObject(tag).proxy());
        }
         return tags;
    }

    /**
     * Define how to link the tag sets to the tags
     * @param tags the tags to be mapped for linking
     * @param tagsets the tag sets to be mapped for linking
     * @return the map of the prospective links
     * @throws Exception unexpected
     */
    private SetMultimap<TagAnnotation, TagAnnotation> defineLinkingTags(List<TagAnnotation> tags, List<TagAnnotation> tagsets) throws Exception {
        final SetMultimap<TagAnnotation, TagAnnotation> members = HashMultimap.create();
        members.put(tagsets.get(0), tags.get(0));
        members.put(tagsets.get(0), tags.get(1));
        members.put(tagsets.get(1), tags.get(1));
        members.put(tagsets.get(1), tags.get(2));
        return members;
    }

    /**
     * Perform the linking
     * of tags and tag sets
     * @param members the map of the links
     * @throws Exception unexpected
     */
    private void linkTagsTagsets(SetMultimap<TagAnnotation, TagAnnotation> members) throws Exception {
    	for (final Map.Entry<TagAnnotation, TagAnnotation> toLink : members.entries()) {
            final AnnotationAnnotationLink link = new AnnotationAnnotationLinkI();
            link.setParent(toLink.getKey());
            link.setChild(toLink.getValue());
            iUpdate.saveObject(link);
        }
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
     * @param option the child option to use in the tagset transfer
     * @throws Exception unexpected
     */
    @Test(dataProvider = "chown annotation test cases")
    public void testChownAnnotationPrivate(boolean isDataOwner, boolean isAdmin, boolean isGroupOwner, boolean isRecipientInGroup,
            boolean isExpectSuccess, Option option) throws Exception {

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

        final List<IObject> annotationsDoublyLinked;
        final List<IObject> annotationsSinglyLinked;
        final List<ImageAnnotationLink> tagLinksOnOtherImage = new ArrayList<ImageAnnotationLink>();
        final List<ImageAnnotationLink> fileAnnLinksOnOtherImage = new ArrayList<ImageAnnotationLink>();
        final List<ImageAnnotationLink> mapAnnLinksOnOtherImage = new ArrayList<ImageAnnotationLink>();
        
        /* import and annotate an image with two sets of annotations */

        init(importer);
        final Image image = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage()).proxy();
        final long imageId = image.getId().getValue();
        testImages.add(imageId);
        annotationsDoublyLinked = annotateImage(image);
        annotationsSinglyLinked = annotateImage(image);

        /* Link Tag, FileAnnotation and MapAnnotation from "annotationsDoublyLinked" to a second image.
         * Note that ALL of both "annotationsDoublyLinked" and "annotationsSinglyLinked"
         * are already linked to the first image.
         * NONE of the "annotationsSinglyLinked" will be linked to the second image.*/

        final Image otherImage = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage()).proxy();
        testImages.add(otherImage.getId().getValue());
        for (final IObject annotation : annotationsDoublyLinked) {
            if (annotation instanceof TagAnnotation) {
                final ImageAnnotationLink link = (ImageAnnotationLink) annotateImage(otherImage, (TagAnnotation) annotation);
                tagLinksOnOtherImage.add((ImageAnnotationLink) link.proxy());
            } else if (annotation instanceof FileAnnotation) {
                final ImageAnnotationLink link = (ImageAnnotationLink) annotateImage(otherImage, (FileAnnotation) annotation);
                fileAnnLinksOnOtherImage.add((ImageAnnotationLink) link.proxy());
            } else if (annotation instanceof MapAnnotation) {
                final ImageAnnotationLink link = (ImageAnnotationLink) annotateImage(otherImage, (MapAnnotation) annotation);
                mapAnnLinksOnOtherImage.add((ImageAnnotationLink) link.proxy());
            }
        }
        
        /* create two tag sets and three tags */
        final List<TagAnnotation> tagsets = createTagsets(2);
        final List<TagAnnotation> tags = createTags(3);

        /* define how to link the tag sets to the tags and link them */
        final SetMultimap<TagAnnotation, TagAnnotation> members = defineLinkingTags(tags, tagsets);
        linkTagsTagsets(members);

        /* chown the image */

        init(chowner);
        Chown2 chown = Requests.chown().target(image).toUser(recipient.userId).build();
        doChange(client, factory, chown, isExpectSuccess);

        if (!isExpectSuccess) {
            return;
        }

        /* check that the objects' ownership is all as expected, i.e. the non-doubly linked
         * annotations belong to recipient, the doubly-linked annotations belong to importer */

        final Set<Long> imageLinkIds = new HashSet<Long>();

        logRootIntoGroup(dataGroupId);
        assertOwnedBy(image, recipient);
        for (final IObject annotation : annotationsDoublyLinked) {
            if (annotation instanceof TagAnnotation || annotation instanceof FileAnnotation ||
                annotation instanceof MapAnnotation) {
                assertOwnedBy(annotation, importer);
            } else if (annotation instanceof ImageAnnotationLink) {
                imageLinkIds.add(annotation.getId().getValue());
            } else {
                assertOwnedBy(annotation, recipient);
            }
        }

        for (final IObject annotation : annotationsSinglyLinked) {
        	assertOwnedBy(annotation, recipient);
        }

        assertOwnedBy(tagLinksOnOtherImage, importer);
        assertOwnedBy(fileAnnLinksOnOtherImage, importer);
        assertOwnedBy(mapAnnLinksOnOtherImage, importer);

        /* check that the image's links to the tags and FileAnnotations that were also linked to the other image were deleted */

        final String query = "SELECT COUNT(id) FROM ImageAnnotationLink WHERE id IN (:ids)";
        final ParametersI params = new ParametersI().addIds(imageLinkIds);
        final List<List<RType>> results = iQuery.projection(query, params);
        final long remainingLinkCount = ((RLong) results.get(0).get(0)).getValue();
        final long deletedLinkCount = tagLinksOnOtherImage.size() + fileAnnLinksOnOtherImage.size() + mapAnnLinksOnOtherImage.size();
        Assert.assertEquals(remainingLinkCount, imageLinkIds.size() - deletedLinkCount);
        
        /* chown the first tag set */
        init(chowner);
        chown = Requests.chown().target(tagsets.get(0)).toUser(recipient.userId).build();

        switch (option) {
        case NONE:
            break;
        case INCLUDE:
            chown.childOptions = Collections.singletonList(Requests.option().includeType("Annotation").build());
            break;
        case EXCLUDE:
            chown.childOptions = Collections.singletonList(Requests.option().excludeType("Annotation").build());
            break;
        case BOTH:
            chown.childOptions = Collections.singletonList(Requests.option().includeType("Annotation")
                                                                            .excludeType("Annotation").build());
            break;
        default:
            Assert.fail("unexpected option for chown");
        }
        doChange(chown);

         /* check that the tag set is transferred and the other remains owned by original owner */
        assertOwnedBy(tagsets.get(0), recipient);
        assertOwnedBy(tagsets.get(1), importer);

        /* check that only the expected tags are transferred */
        switch (option) {
        case NONE:
            assertOwnedBy(tags.get(0), recipient);
            assertOwnedBy(tags.get(1), importer);
            assertOwnedBy(tags.get(2), importer);
            break;
        case BOTH:
            /* include overrides exclude */
        case INCLUDE:
            assertOwnedBy(tags.get(0), recipient);
            assertOwnedBy(tags.get(1), recipient);
            assertOwnedBy(tags.get(2), importer);
            break;
        case EXCLUDE:
            assertOwnedBy(tags.get(0), importer);
            assertOwnedBy(tags.get(1), importer);
            assertOwnedBy(tags.get(2), importer);
            /* transfer the tag that is not in the second tag set */
            init(chowner);
            chown = Requests.chown().target(tags.get(0)).toUser(recipient.userId).build();
            doChange(chown);
            break;
        }

        /* transfer the second tag set */
        init(chowner);
        chown = Requests.chown().target(tagsets.get(1)).toUser(recipient.userId).build();
        doChange(chown);

        /* check that the tag sets are transferred */
        logRootIntoGroup(dataGroupId);
        assertOwnedBy(tagsets, recipient);
        /* check that the tag in the second tag set was implicitly transferred */
        assertOwnedBy(tags.get(2), recipient);
    }

    /**
     * Test a specific case of using {@link Chown2} with targetUsers option, transferring everything 
     * belonging to a targetUser (importerTargetUser) to a recipient completely,
     * where targetUser's shared annotations, images and tag sets are in a read-annotate group,
     * as well as leaving the otherImporter's images, which targetUser annotated, not transferred.
     * The target user has also additional data in a different group, which are to be transferred too.
     * Further, the test deals also with 2 users being passed to the argument of targetUsers (in 2 variations:
     * 2 users from the same group and 2 users from 2 different groups).
     * @param isDataOwner if the user submitting the {@link Chown2} request owns the data in the group
     * @param isAdmin if the user submitting the {@link Chown2} request is a member of the system group
     * @param isGroupOwner if the user submitting the {@link Chown2} request owns the group itself
     * @param isRecipientInGroup if the user receiving data by means of the {@link Chown2} request is a member of the data's group
     * @param isExpectSuccess if the chown is expected to succeed
     * @param option the child option to use in the tagset transfer
     * @throws Exception unexpected
     */
    @Test
    public void testChownAllBelongingToUserReadAnnotate() throws Exception {

        /* set up the users and group for this test case 
         * Note that two pairs of importers (1 and 2) are
         * necessary, because also the multi-argument for targetUser
         * (i.e. two users' data will be chowned in one command)
         * will be tested.*/
        final EventContext importerTargetUser1, otherImporter1, importerTargetUser2, otherImporter2;
        final EventContext chowner, recipient;
        final ExperimenterGroup dataGroup1, dataGroup2;
        final boolean isRecipientInGroup = false;
        final boolean isGroupOwner = false;


        importerTargetUser1 = newUserAndGroup("rwra--", false);
        importerTargetUser2 = newUserAndGroup("rwra--", false);

        final long dataGroupId1 = importerTargetUser1.groupId;
        dataGroup1 = new ExperimenterGroupI(dataGroupId1, false);
        otherImporter1 = newUserInGroup(dataGroup1, false);
        final long dataGroupId2 = importerTargetUser2.groupId;
        dataGroup2 = new ExperimenterGroupI(dataGroupId2, false);
        otherImporter2 = newUserInGroup(dataGroup2, false);
        recipient = newUserInGroup(isRecipientInGroup ? dataGroup2 : otherGroup, false);
        
        /* Add importerTargetUser1 also to "otherGroup" in order to be able
         * to test the case where a user has data in two different groups
         * in this test
         */

        addUsers(otherGroup, Collections.singletonList(importerTargetUser1.userId), false);

        /* make chowner an admin, other cases will not be tested 
         * in this test
         */
        chowner = newUserInGroup(dataGroup1, isGroupOwner);
        final boolean isAdmin = true;
        if (isAdmin) {
            addUsers(systemGroup, Collections.singletonList(chowner.userId), false);
        }

        /* note which objects will be used to annotate an image 
         * Note that two object sets are necessary, for the
         * two pairs of importers, denoted as 1 and 2*/
        final List<IObject> annotationsAndLinksOwnAnnForTripleLinking1;
        final List<IObject> annotationsAndLinksOwnToOthersImage1;
        final List<IObject> annotationsAndLinksOthersToOwnImage1;
        final List<IObject> annotationsAndLinksOthersAnnForTripleLinking1;
        final List<IObject> annotationsOwnForTripleLinking1 = new ArrayList<IObject>();
        final List<IObject> annotationsOthersForTripleLinking1 = new ArrayList<IObject>();
        final List<IObject> linksOwnToOwnAnnOwnImage1 = new ArrayList<IObject>();
        final List<IObject> linksOthersToOthersAnnOtherImage1 = new ArrayList<IObject>();
        final List<ImageAnnotationLink> linksOwnToOthersAnnOwnImage1 = new ArrayList<ImageAnnotationLink>();
        final List<ImageAnnotationLink> linksOwnToOthersAnnOthersImage1 = new ArrayList<ImageAnnotationLink>();
        final List<ImageAnnotationLink> linksOthersToOwnAnnOwnImage1 = new ArrayList<ImageAnnotationLink>();
        final List<ImageAnnotationLink> linksOthersToOwnAnnOthersImage1 = new ArrayList<ImageAnnotationLink>();
        
        /*now note the second set of the objects for image
         * annotations
         */
        final List<IObject> annotationsAndLinksOwnAnnForTripleLinking2;
        final List<IObject> annotationsAndLinksOwnToOthersImage2;
        final List<IObject> annotationsAndLinksOthersToOwnImage2;
        final List<IObject> annotationsAndLinksOthersAnnForTripleLinking2;
        final List<IObject> annotationsOwnForTripleLinking2 = new ArrayList<IObject>();
        final List<IObject> annotationsOthersForTripleLinking2 = new ArrayList<IObject>();
        final List<IObject> linksOwnToOwnAnnOwnImage2 = new ArrayList<IObject>();
        final List<IObject> linksOthersToOthersAnnOtherImage2 = new ArrayList<IObject>();
        final List<ImageAnnotationLink> linksOwnToOthersAnnOwnImage2 = new ArrayList<ImageAnnotationLink>();
        final List<ImageAnnotationLink> linksOwnToOthersAnnOthersImage2 = new ArrayList<ImageAnnotationLink>();
        final List<ImageAnnotationLink> linksOthersToOwnAnnOwnImage2 = new ArrayList<ImageAnnotationLink>();
        final List<ImageAnnotationLink> linksOthersToOwnAnnOthersImage2 = new ArrayList<ImageAnnotationLink>();

        /* First pair of importers imports their respective images, note
         * that importerTargetUser1 is member of 2 groups (dataGroup1 and otherGroup),
         * so care has to be taken to import an image for this user in 
         * each of his groups.*/
        init(importerTargetUser1);
        loginUser(dataGroup1);
        final Image image1 = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage()).proxy();
        final long imageId1 = image1.getId().getValue();
        testImages.add(imageId1);
        loginUser(otherGroup);
        final Image imageOtherGroup1 = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage()).proxy();
        final long imageOtherGroupId1 = imageOtherGroup1.getId().getValue();
        testImages.add(imageOtherGroupId1);
        init(otherImporter1);
        final Image otherImage1 = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage()).proxy();
        testImages.add(otherImage1.getId().getValue());

        /* Second pair of importers imports their respective images*/
        init(importerTargetUser2);
        final Image image2 = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage()).proxy();
        final long imageId2 = image2.getId().getValue();
        testImages.add(imageId2);
        init(otherImporter2);
        final Image otherImage2 = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage()).proxy();
        testImages.add(otherImage2.getId().getValue());

        /* First user/importer (importerTargetUser) annotates both images
         * (image, otherImage) which belongs to the first user/importer (importerTargetUser)
         * and the second user (otherImporter) respectively. 
         * Note that the "...ForTripleLinking" annotations have to be sorted out
         * from the links in the variable and later will be used to be linked again by
         * the other user to both first and second images (image and other image)
         * This procedure will be repeated for the second pair of users/importers.*/
        init(importerTargetUser1);
        annotationsAndLinksOwnAnnForTripleLinking1 = annotateImage(image1);
        annotationsAndLinksOwnToOthersImage1 = annotateImage(otherImage1);

        init(importerTargetUser2);
        annotationsAndLinksOwnAnnForTripleLinking2 = annotateImage(image2);
        annotationsAndLinksOwnToOthersImage2 = annotateImage(otherImage2);

        /* Now sort out the annotations from the links out of the 
         * annotationsAndLinksOwnAnnForTripleLinking bag of annotations and links,
         * and again do it for both sets of annotations, 1 and 2. */
        for (final IObject annotation : annotationsAndLinksOwnAnnForTripleLinking1) {
            if (annotation instanceof ImageAnnotationLink) {
                linksOwnToOwnAnnOwnImage1.add(annotation.proxy());
            } else {
                annotationsOwnForTripleLinking1.add(annotation.proxy());
            }
        }

        for (final IObject annotation : annotationsAndLinksOwnAnnForTripleLinking2) {
            if (annotation instanceof ImageAnnotationLink) {
                linksOwnToOwnAnnOwnImage2.add(annotation.proxy());
            } else {
                annotationsOwnForTripleLinking2.add(annotation.proxy());
            }
        }

        /* Another user (otherImporter) annotates both images
         * (image, otherImage) which belongs to the first user/importer (importerTargetUser)
         * and this second user (otherImporter) respectively.
         * Again, do this for both sets of annotations, 1 and 2.*/
        init(otherImporter1);
        annotationsAndLinksOthersToOwnImage1 = annotateImage(image1);
        annotationsAndLinksOthersAnnForTripleLinking1 = annotateImage(otherImage1);

        init(otherImporter2);
        annotationsAndLinksOthersToOwnImage2 = annotateImage(image2);
        annotationsAndLinksOthersAnnForTripleLinking2 = annotateImage(otherImage2);

        /* Now sort out the annotations from the links out of the 
         * annotationsAndLinksOthersAnnForTripleLinking bag of annotations and links
         * Again, do this for both sets of annotations, 1, and 2 */
        for (final IObject annotation : annotationsAndLinksOthersAnnForTripleLinking1) {
            if (annotation instanceof ImageAnnotationLink) {
                linksOthersToOthersAnnOtherImage1.add(annotation.proxy());
            } else {
                annotationsOthersForTripleLinking1.add(annotation.proxy());
            }
        }

        for (final IObject annotation : annotationsAndLinksOthersAnnForTripleLinking2) {
            if (annotation instanceof ImageAnnotationLink) {
                linksOthersToOthersAnnOtherImage2.add(annotation.proxy());
            } else {
                annotationsOthersForTripleLinking2.add(annotation.proxy());
            }
        }

        /* First user/importer (importerTargetUser) links the second users'
         * annotationsOthersForTripleLinking to the first image
         * (image) which belongs to the first user/importer (importerTargetUser)
         * as well as to the second image (otherImage) which belongs to the otherImporter,
         * thus making them triply linked by two different users to two differently owned
         * images. Note that ROI and thumbnail cannot be multiply linked, and thus will
         * remain just singly linked objects in the annotationsOthersForTripleLinking batch
         * of annotations.
         * Again, do the same sequence of linking for both sets of annotations/images, 1 and 2.*/
        init(importerTargetUser1);
        for (final IObject annotation : annotationsOthersForTripleLinking1) {
            if (!(annotation instanceof Roi) & !(annotation instanceof Thumbnail) & !(annotation instanceof RectangleI)) {
                final ImageAnnotationLink linkOwnImage = (ImageAnnotationLink) annotateImage(image1, (Annotation) annotation);
                linksOwnToOthersAnnOwnImage1.add((ImageAnnotationLink) linkOwnImage.proxy());
                final ImageAnnotationLink linkOtherImage = (ImageAnnotationLink) annotateImage(otherImage1, (Annotation) annotation);
                linksOwnToOthersAnnOthersImage1.add((ImageAnnotationLink) linkOtherImage.proxy());
            }
        }

        init(importerTargetUser2);
        for (final IObject annotation : annotationsOthersForTripleLinking2) {
            if (!(annotation instanceof Roi) & !(annotation instanceof Thumbnail) & !(annotation instanceof RectangleI)) {
                final ImageAnnotationLink linkOwnImage = (ImageAnnotationLink) annotateImage(image2, (Annotation) annotation);
                linksOwnToOthersAnnOwnImage2.add((ImageAnnotationLink) linkOwnImage.proxy());
                final ImageAnnotationLink linkOtherImage = (ImageAnnotationLink) annotateImage(otherImage2, (Annotation) annotation);
                linksOwnToOthersAnnOthersImage2.add((ImageAnnotationLink) linkOtherImage.proxy());
            }
        }

        /* Another user (otherImporter) links the first user/importer's 
         * annotationsOwnForTripleLinking to a second image (otherImage) 
         * which belongs to this other user (otherImporter).
         * as well as to the first image (image) which belongs to the importerTargetUser
         * Note that ROI and thumbnail cannot be multiply linked, and thus will
         * remain just singly linked objects in the annotationsOwnForTripleLinking batch
         * of annotations
         * Again, do the same sequence of linking for both sets of annotations/images, 1 and 2.*/
        init(otherImporter1);
        for (final IObject annotation : annotationsOwnForTripleLinking1) {
            if (!(annotation instanceof Roi) & !(annotation instanceof Thumbnail) & !(annotation instanceof RectangleI)) {
                final ImageAnnotationLink linkOtherImage = (ImageAnnotationLink) annotateImage(otherImage1, (Annotation) annotation);
                linksOthersToOwnAnnOthersImage1.add((ImageAnnotationLink) linkOtherImage.proxy());
                final ImageAnnotationLink linkOwnImage = (ImageAnnotationLink) annotateImage(image1, (Annotation) annotation);
                linksOthersToOwnAnnOwnImage1.add((ImageAnnotationLink) linkOwnImage.proxy());
            }
        }
        init(otherImporter2);
        for (final IObject annotation : annotationsOwnForTripleLinking2) {
            if (!(annotation instanceof Roi) & !(annotation instanceof Thumbnail) & !(annotation instanceof RectangleI)) {
                final ImageAnnotationLink linkOtherImage = (ImageAnnotationLink) annotateImage(otherImage2, (Annotation) annotation);
                linksOthersToOwnAnnOthersImage2.add((ImageAnnotationLink) linkOtherImage.proxy());
                final ImageAnnotationLink linkOwnImage = (ImageAnnotationLink) annotateImage(image2, (Annotation) annotation);
                linksOthersToOwnAnnOwnImage2.add((ImageAnnotationLink) linkOwnImage.proxy());
            }
        }

        /* create two tag sets, one as the first user
         * (importerTargetUser) and another one as the other user
         * and create two tags as the first user and one tag
         * as the other user */
        init(importerTargetUser1);
        final List<TagAnnotation> tagsetOwn = createTagsets(1);
        init(otherImporter1);
        final List<TagAnnotation> tagsetOthers = createTagsets(1);
        init(importerTargetUser1);
        final List<TagAnnotation> tagsOwn = createTags(2);
        init(otherImporter1);
        final List<TagAnnotation> tagsOthers = createTags(1);

        /* group the tagsets to one pot in order to be able to reuse the 
         * definteLinikingTags method
         */
        final List<TagAnnotation> tagsets = new ArrayList<TagAnnotation>();
        for (final TagAnnotation tagset : tagsetOwn) {
            tagsets.add(tagset);
        }
        for (final TagAnnotation tagset : tagsetOthers) {
            tagsets.add(tagset);
        }

        final List<TagAnnotation> tags = new ArrayList<TagAnnotation>();
        for (final TagAnnotation tag : tagsOwn) {
            tags.add(tag);
        }
        for (final TagAnnotation tag : tagsOthers) {
            tags.add(tag);
        }

        /* define how to link the tag sets to the tags and link them,
         * one own tag to the own tagset, the second own tag to the own
         * tagset and to the other user's tagset, and link the other user's
         * tag to the other users tagset (all being importerTargetUser, so all
         * links are "own" */
        init(importerTargetUser1);
        final SetMultimap<TagAnnotation, TagAnnotation> members = defineLinkingTags(tags, tagsets);
        linkTagsTagsets(members);

        /* chown all what belongs to importerTargetUser1 to recipient 
         * This chown has just one user (importerTargetUser1) in the argument*/

        init(chowner);
        Chown2 chown = Requests.chown().targetUsers(importerTargetUser1.userId).toUser(recipient.userId).build();
        doChange(client, factory, chown, true);

        /* check that the ownership of images is as expected,
         * start checking the unannotated image which importerTargetUser1
         * has in otherGroup. Later switch to dataGroup1, where all the other
         * data are and perform the remaining checks*/
        logRootIntoGroup(otherGroup.getId().getValue());
        assertOwnedBy(imageOtherGroup1, recipient);
        logRootIntoGroup(dataGroupId1);
        assertOwnedBy(image1, recipient);
        assertOwnedBy(otherImage1, otherImporter1);

        /* check that all the own (=belonging to targetUserImporter)
         * triply linked annotations and the own mixed bag of annotations
         * (singly linked) were transferred to recipient */
        assertOwnedBy(annotationsOwnForTripleLinking1, recipient);
        assertOwnedBy(annotationsAndLinksOwnToOthersImage1, recipient);

        /* check that all the others' (=belonging to otherImporter)
         * triply linked annotations and the others' mixed bag of annotations
         * (singly linked) are still belonging to otherImporter */
        assertOwnedBy(annotationsOthersForTripleLinking1, otherImporter1);
        assertOwnedBy(annotationsAndLinksOthersToOwnImage1, otherImporter1);

        /* check that all the own (=belonging to targetUserImporter) links
         * were transferred to recipient, irrespective of ownership of the objects
         * they were linking */
        assertOwnedBy(linksOwnToOwnAnnOwnImage1, recipient);
        assertOwnedBy(linksOwnToOthersAnnOthersImage1, recipient);
        assertOwnedBy(linksOwnToOthersAnnOwnImage1, recipient);

        /* check that all the others' (=belonging to otherImporter) links
         * still belong to otherImporter, irrespective of ownership of the objects
         * they were linking */
        assertOwnedBy(linksOthersToOthersAnnOtherImage1, otherImporter1);
        assertOwnedBy(linksOthersToOwnAnnOwnImage1, otherImporter1);
        assertOwnedBy(linksOthersToOwnAnnOthersImage1, otherImporter1);

         /* check that own tag set was transferred to recipient,
          * the others' tag set still belongs to other user */
        assertOwnedBy(tagsetOwn, recipient);
        assertOwnedBy(tagsetOthers, otherImporter1);

        /* check that own tags were transferred to recipient,
         * the others' tag still belongs to other user */
        assertOwnedBy(tagsOwn, recipient);
        assertOwnedBy(tagsOthers, otherImporter1);

        /* Test for targetUser method having two users. For that,
         * first chown everything from the "recipient" back to
         * importerTargerUser1, in order to get the original setup
         * with two users, two images and cross-linked annotations 
         */
        init(chowner);
        Chown2 chownBack = Requests.chown().targetUsers(recipient.userId).toUser(importerTargetUser1.userId).build();
        doChange(client, factory, chownBack, true);

        /* Now chown both importerTargetUser1's as well as 
         * importerTargetUser2's data in one go to recipient.
         * This covers a chown with 2 arguments (two target users)
         * where each of the users is in a different group. */
        init(chowner);
        Chown2 chownTwoUsers = Requests.chown().
                targetUsers(importerTargetUser1.userId, importerTargetUser2.userId).toUser(recipient.userId).build();
        doChange(client, factory, chownTwoUsers, true);
        
        /* check that the ownership of images is as expected*/
        logRootIntoGroup(otherGroup.getId().getValue());
        assertOwnedBy(imageOtherGroup1, recipient);
        logRootIntoGroup(dataGroupId1);
        assertOwnedBy(image1, recipient);
        logRootIntoGroup(dataGroupId2);
        assertOwnedBy(image2, recipient);
        
        /* check that all the own (=belonging to targetUserImporter)
         * triply linked annotations and the own mixed bag of annotations
         * (singly linked) were transferred to recipient 
         * Do this check in both groups for both sets of annotations, 1 and 2.*/
        logRootIntoGroup(dataGroupId1);
        assertOwnedBy(annotationsOwnForTripleLinking1, recipient);
        assertOwnedBy(annotationsAndLinksOwnToOthersImage1, recipient);
        logRootIntoGroup(dataGroupId2);
        assertOwnedBy(annotationsOwnForTripleLinking2, recipient);
        assertOwnedBy(annotationsAndLinksOwnToOthersImage2, recipient);
        
        /* check that all the others' (=belonging to otherImporter)
         * triply linked annotations and the others' mixed bag of annotations
         * (singly linked) are still belonging to otherImporter
         * Do this check in both groups for both sets of annotations, 1 and 2. */
        logRootIntoGroup(dataGroupId1);
        assertOwnedBy(annotationsOthersForTripleLinking1, otherImporter1);
        assertOwnedBy(annotationsAndLinksOthersToOwnImage1, otherImporter1);
        logRootIntoGroup(dataGroupId2);
        assertOwnedBy(annotationsOthersForTripleLinking2, otherImporter2);
        assertOwnedBy(annotationsAndLinksOthersToOwnImage2, otherImporter2);
        
        /* check that all the own (=belonging to targetUserImporter) links
         * were transferred to recipient, irrespective of ownership of the objects
         * they were linking 
         * Do this check in both groups for both sets of annotations, 1 and 2. */
        logRootIntoGroup(dataGroupId1);
        assertOwnedBy(linksOwnToOwnAnnOwnImage1, recipient);
        assertOwnedBy(linksOwnToOthersAnnOthersImage1, recipient);
        assertOwnedBy(linksOwnToOthersAnnOwnImage1, recipient);
        logRootIntoGroup(dataGroupId2);
        assertOwnedBy(linksOwnToOwnAnnOwnImage2, recipient);
        assertOwnedBy(linksOwnToOthersAnnOthersImage2, recipient);
        assertOwnedBy(linksOwnToOthersAnnOwnImage2, recipient);
        
        /* check that all the others' (=belonging to otherImporter) links
         * still belong to otherImporter, irrespective of ownership of the objects
         * they were linking
         * Do this check in both groups for both sets of annotations, 1 and 2. */
        logRootIntoGroup(dataGroupId1);
        assertOwnedBy(linksOthersToOthersAnnOtherImage1, otherImporter1);
        assertOwnedBy(linksOthersToOwnAnnOwnImage1, otherImporter1);
        assertOwnedBy(linksOthersToOwnAnnOthersImage1, otherImporter1);
        logRootIntoGroup(dataGroupId2);
        assertOwnedBy(linksOthersToOthersAnnOtherImage2, otherImporter2);
        assertOwnedBy(linksOthersToOwnAnnOwnImage2, otherImporter2);
        assertOwnedBy(linksOthersToOwnAnnOthersImage2, otherImporter2);
    }
    
    

    /**
     * Test a specific case of using {@link Chown2} with owner's and others' annotations, including tag sets with
     * variously linked tags in a read-annotate group
     * @param isDataOwner if the user submitting the {@link Chown2} request owns the data in the group
     * @param isAdmin if the user submitting the {@link Chown2} request is a member of the system group
     * @param isGroupOwner if the user submitting the {@link Chown2} request owns the group itself
     * @param isRecipientInGroup if the user receiving data by means of the {@link Chown2} request is a member of the data's group
     * @param isExpectSuccess if the chown is expected to succeed
     * @param option the child option to use in the tagset transfer
     * @throws Exception unexpected
     */
    @Test(dataProvider = "chown annotation test cases")
    public void testChownAnnotationReadAnnotate(boolean isDataOwner, boolean isAdmin, boolean isGroupOwner,
            boolean isRecipientInGroup, boolean isExpectSuccess, Option option) throws Exception {

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

        /* have another user annotate the image */

        init(annotator);
        otherAnnotations = annotateImage(image);

        /* create two tag sets and three tags */
        final List<TagAnnotation> tagsets = createTagsets(2);
        final List<TagAnnotation> tags = createTags(3);

        /* define how to link the tag sets to the tags and link them */
        final SetMultimap<TagAnnotation, TagAnnotation> members = defineLinkingTags(tags, tagsets);
        linkTagsTagsets(members);

        /* chown the image */
        init(chowner);
        Chown2 chown = Requests.chown().target(image).toUser(recipient.userId).build();
        doChange(client, factory, chown, isExpectSuccess);

        if (!isExpectSuccess) {
            return;
        }

        /* check that the objects' ownership is all as expected */

        logRootIntoGroup(dataGroupId);
        assertOwnedBy(image, recipient);
        assertOwnedBy(ownerAnnotations, importer);
        assertOwnedBy(otherAnnotations, annotator);

        /* chown the first tag set */
        chown = Requests.chown().target(tagsets.get(0)).toUser(recipient.userId).build();

        switch (option) {
        case NONE:
            break;
        case INCLUDE:
            chown.childOptions = Collections.singletonList(Requests.option().includeType("Annotation").build());
            break;
        case EXCLUDE:
            chown.childOptions = Collections.singletonList(Requests.option().excludeType("Annotation").build());
            break;
        case BOTH:
            chown.childOptions = Collections.singletonList(Requests.option().includeType("Annotation")
                                                                            .excludeType("Annotation").build());
            break;
        default:
            Assert.fail("unexpected option for chown");
        }
        doChange(chown);

        /* check that the tag set is transferred and the other remains owned by original owner */
        assertOwnedBy(tagsets.get(0), recipient);
        assertOwnedBy(tagsets.get(1), annotator);

        /* check that only the expected tags are transferred */
        switch (option) {
        case NONE:
            assertOwnedBy(tags.get(0), annotator);
            assertOwnedBy(tags.get(1), annotator);
            assertOwnedBy(tags.get(2), annotator);
            break;
        case BOTH:
            /* include overrides exclude */
        case INCLUDE:
            assertOwnedBy(tags.get(0), recipient);
            assertOwnedBy(tags.get(1), recipient);
            assertOwnedBy(tags.get(2), annotator);
            break;
        case EXCLUDE:
            assertOwnedBy(tags.get(0), annotator);
            assertOwnedBy(tags.get(1), annotator);
            assertOwnedBy(tags.get(2), annotator);
            /* transfer the tag that is not in the second tag set */
            init(chowner);
            chown = Requests.chown().target(tags.get(0)).toUser(recipient.userId).build();
            doChange(chown);
            break;
        }

        /* transfer the second tag set */
        init(chowner);
        chown = Requests.chown().target(tagsets.get(1)).toUser(recipient.userId).build();
        doChange(chown);

        /* check that the tag sets are transferred */
        logRootIntoGroup(dataGroupId);
        assertOwnedBy(tagsets, recipient);
        /* check that the tag in the second tag set was not implicitly transferred */
        assertOwnedBy(tags.get(2), annotator);
        
    }

    /* child options to try using in transfer */
    private enum Option { NONE, INCLUDE, EXCLUDE, BOTH };

    /**
     * @return a variety of test cases for annotation chown
     */
    @DataProvider(name = "chown annotation test cases")
    public Object[][] provideChownAnnotationCases() {
        int index = 0;
        int count_value = 0;
        final int IS_DATA_OWNER = index++;
        final int IS_ADMIN = index++;
        final int IS_GROUP_OWNER = index++;
        final int IS_RECIPIENT_IN_GROUP = index++;
        final int IS_EXPECT_SUCCESS = index++;
        final int CHILD_OPTION = index++;

        final boolean[] booleanCases = new boolean[]{false, true};

        final Option[] values = Option.values();

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
                        /* only add one tag set child option per one permission
                         * test, but alternate over all child options doing this */
                        testCase[CHILD_OPTION] = values[count_value++ % values.length];
                        //DEBUG: if (isDataOwner == true && isAdmin == true && isGroupOwner == false &&
                        //           isRecipientInGroup == true && testCase[CHILD_OPTION] == Option.BOTH)
                        testCases.add(testCase);
                    }
                }
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * Test a specific case of using {@link Chown2} on an image that is in a dataset or a folder.
     * @param isImageOwner if the user who owns the container also owns the image
     * @param isLinkOwner if the user who owns the container also linked the image to the container
     * @param groupPermissions the permissions on the group in which the data exists
     * @param isInDataset if the image is in a dataset, otherwise a folder
     * @throws Exception unexpected
     */
    @Test(dataProvider = "chown container test cases")
    public void testChownImageInContainer(boolean isImageOwner, boolean isLinkOwner, String groupPermissions, boolean isInDataset)
            throws Exception {

        /* set up the users and group for this test case */

        final EventContext containerOwner, imageOwner, linkOwner, recipient;
        final ExperimenterGroup dataGroup;

        containerOwner = newUserAndGroup(groupPermissions, true);

        final long dataGroupId = containerOwner.groupId;
        dataGroup = new ExperimenterGroupI(dataGroupId, false);

        recipient = newUserInGroup(dataGroup, false);

        if (isImageOwner) {
            imageOwner = containerOwner;
            linkOwner = isLinkOwner ? containerOwner : newUserInGroup(dataGroup, false);
        } else {
            imageOwner = newUserInGroup(dataGroup, true);
            linkOwner = isLinkOwner ? containerOwner : imageOwner;
        }

        /* create a container */

        init(containerOwner);
        IObject container = isInDataset ? mmFactory.simpleDataset() : mmFactory.simpleFolder();
        container = iUpdate.saveAndReturnObject(container).proxy();

        /* create an image */

        init(imageOwner);
        final Image image = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage()).proxy();
        final long imageId = image.getId().getValue();
        testImages.add(imageId);

        /* move the image into the container */

        init(linkOwner);
        final IObject link;
        if (isInDataset) {
            final DatasetImageLink linkDI = new DatasetImageLinkI();
            linkDI.setParent((Dataset) container);
            linkDI.setChild(image);
            link = iUpdate.saveAndReturnObject(linkDI);
        } else {
            final FolderImageLink linkFI = new FolderImageLinkI();
            linkFI.setParent((Folder) container);
            linkFI.setChild(image);
            link = iUpdate.saveAndReturnObject(linkFI);
        }

        /* perform the chown */

        init(imageOwner);
        final Chown2 chown = Requests.chown().target(image).toUser(recipient.userId).build();
        doChange(client, factory, chown, true);

        /* check that the objects' ownership is all as expected */

        logRootIntoGroup(dataGroupId);
        assertOwnedBy(container, containerOwner);
        assertOwnedBy(image, recipient);
        final boolean isExpectLink = "rwrw--".equals(groupPermissions);
        if (isExpectLink) {
            assertExists(link);
            assertOwnedBy(link, linkOwner);
        } else {
            assertDoesNotExist(link);
        }
    }

    /**
     * Test a specific case of using {@link Chown2} on a dataset or a folder that contains an image.
     * @param isImageOwner if the user who owns the container also owns the image
     * @param isLinkOwner if the user who owns the container also linked the image to the container
     * @param groupPermissions the permissions on the group in which the data exists
     * @param isInDataset if the image is in a dataset, otherwise a folder
     * @throws Exception unexpected
     */
    @Test(dataProvider = "chown container test cases")
    public void testChownContainerWithImage(boolean isImageOwner, boolean isLinkOwner, String groupPermissions, boolean isInDataset)
            throws Exception {

        /* set up the users and group for this test case */

        final EventContext containerOwner, imageOwner, linkOwner, recipient;
        final ExperimenterGroup dataGroup;

        containerOwner = newUserAndGroup(groupPermissions, true);

        final long dataGroupId = containerOwner.groupId;
        dataGroup = new ExperimenterGroupI(dataGroupId, false);

        recipient = newUserInGroup(dataGroup, false);

        if (isImageOwner) {
            imageOwner = containerOwner;
            linkOwner = isLinkOwner ? containerOwner : newUserInGroup(dataGroup, false);
        } else {
            imageOwner = newUserInGroup(dataGroup, true);
            linkOwner = isLinkOwner ? containerOwner : imageOwner;
        }

        /* create a container */

        init(containerOwner);
        IObject container = isInDataset ? mmFactory.simpleDataset() : mmFactory.simpleFolder();
        container = iUpdate.saveAndReturnObject(container).proxy();

        /* create an image */

        init(imageOwner);
        final Image image = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage()).proxy();
        final long imageId = image.getId().getValue();
        testImages.add(imageId);

        /* move the image into the container */

        init(linkOwner);
        final IObject link;
        if (isInDataset) {
            final DatasetImageLink linkDI = new DatasetImageLinkI();
            linkDI.setParent((Dataset) container);
            linkDI.setChild(image);
            link = iUpdate.saveAndReturnObject(linkDI);
        } else {
            final FolderImageLink linkFI = new FolderImageLinkI();
            linkFI.setParent((Folder) container);
            linkFI.setChild(image);
            link = iUpdate.saveAndReturnObject(linkFI);
        }

        /* perform the chown */

        init(containerOwner);
        final Chown2 chown = Requests.chown().target(container).toUser(recipient.userId).build();
        doChange(client, factory, chown, true);

        /* check that the objects' ownership is all as expected */

        logRootIntoGroup(dataGroupId);
        assertOwnedBy(container, recipient);
        assertOwnedBy(image, isImageOwner ? recipient : imageOwner);
        assertOwnedBy(link, isImageOwner && isLinkOwner ? recipient : linkOwner);
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
        final int IS_IN_DATASET = index++;

        final boolean[] booleanCases = new boolean[]{false, true};
        final String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final boolean isImageOwner : booleanCases) {
            for (final boolean isLinkOwner : booleanCases) {
                for (final String groupPerms : permsCases) {
                    for (final boolean isInDataset : booleanCases) {
                        if (!(isImageOwner && isLinkOwner || "rwrw--".equals(groupPerms))) {
                            /* test case does not make sense */
                            continue;
                        }
                        final Object[] testCase = new Object[index];
                        testCase[IS_IMAGE_OWNER] = isImageOwner;
                        testCase[IS_LINK_OWNER] = isLinkOwner;
                        testCase[GROUP_PERMS] = groupPerms;
                        testCase[IS_IN_DATASET] = isInDataset;
                        // DEBUG: if (isImageOwner == true && isLinkOwner == true && "rwr---".equals(groupPerms)
                        //        && isInDataset = false)
                        testCases.add(testCase);
                    }
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

        /* another user projects the image */

        init(projectionOwner);
        Image projection = mmFactory.createImage();
        projection.setInstrument(instrument);
        projection = (Image) iUpdate.saveAndReturnObject(projection);
        final long projectionId = projection.getId().getValue();
        testImages.add(projectionId);
        projection = (Image) projection.proxy();

        /* chmod the group to the required permissions */

        logRootIntoGroup(dataGroupId);
        final Chmod2 chmod = Requests.chmod().target(dataGroup).toPerms(groupPermissions).build();
        doChange(client, factory, chmod, true);

        /* perform the chown */

        init(imageOwner);
        final Chown2 chown = Requests.chown().target(image).toUser(recipient.userId).build();
        doChange(client, factory, chown, true);

        /* check that the objects' ownership is all as expected */

        logRootIntoGroup(dataGroupId);
        assertOwnedBy(image, recipient);
        assertOwnedBy(projection, projectionOwner);
        assertOwnedBy(instrument, imageOwner);
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

        /* another user's image is part of the same experiment */

        init(otherImageOwner);
        Image otherImage = mmFactory.createImage();
        otherImage.setExperiment(experiment);
        otherImage = (Image) iUpdate.saveAndReturnObject(otherImage);
        final long otherImageId = otherImage.getId().getValue();
        testImages.add(otherImageId);
        otherImage = (Image) otherImage.proxy();

        /* chmod the group to the required permissions */

        logRootIntoGroup(dataGroupId);
        final Chmod2 chmod = Requests.chmod().target(dataGroup).toPerms(groupPermissions).build();
        doChange(client, factory, chmod, true);

        /* perform the chown */

        init(imageOwner);
        final Chown2 chown = Requests.chown().target(image).toUser(recipient.userId).build();
        doChange(client, factory, chown, true);

        /* check that the objects' ownership is all as expected */

        logRootIntoGroup(dataGroupId);
        assertOwnedBy(image, recipient);
        assertOwnedBy(otherImage, otherImageOwner);
        assertOwnedBy(experiment, imageOwner);
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

        /* the images should be owned by the dataset owner */

        logRootIntoGroup(dataGroupId);
        final Experimenter datasetOwnerActual = new ExperimenterI(datasetOwner.userId, false);
        final List<IObject> images = iQuery.findAllByQuery("FROM Image WHERE id IN (:ids)", new ParametersI().addIds(imageIds));
        Assert.assertEquals(images.size(), imageIds.size());
        for (final IObject image : images) {
            image.getDetails().setOwner(datasetOwnerActual);
        }
        iUpdate.saveCollection(images);

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

        /* check that the objects' ownership is all as expected */

        logRootIntoGroup(dataGroupId);
        assertOwnedBy(dataset, datasetOwner);
        assertOwnedBy(images, datasetOwner);
        assertOwnedBy(links, datasetOwner);
        assertOwnedBy(plate, plateOwner);

        /* perform the chown */

        init(datasetOwner);
        final Chown2 chown;

        switch (target) {
        case DATASET:
            chown = Requests.chown().target(dataset).toUser(recipient.userId).build();
            break;
        case IMAGES:
            chown = Requests.chown().target("Image").id(imageIds).toUser(recipient.userId).build();
            break;
        case PLATE:
            chown = Requests.chown().target(plate).toUser(recipient.userId).build();
            break;
        default:
            chown = null;
            Assert.fail("unexpected target for chown");
        }

        doChange(client, factory, chown, true);

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
            break;
        }

        /* delete the objects as clean-up */

        final Delete2 delete = new Delete2();
        delete.targetObjects = ImmutableMap.of(
                "Dataset", Collections.singletonList(datasetId),
                "Plate", Collections.singletonList(plateId));
        doChange(client, factory, delete, true);
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

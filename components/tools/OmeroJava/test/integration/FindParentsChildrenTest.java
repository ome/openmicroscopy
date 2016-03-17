/*
 * Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
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

package integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import omero.ServerError;
import omero.cmd.FindChildren;
import omero.cmd.FindParents;
import omero.cmd.FoundChildren;
import omero.cmd.FoundParents;
import omero.gateway.util.Requests;
import omero.gateway.util.Requests.Delete2Builder;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Folder;
import omero.model.FolderImageLink;
import omero.model.FolderImageLinkI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.Plate;
import omero.model.Roi;
import omero.model.RoiAnnotationLink;
import omero.model.RoiAnnotationLinkI;
import omero.model.RoiI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;

/**
 * Test the requests for finding the parents and children of model objects.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.3.0
 */
public class FindParentsChildrenTest extends AbstractServerTest {
    private final List<Dataset> datasets = new ArrayList<Dataset>();
    private final List<Folder> folders = new ArrayList<Folder>();
    private final List<Plate> plates = new ArrayList<Plate>();
    private final List<Image> images = new ArrayList<Image>();
    private final List<Roi> rois = new ArrayList<Roi>();
    private final List<CommentAnnotation> comments = new ArrayList<CommentAnnotation>();
    private final List<TagAnnotation> tags = new ArrayList<TagAnnotation>();

    /**
     * Persist the given objects and replace their list elements with the corresponding proxy object.
     * @param objects new model objects
     * @throws ServerError unexpected
     */
    private <X extends IObject> void saveObjects(List<X> objects) throws ServerError {
        for (int index = 0; index < objects.size(); index++) {
            objects.set(index, (X) iUpdate.saveAndReturnObject(objects.get(index)).proxy());
        }
    }

    /**
     * Create a linked graph of model objects to use in testing.
     * Creates two datasets, three folders, and three images:
     * the first and second images in the first dataset and folder, the second and third images in the second dataset and folder.
     * The third folder contains the other two folders.
     * Each image has two ROIs. Each image has a comment and each ROI has a tag.
     * @throws Exception unexpected
     */
    @BeforeClass
    public void setup() throws Exception {
        newUserAndGroup("rw----");

        for (int count = 2; count > 0; count--) {
            datasets.add(mmFactory.simpleDataset());
        }
        for (int count = datasets.size() + 1; count > 0; count--) {
            folders.add(mmFactory.simpleFolder());
        }
        for (int count = datasets.size() + 1; count > 0; count--) {
            images.add(mmFactory.simpleImage());
        }
        for (int count = images.size() * 2; count > 0; count--) {
            rois.add(new RoiI());
        }
        for (int count = images.size(); count > 0; count--) {
            comments.add(new CommentAnnotationI());
        }
        for (int count = rois.size(); count > 0; count--) {
            tags.add(new TagAnnotationI());
        }

        saveObjects(datasets);
        saveObjects(folders);
        saveObjects(images);
        saveObjects(rois);
        saveObjects(comments);
        saveObjects(tags);

        final List<IObject> links = new ArrayList<IObject>();

        for (int datasetIndex = 0; datasetIndex < datasets.size(); datasetIndex++) {
            for (int childOffset = 0; childOffset < 2; childOffset++) {
                final DatasetImageLink dil = new DatasetImageLinkI();
                dil.setParent(datasets.get(datasetIndex));
                dil.setChild(images.get(datasetIndex + childOffset));
                links.add(dil);
            }
        }

        for (int folderIndex = 0; folderIndex < datasets.size(); folderIndex++) {
            for (int childOffset = 0; childOffset < 2; childOffset++) {
                final FolderImageLink fil = new FolderImageLinkI();
                fil.setParent(folders.get(folderIndex));
                fil.setChild(images.get(folderIndex + childOffset));
                links.add(fil);
            }
        }

        for (int roiIndex = 0; roiIndex < rois.size(); roiIndex++) {
            final Roi roi = (Roi) iQuery.get("Roi", rois.get(roiIndex).getId().getValue());
            roi.setImage(images.get(roiIndex / 2));
            links.add(roi);
        }

        for (int imageIndex = 0; imageIndex < images.size(); imageIndex++) {
            final ImageAnnotationLink ial = new ImageAnnotationLinkI();
            ial.setParent(images.get(imageIndex));
            ial.setChild(comments.get(imageIndex));
            links.add(ial);
        }

        for (int roiIndex = 0; roiIndex < rois.size(); roiIndex++) {
            final RoiAnnotationLink ral = new RoiAnnotationLinkI();
            ral.setParent(rois.get(roiIndex));
            ral.setChild(tags.get(roiIndex));
            links.add(ral);
        }

        iUpdate.saveCollection(links);

        for (int folderIndex = 0; folderIndex < datasets.size(); folderIndex++) {
            final Folder folder = (Folder) iQuery.get("Folder", folders.get(folderIndex).getId().getValue());
            folder.setParentFolder(folders.get(datasets.size()));
            iUpdate.saveObject(folder);
        }
    }

    /**
     * Delete the model objects created by {@link #setup()} once the tests are finished.
     * @throws Exception unexpected
     */
    @AfterClass
    public void teardown() throws Exception {
        final Delete2Builder deleter = Requests.delete();
        for (final List<? extends IObject> objects : Arrays.asList(datasets, folders, plates, images, rois, comments, tags)) {
            for (final IObject object : objects) {
                deleter.target(object);
            }
        }
        doChange(deleter.build());
    }

    /**
     * Find the image that a ROI is in.
     * @throws Exception unexpected
     */
    @Test
    public void testFindImageOfRoiSingle() throws Exception {
        final FindParents finder = Requests.findParents().target(rois.get(5)).parentType("Image").build();
        final FoundParents found = (FoundParents) doChange(finder);
        final List<Long> foundImages = found.parents.remove(ome.model.core.Image.class.getName());
        Assert.assertNotNull(foundImages);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundImages, Arrays.asList(images.get(2).getId().getValue())));
        Assert.assertTrue(found.parents.isEmpty());
    }

    /**
     * Find the ROIs of an image.
     * @throws Exception unexpected
     */
    @Test
    public void testFindRoisOfImageSingle() throws Exception {
        final FindChildren finder = Requests.findChildren().target(images.get(2)).childType("Roi").build();
        final FoundChildren found = (FoundChildren) doChange(finder);
        final List<Long> foundRois = found.children.remove(ome.model.roi.Roi.class.getName());
        Assert.assertNotNull(foundRois);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundRois, Arrays.asList(rois.get(4).getId().getValue(),
                                                                                     rois.get(5).getId().getValue())));
        Assert.assertTrue(found.children.isEmpty());
    }

    /**
     * Find the images that ROIs are in.
     * @throws Exception unexpected
     */
    @Test
    public void testFindImageOfRoiMultiple() throws Exception {
        final FindParents finder = Requests.findParents().target(rois.get(1), rois.get(2)).parentType("Image").build();
        final FoundParents found = (FoundParents) doChange(finder);
        final List<Long> foundImages = found.parents.remove(ome.model.core.Image.class.getName());
        Assert.assertNotNull(foundImages);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundImages, Arrays.asList(images.get(0).getId().getValue(),
                                                                                       images.get(1).getId().getValue())));
        Assert.assertTrue(found.parents.isEmpty());
    }

    /**
     * Find the ROIs of images.
     * @throws Exception unexpected
     */
    @Test
    public void testFindRoisOfImageMultiple() throws Exception {
        final FindChildren finder = Requests.findChildren().target(images.get(0), images.get(2)).childType("Roi").build();
        final FoundChildren found = (FoundChildren) doChange(finder);
        final List<Long> foundRois = found.children.remove(ome.model.roi.Roi.class.getName());
        Assert.assertNotNull(foundRois);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundRois, Arrays.asList(rois.get(0).getId().getValue(),
                                                                                     rois.get(1).getId().getValue(),
                                                                                     rois.get(4).getId().getValue(),
                                                                                     rois.get(5).getId().getValue())));
        Assert.assertTrue(found.children.isEmpty());
    }

    /**
     * Find the datasets that an image is in.
     * @throws Exception unexpected
     */
    @Test
    public void testFindDatasetsOfImageSingle() throws Exception {
        final FindParents finder = Requests.findParents().target(images.get(1)).parentType("Dataset").build();
        final FoundParents found = (FoundParents) doChange(finder);
        final List<Long> foundDatasets = found.parents.remove(ome.model.containers.Dataset.class.getName());
        Assert.assertNotNull(foundDatasets);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundDatasets, Arrays.asList(datasets.get(0).getId().getValue(),
                                                                                         datasets.get(1).getId().getValue())));
        Assert.assertTrue(found.parents.isEmpty());
    }

    /**
     * Find the images of a dataset.
     * @throws Exception unexpected
     */
    @Test
    public void testFindImagesInDatasetSingle() throws Exception {
        final FindChildren finder = Requests.findChildren().target(datasets.get(1)).childType("Image").build();
        final FoundChildren found = (FoundChildren) doChange(finder);
        final List<Long> foundImages = found.children.remove(ome.model.core.Image.class.getName());
        Assert.assertNotNull(foundImages);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundImages, Arrays.asList(images.get(1).getId().getValue(),
                                                                                       images.get(2).getId().getValue())));
        Assert.assertTrue(found.children.isEmpty());
    }

    /**
     * Find the datasets that images are in.
     * @throws Exception unexpected
     */
    @Test
    public void testFindDatasetsOfImageMultiple() throws Exception {
        final FindParents finder = Requests.findParents().target(images.get(0), images.get(1)).parentType("Dataset").build();
        final FoundParents found = (FoundParents) doChange(finder);
        final List<Long> foundDatasets = found.parents.remove(ome.model.containers.Dataset.class.getName());
        Assert.assertNotNull(foundDatasets);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundDatasets, Arrays.asList(datasets.get(0).getId().getValue(),
                                                                                         datasets.get(1).getId().getValue())));
        Assert.assertTrue(found.parents.isEmpty());
    }

    /**
     * Find the images of datasets.
     * @throws Exception unexpected
     */
    @Test
    public void testFindImagesInDatasetMultiple() throws Exception {
        final FindChildren finder = Requests.findChildren().target(datasets.get(0), datasets.get(1)).childType("Image").build();
        final FoundChildren found = (FoundChildren) doChange(finder);
        final List<Long> foundImages = found.children.remove(ome.model.core.Image.class.getName());
        Assert.assertNotNull(foundImages);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundImages, Arrays.asList(images.get(0).getId().getValue(),
                                                                                       images.get(1).getId().getValue(),
                                                                                       images.get(2).getId().getValue())));
        Assert.assertTrue(found.children.isEmpty());
    }

    /**
     * Find the datasets that an image is in.
     * Ignore any ROIs.
     * @throws Exception unexpected
     */
    @Test
    public void testFindDatasetsOfImageStopBeforeRoi() throws Exception {
        final FindParents finder = Requests.findParents().target(images.get(1)).parentType("Dataset").stopBefore("Roi").build();
        final FoundParents found = (FoundParents) doChange(finder);
        final List<Long> foundDatasets = found.parents.remove(ome.model.containers.Dataset.class.getName());
        Assert.assertNotNull(foundDatasets);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundDatasets, Arrays.asList(datasets.get(0).getId().getValue(),
                                                                                         datasets.get(1).getId().getValue())));
        Assert.assertTrue(found.parents.isEmpty());
    }

    /**
     * Find the images of a dataset.
     * Ignore any ROIs.
     * @throws Exception unexpected
     */
    @Test
    public void testFindImagesInDatasetStopBeforeRoi() throws Exception {
        final FindChildren finder = Requests.findChildren().target(datasets.get(1)).childType("Image").stopBefore("Roi").build();
        final FoundChildren found = (FoundChildren) doChange(finder);
        final List<Long> foundImages = found.children.remove(ome.model.core.Image.class.getName());
        Assert.assertNotNull(foundImages);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundImages, Arrays.asList(images.get(1).getId().getValue(),
                                                                                       images.get(2).getId().getValue())));
        Assert.assertTrue(found.children.isEmpty());
    }

    /**
     * Find the datasets that an image is in.
     * Ignore any datasets.
     * @throws Exception unexpected
     */
    @Test
    public void testFindDatasetsOfImageStopBeforeDataset() throws Exception {
        final FindParents finder = Requests.findParents().target(images.get(1)).parentType("Dataset").stopBefore("Dataset").build();
        final FoundParents found = (FoundParents) doChange(finder);
        Assert.assertTrue(found.parents.isEmpty());
    }

    /**
     * Find the images of a dataset.
     * Ignore any images.
     * @throws Exception unexpected
     */
    @Test
    public void testFindImagesInDatasetStopBeforeImage() throws Exception {
        final FindChildren finder = Requests.findChildren().target(datasets.get(1)).childType("Image").stopBefore("Image").build();
        final FoundChildren found = (FoundChildren) doChange(finder);
        Assert.assertTrue(found.children.isEmpty());
    }

    /**
     * Find the datasets that a ROI is in.
     * @throws Exception unexpected
     */
    @Test
    public void testFindDatasetsOfRoi() throws Exception {
        final FindParents finder = Requests.findParents().target(rois.get(3)).parentType("Dataset").build();
        final FoundParents found = (FoundParents) doChange(finder);
        final List<Long> foundDatasets = found.parents.remove(ome.model.containers.Dataset.class.getName());
        Assert.assertNotNull(foundDatasets);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundDatasets, Arrays.asList(datasets.get(0).getId().getValue(),
                                                                                         datasets.get(1).getId().getValue())));
        Assert.assertTrue(found.parents.isEmpty());
    }

    /**
     * Find a dataset's ROIs.
     * @throws Exception unexpected
     */
    @Test
    public void testFindRoisInDataset() throws Exception {
        final FindChildren finder = Requests.findChildren().target(datasets.get(0)).childType("Roi").build();
        final FoundChildren found = (FoundChildren) doChange(finder);
        final List<Long> foundImages = found.children.remove(ome.model.roi.Roi.class.getName());
        Assert.assertNotNull(foundImages);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundImages, Arrays.asList(rois.get(0).getId().getValue(),
                                                                                       rois.get(1).getId().getValue(),
                                                                                       rois.get(2).getId().getValue(),
                                                                                       rois.get(3).getId().getValue())));
        Assert.assertTrue(found.children.isEmpty());
    }

    /**
     * Find the datasets that a ROI is in.
     * Ignore any images.
     * @throws Exception unexpected
     */
    @Test
    public void testFindDatasetsOfRoiStopBeforeImage() throws Exception {
        final FindParents finder = Requests.findParents().target(rois.get(3)).parentType("Dataset").stopBefore("Image").build();
        final FoundParents found = (FoundParents) doChange(finder);
        Assert.assertTrue(found.parents.isEmpty());
    }

    /**
     * Find a dataset's ROIs.
     * Ignore any images.
     * @throws Exception unexpected
     */
    @Test
    public void testFindRoisInDatasetStopBeforeImage() throws Exception {
        final FindChildren finder = Requests.findChildren().target(datasets.get(0)).childType("Roi").stopBefore("Image").build();
        final FoundChildren found = (FoundChildren) doChange(finder);
        Assert.assertTrue(found.children.isEmpty());
    }

    /**
     * Find the datasets that a comment is in.
     * @throws Exception unexpected
     */
    @Test
    public void testFindDatasetsOfComment() throws Exception {
        final FindParents finder = Requests.findParents().target(comments.get(2)).parentType("Dataset").build();
        final FoundParents found = (FoundParents) doChange(finder);
        final List<Long> foundDatasets = found.parents.remove(ome.model.containers.Dataset.class.getName());
        Assert.assertNotNull(foundDatasets);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundDatasets, Arrays.asList(datasets.get(1).getId().getValue())));
        Assert.assertTrue(found.parents.isEmpty());
    }

    /**
     * Find a dataset's comments.
     * @throws Exception unexpected
     */
    @Test
    public void testFindCommentsOfDataset() throws Exception {
        final FindChildren finder = Requests.findChildren().target(datasets.get(0)).childType("CommentAnnotation").build();
        final FoundChildren found = (FoundChildren) doChange(finder);
        final List<Long> foundComments = found.children.remove(ome.model.annotations.CommentAnnotation.class.getName());
        Assert.assertNotNull(foundComments);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundComments, Arrays.asList(comments.get(0).getId().getValue(),
                                                                                         comments.get(1).getId().getValue())));
        Assert.assertTrue(found.children.isEmpty());
    }

    /**
     * Find the images that a comment is in.
     * @throws Exception unexpected
     */
    @Test
    public void testFindImagesOfComment() throws Exception {
        final FindParents finder = Requests.findParents().target(comments.get(1)).parentType("Image").build();
        final FoundParents found = (FoundParents) doChange(finder);
        final List<Long> foundImages = found.parents.remove(ome.model.core.Image.class.getName());
        Assert.assertNotNull(foundImages);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundImages, Arrays.asList(images.get(1).getId().getValue())));
        Assert.assertTrue(found.parents.isEmpty());
    }

    /**
     * Find the images that a tag is in.
     * @throws Exception unexpected
     */
    @Test
    public void testFindImagesOfTag() throws Exception {
        final FindParents finder = Requests.findParents().target(tags.get(1)).parentType("Image").build();
        final FoundParents found = (FoundParents) doChange(finder);
        final List<Long> foundImages = found.parents.remove(ome.model.core.Image.class.getName());
        Assert.assertNotNull(foundImages);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundImages, Arrays.asList(images.get(0).getId().getValue())));
        Assert.assertTrue(found.parents.isEmpty());
    }

    /**
     * Find an image's annotations.
     * @throws Exception unexpected
     */
    @Test
    public void testFindAnnotationsOfImage() throws Exception {
        final FindChildren finder = Requests.findChildren().target(images.get(1)).childType("Annotation").build();
        final FoundChildren found = (FoundChildren) doChange(finder);
        final List<Long> foundComments = found.children.remove(ome.model.annotations.CommentAnnotation.class.getName());
        Assert.assertNotNull(foundComments);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundComments, Arrays.asList(comments.get(1).getId().getValue())));
        final List<Long> foundTags = found.children.remove(ome.model.annotations.TagAnnotation.class.getName());
        Assert.assertNotNull(foundTags);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundTags, Arrays.asList(tags.get(2).getId().getValue(),
                                                                                     tags.get(3).getId().getValue())));
        Assert.assertTrue(found.children.isEmpty());
    }

    /**
     * Find the images that a comment is in.
     * Ignore any ROIs.
     * @throws Exception unexpected
     */
    @Test
    public void testFindImagesOfCommentStopBeforeRoi() throws Exception {
        final FindParents finder = Requests.findParents().target(comments.get(1)).parentType("Image").stopBefore("Roi").build();
        final FoundParents found = (FoundParents) doChange(finder);
        final List<Long> foundImages = found.parents.remove(ome.model.core.Image.class.getName());
        Assert.assertNotNull(foundImages);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundImages, Arrays.asList(images.get(1).getId().getValue())));
        Assert.assertTrue(found.parents.isEmpty());
    }

    /**
     * Find the images that a tag is in.
     * Ignore any ROIs.
     * @throws Exception unexpected
     */
    @Test
    public void testFindImagesOfTagStopBeforeRoi() throws Exception {
        final FindParents finder = Requests.findParents().target(tags.get(1)).parentType("Image").stopBefore("Roi").build();
        final FoundParents found = (FoundParents) doChange(finder);
        Assert.assertTrue(found.parents.isEmpty());
    }

    /**
     * Find an image's annotations.
     * Ignore any ROIs.
     * @throws Exception unexpected
     */
    @Test
    public void testFindAnnotationsOfImageStopBeforeRoi() throws Exception {
        final FindChildren finder = Requests.findChildren().target(images.get(1)).childType("Annotation").stopBefore("Roi").build();
        final FoundChildren found = (FoundChildren) doChange(finder);
        final List<Long> foundComments = found.children.remove(ome.model.annotations.CommentAnnotation.class.getName());
        Assert.assertNotNull(foundComments);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundComments, Arrays.asList(comments.get(1).getId().getValue())));
        Assert.assertTrue(found.children.isEmpty());
    }

    /**
     * Find various components of plate structure: contained by a plate; containing the fields' images.
     * @throws Exception unexpected
     */
    @Test
    public void testFindVariousInPlate() throws Exception {
        List<Long> foundPlates, foundRuns, foundWells, foundFields, foundImages;
        plates.add((Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(2, 2, 2, 2, false)).proxy());

        final FindChildren childFinder = Requests.findChildren().target(plates.get(0))
                .childType("PlateAcquisition", "Well", "WellSample", "Image").build();
        final FoundChildren foundChildren = (FoundChildren) doChange(childFinder);
        foundRuns = foundChildren.children.remove(ome.model.screen.PlateAcquisition.class.getName());
        Assert.assertNotNull(foundRuns);
        Assert.assertEquals(foundRuns.size(), 2);
        foundWells = foundChildren.children.remove(ome.model.screen.Well.class.getName());
        Assert.assertNotNull(foundWells);
        Assert.assertEquals(foundWells.size(), 4);
        foundFields = foundChildren.children.remove(ome.model.screen.WellSample.class.getName());
        Assert.assertNotNull(foundFields);
        Assert.assertEquals(foundFields.size(), 16);
        foundImages = foundChildren.children.remove(ome.model.core.Image.class.getName());
        Assert.assertNotNull(foundImages);
        Assert.assertEquals(foundImages.size(), 16);
        Assert.assertTrue(foundChildren.children.isEmpty());

        final FindParents parentFinder = Requests.findParents().target("Image").id(foundImages)
                .parentType("Plate", "PlateAcquisition", "Well", "WellSample").build();
        final FoundParents foundParents = (FoundParents) doChange(parentFinder);
        foundPlates = foundParents.parents.remove(ome.model.screen.Plate.class.getName());
        Assert.assertNotNull(foundPlates);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundPlates, Arrays.asList(plates.get(0).getId().getValue())));
        foundRuns = foundParents.parents.remove(ome.model.screen.PlateAcquisition.class.getName());
        Assert.assertNotNull(foundRuns);
        Assert.assertEquals(foundRuns.size(), 2);
        foundWells = foundParents.parents.remove(ome.model.screen.Well.class.getName());
        Assert.assertNotNull(foundWells);
        Assert.assertEquals(foundWells.size(), 4);
        foundFields = foundParents.parents.remove(ome.model.screen.WellSample.class.getName());
        Assert.assertNotNull(foundFields);
        Assert.assertEquals(foundFields.size(), 16);
        Assert.assertTrue(foundParents.parents.isEmpty());
    }

    /**
     * Find the folders that an image is in.
     * @throws Exception unexpected
     */
    @Test
    public void testFindFoldersOfImage() throws Exception {
        final FindParents finder = Requests.findParents().target(images.get(1)).parentType("Folder").build();
        final FoundParents found = (FoundParents) doChange(finder);
        final List<Long> foundFolders = found.parents.remove(ome.model.containers.Folder.class.getName());
        Assert.assertNotNull(foundFolders);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundFolders, Arrays.asList(folders.get(0).getId().getValue(),
                                                                                        folders.get(1).getId().getValue(),
                                                                                        folders.get(2).getId().getValue())));

        Assert.assertTrue(found.parents.isEmpty());
    }

    /**
     * Find the images of a folder.
     * The images are in the given folder.
     * @throws Exception unexpected
     */
    @Test
    public void testFindImagesInFolderDirect() throws Exception {
        final FindChildren finder = Requests.findChildren().target(folders.get(1)).childType("Image").build();
        final FoundChildren found = (FoundChildren) doChange(finder);
        final List<Long> foundImages = found.children.remove(ome.model.core.Image.class.getName());
        Assert.assertNotNull(foundImages);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundImages, Arrays.asList(images.get(1).getId().getValue(),
                                                                                       images.get(2).getId().getValue())));
        Assert.assertTrue(found.children.isEmpty());
    }

    /**
     * Find the images of a folder.
     * The images are in subfolders of the given folder.
     * @throws Exception unexpected
     */
    @Test
    public void testFindImagesInFolderRecursive() throws Exception {
        final FindChildren finder = Requests.findChildren().target(folders.get(2)).childType("Image").build();
        final FoundChildren found = (FoundChildren) doChange(finder);
        final List<Long> foundImages = found.children.remove(ome.model.core.Image.class.getName());
        Assert.assertNotNull(foundImages);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundImages, Arrays.asList(images.get(0).getId().getValue(),
                                                                                       images.get(1).getId().getValue(),
                                                                                       images.get(2).getId().getValue())));
        Assert.assertTrue(found.children.isEmpty());
    }

    /**
     * Find the folders that a ROI is in.
     * @throws Exception unexpected
     */
    @Test
    public void testFindFoldersOfRoi() throws Exception {
        final FindParents finder = Requests.findParents().target(rois.get(3)).parentType("Folder").build();
        final FoundParents found = (FoundParents) doChange(finder);
        final List<Long> foundFolders = found.parents.remove(ome.model.containers.Folder.class.getName());
        Assert.assertNotNull(foundFolders);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundFolders, Arrays.asList(folders.get(0).getId().getValue(),
                                                                                        folders.get(1).getId().getValue(),
                                                                                        folders.get(2).getId().getValue())));
        Assert.assertTrue(found.parents.isEmpty());
    }

    /**
     * Find a folder's ROIs.
     * @throws Exception unexpected
     */
    @Test
    public void testFindRoisInFolder() throws Exception {
        final FindChildren finder = Requests.findChildren().target(folders.get(0)).childType("Roi").build();
        final FoundChildren found = (FoundChildren) doChange(finder);
        final List<Long> foundImages = found.children.remove(ome.model.roi.Roi.class.getName());
        Assert.assertNotNull(foundImages);
        Assert.assertTrue(CollectionUtils.isEqualCollection(foundImages, Arrays.asList(rois.get(0).getId().getValue(),
                                                                                       rois.get(1).getId().getValue(),
                                                                                       rois.get(2).getId().getValue(),
                                                                                       rois.get(3).getId().getValue())));
        Assert.assertTrue(found.children.isEmpty());
    }

    /**
     * Do not specify which types of image container to find.
     * The search should fail.
     * @throws Exception unexpected
     */
    @Test
    public void testMissingParentTypes() throws Exception {
        final FindParents finder = Requests.findParents().target(images.get(0)).build();
        doChange(client, factory, finder, false);
    }

    /**
     * Do not specify which types of image contents to find.
     * The search should fail.
     * @throws Exception unexpected
     */
    @Test
    public void testMissingChildTypes() throws Exception {
        final FindChildren finder = Requests.findChildren().target(images.get(0)).build();
        doChange(client, factory, finder, false);
    }

    /**
     * Specify to find image containers that are not all mapped by the finder's hierarchy.
     * The search should fail.
     * @throws Exception unexpected
     */
    @Test
    public void testUnknownParentTypes() throws Exception {
        final FindParents finder = Requests.findParents().target(images.get(0)).parentType("Project", "Event").build();
        doChange(client, factory, finder, false);
    }

    /**
     * Specify to find image contents that are not all mapped by the finder's hierarchy.
     * The search should fail.
     * @throws Exception unexpected
     */
    @Test
    public void testUnknownChildTypes() throws Exception {
        final FindChildren finder = Requests.findChildren().target(images.get(0)).childType("Detector", "Event").build();
        doChange(client, factory, finder, false);
    }
}

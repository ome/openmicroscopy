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

package integration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import omero.RLong;
import omero.RString;
import omero.RType;
import omero.ServerError;
import omero.cmd.Delete2;
import omero.cmd.Duplicate;
import omero.cmd.DuplicateResponse;
import omero.cmd.ERR;
import omero.cmd.Response;
import omero.cmd.SkipHead;
import omero.gateway.util.Requests;
import omero.gateway.util.Requests.DuplicateBuilder;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLink;
import omero.model.AnnotationAnnotationLinkI;
import omero.model.Channel;
import omero.model.Dataset;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.DoubleAnnotationI;
import omero.model.Ellipse;
import omero.model.EllipseI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Folder;
import omero.model.FolderImageLink;
import omero.model.FolderImageLinkI;
import omero.model.FolderRoiLink;
import omero.model.FolderRoiLinkI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.Line;
import omero.model.LineI;
import omero.model.LogicalChannel;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.MapAnnotationI;
import omero.model.Pixels;
import omero.model.PlaneInfo;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.Point;
import omero.model.PointI;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.Rectangle;
import omero.model.RectangleI;
import omero.model.Roi;
import omero.model.RoiI;
import omero.model.Screen;
import omero.model.ScreenPlateLink;
import omero.model.ScreenPlateLinkI;
import omero.model.Shape;
import omero.model.StatsInfo;
import omero.model.TagAnnotationI;
import omero.model.TextAnnotation;
import omero.model.Well;
import omero.model.WellSample;
import omero.model.XmlAnnotationI;
import omero.sys.EventContext;
import omero.sys.Parameters;
import omero.sys.ParametersI;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

/**
 * Tests that {@link Duplicate} behaves as expected.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.2.1
 */
public class DuplicationTest extends AbstractServerTest {

    /**
     * A <em>Z</em>, <em>T</em>, <em>C</em> key for indexing {@link PlaneInfo} instances in hash tables.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.2.1
     */
    private static class PlaneInfoKey {

        private final int z, t, c;

        /**
         * Construct a new key.
         * @param planeInfo the {@link PlaneInfo} to which this key is to correspond
         */
        PlaneInfoKey(PlaneInfo planeInfo) {
            this.z = planeInfo.getTheZ().getValue();
            this.t = planeInfo.getTheT().getValue();
            this.c = planeInfo.getTheC().getValue();
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            } else if (object instanceof PlaneInfoKey) {
                final PlaneInfoKey other = (PlaneInfoKey) object;
                return this.z == other.z && this.t == other.t && this.c == other.c;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getClass(), z, t, c);
        }
    }

    private final List<Long> testImages = Collections.synchronizedList(new ArrayList<Long>());

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
     * Assert that the given instances have the same property values.
     * (The properties to compare are based on the behavior of {@link ModelMockFactory#createImage(int, int, int, int, int)}.)
     * @param original the original instance
     * @param duplicate the duplicate instance that is expected to correspond to the original
     */
    private static void assertSameProperties(Image original, Image duplicate) {
        Assert.assertEquals(duplicate.getDescription().getValue(), original.getDescription().getValue());
        Assert.assertEquals(duplicate.getFormat().getValue(), original.getFormat().getValue());
        Assert.assertEquals(duplicate.getName().getValue(), original.getName().getValue());
        Assert.assertEquals(duplicate.getSeries().getValue(), original.getSeries().getValue());
    }

    /**
     * Assert that the given instances have the same property values.
     * (The properties to compare are based on the behavior of {@link ModelMockFactory#createImage(int, int, int, int, int)}.)
     * @param original the original instance
     * @param duplicate the duplicate instance that is expected to correspond to the original
     */
    private static void assertSameProperties(Pixels original, Pixels duplicate) {
        Assert.assertEquals(duplicate.getPhysicalSizeX().getUnit(), original.getPhysicalSizeX().getUnit());
        Assert.assertEquals(duplicate.getPhysicalSizeX().getValue(), original.getPhysicalSizeX().getValue());
        Assert.assertEquals(duplicate.getPhysicalSizeY().getUnit(), original.getPhysicalSizeY().getUnit());
        Assert.assertEquals(duplicate.getPhysicalSizeY().getValue(), original.getPhysicalSizeY().getValue());
        Assert.assertEquals(duplicate.getPhysicalSizeZ().getUnit(), original.getPhysicalSizeZ().getUnit());
        Assert.assertEquals(duplicate.getPhysicalSizeZ().getValue(), original.getPhysicalSizeZ().getValue());
        Assert.assertEquals(duplicate.getSizeX().getValue(), original.getSizeX().getValue());
        Assert.assertEquals(duplicate.getSizeY().getValue(), original.getSizeY().getValue());
        Assert.assertEquals(duplicate.getSizeZ().getValue(), original.getSizeZ().getValue());
        Assert.assertEquals(duplicate.getSizeT().getValue(), original.getSizeT().getValue());
        Assert.assertEquals(duplicate.getSizeC().getValue(), original.getSizeC().getValue());
        Assert.assertEquals(duplicate.getSha1().getValue(), original.getSha1().getValue());
        Assert.assertEquals(duplicate.getDimensionOrder().getValue().getValue(),
                original.getDimensionOrder().getValue().getValue());
        Assert.assertEquals(duplicate.getPixelsType().getBitSize().getValue(), original.getPixelsType().getBitSize().getValue());
        Assert.assertEquals(duplicate.getPixelsType().getValue().getValue(), original.getPixelsType().getValue().getValue());
    }

    /**
     * Assert that the given instances have the same property values.
     * (The properties to compare are based on the behavior of {@link ModelMockFactory#createImage(int, int, int, int, int)}.)
     * @param original the original instance
     * @param duplicate the duplicate instance that is expected to correspond to the original
     */
    private static void assertSameProperties(Channel original, Channel duplicate) {
        // nothing
    }

    /**
     * Assert that the given instances have the same property values.
     * (The properties to compare are based on the behavior of {@link ModelMockFactory#createImage(int, int, int, int, int)}.)
     * @param original the original instance
     * @param duplicate the duplicate instance that is expected to correspond to the original
     */
    private static void assertSameProperties(LogicalChannel original, LogicalChannel duplicate) {
        Assert.assertEquals(duplicate.getContrastMethod().getValue().getValue(),
                original.getContrastMethod().getValue().getValue());
        Assert.assertEquals(duplicate.getEmissionWave().getUnit(), original.getEmissionWave().getUnit());
        Assert.assertEquals(duplicate.getEmissionWave().getValue(), original.getEmissionWave().getValue());
        Assert.assertEquals(duplicate.getIllumination().getValue(), original.getIllumination().getValue());
        Assert.assertEquals(duplicate.getMode().getValue().getValue(), original.getMode().getValue().getValue());
    }

    /**
     * Assert that the given instances have the same property values.
     * (The properties to compare are based on the behavior of {@link ModelMockFactory#createImage(int, int, int, int, int)}.)
     * @param original the original instance
     * @param duplicate the duplicate instance that is expected to correspond to the original
     */
    private static void assertSameProperties(PlaneInfo original, PlaneInfo duplicate) {
        Assert.assertEquals(duplicate.getTheZ().getValue(), original.getTheZ().getValue());
        Assert.assertEquals(duplicate.getTheT().getValue(), original.getTheT().getValue());
        Assert.assertEquals(duplicate.getTheC().getValue(), original.getTheC().getValue());
        Assert.assertEquals(duplicate.getDeltaT().getUnit(), original.getDeltaT().getUnit());
        Assert.assertEquals(duplicate.getDeltaT().getValue(), original.getDeltaT().getValue());
    }

    /**
     * Assert that the given instances have the same property values.
     * (The properties to compare are based on the behavior of {@link ModelMockFactory#createImage(int, int, int, int, int)}.)
     * @param original the original instance
     * @param duplicate the duplicate instance that is expected to correspond to the original
     */
    private static void assertSameProperties(StatsInfo original, StatsInfo duplicate) {
        Assert.assertEquals(duplicate.getGlobalMin().getValue(), original.getGlobalMin().getValue());
        Assert.assertEquals(duplicate.getGlobalMax().getValue(), original.getGlobalMax().getValue());
    }

    /**
     * Assert that the given instances have the same property values.
     * @param original the original instance
     * @param duplicate the duplicate instance that is expected to correspond to the original
     */
    private static void assertSameProperties(Rectangle original, Rectangle duplicate) {
        Assert.assertEquals(duplicate.getTheZ().getValue(), original.getTheZ().getValue());
        Assert.assertEquals(duplicate.getTheT().getValue(), original.getTheT().getValue());
        Assert.assertEquals(duplicate.getTheC().getValue(), original.getTheC().getValue());
        Assert.assertEquals(duplicate.getX().getValue(), original.getX().getValue());
        Assert.assertEquals(duplicate.getY().getValue(), original.getY().getValue());
        Assert.assertEquals(duplicate.getWidth().getValue(), original.getWidth().getValue());
        Assert.assertEquals(duplicate.getHeight().getValue(), original.getHeight().getValue());
    }

    /**
     * Assert that the given instances have the same property values.
     * @param original the original instance
     * @param duplicate the duplicate instance that is expected to correspond to the original
     */
    private static void assertSameProperties(Ellipse original, Ellipse duplicate) {
        Assert.assertEquals(duplicate.getTheZ().getValue(), original.getTheZ().getValue());
        Assert.assertEquals(duplicate.getTheT().getValue(), original.getTheT().getValue());
        Assert.assertEquals(duplicate.getTheC().getValue(), original.getTheC().getValue());
        Assert.assertEquals(duplicate.getX().getValue(), original.getX().getValue());
        Assert.assertEquals(duplicate.getY().getValue(), original.getY().getValue());
        Assert.assertEquals(duplicate.getRadiusX().getValue(), original.getRadiusX().getValue());
        Assert.assertEquals(duplicate.getRadiusY().getValue(), original.getRadiusY().getValue());
    }

    /**
     * Assert that the given instances have the same property values.
     * @param original the original instance
     * @param duplicate the duplicate instance that is expected to correspond to the original
     */
    private static void assertSameProperties(Line original, Line duplicate) {
        Assert.assertEquals(duplicate.getTheZ().getValue(), original.getTheZ().getValue());
        Assert.assertEquals(duplicate.getTheT().getValue(), original.getTheT().getValue());
        Assert.assertEquals(duplicate.getTheC().getValue(), original.getTheC().getValue());
        Assert.assertEquals(duplicate.getX1().getValue(), original.getX1().getValue());
        Assert.assertEquals(duplicate.getY1().getValue(), original.getY1().getValue());
        Assert.assertEquals(duplicate.getX2().getValue(), original.getX2().getValue());
        Assert.assertEquals(duplicate.getY2().getValue(), original.getY2().getValue());
    }

    /**
     * Assert that the given instances have the same property values.
     * @param original the original instance
     * @param duplicate the duplicate instance that is expected to correspond to the original
     */
    private static void assertSameProperties(Point original, Point duplicate) {
        Assert.assertEquals(duplicate.getTheZ().getValue(), original.getTheZ().getValue());
        Assert.assertEquals(duplicate.getTheT().getValue(), original.getTheT().getValue());
        Assert.assertEquals(duplicate.getTheC().getValue(), original.getTheC().getValue());
        Assert.assertEquals(duplicate.getX().getValue(), original.getX().getValue());
        Assert.assertEquals(duplicate.getY().getValue(), original.getY().getValue());
    }

    /**
     * Get the IDs of an image's annotations.
     * @param imageId the ID of an image
     * @return the IDs of the annotations on the image
     * @throws ServerError unexpected
     */
    private Set<Long> getImageAnnotations(long imageId) throws ServerError {
        final Set<Long> annotationIds = new HashSet<Long>();
        for (final List<RType> result : iQuery.projection(
                "SELECT child.id FROM ImageAnnotationLink WHERE parent.id = :id",
                new ParametersI().addId(imageId))) {
            annotationIds.add(((RLong) result.get(0)).getValue());
        }
        return annotationIds;
    }

    private String followLongAnnotations(LongAnnotation start, int depth) throws ServerError {
        if (depth < 1) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(start.getLongValue().getValue());
        for (final IObject result : iQuery.findAllByQuery(
                "SELECT l.child FROM AnnotationAnnotationLink AS l WHERE l.parent.id = :id ORDER BY l.child.longValue",
                new ParametersI().addId(start.getId().getValue()))) {
            if (result instanceof LongAnnotation) {
                sb.append(followLongAnnotations((LongAnnotation) result, depth - 1));
            }
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Create then duplicate an image and test that the properties of the instances in it and its subgraph are correctly duplicated.
     * @throws Exception unexpected
     */
    @Test
    public void testDuplicateImageProperties() throws Exception {
        newUserAndGroup("rwr---");

        /* create and save an image */

        final Image originalImage = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage(1,2,3,4,5));

        /* note the objects (and their IDs) that were thus created and saved */

        final long originalImageId = originalImage.getId().getValue();
        testImages.add(originalImageId);
        final Pixels originalPixels = originalImage.getPrimaryPixels();
        final long originalPixelsId = originalPixels.getId().getValue();
        final List<Channel> originalChannels = new ArrayList<Channel>(originalPixels.sizeOfChannels());
        final Set<Long> originalChannelIds = new HashSet<Long>();
        final List<LogicalChannel> originalLogicalChannels = new ArrayList<LogicalChannel>(originalChannels.size());
        final Set<Long> originalLogicalChannelIds = new HashSet<Long>();
        final List<StatsInfo> originalStatsInfos = new ArrayList<StatsInfo>(originalChannels.size());
        final Set<Long> originalStatsInfoIds = new HashSet<Long>();
        for (int channelIndex = 0; channelIndex < originalPixels.sizeOfChannels(); channelIndex++) {
            final Channel originalChannel = originalPixels.getChannel(channelIndex);
            originalChannels.add(originalChannel);
            originalChannelIds.add(originalChannel.getId().getValue());
            final LogicalChannel originalLogicalChannel = originalChannel.getLogicalChannel();
            originalLogicalChannels.add(originalLogicalChannel);
            originalLogicalChannelIds.add(originalLogicalChannel.getId().getValue());
            final StatsInfo originalStatsInfo = originalChannel.getStatsInfo();
            originalStatsInfos.add(originalStatsInfo);
            originalStatsInfoIds.add(originalStatsInfo.getId().getValue());
        }
        final Map<PlaneInfoKey, PlaneInfo> originalPlaneInfos = new HashMap<PlaneInfoKey, PlaneInfo>();
        final Set<Long> originalPlaneInfoIds = new HashSet<Long>();
        for (final PlaneInfo originalPlaneInfo : originalPixels.copyPlaneInfo()) {
            originalPlaneInfos.put(new PlaneInfoKey(originalPlaneInfo), originalPlaneInfo);
            originalPlaneInfoIds.add(originalPlaneInfo.getId().getValue());
        }

        /* duplicate the image */

        final Duplicate dup = Requests.duplicate().target("Image").id(originalImageId).build();
        final DuplicateResponse response = (DuplicateResponse) doChange(dup);

        /* find out which objects the duplication claims to have created */

        Assert.assertEquals(response.duplicates.size(), 6);
        final Set<Long> remainingImageIds = new HashSet<Long>(response.duplicates.get("ome.model.core.Image"));
        final Set<Long> remainingPixelsIds = new HashSet<Long>(response.duplicates.get("ome.model.core.Pixels"));
        final Set<Long> remainingChannelIds = new HashSet<Long>(response.duplicates.get("ome.model.core.Channel"));
        final Set<Long> remainingLogicalChannelIds = new HashSet<Long>(response.duplicates.get("ome.model.core.LogicalChannel"));
        final Set<Long> remainingStatsInfoIds = new HashSet<Long>(response.duplicates.get("ome.model.stats.StatsInfo"));
        final Set<Long> remainingPlaneInfoIds = new HashSet<Long>(response.duplicates.get("ome.model.core.PlaneInfo"));

        /* check that the new image's subgraph is a set of new IDs that match the response and whose properties are as expected */

        final Long duplicateImageId = remainingImageIds.iterator().next();
        testImages.add(duplicateImageId);
        final Parameters parameters = new ParametersI().addId(duplicateImageId);
        final Image duplicateImage = (Image) iQuery.findByQuery(
                "SELECT i FROM Image i " +
                "JOIN FETCH i.pixels AS p " +
                "JOIN FETCH i.format " +
                "JOIN FETCH p.dimensionOrder " +
                "JOIN FETCH p.pixelsType " +
                "JOIN FETCH p.channels AS c " +
                "JOIN FETCH p.planeInfo AS pi " +
                "JOIN FETCH c.logicalChannel AS lc " +
                "JOIN FETCH lc.contrastMethod " +
                "JOIN FETCH lc.illumination " +
                "JOIN FETCH lc.mode " +
                "JOIN FETCH c.statsInfo " +
                "WHERE i.id = :id", parameters);
        Assert.assertNotEquals(originalImageId, duplicateImageId);
        Assert.assertTrue(remainingImageIds.remove(duplicateImageId));
        assertSameProperties(originalImage, duplicateImage);

        final Pixels duplicatePixels = duplicateImage.getPrimaryPixels();
        final long duplicatePixelsId = duplicatePixels.getId().getValue();
        Assert.assertNotEquals(originalPixelsId, duplicatePixelsId);
        Assert.assertTrue(remainingPixelsIds.remove(duplicatePixelsId));
        assertSameProperties(originalPixels, duplicatePixels);

        for (int channelIndex = 0; channelIndex < duplicatePixels.sizeOfChannels(); channelIndex++) {
            final Channel duplicateChannel = duplicatePixels.getChannel(channelIndex);
            final long duplicateChannelId = duplicateChannel.getId().getValue();
            Assert.assertFalse(originalChannelIds.contains(duplicateChannelId));
            Assert.assertTrue(remainingChannelIds.remove(duplicateChannelId));
            assertSameProperties(originalChannels.get(channelIndex), duplicateChannel);

            final LogicalChannel duplicateLogicalChannel = duplicateChannel.getLogicalChannel();
            final long duplicateLogicalChannelId = duplicateLogicalChannel.getId().getValue();
            Assert.assertFalse(originalLogicalChannelIds.contains(duplicateLogicalChannelId));
            Assert.assertTrue(remainingLogicalChannelIds.remove(duplicateLogicalChannelId));
            assertSameProperties(originalLogicalChannels.get(channelIndex), duplicateLogicalChannel);

            final StatsInfo duplicateStatsInfo = duplicateChannel.getStatsInfo();
            final long duplicateStatsInfoId = duplicateStatsInfo.getId().getValue();
            Assert.assertFalse(originalStatsInfoIds.contains(duplicateStatsInfoId));
            Assert.assertTrue(remainingStatsInfoIds.remove(duplicateStatsInfoId));
            assertSameProperties(originalStatsInfos.get(channelIndex), duplicateStatsInfo);
        }
        for (final PlaneInfo duplicatePlaneInfo : duplicatePixels.copyPlaneInfo()) {
            final long duplicatePlaneInfoId = duplicatePlaneInfo.getId().getValue();
            Assert.assertFalse(originalPlaneInfoIds.contains(duplicatePlaneInfoId));
            Assert.assertTrue(remainingPlaneInfoIds.remove(duplicatePlaneInfoId));
            assertSameProperties(originalPlaneInfos.get(new PlaneInfoKey(duplicatePlaneInfo)), duplicatePlaneInfo);
        }

        /* check that the response reported no IDs for instances whose properties we did not check */

        Assert.assertTrue(remainingImageIds.isEmpty());
        Assert.assertTrue(remainingPixelsIds.isEmpty());
        Assert.assertTrue(remainingChannelIds.isEmpty());
        Assert.assertTrue(remainingLogicalChannelIds.isEmpty());
        Assert.assertTrue(remainingStatsInfoIds.isEmpty());
        Assert.assertTrue(remainingPlaneInfoIds.isEmpty());
    }

    /**
     * Create then duplicate an image in dry-run mode and test that the image's subgraph is returned in the response.
     * @throws Exception unexpected
     */
    @Test
    public void testDuplicateImageDryRun() throws Exception {
        newUserAndGroup("rwr---");

        /* create and save an image */

        final Image originalImage = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage(1,2,3,4,5));

        /* note the objects (and their IDs) that were thus created and saved */

        final long originalImageId = originalImage.getId().getValue();
        testImages.add(originalImageId);
        final Pixels originalPixels = originalImage.getPrimaryPixels();
        final long originalPixelsId = originalPixels.getId().getValue();
        final Set<Long> originalChannelIds = new HashSet<Long>();
        final Set<Long> originalLogicalChannelIds = new HashSet<Long>();
        final Set<Long> originalStatsInfoIds = new HashSet<Long>();
        for (int channelIndex = 0; channelIndex < originalPixels.sizeOfChannels(); channelIndex++) {
            final Channel originalChannel = originalPixels.getChannel(channelIndex);
            originalChannelIds.add(originalChannel.getId().getValue());
            final LogicalChannel originalLogicalChannel = originalChannel.getLogicalChannel();
            originalLogicalChannelIds.add(originalLogicalChannel.getId().getValue());
            final StatsInfo originalStatsInfo = originalChannel.getStatsInfo();
            originalStatsInfoIds.add(originalStatsInfo.getId().getValue());
        }
        final Set<Long> originalPlaneInfoIds = new HashSet<Long>();
        for (final PlaneInfo originalPlaneInfo : originalPixels.copyPlaneInfo()) {
            originalPlaneInfoIds.add(originalPlaneInfo.getId().getValue());
        }

        /* duplicate the image in dry-run mode */

        final Duplicate dup = Requests.duplicate().target("Image").id(originalImageId).dryRun().build();
        final DuplicateResponse response = (DuplicateResponse) doChange(dup);

        /* find out which objects the duplication reports as being targets for processing */

        Assert.assertEquals(response.duplicates.size(), 6);
        final Set<Long> reportedImageIds = new HashSet<Long>(response.duplicates.get("ome.model.core.Image"));
        final Set<Long> reportedPixelsIds = new HashSet<Long>(response.duplicates.get("ome.model.core.Pixels"));
        final Set<Long> reportedChannelIds = new HashSet<Long>(response.duplicates.get("ome.model.core.Channel"));
        final Set<Long> reportedLogicalChannelIds = new HashSet<Long>(response.duplicates.get("ome.model.core.LogicalChannel"));
        final Set<Long> reportedStatsInfoIds = new HashSet<Long>(response.duplicates.get("ome.model.stats.StatsInfo"));
        final Set<Long> reportedPlaneInfoIds = new HashSet<Long>(response.duplicates.get("ome.model.core.PlaneInfo"));

        /* check that the duplication reported exactly the expected targets */

        Assert.assertEquals(reportedImageIds, Collections.singleton(originalImageId));
        Assert.assertEquals(reportedPixelsIds, Collections.singleton(originalPixelsId));
        Assert.assertEquals(reportedChannelIds, originalChannelIds);
        Assert.assertEquals(reportedLogicalChannelIds, originalLogicalChannelIds);
        Assert.assertEquals(reportedStatsInfoIds, originalStatsInfoIds);
        Assert.assertEquals(reportedPlaneInfoIds, originalPlaneInfoIds);
    }

    /**
     * Test duplication of an annotated image with duplication of its annotation.
     * @throws Exception unexpected
     */
    @Test
    public void testDuplicateImageWithAnnotation() throws Exception {
        newUserAndGroup("rwra--");

        /* create an annotated image */

        final String annotationText = getClass().getSimpleName() + ':' + System.nanoTime();

        final Image originalImage = mmFactory.simpleImage();
        final TextAnnotation originalAnnotation = new XmlAnnotationI();
        originalAnnotation.setTextValue(omero.rtypes.rstring(annotationText));
        ImageAnnotationLink originalLink = new ImageAnnotationLinkI();
        originalLink.setParent(originalImage);
        originalLink.setChild(originalAnnotation);
        originalLink = (ImageAnnotationLink) iUpdate.saveAndReturnObject(originalLink);

        /* note the objects (and their IDs) that were thus created and saved */

        final long originalImageId = originalLink.getParent().getId().getValue();
        final long originalAnnotationId = originalLink.getChild().getId().getValue();
        final long originalLinkId = originalLink.getId().getValue();
        testImages.add(originalImageId);

        /* duplicate the image */

        final Duplicate dup = Requests.duplicate().target("Image").id(originalImageId).build();
        final DuplicateResponse response = (DuplicateResponse) doChange(dup);

        /* check that the response includes duplication of an image, link, and annotation */

        final Set<Long> reportedImageIds = new HashSet<Long>(response.duplicates.get("ome.model.core.Image"));
        final Set<Long> reportedAnnotationIds = new HashSet<Long>(response.duplicates.get("ome.model.annotations.XmlAnnotation"));
        final Set<Long> reportedLinkIds = new HashSet<Long>(response.duplicates.get("ome.model.annotations.ImageAnnotationLink"));

        Assert.assertEquals(reportedImageIds.size(), 1);
        Assert.assertEquals(reportedAnnotationIds.size(), 1);
        Assert.assertEquals(reportedLinkIds.size(), 1);

        /* check that the reported image, link, and annotation each have a new ID */

        final long reportedImageId = reportedImageIds.iterator().next();
        final long reportedAnnotationId = reportedAnnotationIds.iterator().next();
        final long reportedLinkId = reportedLinkIds.iterator().next();
        testImages.add(reportedImageId);

        Assert.assertNotEquals(originalImageId, reportedImageId);
        Assert.assertNotEquals(originalAnnotationId, reportedAnnotationId);
        Assert.assertNotEquals(originalLinkId, reportedLinkId);

        /* check that the annotations on the images are exactly as expected */

        Assert.assertEquals(getImageAnnotations(originalImageId), Collections.singleton(originalAnnotationId));
        Assert.assertEquals(getImageAnnotations(reportedImageId), Collections.singleton(reportedAnnotationId));

        /* check that the annotation is indeed a duplicate of the original */

        final TextAnnotation duplicatedAnnotation = (TextAnnotation) iQuery.findByQuery(
                "FROM Annotation WHERE id = :id",
                new ParametersI().addId(reportedAnnotationId));

        Assert.assertEquals(duplicatedAnnotation.getTextValue().getValue(), annotationText);
    }

    /**
     * Test duplication of an annotated image such that it links to the original's annotation.
     * @throws Exception unexpected
     */
    @Test
    public void testDuplicateImageWithPreviousAnnotation() throws Exception {
        newUserAndGroup("rwra--");

        /* create an annotated image */

        final String annotationText = getClass().getSimpleName() + ':' + System.nanoTime();

        final Image originalImage = mmFactory.simpleImage();
        final TextAnnotation originalAnnotation = new XmlAnnotationI();
        originalAnnotation.setTextValue(omero.rtypes.rstring(annotationText));
        ImageAnnotationLink originalLink = new ImageAnnotationLinkI();
        originalLink.setParent(originalImage);
        originalLink.setChild(originalAnnotation);
        originalLink = (ImageAnnotationLink) iUpdate.saveAndReturnObject(originalLink);

        /* note the objects (and their IDs) that were thus created and saved */

        final long originalImageId = originalLink.getParent().getId().getValue();
        final long originalAnnotationId = originalLink.getChild().getId().getValue();
        final long originalLinkId = originalLink.getId().getValue();
        testImages.add(originalImageId);

        /* duplicate the image */

        final Duplicate dup = Requests.duplicate().target("Image").id(originalImageId).referenceType("TextAnnotation").build();
        final DuplicateResponse response = (DuplicateResponse) doChange(dup);

        /* check that the response includes duplication of an image and link, but not an annotation */

        final Set<Long> reportedImageIds = new HashSet<Long>(response.duplicates.get("ome.model.core.Image"));
        Assert.assertFalse(response.duplicates.containsKey("ome.model.annotations.XmlAnnotation"));
        final Set<Long> reportedLinkIds = new HashSet<Long>(response.duplicates.get("ome.model.annotations.ImageAnnotationLink"));

        Assert.assertEquals(reportedImageIds.size(), 1);
        Assert.assertEquals(reportedLinkIds.size(), 1);

        /* check that the reported image and annotation each have a new ID */

        final long reportedImageId = reportedImageIds.iterator().next();
        final long reportedLinkId = reportedLinkIds.iterator().next();
        testImages.add(reportedImageId);

        Assert.assertNotEquals(originalImageId, reportedImageId);
        Assert.assertNotEquals(originalLinkId, reportedLinkId);

        /* check that the annotations on the images are exactly as expected */

        Assert.assertEquals(getImageAnnotations(originalImageId), Collections.singleton(originalAnnotationId));
        Assert.assertEquals(getImageAnnotations(reportedImageId), Collections.singleton(originalAnnotationId));
    }

    /**
     * Test duplication of an annotated image such that it does not link to an annotation.
     * @throws Exception unexpected
     */
    @Test
    public void testDuplicateImageWithNoAnnotation() throws Exception {
        newUserAndGroup("rwra--");

        /* create an annotated image */

        final String annotationText = getClass().getSimpleName() + ':' + System.nanoTime();

        final Image originalImage = mmFactory.simpleImage();
        final TextAnnotation originalAnnotation = new XmlAnnotationI();
        originalAnnotation.setTextValue(omero.rtypes.rstring(annotationText));
        ImageAnnotationLink originalLink = new ImageAnnotationLinkI();
        originalLink.setParent(originalImage);
        originalLink.setChild(originalAnnotation);
        originalLink = (ImageAnnotationLink) iUpdate.saveAndReturnObject(originalLink);

        /* note the objects (and their IDs) that were thus created and saved */

        final long originalImageId = originalLink.getParent().getId().getValue();
        final long originalAnnotationId = originalLink.getChild().getId().getValue();
        final long originalLinkId = originalLink.getId().getValue();
        testImages.add(originalImageId);

        /* duplicate the image */

        final Duplicate dup = Requests.duplicate().target("Image").id(originalImageId).ignoreType("IAnnotationLink").build();
        final DuplicateResponse response = (DuplicateResponse) doChange(dup);

        /* check that the response includes duplication of an image and link, but not an annotation */

        final Set<Long> reportedImageIds = new HashSet<Long>(response.duplicates.get("ome.model.core.Image"));
        Assert.assertFalse(response.duplicates.containsKey("ome.model.annotations.XmlAnnotation"));
        Assert.assertFalse(response.duplicates.containsKey("ome.model.annotations.ImageAnnotationLink"));

        Assert.assertEquals(reportedImageIds.size(), 1);

        /* check that the reported image and annotation each have a new ID */

        final long reportedImageId = reportedImageIds.iterator().next();
        testImages.add(reportedImageId);

        Assert.assertNotEquals(originalImageId, reportedImageId);

        /* check that the annotations on the images are exactly as expected */

        Assert.assertEquals(getImageAnnotations(originalImageId), Collections.singleton(originalAnnotationId));
        Assert.assertEquals(getImageAnnotations(reportedImageId), Collections.emptySet());
    }


    /**
     * Test duplication of an annotated image such that it does not duplicate an attachment.
     * @throws Exception unexpected
     */
    @Test
    public void testDuplicateImageWithFileAnnotation() throws Exception {
        newUserAndGroup("rwra--");

        /* create an image with an attachment */

        final Image originalImage = mmFactory.simpleImage();
        final FileAnnotation originalAttachment = new FileAnnotationI();
        ImageAnnotationLink originalLink = new ImageAnnotationLinkI();
        originalLink.setParent(originalImage);
        originalLink.setChild(originalAttachment);
        originalLink = (ImageAnnotationLink) iUpdate.saveAndReturnObject(originalLink);

        /* note the objects (and their IDs) that were thus created and saved */

        final long originalImageId = originalLink.getParent().getId().getValue();
        final long originalAttachmentId = originalLink.getChild().getId().getValue();
        final long originalLinkId = originalLink.getId().getValue();
        testImages.add(originalImageId);

        /* duplicate the image */

        final Duplicate dup = Requests.duplicate().target("Image").id(originalImageId).build();
        final DuplicateResponse response = (DuplicateResponse) doChange(dup);

        /* check that the response includes duplication of an image and link, but not the attachment */

        final Set<Long> reportedImageIds = new HashSet<Long>(response.duplicates.get("ome.model.core.Image"));
        Assert.assertFalse(response.duplicates.containsKey("ome.model.annotations.FileAnnotation"));
        Assert.assertFalse(response.duplicates.containsKey("ome.model.annotations.ImageAnnotationLink"));

        Assert.assertEquals(reportedImageIds.size(), 1);

        /* check that the reported image and annotation each have a new ID */

        final long reportedImageId = reportedImageIds.iterator().next();
        testImages.add(reportedImageId);

        Assert.assertNotEquals(originalImageId, reportedImageId);

        /* check that the annotations on the images are exactly as expected */

        Assert.assertEquals(getImageAnnotations(originalImageId), Collections.singleton(originalAttachmentId));
        Assert.assertEquals(getImageAnnotations(reportedImageId), Collections.emptySet());
    }

    /**
     * Tests duplication of a cyclic subgraph.
     * @throws Exception unexpected
     */
    @Test
    public void testDuplicateImageWithCyclicAnnotation() throws Exception {
        newUserAndGroup("rwra--");

        /* create a graph of annotations */

        final Image originalImage = mmFactory.simpleImage();
        final List<LongAnnotation> originalAnnotations = new ArrayList<LongAnnotation>();
        List<IObject> originalLinks = new ArrayList<IObject>();
        /* create a string of annotations */
        for (int index = 0; index < 5; index++) {
            originalAnnotations.add(new LongAnnotationI());
            originalAnnotations.get(index).setLongValue(omero.rtypes.rlong(index));
            if (index > 0) {
                final AnnotationAnnotationLink annotationLink = new AnnotationAnnotationLinkI();
                annotationLink.setParent((Annotation) originalAnnotations.get(index - 1));
                annotationLink.setChild(originalAnnotations.get(index));
                originalLinks.add(annotationLink);
            }
        }
        /* add a back-link within the string of annotations */
        final AnnotationAnnotationLink annotationLink = new AnnotationAnnotationLinkI();
        annotationLink.setParent((Annotation) originalAnnotations.get(3));
        annotationLink.setChild(originalAnnotations.get(1));
        originalLinks.add(annotationLink);

        /* annotate an image with the graph */

        ImageAnnotationLink originalImageLink = new ImageAnnotationLinkI();
        originalImageLink.setParent(originalImage);
        originalImageLink.setChild(originalAnnotations.get(0));
        originalLinks.add(0, originalImageLink);
        originalLinks = iUpdate.saveAndReturnArray(originalLinks);
        originalImageLink = (ImageAnnotationLink) originalLinks.get(0);

        /* note the objects (and their IDs) that were thus created and saved */

        final long originalImageId = originalImageLink.getParent().getId().getValue();
        final long originalImageLinkId = ((ImageAnnotationLink) originalLinks.get(0)).getId().getValue();
        final Set<Long> originalAnnotationIds = new HashSet<Long>();
        final Set<Long> originalAnnotationLinkIds = new HashSet<Long>();
        for (final IObject object : originalLinks.subList(1, originalLinks.size())) {
            final AnnotationAnnotationLink link = (AnnotationAnnotationLink) object;
            originalAnnotationIds.add(link.getParent().getId().getValue());
            originalAnnotationIds.add(link.getChild().getId().getValue());
            originalAnnotationLinkIds.add(link.getId().getValue());
        }
        testImages.add(originalImageId);

        /* duplicate the image */

        final Duplicate dup = Requests.duplicate().target("Image").id(originalImageId).build();
        final DuplicateResponse response = (DuplicateResponse) doChange(dup);

        /* check that the response includes duplication of an image and annotations and the links among them */

        final Set<Long> reportedImageIds = new HashSet<Long>(
                response.duplicates.get("ome.model.core.Image"));
        final Set<Long> reportedAnnotationIds = new HashSet<Long>(
                response.duplicates.get("ome.model.annotations.LongAnnotation"));
        final Set<Long> reportedImageLinkIds = new HashSet<Long>(
                response.duplicates.get("ome.model.annotations.ImageAnnotationLink"));
        final Set<Long> reportedAnnotationLinkIds = new HashSet<Long>(
                response.duplicates.get("ome.model.annotations.AnnotationAnnotationLink"));

        Assert.assertEquals(reportedImageIds.size(), 1);
        Assert.assertEquals(reportedAnnotationIds.size(), originalAnnotations.size());
        Assert.assertEquals(reportedImageLinkIds.size() + reportedAnnotationLinkIds.size(), originalLinks.size());

        /* check that the reported image and annotation each have a new ID */

        final long reportedImageId = reportedImageIds.iterator().next();
        final long reportedImageLinkId = reportedImageLinkIds.iterator().next();
        testImages.add(reportedImageId);

        Assert.assertNotEquals(originalImageId, reportedImageId);
        Assert.assertNotEquals(originalImageLinkId, reportedImageLinkId);
        Assert.assertTrue(Sets.intersection(originalAnnotationIds, reportedAnnotationIds).isEmpty());
        Assert.assertTrue(Sets.intersection(originalAnnotationLinkIds, reportedAnnotationLinkIds).isEmpty());

        /* check that the duplicate image has a different annotation from the original image */

        final Set<Long> originalImageAnnotationIds = getImageAnnotations(originalImageId);
        final Set<Long> duplicateImageAnnotationIds = getImageAnnotations(reportedImageId);
        Assert.assertEquals(originalImageAnnotationIds.size(), 1);
        Assert.assertEquals(duplicateImageAnnotationIds.size(), 1);
        final long originalImageAnnotationId = originalImageAnnotationIds.iterator().next();
        final long duplicateImageAnnotationId = duplicateImageAnnotationIds.iterator().next();
        Assert.assertNotEquals(originalImageAnnotationId, duplicateImageAnnotationId);

        /* check that the structure of the cyclic graph of annotations has been correctly duplicated */

        final LongAnnotation originalImageAnnotation  = (LongAnnotation) iQuery.get("LongAnnotation", originalImageAnnotationId);
        final LongAnnotation duplicateImageAnnotation = (LongAnnotation) iQuery.get("LongAnnotation", duplicateImageAnnotationId);

        Assert.assertEquals(followLongAnnotations(originalImageAnnotation,  6), "[0[1[2[3[1[2]][4]]]]]");
        Assert.assertEquals(followLongAnnotations(duplicateImageAnnotation, 6), "[0[1[2[3[1[2]][4]]]]]");
    }

    /**
     * Test duplication of annotated images that have separate and shared annotations.
     * @throws Exception unexpected
     */
    @Test
    public void testDuplicateImagesWithSeparateAndSharedAnnotations() throws Exception {
        newUserAndGroup("rwra--");

        /* create two annotated images with three annotations, the last annotation shared among both images */

        final List<Long> originalImageIds = new ArrayList<Long>();

        for (int ii = 0; ii < 2; ii++) {
            originalImageIds.add(iUpdate.saveAndReturnObject(mmFactory.simpleImage()).getId().getValue());
        }
        testImages.addAll(originalImageIds);

        final List<Long> originalAnnotationIds = new ArrayList<Long>();
        final List<String> originalAnnotationText = new ArrayList<String>();

        for (int ai = 0; ai < 3; ai++) {
            final String textValue = getClass().getSimpleName() + " annotation #" + ai;
            final TextAnnotation originalAnnotation = new XmlAnnotationI();
            originalAnnotation.setTextValue(omero.rtypes.rstring(textValue));
            originalAnnotationIds.add(iUpdate.saveAndReturnObject(originalAnnotation).getId().getValue());
            originalAnnotationText.add(textValue);
        }

        final SetMultimap<Integer, Integer> imageAnnotationLinks = ImmutableSetMultimap.of(0, 0, 1, 1, 0, 2, 1, 2);
        final List<Long> originalLinkIds = new ArrayList<Long>();

        for (Map.Entry<Integer, Integer> toLink : imageAnnotationLinks.entries()) {
            final Image image = new ImageI(originalImageIds.get(toLink.getKey()), false);
            final TextAnnotation annotation = new XmlAnnotationI(originalAnnotationIds.get(toLink.getValue()), false);
            final ImageAnnotationLink link = new ImageAnnotationLinkI();
            link.setParent(image);
            link.setChild(annotation);
            originalLinkIds.add(iUpdate.saveAndReturnObject(link).getId().getValue());
        }

        /* duplicate the images */

        final Duplicate dup = Requests.duplicate().target("Image").id(originalImageIds).build();
        final DuplicateResponse response = (DuplicateResponse) doChange(dup);

        /* check that the response includes duplication of the images, links, and annotations */

        final Set<Long> reportedImageIds = new HashSet<Long>(response.duplicates.get("ome.model.core.Image"));
        final Set<Long> reportedAnnotationIds = new HashSet<Long>(response.duplicates.get("ome.model.annotations.XmlAnnotation"));
        final Set<Long> reportedLinkIds = new HashSet<Long>(response.duplicates.get("ome.model.annotations.ImageAnnotationLink"));
        testImages.addAll(reportedImageIds);

        Assert.assertEquals(reportedImageIds.size(), originalImageIds.size());
        Assert.assertEquals(reportedAnnotationIds.size(), originalAnnotationIds.size());
        Assert.assertEquals(reportedLinkIds.size(), originalLinkIds.size());

        /* check that the reported images, links, and annotations all have a new ID */

        Assert.assertTrue(Sets.intersection(ImmutableSet.of(originalImageIds), reportedImageIds).isEmpty());
        Assert.assertTrue(Sets.intersection(ImmutableSet.of(originalAnnotationIds), reportedAnnotationIds).isEmpty());
        Assert.assertTrue(Sets.intersection(ImmutableSet.of(originalLinkIds), reportedLinkIds).isEmpty());

        /* check that the annotations on the images are exactly as expected */

        for (final Map.Entry<Integer, Collection<Integer>> linksForOneImage : imageAnnotationLinks.asMap().entrySet()) {
            final int ii = linksForOneImage.getKey();
            final Set<Long> expectedAnnotationIds = new HashSet<Long>();
            for (final int ai : linksForOneImage.getValue()) {
                expectedAnnotationIds.add(originalAnnotationIds.get(ai));
            }
            Assert.assertEquals(getImageAnnotations(originalImageIds.get(ii)), expectedAnnotationIds);
        }

        final Iterator<Long> reportedImageIdsIterator = reportedImageIds.iterator();
        final Set<Long> reportedImage1Annotations = getImageAnnotations(reportedImageIdsIterator.next());
        final Set<Long> reportedImage2Annotations = getImageAnnotations(reportedImageIdsIterator.next());

        Assert.assertEquals(reportedImage1Annotations.size(), 2);
        Assert.assertEquals(reportedImage2Annotations.size(), 2);
        Assert.assertEquals(Sets.union(reportedImage1Annotations, reportedImage2Annotations), reportedAnnotationIds);

        /* check that the annotations are indeed duplicates of the original */

        final Set<Long> sharedAnnotationIds = Sets.intersection(reportedImage1Annotations, reportedImage2Annotations);
        final Set<Long> separateAnnotationIds = Sets.symmetricDifference(reportedImage1Annotations, reportedImage2Annotations);

        final String hql = "SELECT textValue FROM TextAnnotation WHERE id IN (:ids)";
        final Set<String> reportedSharedAnnotationText = new HashSet<String>();
        final Set<String> reportedSeparateAnnotationText = new HashSet<String>();
        for (final List<RType> result : iQuery.projection(hql, new ParametersI().addIds(sharedAnnotationIds))) {
            reportedSharedAnnotationText.add(((RString) result.get(0)).getValue());
        }
        for (final List<RType> result : iQuery.projection(hql, new ParametersI().addIds(separateAnnotationIds))) {
            reportedSeparateAnnotationText.add(((RString) result.get(0)).getValue());
        }

        Assert.assertEquals(reportedSharedAnnotationText.size(), 1);
        Assert.assertEquals(reportedSeparateAnnotationText.size(), 2);

        Assert.assertEquals(reportedSharedAnnotationText, ImmutableSet.of(originalAnnotationText.get(2)));
        Assert.assertEquals(reportedSeparateAnnotationText, ImmutableSet.copyOf(originalAnnotationText.subList(0, 2)));
    }

    /**
     * Test preservation of shapes and their properties when duplicating an image with an ROI.
     * @throws Exception unexpected
     */
    @Test
    public void testDuplicateShapeProperties() throws Exception {
        newUserAndGroup("rwra--");

        /* create an image with ROIs */

        Image originalImage = mmFactory.simpleImage();
        Roi originalRoi = new RoiI();
        Rectangle originalRectangle = new RectangleI();
        originalRoi.addShape(originalRectangle);
        Ellipse originalEllipse = new EllipseI();
        originalRoi.addShape(originalEllipse);
        Line originalLine = new LineI();
        originalRoi.addShape(originalLine);
        Point originalPoint = new PointI();
        originalRoi.addShape(originalPoint);

        int propertyValue = 1;
        originalRectangle.setTheZ(omero.rtypes.rint(propertyValue++));
        originalRectangle.setTheT(omero.rtypes.rint(propertyValue++));
        originalRectangle.setTheC(omero.rtypes.rint(propertyValue++));
        originalRectangle.setX(omero.rtypes.rdouble(propertyValue++));
        originalRectangle.setY(omero.rtypes.rdouble(propertyValue++));
        originalRectangle.setWidth(omero.rtypes.rdouble(propertyValue++));
        originalRectangle.setHeight(omero.rtypes.rdouble(propertyValue++));
        originalEllipse.setTheZ(omero.rtypes.rint(propertyValue++));
        originalEllipse.setTheT(omero.rtypes.rint(propertyValue++));
        originalEllipse.setTheC(omero.rtypes.rint(propertyValue++));
        originalEllipse.setX(omero.rtypes.rdouble(propertyValue++));
        originalEllipse.setY(omero.rtypes.rdouble(propertyValue++));
        originalEllipse.setRadiusX(omero.rtypes.rdouble(propertyValue++));
        originalEllipse.setRadiusY(omero.rtypes.rdouble(propertyValue++));
        originalLine.setTheZ(omero.rtypes.rint(propertyValue++));
        originalLine.setTheT(omero.rtypes.rint(propertyValue++));
        originalLine.setTheC(omero.rtypes.rint(propertyValue++));
        originalLine.setX1(omero.rtypes.rdouble(propertyValue++));
        originalLine.setY1(omero.rtypes.rdouble(propertyValue++));
        originalLine.setX2(omero.rtypes.rdouble(propertyValue++));
        originalLine.setY2(omero.rtypes.rdouble(propertyValue++));
        originalPoint.setTheZ(omero.rtypes.rint(propertyValue++));
        originalPoint.setTheT(omero.rtypes.rint(propertyValue++));
        originalPoint.setTheC(omero.rtypes.rint(propertyValue++));
        originalPoint.setX(omero.rtypes.rdouble(propertyValue++));
        originalPoint.setY(omero.rtypes.rdouble(propertyValue++));

        originalRoi = (Roi) iUpdate.saveAndReturnObject(originalRoi);
        final Iterator<Shape> originalShapes = originalRoi.copyShapes().iterator();
        originalRectangle = (Rectangle) originalShapes.next();
        originalEllipse = (Ellipse) originalShapes.next();
        originalLine = (Line) originalShapes.next();
        originalPoint = (Point) originalShapes.next();
        Assert.assertFalse(originalShapes.hasNext());
        originalImage.addRoi(originalRoi);
        originalImage = (Image) iUpdate.saveAndReturnObject(originalImage);

        /* note the objects (and their IDs) that were thus created and saved */

        final long originalImageId = originalImage.getId().getValue();
        final long originalRoiId = originalRoi.getId().getValue();
        final long originalRectangleId = originalRectangle.getId().getValue();
        final long originalEllipseId = originalEllipse.getId().getValue();
        final long originalLineId = originalLine.getId().getValue();
        final long originalPointId = originalPoint.getId().getValue();
        testImages.add(originalImageId);

        /* duplicate the image */

        final Duplicate dup = Requests.duplicate().target("Image").id(originalImageId).build();
        final DuplicateResponse response = (DuplicateResponse) doChange(dup);

        /* check that the response includes duplication of an image, link, and annotation */

        final Set<Long> reportedImageIds = new HashSet<Long>(response.duplicates.get("ome.model.core.Image"));
        final Set<Long> reportedRoiIds = new HashSet<Long>(response.duplicates.get("ome.model.roi.Roi"));
        final Set<Long> reportedRectangleIds = new HashSet<Long>(response.duplicates.get("ome.model.roi.Rectangle"));
        final Set<Long> reportedEllipseIds = new HashSet<Long>(response.duplicates.get("ome.model.roi.Ellipse"));
        final Set<Long> reportedLineIds = new HashSet<Long>(response.duplicates.get("ome.model.roi.Line"));
        final Set<Long> reportedPointIds = new HashSet<Long>(response.duplicates.get("ome.model.roi.Point"));

        Assert.assertEquals(reportedImageIds.size(), 1);
        Assert.assertEquals(reportedRoiIds.size(), 1);
        Assert.assertEquals(reportedRectangleIds.size(), 1);
        Assert.assertEquals(reportedEllipseIds.size(), 1);
        Assert.assertEquals(reportedLineIds.size(), 1);
        Assert.assertEquals(reportedPointIds.size(), 1);

        /* check that the reported image, ROI and shapes each have a new ID */

        final long reportedImageId = reportedImageIds.iterator().next();
        final long reportedRoiId = reportedRoiIds.iterator().next();
        final long reportedRectangleId = reportedRectangleIds.iterator().next();
        final long reportedEllipseId = reportedEllipseIds.iterator().next();
        final long reportedLineId = reportedLineIds.iterator().next();
        final long reportedPointId = reportedPointIds.iterator().next();
        testImages.add(reportedImageId);

        Assert.assertNotEquals(originalImageId, reportedImageId);
        Assert.assertNotEquals(originalRoiId, reportedRoiId);
        Assert.assertNotEquals(originalRectangleId, reportedRectangleId);
        Assert.assertNotEquals(originalEllipseId, reportedEllipseId);
        Assert.assertNotEquals(originalLineId, reportedLineId);
        Assert.assertNotEquals(originalPointId, reportedPointId);

        /* check that the ROI on the images is exactly as expected */

        final Parameters parameters = new ParametersI().addId(reportedImageId);
        final Image duplicateImage = (Image) iQuery.findByQuery(
                "SELECT i FROM Image i " +
                "JOIN FETCH i.rois AS r " +
                "JOIN FETCH r.shapes " +
                "WHERE i.id = :id", parameters);
        final Iterator<Shape> duplicateShapes = duplicateImage.copyRois().get(0).copyShapes().iterator();
        final Rectangle duplicateRectangle = (Rectangle) duplicateShapes.next();
        final Ellipse duplicateEllipse = (Ellipse) duplicateShapes.next();
        final Line duplicateLine = (Line) duplicateShapes.next();
        final Point duplicatePoint = (Point) duplicateShapes.next();
        Assert.assertFalse(duplicateShapes.hasNext());

        Assert.assertEquals(duplicateRectangle.getId().getValue(), reportedRectangleId);
        Assert.assertEquals(duplicateEllipse.getId().getValue(), reportedEllipseId);
        Assert.assertEquals(duplicateLine.getId().getValue(), reportedLineId);
        Assert.assertEquals(duplicatePoint.getId().getValue(), reportedPointId);

        assertSameProperties(originalRectangle, duplicateRectangle);
        assertSameProperties(originalEllipse, duplicateEllipse);
        assertSameProperties(originalLine, duplicateLine);
        assertSameProperties(originalPoint, duplicatePoint);
}

    /**
     * Test duplication of an annotated image with various annotation types treated differently.
     * @throws Exception unexpected
     */
    @Test
    public void testDuplicateImageWithTypeOptions() throws Exception {
        newUserAndGroup("rwra--");

        /* create an annotated image */

        Image originalImage = mmFactory.simpleImage();
        originalImage = (Image) iUpdate.saveAndReturnObject(originalImage).proxy();

        List<IObject> originalLinks = new ArrayList<IObject>();
        for (final Class<? extends Annotation> annotationClass : new Class[]{
                DoubleAnnotationI.class, LongAnnotationI.class, MapAnnotationI.class, TagAnnotationI.class, XmlAnnotationI.class}) {
            final ImageAnnotationLink originalLink = new ImageAnnotationLinkI();
            originalLink.setParent(originalImage);
            originalLink.setChild(annotationClass.newInstance());
            originalLinks.add(originalLink);
        }
        originalLinks = iUpdate.saveAndReturnArray(originalLinks);

        /* note the objects (and their IDs) that were thus created and saved */

        final long originalImageId = originalImage.getId().getValue();
        final Map<String, Long> originalAnnotationIds = new HashMap<String, Long>();
        final Set<Long> originalLinkIds = new HashSet<Long>();
        for (final IObject originalLink : originalLinks) {
            final Annotation annotation = ((ImageAnnotationLink) originalLink).getChild();
            originalAnnotationIds.put(annotation.getClass().getSimpleName(), annotation.getId().getValue());
            originalLinkIds.add(originalLink.getId().getValue());
        }
        testImages.add(originalImageId);

        /* duplicate the image */

        final Duplicate dup = Requests.duplicate().target("Image").id(originalImageId)
                .duplicateType("BasicAnnotation", "XmlAnnotation")
                .referenceType("DoubleAnnotation", "TextAnnotation").build();
        final DuplicateResponse response = (DuplicateResponse) doChange(dup);

        /* check that the response includes duplication of an image, annotations and their links */

        final Set<Long> reportedImageIds = new HashSet<Long>(
                response.duplicates.get("ome.model.core.Image"));
        Assert.assertFalse(response.duplicates.containsKey("ome.model.annotations.DoubleAnnotation"));
        final Set<Long> reportedLongAnnotationIds = new HashSet<Long>(
                response.duplicates.get("ome.model.annotations.LongAnnotation"));
        final Set<Long> reportedMapAnnotationIds = new HashSet<Long>(
                response.duplicates.get("ome.model.annotations.MapAnnotation"));
        Assert.assertFalse(response.duplicates.containsKey("ome.model.annotations.TagAnnotation"));
        final Set<Long> reportedXmlAnnotationIds = new HashSet<Long>(
                response.duplicates.get("ome.model.annotations.XmlAnnotation"));
        final Set<Long> reportedLinkIds = new HashSet<Long>(
                response.duplicates.get("ome.model.annotations.ImageAnnotationLink"));

        Assert.assertEquals(reportedImageIds.size(), 1);
        Assert.assertEquals(reportedLongAnnotationIds.size(), 1);
        Assert.assertEquals(reportedMapAnnotationIds.size(), 1);
        Assert.assertEquals(reportedXmlAnnotationIds.size(), 1);
        Assert.assertEquals(reportedLinkIds.size(), originalLinkIds.size());

        /* check that the reported image, links, and annotations each have a new ID where expected */

        final long reportedImageId = reportedImageIds.iterator().next();
        final long reportedLongAnnotationId = reportedLongAnnotationIds.iterator().next();
        final long reportedMapAnnotationId = reportedMapAnnotationIds.iterator().next();
        final long reportedXmlAnnotationId = reportedXmlAnnotationIds.iterator().next();
        testImages.add(reportedImageId);

        Assert.assertNotEquals(originalImageId, reportedImageId);
        Assert.assertNotEquals(originalAnnotationIds.get("LongAnnotationI"), reportedLongAnnotationId);
        Assert.assertNotEquals(originalAnnotationIds.get("MapAnnotationI"), reportedMapAnnotationId);
        Assert.assertNotEquals(originalAnnotationIds.get("XmlAnnotationI"), reportedXmlAnnotationId);
        Assert.assertTrue(Sets.intersection(originalLinkIds, reportedLinkIds).isEmpty());

        /* check that the annotations on the images are exactly as expected */

        final Set<Long> expectedAnnotationIds = ImmutableSet.of(
                /* reference includes DoubleAnnotation */
                originalAnnotationIds.get("DoubleAnnotationI"),
                /* duplicate includes BasicAnnotation */
                reportedLongAnnotationId,
                /* default behavior */
                reportedMapAnnotationId,
                /* reference includes TextAnnotation */
                originalAnnotationIds.get("TagAnnotationI"),
                /* duplicate includes XmlAnnotation */
                reportedXmlAnnotationId);

        Assert.assertEquals(getImageAnnotations(originalImageId), new HashSet<Long>(originalAnnotationIds.values()));
        Assert.assertEquals(getImageAnnotations(reportedImageId), expectedAnnotationIds);
    }

    /**
     * Test duplication of an annotated image with contradictory treatments for the same annotation types.
     * @throws Exception unexpected
     */
    @Test
    public void testDuplicateImageWithOpposingTypeOptions() throws Exception {
        newUserAndGroup("rwra--");

        /* create an image */

        Image originalImage = mmFactory.simpleImage();
        originalImage = (Image) iUpdate.saveAndReturnObject(originalImage);

        /* note the objects (and their IDs) that were thus created and saved */

        final long originalImageId = originalImage.getId().getValue();
        testImages.add(originalImageId);

        /* duplicate the image with contradictory instructions */

        final Duplicate dup = Requests.duplicate().target("Image").id(originalImageId).build();

        for (int i = 0; i < 4; i++) {
            dup.typesToDuplicate = i == 0 ? null : ImmutableList.of("IAnnotationLink");
            dup.typesToReference = i == 1 ? null : ImmutableList.of("IAnnotationLink");
            dup.typesToIgnore    = i == 2 ? null : ImmutableList.of("IAnnotationLink");
            final ERR response = (ERR) doChange(client, factory, dup, false);
            Assert.assertEquals(response.name, "bad-class");
        }
    }

    /**
     * Test duplication of a dataset's image. {@link SkipHead} is used to identify the image via its dataset.
     * @throws Exception unexpected
     */
    @Test(groups = "ticket:13197")
    public void testDuplicateImageViaSkipHead() throws Exception {
        newUserAndGroup("rwr---");

        /* create a dataset with an image */

        final Dataset originalDataset = mmFactory.simpleDataset();
        final Image originalImage = mmFactory.simpleImage();
        DatasetImageLink originalLink = new DatasetImageLinkI();
        originalLink.setParent(originalDataset);
        originalLink.setChild(originalImage);
        originalLink = (DatasetImageLink) iUpdate.saveAndReturnObject(originalLink);

        /* note the objects (and their IDs) that were thus created and saved */

        final long originalDatasetId = originalLink.getParent().getId().getValue();
        final long originalImageId = originalLink.getChild().getId().getValue();
        final long originalLinkId = originalLink.getId().getValue();
        testImages.add(originalImageId);

        /* duplicate the image via SkipHead */

        final SkipHead dup =
                Requests.skipHead().target("Dataset").id(originalDatasetId).startFrom("Image").request(Duplicate.class).build();
        final DuplicateResponse response = (DuplicateResponse) doChange(dup);

        /* check that the response includes duplication of only the image */

        final List<Long> reportedDatasetIds = response.duplicates.get("ome.model.containers.Dataset");
        final List<Long> reportedImageIds = response.duplicates.get("ome.model.core.Image");
        final List<Long> reportedLinkIds = response.duplicates.get("ome.model.annotations.ImageAnnotationLink");

        Assert.assertNull(reportedDatasetIds);
        Assert.assertEquals(reportedImageIds.size(), 1);
        Assert.assertNull(reportedLinkIds);

        /* check that the reported image has a new ID */

        final long reportedImageId = reportedImageIds.get(0);
        testImages.add(reportedImageId);

        Assert.assertNotEquals(originalImageId, reportedImageId);
    }

    /**
     * Test duplication of links depending on ownership and group permissions.
     * @param groupPerms the permissions on the group in which to test
     * @param myContainer if the parent of the link is owned by the user doing the duplication
     * @param myContained if the child of the link is owned by the user doing the duplication
     * @param myLink if the link is owned by the user doing the duplication
     * @param containerType what kind of model object the parent of the link is
     * @param containedType what kind of model object the child of the link is
     * @throws Exception unexpected
     */
    @Test(dataProvider = "container permissions test cases")
    public void testDuplicateWithContainerPermissions(String groupPerms, boolean myContainer, boolean myContained, boolean myLink,
            Class<? extends IObject> containerType, Class<? extends IObject> containedType) throws Exception {
        final EventContext duplicator = newUserAndGroup(groupPerms);
        final EventContext containerOwner, containedOwner, linkOwner;

        /* set up the model object owners */

        if (myContainer) {
            containerOwner = duplicator;
        } else {
            containerOwner = newUserInGroup();
        }

        if (myContained) {
            containedOwner = duplicator;
        } else if (!myContainer) {
            containedOwner = containerOwner;
        } else {
            containedOwner = newUserInGroup();
        }

        if (myLink) {
            linkOwner = duplicator;
        } else if (!myContainer) {
            linkOwner = containerOwner;
        } else if (!myContained) {
            linkOwner = containedOwner;
        } else {
            linkOwner = newUserInGroup();
        }

        /* create the model objects */

        final IObject container, contained, link;

        loginUser(containerOwner);

        if (containerType == Project.class) {
            container = iUpdate.saveAndReturnObject(mmFactory.simpleProject());
        } else if (containerType == Dataset.class) {
            container = iUpdate.saveAndReturnObject(mmFactory.simpleDataset());
        } else if (containerType == Folder.class) {
            container = iUpdate.saveAndReturnObject(mmFactory.simpleFolder());
        } else if (containerType == Screen.class) {
            container = iUpdate.saveAndReturnObject(mmFactory.simpleScreen());
        } else {
            throw new IllegalArgumentException("container type cannot be " + containerType);
        }

        if (containerOwner != containedOwner) {
            loginUser(containedOwner);
        }

        if (containedType == Dataset.class) {
            contained = iUpdate.saveAndReturnObject(mmFactory.simpleDataset());
        } else if (containedType == Image.class) {
            contained = iUpdate.saveAndReturnObject(mmFactory.simpleImage());
            testImages.add(contained.getId().getValue());
        } else if (containedType == Roi.class) {
            contained = iUpdate.saveAndReturnObject(new RoiI());
        } else if (containedType == Plate.class) {
            contained = iUpdate.saveAndReturnObject(mmFactory.createPlate(0, 0, 0, 0, false));
        } else {
            throw new IllegalArgumentException("contained type cannot be " + containedType);
        }

        if (containedOwner != linkOwner) {
            loginUser(linkOwner);
        }

        if (containerType == Project.class && containedType == Dataset.class) {
            final ProjectDatasetLink linkPD = new ProjectDatasetLinkI();
            linkPD.setParent((Project) container);
            linkPD.setChild((Dataset) contained);
            link = iUpdate.saveAndReturnObject(linkPD);
        } else if (containerType == Dataset.class && containedType == Image.class) {
            final DatasetImageLink linkDI = new DatasetImageLinkI();
            linkDI.setParent((Dataset) container);
            linkDI.setChild((Image) contained);
            link = iUpdate.saveAndReturnObject(linkDI);
        } else if (containerType == Folder.class && containedType == Image.class) {
            final FolderImageLink linkFI = new FolderImageLinkI();
            linkFI.setParent((Folder) container);
            linkFI.setChild((Image) contained);
            link = iUpdate.saveAndReturnObject(linkFI);
        } else if (containerType == Folder.class && containedType == Roi.class) {
            final FolderRoiLink linkFR = new FolderRoiLinkI();
            linkFR.setParent((Folder) container);
            linkFR.setChild((Roi) contained);
            link = iUpdate.saveAndReturnObject(linkFR);
        } else if (containerType == Screen.class && containedType == Plate.class) {
            final ScreenPlateLink linkSP = new ScreenPlateLinkI();
            linkSP.setParent((Screen) container);
            linkSP.setChild((Plate) contained);
            link = iUpdate.saveAndReturnObject(linkSP);
        } else {
            throw new IllegalArgumentException("type " + containerType + " cannot contain type " + containedType);
        }

        /* duplicate the link */

        if (linkOwner != duplicator) {
            loginUser(duplicator);
        }

        final boolean expectSuccess = myContainer || "rwrw--".equals(groupPerms);

        final Duplicate dup = Requests.duplicate().target(link).build();
        final Response response = doChange(client, factory, dup, expectSuccess);

        if (expectSuccess) {

            /* find the duplicated link */

            String linkType = null;
            long linkId = -1;
            for (final Map.Entry<String, List<Long>> duplicate : ((DuplicateResponse) response).duplicates.entrySet()) {
                final String duplicateType = duplicate.getKey();
                final List<Long> duplicateIds = duplicate.getValue();
                if (duplicateType.endsWith("Link")) {
                    Assert.assertNull(linkType);
                    Assert.assertEquals(duplicateIds.size(), 1);
                    linkType = duplicateType;
                    linkId = duplicateIds.get(0);
                }
            }
            Assert.assertNotNull(linkType);

            /* check that the link parent is as before but the child is not */

            final String query = "SELECT parent.id, child.id FROM " + linkType + " WHERE id = :id";
            final Parameters params = new ParametersI().addId(linkId);
            final List<RType> result = iQuery.projection(query, params).get(0);
            final long parentId = ((RLong) result.get(0)).getValue();
            final long childId = ((RLong) result.get(1)).getValue();

            if (containedType == Image.class) {
                testImages.add(childId);
            }

            Assert.assertEquals(parentId, container.getId().getValue());
            Assert.assertNotEquals(childId, contained.getId().getValue());
        }
    }

    /**
     * @return group permissions for container permissions test cases
     */
    @DataProvider(name = "container permissions test cases")
    public Object[][] provideContainerPermissionsCases() {
        int index = 0;
        final int GROUP_PERMS = index++;
        final int MY_CONTAINER = index++;
        final int MY_CONTAINED = index++;
        final int MY_LINK = index++;
        final int CONTAINER_TYPE = index++;
        final int CONTAINED_TYPE = index++;

        final String[] permsCases = new String[]{"rwra--", "rwrw--"};
        final boolean[] booleanCases = new boolean[]{false, true};
        final SetMultimap<Class<? extends IObject>, Class<? extends IObject>> containmentRelations = HashMultimap.create();
        containmentRelations.put(Project.class, Dataset.class);
        containmentRelations.put(Dataset.class, Image.class);
        containmentRelations.put(Folder.class, Image.class);
        containmentRelations.put(Folder.class, Roi.class);
        containmentRelations.put(Screen.class, Plate.class);

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final String groupPerms : permsCases) {
            for (final boolean myContainer : booleanCases) {
                for (final boolean myContained : booleanCases) {
                    for (final boolean myLink : booleanCases) {
                        for (final Map.Entry<Class<? extends IObject>, Class<? extends IObject>> containmentRelation :
                            containmentRelations.entries()) {
                            if ((myContainer != myContained || myContainer != myLink) && "rwra--".equals(groupPerms)) {
                                /* test case does not make sense */
                                continue;
                            }
                            final Object[] testCase = new Object[index];
                            testCase[GROUP_PERMS] = groupPerms;
                            testCase[MY_CONTAINER] = myContainer;
                            testCase[MY_CONTAINED] = myContained;
                            testCase[MY_LINK] = myLink;
                            testCase[CONTAINER_TYPE] = containmentRelation.getKey();
                            testCase[CONTAINED_TYPE] = containmentRelation.getValue();
                            // DEBUG: if ("rwrw--".equals(groupPerms) &&
                            //        myContainer == true && myContained == false && myLink == true &&
                            //        containmentRelation.getKey() == Folder.class && containmentRelation.getValue() == Image.class)
                            testCases.add(testCase);
                        }
                    }
                }
            }
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * Test duplication of a rich plate structure.
     * @throws Exception unexpected
     */
    @Test(groups = "ticket:13171")
    public void testDuplicatePlate() throws Exception {
        newUserAndGroup("rw----");

        final int rowCount = 2;
        final int columnCount = 2;
        final int fieldCount = 2;
        final int runCount = 2;
        final int timeCount = 2;
        final int channelCount = 4;

        /* create a new plate */

        Plate originalPlate = mmFactory.createPlate(rowCount, columnCount, fieldCount, runCount, 1, 1, 1, timeCount, channelCount);
        originalPlate = (Plate) iUpdate.saveAndReturnObject(originalPlate).proxy();

        /* note the objects (and their IDs) that were thus created and saved */

        final long originalPlateId = originalPlate.getId().getValue();

        final String hql1 = "FROM Plate AS plate " +
                "LEFT JOIN FETCH plate.wells AS well " +
                "LEFT JOIN FETCH plate.plateAcquisitions AS run " +
                "LEFT JOIN FETCH run.wellSample AS field " +
                "LEFT JOIN FETCH field.image AS image " +
                "LEFT JOIN FETCH image.pixels AS pixels " +
                "LEFT JOIN FETCH pixels.channels AS channels " +
                "LEFT JOIN FETCH pixels.planeInfo as planeInfo";
        final Parameters params1 = new ParametersI().addId(originalPlateId);
        originalPlate = (Plate) iQuery.findByQuery(hql1, params1);

        final Set<Long> originalWellIds = new HashSet<Long>();
        final Set<Long> originalRunIds = new HashSet<Long>();
        final Set<Long> originalFieldIds = new HashSet<Long>();
        final Set<Long> originalImageIds = new HashSet<Long>();
        final Set<Long> originalPixelsIds = new HashSet<Long>();
        final Set<Long> originalChannelIds = new HashSet<Long>();
        final Set<Long> originalPlaneInfoIds = new HashSet<Long>();

        for (final Well well : originalPlate.copyWells()) {
            originalWellIds.add(well.getId().getValue());
        }
        for (final PlateAcquisition run : originalPlate.copyPlateAcquisitions()) {
            originalRunIds.add(run.getId().getValue());
            for (final WellSample field : run.copyWellSample()) {
                final Image image = field.getImage();
                final Pixels pixels = image.getPrimaryPixels();
                originalFieldIds.add(field.getId().getValue());
                originalImageIds.add(image.getId().getValue());
                originalPixelsIds.add(pixels.getId().getValue());
                for (final Channel channel : pixels.copyChannels()) {
                    originalChannelIds.add(channel.getId().getValue());
                }
                for (final PlaneInfo planeInfo : pixels.copyPlaneInfo()) {
                    originalPlaneInfoIds.add(planeInfo.getId().getValue());
                }
            }
        }

        /* add ROIs to the well sample images and also note their IDs */

        final Set<Long> originalRoiIds = new HashSet<Long>();
        final Set<Long> originalPointIds = new HashSet<Long>();

        for (final Long originalImageId : originalImageIds) {
            Point originalPoint = new PointI();
            originalPoint.setX(omero.rtypes.rdouble(0));
            originalPoint.setY(omero.rtypes.rdouble(0));
            Roi originalRoi = new RoiI();
            originalRoi.setImage(new ImageI(originalImageId, false));
            originalRoi.addShape(originalPoint);
            originalRoi = (Roi) iUpdate.saveAndReturnObject(originalRoi);
            final Long originalRoiId = originalRoi.getId().getValue();
            originalPoint = (Point) iQuery.findByQuery("FROM Point WHERE roi.id = :id", new ParametersI().addId(originalRoiId));
            final Long originalPointId = originalPoint.getId().getValue();
            Assert.assertEquals(originalRoi.getImage().getId().getValue(), (long) originalImageId);
            originalRoiIds.add(originalRoiId);
            originalPointIds.add(originalPointId);
        }

        /* check that the expected numbers of objects were created */

        Assert.assertEquals(originalWellIds.size(), rowCount * columnCount);
        Assert.assertEquals(originalRunIds.size(), runCount);
        Assert.assertEquals(originalFieldIds.size(), rowCount * columnCount * runCount * fieldCount);
        Assert.assertEquals(originalImageIds.size(), rowCount * columnCount * runCount * fieldCount);
        Assert.assertEquals(originalPixelsIds.size(), rowCount * columnCount * runCount * fieldCount);
        Assert.assertEquals(originalChannelIds.size(), rowCount * columnCount * runCount * fieldCount * channelCount);
        Assert.assertEquals(originalPlaneInfoIds.size(), rowCount * columnCount * runCount * fieldCount * timeCount * channelCount);
        Assert.assertEquals(originalRoiIds.size(), rowCount * columnCount * runCount * fieldCount);
        Assert.assertEquals(originalPointIds.size(), rowCount * columnCount * runCount * fieldCount);

        /* combine all the logical channels into one: this often triggers duplication failure in OMERO 5.2.2 */

        final String hql2 = "FROM Channel WHERE id IN (:ids)";
        final Parameters params2 = new ParametersI().addIds(originalChannelIds);
        final List<IObject> results = iQuery.findAllByQuery(hql2, params2);
        final LogicalChannel originalLogicalChannel = ((Channel) results.get(0)).getLogicalChannel();
        for (int index = 1; index < results.size(); index++) {
            final Channel originalChannel = (Channel) results.get(index);
            originalChannel.setLogicalChannel(originalLogicalChannel);
        }
        iUpdate.saveCollection(results);

        /* duplicate the plate */

        final Duplicate dup = Requests.duplicate().target(originalPlate).build();
        final DuplicateResponse response = (DuplicateResponse) doChange(dup);

        /* capture the IDs of the duplicated plate, wells, runs, fields, images, ROIs and shapes */

        final Set<Long> reportedPlateIds = new HashSet<Long>(response.duplicates.get("ome.model.screen.Plate"));
        final Set<Long> reportedWellIds = new HashSet<Long>(response.duplicates.get("ome.model.screen.Well"));
        final Set<Long> reportedRunIds = new HashSet<Long>(response.duplicates.get("ome.model.screen.PlateAcquisition"));
        final Set<Long> reportedFieldIds = new HashSet<Long>(response.duplicates.get("ome.model.screen.WellSample"));
        final Set<Long> reportedImageIds = new HashSet<Long>(response.duplicates.get("ome.model.core.Image"));
        final Set<Long> reportedPixelsIds = new HashSet<Long>(response.duplicates.get("ome.model.core.Pixels"));
        final Set<Long> reportedChannelIds = new HashSet<Long>(response.duplicates.get("ome.model.core.Channel"));
        final Set<Long> reportedPlaneInfoIds = new HashSet<Long>(response.duplicates.get("ome.model.core.PlaneInfo"));
        final Set<Long> reportedRoiIds = new HashSet<Long>(response.duplicates.get("ome.model.roi.Roi"));
        final Set<Long> reportedPointIds = new HashSet<Long>(response.duplicates.get("ome.model.roi.Point"));

        /* delete the original and duplicate plates */

        doChange(Requests.delete()
                .target("Plate").id(originalPlateId).id(reportedPlateIds).build());

        /* check that the reported plate, wells, runs, fields, images, ROIs and shapes all have new IDs */

        Assert.assertEquals(reportedPlateIds.size(), 1);
        Assert.assertEquals(reportedWellIds.size(), originalWellIds.size());
        Assert.assertEquals(reportedRunIds.size(), originalRunIds.size());
        Assert.assertEquals(reportedFieldIds.size(), originalFieldIds.size());
        Assert.assertEquals(reportedImageIds.size(), originalImageIds.size());
        Assert.assertEquals(reportedPixelsIds.size(), originalPixelsIds.size());
        Assert.assertEquals(reportedChannelIds.size(), originalChannelIds.size());
        Assert.assertEquals(reportedPlaneInfoIds.size(), originalPlaneInfoIds.size());
        Assert.assertEquals(reportedRoiIds.size(), originalRoiIds.size());
        Assert.assertEquals(reportedPointIds.size(), originalPointIds.size());

        Assert.assertNotEquals(originalPlateId, reportedPlateIds.iterator().next());
        Assert.assertTrue(Sets.intersection(originalWellIds, reportedWellIds).isEmpty());
        Assert.assertTrue(Sets.intersection(originalRunIds, reportedRunIds).isEmpty());
        Assert.assertTrue(Sets.intersection(originalFieldIds, reportedFieldIds).isEmpty());
        Assert.assertTrue(Sets.intersection(originalImageIds, reportedImageIds).isEmpty());
        Assert.assertTrue(Sets.intersection(originalPixelsIds, reportedPixelsIds).isEmpty());
        Assert.assertTrue(Sets.intersection(originalChannelIds, reportedChannelIds).isEmpty());
        Assert.assertTrue(Sets.intersection(originalPlaneInfoIds, reportedPlaneInfoIds).isEmpty());
        Assert.assertTrue(Sets.intersection(originalRoiIds, reportedRoiIds).isEmpty());
        Assert.assertTrue(Sets.intersection(originalPointIds, reportedPointIds).isEmpty());
    }

    /**
     * Test duplication of plates that share logical channels.
     * @throws Exception unexpected
     */
    @Test(groups = "ticket:13199")
    public void testDuplicatePlatesSharingLogicalChannel() throws Exception {
        newUserAndGroup("rw----");
        final List<Long> plateIds = new ArrayList<Long>();

        /* create the plates */

        final Plate plate1 = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1, 1, 1, 1, true)).proxy();
        final Plate plate2 = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1, 1, 1, 1, true)).proxy();
        plateIds.add(plate1.getId().getValue());
        plateIds.add(plate2.getId().getValue());

        /* combine all the logical channels into one */

        final List<IObject> results = iQuery.findAllByQuery(
                "FROM Channel WHERE pixels.image IN (SELECT image FROM WellSample WHERE well.plate.id IN (:ids))",
                new ParametersI().addIds(plateIds));
        final LogicalChannel originalLogicalChannel = ((Channel) results.get(0)).getLogicalChannel();
        for (int index = 1; index < results.size(); index++) {
            final Channel originalChannel = (Channel) results.get(index);
            originalChannel.setLogicalChannel(originalLogicalChannel);
        }
        iUpdate.saveCollection(results);

        /* perform the tests */

        for (final boolean dupPlate1 : Arrays.asList(true, false)) {
            for (final boolean dupPlate2 : Arrays.asList(true, false)) {
                if (dupPlate1 || dupPlate2) {

                    /* duplicate the plate(s) */

                    final DuplicateBuilder dupBuilder = Requests.duplicate();
                    if (dupPlate1) {
                        dupBuilder.target(plate1);
                    }
                    if (dupPlate2) {
                        dupBuilder.target(plate2);
                    }
                    final DuplicateResponse response = (DuplicateResponse) doChange(dupBuilder.build());

                    /* note the response */

                    final Collection<Long> reportedPlateIds = response.duplicates.get("ome.model.screen.Plate");
                    final Collection<Long> reportedLogicalChannelIds = response.duplicates.get("ome.model.core.LogicalChannel");
                    final Collection<Long> reportedChannelIds = response.duplicates.get("ome.model.core.Channel");
                    plateIds.addAll(reportedPlateIds);

                    /* check that duplication targeted the expected objects */

                    if (dupPlate1 && dupPlate2) {
                        Assert.assertEquals(reportedPlateIds.size(), 2);
                        Assert.assertEquals(reportedChannelIds.size(), 2);
                        Assert.assertEquals(reportedLogicalChannelIds.size(), 1);
                    } else {
                        Assert.assertEquals(reportedPlateIds.size(), 1);
                        Assert.assertEquals(reportedChannelIds.size(), 1);
                        Assert.assertNull(reportedLogicalChannelIds);
                    }
                }
            }
        }

        /* delete the original and duplicate plates */

        doChange(Requests.delete().target("Plate").id(plateIds).build());
    }
}

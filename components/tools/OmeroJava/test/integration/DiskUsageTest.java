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

package integration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ome.services.blitz.repo.PublicRepositoryI;
import omero.RLong;
import omero.RObject;
import omero.RType;
import omero.ServerError;
import omero.api.LongPair;
import omero.cmd.Delete2;
import omero.cmd.DiskUsage;
import omero.cmd.DiskUsageResponse;
import omero.cmd.ManageImageBinaries;
import omero.cmd.ManageImageBinariesResponse;
import omero.cmd.graphs.ChildOption;
import omero.gateway.util.Requests;
import omero.model.Annotation;
import omero.model.Channel;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Instrument;
import omero.model.Objective;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.util.ResourceUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * Integration tests for the {@link omero.cmd.DiskUsage} request.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
@Test(groups = { "integration" })
public class DiskUsageTest extends AbstractServerTest {

    private EventContext ec;
    private Long imageId;
    private Long pixelsId;
    private Long fileSize;
    private Long thumbnailSize;

    /**
     * Convert a {@code Collection<Long>} to a {@code List<Long>}.
     */
    private static Function<Collection<Long>, List<Long>> LONG_COLLECTION_TO_LIST =
            new Function<Collection<Long>, List<Long>>() {
        @Override
        public List<Long> apply(Collection<Long> ids) {
            return new ArrayList<Long>(ids);
        }
    };

    /**
     * Submit a disk usage request for the given objects and return the server's response.
     * @param objects the target objects
     * @return the objects' disk usage
     * @throws Exception if thrown during request execution
     */
    private DiskUsageResponse runDiskUsage(Map<java.lang.String, ? extends Collection<Long>> objects) throws Exception {
        final DiskUsage request = new DiskUsage();
        request.objects = Maps.transformValues(objects, LONG_COLLECTION_TO_LIST);
        return (DiskUsageResponse) doChange(request);
    }

    /**
     * Create a new file annotation.
     * @param size the size of the file annotation
     * @return the ID of the new file annotation
     * @throws Exception unexpected
     */
    private long createFileAnnotation(long size) throws Exception {
        final OriginalFile file = mmFactory.createOriginalFile();
        file.setSize(omero.rtypes.rlong(size));

        FileAnnotationI annotation = new FileAnnotationI();
        annotation.setFile(file);
        annotation = (FileAnnotationI) iUpdate.saveAndReturnObject(annotation);
        return annotation.getId().getValue();
    }

    /**
     * Retrieve a model object from the database. Assumed to exist.
     * @param targetClass the object's class
     * @param targetId the object's ID
     * @return the object
     * @throws ServerError if the retrieval failed
     */
    private <X extends IObject> X queryForObject(Class<X> targetClass, long targetId) throws ServerError {
        final String query = "FROM " + targetClass.getSimpleName() + " WHERE id = " + targetId;
        final List<List<RType>> results = iQuery.projection(query, null);
        return targetClass.cast(((RObject) results.get(0).get(0)).getValue());
    }

    /**
     * Run a HQL query that accepts a single ID and returns a single ID.
     * @param query the query to run
     * @param id the ID for the query's {@code :id} field
     * @return the ID returned by the query
     * @throws ServerError if the query failed
     */
    private long queryForId(String query, long id) throws ServerError {
        final List<List<RType>> results = iQuery.projection(query, new ParametersI().addId(id));
        return ((RLong) results.get(0).get(0)).getValue();
    }

    /**
     * Add an annotation to the given object.
     * @param targetClass the object's class
     * @param targetId the object's ID
     * @param annotationId the ID of the annotation to add to the object
     * @throws Exception unexpected
     */
    private void addAnnotation(Class<? extends IObject> targetClass, long targetId, long annotationId) throws Exception {
        final String className = targetClass.getSimpleName();
        final IObject parent = queryForObject(targetClass, targetId);
        final Annotation child = queryForObject(Annotation.class, annotationId);

        final String linkClassName = "omero.model." + className + "AnnotationLinkI";
        final Class<? extends IObject> linkClass = Class.forName(linkClassName).asSubclass(IObject.class);
        final IObject link = linkClass.newInstance();

        BeanUtils.setProperty(link, "parent", parent);
        BeanUtils.setProperty(link, "child", child);
        iUpdate.saveObject(link);
    }

    /**
     * Test that two maps have the same keys with the same non-null values.
     * @param actual the map of actual values
     * @param expected the map of expected values
     */
    private static <K, V> void assertMapsEqual(Map<K, V> actual, Map<K, V> expected) {
        Assert.assertEquals(actual.size(), expected.size());
        for (final Map.Entry<K, V> actualEntry : actual.entrySet()) {
            final K actualKey = actualEntry.getKey();
            final V actualValue = actualEntry.getValue();
            Assert.assertNotNull(actualValue);
            Assert.assertEquals(actualValue, expected.get(actualKey));
        }
    }

    /**
     * Import a test image and note information related to it.
     * @throws Throwable unexpected
     */
    @BeforeClass
    public void setup() throws Throwable {
        ec = iAdmin.getEventContext();

        final File imageFile = ResourceUtils.getFile("classpath:tinyTest.d3d.dv");
        fileSize = imageFile.length();

        final Pixels pixels = importFile(imageFile, "dv").get(0);
        pixelsId = pixels.getId().getValue();
        imageId = pixels.getImage().getId().getValue();

        final ManageImageBinaries mibRequest = new ManageImageBinaries();
        mibRequest.imageId = imageId;
        final ManageImageBinariesResponse mibResponse = (ManageImageBinariesResponse) doChange(mibRequest);
        thumbnailSize = mibResponse.thumbnailSize;

        Assert.assertNotNull(ec);
        Assert.assertNotNull(imageId);
        Assert.assertNotNull(pixelsId);
        Assert.assertNotNull(fileSize);
        Assert.assertNotNull(thumbnailSize);
    }

    /**
     * Delete the test image.
     * @throws Exception unexpected
     */
    @AfterClass
    public void teardown() throws Exception {
        if (imageId != null) {
            final Delete2 request = Requests.delete("Image", imageId);
            doChange(request);
        }
    }

    /**
     * Test that the usage is associated with the correct user and group.
     * @throws Exception unexpected
     */
    @Test
    public void testOwnership() throws Exception {
        final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
        final ImmutableList<Map<LongPair, ?>> responseElements = ImmutableList.of(
                        response.bytesUsedByReferer, response.fileCountByReferer, response.totalBytesUsed, response.totalFileCount);
        for (final Map<LongPair, ?> responseElement : responseElements) {
            Assert.assertEquals(responseElement.size(), 1);
            for (final LongPair key : responseElement.keySet()) {
                Assert.assertEquals(key.first, ec.userId);
                Assert.assertEquals(key.second, ec.groupId);
            }
        }
    }


    /**
     * Test that the file size of the actual image file is correctly computed.
     * @throws Exception unexpected
     */
    @Test
    public void testFileSize() throws Exception {
        final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
        Assert.assertEquals(response.bytesUsedByReferer.size(), 1);
        for (final Map<String, Long> byReferer : response.bytesUsedByReferer.values()) {
            Assert.assertEquals(byReferer.get("FilesetEntry"), fileSize);
        }
    }

    /**
     * Test that the file size of the actual image file is correctly computed even when containers must be opened.
     * @throws Exception unexpected
     */
    @Test
    public void testFileSizeInContainers() throws Exception {
        final Project project = new ProjectI();
        project.setName(omero.rtypes.rstring("test project"));
        final Dataset dataset = new DatasetI();
        dataset.setName(omero.rtypes.rstring("test dataset"));

        final long projectId = iUpdate.saveAndReturnObject(project).getId().getValue();
        final long datasetId = iUpdate.saveAndReturnObject(dataset).getId().getValue();

        final ProjectDatasetLink pdl = new ProjectDatasetLinkI();
        pdl.setParent(new ProjectI(projectId, false));
        pdl.setChild(new DatasetI(datasetId, false));
        iUpdate.saveObject(pdl);

        final DatasetImageLink dil = new DatasetImageLinkI();
        dil.setParent(new DatasetI(datasetId, false));
        dil.setChild(new ImageI(imageId, false));
        iUpdate.saveObject(dil);

        try {
            final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Project", Collections.singleton(projectId)));
            Assert.assertEquals(response.bytesUsedByReferer.size(), 1);
            for (final Map<String, Long> byReferer : response.bytesUsedByReferer.values()) {
                Assert.assertEquals(byReferer.get("FilesetEntry"), fileSize);
            }
        } finally {
            final Delete2 request = Requests.delete("Project", projectId);

            final ChildOption option = new ChildOption();
            option.excludeType = Collections.singletonList("Image");
            request.childOptions = Collections.singletonList(option);

            doChange(request);
        }
    }

    /**
     * Test that the import log size for the image is correctly computed.
     * @throws Exception unexpected
     */
    @Test
    public void testImportLogSize() throws Exception {
        final String query = "SELECT o.size FROM " +
                "Image i, Fileset f, FilesetJobLink fjl, UploadJob j, JobOriginalFileLink jol, OriginalFile o " +
                "WHERE i.id = :id AND f = i.fileset AND fjl.parent = f AND fjl.child = j AND jol.parent = j AND jol.child = o " +
                "AND o.mimetype = '" + PublicRepositoryI.IMPORT_LOG_MIMETYPE + "'";

        final long importLogSize = queryForId(query, imageId);

        final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
        Assert.assertEquals(response.bytesUsedByReferer.size(), 1);
        for (final Map<String, Long> byReferer : response.bytesUsedByReferer.values()) {
            Assert.assertEquals((long) byReferer.get("Job"), importLogSize);
        }
    }

    /**
     * Test that the size of the thumbnail is correctly computed.
     * @throws Exception unexpected
     */
    @Test
    public void testThumbnailSize() throws Exception {
        final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
        Assert.assertEquals(response.bytesUsedByReferer.size(), 1);
        for (final Map<String, Long> byReferer : response.bytesUsedByReferer.values()) {
            Assert.assertEquals(byReferer.get("Thumbnail"), thumbnailSize);
        }
    }

    /**
     * Test that the size of the file annotations is correctly computed.
     * @throws Exception unexpected
     */
    @Test
    public void testFileAnnotationSize() throws Exception {
        final Random rng = new Random(123456);  // fixed seed for deterministic testing
        final int annotationCount = 5;
        long totalAnnotationSize = 0;
        final List<Long> annotationIds = new ArrayList<Long>(annotationCount);
        for (int i = 0; i < annotationCount; i++) {
            final long size = rng.nextInt(Integer.MAX_VALUE) + 1L;  // positive
            totalAnnotationSize += size;
            annotationIds.add(createFileAnnotation(size));
        }

        final long channelId = queryForId("SELECT id FROM Channel WHERE pixels.id = :id", pixelsId);
        final long instrumentId = queryForId("SELECT instrument.id FROM Image WHERE id = :id", imageId);
        final long objectiveId = queryForId("SELECT id FROM Objective WHERE instrument.id = :id", instrumentId);

        addAnnotation(Image.class, imageId, annotationIds.get(0));
        addAnnotation(Channel.class, channelId, annotationIds.get(1));
        addAnnotation(Instrument.class, instrumentId, annotationIds.get(2));
        addAnnotation(Objective.class, objectiveId, annotationIds.get(3));
        addAnnotation(Annotation.class, annotationIds.get(3), annotationIds.get(4));

        try {
            final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
            Assert.assertEquals(response.bytesUsedByReferer.size(), 1);
            for (final Map<String, Long> byReferer : response.bytesUsedByReferer.values()) {
                Assert.assertEquals(byReferer.get("Annotation"), (Long) totalAnnotationSize);
            }
        } finally {
            final Delete2 request = Requests.delete("Annotation", annotationIds);
            doChange(request);
        }
    }

    /**
     * Test that the size of the file annotations is correctly computed even if some are attached to multiple objects.
     * @throws Exception unexpected
     */
    @Test
    public void testDuplicatedFileAnnotationSize() throws Exception {
        final Random rng = new Random(123456);  // fixed seed for deterministic testing
        final int annotationCount = 3;
        long totalAnnotationSize = 0;
        final List<Long> annotationIds = new ArrayList<Long>(annotationCount);
        for (int i = 0; i < annotationCount; i++) {
            final long size = rng.nextInt(Integer.MAX_VALUE) + 1L;  // positive
            totalAnnotationSize += size;
            annotationIds.add(createFileAnnotation(size));
        }

        final long channelId = queryForId("SELECT id FROM Channel WHERE pixels.id = :id", pixelsId);
        final long instrumentId = queryForId("SELECT instrument.id FROM Image WHERE id = :id", imageId);
        final long objectiveId = queryForId("SELECT id FROM Objective WHERE instrument.id = :id", instrumentId);

        addAnnotation(Image.class, imageId, annotationIds.get(0));
        addAnnotation(Channel.class, channelId, annotationIds.get(1));
        addAnnotation(Instrument.class, instrumentId, annotationIds.get(2));
        addAnnotation(Objective.class, objectiveId, annotationIds.get(0));
        addAnnotation(Annotation.class, annotationIds.get(0), annotationIds.get(1));
        addAnnotation(Annotation.class, annotationIds.get(1), annotationIds.get(2));

        try {
            final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
            Assert.assertEquals(response.bytesUsedByReferer.size(), 1);
            for (final Map<String, Long> byReferer : response.bytesUsedByReferer.values()) {
                Assert.assertEquals(byReferer.get("Annotation"), (Long) totalAnnotationSize);
            }
        } finally {
            final Delete2 request = Requests.delete("Annotation", annotationIds);
            doChange(request);
        }
    }

    /**
     * Test that the size of the file annotations is correctly computed even if the annotations are attached in a cycle.
     * @throws Exception unexpected
     */
    @Test
    public void testCyclicFileAnnotationSize() throws Exception {
        final Random rng = new Random(123456);  // fixed seed for deterministic testing
        final int annotationCount = 3;
        long totalAnnotationSize = 0;
        final List<Long> annotationIds = new ArrayList<Long>(annotationCount);
        for (int i = 0; i < annotationCount; i++) {
            final long size = rng.nextInt(Integer.MAX_VALUE) + 1L;  // positive
            totalAnnotationSize += size;
            annotationIds.add(createFileAnnotation(size));
        }

        addAnnotation(Image.class, imageId, annotationIds.get(0));
        addAnnotation(Annotation.class, annotationIds.get(0), annotationIds.get(1));
        addAnnotation(Annotation.class, annotationIds.get(1), annotationIds.get(2));
        addAnnotation(Annotation.class, annotationIds.get(2), annotationIds.get(0));

        try {
            final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
            Assert.assertEquals(response.bytesUsedByReferer.size(), 1);
            for (final Map<String, Long> byReferer : response.bytesUsedByReferer.values()) {
                Assert.assertEquals(byReferer.get("Annotation"), (Long) totalAnnotationSize);
            }
        } finally {
            final Delete2 request = Requests.delete("Annotation", annotationIds);
            doChange(request);
        }
    }

    /**
     * Test that the file counts are as expected.
     * @throws Exception unexpected
     */
    @Test
    public void testCounts() throws Exception {
        final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
        Assert.assertEquals(response.fileCountByReferer.size(), 1);
        for (final Map<String, Integer> byReferer : response.fileCountByReferer.values()) {
            Assert.assertEquals(byReferer.size(), 3);
            Assert.assertEquals(byReferer.get("FilesetEntry"), Integer.valueOf(1));  // original image file
            Assert.assertEquals(byReferer.get("Job"), Integer.valueOf(1));  // import log
            Assert.assertEquals(byReferer.get("Thumbnail"), Integer.valueOf(1));
        }
    }

    /**
     * Test that the number of file annotations is correctly computed.
     * @throws Exception unexpected
     */
    @Test
    public void testCountsWithFileAnnotations() throws Exception {
        final int annotationCount = 5;
        final List<Long> annotationIds = new ArrayList<Long>(annotationCount);
        for (int i = 0; i < annotationCount; i++) {
            annotationIds.add(createFileAnnotation(1));
        }

        final long channelId = queryForId("SELECT id FROM Channel WHERE pixels.id = :id", pixelsId);
        final long instrumentId = queryForId("SELECT instrument.id FROM Image WHERE id = :id", imageId);
        final long objectiveId = queryForId("SELECT id FROM Objective WHERE instrument.id = :id", instrumentId);

        addAnnotation(Image.class, imageId, annotationIds.get(0));
        addAnnotation(Channel.class, channelId, annotationIds.get(1));
        addAnnotation(Instrument.class, instrumentId, annotationIds.get(2));
        addAnnotation(Objective.class, objectiveId, annotationIds.get(3));
        addAnnotation(Annotation.class, annotationIds.get(3), annotationIds.get(4));

        try {
            final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
            Assert.assertEquals(response.fileCountByReferer.size(), 1);
            for (final Map<String, Integer> byReferer : response.fileCountByReferer.values()) {
                Assert.assertEquals(byReferer.get("Annotation"), (Integer) annotationIds.size());
            }
        } finally {
            final Delete2 request = Requests.delete("Annotation", annotationIds);
            doChange(request);
        }
    }

    /**
     * Test that the number of file annotations is correctly computed even if some are attached to multiple objects.
     * @throws Exception unexpected
     */
    @Test
    public void testCountWithDuplicatedFileAnnotations() throws Exception {
        final int annotationCount = 3;
        final List<Long> annotationIds = new ArrayList<Long>(annotationCount);
        for (int i = 0; i < annotationCount; i++) {
            annotationIds.add(createFileAnnotation(1));
        }

        final long channelId = queryForId("SELECT id FROM Channel WHERE pixels.id = :id", pixelsId);
        final long instrumentId = queryForId("SELECT instrument.id FROM Image WHERE id = :id", imageId);
        final long objectiveId = queryForId("SELECT id FROM Objective WHERE instrument.id = :id", instrumentId);

        addAnnotation(Image.class, imageId, annotationIds.get(0));
        addAnnotation(Channel.class, channelId, annotationIds.get(1));
        addAnnotation(Instrument.class, instrumentId, annotationIds.get(2));
        addAnnotation(Objective.class, objectiveId, annotationIds.get(0));
        addAnnotation(Annotation.class, annotationIds.get(0), annotationIds.get(1));
        addAnnotation(Annotation.class, annotationIds.get(1), annotationIds.get(2));

        try {
            final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
            Assert.assertEquals(response.fileCountByReferer.size(), 1);
            for (final Map<String, Integer> byReferer : response.fileCountByReferer.values()) {
                Assert.assertEquals(byReferer.get("Annotation"), (Integer) annotationIds.size());
            }
        } finally {
            final Delete2 request = Requests.delete("Annotation", annotationIds);
            doChange(request);
        }
    }

    /**
     * Test that the number of file annotations is correctly computed even if some are attached to multiple objects.
     * @throws Exception unexpected
     */
    @Test
    public void testCountWithCyclicFileAnnotations() throws Exception {
        final int annotationCount = 3;
        final List<Long> annotationIds = new ArrayList<Long>(annotationCount);
        for (int i = 0; i < annotationCount; i++) {
            annotationIds.add(createFileAnnotation(1));
        }

        addAnnotation(Image.class, imageId, annotationIds.get(0));
        addAnnotation(Annotation.class, annotationIds.get(0), annotationIds.get(1));
        addAnnotation(Annotation.class, annotationIds.get(1), annotationIds.get(2));
        addAnnotation(Annotation.class, annotationIds.get(2), annotationIds.get(0));

        try {
            final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
            Assert.assertEquals(response.fileCountByReferer.size(), 1);
            for (final Map<String, Integer> byReferer : response.fileCountByReferer.values()) {
                Assert.assertEquals(byReferer.get("Annotation"), (Integer) annotationIds.size());
            }
        } finally {
            final Delete2 request = Requests.delete("Annotation", annotationIds);
            doChange(request);
        }
    }

    /**
     * Test that the total bytes used is the sum of the by-referer breakdown.
     * Applies only when there is no duplication between different referers.
     * @throws Exception unexpected
     */
    @Test
    public void testSizeTotals() throws Exception {
        final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
        final Map<LongPair, Long> expected = new HashMap<LongPair, Long>();
        for (final Map.Entry<LongPair, Map<String, Long>> byReferer : response.bytesUsedByReferer.entrySet()) {
            long total = 0;
            for (final Long size : byReferer.getValue().values()) {
                total += size;
            }
            final Long currentTotal = expected.get(byReferer.getKey());
            if (currentTotal != null) {
                total += currentTotal;
            }
            expected.put(byReferer.getKey(), total);
        }
        assertMapsEqual(response.totalBytesUsed, expected);
    }

    /**
     * Test that the total files used is the sum of the by-referer breakdown.
     * Applies only when there is no duplication between different referers.
     * @throws Exception unexpected
     */
    @Test
    public void testCountTotals() throws Exception {
        final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
        final Map<LongPair, Integer> expected = new HashMap<LongPair, Integer>();
        for (final Map.Entry<LongPair, Map<String, Integer>> byReferer : response.fileCountByReferer.entrySet()) {
            int total = 0;
            for (final Integer size : byReferer.getValue().values()) {
                total += size;
            }
            final Integer currentTotal = expected.get(byReferer.getKey());
            if (currentTotal != null) {
                total += currentTotal;
            }
            expected.put(byReferer.getKey(), total);
        }
        assertMapsEqual(response.totalFileCount, expected);
    }

    /**
     * Test that a bad class name causes an error response.
     * @throws Exception unexpected
     */
    @Test
    public void testBadClassName() throws Exception {
        final DiskUsage request = new DiskUsage();
        request.objects = ImmutableMap.of("NoClass", Collections.singletonList(1L));
        doChange(client, factory, request, false, null);
    }
}

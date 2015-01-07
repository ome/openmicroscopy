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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import omero.RLong;
import omero.RObject;
import omero.RType;
import omero.ServerError;
import omero.api.LongPair;
import omero.cmd.Delete2;
import omero.cmd.DiskUsage;
import omero.cmd.DiskUsageResponse;
import omero.model.Annotation;
import omero.model.Channel;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Instrument;
import omero.model.Objective;
import omero.model.OriginalFile;
import omero.model.Pixels;
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
    private Long pixelsId ;
    private Long fileSize;

    /**
     * Convert a Collection<Long> to a long[].
     */
    private static Function<Collection<Long>, long[]> LONG_COLLECTION_TO_ARRAY =
            new Function<Collection<Long>, long[]>() {
        @Override
        public long[] apply(Collection<Long> ids) {
            final long[] array = new long[ids.size()];
            int index = 0;
            for (final Long id : ids) {
                array[index++] = id;
            }
            return array;
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
        request.objects = Maps.transformValues(objects, LONG_COLLECTION_TO_ARRAY);
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

        Assert.assertNotNull(ec);
        Assert.assertNotNull(imageId);
        Assert.assertNotNull(pixelsId);
        Assert.assertNotNull(fileSize);
    }

    /**
     * Delete the test image.
     * @throws Exception unexpected
     */
    @AfterClass
    public void teardown() throws Exception {
        if (imageId != null) {
            final Delete2 request = new Delete2();
            request.targetObjects = ImmutableMap.of("Image", new long[] {imageId});
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
     * Test that the import log size for the image is correctly computed.
     * @throws Exception unexpected
     */
    @Test
    public void testImportLogSize() throws Exception {
        final String query = "SELECT o.size FROM " +
                "Image i, Fileset f, FilesetJobLink fjl, UploadJob j, JobOriginalFileLink jol, OriginalFile o " +
                "WHERE i.id = :id AND f = i.fileset AND fjl.parent = f AND fjl.child = j AND jol.parent = j AND jol.child = o " +
                "AND o.mimetype = 'application/omero-log-file'";

        final long importLogSize = queryForId(query, imageId);

        final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
        Assert.assertEquals(response.bytesUsedByReferer.size(), 1);
        for (final Map<String, Long> byReferer : response.bytesUsedByReferer.values()) {
            Assert.assertEquals((long) byReferer.get("Job"), importLogSize);
        }
    }

    /**
     * Test that the size of the file annotations is correctly computed.
     * @throws Exception unexpected
     */
    @Test
    public void testFileAnnotationSize() throws Exception {
        final Random rng = new Random(123456);  // fixed seed for deterministic testing
        long totalAnnotationSize = 0;
        final long[] annotationIds = new long[5];
        for (int i = 0; i < annotationIds.length; i++) {
            final long size = rng.nextInt(Integer.MAX_VALUE);  // non-negative
            totalAnnotationSize += size;
            annotationIds[i] = createFileAnnotation(size);
        }

        final long channelId = queryForId("SELECT id FROM Channel WHERE pixels.id = :id", pixelsId);
        final long instrumentId = queryForId("SELECT instrument.id FROM Image WHERE id = :id", imageId);
        final long objectiveId = queryForId("SELECT id FROM Objective WHERE instrument.id = :id", instrumentId);

        addAnnotation(Image.class, imageId, annotationIds[0]);
        addAnnotation(Channel.class, channelId, annotationIds[1]);
        addAnnotation(Instrument.class, instrumentId, annotationIds[2]);
        addAnnotation(Objective.class, objectiveId, annotationIds[3]);
        addAnnotation(Annotation.class, annotationIds[3], annotationIds[4]);

        try {
            final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
            Assert.assertEquals(response.bytesUsedByReferer.size(), 1);
            for (final Map<String, Long> byReferer : response.bytesUsedByReferer.values()) {
                Assert.assertEquals(byReferer.get("Annotation"), (Long) totalAnnotationSize);
            }
        } finally {
            final Delete2 request = new Delete2();
            request.targetObjects = ImmutableMap.of("Annotation", annotationIds);
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
        long totalAnnotationSize = 0;
        final long[] annotationIds = new long[3];
        for (int i = 0; i < annotationIds.length; i++) {
            final long size = rng.nextInt(Integer.MAX_VALUE);  // non-negative
            totalAnnotationSize += size;
            annotationIds[i] = createFileAnnotation(size);
        }

        final long channelId = queryForId("SELECT id FROM Channel WHERE pixels.id = :id", pixelsId);
        final long instrumentId = queryForId("SELECT instrument.id FROM Image WHERE id = :id", imageId);
        final long objectiveId = queryForId("SELECT id FROM Objective WHERE instrument.id = :id", instrumentId);

        addAnnotation(Image.class, imageId, annotationIds[0]);
        addAnnotation(Channel.class, channelId, annotationIds[1]);
        addAnnotation(Instrument.class, instrumentId, annotationIds[2]);
        addAnnotation(Objective.class, objectiveId, annotationIds[0]);
        addAnnotation(Annotation.class, annotationIds[0], annotationIds[1]);
        addAnnotation(Annotation.class, annotationIds[1], annotationIds[2]);

        try {
            final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
            Assert.assertEquals(response.bytesUsedByReferer.size(), 1);
            for (final Map<String, Long> byReferer : response.bytesUsedByReferer.values()) {
                Assert.assertEquals(byReferer.get("Annotation"), (Long) totalAnnotationSize);
            }
        } finally {
            final Delete2 request = new Delete2();
            request.targetObjects = ImmutableMap.of("Annotation", annotationIds);
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
        final long[] annotationIds = new long[5];
        for (int i = 0; i < annotationIds.length; i++) {
            annotationIds[i] = createFileAnnotation(1);
        }

        final long channelId = queryForId("SELECT id FROM Channel WHERE pixels.id = :id", pixelsId);
        final long instrumentId = queryForId("SELECT instrument.id FROM Image WHERE id = :id", imageId);
        final long objectiveId = queryForId("SELECT id FROM Objective WHERE instrument.id = :id", instrumentId);

        addAnnotation(Image.class, imageId, annotationIds[0]);
        addAnnotation(Channel.class, channelId, annotationIds[1]);
        addAnnotation(Instrument.class, instrumentId, annotationIds[2]);
        addAnnotation(Objective.class, objectiveId, annotationIds[3]);
        addAnnotation(Annotation.class, annotationIds[3], annotationIds[4]);

        try {
            final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
            Assert.assertEquals(response.fileCountByReferer.size(), 1);
            for (final Map<String, Integer> byReferer : response.fileCountByReferer.values()) {
                Assert.assertEquals(byReferer.get("Annotation"), (Integer) annotationIds.length);
            }
        } finally {
            final Delete2 request = new Delete2();
            request.targetObjects = ImmutableMap.of("Annotation", annotationIds);
            doChange(request);
        }
    }

    /**
     * Test that the number of file annotations is correctly computed even if some are attached to multiple objects.
     * @throws Exception unexpected
     */
    @Test
    public void testCountWithDuplicatedFileAnnotations() throws Exception {
        final long[] annotationIds = new long[3];
        for (int i = 0; i < annotationIds.length; i++) {
            annotationIds[i] = createFileAnnotation(1);
        }

        final long channelId = queryForId("SELECT id FROM Channel WHERE pixels.id = :id", pixelsId);
        final long instrumentId = queryForId("SELECT instrument.id FROM Image WHERE id = :id", imageId);
        final long objectiveId = queryForId("SELECT id FROM Objective WHERE instrument.id = :id", instrumentId);

        addAnnotation(Image.class, imageId, annotationIds[0]);
        addAnnotation(Channel.class, channelId, annotationIds[1]);
        addAnnotation(Instrument.class, instrumentId, annotationIds[2]);
        addAnnotation(Objective.class, objectiveId, annotationIds[0]);
        addAnnotation(Annotation.class, annotationIds[0], annotationIds[1]);
        addAnnotation(Annotation.class, annotationIds[1], annotationIds[2]);

        try {
            final DiskUsageResponse response = runDiskUsage(ImmutableMap.of("Image", Collections.singleton(imageId)));
            Assert.assertEquals(response.fileCountByReferer.size(), 1);
            for (final Map<String, Integer> byReferer : response.fileCountByReferer.values()) {
                Assert.assertEquals(byReferer.get("Annotation"), (Integer) annotationIds.length);
            }
        } finally {
            final Delete2 request = new Delete2();
            request.targetObjects = ImmutableMap.of("Annotation", annotationIds);
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
}

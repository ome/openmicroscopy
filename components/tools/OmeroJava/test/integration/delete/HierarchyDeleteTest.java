/*
 * $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration.delete;

import static omero.rtypes.rbool;
import static omero.rtypes.rstring;
import integration.AbstractServerTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import ome.parameters.Parameters;
import omero.RLong;
import omero.RType;
import omero.ServerError;
import omero.rtypes;
import omero.api.IAdminPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.ServiceFactoryPrx;
import omero.cmd.Delete2;
import omero.model.Annotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.ImageI;
import omero.model.Instrument;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAcquisitionI;
import omero.model.PlateI;
import omero.model.Project;
import omero.model.ProjectI;
import omero.model.Roi;
import omero.model.Screen;
import omero.model.ScreenI;
import omero.model.Well;
import omero.model.WellI;
import omero.model.WellSample;
import omero.model.WellSampleI;
import omero.sys.ParametersI;

import org.apache.commons.collections.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

/**
 * Tests for deleting hierarchies and the effects that that should have under
 * double- and multiple-linking.
 *
 * @see ticket:3031
 * @see ticket:2994
 * @since 4.2.1
 */
@Test(groups = "ticket:2615")
public class HierarchyDeleteTest extends AbstractServerTest {

    private final static omero.RString t3031 = rstring("#3031");

    /**
     * Test to delete a dataset containing an image also contained in another
     * dataset. The second dataset and the image should not be deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public void testDeletingDataset() throws Exception {

        newUserAndGroup("rwrw--");

        Dataset ds1 = new DatasetI();
        ds1.setName(t3031);

        Dataset ds2 = new DatasetI();
        ds2.setName(t3031);

        Image i1 = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
        i1.unload();

        ds1.linkImage(i1);
        ds1 = (Dataset) iUpdate.saveAndReturnObject(ds1);
        ds2.linkImage(i1);
        ds2 = (Dataset) iUpdate.saveAndReturnObject(ds2);

        // http://trac.openmicroscopy.org.uk/ome/ticket/3031#comment:7
        // This image is only singly linked and should be removed.

        Image i2 = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
        i2.unload();

        ds2.linkImage(i2);
        ds2 = (Dataset) iUpdate.saveAndReturnObject(ds2);

        final Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(ds2.getId().getValue()));
        callback(true, client, dc);

        assertDoesNotExist(ds2);
        assertDoesNotExist(i2);
        assertExists(ds1);
        assertExists(i1);

    }

    /**
     * Test to delete a dataset containing an image also contained in another
     * dataset. The second dataset and the image with an annotation should not
     * be deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public void testDeletingDatasetDoesntRemoveImageAnnotation()
            throws Exception {

        newUserAndGroup("rwrw--");

        Dataset ds1 = new DatasetI();
        ds1.setName(t3031);

        Dataset ds2 = new DatasetI();
        ds2.setName(t3031);

        Image i = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
        i.unload();

        ds1.linkImage(i);
        ds1 = (Dataset) iUpdate.saveAndReturnObject(ds1);
        ds2.linkImage(i);
        ds2 = (Dataset) iUpdate.saveAndReturnObject(ds2);

        Annotation a = (Annotation) iUpdate
                .saveAndReturnObject(new CommentAnnotationI());

        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.setChild((Annotation) a.proxy());
        link.setParent((Image) i.proxy());
        iUpdate.saveAndReturnObject(link);

        final Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(ds2.getId().getValue()));
        callback(true, client, dc);
        assertDoesNotExist(ds2);
        assertExists(ds1);
        assertExists(i);
        assertExists(a);
    }

    /**
     * Test to delete a dataset containing an image also contained in another
     * dataset. The second dataset and the image with ROI should not be deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    public void testDeletingDatasetDoesntRemoveImageRoi() throws Exception {

        newUserAndGroup("rwrw--");

        Dataset ds1 = new DatasetI();
        ds1.setName(t3031);

        Dataset ds2 = new DatasetI();
        ds2.setName(t3031);

        Image i = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImageWithRoi());
        Roi roi = i.copyRois().get(0);
        i.unload();

        ds1.linkImage(i);
        ds1 = (Dataset) iUpdate.saveAndReturnObject(ds1);
        ds2.linkImage(i);
        ds2 = (Dataset) iUpdate.saveAndReturnObject(ds2);

        final Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(ds2.getId().getValue()));
        callback(true, client, dc);

        assertDoesNotExist(ds2);
        assertExists(ds1);
        assertExists(i);
        assertExists(roi);
    }

    /**
     * Test to delete a project containing a dataset also contained in another
     * project. The second project and the dataset should not be deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:3031")
    public void testDeletingProject() throws Exception {

        newUserAndGroup("rwrw--");

        Project p1 = new ProjectI();
        p1.setName(t3031);

        Project p2 = new ProjectI();
        p2.setName(t3031);

        Dataset d = new DatasetI();
        d.setName(t3031);
        d = (Dataset) iUpdate.saveAndReturnObject(d);
        d.unload();

        p1.linkDataset(d);
        p1 = (Project) iUpdate.saveAndReturnObject(p1);
        p2.linkDataset(d);
        p2 = (Project) iUpdate.saveAndReturnObject(p2);

        final Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Project.class.getSimpleName(),
                Collections.singletonList(p2.getId().getValue()));
        callback(true, client, dc);

        assertDoesNotExist(p2);
        assertExists(p1);
        assertExists(d);
    }

    /**
     * Test to delete a screen containing a plate also contained in another
     * screen. The second screen and the plate should not be deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:3031")
    public void testDeletingScreen() throws Exception {

        newUserAndGroup("rwrw--");

        Screen s1 = new ScreenI();
        s1.setName(t3031);

        Screen s2 = new ScreenI();
        s2.setName(t3031);

        Plate p = mmFactory.createPlate(1, 1, 1, 1, false);
        p = (Plate) iUpdate.saveAndReturnObject(p);
        p.unload();

        s1.linkPlate(p);
        s1 = (Screen) iUpdate.saveAndReturnObject(s1);

        s2.linkPlate(p);
        s2 = (Screen) iUpdate.saveAndReturnObject(s2);

        final Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Screen.class.getSimpleName(),
                Collections.singletonList(s2.getId().getValue()));
        callback(true, client, dc);

        assertDoesNotExist(s2);
        assertExists(s1);
        assertExists(p);
    }

    /**
     * Test that the correct wells, samples and images are deleted when runs of a plate are deleted.
     * @throws Exception unexpected
     */
    @Test(groups = "ticket:10564")
    public void testDeletingMultipleRunsWithOrphanedSamples() throws Exception {

        /* Create a new group. */

        final IAdminPrx rootAdminSvc = root.getSession().getAdminService();

        final String normalGroupName = UUID.randomUUID().toString();
        ExperimenterGroup normalGroup = new ExperimenterGroupI();
        normalGroup.setName(rstring(normalGroupName));
        normalGroup.setLdap(rbool(false));
        normalGroup = rootAdminSvc.getGroup(rootAdminSvc.createGroup(normalGroup));

        /* Create a new user in that group. */

        final String userName = UUID.randomUUID().toString();
        final Experimenter experimenter = createExperimenterI(userName, "a", "user");
        long eid = newUserInGroupWithPassword(experimenter, normalGroup, userName);
        rootAdminSvc.setDefaultGroup(rootAdminSvc.getExperimenter(eid), normalGroup);

        /* Create a session for the new user and obtain its query and update services. */

        final omero.client client = newOmeroClient();
        final ServiceFactoryPrx services = client.createSession(userName, userName);
        final IQueryPrx  iQuery  = services.getQueryService();
        final IUpdatePrx iUpdate = services.getUpdateService();

        /* Set up the size of the test.
         * The number of wells depends upon the number of runs because each well has a non-empty
         * set of well samples, each of which may be tied to some run or to no run. Many well samples
         * are created, to tie wells to every combination of runs and just to the plate. */

        final int numberOfRuns = 3;
        final int numberOfWells = (2 << numberOfRuns) - 1;

        /* Create the plate. */

        final Plate plate = new PlateI();
        plate.setName(rtypes.rstring(UUID.randomUUID().toString()));
        plate.setRows(rtypes.rint(1));
        plate.setColumns(rtypes.rint(numberOfWells));

        /* Create the wells of the plate. */

        final List<Well> wells = new ArrayList<Well>(numberOfWells);
        for (int columnNo = 1; columnNo <= numberOfWells; columnNo++) {
            final Well well = new WellI();
            well.setRow(rtypes.rint(1));
            well.setColumn(rtypes.rint(columnNo));
            plate.addWell(well);
            wells.add(well);
        }

        /* Create the runs of the plate. */

        final List<PlateAcquisition> runs = new ArrayList<PlateAcquisition>(numberOfRuns);
        for (int runNo = 1; runNo <= numberOfRuns; runNo++) {
            final PlateAcquisition run = new PlateAcquisitionI();
            run.setName(rtypes.rstring(UUID.randomUUID().toString()));
            plate.addPlateAcquisition(run);
            runs.add(run);
        }

        /* Create well samples to make each well a different combination
         * of being tied to runs and to only the plate. */

        /* Note that this runNo is 0-indexed instead of 1-indexed:
         * the last number represents just the plate rather than a specific run. */

        for (int wellIndex = 0; wellIndex < numberOfWells; wellIndex++) {
            for (int runNo = 0; runNo <= numberOfRuns; runNo++) {
                if ((wellIndex & (1 << runNo)) == 0) {
                    final WellSample sample = new WellSampleI();
                    sample.setImage(this.mmFactory.createImage());
                    wells.get(wellIndex).addWellSample(sample);
                    if (runNo < numberOfRuns) {
                        runs.get(runNo).addWellSample(sample);
                    }
                }
            }
        }

        /* Persist the assembled plate. */

        final long plateId = iUpdate.saveAndReturnObject(plate).getId().getValue();

        /* Create indices of the resulting entity IDs for the plate and how they are linked. */

        final Set<Long> runIds = new HashSet<Long>();
        final Multimap<Long, Long> runsToSamples = HashMultimap.create();
        final Multimap<Long, Long> wellsToRuns   = HashMultimap.create();
        final Map<Long, Long> samplesToWells  = new HashMap<Long, Long>();
        final Map<Long, Long> samplesToImages = new HashMap<Long, Long>();

        /* Index runs, wells and samples. */

        for (final List<RType> mapping : iQuery.projection(
                "SELECT sample.plateAcquisition.id, well.id, sample.id FROM Well well, WellSample sample " +
                "WHERE well.id = sample.well.id AND well.plate.id = :" + Parameters.ID,
                new ParametersI().addId(plateId))) {
            final Long runId = mapping.get(0) == null ? null : ((RLong) mapping.get(0)).getValue();
            final long wellId = ((RLong) mapping.get(1)).getValue();
            final long sampleId = ((RLong) mapping.get(2)).getValue();
            if (runId != null) {
                runIds.add(runId);
            }
            runsToSamples.put(runId, sampleId);
            wellsToRuns.put(wellId, runId);
            samplesToWells.put(sampleId, wellId);
        }

        /* Index samples and images. */

        for (final List<RType> mapping : iQuery.projection(
                "SELECT sample.id, sample.image.id FROM WellSample sample " +
                "WHERE sample.id IN (:" + Parameters.IDS + ")",
                new ParametersI().addIds(samplesToWells.keySet()))) {
            final long sampleId = ((RLong) mapping.get(0)).getValue();
            final long imageId = ((RLong) mapping.get(1)).getValue();
            samplesToImages.put(sampleId, imageId);
        }

        /* Ensure that the wells are indeed tied to every combination of the expected number of runs. */

        final Set<Collection<Long>> runCombinations = new HashSet<Collection<Long>>(numberOfWells);
        for (final Collection<Long> runCombination : wellsToRuns.asMap().values()) {
            Assert.assertTrue(runCombinations.add(runCombination),
                    "expecting each sample to be in a unique combination of runs");
        }
        Assert.assertEquals(runCombinations.size(), numberOfWells,
                "expecting each run combination to be represented in a well");
        Assert.assertEquals(runIds.size(), numberOfRuns,
                "expecting the plate to have the correct number of runs");

        /* Before deleting, note the IDs of all the entities that once existed,
         * so that their deletion (rather than just unlinking) may be verified. */

        final ImmutableSet<Long> allRuns    = ImmutableSet.copyOf(runIds);
        final ImmutableSet<Long> allWells   = ImmutableSet.copyOf(samplesToWells.values());
        final ImmutableSet<Long> allSamples = ImmutableSet.copyOf(samplesToWells.keySet());
        final ImmutableSet<Long> allImages  = ImmutableSet.copyOf(samplesToImages.values());

        /* Delete all the plate's runs one by one, ensuring that entities are deleted accordingly. */

        while (true) {
            /* Choose the next run to delete. */

            final Iterator<Long> runIdsIterator = runIds.iterator();
            if (!runIdsIterator.hasNext()) {
                break;
            }
            final long runIdToDelete = runIdsIterator.next();
            runIdsIterator.remove();

            /* Follow links to determine which entities are expected to remain afterward. */

            final Set<Long> expectedRuns    = new HashSet<Long>();
            final Set<Long> expectedWells   = allWells;
            final Set<Long> expectedSamples = new HashSet<Long>();
            final Set<Long> expectedImages  = new HashSet<Long>();

            expectedRuns.addAll(runIds);
            expectedSamples.addAll(runsToSamples.get(null));  /* well samples not in runs */
            for (final Long runId : expectedRuns) {
                expectedSamples.addAll(runsToSamples.get(runId));
            }
            for (final Long sampleId : expectedSamples) {
                /* expectedWells.add(samplesToWells.get(sampleId));
                 * if deleting a well's samples ought to delete the well
                 */
                expectedImages.add(samplesToImages.get(sampleId));
            }

            /* Delete the run. */

            final Delete2 dc = new Delete2();
            dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                    PlateAcquisition.class.getSimpleName(),
                    Collections.singletonList(runIdToDelete));
            callback(true, client, dc);
            
            /* Verify that exactly the expected entities remain. */

            checkIds(iQuery, "PlateAcquisition", allRuns, expectedRuns);
            checkIds(iQuery, "Well", allWells, expectedWells);
            checkIds(iQuery, "WellSample", allSamples, expectedSamples);
            checkIds(iQuery, "Image", allImages, expectedImages);
       }
 
        /* Delete the plate. */
        final Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Plate.class.getSimpleName(),
                Collections.singletonList(plateId));
        callback(true, client, dc);

        /* Verify that no entities remain. */

        checkIds(iQuery, "PlateAcquisition", allRuns, Collections.<Long>emptySet());
        checkIds(iQuery, "Well", allWells, Collections.<Long>emptySet());
        checkIds(iQuery, "WellSample", allSamples, Collections.<Long>emptySet());
        checkIds(iQuery, "Image", allImages, Collections.<Long>emptySet());

        /* Close the new user's session. */

        client.closeSession();
    }

    /**
     * Verify that the persisted entities are exactly as expected from among a superset.
     * @param iQuery the query service to use
     * @param entityClass the name of the entity class to query
     * @param allIds the IDs of the entities that may exist
     * @param expectedIds the IDs of the entities that should exist, a subset of <code>allIds</code>
     * @throws ServerError if the query fails
     */
    private void checkIds(IQueryPrx iQuery, String entityClass, Collection<Long> allIds, Collection<Long> expectedIds)
            throws ServerError {
        final Set<Long> actualIds = new HashSet<Long>(expectedIds.size());
        for (final List<RType> wrappedId : iQuery.projection(
                "SELECT id FROM " + entityClass + " WHERE id IN (:" + Parameters.IDS + ")",
                new ParametersI().addIds(allIds))) {
            actualIds.add(((RLong) wrappedId.get(0)).getValue());
        }
        Assert.assertTrue(CollectionUtils.isEqualCollection(actualIds, expectedIds),
                "persisted entities not as expected: " + entityClass);
    }

    /**
     * Test to delete a dataset containing an image whose projection is not in the dataset.
     * The image should be deleted, but not its projection.
     * @throws Exception unexpected
     */
    @Test(groups = {"ticket:12856"})
    public void testDeletingDatasetWithProjectedImage() throws Exception {
        newUserAndGroup("rw----");

        /* create a dataset */
        Dataset dataset = new DatasetI();
        dataset.setName(omero.rtypes.rstring("ticket #12856"));
        dataset = (Dataset) iUpdate.saveAndReturnObject(dataset).proxy();

        /* create an instrument */
        Instrument instrument = mmFactory.createInstrument();
        instrument = (Instrument) iUpdate.saveAndReturnObject(instrument).proxy();

        /* the original and its projection are related and share an instrument */
        Image original = mmFactory.createImage();
        original.setInstrument(instrument);
        original = (Image) iUpdate.saveAndReturnObject(original);
        Image projection = mmFactory.createImage();
        projection.setInstrument(instrument);
        projection.getPrimaryPixels().setRelatedTo((Pixels) original.getPrimaryPixels().proxy());
        projection = (Image) iUpdate.saveAndReturnObject(projection);

        original = (Image) original.proxy();
        projection = (Image) projection.proxy();

        /* only the original is in the dataset */
        final DatasetImageLink link = new DatasetImageLinkI();
        link.setParent(dataset);
        link.setChild(original);
        iUpdate.saveAndReturnObject(link);

        /* delete the dataset */
        final Delete2 delete = new Delete2();
        delete.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(dataset.getId().getValue()));
        callback(true, client, delete);

        /* check what is left afterward */
        assertDoesNotExist(dataset);
        assertDoesNotExist(original);
        assertExists(projection);
    }

    /* for dataset to plate test cases */
    private enum Target { DATASET, IMAGES, PLATE };

    /**
     * Test deletion on a dataset, plate, or images, where the plate's images are in the dataset.
     * @param target the target of the delete operation
     * @throws Exception unexpected
     */
    @Test(dataProvider = "dataset to plate test cases")
    public void testDeletingDatasetToPlate(Target target) throws Exception {
        newUserAndGroup("rw----");

        /* create a plate */

        Plate plate = mmFactory.createPlate(2, 2, 1, 1, false);
        plate = (Plate) iUpdate.saveAndReturnObject(plate).proxy();
        final long plateId = plate.getId().getValue();

        /* find the plate's images */

        final List<Long> imageIds = new ArrayList<Long>();
        final String hql = "SELECT image.id FROM WellSample where well.id IN (SELECT id FROM Well WHERE plate.id = :id)";
        final omero.sys.Parameters params = new ParametersI().addId(plateId);
        for (final List<RType> result : iQuery.projection(hql, params)) {
            final Long imageId = ((RLong) result.get(0)).getValue();
            imageIds.add(imageId);
        }

        /* create the dataset and link the images to it */

        final Dataset dataset = (Dataset) iUpdate.saveAndReturnObject(mmFactory.simpleDataset()).proxy();
        final long datasetId = dataset.getId().getValue();

        final List<Long> linkIds = new ArrayList<Long>(imageIds.size());
        for (final long imageId : imageIds) {
            DatasetImageLink link = new DatasetImageLinkI();
            link.setParent(dataset);
            link.setChild(new ImageI(imageId, false));
            linkIds.add(iUpdate.saveAndReturnObject(link).getId().getValue());
        }

        /* perform the deletion */

        final boolean isExpectSuccess = target != Target.IMAGES;

        final Delete2 delete = new Delete2();

        switch (target) {
        case DATASET:
            delete.targetObjects = ImmutableMap.of("Dataset", Collections.singletonList(datasetId));
            break;
        case IMAGES:
            delete.targetObjects = ImmutableMap.of("Image", imageIds);
            break;
        case PLATE:
            delete.targetObjects = ImmutableMap.of("Plate", Collections.singletonList(plateId));
            break;
        }

        doChange(client, factory, delete, isExpectSuccess);

        if (isExpectSuccess) {

            /* check that exactly the expected objects were deleted */

            switch (target) {
            case DATASET:
                assertDoesNotExist("Dataset", datasetId);
                assertAllExist("Image", imageIds);
                assertNoneExist("DatasetImageLink", linkIds);
                assertExists("Plate", plateId);
                break;
            case IMAGES:
                Assert.fail("cannot delete images while used by well samples");
            case PLATE:
                assertExists("Dataset", datasetId);
                assertAllExist("Image", imageIds);
                assertAllExist("DatasetImageLink", linkIds);
                assertDoesNotExist("Plate", plateId);
                break;
            }
        }

        /* delete the remaining "containers" */

        switch (target) {
        case DATASET:
            delete.targetObjects = ImmutableMap.of("Plate", Collections.singletonList(plateId));
            break;
        case IMAGES:
            delete.targetObjects = ImmutableMap.of(
                    "Dataset", Collections.singletonList(datasetId),
                    "Plate", Collections.singletonList(plateId));
            break;
        case PLATE:
            delete.targetObjects = ImmutableMap.of("Dataset", Collections.singletonList(datasetId));
            break;
        }

        doChange(client, factory, delete, true);

        /* check that all the objects are now deleted */

        assertDoesNotExist("Dataset", datasetId);
        assertNoneExist("Image", imageIds);
        assertNoneExist("DatasetImageLink", linkIds);
        assertDoesNotExist("Plate", plateId);
    }

    /**
     * @return a specific test case for dataset to plate
     */
    @DataProvider(name = "dataset to plate test cases (debug)")
    public Object[][] provideDatasetToPlateCaseDebug() {
        int index = 0;
        final int TARGET = index++;

        final List<Object[]> testCases = new ArrayList<Object[]>();

        final Object[] testCase = new Object[index];
        testCase[TARGET] = Target.IMAGES;
        testCases.add(testCase);

        return testCases.toArray(new Object[testCases.size()][]);
    }

    /**
     * @return a variety of test cases for dataset to plate
     */
    @DataProvider(name = "dataset to plate test cases")
    public Object[][] provideDatasetToPlateCases() {
        int index = 0;
        final int TARGET = index++;

        final List<Object[]> testCases = new ArrayList<Object[]>();

        for (final Target target : Target.values()) {
            final Object[] testCase = new Object[index];
            testCase[TARGET] = target;
            testCases.add(testCase);
        }

        return testCases.toArray(new Object[testCases.size()][]);
    }
}

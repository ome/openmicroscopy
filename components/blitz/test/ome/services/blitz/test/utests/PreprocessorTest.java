/*
 * Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
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

package ome.services.blitz.test.utests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import Ice.Object;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;

import omero.cmd.Chgrp;
import omero.cmd.Delete;
import omero.cmd.Request;
import omero.cmd.graphs.ChgrpI;
import omero.cmd.graphs.DeleteI;
import ome.services.query.HierarchyNavigatorWrap;
import omero.cmd.graphs.Preprocessor;

/**
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
@Test(groups = {"fs"})
@SuppressWarnings("deprecation")
public class PreprocessorTest extends Preprocessor {

    private final Ice.Communicator ic = Ice.Util.initialize();

    private final class Factory implements Ice.ObjectFactory {
        @Override
        public Object create(String type) {
            if (ChgrpI.ice_staticId().equals(type)) {
                return new ChgrpI(ic, null, null);
            } else if (DeleteI.ice_staticId().equals(type)) {
                return new DeleteI(ic, null);
            } else {
                return null;
            }
        }

        @Override
        public void destroy() {
            // no-op
        }
        
    }

    /* Rather than query the database and cache the results, this subclass queries a mock model object hierarchy. */
    static final HierarchyNavigatorWrap<TargetType, GraphModifyTarget> hierarchyNavigatorMock =
            new HierarchyNavigatorWrap<TargetType, GraphModifyTarget>(null) {
        @Override
        protected String typeToString(TargetType type) {
            throw new RuntimeException();
        }

        @Override
        protected TargetType stringToType(String typeName) {
            throw new RuntimeException();
        }

        @Override
        protected Entry<String, Long> entityToStringLong(GraphModifyTarget entity) {
            throw new RuntimeException();
        }

        @Override
        protected GraphModifyTarget stringLongToEntity(String typeName, long id) {
            throw new RuntimeException();
        }

        @Override
        public void prepareLookups(TargetType toType, Collection<GraphModifyTarget> from) { }

        @Override
        public ImmutableSet<GraphModifyTarget> doLookup(TargetType toType, GraphModifyTarget from) {
            final Entry<TargetType, GraphModifyTarget> query = Maps.immutableEntry(toType, from);
            final ImmutableSet.Builder<GraphModifyTarget> builder = ImmutableSet.builder();
            if (containers.containsKey(query)) {
                for (final Long containerId : containers.get(query)) {
                    builder.add(new GraphModifyTarget(toType, containerId));
                }
            }
            if (containeds.containsKey(query)) {
                for (final Long containedId : containeds.get(query)) {
                    builder.add(new GraphModifyTarget(toType, containedId));
                }
            }
            return builder.build();
        }
    };

    public PreprocessorTest() {
        super(new ArrayList<Request>(), hierarchyNavigatorMock);
        ic.addObjectFactory(new Factory(), ChgrpI.ice_staticId());
        ic.addObjectFactory(new Factory(), DeleteI.ice_staticId());
    }

    /**
     * For the case where there are no choices, this method wraps each passed
     * parameter in a new String[] for use with {@link #assertRequests(String[]...)}
     */
    private void assertRequests(String...requests) {
        String[][] choices = new String[requests.length][];
        for (int i = 0; i < choices.length; i++) {
            choices[i] = new String[]{ requests[i] };
        }
        assertRequests(choices);
    }

    /**
     * Wrap a string in a string array.
     */
    private String[] _(String s) {
        return new String[]{s};
    }

    /**
     * Choices per location are passed as an array and checked against the
     * current contents. For example, if the first element may be /Fileset:1
     * or /Fileset:2, but the second must be /Plate:1, then this method should
     * be called with "new String[]{"/Fileset:1", "/Fileset:2"}, new String[] {
     * "/Plate:1"}.
     */
    private void assertRequests(String[]...choices) {

        Assert.assertEquals(requests.size(), choices.length);

        for (int idx = 0; idx < choices.length; idx++) {
            final Multiset<String> expected = HashMultiset.create(Arrays.asList(choices[idx]));
            Request request = requests.get(idx);
            String requestString = requestToString(request);
            Assert.assertTrue(expected.remove(requestString),
                String.format("index %s: %s not in %s", idx, requestString,
                        expected));
        }
    }

    /**
     * Test that the target type hierarchy is indeed ordered from higher to lower.
     */
    @Test
    public void testTargetTypeHierarchyOrdering() {
        final Set<TargetType> prohibitedValues = new HashSet<TargetType>();
        for (final Entry<TargetType, TargetType> relationship : targetTypeHierarchy) {
            prohibitedValues.add(relationship.getKey());
            Assert.assertFalse(prohibitedValues.contains(relationship.getValue()));
        }
    }

    /* named for their values */
    private static final SetMultimap<Entry<TargetType, GraphModifyTarget>, Long> containers;
    private static final SetMultimap<Entry<TargetType, GraphModifyTarget>, Long> containeds;

    static {
        /* set up the hierarchy for the test cases, from containers to contained ... */

        containeds = HashMultimap.create();
        containeds.putAll(Maps.immutableEntry(TargetType.IMAGE, new GraphModifyTarget(TargetType.FILESET, 0)), ImmutableList.of(0L));
        containeds.putAll(Maps.immutableEntry(TargetType.IMAGE, new GraphModifyTarget(TargetType.FILESET, 1)), ImmutableList.of(1L, 2L));
        containeds.putAll(Maps.immutableEntry(TargetType.IMAGE, new GraphModifyTarget(TargetType.FILESET, 2)), ImmutableList.of(3L, 4L, 5L));

        containeds.putAll(Maps.immutableEntry(TargetType.IMAGE, new GraphModifyTarget(TargetType.DATASET, 0)), ImmutableList.of(0L, 1L));
        containeds.putAll(Maps.immutableEntry(TargetType.IMAGE, new GraphModifyTarget(TargetType.DATASET, 1)), ImmutableList.of(2L, 3L));

        containeds.putAll(Maps.immutableEntry(TargetType.DATASET, new GraphModifyTarget(TargetType.PROJECT, 0)), ImmutableList.of(0L, 1L));

        for (long id = 0; id < 7; id++) {
            containeds.putAll(Maps.immutableEntry(TargetType.IMAGE, new GraphModifyTarget(TargetType.WELL, id)), ImmutableList.of(id));
        }

        // Wells will always be scattered over plates.
        containeds.putAll(Maps.immutableEntry(TargetType.WELL, new GraphModifyTarget(TargetType.PLATE, 0)), ImmutableList.of(0L, 1L));
        containeds.putAll(Maps.immutableEntry(TargetType.WELL, new GraphModifyTarget(TargetType.PLATE, 1)), ImmutableList.of(2L, 3L));
        containeds.putAll(Maps.immutableEntry(TargetType.WELL, new GraphModifyTarget(TargetType.PLATE, 2)), ImmutableList.of(4L, 5L, 6L));

        containeds.putAll(Maps.immutableEntry(TargetType.PLATE, new GraphModifyTarget(TargetType.SCREEN, 0)), ImmutableList.of(0L, 1L, 2L));

        /* ... then invert for contained to containers */

        containers = HashMultimap.create();
        for (final Entry<Entry<TargetType, GraphModifyTarget>, Long> containing : containeds.entries()) {
            final TargetType containedType = containing.getKey().getKey();
            final GraphModifyTarget container = containing.getKey().getValue();
            final Long containedId = containing.getValue();
            final GraphModifyTarget contained = new GraphModifyTarget(containedType, containedId);
            containers.put(Maps.immutableEntry(container.targetType, contained), container.targetId);
        }
        
    }

    /**
     * Clear the lookup caches and the list of requests ready for a new unit test.
     */
    @BeforeMethod
    public void clearCacheAndRequests() {
        requests.clear();
    }

    /**
     * Add a delete request to the list of requests.
     * @param type the target type
     * @param id the target ID
     */
    private void addDeleteRequest(String type, long id) {
        final Delete delete = new DeleteI(ic, null);
        delete.type = type;
        delete.id = id;
        this.requests.add(delete);
    }

    /**
     * Add a chgrp request to the list of requests.
     * @param type the target type
     * @param id the target ID
     * @param group the destination group
     */
    private void addChgrpRequest(String type, long id, long group) {
        final Chgrp chgrp = new ChgrpI(ic, null, null);
        chgrp.type = type;
        chgrp.id = id;
        chgrp.grp = group;
        this.requests.add(chgrp);
    }

    /**
     * Test conversion of nothing to nothing.
     */
    @Test
    public void testNothingToNothing() {
        process();

        assertRequests(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    /**
     * Test conversion of image requests to fileset requests.
     */
    @Test
    public void testImagesToFilesets() {
        for (long imageId = 1; imageId < 6; imageId++) {
            addDeleteRequest("/Image", imageId);
        }

        process();

        assertRequests(
                "DELETE[/Fileset:1]",
                "DELETE[/Fileset:2]"
                );
    }

    /**
     * Test conversion of image requests to image and fileset requests.
     */
    @Test
    public void testImagesToImagesFilesets() {
        for (long imageId = 0; imageId < 7; imageId++) {
            addDeleteRequest("/Image", imageId);
        }

        process();

        assertRequests(
                "DELETE[/Fileset:0]",
                "DELETE[/Fileset:1]",
                "DELETE[/Fileset:2]",
                "DELETE[/Image:6]"
                );
    }

    /**
     * Test conversion of fileset requests to fileset requests.
     */
    @Test
    public void testFilesetsToFilesets() {
        addDeleteRequest("/Fileset", 1);
        addDeleteRequest("/Fileset", 2);

        process();

        assertRequests(
                "DELETE[/Fileset:1]",
                "DELETE[/Fileset:2]"
                );
    }

    /**
     * Test conversion of image and fileset requests to fileset requests.
     */
    @Test
    public void testImagesFilesetsToFilesets() {
        for (long imageId = 1; imageId < 6; imageId++) {
            addDeleteRequest("/Image", imageId);
        }
        addDeleteRequest("/Fileset", 1);
        addDeleteRequest("/Fileset", 2);

        process();

        assertRequests(
                "DELETE[/Fileset:1]",
                "DELETE[/Fileset:2]"
                );
    }

    /**
     * Test conversion of fileset and image requests to fileset requests.
     */
    @Test
    public void testFilesetsImagesToFilesets() {
        addDeleteRequest("/Fileset", 1);
        addDeleteRequest("/Fileset", 2);
        for (long imageId = 1; imageId < 6; imageId++) {
            addDeleteRequest("/Image", imageId);
        }

        process();

        assertRequests(
                "DELETE[/Fileset:1]",
                "DELETE[/Fileset:2]"
                );
    }

    /**
     * Test conversion of fileset and image requests to fileset and image requests.
     */
    @Test
    public void testFilesetsImagesToFilesetsImages() {
        addChgrpRequest("/Fileset", 1, 8);
        addChgrpRequest("/Fileset", 2, 8);
        for (long imageId = 0; imageId < 7; imageId++) {
            addChgrpRequest("/Image", imageId, 8);
        }

        process();

        assertRequests(
                "CHGRP(8)[/Fileset:0]",
                "CHGRP(8)[/Fileset:1]",
                "CHGRP(8)[/Fileset:2]",
                "CHGRP(8)[/Image:6]"
                );
    }

    /**
     * Test conversion of image and fileset requests to image and fileset requests.
     */
    @Test
    public void testImagesFilesetsToImagesFilesets() {
        for (long imageId = 0; imageId < 7; imageId++) {
            addChgrpRequest("/Image", imageId, 8);
        }
        addChgrpRequest("/Fileset", 1, 8);
        addChgrpRequest("/Fileset", 2, 8);

        process();

        assertRequests(
                "CHGRP(8)[/Fileset:0]",
                "CHGRP(8)[/Image:6]",
                "CHGRP(8)[/Fileset:1]",
                "CHGRP(8)[/Fileset:2]"
                );
    }

    /**
     * Test conversion of image requests to image requests.
     */
    @Test
    public void testImagesToImagesChgrp() {
        addChgrpRequest("/Image", 1, 1);
        addChgrpRequest("/Image", 2, 2);

        process();

        assertRequests(
                "CHGRP(1)[/Image:1]",
                "CHGRP(2)[/Image:2]"
                );
    }

    /**
     * Test conversion of image requests to fileset requests.
     */
    @Test
    public void testImagesToFilesetsChgrp() {
        addChgrpRequest("/Image", 1, 1);
        addChgrpRequest("/Image", 2, 1);

        process();

        assertRequests(
                "CHGRP(1)[/Fileset:1]"
                );
    }

    /**
     * Test conversion of image requests to fileset requests in different groups.
     */
    @Test
    public void testImagesToFilesetsChgrpDifferent() {
        addChgrpRequest("/Image", 3, 8);
        addChgrpRequest("/Image", 1, 9);
        addChgrpRequest("/Image", 4, 8);
        addChgrpRequest("/Image", 2, 9);
        addChgrpRequest("/Image", 5, 8);

        process();

        assertRequests(
                "CHGRP(9)[/Fileset:1]",
                "CHGRP(8)[/Fileset:2]"
                );
    }

    /**
     * Test conversion of dataset requests to dataset requests.
     *
     * If the dataset contains all of the images contained in any
     * fileset, that fileset will be prepended to the list. 
     */
    @Test
    public void testDatasetsToDatasets() {
        addDeleteRequest("/Dataset", 0);

        process();

        assertRequests(
                "DELETE[/Dataset:0]",
                "DELETE[/Fileset:0]"
                );
    }

    /**
     * Test conversion of dataset requests to fileset and dataset requests.
     */
    @Test
    public void testDatasetsToFilesetsDatasets() {
        addDeleteRequest("/Dataset", 0);
        addDeleteRequest("/Dataset", 1);

        process();

        assertRequests(
                "DELETE[/Dataset:0]",
                "DELETE[/Fileset:0]",
                "DELETE[/Dataset:1]",
                "DELETE[/Fileset:1]"
                );
    }

    /**
     * Test conversion of dataset and image requests to fileset, dataset and image requests.
     */
    @Test
    public void testDatasetsImagesToFilesetsDatasetsImages() {
        addDeleteRequest("/Dataset", 0);
        addDeleteRequest("/Dataset", 1);
        addDeleteRequest("/Image", 4);
        addDeleteRequest("/Image", 5);
        addDeleteRequest("/Image", 6);

        process();

        assertRequests(
                "DELETE[/Dataset:0]",
                "DELETE[/Fileset:0]",
                "DELETE[/Dataset:1]",
                "DELETE[/Fileset:1]",
                "DELETE[/Fileset:2]",
                "DELETE[/Image:6]"
                );
    }

    /**
     * Test conversion of project requests to fileset and project requests.
     */
    @Test
    public void testProjectsToFilesetsProjects() {
        addDeleteRequest("/Project", 0);

        process();

        assertRequests(
                "DELETE[/Project:0]",
                "DELETE[/Fileset:0]",
                "DELETE[/Fileset:1]"
                );
    }

    /**
     * Test conversion of project and image requests to fileset, project and image requests.
     */
    @Test
    public void testProjectsImagesToFilesetsProjectsImages() {
        addDeleteRequest("/Project", 0);
        addDeleteRequest("/Image", 4);
        addDeleteRequest("/Image", 5);
        addDeleteRequest("/Image", 6);

        process();

        assertRequests(
                _("DELETE[/Project:0]"),
                _("DELETE[/Fileset:0]"),
                _("DELETE[/Fileset:1]"),
                _("DELETE[/Fileset:2]"),
                _("DELETE[/Image:6]")
                );
    }

    /**
     * Test conversion of image, dataset and project requests to image, fileset, dataset and project requests.
     */
    @Test
    public void testImagesDatasetsProjectsToImagesFilesetsDatasetsProjects() {
        for (long imageId = 0; imageId < 7; imageId++) {
            addDeleteRequest("/Image", imageId);
        }
        addDeleteRequest("/Dataset", 0);
        addDeleteRequest("/Dataset", 1);
        addDeleteRequest("/Project", 0);

        process();

        assertRequests(
                "DELETE[/Image:6]",
                "DELETE[/Dataset:0]",
                "DELETE[/Dataset:1]",
                "DELETE[/Project:0]",
                "DELETE[/Fileset:0]",
                "DELETE[/Fileset:1]",
                "DELETE[/Fileset:2]"
                );
    }

    /**
     * Test conversion of well requests to well requests.
     *
     * Here, the 2 Wells cover the entire Fileset and therefore it is injected
     * at the beginning of the test. This is unlikely to do what one intends,
     * though, because there will be other linkages from /Plate
     */
    @Test
    public void testWellsToWellsCoverage() {
        addChgrpRequest("/Well", 0, 8);
        addChgrpRequest("/Well", 1, 8);

        process();

        assertRequests(
                "CHGRP(8)[/Well:0]",
                "CHGRP(8)[/Fileset:0]",
                "CHGRP(8)[/Well:1]"
                );
    }

    /**
     * Test conversion of well requests to well requests.
     *
     * Here, the 2 Wells do not cover the entire Fileset and therefore no
     * processing is performed.
     */
    @Test
    public void testWellsToWellsNoCoverage() {
        addChgrpRequest("/Well", 4, 8);
        addChgrpRequest("/Well", 5, 8);

        process();

        assertRequests(
                "CHGRP(8)[/Well:4]",
                "CHGRP(8)[/Well:5]"
                );
    }

    /**
     * Test conversion of well requests to fileset and well requests.
     */
    @Test
    public void testWellsToFilesetsWells() {
        addChgrpRequest("/Well", 1, 8);
        addChgrpRequest("/Well", 2, 8);

        process();

        assertRequests(
                "CHGRP(8)[/Well:1]",
                "CHGRP(8)[/Well:2]",
                "CHGRP(8)[/Fileset:1]"
                );
    }

    /**
     * Test conversion of well requests to well and fileset requests.
     */
    @Test
    public void testWellsImagesToWellsFilesets() {
        addChgrpRequest("/Well", 0, 8);
        addChgrpRequest("/Well", 1, 8);
        addChgrpRequest("/Well", 2, 8);

        process();

        assertRequests(
                "CHGRP(8)[/Well:0]",
                "CHGRP(8)[/Fileset:0]",
                "CHGRP(8)[/Well:1]",
                "CHGRP(8)[/Well:2]",
                "CHGRP(8)[/Fileset:1]"
                );
    }

    /**
     * Test conversion of image and well requests to fileset and well requests.
     */
    @Test
    public void testImagesWellsToFilesetsWells() {
        addChgrpRequest("/Image", 1, 8);
        addChgrpRequest("/Image", 2, 8);
        addChgrpRequest("/Well", 1, 8);
        addChgrpRequest("/Well", 2, 8);

        process();

        assertRequests(
                "CHGRP(8)[/Well:1]",
                "CHGRP(8)[/Well:2]",
                "CHGRP(8)[/Fileset:1]"
                );
    }

    /**
     * Test conversion of well and image requests to fileset and well requests.
     */
    @Test
    public void testWellsImagesToFilesetsWells() {
        addChgrpRequest("/Well", 1, 8);
        addChgrpRequest("/Well", 2, 8);
        addChgrpRequest("/Image", 1, 8);
        addChgrpRequest("/Image", 2, 8);

        process();

        assertRequests(
                "CHGRP(8)[/Well:1]",
                "CHGRP(8)[/Well:2]",
                "CHGRP(8)[/Fileset:1]"
                );
    }

    /**
     * Test conversion of image and well requests to image, fileset and well requests.
     */
    @Test
    public void testImagesWellsToImagesFilesetsWells() {
        addChgrpRequest("/Image", 0, 8);
        addChgrpRequest("/Image", 1, 8);
        addChgrpRequest("/Image", 2, 8);
        addChgrpRequest("/Well", 0, 8);
        addChgrpRequest("/Well", 1, 8);
        addChgrpRequest("/Well", 2, 8);

        process();

        assertRequests(
                "CHGRP(8)[/Well:0]",
                "CHGRP(8)[/Fileset:0]",
                "CHGRP(8)[/Well:1]",
                "CHGRP(8)[/Well:2]",
                "CHGRP(8)[/Fileset:1]"
                );
    }

    /**
     * Test conversion of well and image requests to well, fileset and image requests.
     */
    @Test
    public void testWellsImagesToWellsFilesetsImages() {
        addChgrpRequest("/Well", 0, 8);
        addChgrpRequest("/Well", 1, 8);
        addChgrpRequest("/Well", 2, 8);
        addChgrpRequest("/Image", 0, 8);
        addChgrpRequest("/Image", 1, 8);
        addChgrpRequest("/Image", 2, 8);

        process();

        assertRequests(
                "CHGRP(8)[/Well:0]",
                "CHGRP(8)[/Well:1]",
                "CHGRP(8)[/Well:2]",
                "CHGRP(8)[/Fileset:0]",
                "CHGRP(8)[/Fileset:1]"
                );
    }

    /**
     * Test conversion of well requests to fileset and well requests in different groups.
     */
    @Test
    public void testWellsToFilesetsChgrpDifferent() {
        addChgrpRequest("/Well", 3, 8);
        addChgrpRequest("/Well", 1, 9);
        addChgrpRequest("/Well", 4, 8);
        addChgrpRequest("/Well", 2, 9);
        addChgrpRequest("/Well", 5, 8);

        process();

        assertRequests(
                "CHGRP(8)[/Well:3]",
                "CHGRP(9)[/Well:1]",
                "CHGRP(8)[/Well:4]",
                "CHGRP(9)[/Well:2]",
                "CHGRP(9)[/Fileset:1]",
                "CHGRP(8)[/Well:5]",
                "CHGRP(8)[/Fileset:2]"
                );
    }

    /**
     * Test conversion of plate requests to plate requests.
     */
    @Test
    public void testPlatesToPlates() {
        addDeleteRequest("/Plate", 0);

        process();

        assertRequests(
                "DELETE[/Plate:0]",
                "DELETE[/Fileset:0]"
                );
    }

    /**
     * Test conversion of plate requests to fileset and plate requests.
     */
    @Test
    public void testPlatesToFilesetsPlates() {
        addDeleteRequest("/Plate", 0);
        addDeleteRequest("/Plate", 1);

        process();

        assertRequests(
                "DELETE[/Plate:0]",
                "DELETE[/Fileset:0]",
                "DELETE[/Plate:1]",
                "DELETE[/Fileset:1]"
                );
    }

    /**
     * Test conversion of plate and image requests to fileset, plate and image requests.
     */
    @Test
    public void testPlatesImagesToFilesetsPlatesImages() {
        addDeleteRequest("/Plate", 0);
        addDeleteRequest("/Plate", 1);
        addDeleteRequest("/Image", 4);
        addDeleteRequest("/Image", 5);
        addDeleteRequest("/Image", 6);

        process();

        assertRequests(
                "DELETE[/Plate:0]",
                "DELETE[/Fileset:0]",
                "DELETE[/Plate:1]",
                "DELETE[/Fileset:1]",
                "DELETE[/Fileset:2]",
                "DELETE[/Image:6]"
                );
    }

    /**
     * Test conversion of screen requests to fileset and screen requests.
     */
    @Test
    public void testScreensToFilesetsScreens() {
        addDeleteRequest("/Screen", 0);

        process();

        assertRequests(
                "DELETE[/Screen:0]",
                "DELETE[/Fileset:0]",
                "DELETE[/Fileset:1]",
                "DELETE[/Fileset:2]"
                );

    }

    /**
     * Test conversion of screen and image requests to fileset, screen and image requests.
     */
    @Test
    public void testScreensImagesToFilesetsScreensImages() {
        addDeleteRequest("/Screen", 0);
        addDeleteRequest("/Image", 4);
        addDeleteRequest("/Image", 5);
        addDeleteRequest("/Image", 6);

        process();

        assertRequests(
                _("DELETE[/Screen:0]"),
                _("DELETE[/Fileset:0]"),
                _("DELETE[/Fileset:1]"),
                _("DELETE[/Fileset:2]"),
                _("DELETE[/Image:6]")
                );
    }

    /**
     * Test conversion of image, plate and screen requests to image, fileset, plate and screen requests.
     */
    @Test
    public void testImagesPlatesScreensToImagesFilesetsPlatesScreens() {
        for (long imageId = 0; imageId < 7; imageId++) {
            addDeleteRequest("/Image", imageId);
        }
        addDeleteRequest("/Plate", 0);
        addDeleteRequest("/Plate", 1);
        addDeleteRequest("/Screen", 0);

        process();

        assertRequests(
                "DELETE[/Image:6]",
                "DELETE[/Plate:0]",
                "DELETE[/Plate:1]",
                "DELETE[/Screen:0]",
                "DELETE[/Fileset:0]",
                "DELETE[/Fileset:1]",
                "DELETE[/Fileset:2]"
                );
    }

    /**
     * Test conversion of plate and well requests to fileset, plate and well requests.
     */
    @Test
    public void testPlatesWellsToFilesetsPlatesWells() {
        addDeleteRequest("/Plate", 0);
        addDeleteRequest("/Plate", 1);
        addDeleteRequest("/Well", 4);
        addDeleteRequest("/Well", 5);
        addDeleteRequest("/Well", 6);

        process();

        assertRequests(
                "DELETE[/Plate:0]",
                "DELETE[/Fileset:0]",
                "DELETE[/Plate:1]",
                "DELETE[/Fileset:1]",
                "DELETE[/Well:4]",
                "DELETE[/Well:5]",
                "DELETE[/Fileset:2]",
                "DELETE[/Well:6]"
                );
    }

    /**
     * Test conversion of screen and well requests to fileset, screen and well requests.
     */
    @Test
    public void testScreensWellsToFilesetsScreensWells() {
        addDeleteRequest("/Screen", 0);
        addDeleteRequest("/Well", 4);
        addDeleteRequest("/Well", 5);
        addDeleteRequest("/Well", 6);

        process();

        assertRequests(
                _("DELETE[/Screen:0]"),
                _("DELETE[/Fileset:0]"),
                _("DELETE[/Fileset:1]"),
                _("DELETE[/Well:4]"),
                _("DELETE[/Well:5]"),
                _("DELETE[/Fileset:2]"),
                _("DELETE[/Well:6]")
                );
    }

    /**
     * Test conversion of well, plate and screen requests to well, fileset, plate and screen requests.
     */
    @Test
    public void testWellsPlatesScreensToWellsFilesetsPlatesScreens() {
        for (long wellId = 0; wellId < 7; wellId++) {
            addDeleteRequest("/Well", wellId);
        }
        addDeleteRequest("/Plate", 0);
        addDeleteRequest("/Plate", 1);
        addDeleteRequest("/Screen", 0);

        process();

        assertRequests(
                "DELETE[/Well:0]",
                "DELETE[/Well:1]",
                "DELETE[/Well:2]",
                "DELETE[/Well:3]",
                "DELETE[/Well:4]",
                "DELETE[/Well:5]",
                "DELETE[/Well:6]",
                "DELETE[/Plate:0]",
                "DELETE[/Plate:1]",
                "DELETE[/Screen:0]",
                "DELETE[/Fileset:0]",
                "DELETE[/Fileset:1]",
                "DELETE[/Fileset:2]"
                );
    }

    /**
     * Test conversion of mixed image requests to image requests.
     */
    @Test
    public void testImagesToImagesMixedRequests() {
        addChgrpRequest("/Image", 1, 1);
        addDeleteRequest("/Image", 2);

        process();

        assertRequests(
                "CHGRP(1)[/Image:1]",
                "DELETE[/Image:2]"
                );
    }

    /**
     * Test conversion of mixed image requests to fileset requests.
     */
    @Test
    public void testImagesToFilesetsMixedRequests() {
        addChgrpRequest("/Image", 1, 1);
        addDeleteRequest("/Image", 1);
        addChgrpRequest("/Image", 2, 1);
        addDeleteRequest("/Image", 2);

        process();

        assertRequests(
                "CHGRP(1)[/Fileset:1]",
                "DELETE[/Fileset:1]"
                );
    }
}

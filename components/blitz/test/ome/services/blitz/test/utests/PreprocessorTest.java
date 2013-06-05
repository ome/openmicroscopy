/*
 * Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

import omero.cmd.Chgrp;
import omero.cmd.Delete;
import omero.cmd.GraphModify;
import omero.cmd.Request;
import omero.cmd.graphs.ChgrpI;
import omero.cmd.graphs.DeleteI;
import omero.cmd.graphs.Preprocessor;

/**
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
@Test(groups = {"fs"})
public class PreprocessorTest extends Preprocessor {
    public PreprocessorTest() {
        super(new ArrayList<Request>(), null);
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

    /**
     * Test that the HQL query strings match what is needed to navigate the target type hierarchy.
     */
    @Test
    public void testHqlStrings() {
        final Set<Entry<TargetType, TargetType>> expectedKeys = new HashSet<Entry<TargetType, TargetType>>();
        for (final Entry<TargetType, TargetType> relationship : targetTypeHierarchy) {
            expectedKeys.add(relationship);
            expectedKeys.add(Maps.immutableEntry(relationship.getValue(), relationship.getKey()));
        }
        Assert.assertTrue(CollectionUtils.isEqualCollection(expectedKeys, hqlFromTo.keySet()));
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

        containeds.putAll(Maps.immutableEntry(TargetType.IMAGE, new GraphModifyTarget(TargetType.WELL, 0)), ImmutableList.of(0L));
        containeds.putAll(Maps.immutableEntry(TargetType.IMAGE, new GraphModifyTarget(TargetType.WELL, 1)), ImmutableList.of(1L));
        containeds.putAll(Maps.immutableEntry(TargetType.IMAGE, new GraphModifyTarget(TargetType.WELL, 2)), ImmutableList.of(2L));

        containeds.putAll(Maps.immutableEntry(TargetType.WELL, new GraphModifyTarget(TargetType.PLATE, 0)), ImmutableList.of(0L, 1L));
        containeds.putAll(Maps.immutableEntry(TargetType.WELL, new GraphModifyTarget(TargetType.PLATE, 1)), ImmutableList.of(2L, 3L));

        containeds.putAll(Maps.immutableEntry(TargetType.PLATE, new GraphModifyTarget(TargetType.SCREEN, 0)), ImmutableList.of(0L, 1L));

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

    /* load cache with container lookup from test hierarchy */
    @Override
    protected void lookupContainer(TargetType containerType, GraphModifyTarget contained) {
        final Set<Long> containerIds = containers.get(Maps.immutableEntry(containerType, contained));
        for (final Long containerId : containerIds) {
            final GraphModifyTarget container = new GraphModifyTarget(containerType, containerId);
            containerByContained.put(contained, container);
        }
    }

    /* load cache with contained lookup from test hierarchy */
    @Override
    protected void lookupContained(TargetType containedType, GraphModifyTarget container) {
        final Set<Long> containedIds = containeds.get(Maps.immutableEntry(containedType, container));
        for (final Long containedId : containedIds) {
            final GraphModifyTarget contained = new GraphModifyTarget(containedType, containedId);
            containedByContainer.put(container, contained);
        }
    }

    /**
     * Add a delete request to the list of requests.
     * @param type the target type
     * @param id the target ID
     */
    private void addDeleteRequest(String type, long id) {
        final Delete delete = new DeleteI(null);
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
        final Chgrp chgrp = new ChgrpI(null, null);
        chgrp.type = type;
        chgrp.id = id;
        chgrp.grp = group;
        this.requests.add(chgrp);
    }

    /**
     * Convert the list of requests to a pretty string.
     * @return the request string
     */
    private String requestsToString() {
        final StringBuffer requestStrings = new StringBuffer();
        for (final Request request : this.requests) {
            if (request instanceof Delete) {
                requestStrings.append("DELETE");
            } else if (request instanceof Chgrp) {
                requestStrings.append("CHGRP");
                requestStrings.append('(');
                requestStrings.append(((Chgrp) request).grp);
                requestStrings.append(')');
            } else {
                requestStrings.append('?');
            }
            if (request instanceof GraphModify) {
                final GraphModify graphModify = (GraphModify) request;
                requestStrings.append('[');
                requestStrings.append(graphModify.type);
                requestStrings.append(':');
                requestStrings.append(graphModify.id);
                requestStrings.append(']');
            }
            requestStrings.append(", ");
        }
        if (requestStrings.length() > 1) {
            requestStrings.setLength(requestStrings.length() - 2);
        }
        return requestStrings.toString();
    }

    /* TODO: review, rename, expand, comment the unit tests below */

    @Test
    public void testImagesOfFilesets() {
        requests.clear();
        for (long imageId = 1; imageId < 6; imageId++) {
            addDeleteRequest("/Image", imageId);
        }

        final ImmutableList<String> expected = ImmutableList.of(
                "DELETE[/Fileset:1], DELETE[/Fileset:2]",
                "DELETE[/Fileset:2], DELETE[/Fileset:1]");

        process();
        final String actual = requestsToString();
        Assert.assertTrue(expected.contains(actual));
    }

    @Test
    public void testFilesets() {
        requests.clear();
        addDeleteRequest("/Fileset", 1);
        addDeleteRequest("/Fileset", 2);

        final ImmutableList<String> expected = ImmutableList.of(
                "DELETE[/Fileset:1], DELETE[/Fileset:2]");

        process();
        final String actual = requestsToString();
        Assert.assertTrue(expected.contains(actual));
    }

    @Test
    public void testImagesFilesets() {
        requests.clear();
        for (long imageId = 1; imageId < 6; imageId++) {
            addDeleteRequest("/Image", imageId);
        }
        addDeleteRequest("/Fileset", 1);
        addDeleteRequest("/Fileset", 2);

        final ImmutableList<String> expected = ImmutableList.of(
                "DELETE[/Fileset:1], DELETE[/Fileset:2]",
                "DELETE[/Fileset:2], DELETE[/Fileset:1]");

        process();
        final String actual = requestsToString();
        Assert.assertTrue(expected.contains(actual));
    }

    @Test
    public void testFilesetsImages() {
        requests.clear();
        addDeleteRequest("/Fileset", 1);
        addDeleteRequest("/Fileset", 2);
        for (long imageId = 1; imageId < 6; imageId++) {
            addDeleteRequest("/Image", imageId);
        }

        final ImmutableList<String> expected = ImmutableList.of(
                "DELETE[/Fileset:1], DELETE[/Fileset:2]");

        process();
        final String actual = requestsToString();
        Assert.assertTrue(expected.contains(actual));
    }

    @Test
    public void testFilesetImageFilesets() {
        requests.clear();
        for (long filesetId = 1; filesetId < 3; filesetId++) {
            addChgrpRequest("/Fileset", filesetId, 8);
        }
        for (long imageId = 0; imageId < 7; imageId++) {
            addChgrpRequest("/Image", imageId, 8);
        }

        final ImmutableList<String> expected = ImmutableList.of(
                "CHGRP(8)[/Fileset:1], CHGRP(8)[/Fileset:2], CHGRP(8)[/Image:0], CHGRP(8)[/Image:6]",
                "CHGRP(8)[/Fileset:2], CHGRP(8)[/Fileset:1], CHGRP(8)[/Image:0], CHGRP(8)[/Image:6]");

        process();
        final String actual = requestsToString();
        Assert.assertTrue(expected.contains(actual));
    }

    @Test
    public void testImageFilesetFilesets() {
        requests.clear();
        for (long imageId = 0; imageId < 7; imageId++) {
            addChgrpRequest("/Image", imageId, 8);
        }
        for (long filesetId = 1; filesetId < 3; filesetId++) {
            addChgrpRequest("/Fileset", filesetId, 8);
        }

        final ImmutableList<String> expected = ImmutableList.of(
                "CHGRP(8)[/Image:0], CHGRP(8)[/Fileset:1], CHGRP(8)[/Fileset:2], CHGRP(8)[/Image:6]",
                "CHGRP(8)[/Image:0], CHGRP(8)[/Fileset:2], CHGRP(8)[/Fileset:1], CHGRP(8)[/Image:6]");

        process();
        final String actual = requestsToString();
        Assert.assertTrue(expected.contains(actual));
    }

    @Test
    public void testImageFilesets() {
        requests.clear();
        for (long imageId = 0; imageId < 7; imageId++) {
            addChgrpRequest("/Image", imageId, 8);
        }

        final ImmutableList<String> expected = ImmutableList.of(
                "CHGRP(8)[/Image:0], CHGRP(8)[/Fileset:1], CHGRP(8)[/Fileset:2], CHGRP(8)[/Image:6]",
                "CHGRP(8)[/Image:0], CHGRP(8)[/Fileset:2], CHGRP(8)[/Fileset:1], CHGRP(8)[/Image:6]");

        process();
        final String actual = requestsToString();
        Assert.assertTrue(expected.contains(actual));
    }

    @Test
    public void testImageFilesetsChgrpDifferentGroups() {
        requests.clear();
        addChgrpRequest("/Image", 1, 1);
        addChgrpRequest("/Image", 2, 2);

        final ImmutableList<String> expected = ImmutableList.of(
                "CHGRP(1)[/Image:1], CHGRP(2)[/Image:2]");

        process();
        final String actual = requestsToString();
        Assert.assertTrue(expected.contains(actual));
    }

    @Test
    public void testImageFilesetsChgrpSameGroup() {
        requests.clear();
        addChgrpRequest("/Image", 1, 1);
        addChgrpRequest("/Image", 2, 1);

        final ImmutableList<String> expected = ImmutableList.of(
                "CHGRP(1)[/Fileset:1]");

        process();
        final String actual = requestsToString();
        Assert.assertTrue(expected.contains(actual));
    }

    @Test
    public void testProjectFilesets() {
        requests.clear();
        addDeleteRequest("/Project", 0);

        final ImmutableList<String> expected = ImmutableList.of(
                "DELETE[/Fileset:1], DELETE[/Project:0]");

        process();
        final String actual = requestsToString();
        Assert.assertTrue(expected.contains(actual));
    }

    @Test
    public void testScreenFilesets() {
        requests.clear();
        addDeleteRequest("/Screen", 0);

        final ImmutableList<String> expected = ImmutableList.of(
                "DELETE[/Fileset:1], DELETE[/Screen:0]");

        process();
        final String actual = requestsToString();
        Assert.assertTrue(expected.contains(actual));
    }
}

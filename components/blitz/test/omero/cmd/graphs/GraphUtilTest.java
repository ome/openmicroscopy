/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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

package omero.cmd.graphs;

import java.util.Collections;
import java.util.Map.Entry;

import ome.services.graphs.GraphTraversal;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Tests the static utility methods for model graph operations.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
@Test
public class GraphUtilTest {

    /**
     * Test that {@link GraphUtil#getIdListMapSize(java.util.Map)} counts an empty map as having no IDs.
     */
    public void testGetIdListMapSizeUnpopulated() {
        final int expected = 0;
        final int actual = GraphUtil.getIdListMapSize(Collections.<Object, long[]>emptyMap());
        Assert.assertEquals(actual, expected,
                "an empty ID list map must have no IDs");
    }

    /**
     * Test that {@link GraphUtil#getIdListMapSize(java.util.Map)} counts IDs correctly.
     */
    public void testGetIdListMapSizePopulated() {
        final ImmutableMap.Builder<Integer, long[]> builder = ImmutableMap.builder();
        int expected = 0;
        for (int i = 0; i < 20; i++) {
            builder.put(i, new long[i]);
            expected += i;
        }
        final int actual = GraphUtil.getIdListMapSize(builder.build());
        Assert.assertEquals(actual, expected,
                "ID list map size is incorrect");
    }

    /**
     * Test that {@link GraphUtil#idsToArray(java.util.Collection)} converts an empty list to an empty array.
     */
    public void testIdsToArrayUnpopulatedList() {
        final int expected = 0;
        final int actual = GraphUtil.idsToArray(Collections.<Long>emptyList()).length;
        Assert.assertEquals(actual, expected,
                "an empty list should become an empty array");
    }

    /**
     * Test that {@link GraphUtil#idsToArray(java.util.Collection)} converts a set of IDs to an array correctly.
     */
    public void testIdsToArrayPopulatedSet() {
        final ImmutableSet.Builder<Long> builder = ImmutableSet.builder();
        final long[] expected = new long[20];
        for (int i = 0; i < expected.length; i++) {
            builder.add(Long.valueOf(i));
            expected[i] = i;
        }
        final long[] actual = GraphUtil.idsToArray(builder.build());
        Assert.assertEquals(actual.length, expected.length,
                "after conversion to an array there should be the same number of elements");
        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals(actual[i], expected[i],
                    "conversion to an array ought not mutate elements");
        }
    }

    /**
     * Test that {@link GraphUtil#trimPackageNames(SetMultimap)} correctly trim package names from map keys.
     */
    public void testTrimPackageNames() {
        final ImmutableSetMultimap.Builder<String, Integer> builderArgument = ImmutableSetMultimap.builder();
        final ImmutableSetMultimap.Builder<String, Integer> builderExpected = ImmutableSetMultimap.builder();
        for (final Class<?> realClass : ImmutableList.of(GraphTraversal.class, GraphUtil.class, String.class, Integer.class)) {
            builderArgument.put(realClass.getName(), realClass.getSimpleName().length());
            builderExpected.put(realClass.getSimpleName(), realClass.getSimpleName().length());
        }
        final SetMultimap<String, Integer> expected = builderExpected.build();
        final SetMultimap<String, Integer> actual = GraphUtil.trimPackageNames(builderArgument.build());
        Assert.assertEquals(actual.size(), expected.size(),
                "after name trimming there should still be the same number of entries");
        for (final Entry<String, Integer> actualEntry : actual.entries()) {
            Assert.assertTrue(expected.containsEntry(actualEntry.getKey(), actualEntry.getValue()),
                    "every returned entry should be expected");
        }
    }

    /**
     * Generate test data for {@link #testGetFirstClassName(String, String)}.
     * @return pairs of type paths and the first class name from each path
     */
    @DataProvider(name = "type paths")
    public String[][] getTypePaths() {
        return new String[][] {
                new String[] {"/Image", "Image"},
                new String[] {"/Dataset/Image", "Dataset"},
                new String[] {"/Plate/PlateAcquisition/Well", "Plate"}
        };
    }

    /**
     * Test that {@link GraphUtil#getFirstClassName(String)} correctly extracts the first class name from type paths.
     * @param argument the type path argument to {@link GraphUtil#getFirstClassName(String)}
     * @param expected the type path's first class name as expected from {@link GraphUtil#getFirstClassName(String)}
     */
    @Test(dataProvider = "type paths")
    public void testGetFirstClassName(String argument, String expected) {
        final String actual = GraphUtil.getFirstClassName(argument);
        Assert.assertEquals(actual, expected,
                "the first class name should be extracted from the path");
    }
}

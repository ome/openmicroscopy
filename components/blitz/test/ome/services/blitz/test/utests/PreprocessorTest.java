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

import com.google.common.collect.Maps;

import omero.cmd.Request;
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
}

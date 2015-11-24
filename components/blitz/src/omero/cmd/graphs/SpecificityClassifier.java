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

package omero.cmd.graphs;

import java.util.HashMap;
import java.util.Map;

/**
 * A classifier that assigns an entity to the classification listed for the most specific group of which the entity is a member.
 * Consider entities themselves to be singleton groups.
 * Implemented more for obvious correctness than for performance on large data.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.2.1
 * @param <G> the type of the groups of which entities may be members, and also of the entities themselves
 * @param <C> the classes to which entities may be assigned
 */
class SpecificityClassifier<G, C> {

    /**
     * Tests group membership. The membership relation must be irreflexive, asymmetric, and transitive.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.2.1
     * @param <G> the type of the groups of which entities may be members, and also of the entities themselves
     */
    interface ContainmentTester<G> {

        /**
         * Report if one group is a proper superset of the other.
         * @param parent a group that may contain another
         * @param child the other group
         * @return if the first group contains the other without being the same as it
         */
        boolean isProperSupersetOf(G parent, G child);
    }

    private final ContainmentTester<G> tester;
    private final Map<G, C> classOfGroup = new HashMap<G, C>();

    /**
     * Create a classifier that uses the given definition of group membership.
     * @param tester the group membership tester
     */
    SpecificityClassifier(ContainmentTester<G> tester) {
        this.tester = tester;
    }

    /**
     * Assert that specific groups are of the given classification.
     * @param classification a classification
     * @param groupsInClass the groups that are of this classification
     * @throws IllegalArgumentException if any of the groups are already of a different classification
     */
    void addClass(C classification, Iterable<G> groupsInClass) throws IllegalArgumentException {
        for (final G group : groupsInClass) {
            final C previousClass = classOfGroup.put(group, classification);
            if (!(previousClass == null || previousClass.equals(classification))) {
                throw new IllegalArgumentException("cannot classify " + group + " as " + classification +
                        " because it is also " + previousClass);
            }
        }
    }

    /**
     * Classify the given group.
     * @param group a group
     * @return its classification, or {@code null} if it could not be classified
     */
    C getClass(G group) {
        final C directLookup = classOfGroup.get(group);
        if (directLookup != null) {
            return directLookup;
        }
        Map.Entry<G, C> bestGroupClassification = null;
        for (final Map.Entry<G, C> currentGroupClassification : classOfGroup.entrySet()) {
            if (tester.isProperSupersetOf(currentGroupClassification.getKey(), group) &&
                    (bestGroupClassification == null ||
                    tester.isProperSupersetOf(bestGroupClassification.getKey(), currentGroupClassification.getKey()))) {
                bestGroupClassification = currentGroupClassification;
            }
        }
        return bestGroupClassification == null ? null : bestGroupClassification.getValue();
    }
}

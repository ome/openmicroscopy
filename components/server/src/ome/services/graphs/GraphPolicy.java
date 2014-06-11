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

package ome.services.graphs;

import java.util.Map;
import java.util.Set;

import ome.model.IObject;
 
/**
 * A policy guides how to traverse the graph. This class' methods are expected to be fast.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0.x TODO
 */
public abstract class GraphPolicy {

    /**
     * The action to take on an object instance.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.0.x TODO
     */
    public static enum Action {
        /** do not include the object in the operation */
        EXCLUDE,
        /** delete the object */
        DELETE,
        /** include the object in the operation */
        INCLUDE,
        /** object is inappropriate for the operation and may be related to both included and excluded objects */
        OUTSIDE;
    }

    /**
     * If an object instance has any {@link Action#EXCLUDE}d <q>parents</q> that would prevent it from being <q>orphaned</q>.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.0.x TODO
     */
    public static enum Orphan {
        /** it is not known and it does not matter if the object is an orphan: effort should not yet be made to find out */
        IRRELEVANT,
        /** it is not known but it matters if the object is an orphan: effort should be made to find out */
        RELEVANT,
        /** the object is an orphan */
        IS_LAST,
        /** the object is not an orphan */
        IS_NOT_LAST;
    }

    /**
     * A tuple noting the state of a mapped object instance in the current graph traversal.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.0.x TODO
     */
    public static abstract class Details {
        /** the unloaded instance */
        public final IObject subject;
        /** TODO permissions */
        public final Object permissions = null;
        /** the current plan for the object, may be mutated by {@link Policy#review(Map, Details, Map, Set)} */
        public Action action;
        /** 
         * the current <q>orphan</q> state of the object, may be mutated by {@link Policy#review(Map, Details, Map, Set)};
         * applies only if {@link #action} is {@link Action#EXCLUDE}
         */
        public Orphan orphan;

        /**
         * Construct a note of an object and its details.
         * {@link #equals(Object)} and {@link #hashCode()} consider only the subject, not the action or orphan.
         * @param subject the object whose details these are
         * @param action the current plan for the object
         * @param orphan the current <q>orphan</q> state of the object
         */
        Details(IObject subject, Action action, Orphan orphan) {
            this.subject = subject;
            this.action = action;
            this.orphan = orphan;
        }
    }

    /**
     * Any model object about which policy may be asked is first passed to {@link #noteDetails(IObject, String, long)} before
     * {@link #review(Map, Details, Map, Set)}. The same object may be passed multiple times.
     * @param object a model object about which policy may be asked;
     *               it is a Hibernate proxy object loaded without any extra joins on retrieval
     * @param realClass the real class name of the object
     * @param id the ID of the object
     */
    public void noteDetails(IObject object, String realClass, long id) {
        /* This method is a no-op that subclasses may override. */
    }

    /**
     * The action to take about the link between the mapped objects.
     * An {@link Action#EXCLUDE}d object, once changed from that, may not change back to {@link Action#EXCLUDE}.
     * An {@link Action#OUTSIDE} object, once changed to that, may not change back from {@link Action#OUTSIDE}.
     * {@link Orphan} values matter only for {@link Action#EXCLUDE}d objects.
     * Given {@link Orphan#RELEVANT} if {@link Action#IS_LAST} or {@link Action#IS_NOT_LAST} can be returned,
     * or could be if after {@link Orphan#RELEVANT} is returned then resolved for the other object,
     * then appropriate values should be returned accordingly.
     * If {@link Action#RELEVANT} is returned for an object then this method may be called again with
     * {@link Action#IS_LAST} or {@link Action#IS_NOT_LAST}.
     * Class properties' <code>String</code> representation is <code>package.DeclaringClass.propertyName</code>.
     * @param linkedFrom map from class property to objects for which the property links to the root object
     * @param rootObject the object at the center of this review
     * @param linkedTo map from class property to objects to which the property links from the root object
     * @param notNullable which properties from the linkedFrom and linkedTo map keys are not nullable
     * @return changes to make, included unchanged details typically cause review as root object
     * @throws GraphException if there was a problem in applying the policy
     */
    public abstract Set<Details> review(Map<String, Set<Details>> linkedFrom, Details rootObject,
            Map<String, Set<Details>> linkedTo, Set<String> notNullable) throws GraphException;
}

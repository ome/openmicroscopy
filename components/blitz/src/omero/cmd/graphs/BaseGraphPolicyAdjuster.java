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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;

import ome.model.IObject;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPolicy;
import ome.services.graphs.GraphPolicyRulePredicate;

/**
 * The base class assists adjustment of an existing graph traversal policy.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */

public abstract class BaseGraphPolicyAdjuster extends GraphPolicy {

    private final GraphPolicy graphPolicy;

    /**
     * Construct a new graph policy adjuster.
     * @param graphPolicy the graph policy that is to be adjusted
     */
    public BaseGraphPolicyAdjuster(GraphPolicy graphPolicy) {
        this.graphPolicy = graphPolicy.getCleanInstance();
    }

    /**
     * An opportunity to adjust each model object before the graph policy reviews it.
     * @param object the model object before review
     * @return if this object's details were changed by this adjustment
     */
    protected boolean isAdjustedBeforeReview(Details object) {
        return false;
    }

    /**
     * An opportunity to adjust each model object after the graph policy reviews it.
     * @param object the model object after review
     * @return if this object's details were changed by this adjustment
     */
    protected boolean isAdjustedAfterReview(Details object) {
        return false;
    }

    @Override
    public void registerPredicate(GraphPolicyRulePredicate predicate) {
        graphPolicy.registerPredicate(predicate);
    }

    @Override
    public GraphPolicy getCleanInstance() {
        return graphPolicy.getCleanInstance();
    }

    @Override
    public void setCondition(String name) {
        graphPolicy.setCondition(name);
    }

    @Override
    public boolean isCondition(String name) {
        return graphPolicy.isCondition(name);
    }

    @Override
    public void noteDetails(Session session, IObject object, String realClass, long id) {
        graphPolicy.noteDetails(session, object, realClass, id);
    }

    @Override
    public final Set<Details> review(Map<String, Set<Details>> linkedFrom, Details rootObject, Map<String, Set<Details>> linkedTo,
            Set<String> notNullable, boolean isErrorRules) throws GraphException {
        /* note all the model objects that may be adjusted in review */
        final Set<Details> allTerms = new HashSet<Details>();
        allTerms.add(rootObject);
        for (final Set<Details> terms : linkedFrom.values()) {
            allTerms.addAll(terms);
        }
        for (final Set<Details> terms : linkedTo.values()) {
            allTerms.addAll(terms);
        }
        /* allow isAdjustedBeforeReview to adjust objects before review */
        final Set<Details> changedTerms = new HashSet<Details>();
        for (final Details object : allTerms) {
            if (isAdjustedBeforeReview(object)) {
                changedTerms.add(object);
            }
        }
        /* do the review */
        changedTerms.addAll(graphPolicy.review(linkedFrom, rootObject, linkedTo, notNullable, isErrorRules));
        /* allow isAdjustedAfterReview to adjust objects after review */
        for (final Details object : allTerms) {
            if (isAdjustedAfterReview(object)) {
                changedTerms.add(object);
            }
        }
        return changedTerms;
    }
}

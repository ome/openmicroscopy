/*
 * Copyright (C) 2014-2015 University of Dundee & Open Microscopy Environment.
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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

import ome.model.IObject;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPolicy;
import ome.services.graphs.GraphPolicyRulePredicate;

/**
 * Adjust graph traversal policy to avoid processing or acting on certain model object classes.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.2.1
 */
public class SkipTailPolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(SkipTailPolicy.class);

    /**
     * Adjust an existing graph traversal policy so that processing stops at certain model object classes.
     * @param graphPolicy the graph policy to adjust
     * @param isSkipClass if a given class should be not be reviewed or acted on
     * @return the adjusted graph policy
     */
    public static GraphPolicy getSkipTailPolicy(final GraphPolicy graphPolicy,
            final Predicate<Class<? extends IObject>> isSkipClass) {

        /* construct the function corresponding to the model graph descent truncation */

        return new GraphPolicy() {
            @Override
            public void registerPredicate(GraphPolicyRulePredicate predicate) {
                graphPolicy.registerPredicate(predicate);
            }

            @Override
            public GraphPolicy getCleanInstance() {
                throw new IllegalStateException("not expecting to provide a clean instance");
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
            public final Set<Details> review(Map<String, Set<Details>> linkedFrom, Details rootObject,
                    Map<String, Set<Details>> linkedTo, Set<String> notNullable, boolean isErrorRules) throws GraphException {
                if (isSkipClass.apply(rootObject.subject.getClass())) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("halting review at " + rootObject);
                    }
                    /* request parameters specify to review no further */
                    return Collections.emptySet();
                } else {
                    /* do the review */
                    final Set<Details> changes = graphPolicy.review(linkedFrom, rootObject, linkedTo, notNullable, isErrorRules);
                    final Iterator<Details> changesIterator = changes.iterator();
                    while (changesIterator.hasNext()) {
                        final Details change = changesIterator.next();
                        if (change.action == Action.INCLUDE && isSkipClass.apply(change.subject.getClass())) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("forestalling policy-based change " + change);
                            }
                            /* do not act on skipped classes */
                            changesIterator.remove();
                        }
                    }
                    return changes;
                }
            }
        };
    }
}

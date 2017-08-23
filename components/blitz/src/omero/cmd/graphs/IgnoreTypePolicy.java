/*
 * Copyright (C) 2014-2017 University of Dundee & Open Microscopy Environment.
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

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

import ome.model.IObject;
import ome.services.graphs.GraphPolicy;

/**
 * Adjust graph traversal policy to ignore objects according to their type.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.4.0
 * @deprecated experimental: may be wholly removed in next major version
 */
@Deprecated
public class IgnoreTypePolicy {

    private static final Logger LOGGER = LoggerFactory.getLogger(IgnoreTypePolicy.class);

    /**
     * Adjust an existing graph traversal policy so that objects of certain types may be ignored.
     * @param graphPolicyToAdjust the graph policy to adjust
     * @param typesToIgnore the types to ignore as defined by {@link omero.cmd.Delete2#typesToIgnore}
     * @return the adjusted graph policy
     */
    public static GraphPolicy getIgnoreTypePolicy(GraphPolicy graphPolicyToAdjust,
            final Collection<Class<? extends IObject>> typesToIgnore) {
        if (CollectionUtils.isEmpty(typesToIgnore)) {
            return graphPolicyToAdjust;
        }

        final Predicate<Class<? extends IObject>> isTypeToIgnore = new Predicate<Class<? extends IObject>>() {
            @Override
            public boolean apply(Class<? extends IObject> objectClass) {
                for (final Class<? extends IObject> typeToIgnore : typesToIgnore) {
                    if (typeToIgnore.isAssignableFrom(objectClass)) {
                        return true;
                    }
                }
                return false;
            }
        };

        return new BaseGraphPolicyAdjuster(graphPolicyToAdjust) {
            @Override
            protected boolean isAdjustedBeforeReview(Details object) {
                if (object.action == GraphPolicy.Action.EXCLUDE && isTypeToIgnore.apply(object.subject.getClass())) {
                    object.action = GraphPolicy.Action.OUTSIDE;
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("ignoring all objects of its type, so making " + object);
                    }
                    return true;
                }
                return false;
            }
        };
    }
}

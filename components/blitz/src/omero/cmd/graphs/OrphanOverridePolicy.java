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

import java.util.Collection;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import ome.model.IObject;
import ome.services.graphs.GraphPathBean;
import ome.services.graphs.GraphPolicy;
import ome.services.graphs.GraphPolicy.Details;

/**
 * Adjust graph traversal policy based on overrides on the treatment of orphans by type.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class OrphanOverridePolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrphanOverridePolicy.class);

    /**
     * Adjust an existing graph traversal policy so that orphaned model objects will always or never be included,
     * according to their type.
     * @param graphPolicyToAdjust the graph policy to adjust
     * @param graphPathBean the graph path bean, for converting class names to the actual classes
     * @param includeChild the types of orphaned children that must always be included, ignored if empty or {@code null}
     * @param excludeChild the types of orphaned children that must never be included, ignored if empty or {@code null}
     * @param requiredPermissions the permissions required for processing instances with
     * {@link ome.services.graphs.GraphTraversal.Processor#processInstances(String, Collection)}
     * @return the adjusted graph policy
     */
    public static GraphPolicy getOrphanOverridePolicy(final GraphPolicy graphPolicyToAdjust, final GraphPathBean graphPathBean,
            Collection<String> includeChild, Collection<String> excludeChild, final Set<GraphPolicy.Ability> requiredPermissions) {
        if (CollectionUtils.isEmpty(includeChild) && CollectionUtils.isEmpty(excludeChild)) {
            /* there is no adjustment to make */
            return graphPolicyToAdjust;
        }

        /* convert the class names to actual classes */

        final Function<String, Class<? extends IObject>> getClassFromName = new Function<String, Class<? extends IObject>>() {
            @Override
            public Class<? extends IObject> apply(String className) {
                final int lastDot = className.lastIndexOf('.');
                if (lastDot > 0) {
                    className = className.substring(lastDot + 1);
                }
                return graphPathBean.getClassForSimpleName(className);
            }
        };

        /* construct the function corresponding to the inclusion requirements */

        final ImmutableSet<Class<? extends IObject>> inclusions;
        final ImmutableSet<Class<? extends IObject>> exclusions;

        if (CollectionUtils.isEmpty(includeChild)) {
            inclusions = ImmutableSet.of();
        } else {
            inclusions = ImmutableSet.copyOf(Collections2.transform(includeChild, getClassFromName));
        }
        if (CollectionUtils.isEmpty(excludeChild)) {
            exclusions = ImmutableSet.of();
        } else {
            exclusions = ImmutableSet.copyOf(Collections2.transform(excludeChild, getClassFromName));
        }

        final Function<Details, Boolean> isInclude = new Function<Details, Boolean>() {
            @Override
            public Boolean apply(Details object) {
                final Class<? extends IObject> objectClass = object.subject.getClass();
                for (final Class<? extends IObject> inclusion : inclusions) {
                    if (inclusion.isAssignableFrom(objectClass)) {
                        return Boolean.TRUE;
                    }
                }
                for (final Class<? extends IObject> exclusion : exclusions) {
                    if (exclusion.isAssignableFrom(objectClass)) {
                        return Boolean.FALSE;
                    }
                }
                return null;
            }
        };

        /* wrap the traversal policy so that the namespace restriction is effected */

        return new BaseGraphPolicyAdjuster(graphPolicyToAdjust) {
            @Override
            protected boolean isAdjustedBeforeReview(Details object) {
                if (object.action == GraphPolicy.Action.EXCLUDE &&
                        object.orphan != GraphPolicy.Orphan.IS_LAST && object.orphan != GraphPolicy.Orphan.IS_NOT_LAST) {
                    /* the model object is [E]{ir} */
                    final Boolean isIncludeVerdict = isInclude.apply(object);
                    if (isIncludeVerdict == Boolean.TRUE && (requiredPermissions == null ||
                            Sets.difference(requiredPermissions, object.permissions).isEmpty())) {
                        object.orphan = GraphPolicy.Orphan.IS_LAST;
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("including all children of its type, so making " + object);
                        }
                        return true;
                    } else if (isIncludeVerdict == Boolean.FALSE) {
                        object.orphan = GraphPolicy.Orphan.IS_NOT_LAST;
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("excluding all children of its type, so making " + object);
                        }
                        return true;
                    }
                }
                return false;
            }
        };
    }
}

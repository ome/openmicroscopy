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
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

import ome.model.IObject;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPathBean;
import ome.services.graphs.GraphPolicy;
import ome.services.graphs.GraphPolicyRulePredicate;
import omero.cmd.SkipHead;

/**
 * Adjust graph traversal policy to prevent descent into inclusions beyond certain types.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class SkipHeadPolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(SkipHeadPolicy.class);

    /**
     * Adjust an existing graph traversal policy so that orphaned model objects will always or never be included,
     * according to their type.
     * @param graphPolicy the graph policy to adjust
     * @param graphPathBean the graph path bean, for converting class names to the actual classes
     * @param startFrom the model object types to from which to start inclusion, may not be empty or {@code null}
     * @param startAction the action associated with nodes qualifying as start objects
     * @param permissionsOverrides where to note for which {@code startFrom} objects permissions are not to be checked
     * @return the adjusted graph policy
     * @throws GraphException if no start classes are named
     */
    public static GraphPolicy getSkipHeadPolicySkip(final GraphPolicy graphPolicy, final GraphPathBean graphPathBean,
            Collection<String> startFrom, final GraphPolicy.Action startAction,
            final SetMultimap<String, Long> permissionsOverrides) throws GraphException {
        if (CollectionUtils.isEmpty(startFrom)) {
            throw new GraphException(SkipHead.class.getSimpleName() + " requires the start classes to be named");
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

        final ImmutableSet<Class <? extends IObject>> startFromClasses =
                ImmutableSet.copyOf(Collections2.transform(startFrom, getClassFromName));

        final Predicate<IObject> isStartFrom = new Predicate<IObject>() {
            @Override
            public boolean apply(IObject subject) {
                final Class<? extends IObject> subjectClass = subject.getClass();
                for (final Class<? extends IObject> startFromClass : startFromClasses) {
                    if (startFromClass.isAssignableFrom(subjectClass)) {
                        return true;
                    }
                }
                return false;
            }
        };

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
                if (rootObject.action == startAction && isStartFrom.apply(rootObject.subject)) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("deferring review of " + rootObject);
                    }
                    /* note which permissions overrides to start from */
                    final String className = rootObject.subject.getClass().getName();
                    final Long id = rootObject.subject.getId();
                    if (rootObject.isCheckPermissions) {
                        permissionsOverrides.remove(className, id);
                    } else {
                        permissionsOverrides.put(className, id);
                    }
                    /* skip the review, start from this object in a later request */
                    return Collections.emptySet();
                } else {
                    /* do the review */
                    return graphPolicy.review(linkedFrom, rootObject, linkedTo, notNullable, isErrorRules);
                }
            }
        };
    }

    /**
     * Adjust an existing graph traversal policy so that for specific model objects permissions are not checked.
     * @param graphPolicy the graph policy to adjust
     * @param permissionsOverrides for which model objects permissions are not to be checked
     * @return the adjusted graph policy
     */
    public static GraphPolicy getSkipHeadPolicyPerform(final GraphPolicy graphPolicy,
            final SetMultimap<String, Long> permissionsOverrides) {
        return new BaseGraphPolicyAdjuster(graphPolicy) {
            @Override
            protected boolean isAdjustedBeforeReview(Details object) {
                if (object.isCheckPermissions &&
                        permissionsOverrides.containsEntry(object.subject.getClass().getName(), object.subject.getId())) {
                    object.isCheckPermissions = false;
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("preserving previous setting, making " + object);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        };
    }
}

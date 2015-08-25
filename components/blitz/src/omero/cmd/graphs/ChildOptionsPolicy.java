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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.services.graphs.GraphPathBean;
import ome.services.graphs.GraphPolicy;

/**
 * Adjust graph traversal policy for child objects according to their type and, if annotations, namespace.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class ChildOptionsPolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChildOptionsPolicy.class);

    /**
     * Adjust an existing graph traversal policy so that child objects may be included or excluded
     * regardless of if they are truly orphans.
     * @param graphPolicyToAdjust the graph policy to adjust
     * @param childOptions the child options that the policy adjustments are to effect
     * @param requiredPermissions the abilities that the user must have to operate upon an object for it to be included
     * @return the adjusted graph policy
     */
    public static GraphPolicy getChildOptionsPolicy(final GraphPolicy graphPolicyToAdjust,
            final Collection<ChildOptionI> childOptions, final Set<GraphPolicy.Ability> requiredPermissions) {

        if (CollectionUtils.isEmpty(childOptions)) {
            /* there are no adjustments to make to the policy */
            return graphPolicyToAdjust;
        }

        /* wrap the traversal policy so that the child options are effected */

        return new BaseGraphPolicyAdjuster(graphPolicyToAdjust) {
            private final Map<String, String> namespaceCache = new HashMap<String, String>();
            private final Map<Entry<String, Long>, String> objectNamespaces = new HashMap<Entry<String, Long>, String>();

            /**
             * Note each annotation's namespace.
             */
            @Override
            public void noteDetails(Session session, IObject object, String realClass, long id) {
                if (object instanceof Annotation) {
                    final String query = "SELECT ns FROM Annotation WHERE id = :id";
                    final String namespace = (String) session.createQuery(query).setParameter("id", id).uniqueResult();
                    if (namespace != null) {
                        String cachedNamespace = namespaceCache.get(namespace);
                        if (cachedNamespace == null) {
                            cachedNamespace = namespace;
                            namespaceCache.put(namespace, cachedNamespace);
                        }
                        objectNamespaces.put(Maps.immutableEntry(realClass, id), cachedNamespace);
                    }
                }
                super.noteDetails(session, object, realClass, id);
            }

            /**
             * Check if the given object is in the target annotation namespace.
             * @param childOption the child option whose target annotation namespace applies
             * @param object the object to check
             * @return if the annotation is in the target namespace or if the object is not an annotation
             */
            private boolean isTargetNamespace(ChildOptionI childOption, IObject object) {
                if (object instanceof Annotation) {
                    final Entry<String, Long> classAndId = Maps.immutableEntry(object.getClass().getName(), object.getId());
                    return childOption.isTargetNamespace(objectNamespaces.get(classAndId));
                } else {
                    return true;
                }
            }

            @Override
            protected boolean isAdjustedBeforeReview(Details object) {
                if (object.action == GraphPolicy.Action.EXCLUDE && object.orphan == GraphPolicy.Orphan.RELEVANT) {
                    /* the model object is [E]{r} */
                    for (final ChildOptionI childOption : childOptions) {
                        final Boolean isIncludeVerdict = childOption.isIncludeType(object.subject.getClass());
                        if (isIncludeVerdict == Boolean.TRUE && (requiredPermissions == null ||
                                Sets.difference(requiredPermissions, object.permissions).isEmpty()) &&
                                isTargetNamespace(childOption, object.subject)) {
                            object.orphan = GraphPolicy.Orphan.IS_LAST;
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("including all children of its type, so making " + object);
                            }
                            return true;
                        } else if (isIncludeVerdict == Boolean.FALSE && isTargetNamespace(childOption, object.subject)) {
                            object.orphan = GraphPolicy.Orphan.IS_NOT_LAST;
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("excluding all children of its type, so making " + object);
                            }
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }
}

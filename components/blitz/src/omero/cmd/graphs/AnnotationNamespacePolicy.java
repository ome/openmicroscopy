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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPolicy;

/**
 * Adjust graph traversal policy based on annotations' namespace.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */

public class AnnotationNamespacePolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationNamespacePolicy.class);

    /**
     * Adjust an existing graph traversal policy so that orphaned annotations will tend to be included only if their namespace is
     * appropriate. Namespaces may not be named in both {@code includeNamespaces} and {@code excludeNamespaces}.
     * @param graphPolicyToAdjust the graph policy to adjust
     * @param includeNamespaces only annotations in any of these namespaces may be deemed orphans,
     * ignored if empty or {@code null}
     * @param excludeNamespaces only annotations not in any of these namespaces may be deemed orphans,
     * ignored if empty or {@code null}
     * @return the adjusted graph policy
     */
    public static GraphPolicy getAnnotationNamespacePolicy(final GraphPolicy graphPolicyToAdjust,
            Collection<String> includeNamespaces, Collection<String> excludeNamespaces) {
        /* construct the predicate corresponding to the namespace restriction */
        final Predicate<String> isTargetNamespace;

        if (CollectionUtils.isEmpty(includeNamespaces)) {
            if (CollectionUtils.isEmpty(excludeNamespaces)) {
                return graphPolicyToAdjust;
            } else {
                final ImmutableSet<String> exclusions = ImmutableSet.copyOf(excludeNamespaces);
                isTargetNamespace = new Predicate<String>() {
                    @Override
                    public boolean apply(String namespace) {
                        return !exclusions.contains(namespace);
                    }
                };
            }
        } else {
            if (CollectionUtils.isEmpty(excludeNamespaces)) {
                final ImmutableSet<String> inclusions = ImmutableSet.copyOf(includeNamespaces);
                isTargetNamespace = new Predicate<String>() {
                    @Override
                    public boolean apply(String namespace) {
                        return inclusions.contains(namespace);
                    }
                };
            } else {
                throw new IllegalArgumentException("may not both include and exclude namespaces");
            }
        }

        /* wrap the traversal policy so that the namespace restriction is effected */
        return new GraphPolicy() {
            @Override
            public GraphPolicy getCleanInstance() {
                return new GraphPolicy() {
                    private final GraphPolicy graphPolicy = graphPolicyToAdjust.getCleanInstance();
                    private final Set<Long> targetAnnotations = new HashSet<Long>();

                    @Override
                    public void noteDetails(IObject object, String realClass, long id) {
                        if (object instanceof Annotation && isTargetNamespace.apply(((Annotation) object).getNs())) {
                            targetAnnotations.add(id);
                        }
                        graphPolicy.noteDetails(object, realClass, id);
                    }

                    @Override
                    public Set<Details> review(Map<String, Set<Details>> linkedFrom, Details rootObject,
                            Map<String, Set<Details>> linkedTo, Set<String> notNullable) throws GraphException {
                        final Set<Details> terms = new HashSet<Details>();
                        terms.add(rootObject);
                        for (final Map.Entry<String, Set<Details>> dataPerProperty : linkedFrom.entrySet()) {
                            terms.addAll(dataPerProperty.getValue());
                        }
                        for (final Map.Entry<String, Set<Details>> dataPerProperty : linkedTo.entrySet()) {
                            terms.addAll(dataPerProperty.getValue());
                        }
                        final Iterator<Details> termIterator = terms.iterator();
                        while (termIterator.hasNext()) {
                            final GraphPolicy.Details object = termIterator.next();
                            if (object.action == GraphPolicy.Action.EXCLUDE && object.orphan != GraphPolicy.Orphan.IS_NOT_LAST &&
                                    object.subject instanceof Annotation && !targetAnnotations.contains(object.subject.getId())) {
                                /* the annotation does not satisfy the predicate so it may not be deemed an orphan */
                                object.orphan = GraphPolicy.Orphan.IS_NOT_LAST;
                            } else {
                                /* not adjusting this term, so do not include in returned list of changes */
                                termIterator.remove();
                            }
                        }
                        if (!terms.isEmpty() && LOGGER.isDebugEnabled()) {
                            LOGGER.debug("not in target namespace, so making " + Joiner.on(", ").join(terms));
                        }
                        terms.addAll(graphPolicy.review(linkedFrom, rootObject, linkedTo, notNullable));
                        return terms;
                    }
                };
            }

            @Override
            public Set<Details> review(Map<String, Set<Details>> linkedFrom, Details rootObject, Map<String, Set<Details>> linkedTo,
                    Set<String> notNullable) throws GraphException {
                throw new RuntimeException("usable instances only from getCleanInstance method");
            }
        };
    }
}

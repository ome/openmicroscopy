/*
 * Copyright (C) 2013-2014 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

import ome.api.IQuery;
import ome.services.query.HierarchyNavigatorWrap;
import omero.cmd.Chgrp;
import omero.cmd.Delete;
import omero.cmd.DoAll;
import omero.cmd.GraphModify;
import omero.cmd.Helper;
import omero.cmd.Request;

/**
 * Preprocessors have a chance to modify the list of
 * {@link Request} instances which are passed to a
 * {@link DoAll}. If this strategy is continued, this
 * should be refactored behind a discoverable interface.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
@SuppressWarnings("deprecation")
public class Preprocessor {

    private final static Logger log = LoggerFactory.getLogger(Preprocessor.class);

    /**
     * The types of target for which we care about adjusting graph operation requests.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.0
     */
    public static enum TargetType {
        IMAGE, FILESET, DATASET, PROJECT, WELL, PLATE, SCREEN;

        /** lookup from initial-capital name to type, e.g. "/Image" to {@link TargetType#IMAGE} */
        public static final ImmutableMap<String, TargetType> byName;
        /** lookup from type to initial-capital name, e.g. {@link TargetType#IMAGE} to "/Image" */
        public static final ImmutableMap<TargetType, String> byType;

        static {
            final ImmutableMap.Builder<String, TargetType> byNameBuilder = ImmutableMap.builder();
            final ImmutableMap.Builder<TargetType, String> byTypeBuilder = ImmutableMap.builder();
            for (final TargetType value : TargetType.values()) {
                final String name = '/' + value.name().substring(0, 1) + value.name().substring(1).toLowerCase();
                byNameBuilder.put(name, value);
                byTypeBuilder.put(value, name);
            }
            byName = byNameBuilder.build();
            byType = byTypeBuilder.build();
        }
    }

    /**
     * A tuple of target type and target ID.
     * Instances with the same type and ID are {@link #equals(Object)} and have the same {@link #hashCode()}.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.0
     */
    public static class GraphModifyTarget {
        public final TargetType targetType;
        public final long targetId;

        /**
         * Construct a target type-ID tuple.
         * @param targetType the target type
         * @param targetId the target ID
         */
        public GraphModifyTarget(TargetType targetType, long targetId) {
            if (targetType == null) {
                throw new IllegalArgumentException("target type must be set");
            }
            this.targetType = targetType;
            this.targetId = targetId;
        }

        /**
         * Return only the targets of the given type.
         * @param targets some targets
         * @param targetType the desired type of target
         * @return the targets of the desired type
         */
        public static List<GraphModifyTarget> filterByType(Collection<GraphModifyTarget> targets, TargetType targetType) {
            final List<GraphModifyTarget> filtered = new ArrayList<GraphModifyTarget>();
            for (final GraphModifyTarget target : targets) {
                if (target.targetType == targetType) {
                    filtered.add(target);
                }
            }
            return filtered;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof GraphModifyTarget) {
                final GraphModifyTarget same = (GraphModifyTarget) o;
                return this.targetType == same.targetType && this.targetId == same.targetId;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder().append(this.targetType.ordinal()).append(this.targetId).toHashCode();
        }

        @Override
        public String toString() {
            return this.targetType + ":" + this.targetId;
        }
    }

    /** the target type hierarchy, an ordered list descending from higher to lower */
    protected static final ImmutableList<Entry<TargetType, TargetType>> targetTypeHierarchy;

    static {
        final com.google.common.collect.ImmutableList.Builder<Entry<TargetType, TargetType>> builder = ImmutableList.builder();
        builder.add(Maps.immutableEntry(TargetType.PROJECT, TargetType.DATASET));
        builder.add(Maps.immutableEntry(TargetType.DATASET, TargetType.IMAGE));
        builder.add(Maps.immutableEntry(TargetType.SCREEN,  TargetType.PLATE));
        builder.add(Maps.immutableEntry(TargetType.PLATE,   TargetType.WELL));
        builder.add(Maps.immutableEntry(TargetType.WELL,    TargetType.IMAGE));
        builder.add(Maps.immutableEntry(TargetType.FILESET, TargetType.IMAGE));
        targetTypeHierarchy = builder.build();
    }

    protected final List<Request> requests;

    protected final HierarchyNavigatorWrap<TargetType, GraphModifyTarget> hierarchyNavigator;

    /**
     * Transform the list of requests.
     * A prohibited prefix must be in the list, and the first must precede any prohibited suffixes.
     * @param isRelevant which type of graph operation's requests to transform
     * @param newRequestTarget the target of the new request to create to replace the removed ones
     * @param prohibitedPrefixes the requests that may not precede the new request
     * @param prohibitedSuffixes the requests that may be omitted after the new request
     */
    protected final void transform(Predicate<Request> isRelevant, GraphModifyTarget newRequestTarget,
            Set<GraphModifyTarget> prohibitedPrefixes, Set<GraphModifyTarget> prohibitedSuffixes) {

        boolean added = false;

        for (int index = this.requests.size() - 1; index >= 0; index--) {

            if (!isRelevant.apply(requests.get(index))) {
                continue;
            }

            final GraphModify request = (GraphModify) this.requests.get(index);
            final TargetType targetType = TargetType.byName.get(request.type);
            if (targetType == null) {
                continue;
            }

            /* it is a relevant request with an understood target type */
            final GraphModifyTarget requestTarget = new GraphModifyTarget(targetType, request.id);
            /* must we now prefix it? */
            if (!added && prohibitedPrefixes.contains(requestTarget)) {
                added = true;
                /* FIXME: this does not modify user-set options */
                final GraphModify newRequest = 
                        (GraphModify) ((IGraphModifyRequest) request).copy();
                newRequest.type = TargetType.byType.get(newRequestTarget.targetType);
                newRequest.id = newRequestTarget.targetId;
                this.requests.add(index+1, newRequest);
            }
            /* must we remove it? */
            if (prohibitedSuffixes.contains(requestTarget)) {
                if (!added) {
                    throw new IllegalArgumentException("some prohibited prefix must occur before any prohibited suffixes");
                }
                this.requests.remove(index);
            }
        }
        if (!added) {
            throw new IllegalArgumentException("no prohibited prefix is among the requests");
        }
    }

    public Preprocessor(List<Request> requests, Helper helper) {
        final IQuery iQuery = helper.getServiceFactory().getQueryService();

        this.hierarchyNavigator = new HierarchyNavigatorWrap<TargetType, GraphModifyTarget>(iQuery) {
            @Override
            protected String typeToString(TargetType type) {
                return TargetType.byType.get(type);
            }

            @Override
            protected TargetType stringToType(String typeName) {
                return TargetType.byName.get(typeName);
            }

            @Override
            protected Entry<String, Long> entityToStringLong(GraphModifyTarget entity) {
                return Maps.immutableEntry(TargetType.byType.get(entity.targetType), entity.targetId);
            }

            @Override
            protected GraphModifyTarget stringLongToEntity(String typeName, long id) {
                return new GraphModifyTarget(TargetType.byName.get(typeName), id);
            }
        };

        this.requests = requests;

        process();
    }

    public Preprocessor(List<Request> requests, HierarchyNavigatorWrap<TargetType, GraphModifyTarget> hierarchyNavigator) {
        this.hierarchyNavigator = hierarchyNavigator;
        this.requests = requests;

        process();
    }

    /**
     * Returns a copy of the requests field or an empty list if null.
     */
    public List<Request> getRequests() {
        if (requests == null) {
            return new ArrayList<Request>();
        }
        return new ArrayList<Request>(requests);
    }

    /**
     * Recursively find all the direct and indirect containers of the given target.
     * @param contained a target
     * @return all the target's containers
     */
    private Set<GraphModifyTarget> getAllContainers(GraphModifyTarget contained) {
        final Set<GraphModifyTarget> allContainers    = new HashSet<GraphModifyTarget>();
        final Set<GraphModifyTarget> pendingContained = new HashSet<GraphModifyTarget>();
        pendingContained.add(contained);
        while (!pendingContained.isEmpty()) {
            final Iterator<GraphModifyTarget> pendingContainedsIterator = pendingContained.iterator();
            final GraphModifyTarget nextContained = pendingContainedsIterator.next();
            pendingContainedsIterator.remove();
            final Set<GraphModifyTarget> nextContainers = new HashSet<GraphModifyTarget>();
            for (final Entry<TargetType, TargetType> relationship : targetTypeHierarchy) {
                if (relationship.getValue() == nextContained.targetType) {
                    nextContainers.addAll(hierarchyNavigator.doLookup(relationship.getKey(), nextContained));
                }
            }
            allContainers.addAll(nextContainers);
            pendingContained.addAll(nextContainers);
        }
        return allContainers;
    }

    /**
     * Generate predicates where each identifies a distinct group of requests that should be processed together.
     * @return the predicates to guide request list processing
     */
    private Collection<Predicate<Request>> predicatesForRequests() {
        final Set<Long> chgrpRequestGroups = new HashSet<Long>();
        boolean deleteRequest = false;

        final List<Predicate<Request>> predicates = new ArrayList<Predicate<Request>>();
        for (final Request request : this.requests) {
            if (!(request instanceof GraphModify) || ((GraphModify) request).type == null) {
                // do nothing
            } else if (request instanceof Delete && !deleteRequest) {
                deleteRequest = true;
                predicates.add(new Predicate<Request>() {
                    public boolean apply(Request request) {
                        return Delete.class.isAssignableFrom(request.getClass()) &&
                                TargetType.byName.get(((GraphModify) request).type) != null;
                    }
                });
            } else if (request instanceof Chgrp) {
                final long targetGroup = ((Chgrp) request).grp;
                if (chgrpRequestGroups.add(targetGroup)) {
                    predicates.add(new Predicate<Request>() {
                        public boolean apply(Request request) {
                            return Chgrp.class.isAssignableFrom(request.getClass()) &&
                                    ((Chgrp) request).grp == targetGroup &&
                                    TargetType.byName.get(((GraphModify) request).type) != null;
                        }
                    });
                }
            }
        }
        return predicates;
    }

    /**
     * Preprocess the list of requests.
     */
    protected void process() {
        boolean modified = false;
        for (final Predicate<Request> isRelevant : predicatesForRequests()) {
            final SetMultimap<TargetType, GraphModifyTarget> targets = HashMultimap.create();

            /* direct references */

            for (final Request relevantRequest : Collections2.filter(this.requests, isRelevant)) {
                final GraphModify gm = (GraphModify) relevantRequest;
                final TargetType targetType = TargetType.byName.get(gm.type);
                targets.put(targetType, new GraphModifyTarget(targetType, gm.id));
            }

            /* indirect references */

            for (final Entry<TargetType, TargetType> relationship : Preprocessor.targetTypeHierarchy) {
                final TargetType containerType = relationship.getKey();
                final TargetType containedType = relationship.getValue();
                final Set<GraphModifyTarget> containers = targets.get(containerType);
                this.hierarchyNavigator.prepareLookups(containedType, containers);
                for (final GraphModifyTarget container : containers) {
                    targets.putAll(containedType, this.hierarchyNavigator.doLookup(containedType, container));
                }
            }

            /* review the referenced FS images */
            this.hierarchyNavigator.prepareLookups(TargetType.FILESET, targets.get(TargetType.IMAGE));
            while (!targets.get(TargetType.IMAGE).isEmpty()) {
                final GraphModifyTarget image = targets.get(TargetType.IMAGE).iterator().next();
                /* find the image's fileset */
                final Set<GraphModifyTarget> containers = this.hierarchyNavigator.doLookup(TargetType.FILESET, image);
                final Iterator<GraphModifyTarget> filesetIterator =
                        GraphModifyTarget.filterByType(containers, TargetType.FILESET).iterator();
                final GraphModifyTarget fileset;
                if (!filesetIterator.hasNext()) {
                    /* pre-FS image */
                    targets.get(TargetType.IMAGE).remove(image);
                    continue;
                }
                fileset = filesetIterator.next();
                if (filesetIterator.hasNext()) {
                    throw new IllegalStateException("image is contained in multiple filesets");
                }
                /* check that all the fileset's images are referenced */
                final Set<GraphModifyTarget> filesetImages = this.hierarchyNavigator.doLookup(TargetType.IMAGE, fileset);
                final boolean completeFileset = targets.get(TargetType.IMAGE).containsAll(filesetImages);
                /* this iteration will handle this fileset sufficiently, so do not revisit it */
                targets.get(TargetType.IMAGE).removeAll(filesetImages);
                /* this preprocessing is applied only when all of a fileset's images are referenced */
                if (!(completeFileset)) {
                    continue;
                }
                /* okay, list the fileset as a target among the requests, after all of its images and their containers */
                final Set<GraphModifyTarget> prohibitedPrefixes = new HashSet<GraphModifyTarget>(filesetImages);
                for (final GraphModifyTarget filesetImage : filesetImages) {
                    prohibitedPrefixes.addAll(getAllContainers(filesetImage));
                }
                /* and, once listed, neither the fileset nor its images need subsequent requests */
                final Set<GraphModifyTarget> prohibitedSuffixes = new HashSet<GraphModifyTarget>(filesetImages);
                prohibitedSuffixes.add(fileset);
                /* adjust the list of requests accordingly */
                transform(isRelevant, fileset, prohibitedPrefixes, prohibitedSuffixes);
                modified = true;
            }
        }

        if (modified && log.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append(requestToString(requests.get(0)));
            for (int i = 1; i < requests.size(); i++) {
                sb.append(",");
                sb.append(requestToString(requests.get(i)));
            }
            log.debug("transformed to: {}", sb);
        }
    }


    /**
     * Convert a single request to a pretty string.
     */
    protected String requestToString(final Request request) {
        StringBuilder requestString = new StringBuilder();
        if (request instanceof Delete) {
            requestString.append("DELETE");
        } else if (request instanceof Chgrp) {
            requestString.append("CHGRP");
            requestString.append('(');
            requestString.append(((Chgrp) request).grp);
            requestString.append(')');
        } else {
            requestString.append('?');
        }
        if (request instanceof GraphModify) {
            final GraphModify graphModify = (GraphModify) request;
            requestString.append('[');
            requestString.append(graphModify.type);
            requestString.append(':');
            requestString.append(graphModify.id);
            requestString.append(']');
        }
        return requestString.toString();
    }
}

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Session;

import ome.model.core.OriginalFile;
import ome.model.internal.Details;
import ome.services.graphs.GraphException;
import ome.services.graphs.GraphPolicy.Ability;
import ome.services.graphs.GraphTraversal.Processor;
import ome.services.graphs.GraphOpts.Op;
import ome.services.graphs.ModelObjectSequencer;
import omero.cmd.GraphModify2;
import omero.cmd.Request;

import com.google.common.base.Splitter;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

/**
 * Static utility methods for model graph operations.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.0
 */
public class GraphUtil {
    /**
     * Split a list of strings by a given separator, trimming whitespace and ignoring empty items.
     * @param separator the separator between the list items
     * @param list the list
     * @return a means of iterating over the list items
     */
    private static Iterable<String> splitList(char separator, String list) {
        return Splitter.on(separator).trimResults().omitEmptyStrings().split(list);
    }

    /**
     * Copy the {@link GraphModify2} fields of one request to another.
     * @param requestFrom the source of the field copy
     * @param requestTo the target of the field copy
     */
    static void copyFields(GraphModify2 requestFrom, GraphModify2 requestTo) {
        if (requestFrom.targetObjects == null) {
            requestTo.targetObjects = null;
        } else {
            requestTo.targetObjects = new HashMap<String, List<Long>>();
            for (final Map.Entry<String, List<Long>> targetObjectsOneClass : requestFrom.targetObjects.entrySet()) {
                final String targetClass = targetObjectsOneClass.getKey();
                final List<Long> targetIds = targetObjectsOneClass.getValue();
                requestTo.targetObjects.put(targetClass, new ArrayList<Long>(targetIds));
            }
        }
        if (requestFrom.childOptions == null) {
            requestTo.childOptions = null;
        } else {
            requestTo.childOptions = new ArrayList<ChildOption>(requestFrom.childOptions);
        }
        requestTo.dryRun = requestFrom.dryRun;
    }

    /**
     * Approximately translate {@link GraphModify} options in setting the parameters of a {@link GraphModify2} request.
     * @param graphRequestFactory a means of instantiating new child options
     * @param options {@link GraphModify} options, may be {@code null}
     * @param request the request whose options should be updated
     * @param isAdmin if the current user is a system administrator
     * @throws GraphException if a non-administrator attempted to use {@link Op#FORCE}
     * @deprecated because facade classes are deprecated
     */
    @Deprecated
    static void translateOptions(GraphRequestFactory graphRequestFactory, Map<String, String> options,
            GraphModify2 request, boolean isAdmin) throws GraphException {
        if (options == null) {
            return;
        }
        final List<ChildOption> childOptions = new ArrayList<ChildOption>(options.size());
        for (final Map.Entry<String, String> option : options.entrySet()) {
            final ChildOption childOption = graphRequestFactory.createChildOption();
            /* find type to which options apply */
            String optionType = option.getKey();
            if (optionType.charAt(0) == '/') {
                optionType = optionType.substring(1);
            }
            boolean notedType = false;
            for (final String optionValue : GraphUtil.splitList(';', option.getValue())) {
                /* approximately translate each option */
                if (Op.KEEP.toString().equals(optionValue)) {
                    childOption.excludeType = Collections.singletonList(optionType);
                    notedType = true;
                } else if (Op.HARD.toString().equals(optionValue)) {
                    childOption.includeType = Collections.singletonList(optionType);
                    notedType = true;
                } else if (Op.FORCE.toString().equals(optionValue)) {
                    if (!isAdmin) {
                        throw new GraphException("only administrators may specify " + Op.FORCE);
                    }
                } else if (optionValue.startsWith("excludes=")) {
                    if (childOption.excludeNs == null) {
                        childOption.excludeNs = new ArrayList<String>();
                    }
                    for (final String namespace : GraphUtil.splitList(',', optionValue.substring(9))) {
                        childOption.excludeNs.add(namespace);
                    }
                }
            }
            /* each child option must apply to specific types */
            if (notedType) {
                childOptions.add(childOption);
            }
        }
        request.childOptions = childOptions.isEmpty() ? null : childOptions;
    }

    /**
     * Make a copy of a multimap with the full class names in the keys replaced by the simple class names
     * and the ordering of the values preserved.
     * @param entriesByFullName a multimap
     * @return a new multimap with the same contents, except for the package name having been trimmed off each key
     */
    static <X> SetMultimap<String, X> trimPackageNames(SetMultimap<String, X> entriesByFullName) {
        final SetMultimap<String, X> entriesBySimpleName = LinkedHashMultimap.create();
        for (final Map.Entry<String, Collection<X>> entriesForOneClass : entriesByFullName.asMap().entrySet()) {
            final String fullClassName = entriesForOneClass.getKey();
            final String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
            final Collection<X> values = entriesForOneClass.getValue();
            entriesBySimpleName.putAll(simpleClassName, values);
        }
        return entriesBySimpleName;
    }

    /**
     * Find the first class-name in a {@code /}-separated string.
     * @param type a type path in the style of the original graph traversal code
     * @return the first type found in the path
     * @deprecated because facade classes are deprecated
     */
    @Deprecated
    static String getFirstClassName(String type) {
        while (type.charAt(0) == '/') {
            type = type.substring(1);
        }
        final int firstSlash = type.indexOf('/');
        if (firstSlash > 0) {
            type = type.substring(0, firstSlash);
        }
        return type;
    }

    /**
     * Combine consecutive facade requests with the same options into one request with the union of the target model objects.
     * Does not adjust {@link GraphModify2} requests because they already allow the caller to specify multiple target model objects
     * should they wish those objects to be processed together.
     * Call this method before calling {@link omero.cmd.IRequest#init(omero.cmd.Helper)} on the requests.
     * @param requests the list of requests to adjust
     * @deprecated because facade classes are deprecated
     */
    @Deprecated
    public static void combineFacadeRequests(List<Request> requests) {
        if (requests == null) {
            return;
        }
        int index = 0;
        while (index < requests.size() - 1) {
            final Request request1 = requests.get(index);
            final Request request2 = requests.get(index + 1);
            final boolean isCombined;
            if (request1 instanceof ChgrpFacadeI && request2 instanceof ChgrpFacadeI) {
                isCombined = isCombined((ChgrpFacadeI) request1, (ChgrpFacadeI) request2);
            } else if (request1 instanceof ChownFacadeI && request2 instanceof ChownFacadeI) {
                    isCombined = isCombined((ChownFacadeI) request1, (ChownFacadeI) request2);
            } else if (request1 instanceof DeleteFacadeI && request2 instanceof DeleteFacadeI) {
                isCombined = isCombined((DeleteFacadeI) request1, (DeleteFacadeI) request2);
            } else {
                isCombined = false;
            }
            if (isCombined) {
                requests.remove(index + 1);
            } else {
                index++;
            }
        }
    }

    /**
     * Test if the maps have the same contents, regardless of ordering.
     * {@code null} arguments are taken as being empty maps.
     * @param map1 the first map
     * @param map2 the second map
     * @return if the two maps have the same contents
     */
    private static <K, V> boolean isEqualMaps(Map<K, V> map1, Map<K, V> map2) {
        if (map1 == null) {
            map1 = Collections.emptyMap();
        }        
        if (map2 == null) {
            map2 = Collections.emptyMap();
        }
        return CollectionUtils.isEqualCollection(map1.entrySet(), map2.entrySet());
    }

    /**
     * Combine the two chgrp requests should they be sufficiently similar.
     * @param chgrp1 the first request
     * @param chgrp2 the second request
     * @return if the target model object of the second request was successfully merged into those of the first request
     */
    private static boolean isCombined(ChgrpFacadeI chgrp1, ChgrpFacadeI chgrp2) {
        if (isEqualMaps(chgrp1.options, chgrp2.options) &&
            chgrp1.grp == chgrp2.grp) {
            chgrp1.addToTargets(chgrp2.type, chgrp2.id);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Combine the two chown requests should they be sufficiently similar.
     * @param chown1 the first request
     * @param chown2 the second request
     * @return if the target model object of the second request was successfully merged into those of the first request
     */
    private static boolean isCombined(ChownFacadeI chown1, ChownFacadeI chown2) {
        if (isEqualMaps(chown1.options, chown2.options) &&
            chown1.user == chown2.user) {
            chown1.addToTargets(chown2.type, chown2.id);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Combine the two delete requests should they be sufficiently similar.
     * @param delete1 the first request
     * @param delete2 the second request
     * @return if the target model object of the second request was successfully merged into those of the first request
     */
    private static boolean isCombined(DeleteFacadeI delete1, DeleteFacadeI delete2) {
        /* in deleting original files, order is significant */
        if (isEqualMaps(delete1.options, delete2.options)) {
            delete1.addToTargets(delete2.type, delete2.id);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Rearrange the deletion targets such that original files are listed before their containing directories.
     * @param session the Hibernate session
     * @param targetObjects the objects that are to be deleted
     * @return the given target objects with any original files suitably ordered for deletion
     */
    static SetMultimap<String, Long> arrangeDeletionTargets(Session session, SetMultimap<String, Long> targetObjects) {
        if (targetObjects.get(OriginalFile.class.getName()).size() < 2) {
            /* no need to rearrange anything, as there are not multiple original files */
            return targetObjects;
        }
        final SetMultimap<String, Long> orderedIds = LinkedHashMultimap.create();
        for (final Map.Entry<String, Collection<Long>> targetObjectsByClass : targetObjects.asMap().entrySet()) {
            final String className = targetObjectsByClass.getKey();
            Collection<Long> ids = targetObjectsByClass.getValue();
            if (OriginalFile.class.getName().equals(className)) {
                final Collection<Collection<Long>> sortedIds = ModelObjectSequencer.sortOriginalFileIds(session, ids);
                ids = new ArrayList<Long>(ids.size());
                for (final Collection<Long> idBatch : sortedIds) {
                    ids.addAll(idBatch);
                }
            }
            orderedIds.putAll(className, ids);
        }
        return orderedIds;
    }

    /**
     * Wrap a graph traversal processor so that it has no write effects.
     * @param processor a graph traversal processor to wrap
     * @return the graph traversal processor wrapped so that it has no write effects
     */
    static Processor disableProcessor(final Processor processor) {
        return new Processor() {

            @Override
            public void nullProperties(String className, String propertyName, Collection<Long> ids) {
                /* disable this write action */
            }

            @Override
            public void deleteInstances(String className, Collection<Long> ids) {
                /* disable this write action */
            }

            @Override
            public void processInstances(String className, Collection<Long> ids) {
                /* disable this write action */
            }

            @Override
            public Set<Ability> getRequiredPermissions() {
                return processor.getRequiredPermissions();
            }

            @Override
            public void assertMayProcess(String className, long id, Details details) throws GraphException {
                processor.assertMayProcess(className, id, details);
            }
        };
    }
}

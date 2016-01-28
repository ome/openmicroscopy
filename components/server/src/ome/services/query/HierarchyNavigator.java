/*
 * Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
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

package ome.services.query;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import ome.api.IQuery;
import ome.parameters.Parameters;

/**
 * Query the database for relationships between model objects.
 * Caches results, so designed for a short lifetime.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
public class HierarchyNavigator {
    /* This class and {@link HierarchyWrap} are designed to make it easy to adjust the Java types
     * via which the model object hierarchy is navigated, and to make the HQL queries efficient
     * (batching, caching), at the small expense of constructing instances of simple Java objects.
     * The methods are not public to avoid polluting users of subclasses of {@link HierarchyWrap}.
     */

    /** HQL queries to map from ID of first target type to that of the second */
    private static final ImmutableMap<Map.Entry<String, String>, String> hqlFromTo;

    static {
        /* note that there is not yet any treatment of /PlateAcquisition or /WellSample */
        final Builder<Map.Entry<String, String>, String> builder = ImmutableMap.builder();
        builder.put(Maps.immutableEntry("/Project", "/Dataset"),
                "SELECT parent.id, child.id FROM ProjectDatasetLink WHERE parent.id IN (:" + Parameters.IDS + ")");
        builder.put(Maps.immutableEntry("/Dataset", "/Image"),
                "SELECT parent.id, child.id FROM DatasetImageLink WHERE parent.id IN (:" + Parameters.IDS + ")");
        builder.put(Maps.immutableEntry("/Screen", "/Plate"),
                "SELECT parent.id, child.id FROM ScreenPlateLink WHERE parent.id IN (:" + Parameters.IDS + ")");
        builder.put(Maps.immutableEntry("/Plate", "/Well"),
                "SELECT plate.id, id FROM Well WHERE plate.id IN (:" + Parameters.IDS + ")");
        builder.put(Maps.immutableEntry("/Well", "/Image"),
                "SELECT well.id, image.id FROM WellSample WHERE well.id IN (:" + Parameters.IDS + ")");
        builder.put(Maps.immutableEntry("/Fileset", "/Image"),
                "SELECT fileset.id, id FROM Image WHERE fileset.id IN (:" + Parameters.IDS + ")");
        builder.put(Maps.immutableEntry("/Image", "/Fileset"),
                "SELECT id, fileset.id FROM Image WHERE fileset.id IS NOT NULL AND id IN (:" + Parameters.IDS + ")");
        builder.put(Maps.immutableEntry("/Image", "/Well"),
                "SELECT image.id, well.id FROM WellSample WHERE image.id IN (:" + Parameters.IDS + ")");
        builder.put(Maps.immutableEntry("/Well", "/Plate"),
                "SELECT id, plate.id FROM Well WHERE id IN (:" + Parameters.IDS + ")");
        builder.put(Maps.immutableEntry("/Plate", "/Screen"),
                "SELECT child.id, parent.id FROM ScreenPlateLink WHERE child.id IN (:" + Parameters.IDS + ")");
        builder.put(Maps.immutableEntry("/Image", "/Dataset"),
                "SELECT child.id, parent.id FROM DatasetImageLink WHERE child.id IN (:" + Parameters.IDS + ")");
        builder.put(Maps.immutableEntry("/Dataset", "/Project"),
                "SELECT child.id, parent.id FROM ProjectDatasetLink WHERE child.id IN (:" + Parameters.IDS + ")");
        hqlFromTo = builder.build();
    }

    /** available query service */
    protected final IQuery iQuery;

    /** cache of query results */
    private final ModelObjectCache cache = new ModelObjectCache();

    /**
     * Construct a new hierarchy navigator.
     * @param iQuery the query service
     */
    protected HierarchyNavigator(IQuery iQuery) {
        this.iQuery = iQuery;
    }

    /**
     * Perform the database query to discover the IDs of the related objects.
     * @param toType the type of the objects to which the query object may be related, not <code>null</code>
     * @param fromType the query object's type, not <code>null</code>
     * @param fromIds the query objects' database IDs, none <code>null</code>
     * @return pairs of database IDs: of the query object, and an object to which it relates
     */
    private List<Object[]> doQuery(String toType, String fromType, Collection<Long> fromIds) {
        final String queryString = hqlFromTo.get(Maps.immutableEntry(fromType, toType));
        if (queryString == null) {
            throw new IllegalArgumentException("not implemented for " + fromType + " to " + toType);
        }
        return this.iQuery.projection(queryString, new Parameters().addIds(fromIds));
    }

    /**
     * Batch bulk database queries to prime the cache for {@link #doLookup(String, String, Long)}.
     * It is not necessary to call this method, but it is advised if many lookups are anticipated.
     * @param toType the type of the objects to which the query objects may be related, not <code>null</code>
     * @param fromType the query object's type, not <code>null</code>
     * @param fromIds the query objects' database IDs, none <code>null</code>
     */
    protected void prepareLookups(String toType, String fromType, Collection<Long> fromIds) {
        /* note which query object IDs have not already had results cached */
        final Set<Long> fromIdsToQuery = new HashSet<Long>(fromIds);
        for (final long fromId : fromIds) {
            if (cache.getFromCache(fromType, fromId, toType) != null) {
                fromIdsToQuery.remove(fromId);
            }
        }
        if (fromIdsToQuery.isEmpty()) {
            /* ... all of them are already cached */
            return;
        }
        /* collate the results from multiple batches */
        final SetMultimap<Long, Long> fromIdsToIds = HashMultimap.create();
        for (final List<Long> fromIdsToQueryBatch : Iterables.partition(fromIdsToQuery, 256)) {
            for (final Object[] queryResult : doQuery(toType, fromType, fromIdsToQueryBatch)) {
                fromIdsToIds.put((Long) queryResult[0], (Long) queryResult[1]);
            }
        }
        /* cache the results by query object */
        for (final Entry<Long, Collection<Long>> fromIdToIds : fromIdsToIds.asMap().entrySet()) {
            cache.putIntoCache(fromType, fromIdToIds.getKey(), toType, ImmutableSet.copyOf(fromIdToIds.getValue()));
        }
        /* note empty results so that the database is not again queried */
        for (final Long fromId : Sets.difference(fromIdsToQuery, fromIdsToIds.keySet())) {
            cache.putIntoCache(fromType, fromId, toType, ImmutableSet.<Long>of());
        }
    }

    /**
     * Look up which objects of a given type relate to the given query object.
     * Caches results, and one may bulk-cache results in advance using {@link #prepareLookups(String, String, Collection)}.
     * @param toType the type of the objects to which the query object may be related, not <code>null</code>
     * @param fromType the query object's type, not <code>null</code>
     * @param fromId the query object's database ID, not <code>null</code>
     * @return the related objects' database IDs, never <code>null</code>
     */
    protected ImmutableSet<Long> doLookup(String toType, String fromType, Long fromId) {
        final ImmutableSet<Long> result = cache.getFromCache(fromType, fromId, toType);
        if (result == null) {
            /* cache miss, so query the single object */
            final ImmutableSet.Builder<Long> toIdsBuilder = ImmutableSet.builder();
            for (final Object[] queryResult : doQuery(toType, fromType, Collections.singleton(fromId))) {
                toIdsBuilder.add((Long) queryResult[1]);
            }
            final ImmutableSet<Long> toIds = toIdsBuilder.build();
            cache.putIntoCache(fromType, fromId, toType, toIds);
            return toIds;
        } else {
            /* cache hit */
            return result;
        }
    }
}

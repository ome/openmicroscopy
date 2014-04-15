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

package ome.services.query;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;

import ome.api.IQuery;

/**
 * Convenience class for creating versions of {@link HierarchyNavigator} with different model object representations.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
public abstract class HierarchyNavigatorWrap<T, E> extends HierarchyNavigator {
    public HierarchyNavigatorWrap(IQuery iQuery) {
        super(iQuery);
    }

    /**
     * Convert the given object type to the type strings expected by {@link HierarchyNavigator}.
     * @param type an object type
     * @return the corresponding type string
     */
    protected abstract String typeToString(T type);

    /**
     * Convert the given type string to the object type.
     * @param typeName a type string
     * @return the corresponding object type
     */
    protected abstract T stringToType(String typeName);

    /**
     * Convert a model object to the type string and database ID expected by {@link HierarchyNavigator}.
     * @param entity a model object
     * @return the corresponding type string and database ID
     */
    protected abstract Map.Entry<String, Long> entityToStringLong(E entity);

    /**
     * Convert the given type string and database ID to the model object.
     * @param typeName a type string
     * @param id a database ID
     * @return the corresponding model object
     */
    protected abstract E stringLongToEntity(String typeName, long id);

    /**
     * Batch bulk database queries to prime the cache for {@link #doLookup(Object, Object)}.
     * It is not necessary to call this method, but it is advised if many lookups are anticipated.
     * Wraps {@link HierarchyNavigator#prepareLookups(String, String, Collection)}.
     * @param toType the type of the objects to which the query objects may be related, not <code>null</code>
     * @param from the query objects, none <code>null</code>, may be of differing types
     */

    public void prepareLookups(T toType, Collection<E> from) {
        final String toTypeAsString = typeToString(toType);
        final SetMultimap<String, Long> fromIdsByType = HashMultimap.create();
        for (final E entity : from) {
            final Map.Entry<String, Long> fromAsStringLong = entityToStringLong(entity);
            fromIdsByType.put(fromAsStringLong.getKey(), fromAsStringLong.getValue());
        }
        for (final String fromTypeAsString : fromIdsByType.keySet()) {
            final Set<Long> fromIdsAsLongs = fromIdsByType.get(fromTypeAsString);
            prepareLookups(toTypeAsString, fromTypeAsString, fromIdsAsLongs);
        }
    }

    /**
     * Look up which objects of a given type relate to the given query object.
     * Caches results, and one may bulk-cache results in advance using {@link #prepareLookups(Object, Collection)}.
     * Wraps {@link HierarchyNavigator#doLookup(String, String, Long)}.
     * @param toType the type of the objects to which the query object may be related, not <code>null</code>
     * @param from the query object, not <code>null</code>
     * @return the related objects, never <code>null</code>
     */
    public ImmutableSet<E> doLookup(T toType, E from) {
        final String toTypeAsString = typeToString(toType);
        final Map.Entry<String, Long> fromAsStringLong = entityToStringLong(from);
        final ImmutableSet.Builder<E> to = ImmutableSet.builder();
        for (final Long toIdAsLong : doLookup(toTypeAsString, fromAsStringLong.getKey(), fromAsStringLong.getValue())) {
            to.add(stringLongToEntity(toTypeAsString, toIdAsLong));
        }
        return to.build();
    }
}

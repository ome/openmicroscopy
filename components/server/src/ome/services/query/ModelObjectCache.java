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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.collect.ImmutableSet;

/**
 * Simple cache of lookups of which objects of a given type relate to query objects.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
class ModelObjectCache {
    private static class Lookup {
        final String fromType;
        final long fromId;
        final String toType;

        /**
         * A lookup of which objects of a given type relate to a query object.
         * It is assumed that there are not enormously many different object types.
         * @param fromType the query object's type, not <code>null</code>
         * @param fromId the query object's database ID
         * @param toType the type of the objects to which the query object may be related, not <code>null</code>
         */
        private Lookup(String fromType, long fromId, String toType) {
            if (fromType == null || toType == null) {
                throw new IllegalArgumentException(new NullPointerException());
            }
            this.fromType = fromType.intern();
            this.fromId = fromId;
            this.toType = toType.intern();
        }

        /**
         * {@inheritDoc}
         * Different instances are equal if their fields are equal.
         */
        @Override
        public boolean equals(Object object) {
            if (object instanceof Lookup) {
                final Lookup lookup = (Lookup) object;
                /* note that the constructor intern()'d the type values */
                return this.fromType == lookup.fromType &&
                       this.fromId   == lookup.fromId &&
                       this.toType   == lookup.toType;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                .append(this.fromType)
                .append(this.fromId)
                .append(this.toType)
                .toHashCode();
        }
    }

    private final Map<Lookup, ImmutableSet<Long>> lookupCache = new HashMap<Lookup, ImmutableSet<Long>>();

    /**
     * Retrieve related objects from the cache.
     * @param fromType the query object's type, not <code>null</code>
     * @param fromId the query object's database ID
     * @param toType the type of the objects to which the query object may be related, not <code>null</code>
     * @return the related objects, or <code>null</code> for a cache miss
     */
    ImmutableSet<Long> getFromCache(String fromType, Long fromId, String toType) {
        return lookupCache.get(new Lookup(fromType, fromId, toType));
    }

    /**
     * Insert related objects into the cache.
     * Wholly replaces any previous value.
     * @param fromType the query object's type, not <code>null</code>
     * @param fromId the query object's database ID
     * @param toType the type of the objects to which the query object may be related, not <code>null</code>
     * @param toIds the related objects
     */
    void putIntoCache(String fromType, Long fromId, String toType, ImmutableSet<Long> toIds) {
        lookupCache.put(new Lookup(fromType, fromId, toType), toIds);
    }
}

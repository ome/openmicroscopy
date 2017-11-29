/*
 * Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
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

package ome.model.internal;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ArrayUtils;

/**
 * Thread-safe memory-sensitive cache of Boolean arrays. They are expected to be <em>immutable</em>.
 * Shorter arrays are considered identical to longer arrays with the higher indices set to {@code false}.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.4.2
 */
class BooleanArrayCache {

    /* can be resurrected by getArrayFor */
    private SoftReference<? extends Map<Long, boolean[]>> cacheRef = new SoftReference<>(new ConcurrentHashMap<Long, boolean[]>());

    /**
     * Calculate a {@code long} that corresponds to the bit pattern of the array.
     * <code>numberForArray(new boolean[] {false, true, false, false}) == 2</code>
     * @param array the array for which to calculate a number
     * @return the number for the array's bit pattern
     */
    private static long numberForArray(boolean[] array) {
        if (array.length > Long.SIZE) {
            throw new IllegalArgumentException("array may have no more elements than " + Long.SIZE);
        }
        long number = 0;
        long nextBit = 1;
        for (final boolean bit : array) {
            if (bit) {
                number |= nextBit;
            }
            nextBit <<= 1;
        }
        return number;
    }

    /**
     * Return an array with the same {@code true} indices as the given array.
     * If passed {@code null} then returns an empty array.
     * @param array an array
     * @return a corresponding cached array, may be the same as given
     */
    boolean[] getArrayFor(boolean[] array) {
        if (array == null) {
            array = ArrayUtils.EMPTY_BOOLEAN_ARRAY;
        }
        final long number = numberForArray(array);
        Map<Long, boolean[]> cache = cacheRef.get();
        if (cache == null) {
            /* if the old cache grew too large then the garbage collector must have needed the space */
            cache = new ConcurrentHashMap<Long, boolean[]>();
            cacheRef = new SoftReference<>(cache);
        }
        boolean[] cached = cache.get(number);
        if (cached == null) {
            cache.put(number, array);
            return array;
        } else {
            if (numberForArray(cached) != number) {
                throw new IllegalStateException("cache violation");
            }
            return cached;
        }
    }

    /**
     * Provides a method for transforming a Boolean array.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.4.2
     */
    interface Transformer {
        boolean[] transform(boolean[] array);
    }

    /**
     * Transforms the given array using the given transformer.
     * If passed {@code null} then passes the transformer an empty array.
     * The transformed array is passed through {@link #getArrayFor(boolean[])} before being returned.
     * @param transformer an array transform
     * @param array an array to transform
     * @return the transformed array
     */
    public boolean[] transform(Transformer transformer, boolean[] array) {
        if (array == null) {
            array = ArrayUtils.EMPTY_BOOLEAN_ARRAY;
        } else if (array.length > 0) {
            array = Arrays.copyOf(array, array.length);
        }
        return getArrayFor(transformer.transform(array));
    }
}

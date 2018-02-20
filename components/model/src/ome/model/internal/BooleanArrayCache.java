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
import java.util.HashMap;
import java.util.Map;

import ome.util.Counter;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Memory-sensitive cache of Boolean arrays. They are expected to be <em>immutable</em>.
 * Shorter arrays are considered identical to longer arrays with the higher indices set to {@code false}.
 * This class is <em>not</em> thread-safe.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.4.2
 */
class BooleanArrayCache {

    private static Logger logger = LoggerFactory.getLogger(BooleanArrayCache.class);

    /* can be resurrected by getArrayFor */
    private SoftReference<? extends Map<Long, boolean[]>> cacheRef = new SoftReference<>(new HashMap<Long, boolean[]>());

    private final Counter hits = new Counter();  // may grow very large
    private long misses = 0;
    private long resets = 0;

    private final DelayTracker logWhen = new DelayTracker(50 * 60);  // fifty minutes

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
        if (logWhen.isNow()) {
            logger.debug("cache stats: hits={}, misses={}, resets={}", hits.asBigInteger(), misses, resets);
        }
        if (array == null) {
            array = ArrayUtils.EMPTY_BOOLEAN_ARRAY;
        }
        final long number = numberForArray(array);
        Map<Long, boolean[]> cache = cacheRef.get();
        if (cache == null) {
            /* if the old cache grew too large then the garbage collector must have needed the space */
            logger.debug("resetting cache");
            cache = new HashMap<Long, boolean[]>();
            cacheRef = new SoftReference<>(cache);
            hits.reset();
            misses = 0;
            resets++;
        }
        boolean[] cached = cache.get(number);
        if (cached == null) {
            cache.put(number, array);
            misses++;
            return array;
        } else {
            if (numberForArray(cached) != number) {
                throw new IllegalStateException("cache violation");
            }
            hits.increment();
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

    /**
     * Helper class for delaying sufficiently between subsequent repeated actions.
     * @author m.t.b.carroll@dundee.ac.uk
     * @since 5.4.2
     */
    private static class DelayTracker {
        /* given Java 8 could consider LocalDateTime */
        private final long intervalMs;
        private long delayUntil;

        /**
         * Enforce delay intervals of at least the given duration.
         * @param delaySeconds how many seconds to delay at minimum
         */
        DelayTracker(int delaySeconds) {
            intervalMs = delaySeconds * 1000L;
            setNextDelay();
        }

        /**
         * Set the delay to be the current time plus the configured interval.
         */
        private void setNextDelay() {
            delayUntil = System.currentTimeMillis() + intervalMs;
        }

        /**
         * Check if it is at least the configured delay since this method last returned {@code true}.
         * @return if it is now time to perform the act, because the delay is past
         */
        boolean isNow() {
            final boolean isNow = delayUntil <= System.currentTimeMillis();
            if (isNow) {
                setNextDelay();
            }
            return isNow;
        }
    }
}

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

package ome.util;

import java.math.BigInteger;

import org.apache.commons.lang.ArrayUtils;

/**
 * A simple integral counter that can be incremented. Its properties include,
 * <ul>
 * <li><em>unbounded</em></li>
 * <li>{@link #increment()} typically does <em>not</em> require a heap allocation</li>
 * <li><em>not</em> thread-safe.</li>
 * </ul>
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.4.2
 */
public class Counter {

    private byte[] count;

    /**
     * Create a new counter starting at zero.
     */
    public Counter() {
        reset();
    }

    /**
     * Increment the value of this counter by one.
     */
    public void increment() {
        for (int index = count.length; index > 0;) {
            if (++count[--index] != 0) {
                return;
            }
        }
        count = new byte[count.length + 1];
        count[0] = 1;
    }

    /**
     * Reset this counter to zero.
     */
    public void reset() {
        count = ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    /**
     * @return the integer value of this counter
     */
    public BigInteger asBigInteger() {
        return new BigInteger(1, count);
    }
}

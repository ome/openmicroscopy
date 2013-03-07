/*
 * Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.util.checksum;

import java.nio.ByteBuffer;

/**
 * A provider producing checksums or message digests (depending on the
 * implementing class) of a given type of input. The object's internal state
 * represents the current checksum value and each call to <code>putBytes()</code>
 * updates this object's internal state. This is a fluent interface allowing
 * for method chaining.
 * <br/>
 * Inside the <code>ome.util.checksum</code> package, the term <i>checksum</i>
 * is understood as an "umbrella" term covering checksums, message digests and
 * hashes.
 *
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @since 4.4.7
 */
public interface ChecksumProvider {

    /**
     * Updates the internal checksum value with data from a byte array.
     * If the array is null, throws NPE. Note that, although checksum results
     * are consistent for any given hash function and byte array, different hash
     * functions may calculate different checksums for an empty array despite
     * its lack of content.
     *
     * @param byteArray The input byte array.
     * @return ChecksumProvider
     */
    ChecksumProvider putBytes(byte[] byteArray);

    /**
     * Updates the internal checksum value with data from a byte buffer.
     * If the array underlying the byte buffer is not accessible, throws an
     * IllegalArgumentException.
     *
     * @param byteBuffer The input byte buffer.
     * @return ChecksumProvider
     */
    ChecksumProvider putBytes(ByteBuffer byteBuffer);

    /**
     * Updates the internal checksum value with data from a file identified by a
     * path. Throws a RuntimeException in a case of an IO error.
     *
     * @param filePath <code>String</code> representing the absolute file path.
     * @return ChecksumProvider
     */
    ChecksumProvider putBytes(String filePath);

    /**
     * Resets the internal state of the object by clearing the checksum value.
     *
     * @return ChecksumProvider
     */
    ChecksumProvider reset();

    /**
     * Returns a byte array representation of the calculated checksum.
     * Internally this method resets the object state.
     *
     * @return <code>byte[]</code> The checksum in a byte array.
     */
    byte[] checksumAsBytes();

    /**
     * Returns a <code>String</code> representing the checksum in hex form.
     * Internally this method resets the object state.
     *
     * @return <code>String</code> The hexadecimal value of the checksum.
     */
    String checksumAsString();
}

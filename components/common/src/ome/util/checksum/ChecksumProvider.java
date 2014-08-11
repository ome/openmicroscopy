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
 * An interface producing checksums or message digests (depending on the
 * implementing class) of a given type of input. The object's internal state
 * represents the current checksum value and each call to <code>putBytes()</code>
 * updates this object's internal state. It is the callers responsibility to
 * make sure calls to {@link ChecksumProvider#putFile(String)} are not intermixed
 * with <code>putBytes()</code>. This object can only return a checksum for
 * a file or byte structure, never both.
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
     * Updates the internal checksum value with data from a chunk of a byte
     * array. First byte read is <code>byteArray[offset]</code>, last byte read
     * is <code>byteArray[offset + length - 1]</code>. If the array is null,
     * throws NPE. Throws IOOB if indexes are invalid. Note that, although
     * checksum results are consistent for any given hash function and byte
     * array, different hash functions may calculate different checksums for an
     * empty array despite its lack of content.
     *
     * @param byteArray The input byte array.
     * @param offset The offset in the byte array at which to start putting bytes.
     * @param length The number of bytes to put, starting from the offset.
     * @return ChecksumProvider
     */
    ChecksumProvider putBytes(byte[] byteArray, int offset, int length);

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
     * Input previous to this method does not affect the calculated checksum.
     *
     * @param filePath <code>String</code> representing the absolute file path.
     * @return ChecksumProvider
     */
    ChecksumProvider putFile(String filePath);

    /**
     * Returns a byte array representation of the calculated checksum.
     * Subsequent calls to this method will return the same object state. After
     * calling this method any invocation of the mutating methods
     * (<code>put*</code>) will cause it to throw IllegalStateException.
     *
     * @return <code>byte[]</code> The checksum in a byte array.
     */
    byte[] checksumAsBytes();

    /**
     * Returns a <code>String</code> representing the checksum in hex form.
     * Subsequent calls to this method will return the same object state. After
     * calling this method any invocation of the mutating methods
     * (<code>put*</code>) will cause it to throw IllegalStateException.
     *
     * @return <code>String</code> The hexadecimal value of the checksum.
     */
    String checksumAsString();
}

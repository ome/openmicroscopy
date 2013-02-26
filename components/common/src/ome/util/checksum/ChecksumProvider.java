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
 * A provider producing checksum or message digests (depending on the
 * implementing class) of a given type of input. Inside the
 * <code>ome.util.checksum</code> package, the term <i>checksum</i>
 * is understood as an "umbrella" term covering checksums, message digests and
 * hashes. An UnsupportedOperationException is thrown for methods which were
 * not present in the Utils class, which this interface supersedes.
 *
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @since 4.4.7
 */
public interface ChecksumProvider {

    /**
     * Returns a checksum of a byte array. If the array is null, throws NPE.
     * Note that, although checksum results are consistent for any given hash
     * function and byte array, different hash functions may calculate different
     * checksums for an empty array despite its lack of content.
     *
     * @param buffer The input byte array.
     * @return Checksum bytes inside an array.
     */
    byte[] getChecksum(byte[] buffer);

    /**
     * Returns a checksum of a file identified by a path. Throws a
     * RuntimeException in a case of an IO error.
     *
     * @param fileName <code>String</code> representing the absolute file path.
     * @return Checksum bytes inside an array.
     */
    byte[] getChecksum(String fileName);

    /**
     * Returns a checksum of a byte buffer. If the array underlying the byte
     * buffer is not accessible, throws an IllegalArgumentException.
     *
     * @param buffer The input byte buffer.
     * @return Checksum bytes inside an array.
     */
    byte[] getChecksum(ByteBuffer buffer);

}

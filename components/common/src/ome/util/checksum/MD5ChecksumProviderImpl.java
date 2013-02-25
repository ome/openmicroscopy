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

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.ByteBuffer;

/**
 * An implementation of the {@link ChecksumProvider} interface using MD5
 * as the message digest algorithm.
 *
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @since 4.4.7
 */
public class MD5ChecksumProviderImpl implements ChecksumProvider {

    private final HashFunction md5 = Hashing.md5();

    /**
     * @see ChecksumProvider#getChecksum(byte[])
     */
    public byte[] getChecksum(byte[] rawData) {
        return this.md5.newHasher().putBytes(rawData).hash().asBytes();
    }

    /**
     * @see ChecksumProvider#getChecksum(ByteBuffer)
     */
    public byte[] getChecksum(ByteBuffer byteBuffer) {
        byte[] result = null;
        if (byteBuffer.hasArray()) {
            result = this.md5.newHasher().putBytes(byteBuffer.array()).hash().asBytes();
        }
        return result;
    }

    /**
     * @see ChecksumProvider#getChecksum(String)
     */
    public byte[] getChecksum(String filePath) {
        throw new UnsupportedOperationException("provideChecksum() not"
                + "implemented for file path String.");
    }

}

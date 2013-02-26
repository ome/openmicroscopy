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
import com.google.common.hash.Hasher;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import ome.util.Utils;

/**
 * Abstract skeleton class implementing {@link ChecksumProvider} and providing
 * implementations of the interface methods using a universal checksum class
 * object. Classes extending this class shall pass in a concrete checksum
 * algorithm implementation (a {@link HashFunction} instance) as the constructor
 * parameter.
 *
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @since 4.4.7
 */
public class AbstractChecksumProvider implements ChecksumProvider {

    // Array size for buffered file reads
    private static final int BYTEARRAYSIZE = 8192;

    private final HashFunction hashFunction;

    /**
     * Protected ctor. There should not be an instance of this class.
     * @param hashFunction
     */
    protected AbstractChecksumProvider(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    /**
     * @see ChecksumProvider#getChecksum(byte[])
     */
    public byte[] getChecksum(byte[] byteArray) {
        return hashFunction.newHasher().putBytes(byteArray).hash().asBytes();
    }

    /**
     * @see ChecksumProvider#getChecksum(String)
     */
    public byte[] getChecksum(String fileName) {
        FileInputStream fis = null;
        FileChannel fch = null;
        Hasher hasher = this.hashFunction.newHasher();
        try {
            fis = new FileInputStream(fileName);
            fch = fis.getChannel();
            MappedByteBuffer mbb = fch.map(FileChannel.MapMode.READ_ONLY, 0L,
                    fch.size());
            byte[] byteArray = new byte[BYTEARRAYSIZE];
            int byteCount;
            while (mbb.hasRemaining()) {
                byteCount = Math.min(mbb.remaining(), BYTEARRAYSIZE);
                mbb.get(byteArray, 0, byteCount);
                hasher.putBytes(byteArray, 0, byteCount);
            }
            return hasher.hash().asBytes();
        } catch (IOException io) {
            throw new RuntimeException(io);
        } finally {
            Utils.closeQuietly(fis);
            Utils.closeQuietly(fch);
        }
    }

    /**
     * @see ChecksumProvider#getChecksum(ByteBuffer)
     */
    public byte[] getChecksum(ByteBuffer byteBuffer) {
        if (byteBuffer.hasArray()) {
            return this.hashFunction.newHasher().putBytes(byteBuffer.array())
                    .hash().asBytes();
        } else {
            throw new IllegalArgumentException("Supplied ByteBuffer has " +
                    "inaccessible array.");
        }
    }

}

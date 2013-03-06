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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import ome.util.Utils;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;

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

    private final HashFunction hashFunction;

    private Hasher hasher;

    // Array size for buffered file reads
    private static final int BYTEARRAYSIZE = 8192;

    /**
     * Protected ctor. There should not be an instance of this class.
     * @param hashFunction
     */
    protected AbstractChecksumProvider(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
        this.hasher = this.hashFunction.newHasher();
    }

    /**
     * @see ChecksumProvider#putBytes(byte[])
     */
    public ChecksumProvider putBytes(byte[] byteArray) {
        this.hasher.putBytes(byteArray);
        return this;
    }

    /**
     * @see ChecksumProvider#putBytes(ByteBuffer)
     */
    public ChecksumProvider putBytes(ByteBuffer byteBuffer) {
        if (byteBuffer.hasArray()) {
            this.hasher.putBytes(byteBuffer.array());
            return this;
        } else {
            throw new IllegalArgumentException("Supplied ByteBuffer has " +
                    "inaccessible array.");
        }
    }

    /**
     * @see ChecksumProvider#putBytes(String)
     */
    public ChecksumProvider putBytes(String filePath) {
        FileInputStream fis = null;
        FileChannel fch = null;
        try {
            fis = new FileInputStream(filePath);
            fch = fis.getChannel();
            MappedByteBuffer mbb = fch.map(FileChannel.MapMode.READ_ONLY, 0L,
                    fch.size());
            byte[] byteArray = new byte[BYTEARRAYSIZE];
            int byteCount;
            while (mbb.hasRemaining()) {
                byteCount = Math.min(mbb.remaining(), BYTEARRAYSIZE);
                mbb.get(byteArray, 0, byteCount);
                this.hasher.putBytes(byteArray, 0, byteCount);
            }
            return this;
        } catch (IOException io) {
            throw new RuntimeException(io);
        } finally {
            Utils.closeQuietly(fis);
            Utils.closeQuietly(fch);
        }
    }

    public ChecksumProvider reset() {
        this.hasher = this.hashFunction.newHasher();
        return this;
    }

    /**
     * @see ChecksumProvider#toByteChecksum()
     */
    public byte[] toByteChecksum() {
        byte[] result = this.hasher.hash().asBytes();
        this.reset();
        return result;
    }

    /**
     * @see ChecksumProvider#toStringChecksum()
     */
    public String toStringChecksum() {
        String result = Utils.bytesToHex(this.hasher.hash().asBytes());
        this.reset();
        return result;
    }

}

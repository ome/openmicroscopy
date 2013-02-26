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
import com.google.common.hash.Hashing;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import ome.util.Utils;

/**
 * An implementation of the {@link ChecksumProvider} interface using SHA1
 * as the secure hash algorithm.
 *
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @since 4.4.7
 */
public class SHA1ChecksumProviderImpl implements ChecksumProvider {

    private final HashFunction sha1 = Hashing.sha1();

    private static final int BYTEARRAYSIZE = 8192;

    /**
     * @see ChecksumProvider#getChecksum(byte[])
     */
    public byte[] getChecksum(byte[] rawData) {
        return this.sha1.newHasher().putBytes(rawData).hash().asBytes();
    }

    /**
     * @see ChecksumProvider#getChecksum(ByteBuffer)
     */
    public byte[] getChecksum(ByteBuffer byteBuffer) {
        throw new UnsupportedOperationException("Method not "
                + "implemented for ByteBuffer.");
    }

    /**
     * @see ChecksumProvider#getChecksum(String)
     */
    public byte[] getChecksum(String filePath) {
        FileInputStream fis = null;
        FileChannel fch = null;
        Hasher sha1Hasher = this.sha1.newHasher();
        try {
            fis = new FileInputStream(filePath);
            fch = fis.getChannel();
            MappedByteBuffer mbb = fch.map(FileChannel.MapMode.READ_ONLY, 0L, fch.size());
            byte[] byteArray = new byte[BYTEARRAYSIZE];
            int byteCount;
            while (mbb.hasRemaining()) {
                byteCount = Math.min(mbb.remaining(), BYTEARRAYSIZE);
                mbb.get(byteArray, 0, byteCount);
                sha1Hasher.putBytes(byteArray, 0, byteCount);
            }
            return sha1Hasher.hash().asBytes();
        } catch (IOException io) {
            throw new RuntimeException(io);
        } finally {
            Utils.closeQuietly(fis);
            Utils.closeQuietly(fch);
        }
    }

}

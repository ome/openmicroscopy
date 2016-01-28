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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.google.common.base.Optional;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.io.Files;

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

    private Optional<HashCode> hashCode = Optional.absent();

    private Optional<byte[]> hashBytes = Optional.absent();

    private Optional<String> hashString = Optional.absent();

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
        return this.putBytes(byteArray, 0, byteArray.length);
    }

    /**
     * @see ChecksumProvider#putBytes(byte[], int, int)
     */
    public ChecksumProvider putBytes(byte[] byteArray, int offset, int length) {
        this.verifyState(this.hashBytes, this.hashString);
        this.hasher.putBytes(byteArray, offset, length);
        return this;
    }

    /**
     * @see ChecksumProvider#putBytes(ByteBuffer)
     */
    public ChecksumProvider putBytes(ByteBuffer byteBuffer) {
        this.verifyState(this.hashBytes, this.hashString);
        if (byteBuffer.hasArray()) {
            this.hasher.putBytes(byteBuffer.array(), 0, byteBuffer.limit());
            return this;
        } else {
            throw new IllegalArgumentException("Supplied ByteBuffer has " +
                    "inaccessible array.");
        }
    }

    /**
     * @see ChecksumProvider#putFile(String)
     */
    public ChecksumProvider putFile(String filePath) {
        this.verifyState(this.hashBytes, this.hashString);
        try {
            this.hashCode = Optional.of(
                    Files.hash(new File(filePath), this.hashFunction));
            return this;
        } catch (IOException io) {
            throw new RuntimeException(io);
        }
    }

    /**
     * @see ChecksumProvider#checksumAsBytes()
     */
    public byte[] checksumAsBytes() {
        this.hashBytes = Optional.of(this.pickChecksum().asBytes());
        return this.hashBytes.get();
    }

    /**
     * @see ChecksumProvider#checksumAsString()
     */
    public String checksumAsString() {
        this.hashString = Optional.of(this.pickChecksum().toString());
        return this.hashString.get();
    }

    private HashCode pickChecksum() {
        return this.hashCode.isPresent() ?
                this.hashCode.get() : this.hasher.hash();
    }

    private void verifyState(Optional... optionalObjects) {
        for (Optional optionalObject : optionalObjects) {
            if (optionalObject.isPresent()) {
                throw new IllegalStateException("Checksum state already set. " +
                        "Mutation illegal.");
            }
        }
    }
}

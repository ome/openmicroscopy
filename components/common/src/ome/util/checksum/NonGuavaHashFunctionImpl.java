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

import java.nio.charset.Charset;
import java.util.zip.Checksum;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;

/**
 * Wrapper class around a {@link NonGuavaHasherImpl} object and the
 * {@link Checksum} Java interface object. The purpose of this entity is to
 * serve as an interface between the consuming class that expects an
 * object of type {@link HashFunction} and the underlying implementation
 * of type {@link Hasher}. All the hash computation is done in the delegate
 * object. The main responsibility of this class is the <code>newHasher()</code>
 * method, that calls the <code>reset()</code> method on the checksum object
 * and provides it to the {@link Hasher} ctor.
 *
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @since 5.0
 */
final public class NonGuavaHashFunctionImpl implements HashFunction {

    private NonGuavaHasherImpl hasher;

    private final Checksum checksum;

    public NonGuavaHashFunctionImpl(Checksum checksum) {
        this.checksum = checksum;
        this.hasher = new NonGuavaHasherImpl(this.checksum);
    }

    /**
     * Here it is a multiple of 32 bits.
     * @see com.google.common.hash.HashFunction#bits()
     */
    public int bits() {
        return 1;
    }

    /**
     * @see com.google.common.hash.HashFunction#hashBytes(byte[])
     */
    public HashCode hashBytes(byte[] input) {
        return this.hasher.putBytes(input).hash();
    }

    /**
     * @see com.google.common.hash.HashFunction#hashBytes(byte[], int, int)
     */
    public HashCode hashBytes(byte[] input, int off, int len) {
        return this.hasher.putBytes(input, off, len).hash();
    }

    /**
     * @see com.google.common.hash.HashFunction#hashInt(int)
     */
    public HashCode hashInt(int input) {
        return this.hasher.putInt(input).hash();
    }

    /**
     * @see com.google.common.hash.HashFunction#hashLong(long)
     */
    public HashCode hashLong(long input) {
        return this.hasher.putLong(input).hash();
    }

    /**
     * @see com.google.common.hash.HashFunction#hashString(java.lang.CharSequence)
     */
    public HashCode hashString(CharSequence input) {
        return this.hasher.putString(input).hash();
    }

    /**
     * @see com.google.common.hash.HashFunction#hashString(java.lang.CharSequence, java.nio.charset.Charset)
     */
    public HashCode hashString(CharSequence input, Charset charset) {
        return this.hasher.putString(input, charset).hash();
    }

    /**
     * @see com.google.common.hash.HashFunction#newHasher()
     */
    public Hasher newHasher() {
        this.checksum.reset();
        return this.hasher = new NonGuavaHasherImpl(this.checksum);
    }

    /**
     * @see com.google.common.hash.HashFunction#newHasher(int)
     */
    public Hasher newHasher(int expectedInputSize) {
        throw new UnsupportedOperationException();
    }
}

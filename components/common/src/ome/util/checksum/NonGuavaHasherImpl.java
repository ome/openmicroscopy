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
import java.nio.charset.Charset;
import java.util.zip.Checksum;

import com.google.common.hash.Funnel;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashCodes;
import com.google.common.hash.Hasher;

/**
 * An implementation of the {@link Hasher} interface. Uses
 * {@link Checksum} for all computations and as the result source. Any operations
 * not supported in {@link Checksum} throw {@link UnsupportedOperationException}.
 *
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @since 5.0
 */
final class NonGuavaHasherImpl implements Hasher {

    private final Checksum checksum;

    public NonGuavaHasherImpl(Checksum checksum) {
        this.checksum = checksum;
    }

    /**
     * This method uses the {@link HashCodes#fromBytes(byte[])} to produce
     * the required {@link HashCode} return type. The Adler32 output is
     * converted from <code>long</code> to <code>int</code> and then to a byte
     * array. This guarantees endianness consistency, as other methods from
     * {@link HashCodes}, which could be also used, invert the byte order.
     *
     * @see com.google.common.hash.Hasher#hash()
     */
    public HashCode hash() {
        ByteBuffer buf = ByteBuffer.wrap(new byte[4]);
        // If Adler32.getValue starts returning 64 bits, the world will end
        buf.putInt((int) this.checksum.getValue());
        return HashCodes.fromBytes(buf.array());
    }

    /**
     * @see com.google.common.hash.Hasher#putBoolean(boolean)
     */
    public Hasher putBoolean(boolean b) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see com.google.common.hash.Hasher#putByte(byte)
     */
    public Hasher putByte(byte b) {
        this.checksum.update(b);
        return this;
    }

    /**
     * @see com.google.common.hash.Hasher#putBytes(byte[])
     */
    public Hasher putBytes(byte[] bytes) {
        this.checksum.update(bytes, 0, bytes.length);
        return this;
    }

    /**
     * @see com.google.common.hash.Hasher#putBytes(byte[], int, int)
     */
    public Hasher putBytes(byte[] bytes, int off, int len) {
        this.checksum.update(bytes, off, len);
        return this;
    }

    /**
     * @see com.google.common.hash.Hasher#putChar(char)
     */
    public Hasher putChar(char c) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see com.google.common.hash.Hasher#putDouble(double)
     */
    public Hasher putDouble(double d) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see com.google.common.hash.Hasher#putFloat(float)
     */
    public Hasher putFloat(float f) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see com.google.common.hash.Hasher#putInt(int)
     */
    public Hasher putInt(int i) {
        this.checksum.update(i);
        return this;
    }

    /**
     * @see com.google.common.hash.Hasher#putLong(long)
     */
    public Hasher putLong(long l) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see com.google.common.hash.Hasher#putObject(Object, Funnel)
     */
    public <T> Hasher putObject(T instance, Funnel<? super T> funnel) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see com.google.common.hash.Hasher#putShort(short)
     */
    public Hasher putShort(short s) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see com.google.common.hash.Hasher#putString(CharSequence)
     */
    public Hasher putString(CharSequence charSequence) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see com.google.common.hash.Hasher#putString(CharSequence, Charset)
     */
    public Hasher putString(CharSequence charSequence, Charset charset) {
        throw new UnsupportedOperationException();
    }

}

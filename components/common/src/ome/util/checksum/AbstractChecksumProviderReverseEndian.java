/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.util.checksum;

import com.google.common.hash.HashFunction;

/**
 * Reverse the endianness of a checksum provider's hash.
 * 
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0.3
 */
public class AbstractChecksumProviderReverseEndian extends
        AbstractChecksumProvider {

    protected AbstractChecksumProviderReverseEndian(HashFunction hashFunction) {
        super(hashFunction);
    }

    @Override
    public byte[] checksumAsBytes() {
        final byte[] forward = super.checksumAsBytes();
        final byte[] backward = new byte[forward.length];
        int b = backward.length;
        for (int f = 0; f < forward.length; f++) {
            backward[--b] = forward[f];
        }
        return backward;
    }

    @Override
    public String checksumAsString() {
        final String checksum = super.checksumAsString();
        final int checksumLength = checksum.length();
        final StringBuffer sb = new StringBuffer(checksumLength);
        for (int i = 0; i < checksumLength; i += 2) {
            sb.insert(0, checksum.charAt(i));
            sb.insert(1, checksum.charAt(i + 1));
        }
        return sb.toString();
    }
}

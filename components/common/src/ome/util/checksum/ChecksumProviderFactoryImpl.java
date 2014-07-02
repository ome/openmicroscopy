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

import java.util.Arrays;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * An implementation of the {@link ChecksumProviderFactory} interface.
 *
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @since 4.4.7
 */
public class ChecksumProviderFactoryImpl implements ChecksumProviderFactory {

    private static final ImmutableSet<ChecksumType> availableChecksumTypes =
            Sets.immutableEnumSet(Arrays.asList(ChecksumType.values()));

    /**
     * @see ChecksumProviderFactory#getProvider(ChecksumType)
     */
    public ChecksumProvider getProvider(ChecksumType checksumType) {
        // Dumb implementation for now
        // TODO: remove the switch statement
        switch (checksumType) {
            case FILE_SIZE:
                return new FileSizeChecksumProviderImpl();
            case ADLER32:
                return new Adler32ChecksumProviderImpl();
            case CRC32:
                return new CRC32ChecksumProviderImpl();
            case MD5:
                return new MD5ChecksumProviderImpl();
            case MURMUR32:
                return new Murmur32ChecksumProviderImpl();
            case MURMUR128:
                return new Murmur128ChecksumProviderImpl();
            case SHA1:
            default:
                return new SHA1ChecksumProviderImpl();
        }
    }

    @Override
    public Set<ChecksumType> getAvailableTypes() {
        return availableChecksumTypes;
    }
}

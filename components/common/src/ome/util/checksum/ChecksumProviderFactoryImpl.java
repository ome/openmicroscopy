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

/**
 * An implementation of the {@link ChecksumProviderFactory} interface.
 *
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @since 4.4.7
 */
public class ChecksumProviderFactoryImpl implements ChecksumProviderFactory {

    /**
     * @see ChecksumProviderFactory#getProvider()
     */
    public ChecksumProvider getProvider() {
        return this.getProvider(ChecksumType.SHA1);
    }

    /**
     * @see ChecksumProviderFactory#getProvider(ChecksumType)
     */
    public ChecksumProvider getProvider(ChecksumType checksumType) {
        // Dumb implementation for now
        // TODO: remove the switch statement
        switch (checksumType) {
            case MD5:
                return new MD5ChecksumProviderImpl();
            case SHA1:
            default:
                return new SHA1ChecksumProviderImpl();
        }
    }

}

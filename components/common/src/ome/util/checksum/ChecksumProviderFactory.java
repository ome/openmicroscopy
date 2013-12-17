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

import java.util.Set;

/**
 * A factory producing throw-away objects of the {@link ChecksumProvider} type.
 *
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @since 4.4.7
 */
public interface ChecksumProviderFactory {

    /**
     * Returns an implementation of {@link ChecksumProvider} depending on the
     * specified {@link ChecksumType}.
     *
     * @param checksumType The type of requested {@link ChecksumProvider}.
     * @return The {@link ChecksumProvider} implementation.
     */
    ChecksumProvider getProvider(ChecksumType checksumType);

    /**
     * @return the values for which this instance creates a corresponding provider from {@link #getProvider(ChecksumType)}
     */
    Set<ChecksumType> getAvailableTypes();
}

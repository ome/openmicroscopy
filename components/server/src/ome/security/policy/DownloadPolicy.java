/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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

package ome.security.policy;

import ome.model.core.OriginalFile;


/**
 * 
 *
 */
public class DownloadPolicy extends BasePolicy {

    /**
     * This string can also be found in the Constants.ice file in the
     * blitz package.
     */
    public final static String NAME = "RESTRICT-DOWNLOAD";

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Default constructor which is used to create the context-free
     * {@link Policy} which performs the actual checks.
     */
    public DownloadPolicy() {
        
    }

    /**
     * {@link DownloadPolicy} with a particular file as the context.
     * @param file
     */
    public DownloadPolicy(OriginalFile file) {

    }

}
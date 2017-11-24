/*
 * Copyright (C) 2015-2016 University of Dundee & Open Microscopy Environment.
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

package omero.gateway.facility;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;

/**
 * {@link Facility} which provides data transfer functionality, i.e. download
 * files and upload/import files.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class TransferFacility extends Facility {

    /** Reference to the helper class */
    private TransferFacilityHelper helper;

    /**
     * Creates a new instance
     * @param gateway Reference to the {@link Gateway}
     */
    TransferFacility(Gateway gateway) throws ExecutionException {
        super(gateway);
        this.helper = new TransferFacilityHelper(gateway);
    }

    /**
     * Downloads the original file of an image from the server.
     *
     * @param context The security context.
     * @param targetPath Path to the file.
     * @param imageId The identifier of the image.
     * @return See above
     * @throws DSOutOfServiceException
     *             If the connection is broken, or not logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    public List<File> downloadImage(SecurityContext context, String targetPath,
            long imageId) throws DSAccessException, DSOutOfServiceException {
        if (imageId < 0)
            return Collections.emptyList();
        return helper.downloadImage(context, targetPath, imageId);
    }

}

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.formats.importer;

import java.io.File;
import java.io.IOException;

import ome.formats.importer.util.TimeEstimator;
import ome.util.checksum.ChecksumProvider;
import omero.ServerError;
import omero.grid.ImportProcessPrx;

/**
 * Abstracted concept of "getting the file to the server".
 * Implementations are responsible for making sure that when
 * the server accesses the remote (i.e. server-side) location
 * that a file-like object (file, hardlink, symlink, etc.) is
 * present with the right size and checksum.
 *
 * Transfer implementations have a number of responsibilities
 * which make them not completely trivial to implement. Sub-classing
 * an existing implementation is likely the easiest way to
 * modify behavior.
 *
 * @since 5.0
 */
public interface FileTransfer {

    /**
     * Transfers a file and returns the appropriate checksum string for
     * the source file.
     *
     * @param file Source file which is to be transferred.
     * @param index Which of the total files to upload this is.
     * @param total Total number of files to upload.
     * @param proc {@link ImportProcessPrx} which is being imported to.
     * @param library {@link ImportLibrary} to use for notifications.
     * @param estimator
     * @param cp
     * @param buf optional buffer. Need not be used or updated.
     * @return checksum string
     * @throws Exception
     */
    String transfer(File file,
            int index, int total, // as index of
            ImportProcessPrx proc, // to
            ImportLibrary library,
            TimeEstimator estimator,
            ChecksumProvider cp,
            byte[] buf) throws IOException, ServerError;
}

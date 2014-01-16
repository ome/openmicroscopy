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

import java.io.IOException;

import ome.formats.importer.transfers.TransferState;
import omero.ServerError;

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

   /*
    * Transfers a file and returns the appropriate checksum string for
    * the source file.
    */
    String transfer(TransferState state)
        throws IOException, ServerError;

}

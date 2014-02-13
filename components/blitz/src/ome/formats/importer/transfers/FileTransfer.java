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

package ome.formats.importer.transfers;

import java.io.IOException;
import java.util.List;

import omero.ServerError;

/**
 * Abstracted concept of "getting the file to the server". A single
 * {@link FileTransfer} instance should be used for all the instances in
 * a single "import".
 *
 * Implementations are responsible for making sure that when
 * the server accesses the remote (i.e. server-side) location
 * that a file-like object (file, hard-link, soft-link, etc.) is
 * present with the right size and checksum.
 *
 * Transfer implementations have a number of responsibilities such as
 * reporting on progress and estimating remaining time which make them not
 * completely trivial to implement. Sub-classing an existing implementation is
 * likely the easiest way to modify behavior.
 *
 * Implementations should be thread-safe, i.e. callable from multiple
 * threads, and blocking should be avoided if at all possible.
 *
 * @since 5.0
 */
public interface FileTransfer {

   /**
    * Transfers a file and returns the appropriate checksum string for
    * the source file. The {@link TransferState} instance should be unique
    * for this invocation, i.e. not used by any other threads. After
    * execution, the fields can be inspected to see, e.g., the newly created
    * file.
    */
    String transfer(TransferState state)
        throws IOException, ServerError;

    /**
     * Callback which must be invoked after a related set of files has been
     * processed. This provides the {@link FileTransfer} instance a chance to
     * free resources. If any errors have occurred, then no destructive changes
     * should be made, though the user may should be given the option to react.
     */
    void afterTransfer(int errors, List<String> transferredFiles) throws CleanupFailure;

}

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

import java.util.List;

/**
 * Version of the default {@link UploadFileTransfer} which
 * deletes all files in a transfer set if the upload is
 * successful. This is similar to the {@link MoveFileTransfer}
 * but should be considered less safe since a remote copy
 * is involved.
 *
 * @since 5.0.3
 */
public class UploadRmFileTransfer extends UploadFileTransfer {

    /**
     * Deletes all uploaded files if there were no errors.
     */
    @Override
    public void afterTransfer(int errors, List<String> srcFiles) throws CleanupFailure {
        deleteTransferredFiles(errors, srcFiles);
    }
}

/*
 * Copyright (C) April 1st 2015 University of Dundee & Open Microscopy Environment.
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
 * Local-only file transfer mechanism which makes use of symlinking
 * followed by the deletion of the original source file.
 *
 * @since 5.1
 */
public class RemoveFileTransfer extends SymlinkFileTransfer {

    /**
     * Deletes all symlinked files if there were no errors.
     */
    @Override
    public void afterTransfer(int errors, List<String> srcFiles) throws CleanupFailure {
        deleteTransferredFiles(errors, srcFiles);
    }
}

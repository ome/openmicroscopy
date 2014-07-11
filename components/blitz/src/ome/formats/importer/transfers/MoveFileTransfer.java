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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Local-only file transfer mechanism which makes use of hard-linking
 * followed by the deletion of the original source file.
 *
 * @since 5.0
 */
public class MoveFileTransfer extends HardlinkFileTransfer {

    /**
     * Deletes all hard-linked files
     */
    @Override
    public void afterTransfer(int errors, List<String> srcFiles) throws CleanupFailure {

        if (errors > 0) {
            printLine();
            log.error("{} error(s) found.", errors);
            log.error("MoveFileTransfer cleanup not performed!", errors);
            log.error("The following files will *not* be deleted:");
            for (String srcFile : srcFiles) {
                log.error("\t{}", srcFile);
            }
            printLine();
            return;
        }

        List<File> failedFiles = new ArrayList<File>();
        for (String path : srcFiles) {
            File srcFile = new File(path);
            try {
                log.info("Deleting source file {}...", srcFile);
                if (!srcFile.delete()) {
                    throw new RuntimeException("Failed to delete.");
                }
            } catch (Exception e) {
                log.error("Failed to remove source file {}", srcFile);
                failedFiles.add(srcFile);
            }
        }

        if (!failedFiles.isEmpty()) {
            printLine();
            log.error("Cleanup failed!", errors);
            log.error("{} files could not be removed and will need to " +
                "be handled manually", failedFiles.size());
            for (File failedFile : failedFiles) {
                log.error("\t{}", failedFile.getAbsolutePath());
            }
            printLine();
            throw new CleanupFailure(failedFiles);
        }
    }
}
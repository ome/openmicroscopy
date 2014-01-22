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
import java.io.IOException;
import java.util.List;

/**
 * Local-only file transfer mechanism which makes use of hard-linking.
 *
 * This is only useful where the command "ln source target" will work.
 *
 * @since 5.0
 */
public class HardlinkFileTransfer extends AbstractExecFileTransfer {

    /**
     * Executes "ln file location" and fails on non-0 return codes.
     *
     * @param file
     * @param location
     * @throws IOException
     */
    protected ProcessBuilder createProcessBuilder(File file, File location) {
        ProcessBuilder pb = new ProcessBuilder();
        // Doesn't currently support Windows
        pb.command("ln", file.getAbsolutePath(), location.getAbsolutePath());
        return pb;
    }

    /**
     * No cleanup action is taken.
     */
    public void afterSuccess(List<String> srcFiles) throws CleanupFailure {
        // no-op
    }
}

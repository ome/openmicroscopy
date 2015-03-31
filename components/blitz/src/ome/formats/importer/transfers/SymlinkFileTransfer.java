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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Local-only file transfer mechanism which makes use of soft-linking.
 *
 *  This is only useful where the commands "ln -s source target" (Unix) or
 * "mklink target source" (Windows) will work.
 *
 * @since 5.0
 */
public class SymlinkFileTransfer extends AbstractExecFileTransfer {

    /**
     * Executes "ln -s file location" (Unix) or "mklink location file" (Windows)
     * and fails on non-0 return codes.
     *
     * @param file File to be copied.
     * @param location Location to copy to.
     * @throws IOException
     * @TODO Java7: replace ln once Java6 is dropped
     */
    protected ProcessBuilder createProcessBuilder(File file, File location) {
        ProcessBuilder pb = new ProcessBuilder();
        List<String> args = new ArrayList<String>();
        if (isWindows()) {
            args.add("cmd");
            args.add("/c");
            args.add("mklink");
            args.add(location.getAbsolutePath());
            args.add(file.getAbsolutePath());
        } else {
            args.add("ln");
            args.add("-s");
            args.add(file.getAbsolutePath());
            args.add(location.getAbsolutePath());
        }
        pb.command(args);
        return pb;
    }

    /**
     * No cleanup is needed for soft-linking.
     */
    public void afterTransfer(int errors, List<String> srcFiles) throws CleanupFailure {
        // no-op
    }
}

/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package ome.formats.importer.transfers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;


/**
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.2
 */
public class CopyFileTransfer2 extends AbstractExecFileTransfer2 {

    /**
     * Copies the file to the specified location.
     * @see AbstractExecFileTransfer2#execute(File, File)
     */
    protected void execute(File file, File location) throws IOException {
        Files.copy(Paths.get(file.toURI()), Paths.get(location.toURI()),
                StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * No cleanup action is taken.
     */
    public void afterTransfer(int errors, List<String> srcFiles) throws CleanupFailure {
        // no-op
    }

    @Override
    protected String getCommand() {
        return "Copy file";
    }

}

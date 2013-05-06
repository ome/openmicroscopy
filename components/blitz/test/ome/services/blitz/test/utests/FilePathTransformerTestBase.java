/*
 * Copyright (C) 2012 - 2013 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.blitz.test.utests;

import java.io.File;

import ome.services.blitz.repo.path.FilePathRestrictionInstance;
import ome.services.blitz.repo.path.FilePathRestrictions;

/**
 * Utility functions for file path transformer testing.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
public class FilePathTransformerTestBase {
    protected final FilePathRestrictions conservativeRules =
            FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.values());

    /**
     * Get the absolute path of the root directory above the current directory.
     * Assumes that the current directory is named <q><code>.</code></q>.
     * @return the root directory
     */
    private static File getRootDir() {
        File parent = new File(".").getAbsoluteFile();
        File dir = null;
        while (parent != null) {
            dir = parent;
            parent = dir.getParentFile();
        }
        return dir;
    }
    
    /**
     * Converts the path components to a corresponding absolute File.
     * @param components path components
     * @return the corresponding File
     */
    protected File componentsToFile(String... components) {
        File file = getRootDir();
        for (final String component : components)
            file = new File(file, component);
        return file;
    }
}

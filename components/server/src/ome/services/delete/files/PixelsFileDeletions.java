/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
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
package ome.services.delete.files;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;

import ome.io.bioformats.BfPyramidPixelBuffer;
import ome.io.nio.AbstractFileSystemService;
import ome.io.nio.PixelsService;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers the given file paths as well as other Pixel-related
 * files like Pyramids for later deletion via {@link #deleteLocal()}.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.1.0-m3
 */
public class PixelsFileDeletions extends AbstractFileDeletions {

    private static final Logger log = LoggerFactory.getLogger(PixelsFileDeletions.class);

    public PixelsFileDeletions(AbstractFileSystemService afs, Set<Long> deletedIds) {
        super(afs, deletedIds);
        for (Long id : deletedIds) {
            final String filePath = afs.getPixelsPath(id);
            final File file = new File(filePath);
            final File pyrFile = new File(filePath + PixelsService.PYRAMID_SUFFIX);
            final File dir = file.getParentFile();
            final File lockFile = new File(dir, "." + id + PixelsService.PYRAMID_SUFFIX
                + BfPyramidPixelBuffer.PYR_LOCK_EXT);

            // Remove the Pyramid file itself
            addLocalFile(file, id);
            // Try to remove a _pyramid file if it exists
            addLocalFile(pyrFile, id);
            // Now any lock file
            addLocalFile(lockFile, id);

            // Now any tmp files
            FileFilter tmpFileFilter = new WildcardFileFilter("."
                    + id + PixelsService.PYRAMID_SUFFIX + "*.tmp");
            File[] tmpFiles = dir.listFiles(tmpFileFilter);
            if(tmpFiles != null) {
                for (int i = 0; i < tmpFiles.length; i++) {
                    addLocalFile(tmpFiles[i], id);
                }
            }
        }
    }
}

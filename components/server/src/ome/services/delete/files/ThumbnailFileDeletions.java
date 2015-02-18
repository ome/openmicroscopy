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
import java.util.Set;

import ome.io.nio.AbstractFileSystemService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class which sorts through a number of
 * file-based deletions before processing them.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.1.0-m3
 */
public class ThumbnailFileDeletions extends AbstractFileDeletions {

    private static final Logger log = LoggerFactory.getLogger(ThumbnailFileDeletions.class);

    public ThumbnailFileDeletions(AbstractFileSystemService afs, Set<Long> deletedIds) {
        super(afs, deletedIds);
        for (Long id : deletedIds) {
            String filePath = afs.getThumbnailPath(id);
            addLocalFile(new File(filePath), id);
        }
    }
}

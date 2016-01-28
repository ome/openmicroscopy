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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ome.io.nio.AbstractFileSystemService;
import ome.services.messages.DeleteLogMessage;
import ome.services.messages.DeleteLogsMessage;
import ome.system.OmeroContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deletes all repository files immediately. Other files which are considered
 * "local" are handled later by a call to @{link {@link #deleteLocal()}.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.1.0-m3
 */
public class OriginalFileDeletions extends AbstractFileDeletions {

    private static final Logger log = LoggerFactory.getLogger(OriginalFileDeletions.class);

    public OriginalFileDeletions(AbstractFileSystemService afs, Set<Long> deletedIds, OmeroContext ctx) {
        super(afs, deletedIds);

        // First we give the repositories a chance to delete
        // FS-based files.
        List<Long> orderedIds = new ArrayList<Long>(deletedIds);
        DeleteLogsMessage dlms = new DeleteLogsMessage(this, orderedIds);
        try {
            ctx.publishMessage(dlms);
            // We don't expect the message to throw. Instead we have to
            // evaluate its return value to see what failed.
            for (DeleteLogMessage dlm : dlms.getMessages()) {
                // If no logs were found via the publish
                // message, we have to assume that the files are local.
                // This may just log that the file doesn't exist.
                if (dlm.count() == 0) {
                    String filePath = afs.getFilesPath(dlm.getFileId());
                    addLocalFile(new File(filePath), dlm.getFileId());
                }
            }
        }
        catch (Throwable e) {
            // If an exception *is* thrown, we assume everything went awry.
            log.warn("Error on DeleteLogMessage", e);
            for (Long id : orderedIds) {
                // No way to calculate size!
                String filePath = afs.getFilesPath(id);
                File file = new File(filePath);
                fail(file, id, null);
            }
        }

    }
}

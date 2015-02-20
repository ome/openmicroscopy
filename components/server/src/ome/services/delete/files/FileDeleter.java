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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import ome.io.nio.AbstractFileSystemService;
import ome.system.OmeroContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.SetMultimap;

/**
 * Helper class which sorts through a number of
 * file-based deletions before processing them.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.1.0-m3
 */
public class FileDeleter {

    enum Type {
        OriginalFile,
        Pixels,
        Thumbnail;
    }

    private static final Logger log = LoggerFactory.getLogger(FileDeleter.class);

    private OmeroContext ctx;

    private final AbstractFileSystemService afs;

    private final SetMultimap<String, Long> deleteTargets;

    private OriginalFileDeletions originalFD;
 
    private ThumbnailFileDeletions thumbFD;

    private PixelsFileDeletions pixelsFD;

    private HashMap<String, long[]> undeletedFiles;

    private int filesFailed = 0;

    private long bytesFailed = 0;

	public FileDeleter(OmeroContext ctx, AbstractFileSystemService afs, SetMultimap<String, Long> deleteTargets) {
        this.ctx = ctx;
        this.afs = afs;
        this.deleteTargets = deleteTargets;
    }

    public void run() {
        originalFD = new OriginalFileDeletions(afs, load(Type.OriginalFile), ctx);
        filesFailed += originalFD.deleteLocal();
        bytesFailed += originalFD.getBytesFailed();

        thumbFD = new ThumbnailFileDeletions(afs, load(Type.Thumbnail));
        filesFailed += thumbFD.deleteLocal();
        bytesFailed += thumbFD.getBytesFailed();

        pixelsFD = new PixelsFileDeletions(afs, load(Type.Pixels));
        filesFailed += pixelsFD.deleteLocal();
        bytesFailed += pixelsFD.getBytesFailed();

        undeletedFiles = new HashMap<String, long[]>();
        undeletedFiles.put(Type.OriginalFile.toString(), originalFD.getUndeletedFiles());
        undeletedFiles.put(Type.Thumbnail.toString(), thumbFD.getUndeletedFiles());
        undeletedFiles.put(Type.Pixels.toString(), pixelsFD.getUndeletedFiles());

        if (log.isDebugEnabled()) {
            for (String table : undeletedFiles.keySet()) {
                log.debug("Failed to delete files : " + table + ":"
                        + Arrays.toString(undeletedFiles.get(table)));
            }
        }
    }

    /**
     * Lookup the ids which are scheduled for deletion.
     * @param fileType non-null
     * @return the IDs for that file type
     */
    protected Set<Long> load(Type fileType) {
        return deleteTargets.get(fileType.toString());
    }

    public HashMap<String, long[]> getUndeletedFiles() {
        return undeletedFiles;
    }

    public int getFailedFilesCount() {
        return filesFailed;
    }

    public String getWarning() {
        return String.format(
                "Warning: %s file(s) comprising %s bytes were not removed",
                filesFailed, bytesFailed);
    }
}

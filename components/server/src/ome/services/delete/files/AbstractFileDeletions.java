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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.io.nio.AbstractFileSystemService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for managing the removal of files from disk.
 *
 * @since 5.1.0-m3
 */
public abstract class AbstractFileDeletions {

    private static final Logger log = LoggerFactory.getLogger(AbstractFileDeletions.class);
    
    protected final Set<Long> deletedIds;
    
    protected final AbstractFileSystemService afs;

    private Map<File, Long> localFiles = new HashMap<File, Long>();

    private Map<File, Long> failedFiles = new HashMap<File, Long>();

    private long bytesFailed = 0;

    public AbstractFileDeletions(AbstractFileSystemService afs, Set<Long> deletedIds) {
        this.afs = afs;
    	this.deletedIds = deletedIds;
    }

    public void fail(File file, Long id, Long size) {
       failedFiles.put(file, id);
       if (size != null) {
           bytesFailed += size.longValue();
       }
    }

    /**
     * Called during the creation of instances if a particular file should
     * be handled by {@link #deleteLocal()}.
     * @param fileId
     * @param file
     */
    public void addLocalFile(File file, long fileId) {
        localFiles.put(file, fileId);
    }

    /**
     * Helper to delete and log. These files have not been handled elsewhere,
     * for example because they don't live in a repository.
     */
    public int deleteLocal() {
        for (Map.Entry<File, Long> entry: localFiles.entrySet()) {
            File file = entry.getKey();
            Long id = entry.getValue();
            if (file.exists()) {
                if (file.delete()) {
                    log.debug("DELETED: " + file.getAbsolutePath());
                } else {
                    log.debug("Failed to delete " + file.getAbsolutePath());
                    fail(file, id, file.length());
                }
            } else {
                log.debug("File " + file.getAbsolutePath() + " does not exist.");
            }
        }
        return failedFiles.size();
    }

    public long getBytesFailed() {
        return bytesFailed;
    }

    public long[] getUndeletedFiles() {
        List<Long> copy = new ArrayList<Long>(failedFiles.values());
        long[] ids = new long[copy.size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = copy.get(i);
        }
        return ids;
    }
}

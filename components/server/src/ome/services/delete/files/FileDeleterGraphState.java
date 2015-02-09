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

import java.util.Set;

import ome.io.nio.AbstractFileSystemService;
import ome.services.graphs.GraphState;
import ome.system.OmeroContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class which sorts through a number of
 * file-based deletions before processing them.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 5.1.0-m3
 * @deprecated will be removed in OMERO 5.2, so use the
 * <a href="http://www.openmicroscopy.org/site/support/omero5.1/developers/Server/ObjectGraphs.html">new graphs implementation</a>
 */
@Deprecated
public class FileDeleterGraphState extends FileDeleter {

    private static final Logger log = LoggerFactory.getLogger(FileDeleterGraphState.class);

    private final GraphState state;

    private final String type;

    private final long id;

    public FileDeleterGraphState(OmeroContext ctx, AbstractFileSystemService afs, GraphState state, String type, long id) {
        super(ctx, afs, null);
        this.state = state;
        this.type = type;
        this.id = id;
    }

    /**
     * Lookup the ids which are scheduled for deletion from the {@link GraphState}.
     * @param fileType non-null
     */
    @Override
    protected Set<Long> load(Type fileType) {
        Set<Long> deletedIds = state.getProcessedIds(fileType.toString());
        log.debug(String.format("Binary delete of %s for %s:%s: %s",
                fileType, type, id,
                deletedIds));
        return deletedIds;
    }
}

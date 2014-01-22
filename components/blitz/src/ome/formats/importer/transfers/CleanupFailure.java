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
import java.util.Collections;
import java.util.List;

/**
 * {@link Exception} thrown when cleaning up resources after transfer fails
 * partially or completely. The error should likely be shown to users to
 * permit manual cleanup.
 *
 * @since 5.0
 */
public class CleanupFailure extends Exception {

    private final List<File> failedFiles;

    public CleanupFailure(List<File> failedFiles) {
        this.failedFiles = Collections.unmodifiableList(failedFiles);
    }

    private static final long serialVersionUID = 1L;

    public List<File> getFailedFiles() {
        return failedFiles;
    }
}

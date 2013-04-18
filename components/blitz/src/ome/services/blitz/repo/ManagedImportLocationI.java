/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.blitz.repo;

import java.util.List;

import omero.grid.ImportLocation;

/**
 * Server-side implementation of {@link ImportLocation} for also storing the
 * {@link CheckedPath} instances for each used file.
 */
public class ManagedImportLocationI extends ImportLocation {

    private static final long serialVersionUID = 5067747111899744272L;

    public List<CheckedPath> checkedPaths;

    public CheckedPath logFile;

    /**
     * Return the server-side {@link CheckedPath} instance which can be passed to
     * a Bio-Formats reader.
     */
    public CheckedPath getTarget() {
        return checkedPaths.get(0);
    }

    /**
     * Return the server-side {@link CheckedPath} instance which can be used
     * for writing a log file for a fileset.
     */
    public CheckedPath getLogFile() {
        return logFile;
    }
}

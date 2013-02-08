/*
 * Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
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
import java.util.Stack;

/**
 * Creates a nested directory as with {@link File#mkdirs} while noting
 * state that allows the created directories to later be deleted.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
public class FleetingDirectory {
    /* the directory deletion that should follow *
     * (in Java SE 6 use a Deque instead) */
    private final Stack<File> created = new Stack<File>();
    
    /**
     * Ensure that the given directory exists, 
     * by creating it and its parents if necessary.
     * @param directory the directory that is to exist
     */
    public FleetingDirectory(File directory) {
        directory = directory.getAbsoluteFile();
        final Stack<File> toCreate = new Stack<File>();
        
        /* find which directories need to be created */
        while (!directory.exists()) {
            toCreate.push(directory);
            directory = directory.getParentFile();
        }
        
        /* create the directories, noting that they must later be deleted */
        while (!toCreate.isEmpty()) {
            final File nextToCreate = toCreate.pop();
            nextToCreate.mkdir();
            created.push(nextToCreate);
        }
    }
    
    /**
     * Delete the directories that were created in constructing this instance.
     */
    public void deleteCreated() {
        while (!created.isEmpty())
            created.pop().delete();
    }
}

/*
 * ome.services.blitz.repo.RepositoryListConfigI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 *
 * 
 */
package ome.services.blitz.repo;

import omero.grid.RepositoryListConfig;

/**
 *  A config class used to control listing
 * 
 * @author Colin Blackburn <cblackburn at dundee dot ac dot uk>
 */
public class RepositoryListConfigI extends RepositoryListConfig {
    
    RepositoryListConfigI() {
        depth = 1;
        files = true;
        dirs = true;
        system = false;
        registered = true;
    }

    RepositoryListConfigI(int depth, boolean files, boolean dirs, boolean system, boolean registered) {
        depth = depth;
        files = files;
        dirs = dirs;
        system = system;
        registered = registered;
    }
    
    public void setDepth(int depth) {
        depth = depth;
    }
    
    public int getDepth() {
        return depth;
    }

    public void setFiles(boolean files) {
       files = files;
    }
    
    public boolean getFiles() {
        return files;
    }

    public void setDirs(boolean dirs) {
        dirs = dirs;
    }
    
    public boolean getDirs() {
        return dirs;
    }

    public void setSystem(boolean system) {
        system = system;
    }
    
    public boolean getSystem() {
        return system;
    }

    public void setRegistered(boolean registered) {
        registered = registered;
    }
    
    public boolean getRegistered() {
        return registered;
    }
    
}

/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import omero.grid.RepositoryListConfig;

/**
 *  A config class used to control listing
 *
 * @since Beta4.1
 */
public class RepositoryListConfigI extends RepositoryListConfig {
    
    RepositoryListConfigI() {
        depth = 0;
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

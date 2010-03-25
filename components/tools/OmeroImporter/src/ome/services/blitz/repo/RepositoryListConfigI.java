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
        registered = false;
    }

    RepositoryListConfigI(int depth, boolean files, boolean dirs, boolean system, boolean registered) {
        depth = depth;
        files = files;
        dirs = dirs;
        system = system;
        registered = registered;
    }
}

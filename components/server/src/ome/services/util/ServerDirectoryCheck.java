/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import java.io.File;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hook run by the context at startup to create needed directories for the server.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.3.0
 */
public class ServerDirectoryCheck implements Runnable {

    public final static Logger log = LoggerFactory
            .getLogger(ServerDirectoryCheck.class);

    final String omeroDataDir;

    public ServerDirectoryCheck(String omeroDataDir) {
        this.omeroDataDir = omeroDataDir;
    }

    /**
     * Synchronizes the ${omero.data.dir}/Server directory by creating a
     * directory for any user who does not have one. Does not currently remove
     * directories.
     */
    public void run() {
        createDirectories();
    }

    public void createDirectories() {
        for (String directory : Arrays.asList("FullText", "ManagedRepository")) {
            File f = new File(omeroDataDir + File.separator + directory);
            if (f.mkdirs()) {
                log.info("Creating " + f);
            }
        }
    }

}

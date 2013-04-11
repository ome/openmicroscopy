/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hook run by the context. This hook prints an informative message on
 * {@link #start()} and {@link #stop()}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class ServerVersionCheck {

    public final static Logger log = LoggerFactory.getLogger(ServerVersionCheck.class);

    private final String version;

    public ServerVersionCheck(String version) {
        this.version = version;
    }

    /**
     * Prints simple banner with OMERO version.
     */
    public void start() throws Exception {
        printBanner("OMERO Version: %s Ready.", version);
    }

    /**
     * Prints simple banner with "Stopping OMERO"
     */
    public void stop() throws Exception {
        printBanner("Stopping OMERO...");
    }

    private void printBanner(String format, Object... objs) {
        log.info("-------------------------------------------------");
        log.info(String.format(format, objs));
        log.info("-------------------------------------------------");
    }

}

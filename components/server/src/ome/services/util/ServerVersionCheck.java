/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import java.util.ResourceBundle;

import ome.system.Version;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hook run by the context. This hook prints an informative message on
 * {@link #start()} and {@link #stop()}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class ServerVersionCheck {

    public static Log log = LogFactory.getLog(ServerVersionCheck.class);

    ResourceBundle bundle = ResourceBundle.getBundle("omero");

    /**
     * Prints simple banner with OMERO version.
     */
    public void start() throws Exception {
        printBanner("OMERO Version: %s (Rev: %s) Ready.", Version.OMERO,
                Version.getRevision(Version.class));
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

/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.nio.utests;

import ome.system.OmeroContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used to find the "omero.data.dir" environment variable which points to the
 * location used by the server for storing data. This is done by creating an
 * {@link OmeroContext} with "ome/config.xml" (previously
 * "ome/services/config-local.xml"). Primarily useful for testing.
 * 
 * Any other attempt to acquire the omero.data.dir directly may cause issues in
 * non-standard environments.
 * 
 * @see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/800">ticket:800</a>
 */
public class PathUtil {

    private final static Log log = LogFactory.getLog(PathUtil.class);

    private final static PathUtil instance = new PathUtil();

    private final String omeroDataDir;

    private PathUtil() {
        String dataDir;
        try {
            OmeroContext c = new OmeroContext(
                    new String[] { "classpath:ome/config.xml" });
            dataDir = c.getProperty("omero.data.dir");
        } catch (Exception e) {
            dataDir = "/OMERO";
            log.error("Could not find \"omero.data.dir\" "
                    + "in OmeroContext with ome/config.xml", e);
        }
        omeroDataDir = dataDir;
    }

    public static PathUtil getInstance() {
        return instance;
    }

    public String getDataFilePath() {
        return omeroDataDir;
    }

}

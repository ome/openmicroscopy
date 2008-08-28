/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used to find the "omero.data.dir" environment variable which points to the
 * location used by the server for storing data. This is read from the "omero"
 * {@link ResourceBundle}, or if all else fails is set to "/OMERO". This is
 * primarily useful for testing.
 */
public class PathUtil {

    private final static Log log = LogFactory.getLog(PathUtil.class);

    private final static PathUtil instance = new PathUtil();

    private final String omeroDataDir;

    private PathUtil() {
        String dataDir;
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("omero");
            dataDir = bundle.getString("omero.data.dir");
        } catch (Exception e) {
            dataDir = "/OMERO";
            log.error("Could not find \"omero.data.dir\" "
                    + "in \"omero\" ResourceBundle", e);
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

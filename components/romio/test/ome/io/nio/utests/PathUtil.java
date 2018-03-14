/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.io.nio.utests;

import java.io.File;
import java.util.UUID;

import ome.system.OmeroContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to find the "omero.data.dir" environment variable which points to the
 * location used by the server for storing data. This is done by creating an
 * {@link OmeroContext} with "ome/config.xml" (previously
 * "ome/services/config-local.xml"). Primarily useful for testing.
 * 
 * Any other attempt to acquire the omero.data.dir directly may cause issues in
 * non-standard environments.
 * 
 * @see <a href="https://trac.openmicroscopy.org/ome/ticket/800">ticket:800</a>
 */
class PathUtil {

    private final static Logger log = LoggerFactory.getLogger(PathUtil.class);

    private final static PathUtil instance = new PathUtil();

    public static PathUtil getInstance() {
        return instance;
    }

    public String getTemporaryDataFilePath() {
        File tempdir = new File(System.getProperty("java.io.tmpdir"));
        String uuid = UUID.randomUUID().toString();
        tempdir = new File(tempdir, uuid);
        tempdir.mkdirs();
        String path = tempdir.getAbsolutePath() + "/";
        log.debug("Created temporary directory: " + path);
        return path;
    }
}

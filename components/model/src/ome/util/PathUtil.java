/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import ome.conditions.InternalException;

import org.springframework.util.ResourceUtils;

public class PathUtil {

    private final String SPRING_FILE_PATH = "components/romio/resources/beanRefContext.xml";

    private final String OMERO_PROPS = "classpath:omero.properties";

    private static PathUtil instance = null;

    private static Properties properties;

    private PathUtil() {
        properties = new Properties();
        File f = null;
        FileInputStream fis = null;
        try {
            f = ResourceUtils.getFile(OMERO_PROPS);
            fis = new FileInputStream(f);
            properties.load(fis);
        } catch (IOException e) {
            throw new InternalException("Could not load omero.properties:");
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    // ok
                }
            }
        }
    }

    public static PathUtil getInstance() {
        if (instance == null) {
            return new PathUtil();
        } else {
            return instance;
        }
    }

    public String getDataFilePath() {
        String path = properties.getProperty("omero.data.dir");
        return path;
    }

}

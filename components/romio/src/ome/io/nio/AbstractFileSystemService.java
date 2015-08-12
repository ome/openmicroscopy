/*
 * ome.io.nio.AbstractFileSystemService
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

import java.io.File;
import java.util.Formatter;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author callan
 * 
 */
public class AbstractFileSystemService {

    /** The logger for this particular class */
    private static Logger log = LoggerFactory.getLogger(AbstractFileSystemService.class);

    public final static String ROOT_DEFAULT = File.separator + "OMERO"
            + File.separator;

    public final static String PIXELS_PATH = "Pixels" + File.separator;

    public final static String FILES_PATH = "Files" + File.separator;

    public final static String THUMBNAILS_PATH = "Thumbnails" + File.separator;

    private final String root;

    public AbstractFileSystemService(String path) {
        File rootDirectory = new File(path);
        if (!rootDirectory.isDirectory() || !rootDirectory.canRead()
                || !rootDirectory.canWrite()) {
            throw new IllegalArgumentException(
                    "Invalid directory specified for file system service."
		    + rootDirectory);
        }

        this.root = rootDirectory.getAbsolutePath();
        if (log.isDebugEnabled()) {
            log.debug("Using root path: '" + this.root + "'");
        }
    }

    /**
     * Makes sure that for a given path, it's subpath exists. For example, given
     * the path "/foo/bar/foobar.txt" the method will make sure the directory
     * structure "/foo/bar" exists.
     *
     * @param path
     *            the path to check for subpath existance.
     */
    protected void createSubpath(String path) {
        File file = new File(path);
        if (!file.exists()) {
            File directory = new File(file.getParent());
            if (!directory.exists()) {
                directory.mkdirs();
            }
        }
    }

    /**
     * Returns a numbered path relative to the root of this service, but is
     * ignorant of FS and similar constructs. For example, given an id of
     * 12345 this will return "ROOT/Pixels/Dir-123/Dir-456/123456"
     *
     * @param id   the Pixels identifier
     * @return     the path relative to the root
     */
    public String getPixelsPath(Long id) {
        return getPath(PIXELS_PATH, id);
    }

    /**
     * Returns a numbered path relative to the root of this service, but is
     * ignorant of FS and similar constructs. For example, given an id of
     * 123456 this will return "ROOT/Files/Dir-123/Dir-456/123456"
     *
     * @param id   the Files identifier
     * @return     the path relative to the root
     */
    public String getFilesPath(Long id) {
        return getPath(FILES_PATH, id);
    }

    /**
     * Returns a numbered path relative to the root of this service, but is
     * ignorant of FS and similar constructs. For example, given an id of
     * 123456 this will return "ROOT/Thumbnails/Dir-123/Dir-456/123456"
     *
     * @param id     the thumbnail identifier
     * @return       the path relative to the root
     */
    public String getThumbnailPath(Long id) {
        return getPath(THUMBNAILS_PATH, id);
    }

    private String getPath(String prefix, Long id) {
        String suffix = "";
        Long remaining = id;
        Long dirno = 0L;

        if (id == null) {
            throw new NullPointerException("Expecting a not-null id.");
        }

        while (remaining > 999) {
            remaining /= 1000;

            if (remaining > 0) {
                Formatter formatter = new Formatter();
                dirno = remaining % 1000;
                suffix = formatter.format("Dir-%03d", dirno).out().toString()
                        + File.separator + suffix;
            }
        }
        
        String path = FilenameUtils.concat(root, prefix + suffix + id);
        return path;
    }
}

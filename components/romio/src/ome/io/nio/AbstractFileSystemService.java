/*
 * ome.io.nio.AbstractFileSystemService
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

import java.io.File;
import java.text.DateFormatSymbols;
import java.util.Formatter;
import java.util.Calendar;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author callan
 * 
 */
public class AbstractFileSystemService {

    /** The logger for this particular class */
    private static Log log = LogFactory.getLog(AbstractFileSystemService.class);

    public final static String ROOT_DEFAULT = File.separator + "OMERO"
            + File.separator;

    public final static String PIXELS_PATH = "Pixels" + File.separator;

    public final static String FILES_PATH = "Files" + File.separator;

    // FIXME: This should ultimately come from somewhere else
    public final static String MANAGED_REPO_PATH = "ManagedRepository" + File.separator;

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
     * ignorant of FS and similar constructs. For example, given an id of 123456
     * this will return "ROOT/Pixels/Dir-123/Dir-456/123456"
     *
     * Should be marked protected in 4.4  because assumptions on the existence
     * of this file can be dangerous.
     *
     * @param id
     * @return
     */
    public /*protected*/ String getPixelsPath(Long id) {
        return getPath(PIXELS_PATH, id);
    }

    /**
     * Returns a numbered path relative to the root of this service
     * using FS template. For example, given an id of 123456 this
     * may return "ROOT/ManagedRepository/myGroup/me/2011/01/01/123456"
     *
     * @param id
     * @param template
     * @param user
     * @param group
     * @return
     */
    public  String getFilesPath(Long id, String template, String user, String group) {
        Calendar now = Calendar.getInstance();
        DateFormatSymbols dfs = new DateFormatSymbols();
        String path = FilenameUtils.concat(root, MANAGED_REPO_PATH);
        String dir = "";
        String[] elements = template.split("/");
        for (String part : elements) {
            if (part.equals("%fileid%"))
                dir = id.toString();
            else if (part.equals("%groupname%"))
                dir = group;
            else if (part.equals("%username%"))
                dir = user;
            else if (part.equals("%year%"))
                dir = Integer.toString(now.get(Calendar.YEAR));
            else if (part.equals("%month%"))
                dir = Integer.toString(now.get(Calendar.MONTH)+1);
            else if (part.equals("%monthname%"))
                dir = dfs.getMonths()[now.get(Calendar.MONTH)];
            else if (part.equals("%day%"))
                dir = Integer.toString(now.get(Calendar.DAY_OF_MONTH));
            else if (!part.endsWith("%") && !part.startsWith("%"))
                dir = part;
            else {
                log.warn("Ignored unrecognised token in template: " + part);
                dir = "";
            }
            path = FilenameUtils.concat(path, dir);
        }
        return path;
    }

    /**
     * Returns a numbered path relative to the root of this service, but is
     * ignorant of FS and similar constructs. For example, given an id of 123456
     * this will return "ROOT/Files/Dir-123/Dir-456/123456"
     *
     * Should be marked protected in 4.4  because assumptions on the existence
     * of this file can be dangerous.
     *
     * @param id
     * @return
     */
    public /*protected*/ String getFilesPath(Long id) {
        return getPath(FILES_PATH, id);
    }

    /**
     * Returns a numbered path relative to the root of this service, but is
     * ignorant of FS and similar constructs. For example, given an id of 123456
     * this will return "ROOT/Thumbnails/Dir-123/Dir-456/123456"
     *
     * Should be marked protected in 4.4  because assumptions on the existence
     * of this file can be dangerous.
     *
     * @param id
     * @return
     */
    public /*protected*/ String getThumbnailPath(Long id) {
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

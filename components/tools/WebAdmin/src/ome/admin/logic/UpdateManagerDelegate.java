/*
 * ome.admin.logic
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.admin.logic;

// Java imports
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// Application-internal dependencies
import ome.admin.validator.FileValidator;

/**
 * Delegate of Update mangement.
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class UpdateManagerDelegate {

    /**
     * String path
     */
    private String path = null;

    /**
     * Gets path
     * 
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets path
     * 
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Get list of files
     * 
     * @return
     */
    public List<String> getFiles() {
        File f = new File(this.path);
        File[] files = null;
        List<String> list = new ArrayList<String>();

        if (f.isDirectory()) {
            files = f.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()
                        && FileValidator.validFileName(files[i].getName()))
                    list.add(files[i].getName());
            }
        }
        return list;
    }

    /**
     * Gets dir
     * 
     * @return
     */
    public List<String> getDirs() {
        File f = new File(this.path);
        File[] dirs = null;
        List<String> list = new ArrayList<String>();

        if (f.isDirectory()) {
            dirs = f.listFiles();
            for (int i = 0; i < dirs.length; i++) {
                if (dirs[i].isDirectory())
                    list.add(dirs[i].getName());
            }
        }
        return list;
    }

}

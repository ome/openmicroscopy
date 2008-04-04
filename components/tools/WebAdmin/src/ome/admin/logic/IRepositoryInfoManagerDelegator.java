/*
 * ome.admin.logic
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.admin.logic;

// Java imports
import java.util.HashMap;

// Third-party libraries
import org.apache.log4j.Logger;

// Application-internal dependencies
import ome.admin.data.ConnectionDB;

/**
 * Delegate of repository mangement.
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class IRepositoryInfoManagerDelegator implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * log4j logger
     */
    static Logger logger = Logger
            .getLogger(IRepositoryInfoManagerDelegator.class.getName());

    /**
     * 
     */
    private long space = 0L;

    /**
     * {@link ome.admin.data.ConnectionDB}
     */
    private ConnectionDB db = new ConnectionDB();

    /**
     * Creates a new instance of IRepositoryInfoManagerDelegator.
     */
    public IRepositoryInfoManagerDelegator() {
    }

    /**
     * Gets drive space in kilobytes.
     * 
     * @param type
     *            {@link java.lang.String} type of space
     * @return long - space in kilobytes
     */
    public long getSpaceInKilobytes(String type) {
        if (type.equals("used"))
            this.space = db.getUsedSpaceInKilobytes();
        if (type.equals("free"))
            this.space = db.getFreeSpaceInKilobytes();
        return space;
    }

    /**
     * Gets TopTen user space in kilobytes.
     * 
     * @return {@link java.util.HashMap} ExperimenterId, used space in Bytes
     */
    public HashMap getTopTenUserSpace() {
        return db.getTopTen();
    }

}

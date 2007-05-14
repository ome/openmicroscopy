/*
* ome.admin.logic
*
*   Copyright 2007 University of Dundee. All rights reserved.
*   Use is subject to license terms supplied in LICENSE.txt
*/

package ome.admin.logic;

//Java imports
import java.util.HashMap;

import ome.admin.data.ConnectionDB;

//Third-party libraries

//Application-internal dependencies

/**
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class IRepositoryInfoManagerDelegator implements java.io.Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
     * @param String type of space
     * @return long - space in kilobytes
	 */
    public long getSpaceInKilobytes(String type) {
        if(type.equals("used"))
            this.space = db.getUsedSpaceInKilobytes();
        if(type.equals("free"))
            this.space = db.getFreeSpaceInKilobytes();
        return space;
    }

    /**
     * Gets TopTen user space in kilobytes.
     * @return {@link java.util.HashMape} used space in Bytes
	 */
    public HashMap getTopTenUserSpace() {
    	return db.getTopTen();
    }

}


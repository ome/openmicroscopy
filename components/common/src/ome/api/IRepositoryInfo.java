/*
 *  Copyright (C) 2007-2008 Glencoe Software, Inc. All rights reserved.
 *  Use is subject to license terms supplied in LICENSE.txt
 */
package ome.api;

import ome.conditions.InternalException;

/**
 * Provides methods for obtaining information for server repository disk space
 * allocation. Could be used generically to obtain usage information for any 
 * mount point, however, this interface is prepared for the API to provide 
 * methods to obtain usage info for the server filesystem containing the image
 * uploads. For the OMERO server base this is /OMERO. For this implementation 
 * it could be anything e.g. /Data1. 
 * <p>
 * Methods that fail or cannot execute on the server will throw an
 * InternalException. This would not be normal and would indicate some server or
 * disk failure.
 * </p>
 * <p>
 * Copyright 2007 Glencoe Software Inc. All rights reserved.
 * Use is subject to license terms supplied in LICENSE.txt 
 * </p>
 *
 * @author David L. Whitehurst &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:david@glencoesoftware.com">david@glencoesoftware.com</a>
 * @version $Revision$
 * @since 3.0
 */
public interface IRepositoryInfo extends ServiceInterface {

	/**
	 * This method returns the total space in bytes for this file system
	 * including nested subdirectories.  The Java 6 J2SE provides this 
	 * functionality now using similar methods in the class java.io.File. A 
	 * refactoring of related classes should be performed when the later SDK 
	 * is adopted.
	 * 
	 * @return Total space used on this file system.
	 * @throws ResourceError If there is a problem retrieving disk space used.
	 */
	public long getUsedSpaceInKilobytes();

	/**
	 * This method returns the free or available space on this file system
	 * including nested subdirectories. The Java 6 J2SE provides this 
	 * functionality now using similar methods in the class java.io.File. A 
	 * refactoring of related classes should be performed when the later SDK 
	 * is adopted.
	 * 
	 * @return Free space on this file system in KB.
	 * @throws ResourceError If there is a problem retrieving disk space free.
	 */
	public long getFreeSpaceInKilobytes();

	/**
	 * This method returns a double of the used space divided by the free space.
	 * This method will be called by a client to watch the repository 
	 * filesystem so that it doesn't exceed 95% full.
	 * 
	 * @return Fraction of used/free.
	 * @throws ResourceError If there is a problem calculating the usage
	 * fraction.
	 */
	public double getUsageFraction();
	
    /**
     * Checks that image data repository has not exceeded 95% disk space use
     * level.
     * @throws ResourceError If the repository usage has exceeded 95%.
     * @throws InternalException If there is a critical failure while sanity
     * checking the repository.
     */
	public void sanityCheckRepository();
    
    /**
     * Removes all files from the server that do not have an OriginalFile
     * complement in the database, all the Pixels that do not have a complement
     * in the database and all the Thumbnail's that do not have a complement in
     * the database.
     * @throws ResourceError If deletion fails.
     */
    public void removeUnusedFiles();
}

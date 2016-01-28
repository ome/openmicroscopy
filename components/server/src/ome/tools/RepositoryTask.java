/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.tools;

import java.util.ArrayList;
import java.util.List;

import ome.api.IRepositoryInfo;
import ome.util.SqlAction;

/**
 * Class implementation of various mechanized tasks, database queries, file I/O,
 * etc. This class is used by the public services provided by IRepositoryInfo
 * 
 * @since 3.0
 * @see IRepositoryInfo
 */
public class RepositoryTask {

	final private SqlAction sql;

	public RepositoryTask(SqlAction sql) {
	    this.sql = sql;
	}
	
	/**
	 * This public method is used to return a list of file ids that require
	 * deletion from the disk repository.
	 * 
	 * @return List<Long> representing the ids for files that were deleted
	 */
	public List<Long> getFileIds() {
	    return sql.getDeletedIds("ome.model.core.OriginalFile");
	}
	
	/**
	 * This public method is used to return a list of pixel ids that require
	 * deletion from the disk repository.
	 * 
	 * @return List<Long> representing the ids for pixels that were deleted
	 */
	public List<Long> getPixelIds() {
	    return sql.getDeletedIds("ome.model.core.Pixels");
	}
	
	/**
	 * This public method is used to return a list of thumbnail ids that require
	 * deletion from the disk repository.
	 * 
	 * @return List<Long> representing the ids for thumbnails that were deleted
	 */
	public List<Long> getThumbnailIds() {
	    return sql.getDeletedIds("ome.model.display.Thumbnail");
	}
}

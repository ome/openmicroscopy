/*
 * ome.io.nio.OriginalFilesService
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.conditions.ResourceError;
import ome.model.core.OriginalFile;

/**
 * Raw file service which provides access to <code>FileBuffers</code>.
 * 
 * @author Chris Allan &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/08 15:21:59 $) </small>
 * @since OMERO3.0
 */
public class OriginalFilesService extends AbstractFileSystemService {

	/* The logger for this class. */
	private transient static Logger log = LoggerFactory
			.getLogger(OriginalFilesService.class);

    /**
     * Constructor
     * @param path
     */
	@Deprecated
    public OriginalFilesService(String path) {
        super(path);
    }

    public OriginalFilesService(String path, boolean isReadOnlyRepo) {
        super(path, isReadOnlyRepo);
    }

    /**
     * Returns FileBuffer based on OriginalFile path
     * 
     * @param file
     * @return FileBuffer
     */
    public FileBuffer getFileBuffer(OriginalFile file, String mode) {
        String path = getFilesPath(file.getId());
        createSubpath(path);
        return new FileBuffer(path, mode);
    }
    
    /**
     * Removes files from data repository based on a parameterized List of
     * Long file ids
     * 
     * @param fileIds - Long file keys to be deleted
     * @throws ResourceError If deletion fails.
     */
    public void removeFiles(List<Long> fileIds){

    	File file;
    	boolean success = false;
    	
    	for (Iterator<Long> iter = fileIds.iterator(); iter.hasNext();) {
			Long id = iter.next();

			String filePath = getFilesPath(id);
			file = new File(filePath);
			if (file.exists()) {
				success = file.delete();
				if (!success) {
					throw new ResourceError("File " + file.getName()
							+ " deletion failed");
				} else {
					if (log.isInfoEnabled()) {
						log.info("INFO: File " + file.getName() + " deleted.");
					}
				}
			}
		}
    }
    
    /**
     * Returns whether or not an OriginalFile exists on disk.
     * 
     * @param file The original file metadata.
     * @return See above.
     */
    public boolean exists(OriginalFile file) {
        String path = getFilesPath(file.getId());
        return new File(path).exists();
    }
}

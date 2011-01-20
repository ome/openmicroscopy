/*
 * org.openmicroscopy.shoola.env.data.views.calls.FilesLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.views.calls;


//Java imports
import java.io.File;
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

import pojos.FileAnnotationData;

/** 
 * Loads collection of files or a specified file.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FilesLoader 
	extends BatchCallTree
{

	/** Loads the specified annotations. */
    private BatchCall   loadCall;
    
    /** The result of the call. */
    private Object		result;
    
    /**
     * Creates a {@link BatchCall} to download a file previously loaded.
     * 
	 * @param file	The absolute form of this abstract pathname.
	 * @param fileID		The id of the file to download.
	 * @param size			The size of the file.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final File file, 
    						final long fileID, final long size)
    {
        return new BatchCall("Downloading files.") {
            public void doCall() throws Exception
            {
                OmeroMetadataService service = context.getMetadataService();
                result = service.downloadFile(file, fileID, size);
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to download a file previously loaded.
     * 
	 * @param fileAnnotationID	The id of the file to download.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final long fileAnnotationID)
    {
        return new BatchCall("Downloading files.") {
            public void doCall() throws Exception
            {
                OmeroMetadataService service = context.getMetadataService();
                FileAnnotationData fa = (FileAnnotationData) 
                	service.loadAnnotation(fileAnnotationID);
                Map<FileAnnotationData, File> m = 
                	new HashMap<FileAnnotationData, File>();
                File f = service.downloadFile(new File(fa.getFileName()), 
                		fa.getFileID(), fa.getFileSize());
                m.put(fa, f);
                result = m;
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to load the files identified by
     * the passed type.
     * 
     * @param type 		The type of files to load.
	 * @param userID    The id of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeLoadFilesBatchCall(final int type, final long userID)
    {
        return new BatchCall("Downloading files.") {
            public void doCall() throws Exception
            {
                OmeroMetadataService service = context.getMetadataService();
                result = service.loadFiles(type, userID);
            }
        };
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns the collection of archives files.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Creates a new instance.
     * 
	 * @param file	 	The file where to write the data.
	 * @param fileID	The id of the file to download.
	 * @param size		The size of the file.
     */
    public FilesLoader(File file, long fileID, long size)
    {
    	if (file == null) loadCall = makeBatchCall(fileID);
    	else loadCall = makeBatchCall(file, fileID, size);
    }
    
    /**
     * Creates a new instance.
     * 
	 * @param type 		The type of files to load.
	 * @param userID    The id of the user.
     */
    public FilesLoader(int type, long userID)
    {
    	loadCall = makeLoadFilesBatchCall(type, userID);
    }
    
}

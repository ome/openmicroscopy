/*
 * org.openmicroscopy.shoola.env.data.views.calls.FilesLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import omero.model.FileAnnotation;
import omero.model.OriginalFile;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
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

	/** Indicates to load the original file if original file is not set.*/
	public static final int ORIGINAL_FILE = 0;
	
	/** Indicates to load the file annotation if original file is not set.*/
	public static final int FILE_ANNOTATION = 1;
	
	/** Indicates to load the metadata from the image ID.*/
	public static final int METADATA_FROM_IMAGE = 2;
	
	/** Loads the specified annotations. */
    private BatchCall loadCall;
    
    /** The result of the call. */
    private Object result;
    
    /** The result of the call. */
    private Object currentFile;
    
    /** The files to load. */
    private Map<FileAnnotationData, File> files;
    
    /** The security context.*/
    private SecurityContext ctx;
    
    /**
     * Creates a {@link BatchCall} to download a file previously loaded.
     * 
	 * @param file The absolute form of this abstract pathname.
	 * @param fileID The id of the file to download.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeBatchCall(final File file, final long fileID)
    {
    	return new BatchCall("Downloading files.") {
    		public void doCall() throws Exception
    		{
    			OmeroMetadataService service = context.getMetadataService();
    			result = service.downloadFile(ctx, file, fileID);
    		}
    	};
    }
    
    /**
     * Creates a {@link BatchCall} to download a file previously loaded.
     * 
	 * @param id The id of the file annotation to download.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeFileBatchCall(final long id)
    {
    	return new BatchCall("Downloading files.") {
    		public void doCall() throws Exception
    		{
    			OmeroMetadataService service = context.getMetadataService();
    			FileAnnotationData fa = (FileAnnotationData) 
    					service.loadAnnotation(ctx, id);
    			result = service.downloadFile(ctx, new File(fa.getFileName()),
    					fa.getFileID());
    		}
    	};
    }
    
    /**
     * Creates a {@link BatchCall} to download the metadata associated to the
     * image.
     * 
     * @param file where to write the content.
	 * @param id The id of the image to handle.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeFromImageBatchCall(final File file, final long id)
    {
    	return new BatchCall("Downloading original metadata.") {
    		public void doCall() throws Exception
    		{
    			OmeroMetadataService service = context.getMetadataService();
    			result = service.downloadMetadataFile(ctx, file, id);
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
    					service.loadAnnotation(ctx, fileAnnotationID);
    			Map<FileAnnotationData, File> m = 
    					new HashMap<FileAnnotationData, File>();
    			File f = service.downloadFile(ctx, new File(fa.getFileName()),
    					fa.getFileID());
    			m.put(fa, f);
    			result = m;
    		}
    	};
    }

    /** 
     * Loads the specified file.
     * 
     * @param fa The file annotation to handle.
     * @param f  The file to load.
     */
    private void loadFile(final FileAnnotationData fa, final File f)
    {
    	OmeroMetadataService service = context.getMetadataService();
    	Map<FileAnnotationData, File> m = 
    			new HashMap<FileAnnotationData, File>();
    	OriginalFile of;
    	of = ((FileAnnotation) fa.asAnnotation()).getFile();
    	try {
    		service.downloadFile(ctx, f, of.getId().getValue());
    		m.put(fa, f);
    		currentFile = m;
    	} catch (Exception e) {
    		m.put(fa, null);
    		context.getLogger().error(this,
    				"Cannot retrieve file: "+e.getMessage());
    	}
    }
    
    
    /**
     * Creates a {@link BatchCall} to load the files identified by
     * the passed type.
     * 
     * @param type The type of files to load.
	 * @param userID The id of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall makeLoadFilesBatchCall(final int type, final long userID)
    {
    	return new BatchCall("Downloading files.") {
    		public void doCall() throws Exception
    		{
    			OmeroMetadataService service = context.getMetadataService();
    			result = service.loadFiles(ctx, type, userID);
    		}
    	};
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree()
    { 
    	if (files == null && loadCall != null) add(loadCall);
    	else if (files != null) {
    		result = null;
    		Iterator<Entry<FileAnnotationData, File>>
    		i = files.entrySet().iterator();
    		Entry<FileAnnotationData, File> entry;
    		String description = "Loading file";
    		while (i.hasNext()) {
    			entry = i.next();
				final FileAnnotationData fa = entry.getKey();
				final File f = entry.getValue();
				add(new BatchCall(description) {
            		public void doCall() { loadFile(fa, f); }
            	});
			}
    	}
    }

    /**
     * Returns the collection of archives files.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Returns the lastly retrieved file.
     * This will be packed by the framework into a feedback event and
     * sent to the provided call observer, if any.
     * 
     * @return A Map containing the file and file annotation.
     */
    protected Object getPartialResult() { return currentFile; }
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
	 * @param file The file where to write the data.
	 * @param fileID The id of the file to download.
     */
    public FilesLoader(SecurityContext ctx, File file, long fileID)
    {
    	this.ctx = ctx;
    	if (file == null) loadCall = makeBatchCall(fileID);
    	else loadCall = makeBatchCall(file, fileID);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
	 * @param file The file where to write the data.
	 * @param fileID The id of the file to download.
	 * @param index One of the constants defined by this class.
     */
    public FilesLoader(SecurityContext ctx, File file, long fileID, int index)
    {
    	this.ctx = ctx;
    	if (file == null || index == FILE_ANNOTATION) 
    		loadCall = makeFileBatchCall(fileID);
    	else {
    		if (index == METADATA_FROM_IMAGE)
    			loadCall = makeFromImageBatchCall(file, fileID);
    		else loadCall = makeBatchCall(file, fileID);
    	}
    }
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
	 * @param files The files to load.
     */
    public FilesLoader(SecurityContext ctx, Map<FileAnnotationData, File> files)
    {
    	this.ctx = ctx;
    	if (files == null) 
    		throw new IllegalArgumentException("No files to load.");
    	this.files = files;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
	 * @param type The type of files to load.
	 * @param userID The id of the user.
     */
    public FilesLoader(SecurityContext ctx, int type, long userID)
    {
    	this.ctx = ctx;
    	loadCall = makeLoadFilesBatchCall(type, userID);
    }

}

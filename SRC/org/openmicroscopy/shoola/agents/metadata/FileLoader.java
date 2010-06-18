/*
 * org.openmicroscopy.shoola.agents.metadata.FileLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata;


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
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.FileAnnotationData;

/** 
 * Loads images like PNG, TIFF etc linked to a given object.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class FileLoader 
	extends EditorLoader
{

	/** The absolute of the new file. */
	private File 				file;
	
    /** The object the thumbnails are for. */
    private FileAnnotationData 	data;
    
    /** The component where to feed the results back to. */
    private Object 				uiView;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  		handle;
    
    /** The files to load. */
    private Map<FileAnnotationData, Object> files;
    
    /** The files to load. */
    private Map<FileAnnotationData, File> filesMap;
    
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer 	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
	 * @param data		The annotation hosting the file to load.
	 * @param uiView 	The object to handle.
	 */
	public FileLoader(Editor viewer, FileAnnotationData data, Object uiView)
	{
		super(viewer);
		if (data == null)
			throw new IllegalArgumentException("No data set.");
		this.data = data;
		this.uiView = uiView;
		file = new File(MetadataViewerAgent.getOmeroFilesHome() 
				+File.separator+data.getFileName());
		file.deleteOnExit();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer 	The viewer this data loader is for.
	 * 					Mustn't be <code>null</code>.
	 * @param files		The files to load.
	 */
	public FileLoader(Editor viewer, Map<FileAnnotationData, Object> files)
	{
		super(viewer);
		if (files == null)
			throw new IllegalArgumentException("No data set.");
		this.files = files;
	}
	
	/** 
	 * Cancels the data loading. 
	 * @see EditorLoader#cancel()
	 */
	public void cancel()
	{ 
		handle.cancel();
		if (file != null) file.delete();
		if (filesMap != null) {
			Entry entry;
    		Iterator i = filesMap.entrySet().iterator();
    		File f;
    		while (i.hasNext()) {
				entry = (Entry) i.next();
				f = (File) entry.getValue();
				f.delete();
			}
		}
	}

	/** 
	 * Downloads the file. 
	 * @see EditorLoader#cancel()
	 */
	public void load()
	{
		if (data != null) {
			OriginalFile f = ((FileAnnotation) data.asAnnotation()).getFile();
			if (f.isLoaded()) {
				handle = mhView.loadFile(file, f.getId().getValue(), 
						f.getSize().getValue(), this);
			}
		} else {
			Entry entry;
    		Iterator i = files.entrySet().iterator();
    		FileAnnotationData fa;
    		filesMap = new HashMap<FileAnnotationData, File>(files.size());
    		File f;
    		//int index = 0;
    		while (i.hasNext()) {
				entry = (Entry) i.next();
				fa = (FileAnnotationData) entry.getKey();
				f = new File(MetadataViewerAgent.getOmeroFilesHome() 
						+File.separator+fa.getFileName());
				f.deleteOnExit();
				filesMap.put(fa, f);
				//index++;
			}
			handle = mhView.loadFiles(filesMap, this);
		}
		
	}
	
    /** 
     * Feeds the file back to the viewer, as they arrive. 
     * @see EditorLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
       if (data == null) {
    	   Map m = (Map) fe.getPartialResult();
    	   if (m != null) {
    		   Entry entry;
        	   Iterator i = m.entrySet().iterator();
        	   FileAnnotationData fa;
        	   while (i.hasNext()) {
        		   entry = (Entry) i.next();
        		   fa = (FileAnnotationData) entry.getKey();
        		   viewer.setLoadedFile(fa, (File) entry.getValue(), 
        				   files.get(fa));
        	   }
    	   }
       }
    }
    
    /**
     * Does nothing as the asynchronous call returns <code>null</code>.
     * The actual payload is delivered progressively during the updates
     * if data is <code>null</code>.
     * @see EditorLoader#handleNullResult()
     */
    public void handleNullResult()
    {
    	if (data != null)
    		 handleException(new Exception("No data available."));
    }
    
	/**
     * Feeds the result back to the viewer.
     * @see EditorLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	if (data != null) {
    		viewer.setLoadedFile(data, file, uiView);
    	} else {
    		/*
    		Map m = (Map) result;
    		Entry entry;
    		Iterator i = m.entrySet().iterator();
    		FileAnnotationData fa;
    		while (i.hasNext()) {
    			entry = (Entry) i.next();
				fa = (FileAnnotationData) entry.getKey();
				viewer.setLoadedFile(fa, (File) entry.getValue(), 
						files.get(fa));
			}
			*/
    	}
    	
    }
    
}

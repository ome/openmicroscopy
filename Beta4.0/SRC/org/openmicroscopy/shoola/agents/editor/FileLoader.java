/*
 * org.openmicroscopy.shoola.agents.editor.FileLoader 
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
package org.openmicroscopy.shoola.agents.editor;


//Java imports
import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.view.Editor;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.log.LogMessage;

import pojos.FileAnnotationData;

/** 
 * Loads the file to edit. 
 * This class calls one of the <code>loadFile</code> method in the
 * <code>MetadataHandlerView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class FileLoader 
	extends EditorLoader
{

	/** Handle to the async call so that we can cancel it. */
    private CallHandle  		handle;
    
    /** The id of the file to load. */
    private long 				fileID;
    
    /** The size of the file to load. */
    private long 				fileSize;
    
    /** Utility file where the raw data are loaded. */
    private File				file;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer	The Editor this data loader is for.
     *                  Mustn't be <code>null</code>.
     * @param fileName	The name of the file to edit.
     * @param fileID	The id of the file to load OR of the fileAnnotation if fileName is null
     * @param fileSize	The size of the file to load.
     */
	public FileLoader(Editor viewer, String fileName, long fileID, 
				long fileSize)
	{
		super(viewer);
		if (fileID < 0)
			throw new IllegalArgumentException("ID not valid.");
		this.fileID = fileID;
		this.fileSize = fileSize;
		if (fileName != null) file = new File(fileName);
	}
	
	/**
	 * Loads the file.
	 * @see EditorLoader#load()
	 */
	public void load()
	{
		handle = mhView.loadFile(file, fileID, fileSize, this);
	}

	/**
	 * Cancels the data loading.
	 * @see EditorLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }

	 /**
     * Overridden to indicate that no file with the specified id 
     * existed on the server.
     * @see DSCallAdapter#handleException(Throwable)
     */
    public void handleException(Throwable exc) 
    {
    	String s = "Data Retrieval Failure: ";
        LogMessage msg = new LogMessage();
        msg.print(s);
        msg.print(exc);
        registry.getLogger().error(this, msg);
        registry.getUserNotifier().notifyInfo("Loading File.", "" +
        		"The specified file has not previously\n" +
        		"been saved or the passed ID is not valid.");
        viewer.discard();
    }
    
	/**
	 * Feeds the result back to the viewer.
	 * @see EditorLoader#handleResult(Object)
	 */
	public void handleResult(Object result)
	{
		if (viewer.getState() == Browser.DISCARDED) return;  //Async cancel.
		File f;
		FileAnnotationData fa;
		if (file == null) {
			Map m = (Map) result;
			Entry entry;
			Iterator i = m.entrySet().iterator();
			while (i.hasNext()) {
				entry = (Entry) i.next();
				fa = (FileAnnotationData) entry.getKey();
				f = (File) entry.getValue();
				if (f.exists()) {
					viewer.setFileToEdit(fa, f);
					// don't need to keep a copy. Delete the local copy after 
					// opening in viewer. 
					f.delete();
				}
			}
		} else {
			f = (File) result;
			if (f.exists()) {
				viewer.setFileToEdit(null, f);
				// don't need to keep a copy. Delete the local copy after 
				// opening in viewer. 
				String message = "Cannot delete the file.";
				if (f.delete()) message = "File deleted.";
				registry.getLogger().info(this, message);
				file.delete();
			}
		}
	}
	
}

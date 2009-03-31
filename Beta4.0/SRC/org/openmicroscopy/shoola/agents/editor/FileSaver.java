/*
 * org.openmicroscopy.shoola.agents.editor.FileSaver 
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.view.Editor;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.MetadataHandlerView;

import pojos.DataObject;
import pojos.FileAnnotationData;

/** 
 * Saves the file back to the server.
 * This class calls one of the <code>saveFile</code> method in the
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
 * @since 3.0-Beta4
 */
public class FileSaver 
	extends EditorLoader
{

	/** Identifies that the file is of type protocol. */
	public static final int		PROTOCOL = MetadataHandlerView.EDITOR_PROTOCOL;
	
	/** Identifies that the file is of type experiment. */
	public static final int		EXPERIMENT = 
									MetadataHandlerView.EDITOR_EXPERIMENT;
	
	/** Identifies that the file is of type other. */
	public static final int		OTHER = MetadataHandlerView.OTHER;
	
	/** Handle to the async call so that we can cancel it. */
    private CallHandle  		handle;
    
    /** Utility file where the raw data are loaded. */
    private File				file;
    
    /** The fileAnnotation data. */
    private FileAnnotationData	fileAnnotationData;
    
    /** One of the constants defined by this class. */
    private int 				index;
    
    /** The <code>DataObject</code> to link the file annotation to. */
    private DataObject			linkTo;
    
	/**
     * Creates a new instance.
     * 
     * @param viewer The Editor this data loader is for.
     * 				 Mustn't be <code>null</code>.
     * @param file	 The file to save back to the server.
     * @param data	 The id of thet file if previously saved, or
     * 				 <code>-1</code> if not previously saved.
     * @param index  One of the constants defined by this class.
     * @param linkTo The <code></code>
     */
	public FileSaver(Editor viewer, File file, FileAnnotationData data, 
			int index, DataObject linkTo)
	{
		super(viewer);
		if (file == null)
			throw new IllegalArgumentException("No file to save.");
		if (data == null) 
			throw new IllegalArgumentException("No file Annotation.");
		this.file = file;
		this.fileAnnotationData = data;
		this.linkTo = linkTo;
		switch (index) {
			case EXPERIMENT:
			case PROTOCOL:
				this.index = index;
				break;
			case OTHER:
			default:
				this.index = OTHER;
		}
	}
	
	/**
	 * Saves the file.
	 * @see EditorLoader#load()
	 */
	public void load()
	{
		handle = mhView.saveFile(fileAnnotationData, file, index, linkTo, this);
	}

	/**
	 * Cancels the data loading.
	 * @see EditorLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }

	/**
	 * Feeds the result back to the viewer.
	 * @see EditorLoader#handleResult(Object)
	 */
	public void handleResult(Object result)
	{
		if (viewer.getState() == Browser.DISCARDED) return;  //Async cancel.
		viewer.onFileSave((FileAnnotationData) result);
		String message = "Cannot delete the file.";
		if (file.delete()) message = "File deleted.";
		registry.getLogger().info(this, message);
	}
	
}

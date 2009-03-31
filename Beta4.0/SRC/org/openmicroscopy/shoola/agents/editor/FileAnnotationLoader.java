/*
 * org.openmicroscopy.shoola.agents.editor.browser.browserloaders.FileAnnotationLoader
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
package org.openmicroscopy.shoola.agents.editor;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.editor.browser.Browser;
import org.openmicroscopy.shoola.agents.editor.preview.AnnotationHandler;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.FileAnnotationData;

/** 
 * Loads the file annotation corresponding to the passed id. 
 * This class calls one of the <code>loadAnnotation</code> method in the
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
public class FileAnnotationLoader 
	extends BrowserLoader
{

	/** Handle to the async call so that we can cancel it. */
    private CallHandle				handle;
    
    /** The id of the file annotation to load. */
    private long					fileAnnotationID;
    
    /** 
     * The handler of the annotation. Don't want this to be Editor since
     * it would be tricky to handle which class requested the annotation. 
     */
    private AnnotationHandler		annotationHandler;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer	The Editor this data loader is for.
     *                  Mustn't be <code>null</code>.
     * @param fileAnnotationID The Id of the file annotation to load.
     */
	public FileAnnotationLoader(Browser viewer, long fileAnnotationID)
	{
		super(viewer);
		if (fileAnnotationID < 0)
			throw new IllegalArgumentException("ID not valid.");
		this.fileAnnotationID = fileAnnotationID;
	}
	
	/**
	 * Allows classes to register to handle the result.
	 * 
	 * @param handler The handler
	 */
	public void setAnnotationHandler(AnnotationHandler handler) 
	{
		this.annotationHandler = handler;
	}
	
	/**
	 * Loads the file annotation.
	 * @see BrowserLoader#load()
	 */
	public void load()
	{
		handle = mhView.loadAnnotation(fileAnnotationID, this);
	}
	
	/**
	 * Cancels the data loading.
	 * @see BrowserLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }
    
	/**
	 * Feeds the result back to the viewer.
	 * @see BrowserLoader#handleResult(Object)
	 */
	public void handleResult(Object result)
	{
		if (result instanceof FileAnnotationData) {
			if (annotationHandler != null) {
				annotationHandler.handleAnnotation((FileAnnotationData) result);
			}
		}
	}
	
}

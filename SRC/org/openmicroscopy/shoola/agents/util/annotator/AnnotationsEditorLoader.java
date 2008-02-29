/*
 * org.openmicroscopy.shoola.agents.util.annotator.AnnotationsEditorLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.annotator;




//Java imports
import java.util.Collection;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.DataHandler;
import org.openmicroscopy.shoola.agents.util.annotator.view.AnnotatorEditor;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;

/** 
 * Retrieves the annotations linked to a given <code>DataObject</code>.
 * This class calls the <code>loadAnnotation</code> method in the 
 * <code>DataHandlerView</code>.
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
public class AnnotationsEditorLoader 	
	extends AnnotatorEditorLoader
{
	
    /** The {@link DataObject} to handle. */
    private DataObject  	object;
	
	/** Handle to the async call so that we can cancel it. */
	private CallHandle  	handle;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer		The Annotator this loader is for. 
	 * 						Mustn't be <code>null</code>.
	 * @param object		The object to retrieve the annotation for.
	 *						Mustn't be <code>null</code>.
	 * @param type			The type of annotation.
	 */
	public AnnotationsEditorLoader(AnnotatorEditor viewer, DataObject object)
	{
		super(viewer);
		if (!checkAnnotationType(object.getClass()))
			throw new IllegalArgumentException("DataObject not supported: ");
		this.object = object;
	}
	
	/** 
	 * Cancels the data loading. 
	 * @see AnnotatorEditorLoader#load()
	 */
	public void load()
	{
		 handle = mhView.loadTextualAnnotations(object.getClass(), 
				 						object.getId(), -1, this);
	}
	
	/** 
	 * Cancels the data loading. 
	 * @see AnnotatorEditorLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }
	
	/**
	 * Feeds the result back to the viewer.
	 * @see AnnotatorEditorLoader#handleResult(Object)
	 */
	public void handleResult(Object result)
	{
	    if (viewer.getState() == DataHandler.DISCARDED) return; 
	    viewer.setAnnotations((Collection) result);
	}
}

/*
 * org.openmicroscopy.shoola.env.ui.OpenObjectLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.ui;

//Java imports
import java.io.File;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.util.filter.file.OMETIFFFilter;
import pojos.DataObject;
import pojos.FileAnnotationData;
import pojos.ImageData;

/** 
 * Downloads if the passed object is a file or an archived image
 * or exports as OME-TIFF if it is non-archived image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class OpenObjectLoader 
	extends UserNotifierLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  			handle;
    
    /** The object to handle. */
    private DataObject 				object;

    /** The file where to export the object. */
    private String					folderPath;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param registry	Convenience reference for subclasses.
     * @param object	The object to handle.
     * @param folderPath The folder where to copy locally the object.
     * @param activity 	The activity associated to this loader.
     */
	public OpenObjectLoader(UserNotifier viewer,  Registry registry,
			DataObject object, String folderPath,
			ActivityComponent activity)
	{
		super(viewer, registry, activity);
		if (object == null)
			throw new IllegalArgumentException("Object not valid.");
		if (!(object instanceof ImageData || 
				object instanceof FileAnnotationData))
			throw new IllegalArgumentException("Object not valid.");
		this.object = object;
		this.folderPath = folderPath;
	}
	
	/**
     * Downloads or exports the object.
     * @see UserNotifierLoader#load()
     */
    public void load()
    {
    	String path = folderPath+File.separator;
    	File f;
    	if (object instanceof ImageData) {
    		ImageData image = (ImageData) object;
    		path += image.getName();
    		path += image.getId();
    		path += "."+OMETIFFFilter.OME_TIF;
    		f = new File(path);
    		f.deleteOnExit();
    		handle = ivView.exportImageAsOMETiff(image.getId(), f, this);
    	} else {
    		FileAnnotationData fa = (FileAnnotationData) object;
    		path += fa.getFileName();
    		f = new File(path);
    		f.deleteOnExit();
    		handle = mhView.loadFile(f, fa.getFileID(), 
    				FileLoader.FILE_ANNOTATION, this);
    	}
    }
    
    /**
     * Cancels the ongoing data retrieval.
     * @see UserNotifierLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
 
    /** 
     * Feeds the result back to the viewer. 
     * @see UserNotifierLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    { 
    	if (result == null) onException(MESSAGE_RESULT, null);
    	else activity.endActivity(result); 
    }
    
}

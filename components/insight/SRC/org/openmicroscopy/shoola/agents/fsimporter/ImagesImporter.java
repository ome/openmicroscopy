/*
 * org.openmicroscopy.shoola.agents.fsimporter.ImagesImporter 
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
package org.openmicroscopy.shoola.agents.fsimporter;



//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;

/** 
 * Imports the images.
 * This class calls one of the <code>importImages</code> methods in the
 * <code>ImageDataView</code>.
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
public class ImagesImporter 
	extends DataImporterLoader
{

	/** Handle to the async call so that we can cancel it. */
    private CallHandle	handle; 
    
    /** The container where to import the images into. */
    private DataObject 	container;
    
    /** The collection of images to import. */
    private List<Object> images;
    
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer	The Importer this data loader is for.
     * 					Mustn't be <code>null</code>.
	 * @param container	The container where to import the image to.
	 * @param images	The images to import.
	 */
	public ImagesImporter(Importer viewer, DataObject container, 
			List<Object> images)
	{
		super(viewer);
		if (images == null || images.size() == 0)
			throw new IllegalArgumentException("No images to import.");
		this.images = images;
		this.container = container;
	}

	/** 
	 * Starts the import.
	 * @see DataImporterLoader#load()
	 */
	public void load()
	{
		handle = ivView.importImages(container, null, 
				getCurrentUserID(), -1, false, this);
	}
	
	/** 
	 * Cancels the import.
	 * @see DataImporterLoader#load()
	 */
	public void cancel() { handle.cancel(); }

	/** 
     * Feeds the result back to the viewer.
     * @see DataImporterLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Importer.DISCARDED) return;  //Async cancel.
    }

}

/*
 * org.openmicroscopy.shoola.agents.util.classifier.ClassificationsLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.classifier;


//Java imports
import java.util.Set;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.DataHandler;
import org.openmicroscopy.shoola.agents.util.classifier.view.Classifier;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.DataHandlerView;

/** 
 * Loads the classification paths for declassifying or classifying the images
 * depending on the specified mode.
 * This class calls the <code>loadClassificationPaths</code>
 * method in the <code>DataHandlerView</code>.
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
public class ClassificationsLoader
	extends ClassifierLoader	
{

	/** 
	 * One of the following constants: {@link Classifier#CLASSIFY_MODE} or
     * {@link Classifier#DECLASSIFY_MODE}. 
     */
	private int				mode;
	
	/** The Id of the root node. */
	private long			rootID;
	
	/** Collection of image's IDs to classify. */
	private Set				images;

    /** Handle to the async call so that we can cancel it. */
    private CallHandle  	handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer	The Classifier this loader is for.
     * @param images	Collection of image's IDs to classify.
     * @param rootID	The Id of the root node.
     * @param mode		One of the following constants:
     * 					{@link Classifier#CLASSIFY_MODE} or
     * 					{@link Classifier#DECLASSIFY_MODE}.
     */
	public ClassificationsLoader(Classifier viewer, Set images, long rootID, 
								int mode)
	{
		super(viewer);
		if (images == null || images.size() == 0)
			throw new IllegalArgumentException("No images to classify.");
		controlMode(mode);
		this.mode = mode;
		this.images = images;
		this.rootID = rootID;
	}
	
    /**
     * Creates a new instance.
     * 
     * @param viewer	The Classifier this loader is for.
     * @param images	Collection of image's IDs to classify.
     * @param rootID	The Id of the root node..
     */
	public ClassificationsLoader(Classifier viewer, long rootID)
	{
		super(viewer);
		this.mode = Classifier.BULK_CLASSIFY_MODE;
		this.rootID = rootID;
		images = null;
	}
	
	/** 
     * Loads the classifications paths.
     * @see ClassifierLoader#load()
     */
	public void load()
	{
		switch (mode) {
			case Classifier.CLASSIFY_MODE:
			case Classifier.BULK_CLASSIFY_MODE:
				handle = dhView.loadClassificationPaths(images, rootID, 
						DataHandlerView.CLASSIFICATION_NME, this);
				break;
			case Classifier.DECLASSIFY_MODE:	
				handle = dhView.loadClassificationPaths(images, rootID, 
						DataHandlerView.DECLASSIFICATION, this);
		}
	}
	
	/** 
     * Cancels the data loading. 
     * @see ClassifierLoader#cancel()
     */
	public void cancel() { handle.cancel(); }
	
    /**
     * Feeds the result back to the viewer.
     * @see ClassifierLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == DataHandler.DISCARDED) return; 
        viewer.setClassifications((Set) result);
    }
    
}

/*
 * org.openmicroscopy.shoola.agents.util.classifier.ClassificationsSaver 
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

/** 
 * Adds/removes the images to/from the categories depending on the 
 * specified mode.
 * This class calls the <code>classify</code> and <code>declassify</code>
 * methods in the <code>DataHandlerView</code>.
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
public class ClassificationsSaver
	extends ClassifierLoader
{

	/** 
	 * One of the following constants: {@link Classifier#CLASSIFY_MODE} or
     * {@link Classifier#DECLASSIFY_MODE}. 
     */
	private int				mode;
	
	/** The images to classify. */
	private Set				toClassify;
	
	/** The categories to classify the images into. */
	private Set				categories;

    /** Handle to the async call so that we can cancel it. */
    private CallHandle  	handle;

    /**
     * Creates a new instance.
     * 
     * @param viewer		The Annotator this loader is for.
     * @param toClassify	Collection of objects to classify.
     * @param categories	The categories to classify the images into.
     * @param mode			One of the following constants:
     * 						{@link Classifier#CLASSIFY_MODE} or
     * 						{@link Classifier#DECLASSIFY_MODE}.
     */
	public ClassificationsSaver(Classifier viewer, Set toClassify, 
								Set categories, 
								int  mode)
	{
		super(viewer);
		if (categories == null || categories.size() == 0)
			throw new IllegalArgumentException("No category selected.");
		if (toClassify == null || toClassify.size() == 0)
			throw new IllegalArgumentException("No images to classify.");
		controlMode(mode);
		this.mode = mode;
		this.toClassify = toClassify;
		this.categories = categories;
	}
	
	/** 
     * Adds the images to the categories. 
     * @see ClassifierLoader#load()
     */
	public void load()
	{
		switch (mode) {
			case Classifier.CLASSIFY_MODE:
				handle = dhView.classify(toClassify, categories, this);
			case Classifier.DECLASSIFY_MODE:
				handle = dhView.declassify(toClassify, categories, this);
			case Classifier.BULK_CLASSIFY_MODE:
				handle = dhView.classifyChildren(toClassify, categories, this);
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
        viewer.saveClassifications((Set) result);
    }
    
}

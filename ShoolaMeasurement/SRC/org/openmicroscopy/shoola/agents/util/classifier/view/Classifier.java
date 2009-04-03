/*
 * org.openmicroscopy.shoola.agents.util.classifier.view.Classifier 
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
package org.openmicroscopy.shoola.agents.util.classifier.view;


//Java imports
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.DataHandler;

/** 
 * Defines the interface provided by the classifier component.
 * The classifier provides a top-level window to host classifications 
 * and let the user interact with it.
 * <p>The typical life-cycle of an annotator is as follows.The object
 * is first created using the {@link ClassifierFactory}. After
 * creation the object is in the {@link #NEW} state and is waiting for the
 * {@link #activate() activate} method to be called.
 * 
 * When the user quits the window, the {@link #discard() discard} method is
 * invoked and the object transitions to the {@link #DISCARDED} state.
 * At which point, all clients should de-reference the component to allow for
 * garbage collection.
 * 
 * </p>
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
public interface Classifier 
	extends DataHandler
{

    /** Identifies the classify model. */
    public static final int     CLASSIFY_MODE = 0;
    
    /** Identifies the declassify model. */
    public static final int     DECLASSIFY_MODE = 1;
    
    /** Identifies the bulk classify model. */
    public static final int     BULK_CLASSIFY_MODE = 2;
    
    /**
     * Queries the current state.
     * 
     * @return One of the state flags defined by this interface.
     */
    public int getState();
    
    /** Saves the classification. */
	public void finish();
	
	/** Cancels any ongoing data loading. */
	public void cancel();
	
	/**
	 * Sets the classification paths available for <code>DataObject</code>s.
	 * 
	 * @param paths The value to set.
	 */
	public void setClassifications(Set paths);

	/** 
	 * Indicates that the classification has been saved. 
	 * 
	 * @param results 
	 */
	public void saveClassifications(Set results);

}

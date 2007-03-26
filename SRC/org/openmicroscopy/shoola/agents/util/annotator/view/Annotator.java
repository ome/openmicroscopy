/*
 * org.openmicroscopy.shoola.agents.util.annotator.view.Annotator 
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
package org.openmicroscopy.shoola.agents.util.annotator.view;


//Java imports
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.DataHandler;

/** 
 * Defines the interface provided by the annotator component.
 * The annotator provides a top-level window to host annotations 
 * and let the user interact with it.
 * <p>The typical life-cycle of an annotator is as follows.The object
 * is first created using the {@link AnnotatorFactory}. After
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
public interface Annotator
	extends DataHandler
{

    /** Identifies the annotate model. */
    public static final int     ANNOTATE_MODE = 0;
    
    /** Identifies the bulk annotate model. */
    public static final int     BULK_ANNOTATE_MODE = 1;
       
   /**
    * Queries the current state.
    * 
    * @return One of the state flags defined by this interface.
	*/
	public int getState();

	/** Saves the annotations. */
	public void finish();

	/** Cancels any ongoing data loading. */
	public void cancel();
	
	/**
	 * Sets the annotations retrieved for the annotated 
	 * <code>DataObject</code>s.
	 * 
	 * @param annotations The value to set.
	 */
	public void setAnnotations(Map annotations);

	/** 
	 * Indicates that the annotation has been saved. 
	 * 
	 * @param results The updated <code>DataObject</code>s.
	 */
	public void saveAnnotations(List results);
	
}

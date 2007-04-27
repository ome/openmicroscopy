/*
 * org.openmicroscopy.shoola.agents.util.DataHandler 
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
package org.openmicroscopy.shoola.agents.util;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

/** 
 * Defines the interface that a component, like the <code>Classifier</code>
 * or <code>Annotator</code> usually implements. This interface provides
 * methods to {@link #activate() activate} the component and 
 * {@link #discard() discard} it.
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
public interface DataHandler
	extends ObservableComponent
{

	/** 
	 * Bound property indicating that the <code>DataObject</code>s
	 * have been classified.
	 */
	public static final String		CLASSIFIED_PROPERTY = "classified";
	
	/** 
	 * Bound property indicating that the <code>DataObject</code>s
	 * have been annotated.
	 */
	public static final String		ANNOTATED_PROPERTY = "annotated";
	
	 /** Flag to denote the <i>New</i> state. */
    public static final int         NEW = 1;
    
    /** Flag to denote the <i>Discarded</i> state. */
    public static final int         DISCARDED = 2;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int         READY = 3;
    
    /** Flag to denote the <i>LOADING</i> state. */
    public static final int         LOADING = 4;
    
    /** Flag to denote the <i>Saving</i> state. */
    public static final int         SAVING = 5;
    
    /** Indicates to select all items in the list. */
    public static final int 		SELECT_ALL = 100;
    
    /** Indicates to select the selected item. */
    public static final int 		SELECT_ONE = 101;
    
    /**
     * Starts the initialization sequence when the current state is {@link #NEW} 
     * and puts the window on screen.
     * If the state is not {@link #NEW}, then this method simply moves the
     * window to front.
     * 
     * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
     */
    public void activate();
    
    /**
     * Transitions the viewer to the {@link #DISCARDED} state.
     * Any ongoing data loading is cancelled.
     */
    public void discard();
    
    /** Closes the component. */
    public void close();
    
}

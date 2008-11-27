/*
 * org.openmicroscopy.shoola.agents.util.tagging.view.Tagger 
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
package org.openmicroscopy.shoola.agents.util.tagging.view;



//Java imports
import java.util.List;

import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.DataHandler;

/** 
 * Defines the interface provided by the classifier component.
 * The tagger provides a top-level window to host tag and let the user interact
 *  with it.
 * <p>The typical life-cycle of an tagger is as follows. The object
 * is first created using the {@link TaggerFactory}. After
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
public interface Tagger
	extends DataHandler
{

	/** Flag to denote the <i>New</i> state. */
	public static final int         NEW = 1;

	/** Flag to denote the <i>Discarded</i> state. */
	public static final int         DISCARDED = 2;

	/** Flag to denote the <i>Ready</i> state. */
	public static final int         READY = 3;

	/** Flag to denote the <i>LOADING</i> state. */
	public static final int         LOADING = 4;
	
	/** Bounds property indicating to create a tag. */
	public static final String		CREATE_TAG_PROPERTY = "createTag";
	
	/** Bounds property indicating that the tags are loaded. */
	public static final String		TAG_LOADED_PROPERTY = "tagLoaded";
	
	/** Bounds property indicating that images have tagged. */
	public static final String		TAGGED_PROPERTY = "tagged";

	/** Identifies the tagging mode. */
    public static final int     	TAGGING_MODE = 0;
    
	/** Identifies the bulk classify model. */
    public static final int     	BULK_TAGGING_MODE = 1;
    
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
    
    /** Cancels any ongoing data loading. */
    public void cancel();
    
    /**
     * Returns the actual state.
     * 
     * @return See above.
     */
    public int getState();
    
    /** 
     * Returns the view. 
     * 
     * @return See above.
     */
	public JDialog getUI();

	/**
	 * Sets the categories the image is categorised into.
	 * 
	 * @param tags			The tags linked to the image.
	 * @param availableTags	The tags that can be added to the image.
	 * @param tagSets		The tagSets owned by the user.
	 */
	public void setTags(List tags, List availableTags, List tagSets);

	/** Indicates that the images have been tagged. */
	public void setImageTagged();
	
	/** Closes the application. */
	public void close();
	
	/** Saves the tagging. */
	public void finish();

	/** Displays the available tags if any. */
	public void showTags();

	/**
	 * Returns the collection of tags.
	 * 
	 * @return See above.
	 */
	public List getTags();
	
}

/*
 * org.openmicroscopy.shoola.agents.metadata.TagsLoader 
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
package org.openmicroscopy.shoola.agents.metadata;


//Java imports
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.MetadataHandlerView;

/** 
 * Loads the existing tags.
 * This class calls one of the <code>loadExistingAnnotations</code> methods 
 * in the <code>MetadataHandlerView</code>.
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
public class TagsLoader 
	extends EditorLoader
{

	/** Indicates to retrieve the tags. */
	public static final int LEVEL_TAG = MetadataHandlerView.LEVEL_TAG;
	
	/** Indicates to retrieve the tag sets. */
	public static final int LEVEL_TAG_SET = MetadataHandlerView.LEVEL_TAG_SET;

	/** Indicates to retrieve the tag sets and the tags. */
	public static final int LEVEL_ALL = MetadataHandlerView.LEVEL_ALL;
	
    /** Handle to the async call so that we can cancel it. */
    private CallHandle	handle;
    
    /** One of the constants defined by this class. */
    private int			level;
    
    /** 
     * Checks the passed level is supported
     * 
     * @param value The value to control.
     */
    private void checkLevel(int value)
    {
    	switch (value) {
			case LEVEL_TAG:
			case LEVEL_TAG_SET:
			case LEVEL_ALL:
				break;
	
			default:
				throw new IllegalArgumentException("Level not supported.");
		}
    }
    
	 /**	
     * Creates a new instance.
     * 
     * @param viewer 	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param level		One of the constants defined by this class.
     */
    public TagsLoader(Editor viewer, int level)
    {
    	 super(viewer);
    	 checkLevel(level);
    	 this.level = level;
    }
    
	/** 
	 * Loads the tags. 
	 * @see EditorLoader#cancel()
	 */
	public void load()
	{
		long userID = MetadataViewerAgent.getUserDetails().getId();
		handle = mhView.loadExistingTags(level, userID, this);
	}
	
	/** 
	 * Cancels the data loading. 
	 * @see EditorLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }
	
	/**
     * Feeds the result back to the viewer.
     * @see EditorLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	//if (viewer.getState() == MetadataViewer.DISCARDED) return;  //Async cancel.
    	//viewer.setMetadata(refNode, result);
    	switch (level) {
			case LEVEL_TAG:
			case LEVEL_TAG_SET:
				viewer.setExistingTags((Collection) result);
				break;
	
			case LEVEL_ALL:
				break;
		}
    } 
	
}

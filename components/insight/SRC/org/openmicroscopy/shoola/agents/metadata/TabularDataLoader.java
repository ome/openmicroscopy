/*
 * org.openmicroscopy.shoola.agents.metadata.TabularDataLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata;


//Java imports
import java.util.ArrayList;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import org.openmicroscopy.shoola.env.data.model.TableParameters;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Loads the tabular data.
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
public class TabularDataLoader 
	extends EditorLoader
{

	/** Parameters to load the table. */
	private TableParameters parameters;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle	handle;
    
    /**	
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param originalID The identifier of the table.
     */
    public TabularDataLoader(Editor viewer, SecurityContext ctx,
    		long originalFileID)
    {
    	 super(viewer, ctx);
    	 if (originalFileID < 0)
    		 throw new IllegalArgumentException("No file to retrieve.");
    	 List<Long> ids = new ArrayList<Long>();
    	 ids.add(originalFileID);
    	 parameters = new TableParameters(ids);
    }
    
    /** 
	 * Loads the tags. 
	 * @see EditorLoader#cancel()
	 */
	public void load()
	{
		setIds();
		handle = mhView.loadTabularData(ctx, parameters, userID, this);
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
    	//viewer.setExistingTags((Collection) result);
    	//decide what to do with result
    } 

}

/*
 * org.openmicroscopy.shoola.agents.treeviewer.ScriptsLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.treeviewer;



//Java imports
import java.awt.Point;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.ProcessException;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.log.LogMessage;

/** 
 * Loads the scripts. This class calls the <code>loadScripts</code> 
 * method in the <code>MetadataHandlerView</code>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class ScriptsLoader 
	extends DataTreeViewerLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;
    
    /** 
     * Flag indicating to load all the scripts (uploaded and default)
     * or only the uploaded scripts.
     */
    private boolean all;
    
    /** The location of the mouse click.*/
    private Point location;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer Reference to the viewer. Mustn't be <code>null</code>.
     * @param all  	 Pass <code>true</code> to retrieve all the scripts uploaded
	 * 				 ones and the default ones, <code>false</code>.
	 * @param location The location of the mouse click.
     */
    public ScriptsLoader(TreeViewer viewer, boolean all, Point location)
    {
    	super(viewer);
    	this.all = all;
    	this.location = location;
    }
    
    /** 
     * Loads the scripts.
     * @see DataTreeViewerLoader#load()
     */
    public void load()
    {
    	handle = mhView.loadScripts(-1, all, this);
    }
    
    /** 
     * Cancels the data loading. 
     * @see DataTreeViewerLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Feeds the result back to the viewer.
     * @see DataTreeViewerLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	if (viewer.getState() == Browser.DISCARDED) return;
    	viewer.setAvailableScripts((List) result, location);
    }

    /**
     * Notifies the user that an error has occurred.
     * @see DSCallAdapter#handleException(Throwable)
     */
    public void handleException(Throwable exc) 
    {
    	viewer.setAvailableScripts(null, location);
    	super.handleException(exc);
    }
    
}

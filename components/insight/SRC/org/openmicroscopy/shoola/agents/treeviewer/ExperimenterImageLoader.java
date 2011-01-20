/*
 * org.openmicroscopy.shoola.agents.treeviewer.ExperimenterImageLoader 
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
package org.openmicroscopy.shoola.agents.treeviewer;




//Java imports
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeFileSet;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.ExperimenterData;

/** 
 * Retrieves images before or after a given day, or during a period of time.
 * This class calls the <code>loadImages</code> in the
 * <code>DataHandlerView</code>.
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
public class ExperimenterImageLoader 
	extends DataBrowserLoader
{
    
    /** The node hosting the experimenter the data are for. */
    private TreeImageSet		expNode;
    
    /** The node hosting the information about the smart folder. */
    private TreeImageSet		smartFolderNode;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  		handle;
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer    		The viewer this data loader is for.
     *                  		Mustn't be <code>null</code>.
     * @param expNode			The node hosting the experimenter the data are 
     * 							for.
     * 							Mustn't be <code>null</code>.
     * @param smartFolderNode	The node hosting the information about 
     * 							the smart folder. Mustn't be <code>null</code>.
     */
    public ExperimenterImageLoader(Browser viewer, TreeImageSet expNode, 
    							TreeImageSet smartFolderNode)
    {
    	super(viewer);
        if (expNode == null ||
        		!(expNode.getUserObject() instanceof ExperimenterData))
        	throw new IllegalArgumentException("Experimenter node not valid.");
        if (smartFolderNode == null)
        	throw new IllegalArgumentException("No smart folder specified.");
        this.expNode = expNode;
        this.smartFolderNode = smartFolderNode;
    } 
   
    /**
     * Retrieves the data.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
    	ExperimenterData exp = (ExperimenterData) expNode.getUserObject();
    	if (smartFolderNode instanceof TreeImageTimeSet) {
    		TreeImageTimeSet time = (TreeImageTimeSet) smartFolderNode;
    		handle = dhView.loadImages(time.getStartTime(),
					time.getEndTime(), exp.getId(), this);	
    	} else if (smartFolderNode instanceof TreeFileSet) {
    		handle = dhView.loadFiles(((TreeFileSet) smartFolderNode).getType(),
    				exp.getId(), this);	
    	}
    }

    /**
     * Cancels the data loading.
     * @see DataBrowserLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Browser.DISCARDED) return;  //Async cancel.
        viewer.setLeaves((Set) result, smartFolderNode, expNode); 
    }
    
}

/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.util.Set;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeFileSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

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
    
    private int convertType(int type)
    {
    	switch (type) {
	    	case TreeFileSet.TAG:
	    		return OmeroMetadataService.TAG_NOT_OWNED;
	    	case TreeFileSet.MOVIE:
	    		return OmeroMetadataService.MOVIE;
	    	case TreeFileSet.OTHER:
	    		default:
	    		return OmeroMetadataService.OTHER;
		}
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param expNode The node hosting the experimenter/group the data are for.
     *                Mustn't be <code>null</code>.
     * @param smartFolderNode The node hosting the information about
     *                        the smart folder. Mustn't be <code>null</code>.
     */
    public ExperimenterImageLoader(Browser viewer, SecurityContext ctx,
    		TreeImageSet expNode, TreeImageSet smartFolderNode)
    {
    	super(viewer, ctx);
        if (expNode == null)
        	throw new IllegalArgumentException("Node not valid.");
        Object ho = expNode.getUserObject();
        if (!(ho instanceof ExperimenterData || ho instanceof GroupData))
        	throw new IllegalArgumentException("Node not valid.");
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
    	long expID = -1;
    	if (expNode.getUserObject() instanceof ExperimenterData)
    		expID = ((ExperimenterData) expNode.getUserObject()).getId();
    	
    	if (smartFolderNode instanceof TreeImageTimeSet) {
    		TreeImageTimeSet time = (TreeImageTimeSet) smartFolderNode;
    		handle = dhView.loadImages(ctx, time.getStartTime(),
					time.getEndTime(), expID, this);
    	} else if (smartFolderNode instanceof TreeFileSet) {
    		TreeFileSet set = (TreeFileSet) smartFolderNode;
    		if (set.getType() == TreeFileSet.ORPHANED_IMAGES) {
    			handle = dmView.loadImages(ctx, expID, true, this);
    		} else
    			handle = dhView.loadFiles(ctx, convertType(set.getType()),
    					expID, this);
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

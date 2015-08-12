/*
 * org.openmicroscopy.shoola.agents.treeviewer.FilesChecker 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageNode;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;


/** 
 * Controls if the passed nodes hosted files that can be imported.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class FilesChecker 
	extends DataBrowserLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  			handle;
    
    /** The nodes to handle. */
    private List<TreeImageNode>		nodes;
    
    /** Convenience map to update the status of the node. */
    private Map<File, TreeImageNode> map;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param nodes The collection of nodes to handles.
     */
	public FilesChecker(Browser viewer, SecurityContext ctx,
			List<TreeImageNode> nodes)
	{
		super(viewer, ctx);
		if (nodes == null)
			throw new IllegalArgumentException("No nodes specified.");
		this.nodes = nodes;
	}
	
	/**
     * Controls if the passed nodes host files of a supported format.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
    	List<File> list = new ArrayList<File>();
    	map = new HashMap<File, TreeImageNode>();
    	Iterator<TreeImageNode> i = nodes.iterator();
    	TreeImageNode node;
    	Object o;
    	while (i.hasNext()) {
			node = i.next();
			o = node.getUserObject();
			if (o instanceof File) {
				map.put((File) o, node); 
				list.add((File) o);
			}	
		}
    	handle = dmView.checkFileFormat(list, this);
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
    	Map m = (Map) result;
    	//The collection of files formats not supported.
    	List l = (List) m.get(Boolean.valueOf(false));
    	if (l != null && l.size() > 0) {
    		Iterator i = l.iterator();
    		File f;
    		while (i.hasNext()) {
				f = (File) i.next();
				map.get(f).setSelectable(false);
			}
    		//repaint
    		viewer.getUI().repaint();
    	}
    }
    
}

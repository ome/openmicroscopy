/*
 * org.openmicroscopy.shoola.agents.treeviewer.ImagesImporter 
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.ImportObject;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ScreenData;

/** 
 * Imports the specified files.
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
public class ImagesImporter
	extends DataTreeViewerLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  			handle;
    
    /** The collection of nodes to reload. */
    private List<TreeImageDisplay>	nodes;
    
    /** The collection of files to import. */
    private List<ImportObject>  files;
    
    /** Container to download the image into or <code>null</code>. */
    private DataObject				container;
    
    /** Flag indicating to archive the files or not. */
    private boolean 				archived;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer 	The Viewer this data loader is for.
     * 				 	Mustn't be <code>null</code>.
     * @param node	 	The node hosting the container.
     * @param files	 	The collection of files to import.
     * @param archived 	Pass <code>true</code> to archived the files, 
	 * 					<code>false</code> otherwise.
     */
    public ImagesImporter(TreeViewer viewer, TreeImageDisplay node,
    		List<ImportObject> files, boolean archived)
	{
		super(viewer);
		if (files == null || files.size() == 0)
			throw new IllegalArgumentException("No images to import.");
		this.files = files;
		this.archived = archived;
		if (node != null) {
			nodes = new ArrayList<TreeImageDisplay>(1); 
			Object ho = node.getUserObject();
			if (ho instanceof DatasetData || ho instanceof ScreenData) {
				nodes.add(node);
				container = (DataObject) ho;
			}
		}
	}
	
    /**
     * Creates a new instance.
     * 
     * @param viewer	The Viewer this data loader is for.
     * 					Mustn't be <code>null</code>.
     * @param nodes		The collection of nodes to reload.
     * @param files		The collection of files to import.
     * @param archived 	Pass <code>true</code> to archived the files, 
	 * 					<code>false</code> otherwise.
     */
	public ImagesImporter(TreeViewer viewer, List<TreeImageDisplay> nodes,
			List<ImportObject> files, boolean archived)
	{
		super(viewer);
		if (files == null || files.size() == 0)
			throw new IllegalArgumentException("No images to import.");
		this.files = files;
		this.nodes = nodes;
		this.archived = archived;
		container = null;
	}
	
	/** 
     * Imports the images.
     * @see DataTreeViewerLoader#load()
     */
    public void load()
    {
    	handle = ivView.importImages(container, files, getCurrentUserID(), -1, 
    			archived, this);
    }

    /**
     * Cancels the data loading.
     * @see DataTreeViewerLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /** 
     * Feeds the thumbnails back to the viewer, as they arrive. 
     * @see DataTreeViewerLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
        if (viewer.getState() == DataBrowser.DISCARDED) return;  //Async cancel.
        Map m = (Map) fe.getPartialResult();
        if (m != null) {
        	Entry entry;
        	Iterator i = m.entrySet().iterator();
        	while (i.hasNext()) {
				entry = (Entry) i.next();
				viewer.setImportedFiles((File) entry.getKey(), entry.getValue(), 
						nodes, container);
			}
        }
    }
    
    /**
     * Notifies the user that an error has occurred.
     * @see DataTreeViewerLoader#handleException(Throwable)
     */
    public void handleException(Throwable exc) 
    {
        String s = "File Import Failure: ";
        registry.getLogger().error(this, s+exc);
        registry.getUserNotifier().notifyError("File Import Failure", s, exc);
    }
    
    /**
     * Does nothing as the asynchronous call returns <code>null</code>.
     * The actual payload (imported files) is delivered progressively
     * during the updates.
     * @see DataTreeViewerLoader#handleNullResult()
     */
    public void handleNullResult() {}
    
}

/*
 * org.openmicroscopy.shoola.agents.treeviewer.ExperimenterImagesCounter 
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeFileSet;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.MetadataHandlerView;
import org.openmicroscopy.shoola.env.log.LogMessage;
import pojos.ExperimenterData;

/** 
 * Counts the number of images imported during various periods of time
 * by the specified user.
 * This class calls the <code>countExperimenterImages</code> in the
 * <code>DataManagerView</code>.
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
public class ExperimenterImagesCounter
	extends DataBrowserLoader
{

	/** The node hosting the experimenter the data are for. */
    private TreeImageSet			expNode;
    
    /** The node hosting the time information. */
    private List<TreeImageSet>		nodes;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  			handle;
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer    The viewer this data loader is for.
     *                  Mustn't be <code>null</code>.
     * @param expNode	The node hosting the experimenter the data are for.
     * 					Mustn't be <code>null</code>.
     * @param nodes		The time nodes. Mustn't be <code>null</code>.
     */
	public ExperimenterImagesCounter(Browser viewer, TreeImageSet expNode, 
									List<TreeImageSet> nodes)
	{
		super(viewer);
		if (expNode == null ||
				!(expNode.getUserObject() instanceof ExperimenterData))
			throw new IllegalArgumentException("Experimenter node not valid.");
		if (nodes == null || nodes.size() == 0)
			throw new IllegalArgumentException("No time node specified node.");
		this.expNode = expNode;
		this.nodes = nodes;
	}
	
	/**
     * Cancels the data loading.
     * @see DataBrowserLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

	 /**
     * Retrieves the data.
     * @see DataBrowserLoader#load()
     */
	public void load()
	{
		Iterator i = nodes.iterator();
		TimeRefObject ref;
		long userID = expNode.getUserObjectId();
		Map<Integer, TimeRefObject> m;
		m = new LinkedHashMap<Integer, TimeRefObject>(nodes.size());
		TreeImageSet node;
		TreeImageTimeSet time;
		TreeFileSet file;
		while (i.hasNext()) {
			node = (TreeImageSet) i.next();
			if (node instanceof TreeImageTimeSet) {
				time = (TreeImageTimeSet) node;
				ref = new TimeRefObject(userID, TimeRefObject.TIME);
				ref.setTimeInterval(time.getStartTime(), time.getEndTime());
				m.put(time.getType(), ref);
			} else if (node instanceof TreeFileSet) {
				file = (TreeFileSet) node;
				ref = new TimeRefObject(userID, TimeRefObject.FILE);
				switch (file.getType()) {
					case TreeFileSet.PROTOCOL:
						ref.setFileType(MetadataHandlerView.EDITOR_PROTOCOL);
						break;
					case TreeFileSet.EXPERIMENT:
						ref.setFileType(MetadataHandlerView.EDITOR_EXPERIMENT);
						break;
					case TreeFileSet.MOVIE:
						ref.setFileType(MetadataHandlerView.MOVIE);
						break;
					case TreeFileSet.OTHER:
					default:
						ref.setFileType(MetadataHandlerView.OTHER);
				}
				m.put(file.getType(), ref);
			}
		}
		handle = dmView.countExperimenterImages(userID, m, this);
	}

	 /** 
     * Feeds the thumbnails back to the viewer, as they arrive. 
     * @see DataBrowserLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
        if (viewer.getState() == Browser.DISCARDED) return;  //Async cancel.
        Map map = (Map) fe.getPartialResult();
        if (map == null || map.size() != 1) return;
        Set set = map.entrySet();
        Entry entry;
        Iterator i = set.iterator();
        while (i.hasNext()) {
        	entry = (Entry) i.next();
        	viewer.setExperimenterCount(expNode, (Integer) entry.getKey(), 
        			entry.getValue());
		}
    }
    
    /**
     * Does nothing as the asynchronous call returns <code>null</code>.
     * The actual pay-load (number of items) is delivered progressively
     * during the updates.
     */
    public void handleNullResult() {}
    
    /**
     * Notifies the user that an error has occurred.
     * @see DataBrowserLoader#handleException(Throwable)
     */
    public void handleException(Throwable exc) 
    {
        String s = "Counting Failure: ";
        LogMessage msg = new LogMessage();
        msg.print(s);
        msg.print(exc);
        //register error but don't notify user.
        registry.getLogger().error(this, msg);
    }
    
    /**
     * Overridden so that we don't notify the user that the thumbnail
     * retrieval has been canceled.
     * @see DataTreeViewerLoader#handleCancellation() 
     */
    public void handleCancellation() 
    {
        String info = "The data retrieval has been cancelled.";
        registry.getLogger().info(this, info);
    }
    
}

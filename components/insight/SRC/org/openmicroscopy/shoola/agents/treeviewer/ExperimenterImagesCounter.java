/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeFileSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.MetadataHandlerView;
import omero.log.LogMessage;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/** 
 * Counts the number of images imported during various periods of time
 * by the specified user.
 * This class calls the <code>countExperimenterImages</code> in the
 * <code>DataManagerView</code>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class ExperimenterImagesCounter
	extends DataBrowserLoader
{

    /** The node hosting the experimenter the data are for. */
    private TreeImageSet expNode;

    /** The node hosting the time information. */
    private List<TreeImageSet> nodes;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *                Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param expNode The node hosting the experimenter the data are for.
     *                Mustn't be <code>null</code>.
     * @param nodes The time nodes. Mustn't be <code>null</code>.
     */
    public ExperimenterImagesCounter(Browser viewer, SecurityContext ctx,
            TreeImageSet expNode, List<TreeImageSet> nodes)
    {
        super(viewer, ctx);
        if (expNode == null)
            throw new IllegalArgumentException("Node not valid.");
        Object ho = expNode.getUserObject();
        if (!(ho instanceof ExperimenterData || ho instanceof GroupData))
            throw new IllegalArgumentException("Node not valid.");
        if (CollectionUtils.isEmpty(nodes))
            throw new IllegalArgumentException("No time node specified.");
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
        Iterator<TreeImageSet> i = nodes.iterator();
        TimeRefObject ref;
        long userID = -1;
        if (expNode.getUserObject() instanceof ExperimenterData)
            userID = expNode.getUserObjectId();
        Map<Integer, TimeRefObject> m;
        m = new LinkedHashMap<Integer, TimeRefObject>(nodes.size());
        TreeImageSet node;
        TreeImageTimeSet time;
        TreeFileSet file;
        while (i.hasNext()) {
            node = i.next();
            if (node instanceof TreeImageTimeSet) {
                time = (TreeImageTimeSet) node;
                ref = new TimeRefObject(userID, TimeRefObject.TIME);
                ref.setTimeInterval(time.getStartTime(), time.getEndTime());
                m.put(time.getType(), ref);
            } else if (node instanceof TreeFileSet) {
                file = (TreeFileSet) node;
                ref = new TimeRefObject(userID, TimeRefObject.FILE);
                switch (file.getType()) {
                    case TreeFileSet.MOVIE:
                        ref.setFileType(MetadataHandlerView.MOVIE);
                        break;
                    case TreeFileSet.TAG:
                        ref.setFileType(MetadataHandlerView.TAG_NOT_OWNED);
                        break;
                    case TreeFileSet.OTHER:
                    default:
                        ref.setFileType(MetadataHandlerView.OTHER);
                }
                m.put(file.getType(), ref);
            }
        }
        handle = dmView.countExperimenterImages(ctx, userID, m, this);
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

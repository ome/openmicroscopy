/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola.agents.metadata;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.openmicroscopy.shoola.agents.events.metadata.ChannelSavedEvent;
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.event.EventBus;
import omero.gateway.model.ChannelData;
import omero.gateway.model.DataObject;

/**
 * Updates the channels for images related to the specified data object.
 * This class calls one of the <code>saveChannelData</code> methods in the
 * <code>DataManagerView</code>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class ChannelDataSaver
    extends EditorLoader
{

    /** The id of the pixels set. */
    private List<ChannelData> channels;

    /** The id of the user. */
    private DataObject parent;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /**
     * Creates a new instance.
     * 
     * @param viewer Reference to the viewer. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param channels The channels to handle.
     * @param parent The parent of the channels.
     */
    public ChannelDataSaver(Editor viewer, SecurityContext ctx,
            List<ChannelData> channels, DataObject parent)
    {
        super(viewer, ctx);
        if (CollectionUtils.isEmpty(channels))
            throw new IllegalArgumentException("No Channels specified.");
        this.channels = channels;
        this.parent = parent;
    }

    /** 
     * Saves the channels and updates the images linked to the specified
     * object.
     * @see EditorLoader#load()
     */
    public void load()
    {
        List<DataObject> list = new ArrayList<DataObject>();
        if (parent != null) list.add(parent);
        handle = dmView.saveChannelData(ctx, channels, list, this);
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
        EventBus bus = MetadataViewerAgent.getRegistry().getEventBus();
        bus.post(new ChannelSavedEvent(ctx, channels, (List<Long>) result));
    }

}

/*
 * org.openmicroscopy.shoola.agents.imviewer.RenderingSettingsLoader 
 *
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
package org.openmicroscopy.shoola.agents.imviewer;


//Java imports
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

//Third-party libraries
import org.apache.commons.collections.CollectionUtils;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;

import pojos.DataObject;
import pojos.ExperimenterData;

/** 
 * Retrieves all the rendering settings related to the specified set 
 * of pixels.
 * This class calls the <code>getRenderingSettings</code> method in the
 * <code>ImageDataView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class RenderingSettingsLoader
    extends DataLoader
{

    /** The ID of the pixels set. */
    private long        pixelsID;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;

    /** 
     * Flag indicating to retrieve the renderings setting for the currently
     * logged in user if set to <code>true</code>.
     */
    private boolean 	single;

    /** Indicates to apply the setting of the rendering settings. */
    private long		ownerID;

    /**
     * Creates a new instance
     * 
     * @param viewer The view this loader is for. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param pixelsID The id of the pixels set.
     */
    public RenderingSettingsLoader(ImViewer viewer, SecurityContext ctx,
            long pixelsID)
    {
        this(viewer, ctx, pixelsID, false);
    }

    /**
     * Creates a new instance
     * 
     * @param viewer The view this loader is for. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param pixelsID The id of the pixels set.
     * @param single Pass <code>true</code> to indicate that the rendering
     *               settings are only for the current user.
     */
    public RenderingSettingsLoader(ImViewer viewer, SecurityContext ctx,
            long pixelsID, boolean single)
    {
        super(viewer, ctx);
        if (pixelsID < 0)
            throw new IllegalArgumentException("Pixels ID not valid.");
        this.pixelsID = pixelsID;
        this.single = single;
        ownerID = -1;
    }

    /**
     * Sets the identifier of the owner.
     * 
     * @param ownerID The value to set.
     */
    public void setOwner(long ownerID) { this.ownerID = ownerID; }

    /**
     * Retrieves the rendering settings for the selected pixels set.
     * @see DataLoader#load()
     */
    public void load()
    {
        if (single) {
            ExperimenterData exp = ImViewerAgent.getUserDetails();
            handle = ivView.getRenderingSettings(ctx, pixelsID, exp.getId(),
                    this);
        } else handle = ivView.getRenderingSettings(ctx, pixelsID, this);
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /** 
     * Feeds the result back to the viewer. 
     * @see DataLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == ImViewer.DISCARDED) return;  //Async cancel.
        Map<DataObject, Collection<RndProxyDef>> map =
                (Map<DataObject, Collection<RndProxyDef>>) result;

        Entry<DataObject, Collection<RndProxyDef>> entry;
        Iterator<Entry<DataObject, Collection<RndProxyDef>>> i =
                map.entrySet().iterator();
        DataObject exp;
        if (single) { 
            long userID = ImViewerAgent.getUserDetails().getId();
            while (i.hasNext()) {
                entry = i.next();
                exp = entry.getKey();
                if (userID == exp.getId()) {
                    Collection<RndProxyDef> def = entry.getValue();
                    if (CollectionUtils.isNotEmpty(def))
                        viewer.setSettingsToPaste(def.iterator().next());
                }
            }
        } else {
            //Create a new map to avoid major changes for now
            Map<DataObject, RndProxyDef> m =
                    new HashMap<DataObject, RndProxyDef>(map.size());
            while (i.hasNext()) {
                entry = i.next();
                exp = entry.getKey();
                Collection<RndProxyDef> def = entry.getValue();
                if (CollectionUtils.isNotEmpty(def))
                    m.put(entry.getKey(), def.iterator().next());
            }
            viewer.setRenderingSettings(m, ownerID);
        }
    }

}

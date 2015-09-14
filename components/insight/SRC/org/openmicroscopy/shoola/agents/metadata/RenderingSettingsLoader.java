/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata;


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;

import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.env.LookupNames;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import omero.gateway.model.DataObject;
import omero.gateway.model.ExperimenterData;

/** 
 * Loads all rendering settings associated to an image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class RenderingSettingsLoader
    extends MetadataLoader
{

    /** Task Id for loading the viewedby items */
    public static final int TASK_VIEWEDBY = 0;
    
    /** Task Id for loading copied rendering settings */
    public static final int TASK_COPY_PASTE = 1;
    
    /** The ID of the pixels set. */
    private long pixelsID;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;

    /** The task this loader should execute */
    private int task = -1;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param pixelsID The identifier of the pixels set.
     * @param loaderID The identifier of the loader.
     * @param task The task this loader is intended for
     */
    public RenderingSettingsLoader(MetadataViewer viewer, SecurityContext ctx,
            long pixelsID, int loaderID, int task)
    {
        super(viewer, ctx, loaderID);
        this.task = task;
        this.pixelsID = pixelsID;
    }

    /** 
     * Loads the folders containing the object.
     * @see MetadataLoader#cancel()
     */
    public void load()
    {
        handle = ivView.getRenderingSettings(ctx, pixelsID, this);
    }

    /** 
     * Cancels the data loading.
     * @see MetadataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /**
     * Feeds the result back to the viewer.
     * @see MetadataLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
        Map<DataObject, Collection<RndProxyDef>> map =
                (Map<DataObject, Collection<RndProxyDef>>) result;
        
        if(task==TASK_VIEWEDBY) {
            if (viewer.getState() == MetadataViewer.DISCARDED) return;  //Async cancel.
            //Create a new map to avoid major changes for now
            Map<DataObject, RndProxyDef> m =
                    new HashMap<DataObject, RndProxyDef>(map.size());
            Entry<DataObject, Collection<RndProxyDef>> entry;
            Iterator<Entry<DataObject, Collection<RndProxyDef>>> i =
                    map.entrySet().iterator();
            while (i.hasNext()) {
                entry = i.next();
                Collection<RndProxyDef> def = entry.getValue();
                if (CollectionUtils.isNotEmpty(def))
                    m.put(entry.getKey(), def.iterator().next());
            }
            viewer.setViewedBy(m);
        }
        else if (task==TASK_COPY_PASTE) {
            if (viewer.getRenderer() == null)
                return;

            ExperimenterData user = (ExperimenterData) MetadataViewerAgent
                    .getRegistry().lookup(LookupNames.CURRENT_USER_DETAILS);
            
            Iterator<Entry<DataObject, Collection<RndProxyDef>>> i = map
                    .entrySet().iterator();
            while (i.hasNext()) {
                Entry<DataObject, Collection<RndProxyDef>> entry = i.next();
                if (entry.getKey() instanceof ExperimenterData) {
                    ExperimenterData exp = (ExperimenterData) entry.getKey();
                    if (exp.getId() == user.getId()) {
                        Collection<RndProxyDef> def = entry.getValue();
                        viewer.applyRenderingSettings(def.iterator().next());
                        break;
                    }
                }
            }
        }
    }

}

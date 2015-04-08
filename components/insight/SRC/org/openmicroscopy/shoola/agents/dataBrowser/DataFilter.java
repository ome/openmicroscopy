/*
 * org.openmicroscopy.shoola.agents.dataBrowser.DataFilter 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.env.data.util.SearchParameters;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import pojos.DataObject;
import pojos.ImageData;

/** 
 * Filters the data.
 * This class calls the <code>filterData</code> method in the
 * <code>MetadataHandlerView</code>
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
public class DataFilter
    extends DataBrowserLoader
{

    /** The type of node to handle. */
    private Class nodeType;

    /** The collection of nodes to filter. */
    private List<Long> nodeIds;

    /** Store the nodes for later reused. */
    private Map<Long, DataObject> nodes;

    /** The filtering context. */
    private FilterContext context;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /**
     * Filters the image by date. Returns <code>true</code> if the image is
     * in the interval, <code>false</code> otherwise.
     * 
     * @param image The image to handle.
     * @return See above.
     */
    private boolean filterByDate(ImageData image)
    {
        Timestamp start = context.getFromDate();
        Timestamp end = context.getToDate();
        if (start == null && end == null) return true;
        Timestamp date;
        if (context.getTimeType() == SearchParameters.DATE_ACQUISITION) {
            date = image.getAcquisitionDate();
        } else {
            date = image.getCreated();
        }
        if (date == null) return false;
        if (start != null && end != null) {
            return date.after(start) && date.before(end);
        }
        if (start == null) {
            return date.before(end);
        }
        return date.after(start);
    }
    /**
     * Creates a new instance.
     *
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param context The filtering context. Mustn't be <code>null</code>.
     * @param nodes The collection of objects to filter.
     *              Mustn't be <code>null</code>.
     */
    public DataFilter(DataBrowser viewer, SecurityContext ctx,
            FilterContext context, Collection<DataObject> nodes)
    {
        super(viewer, ctx);
        if (CollectionUtils.isEmpty(nodes))
            throw new IllegalArgumentException("No nodes to filter.");
        if (context == null)
            throw new IllegalArgumentException("No filtering context.");
        this.context = context;
        this.nodes = new HashMap<Long, DataObject>();
        nodeIds  = new ArrayList<Long>();
        Iterator<DataObject> i = nodes.iterator();
        DataObject data;
        while (i.hasNext()) {
            data = i.next();
            nodeIds.add(data.getId());
            nodeType = data.getClass();
            this.nodes.put(data.getId(), data);
        }
    }

    /** 
     * Cancels the data loading.
     * @see DataBrowserLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /**
     * Loads the rating annotations for the specified nodes.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
        handle = mhView.filterData(ctx, nodeType, nodeIds, context, -1, this);
    }

    /**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
        if (viewer.getState() == DataBrowser.DISCARDED) return;  //Async cancel.
        Collection r = (Collection) result;
        List<DataObject> filteredNodes = new ArrayList<DataObject>();
        List<String> names = context.getNames();
        if (CollectionUtils.isEmpty(r)) {
            viewer.setFilteredNodes(filteredNodes, names);
        } else {
            //Filter the results by date.
            Iterator i = r.iterator();
            DataObject object;
            while (i.hasNext()) {
                object = nodes.get(i.next());
                if (object instanceof ImageData) {
                    if (filterByDate((ImageData) object)) {
                        filteredNodes.add(object);
                    }
                } else {
                    filteredNodes.add(object);
                }
            }
            viewer.setFilteredNodes(filteredNodes, names);
        }
    }

}

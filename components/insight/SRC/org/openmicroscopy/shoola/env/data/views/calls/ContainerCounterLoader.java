/*
 * org.openmicroscopy.shoola.env.data.views.calls.ContainerCounterLoader
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

package org.openmicroscopy.shoola.env.data.views.calls;

//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.PlateData;
import pojos.TagAnnotationData;

/** 
 * Command to retrieve the number of items contained in a specified collection
 * of containers. 
 * Note that we don't retrieve the items i.e. lazy loading rule.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class ContainerCounterLoader
    extends BatchCallTree
{

    /** The lastly retrieved map. */
    private Object result;

    /** The batch call. */
    private BatchCall loadCall;

    /** 
     * Creates a {@link BatchCall} to retrieve the number of items per
     * container.
     * 
     * @param ctx The security context.
     * @param types A Map whose keys are the type of object and the values are
     *              the identifiers of the objects.
     * @return See above
     */
    private BatchCall makeBatchCall(final SecurityContext ctx,
            final Map<Class<?>, List<Long>> types)
    {

        return new BatchCall("Counting items.") {
            public void doCall() throws Exception
            { 
                OmeroDataService os = context.getDataService();
                Entry<Class<?>, List<Long>> entry;
                Iterator<Entry<Class<?>, List<Long>>> i = types.entrySet().iterator();
                Class<?> type;
                Iterator<Long> j;
                List<Long> ids;
                Long id;
                OmeroImageService ms = context.getImageService();
                ExperimenterData exp = (ExperimenterData) context.lookup(
                        LookupNames.CURRENT_USER_DETAILS);
                long userID = exp.getId();
                Map<Long, Long> m = new HashMap<Long, Long>();
                Map<Class<?>, Map<Long, Long>> count = new HashMap<Class<?>,
                        Map<Long, Long>>();
                AdminService svc = context.getAdminService();
                while (i.hasNext()) {
                    entry = i.next();
                    type = entry.getKey();
                    ids = entry.getValue();
                    if (PlateData.class.equals(type)) {
                        j = ids.iterator();
                        while (j.hasNext()) {
                            id = j.next();
                            m.put(id, Long.valueOf(
                                    ms.loadROIMeasurements(ctx,
                                            type, id, userID).size()));
                        }
                        result = m;
                    } else if (GroupData.class.equals(type)) {
                        svc = context.getAdminService();
                        result = count.put(type, svc.countExperimenters(ctx,
                                ids));
                    } else {
                        count.put(type, os.getCollectionCount(ctx, type,
                                OmeroDataService.IMAGES_PROPERTY, ids));
                    }
                }
                if (count.size() > 0)
                    result = count;
            }
        };
    }

    /**
     * Adds a {@link BatchCall} to the tree for each value to retrieve.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns <code>null</code> as there's no final result.
     * In fact, values are progressively delivered with feedback events.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }

    /**
     * Creates a new instance.
     * If bad arguments are passed, we throw a runtime
     * exception so to fail early and in the caller's thread.
     * 
     * @param ctx The security context.
     * @param rootIDs Collection of root ids. Mustn't be <code>null</code>.
     */
    public ContainerCounterLoader(SecurityContext ctx, Set<DataObject> rootIDs)
    {
        if (rootIDs == null) throw new NullPointerException("No root nodes.");
        Iterator<DataObject> i = rootIDs.iterator();
        DataObject root;
        Long id = null;
        Class<?> rootType = null;
        List<Long> ids = new ArrayList<Long>();
        Map<Class<?>, List<Long>> types = new HashMap<Class<?>, List<Long>>();
        while (i.hasNext()) {
            root = i.next();
            if (root instanceof DatasetData) {
                rootType = DatasetData.class;
                id = Long.valueOf(((DatasetData) root).getId());
            } else if (root instanceof TagAnnotationData) {
                rootType = TagAnnotationData.class;
                id = Long.valueOf(((TagAnnotationData) root).getId());
            } else if (root instanceof PlateData) {
                rootType = PlateData.class;
                id = Long.valueOf(((PlateData) root).getId());
            }  else if (root instanceof GroupData) {
                rootType = GroupData.class;
                id = Long.valueOf(((GroupData) root).getId());
            }
            if (!types.containsKey(rootType))
                types.put(rootType, new ArrayList<Long>());
            ids = types.get(rootType);
            if (id != null) ids.add(id);
        }
        loadCall = makeBatchCall(ctx, types);
    }

}

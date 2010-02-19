/*
 * org.openmicroscopy.shoola.env.data.views.calls.ContainerCounterLoader
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.env.data.views.calls;

//Java imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.OmeroImageService;
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
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class ContainerCounterLoader
	extends BatchCallTree
{

    /** The lastly retrieved map. */
    private Object result;

    /** The batch call. */
    private BatchCall	loadCall;
    
    /** 
     * Creates a {@link BatchCall} to retrieve the number of items per 
     * container.
     * 
     * @param ids  The ids of the node to count.
     * @param type The type of nodes.
     * @return See above
     */
    private BatchCall makeBatchCall(final List<Long> ids, final Class type)
    {
        
        return new BatchCall("Counting items.") {
		    public void doCall() throws Exception
		    { 
		        OmeroDataService os = context.getDataService();
		        if (PlateData.class.equals(type)) {
		        	Iterator<Long> i = ids.iterator();
		        	OmeroImageService ms = context.getImageService();
		        	Long id;
		        	ExperimenterData exp = (ExperimenterData) context.lookup(
							LookupNames.CURRENT_USER_DETAILS);
		        	//Long userID = context.get
		        	Map<Long, Long> m = new HashMap<Long, Long>();
		        	long userID = exp.getId();
		        	while (i.hasNext()) {
						id = i.next();
						m.put(id, new Long(
								ms.loadROIMeasurements(
										type, id, userID).size()));
					}
		        	
		        	result = m;
		        } else if (GroupData.class.equals(type)) {
		        	AdminService svc = context.getAdminService();
		        	result = svc.countExperimenters(ids);
		        } else {
		        	 result = os.getCollectionCount(type, 
		                		OmeroDataService.IMAGES_PROPERTY, ids);
		        }
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
     * @param rootIDs	Collection of root ids. Mustn't be <code>null</code>.
     */
    public ContainerCounterLoader(Set<DataObject> rootIDs)
    {
        if (rootIDs == null) throw new NullPointerException("No root nodes.");
    	Iterator i = rootIDs.iterator();
        DataObject root;
        Long id = null;
        Class rootType = null;
        List<Long> ids = new ArrayList<Long>();
        while (i.hasNext()) {
            root = (DataObject) i.next();
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
            if (id != null) ids.add(id);
        }
        loadCall = makeBatchCall(ids, rootType);
    }
    
}

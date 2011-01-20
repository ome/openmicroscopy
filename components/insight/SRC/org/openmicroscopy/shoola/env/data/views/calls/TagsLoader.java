/*
 * org.openmicroscopy.shoola.env.data.views.calls.TagsLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
import org.openmicroscopy.shoola.env.data.OmeroMetadataService;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.DataObject;


/** 
 * 
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
public class TagsLoader
	extends BatchCallTree
{

	/** The result of the call. */
    private Object		result;
    
    /** Loads the specified experimenter groups. */
    private BatchCall   loadCall;
    
    /**
     * Creates a {@link BatchCall} to retrieve the tags.
     * 
     * @param nodes T
     * @return The {@link BatchCall}.
     */
    private BatchCall reloadTags(final Map<Long, List> nodes)
    {
    	return new BatchCall("Reloadingtags.") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
            	Iterator users = nodes.keySet().iterator();
            	Long userID;
            	List containers;
            	Map<Long, Object> r = new HashMap<Long, Object>(nodes.size());
                Object value;
                Iterator j, c;
                DataObject data;
                List l;
                Set children = null;
                Long id;
                Class klass = null;
                Set newChildren;
                List loaded = new ArrayList();
            	while (users.hasNext()) {
            		userID = (Long) users.next();
            		containers = nodes.get(userID);
            		if (containers == null || containers.size() == 0) {
            			value = os.loadTagSetsContainer(-1L, false, userID);
            		} else {
            			value = os.loadTagSetsContainer(-1L, false, userID);
            			
            			/*
            			l = new ArrayList();
            			j = containers.iterator();
            			while (j.hasNext()) {
            				data = (DataObject) j.next();
            				id = data.getId();
            				loaded.add(id);
            				c = os.loadTagsContainer(id, true, userID);
            				if (c != null && c.size() > 0);
            				l.addAll(c);
						}
            			c = os.loadTagsContainer(-1L, false, userID);
            			j = c.iterator();
            			while (j.hasNext()) {
            				data = (DataObject) j.next();
            				id = data.getId();
            				if (!loaded.contains(id))
            					l.add(data);
						}
            			value = l;
            			*/
            		}
            		r.put(userID, value);
				}
            	result = r;
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to retrieve the tagSets owned by the passed
     * user.
     * 
     * @param id			The id of the tag or <code>-1</code>.
     * @param dataObject    Pass <code>true</code> to load the 
	 * 						<code>DataObject</code> related 
     * 						to the tags, <code>false</code> otherwise.
     * @param topLevel  	Pass <code>true</code> to load <code>Tag Set</code>,
     * 						<code>false</code> to load <code>Tag</code>.
     * 						This will be taken into account if the Id is 
     * 						negative.
     * @param userID		The id of the user.
     * @return The {@link BatchCall}.
     */
    private BatchCall loadTagsCall(final Long id, final boolean dataObject, 
    							final boolean topLevel,	final long userID)
    {
        return new BatchCall("Loading tags.") {
            public void doCall() throws Exception
            {
            	OmeroMetadataService os = context.getMetadataService();
            	result = os.loadTags(id, dataObject, topLevel, userID);
            }
        };
    }
    
    /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns, in a <code>Set</code>, the root nodes of the found trees.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }
    
    /**
     * Creates a new instance.
     * 
     * @param id			The id of the parent the tags are related to, or 
     * 						<code>-1</code>.
     * @param withObjects	Pass <code>true</code> to load the objects related 
     * 						to the tags, <code>false</code> otherwise.
     * @param topLevel  	Pass <code>true</code> to load <code>Tag Set</code>,
     * 						<code>false</code> to load <code>Tag</code>.
     * 						This will be taken into account if the Id is 
     * 						negative.
     * @param userID		The id of the user who owns the tags or tag sets.
     */
	public TagsLoader(Long id, boolean withObjects, boolean topLevel, 
			long userID)
	{
		loadCall = loadTagsCall(id, withObjects, topLevel, userID);
	}

}

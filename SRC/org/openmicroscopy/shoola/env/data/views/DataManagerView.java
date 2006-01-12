/*
 * org.openmicroscopy.shoola.env.data.views.DataManagerView
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data.views;


//Java imports
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import pojos.DataObject;

/** 
 * Provides methods to support data management.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public interface DataManagerView
    extends DataServicesView
{

    /** 
     * Indicates the root of the retrieved data is the <code>World</code>
     * i.e. any user in the system.
     */
    public static final int	WORLD_HIERARCHY_ROOT = 0;
    
    /** 
     * Indicates the root of the retrieved data is the <code>Group</code>
     * i.e. one of the groups the current user belongs to.
     */
    public static final int	GROUP_HIERARCHY_ROOT = 1;
    
    /** 
     * Indicates the root of the retrieved data is the <code>User</code>
     * i.e. the current user.
     */
    public static final int	USER_HIERARCHY_ROOT = 2;
    
    /**
     * Retrieves the hierarchies specified by the 
     * parameters.
     * 
     * @param rootNodeType  The type of the root node. Can only be one out of:
     *                      <code>ProjectData, DatasetData, 
     *                      CategoryGroupData, CategoryData</code>.
     * @param rootNodeIDs   A set of the IDs of top-most containers.
     * @param withLeaves    Passes <code>true</code> to retrieve the images.
     *                      <code>false</code> otherwise.   
     * @param rootLevel		The level of the hierarchy, one of the constants
     * 						defined by this class.
     * @param rootLevelID	The Id of the root. The value is set to <i>-1</i>
     * 						if the rootLevel is either 
     * 						{@link #WORLD_HIERARCHY_ROOT} or 
     * 						{@link #USER_HIERARCHY_ROOT}.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadContainerHierarchy(Class rootNodeType,
                                            Set rootNodeIDs, boolean withLeaves,
            								int rootLevel, int rootLevelID,
                                            AgentEventListener observer);
    
    /**
     * Retrieves the images specified by the parameters.
     * 
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle loadImages(AgentEventListener observer);
    
    /**
     * Retrieves the images container in the specified root nodes.
     * 
     * @param nodeType 		The type of the node. Can only be one out of:
     *                      <code>DatasetData, CategoryData</code>.       
     * @param nodeIDs 		The id of the node.
     * @param rootLevel		The level of the hierarchy, one of the constants
     * 						defined by this class.
     * @param rootLevelID	The Id of the root. The value is set to <i>-1</i>
     * 						if the rootLevel is either 
     * 						{@link #WORLD_HIERARCHY_ROOT} or 
     * 						{@link #USER_HIERARCHY_ROOT}.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle getImages(Class nodeType, Set nodeIDs, int rootLevel, 
            					int rootLevelID, AgentEventListener observer);
    
    /**
     * Creates a new <code>DataObject</code> whose parent is specified by the
     * ID.
     * 
     * @param userObject    The type of <code>DataObject</code> to create.
     * @param parentID      The ID of the parent.  
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle createDataObject(DataObject userObject, int parentID,
                                        AgentEventListener observer);
    
    /**
     * Updates the specified <code>DataObject</code>.
     * 
     * @param userObject    The <code>DataObject</code> to save.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle updateDataObject(DataObject userObject,
                                        AgentEventListener observer);
    
    /**
     * Counts the number of items contained in the specified containers.
     * 
     * @param rootType 		The type of the root node. Can only be one out of:
     *                      <code>DatasetData, CategoryData</code>.
     * @param rootNodeIDs   A set of the IDs of top-most containers.
     * @param observer      Callback handler.
     * @return A handle that can be used to cancel the call.
     */
    public CallHandle countContainerItems(Class rootType, Set rootNodeIDs, 
            							AgentEventListener observer);
    
}

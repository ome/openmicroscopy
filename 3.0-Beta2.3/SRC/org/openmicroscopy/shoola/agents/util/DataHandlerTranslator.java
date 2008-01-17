/*
 * org.openmicroscopy.shoola.agents.util.DataHandlerTranslator 
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
package org.openmicroscopy.shoola.agents.util;


//Java imports

//Third-party libraries

//Application-internal dependencies
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheckNode;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.PermissionData;

/** 
 * This class contains a collection of utility static methods that transform
 * an hierarchy of {@link DataObject}s into a visualisation tree.
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
public class DataHandlerTranslator
{
	
    /**
     * Transforms a {@link DatasetData} into a visualisation object i.e.
     * a {@link TreeCheckNode}.
     * 
     * @param data  The {@link DatasetData} to transform.
     *              Mustn't be <code>null</code>.
     * @return See above.
     */
    private static TreeCheckNode transformDatasetCheckNode(DatasetData data)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        IconManager im = IconManager.getInstance();      
        TreeCheckNode node =  new TreeCheckNode(data, 
                                    im.getIcon(IconManager.DATASET),
                                    data.getName(), true);
        return node;
    }

    /**
     * Transforms a {@link CategoryData} into a visualisation object i.e.
     * a {@link TreeCheckNode}.
     * 
     * @param data  The {@link CategoryData} to transform.
     *              Mustn't be <code>null</code>.
     * @return See above.
     */
    private static TreeCheckNode transformCategoryCheckNode(CategoryData data)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        IconManager im = IconManager.getInstance();      
        TreeCheckNode category =  new TreeCheckNode(data, 
                                    im.getIcon(IconManager.CATEGORY),
                                    data.getName(), true);
        return category;
    }
    
    /**
     * Transforms a {@link CategoryGroupData} into a visualisation object i.e.
     * a {@link TreeCheckNode}. The {@link CategoryData categories} are also
     * transformed and linked to the newly created {@link TreeCheckNode}.
     * 
     * @param data      The {@link CategoryGroupData} to transform.
     *                  Mustn't be <code>null</code>.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.             
     * @return See above.
     */
    private static TreeCheckNode transformCategoryGroupCheckNode(
                                CategoryGroupData data, long userID, 
                                long groupID)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        IconManager im = IconManager.getInstance();
        TreeCheckNode group = new TreeCheckNode(data, 
                                im.getIcon(IconManager.CATEGORY_GROUP), 
                                data.getName(), false);
        Set categories = data.getCategories();
        Iterator i = categories.iterator();
        CategoryData child;
        while (i.hasNext()) {
            child = (CategoryData) i.next();
            if (isWritable(child, userID, groupID))
                group.addChildDisplay(transformCategoryCheckNode(child));
        }
            
        return group;
    }  
    
    /**
     * Transforms a set of {@link DataObject}s into their corresponding 
     * {@link TreeCheckNode} visualization objects. The elements of the set can 
     * either be {@link CategoryGroupData}, {@link CategoryData} or
     * {@link DatasetData}.
     * 
     * @param dataObjects   The collection of {@link DataObject}s to transform.
     * @param userID        The id of the current user.
     * @param groupID       The id of the group the current user selects when 
     *                      retrieving the data.
     * @return A set of visualization objects.
     */
    public static Set transformDataObjectsCheckNode(Set dataObjects,
                                        long userID, long groupID)
    {
        if (dataObjects == null)
            throw new IllegalArgumentException("No objects.");
        Set results = new HashSet(dataObjects.size());
        Iterator i = dataObjects.iterator();
        DataObject ho;
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (isWritable(ho, userID, groupID)) {
                if (ho instanceof CategoryGroupData) {
                    Set categories = ((CategoryGroupData) ho).getCategories();
                    if (categories != null && categories.size() != 0)
                        results.add(transformCategoryGroupCheckNode(
                                (CategoryGroupData) ho, userID, groupID));
                } else if (ho instanceof CategoryData)
                    results.add(transformCategoryCheckNode((CategoryData) ho));
                else if (ho instanceof DatasetData) 
                    results.add(transformDatasetCheckNode((DatasetData) ho));
            }  
        }
        return results;
    }
    
    /**
     * Returns <code>true</code> if the specified data object is readable,
     * <code>false</code> otherwise, depending on the permission.
     * 
     * @param ho        The data object to check.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.
     * @return See above.
     */
    public static boolean isWritable(DataObject ho, long userID, long groupID)
    {
        PermissionData permissions = ho.getPermissions();
        if (userID == ho.getOwner().getId())
            return permissions.isUserWrite();
        /*
        Set groups = ho.getOwner().getGroups();
        Iterator i = groups.iterator();
        long id = -1;
        boolean groupRead = false;
        while (i.hasNext()) {
            id = ((GroupData) i.next()).getId();
            if (groupID == id) {
                groupRead = true;
                break;
            }
        }
        if (groupRead) return permissions.isGroupWrite();
        return permissions.isWorldWrite();
        */
        return permissions.isGroupWrite();
    }
    
}

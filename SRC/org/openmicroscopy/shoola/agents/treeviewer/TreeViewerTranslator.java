/*
 * org.openmicroscopy.shoola.agents.treemng.TreeViewerTranslator
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

package org.openmicroscopy.shoola.agents.treeviewer;



//Java imports
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageNode;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageSet;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheckNode;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PermissionData;
import pojos.ProjectData;

/** 
 * This class contains a collection of utility static methods that transform
 * an hierarchy of {@link DataObject}s into a visualisation tree.
 * The tree is then displayed in the TreeViewer. For example,
 * A list of Projects-Datasets is passed to the 
 * {@link #transformHierarchy(Set, long, long)} method and transforms into a set 
 * of TreeImageSet-TreeImageSet.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TreeViewerTranslator
{
    
    /**
     * Creates a default timestamp.
     * 
     * @return See above.
     */
    private static Timestamp getDefaultTimestamp()
    {
        Date today = new Date();
        return new Timestamp(today.getTime());
    }
    
    /**
     * Formats the toolTip of the specified {@link TreeImageDisplay} node.
     * 
     * @param node The specified node. Mustn't be <code>null</code>.
     */
    private static void formatToolTipFor(TreeImageDisplay node)
    {
        if (node == null) throw new IllegalArgumentException("No node");
        String toolTip = "";
        String title = null;
        if (node.getUserObject() instanceof ImageData) {
            Timestamp time = null;
            try {
                time = ((ImageData) node.getUserObject()).getInserted();
            } catch (Exception e) {
                // TODO: handle exception
            }
            if (time == null) time = getDefaultTimestamp();
            title = DateFormat.getDateInstance().format(time);   
            toolTip = UIUtilities.formatToolTipText(title);
            node.setToolTip(toolTip); 
        }
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
     * Transforms a {@link ImageData} into a visualisation object i.e.
     * a {@link TreeCheckNode}.
     * 
     * @param data  The {@link ImageData} to transform.
     *              Mustn't be <code>null</code>.
     * @return See above.
     */
    private static TreeCheckNode transformImageCheckNode(ImageData data)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        IconManager im = IconManager.getInstance();      
        TreeCheckNode node =  new TreeCheckNode(data, 
                                    im.getIcon(IconManager.IMAGE),
                                    data.getName(), true);
        return node;
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
     * Transforms a {@link DatasetData} into a visualisation object i.e.
     * a {@link TreeImageSet}.
     * 
     * @param data      The {@link DatasetData} to transform.
     *                  Mustn't be <code>null</code>.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.                 
     * @return See above.
     */
    private static TreeImageDisplay transformDataset(DatasetData data, 
                                            long userID, long groupID)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        TreeImageSet dataset =  new TreeImageSet(data);
        Set images = data.getImages();
        if (images == null) { dataset.setNumberItems(0);
        } else {
            dataset.setChildrenLoaded(Boolean.TRUE);
            dataset.setNumberItems(images.size());
            Iterator i = images.iterator();
            ImageData child;
            while (i.hasNext()) {
                child = (ImageData) i.next();
                if (isReadable(child, userID, groupID))
                    dataset.addChildDisplay(transformImage(child));
                
            }
        }
        
        formatToolTipFor(dataset);
        return dataset;
    }
    
    /**
     * Transforms a {@link ProjectData} into a visualisation object i.e.
     * a {@link TreeImageSet}. The {@link DatasetData datasets} are also
     * transformed and linked to the newly created {@link TreeImageSet}.
     * 
     * @param data      The {@link ProjectData} to transform.
     *                  Mustn't be <code>null</code>.
     * @param datasets  The list of datasets to add.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.             
     * @return See above.
     */
    private static TreeImageDisplay transformProject(ProjectData data, 
                        Set datasets, long userID, long groupID)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        TreeImageSet project = new TreeImageSet(data);
        if (datasets != null) {
            project.setChildrenLoaded(Boolean.TRUE);
            Iterator i = datasets.iterator();
            DatasetData child;
            while (i.hasNext()) {
                child = (DatasetData) i.next();
                if (isReadable(child, userID, groupID))
                    project.addChildDisplay(transformDataset(child, userID, 
                                                            groupID));
            }
            project.setNumberItems(datasets.size());
        } else {
            //the datasets were not loaded
            project.setChildrenLoaded(Boolean.FALSE); 
            project.setNumberItems(0);
        }
        return project;
    }
    
    /**
     * Transforms a {@link CategoryData} into a visualisation object i.e.
     * a {@link TreeImageSet}.
     * 
     * @param data      The {@link CategoryData} to transform.
     *                  Mustn't be <code>null</code>.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.                
     * @return See above.
     */
    private static TreeImageDisplay transformCategory(CategoryData data, 
                                                long userID, long groupID)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        TreeImageSet category =  new TreeImageSet(data);
        Set images = data.getImages();
        if (images == null) { category.setNumberItems(0);
        } else {
            category.setChildrenLoaded(Boolean.TRUE);
            category.setNumberItems(images.size());
            Iterator i = images.iterator();
            ImageData child;
            while (i.hasNext()) {
                child = (ImageData) i.next();
                if (isReadable(child, userID, groupID))
                    category.addChildDisplay(transformImage(child));
            }
        }
        return category;
    } 
    
    /**
     * Transforms a {@link CategoryGroupData} into a visualisation object i.e.
     * a {@link TreeImageSet}. The {@link CategoryData categories} are also
     * transformed and linked to the newly created {@link TreeImageSet}.
     * 
     * @param data          The {@link CategoryGroupData} to transform.
     *                      Mustn't be <code>null</code>.
     * @param categories    The categories to add.
     * @param userID        The id of the current user.
     * @param groupID       The id of the group the current user selects when 
     *                      retrieving the data.                    
     * @return See above.
     */
    private static TreeImageDisplay transformCategoryGroup(
            CategoryGroupData data, Set categories, long userID, long groupID)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        TreeImageSet group = new TreeImageSet(data);
        if (categories != null) {
            group.setChildrenLoaded(Boolean.TRUE);
            Iterator i = categories.iterator();
            CategoryData child;
            while (i.hasNext())  {
                child = (CategoryData) i.next();
                if (isReadable(child, userID, groupID))
                    group.addChildDisplay(transformCategory(child, userID, 
                                                            groupID));
            }   
            group.setNumberItems(categories.size());
        } else {
            //categories not loaded.
            group.setChildrenLoaded(Boolean.TRUE);
            group.setNumberItems(0);
        }
        return group;
    }
    
    /**
     * Transforms a {@link ImageData} into a visualisation object i.e.
     * a {@link TreeImageNode}.
     * 
     * @param data  The {@link ImageData} to transform.
     *              Mustn't be <code>null</code>.
     * @return See above.
     */
    private static TreeImageDisplay transformImage(ImageData data)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        TreeImageNode node = new TreeImageNode(data);
        formatToolTipFor(node);
        return node;
    }
    
    /**
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set can either be
     * {@link ProjectData}, {@link CategoryGroupData} or {@link ImageData}.
     * 
     * @param dataObjects   The collection of {@link DataObject}s to transform.
     * @param userID        The id of the current user.
     * @param groupID       The id of the group the current user selects when 
     *                      retrieving the data.    
     * @return A set of visualization objects.
     */
    public static Set transformHierarchy(Set dataObjects, long userID, 
                                        long groupID)
    {
        if (dataObjects == null)
            throw new IllegalArgumentException("No objects.");
        Set results = new HashSet(dataObjects.size());
        Iterator i = dataObjects.iterator();
        DataObject ho;
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (isReadable(ho, userID, groupID)) {
                if (ho instanceof ProjectData)
                  results.add(transformProject((ProjectData) ho, 
                            ((ProjectData) ho).getDatasets(), userID, 
                                                groupID));
                else if (ho instanceof CategoryGroupData)
                    results.add(transformCategoryGroup((CategoryGroupData) ho, 
                            ((CategoryGroupData) ho).getCategories(),
                            userID, groupID));
                else if (ho instanceof ImageData)
                    results.add(transformImage((ImageData) ho));
            }   
        }
        return results;
    }
    
    /**
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set can either be
     * {@link ProjectData} or {@link CategoryGroupData}.
     * 
     * @param dataObjects   The collection of {@link DataObject}s to transform.
     * @param userID        The id of the current user.
     * @param groupID       The id of the group the current user selects when 
     *                      retrieving the data.    
     * @return A set of visualization objects.
     */
    public static Set transformContainers(Set dataObjects, long userID,
                                        long groupID)
    {
        if (dataObjects == null)
            throw new IllegalArgumentException("No objects.");
        Set results = new HashSet(dataObjects.size());
        Iterator i = dataObjects.iterator();
        DataObject ho, child;
        Iterator j;
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (ho instanceof ProjectData) {
                j = ((ProjectData) ho).getDatasets().iterator();
                while (j.hasNext()) {
                    child = (DataObject) j.next();
                    if (isReadable(child, userID, groupID))
                        results.add(transformDataset((DatasetData) child, 
                                                    userID, groupID));
                }  
            } else if (ho instanceof CategoryGroupData) {
                j = ((CategoryGroupData) ho).getCategories().iterator();
                while (j.hasNext()) {
                    child = (DataObject) j.next();
                    if (isReadable(child, userID, groupID))
                        results.add(transformCategory((CategoryData) child, 
                                                    userID, groupID));
                }   
            }
        }
        return results;
    }
    
    public static Set refreshHierarchy(Map nodes, long userID, long groupID)
    {
        if (nodes == null)
            throw new IllegalArgumentException("No objects.");
        Set results = new HashSet(nodes.size());
        Iterator i = nodes.keySet().iterator();
        DataObject ho;
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            System.out.println(ho);
            if (isReadable(ho, userID, groupID)) {
                if (ho instanceof ProjectData) {
                    results.add(transformProject((ProjectData) ho, 
                                            (Set) nodes.get(ho), 
                                            userID, groupID));
                } else if (ho instanceof CategoryGroupData) {
                    results.add(transformCategoryGroup(
                            (CategoryGroupData) ho, 
                            (Set) nodes.get(ho), 
                            userID, groupID)); 
                }
            }
        }
        return results;
    }
    
    /**
     * Transforms the specified {@link DataObject} into a visualisation
     * representation.
     * 
     * @param object    The {@link DataObject} to transform.
     *                  Mustn't be <code>null</code>.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data.                  
     * @return See above
     */
    public static TreeImageDisplay transformDataObject(DataObject object, 
                                long userID, long groupID)
    {
        if (object == null)
            throw new IllegalArgumentException("No object.");
        if (!(isReadable(object, userID, groupID)))
            throw new IllegalArgumentException("Data object not readable.");
        if (object instanceof ProjectData)
            return transformProject((ProjectData) object, 
                    ((ProjectData) object).getDatasets(), userID, groupID);
        else if (object instanceof DatasetData)
            return transformDataset((DatasetData) object, userID, groupID);
        else if (object instanceof CategoryData)
            return transformCategory((CategoryData) object, userID, groupID);
        else if (object instanceof CategoryGroupData)
            return transformCategoryGroup((CategoryGroupData) object, 
                    ((CategoryGroupData) object).getCategories(),
                                        userID, groupID);
        else if (object instanceof ImageData)
            return transformImage((ImageData) object);
        throw new IllegalArgumentException("Data Type not supported.");
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
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set can either be
     * {@link DatasetData}, {@link CategoryData} or {@link ImageData}.
     * 
     * @param paths     Collection of {@link DataObject}s to transform.
     * @param userID    The id of the current user.
     * @param groupID   The id of the group the current user selects when 
     *                      retrieving the data. 
     * @return A set of visualization objects.
     */
    public static Set transformIntoCheckNodes(Set paths, long userID, 
                                                long groupID)
    {
        if (paths == null)
            throw new IllegalArgumentException("No objects.");
        Set results = new HashSet();
        Iterator i = paths.iterator();
        DataObject ho;
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (isWritable(ho, userID, groupID)) {
                if (ho instanceof DatasetData)
                    results.add(transformDatasetCheckNode((DatasetData) ho));
                else if (ho instanceof CategoryData)
                    results.add(transformCategoryCheckNode((CategoryData) ho));
                else if (ho instanceof ImageData)
                    results.add(transformImageCheckNode((ImageData) ho));
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
    public static boolean isReadable(DataObject ho, long userID, long groupID)
    {
        PermissionData permissions = ho.getPermissions();
        if (userID == ho.getOwner().getId())
            return permissions.isUserRead();
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
        if (groupRead) return permissions.isGroupRead();
        return permissions.isWorldRead();
        */ 
        return permissions.isGroupRead();
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

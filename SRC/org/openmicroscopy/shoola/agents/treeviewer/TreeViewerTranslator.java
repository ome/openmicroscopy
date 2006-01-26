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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageNode;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageSet;
import org.openmicroscopy.shoola.env.ui.ViewerSorter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * This class contains a collection of utility static methods that transform
 * an hierarchy of {@link DataObject}s into a visualisation tree.
 * The tree is then displayed in the TreeViewer. For example,
 * A list of Projects-Datasets is passed to the 
 * {@link #transformHierarchy(Set)} method and transforms into a set of 
 * TreeImageSet-TreeImageSet.
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

    private static ViewerSorter sorter = new ViewerSorter();
    
    /**
     * Returns the first annotation of the specified set.
     * Returns <code>null</code> if the set is empty or <code>null</code>.
     * We first make sure that the annotations are ordered by date (ascending
     * order).
     * 
     * @param set The set to analyse.
     * @return See above.
     */
    private static AnnotationData getFirstAnnotation(Set set)
    {
        if (set == null || set.size() == 0) return null;
        HashMap map = new HashMap(set.size());
        Iterator i = set.iterator();
        AnnotationData data;
        HashSet timestamps = new HashSet(set.size());
        while (i.hasNext()) {
            data = (AnnotationData) i.next();
            map.put(data.getLastModified(), data);
            timestamps.add(data.getLastModified());
        }
        sorter.setAscending(false);
        List l = sorter.sort(timestamps);
        return (AnnotationData) map.get(l.get(0));
    }
    
    /**
     * Transforms a {@link DatasetData} into a visualisation object i.e.
     * a {@link TreeImageSet}.
     * 
     * @param data  The {@link DatasetData} to transform.
     *              Mustn't be <code>null</code>.
     * @return See above.
     */
    private static TreeImageDisplay transformDataset(DatasetData data)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        TreeImageSet dataset =  new TreeImageSet(data);
        dataset.setNumberItems(0);
        formatToolTipFor(dataset, getFirstAnnotation(data.getAnnotations()));
        return dataset;
    }
    
    /**
     * Transforms a {@link ProjectData} into a visualisation object i.e.
     * a {@link TreeImageSet}. The {@link DatasetData datasets} are also
     * transformed and linked to the newly created {@link TreeImageSet}.
     * 
     * @param data  The {@link ProjectData} to transform.
     *              Mustn't be <code>null</code>.
     * @return See above.
     */
    private static TreeImageDisplay transformProject(ProjectData data)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        TreeImageSet project = new TreeImageSet(data);
        Set datasets = data.getDatasets();
        Iterator i = datasets.iterator();
        while (i.hasNext()) 
            project.addChildDisplay(transformDataset((DatasetData) i.next()));
        project.setNumberItems(datasets.size());
        return project;
    }
    
    /**
     * Transforms a {@link CategoryData} into a visualisation object i.e.
     * a {@link TreeImageSet}.
     * 
     * @param data  The {@link CategoryData} to transform.
     *              Mustn't be <code>null</code>.
     * @return See above.
     */
    private static TreeImageDisplay transformCategory(CategoryData data)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        TreeImageSet category =  new TreeImageSet(data);
        category.setNumberItems(0);
        return category;
    }
    
    /**
     * Transforms a {@link CategoryGroupData} into a visualisation object i.e.
     * a {@link TreeImageSet}. The {@link CategoryData categories} are also
     * transformed and linked to the newly created {@link TreeImageSet}.
     * 
     * @param data  The {@link CategoryGroupData} to transform.
     *              Mustn't be <code>null</code>.
     * @return See above.
     */
    private static TreeImageDisplay transformCategoryGroup(CategoryGroupData
                                                            data)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        TreeImageSet group = new TreeImageSet(data);
        Set categories = data.getCategories();
        Iterator i = categories.iterator();
        while (i.hasNext())
            group.addChildDisplay(transformCategory((CategoryData) i.next()));
        group.setNumberItems(categories.size());
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
        formatToolTipFor(node, getFirstAnnotation(data.getAnnotations()));
        return node;
    }
    
    /**
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set can either be
     * {@link ProjectData}, {@link CategoryGroupData} or {@link ImageData}.
     * 
     * @param dataObjects The collection of {@link DataObject}s to transform.
     * @return A set of visualization objects.
     */
    public static Set transformHierarchy(Set dataObjects)
    {
        if (dataObjects == null)
            throw new IllegalArgumentException("No objects.");
        Set results = new HashSet(dataObjects.size());
        Iterator i = dataObjects.iterator();
        DataObject ho;
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (ho instanceof ProjectData)
                results.add(transformProject((ProjectData) ho));
            else if (ho instanceof CategoryGroupData)
                results.add(transformCategoryGroup((CategoryGroupData) ho));
            else if (ho instanceof ImageData)
                results.add(transformImage((ImageData) ho));
        }
        return results;
    }
    
    /**
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set can either be
     * {@link ProjectData} or {@link CategoryGroupData}.
     * 
     * @param dataObjects The collection of {@link DataObject}s to transform.
     * @return A set of visualization objects.
     */
    public static Set transformContainers(Set dataObjects)
    {
        if (dataObjects == null)
            throw new IllegalArgumentException("No objects.");
        Set results = new HashSet(dataObjects.size());
        Iterator i = dataObjects.iterator();
        DataObject ho;;
        Iterator j;
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (ho instanceof ProjectData) {
                j = ((ProjectData) ho).getDatasets().iterator();
                while (j.hasNext()) 
                    results.add(transformDataset((DatasetData) j.next()));
            } else if (ho instanceof CategoryGroupData) {
                j = ((CategoryGroupData) ho).getCategories().iterator();
                while (j.hasNext()) 
                    results.add(transformCategory((CategoryData) j.next()));
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
     * @return See above
     */
    public static TreeImageDisplay transformDataObject(DataObject object)
    {
        if (object == null)
            throw new IllegalArgumentException("No object.");
        if (object instanceof ProjectData)
            return transformProject((ProjectData) object);
        else if (object instanceof DatasetData)
            return transformDataset((DatasetData) object);
        else if (object instanceof CategoryData)
            return transformCategory((CategoryData) object);
        else if (object instanceof CategoryGroupData)
            return transformCategoryGroup((CategoryGroupData) object);
        throw new IllegalArgumentException("Data Type not supported.");
    }
    
    /**
     * Formats the toolTip of the specified {@link TreeImageDisplay} node.
     * 
     * @param node The specified node. Mustn't be <code>null</code>.
     * @param data The annotation data.
     */
    public static void formatToolTipFor(TreeImageDisplay node,
                                        AnnotationData data)
    {
        if (node == null) throw new IllegalArgumentException("No node");
        String toolTip = null;
        String title = null;
        if (node.getUserObject() instanceof ImageData) {
            Timestamp time = ((ImageData) node.getUserObject()).getInserted();
            title = DateFormat.getDateInstance().format(time);   
            if (data != null)
                toolTip = UIUtilities.makeParagraph(title, data.getText(),
                                                    UIUtilities.TABLE_WIDTH);
            else toolTip = UIUtilities.formatToolTipText(title);  
        }
        
        if ((node.getUserObject() instanceof DatasetData) && data != null)
            toolTip = UIUtilities.makeParagraph(title, data.getText(),
                    UIUtilities.TABLE_WIDTH);
        node.setToolTip(toolTip);
    }
    
}

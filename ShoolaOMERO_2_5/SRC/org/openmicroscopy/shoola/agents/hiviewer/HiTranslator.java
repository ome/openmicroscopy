/*
 * org.openmicroscopy.shoola.agents.hiviewer.HiTranslator
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

package org.openmicroscopy.shoola.agents.hiviewer;


//Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.env.ui.ViewerSorter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.clsf.TreeCheckNode;
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
 * The tree is then displayed in the HiViewer. For example,
 * A list of Projects-Datasets-Images is passed to the 
 * {@link #transformProjects(Set)} method and transforms into a set of 
 * ImageSet-ImageSet-ImageNode.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class HiTranslator
{
    
    /** Message for unclassified images. */
    private static final String UNCLASSIFIED = "Wild at heart and free images.";
        
    /** 
     * The left element displayed before the number of items contained in a 
     * given container. 
     */
    private static final String LEFT = "[";
    
    /** 
     * The left element displayed before the number of items contained in a 
     * given container. 
     */
    private static final String RIGHT = "]";
    
    /** Helper class to sort elements. */
    private static ViewerSorter sorter = new ViewerSorter();
    
    /** 
     * Returns the first element of the specified set. 
     * Returns <code>null</code> if the set is empty or <code>null</code>.
     * 
     * @param set The set to analyse.
     * @return See above.
     */
    private static ImageDisplay getFirstElement(Set set)
    {
        if (set == null || set.size() == 0) return null;
        ImageDisplay display = null;
        Iterator i = set.iterator();
        while (i.hasNext()) {
            display = (ImageDisplay) i.next();
            break;  
        }
        return display;
    }
    
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
     * Transforms each {@link ImageData} object into a visualisation object
     * i.e. {@link ImageNode}.
     * Then adds the newly created {@link ImageNode} to the specified 
     * {@link ImageSet parent}. 
     * 
     * @param is The {@link ImageData} to transform.
     * @param parent The {@link ImageSet parent} of the image node.
     */
    private static void linkImageTo(ImageData is, ImageSet parent)
    {
        ThumbnailProvider provider = new ThumbnailProvider(is);
        ImageNode node = new ImageNode(is.getName(), is, provider);
        provider.setImageNode(node);
        formatToolTipFor(node, getFirstAnnotation(is.getAnnotations()));  
        parent.addChildDisplay(node);
    }
    
    /** 
     * Transforms each {@link ImageData} object contained in the specified
     * list into a visualisation object i.e. {@link ImageNode}.
     * Then adds the newly created {@link ImageNode} to the specified 
     * {@link ImageSet parent}. 
     * 
     * @param images Collection of {@link ImageData}s.
     * @param parent The {@link ImageSet} corresponding to the
     *              {@link DataObject} containing the images.
     */
    private static void linkImagesTo(Set images, ImageSet parent)
    {
        if (images == null || parent == null) return;
        Iterator i = images.iterator();
        while (i.hasNext()) 
            linkImageTo((ImageData) i.next(), parent);
    }
    
    /** 
     * Links the images contained into the specified {@link DataObject} 
     * to {@link ImageSet} corresponding to the {@link DataObject}.
     * 
     * @param uo Must be instance of {@link DatasetData} or 
     *          {@link CategoryData}.
     * @return  The corresponding {@link ImageDisplay} or <code>null</code>.
     */
    private static ImageDisplay linkImages(DataObject uo)
    {
        ImageSet node = null;
        Set images;
        if (uo instanceof DatasetData) {
            DatasetData ds = (DatasetData) uo;
            images = ds.getImages();
            String note = "";
            if (images != null) note = LEFT+images.size()+RIGHT;
            node = new ImageSet(ds.getName(), note, ds);
            formatToolTipFor(node, getFirstAnnotation(ds.getAnnotations()));
            linkImagesTo(images, node);
        } else if (uo instanceof CategoryData) {
            CategoryData data = (CategoryData) uo;
            String note = "";
            images = data.getImages();
            if (images != null) note = LEFT+images.size()+RIGHT;
            node = new ImageSet(data.getName(), note, data);
            formatToolTipFor(node, null);
            linkImagesTo(images, node);
        }
        return node;
    }

    /**
     * Transforms a Projects/Datasets/Images hierarchy into a visualisation
     * tree. 
     * 
     * @param projects  Collection of {@link ProjectData}s to transform.
     *                  Mustn't be <code>null</code>.
     * @return Collection of corresponding {@link ImageDisplay}s.
     */
    private static Set transformProjects(Set projects)
    {
        if (projects == null) 
            throw new IllegalArgumentException("No projects.");
        Set results = new HashSet(projects.size());
        Iterator i = projects.iterator(), j;
        //DataObject
        ProjectData ps;
        //Visualisation object.
        ImageSet project;  
        Set datasets;
        String note = "";
        while (i.hasNext()) {
            ps = (ProjectData) i.next();
            datasets = ps.getDatasets();
            if (datasets != null) note += LEFT+datasets.size()+RIGHT;
            project = new ImageSet(ps.getName(), note, ps);
            formatToolTipFor(project, null);
            if (datasets != null) {
                j = datasets.iterator();
                while (j.hasNext())
                    project.addChildDisplay(linkImages((DataObject) j.next()));
            }
            results.add(project);
        }
        return results;
    }
 
    /** 
     * Transforms the specified {@link DataObject} into its corresponding
     * visualisation element.
     * 
     * @param project   The {@link DataObject} to transform. 
     *                  Must be an instance of {@link ProjectData}.
     * @return See below.
     */
    private static Set transformProject(DataObject project)
    {
        Set set = new HashSet(1);
        set.add(project);
        return transformProjects(set);
    }
    
    /**
     * Transforms a Datasets/Images hierarchy into a visualisation
     * tree. 
     * 
     * @param datasets  Collection of {@link DatasetData}s to transform.
     *                  Mustn't be <code>null</code>.
     * @return Collection of corresponding {@link ImageDisplay}s.
     */
    private static Set transformDatasets(Set datasets)
    {
        if (datasets == null) 
            throw new IllegalArgumentException("No datasets.");
        Set results = new HashSet(datasets.size());
        Iterator i = datasets.iterator();
        while (i.hasNext()) //create datasetNode.
            results.add(linkImages((DataObject) i.next()));
        return results;
    }
    
    /** 
     * Transforms the specified {@link DataObject} into its corresponding
     * visualisation element.
     * 
     * @param dataset The {@link DataObject} to transform.
     *                Must be an instance of {@link DatasetData}.
     * @return See below.
     */
    private static Set transformDataset(DataObject dataset)
    {
        Set set = new HashSet(1);
        set.add(dataset);
        return transformDatasets(set);
    }
    
    /**
     * Transforms a CategoryGroup/Category/Images hierarchy into a visualisation
     * tree. 
     * 
     * @param groups  Collection of {@link CategoryGroupData}s to transform.
     *                Mustn't be <code>null</code>.
     * @return Collection of corresponding {@link ImageDisplay}s.
     */
    private static Set transformCategoryGroups(Set groups)
    {
        if (groups == null) throw new IllegalArgumentException("No groups.");
        Set results = new HashSet(groups.size());
        Iterator i = groups.iterator(), j;
        //DataObject
        CategoryGroupData  cgData;
        //Visualisation object.
        ImageSet group;  
        Set categories;
        String note = "";
        while (i.hasNext()) {
            cgData = (CategoryGroupData) i.next();
            categories = cgData.getCategories();
            if (categories != null) note = LEFT+categories.size()+RIGHT;
            group = new ImageSet(cgData.getName(), note, cgData);
            formatToolTipFor(group, null);
            
            if (categories != null) {
                j = categories.iterator();
                while (j.hasNext())
                    group.addChildDisplay(linkImages((DataObject) j.next()));
            }
            results.add(group); //add the group ImageSet 
        }
        return results;
    }
    
    /** 
     * Transforms the specified {@link CategoryGroupData} into its corresponding 
     * visualisation element.
     * 
     * @param data The {@link CategoryGroupData} to transform.
     * @return See below.
     */
    private static Set transformCategoryGroup(CategoryGroupData data)
    {
        Set set = new HashSet(1);
        set.add(data);
        return transformCategoryGroups(set);
    }
    
    /**
     * Transforms a Category/Images hierarchy into a visualisation tree. 
     * 
     * @param categories The collection of {@link CategoryData}s to transform.
     * @return Set of corresponding {@link ImageDisplay}s.
     */
    private static Set transformCategories(Set categories)
    {
        if (categories == null) 
            throw new IllegalArgumentException("No categories.");
        Set results = new HashSet(categories.size());
        Iterator i = categories.iterator();
        CategoryData data;
        ImageSet parent;
        Set images;
        String note = "";
        while (i.hasNext()) {
            data = (CategoryData) i.next();
            images = data.getImages();
            if (images != null) note = LEFT+images.size()+RIGHT;
            parent = new ImageSet(data.getName(), note, data);
            formatToolTipFor(parent, null);
            linkImagesTo(images, parent);
            results.add(parent);
        }
        return results;
    }
    
    /** 
     * Transforms the specified {@link CategoryData} into its corresponding 
     * visualisation element.
     * 
     * @param data The {@link CategoryData} to transform.
     * @return See below.
     */
    private static Set transformCategory(CategoryData data)
    {
        Set set = new HashSet(1);
        set.add(data);
        return transformCategories(set);
    }
    
    
    /**
     * Transforms a {@link CategoryData} into a visualisation object i.e.
     * a {@link TreeCheckNode}.
     * 
     * @param data  The {@link CategoryData} to transform.
     *              Mustn't be <code>null</code>.
     * @return See above.
     */
    private static TreeCheckNode transformCategoryPath(CategoryData data)
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
     * @param data  The {@link CategoryGroupData} to transform.
     *              Mustn't be <code>null</code>.
     * @return See above.
     */
    private static TreeCheckNode transformCategoryGroupPath(CategoryGroupData
                                                            data)
    {
        if (data == null)
            throw new IllegalArgumentException("Cannot be null");
        IconManager im = IconManager.getInstance();
        TreeCheckNode group = new TreeCheckNode(data, 
                                im.getIcon(IconManager.CATEGORY_GROUP), 
                                data.getName(), false);
        Set categories = data.getCategories();
        Iterator i = categories.iterator();
        while (i.hasNext())
            group.addChildDisplay(
                    transformCategoryPath((CategoryData) i.next()));
        return group;
    }
    
    /** 
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set can either be
     * {@link ProjectData}, {@link DatasetData}, {@link CategoryGroupData},
     * {@link CategoryData} or {@link ImageData}.
     * The {@link ImageData}s are added to an unclassified {@link ImageSet}.
     * 
     * @param dataObjects   The {@link DataObject}s to transform.
     *                      Mustn't be <code>null</code>.
     * @return See above.
     */
    public static Set transformHierarchy(Set dataObjects)
    {
        if (dataObjects == null)
            throw new IllegalArgumentException("No objects.");
        Set results = new HashSet(dataObjects.size());
        Iterator i = dataObjects.iterator();
        DataObject ho;
        ImageSet unclassified = null;
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (ho instanceof ProjectData)
                results.add(getFirstElement(transformProject(ho)));
            else if (ho instanceof DatasetData)
                results.add(getFirstElement(transformDataset(ho)));
            else if (ho instanceof CategoryGroupData)
                results.add(getFirstElement(
                        transformCategoryGroup((CategoryGroupData) ho)));
            else if (ho instanceof CategoryData)
                results.add(getFirstElement(
                        transformCategory((CategoryData) ho)));
            else if (ho instanceof ImageData) {
                if (unclassified == null) {
                    unclassified = new ImageSet(UNCLASSIFIED, new Object());
                    formatToolTipFor(unclassified, null);
                }
                linkImageTo((ImageData) ho, unclassified);
            }
        }
        if (unclassified != null) results.add(unclassified);
        return results;
    }
    
    /** 
     * Transforms a {@link DataObject} into its corresponding visualization
     * object. The object can either be
     * {@link ProjectData}, {@link DatasetData}, {@link CategoryGroupData},
     * {@link CategoryData} or {@link ImageData}.
     * The {@link ImageData}s are added to an unclassified {@link ImageSet}.
     * 
     * @param ho    The {@link DataObject} to transform.
     *              Mustn't be <code>null</code>.
     * @return See above.
     */
    public static Set transform(DataObject ho)
    {
        if (ho == null) throw new IllegalArgumentException("No objects.");
        Set s = new HashSet(1);
        s.add(ho);
        return transformHierarchy(s);
    }
     
    /**
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set can either be
     * {@link CategoryGroupData} or {@link CategoryData}.
     * 
     * @param paths The collection of {@link DataObject}s to transform.
     * @return A set of visualization objects.
     */
    public static Set transformClassificationPaths(Set paths)
    {
        if (paths == null)
            throw new IllegalArgumentException("No objects.");
        Set results = new HashSet(paths.size());
        Iterator i = paths.iterator();
        DataObject ho;
        while (i.hasNext()) {
            ho = (DataObject) i.next();
            if (ho instanceof CategoryGroupData) {
                Set categories = ((CategoryGroupData) ho).getCategories();
                if (categories != null && categories.size() != 0)
                    results.add(transformCategoryGroupPath(
                            (CategoryGroupData) ho));
            } else if (ho instanceof CategoryData)
                results.add(transformCategoryPath((CategoryData) ho));
        }
        return results;
    }
    
    /**
     * Formats the toolTip of the specified {@link ImageDisplay} node.
     * 
     * @param node The specified node. Mustn't be <code>null</code>.
     * @param data The annotation data.
     */
    public static void formatToolTipFor(ImageDisplay node, AnnotationData data)
    {
        if (node == null) throw new IllegalArgumentException("No node");
        String toolTip = "";
        if (data != null)
            toolTip = UIUtilities.makeParagraph(node.getTitle(), 
                    data.getText(), UIUtilities.TABLE_WIDTH);
        else toolTip = UIUtilities.formatToolTipText(node.getTitle());  
        node.getTitleBar().setToolTipText(toolTip);
    }
   
}

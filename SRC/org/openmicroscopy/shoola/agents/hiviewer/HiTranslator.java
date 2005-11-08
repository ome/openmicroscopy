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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
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
     * Returns the first element in the specified set. 
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
     * Returns the first annotation in the specified set.
     * Returns <code>null</code> if the set is empty or <code>null</code>.
     * 
     * @param set The set to analyse.
     * @return See above.
     */
    private static AnnotationData getFirstAnnotation(Set set)
    {
        if (set == null || set.size() == 0) return null;
        AnnotationData ad = null;
        Iterator i = set.iterator();
        while (i.hasNext()) {
            ad = (AnnotationData) i.next();
            break;  
        }
        return ad;
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
        ImageNode node = new ImageNode(is.getName(), is, 
                new ThumbnailProvider(is));
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
        if (uo instanceof DatasetData) {
            DatasetData ds = (DatasetData) uo;
            node = new ImageSet(ds.getName(), ds);
            formatToolTipFor(node, getFirstAnnotation(ds.getAnnotations()));
            linkImagesTo(ds.getImages(), node);
        } else if (uo instanceof CategoryData) {
            CategoryData data = (CategoryData) uo;
            node = new ImageSet(data.getName(), data);
            formatToolTipFor(node, null);
            linkImagesTo(data.getImages(), node);
        }
        return node;
    }

    /**
     * Transforms a Projects/Datasets/Images hierarchy into a visualisation
     * tree. 
     * 
     * @param projects  Collection of {@link ProjectData}s to transform.
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
        while (i.hasNext()) {
            ps = (ProjectData) i.next();
            project = new ImageSet(ps.getName(), ps);
            formatToolTipFor(project, null);
            datasets = ps.getDatasets();
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
     * @param project The {@link DataObject} to transform. 
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
        while (i.hasNext()) {
            cgData = (CategoryGroupData) i.next();
            group = new ImageSet(cgData.getName(), cgData);
            formatToolTipFor(group, null);
            categories = cgData.getCategories();
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
        while (i.hasNext()) {
            data = (CategoryData) i.next();
            parent = new ImageSet(data.getName(), data);
            formatToolTipFor(parent, null);
            linkImagesTo(data.getImages(), parent);
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
     * Transforms a set of {@link DataObject}s into their corresponding 
     * visualization objects. The elements of the set can either be
     * {@link ProjectData}, {@link DatasetData}, {@link CategoryGroupData},
     * {@link CategoryData} or {@link ImageData}.
     * The {@link ImageData}s are added to an unclassified {@link ImageSet}.
     * 
     * @param dataObjects The {@link DataObject}s to transform.
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
     * @param ho The {@link DataObject} to transform.
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
     * Formats the toolTip of the specified node.
     * 
     * @param node The specified node.
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

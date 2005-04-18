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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.env.data.model.CategoryData;
import org.openmicroscopy.shoola.env.data.model.CategoryGroupData;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.DatasetSummaryLinked;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;

/** 
 * This class contains a collection of utility static methods that transform
 * an hierarchy of {@link DataObject}s into a visualisation tree.
 * The tree is then displayed in the HiViewer. For example,
 * A list of Projects-Datasets-Images is passed to the 
 * {@link #translateProjects(List)} method and transforms into a list of 
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
    
    /** 
     * Transforms each {@link ImageSummary} object contained in the specified
     * list into a visualisation object i.e. {@link ImageNode}.
     * Then adds the newly create {@link ImageNode} to the specified 
     * {@link ImageSet parent}. 
     * 
     * @param   List of ImageSummary.
     * @param   Visualisation object corresponding to the DataObject containing
     *          the images.
     */
    private static void linkImagesToContainer(List images, ImageSet parent)
    {
        if (images == null || parent == null) return;
        Iterator i = images.iterator();
        //DataObject
        ImageSummary is;
        //Visualisation object
        ImageNode node;
        while (i.hasNext()) {
            is = (ImageSummary) i.next();
            node = new ImageNode(is.getName(), is, new ThumbnailProvider(is));
            parent.addChildDisplay(node);
        }
    }
    
    /** 
     * Link the images contained into the specified {@link DataObject} 
     * to the specified visualisation object corresponding to the DataObject.
     * 
     * @param uo        Must be instance of <code>DatasetSummaryLinked</code> or
     *                  <code>DatasetData</code>.
     * @param parent    Parent of the new created visualization element. 
     *                  If parent is <code>null</code>, 
     *                  an ImageSet without parent is created.
     * @return  The visualisation element corresponding to the 
     *              {@link DataObject}.
     */
    private static ImageDisplay linkImages(DataObject uo)
    {
        ImageSet node = null;
        if (uo instanceof DatasetSummaryLinked) {
            DatasetSummaryLinked ds = (DatasetSummaryLinked) uo;
            node = new ImageSet(ds.getName(), ds);
            linkImagesToContainer(ds.getImages(), node);
        }
        return node;
    }
    
    /**
     * Transforms a Projects/Datasets/Images hierarchy into a visualisation
     * tree. 
     * 
     * @param projects  List of projets to transform.
     * @return List of corresponding ImageSet.
     */
    public static Set transformProjects(List projects)
    {
        if (projects == null) throw new NullPointerException("No projects.");
        Set results = new HashSet();
        Iterator i = projects.iterator(), j;
        //DataObject
        ProjectSummary ps;
        //Visualisation object.
        ImageSet project;  
        List datasets;
        while (i.hasNext()) {
            ps = (ProjectSummary) i.next();
            project = new ImageSet(ps.getName(), ps);
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
     * Transforms the specified dataObject into its corresponding visualisation
     * element
     * 
     * @param project   DataObject to transform. Must be an instance of 
     *                  <code>ProjectSummary</code> or <code>ProjectData</code>.
     * @return See below.
     */
    public static Set transformProject(DataObject project)
    {
        if (project == null) throw new NullPointerException("No project.");
        if (!(project instanceof ProjectSummary))
            throw new IllegalArgumentException("No proper DataObject type.");
        List l = new ArrayList();
        l.add(project);
        return transformProjects(l);
    }
    
    /**
     * Transforms a Datasets/Images hierarchy into a visualisation
     * tree. 
     * 
     * @param projects  List of datasets to transform.
     * @return List of corresponding ImageSet.
     */
    public static Set transformDatasets(List datasets)
    {
        if (datasets == null) throw new NullPointerException("No datasets.");
        Set results = new HashSet();
        Iterator i = datasets.iterator();
        while (i.hasNext()) //create datasetNode.
            results.add(linkImages((DataObject) i.next()));
        return results;
    }
    
    /** 
     * Transforms the specified dataObject into its corresponding visualisation
     * element
     * 
     * @param dataset   DataObject to transform. Must be an instance of 
     *                  <code>DatasetSummaryLinked</code> or 
     *                  <code>DatasetData</code>.
     * @return See below.
     */
    public static Set transformDataset(DataObject dataset)
    {
        if (dataset == null) throw new NullPointerException("No dataset.");
        if (!(dataset instanceof DatasetSummaryLinked))
            throw new IllegalArgumentException("No proper DataObject type.");
        List l = new ArrayList();
        l.add(dataset);
        return transformDatasets(l);
    }
    
    /**
     * Transforms a CategoryGroup/Category/Images hierarchy into a visualisation
     * tree. 
     * 
     * @param groups  List of groups to transform.
     * @return List of corresponding ImageSet.
     */
    public static Set transformCategoryGroups(List groups)
    {
        if (groups == null) throw new NullPointerException("No groups.");
        Set results = new HashSet();
        Iterator i = groups.iterator(), j;
        //DataObject
        CategoryGroupData  cgData;
        CategoryData cData;
        //Visualisation object.
        ImageSet group, category;  
        List categories;
        while (i.hasNext()) {
            cgData = (CategoryGroupData) i.next();
            group = new ImageSet(cgData.getName(), cgData);
            categories = cgData.getCategories();
            if (categories != null) {
                j = categories.iterator();
                while (j.hasNext()) {
                    cData = (CategoryData) j.next();
                    category = new ImageSet(cData.getName(), cData);
                    group.addChildDisplay(category);
                    linkImagesToContainer(cData.getImages(), category);
                }
            }
            results.add(group); //add the group ImageSet 
        }
        return results;
    }
    
    /** 
     * Transforms the specified CategoryGroupData into its corresponding 
     * visualisation element
     * 
     * @param data      CategoryGroupData to transform.
     * @return See below.
     */
    public static Set transformCategoryGroup(CategoryGroupData data)
    {
        if (data == null) throw new NullPointerException("No dataset.");
        List l = new ArrayList();
        l.add(data);
        return transformCategoryGroups(l);
    }
    
    /**
     * Transforms a Category/Images hierarchy into a visualisation
     * tree. 
     * 
     * @param groups  List of categories to transform.
     * @return List of corresponding ImageSet.
     */
    public static Set transformCategories(List categories)
    {
        if (categories == null) 
            throw new NullPointerException("No categories.");
        Set results = new HashSet();
        Iterator i = categories.iterator();
        CategoryData data;
        ImageSet parent;
        while (i.hasNext()) {
            data = (CategoryData) i.next();
            parent = new ImageSet(data.getName(), data);
            linkImagesToContainer(data.getImages(), parent);
            results.add(parent);
        }
        return results;
    }
    
    /** 
     * Transforms the specified CategoryData into its corresponding 
     * visualisation element
     * 
     * @param data      CategoryData to transform.
     * @return See below.
     */
    public static Set transformCategory(CategoryData data)
    {
        if (data == null) throw new NullPointerException("No dataset.");
        List l = new ArrayList();
        l.add(data);
        return transformCategories(l);
    }
    
}

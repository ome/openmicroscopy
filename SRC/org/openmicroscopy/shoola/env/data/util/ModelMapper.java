/*
 * org.openmicroscopy.shoola.env.data.util.ModelMapper
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

package org.openmicroscopy.shoola.env.data.util;


//Java imports

//Third-party libraries

//Application-internal dependencies
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ome.model.IObject;
import ome.model.annotations.DatasetAnnotation;
import ome.model.annotations.ImageAnnotation;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.CategoryGroupCategoryLink;
import ome.model.containers.CategoryImageLink;
import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.util.Filter;
import ome.util.Filterable;
import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.ProjectData;

/** 
 * Helper class to map {@link DataObject}s into their corresponding
 * {@link IObject}s.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ModelMapper
{
    /*
    private static class CollectionUnloader implements Filter
    {
        public Filterable filter(String arg0, Filterable arg1){return arg1;}
        public Collection filter(String arg0, Collection arg1){return null;}
        public Map filter(String arg0, Map arg1){return arg1;}
        public Object filter(String arg0, Object arg1){return arg1;}
    }
    
    static Filter unloader = new CollectionUnloader();
    
    public static void unloadCollections(IObject obj)
    {
        obj.acceptFilter( unloader );
    }
    */
    
    /**
     * Links the newly created {@link IObject child} to its
     * {@link IObject parent}.
     * 
     * @param child The newly created child. 
     * @param parent The parent of the newly created child.
     */
    public static void linkParentToChild(IObject child, IObject parent)
    {
        if (parent == null) return;
        if (child == null) throw new IllegalArgumentException("Child cannot" +
                                "be null.");
        if (parent instanceof Project) {
            if (!(child instanceof Dataset))
                throw new IllegalArgumentException("Child not valid.");
            Project p = (Project) parent;
            Dataset d = (Dataset) child;
            ProjectDatasetLink link;
            Iterator it = d.iterateProjectLinks();
            while (it.hasNext()) {
                link = (ProjectDatasetLink) it.next();
                if (p.getId().equals(link.parent().getId()))
                    p.addProjectDatasetLink(link, false);
            }
        } else if (parent instanceof CategoryGroup) {
            if (!(child instanceof Category))
                throw new IllegalArgumentException("Child not valid.");
            CategoryGroup p = (CategoryGroup) parent;
            Category d = (Category) child;
            CategoryGroupCategoryLink link;
            Iterator it = d.iterateCategoryGroupLinks();
            while (it.hasNext()) {
                link = (CategoryGroupCategoryLink) it.next();
                if (p.getId().equals(link.parent().getId()))
                    p.addCategoryGroupCategoryLink(link, false);
            }
        } else if (parent instanceof Dataset) {
            if (!(child instanceof Image))
                throw new IllegalArgumentException("Child not valid.");
            Dataset p = (Dataset) parent;
            Image d = (Image) child;
            DatasetImageLink link;
            Iterator it = d.iterateDatasetLinks();
            while (it.hasNext()) {
                link = (DatasetImageLink) it.next();
                if (p.getId().equals(link.parent().getId()))
                    p.addDatasetImageLink(link, false);
            }
        } else if (parent instanceof Category) {
            if (!(child instanceof Image))
                throw new IllegalArgumentException("Child not valid.");
            Category p = (Category) parent;
            Image d = (Image) child;
            CategoryImageLink link;
            Iterator it = d.iterateCategoryLinks();
            while (it.hasNext()) {
                link = (CategoryImageLink) it.next();
                if (p.getId().equals(link.parent().getId()))
                    p.addCategoryImageLink(link, false);
            }
        }
    }
    
    /**
     * Converts the specified <code>DataObject</code> into its corresponding 
     * <code>IObject</code>.
     * 
     * @param child     The child to create.
     * @param parent    The child's parent.
     * @return The {@link IObject} to create.
     */
    public static IObject createIObject(DataObject child, DataObject parent)
    {
        if (child instanceof ProjectData) {
            ProjectData data = (ProjectData) child;
            Project model = new Project();
            model.setName(data.getName());
            model.setDescription(data.getDescription());
            return model;
        } else if (child instanceof CategoryGroupData) {
            CategoryGroupData data = (CategoryGroupData) child;
            CategoryGroup model = new CategoryGroup();
            model.setName(data.getName());
            model.setDescription(data.getDescription());
            return model;
        } else if (child instanceof DatasetData) {
            if (!(parent instanceof ProjectData)) 
                throw new IllegalArgumentException("Parent not valid.");
            DatasetData data = (DatasetData) child;
            Dataset model = new Dataset();
            model.setName(data.getName());
            model.setDescription(data.getDescription());
            model.linkProject(new Project(new Long(parent.getId()), false));
            //model.linkProject(parent.asProject());
            return model;
        } else if (child instanceof CategoryData) {
            if (!(parent instanceof CategoryGroupData)) 
                throw new IllegalArgumentException("Parent not valid.");
            CategoryData data = (CategoryData) child;
            Category model = new Category();
            model.setName(data.getName());
            model.setDescription(data.getDescription());
            model.linkCategoryGroup(new CategoryGroup(new Long(parent.getId()), 
                                                    false));
            return model;
        } else if (child instanceof ImageData) {
            if (!(parent instanceof CategoryData) && 
                !(parent instanceof DatasetData))
                throw new IllegalArgumentException("Parent not valid.");
            ImageData data = (ImageData) child;
            Image model = new Image();
            model.setName(data.getName());
            model.setDescription(data.getDescription());
            if (parent instanceof CategoryData) 
                model.linkCategory(new Category(new Long(parent.getId()), 
                                                false));
            else if (parent instanceof DatasetData) 
                model.linkDataset(new Dataset(new Long(parent.getId()), 
                                            false));
            return model; 
        }
        throw new IllegalArgumentException("DataObject not supported.");
    }
    
    /**
     * Unlinks the specified child and the parent and returns the 
     * updated child <code>IObject</code>.
     * 
     * @param child     The child to remove.
     * @param parent    The parent of the child.
     * @return See above. 
     */
    public static IObject removeIObject(IObject child, IObject parent)
    {
        if ((child instanceof Dataset) && (parent instanceof Project)) {
            Dataset mChild = (Dataset) child;
            Project mParent = (Project) parent;
            mChild.unlinkProject(mParent);
            return mChild;
        } else if ((child instanceof Category) && 
                (parent instanceof CategoryGroup)) {
            Category mChild = (Category) child;
            CategoryGroup mParent = (CategoryGroup) parent;;
            mChild.unlinkCategoryGroup(mParent);
            return mChild;
        } else if ((child instanceof Image) && (parent instanceof Dataset)) {
            Image mChild = (Image) child;
            Dataset mParent = (Dataset) parent;
            mChild.unlinkDataset(mParent);
            return mChild;
        } else if ((child instanceof Image) && (parent instanceof Category)) {
            Image mChild = (Image) child;
            Category mParent = (Category) parent;
            mChild.unlinkCategory(mParent);
            return mChild;
        }
        throw new IllegalArgumentException("DataObject not supported.");
    }
   
    /**
     * Creates a new annotation <code>IObject</code>.
     * 
     * @param annotatedObject   The <code>DataObject</code> to annotate.
     *                          Can either be a <code>DatasetData</code>
     *                          or a <code>ImageData</code>. Mustn't be
     *                          <code>null</code>.
     * @param data              The annotation to create.
     * @return See above.
     */
    public static IObject createAnnotation(IObject annotatedObject,
                                    AnnotationData data)
    {
        if (annotatedObject instanceof Dataset) {
            Dataset m = (Dataset) annotatedObject; 
            DatasetAnnotation annotation = new DatasetAnnotation();
            annotation.setContent(data.getText());
            annotation.setDataset(m);
            return annotation;
        } else if (annotatedObject instanceof ImageData) {
            Image m = new Image(annotatedObject.getId(), false);
            ImageAnnotation annotation = new ImageAnnotation();
            annotation.setContent(data.getText());
            annotation.setImage(m);
            return annotation;
        }
        throw new IllegalArgumentException("DataObject cannot be annotated.");
    }
    
    /**
     * 
     * @param annotated
     * @param annotation
     */
    public static void setAnnotatedObject(IObject annotated, 
            IObject annotation)  
    {
        if (annotation instanceof ImageAnnotation)
            ((ImageAnnotation) annotation).setImage((Image) annotated);
        if (annotation instanceof DatasetAnnotation)
            ((DatasetAnnotation) annotation).setDataset((Dataset) annotated);
    }
    
    /**
     * Returns the annotated IObject related to the specified annotation.
     * 
     * @param annotation    The annotation.
     * @return  See above.
     */
    public static IObject getAnnotatedObject(IObject annotation)
    {
        if (annotation instanceof ImageAnnotation)
            return ((ImageAnnotation) annotation).getImage();
        else if (annotation instanceof DatasetAnnotation)
            return ((DatasetAnnotation) annotation).getDataset();
        throw new IllegalArgumentException("Annotation can only be " +
                "DatasetAnnoation or ImageAnnotation.");
    }
    
}

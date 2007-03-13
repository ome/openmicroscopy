/*
 * org.openmicroscopy.shoola.env.data.util.ModelMapper
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

package org.openmicroscopy.shoola.env.data.util;


//Java imports
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.model.ILink;
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
import ome.model.meta.Experimenter;
import ome.util.Filter;
import ome.util.Filterable;
import pojos.AnnotationData;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
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
   
    /**
     * Helper field used to unlink an <code>IObject</code> and its related
     * collections.
     */
    private static Filter unloader = new CollectionUnloader();
    
    /**
     *  Utility inner class to unload all collections linked to a given 
     *  <code>IObject</code> e.g. a {@link Project} linked to its
     *  {@link Dataset}s.
     */
    private static class CollectionUnloader
        implements Filter
    {
    	
        /** 
         * Implemented as specified by the {@link Filter} I/F.
         * @see Filter#filter(String, Filterable)
         */
        public Filterable filter(String arg0, Filterable arg1) { return arg1; }
        
        /** 
         * Implemented as specified by the {@link Filter} I/F.
         * @see Filter#filter(String, Collection)
         */
        public Collection filter(String arg0, Collection arg1) { return null; }
        
        /** 
         * Implemented as specified by the {@link Filter} I/F.
         * @see Filter#filter(String, Map)
         */
        public Map filter(String arg0, Map arg1) { return arg1; }
        
        /** 
         * Implemented as specified by the {@link Filter} I/F.
         * @see Filter#filter(String, Object)
         */
        public Object filter(String arg0, Object arg1) { return arg1; }
        
    }
    
    /**
     * Unlinks the collections linked to the specified {@link IObject}.
     * 
     * @param object The object.
     */
    public static void unloadCollections(IObject object)
    {
        if (object == null)
            throw new IllegalArgumentException("The object mustn't be null.");
        object.acceptFilter(unloader);
    }

    /**
     * Unlinks the specified child from its parent and returns the link to 
     * remove.
     * 
     * @param child     The child to unlink.
     * @param parent    The child's parent.
     * @return See above
     */
    public static IObject unlinkChildFromParent(IObject child, IObject parent)
    {
        if (parent instanceof Dataset) {
            if (!(child instanceof Image))
                throw new IllegalArgumentException("Child not valid.");
            List links = ((Image) child).collectDatasetLinks(null);
            Iterator i = links.iterator();
            DatasetImageLink link = null;
            while (i.hasNext()) {
                link = (DatasetImageLink) i.next();
                if (link.getParent().getId().longValue() == 
                    parent.getId().longValue()) break;  
            }
            return link;
        } else if (parent instanceof Category) {
            if (!(child instanceof Image))
                throw new IllegalArgumentException("Child not valid.");
            List links = ((Image) child).collectCategoryLinks(null);
            Iterator i = links.iterator();
            CategoryImageLink link = null;
            while (i.hasNext()) {
                link = (CategoryImageLink) i.next();
                if (link.getParent().getId().longValue() == 
                    parent.getId().longValue()) break;  
            }
            return link;
        } else if (parent instanceof Project) {
            if (!(child instanceof Dataset))
                throw new IllegalArgumentException("Child not valid.");
            List links = ((Project) parent).collectDatasetLinks(null);
            Iterator i = links.iterator();
            ProjectDatasetLink link = null;
            long childID = child.getId().longValue();
            while (i.hasNext()) {
                link = (ProjectDatasetLink) i.next();
                if (link.getChild().getId().longValue() == childID) {
                    return link;  
                }
            }
            //return link;
        } else if (parent instanceof CategoryGroup) {
            if (!(child instanceof Category))
                throw new IllegalArgumentException("Child not valid.");
            List links = ((CategoryGroup) parent).collectCategoryLinks(null);
            Iterator i = links.iterator();
            CategoryGroupCategoryLink link = null;
            while (i.hasNext()) {
                link = (CategoryGroupCategoryLink) i.next();
                if (link.getParent().getId().longValue() == 
                    parent.getId().longValue()) break;  
            }
            return link;
        } throw new IllegalArgumentException("Parent not supported.");
    }

    /**
     * Links the  {@link IObject child} to its {@link IObject parent}.
     * 
     * @param child     The child. 
     * @param parent    The parent.
     * @return The link.
     */
    public static ILink linkParentToChild(IObject child, IObject parent)
    {
        if (parent == null) return null;
        if (child == null) throw new IllegalArgumentException("Child cannot" +
                                "be null.");
        if (parent instanceof Project) {
            if (!(child instanceof Dataset))
                throw new IllegalArgumentException("Child not valid.");
            Project unloadedProject = new Project(parent.getId(), false);
            Dataset unloadedDataset = new Dataset(child.getId(), false);
            ProjectDatasetLink l = new ProjectDatasetLink();
            l.link(unloadedProject, unloadedDataset);
            return l;
        } else if (parent instanceof CategoryGroup) {
            if (!(child instanceof Category))
                throw new IllegalArgumentException("Child not valid.");
            CategoryGroup unloadedGroup = new CategoryGroup(parent.getId(), 
                                                            false);
            Category unloadedCategory = new Category(child.getId(), false);
            
            CategoryGroupCategoryLink l = new CategoryGroupCategoryLink();
            l.link(unloadedGroup, unloadedCategory);
            return l;
        } else if (parent instanceof Dataset) {
            if (!(child instanceof Image))
                throw new IllegalArgumentException("Child not valid.");
            Dataset unloadedDataset = new Dataset(parent.getId(), false);
            Image unloadedImage = new Image(child.getId(), false);
            
            DatasetImageLink l = new DatasetImageLink();
            l.link(unloadedDataset, unloadedImage);
            return l;
        } else if (parent instanceof Dataset) {
            if (!(child instanceof Image))
                throw new IllegalArgumentException("Child not valid.");
            Dataset unloadedDataset = new Dataset(parent.getId(), false);
            Image unloadedImage = new Image(child.getId(), false);
            
            DatasetImageLink l = new DatasetImageLink();
            l.link(unloadedDataset, unloadedImage);
            return l;
        } else if (parent instanceof Category) {
            if (!(child instanceof Image))
                throw new IllegalArgumentException("Child not valid.");
            Category unloadedCategory = new Category(parent.getId(), false);
            Image unloadedImage = new Image(child.getId(), false);
            
            CategoryImageLink l = new CategoryImageLink();
            l.link(unloadedCategory, unloadedImage);
            return l;
        }
        return null;
    }
    
    /**
     * Links the newly created {@link IObject child} to its
     * {@link IObject parent}. This method should only be invoked to add a 
     * newly created child.
     * 
     * @param child     The newly created child. 
     * @param parent    The parent of the newly created child.
     */
    public static void linkParentToNewChild(IObject child, IObject parent)
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
        } else
            throw new IllegalArgumentException("DataObject not supported.");
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
        } else 
            throw new IllegalArgumentException("Child and parent are not" +
                    " compatible.");
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
            Set s = mParent.findProjectDatasetLink(mChild);
            Iterator i = s.iterator();
            while (i.hasNext()) { 
                mParent.removeProjectDatasetLink( 
                        (ProjectDatasetLink) i.next(), false);
            }
            return mParent;
        } else if ((child instanceof Category) && 
                (parent instanceof CategoryGroup)) {
            Category mChild = (Category) child;
            CategoryGroup mParent = (CategoryGroup) parent;
            Set s = mParent.findCategoryGroupCategoryLink(mChild);
            Iterator i = s.iterator();
            while (i.hasNext()) { 
                mParent.removeCategoryGroupCategoryLink( 
                        (CategoryGroupCategoryLink) i.next(), false);
            }
            return mParent;
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
        } else if (annotatedObject instanceof Image) {
            Image m = (Image) annotatedObject; 
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
    
    /**
     * Fills the new IObject with data from the old one.
     * 
     * @param oldObject	The old object.
     * @param newObject	The object to fill.
     */
    public static void fillIObject(IObject oldObject, IObject newObject)
    {
    	if (oldObject == null || newObject == null)
    		throw new IllegalArgumentException("Object cannot be NULL.");
    	if (oldObject.getClass() != newObject.getClass())
    		throw new IllegalArgumentException("Objects should be of the " +
    				"same type.");
    	if (oldObject instanceof Project) {
    		Project n = (Project) newObject;
    		Project o = (Project) oldObject;
    		n.setName(o.getName());
    		n.setDescription(o.getDescription());
    	} else if (oldObject instanceof Dataset) {
    		Dataset n = (Dataset) newObject;
    		Dataset o = (Dataset) oldObject;
    		n.setName(o.getName());
    		n.setDescription(o.getDescription());
    	} else if (oldObject instanceof Image) {
    		Image n = (Image) newObject;
    		Image o = (Image) oldObject;
    		n.setName(o.getName());
    		n.setDescription(o.getDescription());
    	} else if (oldObject instanceof CategoryGroup) {
    		CategoryGroup n = (CategoryGroup) newObject;
    		CategoryGroup o = (CategoryGroup) oldObject;
    		n.setName(o.getName());
    		n.setDescription(o.getDescription());
    	} else if (oldObject instanceof Category) {
    		Category n = (Category) newObject;
    		Category o = (Category) oldObject;
    		n.setName(o.getName());
    		n.setDescription(o.getDescription());
    	} else if (oldObject instanceof ImageAnnotation) {
    		ImageAnnotation n = (ImageAnnotation) newObject;
    		ImageAnnotation o = (ImageAnnotation) oldObject;
    		n.setContent(o.getContent());
    		n.setImage(o.getImage()); 
    	} else if (oldObject instanceof DatasetAnnotation) {
    		DatasetAnnotation n = (DatasetAnnotation) newObject;
    		DatasetAnnotation o = (DatasetAnnotation) oldObject;
    		n.setContent(o.getContent());
    		n.setDataset(o.getDataset()); 
    	} else if (oldObject instanceof Experimenter) {
    		Experimenter n = (Experimenter) newObject;
    		Experimenter o = (Experimenter) oldObject;
    		n.setEmail(o.getEmail());
    		n.setFirstName(o.getFirstName());
    		n.setLastName(o.getLastName());
    		n.setInstitution(o.getInstitution());
    		//n.setDefaultGroup(o.getDefaultGroup());
    	}
    }
    
}
